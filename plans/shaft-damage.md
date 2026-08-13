# Shaft Damage

**Прогресс:** 6/7 ⏳

## Регистрация
- [x] damage type `tfc_aeronautics:shaft` — `DamageTypes.java`
- [x] `data/tfc_aeronautics/damage_type/shaft.json`

## Логика
- [x] `kinetics/ShaftDamageHandler.java` — `@SubscribeEvent EntityTickEvent.Post`; проверка `state.getBlock() instanceof AbstractShaftBlock`; voxel-shape intersection с inflated bbox (`CONTACT_EPSILON=0.05`); формула урона `(rpm - startRpm) / (lethalRpm - startRpm) * lethal * multiplier`; knockback перпендикулярно оси; `AllSoundEvents.CRUSHING_1`; max 1 shaft/тик
- [x] `Config.shaftDamage*` (8 параметров: `shaftDamageEnabled`, `shaftDamageStartRpm`, `shaftDamageLethalRpm`, `shaftDamageLethal`, `shaftDamageMultiplier`, `shaftKnockbackBase`, `shaftKnockbackPerRpm`, `shaftSoundVolume`)
- [x] encased shafts/cogwheels — safe (only bare shafts deal damage)
- [ ] Распространить механику на shafts на движущихся contraptions

## Локализация
- [x] `en_us.json`: `death.attack.tfc_aeronautics.shaft` + `death.attack.tfc_aeronautics.shaft.player`
- [ ] `ru_ru.json`: те же ключи — отсутствуют (см. общий Localization)
