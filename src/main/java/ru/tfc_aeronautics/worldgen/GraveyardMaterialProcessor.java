package ru.tfc_aeronautics.worldgen;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import net.dries007.tfc.common.blocks.LargeVesselBlock;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.settings.RockSettings;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Rewrites the ancient graveyard's building materials to match the terrain it is buried in.
 *
 * <p>The replacements are resolved once per placement by {@link #resolve} — reading the
 * surface soil and TFC rock data <em>before</em> the template overwrites them — and then
 * applied blindly per block. Source blocks are matched by family rather than by a specific
 * variant, so re-authoring the template with a different soil or rock still works.
 */
public class GraveyardMaterialProcessor extends StructureProcessor {
    /**
     * Never used from a processor list — the processor is built from the world at placement
     * time. It only exists to satisfy {@link #getType()}.
     */
    public static final MapCodec<GraveyardMaterialProcessor> CODEC = MapCodec.unit(GraveyardMaterialProcessor::passthrough);

    private static final float GLAZE_CHANCE = 0.5f;

    /** Soil families that actually appear as terrain, used to sniff the local soil variant. */
    private static final List<SoilBlockType> GROUND_TYPES = List.of(
        SoilBlockType.GRASS, SoilBlockType.DIRT, SoilBlockType.DUFF, SoilBlockType.COARSE_DIRT,
        SoilBlockType.ROOTED_DIRT, SoilBlockType.GRASS_PATH, SoilBlockType.FARMLAND,
        SoilBlockType.CLAY_GRASS, SoilBlockType.CLAY_DUFF, SoilBlockType.CLAY, SoilBlockType.MUD
    );

    private static final Map<Block, SoilBlockType.Variant> SOIL_VARIANTS = new HashMap<>();
    private static final Set<Block> MUD_BRICKS = new HashSet<>();
    private static final Set<Block> COBBLES = new HashSet<>();

    static {
        for (SoilBlockType.Variant variant : SoilBlockType.Variant.values()) {
            for (SoilBlockType type : GROUND_TYPES) {
                SOIL_VARIANTS.put(TFCBlocks.SOIL.get(type).get(variant).get(), variant);
            }
            MUD_BRICKS.add(TFCBlocks.SOIL.get(SoilBlockType.MUD_BRICKS).get(variant).get());
        }
        for (Rock rock : Rock.values()) {
            COBBLES.add(TFCBlocks.ROCK_BLOCKS.get(rock).get(Rock.BlockType.COBBLE).get());
            COBBLES.add(TFCBlocks.ROCK_BLOCKS.get(rock).get(Rock.BlockType.MOSSY_COBBLE).get());
        }
    }

    @Nullable private final BlockState mudBricks;
    @Nullable private final BlockState cobble;
    @Nullable private final Block vessel;

    private GraveyardMaterialProcessor(@Nullable BlockState mudBricks, @Nullable BlockState cobble, @Nullable Block vessel) {
        this.mudBricks = mudBricks;
        this.cobble = cobble;
        this.vessel = vessel;
    }

    private static GraveyardMaterialProcessor passthrough() {
        return new GraveyardMaterialProcessor(null, null, null);
    }

    /**
     * Samples the terrain the structure is about to be buried in and bakes the resulting
     * replacements into a processor instance.
     */
    public static GraveyardMaterialProcessor resolve(LevelReader level, BoundingBox box, RandomSource random) {
        final BlockPos center = box.getCenter();
        final DyeColor[] colors = DyeColor.values();
        return new GraveyardMaterialProcessor(
            resolveMudBricks(level, box, center),
            resolveCobble(level, center),
            random.nextFloat() < GLAZE_CHANCE
                ? TFCBlocks.GLAZED_LARGE_VESSELS.get(colors[random.nextInt(colors.length)]).get()
                : null
        );
    }

    @Nullable
    private static BlockState resolveMudBricks(LevelReader level, BoundingBox box, BlockPos center) {
        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = box.maxY() + 4; y >= box.minY(); y--) {
            cursor.set(center.getX(), y, center.getZ());
            final SoilBlockType.Variant variant = SOIL_VARIANTS.get(level.getBlockState(cursor).getBlock());
            if (variant != null) {
                return TFCBlocks.SOIL.get(SoilBlockType.MUD_BRICKS).get(variant).get().defaultBlockState();
            }
        }
        return null;
    }

    @Nullable
    private static BlockState resolveCobble(LevelReader level, BlockPos center) {
        try {
            final ChunkData data = ChunkData.get(level.getChunk(center));
            if (data == ChunkData.EMPTY) {
                return null;
            }
            final RockSettings rock = data.getRockData().getSurfaceRock(center.getX(), center.getZ());
            return rock.cobble().defaultBlockState();
        } catch (RuntimeException e) {
            // TFC rock data is unavailable (non-TFC world type, or chunk data not yet generated).
            return null;
        }
    }

    @Override
    @Nullable
    public StructureTemplate.StructureBlockInfo processBlock(
        LevelReader level,
        BlockPos offset,
        BlockPos pos,
        StructureTemplate.StructureBlockInfo blockInfo,
        StructureTemplate.StructureBlockInfo relativeBlockInfo,
        StructurePlaceSettings settings
    ) {
        final BlockState state = relativeBlockInfo.state();
        final Block block = state.getBlock();

        if (mudBricks != null && MUD_BRICKS.contains(block)) {
            return withState(relativeBlockInfo, mudBricks);
        }
        if (cobble != null && COBBLES.contains(block)) {
            return withState(relativeBlockInfo, cobble);
        }
        if (vessel != null && block instanceof LargeVesselBlock) {
            return withState(relativeBlockInfo, vessel.withPropertiesOf(state));
        }
        return relativeBlockInfo;
    }

    private static StructureTemplate.StructureBlockInfo withState(StructureTemplate.StructureBlockInfo info, BlockState state) {
        return new StructureTemplate.StructureBlockInfo(info.pos(), state, info.nbt());
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return AeronauticsProcessorTypes.GRAVEYARD_MATERIAL.get();
    }
}
