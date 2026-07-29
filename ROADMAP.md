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
| Add create item application recipe: TFC stripped logs + andesite alloy ingot -> Andesite casing | | - |
| Add create item application recipe: TFC stripped logs + brass ingot -> Brass casing | | - |
| Add create item application recipe: TFC stripped logs + copper ingot -> Copper casing | | - |

## Stamping Press

| Task | Status | Dependencies |
|-|-|-|
| Add `tfc_aeronautics:stamping_press` block + item + block-entity | Complete | - |
| Reuse Create press model + textures; animate head via `AllPartialModels.MECHANICAL_PRESS_HEAD` | Complete | - |
| Implement TFC anvil recipe lookup on strike (input + filter item, heat-gated) | Complete | - |
| Filter UI on back face via `FilteringBehaviour` + `StampingPressFilterSlot` | Complete | - |
| Register 8.0 SU stress impact (matches Create press) | Complete | - |
| Reject basin below (canSurvive) and skip basin processing (tryProcessInBasin=false) | Complete | - |
| Make the shaft actually render inside the block + the striking head actually animate | | renderer |
| Move filter slot to a perpendicular face (currently it overlaps the energy input face) | | - |
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

> Models already reference these textures; the files just don't exist on disk yet.
> Models: `models/block/heater_off.json`, `models/block/heater_on.json`, `models/block/heater/flame.json`,
> `blockstates/heater.json`, `models/item/heater.json`.

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
