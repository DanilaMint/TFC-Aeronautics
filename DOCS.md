# TFC Aeronautics — документация мода

> Бридж-аддон между TerraFirmaCraft, Create и Simulated. Добавляет аэронавтику
> в реалистичное выживание TFC: материалы, металлургию, переплавку и механику
> кинетических машин, которые корректно работают с TFC-теплом и TFC-формовкой.

---

## Содержание

1. [Конфигурация](#1-конфигурация)
2. [Металлические порошки](#2-металлические-порошки)
3. [Жидкие металлы (molten fluids)](#3-жидкие-металлы-molten-fluids)
4. [Андезитовый сплав](#4-андезитовый-сплав)
5. [Штамп-пресс (Stamping Press)](#5-штамп-пресс-stamping-press)
6. [Нагреватель (Heater)](#6-нагреватель-heater)
7. [Спут Create + TFC литьё](#7-спут-create--tfc-литьё)
8. [Удалённые рецепты Create](#8-удалённые-рецепты-create)
9. [Вентилятор Create → угольная кузня TFC](#9-вентилятор-create--угольная-кузня-tfc)
10. [Тонкие листы (Tight sheet)](#10-тонкие-листы-tight-sheet)

---

## 1. Конфигурация

Файл: `common.toml` в директории конфигов. Загружается через
`ModConfig.Type.COMMON`, поглощается в `Aeronautics#Aeronautics` через
`modContainer.registerConfig`.

| Ключ | Тип | Диапазон | Назначение |
|------|-----|----------|------------|
| `balloonLiftMultiplier` | double | 0.1–10.0 | Множитель подъёмной силы воздушного шара на единицу горячего воздуха. Больше — «легче». |
| `hotAirBurnRate` | int | 20–72000 | Скорость сжигания топлива в горелке воздушного шара (тиков на единицу топлива). |
| `gliderDecayModifier` | double | 0.0–10.0 | Множитель износа планера. 1.0 = ванильная скорость, 0.0 = не изнашивается. |

Все три ключа пока не используются фичами — это заготовка под аэронавтические
блоки, которые ещё не реализованы. Сами значения уже подцеплены из
`Config.java`, в момент загрузки/перезагрузки выводится лог-сообщение.

---

## 2. Металлические порошки

Мод вводит 10 металлических порошков — перемолотого сырья, пригодного для
переплавки в стандартной TFC-печи.

| Порошок | id | Температура плавления | Жидкий металл |
|---------|----|----------------------|----------------|
| Медный | `copper` | 1080 °C | `tfc:metal/copper` |
| Оловянный | `tin` | 230 °C | `tfc:metal/tin` |
| Цинковый | `zinc` | 420 °C | `tfc:metal/zinc` |
| Висмутный | `bismuth` | 270 °C | `tfc:metal/bismuth` |
| Чугунный | `cast_iron` | 1535 °C | `tfc:metal/cast_iron` |
| Золотой | `gold` | 1060 °C | `tfc:metal/gold` |
| Серебряный | `silver` | 970 °C | `tfc:metal/silver` |
| Никелевый | `nickel` | 1450 °C | `tfc:metal/nickel` |
| Андезитового сплава | `andesite_alloy` | 1150 °C | `tfc_aeronautics:molten_andesite_alloy` |
| Магматитовый | `magmatite` | 1250 °C | `tfc_aeronautics:molten_magmatite` |

### Регистрация

Точка входа: `ru.tfc_aeronautics.powder.PowderRegistration`. Каждое значение
перечисления `MetalPowder` автоматически превращается в `DeferredHolder<Item, ?>`
через `Helpers.mapOf(...)` — тот же приём, что в TFC `TFCItems.METAL_ITEMS`.
Итоговое имя в реестре: `tfc_aeronautics:powder/<id>`.

Каждый порошок — это `MetalPowderItem extends Item`. Сама по себе вещь не
обладает поведением — всё нагревание и метаморфозы делает TFC через датапаки.

### Получение

Два пути, оба добавляются рецептами:

* **`tfc:quern`** — жернов. Перемалывает руду (медленно, руками).
* **`tfc:milling`** — фрезерный станок TFC. Перемалывает руду (быстро, через
  кинетику).

Каждый порошок имеет оба рецепта
(`data/tfc_aeronautics/recipe/quern/<id>_powder.json` и
`.../milling/<id>_powder.json`).

Также для двух сплавов (`andesite_alloy`, `magmatite`) есть парные рецепты
`create:crushing` — Create-мельница даёт тот же порошок.

### Температура и плавка

В датапаках `data/tfc_aeronautics/tfc/item_heat/<id>_powder.json` лежат
характеристики нагрева:

* `heat_capacity` — сколько тепла нужно, чтобы довести порошок до точки
  перехода.
* `forging_temperature` — минимальная температура, при которой порошок можно
  ковать.
* `welding_temperature` — минимальная температура для сварки.

Рецепты `tfc:heating` (`.../recipe/heating/<id>_powder.json`) описывают, что
получается из порошка при полном переплавлении: 100 мB (`amount: 100`)
соответствующего жидкого металла. Для андезитового сплава двойной слиток
даёт 200 мB — это и есть базовый стек TFC для двойного слитка.

### Поведение в инвентаре

`MetalPowderItem` ничего не делает сам. При попадании в нагревающееся
устройство (TFC-печь, наш `Heater`) TFC через `HeatCapability` начнёт вести
учёт температуры, а при достижении порога — выполнит heating-рецепт.

---

## 3. Жидкие металлы (molten fluids)

В моде зарегистрированы два собственных расплавленных металла:

* `tfc_aeronautics:molten_andesite_alloy` — андезитовый сплав.
* `tfc_aeronautics:molten_magmatite` — магматит.

Каждый из них — это связка из четырёх объектов, по образцу TFC
(`TFCFluids.METALS`):

| Объект | Регистрация | Назначение |
|--------|-------------|------------|
| `FluidType` | `AeronauticsFluids.FLUID_TYPES` | Физика жидкости: плотность, вязкость, свет, температура. |
| `Fluid` source + flowing | `AeronauticsFluids.FLUIDS` | Текучий/стоячий блок жидкости. Использует TFC `MoltenFluid.Source` / `Flowing`. |
| `FluidBlock` | `AeronauticsFluidBlocks` | Блок для рендера в мире, `MoltenFluidBlock` из TFC. |
| `BucketItem` | `AeronauticsFluidItems` | Ведро для переноски, ванильный `BucketItem` поверх source-флюида. |

### Свойства FluidType

Оба флюида копируют TFC `lavaLike()`:

```text
adjacentPathType  = LAVA
lightLevel        = 15
density           = 3000
viscosity         = 6000
temperature       = 1300
canDrown          = false
canExtinguish     = false
canHydrate        = false
canPushEntity     = false
canSwim           = false
explosionResistance = 100
```

То есть это «лава на минималках»: светится, обжигает, не тушится водой, не
даёт плавать, тяжёлая и вязкая. Температура 1300 °C — этого достаточно, чтобы
через TFC-механику передавать тепло в `HeatCapability` соседним блокам.

### Рендер-расширение клиента

В `FluidClientExtensions` (`@Dist.CLIENT`) для обоих флюидов регистрируется
`FluidRendererExtension` из TFC. Без него `ContainedFluidModel` для ведра
упал бы с `NullPointerException` при попытке достать still/flowing-текстуру.

* Андезитовый сплав: цвет `0xB06820` (тёмно-оранжевый), прозрачность по альфе.
* Магматит: цвет `0x3F3F3F` (тёмно-серый).

Текстуры не свои — мод переиспользует
`tfc:block/molten_still` и `tfc:block/molten_flow` из JAR TFC. Это законно,
поскольку текстура не копируется, а ссылается через `ResourceLocation`.

### Нагрев соседних блоков

В `data/tfc_aeronautics/tfc/fluid_heat/<fluid>.json` для TFC описаны
характеристики флюида:

```json
{
  "fluid": "tfc_aeronautics:molten_andesite_alloy",
  "melt_temperature": 1150.0,
  "specific_heat_capacity": 0.01
}
```

Файл `lava.json` модифицирует стандартную лаву — у TFC ванильная лава имеет
значения, не подходящие для нашей тех-цепочки; этот рецепт переопределяет
температуру и удельную теплоёмкость лавы, чтобы наши металлы корректно
взаимодействовали с ней.

### Использование в формах

В `data/tfc/tags/fluid/usable_in_ingot_mold.json` (с тегом `tfc`) добавлен
`molten_andesite_alloy` — это разрешает заливать его в инготную форму
через TFC-литьё.

---

## 4. Андезитовый сплав

Это основной материал мода. Идея: переосмыслить `create:andesite_alloy`: вместо
того чтобы крафтить его из железа и андезита, его надо выплавить из магматита
с добавкой чугуна.

### Сплав (alloy recipe)

`data/tfc_aeronautics/recipe/alloy/andesite_alloy.json`:

```json
{
  "type": "tfc:alloy",
  "contents": [
    { "fluid": "tfc_aeronautics:molten_magmatite", "min": 0.95, "max": 0.98 },
    { "fluid": "tfc:metal/cast_iron",             "min": 0.02, "max": 0.05 }
  ],
  "result": "tfc_aeronautics:molten_andesite_alloy"
}
```

95–98 % магматита и 2–5 % чугуна → 100 % андезитового сплава. Рецепт
регистрируется автоматически при запуске сервера, дальше TFC сам подбирает
подходящие флюиды в любой TFC-печи.

### Полный путь

1. **Добыча магматита** — руда в мире TFC, дробится в `magmatite_powder`.
2. **Порошок в печь** — 100 мB `molten_magmatite` на порошок.
3. **Сплав в печи** — типель с магматитом и чугуном → 100 мB
   `molten_andesite_alloy`.
4. **Заливка в форму** — керамическая инготная форма, рецепт
   `tfc:casting/andesite_alloy_ingot.json`:

```json
{ "type": "tfc:casting", "fluid": 100, "mold": "tfc:ceramic/ingot_mold",
  "result": "create:andesite_alloy" }
```

На выходе — `create:andesite_alloy` (тот самый слиток Create, который встречается
везде). Это сохраняет совместимость со всеми Create-рецептами, которые ждут
именно этот предмет.

### Двойной слиток

В `data/tfc_aeronautics/recipe/welding/andesite_alloy_double_ingot.json`:

```json
{ "type": "tfc:welding", "first_input": "c:ingots/andesite_alloy",
  "second_input": "c:ingots/andesite_alloy", "result":
  "tfc_aeronautics:metal/double_ingot/andesite_alloy", "tier": 3 }
```

Свариваются два слитка андезитового сплава на третьем тире наковальни →
`double_ingot/andesite_alloy`. И на двойном слитке висит `tfc:heating`
(200 мB) — то есть двойной слиток можно переплавить обратно в металл.

### Варианты нагрева

* `tfc:item_heat/andesite_alloy.json` — `forging_temperature: 690 °C`,
  `welding_temperature: 920 °C`. Это задаёт минимальные температуры, при
  которых Create-механизмы и наковальня TFC считают слиток «рабочим».
* `tfc:item_heat/andesite_alloy_double_ingot.json` — те же значения на двойной
  слиток.

### Теги

В `data/c/tags/item/`:

* `ingots/andesite_alloy.json` — добавляет `create:andesite_alloy`. Это
  расширяет существующий тег Create, чтобы TFC-рецепты через `c:ingots/...`
  находили наш слиток.
* `double_ingots/andesite_alloy.json` — добавляет `tfc_aeronautics:metal/double_ingot/andesite_alloy`.
  Аналог от Create, нужен для металлургии.

### Кастинг через Create-спут

Это работает и без нашего обходного пути — мод TFC изначально не понимает
Create-спут, но `SpoutCastingBehavior` (раздел 7) ловит момент, когда спут
стоит над `tfc:mold_table`, и вручную исполняет рецепт литья.

### Регистрация

Слиток: `create:andesite_alloy` — сохранили старый id, чтобы ничего не
сломать.
Двойной слиток: `tfc_aeronautics:metal/double_ingot/andesite_alloy`,
реєстрируется в `DoubleIngotRegistration` через `DeferredHolder`.

### Замена моделей

Текстура `assets/create/textures/item/andesite_alloy.png` переопределяется
нашим PNG: тот же путь, но без копейки ванильной текстуры. Внутри наш файл
нарисован в TFC-стиле (тонкие грани, более «металлический» вид).

### Замена крафтов

`RecipeOverrides` (раздел 8) удаляет стандартные Create-крафты:

* `create:crafting/materials/andesite_alloy` — железо + андезит → сплав.
* `create:crafting/materials/andesite_alloy_from_zinc` — цинк + андезит.
* `create:crafting/kinetics/shaft` — 2 слитка → 1 вал.

Пилить вал теперь можно только через наковальню TFC (раздел 5).

---

## 5. Штамп-пресс (Stamping Press)

Это кинетическая машина, которая автоматизирует TFC-работу наковальни:
сбрасывает нагретый предмет, проверяет, что выполняется TFC-рецепт наковальни,
и выдаёт результат. По сути — «наковальня, которую можно подключить к
кинетической сети Create».

### Регистрация

* Блок: `StampingPressBlock extends HorizontalKineticBlock implements IBE<StampingPressBlockEntity>, IWrenchable`.
* BlockEntity: `StampingPressBlockEntity extends KineticBlockEntity implements PressingBehaviourSpecifics`.
* Имя: `tfc_aeronautics:stamping_press`.
* Кинетическая нагрузка: `8.0 SU` (как у Create `mechanical_press`).
* Стресс-импакт регистрируется в `RegisterEvent` внутри `StampingPressRegistration#register`.

### Форма и размещение

* `VoxelShape` — `AllShapes.MECHANICAL_PROCESSOR_SHAPE`. Для игрока
  возвращается `AllShapes.CASING_14PX.get(DOWN)` — это позволяет избежать
  «застревания» внутри блока.
* `canSurvive` запрещает установку над `BasinBlock` (башенкой Create).
* `getRotationAxis` — горизонтальная ось, совпадает с `HORIZONTAL_FACING`.
* `hasShaftTowards` — только от горизонтальной грани, в которую смотрит блок.
* `getStateForPlacement` — ориентирует блок «лицом туда, куда смотрит игрок».

### Поведение

Блок-энтити реализует `PressingBehaviourSpecifics` — тот же интерфейс, что
использует Create Mechanical Press. Create сам вызывает эти методы,
когда кинетика работает.

* `addBehaviours` — добавляет `PressingBehaviour` (анимация бойка) и
  `FilteringBehaviour` (слот фильтра).
* `tryProcessInBasin` возвращает `false` — пресс не вставляет результат в
  башенку, как обычный Create-пресс. Выход — отдельный `ItemEntity`.
* `canProcessInBulk` возвращает `false` — единичная обработка, типичная для
  Create `mechanical_press`.
* `getKineticSpeed()` — возвращает `getSpeed()` текущего блока.

### Алгоритм поиска рецепта (`findRecipe`)

1. Получить `IHeatView` предмета. Если нагрева нет или `canWork() == false`
   (предмет не нагрет до своей ковочной температуры) — рецепта нет.
2. Загрузить все `AnvilRecipe` через `AnvilRecipe.getAll(level, input, MAX_VALUE)`.
3. Для каждого рецепта вызвать `recipe.matches(inv, level)` — это даёт TFC
   полный набор проверок (включая metal tier, ингредиент, температуру).
4. Из всех подходящих оставить только тот, чей `result.getItem()` совпадает с
   предметом-фильтром. Это и есть основная «фишка» пресса — он делает только
   то, что приказано фильтром.

### Обработка в мире (`tryProcessInWorld`)

Create зовёт этот метод, когда `ItemEntity` оказался над блоком во время
удара. Алгоритм:

1. Проверить фильтр (если пустой — ничего).
2. Найти рецепт через `findRecipe`.
3. Уменьшить стек `ItemEntity` на 1, остаток выбросить обратно.
4. Собрать результат одним вызовом `recipe.assemble(inv, registries)`.
5. Скопировать температуру с входа на выход через `HeatCapability`.
6. Заспавнить `ItemEntity` с результатом и частицами.

### Обработка на ленте (`tryProcessOnBelt`)

То же самое, только вход приходит через `TransportedItemStack` от Create-ленты.
Результат кладётся в `outputList` — Create сам создаст `ItemEntity` или
передаст дальше по ленте.

### Анимация

В `StampingPressRenderer`:

* Над блоком рисуется стандартный `KineticBlockEntityRenderer` с валом.
* Дополнительно загружается `AllPartialModels.MECHANICAL_PRESS_HEAD`,
  поворачивается по `HORIZONTAL_FACING`, и его вертикальное смещение берётся
  из `PressingBehaviour.getRenderedHeadOffset(partialTicks) * headOffset`.
  То есть боёк буквально опускается при ударе.
* Если клиент поддерживает Flywheel (`VisualizationManager.supportsVisualization`),
  то возврат из render сразу — модели рисует Flywheel-визуализация.

### Фильтр

`FilteringBehaviour` рендерит слот-фильтр на двух горизонтальных гранях,
перпендикулярных оси вала (см. `StampingPressFilterSlot#isSideActive`):
это `HORIZONTAL_FACING.getClockWise()` и `getCounterClockWise()`. Задняя
грань (противоположная `HORIZONTAL_FACING`) теперь не активна — раньше
фильтр стоял именно там и перекрывал свободный торец блока. Теперь
игрок может поставить/забрать фильтр с любого из двух боков.

`FilteringBehaviour.forRecipes()` — это значит, что слот работает в режиме
recipe-filter (как у Create-механизмов), а не tag-filter.

### Фильтр в режиме "press and copy"

После успешного удара `HotAware`-аналогии нет — пресс не блокирует изъятие
сам. Но фильтр — это и есть способ «задания»: какой предмет является
результатом, то и нужно положить в фильтр.

### Что ещё нужно сделать

В ROADMAP отмечены открытые пункты по прессу:

* вынести фильтр на перпендикулярную грань (сейчас он на задней)
* добавить звук скрипа вала
* добавить звук удара наковальни
* заменить Createpress-модель на TFC-флавированную
* валидировать рендер шахты и бойка

---

## 6. Нагреватель (Heater)

Главный блок мода. Принимает предмет, нагревает его, выдаёт результат (жидкий
металл во встроенный бак либо готовый предмет). Поддерживает топливо,
кузнечные мехи TFC и вентиляторы Create, имеет регулируемый максимум
температуры.

### Регистрация

* Блок: `HeaterBlock extends Block implements IBE<HeaterBlockEntity>, IWrenchable`.
* BlockEntity: `HeaterBlockEntity extends SmartBlockEntity implements IBellowsConsumer`.
* BlockState: `LIT` + `HORIZONTAL_FACING`.
* Свет: `getLightEmission` = 14, когда `LIT == true`.
* Аналоговый сигнал (редстоун): 15 при горении, иначе 0.

### Инвентарь

Внутри `ItemStackHandler(2)`:

| Слот | Имя | Назначение | Лимит |
|------|-----|------------|-------|
| 0 | `SLOT_ITEM` | Нагреваемая вещь. Принимает только предмет с `HeatCapability`. | 1 |
| 1 | `SLOT_FUEL` | Топливо. Принимает только предмет, для которого `Fuel.get(stack) != null` (т.е. TFC-топливо). | 64 |

При замене предмета в слоте 0 инвалидируется кеш heating-рецепта.

### Бак

Внутренний `FluidTank(TANK_CAPACITY = 2000 мB)` для расплавленного металла.
Переопределён `fill`:

* Если в баке уже есть флюид *другого типа* — вставка отклоняется. Металлы не
  смешиваются.
* Если новой порции не хватает места — принимается только то, что влезло.
  Остаток молча отбрасывается. Это сделано сознательно: входной предмет уже
  расплавился, его нужно убрать в любом случае.

### Капасити (Capabilities)

В `HeaterCapabilities.register` (через `RegisterCapabilitiesEvent`):

* `Capabilities.ItemHandler.BLOCK` → `HotAwareItemHandler`. Это обёртка над
  основным `ItemStackHandler`, она запрещает извлечение из слота 0, пока
  предмет не достиг уставки `maxTemperature`.
* `Capabilities.FluidHandler.BLOCK` → возвращается только если `ctx ==
  Direction.DOWN`, иначе `null`. То есть расплавленный металл можно выкачать
  только трубой/ведром снизу.

Это означает, что в любую грань можно подключить Create chute, Create funnel,
Minecart hopper, руку-манипулятор — все они будут работать через этот общий
`IItemHandler`.

### Температурный режим

Внутри блока поддерживаются четыре величины:

* `temperature` — текущая температура устройства (°C).
* `burnTemperature` — целевая температура горения, берётся из `Fuel.get(stack).temperature()`.
* `burnTicks` — сколько тиков осталось гореть.
* `airTicks` — «воздушный заряд» от мехов и вентиляторов (см. ниже).
* `maxTemperature` — уставка, регулируемая игроком через кран (см. ниже).

Каждый тик (только серверная сторона):

1. **Сканирование воздуха.** `scanForAirSources` обходит все 6 направлений, и
   если в одной из клеток стоит `EncasedFanBlockEntity` (Create) и поток
   воздуха направлен в нагреватель — вызывается `intakeAir(level, ..., rpm)`.
2. **Декремент таймеров.** `burnTicks` уменьшается на 1, либо на 2, если есть
   `airTicks > 0` или идёт дождь над блоком (дождь гасит огонь).
   `airTicks` уменьшается на 1.
3. **Повторная загрузка топлива.** Если `burnTicks <= 0` — пытаемся
   сжечь ещё один предмет из слота 1 (`consumeFuel`). Не получилось —
   `burnTemperature = 0`.
4. **Дрейф `temperature`.** Целевая температура — минимум `burnTemperature` и
   `maxTemperature`. Дальше `HeatCapability.adjustDeviceTemp` подтягивает
   `temperature` к цели, учитывая `airTicks` (мехи ускоряют разогрев).
   Жёсткий потолок: `MAX_TEMP + 200` (см. ниже).
5. **Нагрев предмета.** Если в слоте 0 есть предмет с `HeatCapability`, и
   `temperature > 0`, то `HeatCapability.addTemp(heat, temperature)` —
   ежесекундный прирост тепла в сам предмет.
6. **Heating-рецепт.** Когда предмет нагрет до нужного диапазона, TFC
   `HeatingRecipe` отдаёт жидкость и/или новый предмет. Жидкость
   заливается в `tank`, предмет заменяет собой стек в слоте 0.
7. **Синхронизация `LIT`.** При смене `LIT` вызывается
   `level.setBlock(..., UPDATE_ALL)`, что включает свет и текстуру.
8. **Частицы.** Если `burnTicks > 0`, каждые 3 тика отправляются частицы
   `FLAME` и `SMOKE` на верхнюю грань.

### Максимальная температура (max-temperature knob)

`HeaterValueBehaviour extends ScrollValueBehaviour` — стандартная Create-ручка
с UI-доской.

* `INTERVAL = 50 °C`. Доска содержит 32 слота (`max / INTERVAL` = 1600 / 50).
* Диапазон: 0..`MAX_TEMP` (`Heat.maxVisibleTemperature()`).
* Формат: «<число> °C».
* Колбэк: `HeaterBlockEntity::setMaxTemperature`, который зеркалит значение в
  приватное поле через `Mth.clamp`.

`HeaterValueBoxTransform` позиционирует кнопку на грани, совпадающей с
`HORIZONTAL_FACING`. То есть повернуть нагреватель вручную (через wrench)
можно, и кнопка переедет на новую грань.

### Обмен с инвентарём без GUI

В `HeaterBlock#useItemOn` и `useWithoutItem`:

* ПКМ по грани → вставляется 1 единица предмета из руки в подходящий слот.
* Shift + ПКМ → извлекается 1 единица из слота 0 (если предмет догрелся до
  `maxTemperature`).
* Извлечь топливо из слота 1 нельзя.

При разрушении блока `onRemove` сбрасывает оба слота в мир через
`Containers.dropItemStack`.

### Приём воздуха от мехов (TFC) и вентиляторов (Create)

`IBellowsConsumer` — стандартный TFC-интерфейс. TFC сам зовёт
`intakeAir(level, pos, state, amount)`, когда мехи направлены на нагреватель.

В `scanForAirSources` мы дополнительно обходим соседей и ищем
`EncasedFanBlockEntity`. Валидным считается поток, который «дует» именно в
нагреватель (направление воздуха вентилятора противоположно нашему
направлению к вентилятору). Количество воздуха — `Math.min(200, Mth.floor(rpm))`.

### Кинетика и вода

* Блок не кинетический — это `Block`, не `KineticBlock`. То есть подключать
  валы к нему нельзя.
* Дождь гасит огонь (см. выше: `burnTicks` декрементируется на 2 при дожде).
* Вода не тушит (тушением занималась бы TFC-логика, но мы не делаем её явно).
* Температура не связана с горением TFC: дровами/углём в TFC-печи,
  нагреватель — самодостаточное устройство.

### Ограничения

* Входной предмет обрабатывается поштучно (лимит слота 0 = 1).
* Если рецепт требует больше жидкости, чем влезет в бак — лишняя жидкость
  теряется. Это компромисс: проще, чем делать очередь/задержку.
* Один нагреватель — один расплавленный металл. Сменить металл можно только
  после полного опустошения бака.

### Рендер

Своего блок-энтити-рендерера нет: обе визуальные формы задаёт
`blockstates/heater.json`, который ссылается на модели Create blaze burner —
`create:block/blaze_burner/block` при `LIT == false` и
`create:block/blaze_burner/block_with_fire` при `LIT == true`. Огонь во второй
модели — ванильная анимированная текстура `block/campfire_fire`, поэтому
анимация не требует кода. Предмет наследует пустую жаровню
(`models/item/heater.json` → `create:block/blaze_burner/block`).
Никаких звуков пока нет.

---

## 7. Спут Create + TFC литьё

Чтобы TFC-формовка работала в автоматических линиях Create, мы регистрируем
кастомное `BlockSpoutingBehaviour` для блок-энтити TFC `mold_table`.

### Точка подключения

`SpoutCompat.onCommonSetup` (подписан на `FMLCommonSetupEvent`):

```java
BlockSpoutingBehaviour.BY_BLOCK_ENTITY.register(
    TFCBlockEntities.MOLD_TABLE.get(),
    SpoutCastingBehavior.INSTANCE
);
```

Регистрация именно в `enqueueWork`, потому что и TFC-реестр
`TFCBlockEntities`, и реестр Create-интерфейса должны быть живы.

### Поведение (`SpoutCastingBehavior#fillBlock`)

Create вызывает `fillBlock` каждый тик, пока спут активен. Наш обработчик:

1. Получить блок-энтити по позиции. Не TFC `mold_table` → возврат 0.
2. Если `moldTable.getOutputStack()` не пуст → возврат 0 (форма уже залита).
3. Если в форме нет模具 (`moldStack.isEmpty()`) → возврат 0.
4. Получить `IMold` через `IMold.get(moldStack)`. Не форма → 0.
5. Если в форме уже есть флюид (`mold.getFluidInTank(0).isEmpty()` == false) → 0.
6. Загрузить `CastingRecipe.get(mold)` — это TFC рецепт литья, привязанный к
   типу формы. Не нашлось → 0.
7. `amount = recipe.getFluidIngredient().amount()` — сколько мB нужно
   (100 для инготной формы, 200 для двойной, и т.д.).
8. Если в спуте меньше `amount` мB нужного флюида → 0.
9. Если `simulate == true` — сразу возвращаем `amount`. Этот метод Create
   вызывает дважды: сначала simulate, чтобы узнать, сколько мB можно
   забрать, потом «по-настоящему». Поэтому в simulate ничего не портим.
10. `recipe.assemble(mold)` → получаем `ItemStack` результата. Пусто → 0.
11. Кладём результат в `moldTable.setOutputStack(result)`.
12. Сливаем `mold.drainIgnoringTemperature(amount, EXECUTE)` — `mold` —
    это `IMold`, у которого тоже есть бак. Сливаем без проверки температуры,
    потому что спут — это «внешний» источник.
13. `moldTable.markForSync()` — синхронизируем с клиентом.
14. Возвращаем `amount`.

Create затем сам забирает `amount` мB из своего бака спута.

### Ограничения

* Спут работает только сверху над mold_table, никаких других позиций.
* Не работает для форм-«пустышек» без mold.
* Если форма занята или уже залита — спут не сольёт ничего.
* Если в mold_table уже что-то в `OUTPUT_SLOT` — спут не сольёт.

### Аналогия

Это точная копия паттерна, который сам Create использует в
`com.simibubi.create.compat.tconstruct.SpoutCasting`. Только у нас — TFC.

---

## 8. Удалённые рецепты Create

`RecipeOverrides` стирает три Create-рецепта на старте сервера, чтобы убрать
«дешёвые» пути в обход TFC-механик.

### Список

```java
List.of(
  "create:crafting/materials/andesite_alloy",
  "create:crafting/materials/andesite_alloy_from_zinc",
  "create:crafting/kinetics/shaft"
)
```

### Что они делали

| ID | Что делал | Чем заменён |
|----|-----------|-------------|
| `andesite_alloy` | 2 железных слитка + 2 андезита → слиток андезитового сплава. | TFC-сплав через магматит + чугун. |
| `andesite_alloy_from_zinc` | 2 цинковых слитка + 2 андезита → слиток. | То же. |
| `shaft` | 2 слитка андезитового сплава → 1 вал. | TFC-кузнечный рецепт: 1 слиток → 4 вала. |

### Механика

Подписка на `ServerStartedEvent`. После старта сервера мод получает
`RecipeManager`, фильтрует список рецептов через стрим и вызывает
`replaceRecipes(kept)`. Возвращается копия `kept`, за вычетом удалённых.

Лог `Removed {} recipes ... kept {N}` сообщает, сколько рецептов ушло.

### Что не трогаем

* `create:cutting/andesite_alloy` — пильный станок остаётся. Резка
  андезитовых блоков в плиты — отдельная механика, не конкурирует с
  металлургией.
* Создание Create-механизмов через крафт — это уже зависит от
  `create:shaft`, и оно автоматически отвалится, потому что валов нет.

---

## Принципы

* **TFC-металлы.** Сплавы регистрируются как обычные TFC-флюиды
  (`MoltenFluid.Source/Flowing`, `MoltenFluidBlock`), с теми же характеристиками.
* **Кинетика через Create.** Все машины используют стандартные `KineticBlockEntity`
  и `ValueSettingsBehaviour`.
* **Совместимость.** Слитки андезитового сплава остаются под id
  `create:andesite_alloy`, чтобы ничего не сломать в ванильных
  Create-рецептах.
* **Data-driven.** Нагрев, формовка, сплавы — всё в JSON-датапаках. В Java
  только регистрация и поведение машин.
* **Клиент отдельно.** `src/client/java/...` для рендера и `IClientFluidTypeExtensions`.

---

## 9. Вентилятор Create → угольная кузня TFC

В стандартном TFC `tfc:charcoal_forge` принимает воздух от TFC-меха через
`IBellowsConsumer`. Create `Encased Fan` сам по себе этот интерфейс не
реализует, поэтому без доработки вентилятор никак не влияет на кузню —
даже если стоит в упор и дует прямо в неё.

Эта механика закрывает разрыв: `tfc_aeronautics:forge/FanForgeIntake`
каждый серверный тик сканирует загруженные угольные кузни и для каждой
проверяет четыре горизонтальных соседа на наличие
`EncasedFanBlockEntity`. Если вентилятор действительно дует **в сторону**
кузни (т.е. `fan.getAirFlowDirection() == side.getOpposite()`) и у него
ненулевая RPM, кузня получает воздух через публичный метод
`CharcoalForgeBlockEntity.intakeAir(int)`.

### Количество воздуха

```text
amount = floor(abs(fan.getSpeed()) * FAN_FORGE_AIR_PER_TICK)
```

* `FAN_FORGE_AIR_PER_TICK` — параметр конфигурации `common.toml`
  (по умолчанию `1.5`, диапазон `0.0..20.0`).
* При 64 RPM и default `1.5` → 96 air-ticks/тик. С учётом того, что
  TFC `CharcoalForgeBlockEntity.serverTick` декрементит `airTicks--`
  каждый тик, вентилятор средней мощности держит `airTicks` вблизи
  потолка `BellowsBlockEntity.MAX_DEVICE_AIR_TICKS = 600`.
* Один «пуш» TFC-меха выдаёт `BellowsBlockEntity.BELLOWS_AIR = 200`
  air-ticks за раз, т.е. ~13 тиков работы вентилятора на 64 RPM
  эквивалентны одному ручному нажатию меха.

### Эффект в кузне

Когда `airTicks > 0`, TFC `HeatCapability.targetDeviceTemp(...)`
поднимает целевую температуру до `+min(4 * airTicks, 600)` °C
(см. `HeatCapability.targetDeviceTemp`), а `adjustDeviceTemp(...)`
ускоряет нагрев/остывание вдвое. Дополнительно `burnTicks -= 2`
вместо `1`, пока кузня горячая. Итог: кузня с вентилятором рядом
достигает максимальной температуры быстрее и сжигает топливо в 2×
быстрее — точно так же, как от TFC-меха.

### Дождь

Дождь (`level.isRainingAt(forgePos.above())`) снижает целевую
температуру на 300 °C и ускоряет сгорание топлива, как и в ванильном
TFC. Эта механика не отменяет поведение TFC — она лишь подаёт воздух,
всё остальное делает формула TFC.

### Регистрация

Класс `FanForgeIntake` помечен `@EventBusSubscriber(modid =
Aeronautics.MOD_ID, bus = Bus.GAME)`, поэтому отдельной регистрации в
`Aeronautics#Aeronautics` не требуется. Серверный тик-обработчик
подписан на `LevelTickEvent.Post`.

### Трекинг кузниц

Чтобы не сканировать весь мир каждый тик, позиции кузниц хранятся в
`Map<Level, Set<BlockPos>> FORGES`, который обновляется через:

* `BlockEvent.EntityPlaceEvent` — кузню поставили.
* `BlockEvent.BreakEvent` — кузню сломали.
* `ChunkEvent.Load` — чанк загружен (для возобновления сессии).
* `ChunkEvent.Unload` — чанк выгружен (чистка).

При обходе позиции, по которой больше нет `CharcoalForgeBlockEntity`,
она автоматически удаляется из трекера — это защищает от ситуации,
когда блок был уничтожен, но событие `BlockEvent.BreakEvent` по
какой-то причине не сработало.

### Баланс

Конфигурация `fanForgeAirPerTick` позволяет полностью отключить
механику (`0.0`) или наоборот — сделать вентилятор мощнее меха
(например, `5.0` → вентилятор на 256 RPM ≈ 1280 air-ticks/тик, т.е.
моментально насыщает кузню).

---

## 10. Тонкие листы (Tight sheet)

«Тонкий лист» — это промежуточный продукт между слитком и обычным
TFC-листом: одна единица `metal/tight_sheet/<металл>` содержит 100 мB
металла (вдвое меньше, чем стандартный `tfc:metal/sheet/<металл>`).
Используется там, где нужен тонкий, плотно прокатанный металл —
например, для обшивки воздушных шаров и герметичных корпусов.

### Варианты

* `tfc_aeronautics:metal/tight_sheet/copper`
* `tfc_aeronautics:metal/tight_sheet/wrought_iron`
* `tfc_aeronautics:metal/tight_sheet/steel`

Регистрируются через `TightSheetRegistration.TIGHT_SHEETS` — `Map`,
построенный из enum-класса `TightSheet` через `Helpers.mapOf(...)`.
Каждая запись enum-а несёт `id`, `meltTemperature` и ленивый `Supplier`
на выходной жидкий металл из `TFCFluids.METALS.get(Metal.X)`.

### Получение

Два пути, оба data-driven:

1. **Create-пресс.** `data/create/recipe/pressing/tight_sheet_<metal>.json`
   превращает слиток из тега `c:ingots/<metal>` в наш тонкий лист. Этот
   рецепт работает на стандартном Create `Mechanical Press`.
2. **Наковальня TFC.** `data/tfc_aeronautics/recipe/anvil/tight_sheet_<metal>.json`
   — `tfc:anvil`-рецепт «один слиток → один тонкий лист», правила
   `hit_last`, `hit_second_last`, `hit_third_last` (как у обычного
   TFC-листа), тиры 1/3/4 для меди/железа/стали.

### Нагрев

* `data/tfc_aeronautics/tfc/item_heat/<metal>_tight_sheet.json` —
  задаёт `heat_capacity: 9.6`, температуры ковки и сварки (≈60 % и
  ≈80 % от температуры плавления).
* `data/tfc_aeronautics/recipe/heating/<metal>_tight_sheet.json` —
  `tfc:heating`-рецепт: тонкий лист → 100 мB соответствующего жидкого
  металла (`tfc:metal/<metal>`) при температуре плавления.

### Регистрация

`TightSheetRegistration.register(modEventBus)` вызывается в
`Aeronautics#Aeronautics`. Листы попадают в общий креатив-таб через
`TightSheetRegistration.TIGHT_SHEETS.values().forEach(s -> output.accept(s.get()))`.

### Текстуры

`textures/item/metal/tight_sheet/<metal>.png` — 16×16, с тонкими
горизонтальными «полосами прокатки», отличающими плотный тонкий лист
от более «рваного» стандартного TFC-листа. Текстуры сгенерированы
скриптом в `/tmp/gen_tight_sheet_textures.py` (PIL) и при необходимости
перегенерируются тем же скриптом.
