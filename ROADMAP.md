# Roadmap for TFC Aeronautics mod

## Metal Powder

| Task | Status | Dependencies |
|-|-|-|
| Add textures for metal powders | Complete | - |
| Register items | Complete | - |
| Add TFC Heating to items | Complete | Register |
| Add TFC Heating recipe: powder -> molten metal | Complete | Register |

## Refactor Andesite Alloy

| Task | Status | Dependencies |
|-|-|-|
| Replace texture to TFC style | Complete | - |
| Change item name to "Andesite alloy ingot" | Complete | - |
| Register molten metal | Complete | - |
| Add texture to molten metal | Complete | - |
| Add TFC Heating to ingot and fluid | Complete | Register metal |
| Register double ingot of andesite alloy | Complete | - |
| Add texture to double ingot | Complete | - |
| Add TFC Welding recipe: ingot + ingot -> double ingot | Complete | double ingot |
| Add TFC Heating to double ingot | Complete | double ingot |
| Add TFC Heating recipe: double ingot -> molten metal 200 MB | Complete | double ingot |

## Refactor shaft recipes

| Task | Status | Dependencies |
|-|-|-|
| Remove old recipe: 2 andesite alloy -> shaft | Complete | - |
| Add forging recipe: andesite alloy -> 4 shafts | Complete | - |

## Refactor casings

| Task | Status | Dependencies |
|-|-|-|
| Add create item application recipe: TFC stripped logs + andesite alloy ingot -> Andesite casing | Complete | - |
| Add create item application recipe: TFC stripped logs + brass ingot -> Brass casing | Complete | - |
| Add create item application recipe: TFC stripped logs + copper ingot -> Copper casing | Complete | - |

## Override Create recipes using dried kelp

Create uses vanilla `minecraft:dried_kelp` / `minecraft:dried_kelp_block` in 8 recipes. Vanilla dried kelp doesn't exist in TFC's food chain, so all of these need to be shadowed at their original paths under `data/create/recipe/...` per [[feedback-recipe-override-convention]].

| Task | Status | Dependencies |
|-|-|-|
| Shadow `data/create/recipe/crafting/kinetics/belt_connector.json` (replace `minecraft:dried_kelp`) | Complete | - |
| Shadow `data/create/recipe/crafting/kinetics/spout.json` (replace `minecraft:dried_kelp`) | | - |
| Shadow `data/create/recipe/crafting/logistics/andesite_funnel.json` (replace `minecraft:dried_kelp`) | | - |
| Shadow `data/create/recipe/crafting/logistics/andesite_tunnel.json` (replace `minecraft:dried_kelp`) | | - |
| Shadow `data/create/recipe/crafting/logistics/brass_funnel.json` (replace `minecraft:dried_kelp`) | | - |
| Shadow `data/create/recipe/crafting/logistics/brass_tunnel.json` (replace `minecraft:dried_kelp`) | | - |
| Shadow `data/create/recipe/crafting/kinetics/hose_pulley.json` (replace `minecraft:dried_kelp_block`) | | - |
| Shadow `data/create/recipe/crafting/kinetics/elevator_pulley.json` (replace `minecraft:dried_kelp_block`) | | - |

## Stamping Press

| Task | Status | Dependencies |
|-|-|-|
| Add `tfc_aeronautics:stamping_press` block + item + block-entity | Complete | - |
| Reuse Create press model + textures; animate head via `AllPartialModels.MECHANICAL_PRESS_HEAD` | Complete | - |
| Implement TFC anvil recipe lookup on strike (input + filter item, heat-gated) | Complete | - |
| Filter UI on back face via `FilteringBehaviour` + `StampingPressFilterSlot` | Complete | - |
| Register 8.0 SU stress impact (matches Create press) | Complete | - |
| Reject basin below (canSurvive) and skip basin processing (tryProcessInBasin=false) | Complete | - |
| Fix the stamping press model (block renders wrong in world/inventory) | | - |
| Make the shaft actually render inside the block + the striking head actually animate | | renderer |
| Move filter slot to a perpendicular face (currently it overlaps the energy input face) | Complete | - |
| Add running squeak/creak sound when the shaft turns | | - |
| Add anvil strike sound when the head hits the item | | - |
| Replace Create's press model + textures with a TFC-flavoured custom model and texture | | - |

## Heater

| Task | Status | Dependencies |
|-|-|-|
| Add `tfc_aeronautics:heater` block + item + block-entity | Complete | - |
| IItemHandler capability on all faces (chute/funnel/hopper/arm) + IFluidHandler on DOWN face | Complete | - |
| TFC integration: Fuel burn + Bellows boost + Encased Fan air + HeatingRecipe → molten tank | Complete | - |
| LIT block-state property + light emission 14 when burning | Complete | - |
| Max-temperature knob via Create `ValueSettingsBehaviour` (0..MAX_TEMP, 50 °C steps) | Complete | - |
| Two-state block model via blockstate variants (lit/unlit) | Complete | - |
| Animated flame overlay rendered by `HeaterBlockEntityRenderer` (Y-bob + scale flicker) | Complete | - |
| Make heater textures | | - |
|   &nbsp;&nbsp;↳ `src/main/resources/assets/tfc_aeronautics/textures/block/heater_side.png` | | - |
|   &nbsp;&nbsp;↳ `src/main/resources/assets/tfc_aeronautics/textures/block/heater_top_off.png` (rest) | | - |
|   &nbsp;&nbsp;↳ `src/main/resources/assets/tfc_aeronautics/textures/block/heater_top_on.png` (burning) | | - |
|   &nbsp;&nbsp;↳ `src/main/resources/assets/tfc_aeronautics/textures/block/heater_bottom.png` | | - |
|   &nbsp;&nbsp;↳ `src/main/resources/assets/tfc_aeronautics/textures/block/heater_flame.png` (animated overlay) | | - |
| Fix heater texture | | - |

> Models already reference these textures; the files just don't exist on disk yet.
> Models: `models/block/heater_off.json`, `models/block/heater_on.json`, `models/block/heater/flame.json`,
> `blockstates/heater.json`, `models/item/heater.json`.

## Create Spout + TFC Casting

| Task | Status | Dependencies |
|-|-|-|
| Register `BlockSpoutingBehaviour` against `tfc:mold_table` so a Create spout placed above a mold table executes the matching `tfc:casting` recipe (drains `recipe.getFluidIngredient().amount()` mB from the spout, places result in mold table's `OUTPUT_SLOT`) | Complete | - |
| Guard against double-casting: skip when mold stack is empty, mold already contains fluid, or `OUTPUT_SLOT` is occupied | Complete | - |

Implementation: `src/main/java/ru/tfc_aeronautics/recipe/SpoutCastingBehavior.java` + `SpoutCompat.java`. Pattern mirrors Create's own `com.simibubi.create.compat.tconstruct.SpoutCasting`.

## Tight sheet

| Task | Status | Dependencies |
|-|-|-|
| Register `tfc_aeronautics:metal/tight_sheet` for copper, wrought iron and steel | Complete | - |
| Draw textures for them | | - |
| Register TFC Heating recipe: tight sheet -> 100 Mb metal | Complete | - |
| Register Create pressing recipe: ingot -> tight sheet | Complete | - |
| Register TFC Forging recipe: ingot -> tight sheet | Complete | - |

## Shaft contact damage

| Task | Status | Dependencies |
|-|-|-|
| Register `tfc_aeronautics:shaft` damage type + death message | Complete | - |
| Hurt living entities touching bare shafts/cogwheels, scaled by RPM (64 → 160 RPM) | Complete | damage type |
| Leave andesite/brass encased shafts and cogwheels safe | Complete | - |
| Knock the entity perpendicular to the rotation axis + crunch sound | Complete | - |
| Expose start RPM, lethal RPM, lethal damage and a damage multiplier in the config | Complete | - |
| Extend the mechanic to shafts on moving contraptions | | - |

## Refactor Create mechanical press

| Task | Status | Dependencies |
|-|-|-|
| Delete old recipe | | - |
| Register new recipe (same, but iron block replace to wrougth iron double ingot) | | - |

## PR

| Task | Status | Dependencies |
|-|-|-|
| Make logo for the mod | | - |
| Make the trailer | | - |
| Publish to CurseForge | | - |
| Publish to Modrinth | | - |

## Translation

| Task | Status | Dependencies |
|-|-|-|
| Make English US `en_us.lang` | | - |
| Make Russian `ru_ru.lang` | | - |
| Make Spanish `es_es.lang`, `es_mx.lang` | | - |
| Make German `de_de.lang` | | - |
| Make French `fr_fr.lang` | | - |
| Make Chinese `zh_cn.lang`, `zh_tw.lang` | | - |
| Make Japanese `ja_jp.lang` | | - |
| Make Korean `ko_kr.lang` | | - |
