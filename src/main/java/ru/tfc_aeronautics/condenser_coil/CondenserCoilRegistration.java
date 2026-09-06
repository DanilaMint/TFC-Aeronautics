package ru.tfc_aeronautics.condenser_coil;

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
 * Registers the {@code tfc_aeronautics:condenser_coil} block, its block-item,
 * and its block-entity type.
 *
 * <p>Mirrors {@link ru.tfc_aeronautics.heater.HeaterRegistration} in structure:
 * three {@code DeferredRegister}s initialised statically, with a single
 * {@link #register(IEventBus)} entry point called from
 * {@link TFCAeronautics}. The coil does not expose any fluid-handler capability
 * of its own — result fluid is pushed directly into the neighbour on the
 * result face each tick, see
 * {@link CondenserCoilBlockEntity#ejectThroughResultFace(FluidStack)}.
 */
public final class CondenserCoilRegistration {

    public static final DeferredRegister.Blocks BLOCKS =
        DeferredRegister.createBlocks(TFCAeronautics.MOD_ID);

    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(TFCAeronautics.MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TFCAeronautics.MOD_ID);

    public static final DeferredHolder<Block, CondenserCoilBlock> CONDENSER_COIL =
        BLOCKS.register("condenser_coil", () -> new CondenserCoilBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F, 8.0F)
                .sound(SoundType.METAL)
                .pushReaction(PushReaction.BLOCK)
                .noOcclusion()));

    public static final DeferredHolder<Item, BlockItem> CONDENSER_COIL_ITEM =
        ITEMS.register("condenser_coil", () -> new BlockItem(CONDENSER_COIL.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CondenserCoilBlockEntity>> CONDENSER_COIL_BE =
        BLOCK_ENTITY_TYPES.register("condenser_coil", CondenserCoilRegistration::createBlockEntityType);

    private static BlockEntityType<CondenserCoilBlockEntity> createBlockEntityType() {
        return BlockEntityType.Builder.of(
            CondenserCoilBlockEntity::new,
            CONDENSER_COIL.get()
        ).build(null);
    }

    private CondenserCoilRegistration() {}

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
    }
}
