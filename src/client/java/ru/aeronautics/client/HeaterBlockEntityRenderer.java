package ru.aeronautics.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import ru.tfc_aeronautics.heater.HeaterBlockEntity;

/**
 * Renders the heater's animated flame overlay on top of the body when the
 * block is in the {@code lit=true} state. The body itself is drawn by the
 * standard block model (see {@code blockstates/heater.json}), so this
 * renderer only adds the flame quad and its flicker.
 *
 * <p>Flicker is two sine waves of different frequencies combined for a
 * non-periodic feel: one slow bob on Y, one faster jitter on scale.
 */
public class HeaterBlockEntityRenderer implements BlockEntityRenderer<HeaterBlockEntity> {

    public HeaterBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(HeaterBlockEntity be, float partialTicks, PoseStack ms,
                       MultiBufferSource buffer, int light, int overlay) {
        if (!be.getBlockState().getValue(BlockStateProperties.LIT)) return;

        float time = (be.getLevel().getGameTime() + partialTicks) * 0.15f;
        float bob = Mth.sin(time) * 0.04f + Mth.sin(time * 2.7f) * 0.02f;
        float scale = 0.9f + Mth.sin(time * 1.7f) * 0.1f;

        SuperByteBuffer flame = CachedBuffers.partial(HeaterPartialModels.HEATER_FLAME);
        flame.translate(0, 0.5f + bob, 0)
             .scale(scale, scale, scale)
             .light(0xF000F0)
             .renderInto(ms, buffer.getBuffer(RenderType.translucent()));
    }
}