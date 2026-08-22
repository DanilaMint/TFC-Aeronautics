"""Shared constants and helpers for the Python datagen scripts."""
from __future__ import annotations

import json
from pathlib import Path

REPO = Path(__file__).resolve().parent.parent
GEN = REPO / "src" / "generated" / "resources"

WOODS = [
    "acacia", "ash", "aspen", "birch", "blackwood", "chestnut",
    "douglas_fir", "hickory", "kapok", "mangrove", "maple", "oak",
    "palm", "pine", "rosewood", "sequoia", "spruce", "sycamore",
    "white_cedar", "willow",
]


def write_json(rel_path: str | Path, data: dict) -> Path:
    """Write JSON to GEN/rel_path with mkdir -p and trailing newline."""
    p = GEN / rel_path
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n")
    return p
