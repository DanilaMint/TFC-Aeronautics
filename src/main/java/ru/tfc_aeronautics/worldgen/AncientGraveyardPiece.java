package ru.tfc_aeronautics.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.List;

/**
 * The single template piece of {@link AncientGraveyardStructure}.
 *
 * <p>Rotation pivots on the template centre so a rotated tomb keeps the same footprint and
 * the protruding cap stays on the column whose surface height was sampled. Structure voids
 * are dropped so buried faces leave the surrounding terrain intact; real air blocks in the
 * template still carve out the chamber.
 */
public class AncientGraveyardPiece extends TemplateStructurePiece {
    private static final String ROTATION_KEY = "Rotation";
    private static final BlockIgnoreProcessor IGNORE_VOIDS =
        new BlockIgnoreProcessor(List.of(Blocks.STRUCTURE_VOID, Blocks.STRUCTURE_BLOCK));

    public AncientGraveyardPiece(StructureTemplateManager manager, ResourceLocation template, BlockPos pos, Rotation rotation) {
        super(AeronauticsStructurePieceTypes.ANCIENT_GRAVEYARD.get(), 0, manager, template, template.toString(),
            makeSettings(manager, template, rotation), pos);
    }

    public AncientGraveyardPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(AeronauticsStructurePieceTypes.ANCIENT_GRAVEYARD.get(), tag, context.structureTemplateManager(),
            id -> makeSettings(context.structureTemplateManager(), id, Rotation.valueOf(tag.getString(ROTATION_KEY))));
    }

    private static StructurePlaceSettings makeSettings(StructureTemplateManager manager, ResourceLocation template, Rotation rotation) {
        // Pivot is the un-rotated centre of the template bottom: the block at template-local
        // (sizeX/2, sizeY-1, sizeZ/2) — the protruding brick — sits on this column and so
        // lands at the world column sampled by the structure for every rotation. Vanilla's
        // StructureTemplate.getBoundingBox computes the bbox around this pivot, so the chunk
        // generator's load set follows the rotated footprint automatically.
        final Vec3i size = manager.getOrCreate(template).getSize();
        return new StructurePlaceSettings()
            .setRotation(rotation)
            .setRotationPivot(new BlockPos(size.getX() / 2, 0, size.getZ() / 2))
            .addProcessor(IGNORE_VOIDS);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.putString(ROTATION_KEY, placeSettings.getRotation().name());
    }

    @Override
    public void postProcess(
        WorldGenLevel level,
        StructureManager structureManager,
        ChunkGenerator generator,
        RandomSource random,
        BoundingBox box,
        ChunkPos chunkPos,
        BlockPos pos
    ) {
        // Resolve local soil / rock before any template block overwrites the terrain we sample.
        placeSettings.clearProcessors()
            .addProcessor(IGNORE_VOIDS)
            .addProcessor(GraveyardMaterialProcessor.resolve(level, boundingBox, random));
        super.postProcess(level, structureManager, generator, random, box, chunkPos, pos);
    }

    @Override
    protected void handleDataMarker(String name, BlockPos pos, ServerLevelAccessor level, RandomSource random, BoundingBox box) {
        // No data markers in this template.
    }
}
