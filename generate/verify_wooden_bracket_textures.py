#!/usr/bin/env python3
"""
Read-only diagnostic for the per-wood bracket textures.

For every output PNG we report:
  - source (Create) average colour across opaque pixels
  - output average colour across opaque pixels
  - brightness-of-output relative to source (delta per channel)
  - brightest opaque output pixel — the contract is "this should equal the
    wood median colour for that species", because the pipeline is:
        desaturate → scale so brightest B/W pixel becomes #FFFFFF
                  → multiply by wood colour.
    The brightest output pixel therefore has to land on the median wood
    colour. Anything else means the scale step is broken.

A verdict per row:
  OK       brightest pixel matches wood median within 6/channel
  DIM      brightest pixel < 95 % of wood median on every channel
  BRIGHTER wood median - brightest > 6/channel (we somehow overshot the
           median — fine, but flagged)
  MISSING  output PNG not present
"""
from pathlib import Path

from PIL import Image

from _common import WOODS, REPO

CREATE_TEX = REPO / "code_references" / "Create" / "src" / "main" / "resources" / "assets" / "create" / "textures" / "block"
TFC_PLANKS = REPO / "code_references" / "TerraFirmaCraft" / "src" / "main" / "resources" / "assets" / "tfc" / "textures" / "block" / "wood" / "planks"
OUT_DIR = REPO / "src" / "generated" / "resources" / "assets" / "tfc_aeronautics" / "textures" / "block" / "wood" / "bracket"

OK_THRESHOLD = 6           # max channel delta still counts as "matches wood colour"
DIM_FACTOR = 0.95          # brightest pixel below this fraction of wood median → DIM


def avg_rgb(path: Path) -> tuple[float, float, float]:
    """Mean R/G/B across opaque pixels that aren't the intentional black spot.

    Both source and output textures have a single opaque-pure-black pixel
    (geometric hole at the centre of the bracket). Pulling it into the
    average drags every wood species below its real hue, so the diagnostic
    mislabels everything as DIM. Skip it explicitly.
    """
    img = Image.open(path).convert("RGBA")
    rs: list[int] = []
    gs: list[int] = []
    bs: list[int] = []
    for r, g, b, a in img.getdata():
        if a == 0:
            continue
        if r + g + b < 10:        # opaque-near-black = the spot; skip it
            continue
        rs.append(r)
        gs.append(g)
        bs.append(b)
    if not rs:
        return (0.0, 0.0, 0.0)
    return (sum(rs) / len(rs), sum(gs) / len(gs), sum(bs) / len(bs))


def brightest(path: Path) -> tuple[int, int, int]:
    img = Image.open(path).convert("RGBA")
    pixels = [p for p in img.getdata() if p[3] > 0]
    if not pixels:
        return (0, 0, 0)
    return max(pixels, key=lambda p: p[0] + p[1] + p[2])[:3]


def wood_median_rgb(plank_path: Path) -> tuple[int, int, int]:
    img = Image.open(plank_path).convert("RGBA")
    pixels = [p for p in img.getdata() if p[3] > 0]
    rs = sorted(p[0] for p in pixels)
    gs = sorted(p[1] for p in pixels)
    bs = sorted(p[2] for p in pixels)
    mid = len(rs) // 2
    return rs[mid], gs[mid], bs[mid]


def verify_one(prefix: str, src_path: Path, wood: str) -> tuple[str, list[tuple[float, float, float]]]:
    out_path = OUT_DIR / f"{prefix}_{wood}.png"
    plank = TFC_PLANKS / f"{wood}.png"
    if not out_path.exists():
        return ("MISSING", [(0.0, 0.0, 0.0)])
    if not plank.exists():
        return ("MISSING plank", [(0.0, 0.0, 0.0)])

    src_avg = avg_rgb(src_path)
    out_avg = avg_rgb(out_path)
    out_max = brightest(out_path)
    wood_rgb = wood_median_rgb(plank)

    diff_max = [out_max[i] - wood_rgb[i] for i in range(3)]
    if max(abs(d) for d in diff_max) <= OK_THRESHOLD:
        verdict = "OK"
    elif all(0 <= out_max[i] <= DIM_FACTOR * wood_rgb[i] + OK_THRESHOLD for i in range(3)) and wood_rgb[i] > 0:
        verdict = "DIM"
    elif all(diff_max[i] >= OK_THRESHOLD for i in range(3)):
        verdict = "BRIGHTER"
    elif all(diff_max[i] <= -OK_THRESHOLD for i in range(3)):
        verdict = "DARKER"
    else:
        verdict = "DRIFT"

    return verdict, [(src_avg, out_avg, out_max, wood_rgb, diff_max)]


def fmt(rgb) -> str:
    return f"({rgb[0]:6.2f},{rgb[1]:6.2f},{rgb[2]:6.2f})"


def main() -> None:
    sources = {
        "bracket": CREATE_TEX / "bracket_wooden.png",
        "bracket_plate": CREATE_TEX / "bracket_plate_wooden.png",
    }
    missing = [str(p) for p in sources.values() if not p.exists()]
    if missing:
        raise SystemExit(f"Missing Create reference textures: {missing}")

    counts: dict[str, int] = {"OK": 0, "DIM": 0, "BRIGHTER": 0, "DARKER": 0, "DRIFT": 0, "MISSING": 0}
    for prefix, src_path in sources.items():
        for wood in WOODS:
            verdict, rows = verify_one(prefix, src_path, wood)
            counts[verdict] = counts.get(verdict, 0) + 1
            if verdict == "MISSING" or verdict == "MISSING plank":
                print(f"{wood}/{prefix:14s}  {verdict}")
                continue
            src_avg, out_avg, out_max, wood_rgb, diff_max = rows[0]
            print(
                f"{wood}/{prefix:14s}  "
                f"src_avg={fmt(src_avg)}  "
                f"out_avg={fmt(out_avg)}  "
                f"out_max={tuple(out_max)}  "
                f"wood_rgb={tuple(wood_rgb)}  "
                f"diff_max=({diff_max[0]:+d},{diff_max[1]:+d},{diff_max[2]:+d})  "
                f"{verdict}"
            )

    total = sum(counts.values())
    print()
    print(f"OK={counts['OK']}/{total}   DIM={counts['DIM']}   BRIGHTER={counts['BRIGHTER']}   "
          f"DARKER={counts['DARKER']}   DRIFT={counts['DRIFT']}   MISSING={counts['MISSING']}")
    if counts["DIM"] or counts["DRIFT"] or counts["MISSING"]:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
