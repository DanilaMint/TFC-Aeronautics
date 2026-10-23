"""Shared constants and helpers for the Python datagen scripts."""
from __future__ import annotations

import io
import json
import os
import zipfile
from pathlib import Path

REPO = Path(__file__).resolve().parent.parent
GENERATE = REPO / "generate"
GEN = REPO / "src" / "generated" / "resources"

# Upstream assets that datagen needs but cannot reach through Gradle.
#
# `code_references/` (gitignored local checkouts of Create / TFC) used to play
# that role, but it is not part of the repo, so every contributor without
# those checkouts hit "Missing Create reference textures" / "Missing template".
# Anything datagen needs is either vendored under `generate/` next to the
# scripts, or read straight out of the jars `build.gradle` already compiles
# against (`libs/`).
LIBS = REPO / "libs"
VENDORED_TEXTURES = GENERATE / "textures"
VENDORED_TEMPLATES = GENERATE / "templates"

WOODS = (
    "acacia", "ash", "aspen", "birch", "blackwood", "chestnut",
    "douglas_fir", "hickory", "kapok", "mangrove", "maple", "oak",
    "palm", "pine", "rosewood", "sequoia", "spruce", "sycamore",
    "white_cedar", "willow",
)

DYE_COLORS: tuple[str, ...] = ("white", "light_gray", "gray", "black", "red", "orange", "yellow", "lime", "green", "light_blue", "blue", "cyan", "purple", "pink", "brown", "magenta")

# TFC item_size enums (from TerraFirmaCraft/.../component/size/{Size,Weight}.java).
# Used by create_item_sizes_map.py to validate literals at import time.
TFC_SIZE_VALUES: tuple[str, ...] = (
    "tiny", "very_small", "small", "normal", "large", "very_large", "huge",
)
TFC_WEIGHT_VALUES: tuple[str, ...] = (
    "very_light", "light", "medium", "heavy", "very_heavy",
)


def write_json(rel_path: str | Path, data: dict) -> Path:
    """Write JSON to GEN/rel_path with mkdir -p and trailing newline."""
    p = GEN / rel_path
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n")
    return p


def find_lib_jar(prefix: str, env_var: str) -> Path:
    """Locate a compile-time dependency jar inside `libs/`.

    These jars are declared in `build.gradle`, so they are a build
    prerequisite rather than a local-only extra. `env_var` overrides the
    lookup for unusual layouts (e.g. `TFC_JAR=/path/to/tfc-1.21.1.jar`).
    """
    override = os.environ.get(env_var)
    if override:
        p = Path(override).expanduser()
        if not p.is_file():
            raise SystemExit(f"{env_var}={p} does not exist or is not a file")
        return p
    # `[0-9]` keeps `tfc-aeronautics-*.jar`-style bundles out of the match.
    candidates = sorted(p for p in LIBS.glob(f"{prefix}-[0-9]*.jar") if p.is_file())
    if not candidates:
        raise SystemExit(
            f"No {prefix}-[0-9]*.jar in {LIBS}. It is a compile-time dependency "
            f"declared in build.gradle — put it there, or set {env_var} to its path."
        )
    return candidates[-1]


_TFC_JAR: zipfile.ZipFile | None = None


def tfc_plank_rgba(wood: str) -> "Image.Image":
    """RGBA image of a TFC plank texture, read straight out of the TFC jar.

    Vendoring 20 plank PNGs next to the scripts would work too, but they are
    TFC data that shifts with every TFC release — reading them from the jar
    keeps the generated tints in sync with the TFC build we compile against.
    """
    global _TFC_JAR
    if _TFC_JAR is None:
        _TFC_JAR = zipfile.ZipFile(find_lib_jar("tfc", "TFC_JAR"))
    member = f"assets/tfc/textures/block/wood/planks/{wood}.png"
    try:
        data = _TFC_JAR.read(member)
    except KeyError:
        available = sorted(
            n.rsplit("/", 1)[-1][:-4]
            for n in _TFC_JAR.namelist()
            if n.startswith("assets/tfc/textures/block/wood/planks/") and n.endswith(".png")
        )
        raise SystemExit(
            f"{member} not found in {_TFC_JAR.filename}; available planks: {available}"
        ) from None

    from PIL import Image  # local import: JSON-only scripts need no Pillow
    return Image.open(io.BytesIO(data)).convert("RGBA")
