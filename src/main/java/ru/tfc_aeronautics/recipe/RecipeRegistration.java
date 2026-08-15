package ru.tfc_aeronautics.recipe;

import net.neoforged.bus.api.IEventBus;

/**
 * Hooks the quern-milling {@link net.minecraft.world.item.crafting.RecipeType} and
 * {@link net.minecraft.world.item.crafting.RecipeSerializer} into the mod-event bus.
 * Called from {@link TFCAeronautics} during construction.
 */
public final class RecipeRegistration
{
    private RecipeRegistration() {}

    public static void register(IEventBus modEventBus)
    {
        QuernMillingRecipeType.RECIPE_TYPES.register(modEventBus);
        QuernMillingRecipeType.RECIPE_SERIALIZERS.register(modEventBus);
        // Recipe-removal stripping runs in mixin/RecipeManagerMixin; no event-bus
        // registration needed — @Inject fires on every RecipeManager reload.
        RecipeRemoval.LOGGER.debug(
            "Recipe removal armed for {} recipe(s): {}",
            RecipeRemoval.BANNED_RECIPES.size(),
            RecipeRemoval.BANNED_RECIPES
        );
    }
}
