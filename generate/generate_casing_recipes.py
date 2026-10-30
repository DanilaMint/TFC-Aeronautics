#!/usr/bin/env python3
"""
Generate bulk (shapeless) crafting recipes for Create casings out of TFC
tight sheets.

Each variant produces k casings from one tight sheet plus k logs (or k
brass casings), for k = 1..8:

    1x tfc_aeronautics:metal/tight_sheet/brass
  + kx #tfc_aeronautics:stripped_logs
  -> kx create:brass_casing

Why eight recipes instead of one with a count range?
Minecraft 1.21.1 has no ranged counts in recipe JSON:
* `Ingredient` (net.minecraft.world.item.crafting.Ingredient) has no `count`
  field at all — only `item` / `tag`;
* `ShapelessRecipe.CODEC` reads `result` through `ItemStack.STRICT_CODEC`,
  where `count` is `ExtraCodecs.intRange(1, 99)`, i.e. a plain integer.
So "1-8 logs -> 1-8 casings" cannot be expressed in a single recipe.

Eight separate shapeless recipes are exact, though: `ShapelessRecipe.matches()`
requires `input.ingredientCount() == ingredients.size()`, so the number of
non-empty grid slots has to match the ingredient count exactly. The recipe with
k logs only fires when exactly k+1 slots are filled.

k = 8 is the ceiling: one sheet plus eight logs fills a full 3x3 grid, and
`ShapelessRecipe.CODEC` rejects more than nine ingredients.

Caveat for players: the logs have to occupy *separate* slots. A stack of eight
logs in a single slot counts as one occupied slot, so that grid matches the
k = 1 recipe and yields a single casing.

`show_notification` is written as false, but note that it only has an effect on
`minecraft:crafting_shaped` in 1.21.1 — the `ShapelessRecipe` codec has no
such field and always reports `showNotification() == true`. The key is kept for
consistency with the other recipes in this mod and is silently ignored.

Output: src/generated/resources/data/tfc_aeronautics/recipe/crafting/casing/...
"""
from _common import write_json

OUT_REL = "data/tfc_aeronautics/recipe/crafting/casing"

# (file name stem, sheet item, filler ingredient, result item)
# The filler is a tuple so the JSON key stays explicit ("tag" vs "item").
VARIANTS = (
    ("brass_casing", "tfc_aeronautics:metal/tight_sheet/brass",
     ("tag", "tfc_aeronautics:stripped_logs"), "create:brass_casing"),
    ("copper_casing", "tfc_aeronautics:metal/tight_sheet/copper",
     ("tag", "tfc_aeronautics:stripped_logs"), "create:copper_casing"),
    ("andesite_casing", "tfc_aeronautics:metal/tight_sheet/wrought_iron",
     ("tag", "tfc_aeronautics:stripped_logs"), "create:andesite_casing"),
    ("railway_casing", "tfc_aeronautics:metal/tight_sheet/steel",
     ("item", "create:brass_casing"), "create:railway_casing"),
)

MIN_BULK = 1
MAX_BULK = 8  # 1 sheet + 8 logs = 9 ingredients = the 3x3 grid limit


def build_recipe(sheet: str, filler: tuple[str, str], result: str, bulk: int) -> dict:
    key, filler_id = filler
    return {
        "type": "minecraft:crafting_shapeless",
        "category": "misc",
        "ingredients": [{"item": sheet}] + [{key: filler_id}] * bulk,
        "result": {"count": bulk, "id": result},
        "show_notification": False,
    }


def main() -> int:
    written = 0
    for stem, sheet, filler, result in VARIANTS:
        for bulk in range(MIN_BULK, MAX_BULK + 1):
            write_json(f"{OUT_REL}/{stem}_{bulk}.json",
                       build_recipe(sheet, filler, result, bulk))
            written += 1

    print(f"wrote {written} recipes under {OUT_REL}/")
    return written


if __name__ == "__main__":
    raise SystemExit(main())
