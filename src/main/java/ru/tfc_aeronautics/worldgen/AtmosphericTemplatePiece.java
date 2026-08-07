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
 * The single template piece of {@link AtmosphericTemplateStructure}.
 *
 * <p>Rotation pivots on the template's bottom-centre so a rotated footprint keeps the
 * same centre column (the one whose surface height was sampled). Structure voids are
 * dropped so terrain around the structure stays untouched.
 *
 * <p>The {@link LocalMaterialProcessor} is built per-placement: {@link #postProcess}
 * samples the world for local soil / rock / wood, then writes the resolved processor
 * into {@code placeSettings} before super places the template.
 */
public class AtmosphericTemplatePiece extends TemplateStructurePiece {
    private static final String ROTATION_KEY = "Rotation";
    private static final String CRACKED_CHANCE_KEY = "CrackedChance";
    private static final String MOSSY_CHANCE_KEY = "MossyChance";
    private static final String REPLACE_CROPS_KEY = "ReplaceCrops";
    private static final BlockIgnoreProcessor IGNORE_VOIDS =
        new BlockIgnoreProcessor(List.of(Blocks.STRUCTURE_VOID, Blocks.STRUCTURE_BLOCK));

    private final LocalMaterialProcessor.MaterialConfig materialConfig;

    public AtmosphericTemplatePiece(
        StructureTemplateManager manager,
        ResourceLocation template,
        BlockPos pos,
        Rotation rotation,
        LocalMaterialProcessor.MaterialConfig materialConfig
    ) {
        super(AeronauticsStructurePieceTypes.ATMOSPHERIC_TEMPLATE.get(), 0, manager, template, template.toString(),
            makeSettings(manager, template, rotation), pos);
        this.materialConfig = materialConfig;
        ru.tfc_aeronautics.Aeronautics.LOGGER.info("AtmosphericTemplatePiece constructed: template={}, material={}", template, materialConfig);
    }

    public AtmosphericTemplatePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(AeronauticsStructurePieceTypes.ATMOSPHERIC_TEMPLATE.get(), tag, context.structureTemplateManager(),
            id -> makeSettings(context.structureTemplateManager(), id, Rotation.valueOf(tag.getString(ROTATION_KEY))));
        // Vanilla's chunk generation pipeline reads the freshly-built piece back from the
        // chunk's NBT before running postProcess (it round-trips structure starts through
        // the chunk's data attachments so the postProcess pass operates on the same
        // instance that will be saved). The previous version of this constructor always
        // pinned materialConfig to DEFAULT, which silently dropped cracked/mossy chances
        // for any chunk that took the reload path — typically chunks generated while the
        // player is far from spawn, where vanilla's structure-start caching is more
        // aggressive. Persist the three postProcess-relevant fields into NBT and read them
        // back here so both code paths land on the same MaterialConfig.
        ru.tfc_aeronautics.Aeronautics.LOGGER.info("AtmosphericTemplatePiece deserialized: template={}, tagKeys={}", tag.contains("Template") ? tag.getString("Template") : "?", tag.getAllKeys());
        final float cracked = tag.contains(CRACKED_CHANCE_KEY) ? tag.getFloat(CRACKED_CHANCE_KEY) : 0f;
        final float mossy = tag.contains(MOSSY_CHANCE_KEY) ? tag.getFloat(MOSSY_CHANCE_KEY) : 0f;
        final boolean replaceCrops = tag.contains(REPLACE_CROPS_KEY) && tag.getBoolean(REPLACE_CROPS_KEY);
        this.materialConfig = new LocalMaterialProcessor.MaterialConfig(cracked, mossy, replaceCrops, false, 0, false);
    }

    private static StructurePlaceSettings makeSettings(StructureTemplateManager manager, ResourceLocation template, Rotation rotation) {
        // Pivot on the template's bottom-centre so rotated footprints share the same
        // surface column as the un-rotated one. Vanilla's getBoundingBox follows this
        // pivot, so the chunk generator's load set tracks the rotated footprint.
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
        // Persist the postProcess-relevant fields so a deserialized piece (vanilla
        // round-trips structure pieces through chunk NBT before postProcess) ends up
        // with the same cracked/mossy chances as the freshly-built instance. The other
        // MaterialConfig fields (requiresIronOre, oreSearchRadius, placeSurfaceMarker)
        // are only consulted at findGenerationPoint / afterPlace time, which never run
        // for a deserialized piece, so they don't need to round-trip.
        tag.putFloat(CRACKED_CHANCE_KEY, materialConfig.crackedChance());
        tag.putFloat(MOSSY_CHANCE_KEY, materialConfig.mossyChance());
        tag.putBoolean(REPLACE_CROPS_KEY, materialConfig.replaceCrops());
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
        // Resolve local soil / rock / wood before any template block overwrites the
        // terrain we sample from. The processor is built once for this placement and
        // applied to every block of the template.
        placeSettings.clearProcessors()
            .addProcessor(IGNORE_VOIDS)
            .addProcessor(LocalMaterialProcessor.resolve(level, boundingBox, random, materialConfig));
        super.postProcess(level, structureManager, generator, random, box, chunkPos, pos);
        // Walk the placeSettings processors back to find the LocalMaterialProcessor we
        // just installed and emit its brick stats; vanilla holds the same reference we
        // returned, but only by iteration order.
        placeSettings.getProcessors().stream()
            .filter(p -> p instanceof LocalMaterialProcessor)
            .map(p -> (LocalMaterialProcessor) p)
            .forEach(LocalMaterialProcessor::logBrickStats);
    }

    @Override
    protected void handleDataMarker(String name, BlockPos pos, ServerLevelAccessor level, RandomSource random, BoundingBox box) {
        // No data markers in these templates.
    }
}
