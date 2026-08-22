package ru.tfc_aeronautics.portable_engine;

import net.dries007.tfc.util.data.Fuel;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;

import ru.tfc_aeronautics.Config;
import ru.tfc_aeronautics.TFCAeronautics;

/**
 * Routes TFC's {@link Fuel} table through NeoForge's {@link FurnaceFuelBurnTimeEvent} so any
 * {@code ItemStack.getBurnTime(...)} consumer recognises TFC fuel items.
 *
 * <p>The primary target is {@code simulated:portable_engine}, but the override is global by
 * construction: every machine that asks the item for a burn time — the vanilla furnace,
 * Create's Blaze Burner and train engines, etc. — sees TFC's value. Pack authors who want
 * vanilla burn times back can flip {@link Config#TFC_FUEL_IN_ENGINES} off.
 *
 * <p>{@link FurnaceFuelBurnTimeEvent#setBurnTime(int)} implicitly cancels the event once any
 * listener commits a non-zero value, which means default-priority listeners stop running the
 * moment we set ours. To win against third-party fuel handlers that may also call
 * {@code setBurnTime} at normal priority, we subscribe at {@link EventPriority#HIGH}.
 *
 * <p>Duration is multiplied by TFC's {@code purity} factor. Without purity scaling, a stack
 * of renewable leaves would feed an engine for 600 ticks at full quality and bypass the
 * quality signal TFC expects from real fuel.
 */
@EventBusSubscriber(modid = TFCAeronautics.MOD_ID)
public final class PortableEngineFuelHandler
{
    private PortableEngineFuelHandler() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onFuelBurnTime(FurnaceFuelBurnTimeEvent event)
    {
        if (!Config.TFC_FUEL_IN_ENGINES.get()) return;
        Fuel fuel = Fuel.get(event.getItemStack());
        if (fuel == null) return;
        int duration = Mth.floor(fuel.duration() * fuel.purity());
        if (duration <= 0) return;
        event.setBurnTime(duration);
    }
}
