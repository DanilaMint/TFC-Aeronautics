# Нагревательные элементы (Heat Dealers)

**Прогресс:** 19/28 ⏳

Нагревательный элемент — блок, который отдаёт наружу свою текущую температуру в °C по шкале TFC (0…1600, `Heat.maxVisibleTemperature()`). Это общая шина между устройствами TFC и механиками Create: на неё опираются паровой двигатель, паровой вентиль, нагрев в рецептах `create:mixing` и дистиллятор.

Ключевая идея — один реестр вместо N попарных интеграций. Каждый нагревательный элемент регистрируется один раз, и все потребители, существующие и будущие, получают его бесплатно.

```
   [ tfc:firepit (+ stove/grill/pot) ]  [ tfc_aeronautics:heater ]  [ spirit_burner (позже) ]
                    \                            |                        /
                     ----------> HeatDealer.REGISTRY (Block -> HeatDealer) <---------
                                        │  getTemperature(level, pos, state) -> °C
                     ┌──────────────────┼──────────────────────┐
                     ▼                  ▼                      ▼
          BoilerHeater.REGISTRY   BasinBlockEntity        (дистиллятор,
          (паровой котёл,          #getHeatLevel           condenser-coil —
           steam engine)           через mixin →           будущие планы)
                                   create:mixing
```

Реестр — не свой велосипед: используется публичный `com.simibubi.create.api.registry.SimpleRegistry` (тот же, на котором построен `BoilerHeater.REGISTRY` в Create), он потокобезопасен и поддерживает провайдеры по тегам.

## Регистрация
- [x] `heat/HeatDealer.java` — `@FunctionalInterface`, `float getTemperature(Level, BlockPos, BlockState)`, константа `NO_HEAT = -1f`, реестр `SimpleRegistry<Block, HeatDealer> REGISTRY`, статика `findTemperature(Level, BlockPos[, BlockState])` и `isHeatDealer(BlockState)` (быстрая проверка без чтения block entity — нужна миксину на HEAD)
- [x] `heat/HeatDealers.java` — встроенные реализации + конвертации шкал; приватный generic `fromBlockEntity(Class<T>, ToDoubleFunction<T>)`, чтобы три реализации не дублировали код
- [x] `heat/HeatDealerRegistration.java` — `register(IEventBus)` вешает слушатель `FMLCommonSetupEvent` и регистрирует всё через `event.enqueueWork(...)` (холдеры `TFCBlocks` на момент конструирования мода ещё не разрешены)
- [x] вызов `HeatDealerRegistration.register(modEventBus)` в `TFCAeronautics.java`
- [x] `mixin/BasinBlockEntityMixin.java` — инъекция в `BasinBlockEntity#getHeatLevel`
- [x] `"BasinBlockEntityMixin"` в `src/main/resources/tfc_aeronautics.mixins.json`

## Нагревательные элементы
- [x] `tfc:firepit` → `HeatDealers.FIREPIT` (читает `AbstractFirepitBlockEntity#getTemperature`)
- [x] `tfc:stove`, `tfc:stove_pot`, `tfc:grill`, `tfc:pot` → `HeatDealers.FIREPIT` — все четыре наследуют `FirepitBlock` и делят `AbstractFirepitBlockEntity`, так что горят и греют так же; исключать их было бы багом
- [x] `tfc:charcoal_forge` — **сознательно не регистрируется:** кузня тушится, если над ней стоит блок (тег `#tfc:charcoal_forge_invisible`), так что басин или котёл сверху её просто погасит
- [x] `tfc_aeronautics:heater` → `HeatDealers.HEATER` (`HeaterBlockEntity#getTemperature`)
- [ ] `tfc_aeronautics:spirit_burner` — блок, потребляющий горючие жидкости для генерации тепла; см. будущий `plans/spirit-burner.md`. Подключение сводится к одной строке `HeatDealer.REGISTRY.register(...)`

## Логика

### Сервер
- [x] Критерий «не греет» — температура `<= 0`, а не block-state property `LIT`/`HEAT`: остывающее, но ещё горячее устройство обязано продолжать отдавать тепло, иначе рецепты обрываются в момент прогорания топлива
- [x] `HeatDealers.toHeatLevel(float celsius)` — °C → `BlazeBurnerBlock.HeatLevel` для басина: `< 80 → NONE`, `< 400 → SMOULDERING`, `< 800 → FADING`, `< 1400 → KINDLED`, иначе `SEETHING`. Пороги завязаны на шкалу `Heat` из TFC (WARMING начинается с 80 °C) и повторяют градацию `BoilerHeaters#blazeBurner`
- [x] `HeatDealers.toBoilerHeat(float celsius)` + `HeatDealers.boilerAdapter(Level, BlockPos, BlockState)` — °C → шкала `BoilerHeater`: `< 80 → NO_HEAT`; далее линейно по 200 °C: 80…279 → 0, 280…479 → 1, …, 1480…1600 → 7. Сигнатура адаптера совпадает с `BoilerHeater#getHeat`, поэтому передаётся как method reference. Линейная разбивка вместо 4 широких полос — иначе 200 °C и 1399 °C дают одинаковый SU, и нагрев костра не виден в выработке парового двигателя
- [x] `heater/HeaterBlockEntity.java` — шаг 5 в `tick()`: `HeatCapability.provideHeatTo(level, worldPosition.above(), Direction.DOWN, temperature)`. Ровно так делают `AbstractFirepitBlockEntity:110` и `CharcoalForgeBlockEntity:121`. После этого любой блок сверху, выставляющий TFC-capability `BlockCapabilities.HEAT` (`IHeatConsumer`), греется от нагревателя без специального кода
- [x] Миксин на басин не отменяет оригинал, если под басином не зарегистрированный `HeatDealer` — ванильная логика Create (blaze burner, `#create:passive_boiler_heaters`: лава, магма, костры) продолжает работать. `cachedHeatLevel` намеренно не заполняется: температура костра меняется каждый тик, кэшировать её нельзя, а выход на HEAD этот кэш обходит

### Клиент
Client-only файлов нет. Уровень нагрева виден по стандартному рендеру басина Create — своего визуала шина не требует.

## Потребители
- [x] `create:mixing` — басин над нагревательным элементом (через `BasinBlockEntityMixin`)
- [x] Паровой котёл Create / steam engine — через провайдер в `BoilerHeater.REGISTRY`, миксин не нужен: реестр публичный API Create. Любой зарегистрированный `HeatDealer` подключается автоматически
- [ ] Паровой вентиль
- [ ] Дистиллятор — рецепты `tfc_aeronautics:distillation`; см. `plans/condenser-coil.md`
- [ ] `tfc_aeronautics:condenser_coil` — конденсация пара из нагреваемого бака; см. `plans/condenser-coil.md`

## Документация
- [x] `DOCS.md` — раздел «Нагревательные элементы (Heat Dealers)»
- [x] `ROADMAP.md` — строка индекса
- [ ] Ponder-сцена, показывающая басин над костром и котёл над нагревателем

## Верификация
- [x] `./gradlew compileJava` — сборка проходит
- [ ] Басин над зажжённым `tfc:firepit` → рецепт `create:mixing`, требующий нагрева, запускается; над потухшим и остывшим — нет
- [ ] **Регрессия миксина:** басин над лавой, магмой и blaze burner по-прежнему работает
- [ ] Паровой котёл Create (fluid tank + steam engine) над `tfc_aeronautics:heater` с топливом → котёл выдаёт мощность
- [ ] TFC-устройство, принимающее тепло, поставленное над нагревателем, нагревается
