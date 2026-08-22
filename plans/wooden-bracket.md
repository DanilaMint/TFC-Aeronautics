# Wooden Bracket (TFC wood variants)

**Прогресс:** 18/19 ✓ (smoke-чек на стороне пользователя — `./build.sh install` собирает jar без ошибок)

20 блоков-аналогов `create:wooden_bracket` — по одному на каждую породу дерева TFC.
Namespace-путь в стиле TFC convention: `tfc_aeronautics:wood/bracket/<wood>`.
Рецепт: 5 lumber нужного дерева (`tfc:wood/lumber/<wood>`) в форме шлема
`["PPP", "P P"]` → 1 кронштейн. Геометрия и поведение (ПКМ по shaft/cog/pipe)
наследуются от `com.simibubi.create.content.decoration.bracket.BracketBlock`
и `BracketBlockItem`. Текстуры генерируются Python-скриптом из оригинальной
PNG Create с переводом оттенка по медиане TFC-планки нужной породы.
`create:crafting/kinetics/wooden_bracket` банится.

Список 20 пород: acacia, ash, aspen, birch, blackwood, chestnut,
douglas_fir, hickory, kapok, mangrove, maple, oak, palm, pine, rosewood,
sequoia, spruce, sycamore, white_cedar, willow.

## Регистрация блоков и предметов
- [x] `src/main/java/ru/tfc_aeronautics/bracket/WoodenBracket.java` — extends `BracketBlock`, конструктор `(Properties)`
- [x] `src/main/java/ru/tfc_aeronautics/bracket/WoodenBracketItem.java` — extends `BracketBlockItem`, конструктор `(Block, Item.Properties)`
- [x] `src/main/java/ru/tfc_aeronautics/bracket/WoodenBracketRegistration.java` — два `DeferredRegister` + цикл `for (String wood : WOODS)` → `BLOCKS.register("wood/bracket/" + wood, ...)` + `ITEMS.register("wood/bracket/" + wood, ...)` + публичные `Map<String, DeferredHolder<...>> BRACKETS/BRACKET_ITEMS` + `register(IEventBus)`
- [x] Подключить в `TFCAeronautics.java`: `WoodenBracketRegistration.register(modEventBus)` рядом с другими регистрациями (`TFCAeronautics.java:61`)
- [x] В `CreativeTabs.java`: в `displayItems` добавить `WoodenBracketRegistration.BRACKETS.keySet().forEach(wood -> output.accept(WoodenBracketRegistration.BRACKET_ITEMS.get(wood).get()))` (`CreativeTabs.java:50`)

## Текстурирование
- [x] `generate/generate_wooden_bracket_textures.py` (Python + Pillow): на каждый wood берёт эталон `code_references/Create/src/main/resources/assets/create/textures/block/bracket_wooden.png`, семплирует медиану центральной 50% `assets/tfc/textures/block/wood/planks/<wood>.png` из TFC-jar, обесцвечивает эталон в ЧБ по luminance с premultiply-alpha, рескейлит так, чтобы самый яркий пиксель == `#FFFFFF`, умножает на wood-тон и сохраняет в `src/generated/resources/assets/tfc_aeronautics/textures/block/wood/bracket/bracket_<wood>.png` (то же для `bracket_plate_<wood>.png`).
- [x] `generate/verify_wooden_bracket_textures.py` — диагностический (read-only) спутник первого скрипта: для каждой PNG считает avg-цвет источника и выхода и ставит вердикт `OK/DIM/BRIGHTER/DARKER/DRIFT` по тому, насколько ярчайший выходной пиксель совпадает с медианой wood-планки. Исключает из average намеренное чёрное пятно в центре кронштейна.

## Datagen (blockstate + block model + item model)

> **Обходной путь:** Java-провайдеры написаны, но `runData` подвисает на JVM-warmup (`moddev-plugin 2.0.141`). Поэтому JSON-выход эмитится напрямую через `generate/generate_wooden_bracket_assets.py` (string-templating от `code_references/Create/src/generated/resources/assets/create/blockstates/wooden_bracket.json`) — тот же набор 160 файлов, что и runData бы выдал. Сами Java-провайдеры оставлены в репо как документация контракта и как fallback, если runData починят.

- [x] `src/main/java/ru/tfc_aeronautics/datagen/TFCAeronauticsDataGen.java` — `@Mod.EventBusSubscriber` + статический `@SubscribeEvent gatherData(GatherDataEvent event)`; регистрирует `WoodenBracketBlockStateProvider` и `WoodenBracketItemModelProvider` через `event.getGenerator().addProvider(...)` (`TFCAeronauticsDataGen.java:29-30`)
- [x] `src/main/java/ru/tfc_aeronautics/datagen/WoodenBracketBlockStateProvider.java` — extends `BlockStateProvider`; 36 строк blockstate × 20 wood (по rotation-таблице из Create). На каждый wood также 6 per-wood моделей через `withExistingParent(...)` (cog+pipe+shaft × ground+wall). Итого 120 файлов под `models/block/wood/bracket/`.
- [x] `src/main/java/ru/tfc_aeronautics/datagen/WoodenBracketItemModelProvider.java` — extends `ItemModelProvider`; 20 `models/item/wood/bracket/<wood>.json` с `parent: create:block/bracket/item`, текстуры `bracket`/`plate` на per-wood PNG.

## Рецепты
- [x] 20 × `src/main/resources/data/tfc_aeronautics/recipe/crafting/wood/bracket/<wood>.json` (сгенерированы `generate/generate_wooden_bracket_recipes.py`). Шаблон:
  ```json
  {
    "type": "minecraft:crafting_shaped",
    "category": "misc",
    "show_notification": false,
    "key": { "P": { "item": "tfc:wood/lumber/<wood>" } },
    "pattern": ["PPP", "P P"],
    "result": { "count": 1, "id": "tfc_aeronautics:wood/bracket/<wood>" }
  }
  ```
  Per-wood item-id выбран, потому что per-wood TFC tag для lumber отсутствует; общий `tfc:lumber` сломал бы per-wood идентичность результата.

## RecipeRemoval
- [x] В `src/main/java/ru/tfc_aeronautics/recipe/RecipeRemoval.java` добавлено 7-м аргументом в `ImmutableSet.of(...)` (после `metal_bracket`):
  `ResourceLocation.fromNamespaceAndPath("create", "crafting/kinetics/wooden_bracket")` (`RecipeRemoval.java:53`)

## DOCS и ROADMAP
- [x] `DOCS.md` — добавлен §22 «Деревянные кронштейны по породе (TFC Wooden Brackets)» после §21, обновлён TOC (§22 = строки 2322–2388 + запись в TOC на 32)
- [x] `ROADMAP.md` — подсистема отмечена `✓` с явным описанием обходного пути runData (Python-эмиттер) и verify-скрипта
- [x] Локализация добавлена: 40 ключей (20× block + 20× item) в `en_us.json` (`Oak Bracket`, …) и `ru_ru.json` (`Дубовый кронштейн`, `Гренадиловый кронштейн`, `Сейбовый кронштейн`, `Белокедровый кронштейн`, …, по TFC-словарю пород)

## Верификация
- [x] `./gradlew compileJava` — успех (`./gradlew build` отрабатывает, 6 actionable tasks, BUILD SUCCESSFUL ≤ 800ms; `compileJava` UP-TO-DATE после правок)
- [x] Python-эмиттер даты: `python3 generate/generate_wooden_bracket_assets.py` — 160 JSON (20 blockstate + 120 block model + 20 item model) под `src/generated/resources/assets/tfc_aeronautics/`. runData остался запасным вариантом; Java-провайдеры в репо как fallback.
- [x] `python3 generate/generate_wooden_bracket_textures.py` — 40 PNG (2 × 20) под `src/generated/.../textures/block/wood/bracket/`
- [x] `python3 generate/verify_wooden_bracket_textures.py` — `OK=40/40, DIM=0, DRIFT=0, MISSING=0` (ярчайший пиксель каждой PNG точно совпадает с медианой wood-планки)
- [x] `python3 generate/generate_wooden_bracket_recipes.py` — 20 JSON рецептов под `src/main/resources/data/tfc_aeronautics/recipe/crafting/wood/bracket/`
- [ ] **Пользователь проверяет в Prism-лаунчере** (не запускаем `runClient`/`runServer`):
  - `/give @s tfc_aeronautics:wood/bracket/oak` — есть в инвентаре и в creative-табе мода
  - ПКМ по `create:shaft` с разными кронштейнами — ставятся по цвету породы
  - 5 oak-lumber в шлем-pattern → `tfc_aeronautics:wood/bracket/oak`
  - Из `tfc:wood/planks/oak` — рецепт НЕ срабатывает (lumber ≠ planks)
  - JEI: 20 per-wood рецептов; vanilla `create:wooden_bracket` recipe исчез
  - В creative-табе названия: «Oak Bracket» / «Дубовый кронштейн» (а не сырой тег)
