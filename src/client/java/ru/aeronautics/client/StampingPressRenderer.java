package ru.aeronautics.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.press.PressingBehaviour;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import ru.tfc_aeronautics.press.StampingPressBlockEntity;

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

        if (VisualizationManager.supportsVisualization(be.getLevel()))
            return;

        BlockState blockState = be.getBlockState();
        PressingBehaviour pressingBehaviour = be.getPressingBehaviour();
        float renderedHeadOffset = pressingBehaviour.getRenderedHeadOffset(partialTicks)
            * pressingBehaviour.mode.headOffset;

        SuperByteBuffer headRender = CachedBuffers.partialFacing(AllPartialModels.MECHANICAL_PRESS_HEAD, blockState,
            blockState.getValue(BlockStateProperties.HORIZONTAL_FACING));
        headRender.translate(0, -renderedHeadOffset, 0)
            .light(light)
            .renderInto(ms, buffer.getBuffer(RenderType.solid()));
    }

    @Override
    protected BlockState getRenderedBlockState(StampingPressBlockEntity be) {
        return shaft(getRotationAxisOf(be));
    }
}
