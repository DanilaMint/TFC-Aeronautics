package ru.tfc_aeronautics.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import net.dries007.tfc.common.blockentities.ToolRackBlockEntity;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.world.chunkdata.ChunkData;

import ru.tfc_aeronautics.Aeronautics;

import java.util.List;

/**
 * Fills the tool rack inside a smith house with a starter hammer and pads the floor
 * with gravel so a cavern directly below the structure can't collapse the smith's
 * floor.
 *
 * <p>The chest in the smith house is a vanilla {@code RandomizableContainer}, filled by
 * the {@code LootTable} NBT field injected into the template by
 * {@code tmp/inject_chest_loot.py}.
 */
public final class SmithHouseEffects {
    public static final String TOOL_RACK_ID = Aeronautics.MOD_ID + ":smith_house_tool_rack";
    public static final String FLOOR_PAD_ID = Aeronautics.MOD_ID + ":smith_house_floor_pad";

    private static final ResourceKey<net.minecraft.world.level.storage.loot.LootTable> TOOL_RACK_TABLE = ResourceKey.create(
        net.minecraft.core.registries.Registries.LOOT_TABLE,
        ResourceLocation.fromNamespaceAndPath(Aeronautics.MOD_ID, "smith_house_tool_rack"));

    /**
     * How deep the gravel cushion extends below the structure's bottom layer. Two blocks
     * is enough to catch most ravine ceilings; deeper would obscure ore veins a player
     * might be mining.
     */
    private static final int PAD_DEPTH = 2;

    private SmithHouseEffects() {}

    public static void register() {
        AtmosphereSpec.Effect.register(TOOL_RACK_ID, SmithHouseEffects::fillToolRack);
        AtmosphereSpec.Effect.register(FLOOR_PAD_ID, SmithHouseEffects::placeFloorPad);
    }

    private static void fillToolRack(WorldGenLevel level, RandomSource random, BlockPos center, BoundingBox box) {
        ContainerLootFiller.fill(level, random, center, TOOL_RACK_TABLE, SmithHouseEffects::fillToolRackEntity);
    }

    private static void fillToolRackEntity(BlockEntity blockEntity, List<ItemStack> loot, ServerLevel level, BlockPos pos) {
        if (!(blockEntity instanceof ToolRackBlockEntity rack)) {
            return;
        }
        final net.neoforged.neoforge.items.ItemStackHandler inventory = rack.getInventory();
        for (ItemStack stack : loot) {
            if (stack.isEmpty()) {
                continue;
            }
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                if (inventory.getStackInSlot(slot).isEmpty()) {
                    inventory.setStackInSlot(slot, stack);
                    return;
                }
            }
        }
    }

    /**
     * Drops a gravel cushion directly below the smith house footprint, one block at a time.
     * Replaces air pockets so the floor can't dangle over a cavern; leaves solid blocks
     * (dirt, stone, natural gravel) untouched.
     */
    private static void placeFloorPad(WorldGenLevel level, RandomSource random, BlockPos center, BoundingBox box) {
        if (box == null) {
            return;
        }
        final BlockState pad = resolvePadBlock(level, center);
        if (pad == null) {
            return;
        }
        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        final int top = box.minY() - 1;
        final int bottom = top - PAD_DEPTH + 1;
        for (int x = box.minX(); x <= box.maxX(); x++) {
            for (int z = box.minZ(); z <= box.maxZ(); z++) {
                for (int y = top; y >= bottom; y--) {
                    cursor.set(x, y, z);
                    final BlockState existing = level.getBlockState(cursor);
                    if (existing.isAir() || existing.liquid()) {
                        level.setBlock(cursor, pad, 3);
                    }
                }
            }
        }
    }

    /**
     * Resolves the cushion block to the local surface rock's gravel variant so the
     * padding blends into the terrain palette. Falls back to granite gravel if TFC
     * rock data is unavailable (non-TFC worlds).
     */
    private static BlockState resolvePadBlock(WorldGenLevel level, BlockPos center) {
        Rock rock = Rock.GRANITE;
        try {
            final ChunkData data = ChunkData.get(level.getChunk(new BlockPos(center.getX(), 0, center.getZ())));
            if (data != ChunkData.EMPTY) {
                final Rock resolved = LocalMaterialProcessor.lookupRock(
                    data.getRockData().getSurfaceRock(center.getX(), center.getZ()));
                if (resolved != null) {
                    rock = resolved;
                }
            }
        } catch (RuntimeException e) {
            Aeronautics.LOGGER.warn("smith_house_floor_pad: failed to resolve surface rock, using granite", e);
        }
        return TFCBlocks.ROCK_BLOCKS.get(rock).get(Rock.BlockType.GRAVEL).get().defaultBlockState();
    }
}