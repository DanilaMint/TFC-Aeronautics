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
 * Lets Create's spout pour molten metal from its tank into a TFC ceramic mold sitting
 * on a mold table underneath it: each tick the spout holds the belt item under the
 * spout, then drains up to the amount the matching {@code tfc:casting} recipe still
 * needs (100 mB for an empty ingot mold, etc.) and fills it into the mold's fluid
 * handler. Empty molds get filled to the recipe amount; partially-filled molds get
 * topped up with whatever the spout can spare (down to the recipe amount) so the mold
 * can continue on the belt with metal inside.
 *
 * Cast-item extraction is intentionally not handled here — the spout only fills, and
 * any subsequent casting/cooling/extraction is left to other stations.
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

        CastingRecipe recipe = CastingRecipe.get(mold);
        if (recipe == null)
            return 0;

        int amount = recipe.getFluidIngredient().amount();
        if (!recipe.getFluidIngredient().test(availableFluid))
            return 0;

        FluidStack existing = mold.getFluidInTank(0);
        int currentAmount = existing.getAmount();
        int remaining = amount - currentAmount;

        if (remaining <= 0)
            return 0;

        if (currentAmount > 0 && !existing.getFluid().equals(availableFluid.getFluid()))
            return 0;

        int drainable = Math.min(remaining, availableFluid.getAmount());
        if (drainable <= 0)
            return 0;

        if (simulate)
            return drainable;

        FluidStack toFill = new FluidStack(availableFluid.getFluid(), drainable);
        int filled = mold.fill(toFill, IFluidHandler.FluidAction.EXECUTE);

        moldTable.markForSync();

        return filled;
    }
}