package ru.aeronautics.client.jei;

import java.util.List;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import ru.tfc_aeronautics.TFCAeronautics;
import ru.tfc_aeronautics.condenser_coil.CondenserCoilRegistration;
import ru.tfc_aeronautics.recipe.DistillationRecipe;
import ru.tfc_aeronautics.recipe.DistillationRecipeType;

/**
 * JEI plugin for {@code tfc_aeronautics:distillation} recipes consumed by the
 * {@code condenser_coil} block. One {@link DistillationRecipeCategory} is
 * registered; the catalyst is the block-item form of the coil itself, so
 * placing the coil in the world and looking it up in JEI surfaces the
 * distillation category.
 *
 * <p>Modeled on {@code code_references/TerraFirmaCraft/.../jei/JEIIntegration.java}
 * but trimmed to a single category — {@code quern_milling} and any future
 * custom recipes get their own plugin classes (one per category keeps the
 * {@code @JeiPlugin} class small and the failure modes isolated).
 */
@JeiPlugin
public final class DistillationJeiPlugin implements IModPlugin
{
    public static final RecipeType<RecipeHolder<DistillationRecipe>> DISTILLATION =
        RecipeType.createRecipeHolderType(ResourceLocation.fromNamespaceAndPath(TFCAeronautics.MOD_ID, "distillation"));

    @Override
    public ResourceLocation getPluginUid()
    {
        return ResourceLocation.fromNamespaceAndPath(TFCAeronautics.MOD_ID, "jei_distillation");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry)
    {
        IJeiHelpers helpers = registry.getJeiHelpers();
        IGuiHelper gui = helpers.getGuiHelper();
        registry.addRecipeCategories(new DistillationRecipeCategory(DISTILLATION, gui));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registry)
    {
        List<RecipeHolder<DistillationRecipe>> holders = lookupRecipeHolders();
        if (!holders.isEmpty())
        {
            registry.addRecipes(DISTILLATION, holders);
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registry)
    {
        registry.addRecipeCatalyst(new ItemStack(CondenserCoilRegistration.CONDENSER_COIL_ITEM.get()), DISTILLATION);
    }

    /**
     * Pulls all loaded distillation recipes from the active client's recipe manager.
     * Returns an empty list when no client level is loaded yet (very early registration,
     * or running headless) — JEI handles an empty recipe list gracefully and the recipes
     * will appear on the next recipe manager refresh.
     */
    private static List<RecipeHolder<DistillationRecipe>> lookupRecipeHolders()
    {
        if (Minecraft.getInstance().level == null)
        {
            return List.of();
        }
        return Minecraft.getInstance().level.getRecipeManager()
            .getAllRecipesFor(DistillationRecipeType.TYPE.get());
    }
}
