package ru.aeronautics.client.stamping_press;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBox;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;

import net.createmod.catnip.data.Pair;
import net.createmod.catnip.outliner.Outliner;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import ru.tfc_aeronautics.TFCAeronautics;
import ru.tfc_aeronautics.stamping_press.StampingPressBlockEntity;
import ru.tfc_aeronautics.stamping_press.StampingPressFilterSlot;
import ru.tfc_aeronautics.stamping_press.StampingPressInstances;

/**
 * Draws the value-box frame for each installed filter on both perpendicular
 * sides of the press, every client tick. The standard
 * {@code FilteringRenderer.tick()} only shows a frame on the side the player
 * is looking at, and only on hover — here we make both sides visible whenever
 * a filter is installed, regardless of camera angle.
 */
@EventBusSubscriber(modid = TFCAeronautics.MOD_ID, value = Dist.CLIENT)
public final class StampingPressFrameTickHandler {

    private static final AABB EMPTY_BB = new AABB(Vec3.ZERO, Vec3.ZERO);

    private StampingPressFrameTickHandler() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().level == null) return;

        for (StampingPressBlockEntity be : StampingPressInstances.getInstances()) {
            if (be.isRemoved() || !be.hasLevel()) continue;

            BlockState state = be.getBlockState();
            if (state.isAir()) continue;
            if (!state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) continue;

            for (BlockEntityBehaviour behaviour : be.getAllBehaviours()) {
                if (!(behaviour instanceof FilteringBehaviour filter)) continue;
                if (!filter.isActive()) continue;
                ItemStack filterItem = filter.getFilter();
                if (filterItem.isEmpty()) continue;
                if (!(filter.getSlotPositioning() instanceof StampingPressFilterSlot)) continue;

                Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                for (Direction side : new Direction[] {facing.getClockWise(), facing.getCounterClockWise()}) {
                    drawFrame(be, filter, filterItem, side);
                }
            }
        }
    }

    private static void drawFrame(StampingPressBlockEntity be, FilteringBehaviour filter, ItemStack filterItem,
        Direction side) {
        BlockPos pos = be.getBlockPos();

        AABB bb = EMPTY_BB.inflate(.45f, .31f, .2f);
        ValueBox box = new ValueBox.ItemValueBox(
            filter.getLabel(),
            bb,
            pos,
            filterItem,
            filter.getCountLabelForValueBox()
        );
        box.passive(false);
        box.transform(filter.getSlotPositioning());

        Object key = Pair.of("tfc_aero_stampingFilter_" + filter.netId() + "_" + side, pos);

        Outliner.getInstance()
            .showOutline(key, box)
            .lineWidth(1 / 64f)
            .highlightFace(side);
    }
}
