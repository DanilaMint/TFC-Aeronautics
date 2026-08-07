package ru.tfc_aeronautics.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import net.dries007.tfc.common.capabilities.InventoryItemHandler;

import ru.tfc_aeronautics.Aeronautics;

import java.util.List;

/**
 * Shared search-and-fill helper used by every per-container {@link AtmosphereSpec.Effect}.
 *
 * <p>The loop walks a cube around the structure's bounding-box centre and dispatches each
 * matching block entity to a per-container filler. The filler owns its container type —
 * it inspects the BE, decides whether to skip it (already populated, wrong type, etc.),
 * and mutates the inventory in place.
 *
 * <p><strong>Why we mutate the stack list directly.</strong> TFC wires every
 * {@code setStackInSlot} through {@code onContentsChanged} → {@code setAndUpdateSlots}
 * → {@code markForSync} → {@code sendVanillaUpdatePacket} → chunk source's player map.
 * Calling that chain from a chunk-generation worker while the player is teleporting
 * into the same chunk deadlocks: {@code getPlayers} needs the chunk to be registered,
 * which only happens once the worker lets go of it. We sidestep the chain by writing
 * straight into TFC's {@link InventoryItemHandler#getInternalStacks()} backing list.
 * The next time the chunk is saved, vanilla serialises the BE from its in-memory
 * state, so the loot still reaches disk.
 */
final class ContainerLootFiller
{
    private static final int SEARCH_RADIUS = 4;

    private ContainerLootFiller() {}

    /**
     * Rolls {@code tableKey} once and dispatches the resulting loot to {@code filler}
     * for every matching block entity found around {@code center}. Use this when the
     * container's contents are described by a JSON loot table.
     */
    static void fill(WorldGenLevel level, RandomSource random, BlockPos center, ResourceKey<LootTable> tableKey, Filler filler)
    {
        final ServerLevel serverLevel = level.getLevel();
        final LootTable table;
        try
        {
            table = serverLevel.getServer().reloadableRegistries().getLootTable(tableKey);
        }
        catch (RuntimeException e)
        {
            Aeronautics.LOGGER.error("{}: failed to resolve loot table {}", tableKey, e);
            return;
        }
        if (table == LootTable.EMPTY)
        {
            return;
        }

        final List<ItemStack> loot = table.getRandomItems(new LootParams.Builder(serverLevel)
            .withParameter(LootContextParams.ORIGIN, center.getCenter())
            .create(LootContextParamSets.CHEST), random.nextLong());

        dispatch(level, random, center, tableKey.toString(), filler, loot);
    }

    /**
     * Dispatches {@code loot} to {@code filler} for every matching block entity found
     * around {@code center}, without rolling a loot table. Use this when the contents
     * are computed in code (e.g. from local world state). {@code effectId} is logged
     * on dispatch failure so the failing effect can be identified.
     */
    static void fillWithCodeLoot(WorldGenLevel level, RandomSource random, BlockPos center, String effectId, Filler filler, List<ItemStack> loot)
    {
        dispatch(level, random, center, effectId, filler, loot);
    }

    private static void dispatch(WorldGenLevel level, RandomSource random, BlockPos center, String effectId, Filler filler, List<ItemStack> loot)
    {
        final ServerLevel serverLevel = level.getLevel();
        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++)
        {
            for (int dy = -SEARCH_RADIUS; dy <= SEARCH_RADIUS; dy++)
            {
                for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++)
                {
                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    final BlockEntity blockEntity = level.getBlockEntity(cursor);
                    if (blockEntity == null)
                    {
                        continue;
                    }
                    try
                    {
                        filler.accept(blockEntity, loot, serverLevel, cursor.immutable());
                    }
                    catch (RuntimeException | LinkageError e)
                    {
                        Aeronautics.LOGGER.error("{}: fill failed at {}", effectId, cursor, e);
                    }
                }
            }
        }
    }

    /**
     * Writes {@code loot} into the first empty slots of {@code stacks}. Idempotent: if any
     * slot is already occupied, the call returns without mutating the list.
     */
    static void writeLoot(NonNullList<ItemStack> stacks, List<ItemStack> loot)
    {
        int slot = 0;
        for (ItemStack stack : loot)
        {
            if (stack.isEmpty())
            {
                continue;
            }
            while (slot < stacks.size() && !stacks.get(slot).isEmpty())
            {
                slot++;
            }
            if (slot >= stacks.size())
            {
                break;
            }
            stacks.set(slot, stack);
        }
    }

    static boolean isEmpty(NonNullList<ItemStack> stacks)
    {
        for (ItemStack stack : stacks)
        {
            if (!stack.isEmpty())
            {
                return false;
            }
        }
        return true;
    }

    @FunctionalInterface
    interface Filler
    {
        void accept(BlockEntity blockEntity, List<ItemStack> loot, ServerLevel level, BlockPos pos);
    }
}