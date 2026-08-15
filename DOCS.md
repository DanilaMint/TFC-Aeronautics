# TFC Aeronautics — документация мода

> Бридж-аддон между TerraFirmaCraft, Create и Simulated. Добавляет аэронавтику
> в реалистичное выживание TFC: материалы, металлургию, переплавку и механику
> кинетических машин, которые корректно работают с TFC-теплом и TFC-формовкой.

---

## Содержание

1. [Конфигурация](#1-конфигурация)
2. [Металлические порошки](#2-металлические-порошки)
3. [Штамп-пресс (Stamping Press)](#3-штамп-пресс-stamping-press)
4. [Нагреватель (Heater)](#4-нагреватель-heater)
5. [Спут Create + TFC литьё](#5-спут-create--tfc-литьё)
6. [Тонкие листы (Tight sheet)](#6-тонкие-листы-tight-sheet)
7. [Урон от вращающегося вала](#7-урон-от-вращающегося-вала)
8. [Корпуса Create из брёвен TFC](#8-корпуса-create-из-брёвен-tfc)
9. [Фреймворк атмосферных структур](#9-фреймворк-атмосферных-структур)
10. [Древняя гробница (Ancient Graveyard)](#10-древняя-гробница-ancient-graveyard)
11. [Древнее укрытие (Ancient Shelter)](#11-древнее-укрытие-ancient-shelter)
12. [Дом фермера (Farmer House)](#12-дом-фермера-farmer-house)
13. [Богатая гробница (Rich Graveyard)](#13-богатая-гробница-rich-graveyard)
14. [Дом кожевника (Tanner House)](#14-дом-кожевника-tanner-house)
15. [Пропитанная джутовая ткань (Impregnated Burlap Cloth)](#15-пропитанная-джутовая-ткань-impregnated-burlap-cloth)
16. [Нагревательные элементы (Heat Dealers)](#16-нагревательные-элементы-heat-dealers)
17. [Топливо TFC в портативных двигателях](#17-топливо-tfc-в-портативных-двигателях)
18. [Скрытие TFC-кинематики](#18-скрытие-tfc-кинематики)
19. [Простые замены рецептов (Recipe overrides)](#19-простые-замены-рецептов-recipe-overrides)
20. [Замена slimeball на `tfc:glue`](#20-замена-slimeball-на-tfcglue)

---

## 1. Конфигурация

Файл: `common.toml` в директории конфигов. Загружается через
`ModConfig.Type.COMMON`, поглощается в `Aeronautics#Aeronautics` через
`modContainer.registerConfig`.

| Ключ | Тип | Диапазон | Назначение |
|------|-----|----------|------------|
| `tfcFuelInEngines` | boolean | — | Включает распознавание TFC-топлива в `simulated:portable_engine` (и любых других потребителях `getBurnTime`). См. [раздел 17](#17-топливо-tfc-в-simulated-portable_engine). |
| `resinDropChance` | double | 0.0–1.0 | Шанс выпадения комка смолы при обдирании коры. 0.15 = 15%. |
| `shaftDamageEnabled` | boolean | — | Включает урон от касания голого вращающегося вала. См. [раздел 7](#7-урон-от-вращающегося-вала). |
| `shaftDamageStartRpm` | double | 0.0–1024.0 | Минимальный порог оборотов, ниже которого вал безопасен. |
| `shaftDamageLethalRpm` | double | 1.0–1024.0 | Обороты, на которых наносится `shaftDamageLethal`. Выше урон продолжает расти. |
| `shaftDamageLethal` | double | 0.0–1000.0 | Урон в HP на смертельных оборотах. 6.67 ≈ треть HP игрока без брони. |
| `shaftDamageMultiplier` | double | 0.0–100.0 | Сквозной множитель всей кривой урона. |
| `shaftKnockbackBase` | double | 0.0–2.0 | Базовая сила отбрасывания при ударе валом. |
| `shaftKnockbackPerRpm` | double | 0.0–0.1 | Прибавка к отбрасыванию за каждый оборот выше порога. |
| `shaftSoundVolume` | double | 0.0–1.0 | Громкость звука удара. 0.0 — тишина. |

Все ключи работают и предназначены в первую очередь для сборок:
---

## 2. Металлические порошки

Мод вводит 8 металлических порошков — перемолотого сырья, пригодного для
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

### Регистрация

Точка входа: `ru.tfc_aeronautics.powder.PowderRegistration`. Каждое значение
перечисления `MetalPowder` автоматически превращается в `DeferredHolder<Item, ?>`
через `Helpers.mapOf(...)` — тот же приём, что в TFC `TFCItems.METAL_ITEMS`.
Итоговое имя в реестре: `tfc_aeronautics:powder/<id>`.

Каждый порошок — это `MetalPowderItem extends Item`. Сама по себе вещь не
обладает поведением — всё нагревание и метаморфозы делает TFC через датапаки.

### Получение

Один путь на сегодня — только через жернов (см. каталог
`tfc_aeronautics:only_quern` ниже).

* **`tfc:quern`** — жернов. Перемалывает слиток в 20 ед. порошка (медленно,
  руками). Рецепты лежат в `data/tfc_aeronautics/recipe/quern/<id>_powder.json`
  с типом `tfc:quern`.

#### `tfc_aeronautics:only_quern` — каталог рецептов только для жернова

Это не отдельный `RecipeType`, а соглашение об именовании директорий. TFC
обрабатывает любой рецепт с `"type": "tfc:quern"` независимо от вложенной
папки, а Create-мельница видит только `"type": "create:milling"`. Поэтому,
чтобы рецепт «видел только жернов», достаточно не публиковать его
`create:milling`-зеркало.

Сейчас в эту категорию попадают 8 слитков → порошков: bismuth, cast_iron,
copper, gold, nickel, silver, tin, zinc.

#### Зеркалирование TFC `tfc:quern` рецептов в Create `milling`

Большинство TFC-рецептов `tfc:quern` мы зеркалируем в
`data/create/recipe/milling/<тот_же_путь>.json` с типом `create:milling`,
`processing_time: 250`, чтобы Create-мельница тоже их обрабатывала
(зеркалирование — по конвенции в исходный неймспейс, см.
[[feedback-recipe-override-convention]]).

Зеркалируются как обычные `create:milling` (59 файлов под `data/create/recipe/milling/`):

* `canola_paste.json`, `lime_dye.json` — одиночные ингредиенты;
* `ore/gypsum.json` — известняк → 1 гипс;
* `powder/*.json` (56 файлов) — одиночные рудные ингредиенты и `#tfc:fluxstone`.

`bone` → 3 bone_meal уже покрыт собственным рецептом Create
`data/create/recipe/milling/bone.json` (который ещё и бонусом даёт
25% шанс white_dye), так что зеркало не публикуем — иначе бы затенили
полезный бонус.

#### `tfc_aeronautics:quern_milling` — поддержка TFC-модификаторов в мельнице

У TFC `food/<grain>_flour.json` ингредиент — сложный `tfc:and { item: …, tfc:not_rotten }`,
а результат несёт `result.modifiers: [{ type: tfc:copy_food }]` и обёртку
`result.stack`. Стандартный `create:milling` не поддерживает ни сложные
предикаты, ни модификаторы результата, поэтому для этих 6 рецептов
заводим кастомный `RecipeType` и собственный маршрут обработки в
Create-мельнице.

JSON-форма (см. `data/tfc_aeronautics/recipe/milling/food/wheat_flour.json`):

```json
{
  "type": "tfc_aeronautics:quern_milling",
  "ingredient": {
    "type": "tfc:and",
    "children": [
      { "item": "tfc:food/wheat_grain" },
      { "type": "tfc:not_rotten" }
    ]
  },
  "result": {
    "modifiers": [{ "type": "tfc:copy_food" }],
    "stack": { "count": 1, "id": "tfc:food/wheat_flour" }
  },
  "processing_time": 250
}
```

Чтобы мельница вообще увидела этот `RecipeType`, реализация живёт в Java
(`src/main/java/ru/tfc_aeronautics/recipe/`):

* `QuernMillingRecipe` — наследник `MillingRecipe`, хранит `Ingredient` и
  `ItemStackProvider` (включая модификаторы).
* `QuernMillingRecipeSerializer` — `RecipeSerializer`, читает тот же TFC-формат
  через `ItemStackProvider.CODEC` (включая `tfc:copy_food`).
* `QuernMillingRecipeType` — `DeferredRegister` для `RecipeType` и
  `RecipeSerializer` под id `tfc_aeronautics:quern_milling`.
* `mixin/MillstoneBlockEntityMixin` — три `@Inject`/`@Redirect` в `tick()`,
  `process()` и `canProcess()`. Когда вход соответствует нашему рецепсу,
  `process()` вызывает `ItemStackProvider.getSingleStack(input)` вместо
  стандартного `rollResults(random)` — поэтому `tfc:copy_food` (и любой
  другой `ItemStackModifier`) реально применяется, а не теряется.

Поведенческий результат неотличим от жернова: Create-мельница принимает
**только несгнившее** зерно (через `tfc:not_rotten`), а получившаяся мука
получает decay и food-data зерна (через `tfc:copy_food`).

**Синхронизация срока годности.** В первой реализации срок у муки из мельницы
сбрасывался: `MillstoneBlockEntity.process()` сначала делал `inputSlot.shrink(1)`,
потом уже вызывал `rollResults` — к этому моменту в слоте оставалось
`ItemStack.EMPTY`, и `FoodCapability.updateFoodFromPrevious(EMPTY, flour)`
отказывался копировать FOOD-данные (`oldFood == null` → ранний возврат). TFC-жернов
работает иначе: `recipe.assemble(inputStack)` вызывается **до** `shrink(1)`, поэтому
input всегда non-empty. Mixin теперь захватывает pre-shrink input в начале
`process()` через `@Inject(at = @At("HEAD"))` и сохраняет в `@Unique`-поле
**копию** (`inputInv.getStackInSlot(0).copy()`, а не голую ссылку — иначе
последующий in-place `shrink(1)` обнулит count у нашего снимка, и `ItemStack.copy()`
внутри `getSingleStack` отдаст `EMPTY`); redirect `aeronautics$rollResults`
подставляет его в `ItemStackProvider.getSingleStack`. `CopyFoodModifier` получает
реальное зерно и корректно применяет TFC-формулу `Cf = (1 - p) * T + p * Ci` с
`p = newDecay / oldDecay` — мука из мельницы теперь имеет тот же
`creationDate`, что и мука из жернова, помолотого из того же зерна.

**Не зеркалируются** — естественно остаются только на жернове:

* `redstone.json`, `blue_dye.json` … `yellow_dye.json` (13 + redstone) —
  несколько ингредиентов; Create `milling` принимает только один
  (`ProcessingRecipe.validate`, максимум один item-ингредиент).
* `powder/amethyst.json`, `diamond.json`, `emerald.json`, `lapis_lazuli.json`,
  `opal.json`, `ruby.json`, `sapphire.json`, `topaz.json` — `gem/<x>` ИЛИ
  `ore/<x>` (массив из двух).

#### Прочее

Для порошков также есть парные рецепты `create:crushing` — Create-дробилка
даёт тот же порошок (`data/tfc_aeronautics/recipe/crushing/<id>_powder.json`,
как и для листов/двойных_листов/стержней — см. раздел про tight sheet и
metal powder).

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
соответствующего жидкого металла.

### Поведение в инвентаре

`MetalPowderItem` ничего не делает сам. При попадании в нагревающееся
устройство (TFC-печь, наш `Heater`) TFC через `HeatCapability` начнёт вести
учёт температуры, а при достижении порога — выполнит heating-рецепт.

---

## 3. Штамп-пресс (Stamping Press)

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

## 4. Нагреватель (Heater)

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

## 5. Спут Create + TFC литьё

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

## Принципы

* **Кинетика через Create.** Все машины используют стандартные `KineticBlockEntity`
  и `ValueSettingsBehaviour`.
* **Data-driven.** Нагрев, формовка, сплавы — всё в JSON-датапаках. В Java
  только регистрация и поведение машин.
* **Клиент отдельно.** `src/client/java/...` для рендера и `IClientFluidTypeExtensions`.

---

## 6. Тонкие листы (Tight sheet)

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

## 7. Урон от вращающегося вала

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

## 8. Корпуса Create из брёвен TFC

Стандартные рецепты Create используют тег `c:stripped_logs`, в который
обрубленные брёвна TFC не входят. Мод добавляет собственный тег
`tfc_aeronautics:stripped_logs` со всеми 20 вариантами
`tfc:wood/stripped_log/<порода>` и три рецепта `create:item_application`:

| Основа | Наносимый материал | Результат |
|--------|---------------------|-----------|
| Обрубленное бревно TFC | `tfc_aeronautics:composite` | `create:andesite_casing` |
| Обрубленное бревно TFC | `c:ingots/brass` | `create:brass_casing` |
| Обрубленное бревно TFC | `c:ingots/copper` | `create:copper_casing` |

Латунь и медь принимаются через общие теги слитков, поэтому используются
соответствующие TFC-слитки. Обычные брёвна и блоки
`tfc:wood/stripped_wood/<порода>` намеренно не включены: рецепт действует
только на обрубленные брёвна.

---

## 9. Фреймворк атмосферных структур

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
  [раздел 10](#10-древняя-гробница-ancient-graveyard)). Маленький склеп 5×5×5,
  закопаный под поверхностью, с адаптацией материалов под TFC-почву/камень
  и лутом в сосуде.
* `tfc_aeronautics:ancient_shelter` — наземный шалаш из брёвен с большим
  сосудом (климатически-фильтрованный лут) и потухшим костром (зола).
* `tfc_aeronautics:farmer_house` — саманный дом с грядками, инструментом и
  сосудом, где лут подстраивается под климат так же, как культура на грядках.
* `tfc_aeronautics:rich_graveyard` — заглубленный каменный склеп с лутом в
  сундуке и полированным маркером на поверхности.
* `tfc_aeronautics:tanner_house` — деревянный дом с тремя бочками (вода /
  известковое молоко / танин) и сундуком со шкурами и ножом.

---

## 10. Древняя гробница (Ancient Graveyard)

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

### `findGenerationPoint` — как гробница «прячется»

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

---

## 11. Древнее укрытие (Ancient Shelter)

Небольшой наземный шалаш — самый «древний» из наших строений. По сути это
навес из брёвен с двумя контейнерами внутри: большим сосудом и потухшим
костром.

### Материалы

Все деревянные блоки (брёвна, доски, плиты, двери, бочки, полки, листва)
переписываются под локальную породу дерева через `LocalMaterialProcessor`
(см. раздел 9). Камня, самана и почвы в шаблоне нет — только дерево,
структурный пустоты вокруг и два контейнера.

### Эффекты (`AncientShelterEffects`)

| ID | Что делает |
|----|------------|
| `tfc_aeronautics:ancient_shelter_vessel` | Наполняет большой сосуд. Лут катается в коде через `AncientShelterLoot` (см. ниже). |
| `tfc_aeronautics:ancient_shelter_ash` | Засыпает 1–2 `tfc:powder/wood_ash` в потухший firepit (через `setAsh` на `AbstractFirepitBlockEntity`). Лут-таблица — `loot_table/ancient_shelter_ash.json`. |

### Климатически-фильтрованный лут сосуда (`AncientShelterLoot`)

Стандартная JSON-таблица тут не подходит: чтобы в тёплом климате не
выпадали клюква и снежная ягода, а в холодном — бананы и апельсины, нужно
знать локальный климат в момент генерации. Поэтому лут-пул живёт в коде:

```java
private static final List<Entry> POOL = List.of(
    // Без климатических ограничений:
    entry(4, null, "food/beef", 1, 3),
    entry(4, null, "food/pork", 1, 3),
    // ...
    entry(3, null, "powder/salt", 1, 4),
    entry(1, null, "ore/small_native_copper", 1, 8),

    // Тёплые фрукты:
    entry(3, ClimateRanges.BANANA_PLANT, "food/banana", 1, 4),
    entry(3, ClimateRanges.FRUIT_TREES.get(FruitBlocks.Tree.ORANGE), "food/orange", 1, 4),

    // Холодные кусты:
    entry(1, ClimateRanges.STATIONARY_BUSHES.get(FruitBlocks.StationaryBush.SNOWBERRY), "food/snowberry", 1, 4),
    entry(1, ClimateRanges.STATIONARY_BUSHES.get(FruitBlocks.StationaryBush.CLOUDBERRY), "food/cloudberry", 1, 4),
    // ...
);
```

Каждая запись несёт `weight`, `Supplier<ClimateRange>` (или `null`, если
без ограничений), `ResourceLocation` предмета и диапазон количества.
`ClimateRange` берётся прямо из тех же `ClimateRanges`, что управляют
ростом культур и фруктовых деревьев в TFC.

В `roll` мы читаем `ChunkData` для центра структуры, фильтруем пул по
`range.checkBoth(groundwater, temperature, false)` и бросаем один
взвешенный ролл. То есть одно укрытие — один предмет (мясо / фрукт /
овощ / руда / соль).

Идемпотентность: `fillLargeVessel` пропускает сосуд, если он уже непустой
— на случай повторного `afterPlace` при пересечении чанков.

### Где генерируется

`#tfc_aeronautics:has_structure/ancient_shelter` — 12 TFC-биомов
(plains, hills, lowlands, rolling_hills, highlands, plateau, plateau_wide,
low_canyons, river_valley, terrace_upper, terrace_lower, salt_marsh).
Сознательно исключены пустыни и горы — там дерево не растёт.

Плотность: `random_spread`, spacing 22, separation 4, salt 100101
(≈ 1/484 чанков, по дизайну «≈ 1/500 блоков»).

---

## 12. Дом фермера (Farmer House)

Саманный дом с грядками под открытым небом. Саман и земля подстраиваются
под локальную почву, брёвна — под локальное дерево. Внутри: закрытый
большой сосуд с едой и семенами и стеллаж с мотыгой.

### Эффекты (`FarmerHouseEffects`)

| ID | Что делает |
|----|------------|
| `tfc_aeronautics:farmer_house_vessel` | Наполняет сосуд 3–16 единиц еды и 4–16 единиц семян той же культуры, что растёт на грядках. |
| `tfc_aeronautics:farmer_house_tool_rack` | Кладёт каменную (реже — медную) мотыгу в стеллаж. |

### Климатический пикер культур (`FarmerHouseCrops`)

Ключевая инвариантность дома фермера: культура, которую кладёт процессор на
грядки, **та же самая**, что попадает в сосуд. Для этого оба пользуются
общим пикером:

```java
public static Optional<Crop> pick(LevelReader level, BlockPos center) {
    final ChunkData data = ChunkData.get(level.getChunk(center));
    final float temperature = data.getAverageSeaLevelTemp(center);
    final float groundwater = data.getAverageGroundwater(center);
    final List<Crop> suitable = new ArrayList<>();
    for (Crop crop : FOOD_IDS.keySet()) {
        final ClimateRange range = crop.getClimateRange().get();
        if (range != null && range.checkBoth((int) groundwater, temperature, false)) {
            suitable.add(crop);
        }
    }
    final RandomSource rng = RandomSource.create(center.asLong());
    return Optional.of(suitable.get(rng.nextInt(suitable.size())));
}
```

* `FOOD_IDS` — отфильтрованный список `Crop`-ов, у которых есть
  `tfc:food/<...>` (CANOLA, ALFALFA, JUTE и прочие «без еды» отброшены).
* RNG засеян от `center.asLong()`, а не от структурного — это гарантирует,
  что и `LocalMaterialProcessor.resolve` (запускается в `postProcess`), и
  `FarmerHouseEffects.fillVessel` (запускается в `afterPlace`) видят одну
  и ту же последовательность случайных чисел и попадают на одну культуру.
* `WHEAT` как fallback на случай, когда `ChunkData` ещё пуст или в
  диапазоне не нашлось ни одной культуры.

В `fillVessel` дальше:

```java
final Crop crop = FarmerHouseCrops.pick(level, center).orElse(Crop.WHEAT);
final Item foodItem = FarmerHouseCrops.foodItem(crop);
final Item seedItem = FarmerHouseCrops.seedItem(crop);  // TFCItems.CROP_SEEDS.get(crop)
final int foodCount = 3 + random.nextInt(14);   // 3..16
final int seedCount = 4 + random.nextInt(13);   // 4..16
```

### Стеллаж

`ToolRackBlockEntity` — стандартный TFC-стеллаж на `ItemStackHandler`.
Через `setStackInSlot` (это ванильный хендлер без TFC-овского
onContentsChanged-цикла, дедлока нет). Лут-таблица —
`loot_table/farmer_house_tool_rack.json`:

| Предмет | Вес |
|---------|-----|
| `tfc:stone/hoe/sedimentary` | 5 |
| `tfc:stone/hoe/igneous_extrusive` | 4 |
| `tfc:stone/hoe/igneous_intrusive` | 3 |
| `tfc:stone/hoe/metamorphic` | 3 |
| `tfc:metal/hoe/copper` | 2 |

Все с `set_damage: { uniform: 0..0.85 }` — стартовая мотыга почти
истёрта.

### Где генерируется

`#tfc_aeronautics:has_structure/farmer_house` — те же 12 TFC-биомов, что и
у `ancient_shelter` (тёплые и умеренные). Плотность: spacing 26,
separation 4, salt 100102 (≈ 1/676 чанков, по дизайну «1/700 блоков»).

---

## 13. Богатая гробница (Rich Graveyard)

Заглубленный каменный склеп с лутом в сундуке (или в бочке). В отличие от
`ancient_graveyard`, эта структура сделана по общему фреймворку
`AtmosphericTemplateStructure` с типом `placement: underground` — шаблон
укладывается внутри каменного слоя, на 5 блоков ниже поверхности.

### Эффекты (`RichGraveyardEffects`)

| ID | Что делает |
|----|------------|
| `tfc_aerveyard_marker` | Кладёт один блок `tfc:rock/smooth/<локальная_порода>` на поверхность над склепом, чтобы у игрока был визуальный след. |

Сундук/бочка лутятся через стандартный ванильный механизм
`RandomizableContainer` — `LootTable` зашит прямо в NBT блока шаблона
(инжектится скриптом `tmp/inject_chest_loot.py`), TFC-контейнеры
наследуют то же поведение.

### Полированный маркер на поверхности

```java
final Rock rock = LocalMaterialProcessor.lookupRock(
    data.getRockData().getSurfaceRock(center.getX(), center.getZ()));
final BlockState marker = TFCBlocks.ROCK_BLOCKS.get(rock).get(Rock.BlockType.SMOOTH).get().defaultBlockState();
final int surfaceY = generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, ...);
level.setBlock(new BlockPos(x, surfaceY, z), marker, Block.UPDATE_ALL_IMMEDIATE);
```

Резолвится порода слоя под структурой (через `ChunkData`), не
поверхности, потому что шаблон сидит в камне — `getSurfaceRock` на
координатах структуры возвращает ту же породу, что стены склепа.

### Сундук vs бочка

В шаблоне стоит сундук; в NBT дополнительно прописан флаг
«заменить сундук бочкой с шансом 50 %» — реализуется прямо в NBT
блока через `tmp/inject_chest_loot.py`. Если стоит бочка — в ней
наливается вода/алкоголь/уксус до 2000 мБ (распределение — через
`RandomizableContainer`).

### Лут

`loot_table/rich_graveyard_chest.json`, четыре пула:

| Пул | Содержимое |
|-----|------------|
| `bones` | 3–8 костей (всегда). |
| `flesh` | 3–8 тухлятины (всегда). |
| `salt` | 40 % шанс на 1–3 `tfc:powder/salt`, иначе пусто. |
| `valuables` | 45 % пусто; 55 % шанс на слиток (`copper`, `bronze`, `bismuth_bronze`, `silver`, `gold`) 1–2 шт. или самоцвет (`diamond`, `emerald`, `lapis_lazuli`) 1–4 шт. |

### Треснутые и замшелые кирпичи

`MaterialConfig { cracked_chance: 0.30, mossy_chance: 0.30 }` — каждый
каждый кирпич в шаблоне с 30 % шансом превращается в
`rock/cracked_bricks` или в `rock/mossy_bricks` соответственно
(`LocalMaterialProcessor.chooseRockVariant`).

### Где генерируется

`#tfc_aeronautics:has_structure/rich_graveyard` — **21 TFC-биом**, все
обитаемые, в том числе горы, пустыни и побережья (то есть «по всему
миру»). Плотность: spacing 32, separation 4, salt 100103 (≈ 1/1024
чанков).

---

## 14. Дом кожевника (Tanner House)

Деревянное здание с тремя бочками внутри — водой, известковым молоком и
танином — и сундуком со шкурами и ножом.

### Эффекты (`TannerHouseEffects`)

| ID | Что делает |
|----|------------|
| `tfc_aeronautics:tanner_house_chest` | Наполняет сундук шкурами и ножом (через `ContainerLootFiller.fill` + ванильную `LootTable`). |
| `tfc_aeronautics:tanner_house_barrel` | Наполняет три бочки в коде и запечатывает их обратно. |

### Наполнение бочек в коде

Стандартная JSON-таблица тут не подходит: лут — это не предметы, а
жидкости в разных объёмах. Поэтому `TannerHouseEffects` собирает план
бочек в коде:

```java
private static final List<BarrelSpec> BARREL_SPECS = List.of(
    new BarrelSpec(Fluids.WATER, 1000, 5000),     // 1000..5000 мБ
    new BarrelSpec(resolveFluid(LIMEWATER_ID), 1, 2000),
    new BarrelSpec(resolveFluid(TANNIN_ID),    1, 2000)
);
```

`ContainerLootFiller.fillWithCodeLoot` обходит бочки в порядке обхода
`(dx, dy, dz)` вокруг центра структуры и применяет эффект к каждой.
Внутри эффекта:

1. Очистить слоты предметов бочки (через прямой write в
   `InventoryItemHandler.getInternalStacks()` — та же обход-цепочка
   TFC-sync, что и в `clearItems`, см. комментарий на
   `ContainerLootFiller.writeLoot`).
2. Взять следующий `BarrelSpec` по индексу, бросить объём жидкости
   `random.nextInt(min, max + 1)`.
3. `inventory.fill(new FluidStack(fluid, amount), FluidAction.EXECUTE)`.
4. Запечатать бочку обратно:

```java
final BlockState state = level.getBlockState(pos);
level.setBlock(pos, state.setValue(SealableDeviceBlock.SEALED, true),
    Block.UPDATE_ALL_IMMEDIATE);
barrel.onSeal();
```

Запечатывание **после** наполнения: `BarrelInventory.fill` гейтится на
`!getBlockState().getValue(SEALED)`, поэтому запечатанная бочка молча
отбросила бы заливку. `barrel.onSeal()` обновляет `sealedTick`,
помечает BE на синк и проигрывает звук закрытия — здесь это безопасно,
потому что ни вода, ни известковое молоко, ни танин сами по себе не
запускают `barrel_sealed` рецепт (для танина и воды TFC требует ещё
`tannin_logs` в слотах, а известковое молоко — это `barrel_instant`).

Ротация структуры может поменять порядок обхода бочек, и тогда «вода»,
«известковое молоко» и «танин» перемешаются между бочками — это
сознательно: три бочки визуально идентичны, и перемешивание не
ломает нарратив.

### Флюиды (`resolveFluid`)

```java
private static Fluid resolveFluid(ResourceLocation id) {
    final Fluid fluid = BuiltInRegistries.FLUID.get(id);
    if (fluid == null) {
        Aeronautics.LOGGER.error("TannerHouseEffects: required fluid {} is not registered", id);
        return Fluids.EMPTY;
    }
    return fluid;
}
```

TFC — жёсткая зависимость, так что `null` означает сломанный мод; в этом
случае логируем ошибку и пропускаем соответствующую бочку, а не валим
генерацию мира.

### Сундук

`loot_table/tanner_house_chest.json` — девять записей:

| Предмет | Вес | Количество |
|---------|-----|------------|
| `tfc:hide/small/raw` | 5 | 1–4 |
| `tfc:hide/small/scraped` | 4 | 1–4 |
| `tfc:hide/medium/raw` | 3 | 1–3 |
| `tfc:hide/medium/scraped` | 2 | 1–3 |
| `tfc:hide/large/raw` | 1 | 1–2 |
| `tfc:hide/large/scraped` | 1 | 1–2 |
| `tfc:metal/knife/copper` | 4 | 1, dmg 0..0.85 |
| `tfc:stone/knife/sedimentary` | 4 | 1, dmg 0..0.85 |
| `tfc:stone/knife/igneous_extrusive` | 3 | 1, dmg 0..0.85 |

### Где генерируется

`#tfc_aeronautics:has_structure/tanner_house` — 7 TFC-биомов умеренной
полосы (plains, hills, lowlands, rolling_hills, river_valley,
terrace_upper, terrace_lower). Жаркие и холодные биомы исключены —
танин и известковое молоко там неуместны. Плотность: spacing 28,
separation 4, salt 100105 (≈ 1/784 чанков, по дизайну «1/800 блоков»).

---

## 15. Пропитанная джутовая ткань (Impregnated Burlap Cloth)

Полуфабрикат для будущих воздухоплавательных конструкций мода (оболочки
аэростатов, дирижаблей, планера). Джутовая ткань, пропитанная канифолью,
перестаёт пропускать воздух и влагу — то, что нужно для аэростата.

### Регистрация

Пакет `burlap/` (`src/main/java/ru/tfc_aeronautics/burlap/BurlapRegistration.java`)
содержит `DeferredHolder<Item, Item> IMPREGNATED_BURLAP_CLOTH` — vanilla
`Item` со стандартным `Properties`. Регистрируется в `Aeronautics.java`
рядом с `ResinRegistration.register(...)` и выводится в `CreativeTabs.MAIN`
после `resin_clump`.

### Рецепт

`data/tfc_aeronautics/recipe/barrel/impregnated_burlap_cloth.json` —
`type: tfc:barrel_sealed`, `duration: 7200` (6 игровых часов),
`input_item: tfc:burlap_cloth × 1`, `input_fluid: tfc_aeronautics:rosin × 100 мб`,
`output_item: tfc_aeronautics:impregnated_burlap_cloth × 1`. Канифоль
потребляется полностью (нет `output_fluid`). Параметры подобраны под
стандарт TFC для замачивания (как `large_leather.json` в TFC).

### Логика

Это первое «заметное» применение канифоли в моде: раньше `rosin` шла
только в производство (`rosin.json` растворяет `resin_clump` в спирте).
Пропитка ткани замыкает первый производственный цикл: собираем смолу
с брёвен → делаем канифоль → пропитываем ткань → будущая оболочка.

### Текстура

Берёт `tfc:textures/item/burlap_cloth.png` (16×16) и накладывает тёплый
янтарный фильтр (умножение RGB на ~`0.95/0.70/0.30` + общее
затемнение 0.85), чтобы передать «пропитанность» канифолью. Текстура
хранится в `src/main/resources/assets/tfc_aeronautics/textures/item/
impregnated_burlap_cloth.png`; модель — стандартный `item/generated`
с `layer0` на эту текстуру.

---

## 16. Нагревательные элементы (Heat Dealers)

Общая шина тепла между устройствами TFC и механиками Create. Нагревательный
элемент — это блок, который умеет ответить на вопрос «какая у тебя сейчас
температура». Ответ всегда в градусах Цельсия по шкале TFC (0…1600,
`Heat.maxVisibleTemperature()`), а не в грубых уровнях Create.

Смысл абстракции — один реестр вместо попарных интеграций. Без него каждая
механика, которой нужен нагрев (паровой двигатель, паровой вентиль,
`create:mixing`, дистиллятор, змеевик-конденсатор), решала бы задачу «а что за
блок подо мной» заново и своим способом.

### Реестр

`ru.tfc_aeronautics.heat.HeatDealer` — функциональный интерфейс с одним
методом `float getTemperature(Level, BlockPos, BlockState)`. Возвращает
`HeatDealer.NO_HEAT` (`-1f`), если блок сейчас не греет.

Реестр `HeatDealer.REGISTRY` — это `SimpleRegistry<Block, HeatDealer>` из
публичного API Create (`com.simibubi.create.api.registry.SimpleRegistry`), тот
же класс, на котором построен `BoilerHeater.REGISTRY`. Своего реестра мод не
изобретает: этот потокобезопасен, поддерживает провайдеры по тегам и уже
загружен в память.

Запрос делается статикой: `HeatDealer.findTemperature(level, pos)` либо
перегрузкой с готовым `BlockState`. Для случаев, когда нужно узнать «а это
вообще нагреватель?» без чтения block entity, есть `isHeatDealer(BlockState)`.

### Зарегистрированные блоки

| Блок | Реализация | Источник температуры |
|------|-----------|----------------------|
| `tfc:firepit` | `HeatDealers.FIREPIT` | `AbstractFirepitBlockEntity#getTemperature` |
| `tfc:stove`, `tfc:stove_pot`, `tfc:grill`, `tfc:pot` | `HeatDealers.FIREPIT` | то же — все четыре наследуют `FirepitBlock` |
| `tfc_aeronautics:heater` | `HeatDealers.HEATER` | `HeaterBlockEntity#getTemperature` |

`tfc:charcoal_forge` в реестр **намеренно не входит**: кузня тушится, если над
ней стоит блок (проверка по тегу `#tfc:charcoal_forge_invisible`), поэтому басин
или котёл сверху её просто погасит — регистрировать её как нагревательный
элемент бессмысленно.

Регистрация живёт в `heat/HeatDealerRegistration.java` и выполняется в
`FMLCommonSetupEvent` через `enqueueWork`: холдеры `TFCBlocks` на момент
конструирования мода ещё не разрешены.

Критерий «блок не греет» — температура `<= 0`, а не block-state property
`LIT`/`HEAT`. Это осознанное решение: костёр, у которого только что прогорело
топливо, ещё несколько минут остаётся раскалённым, и обрывать по нему рецепты
было бы неверно физически и раздражающе в игре.

### Маппинги в шкалы Create

У Create две несовместимые шкалы нагрева, и обе грубее TFC-градусов, поэтому
конвертация односторонняя — из °C.

`HeatDealers.toHeatLevel(float)` → `BlazeBurnerBlock.HeatLevel` (для басина):

| °C | HeatLevel |
|----|-----------|
| < 80 | `NONE` |
| 80…399 | `SMOULDERING` |
| 400…799 | `FADING` |
| 800…1399 | `KINDLED` |
| ≥ 1400 | `SEETHING` |

`HeatDealers.toBoilerHeat(float)` → шкала `BoilerHeater` (для парового котла):

| °C | SU |
|----|----|
| < 80 | `NO_HEAT` (-1) |
| 80…279 | 0 (пассивный) |
| 280…479 | 1 |
| 480…679 | 2 |
| 680…879 | 3 |
| 880…1079 | 4 |
| 1080…1279 | 5 |
| 1280…1479 | 6 |
| 1480…1600 | 7 |

`SU` суммируется по всем нагревателям под котлом в `BoilerData.activeHeat`;
потолок — `min(18, boilerSize / 4)`. Шаг 200 °C выбран так, чтобы каждое видимое
изменение температуры костра/нагревателя двигало стрелку SU. Старая формула
(`0 / 1 / 2` по полосам 800/1400 °C) давала одинаковый SU на огромных участках
шкалы — игрок не видел эффекта от разведения огня.

### Привязка к паровому котлу

Кода почти нет: `BoilerHeater.REGISTRY.registerProvider(...)` отдаёт адаптер
`HeatDealers::boilerAdapter` для любого блока, у которого есть `HeatDealer`.
Сигнатура адаптера совпадает с `BoilerHeater#getHeat`, поэтому передаётся
method reference'ом. Реестр Create публичный — миксин не нужен, и новые
нагреватели подключаются к котлу автоматически, без правок здесь.

### Привязка к `create:mixing` (миксин на басин)

С басином так не вышло. `BasinBlockEntity.getHeatLevelOf(BlockState)`
принимает **только** состояние блока, а температура костра живёт в его block
entity и из состояния не восстанавливается. Единственное место с доступом к
`level` и позиции — package-private `BasinBlockEntity#getHeatLevel()`, поэтому
`mixin/BasinBlockEntityMixin.java` инжектится туда на `HEAD`.

Два неочевидных момента:

- **Если под басином не зарегистрированный `HeatDealer`, миксин ничего не
  возвращает и просто отдаёт управление оригиналу.** Благодаря этому blaze
  burner и всё содержимое тега `#create:passive_boiler_heaters` (лава, магма,
  ванильные костры) продолжают работать как раньше — регрессии нет.
- **Поле `cachedHeatLevel` намеренно не заполняется.** Температура костра
  меняется каждый тик, пока он разгорается и остывает; закэшированный уровень
  заморозил бы басин на том, что он увидел первым. Выход на `HEAD` этот кэш
  обходит и перечитывает источник при каждом вызове.

### Отдача тепла вверх

`HeaterBlockEntity#tick()` вызывает
`HeatCapability.provideHeatTo(level, worldPosition.above(), Direction.DOWN, temperature)` —
ровно как это делают `AbstractFirepitBlockEntity` и `CharcoalForgeBlockEntity`
в TFC. Это push-канал, дополняющий pull-реестр: любой блок сверху,
выставляющий TFC-capability `BlockCapabilities.HEAT` (`IHeatConsumer`),
начинает греться от нагревателя без единой строчки специального кода.

### Как подключить свой блок

```java
HeatDealer.REGISTRY.register(MyRegistration.MY_BURNER.get(),
    (level, pos, state) -> /* температура в °C, либо HeatDealer.NO_HEAT */);
```

Одна строка в `HeatDealerRegistration#registerHeatDealers` — и блок сразу
работает и с басином, и с паровым котлом, и со всеми будущими потребителями.
Именно так будет подключён `tfc_aeronautics:spirit_burner`.

---

## 17. Топливо TFC в портативных двигателях

`simulated:portable_engine` — кинетический двигатель Simulated, аналог
Create-паровых машин: один слот под твёрдое топливо, на выходе 32 RPM.
Проверка «что считать топливом» в Simulated жёстко завязана на
`ItemStack.getBurnTime(RecipeType.SMELTING)` — ровно тот же путь, что и у
ванильной печи. Никаких тегов, рецептов или хардкода нет.

TFC регистрирует своё топливо параллельно, через `net.dries007.tfc.util.data.Fuel`
(см. [раздел 4](#4-нагреватель-heater) — нагреватель использует тот же источник).
Без перехвата TFC-предметы (`tfc:wood/log/oak`, `tfc:peat`, `tfc:ore/lignite`,
`minecraft:charcoal` с TFC-записью) для двигателя невидимы: их `getBurnTime`
возвращает 0, слот отвергает вставку.

### Перехват

`ru.tfc_aeronautics.portable_engine.PortableEngineFuelHandler` слушает
`FurnaceFuelBurnTimeEvent` на game bus с `EventPriority.HIGH`. На каждый вызов:

1. Ранний выход, если `Config.TFC_FUEL_IN_ENGINES.get() == false`.
2. `Fuel.get(stack)` из TFC; `null` — выход, ванильная логика остаётся.
3. `duration = Mth.floor(fuel.duration() * fuel.purity())`.
4. `event.setBurnTime(duration)` — внутри отменяет событие, останавливая
   default-priority листенеры.

`@EventBusSubscriber(modid = TFCAeronautics.MOD_ID)` без аргумента `bus` —
это `Bus.GAME`. Подключения в `TFCAeronautics.java` не нужно: аннотация
регистрирует хендлер сама через сканер FML — ровно как
`ShaftDamageHandler`, другой хендлер на game bus.

Запись `setBurnTime` автоотменяет событие при `burnTime >= 0`, поэтому
аннотация `@SubscribeEvent` идёт **без** `cancellable = true`.

### Почему `EventPriority.HIGH`

Событие `FurnaceFuelBurnTimeEvent` — `ICancellableEvent`. Как только любой
листенер вызывает `setBurnTime`, default-priority листенеры дальше не
получают событие. Чтобы выиграть гонку у модов, которые могут добавить
свой хендлер на нормальном приоритете (Tech Reborn, Mekanism, сторонние
TFC-аддоны), мы подписываемся на `HIGH`. `HIGHEST` не используется —
оставляем зазор для модов, которые захотят перебить уже TFC-оверрайд.

### Pure as a factor

Перемножение на `purity` — не косметика. Без него `minecraft:leaves`
(600 тиков, purity 0.25) стоит ровно столько же, сколько уголь
(1415 °C, 2000 тиков, purity 1.0): возобновляемый ресурс кормит двигатель
на полную. С purity leaves → 150 тиков, pinecone → 33, driftwood → 160.
Purity по умолчанию `1.0` (`Fuel.CODEC`), поэтому у TFC-записей без
явного `purity` (`coal`, `charcoal`, `peat`, `lignite`, planks) поведение
не меняется.

### Численные примеры

| Топливо | TFC duration | Purity | Engine burn time |
|---------|--------------|--------|------------------|
| `minecraft:coal` | 2000 | 1.0 | 2000 |
| `minecraft:charcoal` | 1800 | 1.0 | 1800 (vs vanilla 1600) |
| `tfc:wood/log/oak` | 1000 | 0.95 | 950 |
| `tfc:wood/planks/oak` | 900 | 1.0 | 900 |
| `tfc:peat` | 2500 | 1.0 | 2500 |
| `tfc:ore/lignite` | 2200 | 1.0 | 2200 |
| `minecraft:leaves` | 600 | 0.25 | 150 |
| `tfc:groundcover/pinecone` | 220 | 0.15 | 33 |
| `tfc:groundcover/driftwood` | 400 | 0.40 | 160 |

### Поверхность воздействия

Хук **глобальный**: касается не только `simulated:portable_engine`, но и
ванильной печи, коптильни, домны, Create Blaze Burner, Create Trains —
всего, что вызывает `ItemStack.getBurnTime`. Гейтинг по
`RecipeType.SMELTING` сознательно не используется: он даёт несогласованное
разделение (печь + двигатель да, коптильня + домна нет), а главное — не
изолирует двигатель от печи. В TFC-аддоне глобальная семантика
естественна: TFC-топливо должно гореть везде, где кто-то попросит.

Для сборок, где это поведение нежелательно, есть escape hatch —
`Config.TFC_FUEL_IN_ENGINES = false` в `common.toml` и `/reload`.

### Ограничения

- **Super-heat** для TFC-топлива остаётся `false`. TFC не в датамапе
  `create:superheated_blaze_burner_fuels`, поэтому `getNextSuperHeated()`
  возвращает 0, и `getGeneratedSpeed()` не удваивается. Это сознательно:
  удвоение скорости — прерогатива blaze cake, а не углей.
- **Гейт вставки.** `PortableEngineInventory.canInsertItem` зовёт
  `getBurnTime(info.type().getDefaultInstance())`. Все 49 TFC fuel JSON
  используют `item`/`tag` ингредиенты (без component-sensitive types), так
  что `Fuel.get(defaultInstance) ≡ Fuel.get(actualStack)`. Вставка и
  сгорание согласованы.
- **`/reload` mid-burn.** `Fuel.CACHE` перезагружается через
  `IndirectHashCollection.reloadAllCaches`. Никакой лок не нужен — это
  та же модель, что в `FireboxBlockEntity` и `AbstractFirepitBlockEntity`
  у TFC. Новые значения разрешаются на лету, горелка продолжает
  декрементироваться.
- **Миксинов нет.** Simulated не нужен на classpath: `getBurnTime`
  перехватывается на game bus, до того как Simulated-сервис успевает
  вернуть что-либо.

---

## 18. Скрытие TFC-кинематики

TFC содержит собственную, полностью независимую от Create подсистему
механического вращения: `RotationNetworkManager` + `Node`/`SourceNode`/
`SinkNode`/`AxleNode`, отдельные пакеты `common.blocks.rotation.*` и
`common.blockentities.rotation.*`, 39 Java-файлов (см. ресёрч
`tmp_docs/tfc_rotation_research.md`). Это конкурирует с Create-кинетикой
за ресурсы и внимание игрока, плюс раздаёт бесполезные TFC-ачивки
(`tfc:story/windmill`, `tfc:story/water_wheel`, и т.д.).

Мод скрывает TFC-кинематику на уровне **рецептов**: все 145 рецептов,
ведущих к TFC-вращательным блокам и их зависимостям, перекрыты
datapack-тенями в нашем моде. Так как `tfc_aeronautics` объявляет TFC
как обязательную зависимость (`mods.toml`, `required`), наш datapack
загружается строго после TFC и перетирает оригиналы по тому же пути
`data/tfc/recipe/<...>.json`. Никакого Java-кода не добавлено.

### Что блокируется

| Категория | Файлов | Что |
|---|---|---|
| `crafting/crankshaft.json` | 1 | `tfc:crankshaft` |
| `crafting/power_loom.json` | 1 | `tfc:power_loom` |
| `crafting/steel_pump.json` | 1 | `tfc:steel_pump` |
| `crafting/trip_hammer.json` | 1 | `tfc:trip_hammer` |
| `crafting/{lattice,rustic}_windmill_blade.json` | 2 | ножницы ветряка |
| `crafting/windmill_blade/white.json` | 1 | белая лопасть |
| `crafting/wood/{axle,bladed_axle,clutch,encased_axle,gear_box,water_wheel}/<wood>.json` | 120 | 6 типов × 20 пород |
| `anvil/steel_pipe.json` | 1 | `tfc:steel_pipe` (через наковальню) |
| `barrel/windmill_blade/<color>.json` × 16 | 16 | цветные лопасти (через бочку) |
| `barrel/bleach_windmill_blade.json` | 1 | отбеливание цветных → белую |
| **Итого** | **145** | |

### Формат пустышек

Все recipes — валидные, но выдают `minecraft:stick` вместо TFC-предмета:

- **Crafting** (`minecraft:crafting_shaped`, 127 файлов): 1×1 grid, ключ `X = stick`, result `1× stick`. `ShapedRecipePattern` требует ≥1 непустого ряда и непустой key, и валидатор 1.21.1 отвергает `count: 0`.
- **Anvil** (`tfc:anvil`, 1 файл `steel_pipe`): реальный `ingredient` (`c:sheets/steel`), `tier: 4` (wrought iron), `rules: ["draw_last"]`, result = stick.
- **Barrel** (`tfc:barrel_sealed`, 17 файлов): реальный `input_fluid` (`tfc:limewater` — всегда есть в TFC, парсер резолвит fluid при загрузке datapack и падает на неизвестных ID), `input_item = stick`, `output_item = stick`.

### Что НЕ блокируется

- `crafting/wood/loom/*.json` — loom не входит в ротационную сеть (отдельный hand-driven блок для ткачества).
- `crafting/bloomery/*.json`, `casting/*.json`, `heating/*.json`, `knapping/*.json` — не связаны с вращением.
- `tfc:brass_mechanisms` — используется в погодных приборах (anemometer, vane, observer, piston), не трогаем.

### Что НЕ делается (намеренно)

- **Предметы остаются в креатив-вкладках TFC и в JEI/EMI.** Косметический недостаток: игрок видит их в поиске, но скрафтить не может. Полное скрытие потребовало бы `BuildCreativeModeTabContentsEvent` + JEI/EMI plugin + блокировку размещения через `BlockEvent.EntityPlaceEvent` — не входит в текущий scope.
- **Уже размещённые блоки в старых мирах остаются как декорации** и продолжают тикать через TFC `RotationNetworkManager`. Полное отключение тиков потребовало бы mixin в TFC.
- **Ачивки TFC остаются доступными** (`/advancement grant @s only tfc:story/windmill` сработает). Чтобы скрыть — нужно положить пустышки в `data/tfc/advancement/story/...`.

### Где лежит

```
src/main/resources/data/tfc/recipe/
├── crafting/{6 standalone + windmill_blade/white + wood/{6 типов × 20 пород}}.json
├── anvil/steel_pipe.json
└── barrel/{bleach_windmill_blade + windmill_blade/16 цветов}.json
```

Скрипты-генераторы для воспроизводимости: `tmp/gen_disabled_recipes.sh`
(первый проход, невалидный — сохранён как история) и
`tmp/fix_disabled_recipes.sh` (второй проход, валидный).

### Диагностика при падении datapack

При апдейте TFC или смене версии `ShapedRecipePattern` может начать
отвергать пустышки. Симптом: экран "Errors in currently selected data
packs prevented the world from loading" при запуске мира. Лечение:
смотреть `logs/latest.log`, искать `ShapedRecipePattern.unpack` /
`RecipeManager.apply` → первый свалившийся recipe. Частые причины:

- пустой `pattern` / `key` — нужна хотя бы одна непустая строка и один ключ;
- `count: 0` в `result` — валидатор 1.21.1+ требует `count ≥ 1`;
- неизвестный fluid в `tfc:barrel_sealed.input_fluid.fluid` — заменить на реальный TFC-fluid (`tfc:limewater`, `tfc:red_dye`, и т.п.).

### Когда пересобирать

При каждом бампе версии TFC: продифференцировать
`code_references/TerraFirmaCraft/src/generated/resources/data/tfc/recipe/...`
против нашего `src/main/resources/data/tfc/recipe/...` — добавить новые
shadow-файлы для появившихся ротационных рецептов.

---

## 19. Простые замены рецептов (Recipe overrides)

Некоторые рецепты Create предполагают наличие ингредиентов, которых в TFC
нет вовсе (ванильные бочки, стандартные пластины) или они должны
использовать модовые tight sheets вместо тяжёлых plates. Для таких случаев
остаётся только простая замена одного-двух ingredients — отдельный JSON-файл
override-рецепта по пути оригинала в namespace источника
(`data/create/recipe/...`). Это тот же convention, что для переноса
milling/pressing/квен-моста — см. `feedback_recipe_override_convention.md`.

Сюда не пишутся:
- сложные рецепты-мосты (milling↔quern, spout+casting, anvil-совмещение) — у
  них свои подробные разделы выше;
- блокировка/скрытие рецептов (раздел 18);
- перенос recipes между namespace в рамках адаптации нового TFC-контента
  (tight sheets в Create pressing и т.п.) — это идёт в профильный plan
  (`plans/tight-sheets.md`).

### Актуальный список

| Override | Заменено | На |
|---|---|---|
| `data/create/recipe/crafting/kinetics/fluid_tank.json` | `c:plates/copper` (Create plates) | `tfc_aeronautics:metal/tight_sheet/copper` |
| | `c:barrels/wooden` (minecraft barrels) | `tfc:barrels` |
| `data/create/recipe/crafting/kinetics/white_sail.json` | `create:andesite_alloy` + `minecraft:wool` + `c:rods/wooden` (pattern `WS/SA`) | `tfc_aeronautics:composite` + `tfc:cloths` + `tfc:lumber` (pattern `PC/CI`) |
| `data/create/recipe/crafting/logistics/andesite_funnel.json` | `minecraft:dried_kelp` | `tfc:cloths` |
| `data/create/recipe/crafting/logistics/andesite_tunnel.json` | `minecraft:dried_kelp` | `tfc:cloths` |
| `data/simulated/recipe/mechanical_crafting/plunger_launcher.json` | `minecraft:slime_ball` (ключ `P`) | `c:slimeballs` (тег; см. [раздел 20](#20-замена-slimeball-на-tfcglue)) |
| `data/create/recipe/crafting/kinetics/super_glue.json` | Create-рецепт `["AS","NA"]` с `c:slimeballs + c:nuggets/iron + c:plates/iron` — невозможен в TFC-сборке (iron-теги пусты) | shapeless `tfc_aeronautics:metal/tight_sheet/steel + tfc:glue` (см. [раздел 20](#20-замена-slimeball-на-tfcglue)) |
| `data/aeronautics/recipe/white_envelope.json` | shaped `["WS","SW"]` с `minecraft:white_wool + minecraft:stick` → 4 | shaped `["CCC","C C"," R "]`: 5× `#tfc:cloths` + `tfc:rope` (снизу-середка) → 8 `aeronautics:white_envelope`. Выход ×2: TFC-ткань реже ванильной шерсти. Требует shadow-тег `tfc:cloths` |
| `data/aeronautics/recipe/{color}_envelope.json` (×15) | shaped `["WS","SW"]` с `minecraft:<color>_wool + minecraft:stick` → 4 | shapeless: `aeronautics:white_envelope` + `minecraft:<color>_dye` → 1 `aeronautics:<color>_envelope` (перекрашивание через ванильные красители) |
| `data/aeronautics/recipe/deploying/deploying_envelope_{color}.json` (×16) | `create:deploying` с `minecraft:<color>_wool + minecraft:stick` → 3 | тень-отключение: оба ингредиента `minecraft:bedrock` (недобываем → рецепт фактически мёртв) |

Для sail/funnel/tunnel потребовался shadow-тег `tfc:cloths`
(`data/tfc/tags/item/cloths.json`): burlap + wool + silk (других cloth items TFC не имеет).

Сюда же добавлять новые простые замены (зеркально — в `plans/recipe-overrides.md`).

---

## 20. Замена slimeball на `tfc:glue`

TFC регистрирует собственный клей — `tfc:glue` (`TFCItems.java:223`, простой `Item` без capability и без fluid-формы). Тематически это аналог ванильного slimeball: клейкий ингредиент для клейки, склеивания и пропитки. В Create и Simulated slimeball встречается во многих местах, и эта механика делает `tfc:glue` полностью взаимозаменяемым с ним — везде, где slimeball принимается сейчас, клей тоже будет принят.

Подробный дизайн: `docs/superpowers/specs/2026-08-15-tfc-glue-slimeball-substitution-design.md`. Реализация — план `plans/glue-substitution.md`.

### Подход

Большинство контекстов, где Create и Simulated принимают slimeball, уже фильтруют по тегу, а не по конкретному предмету. Поэтому основной путь — **расширить slimeball-теги** клеем. Исключения:

- Один рецепт Simulated (`plunger_launcher`) использует хардкод `minecraft:slime_ball` в JSON и не покрывается тегом — для него сделан shadow-override.
- Рецепт `create:super_glue` формально фильтрует по `c:slimeballs` (то есть подстановка тега работает), но **второй и третий ингредиенты** (`c:nuggets/iron`, `c:plates/iron`) в TFC-сборке пусты — TFC заменяет железо на wrought iron и не публикует `c:`-теги для nuggets/plates. Поэтому весь рецепт переписан на shapeless из двух TFC-шных предметов: `tfc_aeronautics:metal/tight_sheet/steel` + `tfc:glue`.

### Что покрывается

| Контекст | Где | Покрытие |
|----------|-----|----------|
| Create recipe `super_glue` | `data/create/recipe/crafting/kinetics/super_glue.json` | **полная замена** на shapeless `tight_sheet/steel + tfc:glue` (исходный recipe невозможен в TFC) |
| Create recipe `sticker` | `data/create/recipe/crafting/kinetics/sticker.json` | фильтр по `c:slimeballs` |
| Create recipe `sticky_mechanical_piston` | `data/create/recipe/crafting/kinetics/sticky_mechanical_piston.json` | фильтр по `c:slimeballs` |
| Create recipe `package_frogport` | `data/create/recipe/crafting/logistics/package_frogport.json` | фильтр по `c:slimeballs` |
| Create runtime: нанесение клея на chassis | `AbstractChassisBlock#useItemOn` | `Tags.Items.SLIMEBALLS` |
| Create runtime: конверсия механического поршня в sticky | `MechanicalPistonBlock#useItemOn` | `Tags.Items.SLIMEBALLS` |
| Simulated logic: merging glue | `simulated:merging_glue` item tag | `#c:slime_balls` (с подчёркиванием) |
| Simulated recipe `plunger_launcher` | `data/simulated/recipe/mechanical_crafting/plunger_launcher.json` | хардкод slimeball → shadow-override |
| Simulated recipe `honey_glue` (басин) | `data/simulated/recipe/filling/honey_glue.json` | **тень-отключение** (см. ниже) |

### Файлы

| Файл | Действие | Назначение |
|------|----------|------------|
| `src/main/resources/data/c/tags/item/slimeballs.json` | создать | добавить `tfc:glue` в `c:slimeballs` (Create-тег, без подчёркивания) |
| `src/main/resources/data/c/tags/item/slime_balls.json` | создать | добавить `tfc:glue` в `c:slime_balls` (Simulated-тег, с подчёркиванием) |
| `src/main/resources/data/simulated/recipe/mechanical_crafting/plunger_launcher.json` | создать | shadow оригинального рецепта; ключ `P`: `"item": "minecraft:slime_ball"` → `"tag": "c:slimeballs"` |
| `src/main/resources/data/create/recipe/crafting/kinetics/super_glue.json` | создать | полная замена: вместо `["AS","NA"]` (slimeball + iron nugget + iron plate) — shapeless `tfc_aeronautics:metal/tight_sheet/steel + tfc:glue`. Исходный рецепт в TFC-сборке невозможно скрафтить: `c:nuggets/iron` и `c:plates/iron` пусты, так как TFC заменяет железо на wrought iron и не публикует `c:`-теги для nuggets/plates |
| `src/main/resources/data/simulated/recipe/filling/honey_glue.json` | создать | тень, отключающая исходный басин-рецепт `create:filling`. Исходный рецепт требует `create:iron_sheet` (нет в TFC) + 500 мБ `c:honey` (тег пуст в TFC) → `simulated:honey_glue`. Новая тень требует `minecraft:bedrock` + несуществующий fluid-тег → `minecraft:stick`: никогда не сматчится, но остаётся валидным datapack-JSON, чтобы datapack-загрузчик не ругался |

### Что НЕ меняется

- **Java-код** — ни одного изменения, всё data-only.
- **Рецепт `simulated:honey_glue` через басин** — исходный `create:filling`-рецепт отключён, потому что в TFC-сборке он невозможен (`create:iron_sheet` не существует, `c:honey` пуст). Альтернативный путь к `simulated:honey_glue` — это наш собственный shapeless-рецепт `tfc:glue + tfc_aeronautics:resin_clump + tfc_aeronautics:metal/tight_sheet/steel` (`data/simulated/recipe/crafting/honey_glue.json`); сам предмет переименован в «Смоляной клей» (`Resin Glue`).
- **`SuperGlueItem.java:63`** — там формируется return-стек `minecraft:slime_ball`, это косметика, на поведение клея-как-ингредиента не влияет.
- **Ponder-сцены Simulated** — там slimeball используется только как визуальная подсказка.
- **Поведение slimeball** — slimeball по-прежнему работает во всех исходных рецептах без изменений; добавление `tfc:glue` аддитивно.

### Почему два тега, а не один

Create и Simulated исторически используют разные slimeball-теги:

- `c:slimeballs` (без подчёркивания) — NeoForge common tag, разрешается в `Tags.Items.SLIMEBALLS`. Create использует именно его.
- `c:slime_balls` (с подчёркиванием) — Simulated вводит свой tag и подключает его к `simulated:merging_glue` через `addTag`.

Оба тега нужно расширить, чтобы покрыть все контексты. Для shadow-override рецепта используется `c:slimeballs` как более распространённый и идиоматичный для Create.

### Smoke-проверка в игре

- [x] Скрафтить `super_glue` через новый shapeless-рецепт `tight_sheet/steel + tfc:glue` — должно сработать.
- [x] Скрафтить `sticker`, `sticky_mechanical_piston`, `package_frogport` — должны принимать `tfc:glue` (через тег `c:slimeballs`).
- [x] ПКМ по chassis с клеем в руке — должно приклеить.
- [x] ПКМ по механическому поршню с клеем — должно превратить в sticky.
- [x] Использовать клей на блоке merging glue — должно сработать.
- [x] Собрать `plunger_launcher` через Create mechanical craft с клеем вместо slimeball — должно сработать.

