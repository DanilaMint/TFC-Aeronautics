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
import ru.tfc_aeronautics.recipe.DistillatingRecipe;
import ru.tfc_aeronautics.recipe.DistillatingRecipeType;

/**
 * JEI plugin for {@code tfc_aeronautics:distillating} recipes consumed by the
 * {@code condenser_coil} block. One {@link DistillatingRecipeCategory} is
 * registered; the catalyst is the block-item form of the coil itself, so
 * placing the coil in the world and looking it up in JEI surfaces the
 * distillating category.
 *
 * <p>Modeled on {@code code_references/TerraFirmaCraft/.../jei/JEIIntegration.java}
 * but trimmed to a single category — {@code quern_milling} and any future
 * custom recipes get their own plugin classes (one per category keeps the
 * {@code @JeiPlugin} class small and the failure modes isolated).
 */
@JeiPlugin
public final class DistillatingJeiPlugin implements IModPlugin
{
    public static final RecipeType<RecipeHolder<DistillatingRecipe>> DISTILLATING =
        RecipeType.createRecipeHolderType(ResourceLocation.fromNamespaceAndPath(TFCAeronautics.MOD_ID, "distillating"));

    @Override
    public ResourceLocation getPluginUid()
    {
        return ResourceLocation.fromNamespaceAndPath(TFCAeronautics.MOD_ID, "jei_distillating");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry)
    {
        IJeiHelpers helpers = registry.getJeiHelpers();
        IGuiHelper gui = helpers.getGuiHelper();
        registry.addRecipeCategories(new DistillatingRecipeCategory(DISTILLATING, gui));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registry)
    {
        List<RecipeHolder<DistillatingRecipe>> holders = lookupRecipeHolders();
        if (!holders.isEmpty())
        {
            registry.addRecipes(DISTILLATING, holders);
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registry)
    {
        registry.addRecipeCatalyst(new ItemStack(CondenserCoilRegistration.CONDENSER_COIL_ITEM.get()), DISTILLATING);
    }

    /**
     * Pulls all loaded distillating recipes from the active client's recipe manager.
     *
     * <p>Uses the network connection's {@code RecipeManager} rather than the
     * {@code ClientLevel}'s: the connection is established before the player
     * has a world loaded, and JEI's {@code registerRecipes} runs at mod-init
     * time when {@code level} is still {@code null} (the original
     * {@code level}-based lookup returned an empty list and JEI cached it as
     * "no recipes for this category", never re-querying on world load).
     *
     * <p>Returns an empty list only when the client is genuinely disconnected
     * (main menu before any singleplayer/multiplayer connection) — JEI will
     * re-invoke {@link #registerRecipes} on the next recipe-manager refresh.
     */
    private static List<RecipeHolder<DistillatingRecipe>> lookupRecipeHolders()
    {
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null)
        {
            return List.of();
        }
        return connection.getRecipeManager()
            .getAllRecipesFor(DistillatingRecipeType.TYPE.get());
    }
}
