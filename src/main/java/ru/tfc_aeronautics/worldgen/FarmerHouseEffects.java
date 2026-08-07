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

import net.dries007.tfc.common.blockentities.LargeVesselBlockEntity;
import net.dries007.tfc.common.blockentities.ToolRackBlockEntity;
import net.dries007.tfc.common.blocks.crop.Crop;
import net.dries007.tfc.common.capabilities.InventoryItemHandler;

import ru.tfc_aeronautics.Aeronautics;

import java.util.ArrayList;
import java.util.List;

/**
 * Fills the containers inside a farmer house.
 *
 * <p>Vessel loot (food + seeds) is rolled by {@link #fillVessel}: the food and seed
 * item come from {@link FarmerHouseCrops#pick}, which keys its decision on the local
 * TFC climate — the same climate the {@link LocalMaterialProcessor} consults when it
 * rewrites the template's placeholder crop block, so the crop on the farmland and
 * the loot in the vessel always match. Tool rack loot (a hoe / scythe) is still
 * rolled from the JSON table by {@link #fillToolRack}.
 */
public final class FarmerHouseEffects
{
    public static final String VESSEL_ID = Aeronautics.MOD_ID + ":farmer_house_vessel";
    public static final String TOOL_RACK_ID = Aeronautics.MOD_ID + ":farmer_house_tool_rack";

    private static final ResourceKey<LootTable> TOOL_RACK_TABLE = ResourceKey.create(
        net.minecraft.core.registries.Registries.LOOT_TABLE,
        ResourceLocation.fromNamespaceAndPath(Aeronautics.MOD_ID, "farmer_house_tool_rack"));

    private FarmerHouseEffects() {}

    public static void register()
    {
        AtmosphereSpec.Effect.register(VESSEL_ID, FarmerHouseEffects::fillVessel);
        AtmosphereSpec.Effect.register(TOOL_RACK_ID, FarmerHouseEffects::fillToolRack);
    }

    private static void fillVessel(WorldGenLevel level, RandomSource random, BlockPos center, BoundingBox box)
    {
        // Pick the same crop the LocalMaterialProcessor placed on the farmland. Both
        // key on centre BlockPos so the vessel's contents always match what's growing.
        final Crop crop = FarmerHouseCrops.pick(level, center).orElse(Crop.WHEAT);
        final net.minecraft.world.item.Item foodItem = FarmerHouseCrops.foodItem(crop);
        final net.minecraft.world.item.Item seedItem = FarmerHouseCrops.seedItem(crop);

        // 3-16 of the food, 4-16 of the seeds — same ranges the old JSON loot table
        // used, so per-vessel totals stay comparable.
        final int foodCount = 3 + random.nextInt(14);
        final int seedCount = 4 + random.nextInt(13);

        final List<ItemStack> loot = new ArrayList<>();
        if (foodItem != null)
        {
            loot.add(new ItemStack(foodItem, foodCount));
        }
        if (seedItem != null)
        {
            loot.add(new ItemStack(seedItem, seedCount));
        }

        ContainerLootFiller.fillWithCodeLoot(level, random, center, VESSEL_ID, FarmerHouseEffects::fillLargeVessel, loot);
    }

    private static void fillLargeVessel(BlockEntity blockEntity, List<ItemStack> loot, ServerLevel level, BlockPos pos)
    {
        if (!(blockEntity instanceof LargeVesselBlockEntity vessel))
        {
            return;
        }
        if (vessel.getLevel() == null)
        {
            vessel.setLevel(level);
        }
        if (!(vessel.getInventory() instanceof InventoryItemHandler inventory))
        {
            return;
        }
        final NonNullList<ItemStack> stacks = inventory.getInternalStacks();
        if (!ContainerLootFiller.isEmpty(stacks))
        {
            return;
        }
        ContainerLootFiller.writeLoot(stacks, loot);
    }

    private static void fillToolRack(WorldGenLevel level, RandomSource random, BlockPos center, BoundingBox box)
    {
        ContainerLootFiller.fill(level, random, center, TOOL_RACK_TABLE, FarmerHouseEffects::fillToolRackEntity);
    }

    private static void fillToolRackEntity(BlockEntity blockEntity, List<ItemStack> loot, ServerLevel level, BlockPos pos)
    {
        if (!(blockEntity instanceof ToolRackBlockEntity rack))
        {
            return;
        }
        // Place the rolled item in the first empty slot. setStackInSlot is safe on a
        // vanilla ItemStackHandler (no-op onContentsChanged).
        final net.neoforged.neoforge.items.ItemStackHandler inventory = rack.getInventory();
        for (ItemStack stack : loot)
        {
            if (stack.isEmpty())
            {
                continue;
            }
            for (int slot = 0; slot < inventory.getSlots(); slot++)
            {
                if (inventory.getStackInSlot(slot).isEmpty())
                {
                    inventory.setStackInSlot(slot, stack);
                    return;
                }
            }
        }
    }
}