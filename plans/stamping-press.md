# Stamping Press

**Прогресс:** 25/27 ⏳

## Регистрация
- [x] block `tfc_aeronautics:stamping_press` — `stamping_press/StampingPressBlock.java` (`HorizontalKineticBlock`, shape `MECHANICAL_PROCESSOR_SHAPE` + `CASING_14PX` для игроков, `canSurvive` блокирует `BasinBlock`, stress 8.0 SU)
- [x] item `tfc_aeronautics:stamping_press_item`
- [x] `stamping_press/StampingPressBlockEntity.java` — `KineticBlockEntity implements PressingBehaviourSpecifics`, `PressingBehaviour` + `FilteringBehaviour.forRecipes()` с `StampingPressFilterSlot`, `MAX_TIER=Integer.MAX_VALUE`, `AnvilRecipe.getAll()` lookup, heat-gated, `assemble()` копирует температуру
- [x] filter slot `stamping_press/StampingPressFilterSlot.java`
- [x] instances `stamping_press/StampingPressInstances.java` — `WeakHashMap` client cache (используется клиентом)
- [x] `stamping_press/StampingPressRegistration.java`
- [x] blockstate `assets/tfc_aeronautics/blockstates/stamping_press.json`

## Текстурирование
- [x] 10 текстур в одном атласе: `assets/tfc_aeronautics/textures/stamping_press/atlas.png` (64×64, плитки по сетке 16)
  + `assets/minecraft/atlases/blocks.json` — source `minecraft:unstitch` режет атлас на 10 спрайтов
  `tfc_aeronautics:block/stamping_press_*` (те же id, что в моделях — модели не менялись)

## Моделирование
- [x] block `assets/tfc_aeronautics/models/block/stamping_press.json`
- [x] partial `assets/tfc_aeronautics/models/block/stamping_press_head.json`
- [x] gui `assets/tfc_aeronautics/models/block/stamping_press_gui.json`
- [x] item `assets/tfc_aeronautics/models/item/stamping_press.json` (parent = `stamping_press_gui`)

## Логика

### Сервер
- [x] `stamping_press/StampingPressBlock.java` — HorizontalKineticBlock, stress 8.0 SU, canSurvive логика
- [x] `stamping_press/StampingPressBlockEntity.java` — KineticBlockEntity + PressingBehaviour + FilteringBehaviour
- [x] `stamping_press/StampingPressFilterSlot.java` — слот фильтра на задней грани
- [x] `stamping_press/StampingPressInstances.java` — клиентский кэш инстансов

### Клиент
- [x] `client/.../stamping_press/StampingPressClientRegistration.java` — `SimpleBlockEntityVisualizer.builder().neverSkipVanillaRender()` + `StampingPressRenderer`
- [x] `client/.../stamping_press/StampingPressRenderer.java` — `KineticBlockEntityRenderer` + `FilteringRenderer.renderOnBlockEntity` + `CachedBuffers.partialFacing` с `renderedHeadOffset`
- [x] `client/.../stamping_press/StampingPressVisual.java` — `SingleAxisRotatingVisual` + `SimpleDynamicVisual`, `OrientedInstance` pressHead
- [x] `client/.../stamping_press/StampingPressPartialModels.java` — `STAMPING_PRESS_HEAD = block/stamping_press_head`
- [x] `client/.../stamping_press/StampingPressFrameTickHandler.java` — `ClientTickEvent.Post` → Outliner API, outline на `facing.getClockWise()/getCounterClockWise()`, `lineWidth 1/64f`, `highlightFace`

## Рецепты
- [x] crafting: `data/tfc_aeronautics/recipe/crafting/kinetics/stamping_press.json` (shaft + brass_casing + `tfc:metal/hammer_head/wrought_iron`)
- [x] Create mechanical_press override: `data/create/recipe/crafting/kinetics/mechanical_press.json` (vanilla path заменён)

## Визуал и звуки
- [x] Anvil strike-звук удара
- [ ] Добавить частицы-искры при ударе

## Ponder-сцены
- [ ] Сцена о использовании
