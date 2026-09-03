package ru.tfc_aeronautics.mixin;

import java.util.List;

import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.foundation.recipe.RecipeApplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

import net.dries007.tfc.common.component.heat.HeatCapability;
import net.dries007.tfc.common.component.heat.IHeat;

/**
 * Preserves TFC item heat through Create's {@code create:pressing} recipe type.
 *
 * <p>Vanilla Create presses ({@code MechanicalPressBlockEntity}, belt presses,
 * sequenced assembly with pressing) build their output {@link ItemStack} via
 * {@link RecipeApplier#applyRecipeOn} — this method rolls the recipe's
 * {@code ProcessingOutput}s into fresh {@code ItemStack} references that carry
 * none of the input's data components. For a hot TFC ingot going through
 * {@code create:pressing/tight_sheet_copper} the resulting tight sheet would
 * therefore be cold, even though the {@code tfc/item_heat/copper_tight_sheet.json}
 * definition supports the heat component.
 *
 * <p>This mixin intercepts the level-overload of {@code applyRecipeOn} at
 * {@code RETURN}, copies the input's mutable {@link IHeat} onto every output
 * {@code ItemStack} using {@code setTemperatureIfWarmer} — exactly the same
 * pattern {@code StampingPressBlockEntity.assemble} and
 * {@code WeldingDepotBlockEntity.tryWeld} already use.
 *
 * <p><b>No-op cases.</b> The mixin does nothing when:
 * <ul>
 *   <li>the recipe is not a {@link PressingRecipe} (covers mixing, milling,
 *       compacting, washing, etc. — they go through the same {@code applyRecipeOn}
 *       but shouldn't gain TFC heat behaviour);</li>
 *   <li>the input lacks a heat component (cold / non-TFC item);</li>
 *   <li>an output lacks a heat component (no {@code tfc/item_heat} JSON) — we
 *       silently skip instead of crashing, so other mods' pressing recipes stay
 *       compatible.</li>
 * </ul>
 *
 * <p><b>Limitation.</b> Basin-mode pressing goes through
 * {@code BasinRecipe.apply} (with {@code recipe.getResultItem(...)} +
 * {@code basin.acceptOutputs(...)}) rather than {@code applyRecipeOn}, so
 * this mixin does not cover that path. For TFC metals basin-pressing is rare
 * (basin recipes are typically fluid-based) and can be addressed separately
 * if needed.
 */
@Mixin(RecipeApplier.class)
public abstract class RecipeApplierHeatMixin
{
    @Inject(method = "applyRecipeOn(Lnet/minecraft/world/level/Level;"
        + "Lnet/minecraft/world/item/ItemStack;"
        + "Lnet/minecraft/world/item/crafting/Recipe;Z)Ljava/util/List;",
        at = @At("RETURN"))
    private static void aeronautics$preserveHeatOnPressingOutputs(
        Level level, ItemStack stackIn, Recipe<?> recipe,
        boolean returnProcessingRemainder, CallbackInfoReturnable<List<ItemStack>> cir
    )
    {
        if (!(recipe instanceof PressingRecipe))
        {
            return;
        }
        if (stackIn.isEmpty())
        {
            return;
        }

        IHeat inputHeat = HeatCapability.get(stackIn);
        if (inputHeat == null)
        {
            return;
        }

        List<ItemStack> outputs = cir.getReturnValue();
        if (outputs == null)
        {
            return;
        }

        for (ItemStack out : outputs)
        {
            if (out.isEmpty())
            {
                continue;
            }
            IHeat outputHeat = HeatCapability.get(out);
            if (outputHeat != null)
            {
                outputHeat.setTemperatureIfWarmer(inputHeat);
            }
        }
    }
}
