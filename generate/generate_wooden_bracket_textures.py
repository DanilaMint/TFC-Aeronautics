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

Why a script: runData's first invocation in this project takes minutes for
asset/Minecraft download + JVM warmup, and re-iterating on the texture set
shouldn't require that.
"""
from pathlib import Path

from PIL import Image

WOODS = [
    "acacia", "ash", "aspen", "birch", "blackwood", "chestnut",
    "douglas_fir", "hickory", "kapok", "mangrove", "maple", "oak",
    "palm", "pine", "rosewood", "sequoia", "spruce", "sycamore",
    "white_cedar", "willow",
]

REPO = Path(__file__).resolve().parent.parent
CREATE_TEX = REPO / "code_references" / "Create" / "src" / "main" / "resources" / "assets" / "create" / "textures" / "block"
TFC_PLANKS = REPO / "code_references" / "TerraFirmaCraft" / "src" / "main" / "resources" / "assets" / "tfc" / "textures" / "block" / "wood" / "planks"
OUT_DIR = REPO / "src" / "generated" / "resources" / "assets" / "tfc_aeronautics" / "textures" / "block" / "wood" / "bracket"


def wood_median_rgb(plank_path: Path) -> tuple[int, int, int]:
    """Median R, G, B across opaque pixels of the plank PNG."""
    img = Image.open(plank_path).convert("RGBA")
    pixels = [p for p in img.getdata() if p[3] > 0]
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


def main() -> None:
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    sources = {
        "bracket": CREATE_TEX / "bracket_wooden.png",
        "bracket_plate": CREATE_TEX / "bracket_plate_wooden.png",
    }
    missing = [str(p) for p in sources.values() if not p.exists()]
    if missing:
        raise SystemExit(f"Missing Create reference textures: {missing}")

    for wood in WOODS:
        plank = TFC_PLANKS / f"{wood}.png"
        if not plank.exists():
            raise SystemExit(f"Missing TFC plank texture: {plank}")
        wood_rgb = wood_median_rgb(plank)
        print(f"{wood:14s} median RGB = {wood_rgb}")
        for prefix, src in sources.items():
            tinted = tint(src, wood_rgb)
            tinted.save(OUT_DIR / f"{prefix}_{wood}.png")

    written = sorted(p.name for p in OUT_DIR.iterdir())
    print(f"Wrote {len(written)} PNGs to {OUT_DIR.relative_to(REPO)}")


if __name__ == "__main__":
    main()
