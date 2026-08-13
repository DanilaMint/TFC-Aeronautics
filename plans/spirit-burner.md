# Spirit Burner

**Прогресс:** 0/26 ☐

Жидкостная горелка — блок, который сжигает горючую жидкость из внутреннего бака и отдаёт наружу стабильную температуру в °C по шкале TFC. Заправляется через Create-трубу снизу. Температура задаётся реестром `CombustionRegistry` (жидкость → профиль горения). Регистрируется как `HeatDealer` — это и есть основной смысл: автоматически попадает в `BasinBlockEntityMixin` (нагрев `create:mixing`), `BoilerHeater.REGISTRY` (паровой котёл/steam engine) и будущий condenser-coil без попарных интеграций.

```
   [ Create fluid pipe (жидкость) ]
                │
                ▼  (IFluidHandler на DOWN)
   ┌───────────────────────────┐
   │  SpiritBurnerBlockEntity  │  ← tank.drain(burnRate, EXECUTE)
   │  tank (1000 mB)           │     temperature = profile.temperatureCelsius
   └───────────────────────────┘     HeatCapability.provideHeatTo(pos.above())
                │                       (как у heater, см. plans/update-heaters.md)
                ▼
   ┌───────────────────────────┐
   │  CombustionRegistry       │  Fluid → CombustionProfile
   │  OLIVE_OIL  → 540 °C      │  (температура пламени фитильной лампы/свечи)
   │  CANOLA_OIL → 520 °C      │
   │  TALLOW     → 870 °C      │
   └───────────────────────────┘
```

Запись в шине `HeatDealer.REGISTRY` — единственная точка подключения. Дальше вся механика работает автоматически: костёр и нагреватель — те же 540 °C, что и оливковое масло здесь.

## Регистрация

- [ ] block `tfc_aeronautics:spirit_burner` — `spirit_burner/SpiritBurnerBlock.java` (`Block implements IBE<SpiritBurnerBlockEntity>`, props `LIT`, light 12, analog signal 15, `mapColor STONE, strength 2.0/6.0, sound STONE, pushReaction BLOCK`)
- [ ] item `tfc_aeronautics:spirit_burner_item`
- [ ] `spirit_burner/SpiritBurnerBlock.java`
- [ ] `spirit_burner/SpiritBurnerBlockEntity.java` — `SmartBlockEntity`, `FluidTank(SPIRIT_BURNER_TANK_CAPACITY)`, тик: drain → temperature drift → provideHeatTo → particles
- [ ] `spirit_burner/SpiritBurnerRegistration.java` — `register(IEventBus)` вешает `FMLCommonSetupEvent` через `event.enqueueWork(...)` (холдеры `TFCFluids` на момент конструирования мода ещё не разрешены); вызов из `TFCAeronautics.java` рядом с `HeatDealerRegistration.register(modEventBus)`
- [ ] capabilities (через `getCapability` override или отдельный `spirit_burner/SpiritBurnerCapabilities.java`) — `IFluidHandler` на `Direction.DOWN`, чтобы Create-труба снизу заливала жидкость в бак

## Текстурирование
- [ ] `assets/tfc_aeronautics/textures/block/spirit_burner_off.png`
- [ ] `assets/tfc_aeronautics/textures/block/spirit_burner_on.png`
- [ ] `assets/tfc_aeronautics/textures/block/spirit_burner_top.png`

## Моделирование
- [ ] blockstate `assets/tfc_aeronautics/blockstates/spirit_burner.json` (lit/unlit)
- [ ] block-модель `assets/tfc_aeronautics/models/block/spirit_burner_off.json`
- [ ] block-модель `assets/tfc_aeronautics/models/block/spirit_burner_on.json`
- [ ] item-модель `assets/tfc_aeronautics/models/item/spirit_burner.json`

## Реестр горения

**`heat/CombustionProfile.java`** — record-класс профиля:
```
public record CombustionProfile(
    float temperatureCelsius,
    int burnRateMillibucketsPerTick,
    float engineStressUnits) {}
```

Третье поле `engineStressUnits` используется ДВС (`plans/combustion-engine.md`) для динамической мощности. Spirit Burner его игнорирует; для профилей, которые регистрируются только ради нагрева, его можно оставить `0f`.

**`heat/CombustionRegistry.java`** — `SimpleRegistry<Fluid, CombustionProfile> REGISTRY`, статики `register(Fluid, CombustionProfile)` и `getProfile(Fluid)` → `CombustionProfile | null`. Без провайдеров: аддоны, которым нужно своё топливо, вызывают `register(...)` из своего common setup.

В `SpiritBurnerRegistration.registerHeatDealers()` (внутри `enqueueWork`) регистрируем три масла TFC. Температуры — из реальных данных по пламени фитильной горелки (источник — поиск по candle/olive oil lamp flame temperature):

| Fluid | `temperatureCelsius` | `burnRateMillibucketsPerTick` | `engineStressUnits` | Обоснование |
|---|---|---|---|---|
| `tfc:olive_oil` | 540 | 1 | 128 | пламя оливковой масляной лампы ~1000 °F ≈ 540 °C; стартовое значение для ДВС |
| `tfc:canola_oil` | 520 | 1 | 112 | рафинированное рапсовое — чуть ниже olive oil, разница ~20 °C; чуть меньше SU |
| `tfc:tallow` | 870 | 1 | 512 | пламя сальной свечи 1400-1600 °F ≈ 760-870 °C; тяжёлое топливо → x4 SU |

`OLIVE_OIL_WATER` и `CANOLA_OIL_WATER` сознательно **не регистрируем**: водная смесь в фитильной горелке плохо горит, и подключать «полу-водяной» профиль ради двух сомнительных жидкостей не стоит — `SpiritBurnerBlockEntity` для незарегистрированной жидкости просто останется негорящим (`getProfile()` → null).

Скорость 1 mB/тик = 20 mB/с, полный бак 1000 mB сгорает за 50 с. Это ориентир; если окажется слишком быстро/медленно в игре, поменять `burnRateMillibucketsPerTick` в одной строке регистрации.

## Логика

### Сервер

- [ ] `SpiritBurnerBlockEntity` — константы `SPIRIT_BURNER_TANK_CAPACITY = 1000`, `NO_TEMPERATURE = 0f`. Поля `temperature`, `wasLit` для синхронизации `LIT` block-state
- [ ] Серверный тик:
    1. Прочитать текущую жидкость из бака
    2. Если бак пуст или `CombustionRegistry.getProfile(fluid) == null`: `temperature = 0`, `LIT = false`, конец тика
    3. Иначе: `profile = CombustionRegistry.getProfile(fluid)`; `tank.drain(profile.burnRateMillibucketsPerTick, FluidAction.EXECUTE)`; `temperature = profile.temperatureCelsius` (стабильная, без drift — топливо уже горит)
    4. `HeatCapability.provideHeatTo(level, worldPosition.above(), Direction.DOWN, temperature)` — ровно тот же шаг, что у heater (`plans/heater.md`); после этого любой блок сверху с `BlockCapabilities.HEAT` греется
    5. Синхронизация `LIT` block-state при смене
    6. Частицы FLAME+SMOKE каждые 3 тика, пока `LIT` (как у heater)
- [ ] IFluidHandler на `Direction.DOWN` — `tank.fill(FluidStack, EXECUTE)` для входящего потока, без ограничений по типу жидкости (фильтрация — на стороне реестра горения: что не зарегистрировано, то не горит, но залить можно)

### Клиент
Нет client-only файлов. Визуал пламени делается за счёт двух вариантов текстуры/модели (lit/unlit) + частиц на сервере.

## Подключение к шине `HeatDealer`

- [ ] `heat/HeatDealers.java` — добавить `public static final HeatDealer SPIRIT_BURNER = fromBlockEntity(SpiritBurnerBlockEntity.class, SpiritBurnerBlockEntity::getTemperature);` (тот же generic-хелпер, что для `HEATER`)
- [ ] `heat/HeatDealerRegistration.java` — `HeatDealer.REGISTRY.register(SpiritBurnerRegistration.SPIRIT_BURNER.get(), HeatDealers.SPIRIT_BURNER);` Сразу после этого блок автоматически попадает в `BasinBlockEntityMixin` (`create:mixing` при наличии нагревателя снизу), в `BoilerHeater.REGISTRY` (steam engine / паровой котёл), и в будущий `condenser-coil` через ту же шину
- [ ] `plans/update-heaters.md:37` — закрыть чек-бокс про `spirit_burner` (запись в `HeatDealer.REGISTRY`); сам механизм сгорания живёт в этом плане, не в `update-heaters.md`

## Документация
- [ ] `ROADMAP.md` — добавить строку индекса `- [Spirit Burner](plans/spirit-burner.md) — 0/26 ☐` и пересчитать сводку «Итого»
- [ ] `DOCS.md` — раздел «Spirit Burner»: что это, какие жидкости горят и при какой температуре, как подключить свою жидкость (через `CombustionRegistry.register(fluid, profile)` из common setup аддона)
- [ ] `plans/update-heaters.md` — закрыть чек-бокс про `spirit_burner` после регистрации в `HeatDealer.REGISTRY`

## Верификация
- [ ] `./gradlew compileJava` — сборка проходит
- [ ] В творческом мире: Create-труба с `tfc:olive_oil` → burner наполняется; зажигается (LIT=true), выдаёт 540 °C; бак опустошается за 50 с; после — гаснет
- [ ] Spirit Burner под басином → рецепт `create:mixing`, требующий нагрева, запускается; рецепты с порогом ≥ ~540 °C (`FADING` и выше по `toHeatLevel`) — да; ниже — нет
- [ ] Spirit Burner под `tfc:firepit` (или наоборот) — регрессионный тест: миксин басина не ломается
- [ ] Паровой котёл Create (fluid tank + steam engine) над Spirit Burner с `tfc:tallow` (870 °C → 7 SU) → steam engine выдаёт полную мощность
- [ ] Залить `tfc:olive_oil_water` (не зарегистрировано) — горелка не зажигается, бак заполняется, но `LIT = false` и температуры нет
- [ ] Залить обычную воду — то же самое: бак заполняется, горения нет