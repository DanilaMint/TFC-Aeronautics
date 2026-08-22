package ru.tfc_aeronautics.wrench;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers the wrench head item. Mirrors TFC's {@code TFCItems.METAL_ITEMS} idiom
 * for a single-entry {@link DeferredRegister}.
 */
public final class WrenchHeadRegistration
{
    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems("tfc_aeronautics");

    public static final DeferredHolder<Item, Item> WRENCH_HEAD =
        ITEMS.register("metal/wrench_head/brass", () -> new Item(new Item.Properties()));

    private WrenchHeadRegistration() {}

    public static void register(IEventBus bus)
    {
        ITEMS.register(bus);
    }
}