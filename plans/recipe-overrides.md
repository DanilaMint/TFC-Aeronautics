# Recipe Overrides

**Прогресс:** 11/? ✓ (overrides + 31 envelope)

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
- [x] `data/create/recipe/crafting/kinetics/whisk.json`
  - оригинал Create: shaped `[" C ","SCS","SSS"]` с `create:andesite_alloy` + `#c:plates/iron` → 1 `create:whisk`
  - TFC-style ромб `[" R ","R R"," R "]`: 4× `tfc:metal/rod/wrought_iron` (стержни по четырём сторонам, углы и центр пусты) → 1 `create:whisk`
  - мотивация: венчик — кованый ручной инструмент; стержни кованого железа естественно ложатся в TFC-металлургический путь (anvil + hammer), а андезитовый сплав и Create iron plates недоступны
  - `show_notification: false` (structural reshape, как у `rope_pulley.json`)
  - шейдинг-тегов не требуется: `tfc:metal/rod/wrought_iron` — прямой item-id (single item), shadow-тег не нужен
  - recipe-id остаётся `create:crafting/kinetics/whisk`, поэтому advancement `data/create/advancement/recipes/misc/crafting/kinetics/whisk.json` зачтётся без правок (см. §19 DOCS.md про recipe-id в namespace источника)
- [x] `data/create/recipe/crafting/kinetics/propeller.json`
  - оригинал Create shaped `[" S ","SCS"," S "]` с `create:andesite_alloy` + `#c:plates/iron` — невозможен в TFC (andesite_alloy нет, `c:plates/iron` пуст)
  - TFC-style `["S S"," R ","S S"]`: 4× `tfc_aeronautics:metal/tight_sheet/wrought_iron` (углы) + 1× `tfc:metal/rod/wrought_iron` (центр) → 1 `create:propeller`. Мотивация: пропеллер — кованое механическое изделие; тонкий лист и стержень кованого железа — естественный результат TFC-кузнечного пути. `show_notification: false` (structural reshape). Шейдинг-тегов не требуется. recipe-id остаётся `create:crafting/kinetics/propeller`, advancement Create засчитывается без правок
- [x] `data/tfc_aeronautics/recipe/crafting/kinetics/steel_propeller.json`
  - параллельный вариант под сталь: те же 4× `tfc_aeronautics:metal/tight_sheet/steel` (углы) + 1× `tfc:metal/rod/steel` (центр) → 1 `create:propeller`
  - recipe-id `tfc_aeronautics:crafting/kinetics/steel_propeller` — даёт игроку выбор металла (wrought_iron через override, сталь через этот рецепт)
  - не требует `BANNED_RECIPES`: исходный Create-рецепт уже замещён первым override'ом
- [x] `data/create/recipe/crafting/kinetics/goggles.json`
  - оригинал Create shaped `[" S ","GPG"]` с `c:glass_blocks` + `c:plates/gold` + 1× `c:strings` → 1 `create:goggles`
  - TFC-style шлем 3×3 `["SSS","S S","LPL"]`: 5× `c:strings` (контур шлема: 3 в ободе купола сверху + 2 по бокам, центр пуст — отверстие под линзы) + 2× `tfc:lens` (глаза) + 1× `tfc:metal/sheet/gold` (переносица) → 1 `create:goggles`
  - мотивация: очки — стекольно-металлический предмет в TFC-мире; `tfc:lens` идёт через TFC glassworking (`data/tfc/recipe/glassworking/lens.json`), `tfc:metal/sheet/gold` — через anvil (`data/tfc/recipe/anvil/metal/sheet/gold.json`). Нитки формируют шлемный каркас с открытым лицом, под которое уходят линзы и золотой мост
  - `show_notification: false` (structural reshape, как у `whisk.json` / `rope_pulley.json` / `propeller.json`)
  - шейдинг-тегов не требуется: `tfc:lens` и `tfc:metal/sheet/gold` — прямые item-id, `c:strings` определён в датапаке TFC и в этой сборке содержит `tfc:wool_yarn` (ванильная `minecraft:string` в тег **не** входит — это поведение совпадает с оригинальным Create-рецептом)
  - recipe-id остаётся `create:crafting/kinetics/goggles`, advancement `data/create/advancement/recipes/misc/crafting/kinetics/goggles.json` засчитывается без правок
  - **внимание к паттерну**: пустой слот — пробел (`" "`), не `.`. В Minecraft 1.21.1 `ShapedRecipePattern` принимает пустым только пробел; любой другой символ вне `key` валит JSON с `JsonSyntaxException: Pattern references symbol '.' but it's not defined in the key`. Прочие override'ы в проекте (`whisk.json`, `propeller.json`) используют пробелы
- [x] `data/create/recipe/crafting/kinetics/gearshift.json`
  - оригинал Create shapeless `andesite_casing` + `cogwheel` + tag `c:dusts/redstone` → 1 `create:gearshift`
  - новый shapeless: `create:clutch` + `create:cogwheel` → 1 `create:gearshift`
  - мотивация: `clutch` уже сам по себе содержит `andesite_casing` + `cogwheel`, поэтому это shortcut — игроку не нужно собирать andesite_casing и не нужен redstone. TFC-контекст: andesite_casing в TFC-мире требует andesite alloy (металл), а clutch — уже готовый механический блок, естественнее положить его
  - структурно — простой sub-recipe override (как `super_glue.json`, `encased_chain_drive.json`), не TFC-style reshape: формат остался shapeless, поменялись только ингредиенты
  - `show_notification: true` (по умолчанию; ничего особенного в получении нет)
  - шейдинг-тегов не требуется: оба ингредиента — прямые item-id
  - recipe-id остаётся `create:crafting/kinetics/gearshift`, advancement Create засчитывается без правок
  - **проверено**: `./gradlew compileJava` UP-TO-DATE, JSON валиден (`python3 -c 'json.load(...)'` OK), сборка `./build.sh install` прошла, JAR установился в `~/.var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher/instances/tfc-aeronautics-dev/minecraft/mods/tfc_aeronautics-0.5.0.jar`

## TODO (новые добавлять сюда)

- [ ]
