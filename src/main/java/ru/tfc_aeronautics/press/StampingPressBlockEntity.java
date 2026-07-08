package ru.tfc_aeronautics.press;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.press.PressingBehaviour;
import com.simibubi.create.content.kinetics.press.PressingBehaviour.PressingBehaviourSpecifics;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;

import net.createmod.catnip.math.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.dries007.tfc.common.component.heat.HeatCapability;
import net.dries007.tfc.common.component.heat.IHeat;
import net.dries007.tfc.common.component.heat.IHeatView;
import net.dries007.tfc.common.recipes.AnvilRecipe;

public class StampingPressBlockEntity extends KineticBlockEntity implements PressingBehaviourSpecifics {

    public PressingBehaviour pressingBehaviour;
    public FilteringBehaviour filtering;

    public StampingPressBlockEntity(BlockPos pos, BlockState state) {
        super(StampingPressRegistration.STAMPING_PRESS_BE.get(), pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        pressingBehaviour = new PressingBehaviour(this);
        behaviours.add(pressingBehaviour);
        filtering = new FilteringBehaviour(this, new StampingPressFilterSlot()).forRecipes();
        behaviours.add(filtering);
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return new AABB(worldPosition).expandTowards(0, -1.5, 0).expandTowards(0, 1, 0);
    }

    @Override
    public boolean tryProcessInBasin(boolean simulate) {
        return false;
    }

    @Override
    public boolean tryProcessInWorld(ItemEntity itemEntity, boolean simulate) {
        ItemStack input = itemEntity.getItem();
        if (input.isEmpty())
            return false;
        ItemStack filter = filtering.getFilter();
        if (filter.isEmpty())
            return false;

        Optional<RecipeHolder<AnvilRecipe>> recipe = findRecipe(input, filter);
        if (recipe.isEmpty())
            return false;
        if (simulate)
            return true;

        ItemStack singleInput = input.copyWithCount(1);
        ItemStack result = assembleResult(recipe.get().value(), singleInput);
        if (result.isEmpty())
            return false;
        propagateHeat(singleInput, result);

        pressingBehaviour.particleItems.add(singleInput.copy());
        input.shrink(1);
        if (itemEntity.getItem().isEmpty())
            itemEntity.discard();

        ItemEntity created = new ItemEntity(level, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), result);
        created.setDefaultPickUpDelay();
        created.setDeltaMovement(VecHelper.offsetRandomly(Vec3.ZERO, level.random, .05f));
        level.addFreshEntity(created);
        spawnStrikeParticles(itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), result);
        return true;
    }

    @Override
    public boolean tryProcessOnBelt(TransportedItemStack input, List<ItemStack> outputList, boolean simulate) {
        ItemStack stack = input.stack;
        if (stack.isEmpty())
            return false;
        ItemStack filter = filtering.getFilter();
        if (filter.isEmpty())
            return false;

        Optional<RecipeHolder<AnvilRecipe>> recipe = findRecipe(stack, filter);
        if (recipe.isEmpty())
            return false;
        if (simulate)
            return true;

        ItemStack singleInput = stack.copyWithCount(1);
        ItemStack result = assembleResult(recipe.get().value(), singleInput);
        if (result.isEmpty())
            return false;
        propagateHeat(singleInput, result);

        pressingBehaviour.particleItems.add(singleInput.copy());
        outputList.add(result);
        return true;
    }

    @Override
    public void onPressingCompleted() {}

    @Override
    public boolean canProcessInBulk() {
        return false;
    }

    @Override
    public int getParticleAmount() {
        return 15;
    }

    @Override
    public float getKineticSpeed() {
        return getSpeed();
    }

    public PressingBehaviour getPressingBehaviour() {
        return pressingBehaviour;
    }

    @Nullable
    private Optional<RecipeHolder<AnvilRecipe>> findRecipe(ItemStack input, ItemStack filter) {
        IHeatView heat = HeatCapability.view(input);
        if (heat == null || !heat.canWork())
            return Optional.empty();
        if (level == null)
            return Optional.empty();

        List<RecipeHolder<AnvilRecipe>> candidates = AnvilRecipe.getAll(level, input, Integer.MAX_VALUE);
        if (candidates.isEmpty())
            return Optional.empty();

        Item filterItem = filter.getItem();
        StampingInventory inv = new StampingInventory(input.copyWithCount(1));
        HolderLookup.Provider registries = level.registryAccess();

        for (RecipeHolder<AnvilRecipe> holder : candidates) {
            AnvilRecipe recipe = holder.value();
            if (!recipe.matches(inv, level))
                continue;
            ItemStack result = recipe.assemble(inv, registries);
            if (!result.isEmpty() && result.getItem() == filterItem)
                return Optional.of(holder);
        }
        return Optional.empty();
    }

    private ItemStack assembleResult(AnvilRecipe recipe, ItemStack input) {
        HolderLookup.Provider registries = level.registryAccess();
        return recipe.assemble(new StampingInventory(input), registries);
    }

    private static void propagateHeat(ItemStack input, ItemStack output) {
        IHeatView inputHeat = HeatCapability.view(input);
        if (inputHeat == null)
            return;
        IHeat outputHeat = HeatCapability.get(output);
        if (outputHeat != null)
            outputHeat.setTemperature(inputHeat.getTemperature());
    }

    private void spawnStrikeParticles(double x, double y, double z, ItemStack stack) {
        if (level == null || level.isClientSide)
            return;
        for (int i = 0; i < 4; i++) {
            Vec3 motion = VecHelper.offsetRandomly(Vec3.ZERO, level.random, .125f).multiply(1, 0, 1);
            level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack),
                x, y - .25, z, motion.x, motion.y + .125, motion.z);
        }
    }

    private record StampingInventory(ItemStack item) implements AnvilRecipe.Inventory {
        @Override public ItemStack getItem() { return item; }
        @Override public int getTier() { return Integer.MAX_VALUE; }
        @Override public long getSeed() { return 0L; }

        // RecipeInput boilerplate
        @Override public ItemStack getItem(int slot) { return slot == 0 ? item : ItemStack.EMPTY; }
        @Override public int size() { return 1; }
        @Override public boolean isEmpty() { return item.isEmpty(); }
    }
}
