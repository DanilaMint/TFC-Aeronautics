package ru.tfc_aeronautics.ceramic;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import ru.tfc_aeronautics.TFCAeronautics;

/**
 * Registers the unfired ceramic basin item — a clay preform shaped around a
 * TFC large vessel and fired into a {@code create:basin}. Mirrors
 * {@code RedstonePlateRegistration}'s single-entry DeferredRegister idiom.
 */
public final class CeramicRegistration
{
    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(TFCAeronautics.MOD_ID);

    public static final DeferredHolder<Item, Item> UNFIRED_BASIN =
        ITEMS.register("ceramic/unfired_basin", () -> new Item(new Item.Properties()));

    private CeramicRegistration() {}

    public static void register(IEventBus bus)
    {
        ITEMS.register(bus);
    }
}