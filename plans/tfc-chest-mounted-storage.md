# TFC-сундуки как хранилища конструкций Create (Mounted Storage)

## Контекст

Create 6.x поддерживает «хранилища на конструкции» (Mounted Storage,
`com.simibubi.create.api.contraption.storage.item`): блок на contraption, для которого
в реестре `MountedItemStorageType.REGISTRY` есть тип, становится хранилищем — пила /
харвестер / воронка складывают в него добычу через
`contraption.getStorage().getAllItems()`, ПКМ открывает generic-GUI, при разборке
конструкции содержимое возвращается в BlockEntity.

Какие блоки — хранилища, задаётся тегами:
- `create:chest_mounted_storage` — ванильные `chest` / `trapped_chest`;
- `create:simple_mounted_storage` — `minecraft:barrel`, шулкеры.

TFC-сундуки (`tfc:wood/chest/*`, `tfc:wood/trapped_chest/*`, 40 блоков) в этих тегах не
состояли → на конструкции они ничего не собирали. Требуется: паритет с ванильным
сундуком (сбор добычи, GUI, возврат содержимого при разборке).

Реализация — **датапак-тегом, без Java**: `TFCChestBlock extends ChestBlock`,
`TFCChestBlockEntity extends ChestBlockEntity` (→ `Container`, 18 слотов 9×2, 36 у
двойного), поэтому штатный `ChestMountedStorageType` подходит без изменений.

## Готово

- [x] `src/main/resources/data/create/tags/block/chest_mounted_storage.json` —
      shadow-датапак в namespace `create`: `"replace": false` + 40 id TFC-сундуков
      (список взят из тега TFC `c:chests/wooden`). Файл Create с тем же id тега
      сливается с нашим (у него нет `"replace": true`), поэтому ванильные сундуки
      продолжают работать.
- [x] `DOCS.md` — раздел 38 «TFC-сундуки как хранилища конструкций Create
      (Mounted Storage)», TOC обновлён.
- [x] Статическая проверка: JSON валиден, 40/40 id совпадают с тегом TFC.

## Известные ограничения

- Лимит размера предмета TFC (`TFCChestBlockEntity#isValid`,
  `size ≤ chestMaximumItemSize`) внутри сундука на конструкции не проверяется: generic-GUI
  Create разрешает любой предмет, `copyContents` при разборке пишет через
  `setStackInSlot` без валидации. Поведение ванильного сундука на конструкции — то же.
- Песты TFC (`PestContainer`) и отложенный лут (`unpackLootTable`) на конструкции не
  работают.
- Ящики (`tfc:wood/crate/*`) и бочки (`tfc:wood/barrel/*`) не подключены (по решению
  пользователя): у crate не соблюдается `isItemValid` при возврате, у barrel 3 слота +
  жидкость — не подходит под generic-GUI Create.

## TODO (новые добавлять сюда)

- [ ] (пусто) — возможная фаза 2: собственный `MountedItemStorageType` с проверкой
      `TFCChestBlockEntity#isValid`, чтобы лимит размера предмета соблюдался и на
      конструкции (регистрация типа в `create:mounted_item_storage_type` через
      `RegisterEvent` + `MountedItemStorageType.REGISTRY.register(block, type)` для 40
      блоков).

## Проверка в игре

Запускать игру нельзя (`runClient`/`runServer` запрещены) — проверяет пользователь в
Prism-лаунчере:

1. Платформа на подшипнике + механическая пила + TFC-сундук на платформе; распилить
   бревно TFC → добыча оказывается в сундуке (ПКМ по сундуку на ходу открывает GUI 9×2).
2. Разобрать конструкцию → содержимое возвращается в сундук в мире.
3. Два соседних TFC-сундука на конструкции → GUI 9×4.
4. Контроль: тот же сценарий с ванильным сундуком — поведение совпадает.
