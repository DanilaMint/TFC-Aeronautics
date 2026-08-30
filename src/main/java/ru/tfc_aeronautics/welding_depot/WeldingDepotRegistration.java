package ru.tfc_aeronautics.welding_depot;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import ru.tfc_aeronautics.TFCAeronautics;

import java.util.LinkedHashMap;

public final class WeldingDepotRegistration {

    public static final DeferredRegister.Blocks BLOCKS =
        DeferredRegister.createBlocks(TFCAeronautics.MOD_ID);

    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(TFCAeronautics.MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TFCAeronautics.MOD_ID);

    public static final LinkedHashMap<String, DeferredHolder<Block, WeldingDepotBlock>> DEPOTS = new LinkedHashMap<>();
    public static final LinkedHashMap<String, DeferredHolder<Item, BlockItem>> DEPOT_ITEMS = new LinkedHashMap<>();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WeldingDepotBlockEntity>> WELDING_DEPOT_BE =
        BLOCK_ENTITY_TYPES.register("welding_depot", WeldingDepotRegistration::createBlockEntityType);

    static {
        for (DepotTier tier : DepotTier.values()) {
            String id = "metal/welding_depot/" + tier.materialSerializedName;
            DeferredHolder<Block, WeldingDepotBlock> block = BLOCKS.register(id, () -> new WeldingDepotBlock(
                BlockBehaviour.Properties.of()
                    .mapColor(tier.mapColor)
                    .strength(5F, 8F)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .requiresCorrectToolForDrops(),
                tier.level));
            DEPOTS.put(tier.materialSerializedName, block);
            DEPOT_ITEMS.put(tier.materialSerializedName, ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties())));
        }
    }

    private static BlockEntityType<WeldingDepotBlockEntity> createBlockEntityType() {
        return BlockEntityType.Builder.of(
            WeldingDepotBlockEntity::new,
            DEPOTS.values().stream().map(DeferredHolder::get).toArray(Block[]::new)
        ).build(null);
    }

    private WeldingDepotRegistration() {}

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
        WeldingDepotCapabilities.register(bus);
    }
}
