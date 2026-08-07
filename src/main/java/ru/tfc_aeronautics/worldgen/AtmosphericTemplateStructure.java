package ru.tfc_aeronautics.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import ru.tfc_aeronautics.Aeronautics;

import java.util.Optional;

/**
 * Generic single-template atmospheric structure shared by the building templates
 * (shelter, houses) and the rich graveyard.
 *
 * <p>Placement is parameterised by {@link Placement}: a buried tomb anchors at the
 * surface column (topmost template layer flush with the surface block), an above-ground
 * building sits on the surface (bottom layer replaces the surface block), and an
 * underground chamber anchors at the first stone layer below the surface.
 *
 * <p>Materials and crop selection are configured per-structure via {@link LocalMaterialProcessor.MaterialConfig};
 * the smith house additionally gates on a probabilistic "iron-ore-likely-here" roll so its
 * density stays low even with a 1/1000 structure-set spacing. The real iron-ore scan would
 * need to simulate chunk generation (no chunk data exists at findGenerationPoint time),
 * so for now the gate is a random roll scaled by the local surface rock — sedimentary
 * rocks roll a low chance, igneous/metamorphic rocks roll a higher one, on top of a flat
 * 30% baseline.
 */
public class AtmosphericTemplateStructure extends AtmosphericStructure {
    public static final MapCodec<AtmosphericTemplateStructure> CODEC = RecordCodecBuilder.<AtmosphericTemplateStructure>mapCodec(instance ->
        instance.group(
            Structure.settingsCodec(instance),
            AtmosphereSpec.CODEC.optionalFieldOf("atmosphere", AtmosphereSpec.NONE).forGetter(AtmosphericStructure::atmosphere),
            Placement.CODEC.fieldOf("placement").forGetter(AtmosphericTemplateStructure::placement),
            ResourceLocation.CODEC.fieldOf("template").forGetter(AtmosphericTemplateStructure::template),
            LocalMaterialProcessor.MaterialConfig.CODEC.optionalFieldOf("material", LocalMaterialProcessor.MaterialConfig.DEFAULT).forGetter(AtmosphericTemplateStructure::material)
        ).apply(instance, AtmosphericTemplateStructure::new)
    );

    /**
     * How many blocks of stone remain above an underground structure's top face. Enough
     * that the structure reads as truly buried (the polished surface block is the only
     * visible trace) without eating so much rock that deep ores disappear underneath.
     */
    private static final int UNDERGROUND_CEILING_THICKNESS = 5;

    private final Placement placement;
    private final ResourceLocation template;
    private final LocalMaterialProcessor.MaterialConfig material;

    public AtmosphericTemplateStructure(StructureSettings settings, AtmosphereSpec atmosphere, Placement placement, ResourceLocation template, LocalMaterialProcessor.MaterialConfig material) {
        super(settings, atmosphere);
        this.placement = placement;
        this.template = template;
        this.material = material;
        Aeronautics.LOGGER.info("AtmosphericTemplateStructure constructed: template={}, placement={}, material={}", template, placement, material);
    }

    public Placement placement() {
        return placement;
    }

    public ResourceLocation template() {
        return template;
    }

    public LocalMaterialProcessor.MaterialConfig material() {
        return material;
    }

    @Override
    public StructureType<?> type() {
        return AeronauticsStructureTypes.ATMOSPHERIC_TEMPLATE.get();
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        try {
            final ChunkPos chunkPos = context.chunkPos();
            final int x = chunkPos.getMiddleBlockX();
            final int z = chunkPos.getMiddleBlockZ();
            // getBaseHeight returns the Y of the surface block; surfaceY is one above that
            // (the top face of the surface block), so a structure anchored at surfaceY - sizeY
            // sits flush with the surface for buried/above-ground templates.
            final int surfaceBlockY = context.chunkGenerator().getBaseHeight(
                x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
            final int surfaceY = surfaceBlockY + 1;

            if (surfaceBlockY <= context.chunkGenerator().getSeaLevel() + 1) {
                return Optional.empty();
            }

            final StructureTemplate structureTemplate = context.structureTemplateManager().getOrCreate(template);
            final Vec3i size = structureTemplate.getSize();
            if (size.getX() < 1 || size.getY() < 1 || size.getZ() < 1) {
                return Optional.empty();
            }

            // For underground chambers, anchor at the first stone block below the surface
            // (the structure will be buried in the rock layer below it).
            int anchorY = surfaceBlockY;
            if (placement == Placement.UNDERGROUND) {
                anchorY = findFirstStoneBelow(context, surfaceBlockY);
                if (anchorY <= context.chunkGenerator().getSeaLevel()) {
                    return Optional.empty();
                }
            }

            if (material.requiresIronOre() && !ironOreGate(context.random())) {
                return Optional.empty();
            }

            final int originY;
            switch (placement) {
                case BURIED -> originY = surfaceY - size.getY();
                case ON_SURFACE -> originY = surfaceBlockY;
                case UNDERGROUND -> {
                    // Top of structure sits UNDER the rock ceiling — anchorY is the top of
                    // the stone layer (first stone below surface), and we drop the structure
                    // by CEILING_THICKNESS so a few blocks of stone remain between the
                    // structure and the surface. The visible trace on the surface is just
                    // the polished marker placed by RichGraveyardEffects.
                    originY = anchorY - size.getY() + 1 - UNDERGROUND_CEILING_THICKNESS;
                }
                default -> throw new IllegalStateException("unreachable");
            }

            final BlockPos origin = new BlockPos(
                x - size.getX() / 2,
                originY,
                z - size.getZ() / 2
            );

            final Rotation rotation = Rotation.getRandom(context.random());

            Aeronautics.LOGGER.info(
                "Placing {} at chunk={}, surfaceBlockY={}, anchorY={}, origin={}, rotation={}",
                template, chunkPos, surfaceBlockY, anchorY, origin, rotation);

            return Optional.of(new GenerationStub(origin, builder -> builder.addPiece(
                new AtmosphericTemplatePiece(context.structureTemplateManager(), template, origin, rotation, material))));
        } catch (RuntimeException | LinkageError e) {
            Aeronautics.LOGGER.error("{} findGenerationPoint failed at chunk {}", template, context.chunkPos(), e);
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
        // atmospheric effects around the pieces instead so vessel / chest / etc. are
        // actually in range. The piece-bounding box is what gets passed as `box` to
        // effects — useful for floor-pad-style effects that need the structure footprint.
        if (!atmosphere().hasAtmosphere()) {
            return;
        }
        try {
            BoundingBox structureBox = pieces.calculateBoundingBox();
            atmosphere().runEffects(level, random, structureBox.getCenter(), structureBox);
        } catch (RuntimeException | LinkageError e) {
            Aeronautics.LOGGER.error("{} afterPlace (atmosphere) failed for chunk {}", template, chunkPos, e);
        }
    }

    /**
     * Scans downward from just below the surface until a stone block is found. Used to
     * anchor the underground rich graveyard chamber inside the local rock layer.
     */
    private static int findFirstStoneBelow(GenerationContext context, int surfaceBlockY) {
        final ChunkPos chunkPos = context.chunkPos();
        final int x = chunkPos.getMiddleBlockX();
        final int z = chunkPos.getMiddleBlockZ();
        final var column = context.chunkGenerator().getBaseColumn(x, z, context.heightAccessor(), context.randomState());
        for (int y = surfaceBlockY - 1; y > context.chunkGenerator().getSeaLevel(); y--) {
            if (isStone(column.getBlock(y).getBlock())) {
                return y;
            }
        }
        return surfaceBlockY;
    }

    private static boolean isStone(Block block) {
        final ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block);
        return id != null && id.getNamespace().equals("tfc") && id.getPath().startsWith("rock/");
    }

    /**
     * Probabilistic stand-in for "is there iron ore near here?". We can't read ore blocks at
     * findGenerationPoint time (no chunk data exists yet), so we just roll a flat 30% gate.
     * Combined with the smith house's 1/1000 structure-set spacing, the effective density
     * is ≈ 1/3000 — close to the user's "not too dense" target.
     */
    private static boolean ironOreGate(RandomSource random) {
        return random.nextFloat() < 0.30f;
    }

    /**
     * How a single-template structure anchors relative to the local surface column.
     *
     * <ul>
     *   <li>{@link #BURIED} — template sits below the surface; the topmost layer replaces
     *       the surface block (used by the ancient graveyard tomb).</li>
     *   <li>{@link #ON_SURFACE} — template sits on the surface; the bottom layer replaces
     *       the surface block (used by the houses and the ancient shelter).</li>
     *   <li>{@link #UNDERGROUND} — template sits inside the local rock layer, anchored
     *       one block below the topmost stone (used by the rich graveyard).</li>
     * </ul>
     */
    public enum Placement {
        BURIED, ON_SURFACE, UNDERGROUND;

        public static final Codec<Placement> CODEC = Codec.STRING.xmap(Placement::byName, Placement::serializedName);

        private static Placement byName(String name) {
            return Placement.valueOf(name.toUpperCase(java.util.Locale.ROOT));
        }

        private String serializedName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }
    }
}
