# Condenser Coil — план реализации

> План исполнения для механики `tfc_aeronautics:condenser_coil` и нового типа рецепта
> `tfc_aeronautics:distillation`. Дизайн (терминология, архитектура, формат рецепта) —
> здесь. Прогресс чеклистов — в [`condenser-coil.md`](condenser-coil.md), который будет
> переписан под финальную схему по результатам этой итерации.

## Context

`plans/condenser-coil.md` (0/18) и две строки-консьюмера в `plans/update-heaters.md` описывают
механику, которой пока нет в коде вообще. Шина нагрева (`ru.tfc_aeronautics.heat.HeatDealer`)
уже готова и используется басейном Create через `BasinBlockEntityMixin` — дистиллятор станет
её вторым потребителем.

Задача: добавить блок `tfc_aeronautics:condenser_coil`, распознавание связки
«HeatDealer → create:fluid_tank → труба Create → змеевик → труба с хладагентом» и новый тип
рецепта `tfc_aeronautics:distillation`, по которому из жидкости в баке получается `result`
на выходе змеевика и `residue` в баке.

Термин `distillage` из старого плана переименовывается в `residue`, `stillage_percent` →
`result_percent`, `temperature` → `temperature_range`. `plans/condenser-coil.md` нужно
переписать под финальную схему.

## Решения (подтверждены пользователем)

| Вопрос | Решение |
|---|---|
| Бак | `create:fluid_tank` (мультиблок Create) |
| Труба бак → змеевик | **логическая связь**, жидкость по ней не течёт, насос не нужен |
| Обрыв условия посреди процесса | пауза с возобновлением, бак остаётся заблокирован |
| Хладагент | пропускается **сквозь** змеевик; тег `#tfc_aeronautics:coolant` (в нём `minecraft:water`) |
| Напор на выходе | мини-насос в сети Create (`addPressure`), дальность из конфига (3) |
| `rate` | float mB/тик |
| `result_percent` | int 0..100, `target_volume = total_volume * result_percent / 100` |
| Объём итерации | блок + модель + логика + рецепты. Без JEI, без ponder, без готовых рецептов/жидкостей |

## Архитектура

### Разделение осей — ключевой момент

Змеевик хостит **свой `FluidTransportBehaviour`**, у которого
`canHaveFlowToward` возвращает `true` **только для двух граней водяной оси**. Для Create он
выглядит как обычная прямая труба вдоль водяной оси — вода течёт сквозь него штатной логикой
Create, своего кода потока писать не нужно. Паровая ось в сеть труб не входит вообще, поэтому
проблема «вода утекла в паровую трубу» не возникает конструктивно.

- **Вход паров** (одна грань паровой оси) — не жидкостное соединение. Змеевик просто идёт по
  линии блоков от этой грани и ищет `create:fluid_tank`.
- **Выход result** (противоположная грань) — змеевик выставляет `Capabilities.FluidHandler.BLOCK`
  только на эту грань (внутренний бак результата, extract-only). Соседняя труба в
  `PipeConnection.determineSource` сначала проверит `FluidPropagator.isOpenEnd` → там
  `pipe.canHaveFlowToward(state, opposite)` вернёт `false` → провалится в
  `hasFluidCapability` → увидит нас как `FlowSource.FluidHandler` и будет тянуть result.
  Проверено по `FluidPropagator.java:168-188` и `PumpBlockEntity.java:272-298`.
- **Вход/выход паров взаимозаменяемы**: входом считается та грань паровой оси, с которой
  нашёлся нагреваемый бак; выходом — противоположная. Отдельного blockstate-свойства не нужно.

### Детект протока хладагента

Читаем собственный behaviour: `getConnection(waterFaceA)` / `getConnection(waterFaceB)`.
Проток есть, если у одной грани `hasFlow() && flow.get().complete && flow.get().inbound`,
у противоположной — flow наружу, и `flow.fluid` в теге `#tfc_aeronautics:coolant`.
Стоячая вода без насоса flow не создаёт — требование «не стоячая, а текущая» выполняется само.

### Детект input-бака — BFS по трубам

`DistillationStructure.walkOneDirection` — BFS-обход по Create-трубам через
`FluidPropagator.getPipeConnections`. Лимит — `MAX_PIPE_BLOCKS = 3` трубы между
coil и баком (совпадает с радиусом эффективного давления на result-face).
Развилки и колена теперь разворачиваются: 3 трубы в линию + T-стык + бак под
стыком — детект находит бак. `traceWalk` тоже BFS, чтобы heartbeat-диагностика
не врала.

### Open-end на result-face

Если перед result-face воздух, `CondenserCoilBlockEntity.spawnOpenEndParticles`
шлёт `ServerLevel.sendParticles` с particle data из `FluidFX.getFluidParticle`
— визуально работает как открытый торец трубы (жидкость «вытекает» с
pour-частицами). Если перед result-face не-fluid блок (камень, бедрок) —
silent drop, как раньше.

### Доставка дистиллята в result-face

`CondenserCoilBlockEntity.ejectThroughResultFace` пушит
`toInsert` в соседний блок через прямой `IFluidHandler.fill`, затем —
если сосед оказался Create-трубой — вызывает
`pushAlongPipeNetwork`, BFS-обход по трубам с прямым `handler.fill`
в каждый `IFluidHandler.BLOCK` на пути. Pipe-граф Create **не
задействован** — мы не используем давление и не открываем
`canHaveFlowToward` для result-face.

**Почему BFS+`handler.fill`, а не давление.** `addPressure` работает
только на гранях, где у behaviour'а есть PipeConnection. Для соседней
трубы на стороне coil'а PipeConnection создаётся только если
`FluidPipeBlock.canConnectTo` вернёт true, а он проверяет
`transport.canHaveFlowToward(neighbour, side.getOpposite())`. У coil'а
result-face намеренно закрыт в `canHaveFlowToward` (иначе cross-flow
с водяной осью), значит `canConnectTo` = false, и `addPressure` —
silent no-op. Попытки обойти через открытие result-face ломают
изоляцию водяного контура.

**Как работает BFS.** Создаю `Set<BlockPos> visited` + два deque для
позиций и направлений входа. Стартовая позиция — труба прямо перед
coil'ом на result-face. На каждой трубе по пути:
1. `handler.fill(probe, EXECUTE)` через `IFluidHandler.BLOCK` трубы —
   кладёт жидкость во внутренний бак трубы.
2. По `FluidPropagator.getPipeConnections(state, pipe)` получаем все
   выходные стороны трубы, кроме `enteredFrom` (не возвращаемся
   назад), и добавляем их в frontier.
3. На воздухе / камне — frontier не расширяется.

Когда BFS доходит до не-трубы с `IFluidHandler` (например,
`create:fluid_tank`), `handler.fill` кладёт остаток дистиллята
туда. На пустом IFluidHandler (basin) — тоже кладёт.

**Trade-off.** Жидкость появляется во всех трубах по пути **в один
тик** — не «дрейфит» постепенно через давление. Для типовой
структуры (1–3 трубы по 500 mB, output-бак 1000+ mB) это не
блокер. Если игрок делает длинную цепочку (4+ трубы) — трубы могут
наполниться раньше, чем дистиллят дойдёт до output-бака.

**Производительность:** 3–4 `handler.fill`'а в тик для типичной
структуры — незначительно. Никакой синхронизации с pipe-графом
Create.

**Open-end.** Если сосед перед coil'ом — воздух, `spawnOpenEndParticles`
вызывается **до** BFS, жидкость «вытекает в воздух» как из открытого
торца трубы (pour-частицы). Воздух внутри BFS просто пропускается.

### Блокировка бака — два уровня

1. **Учётный (обязательный, работает всегда).** На старте процесса снимается снапшот
   `total_volume`. Всё, что игрок дольёт сверху, в расчёт не идёт: перерабатывается ровно
   `target_volume`, в конце в баке `total_volume - target_volume` заменяется на `residue`.
2. **UX (mixin).** `FluidTankBlockEntityMixin` на `handlerForCapability()`
   (`FluidTankBlockEntity.java:374`) — при `@At("RETURN")`, если контроллер бака помечен как
   занятый, подменить возврат на drain-only обёртку. Состояние блокировки хранить в **обычном
   (не-mixin) helper-классе** `DistillationTankLock` с `Map<GlobalPos, BlockPos>` (позиция
   змеевика-владельца) — по опыту проекта кросс-таргетное состояние в самих mixin-классах
   ломается. На lock/unlock вызывать `invalidateCapabilities()` на баке.

Если mixin окажется хрупким — механика остаётся корректной за счёт (1), просто без визуального
запрета залива.

### Состояния змеевика

`IDLE → WARMUP → RUNNING → (PAUSED) → RUNNING → IDLE`

- **WARMUP** входим, когда связка валидна и в баке есть жидкость, подходящая под какой-нибудь
  `distillation`-рецепт. Счётчик `distillationWarmupTicks` (200 = 10 с). Любое изменение
  объёма в баке сбрасывает счётчик обратно на полный — это и есть «можно долить».
- **RUNNING**: снапшот `total_volume`, lock бака, `target_volume = total_volume * result_percent / 100`,
  `produced = 0f`.
- Каждый тик: `progress += rate; int whole = (int) progress; progress -= whole;` — сливаем
  `whole` mB из бака, столько же кладём в внутренний бак результата. `produced += whole`.
  При `rate = 0.2` это 1 mB раз в 5 тиков → 410 mB за 2050 тиков (совпадает с примером).
- **PAUSED** (не отдельное состояние, а пропуск тика): температура вне
  `temperature_range`, нет протока хладагента, или внутренний бак результата полон.
  Прогресс и блокировка сохраняются.
- Завершение при `produced >= target_volume`: содержимое бака заменяется на `residue` объёмом
  `total_volume - target_volume`, unlock, сброс давления на выходной линии.

### Нагрев

`HeatDealer.findTemperature(level, controllerPos.below(), state)`, где `controllerPos` —
позиция `getControllerBE()` найденного бака (у мультиблока Create контроллер снизу).
Сравнение в градусах Цельсия напрямую с `temperature_range` — `HeatDealers.toBoilerHeat`
для логики **не** используется (это шкала SU для бойлера, не для порогов рецепта).
Строку про `toBoilerHeat` в `plans/condenser-coil.md` поправить.

## Blockstate и модель

### Свойства

- `EnumProperty<Direction.Axis> AXIS` — только `X` и `Z` (`EnumProperty.create("axis", Direction.Axis.class, Axis.X, Axis.Z)`)
- `BooleanProperty WATER_VERTICAL` (`"water_vertical"`) — водяная ось вертикальная (Y) или
  вторая горизонтальная (перпендикулярная `AXIS`)

Итого 4 варианта. Ось воды выводится: `WATER_VERTICAL ? Y : (AXIS == Z ? X : Z)`.

### Важно: поворот вокруг Z в blockstate невыразим

MC применяет `Ry(-y) · Rx(-x)`. Комбинация, оставляющая Z на месте и переводящая X → Y,
в этой группе отсутствует. Проверено перебором: из дефолтной модели достижимы только
`(Z,X)` и `(X,Z)`. Поэтому нужны **две** базовые модели:

| blockstate | model | rotation |
|---|---|---|
| `axis=z,water_vertical=false` | `block/condenser_coil` | — |
| `axis=x,water_vertical=false` | `block/condenser_coil` | `y: 90` |
| `axis=z,water_vertical=true` | `block/condenser_coil_vertical` | — |
| `axis=x,water_vertical=true` | `block/condenser_coil_vertical` | `y: 90` |

### Геометрия — обе модели уже есть в `blockbench/`

`blockbench/condenser_coil.bbmodel` и `blockbench/condenser_coil_vertical.bbmodel`.
У обеих `resolution` = 16×16, значит UV уже в MC-пространстве — копировать **verbatim**,
ничего не масштабировать и не выводить руками.

- `condenser_coil.json` ← `condenser_coil.bbmodel`
  - `inner` (пары, вдоль Z): `from [4,4,0]`, `to [12,12,16]`, up/down с `"rotation": 90`
  - `outer` (вода, вдоль X): `from [0,3,3]`, `to [16,13,13]`
- `condenser_coil_vertical.json` ← `condenser_coil_vertical.bbmodel`
  - `inner` — идентичен первой модели
  - `outer` (вода, вдоль Y): `from [3,0,3]`, `to [13,16,13]`; повороты граней уже расставлены
    в исходнике (north 90, east/south/west/up/down 270) — переносить как есть

Текстура одна на обе модели: в обоих файлах `textures[0]` — один и тот же base64 PNG 64×64
с одинаковым uuid. Извлечь один раз в `textures/block/condenser_coil.png`, обе модели ссылаются
на неё. **.bbmodel не удалять** — это единственная копия текстуры.

У `outer` в вертикальной модели `"origin": [16,0,0]`, но поворота (`rotation`) у элемента нет,
так что в MC-модель origin не переносится.

Блок регистрируется с `.noOcclusion()` (не полный куб). `render_type: minecraft:cutout` —
только если в извлечённом PNG есть альфа-канал.

Item-модель — `parent` на `block/condenser_coil` + блок `display`, скопированный из
`models/item/heater.json`.

## Формат рецепта

```json
{
  "type": "tfc_aeronautics:distillation",
  "input": { "id": "tfc:vodka" },
  "temperature_range": [60, 110],
  "result": { "id": "tfc_aeronautics:ethanol" },
  "residue": { "id": "tfc_aeronautics:stillage" },
  "result_percent": 41,
  "rate": 0.2
}
```

- `input` — `{ "id": "<fluid>" }` **или** `{ "tag": "<fluid_tag>" }`. Собственный кодек через
  `Codec.mapEither(BuiltInRegistries.FLUID.byNameCodec().fieldOf("id"), TagKey.codec(Registries.FLUID).fieldOf("tag"))`.
  NeoForge-овский `SizedFluidIngredient.FLAT_CODEC` не подходит: у него ключи `fluid`/`fluid_tag` и обязательный `amount`.
- `temperature_range` — `[min, max]` в °C, кодек с валидацией размера списка = 2
- `result` / `residue` — `{ "id": "<fluid>" }`, объём вычисляется, в JSON не пишется
- `result_percent` — int 0..100
- `rate` — float mB/тик

Поиск рецепта — `RecipeManager.getAllRecipesFor(TYPE)` + фильтр по `FluidStack` в баке
(контейнера для `getRecipeFor` тут нет). Результат кэшируется на BE, инвалидируется при
изменении жидкости в баке.

## Файлы

### Новые — `src/main/java/ru/tfc_aeronautics/condenser_coil/`

| Файл | Что делает |
|---|---|
| `CondenserCoilRegistration.java` | BLOCKS / ITEMS / BLOCK_ENTITY_TYPES, по образцу `stamping_press/StampingPressRegistration.java` |
| `CondenserCoilBlock.java` | `AXIS` + `WATER_VERTICAL`, `getStateForPlacement`, `IWrenchable` (цикл по 4 состояниям), `IBE<CondenserCoilBlockEntity>` |
| `CondenserCoilBlockEntity.java` | `SmartBlockEntity`; внутренний `SmartFluidTank` результата; машина состояний; тик процесса |
| `CondenserCoilFluidBehaviour.java` | `extends FluidTransportBehaviour`, `canHaveFlowToward` = только водяные грани |
| `CondenserCoilCapabilities.java` | `RegisterCapabilitiesEvent` — `FluidHandler.BLOCK` только на выходной грани, по образцу `heater/HeaterCapabilities.java:31-43` |
| `DistillationStructure.java` | обход трубной линии от паровой грани до `create:fluid_tank`, резолв контроллера, чтение `HeatDealer` |
| `CondenserOutputPump.java` | упрощённый `distributePressureTo` с дальностью из конфига |
| `DistillationTankLock.java` | обычный класс, `Map<GlobalPos, BlockPos>` — состояние блокировки для mixin'а |

### Новые — `src/main/java/ru/tfc_aeronautics/recipe/`

`DistillationRecipe.java`, `DistillationRecipeSerializer.java`, `DistillationRecipeType.java` —
по образцу `QuernMillingRecipe*.java` (но **без** наследования от Create-овского
`ProcessingRecipe` и без `RecipeParams` — процесс идёт в нашем BE).

### Новый mixin

`src/main/java/ru/tfc_aeronautics/mixin/FluidTankBlockEntityMixin.java` +
строка в `src/main/resources/tfc_aeronautics.mixins.json`.
CallbackInfo — `CallbackInfoReturnable<IFluidHandler>` (метод не void).

### Правки существующих

- `TFCAeronautics.java` — `CondenserCoilRegistration.register(modEventBus)` рядом с `HeaterRegistration` (~строка 64)
- `recipe/RecipeRegistration.java` — регистрация `DistillationRecipeType.RECIPE_TYPES` / `RECIPE_SERIALIZERS`
- `Config.java` — три значения рядом с `HEATER_SPEED_MULTIPLIER`:
  - `distillationWarmupTicks` — IntValue, 200, [0, 72000]
- `CreativeTabs.java` — добавить `condenser_coil` в `MAIN`

### Ресурсы (все руками в `src/main/resources/`, датаген не нужен — блок один)

- `assets/tfc_aeronautics/blockstates/condenser_coil.json`
- `assets/tfc_aeronautics/models/block/condenser_coil.json`, `condenser_coil_vertical.json`
- `assets/tfc_aeronautics/models/item/condenser_coil.json`
- `assets/tfc_aeronautics/textures/block/condenser_coil.png` (одна на обе модели, извлечь из base64 в bbmodel)
- `assets/tfc_aeronautics/lang/en_us.json` + `ru_ru.json` — `block.tfc_aeronautics.condenser_coil`
- `data/tfc_aeronautics/tags/fluid/coolant.json` — `["minecraft:water"]`

### Документация

- `plans/condenser-coil.md` — переписать под финальную схему (`residue`, `temperature_range`,
  `result_percent`, убрать `toBoilerHeat` из логики), обновить прогресс
- `plans/update-heaters.md` — отметить строки-консьюмеры дистиллятора
- `DOCS.md` — новый раздел про механику дистилляции
- `ROADMAP.md` — обновить счётчик у `Condenser Coil`

## Что НЕ входит в итерацию

- **JEI-категория** — JEI сейчас вообще не подключён: нет зависимости в `build.gradle`,
  каталог `src/client/java/ru/aeronautics/client/jei/` пустой
- **Ponder-сцена** — .nbt-схематику нельзя собрать без запуска игры (запрещено правилами репозитория)
- **Конкретный рецепт vodka → ethanol и сами жидкости `ethanol` / `stillage`**

Последний пункт стоит проговорить отдельно: **без хотя бы одного рецепта механику нельзя
проверить в игре**. Формат и загрузчик рецептов будут готовы, но чтобы ты смог что-то
подистиллировать в Prism, понадобится отдельным шагом завести жидкости и один JSON.

## Порядок исполнения — волны агентов

Исполняю через `superpowers:subagent-driven-development`. Файловые границы между агентами
одной волны не пересекаются, поэтому worktree не нужен (и git-write в этом репозитории
запрещён). Агентам компиляцию не доверяю — после каждой волны **и после каждого агента**
перепроверяю результат вручную (Read созданных файлов + при необходимости `./gradlew`).

### Гейты верификации (после каждого этапа — обязательно)

| Гейт | Что проверяю | Как |
|---|---|---|
| **После A1** | JSON парсится; UV совпадают с bbmodel; `condenser_coil.bbmodel` и `condenser_coil_vertical.bbmodel` не удалены; item-`display` скопирован из `heater.json`; PNG-текстура извлечена | `Read` каждого созданного файла + `python3 -c "import json; json.load(open('.../blockstates/condenser_coil.json'))"` для каждого JSON |
| **После A2** | Сериализатор использует `MapCodec`/`StreamCodec` правильно; рецепт-тип зарегистрирован; JSON-формат совпадает с разделом выше | `Read` 3 новых файлов + diff `RecipeRegistration.java`; проверка, что `RECIPE_TYPES`/`RECIPE_SERIALIZERS` — правильные registry names |
| **После A3** | `CallbackInfoReturnable<IFluidHandler>` (не `CallbackInfo`); lock-состояние в `DistillationTankLock` (plain class), не в самом mixin'е; `Config.java` новые значения с правильными диапазонами; mixin-имя добавлено в `tfc_aeronautics.mixins.json` | `Read` всех 4 файлов; перепроверить каждый пункт чеклиста выше |
| **После волны 1** | Все три агента не наступили друг другу на файлы; общий билд проходит | `./gradlew compileJava` |
| **После A4** | Ссылки на типы из A2/A3 корректные; `AXIS`/`WATER_VERTICAL` объявлены правильно; `canHaveFlowToward` возвращает `true` только для водяных граней; машина состояний соответствует разделу «Состояния змеевика»; снапшот-арифметика 1000 → 410 + 590 | `Read` всех файлов пакета `condenser_coil/` + diff `TFCAeronautics.java` и `CreativeTabs.java`; пройти по чеклисту глазами |
| **После волны 2** | Билд + клиентский сорс-сет | `./gradlew compileJava` + `./gradlew compileClientJava` |
| **После A5** | Отчёт `code-reader`'а: если нашлись расхождения моделей с bbmodel или ошибки в арифметике — фикшу сам в основном потоке | `Read` отчёта; при находках — Edit/Write по указанным путям |
| **После A6** | Все 4 документа соответствуют финальной схеме (терминология, прогресс, разделы) | `Read` каждого файла + сверка с разделами «Формат рецепта» и «Состояния змеевика» |
| **После волны 3** | Полная сборка | `./gradlew build` + `python3 generate/generate.py` (если ещё не запущен) + регенерация `PROJECT_STRUCTURE.md` через `.claude/scripts/sync-structure-docs.sh` |

**Правило:** если на любом гейте найдено расхождение с планом — стоп, фикшу сам, только
потом иду к следующему этапу. Не «накапливать» найденные проблемы до конца волны.

### Волна 1 — три агента параллельно, зависимостей между ними нет

| Агент | Тип | Файлы (не пересекаются) |
|---|---|---|
| **A1. Ресурсы** | `coder` | `assets/.../blockstates/condenser_coil.json`, `models/block/condenser_coil{,_vertical}.json`, `models/item/condenser_coil.json`, `textures/block/condenser_coil.png`, `lang/{en_us,ru_ru}.json`, `data/.../tags/fluid/coolant.json` |
| **A2. Тип рецепта** | `coder` | `recipe/DistillationRecipe.java`, `DistillationRecipeSerializer.java`, `DistillationRecipeType.java`, правка `recipe/RecipeRegistration.java` |
| **A3. Конфиг + блокировка бака** | `coder` | правка `Config.java`, `condenser_coil/DistillationTankLock.java`, `mixin/FluidTankBlockEntityMixin.java`, правка `tfc_aeronautics.mixins.json` |

Что каждому агенту дать в промпте:
- A1 — читать **оба** `.bbmodel` напрямую (не по чужому пересказу), UV копировать verbatim,
  исходники не удалять; образец item-`display` — `models/item/heater.json`; таблица
  blockstate-вариантов из раздела выше
- A2 — образец `recipe/QuernMillingRecipe*.java`; полный JSON-формат из раздела «Формат
  рецепта»; явно предупредить, что `SizedFluidIngredient.FLAT_CODEC` не подходит
- A3 — `FluidTankBlockEntity.java:374` (`handlerForCapability`) как точка инжекта;
  `CallbackInfoReturnable<IFluidHandler>`, а не `CallbackInfo`; состояние держать в
  обычном классе, не в mixin'е; образец `mixin/BasinBlockEntityMixin.java`

### Волна 2 — ядро, один агент

**A4. Блок + BE + логика** (`coder`): весь пакет `condenser_coil/` кроме
`DistillationTankLock.java`. Зависит от типов, созданных в A2 и A3, поэтому параллелить
нельзя. Плюс правки `TFCAeronautics.java` и `CreativeTabs.java`.

В промпт: разделение осей через `canHaveFlowToward`, детект протока через `PipeConnection.flow`,
порт `PumpBlockEntity.distributePressureTo` (`code_references/Create/.../pump/PumpBlockEntity.java:119-228`),
машина состояний и формула снапшота — целиком из разделов «Архитектура» и «Состояния змеевика».

### Волна 3 — ревью и документация, два агента параллельно

| Агент | Тип | Задача |
|---|---|---|
| **A5. Ревью** | `code-reader` | сверить `models/block/*.json` с bbmodel посимвольно; проверить, что арифметика снапшота даёт пример 1000 → 410 + 590; проверить, что паровая ось не попала в `canHaveFlowToward` |
| **A6. Документация** | `coder` | `plans/condenser-coil.md` (переписать под финальную схему), `plans/update-heaters.md`, `DOCS.md`, `ROADMAP.md` |

После волны 3 — финальный `./gradlew build` и регенерация `PROJECT_STRUCTURE.md`
(PostToolUse-хук делает это сам, но после параллельных агентов лучше прогнать явно).

## Проверка

Автоматически (мне доступно):
1. `./gradlew compileJava` — основной сорс-сет
2. `./gradlew compileClientJava` — клиентский
3. `./gradlew build` — полная сборка, включая `runData`/python-датаген
4. Ревизия JSON-моделей: сверить `from`/`to`/UV/`rotation` в обеих моделях с их bbmodel посимвольно

Ручная проверка в Prism-лаунчере (запускать игру мне запрещено):
1. Блок ставится, 4 ориентации крутятся ключом, модель не разъезжается
2. Труба Create визуально стыкуется **только** к водяной оси
3. Насос гонит воду сквозь змеевик, анимация потока идёт насквозь
4. `create:fluid_tank` на костре/нагревателе + труба до змеевика → через 10 с процесс стартует
5. Долив в бак во время отсчёта — счётчик сбрасывается; после старта — залив заблокирован
6. Result доходит по трубе до второго бака на расстоянии 3 блоков **без насоса**
7. Гашение костра / остановка воды — процесс замирает и продолжается после восстановления
8. В конце в исходном баке ровно `total_volume - target_volume` mB `residue`

Пункты 4–8 требуют рецепта и жидкостей — то есть либо временного тестового рецепта, либо
следующей итерации.
