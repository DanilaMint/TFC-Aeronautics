# Ponder

**Прогресс:** 7/9 ⏳

## Регистрация
- [x] `client/.../ponder/PonderRegistration.java` — `PonderIndex.addPlugin`
- [x] `client/.../ponder/PonderPlugin.java` — `getModId`, `registerScenes`, `registerTags`, `registerSharedText("hot_air_burn")`
- [x] `client/.../ponder/PonderScenes.java` — storyboard для heater и stamping_press
- [x] `client/.../ponder/PonderTags.java` — `KINETICS = tfc_aeronautics:kinetics`, title "Kinetics", description "Components built around Create's kinetic system"

## Логика

### Клиент
- [x] `client/.../ponder/scenes/HeaterScenes.java` — intro scene, basePlate 5×5, heater at `grid(2,1,2)`, 5×10×80 idle timing
- [x] `client/.../ponder/scenes/StampingPressScenes.java` — pressing scene, basePlate 5×5, press at `grid(2,1,2)`
- [x] `client/.../ponder/scenes/TemplateScenes.java` — shared storyboard helpers

## Данные (NBT-схематики)
- [ ] `assets/tfc_aeronautics/ponder/heater/*.nbt` — папка создана, файлы отсутствуют
- [ ] `assets/tfc_aeronautics/ponder/stamping_press/*.nbt` — отсутствуют
