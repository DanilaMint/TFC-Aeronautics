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
21. [Наковальни для остальных металлов (Tier-1 Anvils)](#21-наковальни-для-остальных-металлов-tier-1-anvils)
22. [Деревянные кронштейны по породе (TFC Wooden Brackets)](#22-деревянные-кронштейны-по-породе-tfc-wooden-brackets)
23. [Depot: крафт молотком по андезитовому корпусу (Hammer-craft Depot)](#23-depot-крафт-молотком-по-андезитовому-корпусу-hammer-craft-depot)
24. [TFC FOOD processing в Create-машинах](#24-tfc-food-processing-в-create-машинах)

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

## 24. TFC FOOD processing в Create-машинах

TFC сам по себе не предлагает механической автоматизации пищевого
производства — `tfc:barrel_sealed` годится для заквасок и длительной
выдержки, `tfc:pot` — для варки, наковальня — для формовки, — но для
промежуточных шагов (помол, замес, просушка, прессование сыра)
автоматизации нет. Этот раздел фиксирует проброс TFC FOOD шагов в
Create-машины с полной синхронизацией пищевых данных TFC (`tfc:food`
компонент: rot timer, `creationDate`, traits).

Трекер и мета-план — `plans/tfc-food-create-integration.md`.

### Общий принцип синхронизации rot timer

Для каждого шага:

1. TFC-рецепт (`tfc:advanced_shapeless_crafting`, `tfc:quern`, ...) уже
   синхронизирует food data — это baseline.
2. Create-аналог (`create:milling`, `create:mixing`, ...) — более
   быстрый, автоматизированный путь.
3. Если Create-путь не даёт тот же набор `ItemStackModifier`-ов
   (`tfc:copy_food`, `tfc:copy_oldest_food`), подключаем миксин:
   - `HEAD` ловит input **до** in-place `shrink(1)` через `.copy()`
     (см. [[feedback_mixin_itemstack_copy]]);
   - `TAIL` применяет `FoodCapability.updateFoodFromPrevious(input, output)`,
     или маршрутизирует через `ItemStackProvider.getSingleStack(input)`
     (когда нужны произвольные modifiers).

Формула `Cf = (1 - p) * T + p * Ci` с `p = newDecay / oldDecay`
сохраняет долю испорченности между input и output. `creationDate`
пересчитывается так, чтобы rot timer у муки из мельницы совпадал с
rot timer у муки из жернова, помолотого из того же зерна.

### Milling (grain → flour) — Create millstone

См. [раздел 2.3](#tfc_aeronauticsquern_milling--поддержка-tfc-модификаторов-в-мельнице)
(`tfc_aeronautics:quern_milling`): кастомный `RecipeType` extends
`MillingRecipe`, плюс `MillstoneBlockEntityMixin` для маршрутизации
через `ItemStackProvider.getSingleStack(capturedInput)`. Decay-таймер
муки из мельницы идентичен муке из жернова.

### Mixing (flour → dough) — Create basin + mixer

TFC'шное тесто (`tfc:food/{grain}_dough`) — базовый пищевой ингредиент,
который обычно получают через crafting grid + water bucket
(`tfc:advanced_shapeless_crafting`, `tfc:copy_oldest_food`). Альтернатива
— Create basin + mechanical mixer, с такой же синхронизацией rot timer.

Датаген: `generate/generate_mixing_recipes.py` → 6 JSON
`src/generated/resources/data/tfc_aeronautics/recipe/mixing/
{grain}_dough.json` (по одному на каждое зерно: wheat, barley, maize,
oat, rye, rice).

JSON-форма (пример для wheat, `wheat_dough.json`):

```json
{
  "type": "create:mixing",
  "ingredients": [
    { "item": "tfc:food/wheat_flour" },
    { "type": "neoforge:single", "amount": 100, "fluid": "minecraft:water" }
  ],
  "results": [{ "count": 1, "id": "tfc:food/wheat_dough" }]
}
```

Синхронизация rot timer: миксин
`src/main/java/ru/tfc_aeronautics/mixin/BasinMixingFoodDataMixin.java`
на `BasinOperatingBlockEntity.applyBasinRecipe`:

- `HEAD` обходит basin input inventory, ищет TFC flour
  (`tfc:food/*_flour` предикат по item-id) и сохраняет **копию**
  в `aeronautics$capturedFlour`. `.copy()` обязательно — иначе
  последующий in-place `shrink(1)` обнулит count у нашего снимка,
  и `ItemStack.copy()` внутри `updateFoodFromPrevious` отдаст
  `EMPTY` без FOOD-компонента → `tfc:copy_food` тихо срабатывает
  вхолостую. Та же ловушка, что и в `MillstoneBlockEntityMixin` (см.
  [раздел 2.3](#tfc_aeronauticsquern_milling--поддержка-tfc-модификаторов-в-мельнице)).
- `TAIL` обходит basin output inventory, и для каждого результата
  из `TFC_DOUGHS` set (явное перечисление шести TFC dough'ов:
  `tfc:food/{barley,maize,oat,rye,rice,wheat}_dough`) вызывает
  `FoodCapability.updateFoodFromPrevious(captured, output)`.
  Если у output нет `TFCComponents.FOOD` (например, recipe-result
  закешировался и `ItemStackHooks.onModifyItemStackComponents` не
  отработал) — прикрепляем свежий `FoodComponent` через
  `FoodCapability.getDefinition` + `new FoodComponent(def)`.

### Future

- **Dough → bread** — TFC выпекает хлеб в `tfc:pot` / `tfc:firepit`
  (нужна жарка). Create basin не подходит. Кандидаты: кастомная
  машина, либо адаптация `tfc:pot` под basin + heat через
  `tfc_aeronautics:heat_dealers` ([раздел 16](#16-нагревательные-элементы-heat-dealers)).
- **Sourdough starter** — TFC `tfc:barrel_sealed` 12-часовой рецепт.
  Возможен аналог через mixer spin-time или адаптация recipe type
  под sealed-семантику.
- **Dough → pasta** — TFC pasta shaping. Create `mechanical_press`
  через `tfc_aeronautics:stamping_press` ([раздел 3](#3-штамп-пресс-stamping-press))
  — кандидат.
- **Drying / smoking** — TFC drying мяса/рыбы/фруктов в pit/solar
  dryer. Автоматизация firepit/cooler.
- **Cheese pressing** — TFC cheese curds → cheese wheel через press.
  Аналог — `tfc_aeronautics:stamping_press` с другим фильтром.

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
| `data/create/recipe/crafting/kinetics/encased_chain_drive.json` | Create shapeless `andesite_casing` + 3× `c:nuggets/iron` — bypasses TFC-металлургию (iron-нугеты не требуют цепи) | shapeless `create:andesite_casing + create:shaft + tag:c:chains` (`show_notification: false`). Тег `c:chains` задан самим TFC и содержит 9 металлических цепей (bismuth_bronze, black_bronze, bronze, copper, wrought_iron, steel, black_steel, blue_steel, red_steel); ванльная железная цепь в тег не входит. Параллельно цинковый вариант `create:crafting/kinetics/encased_chain_drive_from_zinc` удалён через `BANNED_RECIPES` в `src/main/java/ru/tfc_aeronautics/recipe/RecipeRemoval.java` (по образцу `fluid_pipe`/`fluid_pipe_vertical`), чтобы chain drive добывался только через TFC-цепи |
| `data/create/recipe/crafting/kinetics/water_wheel.json` | Create `["SSS","SCS","SSS"]`: 8× `#minecraft:planks` + 1× `create:shaft` | TFC-style `["LPL","PAP","LPL"]`: 4× `#tfc:lumber` (углы) + 4× `#minecraft:planks` (бока) + 1× `create:shaft` (центр). Lumber — тег из 20 пород TFC, planks после TFC-override — тоже 20 TFC-пород (ванильные плахи TFC заменяет). Рецепт визуально повторяет TFC `tfc:wood/water_wheel/<wood>` (паттерн `LPL/PAP/LPL`), но принимает любое дерево и выдаёт `create:water_wheel` |
| `data/create/recipe/crafting/kinetics/rope_pulley.json` | Create 1×3: 1× `create:andesite_casing` + `#minecraft:wool` + `#c:plates/iron` → 1 | TFC-style 3×3 `RCR`/`RRR`/`RSR`: 1× `create:andesite_casing` (верх-середина) + 1× `tfc:metal/sheet/wrought_iron` (низ-середина) + 7× `tfc:rope` (остальные) → 1 `create:rope_pulley`. Мотивация: rope — естественный заменитель шерсти в TFC (плетёная верёвка); wrought iron sheet — кованый металл вместо Create-only iron plate. `show_notification: false` |
| `data/create/recipe/crafting/kinetics/whisk.json` | Create `[" C ","SCS","SSS"]` с `create:andesite_alloy` + `#c:plates/iron` | TFC-style ромб `[" R ","R R"," R "]`: 4× `tfc:metal/rod/wrought_iron` (стержни кованого железа по четырём сторонам, углы и центр пусты) → 1 `create:whisk`. `show_notification: false` (structural reshape). Шейдинг-тегов не требуется: `tfc:metal/rod/wrought_iron` — прямой item-id. Advancement `data/create/advancement/.../whisk.json` ссылается на тот же `create:crafting/kinetics/whisk`, поэтому засчитывается без правок |
| `data/create/recipe/crafting/kinetics/propeller.json` | Create `[" S ","SCS"," S "]` с `create:andesite_alloy` + `#c:plates/iron` — невозможен в TFC (andesite_alloy Create-only, `c:plates/iron` пуст) | TFC-style `["S S"," R ","S S"]`: 4× `tfc_aeronautics:metal/tight_sheet/wrought_iron` (углы) + 1× `tfc:metal/rod/wrought_iron` (центр) → 1 `create:propeller`. `show_notification: false`. Шейдинг-тегов не требуется. Recipe-id `create:crafting/kinetics/propeller` сохраняется, advancement Create засчитывается без правок |
| `data/tfc_aeronautics/recipe/crafting/kinetics/steel_propeller.json` | (новый рецепт, не замена) | тот же паттерн `["S S"," R ","S S"]`: 4× `tfc_aeronautics:metal/tight_sheet/steel` + 1× `tfc:metal/rod/steel` → 1 `create:propeller`. Параллельный вариант с wrought iron override'ом — игрок выбирает металл в верстаке |
| `data/create/recipe/crafting/kinetics/goggles.json` | Create shaped `[" S ","GPG"]` с `c:glass_blocks` + `c:plates/gold` + 1× `c:strings` | TFC-style шлем 3×3 `["SSS","S S","LPL"]`: 5× `c:strings` (контур шлема: 3 в ободе купола сверху + 2 по бокам, центр пуст — отверстие под линзы) + 2× `tfc:lens` (глаза, TFC glassworking) + 1× `tfc:metal/sheet/gold` (переносица, TFC anvil). `show_notification: false`. Шейдинг-тегов не требуется. Пустой слот — пробел, не `.` (1.21.1 парсер требует именно `' '`) |
| `data/create/recipe/crafting/kinetics/gearshift.json` | Create shapeless `andesite_casing` + `cogwheel` + tag `c:dusts/redstone` → 1 | shapeless `create:clutch` + `create:cogwheel` → 1. Мотивация: `clutch` уже содержит `andesite_casing` + `cogwheel`, поэтому это shortcut — экономит redstone и убирает необходимость собирать andesite_casing руками в TFC. Recipe-id сохраняется, advancement Create засчитывается без правок |
| `data/simulated/recipe/directional_gearshift.json` | Simulated shapeless `create:andesite_casing` + `create:cogwheel` + `minecraft:redstone_torch` + `create:shaft` → 1 | shapeless `create:clutch` + `create:gearshift` + tag `c:dusts/redstone` → 2. Мотивация: directional_gearshift = clutch + gearshift + redstone, поэтому собираем из тех же компонент вместо 4 разнородных. Выход ×2 — clutch и gearshift сами по себе дорогие, directional_gearshift — их сборка. Recipe-id сохраняется, advancement Simulated засчитывается без правок |
| `data/tfc_aeronautics/recipe/kinetics/clutch.json` | Create shapeless `create:andesite_casing` + `create:shaft` + tag `c:dusts/redstone` → 1 | shaped 3×3 `LCL`/`MSR`/`LCL`: 4× `#tfc:lumber` (углы) + 2× `create:andesite_casing` + 1× `tfc:brass_mechanisms` (центр) + 1× `create:shaft` + 1× tag `c:dusts/redstone` → 2 `create:clutch`. Мотивация: оригинал слишком дёшев (3 ингредиента, count 1); TFC-латунный механизм в центре + доски по углам дают механически осмысленный craft в TFC-контексте, count=2 компенсирует трёх-шаговый anvil-recipe для `brass_mechanisms`. `show_notification: false` (structural reshape). Шейдинг-тегов не требуется. **Ветка 2** скилла `recipe-override` — оригинал `create:crafting/kinetics/clutch` (recipe-id из пути `data/create/recipe/crafting/kinetics/clutch.json`) запрещён через `BANNED_RECIPES` в `src/main/java/ru/tfc_aeronautics/recipe/RecipeRemoval.java` |
| `data/create/recipe/crafting/kinetics/hand_crank.json` | Create shaped `["CCC", "  A"]` с 3× `#minecraft:planks` + 1× `create:andesite_alloy` → 1 | shaped `["CCC", "  A"]` с теми же ключами: `C = #tfc:lumber` + `A = tfc_aeronautics:composite` → 1 `create:hand_crank`. Мотивация: в TFC-сборке `#minecraft:planks` — это TFC-плахи (blocks), игроку нужны обработанные lumber-предметы как items. `tfc_aeronautics:composite` (Industrial Composite / Промышленный композит) — наш аналог `andesite_alloy`, производится barrel-рецептом `data/tfc_aeronautics/recipe/barrel/dry_composite.json`. Pattern и аутпут неизменны — простой sub-recipe override, не TFC-style reshape. `show_notification: false`. **Ветка 1** скилла `recipe-override` (recipe-id в namespace `create`, без `BANNED_RECIPES`). Шейдинг-тегов не требуется: `#tfc:lumber` уже в датапаке TFC (20 пород) и использован в 3 других наших override-рецептах (`clutch.json`, `water_wheel.json`, `white_sail.json`), `tfc_aeronautics:composite` — прямой item-id |
| `data/create/recipe/crafting/kinetics/chute.json` | Create shaped 3×1 `A/I/A` с `#c:plates/iron` (A) + `#c:ingots/iron` (I) → 4 | shaped 3×1 `A/I/A`: 2× `tfc_aeronautics:metal/tight_sheet/wrought_iron` (A) + 1× `tfc:metal/ingot/wrought_iron` (I) → 4 `create:chute`. Мотивация: `c:plates/iron` и `c:ingots/iron` в TFC-сборке пусты; tight_sheet — наш аналог plate, TFC ingot — стандартный слиток. Pattern и аутпут неизменны — простой sub-recipe override, не TFC-style reshape. `show_notification: false`. **Ветка 1** скилла `recipe-override` (recipe-id в namespace `create`, без `BANNED_RECIPES`). Шейдинг-тегов не требуется: оба ingredient'а — прямые item-id. Параллельно добавлены `data/tfc_aeronautics/recipe/heating/chute.json` (chute → 75 мБ `tfc:metal/cast_iron` @ 1535°C, сохранение массы: 300 мБ → 4 chute → 75 мБ каждый) и `data/tfc_aeronautics/tfc/item_heat/chute.json` (`heat_capacity: 7.2`) — см. `plans/chute.md` |
| `data/tfc_aeronautics/recipe/anvil/bracket_{wrought_iron,steel,cast_iron}.json` (×3) | Create shaped `["SSS","PCP"]` с 3× `#c:nuggets/iron` (S) + 2× `#c:ingots/iron` (P) + 1× `create:andesite_alloy` (C) → 4 (recipe-id `create:crafting/kinetics/metal_bracket`) | TFC-наковальня per-металл: tag `c:ingots/wrought_iron` (tier 3) → 4 / `c:ingots/steel` (tier 4) → 8 / `c:ingots/cast_iron` (tier 0) → 2 `create:metal_bracket`. Прогрессия count (2 → 4 → 8) отражает «качество металла = выход»: cast_iron на любой наковальне минимум, steel на 4-tier максимум. Мотивация: в TFC-сборке оригинал мёртв (`c:ingots/iron` / `c:nuggets/iron` пусты, `andesite_alloy` — Create-only); скоба — кованая листовая заготовка, естественно идёт через TFC-наковальню. Per-metal tag вместо `c:ingots` umbrella исключает скобы из латуни/бронзы/меди. Bend-паттерн `["bend_last","bend_second_last"]` (2 сгиба) делает рецепт визуально отличимым в JEI от hit-паттерна `tight_sheet_*`. **Ветка 2** скилла `recipe-override` — оригинал `create:crafting/kinetics/metal_bracket` запрещён через `BANNED_RECIPES` в `src/main/java/ru/tfc_aeronautics/recipe/RecipeRemoval.java`. Шейдинг-тегов не требуется: per-metal subtag'и `c:ingots/<metal>` уже в датапаке TFC |
| `data/create/recipe/crafting/kinetics/fluid_valve.json` | Create shapeless `c:plates/iron` (пуст в TFC) + `create:fluid_pipe` → 1 | shaped `[" S ","PPP","   "]`: 1× `tfc_aeronautics:metal/tight_sheet/wrought_iron` (верх-середина) + 3× `create:fluid_pipe` (средний ряд) → 3 `create:fluid_valve`. Выход ×3 — логика «1 труба-сегмент = 1 клапан»: 3 трубы-сегмента + один общий кованый лист-перемычка = 3 готовых клапана. `show_notification: false`. **Ветка 1** скилла `recipe-override` (recipe-id в namespace `create`, без `BANNED_RECIPES`). Шейдинг-тегов не требуется: оба ingredient'а — прямые item-id |
| `data/create/recipe/crafting/kinetics/steam_whistle.json` | Create shaped `["P", "C"]` с `#c:plates/gold` (P) + `#c:ingots/copper` (C) → 1 | тот же pattern, ключи `P = #c:sheets/gold` + `C = #c:ingots/copper` → 1 `create:steam_whistle`. Мотивация: `c:plates/gold` в TFC-сборке содержит только `create:golden_sheet` (Create-only золотой лист, требует mechanical press); `c:sheets/gold` — common-тег золотых листов, в котором TFC регистрирует `tfc:metal/sheet/gold` (получается через TFC anvil из `c:double_ingots/gold`). Замена сохраняет семантику «любой золотой лист», но привязывает свисток к TFC-металлургическому пути — игроку больше не нужен Create-press. `show_notification: false` (конвенция проекта, как `wrench.json`). **Ветка 1** скилла `recipe-override` (recipe-id в namespace `create`, без `BANNED_RECIPES`). Шейдинг-тегов не требуется: `c:sheets/gold` — common-тег |
| `data/tfc_aeronautics/recipe/anvil/copper_valve_handle.json` | Create crafting_shaped `["CCC", " S "]` с `#c:plates/copper` (C) + `create:andesite_alloy` (S) → 1 (recipe-id `create:crafting/kinetics/copper_valve_handle`) | TFC-наковальня tier 1: `tfc:metal/rod/copper` → 1 `create:copper_valve_handle`. Rules `["bend_last", "draw_not_last", "upset_not_last"]` — последний удар всегда BEND (финальный изгиб ручки), среди двух предыдущих должны быть и DRAW (вытяжка), и UPSET (утолщение) — порядок этих двух свободный. Три разные операции (не «просто три удара»), с гибкостью в первой части последовательности. `apply_bonus: false`. Мотивация: в TFC-сборке оригинал мёртв (`c:plates/copper` пуст, `andesite_alloy` — Create-only); ручка клапана — кованое изделие из прутка: вытянуть, осадить конец, согнуть (по аналогии с `whisk.json` / `propeller.json` для wrought iron). **Ветка 2** скилла `recipe-override` — оригинал `create:crafting/kinetics/copper_valve_handle` запрещён через `BANNED_RECIPES` в `src/main/java/ru/tfc_aeronautics/recipe/RecipeRemoval.java`. Шейдинг-тегов не требуется: `tfc:metal/rod/copper` — прямой item-id |
| `data/create/recipe/crafting/kinetics/piston_extension_pole.json` | Create shaped `["P","A","P"]` с `P = #minecraft:planks` + `A = create:andesite_alloy` → 8 | тот же pattern, ключи `P = #tfc:lumber` + `A = tfc_aeronautics:composite` → 2 `create:piston_extension_pole`. Pattern и серилизатор (`crafting_shaped`) неизменны — простой sub-recipe override в духе `hand_crank.json`. Выход снижен 8 → 2 (4× удорожание по материалу): piston_extension_pole — mid-game kinetic-компонент, и в TFC-сборке `#tfc:lumber` (обработанные доски как items) и `tfc_aeronautics:composite` (Industrial Composite — аналог `andesite_alloy`, two-step stamping + barrel) реже ванильных planks/Create-альяжа, поэтому «2 штуки за один крафт» балансит стоимость. `show_notification: false`. **Ветка 1** скилла `recipe-override` (recipe-id в namespace `create`, без `BANNED_RECIPES`). Шейдинг-тегов не требуется: `#tfc:lumber` — стандартный тег TFC (20 пород), `tfc_aeronautics:composite` — прямой item-id |
| `data/create/recipe/crafting/kinetics/linear_chassis.json` | Create shaped `[" P ","LLL"," P "]` с `create:andesite_alloy` (P, 2 шт.) + `#minecraft:logs` (L, 3 шт.) → 3 `create:linear_chassis` | тот же pattern, ключ `P = tfc_aeronautics:composite` → 3 `create:linear_chassis`. Мотивация: `create:andesite_alloy` в TFC-сборке недоступен (Create-only сплав, циклическая зависимость от mechanical mixer); `tfc_aeronautics:composite` — наш аналог, TFC barrel-рецепт `data/tfc_aeronautics/recipe/barrel/dry_composite.json`. Тот же свап, что у `hand_crank.json` / `piston_extension_pole.json`. `show_notification: false`. **Ветка 1** скилла `recipe-override` (без `BANNED_RECIPES`). Шейдинг-тегов не требуется: `#minecraft:logs` уже в датапаке, `tfc_aeronautics:composite` — прямой item-id |

Для sail/funnel/tunnel потребовался shadow-тег `tfc:cloths`
(`data/tfc/tags/item/cloths.json`): burlap + wool + silk (других cloth items TFC не имеет).
Для `encased_chain_drive` ничего не понадобилось: `c:chains` уже определён в датапаке TFC и закрывает все нужные металлы.

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

---

## 21. Наковальни для остальных металлов (Tier-1 Anvils)

TFC регистрирует наковальню только для металлов с `toolTier` (медь, бронзы, кованое железо, стали). Остальные 19 металлов — висмут, латунь, золото, никель, розовое золото, серебро, олово, цинк, стерлинговое серебро, чугун, кричное железо, слабые стали, высокоуглеродистые стали, неизвестный сплав — наковальни не имеют. Эта подсистема добавляет для каждого из них tier-1 наковальню: «даунгрейд»-вариант с полной функциональностью TFC-Forge, но с тиром ниже «настоящих» наковален из тех металлов, где они есть. Подробный дизайн: [`plans/anvil.md`](../plans/anvil.md).

### Регистрация

Точка входа: `ru.tfc_aeronautics.anvil.AnvilRegistration`. На статической инициализации перебирает `Metal.values()`, фильтрует металлы, у которых TFC уже регистрирует наковальню (`Metal.BlockType.ANVIL.has(metal)`), и для оставшихся 19 регистрирует блок + BlockItem.

- Блок: TFC-овский `net.dries007.tfc.common.blocks.devices.AnvilBlock` напрямую (без подкласса). Тир жёстко зашит как `1` в конструкторе. Это работает, потому что мы цепляем блок к `TFCBlockEntities.ANVIL` — его `static interactWithAnvil` хелпер и `AnvilContainer` меню-фабрика ожидают именно этот BE-тип.
- BlockEntity: TFC-овский `AnvilBlockEntity`, без подкласса. Его 2-arg конструктор хардкодит `TFCBlockEntities.ANVIL.get()` как `this.type`, поэтому `getType()` совпадает с тем, что меню-фабрика TFC передаёт в `level.getBlockEntity(pos, type)` — иначе открытие меню падает с `NoSuchElementException`.
- Свойства блока: `ExtendedProperties.of().mapColor(metal.mapColor()).noOcclusion().sound(SoundType.ANVIL).strength(10F, 10F).requiresCorrectToolForDrops().blockEntity(TFCBlockEntities.ANVIL)` — тот же набор, что в TFC-фабрике `Metal.BlockType.ANVIL`.
- Имя: `tfc_aeronautics:metal/anvil/<металл>` (например, `metal/anvil/bismuth`, `metal/anvil/high_carbon_red_steel`, `metal/anvil/unknown`). Совпадает с TFC-овским `tfc:metal/anvil/<металл>` по структуре — мы идём тем же путём, чтобы в логах и табе группа «metal/anvil» визуально стояла рядом.
- Карта `Map<Metal, DeferredHolder<...>>` в публичных полях `ANVILS` / `ANVIL_ITEMS` — для итерирования в креатив-табе и будущих рецептах.

`AnvilRegistration.register(modEventBus)` вызывается в `TFCAeronautics#TFCAeronautics` после `BurlapRegistration` и до `CreativeTabs`. Внутри регистрирует два `DeferredRegister`: `BLOCKS` и `ITEMS`. Также подписывается на `RegisterEvent` для реестра `BLOCK_ENTITY_TYPE` — когда TFC забиндит свой `TFCBlockEntities.ANVIL`, мы расширяем его `validBlocks` через рефлексию (см. ниже). Раньше расширение делалось жадно из supplier'а регистрации блока — это приводило к NPE при запуске, потому что `TFCBlockEntities.ANVIL` ещё не был забинден. Креатив-таб пополняется через `AnvilRegistration.ANVIL_ITEMS.values().forEach(i -> output.accept(i.get()))`.

### Расширение `TFCBlockEntities.ANVIL.validBlocks`

У TFC-овского `TFCBlockEntities.ANVIL.get().validBlocks` нет наших 19 блоков, и `BlockEntityType.create(pos, state)` валится с `IllegalStateException`, если в `validBlocks` нет блока, который пытаются поставить. Подменить BE-тип на свой — нельзя: TFC-овская меню-фабрика `AnvilContainer` хардкодит `TFCBlockEntities.ANVIL.get()` при поиске BE (см. `RegistrationHelpers.registerBlockEntityContainer`), поэтому иначе клиент не откроет меню.

Решение — рефлексивно дополнить `validBlocks` нашими 19 блоками в `extendTfcAnvilTypeValidBlocks()`. Метод вызывается из `RegisterEvent` для `BLOCK_ENTITY_TYPE` (т.е. когда TFC уже забиндил свой `ANVIL`-тип, а наши блоки уже зарегистрированы в `BLOCKS`):

- `BlockEntityType.validBlocks` — `private final Set<Block>`. В Minecraft 1.21.1 — `ObjectLinkedOpenHashSet` (мутабельный), `addAll(ours)` обычно работает. Если это всё же `ImmutableSet` (на каком-нибудь форке Mojang-а) — `UnsupportedOperationException` ловится, и поле подменяется новой мутабельной копией через рефлексию.
- Метод `synchronized` + `extendedTfcAnvilType` флаг, чтобы не прогонять рефлексию 19 раз подряд при cold-start.

Альтернатива через mixin рассматривалась, но отвергнута: `validBlocks = final`, mixin-ом не переопределить, а перезапись сеттера слишком хрупкая.

### Получение

Крафт из слитков (3×3 с пустым центром, 8 слитков на наковальню):

- `data/tfc_aeronautics/recipe/crafting/metal/anvil/<металл>.json` (×19) — shaped `minecraft:crafting_shaped`, паттерн `###` / ` # ` / `###`, ключ `#` → `c:ingots/<металл>`, результат — блок наковальни, count 1.
- Зеркалит форму рецепта TFC для собственных наковален (`data/tfc/recipe/crafting/metal/anvil/<металл>.json` — те же 3×3, та же полая середина), но использует одинарные слитки (`c:ingots/<металл>`) вместо двойных (`c:double_ingots/<металл>`). Суммарный расход металла тот же (8 слитков = 4 двойных слитка), но без обязательного промежуточного шага «слить двойной слиток».

### Модель и текстура

Полностью TFC-овские ассеты, ничего нового не рисуем:

- `assets/tfc_aeronautics/blockstates/metal/anvil/<металл>.json` — 4 facing-варианта, повороты `y=90/180/270/0` (как у TFC-овской `metal/anvil/<металл>.json`).
- `assets/tfc_aeronautics/models/block/metal/anvil/<металл>.json` — `parent: tfc:block/anvil`, `textures.all = tfc:block/metal/smooth/<металл>`, `textures.particle = tfc:block/metal/smooth/<металл>`.
- `assets/tfc_aeronautics/models/item/metal/anvil/<металл>.json` — однострочный `parent: tfc_aeronautics:block/metal/anvil/<металл>` (текстура `#all` наследуется через блок-модель, как и в TFC).

`UNKNOWN` — единственный специальный случай: `tfc:block/metal/smooth/unknown.png` существует (серый placeholder), так что рецепт и текстура работают штатно.

### Конвенции

- Имя блока: `metal/anvil/<металл>` (английское TFC-овское имя через `Metal.getSerializedName()`). Тот же путь, что у TFC-овских наковален (`tfc:metal/anvil/<металл>`), чтобы в логах/табе было визуальное соседство.
- Lang-ключ: `block.tfc_aeronautics.metal.anvil.<металл>` (с точками — слеши в путях конвертируются в точки по TFC-овской конвенции).
- Все 19 блоков — tier-1, без исключений. Tier передаётся вторым аргументом в конструктор `AnvilBlock`. Tier=0 не используется: TFC-овский `AnvilBlockEntityRenderer` имеет мёртвую ветку `tier == 0 ? 0.875f : 0.6875f` для Y-offset рендера предметов, из-за чего предметы «парят» над поверхностью. Rock anvil TFC использует другой `BlockEntity` и этот рендерер не вызывает, поэтому аналогия с rock anvil не работает.
- Подклассы `TierZeroAnvilBlock` и `CustomAnvilBlockEntity` НЕ нужны: `TFCBlockEntities.ANVIL.get()` принимается меню-фабрикой TFC, а 19 наших блоков мы добавляем в её `validBlocks` через рефлексию. Альтернативный путь (свой BE-тип) ломает клиентское открытие меню — подробнее см. секцию «Расширение `TFCBlockEntities.ANVIL.validBlocks`» выше.
- Рецепт `minecraft:crafting_shaped`, не `tfc:anvil`: tier-1 наковальня доступна с самого начала, не требуется наковальня-же для крафта наковальни.
- Lang-ключи: `block.tfc_aeronautics.metal.anvil.<металл>` → «<Metal> Anvil» (`en_us.json`, например «Bismuth Anvil») / «<металл-по-русски> наковальня» или «Наковальня из <металл-по-русски>» (`ru_ru.json`, по TFC-овской конвенции: «Висмутовая наковальня», «Наковальня из слабой синей стали»).

### Совместимость с TFC-овскими `tfc:anvil`-рецептами

`AnvilRecipe.getAll(level, input, MAX_TIER)` фильтрует рецепты по `minTier <= tier`. У всех металлических `tfc:anvil`-рецептов `minTier = metal.tier() >= 1`. С tier=1 наши наковальни автоматически принимают любые `tfc:anvil`-рецепты с `minTier = 1` (например, для олова или розового золота, если такие рецепты есть). Это by design: downgrade-наковальня должна быть совместима с низкотировыми рецептами.

### Чего НЕ делать

- **Не добавлять варианты tier≥2** для этих металлов. Идея подсистемы — единый tier-1 даунгрейд. Если понадобится «настоящая» наковальня из этих металлов — пусть это делает TFC.
- **Не использовать tier=0.** TFC-овский `AnvilBlockEntityRenderer` рисует предметы выше на `tier == 0` (мёртвая ветка `0.875f` vs `0.6875f`), и в vanilla TFC tier=0 для `AnvilBlockEntity` не используется — он зарезервирован для rock anvil, который использует другой BE.
- **Не модифицировать TFC-овские наковальни.** 9 «настоящих» TFC-наковален (`copper, wrought_iron, bronze, bismuth_bronze, black_bronze, steel, black_steel, blue_steel, red_steel`) живут как жили, фильтрация через `Metal.BlockType.ANVIL.has(metal)` их не затрагивает.
- **Не подменять BE-тип на свой.** TFC-овская `AnvilContainer` меню-фабрика хардкодит `TFCBlockEntities.ANVIL.get()` при поиске BE, поэтому свой BE-тип (`tfc_aeronautics:anvil`) приводит к крашу при открытии меню (`NoSuchElementException`). Используем TFC-овский BE-тип и расширяем его `validBlocks` через рефлексию — единственный способ сохранить совместимость с TFC-меню.
- **Не подменять `c:ingots` на `c:double_ingots`.** Суммарный расход металла одинаковый, но требовать предварительного слияния в двойной слиток — лишний шаг, который усложняет early-game прогрессию (tier-1 наковальни по дизайну дешёвые).

---

## 22. Цепи TFC в `chain_conveyor` (TFC-aware Chain Conveyor)

`create:chain_conveyor` жёстко зашит на `Items.CHAIN` в пяти местах Java-кода (`ChainConveyorConnectionHandler.isChain`, `ChainConveyorBlock.useItemOn`, `ChainConveyorBlock.onSneakWrenched`, `ChainConveyorBlockEntity.chainDestroyed` × 2, `ChainConveyorRenderer.renderChain`). Ни одно из них не сверяется с тегом — datapack-тени бесполезны. TFC поставляет 9 металлических цепей (`bismuth_bronze, black_bronze, bronze, copper, wrought_iron, steel, black_steel, blue_steel, red_steel`), все объединены в `c:chains`. Игрок не может построить `chain_conveyor` из стальной цепи.

Решение — **полный source-copy** исходников Create в наш пакет `ru.tfc_aeronautics.chain` под id `tfc_aeronautics:chain_conveyor`. Никаких mixin-ов: 16 verbatim-копий + 2 frogport-файла + 3 инфраструктурных файла регистрации. Старый `create:chain_conveyor` остаётся в мире (забанен по рецепту и убран из creative-таба `create:base`); миграция не делается.

> **Disclaimer по устаревшей спеке:** документ
> `docs/superpowers/specs/2026-08-16-chain-conveyor-tfc-chains-design.md`
> описывает провалившийся mixin-подход (см.
> `InvalidMixinException ... contains non-private static method
> aeronautics$renderChainWithTexture`). **Не использовать как референс.**
> Актуальный план реализации — [`plans/chain-conveyor.md`](plans/chain-conveyor.md).

### Решение: source-copy

Полностью скопированы 16 Java-классов из
`code_references/Create/src/main/java/com/simibubi/create/content/kinetics/chainConveyor/`
в наш пакет `ru.tfc_aeronautics.chain` (общие) и `ru.aeronautics.client.chain`
(клиентские). Имена короткие (без префикса `ChainConveyor`):
`Block`, `BlockEntity`, `ConnectionHandler`, `ConnectionPacket`,
`InteractionHandler`, `Package`, `Shape`, `RoutingTable`,
`ChainPackageInteractionPacket`, `ServerboundRidingPacket`,
`ClientboundRidingPacket` — плюс клиентские `Renderer`, `Visual`,
`RidingHandler`, `PackageInteractionHandler`. Префикс остаётся только там,
где он исторически устоялся и его трогать вредно (`ChainPackageInteractionPacket`,
`ClientboundRidingPacket`).

Классы Create (`com.simibubi.create.content.kinetics.chainConveyor.*`)
импортировать нельзя — нигде в наших файлах. Базовые публичные классы Create
(`KineticBlock`, `KineticBlockEntity`, `AllTags`, `AllPartialModels`,
`FrogportBlockEntity`, `PackagePortTarget`, `CreateRegistries`) импортируются
свободно. Сеттеры `Items.CHAIN` / `Blocks.CHAIN` заменены на per-connection
lookup (см. ниже).

### Per-connection тип цепи

Публичное поле в `ChainConveyorBlockEntity` (рядом с `connections: Set<BlockPos>`):

```java
public Map<BlockPos, ResourceLocation> connectionChains = new HashMap<>();
```

Ключ — относительный `BlockPos` (как у `connections`); значение — `ResourceLocation`
предмета-цепи. Карта сериализуется под NBT-ключом `ConnectionChains` в
`write(...)` и `writeSafe(...)` (две точки — синк клиент/сервер). `read(...)`
десериализует обратно. **Lazy-fallback** на `Items.CHAIN.getKey()` для соединений
из `connections`, отсутствующих в `connectionChains` (старые чанки без нового
NBT-ключа).

#### Жизненный цикл `connectionChains`

| Действие | Что делается |
|---|---|
| `addConnectionTo(BlockPos target, ResourceLocation chainItemId)` | Новая двухпараметровая сигнатура: `connectionChains.put(localTarget, chainItemId)` сразу после `connections.add(...)`. |
| `removeConnectionTo(BlockPos target)` | `connectionChains.remove(localTarget)` параллельно `connectionStats.remove(localTarget)`. |
| `chainDestroyed(BlockPos, boolean, boolean)` | Оба места `new ItemStack(Items.CHAIN, ...)` и `new ItemStack(Blocks.CHAIN.asItem(), ...)` → `new ItemStack(getChainItemForConnection(target), ...)`. |
| `transform(BlockEntity, StructureTransform)` (контрапции) | Перенести записи map по новым относительным смещениям вместе с `connections`. |

### Хелперы на BE

```java
public Item getChainItemForConnection(BlockPos localTarget) {
    ResourceLocation rl = connectionChains.getOrDefault(localTarget,
                                                        Items.CHAIN.getKey());
    return BuiltInRegistries.ITEM.get(rl);
}

public ResourceLocation getChainTextureForConnection(BlockPos localTarget) {
    ResourceLocation rl = connectionChains.getOrDefault(localTarget,
                                                        Items.CHAIN.getKey());
    if (rl.getNamespace().equals("minecraft")) {
        return ResourceLocation.withDefaultNamespace("block/chain");
    }
    // TFC: tfc:metal/chain/<metal> → tfc:item/metal/chain/<metal>
    String last = rl.getPath().substring(rl.getPath().lastIndexOf('/') + 1);
    return ResourceLocation.fromNamespaceAndPath(rl.getNamespace(),
                                                 "item/metal/chain/" + last);
}
```

Маппинг item → текстура:

| Цепь | Текстура |
|---|---|
| `minecraft:chain` | `minecraft:block/chain` |
| `tfc:metal/chain/wrought_iron` | `tfc:item/metal/chain/wrought_iron` (атлас предмета!) |
| остальные 8 TFC-металлов | `tfc:item/metal/chain/<metal>` |
| Любая другая цепь из `c:chains` | fallback на vanilla |

> В TFC атлас предмета (`item/metal/chain/...`) отличается от атласа блока
> (`block/metal/chain/...`) — это разные `.png`. Используем именно item-атлас,
> как в tooltip и в руке.

### API

```java
// Новая сигнатура — id цепи передаётся в BE:
void addConnectionTo(BlockPos target, ResourceLocation chainItemId);

// Чтение для рендера / дропа:
Item getChainItemForConnection(BlockPos localTarget);
ResourceLocation getChainTextureForConnection(BlockPos localTarget);
```

Снаружи `addConnectionTo` зовётся из `ChainConveyorConnectionPacket.applySettings`
на `connect=true`: после успешного `addConnectionTo` на обеих сторонах пишем
`chain.getItem().builtInRegistryHolder().key().location()` в `connectionChains`
на обеих BE (с относительными смещениями — `localTargetForSource = targetPos - be.pos`,
`localSourceForTarget = be.pos - targetPos`). На `connect=false` refund через
`getChainsFromInventory` подменяется на lookup типа из BE и
`placeItemBackInInventory` правильного предмета — игрок получает обратно ровно
ту же цепь, которую потратил.

### Frogport-интеграция

`PackagePortTarget` в Create фильтрует по типу `BlockEntity` — поэтому
`create:chain_conveyor` и наш — две независимые сети. Чтобы фрогпорт мог
стрелять пакетами **в обе** сети, регистрируем собственный target:

- `chain/ChainConveyorFrogportTarget.java` — подкласс
  `com.simibubi.create.content.logistics.packagePort.PackagePortTarget`.
  Логика тела скопирована из `PackagePortTarget.ChainConveyorFrogportTarget`
  (`code_references/Create/.../packagePort/PackagePortTarget.java:69-203`):
  CODEC, STREAM_CODEC, поля `chainPos`/`connection`/`flipped`,
  методы `setup`/`getIcon`/`export`/`register`/`deregister`/
  `getExactTargetLocation`/`canSupport`/`getType` + вложенный `Type`.
  Замены: импорты `ChainConveyorBlockEntity`/`ChainConveyorPackage` → наши;
  `getIcon()` возвращает `new ItemStack(ChainConveyorRegistration.CHAIN_CONVEYOR.get())`.
- `chain/ChainConveyorPackagePortTargets.java` — аналог Create'
  `AllPackagePortTargetTypes`. Использует
  `DeferredRegister.create(CreateRegistries.PACKAGE_PORT_TARGET_TYPE,
  "tfc_aeronautics")` (публичный API Create, см.
  `code_references/Create/.../api/registry/CreateRegistries.java:36`),
  регистрирует entry `tfc_aeronautics:chain_conveyor` под
  `ChainConveyorFrogportTarget.Type::new`. Вызов
  `register(IEventBus)` из `ChainConveyorRegistration.register()`.

`ChainConveyorBlockEntity.tick()` уже обходит `connections` и стреляет в
`FrogportBlockEntity` (`ppbe.startAnimation(box.item, false)`) — это логика
скопирована verbatim, и наша BE импортирует
`com.simibubi.create.content.logistics.packagePort.frogport.FrogportBlockEntity`,
поэтому работает без правок.

### Скрытие оригинала

- **Recipe:** `create:crafting/kinetics/chain_conveyor` → `BANNED_RECIPES` в
  `src/main/java/ru/tfc_aeronautics/recipe/RecipeRemoval.java`.
- **Creative tab:** `src/client/java/ru/aeronautics/client/ChainConveyorCreativeTabFilter.java`
  подписан на `BuildCreativeModeTabContentsEvent` и удаляет
  `create:chain_conveyor` из `create:base`. ResourceKey —
  `ResourceKey.create(Registries.CREATIVE_MODE_TAB,
  ResourceLocation.fromNamespaceAndPath("create", "base"))`.

### Рецепт

Новый файл `src/main/resources/data/tfc_aeronautics/recipe/crafting/kinetics/chain_conveyor.json`
— shaped crafting по образцу Create, выход ×2:

```json
{ "type": "minecraft:crafting_shaped",
  "category": "misc",
  "key": { "A": { "item": "create:large_cogwheel" },
            "C": { "item": "create:andesite_casing" } },
  "pattern": [ " C ", "CAC", " C " ],
  "result": { "count": 2, "id": "tfc_aeronautics:chain_conveyor" } }
```

Под нашим неймспейсом (`tfc_aeronautics/recipe/...`), **не** под `create/...` —
recipe-id должен отличаться от забаненного `create:crafting/kinetics/chain_conveyor`.
Прецедент `data/create/recipe/crafting/kinetics/encased_chain_drive.json` (см.
раздел 19) переопределяет рецепт под исходным id; здесь не переопределение, а
**новый** рецепт.

В паре — advancement:
`src/main/resources/data/tfc_aeronautics/advancement/recipes/misc/crafting/kinetics/chain_conveyor.json`.

### Ограничения (явно)

- **Авто-миграция** `create:chain_conveyor` → `tfc_aeronautics:chain_conveyor`
  в существующих мирах **не делается**. Старые блоки остаются как есть, образуют
  отдельную сеть (разные `BlockEntityType`).
- **Две независимые сети:** `create:chain_conveyor` и наш — два отдельных
  контура. Frogport умеет стрелять в обе (две независимые target-записи), но
  пакеты не «протекают» между сетями.
- **Display Link / Smart Observer** адреса нашего конвейера — **не
  поддерживаются** (out of scope; требует копирования
  `PackageAddressDisplaySource` + `SmartObserverBlockEntity` или mixin).
- **Ponder-сцены** под наш блок — out of scope (стандартные сцены Create для
  `create:chain_conveyor` работают как есть; тип цепи — чисто визуальная
  деталь).
- **Звуки, специфичные для металла цепи** — out of scope (для всех цепей
  используется vanilla-звук `Blocks.CHAIN.defaultBlockState().getSoundType()`).
- **Display Link / Smart Observer** — out of scope.
- **Цепи модов вне TFC** — работают через тег `c:chains`, никакого
  спец-обработчика.
- **chainCost** — distance-based стоимость (`max(round(d / 2.5), 1)`) не
  меняется; тип металла в cost не учитывается.

### Файловое дерево

#### Java (21 файл)

**Общие (16 классов из Create + 3 инфраструктурных + 2 frogport = 21):**

| Путь | Назначение |
|---|---|
| `src/main/java/ru/tfc_aeronautics/chain/Registration.java` | `DeferredRegister`-ы `BLOCKS`/`ITEMS`/`BLOCK_ENTITY_TYPES`. `CHAINS_CONVEYOR`, `CHAIN_CONVEYOR_ITEM`, `CHAIN_CONVEYOR_BE`. `register(IEventBus)` дополнительно вызывает `ChainConveyorPackagePortTargets.register(bus)`. |
| `src/main/java/ru/tfc_aeronautics/chain/Packets.java` | Регистрация 5 payload-ов через `CatnipServices.NETWORK` (catnip-platform — публичный API из Create-инфраструктуры). |
| `src/main/java/ru/tfc_aeronautics/chain/Block.java` | Копия `ChainConveyorBlock`; `Items.CHAIN` → тег `c:chains`; refund в `onSneakWrenched` через BE-lookup; ссылки на `AllBlocks.CHAIN_CONVEYOR`/`AllBlockEntityTypes.CHAIN_CONVEYOR` → наши. |
| `src/main/java/ru/tfc_aeronautics/chain/BlockEntity.java` | Per-connection `connectionChains`, write/read/writeSafe под ключом `ConnectionChains`, lazy-fallback на vanilla chain, хелперы `getChainItemForConnection`/`getChainTextureForConnection`. |
| `src/main/java/ru/tfc_aeronautics/chain/ConnectionHandler.java` | `instanceof ChainConveyorBlock`/`ChainConveyorBlockEntity` → наши; `isChain` → тег `c:chains`. |
| `src/main/java/ru/tfc_aeronautics/chain/ConnectionPacket.java` | Наш packet id; refund через per-connection type; пишет `chain.getItem().builtInRegistryHolder().key().location()` в обе BE на `connect=true`. |
| `src/main/java/ru/tfc_aeronautics/chain/InteractionHandler.java` | Только замена `instanceof` и перенос `loadedChains` статика. |
| `src/main/java/ru/tfc_aeronautics/chain/ChainPackageInteractionPacket.java` | Наш packet id; тип BE → наш. |
| `src/main/java/ru/tfc_aeronautics/chain/ServerboundRidingPacket.java` | Только packet id. |
| `src/main/java/ru/tfc_aeronautics/chain/ClientboundRidingPacket.java` | Только packet id. |
| `src/main/java/ru/tfc_aeronautics/chain/ServerChainConveyorHandler.java` | Замена ссылок на наши packet-классы. |
| `src/main/java/ru/tfc_aeronautics/chain/Shape.java` | Копия без правок. |
| `src/main/java/ru/tfc_aeronautics/chain/RoutingTable.java` | Копия без правок. |
| `src/main/java/ru/tfc_aeronautics/chain/Package.java` | Внутренняя ссылка на BE тип → наш. |
| `src/main/java/ru/tfc_aeronautics/chain/FrogportTarget.java` | Подкласс `PackagePortTarget`; см. §Frogport-интеграция. |
| `src/main/java/ru/tfc_aeronautics/chain/PackagePortTargets.java` | `DeferredRegister` в `CreateRegistries.PACKAGE_PORT_TARGET_TYPE` под id `tfc_aeronautics:chain_conveyor`. |

**Клиентские (5 файлов):**

| Путь | Назначение |
|---|---|
| `src/client/java/ru/aeronautics/client/chain/Renderer.java` | `RenderTypes.chain(CHAIN_LOCATION)` → `RenderTypes.chain(be.getChainTextureForConnection(localPos))`. `renderChain` принимает дополнительный параметр `ResourceLocation chainTex`. |
| `src/client/java/ru/aeronautics/client/chain/Visual.java` | Только `instanceof`. |
| `src/client/java/ru/aeronautics/client/chain/RidingHandler.java` | Только `instanceof`. |
| `src/client/java/ru/aeronautics/client/chain/PackageInteractionHandler.java` | Только `instanceof`. |
| `src/client/java/ru/aeronautics/client/chain/ClientSetup.java` | `RegisterRenderersEvent` для BER. |

**Creative-tab фильтр (1 файл):**

| Путь | Назначение |
|---|---|
| `src/client/java/ru/aeronautics/client/ChainConveyorCreativeTabFilter.java` | Подписчик `BuildCreativeModeTabContentsEvent` для скрытия `create:chain_conveyor` из `create:base`. |

#### Ассеты (9 файлов)

| Путь | Назначение |
|---|---|
| `src/main/resources/data/tfc_aeronautics/recipe/crafting/kinetics/chain_conveyor.json` | Новый recipe (см. §Рецепт). |
| `src/main/resources/data/tfc_aeronautics/advancement/recipes/misc/crafting/kinetics/chain_conveyor.json` | Recipe-unlock advancement. |
| `src/main/resources/assets/tfc_aeronautics/blockstates/chain_conveyor.json` | `{ "variants": { "": { "model": "tfc_aeronautics:block/chain_conveyor/block" } } }`. |
| `src/main/resources/assets/tfc_aeronautics/models/item/chain_conveyor.json` | `{ "parent": "tfc_aeronautics:block/chain_conveyor/item" }`. |
| `src/main/resources/assets/tfc_aeronautics/models/block/chain_conveyor/block.json` | `{ "parent": "create:block/chain_conveyor/block" }`. |
| `src/main/resources/assets/tfc_aeronautics/models/block/chain_conveyor/item.json` | `{ "parent": "create:block/chain_conveyor/item" }`. |
| `src/main/resources/assets/tfc_aeronautics/models/block/chain_conveyor/guard.json` | `{ "parent": "create:block/chain_conveyor/guard" }`. |
| `src/main/resources/assets/tfc_aeronautics/models/block/chain_conveyor/shaft.json` | `{ "parent": "create:block/chain_conveyor/shaft" }`. |
| `src/main/resources/assets/tfc_aeronautics/models/block/chain_conveyor/wheel.json` | `{ "parent": "create:block/chain_conveyor/wheel" }`. |
| `src/main/resources/data/tfc_aeronautics/loot_table/blocks/chain_conveyor.json` | Self-drop loot table. |
| `src/main/resources/assets/tfc_aeronautics/lang/en_us.json` | `"block.tfc_aeronautics.chain_conveyor": "Chain Conveyor"` + ключи ошибок подключения. |
| `src/main/resources/assets/tfc_aeronautics/lang/ru_ru.json` | Те же ключи по-русски. |

> Модель `chain.json` в Create отсутствует — поэтому в нашем дереве её нет.
> Текстуры не копируем — подтягиваются через `parent`-ссылку на
> `create:block/chain_conveyor/...`.

### Существующие файлы — что меняется

| Файл | Изменение |
|---|---|
| `src/main/java/ru/tfc_aeronautics/TFCAeronautics.java` | Добавить `ChainConveyorRegistration.register(modEventBus);` рядом с другими `.register(...)`. |
| `src/main/java/ru/tfc_aeronautics/CreativeTabs.java` | `output.accept(ChainConveyorRegistration.CHAIN_CONVEYOR_ITEM.get());` в `displayItems`. |
| `src/main/java/ru/tfc_aeronautics/recipe/RecipeRemoval.java` | Добавить `ResourceLocation.fromNamespaceAndPath("create", "crafting/kinetics/chain_conveyor")` в `BANNED_RECIPES`. |

### Совместимость и edge-cases

- **TFC не загружен** — `c:chains` содержит только `minecraft:chain` (TFC — единственный источник тега). Поведение совпадает с vanilla.
- **Create не загружен** — мод бездействует, никакие регистрации не активируются.
- **Цепь удалена из реестра другим модом** — `getChainItemForConnection` падает на `Items.CHAIN.getKey()` через fallback; текстура — vanilla; NPE нет.
- **Legacy-чанк без `ConnectionChains` NBT** — `connectionChains` пустая; lazy-fallback на vanilla chain в каждом хелпере.
- **TFC цепь без `Blocks.CHAIN`-эквивалента** — `Blocks.CHAIN.defaultBlockState().getSoundType()` всё равно используется для звука/частиц (частицы декоративные).
- **Контрапция (SContraption)** — `transform(...)` обязан перенести `connectionChains` вместе с `connections`. Без этого тип цепи теряется при перемещении конструкции.
- **Old-world с `create:chain_conveyor`** — остаётся как есть, не соединяется с нашим (разные `BlockEntityType`). Сети независимы.

### Верификация (статическая)

Рантайм-проверка запрещена `CLAUDE.md`; только статически:

```bash
# Должно быть пусто (все Items.CHAIN и Blocks.CHAIN заменены на per-connection lookup):
grep -rn "Items\.CHAIN\|Blocks\.CHAIN" src/main/java/ru/tfc_aeronautics/chain/ src/client/java/ru/aeronautics/client/chain/

# Должно быть пусто (не импортим Create'овские chain-conveyor классы):
grep -rn "import com\.simibubi\.create\.content\.kinetics\.chainConveyor" src/main/java/ru/tfc_aeronautics/chain/ src/client/java/ru/aeronautics/client/chain/

# Должно быть пусто (статик CHAIN_LOCATION больше не используется):
grep -rn "CHAIN_LOCATION" src/main/java/ru/tfc_aeronautics/chain/ src/client/java/ru/aeronautics/client/chain/

# Должно быть пусто (не импортим чужой ChainConveyorBlockEntity):
grep -rn "com\.simibubi\.create\.content\.kinetics\.chainConveyor\.ChainConveyorBlockEntity" src/main/java/ru/tfc_aeronautics/chain/ src/client/java/ru/aeronautics/client/chain/

./gradlew compileJava        # main sources
./gradlew compileClientJava  # клиент-рендер
./gradlew build              # полная сборка + datagen + jar
```

### Smoke-проверка в игре

Без рантайма — передать пользователю для прогона в Prism-лаунчере:

- [ ] Подключить два `chain_conveyor` (наших) ванильной цепью — связь работает.
- [ ] Подключить `tfc:metal/chain/wrought_iron` — работает.
- [ ] Подключить `tfc:metal/chain/steel` — работает (любой из 9 TFC-металлов).
- [ ] Sneak+wrench disconnect — возврат той же цепи, которой подключал.
- [ ] Уничтожить `chain_conveyor` — drop той же цепи по каждому подключению.
- [ ] Multi-segment BE: одно подключение бронзой, другое сталью — разные текстуры на сегментах, drop правильного типа.
- [ ] Frogport в нашу сеть — принимает и стреляет пакеты в обе стороны.
- [ ] Frogport из `create:chain_conveyor` в наш — НЕ работает (разные сети); наоборот — тоже не работает.
- [ ] В creative-табе `create:base` нет иконки `create:chain_conveyor`.
- [ ] Recipe `create:crafting/kinetics/chain_conveyor` отсутствует в recipe manager.
- [ ] Перезайти в мир / перезагрузить чанк — типы цепей сохраняются, рендер совпадает.
- [ ] Save → load — типы цепей сохраняются через серверный restart.



## 22. Деревянные кронштейны по породе (TFC Wooden Brackets)

Create регистрирует единственный `create:wooden_bracket` — vanilla-style, один предмет и один блок. В мире TFC это означало бы, что любой кронштейн выглядит одинаково, независимо от того, из какого дерева стол или обшивка дома. Подсистема заменяет это на 20 per-wood вариантов, привязанных к TFC-овскому списку пород: `tfc_aeronautics:wood/bracket/<wood>` для каждого `<wood>` из 20.

Поведение ПКМ по shaft/cog/pipe полностью наследуется от Create-овских `BracketBlock` / `BracketBlockItem` — никакой своей логики не пишем, нужен только тон.

### Регистрация блоков и предметов

Точка входа: `ru.tfc_aeronautics.bracket.WoodenBracketRegistration`. На статической инициализации по списку WOODS цикл делает:

- `BLOCKS.register("wood/bracket/" + wood, () -> new WoodenBracket(...))` — подкласс `BracketBlock` без переопределений (всё уже объявлено в родителе: `AXIS_ALONG_FIRST_COORDINATE`, `TYPE`).
- `ITEMS.register("wood/bracket/" + wood, () -> new WoodenBracketItem(block, new Item.Properties()))` — подкласс `BracketBlockItem`, тоже без логики, нужен только для типа.

Публичные `BRACKETS` / `BRACKET_ITEMS` (`Map<String, DeferredHolder<...>>`) — для креатив-таба и будущих рецептов.

Свойства: `BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.5F, 0.5F).noOcclusion().sound(SoundType.WOOD)` — дерево-материал, без occlusion (полупрозрачный для рендера).

`WoodenBracketRegistration.register(modEventBus)` подключается в `TFCAeronautics#TFCAeronautics` после `ChainConveyorRegistration`. `CreativeTabs.MAIN.displayItems` добавляет все 20 предметов через `BRACKETS.keySet().forEach(wood -> output.accept(BRACKET_ITEMS.get(wood).get()))`.

### Геометрия и текстурирование

- Геометрия — `create:block/bracket/{cog|pipe|shaft}/{ground|wall}` (Blockbench в коде Create), 6 базовых моделей родителей, которые `WoodenBracketBlockStateProvider` ребиндит по текстурам. На каждый wood получается 6 per-wood моделей (×20 = 120), каждая — `withExistingParent("wood/bracket/<type>/<ground|wall>_<wood>", "create:block/bracket/<type>/<ground|wall>").texture("bracket", tfc_aeronautics:block/wood/bracket/bracket_<wood>).texture("plate", tfc_aeronautics:block/wood/bracket/bracket_plate_<wood>)`.
- Blockstate: 36 вариантов на wood (= 2 `axis_along_first` × 6 `facing` × 3 `type`: cog/pipe/shaft). Rotation-таблица взята один-в-один из Create-овского `wooden_bracket.json` (per-type поворот не меняется) — реализована в `WoodenBracketBlockStateProvider.rotation(facing, alongFirst)`.
- Item-модель: `withExistingParent("wood/bracket/<wood>", "create:block/bracket/item").texture("bracket", ...).texture("plate", ...)` — родительская Blockbench-геометрия та же, что у Create.

### Текстуры

40 PNG (20 × 2: `bracket_<wood>.png` + `bracket_plate_<wood>.png`) генерируются скриптом `generate/generate_wooden_bracket_textures.py` на основе двух Create-овских эталонов (`bracket_wooden.png`, `bracket_plate_wooden.png`). Алгоритм:

1. Берётся медиана RGB центральной 50% TFC-овской планки (`assets/tfc/textures/block/wood/planks/<wood>.png`) — это целевой wood-тон.
2. Каждый пиксель Create-эталона обесцвечивается до ЧБ по luminance-формуле с premultiply-alpha: `gray = round((0.299·R + 0.587·G + 0.114·B) · α/255)`. Затем `gray` рескейлится пропорционально так, чтобы самый яркий непрозрачный пиксель стал ровно `#FFFFFF` (`scale = 255 / max_gray`). Тёмное зерно остаётся тёмным, но динамический диапазон растягивается до полного.
3. Финальный цвет: `R = round(gray' / 255 · wood_r)` (то же для G, B), α — как у Create. То есть самый яркий пиксель читается как **полный** wood-тон породы, а не его тёмная доля.
4. Пишется под `src/generated/resources/assets/tfc_aeronautics/textures/block/wood/bracket/` — datagen видит их как обычные ассеты мода.

Скрипт идемпотентен: перезапуск переписывает 40 PNG одними и теми же значениями.

### Рецепты крафта

20 per-wood рецептов в `data/tfc_aeronautics/recipe/crafting/wood/bracket/<wood>.json`:

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "show_notification": false,
  "key": { "P": { "item": "tfc:wood/lumber/<wood>" } },
  "pattern": [ "PPP", "P P" ],
  "result": { "count": 1, "id": "tfc_aeronautics:wood/bracket/<wood>" }
}
```

Шаблон шлема: 5 lumber в форме перевёрнутой U. Генерируются скриптом `generate/generate_wooden_bracket_recipes.py`.

Per-wood item-id выбран потому, что TFC не даёт per-wood tag для lumber — общий `tfc:lumber` сломал бы per-wood идентичность результата (через крафт из ольхи получался бы «дубовый» кронштейн).

### Бан vanilla рецепта

`create:crafting/kinetics/wooden_bracket` добавляется 7-м аргументом в `RecipeRemoval.BANNED_RECIPES` (`ImmutableSet.of(...)`). Миксин `RecipeManagerMixin` стрипает его из `byName` / `byType` после каждого reload, поэтому в JEI он не виден, а в верстаке — крафт-чек даёт «no recipe». Замена через per-wood crafting-рецепты выше.

### Что НЕ делается

- Нет осмысленного аналога для `tfc:metal/chain/<металл>` — кронштейны декоративно-функциональные, не металлические; эту нишу закрывает Create-овский `metal_bracket`, который тоже запрещён через `RecipeRemoval.BANNED_RECIPES` и не покрывается per-wood версиями.
- Текстуры — статические, не генерируются в момент сборки как часть `runData`. Скрипт `generate/generate_wooden_bracket_textures.py` нужно прогнать вручную до `runData` (или до игры), иначе datagen упадёт на missing textures.
- Lang-строки для предметов и блоков не добавлены — TODO, появится в подсистеме Localization.

### Верификация (статическая)

```bash
./gradlew compileJava                                          # main sources
python3 generate/generate_wooden_bracket_textures.py           # 40 PNG под src/generated
python3 generate/verify_wooden_bracket_textures.py             # spot-check: 40 OK rows (brightest pixel = wood median)
./gradlew runData                                              # 20 blockstate + 120 model + 20 item под src/generated
# Альтернатива runData: Python-эмиттер даёт тот же набор 160 JSON, но без JVM-стартапа.
python3 generate/generate_wooden_bracket_assets.py             # 20 blockstate + 120 model + 20 item под src/generated
python3 generate/generate_wooden_bracket_recipes.py            # 20 recipe JSON под src/main/resources

# Должно быть 0 (никаких упоминаний wooden_bracket без per-wood шага):
grep -rn "WoodType\|woodType" src/main/java/ru/tfc_aeronautics/bracket/

# После runData в src/generated должны появиться (per-wood × штук):
ls src/generated/resources/assets/tfc_aeronautics/blockstates/wood/bracket/   # 20 .json
ls src/generated/resources/assets/tfc_aeronautics/models/block/wood/bracket/  # 120 .json
ls src/generated/resources/assets/tfc_aeronautics/models/item/wood/bracket/   # 20 .json
```

### Smoke-проверка в игре

Без рантайма — передать пользователю для прогона в Prism-лаунчере:

- [ ] `/give @s tfc_aeronautics:wood/bracket/oak` появляется в инвентаре и в creative-табе мода.
- [ ] ПКМ по `create:shaft` разными кронштейнами — ставятся, цвет соответствует породе.
- [ ] ПКМ по `create:large_cogwheel` и `create:fluid_pipe` — тоже работает, переключается в нужный `type` (cog / pipe / shaft) автоматически.
- [ ] Крафт: 5 oak-lumber в шлем-форме `["PPP", "P P"]` → 1 `tfc_aeronautics:wood/bracket/oak`. Тот же рецепт для каждой породы, ингредиент — соответствующий lumber.
- [ ] Крафт: 5 `tfc:wood/planks/oak` в той же форме → рецепт НЕ срабатывает (lumber ≠ planks).
- [ ] JEI: 20 per-wood рецептов видны. Create-овский `create:wooden_bracket` рецепт не виден.
- [ ] В creative-табе — все 20 предметов рядом, в алфавитном порядке.

## 23. Depot: крафт молотком по андезитовому корпусу (Hammer-craft Depot)

**Mechanic:** кликнуть любым молотком (`c:tools/hammer`) по верхней грани блока `create:andesite_casing` (андезитовый корпус, при условии, что блок над ним — воздух), чтобы превратить его в `create:depot`.

**Аналогия:** копия TFC-механики создания каменной наковальни — `code_references/TerraFirmaCraft/.../blocks/rock/RockConvertableToAnvilBlock.java` (per-block override `useItemOn`). У нас `андезитовый корпус` — не наш блок, поэтому та же логика перенесена в event-listener.

**Реализация:** `src/main/java/ru/tfc_aeronautics/depot/DepotCraftHandler.java`.
- common bus `@EventBusSubscriber(modid = TFCAeronautics.MOD_ID)`
- слушает `PlayerInteractEvent.RightClickBlock`
- проверяет `event.getHitVec().getDirection() == Direction.UP`, `level.getBlockState(pos.above()).isAir()`, `state.getBlock() == AllBlocks.ANDESITE_CASING.get()`, `held.is(HAMMERS)` (TagKey `c:tools/hammer`)
- на сервере: `level.setBlockAndUpdate(pos, AllBlocks.DEPOT.get().defaultBlockState())`
- на обеих сторонах: `event.setCanceled(true)` + `setCancellationResult(InteractionResult.CONSUME)` — стандартное использование блока не срабатывает (андезитовый корпус не открывает GUI/не ставится).

**Запрет оригинала:** `create:crafting/kinetics/depot` (recipe-id из `data/create/recipe/crafting/kinetics/depot.json`) добавлен в `BANNED_RECIPES` в `src/main/java/ru/tfc_aeronautics/recipe/RecipeRemoval.java`.
