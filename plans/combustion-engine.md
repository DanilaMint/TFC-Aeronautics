# Combustion Engine (ДВС)

**Прогресс:** 0/22 ☐

Двигатель внутреннего сгорания — кинетический генератор, аналог `simulated:portable_engine`, но работает на горючих жидкостях из реестра `CombustionRegistry` (общий с `spirit_burner`). Заправляется через Create-трубу снизу. RPM всегда 32, SU зависит от залитой жидкости.

```
   [ Create fluid pipe (жидкость) ]
                │
                ▼  (IFluidHandler на DOWN)
   ┌──────────────────────────────────┐
   │  CombustionEngineBlockEntity     │  ← tank.drain(burnRate, EXECUTE)
   │  tank (2000 mB)                  │     getGeneratedSpeed() = 32 RPM
   │  GeneratingKineticBlockEntity    │     calculateAddedStressCapacity() = profile.engineStressUnits
   └──────────────────────────────────┘
                │
   ┌────────────┴────────────┐
   ▼                         ▼
  exhaust                kinetic output
  (дым из                 (в сторону facing,
   противоположной           capacity = profile.engineStressUnits)
   грани)
                │
   ┌────────────┴────────────────────────────┐
   │  CombustionRegistry                     │
   │  OLIVE_OIL  → 540 °C, 1 mB/tick, 128 SU  │
   │  CANOLA_OIL → 520 °C, 1 mB/tick, 112 SU  │
   │  TALLOW     → 870 °C, 1 mB/tick, 512 SU  │
   └──────────────────────────────────────────┘
```

ДВС **не регистрируется в `HeatDealer.REGISTRY`** — тепло сгорания теряется (как в реальном ДВС с радиатором). Это сознательное упрощение: иначе ставишь ДВС под котёл — и у тебя Steam Engine без воды.

## Сравнение с соседями

| Источник | Тип | RPM | SU | Топливо | Тепло |
|---|---|---|---|---|---|
| Steam Engine | kinetic | 64 | 1024 | вода в котле (через `BoilerHeater`) | через `HeatCapability` на баке |
| **ДВС** | **kinetic** | **32** | **128 / 112 / 512** | **жидкость из `CombustionRegistry`** | **нет** |
| Spirit Burner | heat | — | — | жидкость из `CombustionRegistry` | через `HeatCapability` вверх |

ДВС вдвое медленнее Steam Engine по RPM, но компактнее (без котла и воды), а за счёт тяжёлого топлива (tallow) выдаёт половину мощности Steam Engine — экономически оправдано для средних механизмов.

## Регистрация

- [ ] block `tfc_aeronautics:combustion_engine` — `combustion_engine/CombustionEngineBlock.java` (`Block implements IBE<CombustionEngineBlockEntity>`, props `LIT` + `HORIZONTAL_FACING`, light 12, analog signal 15, `mapColor STONE, strength 2.0/6.0, sound STONE, pushReaction BLOCK`)
- [ ] item `tfc_aeronautics:combustion_engine_item`
- [ ] `combustion_engine/CombustionEngineBlock.java`
- [ ] `combustion_engine/CombustionEngineBlockEntity.java` — `GeneratingKineticBlockEntity`, `FluidTank(COMBUSTION_ENGINE_TANK_CAPACITY)`, тик: drain → getGeneratedSpeed() → calculateAddedStressCapacity() → particles
- [ ] `combustion_engine/CombustionEngineRegistration.java` — `register(IEventBus)` вешает `FMLCommonSetupEvent` через `event.enqueueWork(...)`; вызов из `TFCAeronautics.java` рядом с `SpiritBurnerRegistration.register(modEventBus)`
- [ ] capabilities — `IFluidHandler` на `Direction.DOWN` (тот же подход, что у Spirit Burner)
- [ ] blockstate `assets/tfc_aeronautics/blockstates/combustion_engine.json` (lit/unlit × 4 facing)

## Текстурирование
- [ ] `assets/tfc_aeronautics/textures/block/combustion_engine_off.png`
- [ ] `assets/tfc_aeronautics/textures/block/combustion_engine_on.png`

## Моделирование
- [ ] block-модель `assets/tfc_aeronautics/models/block/combustion_engine_off.json`
- [ ] block-модель `assets/tfc_aeronautics/models/block/combustion_engine_on.json`
- [ ] item-модель `assets/tfc_aeronautics/models/item/combustion_engine.json`

## Расширение `CombustionProfile`

Текущий record в `heat/CombustionProfile.java`:
```java
public record CombustionProfile(float temperatureCelsius, int burnRateMillibucketsPerTick) {}
```

Добавить третье поле — `engineStressUnits`. Новое:
```java
public record CombustionProfile(
    float temperatureCelsius,
    int burnRateMillibucketsPerTick,
    float engineStressUnits) {}
```

`engineStressUnits` — пиковая мощность ДВС на этом топливе (при 32 RPM). Для негорючих жидкостей, которые Spirit Burner тоже не использует, поле просто остаётся незаполненным; Spirit Burner его игнорирует.

Три масла TFC обновляются в `SpiritBurnerRegistration.registerCombustionProfiles()`:

| Fluid | `temperatureCelsius` | `burnRateMillibucketsPerTick` | `engineStressUnits` |
|---|---|---|---|
| `tfc:olive_oil` | 540 | 1 | **128** |
| `tfc:canola_oil` | 520 | 1 | **112** |
| `tfc:tallow` | 870 | 1 | **512** |

Числа — стартовый баланс: tallow даёт x4 к olive oil (тяжёлое топливо реально мощнее), canola чуть хуже olive (хуже горит). Steam Engine = 1024 SU — потолок. ДВС на tallow выдаёт ровно половину Steam Engine на той же нагрузке.

## Логика

### Сервер

- [ ] `CombustionEngineBlockEntity` — константы `COMBUSTION_ENGINE_TANK_CAPACITY = 2000`, `GENERATED_RPM = 32`. Поля `wasLit`, `currentProfile` для синхронизации `LIT` block-state и текущего профиля
- [ ] Серверный тик:
    1. Прочитать жидкость из бака
    2. Если бак пуст или `CombustionRegistry.getProfile(fluid) == null`: `currentProfile = null`, `LIT = false`, конец тика
    3. Иначе: `profile = CombustionRegistry.getProfile(fluid)`; `tank.drain(profile.burnRateMillibucketsPerTick, FluidAction.EXECUTE)`; `currentProfile = profile`; `LIT = true`
    4. `super.tick()` — базовая логика `GeneratingKineticBlockEntity` (расчёт `getGeneratedSpeed()`, раздача по сети)
- [ ] `getGeneratedSpeed()` — `return LIT ? GENERATED_RPM : 0` (с учётом направления через `convertToDirection(...)` и `HORIZONTAL_FACING`, как у `PortableEngineBlockEntity:150`)
- [ ] `calculateAddedStressCapacity()` — `return currentProfile != null ? currentProfile.engineStressUnits : 0`. Это override метода из `KineticBlockEntity.java:185-189`, который по умолчанию читает `BlockStressValues.getCapacity(block)` — здесь обходим реестр, потому что capacity динамическая (зависит от топлива в баке)
- [ ] `BlockStressValues.CAPACITIES` для этого блока **не регистрируем**: capacity полностью динамическая
- [ ] IFluidHandler на `Direction.DOWN` — `tank.fill(FluidStack, EXECUTE)`; ограничение по типу жидкости не нужно (фильтрация — на стороне реестра: что не зарегистрировано, то не горит)
- [ ] Частицы SMOKE из грани, противоположной `HORIZONTAL_FACING`, каждые 3 тика пока `LIT` (аналог exhaust-дыма из `PortableEngineBlockEntity:354-402`)
- [ ] Синхронизация `LIT` block-state при смене

### Звуки
- [ ] Звук выхлопа на каждом «такте» — триггерится в серверном тике, когда `clientAngle % 90` пересекает 0 (как `PortableEngineBlockEntity:174-189`); место подключения — отдельный `SoundEvent` `tfc_aeronautics:combustion_engine_puff` (регистрируется в существующем `DamageTypes.java`-аналоге или новом `combustion_engine/CombustionEngineSounds.java`)
- [ ] Ambient-звук мотора на низкой громкости — опционально, можно отложить

### Клиент
- [ ] `combustion_engine/CombustionEngineRenderer.java` (в `src/client/java/ru/aeronautics/client/combustion_engine/`) — рендер вращающегося маховика по аналогии со `stamping_press`. Поскольку блок наследует `GeneratingKineticBlockEntity`, достаточно подписаться на `ClientTickEvent.Post` и через `Outliner` крутить модель. Если стандартного `BlockEntityRenderer` Create достаточно — обходимся без отдельного рендерера
- [ ] **НЕ** использовать `FilteringRenderer.tick()` для вращения маховика, если рендер выносной — `feedback_create_dual_side_frames.md` помнит, что multi-side value-box frames через `FilteringRenderer` показывают только одну сторону

## Гогглы (информация для очков инженера)

`GeneratingKineticBlockEntity` уже реализует `IHaveGoggleInformation`, и `calculateAddedStressCapacity()` подхватывается автоматически. Достаточно того, что блок наследуется от `GeneratingKineticBlockEntity` и корректно переопределяет `calculateAddedStressCapacity()` + `getGeneratedSpeed()` — гогглы покажут RPM и SU без дополнительного кода.

## Документация
- [ ] `ROADMAP.md` — добавить строку индекса `- [Combustion Engine](plans/combustion-engine.md) — 0/22 ☐` и пересчитать сводку «Итого»
- [ ] `DOCS.md` — раздел «Combustion Engine (ДВС)»: что это, сравнение с Steam Engine, таблица SU по топливу, как подключить свою жидкость (расширить `CombustionProfile` через `CombustionRegistry.register(...)` с указанием `engineStressUnits`)
- [ ] `plans/spirit-burner.md` — синхронизировать: `CombustionProfile` теперь имеет 3 поля, обновить пример таблицы масел (добавить колонку `engineStressUnits`)
- [ ] `plans/update-heaters.md:37` — закрыть чек-бокс про `spirit_burner` после полной реализации (HeatDealer регистрация для Spirit Burner, не ДВС — но упоминание этого плана тут полезно для трассировки)

## Верификация
- [ ] `./gradlew compileJava` — сборка проходит
- [ ] В творческом мире: Create-труба с `tfc:tallow` → ДВС заправляется → заводится (LIT=true) → подключённый shaft вращается; RPM = 32; SU = 512 (проверяется через очки инженера)
- [ ] Переключить подачу на `tfc:olive_oil` → ДВС продолжает работать, RPM тот же, **SU падает до 128**
- [ ] Подача пустой/незарегистрированной жидкости → ДВС глохнет, сеть останавливается
- [ ] Частицы дыма выходят из грани, противоположной `HORIZONTAL_FACING`
- [ ] Кинетическая сеть: ДВС → шестерни → мельница — работает; перегрузка (больше потребителей, чем capacity) — корректное замедление, без вылета
- [ ] Spirit Burner над ДВС — **не греется** (ДВС не в `HeatDealer.REGISTRY`); регрессионный тест: Spirit Burner сам по себе продолжает греть