package ru.tfc_aeronautics.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.List;
import java.util.Optional;

/**
 * Declarative specification of an atmospheric structure's "atypical" placement behaviour.
 *
 * <p>An {@code AtmosphereSpec} bundles two things:
 * <ul>
 *   <li>Optional climate bounds — when present, used to gate placement against TFC's
 *       climate model (temperature / rainfall). The bounds are matched against the
 *       position where the structure is being placed. If the position falls outside
 *       the bounds, the structure is skipped.</li>
 *   <li>A list of atmospheric effects — invoked from {@link AtmosphericStructure#afterPlace}
 *       after vanilla jigsaw pieces are placed. Effects are code-defined hooks
 *       ({@link Effect}); they can mutate the world (spawn entities, place extra blocks,
 *       modify loot, etc.). The spec only carries a list of {@code effectId} strings that
 *       the project resolves at runtime — keeping the framework JSON-driven while letting
 *       effects live in code.</li>
 * </ul>
 *
 * <p>The climate matcher is pluggable via {@link Resolver}; the default no-op resolver
 * always accepts, and a TFC-aware resolver is installed at runtime by the TFC integration
 * layer (see {@link #installResolver(Resolver)}).
 */
public record AtmosphereSpec(
    Optional<ClimateBounds> climateBounds,
    List<String> effectIds
) {
    public static final AtmosphereSpec NONE = new AtmosphereSpec(Optional.empty(), List.of());

    private static volatile Resolver ACTIVE_RESOLVER = Resolver.NOOP;

    public static final Codec<AtmosphereSpec> CODEC = RecordCodecBuilder.<AtmosphereSpec>mapCodec(instance ->
        instance.group(
            ClimateBounds.CODEC.optionalFieldOf("climate").forGetter(AtmosphereSpec::climateBounds),
            Codec.STRING.listOf().optionalFieldOf("effects", List.of()).forGetter(AtmosphereSpec::effectIds)
        ).apply(instance, AtmosphereSpec::new)
    ).codec();

    public boolean hasAtmosphere() {
        return climateBounds.isPresent() || !effectIds.isEmpty();
    }

    public boolean isClimateAcceptable(Level level, BlockPos pos, RandomSource random) {
        if (climateBounds.isEmpty()) {
            return true;
        }
        return ACTIVE_RESOLVER.matchesClimate(this, level, pos, random);
    }

    public void runEffects(WorldGenLevel level, RandomSource random, BlockPos center, BoundingBox box) {
        for (String id : effectIds) {
            Effect effect = Effect.REGISTRY.get(id);
            if (effect != null) {
                effect.run(level, random, center, box);
            }
        }
    }

    /**
     * Installs (or replaces) the climate resolver used by all {@code AtmosphereSpec}
     * instances. Intended for the TFC integration layer to swap in a real climate matcher
     * at startup; without this the matcher remains the no-op default.
     */
    public static void installResolver(Resolver resolver) {
        ACTIVE_RESOLVER = resolver;
    }

    /**
     * Climate parameter bounds used to gate placement. Values are world-coordinate bounds
     * over TFC's climate axes (temperature in °C, rainfall in mm). Bounds are inclusive
     * on both ends.
     */
    public record ClimateBounds(
        float minTemperature,
        float maxTemperature,
        float minRainfall,
        float maxRainfall
    ) {
        public static final Codec<ClimateBounds> CODEC = RecordCodecBuilder.<ClimateBounds>mapCodec(instance ->
            instance.group(
                Codec.FLOAT.optionalFieldOf("min_temperature", Float.NEGATIVE_INFINITY).forGetter(ClimateBounds::minTemperature),
                Codec.FLOAT.optionalFieldOf("max_temperature", Float.POSITIVE_INFINITY).forGetter(ClimateBounds::maxTemperature),
                Codec.FLOAT.optionalFieldOf("min_rainfall", Float.NEGATIVE_INFINITY).forGetter(ClimateBounds::minRainfall),
                Codec.FLOAT.optionalFieldOf("max_rainfall", Float.POSITIVE_INFINITY).forGetter(ClimateBounds::maxRainfall)
            ).apply(instance, ClimateBounds::new)
        ).codec();
    }

    /**
     * Pluggable climate matcher. Default implementation always accepts; a TFC-aware
     * implementation (installed via {@link AtmosphereSpec#installResolver}) hooks into
     * {@code ClimatePlacement} for real climate filtering.
     */
    @FunctionalInterface
    public interface Resolver {
        Resolver NOOP = (spec, level, pos, random) -> true;

        boolean matchesClimate(AtmosphereSpec spec, Level level, BlockPos pos, RandomSource random);
    }

    /**
     * Atmospheric effect hook. Effects are registered by id via {@link Effect#REGISTRY}
     * at startup and resolved when {@link AtmosphereSpec#runEffects} runs.
     *
     * <p>{@code box} is the bounding box covering every piece of the structure (computed
     * by vanilla from {@code PiecesContainer.calculateBoundingBox}); effects that need to
     * reason about the structure's footprint (e.g. placing a floor pad) use it, while
     * container-fillers can ignore it and search around {@code center}.
     */
    @FunctionalInterface
    public interface Effect {
        java.util.Map<String, Effect> REGISTRY = new java.util.concurrent.ConcurrentHashMap<>();

        void run(WorldGenLevel level, RandomSource random, BlockPos center, BoundingBox box);

        static void register(String id, Effect effect) {
            REGISTRY.put(id, effect);
        }
    }
}