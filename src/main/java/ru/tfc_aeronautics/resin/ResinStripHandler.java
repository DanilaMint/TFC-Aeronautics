package ru.tfc_aeronautics.resin;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.level.BlockEvent;

import ru.tfc_aeronautics.Aeronautics;
import ru.tfc_aeronautics.Config;

@EventBusSubscriber(modid = Aeronautics.MOD_ID)
public final class ResinStripHandler {
    private ResinStripHandler() {}

    @SubscribeEvent
    public static void onStrip(BlockEvent.BlockToolModificationEvent event) {
        if (event.isSimulated()) return;
        if (event.getItemAbility() != ItemAbilities.AXE_STRIP) return;
        if (!event.getState().is(ResinTags.CAN_COLLECT_RESIN)) return;

        double chance = Config.RESIN_DROP_CHANCE.get();
        if (chance <= 0.0) return;

        Level level = event.getContext().getLevel();
        RandomSource random = level.getRandom();
        if (random.nextDouble() >= chance) return;

        BlockPos pos = event.getPos();
        ItemEntity drop = new ItemEntity(
            level,
            pos.getX() + 0.5,
            pos.getY() + 0.5,
            pos.getZ() + 0.5,
            new ItemStack(ResinRegistration.RESIN_CLUMP.get())
        );
        level.addFreshEntity(drop);
    }
}
