# Copper Valve Handle

**Прогресс:** 2/2 ✓

Заменяет ванильный Create crafting-рецепт `create:copper_valve_handle`
(медная пластина + андезитовый сплав) на forging-путь: ручка клапана
кузнечится на TFC-наковальне из медного прутка с оригинальной
последовательностью `bend → (draw, upset в любом порядке)`.

## Рецепты

- [x] TFC anvil tier 1: `data/tfc_aeronautics/recipe/anvil/copper_valve_handle.json`
  - `ingredient: { item: "tfc:metal/rod/copper" }`
  - `result: { count: 1, id: "create:copper_valve_handle" }`
  - `rules: ["bend_last", "draw_not_last", "upset_not_last"]` — последний
    удар всегда `BEND` (финальный изгиб ручки), а среди двух предыдущих
    ударов должны быть и `DRAW` (вытяжка), и `UPSET` (утолщение) — порядок
    этих двух свободный. Три разные операции, не «просто три удара».
  - `apply_bonus: false`, `tier: 1` (copper)
- [x] Бан исходного рецепта: `create:crafting/kinetics/copper_valve_handle`
  добавлен в `BANNED_RECIPES` в
  `src/main/java/ru/tfc_aeronautics/recipe/RecipeRemoval.java`

## Мотивация

В TFC-сборке исходный Create-рецепт фактически мёртв: `c:plates/copper`
пуст (per TFC convention — металл приходит через per-metal subtag
`c:plates/copper` → TFC не имеет листов как таковых, есть `metal/sheet/*`),
а `create:andesite_alloy` — Create-only сплав, требующий mechanical press.
Ручка клапана семантически — кованое изделие из прутка: вытянуть (DRAW),
осадить конец (UPSET), согнуть (BEND). Аналогия с `whisk.json` / `propeller.json`
(тоже `tfc:metal/rod/wrought_iron`), но для медной ручки клапана.

## Проверено

- [x] JSON валиден (`python3 -c 'import json; json.load(...)'` OK)
- [x] `./gradlew compileJava` BUILD SUCCESSFUL
