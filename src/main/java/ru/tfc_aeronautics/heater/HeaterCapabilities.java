package ru.tfc_aeronautics.heater;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Registers {@code IItemHandler} (for chute / funnel / hopper / Create arm)
 * and {@code IFluidHandler} (for the downward molten-metal pipe) on the heater
 * block entity.
 *
 * <p>Item insertion goes to the appropriate slot via the {@link HeaterBlockEntity#getHotAwareItemHandler()}
 * wrapper, which already rejects invalid items (non-heatable for slot 0,
 * non-fuel for slot 1). Extraction is gated on item temperature so that the
 * chute and arm only pick items up when they are at or above their working
 * temperature.
 *
 * <p>Fluid handling is a one-way street: the heater fills its internal tank
 * with molten metal produced by TFC heating recipes, and any pipe connected
 * on the {@code DOWN} face can drain it.
 */
public final class HeaterCapabilities {

    private HeaterCapabilities() {}

    public static void register(IEventBus bus) {
        bus.addListener(HeaterCapabilities::registerCapabilities);
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            HeaterRegistration.HEATER_BE.get(),
            (be, ctx) -> be.getHotAwareItemHandler()
        );
        event.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK,
            HeaterRegistration.HEATER_BE.get(),
            (be, ctx) -> ctx == net.minecraft.core.Direction.DOWN ? be.getTank() : null
        );
    }
}