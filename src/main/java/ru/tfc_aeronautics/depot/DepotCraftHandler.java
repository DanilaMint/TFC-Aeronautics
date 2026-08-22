package ru.tfc_aeronautics.depot;

import com.simibubi.create.AllBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import ru.tfc_aeronautics.TFCAeronautics;

/**
 * Replaces the vanilla shapeless recipe for {@code create:depot} with a
 * TFC-mimic {@code useItemOn} mechanic: hitting the top face of
 * {@code create:andesite_casing} with any hammer (
 * {@code c:tools/hammer}) transforms the block into {@code create:depot},
 * provided the block above is air. Mirrors
 * {@code RockConvertableToAnvilBlock.useItemOn} in TFC — we cannot inherit
 * from {@code AndesiteCasingBlock} (Create's class), so the same logic is
 * implemented as a NeoForge event listener instead.
 */
@EventBusSubscriber(modid = TFCAeronautics.MOD_ID)
public final class DepotCraftHandler {
    private static final TagKey<Item> HAMMERS = ItemTags.create(
        ResourceLocation.fromNamespaceAndPath("c", "tools/hammer"));

    private DepotCraftHandler() {}

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHitVec().getDirection() != Direction.UP) return;

        BlockPos pos = event.getPos();
        Level level = event.getLevel();
        if (!level.getBlockState(pos.above()).isAir()) return;

        BlockState state = level.getBlockState(pos);
        if (state.getBlock() != AllBlocks.ANDESITE_CASING.get()) return;

        ItemStack held = event.getItemStack();
        if (!held.is(HAMMERS)) return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.CONSUME);

        if (level.isClientSide()) return;

        Block depot = AllBlocks.DEPOT.get();
        level.setBlockAndUpdate(pos, depot.defaultBlockState());
    }
}
