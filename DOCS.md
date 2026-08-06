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
11. [Урон от вращающегося вала](#11-урон-от-вращающегося-вала)
12. [Корпуса Create из брёвен TFC](#12-корпуса-create-из-брёвен-tfc)
13. [Фреймворк атмосферных структур](#13-фреймворк-атмосферных-структур)
14. [Древнее кладбище (Ancient Graveyard)](#14-древнее-кладбище-ancient-graveyard)

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
| `shaftDamageEnabled` | boolean | — | Включает урон от касания голого вращающегося вала. См. [раздел 11](#11-урон-от-вращающегося-вала). |
| `shaftDamageStartRpm` | double | 0.0–1024.0 | Минимальный порог оборотов, ниже которого вал безопасен. |
| `shaftDamageLethalRpm` | double | 1.0–1024.0 | Обороты, на которых наносится `shaftDamageLethal`. Выше урон продолжает расти. |
| `shaftDamageLethal` | double | 0.0–1000.0 | Урон в HP на смертельных оборотах. 6.67 ≈ треть HP игрока без брони. |
| `shaftDamageMultiplier` | double | 0.0–100.0 | Сквозной множитель всей кривой урона. |
| `shaftKnockbackBase` | double | 0.0–2.0 | Базовая сила отбрасывания при ударе валом. |
| `shaftKnockbackPerRpm` | double | 0.0–0.1 | Прибавка к отбрасыванию за каждый оборот выше порога. |
| `shaftSoundVolume` | double | 0.0–1.0 | Громкость звука удара. 0.0 — тишина. |

Первые три ключа пока не используются фичами — это заготовка под аэронавтические
блоки, которые ещё не реализованы. Сами значения уже подцеплены из
`Config.java`, в момент загрузки/перезагрузки выводится лог-сообщение.
Ключи `shaft*` работают и предназначены в первую очередь для сборок:
`config/tfc_aeronautics-common.toml` можно положить в модпак и перенастроить
жёсткость механики, не трогая код.

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

### Алгоритм поиска рецепта (`findMatchingRecipe`)

1. Если фильтр пуст (`filtering.getFilter().isEmpty()`) — рецепта нет, предмет
   просто проезжает под прессом. Фильтр — это и есть «задание» прессу.
2. Получить `IHeat` предмета. Если нагрева нет или `canWork() == false`
   (предмет не нагрет до своей ковочной температуры) — рецепта нет.
3. Загрузить подходящие `AnvilRecipe` через
   `AnvilRecipe.getAll(level, input, MAX_TIER)`, где `MAX_TIER = Integer.MAX_VALUE`.
   Тир обязателен: `getAll` отбрасывает рецепты с `minTier > tier`, а у всех
   металлических рецептов `minTier = metal.tier()` (медь = 1, бронза = 2 …).
   С тиром `0` не проходил ни один металлический рецепт, и пресс молчал.
4. Из всех подходящих оставить первый, чей результат проходит
   `filtering.test(...)`.

Результат собирается в `assemble(recipe, input)`: рецепт применяется к стеку из
одного предмета, а затем на выход переносится температура входа
(`setTemperatureIfWarmer`) — так же, как это делает `AnvilBlockEntity`.

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

### Рендер фильтра: две особенности

Стандартный Create рисует рамку value-box только на той грани, куда смотрит
игрок. Чтобы рамки висели на обеих перпендикулярных гранях постоянно,
`StampingPressFrameTickHandler` на `ClientTickEvent.Post` обходит загруженные
блок-энтити и вызывает `Outliner.getInstance().showOutline(key, box)` с
отдельным ключом на каждую сторону. Сам предмет фильтра при этом рисует
`FilteringRenderer.renderOnBlockEntity` из BER — он уже умеет обходить все
шесть направлений для `Sided`-трансформа.

Две вещи, на которых это ломалось:

* **Позиция слота.** Смещение `Sided`-трансформа используется и для
  hit-теста, и для рендера. Модель пресса — глухая оболочка без выемки под
  слот, поэтому стандартные для Create 15.5 вокселя (полвокселя *внутрь*
  грани) прятали и рамку, и предмет, хотя правый клик продолжал работать.
  `StampingPressFilterSlot#getLocalOffset` выносит слот на `voxelSpace(8, 8,
  16.5f)` — полвокселя *наружу*; рамка рисуется на полвокселя обратно к
  блоку и садится вровень с гранью.
* **Flywheel глушит ванильный BER.** `SimpleBlockEntityVisualizer.Builder` по
  умолчанию отключает `BlockEntityRenderer`, как только у блок-энтити есть
  визуал. Create от этого явно отказывается, и `StampingPressClientRegistration`
  теперь тоже — через `.neverSkipVanillaRender()`. Без этого
  `StampingPressRenderer` не вызывался вовсе, и предмет фильтра не рисовался
  (рамка при этом оставалась видна, потому что её рисует Outliner в обход BER).

### Фильтр в режиме "press and copy"

После успешного удара `HotAware`-аналогии нет — пресс не блокирует изъятие
сам. Но фильтр — это и есть способ «задания»: какой предмет является
результатом, то и нужно положить в фильтр.

### Что ещё нужно сделать

В ROADMAP отмечены открытые пункты по прессу:

* вынести фильтр на перпендикулярную грань (сделано)
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

---

## 11. Урон от вращающегося вала

Голый вращающийся вал или шестерня Create наносят урон всему живому, что их
касается. Урон растёт линейно с оборотами; закрытая корпусом передача
безопасна. Смысл — вернуть кинетике ощущение опасного механизма, как этого
требует TFC, и дать корпусам практическую ценность помимо декоративной.

### Что опасно, а что нет

Проверка одна: `state.getBlock() instanceof AbstractShaftBlock`. В иерархии
Create этот класс покрывает ровно «голые» передачи, а все закрытые варианты
живут на соседней ветке (`AbstractEncasedShaftBlock` / `EncasedCogwheelBlock`).

| Опасно | Безопасно |
|--------|-----------|
| `create:shaft` | `create:andesite_encased_shaft`, `create:brass_encased_shaft` |
| `create:cogwheel`, `create:large_cogwheel` | `create:andesite_encased_cogwheel`, `create:brass_encased_cogwheel` (и large-версии) |
| вал под паровым двигателем (`PoweredShaftBlock`) | `create:gearshift`, `create:girder_encased_shaft` |

То есть чтобы обезопасить передачу, достаточно ПКМ-нуть по ней андезитовым
или латунным корпусом. Отдельного списка блоков в коде нет — защита следует
из иерархии классов Create, поэтому новые закрытые передачи в будущих версиях
Create автоматически окажутся безопасными.

### Кривая урона

| RPM | Урон без брони |
|-----|----------------|
| 128 | 0.0 |
| 160 | 1.7 |
| 192 | 3.3 |
| 224 | 5.0 |
| **256** | **6.67** |
| 320 | 10.0 |

Формула: `(rpm − startRpm) / (lethalRpm − startRpm) × lethalDamage × multiplier`.
Верхней границы нет — выше 256 RPM урон продолжает расти линейно.

Тип урона `tfc_aeronautics:shaft` **не** входит в `minecraft:bypasses_armor`,
поэтому броня урон снижает штатно. `scaling: never` выбран сознательно, чтобы
пороги в RPM означали одно и то же на любой сложности.

Частоту ударов ограничивают штатные кадры неуязвимости: собственного кулдауна
нет, повторный удар возможен не чаще раза в 10 тиков. На практике отбрасывание
обычно разрывает контакт раньше.

### Обратная связь

* **Отбрасывание** — импульс от центра блока, спроецированный на плоскость,
  перпендикулярную оси вращения вала. Горизонтальный вал подкидывает вверх или
  вбок, вертикальный — отшвыривает по горизонтали. Сила: `shaftKnockbackBase +
  (rpm − startRpm) × shaftKnockbackPerRpm`. Обязателен `hurtMarked = true`,
  иначе импульс не доедет до клиента.
* **Звук** — `AllSoundEvents.CRUSHING_1` (хруст дробилки Create), тон растёт
  с оборотами. Отключается через `shaftSoundVolume = 0.0`.

### Сообщение о смерти

`death.attack.tfc_aeronautics.shaft` — «`<Игрок>` намотался(ась) на вал».
Тип урона объявлен вручную в
`src/main/resources/data/tfc_aeronautics/damage_type/shaft.json` (датагена в
проекте нет, все data-файлы пишутся руками) и продублирован ключом
`ResourceKey<DamageType>` в `AeronauticsDamageTypes`.

### Точка подключения

Create подключён как `compileOnly` и миксинов в проекте нет, поэтому
переопределить `Block#entityInside` у классов Create невозможно. Вместо этого
`ShaftDamageHandler` слушает `EntityTickEvent.Post` на GAME-шине и на серверной
стороне перебирает клетки, пересекающиеся с хитбоксом существа (для игрока это
~8 клеток). Касание проверяется по реальной форме блока — стойка 5..11 px у
вала, диск со стойкой у шестерни, — а не по границам клетки: хитбокс раздувается
на 0.05 и пересекается с `state.getShape(...)` через `Shapes.joinIsNotEmpty`.
За тик срабатывает максимум один вал.

### Ограничения

* Валы в составе контрапции — не блоки мира, в скан не попадают.
* Перегруженная сеть и пауза дают `getSpeed() == 0`, урона нет.
* Креатив, спектатор и `isInvulnerable()` — ранний выход.
* Вал в металлической балке (`girder_encased_shaft`) считается закрытым,
  хотя визуально вал виден: балка трактуется как обшивка.

---

## 12. Корпуса Create из брёвен TFC

Стандартные рецепты Create используют тег `c:stripped_logs`, в который
обрубленные брёвна TFC не входят. Мод добавляет собственный тег
`tfc_aeronautics:stripped_logs` со всеми 20 вариантами
`tfc:wood/stripped_log/<порода>` и три рецепта `create:item_application`:

| Основа | Наносимый материал | Результат |
|--------|---------------------|-----------|
| Обрубленное бревно TFC | `create:andesite_alloy` | `create:andesite_casing` |
| Обрубленное бревно TFC | `c:ingots/brass` | `create:brass_casing` |
| Обрубленное бревно TFC | `c:ingots/copper` | `create:copper_casing` |

Латунь и медь принимаются через общие теги слитков, поэтому используются
соответствующие TFC-слитки. Обычные брёвна и блоки
`tfc:wood/stripped_wood/<порода>` намеренно не включены: рецепт действует
только на обрубленные брёвна.

---

## 13. Фреймворк атмосферных структур

Пакет `ru.tfc_aeronautics.worldgen` предоставляет абстракции для регистрации
структур с «нетипичными механиками» — пост-генерационными эффектами
(выпадение специфического лута, спавн НИП, расстановка блоков вокруг), а
также фильтрацией по TFC-климату. Это **скаффолд**: конкретных структур
пока нет, только API и datapack-папки.

### Регистрация

Сами структуры в 1.21.1 — **датапак-реестр** (`Registries.STRUCTURE` динамический),
поэтому в коде регистрируются только статические подреестры:

* `StructureType` — тип кодека. `tfc_aeronautics:atmospheric` для общего фреймворка,
  плюс отдельные типы под конкретные структуры (например,
  `tfc_aeronautics:ancient_graveyard`).
* `StructurePieceType` — фабрика куска. Нужна каждой не-джигсоу структуре.
* `StructureProcessorType` — фабрика процессора блоков.

Сами объекты структур описываются только в JSON:

```json
{
  "type": "tfc_aeronautics:atmospheric",
  "biomes": "#tfc:has_structure/example",
  "step": "surface_structures",
  "terrain_adaptation": "beard_thin",
  "atmosphere": {
    "climate": { "min_temperature": 10.0, "max_temperature": 25.0 },
    "effects": ["spawn_pilots", "mark_landing_pad"]
  }
}
```

Или, если нужны конкретные биомы прямо в структуре:

```java
new StructureSettings(
    Optional.empty(),
    TerrainAdjustment.BEARD_THIN,
    context.lookup(Registries.BIOME).getOrThrow(TagKey.create(Registries.BIOME,
        ResourceLocation.fromNamespaceAndPath("tfc", "has_structure/example")))
)
```

### `AtmosphereSpec`

Запись из двух опциональных частей:

```java
public record AtmosphereSpec(
    Optional<ClimateBounds> climateBounds,  // границы температуры/осадков
    List<String> effectIds                  // кодовые эффекты, выполняемые
                                            // после генерации структуры
) { ... }
```

* `ClimateBounds` — `min/max_temperature` и `min/max_rainfall` в JSON.
  Без TFC-интеграции матчер — `NOOP` (всегда принимает).
  Для реального климат-фильтра нужен `AtmosphereSpec.Resolver`,
  устанавливаемый через `AtmosphereSpec.installResolver(resolver)`
  в момент старт-инициализации (например, в `FMLCommonSetupEvent`).
* `effectIds` — строки-ключи. Каждый ключ резолвится в
  `AtmosphereSpec.Effect` (зарегистрированный в
  `AtmosphereSpec.Effect.REGISTRY` через
  `Effect.register(id, (level, random, center) -> { ... })`).

### JSON-формат структуры

```json
{
  "type": "tfc_aeronautics:atmospheric",
  "biomes": "#tfc:has_structure/example",
  "step": "surface_structures",
  "terrain_adaptation": "beard_thin",
  "atmosphere": {
    "climate": {
      "min_temperature": 10.0,
      "max_temperature": 25.0,
      "min_rainfall": 200.0,
      "max_rainfall": 500.0
    },
    "effects": ["spawn_pilots", "mark_landing_pad"]
  }
}
```

Биомы и шаг — стандартные ванильные поля `StructureSettings`,
`atmosphere` — расширение фреймворка.

### Папки datapack

| Папка | Назначение |
|---|---|
| `data/tfc_aeronautics/worldgen/structure/` | По JSON на структуру (`type: tfc_aeronautics:atmospheric`). |
| `data/tfc_aeronautics/worldgen/structure_set/` | По JSON на размещение структуры по биомам/расстоянию. |
| `data/tfc_aeronautics/worldgen/template_pool/` | Пул джигсоу-кусков. |
| `data/tfc_aeronautics/worldgen/processor_list/` | Процессоры замены блоков (например, ванильный камень → TFC-порода). |
| `data/tfc_aeronautics/structure/` | NBT-файлы джигсоу-кусков. |

### Чего фреймворк пока НЕ делает

* Не реализует кастомный `StructurePlacementType` для климат-фильтрации —
  TFC-овский `ClimateStructurePlacement` дергает внутренний
  `ChunkGeneratorExtension`, который аддону недоступен. Для точечной
  климат-фильтрации — внешний `Resolver` (см. выше).
* Не интегрируется напрямую с TFC — `tfc_aeronautics:atmospheric` тип
  структуры совместим с ванильным `structure_set` (биом-фильтрация через
  теги `#tfc:has_structure/...`).

### Что уже есть

* `tfc_aeronautics:ancient_graveyard` — рабочий пример на фреймворке (см.
  [раздел 14](#14-древнее-кладбище-ancient-graveyard)). Маленький склеп 5×5×5,
  закопаный под поверхностью, с адаптацией материалов под TFC-почву/камень
  и лутом в сосуде.

---

## 14. Древнее кладбище (Ancient Graveyard)

Первая конкретная структура на фреймворке атмосферных структур. Маленький
склеп 5×5×5, который **генерируется только на суше** (TFC-биомы с почвой на
поверхности) и **зарыт под землю**: на поверхности торчит ровно один блок
самого верхнего среднего саманного кирпича, остальное уходит вниз, внутри
полость.

### Шаблон

`data/tfc_aeronautics/structure/ancient_graveyard.nbt` — 5×5×5 куб:

| y | Содержимое |
|---|------------|
| 4 | пусто (structure_void), только центральный блок — саманный кирпич. Это «торчащий» блок. |
| 3 | саманный кирпич сплошняком по периметру, центр — пусто |
| 2 | саманный кирпич по периметру, центр — пусто |
| 1 | саманный кирпич по периметру, внутри — большой сосуд TFC (`tfc:ceramic/large_vessel`) на (3,1,1) |
| 0 | булыжник по всей нижней грани |

То есть:

* Внешние грани (стены, пол, крыша) — саман и булыжник.
* Внутренняя полость — реальный `minecraft:air` (в шаблоне), который при размещении вырезает камеру.
* Всё «пустое место» за стенами (то, что при размещении должно остаться землёй вокруг) — `minecraft:structure_void`. `BlockIgnoreProcessor` в `AncientGraveyardPiece` его пропускает, и наружный рельеф остаётся нетронутым.
* Сосуд внутри хранит лут.

### Архитектура

```
data/tfc_aeronautics/
├── structure/
│   └── ancient_graveyard.nbt          # шаблон
├── worldgen/
│   ├── structure/
│   │   └── ancient_graveyard.json     # описание структуры (тип, биомы, эффекты)
│   └── structure_set/
│       └── ancient_graveyard.json     # random_spread, spacing 24, separation 8
├── tags/
│   └── worldgen/biome/has_structure/
│       └── ancient_graveyard.json     # 12 TFC-биомов с почвой на поверхности
├── tags/
│   └── item/ancient_graveyard/
│       ├── seeds.json                 # 29 культур → лут
│       └── small_ores.json            # 12 рудных осколков → лут
└── loot_table/
    └── ancient_graveyard.json         # 3–5 руллов, 5 взвешенных типов

src/main/java/ru/tfc_aeronautics/worldgen/
├── AncientGraveyardStructure.java     # extends AtmosphericStructure
├── AncientGraveyardPiece.java         # extends TemplateStructurePiece
├── GraveyardMaterialProcessor.java    # StructureProcessor (адаптация материалов)
├── GraveyardLootEffect.java           # AtmosphereSpec.Effect (наполнение сосуда)
├── AeronauticsStructureTypes.java     # +ANCIENT_GRAVEYARD
├── AeronauticsStructurePieceTypes.java
├── AeronauticsProcessorTypes.java
└── WorldgenSetup.java                 # подписчик FMLCommonSetupEvent
```

### Регистрация (статические реестры)

В коде регистрируется три реестра — `AeronauticsStructureTypes`,
`AeronauticsStructurePieceTypes`, `AeronauticsProcessorTypes`. Сами
структуры как объекты живут только в JSON (см. выше), потому что
`Registries.STRUCTURE` — датапак-реестр.

| Реестр | Запись | Что разруливает |
|--------|--------|-----------------|
| `StructureType<?>` | `tfc_aeronautics:ancient_graveyard` → `AncientGraveyardStructure.CODEC` | Десериализация JSON в правильный подкласс. |
| `StructurePieceType` | `tfc_aeronautics:ancient_graveyard` → `AncientGraveyardPiece::new` | Восстановление куска из NBT после выгрузки чанка. |
| `StructureProcessorType<?>` | `tfc_aeronautics:graveyard_material` → `GraveyardMaterialProcessor.CODEC` | Нужен `getType()` (deprecation API); процессор собирается per-placement, не из JSON. |

`AncientGraveyardStructure` объявляет собственный `MapCodec` (с явным
type-witness `RecordCodecBuilder.<AncientGraveyardStructure>mapCodec(...)`)
— иначе DFU не выводит тип, и при десериализации всегда получался бы базовый
`AtmosphericStructure`.

### `findGenerationPoint` — как кладбище «прячется»

```text
1. surfaceY = getFirstOccupiedHeight(x, z, WORLD_SURFACE_WG)
2. if surfaceY ≤ seaLevel → Optional.empty()       (отбраковка под водой)
3. size = template.getSize()
4. origin = (x − size.x/2, surfaceY − (size.y − 1), z − size.z/2)
            ↑ верх шаблона = surfaceY,
              центр верхнего слоя — над выбранной колонкой
5. rotation = Rotation.getRandom(random)
6. → GenerationStub(origin, builder → addPiece(new AncientGraveyardPiece(...)))
```

То есть центр верхнего слоя шаблона (тот самый единственный саманный блок)
ложится ровно на выбранную поверхностную колонку. Всё, что выше него в
слое — `structure_void` и игнорируется, так что окружающая земля остаётся
как была.

### `AncientGraveyardPiece` — один кусок

`TemplateStructurePiece` с двумя особенностями:

* **Rotation pivot = центр шаблона.** При ротации по Y центр остаётся на
  месте → футпринт стабильный, торчащий блок всегда над выбранной колонкой.
* **Rotation персистится в NBT** под ключом `"Rotation"`. Ванильный
  `TemplateStructurePiece.addAdditionalSaveData` этого не делает.

В `postProcess` заново резолвится `GraveyardMaterialProcessor.resolve(...)`,
чтобы переписать материалы под текущий чанк (а не под чанк, в котором
структура впервые сгенерилась — в иных случаях чанк мог уже быть
перезаписан).

### `GraveyardMaterialProcessor` — адаптация материалов

`StructureProcessor`, который **per-placement** подбирает три замены:

1. **Саманные кирпичи** (`tfc:mud_bricks/<variant>`). Ищет вверх от
   `box.maxY() + 4` первый блок из списка грунтовых типов
   (`GRASS, DIRT, DUFF, COARSE_DIRT, ROOTED_DIRT, GRASS_PATH, FARMLAND,
   CLAY_GRASS, CLAY_DUFF, CLAY, MUD`) — это и есть «локальная почва». По
   найденному блоку определяет `SoilBlockType.Variant` (mollisol, podzol
   и т. д.), затем кладёт `TFCBlocks.SOIL.get(MUD_BRICKS).get(variant)`.
2. **Булыжник** (`tfc:rock/cobble/andesite` в шаблоне). Через
   `ChunkData.get(chunk).getRockData().getSurfaceRock(x, z).cobble()`. В
   try/catch — на нетфц-чанках `ChunkData` бросает исключение, и мы
   оставляем andesite как fallback.
3. **Большой сосуд** (`tfc:ceramic/large_vessel`). С шансом 50 % →
   `TFCBlocks.GLAZED_LARGE_VESSELS.get(<случайный DyeColor>)`. Сохраняет
   `facing`/`sealed`/`powered` исходного блока через `withPropertiesOf`.

`processBlock` для каждого блока шаблона возвращает новый `StructureBlockInfo`
с подставленным `BlockState`. Совпадение идёт по семейству блоков
(`MUD_BRICKS`, `COBBLES`, `instanceof LargeVesselBlock`), поэтому можно
пере-авторствовать шаблон с другим `variant`/`rock` — замена всё равно
сработает.

### `afterPlace` — где ваниль подставила нам палку

`Structure.afterPlace(WorldGenLevel, StructureManager, ChunkGenerator,
RandomSource, BoundingBox box, ChunkPos, PiecesContainer)` получает в
качестве `box` **чанк**, в котором сейчас пишется (vanilla-метод
`StructureStart.placeInChunk` итерирует по кускам и зовёт `afterPlace` для
каждого с одним и тем же чанк-боксом). Если вызвать
`atmosphere.runEffects(level, random, box.getCenter())` отсюда, лут-поиск
будет крутиться вокруг центра чанка, а сосуд стоит в центре структуры —
он промажет.

Решение: переопределить `afterPlace` в `AncientGraveyardStructure` и звать
`runEffects` от `pieces.calculateBoundingBox().getCenter()`:

```java
if (atmosphere().hasAtmosphere()) {
    atmosphere().runEffects(level, random, pieces.calculateBoundingBox().getCenter());
}
```

`calculateBoundingBox()` обходит все куски старта и выдаёт их реальный
объединённый бокс. Это даёт правильный центр.

### `GraveyardLootEffect` — наполнение сосуда

`AtmosphereSpec.Effect` с id `tfc_aeronautics:ancient_graveyard_loot`:

1. Загружает `LootTable` через
   `server.getServer().reloadableRegistries().getLootTable(KEY)`. Если
   таблица не нашлась — выход.
2. В кубе 4×4×4 вокруг `center` ищет `LargeVesselBlockEntity`. Для каждого
   сосуда: если пуст — катает `table.getRandomItems(...)` и раскладывает по
   слотам `IItemHandlerModifiable`.

Почему **не** `setLootTable` на `BlockEntity`? Потому что TFC-шный
`LargeVesselBlockEntity extends InventoryBlockEntity`, а не
`RandomizableContainerBlockEntity`. Ванильный путь «запомнить LootTable, при
первом открытии раскатать» для TFC-сосуда просто отсутствует. Поэтому катаем
сразу, при размещении.

Идемпотентность: если сосуд уже непустой (например, при пересечении чанков
структура обрабатывается несколько раз) — пропускаем. Это важно, потому что
`StructureStart.placeInChunk` вызывает `afterPlace` **на каждый чанк**, через
который проходит bounding box структуры.

### Лут-таблица

`data/tfc_aeronautics/loot_table/ancient_graveyard.json` — один пул,
`rolls: { min: 3, max: 5 }`, пять взвешенных записей:

| Запись | Weight | Count | Что на практике |
|--------|--------|-------|-----------------|
| `minecraft:rotten_flesh` | 25 | 0..16 | Тухлятина, основная «масса» лута. |
| `minecraft:bone` | 25 | 0..4 | Кости, фоновый лут. |
| `#tfc_aeronautics:ancient_graveyard/seeds` (`minecraft:tag`) | 15 | 0..16 | Любое из 29 TFC-семян (`tfc:seeds/...`) в случайном количестве. |
| `#tfc_aeronautics:ancient_graveyard/small_ores` (`minecraft:tag`) | 5 | 0..8 | Любой из 12 TFC-рудных осколков (`tfc:ore/small_<ore>`). |
| `tfc:powder/salt` | 5 | 0..8 | Соль. |

Используется `minecraft:tag` entry type (`expand: false` по умолчанию) —
один случайный предмет из тега, а не все сразу. Теги семян и руд лежат в
`data/tfc_aeronautics/tags/item/ancient_graveyard/{seeds,small_ores}.json`.

### Где не генерируется

Тег `#tfc_aeronautics:has_structure/ancient_graveyard` содержит ровно
12 биомов: `plains, hills, lowlands, rolling_hills, highlands, plateau,
plateau_wide, low_canyons, river_valley, terrace_upper, terrace_lower,
salt_marsh`. Сознательно исключены океаны/пляжи, пустыни, голые породы
(каньоны, месы), горы, ледники, карст — везде либо нет почвы, либо
неуместно.

Дополнительно `findGenerationPoint` отбраковывает позиции ниже уровня моря.

### Структура `tfc_aeronautics:ancient_graveyard` как пример

Эта структура — рабочий референс для будущих атмосферных структур:

* Свой `StructureType` (`AeronauticsStructureTypes.ANCIENT_GRAVEYARD`) — иначе
  vanilla десериализует JSON в `AtmosphericStructure` и теряет `findGenerationPoint`.
* Свой `MapCodec` с явным type-witness.
* `TemplateStructurePiece` с rotation pivot по центру и персистом rotation в NBT.
* `StructureProcessor`, который собирается per-placement (не из JSON), чтобы
  делать мир-зависимые решения (тип почвы, камень).
* `AtmosphereSpec.Effect` для пост-генерационных действий, которые не
  выразить процессорами (в нашем случае — наполнение сосуда).
* `afterPlace` override, чтобы получить настоящий центр структуры вместо
  per-chunk бокса от vanilla.

При копировании этого паттерна в новые структуры: меняется `template`,
биомный тег и эффекты — код `AtmosphereSpec.Effect` остаётся узкоспециальным.
