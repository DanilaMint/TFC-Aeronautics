package ru.aeronautics.client.stamping_press;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.press.PressingBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

import ru.tfc_aeronautics.stamping_press.StampingPressBlockEntity;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

/**
 * Renders the stamping press. Mirrors {@code MechanicalPressRenderer} from
 * Create — extends {@link KineticBlockEntityRenderer} so the rotating shaft
 * and any {@code FilteringBehaviour} items are drawn automatically. The press
 * head is animated manually using the same offset as the mechanical press.
 *
 * <p>Filter frames (the value-box outlines) are drawn on both perpendicular
 * sides of the block by {@link StampingPressFrameTickHandler}, not here.
 */
public class StampingPressRenderer extends KineticBlockEntityRenderer<StampingPressBlockEntity> {

    public StampingPressRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRenderOffScreen(StampingPressBlockEntity be) {
        return true;
    }

    @Override
    protected void renderSafe(StampingPressBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
        int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        // Filter items live in perpendicular faces, never overlap the rotating
        // shaft or animated head, so they render cleanly regardless of whether
        // Flywheel is handling the rest of the block.
        FilteringRenderer.renderOnBlockEntity(be, partialTicks, ms, buffer, light, overlay);

        if (VisualizationManager.supportsVisualization(be.getLevel()))
            return;

        BlockState blockState = be.getBlockState();
        PressingBehaviour pressingBehaviour = be.pressingBehaviour;
        float renderedHeadOffset = pressingBehaviour.getRenderedHeadOffset(partialTicks) * pressingBehaviour.mode.headOffset;

        SuperByteBuffer headRender = CachedBuffers.partialFacing(
            StampingPressPartialModels.STAMPING_PRESS_HEAD,
            blockState,
            blockState.getValue(HORIZONTAL_FACING)
        );
        headRender.translate(0, -renderedHeadOffset, 0)
            .light(light)
            .renderInto(ms, buffer.getBuffer(RenderType.solid()));
    }

    @Override
    protected BlockState getRenderedBlockState(StampingPressBlockEntity be) {
        return shaft(getRotationAxisOf(be));
    }
}
