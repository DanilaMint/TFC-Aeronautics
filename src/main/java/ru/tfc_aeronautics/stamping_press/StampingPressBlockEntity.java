package ru.tfc_aeronautics.stamping_press;

import java.util.List;
import java.util.Optional;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.press.PressingBehaviour;
import com.simibubi.create.content.kinetics.press.PressingBehaviour.PressingBehaviourSpecifics;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import net.dries007.tfc.common.component.heat.HeatCapability;
import net.dries007.tfc.common.component.heat.IHeat;
import net.dries007.tfc.common.recipes.AnvilRecipe;

/**
 * Brain of the {@link StampingPressBlock}. Wires a stock {@link PressingBehaviour}
 * for cycle / animation / belt / particle handling, and adds a
 * {@link FilteringBehaviour} that drives the recipe filter.
 *
 * <p>Recipe resolution: one press cycle = one full anvil recipe. We do not use
 * {@code workRemotely} or any {@code ForgingComponent} state — the press fully
 * transforms the input in a single hit. A recipe matches when (a) the input is
 * a valid ingredient of some {@code tfc:anvil} recipe, (b) the input is hot
 * enough to forge ({@link IHeat#canWork()}), and (c) the recipe's output passes
 * the installed filter. With no filter installed, no recipe matches — the item
 * passes under the press untouched.
 *
 * <p>The filter behaviour is shared between both perpendicular sides: a single
 * installed filter is visible and accessible from either side, and the slot
 * frame is drawn on both faces by the client tick handler.
 */
public class StampingPressBlockEntity extends KineticBlockEntity implements PressingBehaviourSpecifics {

    /**
     * Anvil tier the press counts as. TFC gates recipes behind the anvil's metal
     * tier ({@code copper = 1} … {@code red steel = 6}); the press is not made of
     * a TFC metal, so it satisfies every tier.
     */
    private static final int MAX_TIER = Integer.MAX_VALUE;

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

        filtering = new FilteringBehaviour(this, new StampingPressFilterSlot())
            .forRecipes()
            .withCallback(stack -> setChanged());
        behaviours.add(filtering);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level.isClientSide()) {
            StampingPressInstances.add(this);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        StampingPressInstances.remove(this);
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return new AABB(worldPosition).expandTowards(0, -1.5, 0)
            .expandTowards(0, 2, 0);
    }

    // ----- PressingBehaviourSpecifics -----

    @Override
    public boolean tryProcessInBasin(boolean simulate) {
        return false;
    }

    @Override
    public boolean tryProcessOnBelt(TransportedItemStack input, List<ItemStack> outputList, boolean simulate) {
        Optional<AnvilRecipe> recipe = findMatchingRecipe(input.stack);
        if (recipe.isEmpty()) return false;
        if (simulate) return true;

        pressingBehaviour.particleItems.add(input.stack);
        ItemStack output = assemble(recipe.get(), input.stack);
        if (!output.isEmpty()) outputList.add(output);
        return true;
    }

    @Override
    public boolean tryProcessInWorld(ItemEntity itemEntity, boolean simulate) {
        ItemStack input = itemEntity.getItem();
        Optional<AnvilRecipe> recipe = findMatchingRecipe(input);
        if (recipe.isEmpty()) return false;
        if (simulate) return true;

        pressingBehaviour.particleItems.add(input);
        ItemStack output = assemble(recipe.get(), input);

        input.shrink(1);
        if (input.getCount() <= 0) itemEntity.discard();

        if (!output.isEmpty()) {
            ItemEntity out = new ItemEntity(level, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), output);
            out.setDefaultPickUpDelay();
            level.addFreshEntity(out);
        }
        return true;
    }

    @Override
    public boolean canProcessInBulk() {
        return false;
    }

    @Override
    public void onPressingCompleted() {
    }

    @Override
    public int getParticleAmount() {
        return 15;
    }

    @Override
    public float getKineticSpeed() {
        return getSpeed();
    }

    // ----- Recipe lookup -----

    private Optional<AnvilRecipe> findMatchingRecipe(ItemStack input) {
        if (filtering.getFilter().isEmpty()) return Optional.empty();
        if (!HeatCapability.has(input)) return Optional.empty();

        IHeat heat = HeatCapability.get(input);
        if (heat == null || !heat.canWork()) return Optional.empty();

        return AnvilRecipe.getAll(level, input, MAX_TIER).stream()
            .map(RecipeHolder::value)
            .filter(r -> filtering.test(assemble(r, input)))
            .findFirst();
    }

    /**
     * Mirrors {@code AnvilBlockEntity}: the output always inherits the input's
     * temperature, so a pressed part comes out as hot as it went in.
     */
    private ItemStack assemble(AnvilRecipe recipe, ItemStack input) {
        ItemStack output = recipe.assemble(stubInventory(input.copyWithCount(1)), level.registryAccess());
        IHeat outputHeat = HeatCapability.get(output);
        if (outputHeat != null) outputHeat.setTemperatureIfWarmer(HeatCapability.get(input));
        return output;
    }

    private AnvilRecipe.Inventory stubInventory(ItemStack input) {
        return new AnvilRecipe.Inventory() {
            @Override public ItemStack getItem(int slot) { return input; }
            @Override public int size() { return 1; }
            @Override public ItemStack getItem() { return input; }
            @Override public int getTier() { return 0; }
            @Override public long getSeed() { return level instanceof ServerLevel sl ? sl.getSeed() : 0; }
        };
    }
}
