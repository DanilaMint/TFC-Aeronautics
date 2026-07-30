package ru.tfc_aeronautics.heater;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * A 1×1×1 block that heats an inserted item by burning fuel, optionally boosted
 * by a TFC bellows or a Create Encased Fan. Exposes an {@code IItemHandler}
 * capability (so chute / funnel / hopper / Create arm can interact with it)
 * and an {@code IFluidHandler} capability on the down face (so any pipe can
 * extract molten metal).
 *
 * <p>The block has a {@link BlockStateProperties#HORIZONTAL_FACING} property so
 * it can be rotated with Create's wrench. The face the knob points to (where
 * {@link HeaterValueBehaviour} is rendered) follows this facing — see
 * {@link HeaterValueBoxTransform}.
 *
 * <p>RMB behaviour: insert the held item stack into the matching inventory slot
 * (heatable item → slot 0, fuel → slot 1). Sneak + RMB extracts a single
 * hot-enough item from slot 0. There is no GUI container.
 */
public class HeaterBlock extends Block implements IBE<HeaterBlockEntity>, IWrenchable {

    public HeaterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.LIT, BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(
            BlockStateProperties.HORIZONTAL_FACING,
            context.getHorizontalDirection().getOpposite()
        );
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(BlockStateProperties.LIT) ? 14 : 0;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return ItemInteractionResult.sidedSuccess(true);
        if (!tryInsertHeld(level, pos, hit, player, hand, stack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return ItemInteractionResult.sidedSuccess(false);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.sidedSuccess(true);
        if (!player.isShiftKeyDown()) return InteractionResult.PASS;
        if (!tryExtract(level, pos, hit, player)) {
            return InteractionResult.FAIL;
        }
        return InteractionResult.sidedSuccess(false);
    }

    private boolean tryInsertHeld(Level level, BlockPos pos, BlockHitResult hit, Player player, InteractionHand hand, ItemStack held) {
        if (held.isEmpty()) return false;
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, hit.getDirection());
        if (handler == null) return false;

        int target = -1;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (handler.isItemValid(slot, held) && handler.getStackInSlot(slot).getCount() < handler.getSlotLimit(slot)) {
                target = slot;
                break;
            }
        }
        if (target < 0) return false;

        ItemStack toInsert = held.copyWithCount(1);
        ItemStack remainder = handler.insertItem(target, toInsert, false);
        if (remainder.getCount() == toInsert.getCount()) {
            return false;
        }
        ItemStack newHeld = held.copy();
        newHeld.shrink(toInsert.getCount() - remainder.getCount());
        player.setItemInHand(hand, newHeld);
        return true;
    }

    private boolean tryExtract(Level level, BlockPos pos, BlockHitResult hit, Player player) {
        if (!(level.getBlockEntity(pos) instanceof HeaterBlockEntity heater)) return false;
        ItemStack extracted = heater.getInventory().extractItem(HeaterBlockEntity.SLOT_ITEM, 1, false);
        if (extracted.isEmpty()) return false;
        if (!player.getInventory().add(extracted)) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, extracted);
        }
        return true;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof HeaterBlockEntity heater) {
            for (int i = 0; i < heater.getInventory().getSlots(); i++) {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, heater.getInventory().getStackInSlot(i));
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof HeaterBlockEntity heater) {
            return heater.getBurnTicks() > 0 ? 15 : 0;
        }
        return 0;
    }

    @Override
    public Class<HeaterBlockEntity> getBlockEntityClass() {
        return HeaterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends HeaterBlockEntity> getBlockEntityType() {
        return HeaterRegistration.HEATER_BE.get();
    }

    // --- IWrenchable -----------------------------------------------------

    /**
     * The heater stores no kinetic state and has no neighbour-dependent shape, so a
     * plain {@code level.setBlock(... UPDATE_ALL)} is enough — no need for
     * {@code KineticBlockEntity.switchToBlockState} and its kinetic-network bookkeeping.
     * Rotation comes from {@link #getRotatedBlockState(BlockState, Direction)}.
     */
    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState rotated = getRotatedBlockState(state, context.getClickedFace());
        if (rotated == state || !rotated.canSurvive(level, pos))
            return InteractionResult.PASS;

        level.setBlock(pos, rotated, Block.UPDATE_ALL);
        if (level.getBlockState(pos) != state)
            IWrenchable.playRotateSound(level, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        // Default IWrenchable only rotates HORIZONTAL_FACING when the player clicks the top/bottom
        // (axis Y). We do the same here so the four cardinal settings stay consistent, regardless of
        // which face the wrench targets.
        if (targetedFace.getAxis() != Direction.Axis.Y) return originalState;
        if (!originalState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) return originalState;
        return originalState.setValue(
            BlockStateProperties.HORIZONTAL_FACING,
            originalState.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise(Direction.Axis.Y)
        );
    }
}