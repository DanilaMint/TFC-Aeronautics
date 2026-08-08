package ru.tfc_aeronautics.fluid;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.dries007.tfc.common.blocks.MoltenFluidBlock;

import ru.tfc_aeronautics.Aeronautics;

/**
 * Registers the molten magmatite fluid block, mirroring TFC's
 * {@code TFCBlocks.METAL_FLUIDS} pattern (one {@link MoltenFluidBlock} per metal).
 */
public final class AeronauticsFluidBlocks
{
    public static final DeferredRegister.Blocks BLOCKS =
        DeferredRegister.createBlocks(Aeronautics.MOD_ID);

    public static final DeferredHolder<Block, MoltenFluidBlock> MOLTEN_MAGMATITE =
        BLOCKS.register("fluid/molten_magmatite", () -> new MoltenFluidBlock(
            AeronauticsFluids.MOLTEN_MAGMATITE::getSource,
            Block.Properties.ofFullCopy(Blocks.LAVA).noLootTable()));

    private AeronauticsFluidBlocks() {}

    public static void register(IEventBus bus)
    {
        BLOCKS.register(bus);
    }
}