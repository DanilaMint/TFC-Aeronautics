# Saw Blade

**Прогресс:** 8/8 ✓

## Регистрация
- [x] item `tfc_aeronautics:saw_blade`
- [x] `saw/SawBladeRegistration.java`

## Текстурирование
- [ ] `assets/tfc_aeronautics/textures/item/saw_blade.png`

## Моделирование
- [x] `assets/tfc_aeronautics/models/item/saw_blade.json` (`parent: item/generated`)

## Рецепты
- [x] TFC anvil tier 3: `data/tfc_aeronautics/recipe/anvil/saw_blade.json` (wrought_iron sheet → saw_blade)
- [x] TFC heating @1535°C: `data/tfc_aeronautics/recipe/heating/saw_blade.json` (saw_blade → 200 mB cast_iron)
- [x] TFC item_heat: `data/tfc_aeronautics/tfc/item_heat/saw_blade.json` (heat_capacity 6.0, forging 921°C, welding 1228°C)
- [x] Create mechanical_saw override: `data/create/recipe/crafting/kinetics/mechanical_saw.json` (принимает saw_blade)
