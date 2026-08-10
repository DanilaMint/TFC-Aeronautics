package ru.tfc_aeronautics.fluid;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import ru.tfc_aeronautics.TFCAeronautics;

/**
 * Registers fluid bucket items. Vanilla {@link BucketItem} is used directly —
 * TFC's {@code TFCItems.FLUID_BUCKETS} flow goes through
 * {@code FluidId.mapOf(...)} which is coupled to TFC's {@code Metal} enum,
 * so for new fluids we register the bucket by hand.
 */
public final class FluidItems
{
    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(TFCAeronautics.MOD_ID);

    public static final DeferredHolder<Item, BucketItem> ROSIN_BUCKET =
        ITEMS.register("rosin_bucket", () -> new BucketItem(
            Fluids.ROSIN.getSource(),
            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    private FluidItems() {}

    public static void register(IEventBus bus)
    {
        ITEMS.register(bus);
    }
}