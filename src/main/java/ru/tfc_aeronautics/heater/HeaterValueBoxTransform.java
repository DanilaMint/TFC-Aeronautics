package ru.tfc_aeronautics.heater;

import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;

import net.createmod.catnip.math.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Positions the {@link HeaterValueBehaviour} button on the south face of the block,
 * matching the layout used by Create's own {@code Rotation Speed Controller} and by
 * Simulated-Project/aeronautics' {@code HotAirBurnerValueBoxTransform}.
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
        return direction.getAxis().isHorizontal();
    }

    @Override
    public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
        return getSouthLocation();
    }
}