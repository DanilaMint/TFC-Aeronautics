package ru.tfc_aeronautics.redstone_plate;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import ru.tfc_aeronautics.TFCAeronautics;

/**
 * Registers the redstone plate item — a thin smooth-stone slab used as a
 * crafting material for upcoming redstone-themed mechanics. Mirrors
 * {@code DrillHeadRegistration}'s single-entry DeferredRegister idiom.
 */
public final class RedstonePlateRegistration
{
    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(TFCAeronautics.MOD_ID);

    public static final DeferredHolder<Item, Item> REDSTONE_PLATE =
        ITEMS.register("redstone_plate", () -> new Item(new Item.Properties()));

    private RedstonePlateRegistration() {}

    public static void register(IEventBus bus)
    {
        ITEMS.register(bus);
    }
}