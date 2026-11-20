#!/usr/bin/env python3
"""
Datagen for the Kok-Saghyz crop (Taraxacum kok-saghyz).

Every number that describes the crop lives in the CONSTANTS block below, and
every generated file - the climate range, the blockstates and models, the loot
tables, the worldgen features, the tags and the Patchouli page - is derived
from those constants. That keeps the Patchouli page honest: it cannot drift
away from the climate range JSON, because both are printed from the same
values.

The numbers themselves are not invented: they are derived from real agronomic
data for Taraxacum kok-saghyz, fitted against how TFC encodes its own crops.
The full derivation lives in `docs/kok_sagyz_crop.md`; the short version:

  temperature  -15 .. 26 C   TFC encodes a root crop's range as roughly
                              (frost tolerance + 3..5) .. (optimum + 4..5).
                              Kok-Saghyz is a sharply continental steppe
                              perennial, frost hardiness to -25 C, optimum
                              growth 15-20 C, growth stalls above ~26 C.
  hydration     20 .. 75     Deep taproot makes it drought tolerant, but
                              standing water kills it, so the upper bound sits
                              below what a thirsty crop would get.
  N / P / K     0.5 / 0.3 / 0.4
                              TFC's nutrient values are balance tiers rather
                              than agronomic quantities (a linear fit against
                              real crop removal scores R^2 ~= 0.02 for N and
                              ~= 0.15 for P). Real removal for kok-saghyz is
                              N ~100, P ~15, K ~50-75 kg/ha, which places it
                              above every TFC root crop in N and below all of
                              them in K.

Output (under src/generated/resources/):
  data/tfc_aeronautics/tfc/climate_range/crop/kok_sagyz.json
  assets/tfc_aeronautics/blockstates/{crop,dead_crop,wild_crop}/kok_sagyz.json
  assets/tfc_aeronautics/models/block/{crop,dead_crop,wild_crop}/kok_sagyz*.json
  assets/tfc_aeronautics/models/item/{seeds/kok_sagyz,kok_sagyz_roots}.json
  assets/tfc_aeronautics/models/item/wild_crop/kok_sagyz.json
  data/tfc_aeronautics/loot_table/blocks/{crop,dead_crop,wild_crop}/kok_sagyz.json
  data/tfc_aeronautics/worldgen/{configured,placed}_feature/plant/wild_crop/kok_sagyz*.json
  data/tfc/tags/worldgen/placed_feature/feature/crops.json        (additive)
  data/tfc/tags/item/{wild_crops,compost_greens/low}.json         (additive)
  data/minecraft/tags/block/compostables.json                     (additive)
  assets/tfc/patchouli_books/field_guide/{en_us,ru_ru}/entries/food/kok_sagyz.json

The textures themselves are placeholders and live in src/main/resources, so they
are tracked and can be repainted by hand - this script does not touch them.
"""
from __future__ import annotations

from _common import write_json

# ---------------------------------------------------------------------------
# Constants - the single source of truth for the crop
# ---------------------------------------------------------------------------

CROP = "kok_sagyz"
MOD = "tfc_aeronautics"

#: Number of growth stages (block states). Age runs 0 .. STAGES - 1.
STAGES = 6

#: Nutrients consumed per growth cycle, in TFC's units (1.0 == a full
#: farmland nutrient bar). Negative would mean the crop feeds the soil.
NITROGEN = 0.5
PHOSPHOROUS = 0.3
POTASSIUM = 0.4

#: Growable climate window.
MIN_TEMPERATURE = -15.0
MAX_TEMPERATURE = 26.0
MIN_HYDRATION = 20
MAX_HYDRATION = 75

#: Where the wild plant may appear. Narrower than the crop's own range on
#: purpose: kok-saghyz settles on stony, well drained slopes rather than in
#: wet lowlands.
WILD_MIN_TEMPERATURE = -12.0
WILD_MAX_TEMPERATURE = 26.0
WILD_MIN_GROUNDWATER = 60
WILD_MAX_GROUNDWATER = 320

#: Yield of roots from a fully grown plant.
ROOT_YIELD_MIN = 2
ROOT_YIELD_MAX = 5

# ---------------------------------------------------------------------------
# Registry ids
# ---------------------------------------------------------------------------

CROP_BLOCK = f"{MOD}:crop/{CROP}"
DEAD_CROP_BLOCK = f"{MOD}:dead_crop/{CROP}"
WILD_CROP_BLOCK = f"{MOD}:wild_crop/{CROP}"
SEEDS_ITEM = f"{MOD}:seeds/{CROP}"
ROOTS_ITEM = f"{MOD}:kok_sagyz_roots"

SEEDS_TEXTURE = f"{MOD}:textures/item/kok_sagyz_seeds.png"


def _fmt_temp(value: float) -> str:
    """Format a temperature the way TFC's own field guide does (%s%C)."""
    return f"{value:.1f}°C"


# ---------------------------------------------------------------------------
# Climate range
# ---------------------------------------------------------------------------

def write_climate_range() -> int:
    # Key order matches TFC's own climate_range JSONs.
    write_json(f"data/{MOD}/tfc/climate_range/crop/{CROP}.json", {
        "max_hydration": MAX_HYDRATION,
        "max_temperature": MAX_TEMPERATURE,
        "min_hydration": MIN_HYDRATION,
        "min_temperature": MIN_TEMPERATURE,
    })
    return 1


# ---------------------------------------------------------------------------
# Blockstates and models
# ---------------------------------------------------------------------------

def write_blockstates() -> int:
    count = 0

    write_json(f"assets/{MOD}/blockstates/crop/{CROP}.json", {
        "variants": {
            f"age={age}": {"model": f"{MOD}:block/crop/{CROP}_age_{age}"}
            for age in range(STAGES)
        }
    })
    count += 1

    # A dead crop is drawn either as the withered mature plant or as the
    # withered seedling, depending on whether it died fully grown.
    write_json(f"assets/{MOD}/blockstates/dead_crop/{CROP}.json", {
        "variants": {
            "mature=true": {"model": f"{MOD}:block/dead_crop/{CROP}"},
            "mature=false": {"model": f"{MOD}:block/dead_crop/{CROP}_young"},
        }
    })
    count += 1

    # An immature wild plant is drawn with the young dead model, exactly like
    # TFC does for its own wild crops.
    write_json(f"assets/{MOD}/blockstates/wild_crop/{CROP}.json", {
        "variants": {
            "mature=true": {"model": f"{MOD}:block/wild_crop/{CROP}"},
            "mature=false": {"model": f"{MOD}:block/dead_crop/{CROP}_young"},
        }
    })
    count += 1

    return count


def write_models() -> int:
    count = 0

    for age in range(STAGES):
        write_json(f"assets/{MOD}/models/block/crop/{CROP}_age_{age}.json", {
            "parent": "block/crop",
            "textures": {"crop": f"{MOD}:block/crop/{CROP}_{age}"},
        })
        count += 1

    write_json(f"assets/{MOD}/models/block/dead_crop/{CROP}.json", {
        "parent": "block/crop",
        "textures": {"crop": f"{MOD}:block/crop/{CROP}_dead"},
    })
    count += 1

    write_json(f"assets/{MOD}/models/block/dead_crop/{CROP}_young.json", {
        "parent": "block/crop",
        "textures": {"crop": f"{MOD}:block/crop/{CROP}_dead_young"},
    })
    count += 1

    write_json(f"assets/{MOD}/models/block/wild_crop/{CROP}.json", {
        "parent": "tfc:block/wild_crop/crop",
        "textures": {"crop": f"{MOD}:block/crop/{CROP}_wild"},
    })
    count += 1

    write_json(f"assets/{MOD}/models/item/seeds/{CROP}.json", {
        "parent": "item/generated",
        "textures": {"layer0": f"{MOD}:item/kok_sagyz_seeds"},
    })
    count += 1

    write_json(f"assets/{MOD}/models/item/kok_sagyz_roots.json", {
        "parent": "item/generated",
        "textures": {"layer0": f"{MOD}:item/kok_sagyz_roots"},
    })
    count += 1

    write_json(f"assets/{MOD}/models/item/wild_crop/{CROP}.json", {
        "parent": f"{MOD}:block/wild_crop/{CROP}",
    })
    count += 1

    return count


# ---------------------------------------------------------------------------
# Loot tables
# ---------------------------------------------------------------------------

def write_loot_tables() -> int:
    count = 0

    # Growing crop: roots when fully grown, seeds always.
    write_json(f"data/{MOD}/loot_table/blocks/crop/{CROP}.json", {
        "type": "minecraft:block",
        "pools": [
            {
                "name": "loot_pool",
                "rolls": 1,
                "entries": [
                    {
                        "type": "minecraft:item",
                        "name": ROOTS_ITEM,
                        "conditions": [
                            {
                                "condition": "minecraft:block_state_property",
                                "block": CROP_BLOCK,
                                "properties": {"age": str(STAGES - 1)},
                            }
                        ],
                        "functions": [
                            {
                                "function": "minecraft:set_count",
                                "count": {
                                    "type": "tfc:crop_yield_uniform",
                                    "min": 0,
                                    "max": {
                                        "type": "minecraft:uniform",
                                        "min": ROOT_YIELD_MIN,
                                        "max": ROOT_YIELD_MAX,
                                    },
                                },
                            }
                        ],
                    }
                ],
                "conditions": [{"condition": "minecraft:survives_explosion"}],
            },
            {
                "name": "loot_pool",
                "rolls": 1,
                "entries": [{"type": "minecraft:item", "name": SEEDS_ITEM}],
                "conditions": [{"condition": "minecraft:survives_explosion"}],
            },
        ],
    })
    count += 1

    # Dead crop: a mature plant went to seed and gives extra seeds.
    write_json(f"data/{MOD}/loot_table/blocks/dead_crop/{CROP}.json", {
        "type": "minecraft:block",
        "pools": [
            {
                "name": "loot_pool",
                "rolls": 1,
                "entries": [
                    {
                        "type": "minecraft:alternatives",
                        "children": [
                            {
                                "type": "minecraft:item",
                                "name": SEEDS_ITEM,
                                "conditions": [
                                    {
                                        "condition": "minecraft:block_state_property",
                                        "block": DEAD_CROP_BLOCK,
                                        "properties": {"mature": "true"},
                                    }
                                ],
                                "functions": [
                                    {
                                        "function": "minecraft:set_count",
                                        "count": {
                                            "min": 1,
                                            "max": 3,
                                            "type": "minecraft:uniform",
                                        },
                                    }
                                ],
                            },
                            {
                                "type": "minecraft:item",
                                "name": SEEDS_ITEM,
                                "conditions": [
                                    {
                                        "condition": "minecraft:block_state_property",
                                        "block": DEAD_CROP_BLOCK,
                                        "properties": {"mature": "false"},
                                    }
                                ],
                            },
                        ],
                    }
                ],
                "conditions": [{"condition": "minecraft:survives_explosion"}],
            },
        ],
    })
    count += 1

    # Wild plant: roots when mature, seeds always.
    write_json(f"data/{MOD}/loot_table/blocks/wild_crop/{CROP}.json", {
        "type": "minecraft:block",
        "pools": [
            {
                "name": "loot_pool",
                "rolls": 1,
                "entries": [
                    {
                        "type": "minecraft:item",
                        "name": ROOTS_ITEM,
                        "conditions": [
                            {
                                "condition": "minecraft:block_state_property",
                                "block": WILD_CROP_BLOCK,
                                "properties": {"mature": "true"},
                            }
                        ],
                        "functions": [
                            {
                                "function": "minecraft:set_count",
                                "count": {"min": 1, "max": 3, "type": "minecraft:uniform"},
                            }
                        ],
                    }
                ],
                "conditions": [{"condition": "minecraft:survives_explosion"}],
            },
            {
                "name": "loot_pool",
                "rolls": 1,
                "entries": [
                    {
                        "type": "minecraft:item",
                        "name": SEEDS_ITEM,
                        "functions": [
                            {
                                "function": "minecraft:set_count",
                                "count": {"type": "minecraft:uniform", "min": 2, "max": 3},
                            }
                        ],
                    }
                ],
                "conditions": [{"condition": "minecraft:survives_explosion"}],
            },
        ],
    })
    count += 1

    return count


# ---------------------------------------------------------------------------
# Worldgen
# ---------------------------------------------------------------------------

def write_worldgen() -> int:
    count = 0
    base = f"data/{MOD}/worldgen"

    write_json(f"{base}/configured_feature/plant/wild_crop/{CROP}.json", {
        "type": "minecraft:simple_block",
        "config": {
            "to_place": {
                "type": "minecraft:simple_state_provider",
                "state": {"Name": WILD_CROP_BLOCK},
            }
        },
    })
    count += 1

    write_json(f"{base}/configured_feature/plant/wild_crop/{CROP}_patch.json", {
        "type": "minecraft:random_patch",
        "config": {
            "tries": 6,
            "xz_spread": 5,
            "y_spread": 1,
            "feature": f"{MOD}:plant/wild_crop/{CROP}",
        },
    })
    count += 1

    write_json(f"{base}/placed_feature/plant/wild_crop/{CROP}.json", {
        "feature": f"{MOD}:plant/wild_crop/{CROP}",
        "placement": [
            {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"},
            {"type": "block_predicate_filter", "predicate": {"type": "tfc:replaceable"}},
            {
                "type": "block_predicate_filter",
                "predicate": {
                    "type": "would_survive",
                    "state": {"Name": WILD_CROP_BLOCK},
                },
            },
        ],
    })
    count += 1

    write_json(f"{base}/placed_feature/plant/wild_crop/{CROP}_patch.json", {
        "feature": f"{MOD}:plant/wild_crop/{CROP}_patch",
        "placement": [
            {"type": "minecraft:rarity_filter", "chance": 90},
            {"type": "minecraft:in_square"},
            {
                "type": "tfc:climate",
                "min_temperature": WILD_MIN_TEMPERATURE,
                "max_temperature": WILD_MAX_TEMPERATURE,
                "min_groundwater": WILD_MIN_GROUNDWATER,
                "max_groundwater": WILD_MAX_GROUNDWATER,
            },
        ],
    })
    count += 1

    return count


# ---------------------------------------------------------------------------
# Tags - all additive, so TFC's own entries stay intact
# ---------------------------------------------------------------------------

def write_tags() -> int:
    count = 0

    # TFC's land_plants biome decoration already includes #tfc:feature/crops,
    # so adding the placed feature here is enough to make it spawn.
    write_json("data/tfc/tags/worldgen/placed_feature/feature/crops.json", {
        "replace": False,
        "values": [f"{MOD}:plant/wild_crop/{CROP}_patch"],
    })
    count += 1

    write_json("data/tfc/tags/item/wild_crops.json", {
        "replace": False,
        "values": [f"{MOD}:wild_crop/{CROP}"],
    })
    count += 1

    write_json("data/tfc/tags/item/compost_greens/low.json", {
        "replace": False,
        "values": [ROOTS_ITEM],
    })
    count += 1

    write_json("data/minecraft/tags/block/compostables.json", {
        "replace": False,
        "values": [ROOTS_ITEM],
    })
    count += 1

    return count


# ---------------------------------------------------------------------------
# Patchouli - a page in TFC's own field guide
# ---------------------------------------------------------------------------

def _multimultiblock_pages() -> list[dict]:
    """The growth stages, drawn as TFC draws the crops in its guide."""
    return [
        {
            "type": "tfc:multimultiblock",
            "text": "",
            "multiblocks": [
                {
                    "pattern": [["X"], ["Y"], ["0"]],
                    "mapping": {
                        "X": f"{CROP_BLOCK}[age={age}]",
                        "Y": "tfc:farmland/entisol",
                    },
                }
                for age in range(STAGES)
            ],
        }
    ]


def write_patchouli() -> int:
    count = 0

    stats_en = (
        "$(bold)$(l:core_mechanics/climate#temperature)Temperature$(): "
        f"{_fmt_temp(MIN_TEMPERATURE)} - {_fmt_temp(MAX_TEMPERATURE)}$(br)"
        "$(bold)$(l:food/hydration)Hydration$(): "
        f"{MIN_HYDRATION} - {MAX_HYDRATION} %$(br)"
        "$(bold)Category$(): Vegetable$(br)$(br)"
        f"$(bold)$(3)N: {round(NITROGEN * 100)} "
        f"$(bold)$(6)P: {round(PHOSPHOROUS * 100)} "
        f"$(bold)$(d)K: {round(POTASSIUM * 100)}$()$(br)"
        "Kok-Saghyz is a single block crop. Kok-Saghyz seeds can be planted on "
        "farmland and will produce $(thing)Kok-Saghyz Roots$() and "
        "$(thing)Kok-Saghyz Seeds$() as a product. The roots are not edible - "
        "they are harvested for the milky latex they hold, a source of natural "
        "rubber and inulin."
    )

    flavour_en = (
        "Kok-Saghyz (Taraxacum kok-saghyz) is a perennial dandelion of the stony "
        "foothills of the Tien Shan, where it grows under a sharply continental "
        "sky. It is a hardy plant: it shrugs off hard frosts under the snow, but "
        "stops growing once the summer heat and drought close in.$(br2)"
        "The deep taproot is the crop - uprooting the plant gives up the roots, "
        "which run rich in milky latex. Soak a root in 10 mB of ethanol - in a "
        "sealed barrel, or in a mixer - to draw the latex out of it as a "
        "$(thing)Latex Clump$(). "
        "The seeds need a spell of cold "
        "stratification before they germinate evenly, so sowing in autumn is "
        "the surest way to a stand.$(br2)"
        "Wild Kok-Saghyz grows in cold, dry steppe, on stony ground that drains "
        "quickly."
    )

    stats_ru = (
        "$(bold)$(l:core_mechanics/climate#temperature)Температура$(): "
        f"{_fmt_temp(MIN_TEMPERATURE)} - {_fmt_temp(MAX_TEMPERATURE)}$(br)"
        "$(bold)$(l:food/hydration)Влажность$(): "
        f"{MIN_HYDRATION} - {MAX_HYDRATION} %$(br)"
        "$(bold)Категория$(): Овощная$(br)$(br)"
        f"$(bold)$(3)Азот: {round(NITROGEN * 100)} "
        f"$(bold)$(6)Фосфор: {round(PHOSPHOROUS * 100)} "
        f"$(bold)$(d)Калий: {round(POTASSIUM * 100)}$()$(br)"
        "Кок-сагыз — одноблочная культура. Семена кок-сагыза сажаются на грядку "
        "и дают $(thing)Корни кок-сагыза$() и $(thing)Семена кок-сагыза$(). "
        "Корни не съедобны: их собирают ради белёсого млечного сока, из которого "
        "получают натуральный каучук и инулин."
    )

    flavour_ru = (
        "Кок-сагыз (Taraxacum kok-saghyz) — многолетний одуванчик каменистых "
        "предгорий Тянь-Шаня, растущий под резко континентальным небом. Растение "
        "зимостойкое: оно переносит крепкие морозы под снегом, но прекращает "
        "расти, когда вступают летняя жара и засуха.$(br2)"
        "Урожай — стержневой корень: выкапывая растение, получают корни, "
        "наполненные белёсым млечным соком. Чтобы вытянуть из корня каучук, "
        "замочите корень в 10 мБ этанола - в закрытой бочке или в смесителе - и получите $(thing)Каучук$(). Семенам нужна холодная "
        "стратификация, поэтому надёжнее сеять осенью.$(br2)"
        "Дикий кок-сагыз растёт в холодной сухой степи, на каменистом, быстро "
        "дренируемом грунте."
    )

    for lang, name, stats, flavour in (
        ("en_us", "Kok-Saghyz", stats_en, flavour_en),
        ("ru_ru", "Кок-сагыз", stats_ru, flavour_ru),
    ):
        write_json(
            f"assets/tfc/patchouli_books/field_guide/{lang}/entries/food/{CROP}.json",
            {
                "name": name,
                "category": "tfc:food",
                "icon": SEEDS_TEXTURE,
                "pages": [
                    {"type": "patchouli:text", "title": name, "text": stats},
                    *_multimultiblock_pages(),
                    {"type": "patchouli:text", "title": name, "text": flavour},
                ],
                "read_by_default": True,
                "sortnum": 3,
                # Lets the seed and root items open this page from the guide.
                "extra_recipe_mappings": {
                    SEEDS_ITEM: 0,
                    ROOTS_ITEM: 0,
                },
            },
        )
        count += 1

    return count


# ---------------------------------------------------------------------------

def main() -> int:
    total = 0
    for label, fn in (
        ("climate range", write_climate_range),
        ("blockstates", write_blockstates),
        ("models", write_models),
        ("loot tables", write_loot_tables),
        ("worldgen", write_worldgen),
        ("tags", write_tags),
        ("patchouli", write_patchouli),
    ):
        written = fn()
        total += written
        print(f"  {label}: {written} file(s)")

    print(f"wrote {total} file(s) for the {CROP} crop")
    return total


if __name__ == "__main__":
    raise SystemExit(main())
