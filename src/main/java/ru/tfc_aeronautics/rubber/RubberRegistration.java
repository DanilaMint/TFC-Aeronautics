package ru.tfc_aeronautics.rubber;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import ru.tfc_aeronautics.TFCAeronautics;

/**
 * Registers the rubber item — vulcanized latex produced via the
 * {@code create:mixing} recipe in
 * {@code src/main/resources/data/tfc_aeronautics/recipe/mixing/rubber.json}.
 */
public final class RubberRegistration {
    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(TFCAeronautics.MOD_ID);

    public static final DeferredHolder<Item, Item> RUBBER =
        ITEMS.register("rubber", () -> new Item(new Item.Properties()));

    private RubberRegistration() {}

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}