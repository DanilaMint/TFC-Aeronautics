# Spout / Casting

**Прогресс:** 3/3 ✓

## Регистрация
- [x] `recipe/SpoutCompat.java` — `FMLCommonSetupEvent` enqueueWork, регистрирует `BlockSpoutingBehaviour.BY_BLOCK_ENTITY` для `TFCBlockEntities.MOLD_TABLE`
- [x] `recipe/SpoutCastingBehavior.java` — enum `INSTANCE`, `fillBlock` сливает `recipe.getFluidIngredient().amount()` из spout, выполняет `CastingRecipe.get(mold)`, льёт в mold, ставит outputStack в moldTable
- [x] guard: skips если mold stack пустой, mold уже содержит fluid, или `OUTPUT_SLOT` занят
