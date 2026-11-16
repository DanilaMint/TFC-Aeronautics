package ru.tfc_aeronautics.basin;

import java.util.List;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinOperatingBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.SmartInventory;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import net.dries007.tfc.common.recipes.InstantBarrelRecipe;
import net.dries007.tfc.common.recipes.TFCRecipeTypes;
import net.dries007.tfc.common.recipes.ingredients.HeatIngredient;
import net.dries007.tfc.util.Helpers;

/**
 * Makes a Create {@code create:basin} act as a TFC barrel for the {@code tfc:barrel_instant}
 * recipe type: whenever the basin holds an item and a matching fluid, the recipe fires on the
 * next server tick, consuming the item and converting the fluid.
 *
 * <p>This deliberately does <b>not</b> reuse TFC's own machinery:
 * <ul>
 *     <li>{@code InstantBarrelRecipe#matches(BarrelInventory)} is skipped, because it enforces
 *     TFC's {@code moreItemsThanFluid} ratio — that rule only exists so that instant and sealed
 *     barrel recipes can coexist inside one barrel, and it would make a single salt useless in a
 *     full bucket of water.</li>
 *     <li>{@code BarrelRecipe#assembleOutputs(BarrelInventory)} is skipped, because it voids any
 *     leftover input fluid. A basin is an open working container, so surplus fluid stays put.</li>
 * </ul>
 * The recipe <i>data</i> is still TFC's: {@link InstantBarrelRecipe} supplies the ingredient, the
 * fluid ingredient, the output stack provider (with its {@code tfc:empty_bowl} style modifiers)
 * and the completion sound.
 *
 * <p>Recipes whose item ingredient is a {@link HeatIngredient} ({@code tfc:heat}) are skipped:
 * those are the four "cooling" recipes, and letting them run would make any basin holding a
 * trickle of water cool down hot items, which fights Create's own heat handling.
 *
 * <p>Attached to basins through Create's {@code BlockEntityBehaviourEvent}, see
 * {@link BasinBarrelInstantCompat}.
 */
public class BasinBarrelInstantBehaviour extends BlockEntityBehaviour
{
    /**
     * Must be a single static instance: Create keys its behaviour map by reference and
     * {@link BehaviourType} does not override {@code equals}.
     */
    public static final BehaviourType<BasinBarrelInstantBehaviour> TYPE = new BehaviourType<>("tfc_aeronautics:basin_barrel_instant");

    private final BasinBlockEntity basin;

    /**
     * Snapshot of the basin input state (items + tank contents) used to avoid looking recipes up
     * on every tick of an idle basin.
     */
    private ItemStack[] lastItems = new ItemStack[0];
    private FluidStack[] lastFluids = new FluidStack[0];
    private boolean hasSnapshot;

    public BasinBarrelInstantBehaviour(SmartBlockEntity blockEntity)
    {
        super(blockEntity);
        this.basin = (BasinBlockEntity) blockEntity;
    }

    @Override
    public BehaviourType<?> getType()
    {
        return TYPE;
    }

    @Override
    public void tick()
    {
        super.tick();

        final Level level = getWorld();
        if (level == null || level.isClientSide)
        {
            return;
        }

        // A Create operator (mixer, press) sitting above the basin owns the basin's contents.
        // It extracts items from the same input inventory, so both would fight over them.
        if (level.getBlockEntity(basin.getBlockPos().above(2)) instanceof BasinOperatingBlockEntity)
        {
            return;
        }

        final SmartInventory input = basin.getInputInventory();
        if (input.isEmpty())
        {
            hasSnapshot = false;
            return;
        }

        if (hasSnapshot && matchesSnapshot())
        {
            return;
        }

        switch (process(level, input))
        {
            case NO_MATCH -> storeSnapshot();
            // The output did not fit: retry next tick, once the player frees up the output side.
            case BLOCKED -> {}
            // The contents changed, so re-check on the next tick — this is what allows chained
            // recipes (process_olives -> clean_jute_net) to follow each other.
            case EXECUTED -> {}
        }
    }

    private Outcome process(Level level, SmartInventory input)
    {
        final List<RecipeHolder<InstantBarrelRecipe>> recipes = level.getRecipeManager()
            .getAllRecipesFor(TFCRecipeTypes.BARREL_INSTANT.get());

        boolean blocked = false;

        for (int slot = 0; slot < input.getSlots(); slot++)
        {
            final ItemStack stack = input.getStackInSlot(slot);
            if (stack.isEmpty())
            {
                continue;
            }

            for (RecipeHolder<InstantBarrelRecipe> holder : recipes)
            {
                final InstantBarrelRecipe recipe = holder.value();
                if (isCoolingRecipe(recipe))
                {
                    continue;
                }

                final SizedIngredient inputItem = recipe.getInputItem();
                final Ingredient ingredient = inputItem.ingredient();
                if (ingredient.isEmpty() || !ingredient.test(stack))
                {
                    continue;
                }

                final int itemCount = inputItem.count();
                if (stack.getCount() < itemCount)
                {
                    continue;
                }

                final SizedFluidIngredient inputFluid = recipe.getInputFluid();
                final FluidStack fluid = findFluid(inputFluid.ingredient());
                if (fluid.isEmpty())
                {
                    continue;
                }

                final int fluidAmount = inputFluid.amount();
                if (fluid.getAmount() < fluidAmount)
                {
                    continue;
                }

                final int batches = Math.min(fluid.getAmount() / fluidAmount, stack.getCount() / itemCount);
                if (batches <= 0)
                {
                    continue;
                }

                final Outcome outcome = execute(level, recipe, slot, stack, fluid, batches, itemCount, fluidAmount);
                if (outcome == Outcome.EXECUTED)
                {
                    return outcome;
                }
                if (outcome == Outcome.BLOCKED)
                {
                    // The output side was full for this recipe; another one might still fit.
                    blocked = true;
                }
            }
        }

        return blocked ? Outcome.BLOCKED : Outcome.NO_MATCH;
    }

    private Outcome execute(Level level, InstantBarrelRecipe recipe, int slot, ItemStack inputStack, FluidStack fluid, int batches, int itemCount, int fluidAmount)
    {
        // The output stack provider applies TFC's item stack modifiers (tfc:empty_bowl and friends),
        // so it has to see the real input stack - including its components.
        final ItemStack singleItem = recipe.getOutputItem().getSingleStack(inputStack);
        // getOutputFluid() hands out the recipe's own stack instance - mutating it would corrupt the recipe.
        final FluidStack singleFluid = recipe.getOutputFluid().copy();

        if (singleItem.isEmpty() && singleFluid.isEmpty())
        {
            // A recipe without any output would just void the inputs.
            return Outcome.NO_MATCH;
        }

        for (int count = batches; count >= 1; count--)
        {
            List<ItemStack> outputItems = List.of();
            if (!singleItem.isEmpty())
            {
                final ItemStack item = singleItem.copy();
                item.setCount(singleItem.getCount() * count);
                outputItems = List.of(item);
            }

            List<FluidStack> outputFluids = List.of();
            if (!singleFluid.isEmpty())
            {
                outputFluids = List.of(singleFluid.copyWithAmount(singleFluid.getAmount() * count));
            }

            if (!basin.acceptOutputs(outputItems, outputFluids, true))
            {
                // The output side is full (or the basin is facing sideways with nowhere to push
                // to). Try with fewer batches, and give up entirely if even one does not fit.
                continue;
            }

            final SmartInventory input = basin.getInputInventory();
            final ItemStack live = input.getStackInSlot(slot);
            input.setStackInSlot(slot, live.copyWithCount(live.getCount() - (count * itemCount)));

            basin.inputTank.getCapability().drain(fluid.copyWithAmount(count * fluidAmount), FluidAction.EXECUTE);

            basin.acceptOutputs(outputItems, outputFluids, false);
            Helpers.playSound(level, basin.getBlockPos(), recipe.getCompleteSound());

            return Outcome.EXECUTED;
        }

        return Outcome.BLOCKED;
    }

    /**
     * Finds the fluid in the basin's input tank that satisfies the recipe's fluid ingredient.
     * The input tank holds two segments with {@code enforceVariety}, so the wanted fluid is not
     * necessarily in the first one.
     */
    private FluidStack findFluid(FluidIngredient ingredient)
    {
        final IFluidHandler tank = basin.inputTank.getCapability();
        for (int i = 0; i < tank.getTanks(); i++)
        {
            final FluidStack fluid = tank.getFluidInTank(i);
            if (!fluid.isEmpty() && ingredient.test(fluid))
            {
                return fluid;
            }
        }
        return FluidStack.EMPTY;
    }

    /**
     * The four {@code *_cooling} recipes use a {@code tfc:heat} ingredient, i.e. they match any hot
     * item. They are excluded from basins, otherwise a basin with a splash of water would cool down
     * anything hot that ends up in it.
     */
    private static boolean isCoolingRecipe(InstantBarrelRecipe recipe)
    {
        final Ingredient ingredient = recipe.getInputItem().ingredient();
        return ingredient.getCustomIngredient() instanceof HeatIngredient;
    }

    private boolean matchesSnapshot()
    {
        final SmartInventory input = basin.getInputInventory();
        for (int slot = 0; slot < input.getSlots(); slot++)
        {
            final ItemStack now = input.getStackInSlot(slot);
            final ItemStack then = slot < lastItems.length ? lastItems[slot] : ItemStack.EMPTY;
            if (now.isEmpty() != then.isEmpty())
            {
                return false;
            }
            if (!now.isEmpty() && (now.getItem() != then.getItem() || now.getCount() != then.getCount()))
            {
                return false;
            }
        }

        final IFluidHandler tank = basin.inputTank.getCapability();
        for (int i = 0; i < tank.getTanks(); i++)
        {
            final FluidStack now = tank.getFluidInTank(i);
            final FluidStack then = i < lastFluids.length ? lastFluids[i] : FluidStack.EMPTY;
            if (now.isEmpty() != then.isEmpty())
            {
                return false;
            }
            if (!now.isEmpty() && (now.getFluid() != then.getFluid() || now.getAmount() != then.getAmount()))
            {
                return false;
            }
        }

        return true;
    }

    private void storeSnapshot()
    {
        final SmartInventory input = basin.getInputInventory();
        if (lastItems.length != input.getSlots())
        {
            lastItems = new ItemStack[input.getSlots()];
        }
        for (int slot = 0; slot < input.getSlots(); slot++)
        {
            final ItemStack stack = input.getStackInSlot(slot);
            lastItems[slot] = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        }

        final IFluidHandler tank = basin.inputTank.getCapability();
        if (lastFluids.length != tank.getTanks())
        {
            lastFluids = new FluidStack[tank.getTanks()];
        }
        for (int i = 0; i < tank.getTanks(); i++)
        {
            final FluidStack fluid = tank.getFluidInTank(i);
            lastFluids[i] = fluid.isEmpty() ? FluidStack.EMPTY : fluid.copy();
        }

        hasSnapshot = true;
    }

    private enum Outcome
    {
        /** Nothing matched the current contents. */
        NO_MATCH,
        /** Something matched, but the output side had no room for the result. */
        BLOCKED,
        /** A recipe ran. */
        EXECUTED
    }
}
