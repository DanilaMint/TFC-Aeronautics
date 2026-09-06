package ru.tfc_aeronautics.condenser_coil;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which {@code create:fluid_tank} controllers are currently claimed by a
 * running condenser-coil distillation, so the coil can keep its input/output
 * snapshot stable for the duration of the run.
 *
 * <p>When a controller is locked, {@link #isLocked(Level, BlockPos)} returns
 * {@code true} and
 * {@code ru.tfc_aeronautics.mixin.FluidTankBlockEntityMixin#aeronautics$lockHandlerIfDistilling}
 * swaps {@code FluidTankBlockEntity.handlerForCapability()} for a drain-only
 * wrapper, preventing the player (or a Create pump) from adding more fluid while
 * the coil is mid-distillation.
 *
 * <p>The lock map lives in a <em>plain</em> class (not a mixin) on purpose:
 * cross-target state stored in a {@code @Mixin(Standard)} field cannot be
 * {@code cast}-ed back from outside (see
 * {@code feedback_standard_mixin_not_loadable.md}), and another class is needed
 * for {@code FluidTankBlockEntityMixin} to read at apply-time.
 *
 * <p>The map key is the (dimension, controller-pos) pair: a Create fluid tank
 * is a single block entity per chunk column, but the player's level is
 * available as a {@link ResourceKey}. We use a local record
 * ({@link GlobalPos}) instead of Mojang's {@code net.minecraft.core.GlobalPos}
 * because we don't need the extra fields Mojang's record carries.
 *
 * <p>Thread-safety: {@link ConcurrentHashMap} so the condenser-coil logic and
 * any cross-tick interaction (e.g. a future scheduled task) don't need a
 * separate lock.
 */
public final class DistillationTankLock
{
    /**
     * Pure data key: which (dimension, position) the lock refers to.
     *
     * @param dimension dimension the controller lives in
     * @param pos       controller position (== {@code BlockEntity.worldPosition}
     *                  for the controller block itself)
     */
    public record GlobalPos(ResourceKey<Level> dimension, BlockPos pos) {}

    /** Active locks, keyed by (dimension, controller-pos). */
    private static final Map<GlobalPos, BlockPos> LOCKED = new ConcurrentHashMap<>();

    private DistillationTankLock()
    {
        // utility class — no instances
    }

    /**
     * Marks the given fluid-tank controller as locked by the condenser coil at
     * {@code coilPos}. Overwrites any previous lock from another coil at the
     * same controller (Create prevents two coils from sharing a tank, so this
     * is the safe default).
     */
    public static void lock(Level level, BlockPos tankControllerPos, BlockPos coilPos)
    {
        LOCKED.put(new GlobalPos(level.dimension(), tankControllerPos), coilPos);
    }

    /**
     * Releases the lock on a fluid-tank controller. Safe to call when no lock
     * is held.
     */
    public static void unlock(Level level, BlockPos tankControllerPos)
    {
        LOCKED.remove(new GlobalPos(level.dimension(), tankControllerPos));
    }

    /**
     * @return {@code true} if a condenser coil currently holds a lock on the
     *         given fluid-tank controller, {@code false} otherwise.
     */
    public static boolean isLocked(Level level, BlockPos tankControllerPos)
    {
        if (level == null)
        {
            return false;
        }
        return LOCKED.containsKey(new GlobalPos(level.dimension(), tankControllerPos));
    }
}
