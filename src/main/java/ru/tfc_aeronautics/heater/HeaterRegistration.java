package ru.tfc_aeronautics.heater;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import ru.tfc_aeronautics.TFCAeronautics;

/**
 * Registers the {@code tfc_aeronautics:heater} block, its block-item, and its block-entity type.
 *
 * <p>Three {@code DeferredRegister}s initialised statically, with a single {@link #register(IEventBus)}
 * entry point called from {@link TFCAeronautics#TFCAeronautics}.
 *
 * <p>Capability registration for the block entity (IItemHandler for chute/funnel/hopper,
 * IFluidHandler for the downward molten-metal pipe) lives in {@link HeaterCapabilities}.
 */
public final class HeaterRegistration {

    public static final DeferredRegister.Blocks BLOCKS =
        DeferredRegister.createBlocks(TFCAeronautics.MOD_ID);

    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(TFCAeronautics.MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TFCAeronautics.MOD_ID);

    public static final DeferredHolder<Block, HeaterBlock> HEATER =
        BLOCKS.register("heater", () -> new HeaterBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(2.0F, 6.0F)
                .sound(SoundType.STONE)
                .pushReaction(PushReaction.BLOCK)));

    public static final DeferredHolder<Item, BlockItem> HEATER_ITEM =
        ITEMS.register("heater", () -> new BlockItem(HEATER.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HeaterBlockEntity>> HEATER_BE =
        BLOCK_ENTITY_TYPES.register("heater", HeaterRegistration::createBlockEntityType);

    private static BlockEntityType<HeaterBlockEntity> createBlockEntityType() {
        return BlockEntityType.Builder.of(
            HeaterBlockEntity::new,
            HEATER.get()
        ).build(null);
    }

    private HeaterRegistration() {}

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
        HeaterCapabilities.register(bus);
    }
}