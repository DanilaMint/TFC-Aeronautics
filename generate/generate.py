#!/usr/bin/env python3
"""
Orchestrator for all Python datagen scripts.

Usage:
  python3 generate/generate.py                    # generate everything
  python3 generate/generate.py --only brackets   # assets + recipes + textures
  python3 generate/generate.py --only milling    # create:milling recipes
  python3 generate/generate.py --only quern      # quern milling (grain → flour)
  python3 generate/generate.py --only brackets-assets,brackets-textures  # multiple groups

Each sub-script is independently runnable. The orchestrator just runs selected
sub-scripts in sequence and prints a summary.
"""
import argparse
import sys
import time

from generate_bucket_textures import main as gen_buckets
from generate_milling_recipes import main as gen_milling
from generate_mixing_recipes import main as gen_mixing
from generate_quern_food_recipes import main as gen_quern
from generate_wooden_bracket_assets import main as gen_assets
from generate_wooden_bracket_recipes import main as gen_recipes
from generate_wooden_bracket_textures import main as gen_textures

# Aggregate groups expand into leaf entries when explicitly requested; --only
# means "select only what I named", so a user typing --only=brackets-assets
# gets the assets-only run. --only=all runs the leaf groups (re-running the
# aggregate would double-execute the bracket scripts).
LEAVES: list[tuple[str, callable]] = [
    ("brackets-assets",   gen_assets),
    ("brackets-recipes",  gen_recipes),
    ("brackets-textures", gen_textures),
    ("buckets",           gen_buckets),
    ("milling",           gen_milling),
    ("mixing",            gen_mixing),
    ("quern",             gen_quern),
]

GROUPS: dict[str, list[tuple[str, callable]]] = {
    "brackets": [
        ("brackets-assets",   gen_assets),
        ("brackets-recipes",  gen_recipes),
        ("brackets-textures", gen_textures),
    ],
    "brackets-assets":   [LEAVES[0]],
    "brackets-recipes": [LEAVES[1]],
    "brackets-textures": [LEAVES[2]],
    "buckets":           [LEAVES[3]],
    "milling":           [LEAVES[4]],
    "mixing":            [LEAVES[5]],
    "quern":             [LEAVES[6]],
}


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    ap.add_argument(
        "--only",
        default="all",
        help="group key or comma-separated group keys; default 'all' (runs every leaf once)",
    )
    args = ap.parse_args()

    if args.only == "all":
        selected: list[tuple[str, callable]] = list(LEAVES)
    else:
        selected = []
        for raw in args.only.split(","):
            key = raw.strip()
            if key not in GROUPS:
                print(f"unknown group: {key!r}", file=sys.stderr)
                return 2
            selected.extend(GROUPS[key])

    summary: list[tuple[str, int, float]] = []
    for label, fn in selected:
        t0 = time.time()
        print(f"==> {label}")
        n = fn()
        summary.append((label, n, time.time() - t0))

    print("\n--- summary ---")
    for label, n, dt in summary:
        print(f"  {label:24s}  {n:>4} files  {dt:5.2f}s")
    total = sum(n for _, n, _ in summary)
    print(f"  {'TOTAL':24s}  {total:>4} files")
    return 0


if __name__ == "__main__":
    sys.exit(main())
