package ru.tfc_aeronautics.harvester_blade;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import ru.tfc_aeronautics.TFCAeronautics;

public final class HarvesterBladeRegistration
{
    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(TFCAeronautics.MOD_ID);

    public static final DeferredHolder<Item, Item> HARVESTER_BLADE =
        ITEMS.register("harvester_blade", () -> new Item(new Item.Properties()));

    private HarvesterBladeRegistration() {}

    public static void register(IEventBus bus)
    {
        ITEMS.register(bus);
    }
}
