package ru.tfc_aeronautics.metal;

import java.util.Map;
import net.minecraft.world.item.Item;

import net.dries007.tfc.util.Helpers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers tight-sheet items for copper, wrought iron and steel. Mirrors
 * the {@link ru.tfc_aeronautics.powder.PowderRegistration} pattern
 * (one {@link DeferredHolder} per enum constant via {@link Helpers#mapOf}).
 */
public final class TightSheetRegistration
{
    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems("tfc_aeronautics");

    public static final Map<TightSheet, DeferredHolder<Item, ? extends Item>> TIGHT_SHEETS =
        Helpers.mapOf(TightSheet.class, sheet ->
            ITEMS.register(sheet.itemId(), () -> new Item(new Item.Properties())));

    private TightSheetRegistration() {}

    public static void register(IEventBus bus)
    {
        ITEMS.register(bus);
    }
}