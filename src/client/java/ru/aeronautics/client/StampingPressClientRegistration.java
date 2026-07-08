package ru.aeronautics.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderersEvent;

import ru.tfc_aeronautics.Aeronautics;
import ru.tfc_aeronautics.press.StampingPressRegistration;

/**
 * Wires the {@link StampingPressRenderer} into the block-entity renderer registry
 * on the client. Mirrors {@link FluidClientExtensions} in using
 * {@link EventBusSubscriber.Bus#MOD} on the client {@link Dist}.
 */
@EventBusSubscriber(modid = Aeronautics.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class StampingPressClientRegistration {

    private StampingPressClientRegistration() {}

    @SubscribeEvent
    public static void registerBlockEntityRenderers(RegisterRenderersEvent event) {
        event.registerBlockEntityRenderer(StampingPressRegistration.STAMPING_PRESS_BE.get(), StampingPressRenderer::new);
    }
}
