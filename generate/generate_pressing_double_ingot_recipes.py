#!/usr/bin/env python3
"""
Generate `create:pressing` recipes turning TFC double ingots into TFC sheets.

This is the industrial alternative to the TFC anvil path (which requires 3 hammer
hits and a player-tier progression). Mechanical press + connected kinetic turns
1 double ingot into 1 sheet per 240 ticks of press time.

Coverage: 27 metals from `code_references/TerraFirmaCraft/metals.json`
  * pure (8): bismuth, copper, gold, nickel, silver, tin, zinc, wrought_iron
  * cast/pig (2): cast_iron, pig_iron
  * alloys (9): bismuth_bronze, black_bronze, brass, bronze, rose_gold,
                sterling_silver, weak_blue_steel, weak_red_steel, weak_steel
  * steels (8): steel, black_steel, blue_steel, red_steel,
                high_carbon_steel, high_carbon_black_steel,
                high_carbon_blue_steel, high_carbon_red_steel

Output: src/generated/resources/data/tfc_aeronautics/recipe/pressing/<metal>_double_ingot.json

No conflicts with existing pressing recipes:
* vanilla create:pressing/{iron,copper,gold}_ingot use tag inputs (c:ingots/<m>),
  ours uses explicit tfc:metal/double_ingot/<m>.
* existing tight_sheet overrides under data/create/recipe/pressing/ also use
  c:ingots/<m> tags and produce tfc_aeronautics:metal/tight_sheet/<m>.
* RecipeRemoval.BANNED_RECIPES only bans create:pressing/copper_ingot; not
  affected.
"""
from _common import write_json

METALS = [
    # pure
    "bismuth", "copper", "gold", "nickel", "silver", "tin", "zinc", "wrought_iron",
    # cast / pig
    "cast_iron", "pig_iron",
    # alloys
    "bismuth_bronze", "black_bronze", "brass", "bronze", "rose_gold", "sterling_silver",
    "weak_blue_steel", "weak_red_steel", "weak_steel",
    # steels
    "steel", "black_steel", "blue_steel", "red_steel",
    "high_carbon_steel", "high_carbon_black_steel",
    "high_carbon_blue_steel", "high_carbon_red_steel",
]
assert len(METALS) == 27

SKIP: set[str] = set()

RECIPE_TYPE = "create:pressing"


def main() -> int:
    count = 0
    for metal in METALS:
        if metal in SKIP:
            print(f"  skip {metal} (in SKIP)")
            continue
        write_json(
            f"data/tfc_aeronautics/recipe/pressing/{metal}_double_ingot.json",
            {
                "type": RECIPE_TYPE,
                "ingredients": [
                    {"item": f"tfc:metal/double_ingot/{metal}"}
                ],
                "results": [
                    {"id": f"tfc:metal/sheet/{metal}"}
                ],
                "show_notification": True,
            },
        )
        count += 1
    print(f"wrote {count} pressing recipes under data/tfc_aeronautics/recipe/pressing/")
    return count


if __name__ == "__main__":
    raise SystemExit(main())