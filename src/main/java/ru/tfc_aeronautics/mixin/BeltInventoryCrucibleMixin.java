package ru.tfc_aeronautics.mixin;

import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.BeltInventory;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Bridges Create belt → arbitrary block-entity inventory via capability lookup.
 *
 * <p>By default, {@code BeltInventory.resolveEnding} only returns
 * {@code Ending.INSERT} when the target BE is a {@link SmartBlockEntity} that
 * has registered a {@link DirectBeltInputBehaviour}. For non-SmartBlockEntity
 * targets (e.g. TFC Crucible, which extends vanilla
 * {@code TickableInventoryBlockEntity}), the lookup returns {@code null} and
 * the belt sees {@code Ending.BLOCKED} / {@code Ending.EJECT} — items never
 * enter the inventory.
 *
 * <p>This mixin adds a capability-based fallback: any BE (Smart or not) that
 * exposes an {@code ITEM_HANDLER} capability on the belt's
 * {@code movementFacing} side receives a synthetic {@link DirectBeltInputBehaviour}
 * that proxies {@code tryInsert} through {@code ItemHandlerHelper.insertItemStacked}.
 *
 * <h2>Why a wrapper, not raw capability injection?</h2>
 * {@code BeltInventory.tick}'s INSERT branch calls
 * {@code BlockEntityBehaviour.get(...TYPE)} → if non-null, {@code handleInsertion(...)}.
 * We can't insert a plain {@code IItemHandler} into that flow without rewriting
 * the control flow; we can, however, return a {@code DirectBeltInputBehaviour}
 * subclass from the same {@code get(...)} call, which is a no-op from the
 * belt's perspective.
 *
 * <h2>Why {@code super((SmartBlockEntity) null)}</h2>
 * {@code DirectBeltInputBehaviour}'s parent constructor sets
 * {@code blockEntity = be} (no NPE on null), and {@code tryInsert =
 * this::defaultInsertionCallback}. We immediately overwrite {@code tryInsert}
 * via {@code setInsertionHandler(...)} (the only public setter, which writes
 * the private field). The default {@code defaultInsertionCallback} — which
 * dereferences {@code blockEntity} — is never called. {@link SmartBlockEntity}-
 * derived infrastructure methods ({@code getPos}, {@code getWorld}) are never
 * invoked on this wrapper because it's not registered on any
 * {@code SmartBlockEntity}'s behaviour list.
 *
 * <h2>Why reflection for {@code Ending.INSERT}?</h2>
 * {@code BeltInventory.Ending} is a {@code private enum} nested inside
 * {@code BeltInventory} — referencing it from a separate compilation unit is
 * a compile error. We resolve the enum constant by name via
 * {@code Class.forName(...)+Enum.valueOf(...)} once at class-load time, and
 * compare / set it through {@code Object} on the {@code CallbackInfoReturnable}.
 */
@Mixin(BeltInventory.class)
public abstract class BeltInventoryCrucibleMixin
{
    @Unique private static final Object ENDING_INSERT;

    static
    {
        try
        {
            Class<?> endingClass = Class.forName(
                "com.simibubi.create.content.kinetics.belt.transport.BeltInventory$Ending"
            );
            // Enum constants are public static fields — use getField rather than
            // Enum.valueOf to avoid the Class<T extends Enum<T>> generic bound
            // (which `Class.forName` returns as raw Class, breaking inference).
            ENDING_INSERT = endingClass.getField("INSERT").get(null);
        }
        catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e)
        {
            throw new IllegalStateException(
                "BeltInventoryCrucibleMixin: BeltInventory$Ending enum not found — "
                    + "Create refactor may have renamed it; update the mixin accordingly.",
                e);
        }
    }

    @Shadow BeltBlockEntity belt;
    @Shadow boolean beltMovementPositive;

    /**
     * If the default resolution returned BLOCKED / EJECT, check whether the
     * target BE exposes an item capability on the belt's movementFacing side.
     * If yes, flip to INSERT so the tick loop reaches the capability-aware
     * insertion path.
     */
    @Inject(method = "resolveEnding", at = @At("RETURN"), cancellable = true)
    private void aeronautics$resolveCapabilityInsert(CallbackInfoReturnable<Object> cir)
    {
        Object ending = cir.getReturnValue();
        if (ENDING_INSERT.equals(ending))
        {
            return;
        }

        Direction movementFacing = belt.getMovementFacing();
        BlockPos targetPos = BeltHelper.getPositionForOffset(belt, beltMovementPositive ? belt.beltLength : -1);
        Level world = belt.getLevel();

        IItemHandler handler = world.getCapability(Capabilities.ItemHandler.BLOCK, targetPos, movementFacing);
        if (handler == null)
        {
            return;
        }

        cir.setReturnValue(ENDING_INSERT);
    }

    /**
     * When the lookup of {@code DirectBeltInputBehaviour} at the target returns
     * null (because the target is a non-{@code SmartBlockEntity} BE), substitute
     * a {@link CapabilityDirectBeltInputBehaviour} that delegates to capability
     * lookup. For any other behaviour type or for genuine non-null lookups,
     * fall through to the original logic unchanged.
     */
    @Redirect(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "com/simibubi/create/foundation/blockEntity/behaviour/BlockEntityBehaviour"
                + ".get(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;"
                + "Lcom/simibubi/create/foundation/blockEntity/behaviour/BehaviourType;)"
                + "Lcom/simibubi/create/foundation/blockEntity/behaviour/BlockEntityBehaviour;"
        )
    )
    private static <T extends BlockEntityBehaviour> T aeronautics$capabilityBehaviourFallback(
        BlockGetter reader, BlockPos pos, BehaviourType<T> type
    )
    {
        T original = BlockEntityBehaviour.get(reader, pos, type);
        if (original != null)
        {
            return original;
        }
        if (type != DirectBeltInputBehaviour.TYPE)
        {
            return null;
        }
        if (!(reader instanceof Level level))
        {
            return null;
        }
        @SuppressWarnings("unchecked")
        T wrapper = (T) new CapabilityDirectBeltInputBehaviour(level, pos);
        return wrapper;
    }

    /**
     * Lightweight {@link DirectBeltInputBehaviour} backed by capability lookup
     * instead of a {@link SmartBlockEntity}. Constructed with
     * {@code super((SmartBlockEntity) null)}; the default {@code tryInsert}
     * (which dereferences {@code blockEntity}) is overwritten via
     * {@code setInsertionHandler(...)} immediately with a lambda using our
     * cached {@code level}/{@code pos}.
     *
     * <p>Not registered on any {@code SmartBlockEntity}, so neither
     * {@code tick()} nor {@code getPos()}/{@code getWorld()} (which would NPE
     * on a null {@code blockEntity}) are invoked by Create's infrastructure.
     */
    static final class CapabilityDirectBeltInputBehaviour extends DirectBeltInputBehaviour
    {
        private final Level level;
        private final BlockPos pos;

        CapabilityDirectBeltInputBehaviour(Level level, BlockPos pos)
        {
            super((SmartBlockEntity) null);
            this.level = level;
            this.pos = pos;
            this.setInsertionHandler((inserted, side, simulate) -> {
                IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
                if (handler == null)
                {
                    return inserted.stack;
                }
                return ItemHandlerHelper.insertItemStacked(handler, inserted.stack.copy(), simulate);
            });
        }

        @Override
        public boolean canInsertFromSide(Direction side)
        {
            return level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side) != null;
        }
    }

    /**
     * Local import helper for {@link #aeronautics$resolveCapabilityInsert} —
     * keeps the @Inject method body short and avoids re-importing
     * {@code BeltHelper} in the outer mixin class.
     */
    private static final class BeltHelper
    {
        static BlockPos getPositionForOffset(BeltBlockEntity belt, int offset)
        {
            return com.simibubi.create.content.kinetics.belt.BeltHelper.getPositionForOffset(belt, offset);
        }
    }
}
