package ru.aeronautics.client.chain;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.logistics.packagePort.PackagePortTargetSelectionHandler;

import dev.engine_room.flywheel.api.visualization.VisualizerRegistry;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;

import ru.tfc_aeronautics.TFCAeronautics;
import ru.tfc_aeronautics.chain.ChainConveyorConnectionHandler;
import ru.tfc_aeronautics.chain.ChainConveyorRegistration;

/**
 * Registers the {@code tfc_aeronautics:chain_conveyor} Flywheel visual, BER, and
 * the per-tick client hooks that drive the right-click targeting, the chain
 * selection outline and the package-onto-chain drop interaction.
 *
 * <p>The per-tick / input / highlight hooks mirror Create's own wiring in
 * {@code com.simibubi.create.foundation.events.ClientEvents} (lines 194-197, 251)
 * and {@code com.simibubi.create.foundation.events.InputEvents} (line 110):
 * without these subscriptions our copied {@link ChainConveyorInteractionHandler}
 * never sees a tick, its {@code selectedLift} stays {@code null}, and
 * {@code onUse()} early-returns — so the frogport-target fix in
 * {@link ChainConveyorInteractionHandler#onUse()} never fires.
 */
@EventBusSubscriber(modid = TFCAeronautics.MOD_ID, value = Dist.CLIENT)
public class ChainConveyorClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // neverSkipVanillaRender is load-bearing: the vanilla BER's
        // renderChains(...) draws the animated chain OBB between
        // connected conveyors, and the Flywheel visual does not.
        VisualizerRegistry.setVisualizer(
            ChainConveyorRegistration.CHAIN_CONVEYOR_BE.get(),
            SimpleBlockEntityVisualizer.builder(ChainConveyorRegistration.CHAIN_CONVEYOR_BE.get())
                .factory(ChainConveyorVisual::new)
                .neverSkipVanillaRender()
                .apply()
        );
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
            ChainConveyorRegistration.CHAIN_CONVEYOR_BE.get(),
            ChainConveyorRenderer::new
        );
    }

    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().level == null) return;
        ChainConveyorInteractionHandler.clientTick();
        ChainConveyorRidingHandler.clientTick();
        ChainConveyorConnectionHandler.clientTick();
        PackagePortTargetSelectionHandler.tick();
    }

    @SubscribeEvent
    public static void onClickInput(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) return;

        if (ChainConveyorInteractionHandler.onUse()) {
            event.setCanceled(true);
            return;
        }
        if (ChainPackageInteractionHandler.onUse()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onDrawBlockSelection(RenderHighlightEvent.Block event) {
        if (ChainConveyorInteractionHandler.selectedLift == null
            || ChainConveyorInteractionHandler.selectedShape == null)
            return;
        event.setCanceled(true);
        PoseStack ms = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();
        Camera camera = event.getCamera();
        ChainConveyorInteractionHandler.drawCustomBlockSelection(ms, buffer, camera.getPosition());
    }
}