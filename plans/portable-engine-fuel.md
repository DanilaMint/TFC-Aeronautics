# Portable Engine Fuel — TFC Integration

**Прогресс:** 3/3 ✅

## Регистрация

- [x] `portable_engine/PortableEngineFuelHandler.java` — `@EventBusSubscriber(modid = TFCAeronautics.MOD_ID)` (default `Bus.GAME`), `@SubscribeEvent(priority = EventPriority.HIGH)` на `FurnaceFuelBurnTimeEvent`; `Fuel.get(stack)` → `Mth.floor(fuel.duration() * fuel.purity())` → `event.setBurnTime(duration)`. Без `cancellable = true` (setBurnTime сам отменяет на `>= 0`).
- [x] `Config.TFC_FUEL_IN_ENGINES` (default `true`) — escape hatch через `common.toml`.
- [x] `en_us.json`: `tfc_aeronautics.config.tfcFuelInEngines` → "TFC Fuel in Engines".

## Логика

Перехват `FurnaceFuelBurnTimeEvent` на game bus с приоритетом `HIGH`. При вызове:

1. Ранний выход, если `Config.TFC_FUEL_IN_ENGINES.get() == false`.
2. `Fuel.get(stack)` из TFC; если `null` — выход (ванильная логика остаётся).
3. `duration = Mth.floor(fuel.duration() * fuel.purity())`; если `≤ 0` — выход.
4. `event.setBurnTime(duration)` — внутренне отменяет событие, останавливая default-priority листенеры.

Никаких правок в `TFCAeronautics.java` (аннотация `@EventBusSubscriber` самоподключается), без миксинов, без новых JSON-датапаков, без `compileOnly` зависимости на Simulated.

## Ограничения

- Перехват **глобальный**: затрагивает `simulated:portable_engine`, ванильную печь, Create Blaze Burner, Create Trains. Гейтинг по `RecipeType.SMELTING` не используется: он даёт несогласованное разделение (печь + двигатель да, коптильня + домна нет) и не изолирует двигатель от печи.
- `superHeated` для TFC-топлива остаётся `false` — TFC не в `create:superheated_blaze_burner_fuels`, скорость 32 RPM без удвоения.
- `/reload` посреди горения: `Fuel.CACHE` перезагружается через `IndirectHashCollection.reloadAllCaches`; новые значения разрешаются на лету, без локов — соответствует риск-профилю TFC (`FireboxBlockEntity`, `AbstractFirepitBlockEntity`).
- `PortableEngineInventory.canInsertItem` сравнивает burn time через `getDefaultInstance()`. Все 49 TFC fuel JSON используют `item`/`tag` ингредиенты (без component-sensitive types), поэтому `Fuel.get(defaultInstance) ≡ Fuel.get(actualStack)` — протокол вставки и потребления согласованы.

## Численные примеры

| Топливо | TFC duration | Purity | Engine burn time |
|---------|--------------|--------|------------------|
| `minecraft:coal` | 2000 | 1.0 | 2000 |
| `minecraft:charcoal` | 1800 | 1.0 | 1800 (vs vanilla 1600) |
| `tfc:wood/log/oak` | 1000 | 0.95 | 950 |
| `tfc:wood/planks/oak` | 900 | 1.0 | 900 |
| `tfc:peat` | 2500 | 1.0 | 2500 |
| `tfc:ore/lignite` | 2200 | 1.0 | 2200 |
| `minecraft:leaves` | 600 | 0.25 | 150 |
| `tfc:groundcover/pinecone` | 220 | 0.15 | 33 |
| `tfc:groundcover/driftwood` | 400 | 0.40 | 160 |

Purity scaling критичен: без него `minecraft:leaves` = 600 тиков бесплатной энергии, что обходит качественный сигнал TFC.
