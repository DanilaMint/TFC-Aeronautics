# Glue Substitution

**Прогресс:** 6/6 ✓

Делает `tfc:glue` взаимозаменяемым с `minecraft:slime_ball` для Create и Simulated: расширяет slimeball-теги, тенит один рецепт Simulated, заменяет рецепт `create:super_glue` (поскольку его исходный recipe требовал пустые в TFC-сборке теги `c:nuggets/iron` и `c:plates/iron`), а также отключает исходный басин-рецепт Simulated на `simulated:honey_glue` (он требует `create:iron_sheet` + 500 мБ `c:honey`, чего в TFC-сборке нет). Только data, без Java.

Подробный дизайн: `docs/superpowers/specs/2026-08-15-tfc-glue-slimeball-substitution-design.md`.

## Теги (расширения)
- [x] `src/main/resources/data/c/tags/item/slimeballs.json` — добавить `tfc:glue` в `c:slimeballs` (тег Create, без подчёркивания)
- [x] `src/main/resources/data/c/tags/item/slime_balls.json` — добавить `tfc:glue` в `c:slime_balls` (тег Simulated, с подчёркиванием)

## Рецепты
- [x] `src/main/resources/data/simulated/recipe/mechanical_crafting/plunger_launcher.json` — shadow оригинального рецепта; ключ `P` с `"item": "minecraft:slime_ball"` → `"tag": "c:slimeballs"`. Остальные ключи/паттерн/результат — без изменений
- [x] `src/main/resources/data/create/recipe/crafting/kinetics/super_glue.json` — замена оригинального рецепта. Исходный Create-рецепт `["AS","NA"]` с `c:slimeballs + c:nuggets/iron + c:plates/iron` невозможно скрафтить в TFC-сборке (iron-теги пусты, TFC использует wrought iron). Новый рецепт shapeless: `tfc_aeronautics:metal/tight_sheet/steel + tfc:glue → create:super_glue`
- [x] `src/main/resources/data/simulated/recipe/filling/honey_glue.json` — тень, отключающая исходный басин-рецепт `create:filling` (`create:iron_sheet` + 500 мБ `c:honey` → `simulated:honey_glue`). В TFC-сборке `create:iron_sheet` не существует (TFC заменяет vanilla iron на wrought iron), а тег `c:honey` пуст. Новая тень: `minecraft:bedrock` + 500 мБ несуществующего тега `c:does_not_exist` → `minecraft:stick`. Рецепт никогда не сматчится, но остаётся валидным datapack-JSON

## Документация
- [x] Обновить `DOCS.md` — добавить раздел "20. Замена slimeball на tfc:glue" с описанием механики и ссылкой на этот план

---

## Pending (отдельные задачи, к этой механике не относятся)

Записано здесь по запросу пользователя — выполняются отдельно, не блокируют glue substitution.

- [ ] **Перерисовать текстуру `create:super_glue`.** Оригинал `code_references/Create/.../textures/item/super_glue.png`. Целевой образ (пчелиный воск / натуральный клей / янтарь) уточнить у автора. Перекрытие через `src/main/resources/assets/create/textures/item/super_glue.png` + опционально Blockbench-исходник в `blockbench/`.
- [ ] **Рефакторинг «медоклея» из Simulated.** Источники: `code_references/Simulated-Project/simulated/common/src/main/java/dev/simulated_team/simulated/content/entities/honey_glue/` (`HoneyGlueItem`, `HoneyGlueEntity`, `HoneyGlueRenderer`, `HoneyGlueMaxSizing`, `HoneyGlueClientHandler`) + `data/simulated/recipe/filling/honey_glue.json` + ачивка `not_gonna_sugarcoat_it`. Направление рефакторинга (перенос / упрощение / интеграция с TFC) уточнить у автора — после этого разбить на конкретные задачи.
