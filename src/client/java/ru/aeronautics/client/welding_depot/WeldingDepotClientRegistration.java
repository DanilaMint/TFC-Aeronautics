package ru.aeronautics.client.welding_depot;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import ru.tfc_aeronautics.TFCAeronautics;
import ru.tfc_aeronautics.welding_depot.WeldingDepotRegistration;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD, modid = TFCAeronautics.MOD_ID)
public class WeldingDepotClientRegistration {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(WeldingDepotRegistration.WELDING_DEPOT_BE.get(), WeldingDepotBlockEntityRenderer::new);
    }
}
