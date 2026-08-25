# Anvil (tier-1 downgrade для остальных металлов)

## Идея

TFC регистрирует наковальню только для металлов с `toolTier` (9 металлов:
медь, кованое железо, бронзы, стали). Остальные 19 металлов остаются без
наковальни — рецепты наковальни (например, штамп-пресс) для них невозможны.
Эта подсистема закрывает пробел: для каждого из 19 металлов регистрируется
tier-1 наковальня. Это «даунгрейд»-вариант с полной функциональностью
(TFC-овский `AnvilBlock` + `AnvilBlockEntity`), но с самым низким визуально
корректным тиром.

## Что сделано

19/19 ✓

- `AnvilRegistration` (Java) — итерация по `Metal.values()`, фильтр через
  `Metal.BlockType.ANVIL.has(metal)`, hardcoded tier=1. Регистрирует 2
  `DeferredRegister`: `BLOCKS` + `ITEMS`.
- TFC-овские классы напрямую: `AnvilBlock` (без подкласса) и `AnvilBlockEntity`
  (без подкласса), привязанные к `TFCBlockEntities.ANVIL.get()`. Это
  необходимо, потому что TFC-овская `AnvilContainer` меню-фабрика
  хардкодит именно этот BE-тип при поиске — подмена типа ломает открытие меню.
- `extendTfcAnvilTypeValidBlocks()` (Java, рефлексия) — добавляет 19 наших
  блоков в `TFCBlockEntities.ANVIL.get().validBlocks`, без чего
  `BlockEntityType.create(pos, state)` валится с `IllegalStateException` при
  попытке поставить блок.
- Per-metal JSON-ассеты (blockstate × 4 facing, block model с parent
  `tfc:block/anvil`, item model) — все 57 файлов руками, в
  `src/main/resources/assets/tfc_aeronautics/`.
- Per-metal shaped crafting recipe (3×3 полый, 8 слитков) в
  `src/main/resources/data/tfc_aeronautics/recipe/crafting/metal/anvil/`:
  - 10 металлов с покрытием `c:double_ingots/<metal>` (bismuth, brass,
    cast_iron, gold, nickel, rose_gold, silver, sterling_silver, tin,
    zinc) — тег `c:double_ingots/<metal>`.
  - 9 металлов без покрытия (pig_iron, weak_steel, weak_blue_steel,
    weak_red_steel, high_carbon_steel, high_carbon_black_steel,
    high_carbon_blue_steel, high_carbon_red_steel, unknown) — тег
    `c:ingots/<metal>`. Для них добавлены `tfc:heating` рецепты на 700
    mB (см. ниже).
- Per-metal `tfc:heating` рецепт для переплавки наковальни обратно в
  жидкий металл: `src/main/resources/data/tfc_aeronautics/recipe/heating/metal/anvil/<metal>.json`.
  10 покрытых — стандартные 1400 mB, 9 непокрытых — сниженные 700 mB.
- Lang entries в `en_us.json` и `ru_ru.json`.
- DOCS.md / ROADMAP.md / PROJECT_STRUCTURE.md синхронизированы.

## Список металлов

`bismuth`, `brass`, `gold`, `nickel`, `rose_gold`, `silver`, `tin`, `zinc`,
`sterling_silver`, `cast_iron`, `pig_iron`, `weak_steel`, `weak_blue_steel`,
`weak_red_steel`, `high_carbon_steel`, `high_carbon_black_steel`,
`high_carbon_blue_steel`, `high_carbon_red_steel`, `unknown` (19 шт).

## Дизайн-решения

- **Hardcoded tier=1**: TFC-овские наковальни используют
  `metal.toolTier().level()`, у этих металлов `toolTier == null` → NPE.
  Раньше фиксировали tier=0 как аналогию с rock anvil TFC, но
  `AnvilBlockEntityRenderer` имеет мёртвую ветку для `tier == 0`
  (`yOffset = 0.875f` вместо `0.6875f`), из-за чего предметы на таких
  наковальнях визуально «парят» над поверхностью. Rock anvil использует
  другой `BlockEntity`, и этот рендерер для него не запускается — аналогия
  была ошибочной. tier=1 — наименьший функциональный тир, который TFC
  реально использует (олово/розовое золото), и для которого рендерер
  рисует предметы корректно.

- **Reuse TFC `AnvilBlock` + `TFCBlockEntities.ANVIL` напрямую**: сначала
  пробовали через подклассы (`TierZeroAnvilBlock` + `CustomAnvilBlockEntity`,
  со своим `BlockEntityType`), но клиентский `AnvilContainer` падал с
  `NoSuchElementException: No value present` — TFC-овская
  `RegistrationHelpers.registerBlockEntityContainer` хардкодит
  `TFCBlockEntities.ANVIL.get()` при поиске BE, и с нашим типом
  `getBlockEntity(pos, TFCBlockEntities.ANVIL.get())` возвращал `Optional.empty()`.
  Решение: используем тот же BE-тип, что и TFC, но расширяем его
  `validBlocks` через рефлексию.

- **Расширение `validBlocks` через рефлексию**: `BlockEntityType.validBlocks`
  — `private final Set<Block>`. Обычно в Minecraft 1.21.1 это
  `ObjectLinkedOpenHashSet` (мутабельный), `addAll(ours)` работает. Если
  какой-то форк Mojang-а вернёт `ImmutableSet` — `UnsupportedOperationException`
  ловится, и поле подменяется новой мутабельной копией. Метод
  `synchronized` + флаг `extendedTfcAnvilType`, чтобы не прогонять
  рефлексию 19 раз подряд на cold-start.

- **Имя блока `tfc_aeronautics:metal/anvil/<metal>`**: тот же путь, что у
  TFC-овского `tfc:metal/anvil/<металл>` — в логах и табе группа «metal/anvil»
  стоит рядом, что упрощает навигацию. Слеши в пути, точки в lang-ключе
  (`block.tfc_aeronautics.metal.anvil.<metal>`) — как у TFC.

- **Сплит крафта по покрытию `c:double_ingots/<metal>`**: для 10 металлов с
  покрытием используем `c:double_ingots/<metal>` (8 двойных слитков —
  зеркало TFC-овского рецепта, который требует 4 двойных слитка, у нас
  по sum-of-matter получается 8 двойных = 16 одинарных). Для 9 металлов
  без покрытия (pig_iron, weak_*, high_carbon_*, unknown) — 8 одинарных
  из `c:ingots/<metal>`. Pattern `###` / ` # ` / `###` и count 8 одинаковы
  для обеих групп.

- **Heating для 9 непокрытых на 700 mB вместо 1400**: TFC-овский стандарт
  переплавки наковальни — 1400 mB жидкого металла. Для 9 непокрытых
  металлов (без `c:double_ingots/<metal>` и без реального пути получить
  двойной слиток) снижаем до 700 mB, чтобы компенсировать отсутствие
  промежуточного шага «слить двойной слиток». Для high_carbon_*
  возвращаем базовую форму жидкости (high_carbon_steel →
  `tfc:metal/pig_iron`, и т.д.), как в TFC-овском ingot-heating.

- **Не пытаемся переопределить TFC-овский `Metal` enum** — он
  документирован как «Not extensible», и расширять его через API
  невозможно. Работаем снаружи: регистрируем блоки в своём namespace,
  фильтруем через `Metal.BlockType.ANVIL.has(metal)`.

## Что НЕ делали

- **Не добавляли tier≥2 варианты.** Если понадобится — это уже отдельная
  задача, не «tier-1 даунгрейды».
- **Не модифицировали TFC-овские наковальни.** 9 «настоящих» TFC-наковален
  работают штатно, фильтрация их не затрагивает.
- **Не подменяли TFC-овский BE-тип на свой.** Альтернатива ломает клиентское
  открытие меню (см. «Дизайн-решения»).
- **Не дублировали текстуры/модели.** Всё через parent-ссылку на
  `tfc:block/anvil` + `tfc:block/metal/smooth/<metal>`.

## Замечание о совместимости с TFC-овскими рецептами

С tier=1 наши наковальни **подходят** для `tfc:anvil`-рецептов с
`minTier <= 1`. В vanilla TFC таких нет (минимальный `minTier` среди
металлических рецептов — 1 для олова/розового золота, и для них уже
есть «настоящие» наковальни). Если в будущих версиях TFC появятся
`tfc:anvil`-рецепты с `minTier = 1` для других металлов — наши
наковальни автоматически их примут. Это by design: downgrade-вариант
должен быть совместим с низкотировыми рецептами.

## Открытые вопросы / будущее

- **Рецепты, использующие tier-1 наковальни.** Наши наковальни совместимы
  с любыми `tfc:anvil`-рецептами `minTier <= 1`. В vanilla TFC таких
  нет (минимальный `minTier` — 1 для олова/розового золота). Возможный
  follow-up: добавить специфичные для нашего мода рецепты, требующие
  tier=1 (например, «дешёвые» изделия из этих металлов).
- **`unknown` наковальня** функциональна (есть блок, рецепт, текстура),
  но семантически странная: «неизвестный сплав» — это placeholder для
  неопознанных слитков. Крафтить наковальню из них тривиально. Оставляем
  как есть (по явной просьбе пользователя — «даже неизвестного сплава»),
  но в будущем можно рассмотреть blacklist.
