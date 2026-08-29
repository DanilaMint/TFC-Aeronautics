# План: Сварочный стол (Welding Depot)

## Context

**Зачем:** добавить блок, который позволяет автоматизировать TFC-сварку через `create:mechanical_press`. Игрок кладёт на стол две детали и флюс; когда пресс над столом опускается в нижнюю точку, стол ищет `tfc:welding`-рецепт по двум деталям и, если tier подходит и обе детали достаточно горячие, выполняет его.

**Что получаем:**
- 5 блоков (по одному на металл: wrought_iron, steel, black_steel, blue_steel, red_steel) с разным tier и текстурой верха.
- Tier берётся **реальный из `TFCTiers.java`** (а не из начальной спецификации пользователя, где red_steel был ошибочно 7):
  - wrought_iron = 3
  - steel = 4
  - black_steel = 5
  - blue_steel = 6
  - red_steel = 6
- Существующие welding-рецепты мода (`drill_head_cast_iron` tier 3, `drill_head_steel` tier 4) автоматически работают на совместимом депо.
- Новых welding-рецептов в рамках этой задачи не добавляем — депо является универсальным исполнителем `tfc:welding`.

**Решения, утверждённые пользователем:**
- **4 слота:** SLOT_LEFT=0 (первая деталь), SLOT_RIGHT=1 (вторая деталь), SLOT_FLUX=2 (только `tfc:powder/flux`), **SLOT_OUTPUT=3 (отдельный слот для результата сварки)**.
- **Визуально 4 предмета в 2 ряда:**
  - Передний ряд (Z = +0.18): LEFT (-0.25), FLUX (0), RIGHT (+0.25).
  - Задний ряд (Z = -0.18): OUTPUT (0, по центру).
- Результат сварки: уходит в SLOT_OUTPUT (видно сзади). SLOT_LEFT/RIGHT очищаются, SLOT_FLUX уменьшается на 1.
- **Жёсткое разделение insert/extract:** IItemHandler-обёртка разрешает вставку только в SLOT_LEFT/RIGHT/FLUX, а извлечение — только из SLOT_OUTPUT. Create-шлюзы, воронки и хопперы физически не смогут забрать флюс или входные детали — только готовый результат.
- **Автоподбор сверху:** переопределить `Block.fallOn` — когда ItemEntity падает на верхнюю грань стола, его ItemStack вставляется в первый подходящий слот (0-2). Это даёт поведение «как у create:depot»: кинул предмет на стол — он лежит.
- Детекция удара пресса: активный tick-опрос `PressingBehaviour` (а не миксин/событие).

**Что нужно от пользователя перед финальной сборкой:**
- `.bbmodel` для блока (пользователь сказал, что скинет позже). До его получения план содержит placeholder-структуру.

---

## Архитектура

### Файлы, которые создаём

**Java (пакет `ru.tfc_aeronautics.welding_depot`):**
1. `WeldingDepotBlock.java` — общий класс блока с tier-полем и `IWrenchable`.
2. `WeldingDepotBlockEntity.java` — `SmartBlockEntity` с 3-слот инвентарём и tick-детекцией пресса.
3. `WeldingDepotBlockItem.java` — обёртка для предотвращения выброса самого блока при краше (опционально; посмотреть, нужно ли это, после `.bbmodel`).
4. `WeldingDepotRegistration.java` — `DeferredRegister<Block>`, `Items`, `BlockEntityTypes` + `register(IEventBus)`.
5. `WeldingDepotCapabilities.java` — регистрация `IItemHandler` capability через `RegisterCapabilitiesEvent`.
6. `WeldingDepotItemHandler.java` — `IItemHandler`-обёртка над `ItemStackHandler(4)` с разделением insert/extract (insert в 0-2, extract только из 3).

**Клиент (пакет `ru.aeronautics.client.welding_depot`):**
7. `WeldingDepotBlockEntityRenderer.java` — рендер 3 предметов сверху: левый, центральный, правый.

**Ресурсы:**
8. `src/main/resources/assets/tfc_aeronautics/models/block/welding_depot/base.json` — родительская модель с плейсхолдерами `#side`, `#top`, `#casing`.
9. `src/main/resources/assets/tfc_aeronautics/models/block/welding_depot/wrought_iron.json` — child с `#top = top_wrought_iron`.
10. …то же для `steel`, `black_steel`, `blue_steel`, `red_steel`.
11. `src/main/resources/assets/tfc_aeronautics/models/item/welding_depot/wrought_iron.json` и ещё 4 — `{ "parent": ".../block/welding_depot/wrought_iron" }`.
12. `src/main/resources/assets/tfc_aeronautics/blockstates/welding_depot/wrought_iron.json` и ещё 4 — `{ "variants": { "": { "model": "..." } } }`.
13. `src/main/resources/assets/tfc_aeronautics/textures/block/welding_depot/side.png` — общая боковая.
14. `src/main/resources/assets/tfc_aeronautics/textures/block/welding_depot/top_wrought_iron.png` и ещё 4 — уникальные верхи.
15. `src/main/resources/assets/tfc_aeronautics/textures/block/welding_depot/casing.png` — низ/внутри.
16. `src/main/resources/assets/tfc_aeronautics/lang/en_us.json` — добавить 5 ключей `block.tfc_aeronautics.metal.welding_depot.*`.
17. `src/main/resources/assets/tfc_aeronautics/lang/ru_ru.json` — добавить 5 русских переводов.

**Рецепты блоков:**
18. `src/main/resources/data/tfc_aeronautics/recipe/crafting/welding_depot/wrought_iron.json` — `"III", " C "` для wrought_iron.
19. …то же для 4 других металлов.

**Клиент регистрация:**
20. `src/client/java/ru/aeronautics/client/welding_depot/WeldingDepotClientRegistration.java` — `RegisterRenderersEvent`/`RegisterBlockEntityRendererEvent` для BER.

**Главный мод:**
21. `src/main/java/ru/tfc_aeronautics/TFCAeronautics.java` — вызвать `WeldingDepotRegistration.register(bus)`.
22. `src/main/java/ru/tfc_aeronautics/CreativeTabs.java` — добавить 5 `output.accept` строк.

**Документация:**
23. `DOCS.md` — раздел про Welding Depot (по конвенции проекта — каждый новый механизм имеет раздел).
24. `ROADMAP.md` — отметить подсистему как реализованную.

### Паттерны и существующий код, который переиспользуем

| Что | Где |
|---|---|
| `Block implements IBE, IWrenchable` | `src/main/java/ru/tfc_aeronautics/heater/HeaterBlock.java:45` |
| `getStateForPlacement` с `HORIZONTAL_FACING=false`, `LIT=false` | `HeaterBlock.java:57-63` |
| `onWrenched` + `getRotatedBlockState` | `HeaterBlock.java:167-191` |
| `SmartBlockEntity` + `ItemStackHandler` в конструкторе | `HeaterBlockEntity.java:60-110` |
| Per-material регистрация (EnumMap + конструкторный tier) | `src/main/java/ru/tfc_aeronautics/anvil/AnvilRegistration.java:47-76` |
| Регистрация capability через `RegisterCapabilitiesEvent` | `src/main/java/ru/tfc_aeronautics/heater/HeaterCapabilities.java` |
| `noOcclusion()` + per-face текстуры в модели | `src/main/resources/assets/tfc_aeronautics/models/block/stamping_press.json` |
| Parent + child с подменой одной текстуры (per-material) | `src/main/resources/assets/tfc_aeronautics/models/block/metal/anvil/high_carbon_steel.json` |
| Per-material item-model = `parent: block/...` | `src/main/resources/assets/tfc_aeronautics/models/item/metal/anvil/high_carbon_steel.json` |
| Per-material blockstate = `{ "variants": { "": {...} } }` | `src/main/resources/assets/tfc_aeronautics/blockstates/metal/anvil/high_carbon_steel.json` |
| Heat API (`HeatCapability.get(stack).canWeld()`) | `src/main/java/ru/tfc_aeronautics/stamping_press/StampingPressBlockEntity.java:154-172` |
| `RecipeHelpers.getHolder(level, TFCRecipeTypes.WELDING, inventory)` + `recipe.assemble(inventory)` | `code_references/TerraFirmaCraft/src/main/java/net/dries007/tfc/common/blockentities/AnvilBlockEntity.java:439-491` |
| Creative tab — per-material цикл | `src/main/java/ru/tfc_aeronautics/CreativeTabs.java:55` (`TightSheetRegistration`) |
| Lang — per-material ключи `block.<modid>.<path с / на .>` | `src/main/resources/assets/tfc_aeronautics/lang/en_us.json:36-55` |

### Внешние API, на которые опираемся

- `com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity.getPressingBehaviour()` — `code_references/Create/src/main/java/com/simibubi/create/content/kinetics/press/MechanicalPressBlockEntity.java:78-80`.
- `com.simibubi.create.content.kinetics.press.PressingBehaviour.running`, `runningTicks`, `CYCLE` — `code_references/Create/src/main/java/com/simibubi/create/content/kinetics/press/PressingBehaviour.java:41-42` (public).
- `net.dries007.tfc.common.recipes.TFCRecipeTypes.WELDING`, `WeldingRecipe` — `code_references/TerraFirmaCraft/src/main/java/net/dries007/tfc/common/recipes/WeldingRecipe.java:31-160` (matches, assemble, isCorrectTier).
- `net.dries007.tfc.common.recipes.RecipeHelpers.getHolder(level, type, inventory)` — `code_references/TerraFirmaCraft/src/main/java/net/dries007/tfc/common/recipes/RecipeHelpers.java:184`.
- `net.dries007.tfc.common.component.heat.HeatCapability.get(stack).canWeld()` — `code_references/TerraFirmaCraft/src/main/java/net/dries007/tfc/common/component/heat/HeatCapability.java:41-115`.
- `net.dries007.tfc.common.TFCTags.Items.WELDING_FLUX` — `code_references/TerraFirmaCraft/src/main/java/net/dries007/tfc/common/TFCTags.java:587`.

---

## Структура Block / BlockEntity

### `WeldingDepotBlock`

```java
public class WeldingDepotBlock extends Block implements IBE<WeldingDepotBlockEntity>, IWrenchable {

    private final int tier; // 3..6 (wrought_iron..red_steel)

    public WeldingDepotBlock(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }

    public int getTier() { return tier; }

    // getStateForPlacement — НЕ задаём HORIZONTAL_FACING (см. решение ниже)
    // IBE<WeldingDepotBlockEntity> — getBlockEntityClass, getBlockEntityType
    // onWrenched / getRotatedBlockState — НЕ нужны (нет поворота)
}
```

**Решение по повороту:** Create-depot не вращается (`blockstates/depot.json` — без variants). У нашего стола тоже нет асимметрии — он симметричен по всем 4 горизонтальным направлениям. Поэтому `BlockStateProperties.HORIZONTAL_FACING` НЕ добавляется, `IWrenchable` НЕ реализуется. Это даёт самый чистый blockstate и самую простую модель.

**`onRemove`** (как в `HeaterBlock.java:124-132`): дропнуть все 4 предмета из инвентаря (LEFT, RIGHT, FLUX, OUTPUT).

**`useItemOn`** (правый клик с предметом в руке): если хит — верх, положить предмет в первый подходящий пустой слот (по логике из `HeaterBlock.java:89-112`). SLOT_OUTPUT пропускается — игрок не может положить туда предмет рукой.

**`useWithoutItem`** (правый клик пустой рукой): забрать **один** предмет из инвентаря по приоритету **OUTPUT → LEFT → RIGHT → FLUX**. То есть: сначала проверяем `inventory.extractItem(SLOT_OUTPUT, 1, false)` — если не пусто, отдаём игроку; если OUTPUT пуст → пробуем LEFT, потом RIGHT, потом FLUX. Если ни в одном слоте ничего нет — fail. Если игрок на shift+ПКМ — то же поведение (по одному предмету; если хочется забрать всё — несколько кликов). Это соответствует поведению create:depot.

Предмет добавляется в `player.getInventory()` или дропается на пол через `Containers.dropItemStack(...)` (как в `HeaterBlock.tryExtract`, строки 114-122).

**`fallOn(Level, BlockState, BlockPos, Entity, float)`** (предмет падает сверху на стол): если `Entity instanceof ItemEntity itemEntity`, попытаться вставить `itemEntity.getItem()` в первый подходящий слот 0-2 через IItemHandler. При успешной полной вставке — `itemEntity.discard()`. При частичной — `itemEntity.setItem(remainder)`. Это поведение «предмет кинул — он лежит», как у create:depot. Если падает игрок — игнорируем (стандартная fall-обработка).

Логика подбора — общий хелпер, переиспользуется из `useItemOn` и `fallOn`.

### `WeldingDepotBlockEntity`

```java
public class WeldingDepotBlockEntity extends SmartBlockEntity {

    public static final int SLOT_LEFT   = 0;
    public static final int SLOT_RIGHT  = 1;
    public static final int SLOT_FLUX   = 2;
    public static final int SLOT_OUTPUT = 3;

    private final ItemStackHandler inventory;       // 4 слота, из них OUTPUT скрыт для extract
    private final WeldingDepotItemHandler externalHandler; // IItemHandler-обёртка для capability

    public WeldingDepotBlockEntity(BlockPos pos, BlockState state) {
        super(WeldingDepotRegistration.WELDING_DEPOT_BE.get(), pos, state);
        this.inventory = new ItemStackHandler(4) {
            @Override public boolean isItemValid(int slot, ItemStack stack) {
                if (slot == SLOT_FLUX)   return Helpers.isItem(stack, TFCTags.Items.WELDING_FLUX);
                if (slot == SLOT_OUTPUT) return false; // OUTPUT принимается только кодом, не логистикой
                return true;
            }
            @Override protected void onContentsChanged(int slot) { setChanged(); }
        };
        this.externalHandler = new WeldingDepotItemHandler(inventory);
    }

    @Override public void tick() {
        super.tick();
        if (level.isClientSide) return;
        BlockPos above = worldPosition.above();
        BlockEntity be = level.getBlockEntity(above);
        if (!(be instanceof MechanicalPressBlockEntity press)) return;
        PressingBehaviour pb = press.getPressingBehaviour();
        if (!pb.running) return;
        if (pb.runningTicks != PressingBehaviour.CYCLE / 2) return;
        tryWeld();
    }

    private void tryWeld() {
        ItemStack left  = inventory.getStackInSlot(SLOT_LEFT);
        ItemStack right = inventory.getStackInSlot(SLOT_RIGHT);
        ItemStack flux  = inventory.getStackInSlot(SLOT_FLUX);
        ItemStack out   = inventory.getStackInSlot(SLOT_OUTPUT);
        if (left.isEmpty() || right.isEmpty() || flux.isEmpty()) return;
        if (!out.isEmpty()) return; // не перезаписывать существующий результат — ждём извлечения
        if (!HeatCapability.get(left).canWeld())  return;
        if (!HeatCapability.get(right).canWeld()) return;

        WeldingInventory inv = new WeldingInventory(left, right, getTier());
        Optional<RecipeHolder<WeldingRecipe>> holder =
            RecipeHelpers.getHolder(level, TFCRecipeTypes.WELDING, inv);
        if (holder.isEmpty()) return;
        WeldingRecipe recipe = holder.get().value();
        if (!recipe.isCorrectTier(getTier())) return;

        ItemStack result = recipe.assemble(inv);
        // копируем heat (как в TFC anvil)
        IHeat resultHeat = HeatCapability.get(result);
        IHeat leftHeat   = HeatCapability.get(left);
        IHeat rightHeat  = HeatCapability.get(right);
        if (resultHeat != null) {
            if (leftHeat  != null) resultHeat.setTemperatureIfWarmer(leftHeat);
            if (rightHeat != null) resultHeat.setTemperatureIfWarmer(rightHeat);
        }
        inventory.setStackInSlot(SLOT_OUTPUT, result);
        inventory.setStackInSlot(SLOT_LEFT,   ItemStack.EMPTY);
        inventory.setStackInSlot(SLOT_RIGHT,  ItemStack.EMPTY);
        inventory.getStackInSlot(SLOT_FLUX).shrink(1);
        setChanged();
    }

    public ItemStackHandler getInventory() { return inventory; }
    public IItemHandler getExternalHandler() { return externalHandler; }
    public int getTier() { return ((WeldingDepotBlock) getBlockState().getBlock()).getTier(); }
}
```

**Внутренний `WeldingInventory`** (record/implements `WeldingRecipe.Inventory`):
```java
record WeldingInventory(ItemStack main, ItemStack secondary, int tier)
    implements WeldingRecipe.Inventory {
    public ItemStack getMain()      { return main; }
    public ItemStack getSecondary() { return secondary; }
    public int getTier()            { return tier; }
    public ItemStack getItem(int i) { return i == 0 ? main : secondary; }
    public int size()               { return 2; }
    public boolean isEmpty()        { return main.isEmpty() && secondary.isEmpty(); }
}
```

**Tick-стратегия:**
- `tick()` (20 Гц) — допустимо, т.к. `getBlockEntity(pos.above())` это O(1) lookup + один `instanceof` + один branch по `pb.runningTicks`. Не тяжелее, чем `HeaterBlockEntity.tick()` (строки 165-229).
- Альтернатива: `lazyTick()` (раз в 30 тиков) + флаг в `tick()`. **Выбираем первый** (20 Гц) — проще и не страдает производительность, т.к. ветка ранняя.

### `WeldingDepotItemHandler` — обёртка с разделением insert/extract

Чтобы воронки/шлюзы/хопперы Create не могли забрать флюс или входные детали, IItemHandler жёстко разделяет insert и extract по слотам:

```java
public class WeldingDepotItemHandler implements IItemHandler {
    private final ItemStackHandler inv; // 4 слота: LEFT, RIGHT, FLUX, OUTPUT

    @Override public int getSlots() { return 4; }

    @Override public ItemStack getStackInSlot(int slot) {
        return inv.getStackInSlot(slot);
    }

    // Вставка — только в SLOT_LEFT/RIGHT/FLUX (0-2). В OUTPUT отказываем.
    @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (slot == SLOT_OUTPUT) return stack;
        if (!isItemValid(slot, stack)) return stack;
        return inv.insertItem(slot, stack, simulate);
    }

    // Извлечение — только из SLOT_OUTPUT. Из LEFT/RIGHT/FLUX возвращаем EMPTY.
    @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot != SLOT_OUTPUT) return ItemStack.EMPTY;
        return inv.extractItem(slot, amount, simulate);
    }

    @Override public int getSlotLimit(int slot) { return inv.getSlotLimit(slot); }

    @Override public boolean isItemValid(int slot, ItemStack stack) {
        if (slot == SLOT_FLUX)   return Helpers.isItem(stack, TFCTags.Items.WELDING_FLUX);
        if (slot == SLOT_OUTPUT) return false;
        return true;
    }
}
```

**Дополнительная логика для `Block.useItemOn`** (правый клик): при ручной вставке игрок кладёт в первый подходящий слот из 0-2 (как Heater делает сейчас). SLOT_OUTPUT не предлагается — он зарезервирован для сварки.

### Регистрация

**`WeldingDepotRegistration`** — структура по образцу `StampingPressRegistration`:
```java
public class WeldingDepotRegistration {
    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(BusUtils.BUS, TFCAeronautics.MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(BusUtils.BUS, TFCAeronautics.MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
        DeferredRegister.create(BusUtils.BUS, TFCAeronautics.MOD_ID);

    public static final LinkedHashMap<Metal, DeferredHolder<Block, WeldingDepotBlock>> DEPOTS = new LinkedHashMap<>();

    public static final LinkedHashMap<Metal, DeferredHolder<Item, Item>> DEPOT_ITEMS = new LinkedHashMap<>();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WeldingDepotBlockEntity>> WELDING_DEPOT_BE =
        BLOCK_ENTITY_TYPES.register("welding_depot", () ->
            BlockEntityType.Builder.of(WeldingDepotBlockEntity::new,
                DEPOTS.values().stream().map(DeferredHolder::get).toArray(Block[]::new))
            .build(null));

    static {
        for (DepotTier tier : DepotTier.values()) {
            String id = "metal/welding_depot/" + tier.materialSerializedName;
            DeferredHolder<Block, WeldingDepotBlock> block = BLOCKS.register(id,
                () -> new WeldingDepotBlock(BlockBehaviour.Properties.of()
                    .mapColor(tier.mapColor)
                    .strength(5F, 8F)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .requiresCorrectToolForDrops(),
                    tier.level));
            DEPOTS.put(tier.metal, block);
            DEPOT_ITEMS.put(tier.metal, ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties())));
        }
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
    }
}
```

**Enum `DepotTier`** — пять вариантов с полями `{metal, materialSerializedName, mapColor, level}`:
```java
enum DepotTier {
    WROUGHT_IRON (Metal.WROUGHT_IRON, "wrought_iron", MapColor.METAL,        3),
    STEEL        (Metal.STEEL,        "steel",        MapColor.COLOR_LIGHT_GRAY, 4),
    BLACK_STEEL  (Metal.BLACK_STEEL,  "black_steel",  MapColor.COLOR_BLACK,  5),
    BLUE_STEEL   (Metal.BLUE_STEEL,   "blue_steel",   MapColor.COLOR_BLUE,   6),
    RED_STEEL    (Metal.RED_STEEL,    "red_steel",    MapColor.COLOR_RED,    6);
    ...
}
```

(Металлы из `net.dries007.tfc.util.Metal` — у них есть `getSerializedName()` и `mapColor()`.)

### `WeldingDepotCapabilities`

По образцу `HeaterCapabilities.java`:
```java
public class WeldingDepotCapabilities {
    public static void register(IEventBus bus) {
        bus.addListener(WeldingDepotCapabilities::onRegister);
    }

    private static void onRegister(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
            WeldingDepotRegistration.WELDING_DEPOT_BE.get(),
            (be, side) -> be.getExternalHandler());
    }
}
```

Изменение в `WeldingDepotRegistration.register()`:
```java
public static void register(IEventBus bus) {
    BLOCKS.register(bus);
    ITEMS.register(bus);
    BLOCK_ENTITY_TYPES.register(bus);
    WeldingDepotCapabilities.register(bus);
}
```

### Рендерер (клиентский)

`WeldingDepotBlockEntityRenderer extends SafeBlockEntityRenderer<WeldingDepotBlockEntity>`:
- Получить 4 `ItemStack` через `entity.getInventory().getStackInSlot(SLOT_LEFT/RIGHT/FLUX/OUTPUT)`.
- `poseStack.translate(0.5, 15/16.0, 0.5)`.
- 4 draw-call'а в 2 ряда:
  - **Передний ряд** (translate Z = +0.18, потом X):
    - LEFT: translate X = -0.25
    - FLUX: translate X = 0
    - RIGHT: translate X = +0.25
  - **Задний ряд** (translate Z = -0.18):
    - OUTPUT: translate X = 0
- Каждый: `BakedModel` через `Minecraft.getInstance().getItemRenderer().getModel(stack, ...)` → `ItemRenderer.render(...)`.
- Лёгкая орбитальная анимация (как у Create depot, `Mth.lerp` по partialTicks) — для красоты, не критично.

Регистрация:
```java
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD, modid = TFCAeronautics.MOD_ID)
public class WeldingDepotClientRegistration {
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(WeldingDepotRegistration.WELDING_DEPOT_BE.get(),
            WeldingDepotBlockEntityRenderer::new);
    }
}
```

### Модели

**Родитель** (`models/block/welding_depot/base.json`) — структура как у Create depot, но **.bbmodel от пользователя**. До получения `.bbmodel` файл содержит заглушку из 2 элементов с текстурами `#side`, `#top`, `#casing` по образцу `models/block/stamping_press.json`.

**5 children** — каждый `{ "parent": ".../welding_depot/base", "textures": { "top": ".../top_<material>" } }` по образцу `models/block/metal/anvil/high_carbon_steel.json`.

**5 item-models** — `{ "parent": ".../block/welding_depot/<material>" }`.

**5 blockstates** — `{ "variants": { "": { "model": ".../block/welding_depot/<material>" } } }`.

### Рецепты блоков

5 файлов в `src/main/resources/data/tfc_aeronautics/recipe/crafting/welding_depot/<material>.json`:
```json
{
  "type": "minecraft:crafting_shaped",
  "pattern": ["III", " C "],
  "key": {
    "I": { "item": "tfc:metal/double_ingot/<material>" },
    "C": { "item": "create:andesite_casing" }
  },
  "result": { "count": 1, "id": "tfc_aeronautics:metal/welding_depot/<material>" },
  "show_notification": false
}
```

(`show_notification: false` — конвенция мода для override-рецептов, см. CLAUDE.md.)

---

## Файлы, которые читаем перед редактированием

| Файл | Зачем |
|---|---|
| `src/main/java/ru/tfc_aeronautics/heater/HeaterBlock.java` | Шаблон для `WeldingDepotBlock` (без rotation/facing — упрощённый вариант) |
| `src/main/java/ru/tfc_aeronautics/heater/HeaterBlockEntity.java` | Шаблон для SmartBlockEntity + ItemStackHandler |
| `src/main/java/ru/tfc_aeronautics/heater/HeaterRegistration.java` | Шаблон для 3-`DeferredRegister` структуры |
| `src/main/java/ru/tfc_aeronautics/heater/HeaterCapabilities.java` | Шаблон для `RegisterCapabilitiesEvent` |
| `src/main/java/ru/tfc_aeronautics/anvil/AnvilRegistration.java` | Шаблон для per-material цикла + tier как конструкторный аргумент |
| `src/main/java/ru/tfc_aeronautics/stamping_press/StampingPressBlockEntity.java:154-184` | Шаблон проверки heat через `HeatCapability.get(...).canWeld()` |
| `src/main/java/ru/tfc_aeronautics/CreativeTabs.java` | Место для добавления 5 строк в `displayItems` |
| `src/main/resources/assets/tfc_aeronautics/models/block/metal/anvil/high_carbon_steel.json` | Шаблон per-material child модели |
| `src/main/resources/assets/tfc_aeronautics/models/block/stamping_press.json` | Шаблон multi-texture модели (side + top + casing) |
| `src/main/resources/assets/tfc_aeronautics/lang/en_us.json:36-55` | Шаблон per-material ключей |
| `src/main/resources/assets/tfc_aeronautics/lang/ru_ru.json:37-52` | Шаблон per-material ключей |
| `code_references/TerraFirmaCraft/src/main/java/net/dries007/tfc/common/blockentities/AnvilBlockEntity.java:439-491` | Референс TFC welding логики |
| `code_references/Create/src/main/java/com/simibubi/create/content/kinetics/press/PressingBehaviour.java:41-42` | Поля `running`, `runningTicks`, `CYCLE` |
| `code_references/Create/src/main/java/com/simibubi/create/content/kinetics/press/MechanicalPressBlockEntity.java:78-80` | Геттер `getPressingBehaviour()` |
| `code_references/TerraFirmaCraft/src/main/java/net/dries007/tfc/common/recipes/WeldingRecipe.java` | `matches`, `assemble`, `isCorrectTier` |
| `code_references/TerraFirmaCraft/src/main/java/net/dries007/tfc/util/Metal.java:216-219` | Реальные tier-значения металлов |

---

## Порядок реализации

1. **Java-каркас (без рендера):**
   1. `WeldingDepotBlock.java`
   2. `WeldingDepotBlockEntity.java` (с `tryWeld()`)
   3. `DepotTier.java` (enum)
   4. `WeldingDepotRegistration.java`
   5. `WeldingDepotCapabilities.java`
   6. Подключить в `TFCAeronautics.java` + `CreativeTabs.java`
2. **Проверить компиляцию:** `./gradlew compileJava compileClientJava`. Ожидаемо чисто.
3. **Ассеты моделей (после получения `.bbmodel` от пользователя):**
   1. `models/block/welding_depot/base.json` (родитель)
   2. 5 child-моделей
   3. 5 item-моделей
   4. 5 blockstate-файлов
   5. 6 текстур (1 `side`, 1 `casing`, 5 `top_*`)
4. **Lang:**
   1. 5 строк в `en_us.json`
   2. 5 строк в `ru_ru.json`
5. **Рецепты блоков:**
   1. 5 файлов в `data/tfc_aeronautics/recipe/crafting/welding_depot/`
6. **Клиентский рендер:**
   1. `WeldingDepotBlockEntityRenderer.java`
   2. `WeldingDepotClientRegistration.java`
7. **Документация:**
   1. Раздел в `DOCS.md`
   2. Отметка в `ROADMAP.md`
8. **Финальная проверка:** `./gradlew build runData`. Должны сгенерироваться datapack-ассеты и пройти компиляция без warnings.

---

## Verification (как проверить до сдачи)

По правилам `CLAUDE.md` рантайм-проверка через `./gradlew runClient` запрещена. Делаем так:

1. **Статика:** `./gradlew compileJava compileClientJava` — должен пройти без ошибок/warnings про mixin/applies.
2. **Datagen:** `./gradlew runData` — должен пройти. Проверить, что в `run/datapacks/` появились 5 рецептов блоков.
3. **Билд:** `./gradlew build` — должен пройти. Проверить, что в JAR появились все модели/текстуры/lang-ключи.
4. **Recipe overrides convention:** убедиться, что `show_notification: false` стоит во всех 5 crafting-рецептах (по CLAUDE.md).
5. **Capability:** убедиться через чтение кода, что `IItemHandler` зарегистрирован на всех 5 вариантах блока (через общий `BlockEntityType`).
6. **Smoke-test через статический чек:** прочитать `WeldingDepotBlockEntity.tryWeld()` и убедиться:
   - Проверка tier'а: `recipe.isCorrectTier(getTier())`.
   - Проверка heat: `HeatCapability.get(stack).canWeld()`.
   - Проверка flux: только `tfc:powder/flux` через `TFCTags.Items.WELDING_FLUX`.
   - Запись: SLOT_OUTPUT ← result, SLOT_LEFT/RIGHT ← EMPTY, SLOT_FLUX.shrink(1).
   - Гейт: если SLOT_OUTPUT непустой — сварка не выполняется (не теряем предыдущий результат).
   - Heat copy: `resultHeat.setTemperatureIfWarmer(leftHeat/rightHeat)`.
7. **Проверка разделения insert/extract:**
   - Прочитать `WeldingDepotItemHandler` и убедиться: `insertItem(slot, ...)` возвращает `stack` неизменённым для `slot == SLOT_OUTPUT`, `extractItem(slot, ...)` возвращает `EMPTY` для `slot ∈ {LEFT, RIGHT, FLUX}`.
   - Это гарантирует, что воронки/шлюзы/хопперы физически не могут забрать ни флюс, ни входные детали.

Пользователь проверяет в Prism-лаунчере: ставит механический пресс над депо, кладёт 2 горячие детали + флюс, ждёт цикл пресса — должен получить результат рецепта в SLOT_OUTPUT.

---

## Открытые вопросы / что блокирует

1. **`.bbmodel` ещё не получен.** До него модели в плане описаны структурно (parent + 5 children), но конкретные элементы и UV будут заполнены по модели пользователя. Если `.bbmodel` отложен надолго — реализуем всё кроме ассетов моделей/текстур; блок регистрируется, но без визуала.
2. **Welding-рецепты мода (`drill_head_cast_iron` tier 3, `drill_head_steel` tier 4):** они уже работают с TFC-наковальнями; подтвердили, что депо их тоже подхватит автоматически (recipe manager не знает, откуда запрос). Никаких правок не нужно.
3. **Custom heat definition для депо как блока:** не нужен — депо не item.
4. **Дополнительные welding-рецепты, специфичные для депо:** вне scope этой задачи. Если в будущем понадобятся, добавляются по тому же пути `data/tfc/recipe/welding/<name>.json`.
5. **Дроп предметов при краше блока:** уже реализовано через `onRemove` (см. HeaterBlock.java:124-132).

---

## Потенциальные баги и точки для перепроверки

### Логика сварки (`tryWeld`)

1. **Tier проверка:** убедиться, что `recipe.isCorrectTier(getTier())` сравнивает `recipe.tier <= depot.tier` (не строго равно). Иначе steel-tier депо не сможет выполнить tier-3 recipe. Референс: `WeldingRecipe.isCorrectTier` в code_references — `return anvilTier >= tier`.

2. **Heat capability на предметах без неё:** `HeatCapability.get(stack)` возвращает `@Nullable IHeat`. Если предмет не имеет heat-компонента (например, мод-ный предмет), `null` означает "нет требования к температуре". В коде TFC это проверяется как `(leftHeat != null && !leftHeat.canWeld())`. Наш код должен делать то же: если heat == null → разрешаем сварку. **При реализации использовать null-safe вариант, а не голый `heat.canWeld()`.**

3. **ItemStack.copy() для heat:** после `recipe.assemble(inv)` результат — свежий `ItemStack` без heat. Установка температуры через `resultHeat.setTemperatureIfWarmer(leftHeat)` правильно копирует, но если у результата нет heat capability (не зарегистрирован в `data/tfc_aeronautics/tfc/item_heat/`), `resultHeat == null` — пропускаем без потерь.

4. **SLOT_OUTPUT занят:** если предыдущий результат ещё не извлечён воронкой, новая сварка не должна его перезаписать. Уже добавлен гейт `if (!out.isEmpty()) return`. Серверный тик синхронный для одного BlockEntity — гонок между `tick()` и `extractItem` не будет.

5. **Recipe lookup с двумя пустыми слотами:** если LEFT и RIGHT оба пустые — ранний return уже есть. Убедиться, что flux тоже проверяется (`flux.isEmpty() → return`).

### Детекция пресса

6. **Гонка `runningTicks == CYCLE/2`:** `PressingBehaviour.CYCLE == 40`, момент — тик 20, длится один серверный тик. Если наш `tick()` пропустил этот момент из-за рассинхронизации с `PressingBehaviour.tick()` — сварка не сработает. Если пропуски замечены — добавить `lastPressedTick` и условие `runningTicks in [CYCLE/2 - 1, CYCLE/2]`.

7. **`pb.running == false`:** обязательная проверка перед `runningTicks`, иначе будет `false == 20` и варка никогда не сработает.

8. **Create API доступность:** `MechanicalPressBlockEntity.getPressingBehaviour()` и `PressingBehaviour.CYCLE` — публичные поля. Если Create изменит сигнатуру (6.x → 7.x), код упадёт. Проверить, что в `gradle.properties` зафиксирована версия Create.

9. **`StampingPressBlock`:** собственный StampingPress мода не наследует `MechanicalPressBlock`, поэтому `instanceof MechanicalPressBlockEntity` для него вернёт false. Если пользователь захочет использовать свой пресс — добавить `instanceof StampingPressBlockEntity` дополнительно (но это вне текущей спецификации).

### BlockEntity и инвентарь

10. **NBT round-trip:** `inventory.serializeNBT()` / `deserializeNBT()` — стандартный путь. Проверить, что CompoundTag сохраняет все 4 слота, и что после перезагрузки мира `inventory.getStackInSlot(SLOT_OUTPUT)` не пустой (не потеряли результат).

11. **`onRemove` дроп:** убедиться, что дропаем все 4 слота, включая OUTPUT (даже если результат не извлечён воронкой — игрок должен иметь возможность забрать).

12. **`notifyUpdate()` vs `setChanged()`:** `tryWeld()` вызывает `setChanged()` — это OK для server-side persistence. Для визуального обновления рендерера ещё нужен `blockEntity.sendData()` (через `notifyUpdate()`). Без этого клиент не увидит результат до переподключения. **Добавить `notifyUpdate()` после `setChanged()` в `tryWeld`.**

13. **Capability на разные стороны:** Create-шлюзы запрашивают capability с разными `Direction`. Проверить, что наш capability provider не зависит от `side` (или зарегистрирован для всех сторон одинаково). Текущий план: `(be, side) -> be.getExternalHandler()` — side игнорируется, должно работать для всех сторон.

14. **`ItemStackHandler` thread safety:** NeoForge предполагает, что все манипуляции с `ItemStackHandler` происходят на server thread. Серверный тик однопоточный между BlockEntity — OK.

### Автоподбор сверху (`fallOn`)

15. **Только сверху:** `fallOn` вызывается когда entity приземляется на **верхнюю грань**. Если игрок бросает предмет **сбоку** (через стекло), `fallOn` тоже сработает — но это поведение create:depot, обычно нормально. Проверить, что entity, летящие горизонтально, не подбираются (они обычно не вызывают `fallOn`).

16. **Частичная вставка:** если в слотах нет места для всех предметов из ItemEntity, оставшиеся должны остаться в ItemEntity. Реализация через `insertItem(slot, stack, false)` возвращает `remainder` — его кладём обратно через `itemEntity.setItem(remainder)`.

17. **`itemEntity.discard()` vs `setItem(EMPTY)`:** если вся стопка вставлена — `setItem(ItemStack.EMPTY)` достаточно, discard вызовется автоматически. Не вызывать `discard()` явно без проверки.

18. **Игрок падает на стол:** `fallOn` вызывается и для игроков. Проверить `entity instanceof ItemEntity` иначе вызывать `super.fallOn(...)` для стандартного fall damage. Реализация: `if (entity instanceof ItemEntity ie) {...} else { super.fallOn(...); }`.

### Ручной extract по приоритету (ПКМ пустой рукой)

19. **Приоритет OUTPUT → LEFT → RIGHT → FLUX:** код перебирает слоты в этом порядке и для каждого вызывает `inventory.extractItem(slot, 1, false)`. Если результат непустой — отдать игроку и выйти. Убедиться, что итерация не продолжается после успешного extract.

20. **Только один предмет за клик:** чтобы не дать игроку случайно забрать 4 предмета одним кликом. Если игрок хочет всё — несколько кликов.

21. **Ручной extract идёт мимо `WeldingDepotItemHandler`:** мы вызываем `inventory.extractItem(...)` напрямую, а не через capability. Это правильно — ручной extract из LEFT/RIGHT/FLUX должен работать (в отличие от логистики). `WeldingDepotItemHandler` блокирует extract из этих слотов только для внешних систем (воронки/хопперы/шлюзы).

22. **Shift+ПКМ:** сейчас ведёт себя так же, как обычный ПКМ (один предмет). Альтернатива — extract всех 4 слотов подряд. Выбираем вариант «один предмет» для простоты.

### Capability и логистика

23. **`isItemValid` для SLOT_OUTPUT=false:** важно убедиться, что воронки **физически не могут** вставить предмет в OUTPUT. Наша обёртка `WeldingDepotItemHandler.insertItem` для `slot == SLOT_OUTPUT` возвращает `stack` неизменённым — правильно.

24. **`getSlotLimit(3)`:** стандартный ItemStackHandler возвращает 64. Для OUTPUT это нормально (1 результат, редко стопка).

25. **Хоппер Minecraft vs воронка Create:** оба используют `Capabilities.ItemHandler.BLOCK`. Наша обёртка работает для обоих. Minecraft-хоппер может вставить в любой слот по индексу — но `insertItem` всё равно отказывает для OUTPUT. ✓

26. **Create Chute:** убедиться, что Chute не пытается вытащить предмет, который не подходит (например, флюс). Create проверяет `ItemStack.matches()` или подобное — должно работать корректно, но проверить вручную.

### Рендерер

27. **Flywheel:** если Flywheel активен, vanilla BER может не вызываться по умолчанию (см. memory: `feedback_flywheel_skip_vanilla_render.md`). Если BER пропускается — вызвать `.neverSkipVanillaRender()` на visual builder (но это не применимо к нашему случаю, т.к. мы не используем Flywheel visual напрямую; BER наш собственный).

28. **4 предмета — производительность:** 4 draw-call'а на блок, 20 Гц — не страшно. Но на серверах с тысячами депо может стать узким местом. Решение: перерисовывать только при изменении инвентаря (через `entity.sendData()` — клиент уже сам тригерится).

### Рецепты блоков

29. **`tfc:metal/double_ingot/<material>`:** проверить, что TFC имеет `double_ingot` для всех 5 материалов: wrought_iron, steel, black_steel, blue_steel, red_steel — все есть.

30. **`create:andesite_casing`:** стандартный предмет Create, есть в любой версии.

31. **`show_notification: false`:** конвенция мода для override-рецептов. Применить ко всем 5 crafting-рецептам.

### Локализация

32. **Переводы на русский:** для `red_steel` — "красная сталь", `blue_steel` — "синяя сталь", `black_steel` — "чёрная сталь", `steel` — "сталь", `wrought_iron` — "кованое железо". По аналогии с `block.tfc_aeronautics.metal.anvil.*` в `ru_ru.json`.

### Blockstate / рендер

33. **Hitbox:** стандартный `Block.shape` — полный куб. Если модель имеет вырезы (как у create:depot), нужен кастомный `getShape`. После получения `.bbmodel` — заполнить.

34. **`noOcclusion()`:** по memory `project_no_occlusion_pattern.md` — все блоки с прозрачной/cutout геометрией должны иметь `.noOcclusion()`. Сварочный стол, скорее всего, solid (без прозрачности), но проверить по bbmodel.

35. **Звук:** `Block.sound(SoundType.METAL)` или `SoundType.ANVIL` — решить после bbmodel.
