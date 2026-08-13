# Heater

**Прогресс:** 22/26 ⏳

## Регистрация
- [x] block `tfc_aeronautics:heater` — `heater/HeaterBlock.java` (`Block implements IBE<HeaterBlockEntity>, IWrenchable`, props `LIT` + `HORIZONTAL_FACING`, light 14, analog signal 15, `mapColor STONE, strength 2.0/6.0, sound STONE, pushReaction BLOCK`)
- [x] item `tfc_aeronautics:heater_item`
- [x] `heater/HeaterBlockEntity.java` — `SmartBlockEntity implements IBellowsConsumer`, `ItemStackHandler(2)` (`SLOT_ITEM` limit 1 gated by `HeatCapability.has`, `SLOT_FUEL` limit 64), `FluidTank(2000 mB)`, `HotAwareItemHandler`, `HeaterValueBehaviour` (ScrollValueBehaviour, INTERVAL=50, range 0..`Heat.maxVisibleTemperature()`, step 50°C)
- [x] `heater/HeaterRegistration.java`
- [x] capabilities `heater/HeaterCapabilities.java` — ItemHandler на всех гранях, FluidHandler только `Direction.DOWN`
- [x] value box `heater/HeaterValueBoxTransform.java` (`ValueBoxTransform.Sided`, `isSideActive` = `HORIZONTAL_FACING`)
- [x] blockstate `assets/tfc_aeronautics/blockstates/heater.json` (lit/unlit variants)

## Текстурирование
- [x] `assets/tfc_aeronautics/textures/block/heater_off.png`
- [x] `assets/tfc_aeronautics/textures/block/heater_on.png`
- [x] `assets/tfc_aeronautics/textures/block/heater_flame.png`
- [x] `assets/tfc_aeronautics/textures/block/heater_top_off.png`
- [x] `assets/tfc_aeronautics/textures/block/heater_top_on.png`
- [x] `assets/tfc_aeronautics/textures/block/heater_bottom.png`
- [x] `assets/tfc_aeronautics/textures/block/heater_side.png`

## Моделирование
- [x] `assets/tfc_aeronautics/models/block/heater_off.json`
- [x] `assets/tfc_aeronautics/models/block/heater_on.json`
- [x] `assets/tfc_aeronautics/models/item/heater.json`

## Логика

### Сервер
- [x] `heater/HeaterBlock.java` — block states (LIT, HORIZONTAL_FACING), IWrenchable
- [x] `heater/HeaterBlockEntity.java` — SmartBlockEntity, IBellowsConsumer, item/fluid handlers, HeaterValueBehaviour, частицы FLAME+SMOKE каждые 3 тика
- [x] `heater/HeaterCapabilities.java` — IItemHandler все грани, IFluidHandler DOWN
- [x] `heater/HeaterValueBoxTransform.java` — UI transform для value-box
- [ ] Ускорить нагрев в 1.5 раз
- [x] Добавить возможность нагревать жидкостные баки для создания парового двигателя и дистиллятора — реализовано через шину `HeatDealer`, см. `plans/update-heaters.md`

### Клиент
Нет client-only файлов; анимированное пламя не реализовано (см. Текстурирование).

## Звуки
- [ ] Добавить звук при горении как у charcoal forge

## Ponder-сцены
- [ ] Сцена о загрузке и выгрузке топлива и предметов автоматически или вручную
- [ ] Сцена о плавлении предметов в жидкость
