package ru.tfc_aeronautics.metal;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers double-ingot items for alloys. Mirrors the {@code PowderRegistration}
 * pattern. Currently exposes a single item
 * ({@code tfc_aeronautics:metal/double_ingot/andesite_alloy}), to be expanded
 * as the metal registry grows.
 */
public final class DoubleIngotRegistration
{
    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems("tfc_aeronautics");

    public static final DeferredHolder<Item, Item> ANDESITE_ALLOY =
        ITEMS.register("metal/double_ingot/andesite_alloy", () -> new Item(new Item.Properties()));

    private DoubleIngotRegistration() {}

    public static void register(IEventBus bus)
    {
        ITEMS.register(bus);
    }
}
