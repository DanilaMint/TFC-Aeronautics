# Worldgen

**Прогресс:** 46/46 ✓

## Регистрация
- [x] `worldgen/WorldgenSetup.java` — FMLCommonSetupEvent, регистрация всего
- [x] `worldgen/StructureTypes.java` — `ATMOSPHERIC`, `ANCIENT_GRAVEYARD`, `ATMOSPHERIC_TEMPLATE`
- [x] `worldgen/StructurePieceTypes.java` — `ANCIENT_GRAVEYARD`, `ATMOSPHERIC_TEMPLATE`
- [x] `worldgen/ProcessorTypes.java` — `GRAVEYARD_MATERIAL`, `LOCAL_MATERIAL`

## Логика

### Фреймворк
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

### Loot-эффекты
- [x] `worldgen/GraveyardLootEffect.java` — `tfc_aeronautics:ancient_graveyard_loot`, заполняет TFC `LargeVesselBlockEntity` через `inventory.getInternalStacks()` (обход chunk-gen deadlock)
- [x] `worldgen/AncientShelterEffects.java` — vessel + ash в firepit (`setAsh`)
- [x] `worldgen/AncientShelterLoot.java` — 30+ entries с optional `ClimateRange`
- [x] `worldgen/FarmerHouseEffects.java` — vessel-crops + tool rack
- [x] `worldgen/FarmerHouseCrops.java` — 19 культур, `Crop.getClimateRange` lookup, RNG seeded by `center.asLong()`, default WHEAT
- [x] `worldgen/RichGraveyardEffects.java` — smooth-rock marker на поверхности
- [x] `worldgen/TannerHouseEffects.java` — chest + 3 sealed barrels (water/limewater/tannin через `SealableDeviceBlock.SEALED`)

## Данные

### Биомные теги
- [x] `data/tfc_aeronautics/tags/worldgen/biome/has_structure/ancient_graveyard.json` (12 биомов)
- [x] `data/tfc_aeronautics/tags/worldgen/biome/has_structure/ancient_shelter.json` (12 биомов)
- [x] `data/tfc_aeronautics/tags/worldgen/biome/has_structure/farmer_house.json` (12 биомов)
- [x] `data/tfc_aeronautics/tags/worldgen/biome/has_structure/rich_graveyard.json` (8 биомов)
- [x] `data/tfc_aeronautics/tags/worldgen/biome/has_structure/tanner_house.json` (7 биомов)

### Структуры
- [x] 5× structures JSON: `data/tfc_aeronautics/worldgen/structure/{ancient_graveyard,ancient_shelter,farmer_house,rich_graveyard,tanner_house}.json`
- [x] 5× structure sets: `data/tfc_aeronautics/worldgen/structure_set/{ancient_graveyard,ancient_shelter,farmer_house,rich_graveyard,tanner_house}.json`
- [x] 5× NBT templates: `data/tfc_aeronautics/structure/{ancient_graveyard,ancient_shelter,farmer_house,rich_graveyard,tanner_house}.nbt`

### Loot tables
- [x] `data/tfc_aeronautics/loot_tables/chests/ancient_graveyard.json` (3-5 rolls: rotten_flesh/bone/seed tag/small_ores/salt)
- [x] `data/tfc_aeronautics/loot_tables/blocks/ancient_shelter_ash.json` (1-2 wood_ash)
- [x] `data/tfc_aeronautics/loot_tables/chests/farmer_house_tool_rack.json` (5 weighted stone+copper hoes с 0-0.85 damage)
- [x] `data/tfc_aeronautics/loot_tables/chests/rich_graveyard_chest.json` (4 пула: bones/flesh/salt/valuables с diamond/emerald/lapis)
- [x] `data/tfc_aeronautics/loot_tables/chests/tanner_house_chest.json` (6 hides + 3 knives)
