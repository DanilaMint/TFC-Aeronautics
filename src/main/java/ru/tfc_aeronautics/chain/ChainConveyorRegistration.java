package ru.tfc_aeronautics.chain;

import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import ru.tfc_aeronautics.TFCAeronautics;

/**
 * Registers the {@code tfc_aeronautics:chain_conveyor} block, its block-item, and its block-entity type.
 *
 * <p>Block properties mirror Create's {@code AllBlocks.CHAIN_CONVEYOR}: stone-strength,
 * {@code PODZOL} map colour, no occlusion. The block is intentionally registered in the
 * {@code tfc_aeronautics} namespace even though the logic is verbatim from Create — this keeps
 * worldgen / loot / tag edits pointed at our mod and avoids a cross-mod dependency on a
 * specific Create block id that Create may remove or rename in later versions.
 *
 * <p>Package-port-target registration lives in {@link ChainConveyorPackagePortTargets}.
 */
public final class ChainConveyorRegistration {

    public static final DeferredRegister.Blocks BLOCKS =
        DeferredRegister.createBlocks(TFCAeronautics.MOD_ID);

    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(TFCAeronautics.MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TFCAeronautics.MOD_ID);

    public static final DeferredHolder<Block, ChainConveyorBlock> CHAIN_CONVEYOR =
        BLOCKS.register("chain_conveyor", () -> new ChainConveyorBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.PODZOL)
                .strength(1.5F, 6.0F)
                .noOcclusion()));

    public static final DeferredHolder<Item, BlockItem> CHAIN_CONVEYOR_ITEM =
        ITEMS.register("chain_conveyor",
            () -> new BlockItem(CHAIN_CONVEYOR.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChainConveyorBlockEntity>> CHAIN_CONVEYOR_BE =
        BLOCK_ENTITY_TYPES.register("chain_conveyor", ChainConveyorRegistration::createBlockEntityType);

    private static BlockEntityType<ChainConveyorBlockEntity> createBlockEntityType() {
        // The BE constructor requires the very BlockEntityType we are constructing; the type
        // holder is not yet wired when the builder runs, so defer it through an AtomicReference
        // populated below. Once createBlockEntityType() returns and the registry freezes,
        // TYPE.get() is the fully-built type that placeBlockEntity will hand back.
        AtomicReference<BlockEntityType<ChainConveyorBlockEntity>> ref = new AtomicReference<>();
        BlockEntityType<ChainConveyorBlockEntity> type = BlockEntityType.Builder.of(
            (pos, state) -> new ChainConveyorBlockEntity(ref.get(), pos, state),
            CHAIN_CONVEYOR.get()
        ).build(null);
        ref.set(type);
        return type;
    }

    private ChainConveyorRegistration() {}

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
        ChainConveyorPackagePortTargets.register(bus);
        ChainConveyorPackets.register(bus);
    }
}