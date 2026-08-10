package ru.tfc_aeronautics.burlap;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import ru.tfc_aeronautics.TFCAeronautics;

/**
 * Registers the impregnated burlap cloth item. The item itself is a vanilla {@link Item};
 * it is produced by sealing a {@code tfc:burlap_cloth} in a barrel with
 * {@code tfc_aeronautics:rosin} (see {@code data/tfc_aeronautics/recipe/barrel/impregnated_burlap_cloth.json}).
 */
public final class BurlapRegistration {
    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(TFCAeronautics.MOD_ID);

    public static final DeferredHolder<Item, Item> IMPREGNATED_BURLAP_CLOTH =
        ITEMS.register("impregnated_burlap_cloth", () -> new Item(new Item.Properties()));

    private BurlapRegistration() {}

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
