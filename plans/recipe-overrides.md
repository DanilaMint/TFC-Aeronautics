# Recipe Overrides

**Прогресс:** 4/? ✓

## Контекст

Простые замены ингредиентов в рецептах Create (реже — TFC) на TFC-эквиваленты.
Каждый override — отдельный JSON-файл в namespace источника
(`data/create/recipe/...` или `data/tfc/recipe/...`), который шейдит
оригинальный рецепт по тому же пути (см. `feedback_recipe_override_convention.md`).

Сюда НЕ пишутся:
- Сложные рецепты-мосты (milling↔quern, spout+casting, anvil совмещение) — у
  них свои разделы в DOCS.md.
- Блокировка/скрытие рецептов TFC — раздел 18 DOCS.md.
- Перенос рецептов между namespace при адаптации нового TFC-контента
  (tight sheets в pressing/ и т.п.) — идёт в профильный plan.

## Готово

- [x] `data/create/recipe/crafting/kinetics/fluid_tank.json`
  - `c:plates/copper` → `tfc_aeronautics:metal/tight_sheet/copper`
  - `c:barrels/wooden` → `tfc:barrels`
- [x] `data/create/recipe/crafting/kinetics/white_sail.json`
  - `create:andesite_alloy` + `minecraft:wool` + `c:rods/wooden` → `tfc_aeronautics:composite` + `tfc:cloths` + `tfc:lumber`
  - pattern `["WS","SA"]` → `["PC","CI"]`
  - требует shadow-тег `tfc:cloths` (`data/tfc/tags/item/cloths.json`: burlap/wool/silk)
- [x] `data/create/recipe/crafting/logistics/andesite_funnel.json`
  - `minecraft:dried_kelp` → `tfc:cloths`
- [x] `data/create/recipe/crafting/logistics/andesite_tunnel.json`
  - `minecraft:dried_kelp` → `tfc:cloths`

## TODO (новые добавлять сюда)

- [ ]
