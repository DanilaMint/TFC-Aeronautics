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
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootTable;

import net.dries007.tfc.common.blockentities.AbstractFirepitBlockEntity;
import net.dries007.tfc.common.blockentities.LargeVesselBlockEntity;
import net.dries007.tfc.common.capabilities.InventoryItemHandler;

import ru.tfc_aeronautics.Aeronautics;

import java.util.List;

/**
 * Fills the containers inside an ancient shelter from loot tables.
 *
 * <p>The large vessel is a TFC custom inventory (not a {@code RandomizableContainer}), so
 * its loot is rolled here. The firepit ash is a TFC-specific counter — we set it directly
 * with {@link AbstractFirepitBlockEntity#setAsh}.
 */
public final class AncientShelterEffects {
    public static final String VESSEL_ID = Aeronautics.MOD_ID + ":ancient_shelter_vessel";
    public static final String ASH_ID = Aeronautics.MOD_ID + ":ancient_shelter_ash";

    private static final ResourceKey<LootTable> VESSEL_TABLE = ResourceKey.create(
        net.minecraft.core.registries.Registries.LOOT_TABLE,
        ResourceLocation.fromNamespaceAndPath(Aeronautics.MOD_ID, "ancient_shelter_vessel"));

    private static final ResourceKey<LootTable> ASH_TABLE = ResourceKey.create(
        net.minecraft.core.registries.Registries.LOOT_TABLE,
        ResourceLocation.fromNamespaceAndPath(Aeronautics.MOD_ID, "ancient_shelter_ash"));

    private AncientShelterEffects() {}

    public static void register() {
        AtmosphereSpec.Effect.register(VESSEL_ID, AncientShelterEffects::fillVessel);
        AtmosphereSpec.Effect.register(ASH_ID, AncientShelterEffects::fillAsh);
    }

    private static void fillVessel(WorldGenLevel level, RandomSource random, BlockPos center, BoundingBox box) {
        ContainerLootFiller.fill(level, random, center, VESSEL_TABLE, AncientShelterEffects::fillLargeVessel);
    }

    private static void fillLargeVessel(BlockEntity blockEntity, List<ItemStack> loot, ServerLevel level, BlockPos pos) {
        if (!(blockEntity instanceof LargeVesselBlockEntity vessel)) {
            return;
        }
        if (vessel.getLevel() == null) {
            vessel.setLevel(level);
        }
        if (!(vessel.getInventory() instanceof InventoryItemHandler inventory)) {
            return;
        }
        final NonNullList<ItemStack> stacks = inventory.getInternalStacks();
        if (!ContainerLootFiller.isEmpty(stacks)) {
            return;
        }
        ContainerLootFiller.writeLoot(stacks, loot);
    }

    private static void fillAsh(WorldGenLevel level, RandomSource random, BlockPos center, BoundingBox box) {
        ContainerLootFiller.fill(level, random, center, ASH_TABLE, AncientShelterEffects::fillFirepitAsh);
    }

    private static void fillFirepitAsh(BlockEntity blockEntity, List<ItemStack> loot, ServerLevel level, BlockPos pos) {
        if (!(blockEntity instanceof AbstractFirepitBlockEntity<?> firepit)) {
            return;
        }
        if (firepit.getAsh() > 0) {
            return;
        }
        for (ItemStack stack : loot) {
            if (stack.isEmpty()) {
                continue;
            }
            firepit.setAsh(firepit.getAsh() + stack.getCount());
        }
    }
}
