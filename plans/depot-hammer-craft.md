# Depot: hammer-craft mechanic

## Контекст

Замена ванильного craft-рецепта `create:depot` (shapeless:
`create:andesite_alloy + create:andesite_casing → 1× create:depot`)
на механику в стиле TFC: удар молотком по верхней грани блока
`create:andesite_casing` (андезитовый корпус) превращает его в
`create:depot`, при условии, что блок над ним — воздух. Полная копия
TFC-механики создания каменной наковальни
(`RockConvertableToAnvilBlock.useItemOn` в `code_references/TerraFirmaCraft/`).

Реализовано через NeoForge event-listener (не JSON-recipe-override),
поэтому не трекается в `plans/recipe-overrides.md` — там только JSON
overrides.

## Готово

- [x] `src/main/java/ru/tfc_aeronautics/depot/DepotCraftHandler.java` —
  `@EventBusSubscriber(common bus)`, слушает
  `PlayerInteractEvent.RightClickBlock`, проверяет `Direction.UP`,
  air above, target = `AllBlocks.ANDESITE_CASING.get()`, held in
  `c:tools/hammer`; отменяет default и (на сервере) заменяет блок на
  `AllBlocks.DEPOT.get().defaultBlockState()`.
- [x] `src/main/java/ru/tfc_aeronautics/recipe/RecipeRemoval.java` —
  `create:crafting/kinetics/depot` добавлен в `BANNED_RECIPES`.
- [x] `DOCS.md` — отдельный раздел «Depot: крафт молотком по андезитовому
  корпусу», TOC обновлён.
- [x] `./gradlew compileJava` UP-TO-DATE.

## TODO (новые добавлять сюда)

- [ ] (пусто)
