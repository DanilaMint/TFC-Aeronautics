package ru.aeronautics.client.ponder;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import net.createmod.ponder.foundation.PonderIndex;

import ru.tfc_aeronautics.Aeronautics;

/**
 * Hooks {@link AeronauticsPonderPlugin} into Create's PonderIndex on client setup.
 *
 * Mirrors the call site in CreateClient.clientInit():
 *     PonderIndex.addPlugin(new CreatePonderPlugin());
 * Split into a separate EventBusSubscriber so Aeronautics.java stays unaware of
 * client-only code.
 */
@EventBusSubscriber(modid = Aeronautics.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AeronauticsPonderRegistration {

    private AeronauticsPonderRegistration() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new AeronauticsPonderPlugin());
    }
}