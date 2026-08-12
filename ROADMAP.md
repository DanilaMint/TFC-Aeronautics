# Roadmap

Статус механик мода.

Формат: каждая подсистема — H2. Внутри — подкатегории `Текстурирование`, `Регистрация`, `Логика`, `Рецепты`, `Моделирование` (используются только применимые). Под-категории `#### Сервер` / `#### Клиент` — где обе стороны нетривиальные. `[x]` — закрыто, `[ ]` — открыто.

## Metal Powders

### Регистрация
- [x] items `tfc_aeronautics:powder/{copper,tin,zinc,bismuth,cast_iron,gold,silver,nickel}`
- [x] `powder/PowderRegistration.java`
- [x] enum `powder/MetalPowder.java` — melt temp °C (copper 1080, tin 230, zinc 420, bismuth 270, cast_iron 1535, gold 1060, silver 970, nickel 1450)
- [x] item class `powder/MetalPowderItem.java`

### Текстурирование
- [x] `assets/tfc_aeronautics/textures/item/powder/{copper,tin,zinc,bismuth,cast_iron,gold,silver,nickel}.png` (8 PNG)

### Моделирование
- [x] `assets/tfc_aeronautics/models/item/powder/{copper,tin,zinc,bismuth,cast_iron,gold,silver,nickel}.json` (8 item models)

### Рецепты
- [x] 8× TFC quern: `data/tfc_aeronautics/recipe/quern/<metal>_powder.json` (ingot → 20 powder)
- [x] 8× TFC heating: `data/tfc_aeronautics/recipe/heating/<metal>_powder.json` (5 mB metal)
- [x] 8× Create crushing: `data/tfc_aeronautics/recipe/crushing/<metal>_powder.json` (millstone)
- [x] 8× TFC item_heat: `data/tfc_aeronautics/tfc/item_heat/<metal>_powder.json` (heat_capacity 0.142857, forging/welding 60%/80% melt)

## Industrial Composite

Заменяет `create:andesite_alloy` как базовый переходный материал между TFC и Create.

### Регистрация
- [x] items `tfc_aeronautics:{dry_composite, composite}`
- [x] `composite/CompositeRegistration.java`
- [x] tag `tfc_aeronautics:stripped_logs` (20 TFC wood)
- [x] tag `tfc_aeronautics:igneous_gravels` (7 igneous gravels)

### Текстурирование
- [x] `assets/tfc_aeronautics/textures/item/composite.png`
- [x] `assets/tfc_aeronautics/textures/item/dry_composite.png`

### Моделирование
- [x] `assets/tfc_aeronautics/models/item/composite.json`
- [x] `assets/tfc_aeronautics/models/item/dry_composite.json`

### Рецепты
- [x] shapeless crafting: `data/tfc_aeronautics/recipe/crafting/dry_composite.json` (cast_iron_powder + `#tfc_aeronautics:igneous_gravels` → dry_composite)
- [x] barrel instant: `data/tfc_aeronautics/recipe/barrel/dry_composite.json` (25 mB limewater + dry_composite → composite)
- [x] TFC heating: `data/tfc_aeronautics/recipe/heating/composite_shaft.json` (composite @300°C → `create:shaft`)
- [x] TFC item_heat: `data/tfc_aeronautics/tfc/item_heat/composite.json` (heat_capacity 0.5)
- [x] Create item_application overrides (3): `data/create/recipe/item_application/andesite_casing.json`, `tfc_aeronautics_brass_casing.json`, `tfc_aeronautics_copper_casing.json` — vanilla logs заменены на TFC `stripped_logs`

## Resin

### Регистрация
- [x] item `tfc_aeronautics:resin_clump`
- [x] `resin/ResinRegistration.java`
- [x] tag `tfc_aeronautics:can_collect_resin` (5 conifers: douglas_fir, pine, sequoia, spruce, white_cedar)

### Текстурирование
- [x] `assets/tfc_aeronautics/textures/item/resin_clump.png`

### Моделирование
- [x] `assets/tfc_aeronautics/models/item/resin_clump.json`

### Логика
- [x] `resin/ResinStripHandler.java` — `BlockEvent.BlockToolModificationEvent` на `AXE_STRIP` по блокам в `#tfc_aeronautics:can_collect_resin` → drops resin_clump с шансом `Config.resinDropChance`
- [x] tag `tfc_aeronautics:strong_alcohol` (vodka, rum, whiskey, rye_whiskey, corn_whiskey) — для barrel-рецепта rosin

### Рецепты
- [x] barrel sealed: `data/tfc_aeronautics/recipe/barrel/rosin.json` (50 mB `#tfc_aeronautics:strong_alcohol` + 1 resin_clump → 50 mB rosin, 1000 ticks) — см. также Rosin

## Rosin

### Регистрация
- [x] fluid `tfc_aeronautics:rosin` — `MixingFluid.Source` / `MixingFluid.Flowing`, `fluid/Fluids.java`, `waterLikeRosin()` (drown/extinguish/hydrate/push/swim/boating, `fallDistanceModifier=0`)
- [x] block `tfc_aeronautics:fluid/rosin` — `fluid/FluidBlocks.java` (`LiquidBlock`)
- [x] item `tfc_aeronautics:rosin_bucket` — `fluid/FluidItems.java`

### Логика
#### Клиент
- [x] `client/FluidClientExtensions.java` — `FluidRendererExtension` registration, amber tint `0xC68A3A`, vanilla water still/flow textures

### Рецепты
- [x] barrel sealed: `data/tfc_aeronautics/recipe/barrel/rosin.json` (50 mB `#tfc_aeronautics:strong_alcohol` + 1 resin_clump → 50 mB rosin, 1000 ticks; общий рецепт с Resin)
- [x] shadow `data/tfc/tags/fluid/ingredients.json` — rosin добавлен для TFC casting

## Tight Sheets

### Регистрация
- [x] items `tfc_aeronautics:metal/tight_sheet/{copper,wrought_iron,steel}`
- [x] `metal/TightSheetRegistration.java`
- [x] enum `metal/TightSheet.java` — melt temp °C (copper 1080, wrought_iron 1535, steel 1540)

### Текстурирование
- [x] `assets/tfc_aeronautics/textures/item/metal/tight_sheet/{copper,wrought_iron,steel}.png` (3 PNG)

### Моделирование
- [x] `assets/tfc_aeronautics/models/item/metal/tight_sheet/{copper,wrought_iron,steel}.json` (3 item models)

### Рецепты
- [x] 3× TFC anvil: `data/tfc_aeronautics/recipe/anvil/tight_sheet_{copper,wrought_iron,steel}.json` (tiers 1/3/4, hit_last/second/third)
- [x] 3× TFC heating: `data/tfc_aeronautics/recipe/heating/{copper,wrought_iron,steel}_tight_sheet.json` (100 mB metal)
- [x] 3× TFC item_heat: `data/tfc_aeronautics/tfc/item_heat/{copper,wrought_iron,steel}_tight_sheet.json` (heat_capacity 9.6)
- [x] 3× Create pressing override: `data/create/recipe/pressing/tight_sheet_{copper,wrought_iron,steel}.json`
- [x] 3× Create crushing override: `data/tfc_aeronautics/recipe/crushing/{copper,wrought_iron,steel}_tight_sheet.json`

## Saw Blade

### Регистрация
- [x] item `tfc_aeronautics:saw_blade`
- [x] `saw/SawBladeRegistration.java`

### Текстурирование
- [x] `assets/tfc_aeronautics/textures/item/saw_blade.png`

### Моделирование
- [x] `assets/tfc_aeronautics/models/item/saw_blade.json` (`parent: item/generated`)

### Рецепты
- [x] TFC anvil tier 3: `data/tfc_aeronautics/recipe/anvil/saw_blade.json` (wrought_iron sheet → saw_blade)
- [x] TFC heating @1535°C: `data/tfc_aeronautics/recipe/heating/saw_blade.json` (saw_blade → 200 mB cast_iron)
- [x] TFC item_heat: `data/tfc_aeronautics/tfc/item_heat/saw_blade.json` (heat_capacity 6.0, forging 921°C, welding 1228°C)
- [x] Create mechanical_saw override: `data/create/recipe/crafting/kinetics/mechanical_saw.json` (принимает saw_blade)

## Impregnated Burlap Cloth

### Регистрация
- [x] item `tfc_aeronautics:impregnated_burlap_cloth`
- [x] `burlap/BurlapRegistration.java`

### Текстурирование
- [x] `assets/tfc_aeronautics/textures/item/impregnated_burlap_cloth.png`

### Моделирование
- [x] `assets/tfc_aeronautics/models/item/impregnated_burlap_cloth.json`

### Рецепты
- [x] barrel sealed: `data/tfc_aeronautics/recipe/barrel/impregnated_burlap_cloth.json` (100 mB rosin + 1 `tfc:burlap_cloth` → 1 impregnated_burlap_cloth, 7200 ticks)
- [x] Create belt_connector override: `data/create/recipe/crafting/kinetics/belt_connector.json` (принимает impregnated_burlap_cloth или leather)

## Heater

### Регистрация
- [x] block `tfc_aeronautics:heater` — `heater/HeaterBlock.java` (`Block implements IBE<HeaterBlockEntity>, IWrenchable`, props `LIT` + `HORIZONTAL_FACING`, light 14, analog signal 15, `mapColor STONE, strength 2.0/6.0, sound STONE, pushReaction BLOCK`)
- [x] item `tfc_aeronautics:heater_item`
- [x] `heater/HeaterBlockEntity.java` — `SmartBlockEntity implements IBellowsConsumer`, `ItemStackHandler(2)` (`SLOT_ITEM` limit 1 gated by `HeatCapability.has`, `SLOT_FUEL` limit 64), `FluidTank(2000 mB)`, `HotAwareItemHandler`, `HeaterValueBehaviour` (ScrollValueBehaviour, INTERVAL=50, range 0..`Heat.maxVisibleTemperature()`, step 50°C)
- [x] `heater/HeaterRegistration.java`
- [x] capabilities `heater/HeaterCapabilities.java` — ItemHandler на всех гранях, FluidHandler только `Direction.DOWN`
- [x] value box `heater/HeaterValueBoxTransform.java` (`ValueBoxTransform.Sided`, `isSideActive` = `HORIZONTAL_FACING`)
- [x] blockstate `assets/tfc_aeronautics/blockstates/heater.json` (lit/unlit variants)

### Текстурирование
- [x] `assets/tfc_aeronautics/textures/block/heater_off.png`
- [x] `assets/tfc_aeronautics/textures/block/heater_on.png`
- [x] `assets/tfc_aeronautics/textures/block/heater_flame.png`
- [x] `assets/tfc_aeronautics/textures/block/heater_top_off.png`
- [x] `assets/tfc_aeronautics/textures/block/heater_top_on.png`
- [x] `assets/tfc_aeronautics/textures/block/heater_bottom.png`
- [x] `assets/tfc_aeronautics/textures/block/heater_side.png`

### Моделирование
- [x] `assets/tfc_aeronautics/models/block/heater_off.json`
- [x] `assets/tfc_aeronautics/models/block/heater_on.json`
- [x] `assets/tfc_aeronautics/models/item/heater.json`

### Логика
#### Сервер
- [x] `heater/HeaterBlock.java` — block states (LIT, HORIZONTAL_FACING), IWrenchable
- [x] `heater/HeaterBlockEntity.java` — SmartBlockEntity, IBellowsConsumer, item/fluid handlers, HeaterValueBehaviour, частицы FLAME+SMOKE каждые 3 тика
- [x] `heater/HeaterCapabilities.java` — IItemHandler все грани, IFluidHandler DOWN
- [x] `heater/HeaterValueBoxTransform.java` — UI transform для value-box
- [ ] Ускорить нагрев в 1.5 раз
- [ ] Добавить возможность нагревать жидкостные баки для создания парового двигателя и дистиллятора

#### Клиент
- Нет client-only файлов; анимированное пламя не реализовано (см. Текстурирование)

### Звуки
- [ ] Добавить звук при горении как у charcoal forge

### Ponder-сцены
- [ ] Сцена о загрузке и выгрузке топлива и предметов автоматически или вручную
- [ ] Сцена о плавлении предметов в жидкость

## Stamping Press

### Регистрация
- [x] block `tfc_aeronautics:stamping_press` — `stamping_press/StampingPressBlock.java` (`HorizontalKineticBlock`, shape `MECHANICAL_PROCESSOR_SHAPE` + `CASING_14PX` для игроков, `canSurvive` блокирует `BasinBlock`, stress 8.0 SU)
- [x] item `tfc_aeronautics:stamping_press_item`
- [x] `stamping_press/StampingPressBlockEntity.java` — `KineticBlockEntity implements PressingBehaviourSpecifics`, `PressingBehaviour` + `FilteringBehaviour.forRecipes()` с `StampingPressFilterSlot`, `MAX_TIER=Integer.MAX_VALUE`, `AnvilRecipe.getAll()` lookup, heat-gated, `assemble()` копирует температуру
- [x] filter slot `stamping_press/StampingPressFilterSlot.java`
- [x] instances `stamping_press/StampingPressInstances.java` — `WeakHashMap` client cache (используется клиентом)
- [x] `stamping_press/StampingPressRegistration.java`
- [x] blockstate `assets/tfc_aeronautics/blockstates/stamping_press.json`

### Текстурирование
- [x] 6 граней: `assets/tfc_aeronautics/textures/block/stamping_press_{top,north,south,east,west}.png` + `stamping_press_shaft.png`
- [x] 4 head: `assets/tfc_aeronautics/textures/block/stamping_press_head_{peen_side,peen_top,rod_side,rod_top}.png`

### Моделирование
- [x] block `assets/tfc_aeronautics/models/block/stamping_press.json`
- [x] partial `assets/tfc_aeronautics/models/block/stamping_press_head.json`
- [x] gui `assets/tfc_aeronautics/models/block/stamping_press_gui.json`
- [x] item `assets/tfc_aeronautics/models/item/stamping_press.json` (parent = `stamping_press_gui`)

### Логика
#### Сервер
- [x] `stamping_press/StampingPressBlock.java` — HorizontalKineticBlock, stress 8.0 SU, canSurvive логика
- [x] `stamping_press/StampingPressBlockEntity.java` — KineticBlockEntity + PressingBehaviour + FilteringBehaviour
- [x] `stamping_press/StampingPressFilterSlot.java` — слот фильтра на задней грани
- [x] `stamping_press/StampingPressInstances.java` — клиентский кэш инстансов

#### Клиент
- [x] `client/.../stamping_press/StampingPressClientRegistration.java` — `SimpleBlockEntityVisualizer.builder().neverSkipVanillaRender()` + `StampingPressRenderer`
- [x] `client/.../stamping_press/StampingPressRenderer.java` — `KineticBlockEntityRenderer` + `FilteringRenderer.renderOnBlockEntity` + `CachedBuffers.partialFacing` с `renderedHeadOffset`
- [x] `client/.../stamping_press/StampingPressVisual.java` — `SingleAxisRotatingVisual` + `SimpleDynamicVisual`, `OrientedInstance` pressHead
- [x] `client/.../stamping_press/StampingPressPartialModels.java` — `STAMPING_PRESS_HEAD = block/stamping_press_head`
- [x] `client/.../stamping_press/StampingPressFrameTickHandler.java` — `ClientTickEvent.Post` → Outliner API, outline на `facing.getClockWise()/getCounterClockWise()`, `lineWidth 1/64f`, `highlightFace`

### Рецепты
- [x] crafting: `data/tfc_aeronautics/recipe/crafting/kinetics/stamping_press.json` (shaft + brass_casing + `tfc:metal/hammer_head/wrought_iron`)
- [x] Create mechanical_press override: `data/create/recipe/crafting/kinetics/mechanical_press.json` (vanilla path заменён)

### Визуал и звуки
- [x] Anvil strike-звук удара
- [ ] Добавить частицы-искры при ударе

### Ponder-сцены
- [ ] Сцена о использовании

## Condenser Coil

Змеевик — блок, который ведёт себя как труба и конденсирует пар из нагреваемого бака в новую жидкость. Работает только в составе связки:

```
[источник нагрева] → [жидкостный бак] → труба → [condenser_coil] → выход result
                                                       ↑
                                            труба с проточной пресной водой
```

- вход: труба от жидкостного бака, который нагревается снизу (см. Heater → Логика → «нагревать жидкостные баки»)
- охлаждение: перпендикулярно подключённая труба с **движущейся** пресной водой — наличия воды недостаточно, нужен именно проток
- выход: с противоположной от входа стороны змеевика выходит `result` рецепта
- если хотя бы одно условие нарушено (бак не греется / нет протока воды), процесс останавливается

### Ориентация
- вход паров и выход дистиллята взаимозаменяемы — разворот змеевика на 180° по этой оси ничего не меняет
- ось «пары ↔ дистиллят» всегда перпендикулярна Y, то есть только вдоль X или Z; вертикальная установка этой оси не допускается
- вход и выход пресной воды — по любой оси и в любую сторону
- дефолтная ориентация модели: пары/дистиллят вдоль Z, труба пресной воды вдоль X

### Регистрация
- [ ] block `tfc_aeronautics:condenser_coil` — ведёт себя как труба
- [ ] blockstate-свойства: горизонтальная ось пар/дистиллят (X или Z) + ось-направление водяной трубы
- [ ] item `tfc_aeronautics:condenser_coil_item`
- [ ] `condenser_coil/CondenserCoilRegistration.java`
- [ ] blockstate `assets/tfc_aeronautics/blockstates/condenser_coil.json`

### Текстурирование
- [ ] Создать текстуру для `condenser_coil`

### Моделирование
- [ ] Создать модель для `condenser_coil`
- [ ] item-модель `assets/tfc_aeronautics/models/item/condenser_coil.json`

### Логика
- [ ] Тип рецептов `tfc_aeronautics:distillation` — RecipeType + serializer + codec
- [ ] Валидация структуры дистиллятора: источник нагрева → жидкостный бак → труба → змеевик
- [ ] Детект протока пресной воды в перпендикулярной трубе (движение жидкости, а не её наличие)
- [ ] Контроль температуры: процесс идёт, пока бак держит `temperature` рецепта
- [ ] Тик обработки: изымать `rate` mB `ingredient` из бака
- [ ] Раздача результата: `result` выходит из змеевика с противоположной грани, `stillage` остаётся в баке
- [ ] Пропорции: `stillage` = `stillage_percent` × переработанного объёма, `result` = (1 − `stillage_percent`) × объёма

### Рецепты
- [ ] Формат `tfc_aeronautics:distillation`:
  - `ingredient` — жидкость, список жидкостей или тег жидкостей; должна находиться в нагреваемом баке
  - `temperature` — температура, которую нужно поддерживать на протяжении процесса
  - `rate` — сколько mB жидкости обрабатывается за один тик
  - `result` — жидкость, образующаяся в змеевике
  - `stillage` — жидкость-остаток, образующийся в нагреваемом баке после полной дистилляции
  - `stillage_percent` — float 0..1, доля объёма, остающаяся как `stillage`

### Ponder-сцены
- [ ] Сцена о работе дистиллятора: установка связки источник нагрева → бак → труба → змеевик → водяная труба; запуск нагрева; подача протока пресной воды; конденсация и выход `result`/`stillage`

## Spout / Casting

### Регистрация
- [x] `recipe/SpoutCompat.java` — `FMLCommonSetupEvent` enqueueWork, регистрирует `BlockSpoutingBehaviour.BY_BLOCK_ENTITY` для `TFCBlockEntities.MOLD_TABLE`
- [x] `recipe/SpoutCastingBehavior.java` — enum `INSTANCE`, `fillBlock` сливает `recipe.getFluidIngredient().amount()` из spout, выполняет `CastingRecipe.get(mold)`, льёт в mold, ставит outputStack в moldTable
- [x] guard: skips если mold stack пустой, mold уже содержит fluid, или `OUTPUT_SLOT` занят

## Quern / Millstone Sync

### Регистрация
- [x] `recipe/QuernMillingRecipeType.java` — `tfc_aeronautics:quern_milling`
- [x] `recipe/QuernMillingRecipe.java` — extends Create `MillingRecipe`, хранит `Ingredient + ItemStackProvider`
- [x] `recipe/QuernMillingRecipeSerializer.java` — `MapCodec` читает TFC `ItemStackProvider` с `tfc:copy_food` модификаторами
- [x] `recipe/QuernMillingRecipeParams.java` — subclass `ProcessingRecipeParams`
- [x] `recipe/RecipeRegistration.java` — wiring

### Логика
- [x] `mixin/MillstoneBlockEntityMixin.java` — 3 injects/redirects в `tick` / `process` / `canProcess`, роутит `tfc_aeronautics:quern_milling` через `ItemStackProvider.getSingleStack(input)`

### Рецепты
- [x] 6× grain → flour: `data/tfc_aeronautics/recipe/milling/food/{wheat,barley,maize,oat,rice,rye}_flour.json`
- [x] 8× порошковые quern-зеркала — перечислены в Metal Powders
- [x] TFC quern → Create milling mirror — зарегистрировано в Quern/Millstone Sync как `quern_milling` RecipeType; файловое зеркало под `tfc_aeronautics:quern` остаётся в `data/tfc_aeronautics/recipe/quern/` (см. Metal Powders)

## Shaft Damage

### Регистрация
- [x] damage type `tfc_aeronautics:shaft` — `DamageTypes.java`
- [x] `data/tfc_aeronautics/damage_type/shaft.json`

### Логика
- [x] `kinetics/ShaftDamageHandler.java` — `@SubscribeEvent EntityTickEvent.Post`; проверка `state.getBlock() instanceof AbstractShaftBlock`; voxel-shape intersection с inflated bbox (`CONTACT_EPSILON=0.05`); формула урона `(rpm - startRpm) / (lethalRpm - startRpm) * lethal * multiplier`; knockback перпендикулярно оси; `AllSoundEvents.CRUSHING_1`; max 1 shaft/тик
- [x] `Config.shaftDamage*` (8 параметров: `shaftDamageEnabled`, `shaftDamageStartRpm`, `shaftDamageLethalRpm`, `shaftDamageLethal`, `shaftDamageMultiplier`, `shaftKnockbackBase`, `shaftKnockbackPerRpm`, `shaftSoundVolume`)
- [x] encased shafts/cogwheels — safe (only bare shafts deal damage)
- [ ] Распространить механику на shafts на движущихся contraptions

### Локализация
- [x] `en_us.json`: `death.attack.tfc_aeronautics.shaft` + `death.attack.tfc_aeronautics.shaft.player`
- [ ] `ru_ru.json`: те же ключи — отсутствуют (см. общий Localization)

## Worldgen

### Регистрация
- [x] `worldgen/WorldgenSetup.java` — FMLCommonSetupEvent, регистрация всего
- [x] `worldgen/StructureTypes.java` — `ATMOSPHERIC`, `ANCIENT_GRAVEYARD`, `ATMOSPHERIC_TEMPLATE`
- [x] `worldgen/StructurePieceTypes.java` — `ANCIENT_GRAVEYARD`, `ATMOSPHERIC_TEMPLATE`
- [x] `worldgen/ProcessorTypes.java` — `GRAVEYARD_MATERIAL`, `LOCAL_MATERIAL`

### Логика
#### Фреймворк
- [x] `worldgen/AtmosphereSpec.java` — `ClimateBounds`, `Effect` интерфейс, concurrent REGISTRY
- [x] `worldgen/AtmosphericStructure.java` — extends `Structure`, CODEC = settingsCodec + atmosphere
- [x] `worldgen/AtmosphericTemplateStructure.java` — Placement `BURIED`/`ON_SURFACE`/`UNDERGROUND`, ceiling-thickness 5
- [x] `worldgen/AtmosphericTemplatePiece.java` — `BlockIgnoreProcessor` для `STRUCTURE_VOID`/`STRUCTURE_BLOCK`, rotation pivot в bottom-centre, ключи Rotation/CrackedChance/MossyChance/ReplaceCrops
- [x] `worldgen/GraveyardMaterialProcessor.java` — per-placement, mud_bricks/cobble/large vessel glaze
- [x] `worldgen/LocalMaterialProcessor.java` — `MaterialConfig(crackedChance, mossyChance, replaceCrops, placeSurfaceMarker)`, resolveWood/Soil/Rock
- [x] `worldgen/ContainerLootFiller.java` — `SEARCH_RADIUS=4`, writeLoot/isEmpty helpers, reflection access to `ChestBlockEntity.items`
- [x] `worldgen/AncientGraveyardStructure.java` — 5×5×5 buried tomb
- [x] `worldgen/AncientGraveyardPiece.java` — persists `ROTATION_KEY`
- [x] все структуры используют `minecraft:random_spread` (spacing/salt)

#### Loot-эффекты
- [x] `worldgen/GraveyardLootEffect.java` — `tfc_aeronautics:ancient_graveyard_loot`, заполняет TFC `LargeVesselBlockEntity` через `inventory.getInternalStacks()` (обход chunk-gen deadlock)
- [x] `worldgen/AncientShelterEffects.java` — vessel + ash в firepit (`setAsh`)
- [x] `worldgen/AncientShelterLoot.java` — 30+ entries с optional `ClimateRange`
- [x] `worldgen/FarmerHouseEffects.java` — vessel-crops + tool rack
- [x] `worldgen/FarmerHouseCrops.java` — 19 культур, `Crop.getClimateRange` lookup, RNG seeded by `center.asLong()`, default WHEAT
- [x] `worldgen/RichGraveyardEffects.java` — smooth-rock marker на поверхности
- [x] `worldgen/TannerHouseEffects.java` — chest + 3 sealed barrels (water/limewater/tannin через `SealableDeviceBlock.SEALED`)

### Данные
#### Биомные теги
- [x] `data/tfc_aeronautics/tags/worldgen/biome/has_structure/ancient_graveyard.json` (12 биомов)
- [x] `data/tfc_aeronautics/tags/worldgen/biome/has_structure/ancient_shelter.json` (12 биомов)
- [x] `data/tfc_aeronautics/tags/worldgen/biome/has_structure/farmer_house.json` (12 биомов)
- [x] `data/tfc_aeronautics/tags/worldgen/biome/has_structure/rich_graveyard.json` (8 биомов)
- [x] `data/tfc_aeronautics/tags/worldgen/biome/has_structure/tanner_house.json` (7 биомов)

#### Структуры
- [x] 5× structures JSON: `data/tfc_aeronautics/worldgen/structure/{ancient_graveyard,ancient_shelter,farmer_house,rich_graveyard,tanner_house}.json`
- [x] 5× structure sets: `data/tfc_aeronautics/worldgen/structure_set/{ancient_graveyard,ancient_shelter,farmer_house,rich_graveyard,tanner_house}.json`
- [x] 5× NBT templates: `data/tfc_aeronautics/structure/{ancient_graveyard,ancient_shelter,farmer_house,rich_graveyard,tanner_house}.nbt`

#### Loot tables
- [x] `data/tfc_aeronautics/loot_tables/chests/ancient_graveyard.json` (3-5 rolls: rotten_flesh/bone/seed tag/small_ores/salt)
- [x] `data/tfc_aeronautics/loot_tables/blocks/ancient_shelter_ash.json` (1-2 wood_ash)
- [x] `data/tfc_aeronautics/loot_tables/chests/farmer_house_tool_rack.json` (5 weighted stone+copper hoes с 0-0.85 damage)
- [x] `data/tfc_aeronautics/loot_tables/chests/rich_graveyard_chest.json` (4 пула: bones/flesh/salt/valuables с diamond/emerald/lapis)
- [x] `data/tfc_aeronautics/loot_tables/chests/tanner_house_chest.json` (6 hides + 3 knives)

## Ponder

### Регистрация
- [x] `client/.../ponder/PonderRegistration.java` — `PonderIndex.addPlugin`
- [x] `client/.../ponder/PonderPlugin.java` — `getModId`, `registerScenes`, `registerTags`, `registerSharedText("hot_air_burn")`
- [x] `client/.../ponder/PonderScenes.java` — storyboard для heater и stamping_press
- [x] `client/.../ponder/PonderTags.java` — `KINETICS = tfc_aeronautics:kinetics`, title "Kinetics", description "Components built around Create's kinetic system"

### Логика
#### Клиент
- [x] `client/.../ponder/scenes/HeaterScenes.java` — intro scene, basePlate 5×5, heater at `grid(2,1,2)`, 5×10×80 idle timing
- [x] `client/.../ponder/scenes/StampingPressScenes.java` — pressing scene, basePlate 5×5, press at `grid(2,1,2)`
- [x] `client/.../ponder/scenes/TemplateScenes.java` — shared storyboard helpers

### Данные (NBT-схематики)
- [ ] `assets/tfc_aeronautics/ponder/heater/*.nbt` — папка создана, файлы отсутствуют
- [ ] `assets/tfc_aeronautics/ponder/stamping_press/*.nbt` — отсутствуют

## Localization

### en_us
- [x] `itemGroup.tfc_aeronautics` — "TFC Aeronautics"
- [x] 8× `item.tfc_aeronautics.powder.{copper,tin,zinc,bismuth,cast_iron,gold,silver,nickel}`
- [x] `item.tfc_aeronautics.dry_composite`
- [x] `item.tfc_aeronautics.composite`
- [x] `item.tfc_aeronautics.resin_clump`
- [x] `item.tfc_aeronautics.impregnated_burlap_cloth`
- [x] `fluid.tfc_aeronautics.rosin`
- [x] `item.tfc_aeronautics.rosin_bucket`
- [x] 3× `item.tfc_aeronautics.metal.tight_sheet.{copper,wrought_iron,steel}`
- [x] `item.tfc_aeronautics.saw_blade`
- [x] `block.tfc_aeronautics.heater`
- [x] `tfc_aeronautics.heater.max_temperature`
- [x] `block.tfc_aeronautics.stamping_press`
- [x] `tfc.recipe.barrel.tfc_aeronautics.barrel.rosin`
- [x] `tfc.recipe.barrel.tfc_aeronautics.barrel.impregnated_burlap_cloth`
- [x] 9× config: `tfc_aeronautics.config.resinDropChance` + 8× `tfc_aeronautics.config.shaftDamage*`
- [x] 2× death-attack: `death.attack.tfc_aeronautics.shaft` + `.player` — см. Shaft Damage

### ru_ru
- [x] `itemGroup.tfc_aeronautics`
- [x] `item.tfc_aeronautics.dry_composite`
- [x] `item.tfc_aeronautics.composite`
- [x] `item.tfc_aeronautics.impregnated_burlap_cloth`
- [x] 3× `item.tfc_aeronautics.metal.tight_sheet.{copper,wrought_iron,steel}`
- [x] `block.tfc_aeronautics.heater`
- [x] `tfc_aeronautics.heater.max_temperature`
- [x] `block.tfc_aeronautics.stamping_press`
- [x] `tfc.recipe.barrel.tfc_aeronautics.barrel.impregnated_burlap_cloth`
- [x] 8× `tfc_aeronautics.config.shaftDamage*`
- [x] 2× `death.attack.tfc_aeronautics.shaft` + `.player`
- [ ] 8× `item.tfc_aeronautics.powder.{copper,tin,zinc,bismuth,cast_iron,gold,silver,nickel}`
- [ ] `item.tfc_aeronautics.resin_clump`
- [ ] `item.tfc_aeronautics.saw_blade`
- [ ] `fluid.tfc_aeronautics.rosin`
- [ ] `item.tfc_aeronautics.rosin_bucket`
- [ ] `tfc.recipe.barrel.tfc_aeronautics.barrel.rosin`
- [ ] `tfc_aeronautics.config.resinDropChance`

### zh_cn

### de_de

### es_es

### es_mx

### fr_fr

### pt_br

### ja_jp

### ko_kr


