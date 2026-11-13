# Press to Sheet (Create mechanical press)

**Прогресс:** 27/27 ✓

## Рецепты
- [x] 27× Create pressing: `data/tfc_aeronautics/recipe/pressing/<metal>_double_ingot.json`
  для каждого из 27 металлов: pure (bismuth, copper, gold, nickel, silver, tin, zinc, wrought_iron),
  cast/pig (cast_iron, pig_iron), alloys (bismuth_bronze, black_bronze, brass, bronze, rose_gold,
  sterling_silver, weak_blue_steel, weak_red_steel, weak_steel), steels (steel, black_steel,
  blue_steel, red_steel, high_carbon_steel, high_carbon_black_steel, high_carbon_blue_steel,
  high_carbon_red_steel). Input `tfc:metal/double_ingot/<metal>` → output `tfc:metal/sheet/<metal>`.
- [x] Datagen: `generate/generate_pressing_double_ingot_recipes.py`, подключён в `generate/generate.py`
  (LEAVES + GROUPS).

## Мотивация

TFC-наковальня даёт sheet за 3 удара молота (player-progression, tiers 1/3/4, hit_last/second/third).
Create mechanical press — индустриальная альтернатива: 1 двойной слиток → 1 лист за 240 тиков
пресса при подведённой кинетике. Не заменяет TFC-путь, дополняет его для игроков с готовым
Create-сетапом.

## Конфликты — нет

- Ванильный `create:pressing/copper_ingot` (input — tag `c:ingots/copper`) забанен в
  `RecipeRemoval.BANNED_RECIPES`; наши рецепты используют явный item `tfc:metal/double_ingot/<metal>`,
  пересечений нет.
- Существующие tight_sheet pressing-override'ы под `data/create/recipe/pressing/tight_sheet_*.json`
  берут tag `c:ingots/<metal>` (не `tfc:metal/double_ingot/<metal>`) — пересечений нет.

## Что НЕ нужно

- `tfc:item_heat` для листов — прессование холодное, как и ванильный Create pressing.
- Правки в Java-коде — только datapack recipes.
- Правки в `RecipeRemoval.BANNED_RECIPES` — нет конфликтов с новыми input'ами.