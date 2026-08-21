# Recipe Overrides

**Прогресс:** 16/? ✓ (overrides + 31 envelope)

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

- [x] `data/create/recipe/crafting/kinetics/piston_extension_pole.json`
  - `minecraft:planks` → `tfc:lumber`
  - `create:andesite_alloy` → `tfc_aeronautics:composite`
  - выход 8 → 2
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
- [x] `data/simulated/recipe/directional_gearshift.json`
  - оригинал Simulated shapeless `create:andesite_casing` + `create:cogwheel` + `minecraft:redstone_torch` + `create:shaft` → 1 `simulated:directional_gearshift`
  - новый shapeless: `create:clutch` + `create:gearshift` + tag `c:dusts/redstone` → 2 `simulated:directional_gearshift`
  - мотивация: directional_gearshift — это «улучшенная» связка clutch + gearshift. Собирать её из тех же clutch + gearshift + redstone интуитивнее, чем из 4 разнородных компонент (включая редстоун-факел). `clutch` уже содержит andesite_casing + cogwheel; `gearshift` — clutch + cogwheel; вместе они покрывают andesite_casing + cogwheel из исходного рецепта, а редстоун-источник остаётся обязательным «электрическим» ингредиентом. Выход ×2 — два directional_gearshift за раз, потому что clutch и gearshift сами по себе дорогие механические блоки, а directional_gearshift — это их сборка
  - структурно — простой sub-recipe override (как `gearshift.json`, `super_glue.json`, `encased_chain_drive.json`): формат остался shapeless, поменялись ингредиенты и выход
  - `show_notification: true` (по умолчанию)
  - шейдинг-тегов не требуется: `create:clutch` и `create:gearshift` — прямые item-id; `c:dusts/redstone` уже определён в рантайме (используется самим Create в 20+ рецептах включая clutch и gearshift)
  - recipe-id остаётся `simulated:directional_gearshift`, advancement `data/simulated/advancement/recipes/misc/directional_gearshift.json` ссылается на этот же recipe-id — засчитывается без правок
  - **проверено**: JSON валиден (`python3 -c 'import json; json.load(...)'` OK), `./gradlew compileJava` UP-TO-DATE
- [x] `data/tfc_aeronautics/recipe/kinetics/clutch.json`
  - оригинал Create shapeless `create:andesite_casing` + `create:shaft` + tag `c:dusts/redstone` → 1 `create:clutch`
  - новый shaped 3×3 `LCL`/`MSR`/`LCL`: 4× `#tfc:lumber` (углы) + 2× `create:andesite_casing` + 1× `tfc:brass_mechanisms` (центр) + 1× `create:shaft` + 1× tag `c:dusts/redstone` → 2 `create:clutch`
  - мотивация: оригинал слишком дёшев (3 ингредиента, count 1) и не использует TFC-материалы; TFC-латунный механизм в центре (выковывается через TFC anvil из `c:ingots/brass`) + доски по углам + редстоун дают механически осмысленный craft в TFC-контексте. Выход ×2 компенсирует добавление `brass_mechanisms` (3-шаговый anvil-recipe)
  - структурно — **TFC-style reshape** (shapeless → shaped), как `whisk.json` / `rope_pulley.json` / `propeller.json`. Ветка 2 скилла `recipe-override` (recipe-id в `tfc_aeronautics`, оригинал запрещён через `BANNED_RECIPES`)
  - `show_notification: false` (structural reshape)
  - шейдинг-тегов не требуется: `tfc:lumber` определён в датапаке TFC (20 пород), `c:dusts/redstone` — общий common-тег
  - оригинал `create:crafting/kinetics/clutch` (recipe-id из пути `data/create/recipe/crafting/kinetics/clutch.json`) добавлен в `BANNED_RECIPES` в `src/main/java/ru/tfc_aeronautics/recipe/RecipeRemoval.java`
- [x] `data/create/recipe/crafting/kinetics/hand_crank.json`
  - оригинал Create shaped `["CCC", "  A"]` с 3× `#minecraft:planks` + 1× `create:andesite_alloy` → 1 `create:hand_crank`
  - новый: те же `["CCC", "  A"]`, ключи `C = #tfc:lumber` + `A = tfc_aeronautics:composite` → 1 `create:hand_crank`
  - мотивация: `minecraft:planks` в TFC-сборке — это TFC-плахи (blocks). Игроку нужны lumber — обработанные доски как items. `tfc_aeronautics:composite` (Industrial Composite / Промышленный композит) — наш аналог `create:andesite_alloy`, производится через barrel-рецепт `data/tfc_aeronautics/recipe/barrel/dry_composite.json`. Pattern и аутпут неизменны — простой sub-recipe override (как `super_glue.json` / `gearshift.json`), не TFC-style reshape
  - ветка 1 скилла `recipe-override` (recipe-id в namespace `create`, без `BANNED_RECIPES`)
  - `show_notification: false` (consistent with 8 existing overrides: encased_chain_drive, wrench, belt_connector, whisk, propeller, rope_pulley, mechanical_saw, goggles)
  - шейдинг-тегов не требуется: `#tfc:lumber` уже в датапаке TFC (20 пород) и использован в 3 других наших override-рецептах (`clutch.json`, `water_wheel.json`, `white_sail.json`); `tfc_aeronautics:composite` — прямой item-id из `composite/CompositeRegistration.java`
  - recipe-id остаётся `create:crafting/kinetics/hand_crank`, advancement Create засчитывается без правок
- [x] `data/create/recipe/crafting/kinetics/chute.json`
  - оригинал Create shaped 3×1 `A/I/A` с `#c:plates/iron` (A) + `#c:ingots/iron` (I) → 4 `create:chute`
  - TFC-style shaped 3×1 `A/I/A`: 2× `tfc_aeronautics:metal/tight_sheet/wrought_iron` (A) + 1× `tfc:metal/ingot/wrought_iron` (I) → 4 `create:chute`
  - мотивация: `c:plates/iron` и `c:ingots/iron` в TFC-сборке пусты (per TFC convention — металл приходит через `c:plates/<metal>` / `c:ingots/<metal>`); tight_sheet — наш аналог plate для wrought iron, ingot — TFC слиток. Pattern и аутпут неизменны — простой sub-recipe override (как `rope_pulley.json` / `propeller.json`), не TFC-style reshape
  - ветка 1 скилла `recipe-override` (recipe-id в namespace `create`, без `BANNED_RECIPES`)
  - `show_notification: false` (consistent с 10 существующими overrides)
  - шейдинг-тегов не требуется: `tfc_aeronautics:metal/tight_sheet/wrought_iron` и `tfc:metal/ingot/wrought_iron` — прямые item-id (TFC ingot item, уникальный tight_sheet из `metal/TightSheet.java`)
  - recipe-id остаётся `create:crafting/kinetics/chute`, advancement Create засчитывается без правок
  - параллельно добавлены `data/tfc_aeronautics/recipe/heating/chute.json` (chute → 75 мБ `tfc:metal/cast_iron` @ 1535°C) и `data/tfc_aeronautics/tfc/item_heat/chute.json` (`heat_capacity: 7.2`) — см. `plans/chute.md`
- [x] `data/tfc_aeronautics/recipe/anvil/bracket_{wrought_iron,steel,cast_iron}.json` (3 файла)
  - оригинал Create crafting_shaped `["SSS","PCP"]`: 3× `#c:nuggets/iron` (S) + 2× `#c:ingots/iron` (P) + 1× `create:andesite_alloy` (C) → 4 `create:metal_bracket`. В TFC-сборке фактически мёртв: `c:ingots/iron` / `c:nuggets/iron` пусты (per TFC convention — металл через per-metal subtag), `andesite_alloy` — Create-only
  - новые: 3 файла anvil-рецептов (один per-металл — TFC-наковальня не принимает несколько тегов в одном ингедиенте). Шаблон: `tfc:anvil` + tag `c:ingots/<metal>` + `rules: ["bend_last","bend_second_last"]` (2 сгиба, уникальный bend-паттерн — отличается от hit-паттерна в `tight_sheet_*`) + `apply_bonus: false` (аналогично `tight_sheet_*`, без бонусного выхода за мастерство). Per-металл `count`: `wrought_iron` → 4, `steel` → 8 (steel даёт больше как лучший металл), `cast_iron` → 2 (минимальный выход для «грязного» tier-0 пути)
  - **tier per-металл**: `wrought_iron` → 3, `steel` → 4 (совпадает с `tight_sheet_wrought_iron.json` / `tight_sheet_steel.json`); `cast_iron` → 0 (по запросу пользователя — соответствует TFC vanilla cast iron recipes, которые не указывают tier; `default = 0` в `AnvilRecipe.java:71`, означает «любая наковальня»)
  - мотивация: скоба — кованая листовая заготовка, естественно идёт через TFC-наковальню. Per-metal tag вместо `c:ingots` umbrella (как в `tight_sheet_*`): исключает скобы из латуни/бронзы/меди, держит баланс металлов. Прогрессия count (2 → 4 → 8) повторяет «качество металла = выход»: cast_iron на любой наковальне даёт минимум, steel на 4-tier даёт максимум. Bend-паттерн (не hit) делает рецепт визуально отличимым в JEI от прочих anvil-операций в моде
  - структурно — **TFC-style reshape** (shaped → tfc:anvil), ветка 2 скилла `recipe-override` (recipe-id в `tfc_aeronautics`, оригинал запрещён через `BANNED_RECIPES`)
  - `show_notification: false` (по умолчанию для anvil-рецептов — UI TFC-наковальни сам показывает результат)
  - шейдинг-тегов не требуется: `c:ingots/wrought_iron`, `c:ingots/steel`, `c:ingots/cast_iron` — per-metal subtag'и, уже определённые в датапаке TFC
  - recipe-id'ы **новые** (`tfc_aeronautics:anvil/bracket_wrought_iron` / `.../bracket_steel` / `.../bracket_cast_iron`), не override существующих — JEI/advancement привязывать не нужно
  - оригинал `create:crafting/kinetics/metal_bracket` (recipe-id из пути `data/create/recipe/crafting/kinetics/metal_bracket.json`) добавлен в `BANNED_RECIPES` в `src/main/java/ru/tfc_aeronautics/recipe/RecipeRemoval.java`
  - **проверено**: JSON валиден (`python3 -c 'json.load(...)'` OK × 3), `./gradlew compileJava` BUILD SUCCESSFUL
- [x] `data/create/recipe/crafting/kinetics/fluid_valve.json`
  - Create shapeless `c:plates/iron` (пуст в TFC) + `create:fluid_pipe` → 1 → shaped `[" S ","PPP","   "]`: 1× `tfc_aeronautics:metal/tight_sheet/wrought_iron` (верх-середина) + 3× `create:fluid_pipe` (средний ряд) → 3 `create:fluid_valve`
  - выход ×3: логика «1 труба-сегмент = 1 клапан», один лист сверху — общая перемычка-коромысло для трёх
  - **ветка 1** скилла `recipe-override`: recipe-id `create:crafting/kinetics/fluid_valve` сохранён, advancement Create засчитывается без правок; `BANNED_RECIPES` не трогаем
  - `show_notification: false` (structural reshape)
  - шейдинг-тегов не требуется: оба ingredient'а — прямые item-id
- [x] `data/create/recipe/crafting/kinetics/steam_whistle.json`
  - оригинал Create shaped `["P", "C"]` с `#c:plates/gold` (P) + `#c:ingots/copper` (C) → 1 `create:steam_whistle`
  - новый: тот же pattern, ключи `P = #c:sheets/gold` + `C = #c:ingots/copper` → 1 `create:steam_whistle`
  - мотивация: `c:plates/gold` в TFC-сборке содержит только `create:golden_sheet` (Create-only золотой лист, требует mechanical press); `c:sheets/gold` — common-тег золотых листов, в котором TFC регистрирует `tfc:metal/sheet/gold` (получается через TFC anvil из `c:double_ingots/gold`, см. `data/tfc/recipe/anvil/metal/sheet/gold.json`). Замена сохраняет семантику «любой золотой лист», но привязывает свисток к TFC-металлургическому пути — игрок больше не обязан делать Create-press для золотой пластины
  - **ветка 1** скилла `recipe-override`: recipe-id `create:crafting/kinetics/steam_whistle` сохранён, advancement Create засчитывается без правок; `BANNED_RECIPES` не трогаем
  - `show_notification: false` (конвенция проекта, как `wrench.json`)
  - шейдинг-тегов не требуется: `c:sheets/gold` — common-тег, в TFC-сборке содержит `tfc:metal/sheet/gold` (`code_references/TerraFirmaCraft/src/generated/resources/data/c/tags/item/sheets/gold.json`)
- [x] `data/tfc_aeronautics/recipe/anvil/copper_valve_handle.json`
  - оригинал Create crafting_shaped `["CCC", " S "]` с `#c:plates/copper` (C) + `create:andesite_alloy` (S) → 1 `create:copper_valve_handle`. В TFC-сборке фактически мёртв: `c:plates/copper` пуст (per TFC convention — металл идёт через per-metal subtag'и и `tfc:metal/sheet/*`, см. `steam_whistle.json` для той же логики), `andesite_alloy` — Create-only сплав, требующий mechanical press
  - новый: TFC anvil tier 1, `ingredient: { item: "tfc:metal/rod/copper" }` → 1 `create:copper_valve_handle`. Rules `["bend_last", "draw_not_last", "upset_not_last"]` — последний удар всегда `BEND` (финальный изгиб ручки), среди двух предыдущих должны быть и `DRAW` (вытяжка), и `UPSET` (утолщение) — порядок этих двух свободный. Три разные операции (не «просто три удара»), с гибкостью в первой части последовательности. `apply_bonus: false` (стандарт для немодификаторных anvil-рецептов)
  - **tier**: `1` (copper tier; см. `tmp_docs/tfc_smithing_research.md` строки 643–651)
  - мотивация: ручка клапана семантически — кованое изделие из прутка: вытянуть (DRAW), осадить конец (UPSET), согнуть (BEND). По аналогии с `whisk.json` / `propeller.json` (тоже `tfc:metal/rod/wrought_iron`), но для медной ручки — TFC-наковальня естественный путь, и copper — tier-1 металл, доступный рано. Work-offsets операций (BEND=+7, DRAW=−15, UPSET=+13) дают осмысленный кузнечный мини-челлендж вместо тривиальной серии однотипных ударов
  - структурно — **TFC-style reshape** (shaped → tfc:anvil), ветка 2 скилла `recipe-override` (recipe-id в `tfc_aeronautics`, оригинал запрещён через `BANNED_RECIPES`)
  - шейдинг-тегов не требуется: `tfc:metal/rod/copper` — прямой item-id (single item), shadow-тег не нужен (см. `plans/recipe-overrides.md` — `whisk.json` для той же конвенции)
  - recipe-id **новый** (`tfc_aeronautics:anvil/copper_valve_handle`), не override существующего — JEI/advancement привязывать не нужно
  - оригинал `create:crafting/kinetics/copper_valve_handle` (recipe-id из пути `data/create/recipe/crafting/kinetics/copper_valve_handle.json`) добавлен в `BANNED_RECIPES` в `src/main/java/ru/tfc_aeronautics/recipe/RecipeRemoval.java`
  - подробный план: [`plans/copper-valve-handle.md`](copper-valve-handle.md)
- [x] `data/create/recipe/crafting/kinetics/linear_chassis_from_conversion.json`
  - оригинал Create shaped `[" P ","LLL"," P "]`: 2× `create:andesite_alloy` (P) + 3× `#minecraft:logs` (L) → 3 `create:linear_chassis`
  - новый: тот же pattern, ключ `P = tfc_aeronautics:composite` → 3 `create:linear_chassis`
  - мотивация: `create:andesite_alloy` в TFC-сборке недоступен (Create-only сплав, циклическая зависимость от mechanical mixer, который сам требует andesite_alloy). `tfc_aeronautics:composite` (Industrial Composite) — наш аналог, TFC barrel-рецепт `data/tfc_aeronautics/recipe/barrel/dry_composite.json`. Тот же свап, что в `hand_crank.json` / `piston_extension_pole.json` (другие overrides с `andesite_alloy` → `composite`)
  - **ветка 1** скилла `recipe-override` (recipe-id в namespace `create`, без `BANNED_RECIPES`) — файл по тому же пути, что оригинал, затеняет Create'овский рецепт автоматически (конвенция: см. `feedback_recipe_override_convention.md`)
  - `show_notification: false` (конвенция проекта для всех sub-recipe overrides)
  - шейдинг-тегов не требуется: `#minecraft:logs` уже в датапаке, `tfc_aeronautics:composite` — прямой item-id из `composite/CompositeRegistration.java:28`
  - recipe-id остаётся `create:crafting/kinetics/linear_chassis_from_conversion`, advancement `data/create/advancement/recipes/misc/crafting/kinetics/linear_chassis_from_conversion.json` засчитывается без правок

## TODO (новые добавлять сюда)

- [ ]
