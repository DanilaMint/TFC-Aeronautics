#!/usr/bin/env python3
"""
Generate per-wood bracket textures for TFC Aeronautics.

For each wood, two PNGs are written to
  src/generated/resources/assets/tfc_aeronautics/textures/block/wood/bracket/:
    - bracket_<wood>.png        (the bracket shape, wood-tinted)
    - bracket_plate_<wood>.png  (the wood plate the bracket sits on)

The pipeline is fixed-brightness + wood-tint:

1. Take the median RGB of the central 50% of the TFC plank texture as the
   wood colour for that species.
2. Load the matching Create reference (bracket_wooden.png /
   bracket_plate_wooden.png) and convert each pixel to a single luminance
   value (true B/W, premultiplied by alpha).
3. Find the brightest opaque pixel; scale the whole B/W image so that this
   pixel becomes #FFFFFF. Darkest pixel stays at its raw scaled value
   (not stretched to black — that would destroy the silhouette).
4. Multiply the scaled B/W image by the wood colour: each pixel becomes
   (gray/255) · wood_colour. The brightest end of the bracket reads as the
   full TFC wood tone rather than a dimmed fraction of it.

Inputs (neither needs the gitignored `code_references/` checkouts):
  generate/textures/bracket_wooden.png         vendored verbatim from
  generate/textures/bracket_plate_wooden.png    Create 6.0.x
                                               (assets/create/textures/block/)
  assets/tfc/textures/block/wood/planks/*.png  read out of the TFC jar in
                                               `libs/` (see _common.tfc_plank_rgba)

Why a script: runData's first invocation in this project takes minutes for
asset/Minecraft download + JVM warmup, and re-iterating on the texture set
shouldn't require that.
"""
from pathlib import Path

from PIL import Image

from _common import WOODS, REPO, VENDORED_TEXTURES, tfc_plank_rgba

# Vendored Create reference textures, sitting next to this script.
BRACKET_TEX = VENDORED_TEXTURES
OUT_DIR = REPO / "src" / "generated" / "resources" / "assets" / "tfc_aeronautics" / "textures" / "block" / "wood" / "bracket"

SOURCES = {
    "bracket": BRACKET_TEX / "bracket_wooden.png",
    "bracket_plate": BRACKET_TEX / "bracket_plate_wooden.png",
}


def wood_median_rgb(plank: Image.Image) -> tuple[int, int, int]:
    """Median R, G, B across opaque pixels of the plank PNG."""
    pixels = [p for p in plank.getdata() if p[3] > 0]
    rs = sorted(p[0] for p in pixels)
    gs = sorted(p[1] for p in pixels)
    bs = sorted(p[2] for p in pixels)
    mid = len(rs) // 2
    return rs[mid], gs[mid], bs[mid]


def desaturate_and_scale(src: Image.Image) -> tuple[list[int], list[int]]:
    """Convert src to a single B/W luminance list, then scale so max = 255.

    Returns (gray_per_pixel, alpha_per_pixel) — both length = src.size[0]*src.size[1].
    Transparent pixels (α=0) get gray=0 (so they stay black if the wood colour
    has any red/green/blue leakage); alpha is preserved.
    """
    pixels = list(src.getdata())
    grays: list[int] = []
    alphas: list[int] = []
    max_gray = 0
    for r, g, b, a in pixels:
        if a == 0:
            grays.append(0)
            alphas.append(0)
            continue
        # premultiplied luminance — what a non-premultiplied B/W rendering of
        # this pixel would look like once displayed against a fully opaque
        # background. This keeps bright-but-transparent-from-alpha pixels from
        # blowing up the dynamic range of the scale step.
        premul = round((0.299 * r + 0.587 * g + 0.114 * b) * (a / 255.0))
        grays.append(premul)
        alphas.append(a)
        if premul > max_gray:
            max_gray = premul

    if max_gray > 0:
        scale = 255.0 / max_gray
        grays = [min(255, round(g * scale)) for g in grays]
    return grays, alphas


def tint(texture_path: Path, wood_rgb: tuple[int, int, int]) -> Image.Image:
    """Desaturate + scale + multiply-blend the source with the wood colour."""
    src = Image.open(texture_path).convert("RGBA")
    grays, alphas = desaturate_and_scale(src)
    wr, wg, wb = wood_rgb
    out = Image.new("RGBA", src.size)
    for i, ((_, _, _, _), gray, alpha) in enumerate(zip(src.getdata(), grays, alphas)):
        if alpha == 0:
            out.putpixel((i % src.width, i // src.width), (0, 0, 0, 0))
            continue
        factor = gray / 255.0
        out.putpixel(
            (i % src.width, i // src.width),
            (round(wr * factor), round(wg * factor), round(wb * factor), alpha),
        )
    return out


def main() -> int:
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    missing = [str(p) for p in SOURCES.values() if not p.exists()]
    if missing:
        raise SystemExit(
            f"Missing vendored Create reference textures: {missing} "
            f"(expected under {BRACKET_TEX}, next to this script)"
        )

    written = 0
    for wood in WOODS:
        wood_rgb = wood_median_rgb(tfc_plank_rgba(wood))
        print(f"{wood:14s} median RGB = {wood_rgb}")
        for prefix, src in SOURCES.items():
            tinted = tint(src, wood_rgb)
            tinted.save(OUT_DIR / f"{prefix}_{wood}.png")
            written += 1

    print(f"wrote {written} PNGs to {OUT_DIR.relative_to(REPO)}")
    return written


if __name__ == "__main__":
    raise SystemExit(main())
