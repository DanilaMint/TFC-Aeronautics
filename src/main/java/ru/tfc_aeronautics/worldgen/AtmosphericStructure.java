package ru.tfc_aeronautics.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;

import java.util.Optional;

/**
 * Base class for structures with atypical mechanics.
 *
 * <p>Extends vanilla {@link Structure} directly (rather than {@code JigsawStructure}) so
 * that the framework is not coupled to jigsaw specifics — concrete subclasses can layer
 * jigsaw piece placement on top, or use any other layout strategy, while still benefiting
 * from the {@link AtmosphereSpec} post-placement hook.
 *
 * <p>The codec encodes a standard {@link StructureSettings} block plus an optional
 * {@code "atmosphere"} object carrying climate bounds and effect ids. Subclasses
 * typically reuse this codec verbatim via {@code Codec.xmap}-style composition when they
 * add extra fields of their own.
 *
 * <p>After all pieces of a structure are placed, vanilla calls {@link #afterPlace}; this
 * implementation delegates to {@link AtmosphereSpec#runEffects} at the structure's
 * bounding-box centre, dispatching any registered effect hooks.
 *
 * <p>The default {@link #findGenerationPoint} returns {@link Optional#empty()} so a
 * registered {@code AtmosphericStructure} instance placed in a structure-set generates no
 * pieces. Subclasses are expected to override it (typically by delegating to a jigsaw
 * piece placement strategy).
 */
public class AtmosphericStructure extends Structure {
    public static final MapCodec<AtmosphericStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Structure.settingsCodec(instance),
            AtmosphereSpec.CODEC.optionalFieldOf("atmosphere", AtmosphereSpec.NONE).forGetter(AtmosphericStructure::atmosphere)
        ).apply(instance, AtmosphericStructure::new)
    );

    private final AtmosphereSpec atmosphere;

    public AtmosphericStructure(StructureSettings settings, AtmosphereSpec atmosphere) {
        super(settings);
        this.atmosphere = atmosphere;
    }

    public AtmosphereSpec atmosphere() {
        return atmosphere;
    }

    @Override
    public StructureType<?> type() {
        return StructureTypes.ATMOSPHERIC.get();
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        return Optional.empty();
    }

    @Override
    public void afterPlace(
        WorldGenLevel level,
        StructureManager structureManager,
        ChunkGenerator generator,
        RandomSource random,
        BoundingBox box,
        ChunkPos chunkPos,
        PiecesContainer pieces
    ) {
        super.afterPlace(level, structureManager, generator, random, box, chunkPos, pieces);
        if (atmosphere.hasAtmosphere()) {
            BoundingBox structureBox = pieces.calculateBoundingBox();
            atmosphere.runEffects(level, random, structureBox.getCenter(), structureBox);
        }
    }
}