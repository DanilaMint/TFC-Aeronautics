package ru.aeronautics.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import dev.engine_room.flywheel.api.visualization.BlockEntityVisualizer;
import dev.engine_room.flywheel.api.visualization.VisualizerRegistry;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;

import ru.tfc_aeronautics.Aeronautics;
import ru.tfc_aeronautics.press.StampingPressBlockEntity;
import ru.tfc_aeronautics.press.StampingPressRegistration;

/**
 * Wires the {@link StampingPressRenderer} into the block-entity renderer
 * registry AND registers the {@link StampingPressVisual} with Flywheel.
 *
 * <p>Both registrations are needed because {@code KineticBlockEntityRenderer}
 * returns early when {@code VisualizationManager.supportsVisualization} is
 * true, so without a Flywheel visual the shaft and head are invisible on any
 * client running Flywheel (which is most setups, including this dev modpack).
 * See {@code com.simibubi.create.content.kinetics.press.PressVisual} for the
 * upstream parallel — {@code CreateBlockEntityBuilder.registerVisualizer()}
 * does the same {@code VisualizerRegistry.setVisualizer} call we make here.
 */
@EventBusSubscriber(modid = Aeronautics.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class StampingPressClientRegistration {

    private StampingPressClientRegistration() {}

    @SubscribeEvent
    public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(StampingPressRegistration.STAMPING_PRESS_BE.get(), StampingPressRenderer::new);
    }

    /**
     * Register the Flywheel visualizer during client setup. Flywheel's
     * {@code VisualizerRegistry} is populated lazily, and {@link
     * FMLClientSetupEvent} fires before any block-entity visual is requested,
     * so this is the safe hook.
     */
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        BlockEntityVisualizer<StampingPressBlockEntity> visualizer =
            SimpleBlockEntityVisualizer.builder(StampingPressRegistration.STAMPING_PRESS_BE.get())
                .factory(StampingPressVisual::new)
                .apply();
        VisualizerRegistry.setVisualizer(StampingPressRegistration.STAMPING_PRESS_BE.get(), visualizer);
    }
}
