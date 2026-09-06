package ru.tfc_aeronautics.condenser_coil;

import com.simibubi.create.content.fluids.FluidTransportBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Create-side fluid pipe behaviour for the condenser coil. The coil is
 * effectively a one-block-long straight pipe along the <em>water</em> axis only;
 * the steam axis is not part of any pipe network at all.
 *
 * <p>What this class actually does:
 * <ul>
 *   <li>Reports {@code canHaveFlowToward} as {@code true} for exactly the two
 *       faces that line up with the water axis (computed from the blockstate's
 *       {@code AXIS} × {@code WATER_VERTICAL}). The steam axis faces and the
 *       result face return {@code false}, so Create's pipe network wiring
 *       (pipe-to-pipe flow, neighbour search, propagation) never crosses into
 *       the steam axis and never attaches a neighbour pipe to the result face.</li>
 *   <li>Inherits everything else from {@link FluidTransportBehaviour}: the
 *       Create pipe graph runs normally along the water axis, so a Create pump
 *       or a hanging water source up-stream pushes water through the coil just
 *       like a regular fluid pipe. The condensation logic in
 *       {@link CondenserCoilBlockEntity} only needs to inspect
 *       {@code getConnection(face).flow} on the two water faces to know whether
 *       coolant is flowing — no per-tick simulation needed.</li>
 * </ul>
 *
 * <p>The class does NOT push any fluid itself: the coil is a passive consumer
 * of coolant, and it deliberately keeps the result face out of Create's pipe
 * graph so coolant cannot leak across into the result tank. Distillate
 * delivery is handled by
 * {@link CondenserCoilBlockEntity#ejectThroughResultFace(FluidStack)} each
 * tick — a direct fill into the neighbour on the result face, plus an explicit
 * BFS through the downstream Create pipe graph (which works regardless of
 * whether the neighbour pipe is wired into the coil's interfaces).
 */
public class CondenserCoilFluidBehaviour extends FluidTransportBehaviour {

    public CondenserCoilFluidBehaviour(CondenserCoilBlockEntity be) {
        super(be);
    }

    /**
     * True iff {@code direction} lines up with the coil's water axis. The water
     * axis is the orthogonal complement of {@code AXIS} on the horizontal plane
     * when {@code WATER_VERTICAL} is {@code false}, and is the Y axis when
     * {@code WATER_VERTICAL} is {@code true}. The four steam-axis faces (the
     * two on the steam axis itself) and the two faces on the <em>other</em>
     * orthogonal horizontal axis return {@code false}.
     *
     * <p>Critically, {@code resultFace} is intentionally <em>closed</em>: if
     * we opened it, Create would treat the coil as a junction between the
     * result-side pipe network and any other axis whose
     * {@code canHaveFlowToward} also returned {@code true}. That would let
     * coolant water from the water-axis network flow straight through the coil
     * into the result tank, contaminating the distillate. Keeping the result
     * face out of the pipe graph is what isolates the coolant loop from the
     * distillate output. Delivery to the result side happens through
     * {@link CondenserCoilBlockEntity#ejectThroughResultFace} (direct
     * {@code IFluidHandler.fill} on the first non-pipe neighbour plus an
     * explicit BFS through the pipe graph), not through Create's flow
     * algorithm — so the neighbour pipe does not need to be wired into the
     * coil's interfaces for fluid to reach the output tank.
     */
    @Override
    public boolean canHaveFlowToward(BlockState state, Direction direction) {
        return direction.getAxis() == waterAxis(state);
    }

    /**
     * Resolves the water axis from the blockstate.
     *
     * @return {@link Direction.Axis#Y} when {@code WATER_VERTICAL} is {@code true};
     *         otherwise the horizontal axis orthogonal to {@code AXIS} (X → Z, Z → X).
     */
    public static Direction.Axis waterAxis(BlockState state) {
        if (state.getValue(CondenserCoilBlock.WATER_VERTICAL)) {
            return Direction.Axis.Y;
        }
        return state.getValue(CondenserCoilBlock.AXIS) == Direction.Axis.X
            ? Direction.Axis.Z
            : Direction.Axis.X;
    }

    /**
     * Resolves the steam axis from the blockstate. The steam axis is always
     * {@code AXIS} itself — Y is never a steam axis (the block cannot be
     * placed with a vertical steam axis).
     */
    public static Direction.Axis steamAxis(BlockState state) {
        return state.getValue(CondenserCoilBlock.AXIS);
    }

    /**
     * The two {@link Direction} values whose {@code getAxis()} equals
     * {@code waterAxis(state)}. Used by callers that want to iterate over the
     * two water faces in order (e.g. the condensation tick to inspect both
     * inlet and outlet flow directions).
     */
    public static Direction[] waterFaces(BlockState state) {
        Direction.Axis axis = waterAxis(state);
        if (axis == Direction.Axis.Y) {
            return new Direction[] { Direction.UP, Direction.DOWN };
        }
        if (axis == Direction.Axis.X) {
            return new Direction[] { Direction.EAST, Direction.WEST };
        }
        return new Direction[] { Direction.SOUTH, Direction.NORTH };
    }

    /**
     * Returns the "heat side" face of the coil — the face along the steam axis
     * where the heat source (and the Create fluid tank acting as the input) is
     * expected to be. The opposite face is the "result side" where the
     * distillate is pushed out.
     *
     * <p>The "positive" direction along {@code AXIS} is chosen as the heat side
     * by convention; the negative side is where the BE ejects result fluid.
     */
    public static Direction heatFace(BlockState state) {
        return Direction.get(Direction.AxisDirection.POSITIVE, steamAxis(state));
    }

    /**
     * The opposite of {@link #heatFace(BlockState)} — where the BE ejects
     * result fluid each tick via
     * {@link CondenserCoilBlockEntity#ejectThroughResultFace(FluidStack)}.
     */
    public static Direction resultFace(BlockState state) {
        return heatFace(state).getOpposite();
    }

    /**
     * Convenience: the {@link BlockPos} of the heat-side neighbour — where
     * the Create fluid tank being distilled should be located.
     */
    public static BlockPos heatNeighbor(BlockPos self, BlockState state) {
        return self.relative(heatFace(state));
    }

    /**
     * Convenience: the {@link BlockPos} of the result-side neighbour — where
     * the output pipe network starts.
     */
    public static BlockPos resultNeighbor(BlockPos self, BlockState state) {
        return self.relative(resultFace(state));
    }
}
