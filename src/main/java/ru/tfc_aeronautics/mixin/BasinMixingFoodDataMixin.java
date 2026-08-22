package ru.tfc_aeronautics.mixin;

import java.util.Optional;
import java.util.Set;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinOperatingBlockEntity;
import com.simibubi.create.foundation.item.SmartInventory;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import net.dries007.tfc.common.component.food.FoodCapability;
import net.dries007.tfc.common.component.food.FoodComponent;
import net.dries007.tfc.common.component.food.FoodDefinition;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Synchronizes TFC food data (rot timer, creationDate, traits) from the
 * captured flour at the start of basin mixing to the produced dough at the
 * end of basin mixing. Without this, dough produced via Create's basin
 * would have no behaviour parity with TFC's
 * {@code tfc:advanced_shapeless_crafting} recipe — TFC's recipe applies
 * {@code tfc:copy_oldest_food} via {@code ItemStackProvider}; Create's
 * {@code create:mixing} has no equivalent modifier chain, so this mixin
 * fills the gap by calling {@link FoodCapability#updateFoodFromPrevious}
 * directly.
 *
 * <p>{@code TFC_DOUGHS} is the explicit set of TFC dough items (six
 * grain variants). If TFC adds new doughs in a future version, extend
 * this set. Heuristic on the flour side because grain flour lives under
 * {@code tfc:food/<grain>_flour} and the basin's input may contain
 * other items.
 *
 * <p>{@code aerodynamic$capturedFlour} is a {@code .copy()} snapshot
 * because the basin's {@code applyBasinRecipe → BasinRecipe.apply}
 * pipeline mutates the input slots in place; without the snapshot the
 * referenced stack would already have {@code count == 0} by the time
 * the TAIL inject runs (same trap as
 * {@code MillstoneBlockEntityMixin.aeronautics$capturedInput}).
 */
@Mixin(BasinOperatingBlockEntity.class)
public abstract class BasinMixingFoodDataMixin
{
    @Unique
    private static final Set<ResourceLocation> TFC_DOUGHS = Set.of(
        ResourceLocation.fromNamespaceAndPath("tfc", "food/barley_dough"),
        ResourceLocation.fromNamespaceAndPath("tfc", "food/maize_dough"),
        ResourceLocation.fromNamespaceAndPath("tfc", "food/oat_dough"),
        ResourceLocation.fromNamespaceAndPath("tfc", "food/rye_dough"),
        ResourceLocation.fromNamespaceAndPath("tfc", "food/rice_dough"),
        ResourceLocation.fromNamespaceAndPath("tfc", "food/wheat_dough")
    );

    @Unique
    private ItemStack aeronautics$capturedFlour = ItemStack.EMPTY;

    @Shadow
    protected abstract Optional<BasinBlockEntity> getBasin();

    @Inject(method = "applyBasinRecipe", at = @At("HEAD"))
    private void aeronautics$captureFlour(CallbackInfo ci)
    {
        aeronautics$capturedFlour = ItemStack.EMPTY;
        Optional<BasinBlockEntity> basin = getBasin();
        if (basin.isEmpty())
        {
            return;
        }
        SmartInventory inputInventory = basin.get().getInputInventory();
        for (int i = 0; i < inputInventory.getSlots(); i++)
        {
            ItemStack stack = inputInventory.getStackInSlot(i);
            if (isTFCFlour(stack))
            {
                aeronautics$capturedFlour = stack.copy();
                return;
            }
        }
    }

    @Inject(method = "applyBasinRecipe", at = @At("TAIL"))
    private void aeronautics$applyFoodData(CallbackInfo ci)
    {
        ItemStack captured = aeronautics$capturedFlour;
        aeronautics$capturedFlour = ItemStack.EMPTY;
        if (captured.isEmpty())
        {
            return;
        }

        Optional<BasinBlockEntity> basin = getBasin();
        if (basin.isEmpty())
        {
            return;
        }
        SmartInventory outputInventory = basin.get().getOutputInventory();
        for (int i = 0; i < outputInventory.getSlots(); i++)
        {
            ItemStack output = outputInventory.getStackInSlot(i);
            if (isTFCDough(output))
            {
                if (output.get(net.dries007.tfc.common.component.TFCComponents.FOOD) == null)
                {
                    // Fallback: by rights TFC's ItemStackHooks.onModifyItemStackComponents
                    // attaches a FoodComponent when the dough ItemStack is created.
                    // If that hook didn't fire for some reason (e.g. cached recipe
                    // result reused), attach a fresh FoodComponent manually so
                    // updateFoodFromPrevious has a target to write to.
                    FoodDefinition def = FoodCapability.getDefinition(output);
                    if (def != null)
                    {
                        output.set(net.dries007.tfc.common.component.TFCComponents.FOOD, new FoodComponent(def));
                    }
                }
                FoodCapability.updateFoodFromPrevious(captured, output);
            }
        }
    }

    @Unique
    private static boolean isTFCFlour(ItemStack stack)
    {
        if (stack.isEmpty())
        {
            return false;
        }
        ResourceLocation id = stack.getItem().builtInRegistryHolder().key().location();
        if (!"tfc".equals(id.getNamespace()))
        {
            return false;
        }
        String path = id.getPath();
        return path.startsWith("food/") && path.endsWith("_flour");
    }

    @Unique
    private static boolean isTFCDough(ItemStack stack)
    {
        if (stack.isEmpty())
        {
            return false;
        }
        return TFC_DOUGHS.contains(stack.getItem().builtInRegistryHolder().key().location());
    }
}
