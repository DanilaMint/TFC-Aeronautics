# Recipe Overrides

**Прогресс:** 6/? ✓ (overrides + 31 envelope)

## Контекст

Простые замены ингредиентов в рецептах Create / Simulated / Aeronautics
(реже — TFC) на TFC-эквиваленты. Каждый override — отдельный JSON-файл в
namespace источника (`data/create/recipe/...`, `data/simulated/recipe/...`,
`data/aeronautics/recipe/...`), который шейдит оригинальный рецепт по тому же
пути (см. `feedback_recipe_override_convention.md`).

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
- [x] `data/create/recipe/crafting/kinetics/water_wheel.json`
  - pattern Create `["SSS","SCS","SSS"]` (8× `#minecraft:planks` + 1× `create:shaft`) → TFC-style `["LPL","PAP","LPL"]` (4× `#tfc:lumber` + 4× `#minecraft:planks` + 1× `create:shaft`): lumber по углам, planks на боках, вал в центре
  - ключ `A` = `create:shaft` (центр; в оригинале это был ключ `C` — заменили только букву, предмет тот же)
  - `minecraft:planks` после TFC содержит только TFC-плахи (20 пород), так что «любая доска» в TFC-сборке == TFC-доска. Тег `tfc:lumber` уже есть в датапаке TFC, shadow не нужен
- [x] **Aeronautics envelopes** (31 файл в `data/aeronautics/recipe/...`):
  - `white_envelope.json` — shaped: 5× `#tfc:cloths` (helmet-curl: `CCC` / `C C`)
    + `tfc:rope` в нижней середке (` R `) → 8 `aeronautics:white_envelope`.
    Выход увеличен с 4 до 8: TFC-ткань реже ванильной шерсти.
  - `<color>_envelope.json` ×15 (orange, magenta, light_blue, yellow, lime,
    pink, gray, light_gray, cyan, purple, blue, brown, green, red, black) —
    shapeless crafting: `aeronautics:white_envelope` + `minecraft:<color>_dye` →
    1 `aeronautics:<color>_envelope`. Перекрашивание через ванильные красители
    (TFC их производит через barrel-рецепты).
  - `deploying/deploying_envelope_<color>.json` ×15 (всё кроме white) —
    `create:deploying`: deployer с красителем в «руке» тыкает по
    `aeronautics:white_envelope` в мире → 1 `aeronautics:<color>_envelope`.
    Альтернативный craft-путь перекрашивания, без ручного верстака; особенно
    удобно для массовой раскраски в рамках Create-конвейера.
    `deploying_envelope_white.json` удалён: vanilla-белого красителя нет.
- [x] `data/create/recipe/crafting/kinetics/rope_pulley.json`
  - оригинал Create (1×3: casing+`#minecraft:wool`+`#c:plates/iron`) → TFC-style 3×3 `RCR`/`RRR`/`RSR`
  - `C` = `create:andesite_casing` (верх-середина), `S` = `tfc:metal/sheet/wrought_iron` (низ-середина), `R` = `tfc:rope` (7× остальные)
  - мотивация: `tfc:rope` — естественный заменитель шерсти в TFC; wrought iron sheet — кованая железная плита вместо Create-only iron plate. Rope подчёркивает «грузовую» суть кинематического блока

## TODO (новые добавлять сюда)

- [ ]
