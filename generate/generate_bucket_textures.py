#!/usr/bin/env python3
"""
Generate the rosin bucket item texture.

Composites the vanilla Minecraft empty bucket with a fluid overlay tinted
with the rosin colour. The "fluid region" shape is derived from
vanilla `water_bucket.png` — pixels where it differs significantly from
the empty `bucket.png` mark the inside-of-the-bucket area where any
fluid would be visible. Those pixels get re-tinted with rosin; pixels
identical to the empty bucket are kept as-is.

Output:
  src/generated/resources/assets/tfc_aeronautics/textures/item/rosin_bucket.png

The rosin colour is the same one used for the in-world fluid render —
see `src/main/java/ru/tfc_aeronautics/client/FluidClientExtensions.java:35`.

The vanilla bucket textures are read from
  code_references/Minecraft/1.21.1/assets/minecraft/textures/item/{bucket,water_bucket}.png
(`code_references/` is gitignored — see CLAUDE.md). If those files are
missing, the script falls back to extracting them on the fly from a
Minecraft 1.21.1 client jar. Override the jar path via $MC_JAR; otherwise
the script probes the Flatpak Prism launcher location used on this dev box.
"""
from __future__ import annotations

import os
import zipfile
from pathlib import Path

from PIL import Image

from _common import REPO

MC_ITEM_TEX = (
    REPO
    / "code_references"
    / "Minecraft"
    / "1.21.1"
    / "assets"
    / "minecraft"
    / "textures"
    / "item"
)
OUT = (
    REPO
    / "src"
    / "generated"
    / "resources"
    / "assets"
    / "tfc_aeronautics"
    / "textures"
    / "item"
    / "rosin_bucket.png"
)

# Source of truth: FluidClientExtensions.java:35 — `TFCFluids.ALPHA_MASK | 0xC68A3A`.
ROSIN_RGB: tuple[int, int, int] = (0xC6, 0x8A, 0x3A)

# Sum-of-channel-difference threshold (0..765). Anything above this means the
# water_bucket pixel has clearly deviated from the empty bucket pixel —
# empirically this separates the blue water fill (~35/79/204) from the gray
# rim shading (~84-168/84-168/84-168) with plenty of headroom.
FLUID_DIFF_THRESHOLD = 30

# Probed in order; first hit wins. Override via the MC_JAR env var to skip
# the search (e.g. on a MultiMC install or a CI runner).
_DEFAULT_MC_JAR_CANDIDATES = (
    Path(
        "~/.var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher"
        "/libraries/com/mojang/minecraft/1.21.1/minecraft-1.21.1-client.jar"
    ),
    Path("~/.minecraft/versions/1.21.1/1.21.1.jar"),
    Path("~/Library/Application Support/minecraft/versions/1.21.1/1.21.1.jar"),
)


def _resolve_mc_jar() -> Path:
    env = os.environ.get("MC_JAR")
    if env:
        p = Path(env).expanduser()
        if not p.is_file():
            raise SystemExit(f"MC_JAR={p} does not exist or is not a file")
        return p
    for cand in _DEFAULT_MC_JAR_CANDIDATES:
        p = cand.expanduser()
        if p.is_file():
            return p
    raise SystemExit(
        "Minecraft 1.21.1 client jar not found. Set $MC_JAR to its path or "
        f"vendor bucket.png + water_bucket.png into {MC_ITEM_TEX}."
    )


def _ensure_vanilla_textures() -> tuple[Path, Path]:
    empty_path = MC_ITEM_TEX / "bucket.png"
    water_path = MC_ITEM_TEX / "water_bucket.png"
    if empty_path.is_file() and water_path.is_file():
        return empty_path, water_path

    jar = _resolve_mc_jar()
    MC_ITEM_TEX.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(jar) as zf:
        for member, dest in (
            ("assets/minecraft/textures/item/bucket.png", empty_path),
            ("assets/minecraft/textures/item/water_bucket.png", water_path),
        ):
            with zf.open(member) as src, open(dest, "wb") as dst:
                dst.write(src.read())
    print(f"extracted vanilla textures from {jar} into {MC_ITEM_TEX}")
    return empty_path, water_path


def composite_fluid_bucket(
    empty: Image.Image, water: Image.Image, fluid_rgb: tuple[int, int, int]
) -> Image.Image:
    fr, fg, fb = fluid_rgb
    out = Image.new("RGBA", empty.size)

    for y in range(empty.height):
        for x in range(empty.width):
            er, eg, eb, ea = empty.getpixel((x, y))
            wr, wg, wb, wa = water.getpixel((x, y))

            diff = abs(wr - er) + abs(wg - eg) + abs(wb - eb)
            if diff <= FLUID_DIFF_THRESHOLD:
                out.putpixel((x, y), (er, eg, eb, ea))
                continue

            luma = max(0.05, (wr + wg + wb) / (3 * 255))
            tinted = (round(fr * luma), round(fg * luma), round(fb * luma))
            alpha = max(ea, wa) / 255.0
            base_r = er * (1 - alpha) + tinted[0] * alpha
            base_g = eg * (1 - alpha) + tinted[1] * alpha
            base_b = eb * (1 - alpha) + tinted[2] * alpha
            out.putpixel(
                (x, y),
                (round(base_r), round(base_g), round(base_b), max(ea, wa)),
            )

    return out


def main() -> int:
    empty_path, water_path = _ensure_vanilla_textures()

    empty = Image.open(empty_path).convert("RGBA")
    water = Image.open(water_path).convert("RGBA")
    if empty.size != water.size:
        raise SystemExit(
            f"Size mismatch: {empty_path.name}={empty.size} vs {water_path.name}={water.size}"
        )

    OUT.parent.mkdir(parents=True, exist_ok=True)
    composite_fluid_bucket(empty, water, ROSIN_RGB).save(OUT)
    print(f"wrote 1 PNG to {OUT.relative_to(REPO)}")
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
