#!/usr/bin/env python3
"""
Generate `create:milling` recipes shadowing Create's processing under TFC ingredients.

These mirror the JSON files originally hand-added in commit `8d6a650 "sync quern
and millstone"`. Two pieces:

* 11 specials — pattern (input_kind, input_value, output_id, count) per file.
  Only `powder/flux` uses a tag ingredient; everything else is an item.
* 48 ore → powder — 12 TFC ore minerals × 4 grades (small/poor/normal/rich).
  Count per grade: small=2, poor=3, normal=5, rich=7.

Output: src/generated/resources/data/create/recipe/milling/...
"""
from _common import write_json

SPECIALS: list[tuple[str, str, str, str, int]] = [
    # (rel_path, ingredient_kind, ingredient_value, output, count)
    ("powder/charcoal.json",        "item", "minecraft:charcoal",      "tfc:powder/charcoal",  4),
    ("powder/flux.json",            "tag",  "tfc:fluxstone",           "tfc:powder/flux",      2),
    ("powder/flux_from_borax.json", "item", "tfc:ore/borax",           "tfc:powder/flux",      6),
    ("powder/graphite.json",        "item", "tfc:ore/graphite",        "tfc:powder/graphite",  4),
    ("powder/salt.json",            "item", "tfc:ore/halite",          "tfc:powder/salt",      4),
    ("powder/saltpeter.json",       "item", "tfc:ore/saltpeter",       "tfc:powder/saltpeter", 4),
    ("powder/sulfur.json",          "item", "tfc:ore/sulfur",          "tfc:powder/sulfur",    4),
    ("powder/sylvite.json",         "item", "tfc:ore/sylvite",         "tfc:powder/sylvite",   4),
    ("ore/gypsum.json",             "item", "tfc:rock/raw/limestone",  "tfc:ore/gypsum",       1),
    ("canola_paste.json",           "item", "tfc:seeds/canola",        "tfc:canola_paste",     2),
    ("lime_dye.json",               "item", "tfc:plant/moss",          "minecraft:lime_dye",   2),
]

ORE_MINERALS = [
    "bismuthinite", "cassiterite", "garnierite", "hematite", "limonite",
    "magnetite", "malachite", "native_copper", "native_gold", "native_silver",
    "sphalerite", "tetrahedrite",
]

GRADES: list[tuple[str, int]] = [("small", 2), ("poor", 3), ("normal", 5), ("rich", 7)]

PROCESSING_TIME = 250
RECIPE_TYPE = "create:milling"


def main() -> int:
    count = 0

    for rel_path, kind, value, output, out_count in SPECIALS:
        write_json(f"data/create/recipe/milling/{rel_path}", {
            "type": RECIPE_TYPE,
            "ingredients": [{kind: value}],
            "processing_time": PROCESSING_TIME,
            "results": [{"count": out_count, "id": output}],
        })
        count += 1

    for mineral in ORE_MINERALS:
        for grade, grade_count in GRADES:
            write_json(f"data/create/recipe/milling/powder/{mineral}_{grade}.json", {
                "type": RECIPE_TYPE,
                "ingredients": [{"item": f"tfc:ore/{grade}_{mineral}"}],
                "processing_time": PROCESSING_TIME,
                "results": [{"count": grade_count, "id": f"tfc:powder/{mineral}"}],
            })
            count += 1

    print(f"wrote {count} milling recipes under data/create/recipe/milling/")
    return count


if __name__ == "__main__":
    raise SystemExit(main())
