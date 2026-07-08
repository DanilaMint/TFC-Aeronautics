package ru.tfc_aeronautics.press;

import com.simibubi.create.api.stress.BlockStressValues;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import ru.tfc_aeronautics.Aeronautics;

/**
 * Registers the {@code tfc_aeronautics:stamping_press} block, its block item, and its
 * block-entity type, plus the kinetic stress impact (8.0 SU, matching Create's own
 * {@code mechanical_press}).
 *
 * <p>Mirrors the {@link ru.tfc_aeronautics.fluid.AeronauticsFluidBlocks} /
 * {@link ru.tfc_aeronautics.metal.DoubleIngotRegistration} convention: three
 * {@code DeferredRegister}s initialised statically, with a single {@link #register(IEventBus)}
 * entry point called from {@link Aeronautics#Aeronautics}.
 */
public final class StampingPressRegistration {

    public static final DeferredRegister.Blocks BLOCKS =
        DeferredRegister.createBlocks(Aeronautics.MOD_ID);

    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(Aeronautics.MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Aeronautics.MOD_ID);

    public static final DeferredHolder<Block, StampingPressBlock> STAMPING_PRESS =
        BLOCKS.register("stamping_press", () -> new StampingPressBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.PODZOL)
                .strength(2.0F, 6.0F)
                .noOcclusion()
                .sound(SoundType.STONE)
                .pushReaction(PushReaction.BLOCK)));

    public static final DeferredHolder<Item, BlockItem> STAMPING_PRESS_ITEM =
        ITEMS.register("stamping_press", () -> new BlockItem(STAMPING_PRESS.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StampingPressBlockEntity>> STAMPING_PRESS_BE =
        BLOCK_ENTITY_TYPES.register("stamping_press", StampingPressRegistration::createBlockEntityType);

    private static BlockEntityType<StampingPressBlockEntity> createBlockEntityType() {
        return BlockEntityType.Builder.of(
            StampingPressBlockEntity::new,
            STAMPING_PRESS.get()
        ).build(null);
    }

    private StampingPressRegistration() {}

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
        bus.addListener((net.neoforged.neoforge.registries.RegisterEvent event) -> {
            if (event.getRegistryKey().equals(Registries.BLOCK)) {
                BlockStressValues.IMPACTS.register(STAMPING_PRESS.get(), () -> 8.0);
            }
        });
    }
}
