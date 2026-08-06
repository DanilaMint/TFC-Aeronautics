# Atmospheric structure datapack layout

Datapack folder for structures registered through the atmospheric structure framework
(`ru.tfc_aeronautics.worldgen`). Each subfolder follows the vanilla 1.21.1 worldgen
convention; the framework only adds a custom `structure_type` (`tfc_aeronautics:atmospheric`)
and an `atmosphere` block on each structure JSON.

## Subfolders

| Folder | What goes here |
|---|---|
| `structure/` | `*.json` files describing each structure (`type: "tfc_aeronautics:atmospheric"`). Encodes jigsaw fields (start pool, size, heightmap, max depth) plus the optional `atmosphere` object. |
| `structure_set/` | `*.json` files describing how structures are placed into the world (which biomes, spacing, separation). |
| `template_pool/` | `*.json` template-pool definitions used by jigsaw pieces. Each atmospheric structure references one start pool from here. |
| `processor_list/` | `*.json` block-processor lists referenced from template pools (for replacing structure blocks, e.g. vanilla stone → TFC rock variants). |

## Adding an atmospheric structure

1. Register the structure via `AeronauticsStructures.STRUCTURES.register(...)` (or
   via the JSON codec below if you want pure-datapack authoring).
2. Drop the jigsaw `.nbt` template into `structure/<your_structure>/<piece>.nbt`.
3. Reference it from a `template_pool/<your_structure>/start.json`.
4. Add a `structure/<your_structure>.json` describing the structure with
   `"type": "tfc_aeronautics:atmospheric"` and any `atmosphere` overrides.
5. Add a `structure_set/<your_structure>.json` linking it to biomes (use TFC biome
   tags like `#tfc:has_structure/...` for climate-aware placement).

## Climate filtering

The framework does not ship a custom `StructurePlacementType` (TFC's
`ClimateStructurePlacement` requires access to internal `ChunkGeneratorExtension` and
is not reusable from addons). For climate-aware placement, use biome filtering in the
`structure_set` JSON (TFC biomes already encode climate ranges), or extend
`AtmosphereSpec.Resolver` and call `AtmosphereSpec.installResolver(...)` at startup
to provide an external hook for finer-grained checks.