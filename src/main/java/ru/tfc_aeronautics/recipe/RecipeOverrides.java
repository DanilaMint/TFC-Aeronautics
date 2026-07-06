package ru.tfc_aeronautics.recipe;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import ru.tfc_aeronautics.Aeronautics;

/**
 * Removes Create's vanilla crafting recipes for andesite alloy and shafts so
 * the only production path for shafts is TFC's anvil workflow (alloy ingot on
 * an anvil → shafts). The saw/cutting recipe (create:cutting/andesite_alloy)
 * is intentionally left alone.
 *
 * The IDs match what Create's datagen writes — {@code
 * create:crafting/<folder>/<name>}, with the {@code crafting/} prefix added
 * by {@code ProcessingRecipeBuilder#build} via
 * {@code recipeId.withPrefix(typeId.getPath() + "/")}.
 */
public final class RecipeOverrides
{
    private static final List<ResourceLocation> REMOVED = List.of(
        ResourceLocation.fromNamespaceAndPath("create", "crafting/materials/andesite_alloy"),
        ResourceLocation.fromNamespaceAndPath("create", "crafting/materials/andesite_alloy_from_zinc"),
        ResourceLocation.fromNamespaceAndPath("create", "crafting/kinetics/shaft")
    );

    private RecipeOverrides() {}

    public static void register()
    {
        NeoForge.EVENT_BUS.register(new RecipeOverrides());
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event)
    {
        RecipeManager manager = event.getServer().getRecipeManager();
        List<RecipeHolder<?>> kept = manager.getRecipes().stream()
            .filter(r -> !REMOVED.contains(r.id()))
            .toList();
        int removed = manager.getRecipes().size() - kept.size();
        manager.replaceRecipes(kept);
        Aeronautics.LOGGER.info("Removed {} recipes ({}); kept {}", removed, REMOVED, kept.size());
    }
}