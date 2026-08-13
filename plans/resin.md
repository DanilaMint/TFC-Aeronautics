# Resin

**Прогресс:** 8/8 ✓

## Регистрация
- [x] item `tfc_aeronautics:resin_clump`
- [x] `resin/ResinRegistration.java`
- [x] tag `tfc_aeronautics:can_collect_resin` (5 conifers: douglas_fir, pine, sequoia, spruce, white_cedar)

## Текстурирование
- [x] `assets/tfc_aeronautics/textures/item/resin_clump.png`

## Моделирование
- [x] `assets/tfc_aeronautics/models/item/resin_clump.json`

## Логика
- [x] `resin/ResinStripHandler.java` — `BlockEvent.BlockToolModificationEvent` на `AXE_STRIP` по блокам в `#tfc_aeronautics:can_collect_resin` → drops resin_clump с шансом `Config.resinDropChance`
- [x] tag `tfc_aeronautics:strong_alcohol` (vodka, rum, whiskey, rye_whiskey, corn_whiskey) — для barrel-рецепта rosin

## Рецепты
- [x] barrel sealed: `data/tfc_aeronautics/recipe/barrel/rosin.json` (50 mB `#tfc_aeronautics:strong_alcohol` + 1 resin_clump → 50 mB rosin, 1000 ticks) — см. также Rosin
