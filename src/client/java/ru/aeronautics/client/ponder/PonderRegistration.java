package ru.aeronautics.client.ponder;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import net.createmod.ponder.foundation.PonderIndex;

import ru.tfc_aeronautics.TFCAeronautics;

/**
 * Hooks {@link PonderPlugin} into Create's PonderIndex on client setup.
 *
 * Mirrors the call site in CreateClient.clientInit():
 *     PonderIndex.addPlugin(new CreatePonderPlugin());
 * Split into a separate EventBusSubscriber so TFCAeronautics.java stays unaware of
 * client-only code.
 */
@EventBusSubscriber(modid = TFCAeronautics.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class PonderRegistration {

    private PonderRegistration() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new PonderPlugin());
    }
}