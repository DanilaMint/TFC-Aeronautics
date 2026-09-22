package ru.tfc_aeronautics.mixin;

import java.lang.reflect.Field;

import net.dries007.tfc.common.blockentities.CrucibleBlockEntity;
import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.capabilities.PartialFluidHandler;
import net.dries007.tfc.common.capabilities.PartialItemHandler;
import net.dries007.tfc.common.capabilities.SidedHandler;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Wires up TFC's {@code CrucibleBlockEntity} sided inventory/fluid handlers
 * unconditionally — independent of the {@code crucibleEnableAutomation} server
 * config flag — so that all five Create/automation integrations work:
 *
 * <ol>
 *   <li>Item drop from above → {@code INPUT} slots 0-8 ({@link Direction#UP}).</li>
 *   <li>Create belt from the side → {@code INPUT} slots 0-8
 *       ({@link Direction.Plane#HORIZONTAL} insert).</li>
 *   <li>Create fluid pipe → {@code UP} insert, {@code HORIZONTAL} extract.</li>
 *   <li>Create andesite funnel from the side → extracts from
 *   {@code SLOT_OUTPUT} (=9) ({@link Direction.Plane#HORIZONTAL} extract).</li>
 *   <li>TFC firepit/charcoal forge/firebox and our heater already heat the
 *   crucible via TFC's own {@code IHeatConsumer} capability on {@code DOWN}
 *   — no work needed here.</li>
 * </ol>
 *
 * <h2>Why no {@code @Shadow} for {@code sidedInventory}?</h2>
 * Mixin's {@code @Shadow} searches for the field only in the target class
 * itself — it does NOT walk the inheritance chain. {@code sidedInventory} is
 * declared {@code protected} on the parent {@code InventoryBlockEntity}, so a
 * {@code @Shadow} on this mixin's target ({@code CrucibleBlockEntity}) throws
 * {@code InvalidMixinException: @Shadow field sidedInventory was not located
 * in the target class} at apply time. The Java compiler also refuses plain
 * {@code self.sidedInventory} access because the mixin class — which has no
 * declared {@code extends} in source — is not a recognised subclass of
 * {@code InventoryBlockEntity} for protected-visibility purposes. We therefore
 * resolve the inherited field through a {@code static} reflection handle
 * captured once at class-load time. The handle is then used inside the TAIL
 * injection.
 *
 * <p>{@code sidedFluidInventory} is declared {@code private} on
 * {@code CrucibleBlockEntity} itself, so {@code @Shadow} works for it as usual.
 *
 * <h2>Why TAIL injection point</h2>
 * TFC's own {@code CrucibleBlockEntity.<init>} body contains
 * {@code if (TFCConfig.SERVER.crucibleEnableAutomation.get()) { ... }} which
 * calls {@code sidedInventory.on(...)} / {@code sidedFluidInventory.on(...)}
 * with TFC's defaults. {@code @At("TAIL")} on the constructor runs AFTER that
 * block, so our subsequent {@code .on(...)} calls overwrite TFC's wiring with
 * our (stronger, belt-friendly) configuration. When
 * {@code crucibleEnableAutomation=false} TFC skips its block entirely and our
 * TAIL injection is what populates the handlers.
 *
 * <h2>Capability plumbing is unchanged</h2>
 * TFC registers its own capability callbacks via
 * {@code event.registerBlockEntity(ITEM_HANDLER, TFCBlockEntities.CRUCIBLE.get(), InventoryBlockEntity::getSidedInventory)}
 * ({@code TerraFirmaCraft/.../BlockCapabilities.java:51}). That callback reads
 * {@code sidedInventory.get(side)} on every capability lookup, so mutating the
 * {@code SidedHandler} in-place is enough — no separate capability
 * registration needed.
 */
@Mixin(CrucibleBlockEntity.class)
public abstract class CrucibleBlockEntityMixin
{
    /**
     * Captured once at class-load: {@code InventoryBlockEntity.sidedInventory}
     * is a {@code protected} field inherited by {@code CrucibleBlockEntity},
     * and we cannot reach it through {@code @Shadow} (Mixin does not walk the
     * inheritance chain) nor through the Java compiler's protected-visibility
     * check (the mixin class has no declared {@code extends} in source).
     * Reflection is the only mechanism that works at both compile time and
     * apply time without modifying TFC bytecode via an AccessTransformer.
     */
    private static final Field CRUCIBLE_SIDED_INVENTORY_FIELD;

    static
    {
        try
        {
            CRUCIBLE_SIDED_INVENTORY_FIELD = InventoryBlockEntity.class.getDeclaredField("sidedInventory");
            CRUCIBLE_SIDED_INVENTORY_FIELD.setAccessible(true);
        }
        catch (NoSuchFieldException e)
        {
            throw new IllegalStateException(
                "CrucibleBlockEntityMixin: InventoryBlockEntity.sidedInventory not found — "
                    + "TFC refactor may have renamed the field; update the mixin accordingly.",
                e);
        }
    }

    @Shadow public SidedHandler<IFluidHandler> sidedFluidInventory;

    @Inject(
        method = "<init>(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V",
        at = @At("TAIL")
    )
    private void aeronautics$ensureAutomationSidedAccess(BlockPos pos, BlockState state, CallbackInfo ci)
        throws IllegalAccessException
    {
        CrucibleBlockEntity self = (CrucibleBlockEntity) (Object) this;
        IItemHandlerModifiable inv = self.getInventory();

        // Reach the inherited `sidedInventory` via the captured reflection handle.
        // After this call, both `sidedInventory` and `sidedFluidInventory` carry our
        // desired sided-configuration regardless of TFC's `crucibleEnableAutomation` flag.
        SidedHandler<IItemHandlerModifiable> sidedItem =
            (SidedHandler<IItemHandlerModifiable>) CRUCIBLE_SIDED_INVENTORY_FIELD.get(self);

        sidedItem
            .on(new PartialItemHandler(inv).insert(0, 1, 2, 3, 4, 5, 6, 7, 8), Direction.UP)
            .on(new PartialItemHandler(inv)
                    .insert(0, 1, 2, 3, 4, 5, 6, 7, 8)
                    .extract(CrucibleBlockEntity.SLOT_OUTPUT),
                Direction.Plane.HORIZONTAL);

        this.sidedFluidInventory
            .on(PartialFluidHandler::insertOnly, Direction.UP)
            .on(PartialFluidHandler::extractOnly, Direction.Plane.HORIZONTAL);
    }

    /**
     * Picks up {@link net.minecraft.world.entity.item.ItemEntity} instances sitting on top of the crucible
     * and inserts them into {@code inventory} via
     * {@link Helpers#gatherAndConsumeItems(Level, AABB, net.neoforged.neoforge.items.IItemHandler, int, int)}.
     *
     * <p>Vanilla {@code ItemEntity.tick()} does NOT auto-insert into arbitrary block entities —
     * it only merges with other {@code ItemEntity} instances and decays. TFC's barrel uses the
     * same {@code gatherAndConsumeItems} helper (see {@code BarrelBlockEntity.serverTick:175})
     * to implement "drop items into barrel" behaviour, but TFC's Crucible does not. We add the
     * pickup here so a player Q-dropping raw ore onto a crucible (or an andesite funnel
     * dropping items onto it from above) ends up in slots 0-8.
     *
     * <p>The AABB sits just above the crucible's footprint (y = 1.0 .. 1.5 relative to the BE pos)
     * to catch items at their resting position on the block top.
     *
     * <p>{@code SLOT_INPUT_START=0}, {@code SLOT_INPUT_END=8} — the output slot (9) is a mold
     * slot and items dropped on top must never land there.
     */
    @Inject(
        method = "serverTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/dries007/tfc/common/blockentities/CrucibleBlockEntity;)V",
        at = @At("TAIL")
    )
    private static void aeronautics$pickupDroppedItems(
        Level level, BlockPos pos, BlockState state, CrucibleBlockEntity crucible, CallbackInfo ci
    )
    {
        AABB bounds = new AABB(0.125, 1.0, 0.125, 0.875, 1.5, 0.875).move(pos);
        Helpers.gatherAndConsumeItems(
            level, bounds, crucible.getInventory(),
            CrucibleBlockEntity.SLOT_INPUT_START, CrucibleBlockEntity.SLOT_INPUT_END
        );
    }
}