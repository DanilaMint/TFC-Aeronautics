package ru.tfc_aeronautics.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock;

import ru.tfc_aeronautics.Aeronautics;

/**
 * Post-placement effect for the rich graveyard:
 *
 * <ul>
 *   <li>{@code rich_graveyard_marker} — places a single polished (smooth) stone block
 *       on the surface above the buried chamber so players have a visible trace.</li>
 * </ul>
 *
 * <p>The chamber's chest is populated by its vanilla {@code LootTable} NBT, which is
 * injected into the template at build time and resolved on first chest open.
 */
public final class RichGraveyardEffects {
    public static final String MARKER_ID = Aeronautics.MOD_ID + ":rich_graveyard_marker";

    private RichGraveyardEffects() {}

    public static void register() {
        AtmosphereSpec.Effect.register(MARKER_ID, RichGraveyardEffects::placeMarker);
    }

    private static void placeMarker(WorldGenLevel level, RandomSource random, BlockPos center, BoundingBox box) {
        final ServerLevel serverLevel = level.getLevel();
        Rock rock = null;
        try {
            final net.dries007.tfc.world.chunkdata.ChunkData data = net.dries007.tfc.world.chunkdata.ChunkData.get(
                level.getChunk(new BlockPos(center.getX(), 0, center.getZ())));
            if (data != net.dries007.tfc.world.chunkdata.ChunkData.EMPTY) {
                final net.dries007.tfc.world.settings.RockSettings settings = data.getRockData().getSurfaceRock(center.getX(), center.getZ());
                rock = LocalMaterialProcessor.lookupRock(settings);
            }
        } catch (RuntimeException e) {
            Aeronautics.LOGGER.warn("rich_graveyard_marker: failed to resolve surface rock", e);
        }
        if (rock == null) {
            rock = Rock.GRANITE;
        }

        final BlockState marker = TFCBlocks.ROCK_BLOCKS.get(rock).get(Rock.BlockType.SMOOTH).get().defaultBlockState();
        final ChunkGenerator generator = serverLevel.getChunkSource().getGenerator();

        final int surfaceY = generator.getBaseHeight(
            center.getX(), center.getZ(), Heightmap.Types.WORLD_SURFACE_WG,
            level, serverLevel.getChunkSource().randomState());

        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(center.getX(), surfaceY, center.getZ());
        try {
            level.setBlock(cursor, marker, 3);
            Aeronautics.LOGGER.info("rich_graveyard_marker placed {} at {}", marker, cursor);
        } catch (RuntimeException | LinkageError e) {
            Aeronautics.LOGGER.error("rich_graveyard_marker: failed to set block at {}", cursor, e);
        }
    }
}
