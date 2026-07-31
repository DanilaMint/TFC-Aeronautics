package ru.tfc_aeronautics.press;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;

import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Positions the {@link com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour}
 * value box on the two horizontal faces that are perpendicular to the shaft axis
 * (i.e. perpendicular to {@code HORIZONTAL_FACING}). The shaft enters through
 * {@code HORIZONTAL_FACING}, so the back/front faces are reserved for the
 * mechanical linkage — the filter UI goes on the two free sides instead.
 *
 * <p>The slot center is pushed to z=17 (1.0625 block units → 0.0625 outside
 * the south face), so after the Sided rotation the item lands just clear of
 * the block geometry rather than being half-occluded by the block face.
 *
 * <p><b>getScale()</b> is overridden to 0.25. The Outliner applies a fixed
 * -2.01 scale on top of the slot's scale when rendering the box outline, so
 * the default slot scale of 0.5 produces a 1×1-block outline that covers the
 * entire face. 0.25 gives a 0.5-block outline that sits cleanly on the
 * perpendicular side.
 *
 * <p><b>rotate()</b> is overridden to drop the +180 that the base
 * {@link ValueBoxTransform.Sided} applies. That +180 is fine for the value-box
 * outline (the Outliner draws the outline with a -2.01 scale flip, so it
 * stays double-sided / camera-facing either way), but it rotates the filter
 * item a full 180° past the side's natural angle — so for east/west sides the
 * 2D sprite item's "face" points <i>into</i> the block, backface-culled, and
 * the user sees nothing in the box. Removing the +180 puts the item's face
 * toward the user without affecting the outline or the label (both are
 * billboarded independently of the slot orientation).
 *
 * <p><b>TODO(filter-visibility):</b> The filter item is not visible from the
 * front of the press. The box outline is anchored to the east/west
 * perpendicular sides, so the item at the box center sits at z=0.5 — behind
 * the south face from the camera's perspective and occluded by the block. The
 * Outliner ignores depth, which is why the frame is still visible from the
 * front but the item is not. To make the item readable from the front, either
 * (a) move the box anchor to the front face itself (above or below the shaft
 * entry, with a non-Sided transform), or (b) render the item through
 * {@code ValueBoxRenderer.renderFlatItemIntoValueBox} (billboarded, like
 * CONTRAPTION_CONTROLS) and accept the small visual difference. See commit
 * history on this file for the iterations tried so far.
 */
public class StampingPressFilterSlot extends ValueBoxTransform.Sided {

    @Override
    protected Vec3 getSouthLocation() {
        return VecHelper.voxelSpace(8f, 8f, 17f);
    }

    @Override
    public float getScale() {
        return 0.25f;
    }

    @Override
    public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack ms) {
        float yRot = AngleHelper.horizontalAngle(getSide());
        float xRot = getSide() == Direction.UP ? 90 : getSide() == Direction.DOWN ? 270 : 0;
        ms.mulPose(Axis.YP.rotationDegrees(yRot));
        ms.mulPose(Axis.XP.rotationDegrees(xRot));
    }

    @Override
    protected boolean isSideActive(BlockState state, Direction side) {
        if (side.getAxis() != Direction.Axis.X && side.getAxis() != Direction.Axis.Z)
            return false;
        Direction facing = state.getValue(StampingPressBlock.HORIZONTAL_FACING);
        return side == facing.getClockWise() || side == facing.getCounterClockWise();
    }
}
