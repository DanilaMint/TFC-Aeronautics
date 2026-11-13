#!/usr/bin/env python3
"""
Generate TFC `item_size` data entries so Create/TFC items get a stack size and
an inventory footprint.

The item -> (size, weight) table lives in `item_sizes_map.py`; this script only
expands it into one JSON per item, mirroring TFC's own
`data/<ns>/tfc/item_size/<item_ns>/<item_id>.json` layout:

  {
    "ingredient": { "item": "create:cogwheel" },
    "size": "small",
    "weight": "light"
  }

`generate.py` calls `main()` and uses the return value as the file count for
its summary, so it must stay an int.
"""
from item_sizes_map import GROUPS
from _common import write_json


def main() -> int:
    count = 0

    for group in GROUPS:
        for id in group.ids:
            path = f"data/tfc_aeronautics/tfc/item_size/{group.namespace}/{id}.json"
            full_id = f"{group.namespace}:{id}"

            write_json(path, {
                "ingredient": {
                    "item": full_id
                },
                "size": group.size,
                "weight": group.weight
            })
            count += 1

        print(f"  {group.namespace}: {len(group.ids)} item(s), "
              f"size={group.size}, weight={group.weight}")

    print(f"wrote {count} item_size entries under "
          f"data/tfc_aeronautics/tfc/item_size/")
    return count


if __name__ == "__main__":
    raise SystemExit(main())