package ru.tfc_aeronautics.resin;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import ru.tfc_aeronautics.TFCAeronautics;

/**
 * Registers the resin clump item. The item itself is a vanilla {@link Item}; all behaviour
 * (chance-based drop on axe-strip of {@code tfc_aeronautics:can_collect_resin} blocks)
 * is handled by {@link ResinStripHandler}.
 */
public final class ResinRegistration {
    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(TFCAeronautics.MOD_ID);

    public static final DeferredHolder<Item, Item> RESIN_CLUMP =
        ITEMS.register("resin_clump", () -> new Item(new Item.Properties()));

    private ResinRegistration() {}

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
