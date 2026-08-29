package ru.aeronautics.client.welding_depot;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import ru.tfc_aeronautics.welding_depot.WeldingDepotBlockEntity;

public class WeldingDepotBlockEntityRenderer extends SafeBlockEntityRenderer<WeldingDepotBlockEntity> {

    public WeldingDepotBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super();
    }

    @Override
    protected void renderSafe(WeldingDepotBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        renderSlot(be, WeldingDepotBlockEntity.SLOT_LEFT,   -0.25F, 0.9375F, 0.18F,  ms, buffer, light);
        renderSlot(be, WeldingDepotBlockEntity.SLOT_FLUX,    0.0F,  0.9375F, 0.18F,  ms, buffer, light);
        renderSlot(be, WeldingDepotBlockEntity.SLOT_RIGHT,   0.25F, 0.9375F, 0.18F,  ms, buffer, light);
        renderSlot(be, WeldingDepotBlockEntity.SLOT_OUTPUT,  0.0F,  0.9375F, -0.18F, ms, buffer, light);
    }

    private void renderSlot(WeldingDepotBlockEntity be, int slot, float dx, float dy, float dz, PoseStack ms, MultiBufferSource buffer, int light) {
        ItemStack stack = be.getInventory().getStackInSlot(slot);
        if (stack.isEmpty()) return;
        ms.pushPose();
        ms.translate(0.5 + dx, dy, 0.5 + dz);

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        boolean blockItem = itemRenderer.getModel(stack, null, null, 0).isGui3d();

        if (blockItem) {
            ms.translate(0, -0.0625F, 0);
            ms.scale(0.5F, 0.5F, 0.5F);
        } else {
            ms.scale(0.5F, 0.5F, 0.5F);
            ms.translate(0, -0.1875F, 0);
            ms.mulPose(Axis.XP.rotationDegrees(90));
        }

        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, ms, buffer, be.getLevel(), 0);
        ms.popPose();
    }
}