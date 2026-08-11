# Roadmap

Статус реализованных механик мода. Будущие планы (PR, дополнительные языки) и ещё не начатые рефакторы — намеренно не включены.

| Раздел | Готово |
|-|-|
| Metal Powders | 6/6 ✅ |
| Industrial Composite | 20/20 ✅ |
| Resin | 4/4 ✅ |
| Rosin | 5/5 ✅ |
| Tight Sheets | 7/7 ✅ |
| Saw Blade | 5/5 ✅ |
| Impregnated Burlap Cloth | 5/5 ✅ |
| Heater | 7/12 |
| Stamping Press | 11/15 |
| Create Spout + TFC Casting | 2/2 ✅ |
| Quern / Millstone Sync | 9/9 ✅ |
| Shaft Damage | 5/6 |
| Worldgen Structures | 22/22 ✅ |
| Ponder | 4/6 |
| Localization | 1/6 |

## Metal Powders

- [x] **Регистрация**
  - [x] 8 metal powders: copper, tin, zinc, bismuth, cast_iron, gold, silver, nickel
  - [x] Item models для каждого порошка
- [x] **Текстуры**
  - [x] `tfc_aeronautics/textures/item/powder/bismuth.png`
  - [x] `tfc_aeronautics/textures/item/powder/cast_iron.png`
  - [x] `tfc_aeronautics/textures/item/powder/copper.png`
  - [x] `tfc_aeronautics/textures/item/powder/gold.png`
  - [x] `tfc_aeronautics/textures/item/powder/nickel.png`
  - [x] `tfc_aeronautics/textures/item/powder/silver.png`
  - [x] `tfc_aeronautics/textures/item/powder/tin.png`
  - [x] `tfc_aeronautics/textures/item/powder/zinc.png`
- [x] **Рецепты**
  - [x] `tfc_aeronautics/recipe/heating/<metal>_powder.json`
  - [x] `create:crushing` рецепты
  - [x] `tfc:quern` рецепты из слитков

## Industrial Composite

Заменяет «andesite alloy» как базовый переходный материал между TFC и Create.

- [x] **Регистрация**
  - [x] `dry_composite`
  - [x] `composite`
- [x] **Текстуры**
  - [x] `composite.png` 
  - [x] `dry_composite.png`
- [x] **Рецепты** 
  - [x] Shapeless: cast_iron powder + `igneous_gravels` tag → dry_composite
  - [x] Barrel instant: 25 mB limewater + dry_composite → composite
- [x] **TFC-интеграция**
  - [x] Heating: composite @300 °C → `create:shaft`
  - [x] `tfc/item_heat/composite.json`
- [x] **Item application (корпуса из TFC-брёвен)**
  - [x] `andesite_casing.json` — `stripped_logs` + composite → andesite_casing
  - [x] `copper_casing.json` — `stripped_logs` + copper ingot → copper_casing
  - [x] `brass_casing.json` — `stripped_logs` + brass ingot → brass_casing
- [x] **Теги**
  - [x] `tfc_aeronautics:stripped_logs` — 20 TFC-пород
  - [x] `tfc_aeronautics:igneous_gravels` — 7 igneous gravel

## Resin

- [x] **Resin clump**
  - [x] Регистрация item `tfc_aeronautics:resin_clump`
- [x] **Текстура**
  - [x] `tfc_aeronautics/textures/item/resin_clump.png` 
- [x] **Resin Strip Handler** — `BlockEvent.BlockToolModificationEvent`
  - [x] Выпадение resin_clump при AXE_STRIP на `tfc_aeronautics:can_collect_resin` (5 conifer-пород)
  - [x] Config: `resinDropChance` (0.0–1.0, default 0.15)

## Rosin

- [x] **Rosin (жидкость)**
  - [x] Регистрация: `tfc_aeronautics:rosin` (`MixingFluid.Source/Flowing`), `rosin_bucket` (`BucketItem`), `LiquidBlock`, Client extension (`FluidClientExtensions`) — amber-тинт
  - [x] TFC-интеграция: Shadow `data/tfc/tags/fluid/ingredients.json` (rosin для TFC casting)
- [x] **Barrel-рецепт rosin**
  - [x] 50 mB `tfc_aeronautics:strong_alcohol` + 1× resin_clump → 50 mB rosin (1000 ticks sealed)
  - [x] Тег `tfc_aeronautics:strong_alcohol` (vodka, rum, whiskey, rye_whiskey, corn_whiskey)
- [x] **Barrel-рецепт impregnated burlap cloth**
  - [x] 100 mB rosin + 1× burlap_cloth → 1× impregnated_burlap_cloth (7200 ticks sealed)

## Tight Sheets

Тонкие листы Cu / wrought iron / steel.

- [x] **Регистрация**
  - [x] `tfc_aeronautics:metal/tight_sheet/{copper,wrought_iron,steel}`
  - [x] Item models
- [x] **Текстуры**
  - [x] Для всех 3 металлов
- [x] **Получение**
  - [x] TFC anvil recipes (`recipe/anvil/tight_sheet_*.json`)
  - [x] Create pressing (override `create/recipe/pressing/tight_sheet_*.json`)
- [x] **TFC Heating**
  - [x] tight_sheet → 100 mB metal (3 heating recipes)
  - [x] `tfc/item_heat/*tight_sheet.json`

## Saw Blade

- [x] **Регистрация**
  - [x] `tfc_aeronautics:saw_blade` — Item
- [x] **Текстуры**
  - [x] PNG + item model
- [x] **Крафт**
  - [x] Anvil tier 3: wrought_iron sheet → saw_blade (`recipe/anvil/saw_blade.json`)
- [x] **TFC Heating**
  - [x] saw_blade @1535 °C → 200 mB cast_iron (`recipe/heating/saw_blade.json`)
  - [x] `tfc/item_heat/saw_blade.json`
- [x] **Интеграция с Create saw** (`data/create/recipe/crafting/kinetics/mechanical_saw.json`)

## Impregnated Burlap Cloth

- [x] **Регистрация**
  - [x] `tfc_aeronautics:impregnated_burlap_cloth` — Item
- [x] **Текстуры**
  - [x] PNG + item model
- [x] **Получение**
  - [x] Barrel sealed: 100 mB rosin + 1× burlap_cloth → 1× impregnated_burlap_cloth (7200 ticks)
- [x] **Использование**
  - [x] Belt connector override — допускает `impregnated_burlap_cloth` как замену кожи
- [x] **Локализация**
  - [x] `en_us.json` + `ru_ru.json`

## Heater

Create-совместимый нагреватель с TFC-интеграцией.

- [x] **Ядро**
  - [x] Регистрация `tfc_aeronautics:heater` (block + item + BE)
  - [x] `IItemHandler` на всех гранях + `IFluidHandler` на DOWN
  - [x] TFC-интеграция: fuel + bellows + Encased Fan + HeatingRecipe → molten tank
  - [x] `LIT` block-state + light emission 14 при горении
  - [x] Двух-статусная модель через blockstate variants (lit/unlit)
  - [x] Макс-температура через `ValueSettingsBehaviour` (0..MAX_TEMP, шаг 50 °C)
  - [x] Анимированное пламя через `HeaterBlockEntityRenderer` (Y-bob + scale flicker)
- [ ] **Текстуры** (используются заглушки)
  - [ ] `textures/block/heater_side.png`
  - [ ] `textures/block/heater_top_off.png`
  - [ ] `textures/block/heater_top_on.png`
  - [ ] `textures/block/heater_bottom.png`
  - [ ] `textures/block/heater_flame.png`

## Stamping Press

Create-пресс с TFC-наковальней: heat-gated anvil recipe на ударе.

- [x] **Ядро блока**
  - [x] Регистрация `tfc_aeronautics:stamping_press` (block + item + BE)
  - [x] Reuse Create press model + текстуры; анимация головы через `AllPartialModels.MECHANICAL_PRESS_HEAD`
  - [x] TFC anvil recipe lookup на ударе (input + filter item, heat-gated)
  - [x] 8.0 SU stress impact
  - [x] Запретить basin снизу (canSurvive) + пропустить basin processing
  - [x] Flywheel `StampingPressVisual` (SingleAxisRotatingVisual + press head)
  - [x] Vanilla `StampingPressRenderer` (KineticBlockEntityRenderer + FilteringRenderer)
  - [x] Кастомный partial `STAMPING_PRESS_HEAD`
- [x] **Фильтр**
  - [x] `FilteringBehaviour` + `StampingPressFilterSlot` на задней грани
  - [x] Перенос слота фильтра на перпендикулярную грань
  - [x] `StampingPressFrameTickHandler` — value-box frame на обоих перпендикулярных гранях
- [ ] **Визуал и звуки**
  - [ ] Подключить bbmodel-набор (head + shaft + 5 граней) в blockstate — сейчас используется 1 модель
  - [ ] Squeak/creak-звук вращения вала
  - [ ] Anvil strike-звук удара
  - [ ] Заменить модель + текстуры Create на TFC-flavoured custom

## Create Spout + TFC Casting

- [x] Зарегистрировать `BlockSpoutingBehaviour` против `tfc:mold_table` (drains `recipe.getFluidIngredient().amount()` mB, кладёт результат в `OUTPUT_SLOT`)
- [x] Guard против double-cast: пропустить, если mold stack пустой, mold уже содержит fluid, или `OUTPUT_SLOT` занят

## Quern / Millstone Sync

- [x] **Milling-зеркала**
  - [x] `data/create/recipe/milling/{ore_*, powder/*, salt, charcoal, flux, graphite, saltpeter, sulfur, sylvite, lime_dye, canola_paste}.json`
  - [x] Пропустить multi-ingredient / compound (gem+ore powders, plant dyes) — остаются quern-only
  - [x] Пропустить `bone_meal.json` — Create уже определяет `bone.json` (superset)
- [x] **TFC quern → Create milling**
  - [x] Quern-рецепты для 8 металлических порошков в `data/tfc_aeronautics/recipe/quern/`
- [x] **Свой RecipeType + Mixin**
  - [x] Зарегистрировать `tfc_aeronautics:quern_milling` RecipeType + Serializer (читает TFC-shapes через `ItemStackProvider.CODEC`)
  - [x] Mixin в `MillstoneBlockEntity` (`tick` / `process` / `canProcess`) — роутит наш тип через `ItemStackProvider.getSingleStack(input)`
- [x] **Зерно → мука**
  - [x] 6 flat-зеркал на quern-shaped `tfc_aeronautics:quern_milling` рецепты в `data/tfc_aeronautics/recipe/milling/food/`
- [x] **Инфраструктура и доки**
  - [x] Включить `[[mixins]]` в `neoforge.mods.toml` + wire `tfc_aeronautics.mixins.json`
  - [x] Обновить DOCS.md «Получение»: `only_quern`, scope зеркал, новый `quern_milling` RecipeType + mixin

> **Известная проблема**: datagen-провайдеры `MetalCrushingRecipeProvider` / `AeronauticsDatagen` удалены из кодовой базы, но их выход в `src/generated/resources/data/create/recipe/milling/*` остался (~50 файлов-сирот). Файлы загружаются рантаймом, но не пересоздаются при `./gradlew runData`. При следующем прогоне без восстановленных провайдеров они исчезнут.

## Shaft Damage

- [x] **Базовый урон**
  - [x] Зарегистрировать `tfc_aeronautics:shaft` damage type + death message
  - [x] Hurt living entities при контакте с bare shafts/cogwheels, scaled по RPM (64 → 160)
  - [x] Andesite/brass encased shafts и cogwheels — safe
  - [x] Knock perpendicular к оси вращения + crunch sound
  - [x] Config: start RPM, lethal RPM, lethal damage, multiplier, knockback base / per RPM, sound volume (8 параметров)
- [ ] **Contraptions**
  - [ ] Распространить механику на shafts на движущихся contraptions

## Worldgen Structures

- [x] **Фреймворк**
  - [x] `AtmosphereSpec`, `MaterialConfig`, `LocalMaterialProcessor` — climate-aware подбор wood/soil/rock
  - [x] `AtmosphericStructure`, `AtmosphericTemplateStructure`, `AtmosphericTemplatePiece`
  - [x] `GraveyardMaterialProcessor` — для ancient_graveyard (обратная совместимость)
  - [x] Все структуры используют `minecraft:random_spread` (spacing/salt)
- [x] **Ancient Graveyard**
  - [x] `data/tfc_aeronautics/worldgen/structure/ancient_graveyard.json` (spacing 32, salt 10387312)
  - [x] NBT-шаблон + loot `graveyard_vessel` (`AncientGraveyardLoot.roll()`)
  - [x] Биомный тег `has_structure/ancient_graveyard`
- [x] **Ancient Shelter**
  - [x] `worldgen/structure/ancient_shelter.json` (spacing 22, salt 100101)
  - [x] NBT-шаблон + vessel + ash loot
  - [x] `AncientShelterEffects` (climate-aware `AncientShelterLoot.roll()`)
  - [x] Биомный тег `has_structure/ancient_shelter`
- [x] **Farmer House**
  - [x] `worldgen/structure/farmer_house.json` (spacing 26, salt 100102)
  - [x] NBT-шаблон + vessel + tool rack loot
  - [x] `FarmerHouseEffects` + `FarmerHouseCrops.pick()`
  - [x] Биомный тег `has_structure/farmer_house`
- [x] **Rich Graveyard**
  - [x] `worldgen/structure/rich_graveyard.json` (spacing 32, salt 100103)
  - [x] Подземная камера, NBT-шаблон + chest loot (`RichGraveyardEffects`)
  - [x] Биомный тег `has_structure/rich_graveyard` (8 биомов)
- [x] **Tanner House**
  - [x] `worldgen/structure/tanner_house.json` (spacing 28, salt 100105)
  - [x] NBT-шаблон + chest + barrel loot (`TannerHouseEffects`)
  - [x] Биомный тег `has_structure/tanner_house`
- [x] **Теги**
  - [x] `has_structure/{ancient_graveyard, ancient_shelter, farmer_house, rich_graveyard, tanner_house}` — все biome-теги

## Ponder

- [x] **Регистрация**
  - [x] `PonderPlugin` зарегистрирован через `PonderIndex.addPlugin`
  - [x] `PonderTag` — `Kinetics`
  - [x] `PonderScenes` — storyboard для heater и stamping_press
- [ ] **NBT-схематики**
  - [ ] `assets/tfc_aeronautics/ponder/heater/*.nbt` — папка создана, но пуста
  - [ ] `assets/tfc_aeronautics/ponder/stamping_press/*.nbt` — отсутствуют
- [x] **Общий текст**
  - [x] `registerSharedText("hot_air_burn")` — заготовка под будущий воздушный шар

## Localization

- [x] **en_us** — 35 ключей: items, blocks, fluids, config, damage types, barrel recipes
- [ ] **ru_ru** — 21/35 ключей. Отсутствуют:
  - [ ] `item.tfc_aeronautics.powder.*` (8 металлов)
  - [ ] `item.tfc_aeronautics.resin_clump`
  - [ ] `item.tfc_aeronautics.saw_blade`
  - [ ] `fluid.tfc_aeronautics.rosin`
  - [ ] `item.tfc_aeronautics.rosin_bucket`
  - [ ] `tfc_aeronautics.barrel.rosin`
