package ru.tfc_aeronautics.drill_head;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers the drill head item. Mirrors {@code SawBladeRegistration}'s
 * idiom for a single-entry {@link DeferredRegister}.
 */
public final class DrillHeadRegistration
{
    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems("tfc_aeronautics");

    public static final DeferredHolder<Item, Item> DRILL_HEAD =
        ITEMS.register("drill_head", () -> new Item(new Item.Properties()));

    private DrillHeadRegistration() {}

    public static void register(IEventBus bus)
    {
        ITEMS.register(bus);
    }
}
