package ru.tfc_aeronautics.crop;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.dries007.tfc.common.blockentities.CropBlockEntity;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.crop.DeadCropBlock;
import net.dries007.tfc.common.blocks.crop.WildCropBlock;

import ru.tfc_aeronautics.TFCAeronautics;

/**
 * Registers the Kok-Saghyz crop: the living plant, its dead and wild
 * variants, the seeds and the harvested roots.
 *
 * <p>The crop needs its own block entity type. TFC's own crop entity type is
 * built from the blocks of TFC's Crop enum, and that set is immutable - a
 * block that is not in it gets no block entity at all, and a crop without a
 * block entity never grows, because the growth tick lives entirely in the
 * block entity.
 */
public final class KokSagyzRegistration
{
    private static final String CROP_PATH = "crop/" + KokSagyzCrop.ID;
    private static final String DEAD_CROP_PATH = "dead_crop/" + KokSagyzCrop.ID;
    private static final String WILD_CROP_PATH = "wild_crop/" + KokSagyzCrop.ID;
    private static final String SEEDS_PATH = "seeds/" + KokSagyzCrop.ID;
    private static final String ROOTS_PATH = "kok_sagyz_roots";
    private static final String CROP_BE_PATH = "crop_kok_sagyz";

    public static final DeferredRegister.Blocks BLOCKS =
        DeferredRegister.createBlocks(TFCAeronautics.MOD_ID);

    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(TFCAeronautics.MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TFCAeronautics.MOD_ID);

    public static final DeferredHolder<Block, KokSagyzCropBlock> CROP =
        BLOCKS.register(CROP_PATH, () -> new KokSagyzCropBlock(
            cropProperties(),
            () -> KokSagyzRegistration.DEAD_CROP.get(),
            () -> KokSagyzRegistration.SEEDS.get()));

    public static final DeferredHolder<Block, DeadCropBlock> DEAD_CROP =
        BLOCKS.register(DEAD_CROP_PATH, () -> new DeadCropBlock(plantProperties(), KokSagyzCrop.CLIMATE));

    public static final DeferredHolder<Block, WildCropBlock> WILD_CROP =
        BLOCKS.register(WILD_CROP_PATH, () -> new WildCropBlock(plantProperties()));

    public static final DeferredHolder<Item, KokSagyzSeedItem> SEEDS =
        ITEMS.register(SEEDS_PATH, () -> new KokSagyzSeedItem(
            KokSagyzRegistration.CROP.get(),
            KokSagyzRegistration.DEAD_CROP.get(),
            new Item.Properties()));

    public static final DeferredHolder<Item, BlockItem> WILD_CROP_ITEM =
        ITEMS.register(WILD_CROP_PATH, () -> new BlockItem(WILD_CROP.get(), new Item.Properties()));

    public static final DeferredHolder<Item, Item> ROOTS =
        ITEMS.register(ROOTS_PATH, () -> new Item(new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<KokSagyzCropBlockEntity>> CROP_BE =
        BLOCK_ENTITIES.register(CROP_BE_PATH, () -> BlockEntityType.Builder
            .of(KokSagyzCropBlockEntity::new, KokSagyzRegistration.CROP.get())
            .build(null));

    /**
     * Properties shared by the dead and the wild plant: no collision, breakable
     * by hand, and it ticks so the wild variant can ripen with the seasons.
     */
    private static ExtendedProperties plantProperties()
    {
        return ExtendedProperties.of(MapColor.PLANT)
            .noCollission()
            .randomTicks()
            .strength(0.4F)
            .sound(SoundType.CROP)
            .flammable(60, 30)
            .pushReaction(PushReaction.DESTROY);
    }

    /**
     * The living plant additionally carries the block entity that runs the
     * growth tick.
     */
    private static ExtendedProperties cropProperties()
    {
        return plantProperties()
            .blockEntity(() -> KokSagyzRegistration.CROP_BE.get())
            .serverTicks(CropBlockEntity::serverTick);
    }

    private KokSagyzRegistration() {}

    public static void register(IEventBus bus)
    {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
    }
}
