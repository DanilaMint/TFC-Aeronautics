package ru.tfc_aeronautics.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import net.dries007.tfc.common.blockentities.BarrelBlockEntity;
import net.dries007.tfc.common.blocks.devices.SealableDeviceBlock;
import net.dries007.tfc.common.capabilities.DelegateItemHandler;
import net.dries007.tfc.common.capabilities.InventoryItemHandler;

import ru.tfc_aeronautics.TFCAeronautics;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Fills the containers inside a tanner house.
 *
 * <ul>
 *   <li>The three barrels are filled with water (1000–5000 mB), limewater
 *       (1–2000 mB) and tannin (1–2000 mB), picked up in iteration order, then
 *       sealed back.</li>
 *   <li>The TFC chest is filled from {@code loot_table/tanner_house_chest.json}
 *       by writing items straight into its backing list. The vanilla
 *       {@code RandomizableContainer} lazy-roll path (NBT-injected
 *       {@code LootTable}) doesn't fire during worldgen for TFC chests, so the
 *       effect has to populate the inventory itself.</li>
 * </ul>
 */
public final class TannerHouseEffects {
    public static final String CHEST_ID = TFCAeronautics.MOD_ID + ":tanner_house_chest";
    public static final String BARREL_ID = TFCAeronautics.MOD_ID + ":tanner_house_barrel";

    private static final ResourceLocation LIMEWATER_ID = ResourceLocation.fromNamespaceAndPath("tfc", "limewater");
    private static final ResourceLocation TANNIN_ID = ResourceLocation.fromNamespaceAndPath("tfc", "tannin");

    private static final ResourceKey<LootTable> CHEST_TABLE = ResourceKey.create(
        net.minecraft.core.registries.Registries.LOOT_TABLE,
        ResourceLocation.fromNamespaceAndPath(TFCAeronautics.MOD_ID, "tanner_house_chest"));

    // The fluids are resolved at static-init time so the worldgen code path stays
    // lookup-free. TFC is a hard dependency, so resolving to EMPTY would mean the
    // mod is broken — log loudly and skip the barrel rather than crash.
    private static final List<BarrelSpec> BARREL_SPECS = List.of(
        new BarrelSpec(Fluids.WATER, 1000, 5000),
        new BarrelSpec(resolveFluid(LIMEWATER_ID), 1, 2000),
        new BarrelSpec(resolveFluid(TANNIN_ID), 1, 2000)
    );

    private TannerHouseEffects() {}

    public static void register() {
        AtmosphereSpec.Effect.register(CHEST_ID, TannerHouseEffects::fillChest);
        AtmosphereSpec.Effect.register(BARREL_ID, TannerHouseEffects::fillBarrels);
    }

    private static Fluid resolveFluid(ResourceLocation id) {
        final Fluid fluid = BuiltInRegistries.FLUID.get(id);
        if (fluid == null) {
            TFCAeronautics.LOGGER.error("TannerHouseEffects: required fluid {} is not registered", id);
            return Fluids.EMPTY;
        }
        return fluid;
    }

    private static void fillBarrels(WorldGenLevel level, RandomSource random, BlockPos center, BoundingBox box) {
        // specIndex is captured by the lambda and bumped per barrel we touch, so the
        // first barrel gets water, the second limewater, the third tannin. The
        // ContainerLootFiller walks barrels in world-space (dx, dy, dz) order, so
        // rotation can change which barrel gets which fluid — fine for a structure
        // whose three barrels are visually interchangeable.
        final int[] specIndex = {0};
        ContainerLootFiller.fillWithCodeLoot(level, random, center, BARREL_ID, (blockEntity, loot, serverLevel, pos) -> {
            if (!(blockEntity instanceof BarrelBlockEntity barrel)) {
                return;
            }
            final BarrelBlockEntity.BarrelInventory inventory = barrel.getInventory();
            if (inventory == null) {
                return;
            }

            clearItems(inventory);

            if (specIndex[0] >= BARREL_SPECS.size()) {
                return;
            }
            final BarrelSpec spec = BARREL_SPECS.get(specIndex[0]++);
            if (spec.fluid() == Fluids.EMPTY) {
                return;
            }
            final int amount = random.nextInt(spec.minAmount(), spec.maxAmount() + 1);
            inventory.fill(new FluidStack(spec.fluid(), amount), IFluidHandler.FluidAction.EXECUTE);

            seal(level, pos, barrel);
        }, List.of());
    }

    /**
     * Populates the TFC chest from the JSON loot table. TFC's chest extends
     * vanilla {@link ChestBlockEntity} (a {@code RandomizableContainer}), and
     * the template NBT injects the same {@code LootTable} reference we use
     * here — but routing writes through {@code setItem} lands on
     * {@link net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity#setItem},
     * which calls {@code unpackLootTable(null)} first. That recursive call
     * re-fills the container with its own seed and silently swallows our
     * writes (the second {@code super.setItem} lands on a slot vanilla just
     * touched, but the resulting mix doesn't reflect the JSON roll). We
     * bypass the whole override chain by reaching into the {@code items}
     * backing list via reflection — same trick {@link GraveyardLootEffect}
     * uses for TFC's vessel inventory, and the same reason: TFC wraps every
     * normal {@code setStackInSlot} in a sync chain that deadlocks chunk-gen
     * worker threads.
     */
    private static void fillChest(WorldGenLevel level, RandomSource random, BlockPos center, BoundingBox box) {
        ContainerLootFiller.fill(level, random, center, CHEST_TABLE, TannerHouseEffects::fillChestEntity);
    }

    private static void fillChestEntity(BlockEntity blockEntity, List<ItemStack> loot, net.minecraft.server.level.ServerLevel level, BlockPos pos) {
        if (!(blockEntity instanceof ChestBlockEntity chest)) {
            return;
        }
        final NonNullList<ItemStack> items;
        try {
            items = chestItems(chest);
        } catch (ReflectiveOperationException e) {
            TFCAeronautics.LOGGER.error("tanner_house_chest: failed to access chest items at {}", pos, e);
            return;
        }
        // Idempotency: skip if anything is already inside.
        if (!ContainerLootFiller.isEmpty(items)) {
            return;
        }
        // Suppress the vanilla lazy-roll — the LootTable key from the template NBT
        // is still set on the BE, and would otherwise fire the next time the chest
        // is opened and silently overwrite whatever we just wrote.
        chest.setLootTable(null);
        ContainerLootFiller.writeLoot(items, loot);
        chest.setChanged();
    }

    /**
     * Pulls the {@code items} backing list out of a {@link ChestBlockEntity} via
     * reflection. Vanilla declares the field {@code private} on
     * {@link ChestBlockEntity}; we have to bypass access checks. {@code TFCChestBlockEntity}
     * does not redeclare the field, so the superclass field is the canonical store.
     */
    private static NonNullList<ItemStack> chestItems(ChestBlockEntity chest) throws ReflectiveOperationException {
        final Field field = ChestBlockEntity.class.getDeclaredField("items");
        field.setAccessible(true);
        return (NonNullList<ItemStack>) field.get(chest);
    }

    /**
     * Flips the barrel to its sealed block state and notifies TFC. Done after
     * filling because {@link BarrelBlockEntity.BarrelInventory#fill} gates on
     * {@code !getBlockState().getValue(SEALED)} — sealing first would silently
     * drop the fill.
     */
    private static void seal(WorldGenLevel level, BlockPos pos, BarrelBlockEntity barrel) {
        final BlockState state = level.getBlockState(pos);
        if (!state.hasProperty(SealableDeviceBlock.SEALED) || state.getValue(SealableDeviceBlock.SEALED)) {
            return;
        }
        level.setBlock(pos, state.setValue(SealableDeviceBlock.SEALED, true), Block.UPDATE_ALL_IMMEDIATE);
        barrel.onSeal();
    }

    /**
     * Empties the barrel's item slots by writing EMPTY directly into the
     * {@link InventoryItemHandler} backing list. Bypasses the same TFC sync chain
     * that {@link ContainerLootFiller#writeLoot} avoids — see the class doc on
     * {@link ContainerLootFiller} for why writing through
     * {@code setStackInSlot} during chunk generation can deadlock.
     */
    private static void clearItems(BarrelBlockEntity.BarrelInventory inventory) {
        if (!(inventory instanceof DelegateItemHandler delegate)) {
            return;
        }
        if (!(delegate.getItemHandler() instanceof InventoryItemHandler itemHandler)) {
            return;
        }
        final NonNullList<ItemStack> stacks = itemHandler.getInternalStacks();
        for (int i = 0; i < stacks.size(); i++) {
            stacks.set(i, ItemStack.EMPTY);
        }
    }

    private record BarrelSpec(Fluid fluid, int minAmount, int maxAmount) {}
}

