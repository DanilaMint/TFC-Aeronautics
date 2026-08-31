package ru.tfc_aeronautics.mixin;

import com.simibubi.create.content.processing.AssemblyOperatorBlockItem;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ru.tfc_aeronautics.welding_depot.WeldingDepotBlock;

/**
 * Treats a {@link WeldingDepotBlock} as an "assembly operator" target so that
 * any item registered through Create's {@link AssemblyOperatorBlockItem}
 * (mechanical press, mixer, spout, deployer) is placed one block above the
 * depot with an air gap, matching the behaviour of {@code create:depot}.
 *
 * <p>The Create {@code place(...)} method already shifts the placement
 * position to {@code placedOnPos.above(2)} when {@code operatesOn} returns
 * {@code true} and the click face is {@code UP}, so this mixin only needs to
 * add the depot to the recognised-target set.
 */
@Mixin(AssemblyOperatorBlockItem.class)
public abstract class AssemblyOperatorBlockItemMixin
{
    @Inject(method = "operatesOn", at = @At("TAIL"), cancellable = true)
    private void aeronautics$operatesOnWeldingDepot(
        LevelReader world, BlockPos pos, BlockState placedOnState,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (cir.getReturnValue()) {
            return;
        }
        if (placedOnState.getBlock() instanceof WeldingDepotBlock) {
            cir.setReturnValue(true);
        }
    }
}
