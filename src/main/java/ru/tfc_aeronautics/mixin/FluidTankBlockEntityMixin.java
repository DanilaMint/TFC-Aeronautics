package ru.tfc_aeronautics.mixin;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ru.tfc_aeronautics.condenser_coil.DistillatingTankLock;

/**
 * Locks a {@code create:fluid_tank} controller for the duration of a condenser
 * coil distillation.
 *
 * <p>The injection targets
 * {@link FluidTankBlockEntity#handlerForCapability()} (private, non-{@code void},
 * so the callback type must be {@link CallbackInfoReturnable} — using
 * {@code CallbackInfo} on a non-{@code void} target compiles fine but the mixin
 * fails to apply at runtime with
 * {@code Invalid descriptor ... expected ... but found ..., see
 * feedback_mixin_callback_info.md}). We use {@code at = @At("RETURN")} so the
 * real handler from Create has already been computed; we only swap it for a
 * read-only wrapper when needed.
 *
 * <p>Why a wrapper and not a hard return: the rest of the tank's logic still
 * expects an {@link IFluidHandler} (e.g. FluidValve/encoded-pattern lookups),
 * and Create's turbine/drain-side interactions must keep reading fluid out.
 * We only block {@link IFluidHandler#fill}. Drain side and read-only accessors
 * ({@link #getFluidInTank(int)}, {@link #getTankCapacity(int)},
 * {@link #getTanks()}, {@link #isFluidValid(int, FluidStack)}) delegate to
 * the original so the snapshot Create took at the start of the distillation
 * stays consistent.
 *
 * <p>The lock state itself lives in
 * {@link DistillatingTankLock}, not on the mixin — Standard mixin classes are
 * not loadable from outside (see
 * {@code feedback_standard_mixin_not_loadable.md}), so the only safe place for
 * cross-target state is a plain helper class.
 *
 * <p>Only controllers are checked: from a non-controller we recurse into
 * {@code getControllerBE().handlerForCapability()}, so every reached
 * invocation will eventually call this mixin on the controller. We still
 * guard on {@code isController()} for paranoia against private recursion.
 */
@Mixin(FluidTankBlockEntity.class)
public abstract class FluidTankBlockEntityMixin
{
    @Inject(method = "handlerForCapability", at = @At("RETURN"), cancellable = true)
    private void aeronautics$lockHandlerIfDistillating(CallbackInfoReturnable<IFluidHandler> cir)
    {
        FluidTankBlockEntity self = (FluidTankBlockEntity) (Object) this;
        if (!self.isController())
        {
            return;
        }
        Level level = self.getLevel();
        if (level == null)
        {
            return;
        }
        if (!DistillatingTankLock.isLocked(level, self.getBlockPos()))
        {
            return;
        }
        IFluidHandler original = cir.getReturnValue();
        if (original == null)
        {
            return;
        }
        cir.setReturnValue(new DrainOnlyFluidHandler(original));
    }

    /**
     * Read-only proxy around an {@link IFluidHandler}: every {@code fill}
     * returns {@code 0} (refuses to take any fluid in), while all other
     * operations pass through to the delegate so the tank keeps reporting the
     * same contents it had when the distillation was started.
     *
     * <p>Stateless on purpose: every method just defers to
     * {@link #delegate}. The lifecycle is tied to the mixin's
     * {@code cir.setReturnValue(...)} — Create re-creates the capability on
     * {@code refreshCapability()} which would hand out a fresh wrapper anyway.
     */
    static final class DrainOnlyFluidHandler implements IFluidHandler
    {
        private final IFluidHandler delegate;

        DrainOnlyFluidHandler(IFluidHandler delegate)
        {
            this.delegate = delegate;
        }

        @Override
        public int getTanks()
        {
            return delegate.getTanks();
        }

        @Override
        public FluidStack getFluidInTank(int tank)
        {
            return delegate.getFluidInTank(tank);
        }

        @Override
        public int getTankCapacity(int tank)
        {
            return delegate.getTankCapacity(tank);
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack)
        {
            // Keep validation honest: if the underlying handler wouldn't
            // accept this fluid, we wouldn't either, even if our fill() would
            // return 0 anyway.
            return delegate.isFluidValid(tank, stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action)
        {
            // Hard refusal: a distillation in progress must not be diluted.
            // We deliberately ignore `action.SIMULATE` vs EXECUTE — there is
            // no meaningful difference here because the answer is always 0.
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action)
        {
            return delegate.drain(resource, action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action)
        {
            return delegate.drain(maxDrain, action);
        }
    }
}
