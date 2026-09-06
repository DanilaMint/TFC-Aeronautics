package ru.tfc_aeronautics.recipe;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.material.Fluid;

import ru.tfc_aeronautics.recipe.DistillationRecipe.IntPair;

/**
 * JSON + network codec for {@link DistillationRecipe}.
 *
 * <p>The JSON shape is:
 * <pre>{@code
 * {
 *   "type": "tfc_aeronautics:distillation",
 *   "input": { "id": "<fluid>" } | { "tag": "<fluid_tag>" },
 *   "temperature_range": [min, max],
 *   "result":  "<fluid>",
 *   "residue": "<fluid>",
 *   "result_percent": 41,
 *   "rate": 0.2
 * }
 * }</pre>
 *
 * <p>The {@code input} field uses {@link com.mojang.serialization.Codec#mapEither}:
 * the decoder tries the {@code id} branch first (a fluid registry id), and if
 * that key is absent, falls back to the {@code tag} branch (a fluid
 * {@link TagKey}). We deliberately do <em>not</em> use
 * {@code net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient.FLAT_CODEC}
 * — that codec uses keys {@code fluid}/{@code fluid_tag} and requires an
 * {@code amount}, which is incompatible with our "compute volume at runtime"
 * semantics.
 *
 * <p>The {@code temperature_range} field is encoded as a JSON array of exactly
 * two ints (closed interval). The codec enforces the arity via
 * {@code Codec.INT.listOf().comapFlatMap(...)} in
 * {@link DistillationRecipe#INT_PAIR_CODEC}; values outside {@code [min, max]}
 * orderings are rejected by {@link IntPair}'s compact constructor.
 *
 * <p>The {@link StreamCodec} is symmetric to the JSON codec for the simple
 * scalar fields (fluid, percent, rate, temperature pair). For the
 * {@code input} selector the network form is a one-byte discriminator
 * ({@code false = id branch, true = tag branch}) followed by either a
 * {@link ResourceLocation} or the tag's {@link ResourceLocation} location —
 * tag identity is fully described by its location for our purposes because the
 * registry is {@link net.minecraft.core.registries.Registries#FLUID} in both
 * branches.
 */
public final class DistillationRecipeSerializer implements RecipeSerializer<DistillationRecipe>
{
    public static final DistillationRecipeSerializer INSTANCE = new DistillationRecipeSerializer();

    private static final MapCodec<DistillationRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        DistillationRecipe.INPUT_CODEC.fieldOf("input").forGetter(DistillationRecipe::input),
        DistillationRecipe.INT_PAIR_CODEC.fieldOf("temperature_range").forGetter(DistillationRecipe::temperature_range),
        DistillationRecipe.FLUID_CODEC.fieldOf("result").forGetter(DistillationRecipe::result),
        DistillationRecipe.FLUID_CODEC.fieldOf("residue").forGetter(DistillationRecipe::residue),
        com.mojang.serialization.Codec.INT.fieldOf("result_percent").forGetter(DistillationRecipe::result_percent),
        com.mojang.serialization.Codec.FLOAT.fieldOf("rate").forGetter(DistillationRecipe::rate)
    ).apply(i, DistillationRecipe::new));

    /**
     * Network codec.
     *
     * <p>Custom-written (rather than {@code ByteBufCodecs.fromCodec}) because
     * the {@code input} field is an {@link Either} of {@link ResourceLocation}
     * and {@link TagKey} — neither has a direct {@code StreamCodec} shape that
     * matches our JSON encoding one-to-one, so we serialize a discriminator
     * byte followed by the active branch's payload. Fluids are written via
     * {@link ByteBufCodecs#registry} (vanilla idiom; {@code Fluid} itself
     * does not expose a {@code STREAM_CODEC}).
     */
    private static final StreamCodec<RegistryFriendlyByteBuf, DistillationRecipe> STREAM_CODEC =
        new StreamCodec<>()
        {
            private static final StreamCodec<RegistryFriendlyByteBuf, Fluid> FLUID_STREAM_CODEC =
                ByteBufCodecs.registry(net.minecraft.core.registries.Registries.FLUID);

            @Override
            public DistillationRecipe decode(RegistryFriendlyByteBuf buf)
            {
                boolean isTag = buf.readBoolean();
                Either<ResourceLocation, TagKey<Fluid>> input;
                if (isTag)
                {
                    ResourceLocation tagLoc = ResourceLocation.STREAM_CODEC.decode(buf);
                    input = Either.right(TagKey.create(net.minecraft.core.registries.Registries.FLUID, tagLoc));
                }
                else
                {
                    ResourceLocation id = ResourceLocation.STREAM_CODEC.decode(buf);
                    input = Either.left(id);
                }
                IntPair temps = new IntPair(buf.readInt(), buf.readInt());
                Fluid result = FLUID_STREAM_CODEC.decode(buf);
                Fluid residue = FLUID_STREAM_CODEC.decode(buf);
                int percent = buf.readInt();
                float rate = buf.readFloat();
                return new DistillationRecipe(input, temps, result, residue, percent, rate);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, DistillationRecipe recipe)
            {
                Either<ResourceLocation, TagKey<Fluid>> input = recipe.input();
                if (input.right().isPresent())
                {
                    buf.writeBoolean(true);
                    ResourceLocation.STREAM_CODEC.encode(buf, input.right().get().location());
                }
                else
                {
                    buf.writeBoolean(false);
                    ResourceLocation.STREAM_CODEC.encode(buf, input.left().get());
                }
                buf.writeInt(recipe.temperature_range().min());
                buf.writeInt(recipe.temperature_range().max());
                FLUID_STREAM_CODEC.encode(buf, recipe.result());
                FLUID_STREAM_CODEC.encode(buf, recipe.residue());
                buf.writeInt(recipe.result_percent());
                buf.writeFloat(recipe.rate());
            }
        };

    private DistillationRecipeSerializer() {}

    @Override
    public MapCodec<DistillationRecipe> codec()
    {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, DistillationRecipe> streamCodec()
    {
        return STREAM_CODEC;
    }
}
