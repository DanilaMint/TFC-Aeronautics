# `tfc_aeronautics:chain_conveyor` — план реализации

## Контекст

`create:chain_conveyor` в Create 6.0.10 жёстко зашит на `Items.CHAIN` в пяти местах Java-кода — никакие теги или datapack-shadow не помогают. TFC поставляет 9 металлических цепей (`tfc:metal/chain/{bismuth_bronze,black_bronze,bronze,copper,wrought_iron,steel,black_steel,blue_steel,red_steel}`), все они объединены в `c:chains`. Игрок не может построить конвейер из стальной цепи.

Предыдущая попытка (mixin-подход, спека `docs/superpowers/specs/2026-08-16-chain-conveyor-tfc-chains-design.md`) упала в рантайме: `InvalidMixinException ... contains non-private static method aeronautics$renderChainWithTexture` — статический `@Overwrite` не позволяет добавить параметр в `renderChain`, не сделав метод приватным через `@Shadow`. См. `crashlog.txt:343`, `latest.log:362`.

Принятое решение: полный source-copy из `code_references/Create/src/main/java/com/simibubi/create/content/kinetics/chainConveyor/` в наш неймспейс с короткими именами (как в остальном моде — `StampingPressBlock`, `HeaterBlock`). Без mixin-ов, без обратной совместимости со старыми `create:chain_conveyor` (см. раздел «Риски»).

## Принятые решения

- **Old-world `create:chain_conveyor`**: не трогаем — продолжают работать как есть, образуют отдельную сеть (не соединяются с нашими).
- **Скрытие старого блока**: `BANNED_RECIPES` + удаление из creative-таба `create:base`.
- **Per-segment текстуры**: цепь каждого металла рисуется через `tfc:item/metal/chain/<metal>` (ванильная цепь — `minecraft:block/chain`).
- **Item id нового блока**: `tfc_aeronautics:chain_conveyor`.

## Подход

Полное копирование 16 Java-файлов из Create в наш пакет `ru.tfc_aeronautics.chain` с короткими именами, с точечной модификацией мест, где упоминается `Items.CHAIN`/`Blocks.CHAIN` — на per-connection `ResourceLocation` хранилищем.

### Что копируется

Класс Create → наша копия (`src/main/java/ru/tfc_aeronautics/chain/` для общих, `src/client/java/ru/aeronautics/client/chain/` для клиента). Правки:

| Источник `com/simibubi/create/content/kinetics/chainConveyor/` | Наша копия | Правки |
|---|---|---|
| `ChainConveyorBlock.java` | `ChainConveyorBlock.java` | `Items.CHAIN` → тег `c:chains`; refund в `onSneakWrenched` → через BE lookup; ссылки на `AllBlocks.CHAIN_CONVEYOR`/`AllBlockEntityTypes.CHAIN_CONVEYOR` → наши |
| `ChainConveyorBlockEntity.java` | `ChainConveyorBlockEntity.java` | Новое поле `Map<BlockPos, ResourceLocation> connectionChains` (см. §1); все `Items.CHAIN`/`Blocks.CHAIN` → через хелперы |
| `ChainConveyorConnectionHandler.java` | `ChainConveyorConnectionHandler.java` | `AllBlocks.CHAIN_CONVEYOR.has(...)` → `instanceof ChainConveyorBlock` (наш); `isChain` → тег `c:chains`; `instanceof ChainConveyorBlockEntity` → наш тип |
| `ChainConveyorConnectionPacket.java` | `ChainConveyorConnectionPacket.java` | Регистрируется под нашим packet id; refund через per-connection type |
| `ChainConveyorInteractionHandler.java` | `ChainConveyorInteractionHandler.java` | Только замена `instanceof` и перенос `loadedChains` статика |
| `ChainConveyorPackage.java` | `ChainConveyorPackage.java` | Внутренняя ссылка на BE тип → наш |
| `ChainConveyorRenderer.java` | `client/chain/ChainConveyorRenderer.java` | `RenderTypes.chain(CHAIN_LOCATION)` → через `be.getChainTextureForConnection(localPos)` (см. §3) |
| `ChainConveyorRidingHandler.java` | `client/chain/ChainConveyorRidingHandler.java` | Только `instanceof` |
| `ChainConveyorRoutingTable.java` | `ChainConveyorRoutingTable.java` | Без правок |
| `ChainConveyorShape.java` | `ChainConveyorShape.java` | Без правок |
| `ChainConveyorVisual.java` | `client/chain/ChainConveyorVisual.java` | Только `instanceof` |
| `ChainPackageInteractionHandler.java` | `client/chain/ChainPackageInteractionHandler.java` | Только `instanceof` |
| `ChainPackageInteractionPacket.java` | `ChainPackageInteractionPacket.java` | Наш packet id; тип BE → наш |
| `ClientboundChainConveyorRidingPacket.java` | `client/chain/ClientboundChainConveyorRidingPacket.java` | Только packet id |
| `ServerboundChainConveyorRidingPacket.java` | `ServerboundChainConveyorRidingPacket.java` | Только packet id |
| `ServerChainConveyorHandler.java` | `ServerChainConveyorHandler.java` | Замена ссылок на наши packet-классы |

Классы Create (`com.simibubi.create.content.kinetics.chainConveyor.*`) импортировать нельзя. Если внутри скопированного класса нужен Create-класс (например, `KineticBlock`, `KineticBlockEntity`, `AllTags`, `AllPartialModels`, `FrogportBlockEntity`) — это публичный API Create, импортируем свободно.

**Frogport-интеграция (2 дополнительных файла):**

| Источник | Наша копия | Назначение |
|---|---|---|
| (нет — новое) | `chain/ChainConveyorFrogportTarget.java` | Подкласс `com.simibubi.create.content.logistics.packagePort.PackagePortTarget`. Копируем логику тела `ChainConveyorFrogportTarget` из `code_references/Create/.../PackagePortTarget.java:69-203` (CODEC, STREAM_CODEC, поля `chainPos`/`connection`/`flipped`, методы `setup`/`getIcon`/`export`/`register`/`deregister`/`getExactTargetLocation`/`canSupport`/`getType` + вложенный `Type`). Замены: импорты `ChainConveyorBlockEntity`/`ChainConveyorPackage` на наши; `getIcon()` возвращает наш блок; `AllBlocks.CHAIN_CONVEYOR` → `ChainConveyorRegistration.CHAIN_CONVEYOR.get()`. |
| (нет — новое) | `chain/ChainConveyorPackagePortTargets.java` | Аналог `AllPackagePortTargetTypes` (`code_references/Create/.../packagePort/AllPackagePortTargetTypes.java`). Использует `DeferredRegister.create(CreateRegistries.PACKAGE_PORT_TARGET_TYPE, "tfc_aeronautics")` — `CreateRegistries.PACKAGE_PORT_TARGET_TYPE` публичен (см. `code_references/Create/.../api/registry/CreateRegistries.java:36`), регистрирует entry `tfc_aeronautics:tfc_chain_conveyor` под `ChainConveyorFrogportTarget.Type::new`. **Ключ реестра — `tfc_chain_conveyor`, не `chain_conveyor`**: совпадение `path` под разными неймспейсами (`create:chain_conveyor` и `tfc_aeronautics:chain_conveyor`) роняет сериализацию `PackagePortTarget` через `ByteBufCodecs.registry(...).dispatch(getType, streamCodec)` с `ClassCastException` (`debug/disconnect-2026-08-16_23.23.50-client.txt:39,48`) — наш экземпляр пытается привестись к внутреннему классу Create. Вызов `register(IEventBus)` из `ChainConveyorRegistration.register()`. |

`canSupport(BlockEntity be)` возвращает `AllBlockEntityTypes.PACKAGE_FROGPORT.is(be)` — это Create-класс, импортируется свободно. Само `getIcon()` для нашего блока — `new ItemStack(ChainConveyorRegistration.CHAIN_CONVEYOR.get())`.

**Откуда стрелять пакетами в фрогпорт?** `ChainConveyorBlockEntity.tick()` (строки 295-313 у Create) уже обходит `connections` и для каждого соседа проверяет `instanceof FrogportBlockEntity ppbe` → `ppbe.startAnimation(box.item, false)`. Поскольку мы копируем BE verbatim (только ренейм), эта логика работает без правок, как только наш BE импортирует `com.simibubi.create.content.logistics.packagePort.frogport.FrogportBlockEntity`.

Дополнительно создаём:
- `src/main/java/ru/tfc_aeronautics/chain/ChainConveyorRegistration.java` — наши `DeferredRegister`-ы. Зеркалит `heater/HeaterRegistration.java:28-67`. Внутри `register(IEventBus)` дополнительно вызывает `ChainConveyorPackagePortTargets.register(bus)`.
- `src/main/java/ru/tfc_aeronautics/chain/ChainConveyorPackets.java` — регистрация 5 payload-ов через `CatnipServices.NETWORK` под id `tfc_aeronautics:chain_conveyor_connect/_riding/_package/_clientbound`.
- `src/client/java/ru/aeronautics/client/chain/ChainConveyorClientSetup.java` — `RegisterRenderersEvent` для BER.
- `src/client/java/ru/aeronautics/client/ChainConveyorCreativeTabFilter.java` — подписчик `BuildCreativeModeTabContentsEvent`, удаляет `create:chain_conveyor` из `create:base`.

### Регистрация — `chain/ChainConveyorRegistration.java`

Зеркалит `heater/HeaterRegistration.java`:
- 3 `DeferredRegister`: `BLOCKS`, `ITEMS`, `BLOCK_ENTITY_TYPES`.
- `CHAIN_CONVEYOR` = `BLOCKS.register("chain_conveyor", ...)` (наш `KineticBlock`).
- `CHAIN_CONVEYOR_ITEM` = `ITEMS.register(...)` (`BlockItem`).
- `CHAIN_CONVEYOR_BE` = `BLOCK_ENTITY_TYPES.register(...)` с `BlockEntityType.Builder.of(ChainConveyorBlockEntity::new, CHAIN_CONVEYOR.get())`.
- `public static void register(IEventBus bus)` — единая точка входа.

Добавляется в `TFCAeronautics.java:39` новой строкой `ChainConveyorRegistration.register(modEventBus);` рядом с другими вызовами `.register(...)).

Также добавить `output.accept(ChainConveyorRegistration.CHAIN_CONVEYOR_ITEM.get());` в `CreativeTabs.displayItems`.

## Критичные куски дизайна

### 1. Per-connection chain map в BE

**Файл:** `ChainConveyorBlockEntity.java`

```java
// Зеркалит `connections: Set<BlockPos>`
public Map<BlockPos, ResourceLocation> connectionChains = new HashMap<>();
```

**NBT (write/read)**: рядом с уже существующим блоком `compound.put("Connections", ...)` (строки 683-700 у Create) добавить:
```java
compound.put("ConnectionChains", NBTHelper.writeCompoundList(connectionChains.entrySet(), entry -> {
    CompoundTag t = new CompoundTag();
    t.put("Pos", NbtUtils.writeBlockPos(entry.getKey()));
    t.putString("Chain", entry.getValue().toString());
    return t;
}));
```

При чтении (после декода `connections` — строки 703-723 у Create) — параллельный `iterateCompoundList` + `connectionChains.put(NBTHelper.readBlockPos(c, "Pos"), ResourceLocation.parse(c.getString("Chain")))`.

**Миграция**: если у существующего соединения нет ключа в `connectionChains` (старый мир), дотягиваем `Items.CHAIN.getKey()` — fallback на ваниль.

**`addConnectionTo(BlockPos target, ResourceLocation chainItemId)`** — добавляем второй аргумент; внутри `connectionChains.put(localTarget, chainItemId)` сразу после `connections.add(...)`.

**`removeConnectionTo(BlockPos target)`** — после `connectionStats.remove(localTarget)` добавить `connectionChains.remove(localTarget)`.

**`transform(BlockEntity, StructureTransform)`** (контрапция) — перестроить map вместе с `connections`.

### 2. Хелперы на BE для drops/refund/текстуры

```java
public Item getChainItemForConnection(BlockPos localTarget) {
    ResourceLocation rl = connectionChains.getOrDefault(localTarget, Items.CHAIN.getKey());
    return BuiltInRegistries.ITEM.get(rl);
}

public ResourceLocation getChainTextureForConnection(BlockPos localTarget) {
    ResourceLocation rl = connectionChains.getOrDefault(localTarget, Items.CHAIN.getKey());
    if (rl.getNamespace().equals("minecraft")) {
        return ResourceLocation.withDefaultNamespace("block/chain"); // minecraft:textures/block/chain.png
    }
    // TFC: tfc:metal/chain/<metal> → tfc:item/metal/chain/<metal>
    String last = rl.getPath().substring(rl.getPath().lastIndexOf('/') + 1);
    return ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), "item/metal/chain/" + last);
}
```

`chainDestroyed(BlockPos, boolean, boolean)` (строки 524-540 у Create): оба места `new ItemStack(Items.CHAIN)` и `new ItemStack(Blocks.CHAIN.asItem(), ...)` → `new ItemStack(getChainItemForConnection(target), ...)`.

`sound` в `ChainConveyorConnectionHandler.validateAndConnect` (строка 108): `Blocks.CHAIN.defaultBlockState().getSoundType()` → оставляем ванильный звук для всех цепей (TFC цепи не имеют block-формы).

### 3. Texture plumb в renderer

`ChainConveyorRenderer.renderChains` (аналог строки 134-195 у Create): в цикле `for (BlockPos blockPos : ...)` перед вызовом `renderChain(...)`:
```java
ResourceLocation chainTex = be.getChainTextureForConnection(blockPos);
renderChain(ms, buffer, animation, stats.chainLength(), light1, light2, far, chainTex);
```

`renderChain` (строка 197 у Create) получает дополнительный параметр `ResourceLocation chainTex`; внутри на строке 208 `RenderTypes.chain(CHAIN_LOCATION)` → `RenderTypes.chain(chainTex)`.

`renderPart`/`renderQuad`/`addVertex` не трогаем: UV передаются через `VertexConsumer`, текстура привязана к `RenderType`.

`ChainConveyorVisual.setupGuards` — только замена `instanceof ChainConveyorBlockEntity` → `ChainConveyorBlockEntity` (наш).

### 4. Creative-tab скрытие

Create регистрирует таб как `BASE_CREATIVE_TAB = REGISTER.register("base", ...)` (`code_references/Create/AllCreativeModeTabs.java:64`), значит ResourceKey — `ResourceLocation.fromNamespaceAndPath("create", "base")`.

Подписчик `src/client/java/ru/aeronautics/client/ChainConveyorCreativeTabFilter.java` на `BuildCreativeModeTabContentsEvent`:
```java
@SubscribeEvent
public static void onBuildContents(BuildCreativeModeTabContentsEvent event) {
    if (!event.getTabKey().equals(
            ResourceKey.create(Registries.CREATIVE_MODE_TAB,
                ResourceLocation.fromNamespaceAndPath("create", "base")))) return;
    event.getEntries().removeIf(entry ->
        entry.getKey() != null && entry.getKey().location()
              .equals(ResourceLocation.fromNamespaceAndPath("create", "chain_conveyor")));
}
```

### 5. Hide + recipe

В `src/main/java/ru/tfc_aeronautics/recipe/RecipeRemoval.java:44-48` дописать:
```java
ResourceLocation.fromNamespaceAndPath("create", "crafting/kinetics/chain_conveyor"),
```

Новый файл `src/main/resources/data/tfc_aeronautics/recipe/crafting/kinetics/chain_conveyor.json` — shaped crafting как у Create, но output:
```json
{ "type": "minecraft:crafting_shaped",
  "category": "misc",
  "key": { "A": { "item": "create:large_cogwheel" },
            "C": { "item": "create:andesite_casing" } },
  "pattern": [ " C ", "CAC", " C " ],
  "result": { "count": 2, "id": "tfc_aeronautics:chain_conveyor" } }
```

Положим под наш неймспейс (`tfc_aeronautics/recipe/...`), не под `create/...` — потому что recipe-id должен отличаться от забаненного `create:crafting/kinetics/chain_conveyor`. Память проекта: переопределения — в исходном неймспейсе; здесь не переопределение, а новый recipe.

Дополнительно: `data/tfc_aeronautics/advancement/recipes/misc/crafting/kinetics/chain_conveyor.json` (стандартная recipe-unlock запись).

## Что модифицируется в существующих файлах

| Файл | Изменение |
|---|---|
| `src/main/java/ru/tfc_aeronautics/TFCAeronautics.java` | Добавить `ChainConveyorRegistration.register(modEventBus);` |
| `src/main/java/ru/tfc_aeronautics/CreativeTabs.java` | Добавить `output.accept(ChainConveyorRegistration.CHAIN_CONVEYOR_ITEM.get());` в `displayItems` |
| `src/main/java/ru/tfc_aeronautics/recipe/RecipeRemoval.java` | Добавить `chain_conveyor` recipe id в `BANNED_RECIPES` |
| `src/main/resources/assets/tfc_aeronautics/lang/en_us.json` | `"block.tfc_aeronautics.chain_conveyor": "Chain Conveyor"` + ключи ошибок подключения |
| `src/main/resources/assets/tfc_aeronautics/lang/ru_ru.json` | Те же ключи по-русски |
| `DOCS.md` | Новый раздел "Chain Conveyor (TFC-aware)" |

## Ресурсы (создать)

- `src/main/resources/assets/tfc_aeronautics/blockstates/chain_conveyor.json` — `{ "variants": { "": { "model": "tfc_aeronautics:block/chain_conveyor/block" } } }`
- `src/main/resources/assets/tfc_aeronautics/models/item/chain_conveyor.json` — `{ "parent": "tfc_aeronautics:block/chain_conveyor/item" }`
- `src/main/resources/assets/tfc_aeronautics/models/block/chain_conveyor/{block,item,guard,shaft,wheel}.json` — каждый `{ "parent": "create:block/chain_conveyor/..." }` (Create делится моделями через регистри Create, текстуры подтянутся). Текстуры не копируем.
- `src/main/resources/data/tfc_aeronautics/loot_table/blocks/chain_conveyor.json` — пустой/само-дроп.

## Риски и миграции

| Риск | Митигация |
|---|---|
| **Старые миры с `create:chain_conveyor`**: остаются как есть, не соединяются с нашими (разные `BlockEntityType`). | Документировать в `DOCS.md`. Сети несовместимы. |
| **Frogport interop**: `PackagePortTarget` фильтрует наш тип BE → пакеты не передаются между `create:chain_conveyor` и `tfc_aeronautics:chain_conveyor`. | Решаемо через регистрацию собственного `ChainConveyorFrogportTarget` в `CreateRegistries.PACKAGE_PORT_TARGET_TYPE` (см. таблицу выше). Обе сети принимают пакеты с любого `create:frogport`, но не «склеиваются» друг с другом — разные классы BE. |
| **Контрапции**: `transform(...)` обязан переместить `connectionChains` вместе с `connections`. | §1. |
| **Packet payload регистрация**: используем `CatnipServices.NETWORK` из catnip-platform (публичный API из Create-инфраструктуры). Если при компиле недоступен — fallback на vanilla `PayloadTypeRegistry.playS2C()` + `PlayNetworking.registerGlobalReceiver()`. | Сначала через catnip; при необходимости — fallback. |
| **NBT старых миров без `ConnectionChains`**: соединения в `connections` есть, а в `connectionChains` — нет. | Lazy fallback: в `read(...)` инжектим `Items.CHAIN.getKey()` для всех `localPos` из `connections`, отсутствующих в `connectionChains`. |
| **Звук для TFC цепей**: `Blocks.CHAIN.defaultBlockState().getSoundType()` отвалится для не-ванильных цепей. | Использовать ванильный звук для всех цепей. |
| **Visual particles**: `BlockParticleOption(ParticleTypes.BLOCK, ...)` нуждается в `BlockState`, у TFC item его нет. | Оставляем ванильный `Blocks.CHAIN.defaultBlockState()` для любых цепей (частицы декоративные). |
| **Sync клиент-сервер**: `connectionChains` добавляется в `write(...)`/`read(...)` (client packet path). Проверить, что и в `writeSafe` есть, иначе клиент получит неполные данные при стрим-пакете. | Добавить в оба блока сериализации. |
| **`Items.CHAIN`/`Blocks.CHAIN` остатки** в копиях. | Проверить grep'ом в `src/main/java/ru/tfc_aeronautics/chain/` и `src/client/java/.../chain/` перед компилом. |
| **`ChainConveyorBlock` коллизия имён с Create**. | Мы не импортируем Create-класс `ChainConveyorBlock` нигде в нашем коде (только свободные базы — `KineticBlock`, `AllPartialModels` и т.п.). Если потребуется одновременно — через полный FQN. |
| **Уникальность ключей в общих реестрах Create** (урок из `debug/disconnect-2026-08-16_23.23.50-client.txt`). | При добавлении новых записей в `CreateRegistries.*` (например, `PACKAGE_PORT_TARGET_TYPE`, `ARM_INTERACTION_POINT_TYPE`, `ITEM_ATTRIBUTE_TYPE`, `MOUNTED_*_STORAGE_TYPE`, `CONTRAPTION_TYPE`, `DISPLAY_SOURCE/TARGET`, `FAN_PROCESSING_TYPE`) **избегать ключа, который уже используется Create по тому же `path`** (даже если `DeferredRegister` сидит в нашем неймспейсе). Префикс `tfc_` на ключе делает невозможной коллизию. Без префикса диспатчер `ByteBufCodecs.registry(...).dispatch(...)` (`net/minecraft/network/codec/StreamCodec.java:91-110`) подбирает StreamCodec по `Registry.getIdOrThrow(type)` и `Registry.byIdOrThrow(id)`; при коллизии наш экземпляр `PackagePortTarget` попадает в кодеки Create и кастится к внутреннему классу Create → ClassCastException → дисконнект. Применённая защита: реестровый ключ — `tfc_chain_conveyor`. |

## План реализации по шагам

1. **Подготовка регистрации** — `chain/ChainConveyorRegistration.java` (каркас DeferredRegister, без классов внутри). `register(IEventBus)` помимо своих регистров вызывает `ChainConveyorPackagePortTargets.register(bus)`.
2. **Безцепный слой** (тип-нейтральный) — `ChainConveyorShape.java`, `ChainConveyorRoutingTable.java`, `ChainConveyorPackage.java`. Без правок, только ренейм.
3. **BE с правкой** — `ChainConveyorBlockEntity.java`: добавить `connectionChains`, write/read, fallback, хелперы. Переименовать `instanceof` и ссылки на `Blocks.CHAIN`/`Items.CHAIN`. Импортировать `FrogportBlockEntity` из Create (публичный API) — `tick()` уже стреляет пакетами в фрогпорт.
4. **Block + Connection + Packets** — `ChainConveyorBlock.java`, `ChainConveyorConnectionHandler.java`, `ChainConveyorConnectionPacket.java`, `ChainConveyorInteractionHandler.java`, `ChainConveyorRidingHandler.java`, `ChainPackageInteractionHandler.java`, `ChainPackageInteractionPacket.java`, `ClientboundChainConveyorRidingPacket.java`, `ServerboundChainConveyorRidingPacket.java`, `ServerChainConveyorHandler.java`.
5. **Frogport-интеграция** — `chain/ChainConveyorFrogportTarget.java` (copy of `PackagePortTarget.ChainConveyorFrogportTarget`, наши типы BE/Package) + `chain/ChainConveyorPackagePortTargets.java` (регистрация `tfc_aeronautics:chain_conveyor` в `CreateRegistries.PACKAGE_PORT_TARGET_TYPE`).
6. **Пакетный конфиг** — `chain/ChainConveyorPackets.java` с регистрацией 5 payload.
7. **Клиент-рендер** — `client/chain/ChainConveyorRenderer.java` + `client/chain/ChainConveyorVisual.java` + `client/chain/ChainConveyorClientSetup.java`.
8. **Активация** — дописать `register` в `TFCAeronautics.java`, добавить в `CreativeTabs.java`.
9. **Скрытие Create** — `BANNED_RECIPES` + `client/ChainConveyorCreativeTabFilter.java`.
10. **Рецепт + ассет** — JSON-рецепт под `tfc_aeronautics`, advancement, blockstate, models, loot, lang.
11. **Документация** — `DOCS.md`.
12. **Верификация** — см. ниже.

## Верификация

Только статическая; рантайм-запуск запрещён `CLAUDE.md`.

```bash
./gradlew compileJava       # main sources компилируются
./gradlew compileClientJava # клиент-рендер компилируется
./gradlew build             # полная сборка + datagen + jar
```

Грэпом убедиться:
```bash
# Должно быть пусто (все Items.CHAIN и Blocks.CHAIN заменены на per-connection lookup):
grep -rn "Items\.CHAIN\|Blocks\.CHAIN" src/main/java/ru/tfc_aeronautics/chain/ src/client/java/ru/aeronautics/client/chain/

# Должно быть пусто (не импортим Create'овские chain-conveyor классы):
grep -rn "import com\.simibubi\.create\.content\.kinetics\.chainConveyor" src/main/java/ru/tfc_aeronautics/chain/ src/client/java/ru/aeronautics/client/chain/

# Должно быть пусто (статик CHAIN_LOCATION больше не используется):
grep -rn "CHAIN_LOCATION" src/main/java/ru/tfc_aeronautics/chain/ src/client/java/ru/aeronautics/client/chain/
```

Если пользователь сообщает, что что-то сломалось в рантайме — зайти через `crashlog.txt`/`latest.log`, применить `superpowers:systematic-debugging`.

## После задачи — перепроверь себя

Эта секция — **отдельный шаг**, выполняется после реализации, когда весь код написан и `./gradlew build` зелёный. Цель: не пропустить расхождения между планом и кодом, поймать регрессии и edge-cases, которые gradle-компилятор не проверяет. Каждый пункт — конкретное действие, не абстрактное «подумай».

### A. Сверка с планом (план → код)

- [ ] **Список 16 скопированных классов** из таблицы «Что копируется» — все 16 присутствуют в `src/main/java/ru/tfc_aeronautics/chain/` или `src/client/java/ru/aeronautics/client/chain/`. Отсутствие хотя бы одного → причина краша.
- [ ] **2 frogport-файла** (`ChainConveyorFrogportTarget`, `ChainConveyorPackagePortTargets`) — оба созданы, второй вызывается из `ChainConveyorRegistration.register()`.
- [ ] **3 файла регистрации/инфраструктуры** — `ChainConveyorRegistration.java`, `ChainConveyorPackets.java`, `ChainConveyorClientSetup.java` — присутствуют.
- [ ] **`RecipeRemoval.BANNED_RECIPES`** содержит `create:crafting/kinetics/chain_conveyor` (рядом с уже существующими `fluid_pipe*`, `encased_chain_drive_from_zinc`).
- [ ] **`TFCAeronautics.java`** содержит `ChainConveyorRegistration.register(modEventBus);` рядом с другими `.register(...)`.
- [ ] **`CreativeTabs.displayItems`** содержит `output.accept(ChainConveyorRegistration.CHAIN_CONVEYOR_ITEM.get());`.
- [ ] **`chain/ChainConveyorCreativeTabFilter.java`** подписан на `BuildCreativeModeTabContentsEvent` и удаляет `create:chain_conveyor` из `create:base`.
- [ ] **Ассеты**: blockstate, 5 models (block, item, guard, shaft, wheel), loot, en_us/ru_ru lang, advancement — все на диске под `src/main/resources/`.
- [ ] **Recipe под `tfc_aeronautics`** — НЕ под `create/...` (иначе mixin RecipeManager его забанит как забаненный).

### B. Сверка с кодом Create (что не забыли переименовать)

Прогнать grep'ом по новым файлам; все 4 команды должны вернуть пусто:

```bash
grep -rn "Items\.CHAIN\|Blocks\.CHAIN" src/main/java/ru/tfc_aeronautics/chain/ src/client/java/ru/aeronautics/client/chain/
grep -rn "import com\.simibubi\.create\.content\.kinetics\.chainConveyor" src/main/java/ru/tfc_aeronautics/chain/ src/client/java/ru/aeronautics/client/chain/
grep -rn "CHAIN_LOCATION" src/main/java/ru/tfc_aeronautics/chain/ src/client/java/ru/aeronautics/client/chain/
grep -rn "AllBlocks\.CHAIN_CONVEYOR\|AllBlockEntityTypes\.CHAIN_CONVEYOR" src/main/java/ru/tfc_aeronautics/chain/ src/client/java/ru/aeronautics/client/chain/
```

(Последний — для замены статических `AllBlocks`-ссылок на `ChainConveyorRegistration.CHAIN_CONVEYOR.get()`.)

Также: `grep -rn "com\.simibubi\.create\.content\.kinetics\.chainConveyor\.ChainConveyorBlockEntity" src/main/java/ru/tfc_aeronautics/chain/ src/client/java/ru/aeronautics/client/chain/` — должно быть пусто (создали свой тип, не импортим чужой).

### C. Per-connection map — все 5 точек покрыты

Проверить по `ChainConveyorBlockEntity.java`, что `connectionChains` корректно живёт во всех жизненных циклах:

- [ ] **Инициализация / декларация поля** — `public Map<BlockPos, ResourceLocation> connectionChains = new HashMap<>();`
- [ ] **`addConnectionTo(BlockPos, ResourceLocation)`** — новый второй параметр, кладёт в map.
- [ ] **`removeConnectionTo(BlockPos)`** — удаляет из map.
- [ ] **`write(...)` и `writeSafe(...)`** — оба сериализуют `connectionChains` под ключом `ConnectionChains`.
- [ ] **`read(...)`** — десериализует, плюс lazy-fallback `Items.CHAIN.getKey()` для уже существующих в `connections` записей без ключа в `connectionChains`.
- [ ] **`transform(...)`** (контрапции) — переносит записи map по новым относительным координатам.

Если хоть один пропущен — старые миры ломаются или контрапции теряют тип цепи.

### D. Renderer — texture plumb

- [ ] `renderChains` (loop) — внутри цикла по `BlockPos` подтягивает `be.getChainTextureForConnection(blockPos)` и передаёт в `renderChain`.
- [ ] `renderChain` — принимает `ResourceLocation chainTex`, передаёт в `RenderTypes.chain(...)` вместо `CHAIN_LOCATION`.
- [ ] `chainDestroyed(...)` в BE — оба места drop-стека (`new ItemStack(Items.CHAIN, ...)` и `new ItemStack(Blocks.CHAIN.asItem(), ...)`) заменены на `new ItemStack(getChainItemForConnection(target), ...)`.
- [ ] `ChainConveyorConnectionPacket.applySettings` — путь `connect=true` пишет тип цепи в обе стороны; путь `connect=false` рефандит per-connection.
- [ ] Проверить, что в TFC-текстуру уходит `tfc:item/metal/chain/<metal>` (атлас предмета), а не `tfc:block/metal/chain/<metal>` (атлас блока) — это разные `.png`!

### E. Frogport-интеграция

- [ ] `ChainConveyorFrogportTarget.Type` зарегистрирован в `CreateRegistries.PACKAGE_PORT_TARGET_TYPE` под id `tfc_aeronautics:chain_conveyor` через `DeferredRegister` (см. `code_references/Create/.../AllPackagePortTargetTypes.java` как образец).
- [ ] `canSupport(BlockEntity be)` возвращает `true` только для `ChainConveyorBlockEntity` (нашего), не для Create-овского.
- [ ] Конструктор `ChainConveyorFrogportTarget(BlockPos chainPos, BoxEntry connection, boolean flipped)` принимает наши `BoxEntry` (из нашего `ChainConveyorPackage`) — не Create-овский.
- [ ] `getIcon()` возвращает `new ItemStack(ChainConveyorRegistration.CHAIN_CONVEYOR.get())`.
- [ ] `tick()` в `ChainConveyorBlockEntity` — фрогпорт-импульсы идут в обе стороны (наш BE → фрогпорт и обратно).

### F. Скрытие оригинала

- [ ] Запуск `./gradlew build` → загрузка data-pack → рецепт `create:crafting/kinetics/chain_conveyor` отсутствует в recipe manager (проверить `latest.log` после запуска мира, либо `datagen` reports).
- [ ] В креатив-табе `create:base` нет иконки `create:chain_conveyor` (проверяется только в игре; статически — что фильтр подписан на правильный `ResourceKey`).
- [ ] Забаненный рецепт действительно фильтруется миксином `RecipeManagerMixin` — см. `src/main/java/ru/tfc_aeronautics/recipe/RecipeRemoval.java` и `src/main/java/ru/tfc_aeronautics/mixin/`.

### G. Тесты из старой спеки (regressions)

Старая спека `docs/superpowers/specs/2026-08-16-chain-conveyor-tfc-chains-design.md` даёт checklist in-game. Без рантайма — только подготовить список для пользователя:

- [ ] Подключение vanilla `minecraft:chain` работает.
- [ ] Подключение `tfc:metal/chain/wrought_iron` (и ещё 1–2 металла) — работает.
- [ ] Disconnect sneak+wrench — рефандит тот же металл, что был подключён.
- [ ] Destroy блока — drop тем же металлом.
- [ ] Два разных металла в одной цепи — у каждого сегмента своя текстура.
- [ ] Logout/rejoin — тип сохраняется.
- [ ] Save/reload мира — тип сохраняется.

Эти пункты я не могу проверить сам. В итоге задачи — попросить пользователя зайти в Prism-лаунчер, прогнать список, и прислать `crashlog.txt`/`latest.log` если что-то крашится.

### H. Документация

- [ ] `DOCS.md` — добавлен раздел "Chain Conveyor (TFC-aware)" с описанием решения, отличий от Create, ограничений (нет авто-миграции, две несвязные сети).
- [ ] `PROJECT_STRUCTURE.md` обновится автоматически через `sync-structure-docs.sh` hook после Write/Edit (см. `feedback_structure_docs.md`).
- [ ] Если в этом проекте вы ведёте `ROADMAP.md` — добавить запись «Chain Conveyor (TFC-aware) — N/N ✓».

### I. Финальный чек

- [ ] `./gradlew build` — зелёный.
- [ ] Все 4 grep'а из §B возвращают пусто.
- [ ] Все 3 sentinel-файла (Registration, Packets, ClientSetup) скомпилированы.
- [ ] Запрос пользователю: прогон in-game checklist из §G; если crash — `crashlog.txt` в чат.

## Out of scope

- Авто-миграция `create:chain_conveyor` → `tfc_aeronautics:chain_conveyor` в существующих мирах.
- Склейка двух сетей через фрогпорт — `create:chain_conveyor` и наш образуют два отдельных контура, фрогпорт умеет стрелять в обе (две независимые target-записи), но пакеты не «протекают» между сетями.
- Display Link / Smart Observer адреса нашего конвейера (требует копирования `PackageAddressDisplaySource` и `SmartObserverBlockEntity` или mixin — пока за рамками).
- Поддержка цепей из модов вне TFC (достаточно существующего тега `c:chains`).
- Ponder-сцены под наш блок.
- Звуки, специфичные для металла цепи.
