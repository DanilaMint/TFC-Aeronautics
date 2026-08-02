package ru.tfc_aeronautics.stamping_press;

import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;

import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

/**
 * Positions the filter value box for {@link StampingPressBlockEntity}'s
 * {@link com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour}
 * on the two horizontal sides perpendicular to the press's facing. The press
 * is oriented so the shaft connects along the facing axis; the filter slot
 * is mirrored on both perpendicular faces so the player can install a filter
 * from either side.
 *
 * <p>Positioning follows the {@code SmartChuteFilterSlotPositioning} pattern:
 * the south location is rotated explicitly by the looked-at side via
 * {@link #getLocalOffset}. The default {@code Sided.getLocalOffset} rotates by
 * both {@code horizontalAngle} and {@code verticalAngle}, which works for the
 * single-side case but is redundant here (only horizontal sides are active).
 * Overriding {@code getLocalOffset} directly keeps the slot plane perpendicular
 * to the active face without an extra vertical-axis rotation.
 */
public class StampingPressFilterSlot extends ValueBoxTransform.Sided {

    public StampingPressFilterSlot() {
    }

    @Override
    public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
        Direction side = getSide();
        float horizontalAngle = AngleHelper.horizontalAngle(side);
        // The press model is an opaque shell with no transparent "filter slot"
        // cutout in its texture, so the value box has to sit OUTSIDE the
        // model's outer surface (z=16 is the south face; z=16.5 puts the item
        // a half-voxel past it). The Sided default would rotate this point
        // onto the OPPOSITE face for the N/S axes (because of how the
        // horizontalAngle formula maps +Z → +Z for NORTH but +Z → -Z for
        // SOUTH), but for our perpendicular sides (W/E) the rotation maps
        // +Z → ±X, putting the slot cleanly on the WEST or EAST face.
        Vec3 southLocation = VecHelper.voxelSpace(8, 8, 16.5f);
        return VecHelper.rotateCentered(southLocation, horizontalAngle, Axis.Y);
    }

    @Override
    protected Vec3 getSouthLocation() {
        return Vec3.ZERO;
    }

    @Override
    public float getScale() {
        return 0.5f;
    }

    @Override
    protected boolean isSideActive(BlockState state, Direction direction) {
        if (!direction.getAxis().isHorizontal()) return false;
        if (!state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) return false;
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        return facing.getClockWise() == direction || facing.getCounterClockWise() == direction;
    }
}