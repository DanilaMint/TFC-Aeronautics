package ru.aeronautics.client.stamping_press;

import dev.engine_room.flywheel.api.visualization.VisualizerRegistry;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import ru.tfc_aeronautics.TFCAeronautics;
import ru.tfc_aeronautics.stamping_press.StampingPressRegistration;

/**
 * Wires up client-side rendering for the stamping press:
 * <ul>
 *   <li>registers the vanilla BER ({@link StampingPressRenderer}), which always
 *       runs — it draws the filter items and, when Flywheel is unavailable,
 *       the shaft and press head too;</li>
 *   <li>registers a Flywheel visual ({@link StampingPressVisual}) so the
 *       rotating shaft + animated head render through Flywheel's instanced
 *       pipeline when it is available.</li>
 * </ul>
 */
@EventBusSubscriber(modid = TFCAeronautics.MOD_ID, value = Dist.CLIENT)
public final class StampingPressClientRegistration {

    private StampingPressClientRegistration() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Force class init so the head PartialModel is registered with Flywheel
        // before model baking runs.
        StampingPressPartialModels.STAMPING_PRESS_HEAD.modelLocation();

        // neverSkipVanillaRender is load-bearing: Flywheel otherwise suppresses
        // the vanilla BER once a visual exists, which would also suppress the
        // FilteringRenderer call that draws the filter items.
        VisualizerRegistry.setVisualizer(
            StampingPressRegistration.STAMPING_PRESS_BE.get(),
            SimpleBlockEntityVisualizer.builder(StampingPressRegistration.STAMPING_PRESS_BE.get())
                .factory(StampingPressVisual::new)
                .neverSkipVanillaRender()
                .apply()
        );
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
            StampingPressRegistration.STAMPING_PRESS_BE.get(),
            StampingPressRenderer::new
        );
    }
}
