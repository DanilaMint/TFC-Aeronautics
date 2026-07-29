package ru.aeronautics.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderersEvent;

import ru.tfc_aeronautics.Aeronautics;
import ru.tfc_aeronautics.heater.HeaterRegistration;

/**
 * Wires {@link HeaterBlockEntityRenderer} into the block-entity renderer
 * registry on the client. Mirrors {@link StampingPressClientRegistration}.
 */
@EventBusSubscriber(modid = Aeronautics.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class HeaterClientRegistration {

    private HeaterClientRegistration() {}

    @SubscribeEvent
    public static void registerBlockEntityRenderers(RegisterRenderersEvent event) {
        event.registerBlockEntityRenderer(HeaterRegistration.HEATER_BE.get(), HeaterBlockEntityRenderer::new);
    }
}