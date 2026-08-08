package ru.tfc_aeronautics.composite;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import ru.tfc_aeronautics.Aeronautics;

/**
 * Registers the two composite items:
 * <ul>
 *   <li>{@code tfc_aeronautics:dry_composite} — shapeless mix of cast iron powder + igneous gravel.</li>
 *   <li>{@code tfc_aeronautics:composite} — {@code dry_composite} sealed with 25 mB limewater in a barrel;
 *       the wet/cured state used by the casing and shaft recipes.</li>
 * </ul>
 * Both items are vanilla {@link Item} instances: all behaviour (heating, item_application, barrel mixing)
 * is data-driven via JSON recipes, so no custom logic lives here.
 */
public final class CompositeRegistration
{
    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(Aeronautics.MOD_ID);

    public static final DeferredHolder<Item, Item> DRY_COMPOSITE =
        ITEMS.register("dry_composite", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> COMPOSITE =
        ITEMS.register("composite", () -> new Item(new Item.Properties()));

    private CompositeRegistration() {}

    public static void register(IEventBus bus)
    {
        ITEMS.register(bus);
    }
}
