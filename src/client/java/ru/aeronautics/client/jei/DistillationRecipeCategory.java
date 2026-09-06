package ru.aeronautics.client.jei;

import java.util.List;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import ru.tfc_aeronautics.recipe.DistillationRecipe;

/**
 * JEI category for {@code tfc_aeronautics:distillation} recipes. Three slots:
 * input fluid on the left, {@code result} fluid on the right, {@code residue}
 * fluid further right. The recipe's closed temperature interval is shown as a
 * text widget under the slots via {@link IRecipeExtrasBuilder#addText}.
 *
 * <p>The category itself is purely a presentation layer — the actual matching
 * logic lives in {@link DistillationRecipe#matches(FluidStack)} and is invoked
 * by the {@code condenser_coil} block entity at runtime.
 */
public final class DistillationRecipeCategory extends AbstractRecipeCategory<RecipeHolder<DistillationRecipe>>
{
    private static final int WIDTH = 130;
    private static final int HEIGHT = 60;

    private static final int INPUT_X = 10;
    private static final int RESULT_X = 78;
    private static final int RESIDUE_X = 110;
    private static final int SLOTS_Y = 22;

    private static final int TEMP_TEXT_X = 10;
    private static final int TEMP_TEXT_Y = 44;

    public DistillationRecipeCategory(RecipeType<RecipeHolder<DistillationRecipe>> type, IGuiHelper helper)
    {
        super(
            type,
            Component.translatable("jei.tfc_aeronautics.distillation"),
            helper.createDrawableItemStack(new net.minecraft.world.item.ItemStack(
                ru.tfc_aeronautics.condenser_coil.CondenserCoilRegistration.CONDENSER_COIL_ITEM.get())),
            WIDTH,
            HEIGHT);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<DistillationRecipe> holder, IFocusGroup focuses)
    {
        DistillationRecipe recipe = holder.value();

        IRecipeSlotBuilder input = builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X, SLOTS_Y);
        List<Fluid> inputFluids = resolveInputFluids(recipe);
        if (inputFluids.isEmpty())
        {
            input.addIngredients(NeoForgeTypes.FLUID_STACK, List.of());
        }
        else if (inputFluids.size() == 1)
        {
            input.addIngredient(NeoForgeTypes.FLUID_STACK, new FluidStack(inputFluids.get(0), 1000));
        }
        else
        {
            List<FluidStack> stacks = inputFluids.stream().map(f -> new FluidStack(f, 1000)).toList();
            input.addIngredients(NeoForgeTypes.FLUID_STACK, stacks);
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, RESULT_X, SLOTS_Y)
            .addIngredient(NeoForgeTypes.FLUID_STACK, new FluidStack(recipe.result(), 1000));

        builder.addSlot(RecipeIngredientRole.OUTPUT, RESIDUE_X, SLOTS_Y)
            .addIngredient(NeoForgeTypes.FLUID_STACK, new FluidStack(recipe.residue(), 1000));
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<DistillationRecipe> holder, IFocusGroup focuses)
    {
        DistillationRecipe recipe = holder.value();
        Component text = Component.translatable(
            "jei.tfc_aeronautics.distillation.temp",
            recipe.minTemperature(),
            recipe.maxTemperature());
        builder.addText(text, TEMP_TEXT_X, TEMP_TEXT_Y);
    }

    /**
     * Returns the fluids that satisfy the recipe's input selector.
     *
     * <p>If the selector is an id branch, the list contains exactly that fluid.
     * If it is a tag branch, the list contains every fluid registered into the
     * tag at the time the category renders (JEI re-invokes {@link #setRecipe}
     * on relevant recipe manager reloads).
     *
     * <p>When the client level is not yet available, the tag branch resolves to
     * an empty list — JEI will simply show the input slot empty, which is
     * visually distinguishable from the {@code result} and {@code residue} slots.
     */
    private static List<Fluid> resolveInputFluids(DistillationRecipe recipe)
    {
        return recipe.input().map(
            id -> List.of(net.minecraft.core.registries.BuiltInRegistries.FLUID.get(id)),
            tag -> {
                if (Minecraft.getInstance().level == null)
                {
                    return List.of();
                }
                Registry<Fluid> registry = Minecraft.getInstance().level.registryAccess()
                    .registryOrThrow(Registries.FLUID);
                return registry.getTag(tag)
                    .map(holders -> holders.stream().map(Holder::value).toList())
                    .orElse(List.of());
            }
        );
    }
}
