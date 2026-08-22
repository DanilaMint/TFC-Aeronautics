# Rosin

**Прогресс:** 6/6 ✓

## Регистрация
- [x] fluid `tfc_aeronautics:rosin` — `MixingFluid.Source` / `MixingFluid.Flowing`, `fluid/Fluids.java`, `waterLikeRosin()` (drown/extinguish/hydrate/push/swim/boating, `fallDistanceModifier=0`)
- [x] block `tfc_aeronautics:fluid/rosin` — `fluid/FluidBlocks.java` (`LiquidBlock`)
- [x] item `tfc_aeronautics:rosin_bucket` — `fluid/FluidItems.java`

## Логика

### Клиент
- [x] `client/FluidClientExtensions.java` — `FluidRendererExtension` registration, amber tint `0xC68A3A`, vanilla water still/flow textures

## Рецепты
- [x] barrel sealed: `data/tfc_aeronautics/recipe/barrel/rosin.json` (50 mB `#tfc_aeronautics:strong_alcohol` + 1 resin_clump → 50 mB rosin, 1000 ticks; общий рецепт с Resin)
- [x] shadow `data/tfc/tags/fluid/ingredients.json` — rosin добавлен для TFC casting
