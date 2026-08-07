package ru.tfc_aeronautics.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;

import net.dries007.tfc.common.blocks.crop.Crop;
import net.dries007.tfc.common.blocks.plant.fruit.FruitBlocks;
import net.dries007.tfc.util.climate.ClimateRange;
import net.dries007.tfc.util.climate.ClimateRanges;
import net.dries007.tfc.world.chunkdata.ChunkData;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Climate-filtered loot generator for the ancient_shelter vessel.
 *
 * <p>Replaces the old {@code ancient_shelter_vessel.json} table: every entry in the
 * pool either has no climate requirement (meats, salt, ores) or carries a TFC
 * {@link ClimateRange} from the same data files that gate crop / fruit growth in
 * survival. At generation time we read the local {@code ChunkData}, keep only the
 * entries whose range contains the local temperature and groundwater, then roll
 * one weighted pick.
 *
 * <p>This keeps the loot coherent across climates: bananas and oranges never fall
 * out of an eternal-frost shelter, but cranberries, snowberries and cloudberries
 * appear there instead. Apples are dropped from hot-climate shelters because the
 * apple tree's range caps at ~12 °C. Vegetables (cabbage, carrot, onion, beet) are
 * left unrestricted — their ranges cover the whole habitable band — except for
 * tomato, which TFC gates to ≥ 2 °C.
 */
public final class AncientShelterLoot
{
    /**
     * One row of the loot pool: weight, optional climate range, item id, stack
     * size range. {@code climate} is {@code null} for entries that are valid in
     * every climate (meats, salt, ores, wide-range vegetables).
     */
    private record Entry(int weight, Supplier<ClimateRange> climate, ResourceLocation item, int minCount, int maxCount) {}

    private static final List<Entry> POOL = List.of(
        // --- always available ---
        entry(4, null, "food/beef", 1, 3),
        entry(4, null, "food/pork", 1, 3),
        entry(4, null, "food/mutton", 1, 3),
        entry(3, null, "food/chicken", 1, 3),
        entry(3, null, "food/venison", 1, 2),
        entry(3, null, "powder/salt", 1, 4),
        entry(1, null, "ore/small_native_copper", 1, 8),
        entry(1, null, "ore/small_native_gold", 1, 8),

        // --- tropical fruits (warm only) ---
        entry(3, ClimateRanges.BANANA_PLANT, "food/banana", 1, 4),
        entry(3, ClimateRanges.FRUIT_TREES.get(FruitBlocks.Tree.ORANGE), "food/orange", 1, 4),
        entry(3, ClimateRanges.FRUIT_TREES.get(FruitBlocks.Tree.LEMON), "food/lemon", 1, 4),

        // --- tree fruits (mostly cool, olive & peach lean warm) ---
        entry(5, ClimateRanges.FRUIT_TREES.get(FruitBlocks.Tree.RED_APPLE), "food/red_apple", 1, 4),
        entry(2, ClimateRanges.FRUIT_TREES.get(FruitBlocks.Tree.GREEN_APPLE), "food/green_apple", 1, 4),
        entry(3, ClimateRanges.FRUIT_TREES.get(FruitBlocks.Tree.CHERRY), "food/cherry", 1, 4),
        entry(3, ClimateRanges.FRUIT_TREES.get(FruitBlocks.Tree.PLUM), "food/plum", 1, 4),
        entry(3, ClimateRanges.FRUIT_TREES.get(FruitBlocks.Tree.PEACH), "food/peach", 1, 4),
        entry(2, ClimateRanges.FRUIT_TREES.get(FruitBlocks.Tree.OLIVE), "food/olive", 1, 4),

        // --- spreading bushes ---
        entry(2, ClimateRanges.SPREADING_BUSHES.get(FruitBlocks.SpreadingBush.BLACKBERRY), "food/blackberry", 1, 4),
        entry(3, ClimateRanges.SPREADING_BUSHES.get(FruitBlocks.SpreadingBush.BLUEBERRY), "food/blueberry", 1, 4),
        entry(2, ClimateRanges.SPREADING_BUSHES.get(FruitBlocks.SpreadingBush.ELDERBERRY), "food/elderberry", 1, 4),
        entry(2, ClimateRanges.SPREADING_BUSHES.get(FruitBlocks.SpreadingBush.RASPBERRY), "food/raspberry", 1, 4),
        entry(2, ClimateRanges.CRANBERRY_BUSH, "food/cranberry", 1, 4),

        // --- stationary bushes ---
        entry(1, ClimateRanges.STATIONARY_BUSHES.get(FruitBlocks.StationaryBush.SNOWBERRY), "food/snowberry", 1, 4),
        entry(1, ClimateRanges.STATIONARY_BUSHES.get(FruitBlocks.StationaryBush.CLOUDBERRY), "food/cloudberry", 1, 4),
        entry(2, ClimateRanges.STATIONARY_BUSHES.get(FruitBlocks.StationaryBush.GOOSEBERRY), "food/gooseberry", 1, 4),
        entry(2, ClimateRanges.STATIONARY_BUSHES.get(FruitBlocks.StationaryBush.STRAWBERRY), "food/strawberry", 1, 4),
        entry(1, ClimateRanges.STATIONARY_BUSHES.get(FruitBlocks.StationaryBush.WINTERGREEN_BERRY), "food/wintergreen_berry", 1, 4),
        entry(1, ClimateRanges.STATIONARY_BUSHES.get(FruitBlocks.StationaryBush.BUNCHBERRY), "food/bunchberry", 1, 4),

        // --- vegetables (tomato is warmth-only; the rest are wide-range enough to keep unconditional) ---
        entry(4, null, "food/cabbage", 1, 4),
        entry(4, ClimateRanges.CROPS.get(Crop.TOMATO), "food/tomato", 1, 4),
        entry(4, null, "food/carrot", 1, 4),
        entry(3, null, "food/onion", 1, 4),
        entry(3, null, "food/beet", 1, 4)
    );

    private AncientShelterLoot() {}

    /**
     * Rolls one climate-appropriate entry from {@link #POOL} and returns it as a
     * single-item list. Mirrors the old JSON table's single-roll shape, so
     * {@code ContainerLootFiller.writeLoot} still writes into the first empty slot.
     */
    public static List<ItemStack> roll(LevelReader level, BlockPos center, RandomSource random)
    {
        float temperature = 0f;
        int groundwater = 50;
        try
        {
            final ChunkData data = ChunkData.get(level.getChunk(center));
            if (data != ChunkData.EMPTY)
            {
                temperature = data.getAverageSeaLevelTemp(center);
                groundwater = (int) data.getAverageGroundwater(center);
            }
        }
        catch (RuntimeException ignored) {}

        final List<Entry> eligible = new ArrayList<>();
        for (Entry entry : POOL)
        {
            if (entry.climate() == null)
            {
                eligible.add(entry);
                continue;
            }
            final ClimateRange range = entry.climate().get();
            if (range != null && range.checkBoth(groundwater, temperature, false))
            {
                eligible.add(entry);
            }
        }
        if (eligible.isEmpty())
        {
            // Always-available items guarantee the pool is never empty, but be
            // defensive: if something stripped them, fall back to the first entry.
            return stackFrom(POOL.get(0), random);
        }

        int totalWeight = 0;
        for (Entry e : eligible) totalWeight += e.weight();
        int roll = random.nextInt(totalWeight);
        Entry chosen = eligible.get(eligible.size() - 1);
        for (Entry e : eligible)
        {
            roll -= e.weight();
            if (roll < 0)
            {
                chosen = e;
                break;
            }
        }
        return stackFrom(chosen, random);
    }

    private static List<ItemStack> stackFrom(Entry entry, RandomSource random)
    {
        final Item item = BuiltInRegistries.ITEM.get(entry.item());
        if (item == null)
        {
            return List.of();
        }
        final int count = entry.minCount() + random.nextInt(entry.maxCount() - entry.minCount() + 1);
        return List.of(new ItemStack(item, count));
    }

    private static Entry entry(int weight, Supplier<ClimateRange> climate, String itemPath, int minCount, int maxCount)
    {
        return new Entry(weight, climate, ResourceLocation.fromNamespaceAndPath("tfc", itemPath), minCount, maxCount);
    }
}