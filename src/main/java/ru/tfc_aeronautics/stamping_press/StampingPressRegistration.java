package ru.tfc_aeronautics.stamping_press;

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

import com.simibubi.create.content.processing.AssemblyOperatorBlockItem;

import ru.tfc_aeronautics.TFCAeronautics;

/**
 * Registers the {@code tfc_aeronautics:stamping_press} block, its block-item,
 * and its block-entity type. Mirrors the layout of {@code HeaterRegistration}.
 */
public final class StampingPressRegistration {

    public static final DeferredRegister.Blocks BLOCKS =
        DeferredRegister.createBlocks(TFCAeronautics.MOD_ID);

    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(TFCAeronautics.MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TFCAeronautics.MOD_ID);

    public static final DeferredHolder<Block, StampingPressBlock> STAMPING_PRESS =
        BLOCKS.register("stamping_press", () -> new StampingPressBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.PODZOL)
                .strength(2.0F, 6.0F)
                .sound(SoundType.STONE)
                .noOcclusion()
                .pushReaction(PushReaction.BLOCK)));

    public static final DeferredHolder<Item, BlockItem> STAMPING_PRESS_ITEM =
        ITEMS.register("stamping_press", () -> new AssemblyOperatorBlockItem(STAMPING_PRESS.get(), new Item.Properties()));

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
    }
}
