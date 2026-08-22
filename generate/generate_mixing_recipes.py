#!/usr/bin/env python3
"""Generate `create:mixing` recipes for TFC grain flour + water → dough.

Bridge recipes that let the TFC food pipeline use Create's basin + mixer as
an alternative to `tfc:advanced_shapeless_crafting`. Without a mixin
(`BasinMixingFoodDataMixin`) the dough produced by Create would have no
TFC `food` data and never decay — the mixin copies rot timer / traits
from the captured flour to the result.

Output: src/generated/resources/data/tfc_aeronautics/recipe/mixing/<grain>_dough.json
"""
from _common import write_json

GRAINS = ["barley", "maize", "oat", "rice", "rye", "wheat"]
RECIPE_TYPE = "create:mixing"
WATER_AMOUNT = 100  # mB per batch — matches TFC's crafting recipe (100mB → 1 dough)


def main() -> int:
    count = 0
    for grain in GRAINS:
        write_json(f"data/tfc_aeronautics/recipe/mixing/{grain}_dough.json", {
            "type": RECIPE_TYPE,
            "ingredients": [
                {"item": f"tfc:food/{grain}_flour"},
                {"type": "neoforge:single", "amount": WATER_AMOUNT, "fluid": "minecraft:water"},
            ],
            "results": [{"count": 1, "id": f"tfc:food/{grain}_dough"}],
        })
        count += 1
    print(f"wrote {count} mixing recipes under data/tfc_aeronautics/recipe/mixing/")
    return count


if __name__ == "__main__":
    raise SystemExit(main())
