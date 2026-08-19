#!/usr/bin/env python3
"""
Emit the datagen output for per-wood wooden brackets directly as JSON files.

Why this exists alongside the Java datagen classes
(`src/main/java/ru/tfc_aeronautics/datagen/WoodenBracketBlockStateProvider.java`
and `WoodenBracketItemModelProvider.java`):

The Java providers produce the same files via
`./gradlew runData`, but in this project runData's first run takes minutes for
asset/Minecraft download + JVM warmup, and a fast iteration on the texture
set or rotation table shouldn't require that. This script mirrors the Java
output exactly so a contributor can re-run it cheaply and diff the result.

Output:
  src/generated/resources/assets/tfc_aeronautics/blockstates/wood/bracket/<wood>.json     (20)
  src/generated/resources/assets/tfc_aeronautics/models/block/wood/bracket/<type>/<pos>_<wood>.json  (120)
  src/generated/resources/assets/tfc_aeronautics/models/item/wood/bracket/<wood>.json   (20)

Source template:
  code_references/Create/src/generated/resources/assets/create/blockstates/wooden_bracket.json

The template is rewritten per-wood with two string substitutions:
  create:block/bracket/...     ->  tfc_aeronautics:block/wood/bracket/...
  _wooden                       ->  _<wood>
"""
import json
import shutil
from pathlib import Path

WOODS = [
    "acacia", "ash", "aspen", "birch", "blackwood", "chestnut",
    "douglas_fir", "hickory", "kapok", "mangrove", "maple", "oak",
    "palm", "pine", "rosewood", "sequoia", "spruce", "sycamore",
    "white_cedar", "willow",
]

TYPES = ["cog", "pipe", "shaft"]
POSITIONS = ["ground", "wall"]

REPO = Path(__file__).resolve().parent.parent
TEMPLATE = REPO / "code_references" / "Create" / "src" / "generated" / "resources" / "assets" / "create" / "blockstates" / "wooden_bracket.json"
GEN = REPO / "src" / "generated" / "resources" / "assets" / "tfc_aeronautics"


def render_blockstate(template_text: str, wood: str) -> str:
    # Rewrite the variant map: namespace path and material suffix swap.
    src_ns = "create:block/bracket/"
    dst_ns = "tfc_aeronautics:block/wood/bracket/"
    out = template_text.replace(src_ns, dst_ns)
    out = out.replace("_wooden", f"_{wood}")
    return out


def write_block_model(wood: str, type_: str, pos: str) -> Path:
    body = {
        "parent": f"create:block/bracket/{type_}/{pos}",
        "textures": {
            "bracket": f"tfc_aeronautics:block/wood/bracket/bracket_{wood}",
            "plate":   f"tfc_aeronautics:block/wood/bracket/bracket_plate_{wood}",
        },
    }
    path = GEN / "models" / "block" / "wood" / "bracket" / type_ / f"{pos}_{wood}.json"
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(body, indent=2) + "\n")
    return path


def write_item_model(wood: str) -> Path:
    body = {
        "parent": "create:block/bracket/item",
        "textures": {
            "bracket": f"tfc_aeronautics:block/wood/bracket/bracket_{wood}",
            "plate":   f"tfc_aeronautics:block/wood/bracket/bracket_plate_{wood}",
        },
    }
    path = GEN / "models" / "item" / "wood" / "bracket" / f"{wood}.json"
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(body, indent=2) + "\n")
    return path


def write_blockstate(wood: str, rendered: str) -> Path:
    path = GEN / "blockstates" / "wood" / "bracket" / f"{wood}.json"
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(rendered)
    return path


def main() -> None:
    if not TEMPLATE.exists():
        raise SystemExit(f"Missing template: {TEMPLATE}")

    template_text = TEMPLATE.read_text()
    # JSON sanity: make sure the template itself parses — catch corruption early.
    json.loads(template_text)

    bs_count = 0
    bm_count = 0
    im_count = 0
    for wood in WOODS:
        bs_count += 1
        write_blockstate(wood, render_blockstate(template_text, wood))
        for type_ in TYPES:
            for pos in POSITIONS:
                write_block_model(wood, type_, pos)
                bm_count += 1
        write_item_model(wood)
        im_count += 1

    print(f"Wrote {bs_count} blockstates, {bm_count} block models, {im_count} item models under")
    print(f"  {GEN.relative_to(REPO)}")


if __name__ == "__main__":
    main()
