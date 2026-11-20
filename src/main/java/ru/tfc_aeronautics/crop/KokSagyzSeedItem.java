package ru.tfc_aeronautics.crop;

import java.util.Map;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.level.block.Block;

import net.dries007.tfc.common.items.PlantableInfo;
import net.dries007.tfc.util.climate.ClimateRange;

/**
 * Kok-Saghyz seeds.
 *
 * <p>TFC's own {@code SeedItem} is tied to the {@code Crop} enum, which an
 * addon cannot extend, so this is a standalone equivalent: it plants the crop
 * like any vanilla seed item, and adds the same hover tooltip TFC shows for its
 * crops - the climate window and the nutrients the plant takes out of the soil.
 */
public class KokSagyzSeedItem extends ItemNameBlockItem implements PlantableInfo
{
    private final Block deadBlock;

    public KokSagyzSeedItem(Block block, Block deadBlock, Item.Properties properties)
    {
        super(block, properties);
        this.deadBlock = deadBlock;
    }

    @Override
    public PlantNutrients getNutrientsInfo()
    {
        return new PlantNutrients(KokSagyzCrop.NITROGEN, KokSagyzCrop.PHOSPHOROUS, KokSagyzCrop.POTASSIUM);
    }

    @Override
    public ClimateRange getClimateRangeInfo()
    {
        return KokSagyzCrop.CLIMATE.get();
    }

    /**
     * Point the dead crop block at the seeds as well, so that breaking a plant
     * that died in the ground hands back seeds rather than nothing.
     */
    @Override
    public void registerBlocks(Map<Block, Item> blockToItemMap, Item item)
    {
        super.registerBlocks(blockToItemMap, item);
        blockToItemMap.put(deadBlock, item);
    }
}
