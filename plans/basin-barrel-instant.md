# Чаша Create × `tfc:barrel_instant`

**Прогресс:** 23/24 ⏳ (не закрыт только рантайм-чек в Prism-лаунчере)

Задача из `list.md`: «синхронизировать рецепты `tfc:barrel_instant` и mixer».
Реализовано как **пассивная чаша**: `create:basin` сам исполняет `tfc:barrel_instant`
(без Create-миксера).

## Регистрация
- [x] `basin/BasinBarrelInstantCompat.java` — `@EventBusSubscriber(modid = TFCAeronautics.MOD_ID)`,
      слушает `com.simibubi.create.api.event.BlockEntityBehaviourEvent` и вешает
      `BasinBarrelInstantBehaviour` на `AllBlockEntityTypes.BASIN` через `event.attach`.
      Игровая шина (GAME-bus по умолчанию) — на mod-шине событие не услышат.
- [x] `basin/BasinBarrelInstantBehaviour.java` — `extends BlockEntityBehaviour`,
      `TYPE` — **единственный** `static final` `BehaviourType` (карта поведений Create —
      `Reference2ObjectArrayMap` со сравнением ключей по ссылке, `BehaviourType` не
      переопределяет `equals`).

## Логика
- [x] `tick()`: серверный гейт (`level == null || level.isClientSide`), гейт по оператору
      (`BasinOperatingBlockEntity` на 2 блока выше — приоритет у машины), быстрый выход на
      пустом input-инвентаре, «грязный» чек по снимку состояния (9 слотов + бак).
- [x] Поиск совпадения: `getAllRecipesFor(TFCRecipeTypes.BARREL_INSTANT.get())` × слоты.
      Предмет — `getInputItem().ingredient().test(stack)` (**сырой** `Ingredient`, без
      проверки count). Жидкость — `getInputFluid().ingredient().test(fluid)`, рабочая
      жидкость ищется по **обоим** сегментам `inputTank` (у бака `enforceVariety`).
- [x] Фильтр охлаждающих рецептов: `getInputItem().ingredient().getCustomIngredient()
      instanceof HeatIngredient` — это `water_cooling`, `salt_water_cooling`,
      `olive_oil_cooling`, `canola_oil_cooling`.
- [x] Расчёт партий: `batches = min(жидкость / inputFluid.amount(), предметы / inputItem.count())`.
      Без правила `moreItemsThanFluid` — в чаше нет sealed-рецептов, которые с ним
      конкурируют.
- [x] Подгонка под вместимость выхода: перебор `batches` вниз, пока
      `basin.acceptOutputs(items, fluids, true)` не вернёт `true`. Не влезает даже одна
      партия → ждём следующий тик (снимок не обновляется).
- [x] Списание: предметы через `input.setStackInSlot(...)` (**не** `extractItem` —
      `BasinInventory` имеет `stackNonStackables = true`, и `SmartInventory#extractItem`
      зажимает количество до `maxStackSize`), жидкость через
      `basin.inputTank.getCapability().drain(fluid.copyWithAmount(...), EXECUTE)`.
- [x] Выдача результата: `basin.acceptOutputs(items, fluids, false)`; звук —
      `Helpers.playSound(level, pos, recipe.getCompleteSound())`.

## Отличия от TFC-бочки (осознанные)
- [x] `InstantBarrelRecipe#matches(BarrelInventory)` не вызывается — снято правило
      `moreItemsThanFluid` (1 соль на ведро воды срабатывает, а не игнорируется).
- [x] `BarrelRecipe#assembleOutputs(BarrelInventory)` не вызывается — лишняя входная
      жидкость не уничтожается, а остаётся в чаше. Интерфейс `BarrelInventory` не
      реализуется.
- [x] Данные рецепта берутся из TFC-объекта `InstantBarrelRecipe` (`getInputItem()`,
      `getInputFluid()`, `getOutputItem()`, `getOutputFluid()`, `getCompleteSound()`),
      поэтому модификаторы результата (`tfc:empty_bowl` и т.п.) работают как в TFC.

## Рецепты
- [x] Новых JSON не требуется — используются 20 ванильных `tfc:barrel_instant` +
      `tfc_aeronautics/barrel/dry_composite.json` (25 mB `tfc:limewater` +
      `tfc_aeronautics:dry_composite` → `tfc_aeronautics:composite`).
- [x] Не поддерживается: `tfc:barrel_instant_fluid` (жидкость + жидкость) и
      4 охлаждающих рецепта с ингредиентом `tfc:heat`.

## Известные ограничения
- [x] Спут Create в чашу не наливает (`create:basin` не зарегистрирован в
      `AllBlockSpoutingBehaviours`) — жидкость только ведром/трубой/воронкой.
- [x] Предмет ПКМ в чашу не вставляется (Create 6.0.11 не умеет) — только падение
      `ItemEntity`, воронка/хоппер/лента, рука Create.
- [x] Забрать предмет пустой рукой нельзя — баг порта Create 6.0.11 (`BasinBlock` не
      переопределяет `useWithoutItem`). Результат достаётся воронкой/лентой.
- [x] Чаша вмещает максимум 1000 mB одной жидкости (`enforceVariety`).
- [x] При `FACING != DOWN` результат уходит в `spoutputBuffer` (в ленту под чашей) —
      стандартное поведение Create.

## Документация
- [x] `DOCS.md` — раздел «39. Чаша Create выполняет `tfc:barrel_instant` рецепты»
      (подключение, алгоритм, таблица отличий от бочки, список рецептов, ограничения).
- [x] `list.md` — пункт «синхронизировать рецепты tfc:barrel_instant и mixer» отмечен `[x]`.

## Проверка
- [x] `./gradlew compileJava` — OK.
- [x] `./gradlew build` — OK.
- [ ] Рантайм-проверка в Prism-лаунчере (игру запускать нельзя):
      вода + `tfc:powder/salt` → `tfc:salt_water`; `tfc:hardened_clay` + 100 mB воды →
      4 `minecraft:clay_ball` и ~900 mB воды в чаше; `tfc:dirt/alfisol` + 250 mB воды →
      `tfc:mud/alfisol`; `tfc_aeronautics:dry_composite` + 25 mB `tfc:limewater` →
      `tfc_aeronautics:composite`; горячий предмет + вода не остывает; миксер над чашей
      блокирует переработку.
