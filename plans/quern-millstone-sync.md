# Quern / Millstone Sync

**Прогресс:** 10/10 ✓

## Регистрация
- [x] `recipe/QuernMillingRecipeType.java` — `tfc_aeronautics:quern_milling`
- [x] `recipe/QuernMillingRecipe.java` — extends Create `MillingRecipe`, хранит `Ingredient + ItemStackProvider`
- [x] `recipe/QuernMillingRecipeSerializer.java` — `MapCodec` читает TFC `ItemStackProvider` с `tfc:copy_food` модификаторами
- [x] `recipe/QuernMillingRecipeParams.java` — subclass `ProcessingRecipeParams`
- [x] `recipe/RecipeRegistration.java` — wiring

## Логика
- [x] `mixin/MillstoneBlockEntityMixin.java` — 4 injects/redirects в `process` (HEAD capture + rollResults) / `tick` / `canProcess`, роутит `tfc_aeronautics:quern_milling` через `ItemStackProvider.getSingleStack(capturedInput)`

## Рецепты
- [x] 6× grain → flour: `data/tfc_aeronautics/recipe/milling/food/{wheat,barley,maize,oat,rice,rye}_flour.json`
- [x] 8× порошковые quern-зеркала — перечислены в Metal Powders
- [x] TFC quern → Create milling mirror — зарегистрировано в Quern/Millstone Sync как `quern_milling` RecipeType; файловое зеркало под `tfc_aeronautics:quern` остаётся в `data/tfc_aeronautics/recipe/quern/` (см. Metal Powders)

## Баг-фикс
- [x] `mixin/MillstoneBlockEntityMixin.java` — `@Inject(at = @At("HEAD"))` в `process()` захватывает **копию** (`inputInv.getStackInSlot(0).copy()`) pre-shrink input в `@Unique`-поле `aeronautics$capturedInput`. `.copy()` обязателен: иначе снимок хранит ту же ссылку, что и слот, и после `shrink(1)` его count = 0 — `ItemStack.copy()` внутри `getSingleStack` тогда возвращает `EMPTY` без FOOD-компонента, и `CopyFoodModifier` тихо срабатывает вхолостую. Redirect `aeronautics$rollResults` использует сохранённый снапшот вместо `inputInv.getStackInSlot(0)` (который уже `EMPTY` после `shrink(1)`). Синхронизирует срок годности муки из мельницы с мукой из жернова — `CopyFoodModifier` теперь получает реальное зерно и применяет TFC-формулу `Cf = (1 - p) * T + p * Ci`.

## Документация
- [x] `DOCS.md` — параграф «Синхронизация срока годности» в разделе `tfc_aeronautics:quern_milling` (~строка 162) с описанием post-shrink EMPTY бага и pre-shrink capture фикса.
