# Spout / Casting

**Прогресс:** 4/4 ✓

## Регистрация
- [x] `recipe/SpoutCompat.java` — `FMLCommonSetupEvent` enqueueWork, регистрирует `BlockSpoutingBehaviour.BY_BLOCK_ENTITY` для `TFCBlockEntities.MOLD_TABLE`
- [x] `recipe/SpoutCastingBehavior.java` — enum `INSTANCE`, `fillBlock` заливает `min(remaining, available)` из spout в mold через `IFluidHandlerItem.fill`
- [x] partial fill: поддерживает доливку в частично заполненный mold (тот же металл)
- [x] guards: skips если mold stack пустой, нет рецепта, нет fluid, разные металлы, или `OUTPUT_SLOT` занят

## Поведение по сценариям

| # | Сценарий | Результат |
|---|----------|-----------|
| 1 | пустой mold + ≥100 mB нужного металла | mold заполняется до 100 mB, едет дальше |
| 2 | mold полный (≥ amount mB) | проходит без изменений |
| 3 | mold с металлом A + spout с металлом B | проходит без изменений |
| 4 | mold 30 mB + spout 60 mB (тот же металл) | доливаем 60 → mold 90 mB, едет дальше |
| 5 | mold 50 mB + spout 100 mB (тот же металл) | доливаем 50 → mold 100 mB, едет дальше |

Cast-извлечение через `recipe.assemble` намеренно НЕ выполняется — это задача отдельной станции (см. открытый вопрос в плане).
