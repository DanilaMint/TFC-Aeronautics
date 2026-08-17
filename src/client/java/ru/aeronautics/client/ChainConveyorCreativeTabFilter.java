package ru.aeronautics.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import ru.tfc_aeronautics.TFCAeronautics;

@EventBusSubscriber(modid = TFCAeronautics.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ChainConveyorCreativeTabFilter {

    private static final ResourceKey<net.minecraft.world.item.CreativeModeTab> CREATE_BASE_TAB =
        ResourceKey.create(Registries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath("create", "base"));

    @SubscribeEvent
    public static void onBuildContents(BuildCreativeModeTabContentsEvent event) {
        if (!event.getTabKey().equals(CREATE_BASE_TAB)) {
            return;
        }
        List<ItemStack> toRemove = new ArrayList<>();
        for (ItemStack stack : event.getParentEntries()) {
            if (isChainConveyor(stack)) {
                toRemove.add(stack);
            }
        }
        for (ItemStack stack : toRemove) {
            event.remove(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

    private static boolean isChainConveyor(ItemStack stack) {
        return stack.getItem().builtInRegistryHolder().key().location()
            .equals(ResourceLocation.fromNamespaceAndPath("create", "chain_conveyor"));
    }
}