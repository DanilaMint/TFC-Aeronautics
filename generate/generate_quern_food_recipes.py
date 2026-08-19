#!/usr/bin/env python3
"""
Generate `tfc_aeronautics:quern_milling` recipes for grain → flour.

The ingredient is a `tfc:and` matching the grain item AND `tfc:not_rotten`
(milling damaged grain is rejected). The result carries `tfc:copy_food` so the
flour inherits the grain's rot timer.

Output: src/generated/resources/data/tfc_aeronautics/recipe/milling/food/<grain>_flour.json
"""
from _common import write_json

GRAINS = ["barley", "maize", "oat", "rice", "rye", "wheat"]

PROCESSING_TIME = 250
RECIPE_TYPE = "tfc_aeronautics:quern_milling"


def main() -> int:
    count = 0
    for grain in GRAINS:
        write_json(f"data/tfc_aeronautics/recipe/milling/food/{grain}_flour.json", {
            "type": RECIPE_TYPE,
            "ingredient": {
                "type": "tfc:and",
                "children": [
                    {"item": f"tfc:food/{grain}_grain"},
                    {"type": "tfc:not_rotten"},
                ],
            },
            "result": {
                "modifiers": [{"type": "tfc:copy_food"}],
                "stack": {"count": 1, "id": f"tfc:food/{grain}_flour"},
            },
            "processing_time": PROCESSING_TIME,
        })
        count += 1

    print(f"wrote {count} quern milling recipes under data/tfc_aeronautics/recipe/milling/food/")
    return count


if __name__ == "__main__":
    raise SystemExit(main())
