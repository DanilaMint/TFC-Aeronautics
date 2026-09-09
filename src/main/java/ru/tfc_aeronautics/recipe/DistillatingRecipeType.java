package ru.tfc_aeronautics.recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import ru.tfc_aeronautics.TFCAeronautics;

/**
 * Registers the {@code tfc_aeronautics:distillating} {@link RecipeType} and its
 * {@link RecipeSerializer} as {@link DeferredHolder}s so they can be referenced
 * from the {@code condenser_coil} block entity and from JEI plugins.
 *
 * <p>Mirrors {@link QuernMillingRecipeType} in structure; the difference is
 * only in the namespace path ({@code distillating} vs {@code quern_milling})
 * and the backing serializer instance.
 */
public final class DistillatingRecipeType
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(TFCAeronautics.MOD_ID, "distillating");

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
        DeferredRegister.create(Registries.RECIPE_TYPE, TFCAeronautics.MOD_ID);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
        DeferredRegister.create(Registries.RECIPE_SERIALIZER, TFCAeronautics.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<DistillatingRecipe>> TYPE =
        RECIPE_TYPES.register("distillating", () -> RecipeType.simple(ID));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DistillatingRecipe>> SERIALIZER =
        RECIPE_SERIALIZERS.register("distillating", () -> DistillatingRecipeSerializer.INSTANCE);

    private DistillatingRecipeType() {}
}
