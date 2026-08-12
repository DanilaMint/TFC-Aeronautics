package ru.tfc_aeronautics.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import ru.tfc_aeronautics.TFCAeronautics;

import java.util.Optional;

/**
 * A single-template tomb buried just under the surface.
 *
 * <p>Placement is deliberately "atypical": the template is sunk so that only its topmost
 * layer sits at the surface height sampled at the chunk centre. In the shipped template
 * that layer contains a single mud brick, so all a player sees is one out-of-place block;
 * every other cell of that layer is {@code minecraft:structure_void} and leaves the
 * surrounding terrain untouched. The hollow interior comes from real air blocks in the
 * template.
 *
 * <p>Materials are not baked into the template — {@link GraveyardMaterialProcessor} is
 * attached at placement time and rewrites mud bricks / cobble / the large vessel to match
 * the local TFC soil, surface rock and a random glaze.
 */
public class AncientGraveyardStructure extends AtmosphericStructure {
    public static final MapCodec<AncientGraveyardStructure> CODEC = RecordCodecBuilder.<AncientGraveyardStructure>mapCodec(instance ->
        instance.group(
            Structure.settingsCodec(instance),
            AtmosphereSpec.CODEC.optionalFieldOf("atmosphere", AtmosphereSpec.NONE).forGetter(AtmosphericStructure::atmosphere),
            ResourceLocation.CODEC.fieldOf("template").forGetter(AncientGraveyardStructure::template)
        ).apply(instance, AncientGraveyardStructure::new)
    );

    private final ResourceLocation template;

    public AncientGraveyardStructure(StructureSettings settings, AtmosphereSpec atmosphere, ResourceLocation template) {
        super(settings, atmosphere);
        this.template = template;
    }

    public ResourceLocation template() {
        return template;
    }

    @Override
    public StructureType<?> type() {
        return StructureTypes.ANCIENT_GRAVEYARD.get();
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        try {
            final ChunkPos chunkPos = context.chunkPos();
            final int x = chunkPos.getMiddleBlockX();
            final int z = chunkPos.getMiddleBlockZ();
            // getBaseHeight returns the noise-based terrain height (no surface layer yet). The actual
            // surface after TFC's surface builder runs is 1 block above that (grass/sand over dirt/rock).
            // The "+1" compensates so the protruding brick lands on top of the surface, not inside it.
            final int surfaceY = context.chunkGenerator().getBaseHeight(
                x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState()) + 1;

            if (surfaceY <= context.chunkGenerator().getSeaLevel() + 1) {
                return Optional.empty();
            }

            final StructureTemplate structureTemplate = context.structureTemplateManager().getOrCreate(template);
            final Vec3i size = structureTemplate.getSize();
            if (size.getX() < 1 || size.getY() < 1 || size.getZ() < 1) {
                return Optional.empty();
            }

            final Rotation rotation = Rotation.getRandom(context.random());
            // Offset so the centre of the template's topmost layer — the block that pokes out —
            // lands one block below the sampled surface column (tomb is fully buried).
            // The pivot in AncientGraveyardPiece is the un-rotated centre of the template,
            // so the protruding brick column is preserved across all four rotations.
            final BlockPos origin = new BlockPos(
                x - size.getX() / 2,
                surfaceY - size.getY(),
                z - size.getZ() / 2
            );

            TFCAeronautics.LOGGER.info("Placing ancient_graveyard at chunk={}, surfaceY={}, origin={}, rotation={}", chunkPos, surfaceY, origin, rotation);

            return Optional.of(new GenerationStub(origin, builder -> builder.addPiece(
                new AncientGraveyardPiece(context.structureTemplateManager(), template, origin, rotation))));
        } catch (RuntimeException | LinkageError e) {
            // Vanilla catches anything thrown here and puts the chunk into a retry state; left
            // unlogged, the symptom is a fully silent hang in chunk generation. Surface it.
            TFCAeronautics.LOGGER.error("ancient_graveyard findGenerationPoint failed at chunk {}", context.chunkPos(), e);
            return Optional.empty();
        }
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
        // The box vanilla hands us is the chunk being written, not the structure; run
        // atmospheric effects around the pieces instead so the vessel is actually in range.
        if (!atmosphere().hasAtmosphere()) {
            return;
        }
        try {
            final net.minecraft.world.level.levelgen.structure.BoundingBox structureBox = pieces.calculateBoundingBox();
            atmosphere().runEffects(level, random, structureBox.getCenter(), structureBox);
        } catch (RuntimeException | LinkageError e) {
            TFCAeronautics.LOGGER.error("ancient_graveyard afterPlace (atmosphere) failed for chunk {}", chunkPos, e);
        }
    }
}
