package ru.tfc_aeronautics.fluid;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import ru.tfc_aeronautics.TFCAeronautics;

/**
 * Registers fluid blocks. Rosin uses vanilla {@link LiquidBlock},
 * mirroring how TFC's alcohol fluids register their blocks.
 */
public final class FluidBlocks
{
    public static final DeferredRegister.Blocks BLOCKS =
        DeferredRegister.createBlocks(TFCAeronautics.MOD_ID);

    public static final DeferredHolder<Block, LiquidBlock> ROSIN =
        BLOCKS.register("fluid/rosin", () -> new LiquidBlock(
            Fluids.ROSIN.getSource(),
            Block.Properties.ofFullCopy(Blocks.WATER).noLootTable()));

    private FluidBlocks() {}

    public static void register(IEventBus bus)
    {
        BLOCKS.register(bus);
    }
}