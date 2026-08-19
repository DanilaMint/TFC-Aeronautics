#!/usr/bin/env python3
"""
Generate 20 per-wood crafting recipes for the TFC Aeronautics wooden bracket.

Each recipe takes five pieces of TFC lumber of the matching wood in the helmet
pattern ["PPP","P P"] and produces one tfc_aeronautics:wood/bracket/<wood>.
The single-tag form was avoided because no per-wood tag exists for lumber and
a global tfc:lumber tag would make the result independent of the ingredient.
"""
from pathlib import Path

WOODS = [
    "acacia", "ash", "aspen", "birch", "blackwood", "chestnut",
    "douglas_fir", "hickory", "kapok", "mangrove", "maple", "oak",
    "palm", "pine", "rosewood", "sequoia", "spruce", "sycamore",
    "white_cedar", "willow",
]

REPO = Path(__file__).resolve().parent.parent
OUT_DIR = REPO / "src" / "main" / "resources" / "data" / "tfc_aeronautics" / "recipe" / "crafting" / "wood" / "bracket"

TEMPLATE = """{{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "show_notification": false,
  "key": {{
    "P": {{ "item": "tfc:wood/lumber/{wood}" }}
  }},
  "pattern": [ "PPP", "P P" ],
  "result": {{ "count": 1, "id": "tfc_aeronautics:wood/bracket/{wood}" }}
}}
"""


def main() -> None:
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    for wood in WOODS:
        path = OUT_DIR / f"{wood}.json"
        path.write_text(TEMPLATE.format(wood=wood))
        print(f"wrote {path.relative_to(REPO)}")

    print(f"\nDone: {len(WOODS)} recipes")


if __name__ == "__main__":
    main()
