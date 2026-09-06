package ru.tfc_aeronautics.condenser_coil;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import org.jetbrains.annotations.Nullable;

/**
 * The condenser-coil block. It is a heat-driven distillation head that
 * consumes fluid from a Create fluid-tank sitting on the <em>steam</em> axis
 * (the axis orthogonal to the water axis declared on the blockstate) and
 * pushes the resulting distillate along a pipe network starting on the same
 * steam axis but in the opposite direction (the result side).
 *
 * <p>The block carries two state properties:
 * <ul>
 *   <li>{@link #AXIS} — horizontal axis (X or Z) of the visible coil windings
 *       (the <em>steam</em> axis the coil is oriented along in space). The
 *       water axis is orthogonal to this on the horizontal plane, and goes
 *       vertical when {@link #WATER_VERTICAL} is {@code true}.</li>
 *   <li>{@link #WATER_VERTICAL} — {@code true} when the water axis is the
 *       Y-axis (coolant flows down through the coil), {@code false} when it
 *       is the second horizontal axis.</li>
 * </ul>
 *
 * <p>The coil itself acts as a one-segment Create fluid pipe along the water
 * axis only (see {@link CondenserCoilFluidBehaviour}). The steam axis is
 * not part of any pipe network: the coil "reads" the tank via direct
 * capability access on the heat side, and "writes" the result by pushing
 * fluid directly into the block on the opposite face each tick (see
 * {@link CondenserCoilBlockEntity#ejectThroughResultFace(FluidStack)}). If
 * that neighbour is not a fluid-bearing block the result fluid is discarded.
 *
 * <p>The block is rotatable with Create's wrench via
 * {@link IWrenchable}; the four states cycle through the (AXIS × WATER_VERTICAL)
 * combinations so the player can always re-aim both axes.
 */
public class CondenserCoilBlock extends Block implements IBE<CondenserCoilBlockEntity>, IWrenchable {

    /**
     * Horizontal axis the coil winding is aligned with in space. Acts as the
     * "steam" axis (the side that talks to the fluid tank being heated). Only
     * X and Z are valid: the coil cannot be placed with its steam axis vertical
     * because then the model would have to be rotated 90 degrees and the
     * water-axis split would no longer be representable on the same blockstate.
     */
    public static final EnumProperty<Direction.Axis> AXIS =
        EnumProperty.create("axis", Direction.Axis.class, Direction.Axis.X, Direction.Axis.Z);

    /**
     * {@code true} when the water (coolant) axis is the Y-axis (vertical
     * flow, e.g. water cascading from above), {@code false} when it is the
     * other horizontal axis (coolant flowing sideways through the coil).
     */
    public static final BooleanProperty WATER_VERTICAL = BooleanProperty.create("water_vertical");

    public CondenserCoilBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(AXIS, Direction.Axis.Z).setValue(WATER_VERTICAL, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AXIS, WATER_VERTICAL);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Steam axis defaults to the horizontal axis the player is looking
        // along (so the visible coil winds "point" toward the tank the player
        // just placed this above). Water axis defaults to horizontal (false).
        Direction horizontal = context.getHorizontalDirection();
        Direction.Axis steamAxis = horizontal.getAxis();
        // The EnumProperty only allows X/Z; if the player somehow looks along
        // the Y axis we fall back to Z.
        if (steamAxis == Direction.Axis.Y) {
            steamAxis = Direction.Axis.Z;
        }
        return defaultBlockState().setValue(AXIS, steamAxis).setValue(WATER_VERTICAL, false);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        IBE.onRemove(state, level, pos, newState);
    }

    @Override
    public Class<CondenserCoilBlockEntity> getBlockEntityClass() {
        return CondenserCoilBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CondenserCoilBlockEntity> getBlockEntityType() {
        return CondenserCoilRegistration.CONDENSER_COIL_BE.get();
    }

    // --- IWrenchable -----------------------------------------------------

    /**
     * The coil has no kinetic state and the placement-level swap is purely
     * blockstate-level, so a plain {@code level.setBlock(... UPDATE_ALL)} is
     * enough — no need for {@code KineticBlockEntity.switchToBlockState} and
     * its kinetic-network bookkeeping. Rotation comes from
     * {@link #getRotatedBlockState(BlockState, Direction)}.
     */
    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState rotated = getRotatedBlockState(state, context.getClickedFace());
        if (rotated == state) {
            return InteractionResult.PASS;
        }

        level.setBlock(pos, rotated, Block.UPDATE_ALL);
        if (level.getBlockState(pos) != state) {
            IWrenchable.playRotateSound(level, pos);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Cycles through the four blockstate variants (axis X/Z, water vertical on/off).
     * Only the top/bottom face triggers rotation (same convention as
     * {@link ru.tfc_aeronautics.heater.HeaterBlock#getRotatedBlockState}) so the
     * pattern stays predictable regardless of which face the wrench targets.
     *
     * <p>Order:
     * <pre>
     *   (X, false) -> (X, true) -> (Z, true) -> (Z, false) -> (X, false)
     * </pre>
     * That is: flip WATER_VERTICAL first; if it was already true, swap the
     * steam axis and reset WATER_VERTICAL to false. The result is a 4-state
     * cycle that always visits all four blockstate variants.
     */
    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        if (targetedFace.getAxis() != Direction.Axis.Y) {
            return originalState;
        }
        Direction.Axis axis = originalState.getValue(AXIS);
        boolean waterVertical = originalState.getValue(WATER_VERTICAL);
        if (!waterVertical) {
            return originalState.setValue(WATER_VERTICAL, true);
        }
        Direction.Axis newAxis = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        return originalState.setValue(AXIS, newAxis).setValue(WATER_VERTICAL, false);
    }
}
