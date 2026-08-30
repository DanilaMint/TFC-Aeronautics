package ru.tfc_aeronautics.welding_depot;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class WeldingDepotCapabilities {

    private WeldingDepotCapabilities() {}

    public static void register(IEventBus bus) {
        bus.addListener(WeldingDepotCapabilities::registerCapability);
    }

    @SubscribeEvent
    public static void registerCapability(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            WeldingDepotRegistration.WELDING_DEPOT_BE.get(),
            (be, ctx) -> be.getExternalHandler()
        );
    }
}
