package ru.tfc_aeronautics.welding_depot;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.IItemHandler;

public class WeldingDepotBlock extends Block implements IBE<WeldingDepotBlockEntity> {

    private final int tier;

    public WeldingDepotBlock(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }

    public int getTier() {
        return tier;
    }

    @Override
    public Class<WeldingDepotBlockEntity> getBlockEntityClass() {
        return WeldingDepotBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends WeldingDepotBlockEntity> getBlockEntityType() {
        return WeldingDepotRegistration.WELDING_DEPOT_BE.get();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return ItemInteractionResult.sidedSuccess(true);
        if (stack.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        WeldingDepotBlockEntity be = getBlockEntity(level, pos);
        if (be == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        IItemHandler handler = be.getExternalHandler();
        for (int slot = 0; slot < 3; slot++) {
            if (!be.getInventory().getStackInSlot(slot).isEmpty()) continue;
            if (!handler.isItemValid(slot, stack)) continue;
            int toInsert = Math.min(stack.getCount(), handler.getSlotLimit(slot));
            ItemStack toInsertStack = stack.split(toInsert);
            ItemStack remainder = handler.insertItem(slot, toInsertStack, false);
            if (!remainder.isEmpty()) {
                if (!player.getInventory().add(remainder)) {
                    player.drop(remainder, false);
                }
            }
            return ItemInteractionResult.sidedSuccess(false);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.sidedSuccess(true);
        WeldingDepotBlockEntity be = getBlockEntity(level, pos);
        if (be == null) return InteractionResult.PASS;
        for (int slot : WeldingDepotBlockEntity.EXTRACT_PRIORITY) {
            ItemStack current = be.getInventory().getStackInSlot(slot);
            if (current.isEmpty()) continue;
            int amount = slot == WeldingDepotBlockEntity.SLOT_FLUX ? current.getCount() : 1;
            ItemStack extracted = be.getInventory().extractItem(slot, amount, false);
            if (extracted.isEmpty()) continue;
            if (!player.getInventory().add(extracted)) {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, extracted);
            }
            return InteractionResult.sidedSuccess(false);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof WeldingDepotBlockEntity wdbe) {
            for (int slot = 0; slot < wdbe.getInventory().getSlots(); slot++) {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    wdbe.getInventory().getStackInSlot(slot));
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        super.fallOn(level, state, pos, entity, fallDistance);
        if (!(entity instanceof ItemEntity itemEntity)) return;
        if (!(level.getBlockEntity(pos) instanceof WeldingDepotBlockEntity be)) return;
        IItemHandler handler = be.getExternalHandler();
        ItemStack carried = itemEntity.getItem();
        for (int slot = 0; slot < 3 && !carried.isEmpty(); slot++) {
            if (!be.getInventory().getStackInSlot(slot).isEmpty()) continue;
            if (!handler.isItemValid(slot, carried)) continue;
            int toInsert = Math.min(carried.getCount(), handler.getSlotLimit(slot));
            ItemStack toInsertStack = carried.split(toInsert);
            ItemStack remainder = handler.insertItem(slot, toInsertStack, false);
            if (!remainder.isEmpty()) carried = remainder;
        }
        if (carried.isEmpty()) itemEntity.setItem(ItemStack.EMPTY);
        else itemEntity.setItem(carried);
    }
}
