# Metal Powders

**Прогресс:** 10/10 ✓

## Регистрация
- [x] items `tfc_aeronautics:powder/{copper,tin,zinc,bismuth,cast_iron,gold,silver,nickel}`
- [x] `powder/PowderRegistration.java`
- [x] enum `powder/MetalPowder.java` — melt temp °C (copper 1080, tin 230, zinc 420, bismuth 270, cast_iron 1535, gold 1060, silver 970, nickel 1450)
- [x] item class `powder/MetalPowderItem.java`

## Текстурирование
- [x] `assets/tfc_aeronautics/textures/item/powder/{copper,tin,zinc,bismuth,cast_iron,gold,silver,nickel}.png` (8 PNG)

## Моделирование
- [x] `assets/tfc_aeronautics/models/item/powder/{copper,tin,zinc,bismuth,cast_iron,gold,silver,nickel}.json` (8 item models)

## Рецепты
- [x] 8× TFC quern: `data/tfc_aeronautics/recipe/quern/<metal>_powder.json` (ingot → 20 powder)
- [x] 8× TFC heating: `data/tfc_aeronautics/recipe/heating/<metal>_powder.json` (5 mB metal)
- [x] 8× Create crushing: `data/tfc_aeronautics/recipe/crushing/<metal>_powder.json` (millstone)
- [x] 8× TFC item_heat: `data/tfc_aeronautics/tfc/item_heat/<metal>_powder.json` (heat_capacity 0.142857, forging/welding 60%/80% melt)
