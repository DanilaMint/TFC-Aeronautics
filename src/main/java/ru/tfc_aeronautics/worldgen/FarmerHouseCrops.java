package ru.tfc_aeronautics.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.LevelReader;

import net.dries007.tfc.common.blocks.crop.Crop;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.climate.ClimateRange;
import net.dries007.tfc.world.chunkdata.ChunkData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Climate-driven crop picker for farmer_house atmospheric structures.
 *
 * <p>The crop on the building's farmland and the matching food + seed stacks in its
 * sealed vessel are drawn from a single deterministic pick per placement: both the
 * {@link LocalMaterialProcessor} (which rewrites the template's placeholder crop block)
 * and {@link FarmerHouseEffects} (which fills the vessel loot) call
 * {@link #pick(LevelReader, BlockPos)} with the same structure centre and get the
 * same {@link Crop} back. The pick is keyed on {@code BlockPos.asLong()} so the result
 * is reproducible across calls and across chunk-gen re-runs (vanilla round-trips
 * structure pieces through chunk NBT before post-process runs).
 *
 * <p>Crops without a TFC food item (CANOLA, ALFALFA, JUTE, PAPYRUS, PUMPKIN, MELON,
 * the bell peppers, etc.) are filtered out of the picker's candidate set so the
 * vessel filler never has to fall back to a crop that doesn't match what's growing
 * on the beds.
 */
public final class FarmerHouseCrops
{
    /**
     * Crops eligible for farmer_house placement. Each entry is a {@link Crop} that
     * also has a {@code tfc:food/<...>} item; the value is that item's
     * {@link ResourceLocation}. {@link #pick} iterates the keys of this map.
     */
    private static final Map<Crop, ResourceLocation> FOOD_IDS = Map.ofEntries(
        Map.entry(Crop.CASSAVA, id("food/cassava")),
        Map.entry(Crop.LENTIL, id("food/lentil")),
        Map.entry(Crop.PEANUT, id("food/peanut")),
        Map.entry(Crop.SOYBEAN, id("food/soybean")),
        Map.entry(Crop.BARLEY, id("food/barley_grain")),
        Map.entry(Crop.OAT, id("food/oat_grain")),
        Map.entry(Crop.RYE, id("food/rye_grain")),
        Map.entry(Crop.WHEAT, id("food/wheat_grain")),
        Map.entry(Crop.MAIZE, id("food/maize_grain")),
        Map.entry(Crop.RICE, id("food/rice_grain")),
        Map.entry(Crop.BEET, id("food/beet")),
        Map.entry(Crop.CABBAGE, id("food/cabbage")),
        Map.entry(Crop.CARROT, id("food/carrot")),
        Map.entry(Crop.GARLIC, id("food/garlic")),
        Map.entry(Crop.ONION, id("food/onion")),
        Map.entry(Crop.POTATO, id("food/potato")),
        Map.entry(Crop.SQUASH, id("food/squash")),
        Map.entry(Crop.TOMATO, id("food/tomato")),
        Map.entry(Crop.RADISH, id("food/radish"))
    );

    private FarmerHouseCrops() {}

    /**
     * Picks the climate-suitable crop for the structure centred at {@code center}.
     * Returns {@link Crop#WHEAT} when no chunk data is available yet or when nothing
     * in {@link #FOOD_IDS} matches the local climate — wheat's range is wide enough
     * that this fallback is rarely triggered.
     */
    public static Optional<Crop> pick(LevelReader level, BlockPos center)
    {
        try
        {
            final ChunkData data = ChunkData.get(level.getChunk(center));
            if (data == ChunkData.EMPTY)
            {
                return Optional.of(Crop.WHEAT);
            }
            final float temperature = data.getAverageSeaLevelTemp(center);
            final float groundwater = data.getAverageGroundwater(center);
            final List<Crop> suitable = new ArrayList<>();
            for (Crop crop : FOOD_IDS.keySet())
            {
                final ClimateRange range = crop.getClimateRange().get();
                if (range == null)
                {
                    continue;
                }
                // Match TFC's own CropBlock check: both temperature AND hydration
                // must fall inside the range. Allow wiggle = false so the crop has
                // to actually be in season, not just within tolerance of the edge.
                if (range.checkBoth((int) groundwater, temperature, false))
                {
                    suitable.add(crop);
                }
            }
            if (suitable.isEmpty())
            {
                return Optional.of(Crop.WHEAT);
            }
            // Seed from the centre position so the pick is deterministic across the
            // processor (which runs during postProcess) and the vessel filler (which
            // runs from afterPlace). Both calls produce the same RNG state.
            final RandomSource rng = RandomSource.create(center.asLong());
            return Optional.of(suitable.get(rng.nextInt(suitable.size())));
        }
        catch (RuntimeException e)
        {
            return Optional.of(Crop.WHEAT);
        }
    }

    public static Item foodItem(Crop crop)
    {
        final ResourceLocation foodId = FOOD_IDS.get(crop);
        if (foodId == null)
        {
            return null;
        }
        return BuiltInRegistries.ITEM.get(foodId);
    }

    public static Item seedItem(Crop crop)
    {
        return TFCItems.CROP_SEEDS.get(crop).get();
    }

    private static ResourceLocation id(String path)
    {
        return ResourceLocation.fromNamespaceAndPath("tfc", path);
    }
}