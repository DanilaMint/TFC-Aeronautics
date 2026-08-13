# Industrial Composite

**Прогресс:** 13/13 ✓

Заменяет `create:andesite_alloy` как базовый переходный материал между TFC и Create.

## Регистрация
- [x] items `tfc_aeronautics:{dry_composite, composite}`
- [x] `composite/CompositeRegistration.java`
- [x] tag `tfc_aeronautics:stripped_logs` (20 TFC wood)
- [x] tag `tfc_aeronautics:igneous_gravels` (7 igneous gravels)

## Текстурирование
- [x] `assets/tfc_aeronautics/textures/item/composite.png`
- [x] `assets/tfc_aeronautics/textures/item/dry_composite.png`

## Моделирование
- [x] `assets/tfc_aeronautics/models/item/composite.json`
- [x] `assets/tfc_aeronautics/models/item/dry_composite.json`

## Рецепты
- [x] shapeless crafting: `data/tfc_aeronautics/recipe/crafting/dry_composite.json` (cast_iron_powder + `#tfc_aeronautics:igneous_gravels` → dry_composite)
- [x] barrel instant: `data/tfc_aeronautics/recipe/barrel/dry_composite.json` (25 mB limewater + dry_composite → composite)
- [x] TFC heating: `data/tfc_aeronautics/recipe/heating/composite_shaft.json` (composite @300°C → `create:shaft`)
- [x] TFC item_heat: `data/tfc_aeronautics/tfc/item_heat/composite.json` (heat_capacity 0.5)
- [x] Create item_application overrides (3): `data/create/recipe/item_application/andesite_casing.json`, `tfc_aeronautics_brass_casing.json`, `tfc_aeronautics_copper_casing.json` — vanilla logs заменены на TFC `stripped_logs`
