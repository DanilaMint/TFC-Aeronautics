package ru.tfc_aeronautics.recipe;

import java.util.Optional;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

/**
 * Recipe consumed by the {@code condenser_coil} block entity.
 *
 * <p>Each recipe describes a single distillating step:
 * <ul>
 *   <li>{@link #input} — the fluid being distilled, either a single
 *       {@link ResourceLocation} (resolved through {@link BuiltInRegistries#FLUID})
 *       or a {@link TagKey} of fluids (matched against the tank's current
 *       {@link FluidStack}). The {@link Either} shape is what the JSON codec
 *       enforces with {@code Codec.mapEither}.</li>
 *   <li>{@link #minTemperature} — нижняя граница в градусах Цельсия.
 *       Дистилляция идёт при любой температуре {@code t >= minTemperature};
 *       ниже порога — BE простаивает (matches возвращает true, но прогресс не двигается).</li>
 *   <li>{@link #result} / {@link #residue} — the two output fluids. Volumes
 *       are <em>not</em> declared on the recipe: the BE computes them at run-time
 *       as {@code total * result_percent / 100} and {@code total - result}.</li>
 *   <li>{@link #result_percent} — integer 0..100, fraction of the consumed
 *       input volume that goes into {@link #result}.</li>
 *   <li>{@link #rate} — float mB/tick at which the recipe drains the input tank.
 *       Independent of temperature, but temperature-gated (see
 *       {@link #temperatureInRange(float)}).</li>
 * </ul>
 *
 * <p>The class is intentionally a plain record-like holder (no inheritance from
 * Create's {@code ProcessingRecipe}): the BE does the work directly and does not
 * need Create's output-roll mechanics. Keeping it lightweight also avoids pulling
 * in Create-recipe mixin surface that is specific to the millstone pathway.
 */
public record DistillatingRecipe(
    Either<ResourceLocation, TagKey<Fluid>> input,
    int minTemperature,
    Fluid result,
    Fluid residue,
    int result_percent,
    float rate
) implements Recipe<RecipeInput>
{
    /**
     * Codec for the {@code input} field, exposed so the serializer and any
     * ad-hoc callers (e.g. tests, the BE during lookup) can share the exact
     * "either id or tag" semantics declared in the recipe JSON.
     *
     * <p>Wraps the two branches in {@link Codec#mapEither}: left is the bare
     * fluid id (registered in {@link BuiltInRegistries#FLUID}), right is a fluid
     * {@link TagKey}. The recipe author picks one in the JSON; mixing
     * both produces a decode error. Note that {@code Codec.mapEither} returns
     * a {@link MapCodec} (it operates on object-keyed fields, not bare values),
     * which is exactly what {@code RecordCodecBuilder.fieldOf} expects.
     */
    public static final MapCodec<Either<ResourceLocation, TagKey<Fluid>>> INPUT_CODEC =
        Codec.mapEither(
            ResourceLocation.CODEC.fieldOf("id"),
            TagKey.codec(Registries.FLUID).fieldOf("tag")
        );

    /**
     * Codec for {@link Fluid} restricted to fluids registered in
     * {@link BuiltInRegistries#FLUID}. Used by the serializer to decode the
     * {@code result} and {@code residue} fields, which are bare fluid id
     * strings in the recipe JSON (e.g. {@code "result": "minecraft:water"}).
     * Unknown ids resolve to a loader error via {@code byNameCodec}'s built-in
     * handling.
     */
    public static final Codec<Fluid> FLUID_CODEC = BuiltInRegistries.FLUID.byNameCodec();

    /**
     * Canonical record constructor. Field validation lives in {@link IntPair};
     * here we sanity-check the percent range and rate because they have no
     * natural home otherwise.
     */
    public DistillatingRecipe
    {
        if (result_percent < 0 || result_percent > 100)
        {
            throw new IllegalArgumentException("result_percent must be in [0, 100], got " + result_percent);
        }
        if (rate < 0.0f)
        {
            throw new IllegalArgumentException("rate must be non-negative, got " + rate);
        }
        ru.tfc_aeronautics.TFCAeronautics.LOGGER.info(
            "[diag] DistillatingRecipe constructed: input={}, min_temperature={}, result={}, residue={}, result_percent={}, rate={}",
            input, minTemperature, result, residue, result_percent, rate);
    }

    /**
     * Returns true if the given tank stack matches the recipe's input
     * selector.
     *
     * <p>If {@link #input} is a {@link ResourceLocation} (left branch),
     * the tank fluid must equal exactly that registered fluid.
     *
     * <p>If {@link #input} is a {@link TagKey} (right branch), the tank
     * fluid must be contained in the tag (delegated to
     * {@link FluidStack#is(TagKey)}). {@link FluidStack#isEmpty()} always
     * returns false so empty tanks never match.
     */
    public boolean matches(FluidStack tankStack)
    {
        if (tankStack == null || tankStack.isEmpty())
        {
            return false;
        }
        return input.map(
            id -> BuiltInRegistries.FLUID.get(id) == tankStack.getFluid(),
            tankStack::is
        );
    }

    /**
     * Lower-bound check: returns true iff {@code celsius >= minTemperature}.
     * Used by the BE to decide whether to advance production this tick.
     */
    public boolean temperatureInRange(float celsius)
    {
        return celsius >= minTemperature;
    }

    /**
     * Convenience accessor for the registered input fluid when the recipe
     * is the id-branch. Returns empty when the recipe uses a tag branch.
     */
    public Optional<Fluid> inputFluid()
    {
        return input.left().map(BuiltInRegistries.FLUID::get);
    }

    /**
     * Convenience accessor for the tag key when the recipe uses the
     * tag-branch. Returns empty when the recipe uses an id branch.
     */
    public Optional<TagKey<Fluid>> inputTag()
    {
        return input.right();
    }

    /**
     * Fraction of consumed input that ends up in {@link #result}, as a
     * multiplier in {@code [0.0, 1.0]}. Returned for callers (e.g. the BE)
     * that prefer a float over the integer percent representation.
     */
    public float resultFraction()
    {
        return result_percent / 100.0f;
    }

    // -------------------------------------------------------------------------
    // Recipe<RecipeInput> plumbing
    //
    // The vanilla RecipeManager requires recipes to implement Recipe<?> so they
    // can be looked up by type via getAllRecipesFor(). Distillating has no
    // item-based "crafting grid" semantics — inputs are fluids inside the
    // condenser_coil's tank, not items in an inventory — so the standard
    // RecipeInput-based matching/assembly methods are no-ops for our purposes.
    // The BE does the real matching via {@link #matches(FluidStack)} and
    // assembles outputs directly from the recipe fields.
    // -------------------------------------------------------------------------

    @Override
    public boolean matches(RecipeInput inv, Level level)
    {
        // Item-grid matching is meaningless for distillating; the BE does the
        // real (fluid-tank) matching through the other matches() overload.
        return false;
    }

    @Override
    public net.minecraft.world.item.ItemStack assemble(RecipeInput inv, net.minecraft.core.HolderLookup.Provider registries)
    {
        // No item output to assemble.
        return net.minecraft.world.item.ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height)
    {
        return false;
    }

    @Override
    public net.minecraft.world.item.ItemStack getResultItem(net.minecraft.core.HolderLookup.Provider registries)
    {
        // Distillating does not produce an item, only fluids.
        return net.minecraft.world.item.ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return DistillatingRecipeType.SERIALIZER.value();
    }

    @Override
    public RecipeType<?> getType()
    {
        return DistillatingRecipeType.TYPE.value();
    }
}
