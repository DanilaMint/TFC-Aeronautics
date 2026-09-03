package ru.aeronautics.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import ru.tfc_aeronautics.TFCAeronautics;

/**
 * Registers NeoForge's built-in {@link ConfigurationScreen} as the in-game
 * config UI for this mod, so every {@code ModConfigSpec} value (including
 * {@code heaterSpeedMultiplier}) appears in the mods list config screen.
 */
@EventBusSubscriber(modid = TFCAeronautics.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ConfigScreenRegistration {
    private ConfigScreenRegistration() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ModList.get().getModContainerById(TFCAeronautics.MOD_ID).ifPresent(container ->
            container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new)
        );
    }
}