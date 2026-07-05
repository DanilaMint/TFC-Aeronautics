package ru.tfc_aeronautics.fluid;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import ru.tfc_aeronautics.Aeronautics;

/**
 * Registers the molten andesite alloy bucket item. Vanilla {@link BucketItem}
 * is used directly — TFC's {@code TFCItems.FLUID_BUCKETS} flow goes through
 * {@code FluidId.mapOf(...)} which is coupled to TFC's {@code Metal} enum,
 * so for a single new fluid we register the bucket by hand.
 */
public final class AeronauticsFluidItems
{
    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(Aeronautics.MOD_ID);

    public static final DeferredHolder<Item, BucketItem> MOLTEN_ANDESITE_ALLOY_BUCKET =
        ITEMS.register("molten_andesite_alloy_bucket", () -> new BucketItem(
            AeronauticsFluids.MOLTEN_ANDESITE_ALLOY.getSource(),
            new Item.Properties()));

    public static final DeferredHolder<Item, BucketItem> MOLTEN_MAGMATITE_BUCKET =
        ITEMS.register("molten_magmatite_bucket", () -> new BucketItem(
            AeronauticsFluids.MOLTEN_MAGMATITE.getSource(),
            new Item.Properties()));

    private AeronauticsFluidItems() {}

    public static void register(IEventBus bus)
    {
        ITEMS.register(bus);
    }
}