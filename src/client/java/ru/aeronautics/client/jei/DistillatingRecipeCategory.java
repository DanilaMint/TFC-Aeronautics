package ru.aeronautics.client.jei;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import ru.tfc_aeronautics.condenser_coil.CondenserCoilRegistration;
import ru.tfc_aeronautics.recipe.DistillatingRecipe;

/**
 * JEI category for {@code tfc_aeronautics:distillating} recipes consumed by
 * the {@code condenser_coil} block. Layout (130 x 88):
 *
 * <pre>
 *   [input 100mB]   [bubbles]   [output XmB]
 *           [X..Y°C]
 *               V
 *           [residue YmB]
 * </pre>
 *
 * <p>Each fluid slot uses {@code setFluidRenderer(amount, true, 16, 16)} with
 * {@code isTip = true} so the displayed tooltip is just "{@code X mB}" (no
 * "{@code X/Y mB}" suffix that JEI adds in proportional-fill mode). All fluid
 * slots use JEI's standard slot background drawn via {@code setBackground}.
 *
 * <p>The bubbles are the "empty" (unfilled) brewing-stand bubbles, blitted out
 * of the vanilla GUI background art. Note that in 1.21.1 the brewing widgets
 * were split: the animated *filled* bubbles moved to a standalone sprite under
 * {@code gui/sprites/container/brewing_stand/}, while the hollow outlines
 * stayed in {@code brewing_stand.png} at the position the filled ones get
 * drawn over.
 */
public final class DistillatingRecipeCategory extends AbstractRecipeCategory<RecipeHolder<DistillatingRecipe>>
{
    private static final int WIDTH = 89;
    private static final int HEIGHT = 91;

    private static final int INPUT_X = 6;
    private static final int OUTPUT_X = 65;
    private static final int RESIDUE_X = 36;
    private static final int SLOTS_Y = 6;

    private static final int BUBBLES_X = 29;
    private static final int BUBBLES_Y = 8;

    private static final int TEMP_FRAME_X = 9;
    private static final int TEMP_FRAME_Y = 30;
    private static final int TEMP_FRAME_W = 65;
    private static final int TEMP_FRAME_H = 12;

    private static final int ARROW_X = 36;
    private static final int ARROW_Y = 43;

    private static final int RESIDUE_Y = 67;

    private static final int INPUT_MB = 100;

    // Vanilla brewing-stand bubbles, "empty" (unfilled) variant. These live in
    // the GUI *background* art at u=63, v=14, 12x29 — the exact spot vanilla
    // later blits the filled bubbles over. The standalone sprite at
    // gui/sprites/container/brewing_stand/bubbles.png is the *filled* variant
    // (solid white discs), which is not what this layout wants.
    private static final ResourceLocation BREWING_BG =
        ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/brewing_stand.png");
    private static final int BUBBLES_U = 63;
    private static final int BUBBLES_V = 14;
    private static final int BUBBLES_W = 12;
    private static final int BUBBLES_H = 29;

    // TFC's JEI icons texture (uses helper.createDrawable reliably, unlike vanilla/Create).
    private static final ResourceLocation TFC_ICONS =
        ResourceLocation.fromNamespaceAndPath("tfc", "textures/gui/jei/icons.png");
    // The icons sheet only has a right-pointing arrow (22x16 at u=0, v=14); we
    // rotate it 90 deg to get the down arrow the layout calls for.
    private static final int ARROW_W = 22;
    private static final int ARROW_H = 16;

    private final IDrawable slotBackground;
    private final IDrawable downArrow;

    public DistillatingRecipeCategory(RecipeType<RecipeHolder<DistillatingRecipe>> type, IGuiHelper helper)
    {
        super(
            type,
            Component.translatable("jei.tfc_aeronautics.distillating"),
            helper.createDrawableItemStack(new ItemStack(CondenserCoilRegistration.CONDENSER_COIL_ITEM.get())),
            WIDTH,
            HEIGHT);
        // Standard JEI slot background — same as TFC's BarrelRecipeCategory
        // (uses helper.getSlotDrawable()).
        this.slotBackground = helper.getSlotDrawable();
        // Down arrow from TFC's JEI icons (u=0, v=14, 22x16 in
        // tfc:textures/gui/jei/icons.png). The sheet only has the right-pointing
        // variant, so draw() rotates it 90 deg.
        this.downArrow = helper.createDrawable(TFC_ICONS, 0, 14, ARROW_W, ARROW_H);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<DistillatingRecipe> holder, IFocusGroup focuses)
    {
        DistillatingRecipe recipe = holder.value();
        int resultPercent = Math.max(0, Math.min(100, recipe.result_percent()));
        int residuePercent = 100 - resultPercent;

        IRecipeSlotBuilder input = builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X, SLOTS_Y)
            .setBackground(slotBackground, -1, -1);
        List<Fluid> inputFluids = resolveInputFluids(recipe);
        if (inputFluids.isEmpty())
        {
            input.addIngredients(NeoForgeTypes.FLUID_STACK, List.of());
        }
        else if (inputFluids.size() == 1)
        {
            input.addIngredient(NeoForgeTypes.FLUID_STACK, new FluidStack(inputFluids.get(0), INPUT_MB))
                .setFluidRenderer(INPUT_MB, true, 16, 16);
        }
        else
        {
            List<FluidStack> stacks = inputFluids.stream()
                .map(f -> new FluidStack(f, INPUT_MB))
                .toList();
            input.addIngredients(NeoForgeTypes.FLUID_STACK, stacks)
                .setFluidRenderer(INPUT_MB, true, 16, 16);
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, SLOTS_Y)
            .setBackground(slotBackground, -1, -1)
            .addIngredient(NeoForgeTypes.FLUID_STACK, new FluidStack(recipe.result(), resultPercent))
            .setFluidRenderer(resultPercent, true, 16, 16);

        builder.addSlot(RecipeIngredientRole.OUTPUT, RESIDUE_X, RESIDUE_Y)
            .setBackground(slotBackground, -1, -1)
            .addIngredient(NeoForgeTypes.FLUID_STACK, new FluidStack(recipe.residue(), residuePercent))
            .setFluidRenderer(residuePercent, true, 16, 16);
    }

    @Override
    public void draw(RecipeHolder<DistillatingRecipe> holder, IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics, double mouseX, double mouseY)
    {
        // Bubbles, rotated 90 deg so the column flows left-to-right (input ->
        // output). +90 about Z maps source +x to screen +y and source -y (the
        // direction the bubbles rise) to screen +x.
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(BUBBLES_X + (double) BUBBLES_H / 2.0, BUBBLES_Y + (double) BUBBLES_W / 2.0, 0.0);
        pose.mulPose(Axis.ZP.rotationDegrees(90.0f));
        pose.translate(-(double) BUBBLES_W / 2.0, -(double) BUBBLES_H / 2.0, 0.0);
        guiGraphics.blit(BREWING_BG, 0, 0, BUBBLES_U, BUBBLES_V, BUBBLES_W, BUBBLES_H);
        pose.popPose();

        // Temperature frame (65 x 9) — outlined box around the temperature label.
        final int frameColor = 0xFF606060;
        guiGraphics.fill(TEMP_FRAME_X, TEMP_FRAME_Y,
            TEMP_FRAME_X + TEMP_FRAME_W, TEMP_FRAME_Y + 1, frameColor);
        guiGraphics.fill(TEMP_FRAME_X, TEMP_FRAME_Y + TEMP_FRAME_H - 1,
            TEMP_FRAME_X + TEMP_FRAME_W, TEMP_FRAME_Y + TEMP_FRAME_H, frameColor);
        guiGraphics.fill(TEMP_FRAME_X, TEMP_FRAME_Y,
            TEMP_FRAME_X + 1, TEMP_FRAME_Y + TEMP_FRAME_H, frameColor);
        guiGraphics.fill(TEMP_FRAME_X + TEMP_FRAME_W - 1, TEMP_FRAME_Y,
            TEMP_FRAME_X + TEMP_FRAME_W, TEMP_FRAME_Y + TEMP_FRAME_H, frameColor);

        // Temperature label, red, centered inside the frame.
        DistillatingRecipe recipe = holder.value();
        Component tempText = Component.literal(recipe.minTemperature() + ".." + recipe.maxTemperature() + "°C");
        Font font = Minecraft.getInstance().font;
        int textWidth = font.width(tempText);
        int textX = TEMP_FRAME_X + (TEMP_FRAME_W - textWidth) / 2;
        int textY = TEMP_FRAME_Y + (TEMP_FRAME_H - font.lineHeight) / 2 + 1;
        guiGraphics.drawString(font, tempText, textX, textY, 0xFFFFFFFF, true);

        // Down arrow above the residue slot: TFC's right-pointing arrow rotated
        // 90 deg about Z (source +x -> screen +y). Occupies ARROW_H x ARROW_W
        // (16 x 22) on screen, centred on the residue column.
        pose.pushPose();
        pose.translate(ARROW_X + (double) ARROW_H / 2.0, ARROW_Y + (double) ARROW_W / 2.0, 0.0);
        pose.mulPose(Axis.ZP.rotationDegrees(90.0f));
        pose.translate(-(double) ARROW_W / 2.0, -(double) ARROW_H / 2.0, 0.0);
        downArrow.draw(guiGraphics, 0, 0);
        pose.popPose();
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
    private static List<Fluid> resolveInputFluids(DistillatingRecipe recipe)
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
