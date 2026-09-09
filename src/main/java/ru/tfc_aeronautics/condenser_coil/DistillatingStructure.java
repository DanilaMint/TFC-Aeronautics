package ru.tfc_aeronautics.condenser_coil;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import ru.tfc_aeronautics.heat.HeatDealer;

/**
 * Static helpers for finding the Create fluid-tank block entity ("boiler tank")
 * that the condenser coil is wired to along its steam axis.
 *
 * <p>The expected layout is one block of {@code create:fluid_tank} sitting
 * flush against the coil on the heat side ({@link CondenserCoilFluidBehaviour#heatFace}),
 * optionally with up to {@link #MAX_PIPE_BLOCKS} Create fluid pipes in between
 * (and T-junctions / elbows — the walk is a proper BFS, not a straight line).
 * Multi-block tanks are supported: any of the tank sub-positions resolve to
 * the controller's {@link BlockEntity#getBlockPos()} via
 * {@link FluidTankBlockEntity#getController()}.
 */
public final class DistillatingStructure {

    /**
     * Maximum number of Create fluid-pipe blocks the BFS may traverse between
     * the coil and the input tank. {@code FluidTankBlockEntity} itself does not
     * count against this budget (it's the goal of the walk). Set to 3 because
     * the distillator's intended layout has the input tank within 1–3 pipe
     * blocks of the coil; going further would just be matching tanks that
     * have nothing to do with this coil.
     */
    private static final int MAX_PIPE_BLOCKS = 3;

    private DistillatingStructure() {
        // utility class — no instances
    }

    /**
     * Walks the steam axis from {@code coilPos} and, if a Create fluid-tank
     * block entity is found (directly or after traversing up to
     * {@link #MAX_PIPE_BLOCKS} Create fluid-pipe blocks — through any shape,
     * including T-junctions and elbows — using a BFS), returns its controller
     * position.
     *
     * <p>The pipe walk is intentionally short: the design assumes a fluid-tank
     * multi-block placed right next to the coil, optionally with a small pipe
     * run (≤3 blocks, any shape). Going beyond would just be checking tanks
     * connected far away that have nothing to do with this coil.
     *
     * <p>Selection rule: when both faces have a reachable tank, the coil
     * prefers the tank with a {@link HeatDealer} directly underneath its
     * controller — that's the <em>input</em> tank (the one we're distilling
     * from, which must be heated). The opposite face is treated as the
     * output/distillate tank and is what the coil ejects toward via
     * {@link CondenserCoilBlockEntity#ejectThroughResultFace}. This makes the
     * coil orientation-agnostic: the player can put the input tank on either
     * end of the steam axis and the coil figures out which is which.
     *
     * <p>Tie-breaking: if both candidate tanks have a heat source under them
     * (or neither does), the heat face wins — that's the convention from the
     * original design. In the "neither has a heater" case the existing
     * "no HeatDealer under tank" diagnostic still fires, so the player gets
     * a clear error rather than silent mis-detection.
     *
     * <p>If only one face has a reachable tank, that tank is used regardless
     * of whether a heater is under it — preserving the prior behaviour for
     * single-tank setups.
     *
     * <p>The returned {@link Resolved} bundles the controller position with
     * the face it was found on, so callers can compute the output face as
     * {@code inputFace.getOpposite()} regardless of where the input ended up.
     *
     * @param level   server level
     * @param coilPos position of the coil block entity
     * @param state   coil blockstate, used to read the steam axis
     * @return resolved tank info (controller pos + input face), or {@code null}
     *         if nothing tank-shaped is reachable within the walk budget in
     *         either direction
     */
    @Nullable
    public static Resolved findTank(Level level, BlockPos coilPos, BlockState state) {
        Direction heatFace = CondenserCoilFluidBehaviour.heatFace(state);
        Direction resultFace = heatFace.getOpposite();

        BlockPos heatController = walkOneDirection(level, coilPos, heatFace);
        BlockPos resultController = walkOneDirection(level, coilPos, resultFace);

        boolean heatHasHeater = tankHasHeatDealer(level, heatController);
        boolean resultHasHeater = tankHasHeatDealer(level, resultController);

        // Preferred case: exactly one of the candidates has a heater.
        // Pick the one that does — that's the input tank.
        if (heatHasHeater && !resultHasHeater && heatController != null) {
            return new Resolved(heatController, heatFace);
        }
        if (resultHasHeater && !heatHasHeater && resultController != null) {
            return new Resolved(resultController, resultFace);
        }

        // Either both have a heater or neither does. Fall back to the
        // heat-face ordering so the existing diagnostics fire on the same
        // side as before; the player can wrench-rotate the coil if they
        // meant the other one to be the input.
        if (heatController != null) return new Resolved(heatController, heatFace);
        if (resultController != null) return new Resolved(resultController, resultFace);
        return null;
    }

    /**
     * Result of {@link #findTank}: the controller position of the resolved
     * (input) tank plus the face of the coil it's connected on. The output
     * face is always {@code inputFace.getOpposite()}, since the coil's two
     * faces along the steam axis are mirrored.
     */
    public record Resolved(BlockPos controller, Direction inputFace) {
        /** Face the coil ejects result fluid to. */
        public Direction outputFace() {
            return inputFace.getOpposite();
        }
    }

    /**
     * Back-compat sugar: returns just the controller position, dropping the
     * input face. Used by callers that don't need to know which side the
     * tank was found on (e.g. early structural diagnostics).
     */
    @Nullable
    public static BlockPos findTankController(Level level, BlockPos coilPos, BlockState state) {
        Resolved r = findTank(level, coilPos, state);
        return r == null ? null : r.controller();
    }

    /**
     * {@code true} iff {@code controllerPos} is non-null and the block at
     * {@code controllerPos.below()} is registered as a {@link HeatDealer}.
     * The block's <em>temperature</em> is not checked here — a heater with
     * no fuel is still a heat dealer for selection purposes, so the diagnostic
     * "no HeatDealer under tank" stays meaningful (it only fires when the
     * block at that position isn't a dealer at all).
     */
    private static boolean tankHasHeatDealer(Level level, @Nullable BlockPos controllerPos) {
        if (level == null || controllerPos == null) {
            return false;
        }
        BlockState belowState = level.getBlockState(controllerPos.below());
        return HeatDealer.isHeatDealer(belowState);
    }

    /**
     * BFS-walks from {@code coilPos} along Create fluid pipes in {@code face}
     * and any directions those pipes connect to, up to
     * {@link #MAX_PIPE_BLOCKS} pipe blocks, returning the controller position
     * of the first {@link FluidTankBlockEntity} found.
     *
     * <p>Returns {@code null} when no tank is reachable: either because the
     * walk hit a non-pipe block (air, dirt, a modded pipe that Create's
     * {@link FluidPropagator#getPipe} doesn't recognise), or because the
     * pipe-block budget was exhausted, or because the BFS ran dry.
     *
     * <p>The walk follows {@link FluidPropagator#getPipeConnections} at each
     * pipe, so a T-junction (3-way) or elbow correctly turns the corner
     * instead of blindly stepping straight on. {@code coilPos} is added to
     * {@code visited} up front so the walk never returns into the coil itself
     * even if a pipe on the search frontier has a connection back to it.
     *
     * <p>Why {@code FluidPropagator.getPipe} (not the side-aware
     * {@code hasFluidCapability(level, pos, side)}): the side-aware check
     * returned {@code false} on the very first step of the old linear walk
     * (a pipe with {@code west=true}/{@code east=true} failed the EAST
     * capability lookup because Create's exposed-capability model differs
     * from what we'd hoped). For the structural walk we only need
     * "is this a Create pipe we can pass through" — direction-agnostic.
     */
    @Nullable
    private static BlockPos walkOneDirection(Level level, BlockPos coilPos, Direction face) {
        Set<BlockPos> visited = new HashSet<>();
        visited.add(coilPos);
        Deque<BfsStep> frontier = new ArrayDeque<>();
        frontier.add(new BfsStep(coilPos.relative(face), 0));

        while (!frontier.isEmpty()) {
            BfsStep step = frontier.poll();
            BlockPos pos = step.pos;
            if (!visited.add(pos)) {
                continue;
            }

            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FluidTankBlockEntity tank) {
                return tank.getController();
            }

            FluidTransportBehaviour pipe = FluidPropagator.getPipe(level, pos);
            if (pipe == null) {
                continue; // dead end on this branch; other branches keep exploring
            }
            if (step.pipesTraversed >= MAX_PIPE_BLOCKS) {
                continue; // budget exhausted on this branch; the tank we want is not this close
            }

            BlockState pipeState = level.getBlockState(pos);
            for (Direction next : FluidPropagator.getPipeConnections(pipeState, pipe)) {
                frontier.add(new BfsStep(pos.relative(next), step.pipesTraversed + 1));
            }
        }
        return null;
    }

    /** One BFS frame: the position to inspect and how many pipe blocks we already crossed to reach it. */
    private record BfsStep(BlockPos pos, int pipesTraversed) {
    }

    /**
     * Diagnostic trace: BFS-walks (mirroring {@link #walkOneDirection}) and
     * returns a short human-readable description of every block visited and
     * where the walk stopped. Used by the heartbeat logger when the
     * structural resolution fails, so the player can see at a glance whether
     * the tank is "right there but unreachable through a non-pipe gap" vs.
     * "behind 14 fluid pipes but never actually a tank".
     *
     * <p>The trace caps the BFS at {@link #MAX_PIPE_BLOCKS} (the same budget
     * as the resolver) and emits a step description for every block visited in
     * BFS order — including branch turns — so the player can see "the walk
     * turned left at step 1 and ran into air" vs. "the walk exhausted its
     * 3-pipe budget without ever seeing a tank".
     *
     * <p>Returned string format:
     * <pre>"step 0 @ (x,y,z) = create:fluid_pipe (be=FluidPipeBlockEntity); step 1 @ ... = TANK (controller @ ...); stop=tank"</pre>
     * or, when the walk exhausts the budget without finding a tank:
     * <pre>"step 0 @ ... = create:fluid_pipe; ...; step N @ ... = create:fluid_pipe; stop=budget exhausted (3 pipe blocks)"</pre>
     */
    public static String traceWalk(Level level, BlockPos coilPos, Direction face) {
        Set<BlockPos> visited = new HashSet<>();
        visited.add(coilPos);
        Deque<BfsStep> frontier = new ArrayDeque<>();
        frontier.add(new BfsStep(coilPos.relative(face), 0));

        StringBuilder sb = new StringBuilder();
        int stepId = 0;

        while (!frontier.isEmpty()) {
            BfsStep step = frontier.poll();
            BlockPos pos = step.pos;
            if (!visited.add(pos)) {
                continue;
            }

            String blockId = level.getBlockState(pos).getBlockHolder().unwrapKey()
                .map(k -> k.location().toString()).orElse("?");
            BlockEntity be = level.getBlockEntity(pos);
            String beName = be == null ? "none" : be.getClass().getSimpleName();

            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append("step ").append(stepId++).append(" @ ").append(pos)
                .append(" = ").append(blockId).append(" (be=").append(beName).append(")");

            if (be instanceof FluidTankBlockEntity tank) {
                sb.append("; stop=TANK (controller @ ").append(tank.getController()).append(")");
                return sb.toString();
            }

            FluidTransportBehaviour pipe = FluidPropagator.getPipe(level, pos);
            if (pipe == null) {
                sb.append("; stop=non-pipe block");
                return sb.toString();
            }
            if (step.pipesTraversed >= MAX_PIPE_BLOCKS) {
                sb.append("; stop=budget exhausted (").append(MAX_PIPE_BLOCKS).append(" pipe blocks)");
                return sb.toString();
            }

            BlockState pipeState = level.getBlockState(pos);
            for (Direction next : FluidPropagator.getPipeConnections(pipeState, pipe)) {
                frontier.add(new BfsStep(pos.relative(next), step.pipesTraversed + 1));
            }
        }
        sb.append("; stop=BFS drained (no tank within ").append(MAX_PIPE_BLOCKS).append(" pipe blocks)");
        return sb.toString();
    }

    /**
     * Convenience overload that returns the resolved tank block entity itself
     * (the controller, not the sub-position), or {@code null} when none is
     * reachable. Sugar over {@link #findTankController} for callers that want
     * to inspect tank state directly.
     */
    @Nullable
    public static FluidTankBlockEntity findTankControllerBE(Level level, BlockPos coilPos, BlockState state) {
        BlockPos controllerPos = findTankController(level, coilPos, state);
        if (controllerPos == null) {
            return null;
        }
        BlockEntity be = level.getBlockEntity(controllerPos);
        return be instanceof FluidTankBlockEntity tank ? tank : null;
    }
}
