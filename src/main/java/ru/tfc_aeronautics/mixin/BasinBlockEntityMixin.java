package ru.tfc_aeronautics.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;

import ru.tfc_aeronautics.heat.HeatDealer;
import ru.tfc_aeronautics.heat.HeatDealers;

/**
 * Lets a basin sitting on top of a TFC heat source (lit firepit, charcoal forge,
 * {@code tfc_aeronautics:heater}) count as heated, so {@code create:mixing} recipes
 * that require heat actually run.
 *
 * <p>The injection targets {@code getHeatLevel()} rather than the more obvious
 * {@code getHeatLevelOf(BlockState)} because the latter only receives a
 * {@link BlockState} — a firepit's temperature lives in its block entity and cannot
 * be recovered from the state alone. {@code getHeatLevel()} is the only place with
 * access to both {@code level} and the basin's position.
 *
 * <p>{@code cachedHeatLevel} is deliberately left untouched: a firepit's temperature
 * changes every tick while it heats up and cools down, so caching it would freeze the
 * basin at whatever level it saw first. Returning from {@code HEAD} bypasses that cache
 * entirely and re-reads the source each call.
 *
 * <p>When the block below is not a registered {@link HeatDealer} we fall through to
 * Create's own logic without setting a return value, so blaze burners and everything
 * in {@code #create:passive_boiler_heaters} (lava, magma, campfires) keep working.
 */
@Mixin(BasinBlockEntity.class)
public abstract class BasinBlockEntityMixin
{
    @Inject(method = "getHeatLevel", at = @At("HEAD"), cancellable = true)
    private void aeronautics$heatLevelFromHeatDealer(CallbackInfoReturnable<BlazeBurnerBlock.HeatLevel> cir)
    {
        BasinBlockEntity self = (BasinBlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level == null)
        {
            return;
        }

        BlockPos belowPos = self.getBlockPos().below();
        BlockState state = level.getBlockState(belowPos);
        if (!HeatDealer.isHeatDealer(state))
        {
            return;
        }

        cir.setReturnValue(HeatDealers.toHeatLevel(HeatDealer.findTemperature(level, belowPos, state)));
    }
}
