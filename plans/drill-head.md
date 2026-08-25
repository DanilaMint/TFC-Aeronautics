# Drill Head

**Прогресс:** 4/4 ✓ (модель = placeholder, реальная 3D отложена)

Заменяет ванильный крафт `create:mechanical_drill` (андезитовый сплав +
iron + casing) на TFC-металлургический путь: сварить на наковальне головку
сверла из двух металлов (cast_iron+wrought_iron или steel+steel), затем
собрать на верстаке с casing и shaft.

Полная цепочка получения `create:mechanical_drill` в TFC-сборке:
1. Сварить `drill_head` на TFC-наковальне (любой из двух welding-рецептов).
2. Заверстать 3-символьный pattern `D/C/S` на верстаке.

## Регистрация
- [x] item `tfc_aeronautics:drill_head`
- [x] `drill_head/DrillHeadRegistration.java` (по образцу `SawBladeRegistration`,
  один `DeferredHolder<Item, Item>`); `DrillHeadRegistration.register(modEventBus)`
  подключён в `TFCAeronautics.java` рядом с saw/wrench
- [x] добавлен в `CreativeTabs.MAIN.displayItems` рядом с saw и wrench

## Текстурирование
- [x] `assets/tfc_aeronautics/textures/item/drill_head.png` — уже лежала
  с прошлой попытки (494 байта, leftover)
- [x] реальный арт — **placeholder 16×16** (см. моделирование)

## Моделирование
- [x] `assets/tfc_aeronautics/models/item/drill_head.json` (`parent: item/generated`,
  `layer0: tfc_aeronautics:item/drill_head`)
- [ ] настоящая 3D-модель — отдельная задача; см. «Моделирование 3D» внизу

## Рецепты
- [x] TFC welding #1 (tier 3, cast_iron-ветка):
  `data/tfc/recipe/welding/drill_head_cast_iron.json` —
  `tfc:metal/double_ingot/cast_iron` + `tfc:metal/sheet/wrought_iron` → 1 `drill_head`
- [x] TFC welding #2 (tier 4, steel-ветка):
  `data/tfc/recipe/welding/drill_head_steel.json` —
  `#c:ingots/steel` + `tfc_aeronautics:metal/tight_sheet/steel` → 1 `drill_head`
- [x] Create mechanical_drill override (ветка 1 `recipe-override`, без `BANNED_RECIPES`):
  `data/create/recipe/crafting/kinetics/mechanical_drill.json` —
  shaped 3×1 `["D","C","S"]`, ключи `D = drill_head` + `C = create:andesite_casing`
  + `S = create:shaft` → 1 `create:mechanical_drill`, `show_notification: false`

## Локализация
- [x] en_us: `item.tfc_aeronautics.drill_head` → "Drill Head"
- [x] ru_ru: `item.tfc_aeronautics.drill_head` → "Сверло"

## Дизайн-заметки

### Почему два welding-рецепта

* **Cast iron + wrought iron, tier 3** — «дешёвая» ветка. Cast iron доступен
  рано (двойной слиток выходит из обычной TFC-сварки двух cast iron инготов).
  Tier 3 совпадает с `tight_sheet_wrought_iron.json` и `tfc:metal/sheet/wrought_iron`
  — стандартный порог для кованых железных изделий. Wrought iron sheet как
  «обкладка» вокруг чугунного сердечника — семантика двухкомпонентного
  кованого инструмента, ближе к `wrench_head_brass` / `goggles` по логике
  «простая головка + простая оболочка».
* **Steel + tight_sheet/steel, tier 4** — «продвинутая» ветка. Tight_sheet/steel
  — аэронавтический полуфабрикат (`tfc_aeronautics:metal/tight_sheet/steel`,
  см. `plans/tight-sheets.md`): производится через TFC-наковальню из
  `c:double_ingots/steel` или через Create-пресс (`data/tfc_aeronautics/recipe/pressing/tight_sheet_steel.json`).
  Tier 4 совпадает с `tight_sheet_steel.json` — игрок уже на этом тире,
  свить drill_head из тех же материалов, что tight_sheet, естественно.
  Сталь лучше держит нагрузку: на той же Tier-4-наковальне можно делать
  более «качественную» головку сверла.

Выбор между ними — баланс/прогрессия, не «один правильный путь». Оба
рецепта доступны параллельно.

### Почему pattern `["D","C","S"]`

Декомпозиция механического бура по устройству (которое в ванильном Create
выглядит как вертикальный механизм с головкой-шпинделем, корпусом-кожухом
и нижним фланцем под shaft):

* `D` (верх) — собственно режущая головка;
* `C` (центр) — `andesite_casing` как корпус с кинематикой;
* `S` (низ) — `shaft` как ось, идущая вниз в кинематическую сеть.

3-символьный вертикальный pattern естественно ложится на 3-уровневое
устройство. Альтернативы (типа оригинального `[" A ","AIA"," C "]`)
в TFC-сборке мертвы — `#c:ingots/iron` пуст и `create:andesite_alloy`
не производится.

### Моделирование 3D (отложено)

Сейчас drill_head — Item с placeholder `item/generated` поверх временной
текстуры. Для настоящего 3D-рендера в проекте нет готового паттерна
(все «3D-модели» в моде — Block+BlockEntity через Flywheel/BER; ISTER
для Item-only не реализован). Варианты, когда дойдёт арт:

1. **Block+BlockEntity** (как `stamping_press_head.bbmodel`) — превратить
   drill_head в полноценный блок-модель, рендерить через PartialModel
   (Flywheel). Прецедент есть (`stamping_press_head.bbmodel` рендерится
   в блоке `stamping_press`); нужно зеркалить под drill head.
2. **ISTER** (BlockEntityWithoutLevelRenderer) — модифицировать `drill_head`
   как Item, который тянет собственный BE-подобный рендерер для отображения
   в инвентаре/в руке/на земле. В проекте прецедентов нет; придётся
   вводить с нуля.
3. **item/handheld + кастомная JSON-модель** — без полного 3D, но с
   2.5D-многослойной текстурой. Не «3D», но простой middle-ground, если
   полное 3D не нужно.

Решение отложено до прихода арта: пользователь выберет, какой уровень
«3D» нужен.

### Что НЕ сделано

* Нет `tfc/item_heat/drill_head.json` — drill_head не плавится ни в один
  металл (потому что материал сварки может быть разный). Если потом
  понадобится переплавка (например, при поломке) — потребуется
  per-материал или compromise-металл + heat-tuning.
* Не используется `bonus: copy_worst` в welding-рецептах — heat не
  наследуется сваркой. Если потом нужно, чтобы готовая головка «помнила»
  металл, из которого её сварили (cast iron vs steel — разные свойства
  при бурении), добавить `bonus: copy_worst` + `tfc/item_heat/drill_head.json`
  по прецеденту `tfc:metal/shears/wrought_iron` в TFC reference.
