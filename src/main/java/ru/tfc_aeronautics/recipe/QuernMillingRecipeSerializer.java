package ru.tfc_aeronautics.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;

/**
 * JSON Serializer for {@link QuernMillingRecipe}. Reads a TFC {@code tfc:quern}-shaped
 * record ({@code ingredient}, {@code result} with stack + modifiers, {@code processing_time})
 * and decodes the {@link ItemStackProvider} via TFC's own codec so that custom modifiers
 * (e.g. {@code tfc:copy_food}) are parsed correctly.
 */
public final class QuernMillingRecipeSerializer implements RecipeSerializer<QuernMillingRecipe>
{
    public static final QuernMillingRecipeSerializer INSTANCE = new QuernMillingRecipeSerializer();

    private static final MapCodec<QuernMillingRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Ingredient.CODEC.fieldOf("ingredient").forGetter(QuernMillingRecipe::getIngredient),
        ItemStackProvider.CODEC.fieldOf("result").forGetter(QuernMillingRecipe::getResult),
        Codec.INT.optionalFieldOf("processing_time", 250).forGetter(r -> r.getProcessingDuration())
    ).apply(i, QuernMillingRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, QuernMillingRecipe> STREAM_CODEC = StreamCodec.composite(
        Ingredient.CONTENTS_STREAM_CODEC, QuernMillingRecipe::getIngredient,
        ItemStackProvider.STREAM_CODEC, QuernMillingRecipe::getResult,
        ByteBufCodecs.VAR_INT, QuernMillingRecipe::getProcessingDuration,
        QuernMillingRecipe::new
    );

    private QuernMillingRecipeSerializer() {}

    @Override
    public MapCodec<QuernMillingRecipe> codec()
    {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, QuernMillingRecipe> streamCodec()
    {
        return STREAM_CODEC;
    }
}
