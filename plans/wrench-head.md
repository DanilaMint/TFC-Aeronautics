# Wrench Head

**Прогресс:** 5/8 ⏳ (texturing остаётся placeholder ☐, `.bbmodel` отложен)

Заменяет `create:wrench` (gold plate + cogwheel + wood rod) на forging-путь: латунная головка выковывается на наковальне tier 2, затем совмещается с палкой.

## Регистрация
- [x] item `tfc_aeronautics:metal/wrench_head/brass`
- [x] `wrench/WrenchHeadRegistration.java` (по образцу `SawBladeRegistration`, `DeferredHolder<Item, Item>`) — также `WrenchHeadRegistration.register(modEventBus)` подключён в `TFCAeronautics.java`

## Текстурирование
- [ ] `assets/tfc_aeronautics/textures/item/metal/wrench_head/brass.png` (16×16, brass palette ≈ `#7C5E33`) — **PLACEHOLDER только** (силуэт с outline/highlight). Настоящая текстура — отдельная задача.
- [ ] `blockbench/wrench_head.bbmodel` (исходник с embedded texture) — отложено до прихода настоящего арта

## Моделирование
- [x] `assets/tfc_aeronautics/models/item/metal/wrench_head/brass.json` (`parent: item/generated`, `layer0: tfc_aeronautics:item/metal/wrench_head/brass`)

## Рецепты
- [x] TFC anvil tier 2: `data/tfc_aeronautics/recipe/anvil/wrench_head_brass.json` (`c:ingots/brass` → head, hit_last/second/third, `apply_bonus: true`)
- [x] Heating: `data/tfc_aeronautics/recipe/heating/wrench_head_brass.json` (100 mB → `tfc:metal/brass` @ 930°)
- [x] Item heat: `data/tfc_aeronautics/tfc/item_heat/wrench_head_brass.json` (forging 558°, welding 744°, heat_capacity 4.5)
- [x] Create wrench override: `data/create/recipe/crafting/kinetics/wrench.json` (паттерн `["H","S"]`: head + `c:rods/wooden` → `create:wrench`, `show_notification: false`)

## Локализация
- [x] en_us: `item.tfc_aeronautics.metal.wrench_head.brass` → "Brass Wrench Head"
