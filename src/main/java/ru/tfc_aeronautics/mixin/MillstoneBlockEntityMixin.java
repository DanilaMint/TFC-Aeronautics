package ru.tfc_aeronautics.mixin;

import java.util.List;
import java.util.Optional;

import com.simibubi.create.content.kinetics.millstone.MillingRecipe;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;

import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.kinetics.millstone.MillstoneBlockEntity;

import ru.tfc_aeronautics.recipe.QuernMillingRecipe;
import ru.tfc_aeronautics.recipe.QuernMillingRecipeType;

/**
 * Extends Create's millstone to process TFC-shaped quern recipes through the
 * {@code tfc_aeronautics:quern_milling} recipe type. The millstone itself stays
 * pointed at {@code create:milling} — we only intercept the four hot spots
 * (input capture, recipe lookup, result application, item-validity check) so
 * the original behavior continues to work for ordinary {@code create:milling}
 * recipes.
 *
 * <p>{@code sendData()} lives four levels up the inheritance chain
 * ({@code SyncedBlockEntity ← CachedRenderBBBlockEntity ← SmartBlockEntity ← KineticBlockEntity}),
 * so SpongeMixin's {@code @Shadow} cannot resolve it on the target class.
 * We reach it through the {@code MillstoneBlockEntity} cast instead.
 */
@Mixin(MillstoneBlockEntity.class)
public abstract class MillstoneBlockEntityMixin
{
    @Shadow public ItemStackHandler inputInv;

    @Shadow private MillingRecipe lastRecipe;

    @Shadow public int timer;

    /**
     * Pre-shrink input snapshot. Create's {@code MillstoneBlockEntity.process()}
     * shrinks the input and writes the slot BEFORE invoking
     * {@code lastRecipe.rollResults(...)}, so by the time
     * {@link #aeronautics$rollResults} fires the input slot is already empty
     * for single-item recipes. Capturing at HEAD of {@code process()} gives
     * {@code QuernMillingRecipe#getResult()} a real source stack for
     * {@code CopyFoodModifier} to read expiry/creationDate from.
     * <p>
     * Must be a {@code .copy()} snapshot, not a raw reference: the captured
     * value would otherwise be the same {@link ItemStack} object that
     * {@code process()} subsequently mutates via {@code shrink(1)}, leaving
     * the captured copy with {@code count == 0}. {@code ItemStack.copy()}
     * then returns {@link ItemStack#EMPTY} for a zero-count stack, dropping
     * the FOOD component that {@code CopyFoodModifier} needs.
     */
    @Unique
    private ItemStack aeronautics$capturedInput = ItemStack.EMPTY;

    /**
     * Input capture: snapshot the input slot before Create shrinks it.
     */
    @Inject(method = "process", at = @At("HEAD"))
    private void aeronautics$captureInput(CallbackInfo ci)
    {
        ItemStack inSlot = inputInv.getStackInSlot(0);
        aeronautics$capturedInput = inSlot.copy();
    }

    /**
     * Tick tail: if the original {@code tick()} couldn't find a recipe via
     * {@code create:milling}, try our custom type. Only fires when the millstone
     * is spinning, the input is non-empty, no recipe had been cached yet, and
     * the original logic entered the "no recipe" state (timer == 100).
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void aeronautics$tickTryQuernType(CallbackInfo ci)
    {
        if (lastRecipe != null)
        {
            return;
        }
        if (inputInv.getStackInSlot(0).isEmpty())
        {
            return;
        }
        if (timer != 100)
        {
            return;
        }

        MillstoneBlockEntity self = (MillstoneBlockEntity) (Object) this;
        RecipeInput inventoryIn = new RecipeWrapper(inputInv);
        Optional<RecipeHolder<QuernMillingRecipe>> match = self.getLevel()
            .getRecipeManager()
            .getRecipeFor(QuernMillingRecipeType.TYPE.get(), inventoryIn, self.getLevel());

        if (match.isEmpty())
        {
            return;
        }

        lastRecipe = match.get().value();
        timer = lastRecipe.getProcessingDuration();
        self.sendData();
    }

    /**
     * Result application: when the cached recipe is one of ours, route through
     * {@link QuernMillingRecipe#getResult()} so {@code tfc:copy_food} and
     * friends actually run. The captured input from
     * {@link #aeronautics$captureInput} is used instead of
     * {@code inputInv.getStackInSlot(0)} because Create shrinks the input slot
     * before invoking {@code rollResults} — the slot is already
     * {@link ItemStack#EMPTY} for single-item grains, which would silently
     * skip {@code CopyFoodModifier}.
     * {@link com.dries007.tfc.common.recipes.outputs.ItemStackProvider#getSingleStack}
     * resets the count to 1 so output count is deterministic.
     */
    @Redirect(
        method = "process",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/kinetics/millstone/MillingRecipe;rollResults(Lnet/minecraft/util/RandomSource;)Ljava/util/List;"
        )
    )
    private List<ItemStack> aeronautics$rollResults(MillingRecipe recipe, RandomSource random)
    {
        if (recipe instanceof QuernMillingRecipe quern)
        {
            ItemStack input = aeronautics$capturedInput;
            aeronautics$capturedInput = ItemStack.EMPTY;
            return List.of(quern.getResult().getSingleStack(input));
        }
        return recipe.rollResults(random);
    }

    /**
     * Item-validity tail: if the original {@code canProcess} rejected the
     * stack, see if a {@code tfc_aeronautics:quern_milling} recipe would match.
     * When found, cache it as {@code lastRecipe} so the next {@code tick()}
     * starts the processing timer without an extra lookup.
     */
    @Inject(method = "canProcess", at = @At("TAIL"), cancellable = true)
    private void aeronautics$canProcessQuernType(ItemStack stack, CallbackInfoReturnable<Boolean> cir)
    {
        if (cir.getReturnValue())
        {
            return;
        }

        MillstoneBlockEntity self = (MillstoneBlockEntity) (Object) this;
        ItemStackHandler tester = new ItemStackHandler(1);
        tester.setStackInSlot(0, stack);
        RecipeInput inventoryIn = new RecipeWrapper(tester);

        Optional<RecipeHolder<QuernMillingRecipe>> match = self.getLevel()
            .getRecipeManager()
            .getRecipeFor(QuernMillingRecipeType.TYPE.get(), inventoryIn, self.getLevel());

        if (match.isEmpty())
        {
            return;
        }

        lastRecipe = match.get().value();
        cir.setReturnValue(true);
    }
}
