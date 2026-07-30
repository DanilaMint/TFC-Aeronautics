package ru.tfc_aeronautics.recipe;

import com.simibubi.create.api.behaviour.spouting.BlockSpoutingBehaviour;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.dries007.tfc.common.blockentities.MoldTableBlockEntity;
import net.dries007.tfc.common.component.mold.IMold;
import net.dries007.tfc.common.recipes.CastingRecipe;

/**
 * Lets Create's spout execute TFC's casting recipes: when the spout is placed above
 * a mold table holding an empty mold, and the spout's tank contains the molten metal
 * the matching {@code tfc:casting} recipe wants, the spout pours the recipe's fluid
 * amount (100 mB for ingot molds, etc.) into the mold and immediately produces the
 * cast item into the mold table's output slot.
 *
 * Registered against TFC's {@code mold_table} block entity via
 * {@link BlockSpoutingBehaviour#BY_BLOCK_ENTITY}.
 */
public enum SpoutCastingBehavior implements BlockSpoutingBehaviour {
    INSTANCE;

    @Override
    public int fillBlock(Level level, BlockPos pos, SpoutBlockEntity spout, FluidStack availableFluid, boolean simulate)
    {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof MoldTableBlockEntity moldTable))
            return 0;

        if (!moldTable.getOutputStack().isEmpty())
            return 0;

        ItemStack moldStack = moldTable.getMoldStack();
        if (moldStack.isEmpty())
            return 0;

        IMold mold = IMold.get(moldStack);
        if (mold == null)
            return 0;

        if (!mold.getFluidInTank(0).isEmpty())
            return 0;

        CastingRecipe recipe = CastingRecipe.get(mold);
        if (recipe == null)
            return 0;

        int amount = recipe.getFluidIngredient().amount();
        if (availableFluid.getAmount() < amount)
            return 0;

        if (!recipe.getFluidIngredient().test(availableFluid))
            return 0;

        if (simulate)
            return amount;

        ItemStack result = recipe.assemble(mold);
        if (result.isEmpty())
            return 0;

        moldTable.setOutputStack(result);
        mold.drainIgnoringTemperature(amount, IFluidHandler.FluidAction.EXECUTE);
        moldTable.markForSync();

        return amount;
    }
}