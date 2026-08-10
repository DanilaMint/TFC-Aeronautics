package ru.tfc_aeronautics.recipe;

import com.simibubi.create.content.kinetics.millstone.MillingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;

import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;

/**
 * A milling recipe that wraps a TFC {@code tfc:quern}-shaped pair of
 * {@link Ingredient} + {@link ItemStackProvider} so that the result modifiers
 * (e.g. {@code tfc:copy_food}) actually run when the millstone processes it.
 *
 * <p>Inherits from {@link MillingRecipe} so it can be assigned to
 * {@code MillstoneBlockEntity.lastRecipe} (which is typed {@code MillingRecipe}).
 * The heavy lifting — applying the result with the real input — happens in
 * {@link ru.tfc_aeronautics.mixin.MillstoneBlockEntityMixin}.
 */
public final class QuernMillingRecipe extends MillingRecipe
{
    private final Ingredient ingredient;
    private final ItemStackProvider result;

    public QuernMillingRecipe(Ingredient ingredient, ItemStackProvider result, int processingTime)
    {
        super(new QuernMillingRecipeParams());
        // Populate the parent-class NonNullList fields post-super. The lists
        // originate from the params' protected fields, so adding to them here
        // also makes them visible to the inherited validate() / rollResults().
        this.ingredients.add(ingredient);
        this.results.add(new ProcessingOutput(Items.AIR, 0, 1F));
        this.processingDuration = processingTime;
        this.ingredient = ingredient;
        this.result = result;
        ru.tfc_aeronautics.TFCAeronautics.LOGGER.info("[diag] QuernMillingRecipe constructed: ingredient={}, processingDuration={}", ingredient, processingTime);
    }

    public Ingredient getIngredient()
    {
        return ingredient;
    }

    public ItemStackProvider getResult()
    {
        return result;
    }

    @Override
    public boolean matches(RecipeInput inv, Level worldIn)
    {
        if (inv.isEmpty())
        {
            return false;
        }
        return ingredient.test(inv.getItem(0));
    }
}
