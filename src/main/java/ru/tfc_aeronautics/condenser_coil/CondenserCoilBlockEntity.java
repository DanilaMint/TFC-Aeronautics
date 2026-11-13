package ru.tfc_aeronautics.condenser_coil;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.PipeConnection;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import org.jetbrains.annotations.Nullable;

import ru.tfc_aeronautics.Config;
import ru.tfc_aeronautics.TFCAeronautics;
import ru.tfc_aeronautics.heat.HeatDealer;
import ru.tfc_aeronautics.recipe.DistillatingRecipe;
import ru.tfc_aeronautics.recipe.DistillatingRecipeType;

/**
 * The condenser coil's brain: tracks the heat source underneath the fluid tank,
 * the input tank snapshot, the active distillating recipe, and the warmup timer.
 *
 * <p>The state machine is intentionally small: {@code IDLE → WARMUP → RUNNING →
 * IDLE}. {@code PAUSED} is not a separate state — when the coil cannot advance
 * (no coolant flow, temperature out of range) the tick simply leaves the
 * progress and lock in place and returns. That keeps the persisted state
 * simple (no per-pause flag) and avoids losing work when the heat drops
 * momentarily.
 *
 * <p>Result fluid is <em>not</em> buffered internally. On every production
 * tick the coil drains the same amount from the source tank and pushes the
 * resulting fluid through the block adjacent to its result face via a direct
 * {@link IFluidHandler#fill(FluidStack, IFluidHandler.FluidAction) fill} call.
 * If that neighbour has no fluid handler (no pipe, no tank, just air) the
 * fluid is intentionally destroyed rather than saved up — this keeps the coil
 * out of Create's pipe graph on the steam axis and means a missing result
 * pipe cannot stall the distillation: the source tank drains and the residue
 * is restored on schedule regardless.
 *
 * <p>Tick logic:
 * <ol>
 *   <li>Resolve the structure: heat source below the coil's tank; tank
 *       controller along the steam axis; coolant flow on the water axis.</li>
 *   <li>If structure is invalid, leave state alone (the lock is released so a
 *       broken structure doesn't keep a tank pinned shut).</li>
 *   <li>If {@code IDLE}: try to enter {@code WARMUP} when the tank has a
 *       matching recipe and the heat is within range.</li>
 *   <li>If {@code WARMUP}: decrement the warmup timer; any change to the tank's
 *       fluid contents resets it to the full value (the player can top off
 *       during the warmup window). On expiry, enter {@code RUNNING}: snapshot
 *       the volume, lock the tank.</li>
 *   <li>If {@code RUNNING}: per-tick production at the recipe's rate, skipping
 *       when out of range or no coolant flow. Each tick drains input and
 *       ejects result through the result face. On completion, drain the
 *       residue in, unlock.</li>
 * </ol>
 *
 * <p>{@link #addBehaviours(List)} only registers the
 * {@link CondenserCoilFluidBehaviour}; the rest of the BE is plain
 * {@link SmartBlockEntity} territory.
 */
public class CondenserCoilBlockEntity extends SmartBlockEntity {

    /** Tag namespace/path for the coolant fluid tag. */
    public static final TagKey<net.minecraft.world.level.material.Fluid> COOLANT_TAG =
        TagKey.create(net.minecraft.core.registries.Registries.FLUID,
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(TFCAeronautics.MOD_ID, "coolant"));

    /**
     * Three-step state machine. Persisted as a byte under {@code state}; the
     * ordinal is the wire format so DO NOT reorder the constants.
     */
    public enum State {
        IDLE,
        WARMUP,
        RUNNING
    }

    // --- persistent state ---------------------------------------------------

    private State state = State.IDLE;
    @Nullable private DistillatingRecipe activeRecipe;
    @Nullable private BlockPos tankControllerPos;
    /**
     * The face of the coil along the steam axis where the resolved input tank
     * sits. {@code null} until the first successful structural resolution, then
     * either {@link CondenserCoilFluidBehaviour#heatFace} or
     * {@link CondenserCoilFluidBehaviour#resultFace} depending on which side
     * the heater-below-tank was actually found. Used to compute the result
     * face as {@code inputFace.getOpposite()} for
     * {@link #ejectThroughResultFace}.
     */
    @Nullable private Direction inputFace;
    private BlockPos lastKnownResultNeighbor = BlockPos.ZERO;
    private int warmupTicks;
    private float progress;
    private int produced;
    private int snapshotVolume;
    private int targetVolume;

    // --- transient (per-tick) state -----------------------------------------

    /** Last observed tank volume; used to detect "fluid changed" during WARMUP. */
    private int lastObservedTankVolume = -1;

    /** Last heat temperature observed by the tick; null = none / not yet seen. */
    @Nullable private Float lastLoggedHeatTemp;

    /** Last coolant-flow flag observed by the tick. */
    private boolean lastLoggedCoolantFlow;

    /** Tick counter for the periodic diagnostics heartbeat. */
    private int heartbeatCounter;

    /** Tick counter for throttling the per-tick "structure not ready" log. */
    private int throttleCounter;

    public CondenserCoilBlockEntity(BlockPos pos, BlockState state) {
        super(CondenserCoilRegistration.CONDENSER_COIL_BE.get(), pos, state);
    }

    /**
     * Resolves the heat source. The source is the {@code HeatDealer} directly
     * under the controller block of the Create fluid tank on the heat side —
     * Create's fluid tank itself sits one block further from the coil than the
     * actual heat source (a firepit / stove / heater / charcoal forge).
     */
    @Nullable
    private static Float readHeatSourceTemperature(Level level, BlockPos tankControllerPos) {
        if (level == null) {
            return null;
        }
        BlockPos below = tankControllerPos.below();
        BlockState belowState = level.getBlockState(below);
        if (!HeatDealer.isHeatDealer(belowState)) {
            return null;
        }
        float t = HeatDealer.findTemperature(level, below, belowState);
        if (t == HeatDealer.NO_HEAT) {
            return null;
        }
        return t;
    }

    // --- behaviour registration --------------------------------------------

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(new CondenserCoilFluidBehaviour(this));
    }

    // --- main tick ---------------------------------------------------------

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) {
            return;
        }

        // Always watch the result-side neighbour: when a pipe breaks or is
        // rebuilt, or when the input tank swaps sides after a wrench rotation,
        // we want to re-prime the pump on the next distillating start.
        // Note: this uses the static blockstate-derived result face, not the
        // dynamic getResultFace(), because at this point inputFace has not been
        // re-resolved yet for this tick. The next tick will pick up the change
        // once the structural walk has had a chance to update inputFace.
        BlockPos resultNeighbor = CondenserCoilFluidBehaviour.resultNeighbor(worldPosition, getBlockState());
        if (!resultNeighbor.equals(lastKnownResultNeighbor)) {
            TFCAeronautics.LOGGER.debug("condenser_coil: result neighbour at {} changed: {} -> {}",
                worldPosition, lastKnownResultNeighbor, resultNeighbor);
            lastKnownResultNeighbor = resultNeighbor;
            // Force the pump to be re-applied on the next RUNNING entry.
        }

        // 1. Resolve structure.
        DistillatingStructure.Resolved resolved = DistillatingStructure.findTank(level, worldPosition, getBlockState());
        BlockPos controllerPos = resolved != null ? resolved.controller() : null;
        inputFace = resolved != null ? resolved.inputFace() : null;
        tankControllerPos = controllerPos;
        FluidTankBlockEntity controller = controllerPos != null
            ? DistillatingStructure.findTankControllerBE(level, worldPosition, getBlockState())
            : null;
        boolean hasCoolantFlow = hasCoolantFlow();
        Float heatTempC = controller != null ? readHeatSourceTemperature(level, controllerPos) : null;

        // Diff-log external inputs (heat, coolant). Only fires when something
        // actually changes — keeps the log readable in long sessions.
        if (!Objects.equals(heatTempC, lastLoggedHeatTemp)) {
            TFCAeronautics.LOGGER.debug("condenser_coil: heat at {} changed: {} -> {} (recipe min_temp={})",
                worldPosition, lastLoggedHeatTemp, heatTempC,
                activeRecipe != null ? String.valueOf(activeRecipe.minTemperature()) : "<none>");
            lastLoggedHeatTemp = heatTempC;
        }
        if (hasCoolantFlow != lastLoggedCoolantFlow) {
            TFCAeronautics.LOGGER.debug("condenser_coil: coolant flow at {} changed: {} -> {}",
                worldPosition, lastLoggedCoolantFlow, hasCoolantFlow);
            lastLoggedCoolantFlow = hasCoolantFlow;
        }

        // Reset heartbeat/throttle counters so the next time we re-enter the
        // "structure invalid" branch we get a full 5 s before the next log.
        // Otherwise a brief valid phase (e.g. a flicker) could leave the counter
        // near 100 and cause an instant heartbeat on the next invalid tick.
        heartbeatCounter = 0;
        throttleCounter = 0;

        // 2. Structure invalid → idle out and unlock the tank (if we owned a lock).
        if (controller == null || heatTempC == null) {
            String reason = controller == null ? "no Create fluid_tank in range"
                : describeHeatFailure(level, controllerPos);

            // Periodic diagnostics heartbeat (every 100 ticks ≈ 5 s). Surfaces
            // the coil's orientation, the blocks on its 4 horizontal faces, and
            // the actual block ID + heat value at controller.below() so the user
            // can tell whether the heat dealer is missing entirely vs. present
            // but cold — without needing to enter the world.
            heartbeatCounter++;
            if (heartbeatCounter >= 100) {
                heartbeatCounter = 0;
                BlockState self = getBlockState();
                Direction.Axis axis = CondenserCoilFluidBehaviour.steamAxis(self);
                Direction heat = CondenserCoilFluidBehaviour.heatFace(self);
                Direction result = heat.getOpposite();
                Direction perp = axis == Direction.Axis.X ? Direction.NORTH : Direction.EAST;
                BlockPos heatPos = worldPosition.relative(heat);
                BlockPos resultPos = worldPosition.relative(result);
                BlockPos perpPos = worldPosition.relative(perp);
                String heatWalk = DistillatingStructure.traceWalk(level, worldPosition, heat);
                String resultWalk = DistillatingStructure.traceWalk(level, worldPosition, result);
                String heatSourceInfo = controllerPos != null
                    ? describeHeatSource(level, controllerPos)
                    : "<no controller>";
                TFCAeronautics.LOGGER.info(
                    "condenser_coil: heartbeat at {}: AXIS={}, "
                        + "heat={}: [{}], "
                        + "result={}: [{}], "
                        + "perp={}={} (be={}), "
                        + "heatSource={}; "
                        + "reason={}",
                    worldPosition, axis,
                    heat, heatWalk,
                    result, resultWalk,
                    perp, describeBlock(level, perpPos),
                    describeBlockEntity(level, perpPos),
                    heatSourceInfo,
                    reason);
            }

            // Throttle the per-tick "structure not ready" chatter to once every
            // 5 s (matching the heartbeat cadence) so the log stays readable
            // when the player has the coil sitting in a broken config. The
            // heartbeat above already carries the full diagnostic context.
            throttleCounter++;
            boolean emitReadyLog = throttleCounter >= 100;
            if (emitReadyLog) {
                throttleCounter = 0;
            }
            if (state != State.IDLE) {
                TFCAeronautics.LOGGER.info("condenser_coil: structure invalid at {} ({}) — {} -> IDLE",
                    worldPosition, reason, state);
                throttleCounter = 0;
            } else if (emitReadyLog) {
                TFCAeronautics.LOGGER.info("condenser_coil: structure not ready at {} ({})",
                    worldPosition, reason);
            }
            if (state == State.RUNNING && controllerPos != null) {
                DistillatingTankLock.unlock(level, controllerPos);
            }
            state = State.IDLE;
            activeRecipe = null;
            return;
        }

        // 3. IDLE → try to enter WARMUP.
        if (state == State.IDLE) {
            FluidStack tankFluid = readTankFluid(controller);
            Optional<DistillatingRecipe> match = findMatchingRecipe(tankFluid);
            if (match.isPresent() && match.get().temperatureInRange(heatTempC) && tankFluid.getAmount() > 0) {
                activeRecipe = match.get();
                warmupTicks = Config.DISTILLATING_WARMUP_TICKS.get();
                lastObservedTankVolume = tankFluid.getAmount();
                state = State.WARMUP;
                TFCAeronautics.LOGGER.info(
                    "condenser_coil: IDLE -> WARMUP at {}: recipe={} (input={}, min_temperature={}), tankVol={}, heat={}°C, warmupTicks={}",
                    worldPosition, activeRecipe, activeRecipe.input(), activeRecipe.minTemperature(),
                    tankFluid.getAmount(), heatTempC, warmupTicks);
            } else if (tankFluid.getAmount() > 0) {
                TFCAeronautics.LOGGER.trace(
                    "condenser_coil: IDLE wait at {}: no recipe match for tank fluid={} (amount={}, heat={}°C)",
                    worldPosition, tankFluid.getFluid(), tankFluid.getAmount(), heatTempC);
            }
            return;
        }

        // 4. WARMUP → wait out the timer (any fluid change resets it).
        if (state == State.WARMUP) {
            if (!requireActiveRecipe(controller, State.WARMUP)) {
                return;
            }
            FluidStack tankFluid = readTankFluid(controller);
            int observed = tankFluid.getAmount();
            if (observed != lastObservedTankVolume) {
                TFCAeronautics.LOGGER.debug(
                    "condenser_coil: WARMUP timer reset at {}: tankVol {} -> {} (recipe={})",
                    worldPosition, lastObservedTankVolume, observed, activeRecipe.input());
                warmupTicks = Config.DISTILLATING_WARMUP_TICKS.get();
                lastObservedTankVolume = observed;
            }
            if (!hasCoolantFlow || !activeRecipe.temperatureInRange(heatTempC) || observed <= 0) {
                // Abort: lost the prerequisites.
                if (!hasCoolantFlow || !activeRecipe.temperatureInRange(heatTempC)) {
                    String reason = !hasCoolantFlow ? "no coolant flow"
                        : "temp " + heatTempC + " below min " + activeRecipe.minTemperature();
                    TFCAeronautics.LOGGER.debug(
                        "condenser_coil: WARMUP waiting at {}: {} (will resume when fixed)",
                        worldPosition, reason);
                    // Stay in WARMUP but reset; the player might fix things.
                    return;
                }
                TFCAeronautics.LOGGER.debug(
                    "condenser_coil: WARMUP aborted at {}: tank empty (recipe={})",
                    worldPosition, activeRecipe.input());
                state = State.IDLE;
                activeRecipe = null;
                return;
            }
            warmupTicks--;
            TFCAeronautics.LOGGER.trace(
                "condenser_coil: WARMUP ticking at {}: warmupTicks={}, recipe={}",
                worldPosition, warmupTicks, activeRecipe.input());
            if (warmupTicks > 0) {
                return;
            }
            // Lock and snapshot.
            snapshotVolume = observed;
            if (snapshotVolume <= 0) {
                state = State.IDLE;
                activeRecipe = null;
                return;
            }
            int target = snapshotVolume * activeRecipe.result_percent() / 100;
            if (target <= 0) {
                state = State.IDLE;
                activeRecipe = null;
                return;
            }
            targetVolume = target;
            produced = 0;
            progress = 0f;
            DistillatingTankLock.lock(level, controllerPos, worldPosition);
            state = State.RUNNING;
            TFCAeronautics.LOGGER.info(
                "condenser_coil: WARMUP -> RUNNING at {}: snapshotVol={}, targetVol={}, residueVol={}, "
                    + "result={}, residue={}",
                worldPosition, snapshotVolume, targetVolume, snapshotVolume - targetVolume,
                activeRecipe.result(), activeRecipe.residue());
            return;
        }

        // 5. RUNNING → produce.
        if (state == State.RUNNING) {
            if (!requireActiveRecipe(controller, State.RUNNING)) {
                // Couldn't recover the recipe after load — drain residual input
                // back to nothing and drop to IDLE so the player can restart
                // cleanly. Anything we already produced is lost; the tank lock
                // is released by the structure-invalid branch on the next tick.
                return;
            }
            if (!hasCoolantFlow || !activeRecipe.temperatureInRange(heatTempC)) {
                String reason = !hasCoolantFlow ? "no coolant flow"
                    : "temp " + heatTempC + " below min " + activeRecipe.minTemperature();
                TFCAeronautics.LOGGER.debug(
                    "condenser_coil: RUNNING paused at {}: produced={}/{}, reason={}",
                    worldPosition, produced, targetVolume, reason);
                // Pause (no progress, no abort). Keep the lock.
                return;
            }
            if (produced >= targetVolume) {
                finishRun(controller);
                return;
            }
            float rate = activeRecipe.rate();
            progress += rate;
            int whole = (int) progress;
            if (whole <= 0) {
                TFCAeronautics.LOGGER.trace(
                    "condenser_coil: RUNNING accumulating at {}: progress={}, produced={}/{}",
                    worldPosition, progress, produced, targetVolume);
                return;
            }
            if (whole > targetVolume - produced) {
                whole = targetVolume - produced;
            }
            progress -= whole;

            // Drain `whole` mB from the source tank and eject the same amount of
            // result fluid through the coil's result face. If the neighbour has
            // no IFluidHandler (no pipe / tank / basin, just air), the fluid is
            // intentionally destroyed rather than buffered.
            FluidStack drained = controller.getTankInventory().drain(whole, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            int actual = drained.getAmount();
            if (actual <= 0) {
                TFCAeronautics.LOGGER.warn(
                    "condenser_coil: tank drain returned 0 at {} during RUNNING — aborting (produced={}/{}, recipe={})",
                    worldPosition, produced, targetVolume, activeRecipe.input());
                // Nothing came out — abort the run rather than spin forever.
                finishRun(controller);
                return;
            }
            FluidStack toInsert = new FluidStack(activeRecipe.result(), actual);
            int accepted = ejectThroughResultFace(toInsert);
            if (accepted < actual) {
                TFCAeronautics.LOGGER.trace(
                    "condenser_coil: result face accepted only {}/{} at {} — remainder destroyed (no destination)",
                    accepted, actual, worldPosition);
            }
            produced += actual;
            TFCAeronautics.LOGGER.info(
                "condenser_coil: RUNNING produced +{} mB at {}: produced={}/{}, resultFluid={}, ejected={} mB",
                actual, worldPosition, produced, targetVolume, activeRecipe.result(), accepted);
            return;
        }
    }

    /**
     * Pushes {@code toInsert} into the {@link IFluidHandler} exposed by the
     * block adjacent to this coil on its result face, and walks the
     * downstream Create pipe graph to deliver the remainder to whatever
     * {@code IFluidHandler} sits at the end of the chain — typically the
     * output {@code create:fluid_tank}. Returns the amount the network
     * accepted in total; any remainder is discarded (no internal buffer).
     *
     * <p>If the neighbour is air, the fluid is "spilled" — server-side
     * pour-particles are emitted at the air block's position so the player
     * gets the same visual feedback as an open-end pipe, and the caller still
     * counts the volume toward {@code produced}. If the neighbour is a
     * non-fluid block (cobblestone, bedrock, modded block without
     * {@code IFluidHandler.BLOCK}), the fluid is silently destroyed with no
     * particles.
     *
     * <p>If the neighbour is a Create fluid pipe, the call always invokes
     * {@link #pushAlongPipeNetwork}: pipe blocks do not expose
     * {@code IFluidHandler.BLOCK} (their fluid state lives in
     * {@link PipeConnection} flows, not in a tank), so the
     * {@code handler.fill} probe above almost always returns 0 for them.
     * Without the explicit BFS, a coil-→-pipe-→-tank chain silently drops the
     * result. The BFS walks the pipe graph itself — independent of whether
     * {@code canHaveFlowToward} is open on the result face — and fills the
     * first non-pipe {@code IFluidHandler} downstream.
     */
    private int ejectThroughResultFace(FluidStack toInsert) {
        Direction resultFace = getResultFace();
        BlockPos neighborPos = worldPosition.relative(resultFace);
        BlockEntity neighbor = level.getBlockEntity(neighborPos);
        if (neighbor == null) {
            spawnOpenEndParticles(toInsert, neighborPos, resultFace);
            return 0;
        }
        IFluidHandler handler = level.getCapability(
            Capabilities.FluidHandler.BLOCK, neighborPos, resultFace.getOpposite());
        boolean neighborIsPipe = FluidPropagator.getPipe(level, neighborPos) != null;
        int accepted = 0;
        if (handler != null) {
            FluidStack probe = toInsert.copy();
            accepted = handler.fill(probe, IFluidHandler.FluidAction.EXECUTE);
        }
        if (neighborIsPipe) {
            // Walk the rest of the pipe graph to deliver the remainder to
            // the output tank (or any IFluidHandler we encounter on the way).
            // Pipes have no IFluidHandler of their own, so the only way to
            // push fluid through a pipe chain is the explicit BFS — skipping
            // it would silently drop the result whenever the neighbour is a
            // pipe (the common layout).
            FluidStack remaining = toInsert.copy();
            remaining.shrink(accepted);
            pushAlongPipeNetwork(remaining, resultFace, neighborPos);
            accepted = toInsert.getAmount() - remaining.getAmount();
        } else if (handler == null) {
            return 0; // non-fluid block (stone, bedrock, etc.) — silent drop
        }
        return accepted;
    }

    /**
     * BFS from {@code startPos} (the pipe directly adjacent to the coil on
     * the result face) through Create fluid pipes, dropping fluid directly
     * into each pipe's {@code IFluidHandler} and into the first non-pipe
     * {@code IFluidHandler} reachable downstream — typically the output
     * {@code create:fluid_tank}. Stops when {@code remaining} is empty, the
     * pipe graph drains, or the walk hits a non-fluid block.
     *
     * <p>Why BFS+{@code handler.fill} and not {@code addPressure}? The coil's
     * result face is closed in {@code canHaveFlowToward} to isolate the
     * coolant loop from the result tank; as a side effect, the neighbour pipe
     * has no PipeConnection on the side facing the coil
     * ({@code PROPERTY_BY_DIRECTION} = false), so {@code addPressure} on it
     * is a silent no-op. Walking the graph and pushing through each pipe's
     * capability bypasses the pipe-graph logic entirely — it works even when
     * Create's network would refuse to balance.
     *
     * <p>Branches: a T-junction in the middle of the path will get fluid
     * pushed into both branches' first IFluidHandler. That's a deliberate
     * trade-off — keeping branches isolated would require per-branch pressure
     * logic like {@code PumpBlockEntity.distributePressureTo}, which we
     * explicitly avoided for cross-flow reasons. The distillator's intended
     * layout is a straight dead-end pipe run; players who build a T-junction
     * get the obvious behaviour: fluid goes everywhere fluid can go.
     *
     * <p>Loop guard: we don't walk back through {@code enteredFrom}, so we
     * never return to the coil. {@code coilPos} is also pre-seeded into
     * {@code visited} so a pipe that has a connection back to it still
     * doesn't loop us back. BFS is bounded by the small pipe graph the
     * distillator is designed for (≤3 pipes), so the per-tick cost is tiny.
     *
     * <p>{@code remaining} is mutated in place: each fill shrinks it by the
     * accepted amount. When the caller is done, the difference between the
     * original and the final amount is what the BFS distributed beyond the
     * first neighbour.
     */
    private void pushAlongPipeNetwork(FluidStack remaining, Direction resultFace, BlockPos startPos) {
        if (remaining.isEmpty()) {
            return;
        }
        Set<BlockPos> visited = new HashSet<>();
        visited.add(worldPosition);
        visited.add(startPos);
        // frontier: (entered-from direction of pipe, position to inspect)
        Deque<Direction> entryDirs = new ArrayDeque<>();
        Deque<BlockPos> positions = new ArrayDeque<>();
        entryDirs.push(resultFace.getOpposite());
        positions.push(startPos);

        while (!positions.isEmpty() && !remaining.isEmpty()) {
            Direction enteredFrom = entryDirs.pop();
            BlockPos pipePos = positions.pop();
            if (!level.isLoaded(pipePos)) {
                continue;
            }

            BlockState pipeState = level.getBlockState(pipePos);
            FluidTransportBehaviour pipe = FluidPropagator.getPipe(level, pipePos);

            if (pipe != null) {
                // Pipe — push fluid into its internal tank and continue walking.
                IFluidHandler pipeHandler = level.getCapability(
                    Capabilities.FluidHandler.BLOCK, pipePos, null);
                if (pipeHandler != null) {
                    FluidStack probe = remaining.copy();
                    int accepted = pipeHandler.fill(probe, IFluidHandler.FluidAction.EXECUTE);
                    if (accepted > 0) {
                        remaining.shrink(accepted);
                        if (remaining.isEmpty()) {
                            return;
                        }
                    }
                }
                for (Direction next : FluidPropagator.getPipeConnections(pipeState, pipe)) {
                    if (next == enteredFrom) {
                        continue; // don't walk back through where we came from
                    }
                    BlockPos nextPos = pipePos.relative(next);
                    if (!visited.add(nextPos)) {
                        continue;
                    }
                    // Filter to IFluidHandler endpoints and other pipes only —
                    // air, stone, etc. are dead ends the walk stops at.
                    BlockEntity nextBE = level.getBlockEntity(nextPos);
                    if (nextBE == null) {
                        continue;
                    }
                    entryDirs.push(next.getOpposite());
                    positions.push(nextPos);
                }
            } else {
                // Non-pipe endpoint — try to push fluid into its handler.
                IFluidHandler handler = level.getCapability(
                    Capabilities.FluidHandler.BLOCK, pipePos, null);
                if (handler != null) {
                    FluidStack probe = remaining.copy();
                    int accepted = handler.fill(probe, IFluidHandler.FluidAction.EXECUTE);
                    if (accepted > 0) {
                        remaining.shrink(accepted);
                        if (remaining.isEmpty()) {
                            return;
                        }
                    }
                }
                // Stone / bedrock / no handler — stop on this branch.
            }
        }
    }

    /**
     * Emits pour-style particles at {@code airPos} (a block of air directly
     * adjacent to the coil on the result face) so the player gets visible
     * "spilling into open air" feedback when the coil is running without a
     * pipe output. Uses {@link ServerLevel#sendParticles} so the packets reach
     * the clients; the {@code FluidFX}-derived particle type is registered on
     * both sides so the client can render it.
     *
     * <p>Particle count scales with the volume spilled (1 per ~10 mB, capped
     * at 8 per tick) so a 50 mB/tick output produces a steady trickle rather
     * than a single brief puff. The offset/speed is small — fluid falling
     * straight down by gravity, the natural direction for an open-end pipe
     * pointing horizontally.
     */
    private void spawnOpenEndParticles(FluidStack fluid, BlockPos airPos, Direction resultFace) {
        if (fluid.isEmpty() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        ParticleOptions particle = com.simibubi.create.content.fluids.FluidFX.getFluidParticle(fluid);
        int count = Math.min(8, Math.max(1, fluid.getAmount() / 10));
        // Pour direction: fluid leaves the coil toward the result face.
        Vec3 dir = Vec3.atLowerCornerOf(resultFace.getNormal());
        Vec3 center = airPos.getCenter().add(dir.scale(0.25));
        double spread = 0.1;
        double speed = 0.05;
        serverLevel.sendParticles(particle,
            center.x, center.y, center.z,
            count,
            spread, spread, spread,
            speed);
    }

    private void finishRun(FluidTankBlockEntity controller) {
        // Replace whatever's left in the tank with `residueVol` mB of residue.
        // The tank still holds `(snapshotVolume - produced)` mB of the input
        // fluid — drain it, then fill residueVol mB of the recipe's residue.
        // Example: snapshot=1000, target=410 -> residueVol=590 -> final tank
        // contents: 590 mB of residue (matches the plan's "1000 -> 410 + 590").
        int residueVol = snapshotVolume - targetVolume;
        if (residueVol < 0) {
            residueVol = 0;
        }
        FluidTank tankInventory = controller.getTankInventory();

        FluidStack current = tankInventory.getFluid();
        int currentVol = current.getAmount();
        if (currentVol > 0) {
            tankInventory.drain(currentVol,
                net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
        }
        boolean residueAccepted = false;
        if (residueVol > 0 && activeRecipe != null) {
            FluidStack residue = new FluidStack(activeRecipe.residue(), residueVol);
            int filled = tankInventory.fill(residue,
                net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            residueAccepted = filled == residueVol;
            if (!residueAccepted) {
                TFCAeronautics.LOGGER.warn(
                    "condenser_coil: tank accepted only {}/{} mB of residue={} at {} — residue partially dropped",
                    filled, residueVol, activeRecipe.residue(), worldPosition);
            }
        }

        TFCAeronautics.LOGGER.info(
            "condenser_coil: RUNNING -> IDLE at {}: recipe={}, drained={} mB of input, produced={} mB of {}, "
                + "tank now {} mB of {} (target was {})",
            worldPosition, activeRecipe, currentVol, produced, activeRecipe != null ? activeRecipe.result() : "<none>",
            residueVol, activeRecipe != null ? activeRecipe.residue() : "<none>", residueVol);

        DistillatingTankLock.unlock(level, tankControllerPos);
        state = State.IDLE;
        activeRecipe = null;
        produced = 0;
        progress = 0f;
        snapshotVolume = 0;
        targetVolume = 0;
        lastObservedTankVolume = -1;
        setChanged();
        sendData();
    }

    @Nullable
    private FluidStack readTankFluid(FluidTankBlockEntity controller) {
        return controller.getTankInventory().getFluid();
    }

    private Optional<DistillatingRecipe> findMatchingRecipe(FluidStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        List<net.minecraft.world.item.crafting.RecipeHolder<DistillatingRecipe>> all =
            level.getRecipeManager().getAllRecipesFor(DistillatingRecipeType.TYPE.value());
        TFCAeronautics.LOGGER.trace(
            "condenser_coil: scanning {} distillating recipes at {} for tank fluid={}",
            all.size(), worldPosition, stack.getFluid());
        for (net.minecraft.world.item.crafting.RecipeHolder<DistillatingRecipe> holder : all) {
            DistillatingRecipe r = holder.value();
            if (r.matches(stack)) {
                TFCAeronautics.LOGGER.debug(
                    "condenser_coil: matched distillating recipe at {}: input={}, result={}, residue={}, "
                        + "result_percent={}, rate={}, min_temperature={}",
                    worldPosition, r.input(), r.result(), r.residue(), r.result_percent(), r.rate(),
                    r.minTemperature());
                return Optional.of(r);
            }
        }
        return Optional.empty();
    }

    /**
     * Recovery hook for save/load: {@link #activeRecipe} is intentionally NOT
     * persisted (datapacks can change recipes between sessions), but the
     * persisted {@link #state} may already be {@link State#WARMUP} or
     * {@link State#RUNNING} when the world loads. Re-resolves the recipe from
     * the current tank fluid so the WARMUP/RUNNING branches can use it without
     * a {@link NullPointerException}.
     *
     * <p>Returns {@code true} if {@code activeRecipe} is usable (was already
     * set, or was just resolved). Returns {@code false} if no matching recipe
     * exists — in that case the caller should drop back to IDLE and let the
     * player re-trigger warmup once the tank is refilled.
     */
    private boolean ensureActiveRecipe(FluidTankBlockEntity controller) {
        if (activeRecipe != null) {
            return true;
        }
        FluidStack tankFluid = readTankFluid(controller);
        Optional<DistillatingRecipe> match = findMatchingRecipe(tankFluid);
        if (match.isEmpty()) {
            TFCAeronautics.LOGGER.info(
                "condenser_coil: post-load recipe re-resolution failed at {}: no recipe for tank fluid={} — aborting to IDLE",
                worldPosition, tankFluid.getFluid());
            return false;
        }
        activeRecipe = match.get();
        TFCAeronautics.LOGGER.info(
            "condenser_coil: post-load recipe re-resolved at {}: recipe={}",
            worldPosition, activeRecipe);
        return true;
    }

    /**
     * Defensive guard for the WARMUP/RUNNING hot-path branches that read
     * {@link #activeRecipe}. On null, logs a warning with enough context to
     * triangulate the regression (state, tank fluid amount, controller pos)
     * and returns {@code false} so the caller can transition to
     * {@link State#IDLE} and return.
     *
     * <p>Replaces the silent {@code NullPointerException} the pre-fix coil
     * threw on a state-mismatch (e.g. after a save/load where {@code state}
     * restored as {@code WARMUP}/{@code RUNNING} but the recipe manager has
     * no matching recipe for the current tank fluid). The original behaviour
     * was a server-side crash; this gives the player a clean transition to
     * IDLE plus a WARN log line so a recurrence can be diagnosed.
     */
    private boolean requireActiveRecipe(FluidTankBlockEntity controller, State expected) {
        if (activeRecipe != null) {
            return true;
        }
        int tankAmount = -1;
        if (controller != null) {
            FluidStack fluid = controller.getTankInventory().getFluid();
            tankAmount = fluid != null ? fluid.getAmount() : -1;
        }
        TFCAeronautics.LOGGER.warn(
            "condenser_coil: activeRecipe null in {} branch at {} — falling back to IDLE "
                + "(tankFluidAmount={}, controllerPos={}, state={}). "
                + "If this recurs, capture the previous tick's DEBUG log for the recipe re-resolution path.",
            expected, worldPosition, tankAmount, tankControllerPos, state);
        state = State.IDLE;
        activeRecipe = null;
        return false;
    }

    /**
     * Returns {@code true} when the coil sees a complete inbound/outbound flow
     * pair along the water axis with the fluid belonging to {@link #COOLANT_TAG}.
     * Stagnant coolant (no flow at all) does NOT count.
     */
    private boolean hasCoolantFlow() {
        FluidTransportBehaviour behaviour = getBehaviour(FluidTransportBehaviour.TYPE);
        if (behaviour == null) {
            return false;
        }
        Direction[] faces = CondenserCoilFluidBehaviour.waterFaces(getBlockState());
        PipeConnection.Flow a = flow(behaviour, faces[0]);
        PipeConnection.Flow b = flow(behaviour, faces[1]);
        if (a == null || b == null) {
            return false;
        }
        if (!a.complete || !b.complete) {
            return false;
        }
        if (!(a.inbound ^ b.inbound)) {
            return false; // both inbound or both outbound — no net flow
        }
        PipeConnection.Flow inflow = a.inbound ? a : b;
        FluidStack fluid = inflow.fluid;
        if (fluid == null || fluid.isEmpty()) {
            return false;
        }
        return fluid.is(COOLANT_TAG);
    }

    @Nullable
    private static PipeConnection.Flow flow(FluidTransportBehaviour behaviour, Direction face) {
        return behaviour.getFlow(face);
    }

    // --- public accessors used by capabilities / external callers ---------

    /**
     * Result face of the coil — the face the distillate is ejected toward.
     *
     * <p>Resolved dynamically from {@link #inputFace} (the face the input tank
     * was actually found on) rather than from the static
     * {@link CondenserCoilFluidBehaviour#resultFace}, because the input tank
     * can legitimately sit on either side of the coil and the coil only knows
     * which side at runtime. If the structure is unresolved, falls back to the
     * blockstate-derived result face so callers used during diagnostics still
     * have a deterministic direction.
     */
    public Direction getResultFace() {
        if (inputFace != null) {
            return inputFace.getOpposite();
        }
        return CondenserCoilFluidBehaviour.resultFace(getBlockState());
    }

    /** Public read-only access to the active recipe (used for goggle info etc). */
    public Optional<DistillatingRecipe> getActiveRecipe() {
        return Optional.ofNullable(activeRecipe);
    }

    public State getDistillationState() {
        return state;
    }

    public int getProduced() {
        return produced;
    }

    public int getTargetVolume() {
        return targetVolume;
    }

    public int getWarmupTicks() {
        return warmupTicks;
    }

    // --- persistence -------------------------------------------------------

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putByte("state", (byte) state.ordinal());
        tag.putInt("warmup", warmupTicks);
        tag.putFloat("progress", progress);
        tag.putInt("produced", produced);
        tag.putInt("snapshotVolume", snapshotVolume);
        tag.putInt("targetVolume", targetVolume);
        if (tankControllerPos != null) {
            tag.putLong("tankControllerPos", tankControllerPos.asLong());
        }
        if (inputFace != null) {
            tag.putInt("inputFace", inputFace.ordinal());
        }
        if (lastKnownResultNeighbor != null) {
            tag.putLong("resultNeighbor", lastKnownResultNeighbor.asLong());
        }
        // Note: activeRecipe is not persisted directly — RecipeManager resolves it again next tick.
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        state = State.values()[tag.getByte("state") & 0xff];
        warmupTicks = tag.getInt("warmup");
        progress = tag.getFloat("progress");
        produced = tag.getInt("produced");
        snapshotVolume = tag.getInt("snapshotVolume");
        targetVolume = tag.getInt("targetVolume");
        tankControllerPos = tag.contains("tankControllerPos")
            ? BlockPos.of(tag.getLong("tankControllerPos"))
            : null;
        if (tag.contains("inputFace")) {
            int ord = tag.getInt("inputFace");
            inputFace = (ord >= 0 && ord < Direction.values().length) ? Direction.values()[ord] : null;
        } else {
            inputFace = null;
        }
        lastKnownResultNeighbor = tag.contains("resultNeighbor")
            ? BlockPos.of(tag.getLong("resultNeighbor"))
            : BlockPos.ZERO;
        activeRecipe = null;
        // Reset transient diff-tracking fields so the first post-load tick
        // re-emits a "structure changed" DEBUG log.
        lastLoggedHeatTemp = null;
        lastLoggedCoolantFlow = false;
    }

    // --- diagnostic helpers -----------------------------------------------

    /** Short {@code "<id>#<state>"} description for log messages. */
    private static String describeBlock(Level level, BlockPos pos) {
        if (level == null) {
            return "?";
        }
        BlockState state = level.getBlockState(pos);
        return state.getBlockHolder().unwrapKey().map(k -> k.location().toString()).orElse("?") + state;
    }

    /** Short {@code "<class>" or "<none>"} description for log messages. */
    private static String describeBlockEntity(Level level, BlockPos pos) {
        if (level == null) {
            return "?";
        }
        BlockEntity be = level.getBlockEntity(pos);
        return be == null ? "none" : be.getClass().getSimpleName();
    }

    /**
     * Describes the block at {@code controller.below()} for the heartbeat log.
     * Distinguishes the three failure modes the player can hit:
     * <ul>
     *   <li>no controller (shouldn't happen if {@code controllerPos != null}, but defensive);</li>
     *   <li>block at the heat-source slot is not a registered HeatDealer
     *       (e.g. dirt, fluid, plain stone) — names the actual block id;</li>
     *   <li>block <em>is</em> a HeatDealer but reports
     *       {@link HeatDealer#NO_HEAT} — names the block and reports the
     *       dealer's temperature so the player can see "heater is there and
     *       cold, not missing".</li>
     * </ul>
     */
    private static String describeHeatSource(Level level, BlockPos controllerPos) {
        if (level == null) {
            return "<no level>";
        }
        BlockPos below = controllerPos.below();
        BlockState state = level.getBlockState(below);
        String blockId = state.getBlockHolder().unwrapKey()
            .map(k -> k.location().toString()).orElse("?");
        if (!HeatDealer.isHeatDealer(state)) {
            return below + "=" + blockId + " (not a HeatDealer)";
        }
        float t = HeatDealer.findTemperature(level, below, state);
        if (t == HeatDealer.NO_HEAT) {
            return below + "=" + blockId + " (HeatDealer present, temp=0/NO_HEAT)";
        }
        return below + "=" + blockId + " (temp=" + t + "°C)";
    }

    /**
     * Returns a one-line reason for the "no heat under tank" branch of the
     * structure-invalid log. Distinguishes "no block there / wrong block"
     * ("no HeatDealer at controller.below()") from "block is a HeatDealer but
     * currently cold" ("HeatDealer at controller.below() reports NO_HEAT")
     * so the player doesn't have to guess which setup mistake they made.
     */
    private static String describeHeatFailure(Level level, BlockPos controllerPos) {
        if (level == null || controllerPos == null) {
            return "no HeatDealer / heat=0 under tank";
        }
        BlockPos below = controllerPos.below();
        BlockState state = level.getBlockState(below);
        if (!HeatDealer.isHeatDealer(state)) {
            return "no HeatDealer at " + below + " (found " + describeBlock(level, below) + ")";
        }
        return "HeatDealer at " + below + " reports NO_HEAT (found " + describeBlock(level, below) + ")";
    }
}
