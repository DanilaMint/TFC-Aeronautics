package ru.tfc_aeronautics.press;

import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;

import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Positions the {@link com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour}
 * value box on the face opposite the press's {@code HORIZONTAL_FACING} — the side where the
 * filter "stands". All other faces (facing, sides, top, bottom) are disabled.
 */
public class StampingPressFilterSlot extends ValueBoxTransform.Sided {

    @Override
    protected Vec3 getSouthLocation() {
        return VecHelper.voxelSpace(8f, 8f, 15.5f);
    }

    @Override
    protected boolean isSideActive(BlockState state, Direction side) {
        if (side.getAxis() != Direction.Axis.X && side.getAxis() != Direction.Axis.Z)
            return false;
        Direction back = state.getValue(StampingPressBlock.HORIZONTAL_FACING).getOpposite();
        return side == back;
    }
}
