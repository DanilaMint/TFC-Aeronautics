# Quern / Millstone Sync

**Прогресс:** 9/9 ✓

## Регистрация
- [x] `recipe/QuernMillingRecipeType.java` — `tfc_aeronautics:quern_milling`
- [x] `recipe/QuernMillingRecipe.java` — extends Create `MillingRecipe`, хранит `Ingredient + ItemStackProvider`
- [x] `recipe/QuernMillingRecipeSerializer.java` — `MapCodec` читает TFC `ItemStackProvider` с `tfc:copy_food` модификаторами
- [x] `recipe/QuernMillingRecipeParams.java` — subclass `ProcessingRecipeParams`
- [x] `recipe/RecipeRegistration.java` — wiring

## Логика
- [x] `mixin/MillstoneBlockEntityMixin.java` — 3 injects/redirects в `tick` / `process` / `canProcess`, роутит `tfc_aeronautics:quern_milling` через `ItemStackProvider.getSingleStack(input)`

## Рецепты
- [x] 6× grain → flour: `data/tfc_aeronautics/recipe/milling/food/{wheat,barley,maize,oat,rice,rye}_flour.json`
- [x] 8× порошковые quern-зеркала — перечислены в Metal Powders
- [x] TFC quern → Create milling mirror — зарегистрировано в Quern/Millstone Sync как `quern_milling` RecipeType; файловое зеркало под `tfc_aeronautics:quern` остаётся в `data/tfc_aeronautics/recipe/quern/` (см. Metal Powders)
