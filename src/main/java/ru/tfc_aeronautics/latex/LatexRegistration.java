package ru.tfc_aeronautics.latex;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import ru.tfc_aeronautics.TFCAeronautics;

/**
 * Registers the latex clump item. The item itself is a vanilla {@link Item}; all behaviour
 * (chance-based drop on axe-strip of {@code tfc_aeronautics:can_collect_latex} blocks)
 * is handled by {@link LatexStripHandler}.
 */
public final class LatexRegistration {
    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(TFCAeronautics.MOD_ID);

    public static final DeferredHolder<Item, Item> LATEX_CLUMP =
        ITEMS.register("latex_clump", () -> new Item(new Item.Properties()));

    private LatexRegistration() {}

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}