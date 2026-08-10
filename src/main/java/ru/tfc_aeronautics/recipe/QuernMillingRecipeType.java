package ru.tfc_aeronautics.recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import ru.tfc_aeronautics.TFCAeronautics;

/**
 * Registers the {@code tfc_aeronautics:quern_milling} {@link RecipeType} and its
 * {@link RecipeSerializer} as {@link DeferredHolder}s so they can be referenced
 * from the millstone mixin and from JEI plugins.
 */
public final class QuernMillingRecipeType
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(TFCAeronautics.MOD_ID, "quern_milling");

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
        DeferredRegister.create(Registries.RECIPE_TYPE, TFCAeronautics.MOD_ID);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
        DeferredRegister.create(Registries.RECIPE_SERIALIZER, TFCAeronautics.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<QuernMillingRecipe>> TYPE =
        RECIPE_TYPES.register("quern_milling", () -> RecipeType.simple(ID));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<QuernMillingRecipe>> SERIALIZER =
        RECIPE_SERIALIZERS.register("quern_milling", () -> QuernMillingRecipeSerializer.INSTANCE);

    private QuernMillingRecipeType() {}
}
