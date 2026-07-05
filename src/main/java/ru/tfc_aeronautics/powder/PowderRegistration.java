package ru.tfc_aeronautics.powder;

import java.util.Map;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.dries007.tfc.util.Helpers;

/**
 * Registers the powder items. Mirrors TFC's {@code TFCItems.METAL_ITEMS} idiom
 * (one {@link DeferredHolder} per enum constant via {@link Helpers#mapOf}).
 */
public final class PowderRegistration
{
    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems("tfc_aeronautics");

    public static final Map<MetalPowder, DeferredHolder<Item, ? extends Item>> POWDERS =
        Helpers.mapOf(MetalPowder.class, powder ->
            ITEMS.register(powder.itemId(), () -> new MetalPowderItem(new Item.Properties())));

    private PowderRegistration() {}

    public static void register(IEventBus bus)
    {
        ITEMS.register(bus);
    }
}