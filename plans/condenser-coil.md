# Condenser Coil

**Прогресс:** 26/28 ⏳

Змеевик — блок, перегоняющий жидкость из `create:fluid_tank`, который стоит над
нагревательным элементом. Паровая ось (вход/выход паров и дистиллята) всегда
горизонтальная (X или Z), перпендикулярная ей ось — труба с хладагентом
(пресной водой), сквозь которую змеевик пропускает поток. Паровая ось не
входит в трубную сеть Create: жидкость по ней не течёт, насос не нужен, вход
и выход — логические концы змеевика.

```
[источник нагрева] → [create:fluid_tank] → логическая связь → [condenser_coil] → [труба с result]
                                       ↑
                          (через ось трубы Create, физически)
```

## Терминология

| Поле | Тип | Смысл |
|------|-----|-------|
| `input` | Either `{ "id": "<fluid>" }` или `{ "tag": "<fluid_tag>" }` | Что должно быть в нагреваемом баке |
| `temperature_range` | `[min, max]` в °C, закрытый интервал | Допустимый диапазон температуры под баком; вне него процесс ставится на паузу |
| `result` | `{ "id": "<fluid>" }` | Что выходит из змеевика через выходную грань |
| `residue` | `{ "id": "<fluid>" }` | Что остаётся в баке по завершении |
| `result_percent` | int 0..100 | Доля объёма, превращающаяся в `result` (`target = total * result_percent / 100`) |
| `rate` | float | mB/тик — скорость обработки |

Устаревшие термины из прошлой версии плана — `distillage`, `stillage`,
`stillage_percent`, `temperature` — больше не используются; заменены
соответственно на `residue`, `residue`, `result_percent`, `temperature_range`.

## Blockstate

| Свойство | Тип | Допустимые значения | Назначение |
|----------|-----|---------------------|------------|
| `AXIS` | `EnumProperty<Direction.Axis>` | `X`, `Z` | Горизонтальная ось, вдоль которой идут пары (вход/выход). Y исключена сознательно — вертикальный змеевик в этой схеме не нужен. |
| `WATER_VERTICAL` | `BooleanProperty` | `true`/`false` | Если `true` — водяная ось вертикальная (Y); иначе — вторая горизонтальная (перпендикулярная `AXIS`). |

Итого 4 варианта ориентации (`AXIS × WATER_VERTICAL`). Цикл поворота ключом:
сначала переключается `WATER_VERTICAL`, при повторном цикле меняется `AXIS` и
`WATER_VERTICAL` сбрасывается в `false`. Ось воды вычисляется:

```text
waterAxis = WATER_VERTICAL ? Y : (AXIS == Z ? X : Z)
```

## Разделение осей — `canHaveFlowToward`

Змеевик хостит собственный `FluidTransportBehaviour`, у которого
`canHaveFlowToward(state, face)` возвращает `true` **только для двух граней
водяной оси**. Для Create он выглядит как обычная прямая труба вдоль этой
оси — вода течёт сквозь него штатной логикой Create, своего кода потока
писать не нужно.

- Вход паров (одна грань паровой оси) — **не** жидкостное соединение. Змеевик
  просто идёт по линии блоков от этой грани и ищет `create:fluid_tank`.
- Выход `result` (противоположная грань) — змеевик выставляет
  `Capabilities.FluidHandler.BLOCK` только на эту грань (внутренний бак
  результата, extract-only). Соседняя труба в `PipeConnection.determineSource`
  сначала проверит `FluidPropagator.isOpenEnd` — там
  `pipe.canHaveFlowToward(state, opposite)` вернёт `false` — провалится в
  `hasFluidCapability` и увидит нас как `FlowSource.FluidHandler`.
- Вход/выход паров **взаимозаменяемы**: входом считается та грань паровой
  оси, с которой нашёлся нагреваемый бак; выходом — противоположная.
  Отдельного blockstate-свойства для этого не нужно.

## Состояния змеевика

`IDLE → WARMUP → RUNNING → IDLE`. `PAUSED` — не отдельное состояние, а пропуск
тика: прогресс и блокировка бака сохраняются, ни `produced`, ни `progress` не
обнуляются.

| Состояние | Что происходит | Переход |
|-----------|----------------|---------|
| `IDLE` | Начальное. Каждый тик — попытка войти в `WARMUP`, если связка валидна и в баке есть жидкость под какой-нибудь `distillation`-рецепт. | → `WARMUP` при совпадении условий |
| `WARMUP` | Счётчик `distillationWarmupTicks` (200 = 10 с). Любое изменение объёма в баке сбрасывает счётчик на полное значение — это и есть «можно долить». | → `RUNNING` по истечении счётчика; → `IDLE` если связка сломалась |
| `RUNNING` | Снят снапшот `total_volume`, бак заблокирован, `target_volume = total_volume * result_percent / 100`, `produced = 0f`. Каждый тик: `progress += rate; int whole = (int) progress; progress -= whole` — сливаем `whole` mB из бака, столько же кладём во внутренний бак результата. | → `IDLE` при `produced >= target_volume`: содержимое бака заменяется на `residue` объёмом `total_volume - target_volume`, unlock, сброс давления |
| пропуск тика | Не состояние, а ситуация в `RUNNING`: температура вне `temperature_range`, нет протока хладагента, или внутренний бак результата полон. Прогресс и блокировка сохраняются. | следующий тик снова проверка условий |

## Снапшот-арифметика

```
snapshotVolume = totalVolume на момент старта RUNNING
targetVolume   = snapshotVolume * result_percent / 100
residueVol     = snapshotVolume - targetVolume
```

Пример из плана: `snapshotVolume = 1000`, `result_percent = 41` →
`targetVolume = 410`, `residueVol = 590`. В конце процесса бак осушается
полностью и заливается `590 mB residue` — независимо от того, оставалось ли
там что-то ещё (долив сверх снапшота в `RUNNING` невозможен из-за
блокировки).

Сам `result` копится во внутреннем баке результата ёмкостью `4000 mB` и
расходуется линией Create (труба → конечный бак). Доставка в result-face
идёт **прямым `IFluidHandler.fill`** через
`CondenserCoilBlockEntity.ejectThroughResultFace`, затем — если сосед
Create-труба — BFS-обход `pushAlongPipeNetwork` кладёт жидкость в
`IFluidHandler.BLOCK` каждой трубы по пути и в первый бак (output).

**BFS вызывается всегда, когда сосед — pipe.** Первая реализация
делала `handler.fill` в соседа и при `handler == null` ранний return:
для трубы `IFluidHandler.BLOCK` всегда `null` (жидкость в трубе живёт
в `PipeConnection.flow`, не в tank'е), и BFS никогда не запускался.
Сейчас `ejectThroughResultFace` отдельно проверяет
`FluidPropagator.getPipe(level, neighborPos) != null` и вызывает BFS
для pipe-соседа в любом случае — независимо от того, есть ли у него
IFluidHandler. Это разделяет две вещи: «куда положить жидкость в первом
блоке» (через `handler.fill`, если есть) и «пройти по цепочке до
конечного бака» (через BFS).

**Почему не давление**: `addPressure` требует открытого
`canHaveFlowToward` на result-face coil'а (для PipeConnection на
neighbour-трубе), а это вызывает cross-flow с водяной осью через
общий `interfaces: Map<Direction, PipeConnection>`. Давление на
neighbour-трубе не работает потому, что `FluidPipeBlock.canConnectTo`
проверяет `transport.canHaveFlowToward` у coil'а и возвращает false.
BFS+fill обходит это — мы работаем через capability-интерфейс, а
не через pipe-граф Create.

**Известное cosmetic-ограничение**: раз `canHaveFlowToward` на
result-face закрыт, соседняя труба визуально не подключается к coil'у
(`PROPERTY_BY_DIRECTION` на стороне трубы, смотрящей на coil, остаётся
`false`). BFS работает независимо от того, есть ли pipe-соединение —
жидкость всё равно доходит до output-бака. Это видно только в
outliner'е Create (рамка «connection» на стыке не рисуется); сам факт
доставки дистиллята от этого не страдает. Чтобы вернуть визуальное
подключение, нужно открыть `canHaveFlowToward` для result-face +
поставить Mixin на `PipeConnection.tryStartingNewFlow` (или
`FluidTransportBehaviour.tick`), который запрещает стартовать
coolant-flow на result-face — иначе cross-flow. Это отдельная задача,
сейчас не сделана.

## Нагрев

Температура читается напрямую в градусах Цельсия:

```java
HeatDealer.findTemperature(level, tankControllerPos.below(), belowState)
```

`HeatDealers.toBoilerHeat(...)` для логики змеевика **не используется**: это
шкала `SU` для бойлера Create, и пороги в ней линейные по 200 °C. Змеевику
нужны сырые °C — они сравниваются с `temperature_range` рецепта напрямую
(`min <= t && t <= max`). Шина нагрева та же, что и для басина
(`BasinBlockEntityMixin`) — см. [`update-heaters.md`](update-heaters.md).

## Детект протока хладагента

Читаем собственный `FluidTransportBehaviour`: для каждой из двух граней
водяной оси запрашиваем `getConnection(face)`. Проток есть, если:

- на одной грани `flow.hasFlow() && flow.get().complete && flow.get().inbound`;
- на противоположной — `flow.get().outbound`;
- `flow.get().fluid` в теге `#tfc_aeronautics:coolant` (сейчас в нём
  `minecraft:water`).

Стоячая вода без насоса flow не создаёт — требование «не стоячая, а текущая»
выполняется само.

## Блокировка бака — два уровня

1. **Учётный (обязательный, работает всегда).** На старте `RUNNING` снимается
   снапшот `total_volume`. Всё, что игрок дольёт сверху, в расчёт не идёт:
   перерабатывается ровно `target_volume`, в конце в баке `total_volume -
   target_volume` заменяется на `residue`.
2. **UX (mixin).** `FluidTankBlockEntityMixin` на
   `handlerForCapability()` (`FluidTankBlockEntity.java:374`) — при
   `@At("RETURN")`, если контроллер бака помечен как занятый, подменяет
   возврат на drain-only обёртку. Состояние блокировки хранится в **обычном
   (не-mixin) helper-классе** `DistillationTankLock` с `Map<GlobalPos,
   BlockPos>` (позиция змеевика-владельца) — по опыту проекта
   кросс-таргетное состояние в самих mixin-классах ломается. На lock/unlock
   вызывается `invalidateCapabilities()` на баке.

Если mixin окажется хрупким — механика остаётся корректной за счёт (1),
просто без визуального запрета залива.

## Формат рецепта `tfc_aeronautics:distillation`

```json
{
  "type": "tfc_aeronautics:distillation",
  "input": { "id": "tfc:vodka" },
  "temperature_range": [60, 110],
  "result": "tfc_aeronautics:ethanol",
  "residue": "tfc_aeronautics:stillage",
  "result_percent": 41,
  "rate": 0.2
}
```

- `input` — собственный кодек через `Codec.mapEither(BuiltInRegistries.FLUID.byNameCodec().fieldOf("id"), TagKey.codec(Registries.FLUID).fieldOf("tag"))`. NeoForge'овский `SizedFluidIngredient.FLAT_CODEC` не подходит: у него ключи `fluid`/`fluid_tag` и обязательный `amount`.
- `temperature_range` — `[min, max]` в °C; кодек с валидацией размера списка = 2 и `min <= max`.
- `result` / `residue` — строковый ID жидкости (как у ванильных рецептов); объёмы вычисляются, в JSON не пишутся.
- `result_percent` — int 0..100.
- `rate` — float mB/тик.

Поиск рецепта — `RecipeManager.getAllRecipesFor(TYPE)` + фильтр по `FluidStack`
в баке (контейнера для `getRecipeFor` тут нет). Результат кэшируется на BE,
инвалидируется при изменении жидкости в баке.

## Регистрация
- [x] block `tfc_aeronautics:condenser_coil` — ведёт себя как труба
- [x] blockstate-свойства: `AXIS` (X/Z) + `WATER_VERTICAL` (4 варианта ориентации)
- [x] item `tfc_aeronautics:condenser_coil_item`
- [x] `condenser_coil/CondenserCoilRegistration.java`
- [x] blockstate `assets/tfc_aeronautics/blockstates/condenser_coil.json`

## Текстурирование
- [x] Создать текстуру для `condenser_coil` (извлечена из base64 в `blockbench/condenser_coil.bbmodel`)

## Моделирование
- [x] Создать модель для `condenser_coil` (`block/condenser_coil.json` + `block/condenser_coil_vertical.json`)
- [x] item-модель `assets/tfc_aeronautics/models/item/condenser_coil.json`

## Логика
- [x] Тип рецептов `tfc_aeronautics:distillation` — `DistillationRecipe` + `DistillationRecipeSerializer` + `DistillationRecipeType` (без наследования от Create-овского `ProcessingRecipe`, без `RecipeParams`)
- [x] Собственный кодек `input` (`Either<id, tag>`), собственный кодек `temperature_range` (`[min, max]` int-pair с валидацией)
- [x] Валидация структуры дистиллятора: источник нагрева → жидкостный бак → логическая связь → змеевик (`DistillationStructure`)
- [x] Детект нагрева через `HeatDealer.findTemperature(level, controllerPos.below(), state)` — температура в °C сравнивается с `temperature_range` напрямую (НЕ через `HeatDealers.toBoilerHeat`)
- [x] Детект протока хладагента в перпендикулярной оси (`PipeConnection.flow` + проверка `Fluid` в теге `#tfc_aeronautics:coolant`)
- [x] `CondenserCoilFluidBehaviour extends FluidTransportBehaviour` с `canHaveFlowToward` только для двух граней водяной оси
- [x] Машина состояний IDLE → WARMUP → RUNNING → IDLE (пропуск тика как `PAUSED`)
- [x] Тик обработки: `progress += rate; int whole = (int) progress; progress -= whole` — сливаем `whole` mB из бака, столько же кладём во внутренний бак результата
- [x] Снапшот-арифметика: `target = snapshot * result_percent / 100`, `residueVol = snapshot - target`; по завершении — drain бака, fill `residueVol` mB `residue`
- [x] Доставка дистиллята в result-face: прямой `IFluidHandler.fill` + BFS `pushAlongPipeNetwork` (≤3 трубы) кладёт жидкость в каждый IFluidHandler по пути и в output-бак; pipe-граф Create не задействован (давление/addPressure не работает из-за cross-flow); BFS вызывается даже когда первый сосед — Create-труба (`IFluidHandler.BLOCK` у трубы всегда `null`, ранний return на этом был основным багом ранних итераций)
- [x] BFS-обход для детекта input-бака (≤3 трубы, разворачивается на коленах/развилках)
- [x] Open-end на result-face: pour-частицы через `ServerLevel.sendParticles` при воздухе, silent drop при не-fluid блоке
- [x] Поддержка JEI для рецептов `tfc_aeronautics:distillation`: категория с тремя слотами жидкостей (input / result / residue) и строкой температурного диапазона под ними; катализатор — `condenser_coil` (см. [раздел 34 DOCS.md](../DOCS.md#34-змеевик-конденсатор-condenser-coil))

## Блокировка бака
- [x] `mixin/FluidTankBlockEntityMixin` на `handlerForCapability()` (`@At("RETURN")` → `CallbackInfoReturnable<IFluidHandler>`)
- [x] Состояние блокировки в `condenser_coil/DistillationTankLock` (plain class, `Map<GlobalPos, BlockPos>` — не в самом mixin'е)
- [x] Регистрация миксина в `tfc_aeronautics.mixins.json`

## Конфигурация (`Config.java`)
- [x] `distillationWarmupTicks` — `IntValue`, 200, `[0, 72000]`
- [ ] `distillationOutputRange` — не реализован: BFS-сеть Create наследует радиус от `FluidPropagator.getPumpRange()` (= 16), отдельный ключ не нужен
- [ ] `distillationOutputPressure` — не реализован: давление не используется (push через прямой `handler.fill` + BFS)

## Рецепты
- [ ] Готовые рецепты и жидкости (`ethanol`/`stillage` и т.п.) — отложено до следующей итерации
- [x] Формат `tfc_aeronautics:distillation` реализован и валидируется кодеком

## Ponder-сцены
- [ ] Сцена о работе дистиллятора — отложено: `.nbt`-схематику нельзя собрать без запуска игры
