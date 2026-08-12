package ru.tfc_aeronautics.saw;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers the saw blade item. Mirrors TFC's {@code TFCItems.METAL_ITEMS} idiom
 * for a single-entry {@link DeferredRegister}.
 */
public final class SawBladeRegistration
{
    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems("tfc_aeronautics");

    public static final DeferredHolder<Item, Item> SAW_BLADE =
        ITEMS.register("saw_blade", () -> new Item(new Item.Properties()));

    private SawBladeRegistration() {}

    public static void register(IEventBus bus)
    {
        ITEMS.register(bus);
    }
}