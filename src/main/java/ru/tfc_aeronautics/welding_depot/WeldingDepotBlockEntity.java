package ru.tfc_aeronautics.welding_depot;

import java.util.List;

import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.simibubi.create.content.kinetics.press.PressingBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.dries007.tfc.common.component.heat.IHeat;
import net.dries007.tfc.common.recipes.RecipeHelpers;
import net.dries007.tfc.common.recipes.TFCRecipeTypes;
import net.dries007.tfc.common.recipes.WeldingRecipe;
import net.dries007.tfc.util.Helpers;

import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import ru.tfc_aeronautics.TFCAeronautics;

public class WeldingDepotBlockEntity extends SmartBlockEntity {

    public static final int SLOT_LEFT = 0;
    public static final int SLOT_RIGHT = 1;
    public static final int SLOT_FLUX = 2;
    public static final int SLOT_OUTPUT = 3;

    /**
     * Priority for extracting items from the depot:
     * 1. OUTPUT first — so the player gets the welding result immediately.
     * 2. Then LEFT and RIGHT — the input pieces (may still be useful).
     * 3. FLUX last — cheap to replace, lowest priority.
     */
    public static final int[] EXTRACT_PRIORITY = {
        SLOT_OUTPUT,
        SLOT_LEFT,
        SLOT_RIGHT,
        SLOT_FLUX,
    };

    private final ItemStackHandler inventory;
    private final WeldingDepotItemHandler externalHandler;
    private long lastWeldGameTime = Long.MIN_VALUE;
    private DirectBeltInputBehaviour beltInputBehaviour;

    public WeldingDepotBlockEntity(BlockPos pos, BlockState state) {
        super(WeldingDepotRegistration.WELDING_DEPOT_BE.get(), pos, state);
        this.inventory = new ItemStackHandler(4) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                boolean isFlux = Helpers.isItem(stack, TFCTags.Items.WELDING_FLUX);
                if (slot == SLOT_FLUX) return isFlux;
                if (slot == SLOT_OUTPUT) return false;
                return !isFlux;
            }

            @Override
            public int getSlotLimit(int slot) {
                return slot == SLOT_FLUX ? 64 : 1;
            }

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                setChanged();
                if (level != null && !level.isClientSide) notifyUpdate();
                ItemStack now = getStackInSlot(slot);
                TFCAeronautics.LOGGER.info("[welding_depot] at {}: slot {} -> {}",
                    worldPosition, slot, describe(now));
            }
        };
        this.externalHandler = new WeldingDepotItemHandler(inventory);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        beltInputBehaviour = new DirectBeltInputBehaviour(this)
            .setInsertionHandler(this::tryInsertingFromSide)
            .considerOccupiedWhen(this::isOccupied);
        behaviours.add(beltInputBehaviour);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (tag.contains("inventory", CompoundTag.TAG_COMPOUND))
            inventory.deserializeNBT(registries, tag.getCompound("inventory"));
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) return;
        long gameTime = level.getGameTime();
        boolean hasItems = !inventory.getStackInSlot(SLOT_LEFT).isEmpty()
            || !inventory.getStackInSlot(SLOT_RIGHT).isEmpty()
            || !inventory.getStackInSlot(SLOT_FLUX).isEmpty();
        if (hasItems && gameTime % 20 == 0) {
            BlockPos above1 = worldPosition.above();
            BlockPos above2 = worldPosition.above(2);
            BlockEntity be1 = level.getBlockEntity(above1);
            BlockEntity be2 = level.getBlockEntity(above2);
            MechanicalPressBlockEntity press = findPress();
            String pbDesc;
            if (press != null) {
                PressingBehaviour pb = press.getPressingBehaviour();
                pbDesc = String.format("running=%s, prev=%d, cur=%d, mode=%s",
                    pb.running, pb.prevRunningTicks, pb.runningTicks, pb.mode);
            } else {
                pbDesc = "n/a";
            }
            TFCAeronautics.LOGGER.info("[welding_depot] at {}: +1={} {}, +2={} {}, press={}",
                worldPosition,
                above1, be1 == null ? "null" : be1.getClass().getSimpleName(),
                above2, be2 == null ? "null" : be2.getClass().getSimpleName(),
                pbDesc);
        }
        MechanicalPressBlockEntity press = findPress();
        if (press == null) return;
        PressingBehaviour pb = press.getPressingBehaviour();
        if (pb.running) {
            int half = PressingBehaviour.CYCLE / 2;
            if (pb.prevRunningTicks < half && pb.runningTicks >= half) {
                tryWeld();
            }
            return;
        }
        if (press.getKineticSpeed() != 0 && slotsReadyForWeld()) {
            pb.start(PressingBehaviour.Mode.WORLD);
        }
    }

    /**
     * Returns the {@link MechanicalPressBlockEntity} that presses this depot, if any.
     * Checks both {@code above()} (press sitting directly on the depot) and
     * {@code above(2)} (press placed with a one-block gap — the press head reaches
     * down through the air block).
     */
    @Nullable
    private MechanicalPressBlockEntity findPress() {
        BlockEntity be = level.getBlockEntity(worldPosition.above());
        if (be instanceof MechanicalPressBlockEntity press) return press;
        be = level.getBlockEntity(worldPosition.above(2));
        if (be instanceof MechanicalPressBlockEntity press) return press;
        return null;
    }

    private boolean slotsReadyForWeld() {
        return !inventory.getStackInSlot(SLOT_LEFT).isEmpty()
            && !inventory.getStackInSlot(SLOT_RIGHT).isEmpty()
            && !inventory.getStackInSlot(SLOT_FLUX).isEmpty()
            && inventory.getStackInSlot(SLOT_OUTPUT).isEmpty();
    }

    private void tryWeld() {
        ItemStack left = inventory.getStackInSlot(SLOT_LEFT);
        ItemStack right = inventory.getStackInSlot(SLOT_RIGHT);
        ItemStack flux = inventory.getStackInSlot(SLOT_FLUX);
        ItemStack out = inventory.getStackInSlot(SLOT_OUTPUT);
        if (left.isEmpty() || right.isEmpty() || flux.isEmpty() || !out.isEmpty()) {
            TFCAeronautics.LOGGER.info("[welding_depot] at {}: slots not ready (L={}, R={}, F={}, out={})",
                worldPosition, describe(left), describe(right), describe(flux), describe(out));
            return;
        }

        WeldingInventory inv = new WeldingInventory(left, right, getTier());
        @Nullable RecipeHolder<WeldingRecipe> holder = RecipeHelpers.getHolder(level, TFCRecipeTypes.WELDING, inv);
        if (holder == null) {
            TFCAeronautics.LOGGER.info("[welding_depot] at {}: no welding recipe for L={}, R={}, tier={}",
                worldPosition, describe(left), describe(right), getTier());
            return;
        }
        WeldingRecipe recipe = holder.value();
        if (!recipe.isCorrectTier(getTier())) {
            TFCAeronautics.LOGGER.info("[welding_depot] at {}: recipe {} requires tier {}, depot tier {}",
                worldPosition, holder.id(), recipe.getTier(), getTier());
            return;
        }

        @Nullable IHeat leftHeat = HeatCapability.get(left);
        @Nullable IHeat rightHeat = HeatCapability.get(right);
        if ((leftHeat != null && !leftHeat.canWeld()) || (rightHeat != null && !rightHeat.canWeld())) {
            TFCAeronautics.LOGGER.info("[welding_depot] at {}: heat insufficient (L={}, R={})",
                worldPosition,
                leftHeat == null ? "n/a" : Float.toString(leftHeat.getTemperature()),
                rightHeat == null ? "n/a" : Float.toString(rightHeat.getTemperature()));
            return;
        }

        ItemStack result = recipe.assemble(inv);
        @Nullable IHeat resultHeat = HeatCapability.get(result);
        if (resultHeat != null) {
            resultHeat.setTemperatureIfWarmer(leftHeat);
            resultHeat.setTemperatureIfWarmer(rightHeat);
        }

        inventory.setStackInSlot(SLOT_OUTPUT, result);
        inventory.setStackInSlot(SLOT_LEFT, ItemStack.EMPTY);
        inventory.setStackInSlot(SLOT_RIGHT, ItemStack.EMPTY);
        inventory.getStackInSlot(SLOT_FLUX).shrink(1);
        TFCAeronautics.LOGGER.info("[welding_depot] at {}: welded into {} (tier={})",
            worldPosition, describe(result), getTier());
    }

    private static String describe(ItemStack stack) {
        if (stack.isEmpty()) return "empty";
        return stack.getItem().getDescriptionId() + "x" + stack.getCount();
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public IItemHandler getExternalHandler() {
        return externalHandler;
    }

    /**
     * Belt-insert callback for {@link DirectBeltInputBehaviour}: routes a stack
     * from the end of a Create belt into the depot. Flux → SLOT_FLUX (limit 64);
     * everything else → SLOT_LEFT then SLOT_RIGHT (limit 1 each). If the only
     * matching slot is occupied, returns the stack unchanged so the belt keeps it.
     */
    public ItemStack tryInsertingFromSide(TransportedItemStack transported, Direction side, boolean simulate) {
        ItemStack item = transported.stack;
        if (item.isEmpty()) return item;
        boolean isFlux = Helpers.isItem(item, TFCTags.Items.WELDING_FLUX);
        if (isFlux) {
            ItemStack current = inventory.getStackInSlot(SLOT_FLUX);
            if (current.getCount() >= inventory.getSlotLimit(SLOT_FLUX)) return item;
            return inventory.insertItem(SLOT_FLUX, item, simulate);
        }
        if (inventory.getStackInSlot(SLOT_LEFT).isEmpty())
            return inventory.insertItem(SLOT_LEFT, item, simulate);
        if (inventory.getStackInSlot(SLOT_RIGHT).isEmpty())
            return inventory.insertItem(SLOT_RIGHT, item, simulate);
        return item;
    }

    /**
     * Belt-input-occupied: both work-piece slots full means the depot can't
     * accept another ingot from the belt. Flux slot is intentionally excluded
     * — it does not block ingot delivery.
     */
    public boolean isOccupied(Direction side) {
        return !inventory.getStackInSlot(SLOT_LEFT).isEmpty()
            && !inventory.getStackInSlot(SLOT_RIGHT).isEmpty();
    }

    public int getTier() {
        return ((WeldingDepotBlock) getBlockState().getBlock()).getTier();
    }

    private record WeldingInventory(ItemStack main, ItemStack secondary, int tier)
        implements WeldingRecipe.Inventory {
        @Override public ItemStack getMain() { return main; }
        @Override public ItemStack getSecondary() { return secondary; }
        @Override public int getTier() { return tier; }
        @Override public ItemStack getItem(int i) { return i == 0 ? main : secondary; }
        @Override public int size() { return 2; }
        @Override public boolean isEmpty() { return main.isEmpty() && secondary.isEmpty(); }
    }
}
