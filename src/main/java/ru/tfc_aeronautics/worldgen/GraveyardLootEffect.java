package ru.tfc_aeronautics.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import net.dries007.tfc.common.blockentities.LargeVesselBlockEntity;
import net.dries007.tfc.common.capabilities.InventoryItemHandler;

import ru.tfc_aeronautics.Aeronautics;

import java.util.List;

/**
 * Fills the large vessel buried inside an ancient graveyard from a loot table.
 *
 * <p>Registered as an {@link AtmosphereSpec.Effect} so the loot lives in the structure's
 * datapack JSON rather than in code. TFC's {@link LargeVesselBlockEntity} is not a
 * {@link net.minecraft.world.RandomizableContainer}, so the vanilla "roll the table when
 * the player first opens it" path is unavailable — the table is rolled here instead.
 *
 * <p>Vanilla calls {@code afterPlace} once per chunk a structure touches, so the effect
 * only fills vessels that are still empty and stays idempotent across chunk borders.
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
public final class GraveyardLootEffect implements AtmosphereSpec.Effect {
    public static final String ID = Aeronautics.MOD_ID + ":ancient_graveyard_loot";

    private static final ResourceKey<LootTable> LOOT_TABLE = ResourceKey.create(
        net.minecraft.core.registries.Registries.LOOT_TABLE,
        ResourceLocation.fromNamespaceAndPath(Aeronautics.MOD_ID, "ancient_graveyard"));

    private static final int SEARCH_RADIUS = 4;

    private GraveyardLootEffect() {}

    public static void register() {
        AtmosphereSpec.Effect.register(ID, new GraveyardLootEffect());
    }

    @Override
    public void run(WorldGenLevel level, RandomSource random, BlockPos center) {
        final ServerLevel serverLevel = level.getLevel();
        final LootTable table;
        try {
            table = serverLevel.getServer().reloadableRegistries().getLootTable(LOOT_TABLE);
        } catch (RuntimeException e) {
            Aeronautics.LOGGER.error("ancient_graveyard_loot: failed to resolve loot table {}", LOOT_TABLE, e);
            return;
        }
        if (table == LootTable.EMPTY) {
            return;
        }

        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
            for (int dy = -SEARCH_RADIUS; dy <= SEARCH_RADIUS; dy++) {
                for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    final BlockEntity blockEntity = level.getBlockEntity(cursor);
                    if (blockEntity instanceof LargeVesselBlockEntity vessel) {
                        try {
                            fill(vessel, table, serverLevel, cursor.immutable(), random);
                        } catch (RuntimeException | LinkageError e) {
                            Aeronautics.LOGGER.error("ancient_graveyard_loot: fill failed at {}", cursor, e);
                        }
                    }
                }
            }
        }
    }

    private static void fill(LargeVesselBlockEntity vessel, LootTable table, ServerLevel level, BlockPos pos, RandomSource random) {
        // ProtoChunk.addBlockEntity never calls setLevel(); without a level TFC's
        // InventoryBlockEntity NPEs on the first packet send. We need the level set
        // only for the loot-table call below, which dereferences it.
        if (vessel.getLevel() == null) {
            vessel.setLevel(level);
        }

        if (!(vessel.getInventory() instanceof InventoryItemHandler inventory)) {
            return;
        }

        final NonNullList<ItemStack> stacks = inventory.getInternalStacks();
        if (!isEmpty(stacks)) {
            return;
        }

        final List<ItemStack> loot = table.getRandomItems(new LootParams.Builder(level)
            .withParameter(LootContextParams.ORIGIN, pos.getCenter())
            .create(LootContextParamSets.CHEST), random.nextLong());

        int slot = 0;
        for (ItemStack stack : loot) {
            if (stack.isEmpty()) {
                continue;
            }
            while (slot < stacks.size() && !stacks.get(slot).isEmpty()) {
                slot++;
            }
            if (slot >= stacks.size()) {
                break;
            }
            stacks.set(slot, stack);
        }
    }

    private static boolean isEmpty(NonNullList<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
