package ru.tfc_aeronautics.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootTable;

import net.dries007.tfc.common.blockentities.BarrelBlockEntity;
import net.dries007.tfc.common.capabilities.DelegateItemHandler;
import net.dries007.tfc.common.capabilities.InventoryItemHandler;

import ru.tfc_aeronautics.Aeronautics;

import java.util.List;

/**
 * Fills the barrel inside a tanner house. The barrel's inventory is a
 * {@code BarrelInventory} that delegates to a TFC {@link InventoryItemHandler}; we
 * write directly into the inner handler's backing list to skip TFC's sync chain
 * (see {@link ContainerLootFiller}).
 */
public final class TannerHouseEffects {
    public static final String BARREL_ID = Aeronautics.MOD_ID + ":tanner_house_barrel";

    private static final ResourceKey<LootTable> BARREL_TABLE = ResourceKey.create(
        net.minecraft.core.registries.Registries.LOOT_TABLE,
        ResourceLocation.fromNamespaceAndPath(Aeronautics.MOD_ID, "tanner_house_barrel"));

    private TannerHouseEffects() {}

    public static void register() {
        AtmosphereSpec.Effect.register(BARREL_ID, TannerHouseEffects::fillBarrel);
    }

    private static void fillBarrel(WorldGenLevel level, RandomSource random, BlockPos center, BoundingBox box) {
        ContainerLootFiller.fill(level, random, center, BARREL_TABLE, TannerHouseEffects::fillBarrelEntity);
    }

    private static void fillBarrelEntity(BlockEntity blockEntity, List<ItemStack> loot, net.minecraft.server.level.ServerLevel level, BlockPos pos) {
        if (!(blockEntity instanceof BarrelBlockEntity barrel)) {
            return;
        }
        if (!(barrel.getInventory() instanceof DelegateItemHandler delegate)) {
            return;
        }
        if (!(delegate.getItemHandler() instanceof InventoryItemHandler inventory)) {
            return;
        }
        final NonNullList<ItemStack> stacks = inventory.getInternalStacks();
        if (!ContainerLootFiller.isEmpty(stacks)) {
            return;
        }
        ContainerLootFiller.writeLoot(stacks, loot);
    }
}
