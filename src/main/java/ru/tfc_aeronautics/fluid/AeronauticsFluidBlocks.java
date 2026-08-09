package ru.tfc_aeronautics.fluid;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.dries007.tfc.common.blocks.MoltenFluidBlock;

import ru.tfc_aeronautics.Aeronautics;

/**
 * Registers fluid blocks: molten magmatite via TFC's {@link MoltenFluidBlock}
 * (mirroring {@code TFCBlocks.METAL_FLUIDS}), and rosin via vanilla {@link LiquidBlock}
 * (mirroring how TFC's alcohol fluids register their blocks).
 */
public final class AeronauticsFluidBlocks
{
    public static final DeferredRegister.Blocks BLOCKS =
        DeferredRegister.createBlocks(Aeronautics.MOD_ID);

    public static final DeferredHolder<Block, MoltenFluidBlock> MOLTEN_MAGMATITE =
        BLOCKS.register("fluid/molten_magmatite", () -> new MoltenFluidBlock(
            AeronauticsFluids.MOLTEN_MAGMATITE::getSource,
            Block.Properties.ofFullCopy(Blocks.LAVA).noLootTable()));

    public static final DeferredHolder<Block, LiquidBlock> ROSIN =
        BLOCKS.register("fluid/rosin", () -> new LiquidBlock(
            AeronauticsFluids.ROSIN.getSource(),
            Block.Properties.ofFullCopy(Blocks.WATER).noLootTable()));

    private AeronauticsFluidBlocks() {}

    public static void register(IEventBus bus)
    {
        BLOCKS.register(bus);
    }
}