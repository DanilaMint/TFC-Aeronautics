# TFC FOOD + Create integration

**Прогресс:** 1/2 ✓ (milling ✓, mixing ⏳)

## Назначение

Мета-план: единая точка трекинга всех интеграций, где TFC FOOD pipeline
заводится на Create машины. TFC само по себе не предлагает механической
автоматизации пищевого производства — только `tfc:barrel_sealed` для
заквасок и «выдержки», `tfc:pot` для варки, наковальню для формовки.
Этот план фиксирует, какие TFC FOOD шаги проброшены в Create и в каком
состоянии.

Разбор механик — в `DOCS.md` §N (TFC FOOD processing in Create machines).
Профильные sub-plans хранят детали реализации каждого шага.

## Готово

- [x] **Milling (grain → flour)** — TFC quern-зеркало в Create
  millstone. Реализация через кастомный `RecipeType`
  `tfc_aeronautics:quern_milling` (extends `MillingRecipe`) + миксин
  `MillstoneBlockEntityMixin` для маршрутизации через
  `ItemStackProvider.getSingleStack(capturedInput)`. Разбор — `DOCS.md`
  §2.3 (`tfc_aeronautics:quern_milling`). Decay-таймер муки из мельницы
  идентичен муке из жернова (`tfc:copy_food` + `Cf = (1 - p) * T + p * Ci`).
  - 6 grain'ов × помол
  - files: `data/tfc_aeronautics/recipe/milling/food/<grain>_flour.json`
  - детали: `plans/quern-millstone-sync.md`

## В работе

- [x] **Mixing (flour → dough)** — TFC crafting grid версия
  (`tfc:advanced_shapeless_crafting`) → Create basin + mixer.
  - 6 grain'ов × `create:mixing` recipes
  - files: `src/generated/resources/data/tfc_aeronautics/recipe/mixing/
    {wheat,barley,maize,oat,rye,rice}_dough.json`
  - datagen: `generate/generate_mixing_recipes.py`
  - rot timer sync: миксин `mixin/BasinMixingFoodDataMixin.java` —
    `HEAD` ловит TFC flour из basin input, `TAIL` применяет
    `FoodCapability.updateFoodFromPrevious` к результату, если он —
    один из 6 TFC doughs (`TFC_DOUGHS` set)
  - основной план: `plans/misty-jumping-snail.md` (см. ретроспективно
    на следующий итерации)

## Планируется

- [ ] **Dough → bread** — TFC выпекает хлеб в `tfc:pot` / `tfc:firepit`
  (нужна жарка). Create `mechanical_press` не подходит. Варианты:
  кастомная машина, либо адаптация `tfc:pot` под basin + heat.
  - heat через `tfc_aeronautics:heat_dealers` (см. `DOCS.md` §16)
  - альтернатива: эмулировать TFC pot recipe через basin + heat
- [ ] **Sourdough starter** — TFC `tfc:barrel_sealed` 12-часовой
  рецепт. Возможен аналог через mixer spin-time (модифицировать
  `processing_time` или адаптировать recipe type под sealed-семантику).
- [ ] **Dough → pasta** — TFC pasta shaping. Create `mechanical_press`
  через `tfc_aeronautics:stamping_press` (см. `DOCS.md` §3) — кандидат.
- [ ] **Drying / smoking** — TFC food drying (мясо, рыба, фрукты) в
  pit/solar dryer. Можно через автоматизацию firepit/cooler.
- [ ] **Cheese pressing** — TFC cheese curds → cheese wheel через
  press. Аналог — `tfc_aeronautics:stamping_press` с другим фильтром.

## Принцип синхронизации rot timer (общий)

TFC food data — компонент `tfc:food` (через `TFCComponents.FOOD`):
- `creationDate` — когда декремент-таймер начался
- `food` (`FoodData`) — nutrition, water, saturation
- `traits` — листва FoodTrait (калорийность, скорость порчи…)

`FoodCapability.updateFoodFromPrevious(oldStack, newStack)` копирует
`traits` с `oldStack` на `newStack` и пересчитывает `creationDate`
так, чтобы decay proportion сохранился:
```
Cf = (1 - p) * T + p * Ci
```
где `p = newDecay / oldDecay`. TFC-вские `tfc:copy_food` /
`tfc:copy_oldest_food` modifiers вызывают `updateFoodFromPrevious` /
`updateFoodFromAllPrevious` соответственно.

В Create-flow эта цепочка не запускается автоматически. Если шаг
должен сохранить rot timer — нужен либо кастомный `RecipeType`
(как `tfc_aeronautics:quern_milling`), либо post-processing mixin
(как `BasinMixingFoodDataMixin`) с вызовом
`FoodCapability.updateFoodFromPrevious(capturedInput, result)`.

## Баг-фикс (наследуется из milling)

- [x] Pre-shrink input capture — `MillstoneBlockEntityMixin` (см.
  `quern-millstone-sync.md`). Для basin: mixin HEAD захватывает
  `copy()` flour ДО того, как `BasinRecipe.apply` его прочтёт/сожрёт.
  `.copy()` обязательно, иначе in-place `shrink(1)` обнулит count у
  нашего снимка, и `ItemStack.copy()` внутри `updateFoodFromPrevious`
  отдаст `EMPTY` без FOOD-компонента → `copy_food` тихо срабатывает
  вхолостую.

## Документация

- [ ] `DOCS.md` — добавить раздел «TFC FOOD processing in Create
  machines» (после §16 Heat Dealers, перед §17 Fuel). Подсекции:
  overview, milling (ссылка на §2.3), mixing (эта реализация),
  future.
