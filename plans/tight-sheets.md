# Tight Sheets

**Прогресс:** 10/10 ✓

## Регистрация
- [x] items `tfc_aeronautics:metal/tight_sheet/{copper,wrought_iron,steel}`
- [x] `metal/TightSheetRegistration.java`
- [x] enum `metal/TightSheet.java` — melt temp °C (copper 1080, wrought_iron 1535, steel 1540)

## Текстурирование
- [x] `assets/tfc_aeronautics/textures/item/metal/tight_sheet/{copper,wrought_iron,steel}.png` (3 PNG)

## Моделирование
- [x] `assets/tfc_aeronautics/models/item/metal/tight_sheet/{copper,wrought_iron,steel}.json` (3 item models)

## Рецепты
- [x] 3× TFC anvil: `data/tfc_aeronautics/recipe/anvil/tight_sheet_{copper,wrought_iron,steel}.json` (tiers 1/3/4, hit_last/second/third)
- [x] 3× TFC heating: `data/tfc_aeronautics/recipe/heating/{copper,wrought_iron,steel}_tight_sheet.json` (100 mB metal)
- [x] 3× TFC item_heat: `data/tfc_aeronautics/tfc/item_heat/{copper,wrought_iron,steel}_tight_sheet.json` (heat_capacity 9.6)
- [x] 3× Create pressing override: `data/create/recipe/pressing/tight_sheet_{copper,wrought_iron,steel}.json`
- [x] 3× Create crushing override: `data/tfc_aeronautics/recipe/crushing/{copper,wrought_iron,steel}_tight_sheet.json`
