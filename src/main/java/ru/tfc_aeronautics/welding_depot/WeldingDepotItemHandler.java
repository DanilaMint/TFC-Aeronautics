package ru.tfc_aeronautics.welding_depot;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.util.Helpers;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public class WeldingDepotItemHandler implements IItemHandler {

    private final ItemStackHandler inv;

    public WeldingDepotItemHandler(ItemStackHandler inv) {
        this.inv = inv;
    }

    @Override
    public int getSlots() {
        return 4;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inv.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (slot == WeldingDepotBlockEntity.SLOT_OUTPUT) return stack;
        if (!isItemValid(slot, stack)) return stack;
        return inv.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot != WeldingDepotBlockEntity.SLOT_OUTPUT) return ItemStack.EMPTY;
        return inv.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return inv.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        boolean isFlux = Helpers.isItem(stack, TFCTags.Items.WELDING_FLUX);
        if (slot == WeldingDepotBlockEntity.SLOT_FLUX) return isFlux;
        if (slot == WeldingDepotBlockEntity.SLOT_OUTPUT) return false;
        return !isFlux;
    }
}
