package ru.tfc_aeronautics.heater;

import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;

import net.createmod.catnip.math.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

/**
 * Positions the {@link HeaterValueBehaviour} button on the face that matches the
 * block's {@link BlockStateProperties#HORIZONTAL_FACING}. Because the renderer
 * iterates over every face and asks {@link #isSideActive(BlockState, Direction)},
 * the box is only drawn on the one horizontal face the wrench is currently set to —
 * which means rotating the heater with a wrench moves the knob to the new face.
 */
public class HeaterValueBoxTransform extends ValueBoxTransform.Sided {

    @Override
    protected Vec3 getSouthLocation() {
        return VecHelper.voxelSpace(8, 8, 15);
    }

    @Override
    public float getScale() {
        return 0.4f;
    }

    @Override
    protected boolean isSideActive(BlockState state, Direction direction) {
        if (!direction.getAxis().isHorizontal()) return false;
        if (!state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) return false;
        return state.getValue(BlockStateProperties.HORIZONTAL_FACING) == direction;
    }

    @Override
    public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
        return getSouthLocation();
    }
}