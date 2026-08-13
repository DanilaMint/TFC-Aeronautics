# Wrench Head

**Прогресс:** 0/7 ☐

Заменяет `create:wrench` (gold plate + cogwheel + wood rod) на forging-путь: латунная головка выковывается на наковальне tier 2, затем совмещается с палкой.

## Регистрация
- [ ] item `tfc_aeronautics:metal/wrench_head/brass`
- [ ] `wrench/WrenchHeadRegistration.java` (по образцу `SawBladeRegistration`, `DeferredHolder<Item, Item>`)

## Текстурирование
- [ ] `assets/tfc_aeronautics/textures/item/metal/wrench_head/brass.png` (16×16, brass palette ≈ `#7C5E33`)
- [ ] `blockbench/wrench_head.bbmodel` (исходник с embedded texture)

## Моделирование
- [ ] `assets/tfc_aeronautics/models/item/metal/wrench_head/brass.json` (`parent: item/generated`, `layer0: tfc_aeronautics:item/metal/wrench_head/brass`)

## Рецепты
- [ ] TFC anvil tier 2: `data/tfc_aeronautics/recipe/anvil/wrench_head_brass.json` (`c:ingots/brass` → head, hit_last/second/third, `apply_bonus: true`)
- [ ] Create wrench override: `data/create/recipe/crafting/kinetics/wrench.json` (паттерн `["H","S"]`: head + stick → `create:wrench`, `show_notification: false`)
