package ru.tfc_aeronautics;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Configuration for the aeronautics mod.
 * Values are loaded from common.toml under the config directory.
 */
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue BALLOON_LIFT_MULTIPLIER = BUILDER
            .comment("How much lift a balloon provides per unit of hot air. Higher = floatier.")
            .defineInRange("balloonLiftMultiplier", 1.0, 0.1, 10.0);

    public static final ModConfigSpec.IntValue HOT_AIR_BURN_RATE = BUILDER
            .comment("How quickly fuel is consumed in a hot air balloon furnace (ticks per fuel item).")
            .defineInRange("hotAirBurnRate", 200, 20, 72000);

    public static final ModConfigSpec.DoubleValue GLIDER_DECAY_MODIFIER = BUILDER
            .comment("Multiplier applied to glider durability loss. 1.0 = vanilla rate.")
            .defineInRange("gliderDecayModifier", 1.0, 0.0, 10.0);

    public static final ModConfigSpec.DoubleValue FAN_FORGE_AIR_PER_TICK = BUILDER
            .comment("How many air-ticks per server tick a Create Encased Fan delivers to an adjacent TFC charcoal forge, per unit of RPM (one bellows push = 200 air-ticks).")
            .defineInRange("fanForgeAirPerTick", 1.5, 0.0, 20.0);

    public static final ModConfigSpec SPEC = BUILDER.build();

    @EventBusSubscriber(modid = Aeronautics.MOD_ID)
    public static class ConfigEvents {
        @SubscribeEvent
        public static void onConfigLoad(ModConfigEvent.Loading event) {
            Aeronautics.LOGGER.info("Loading aeronautics config");
        }

        @SubscribeEvent
        public static void onConfigReload(ModConfigEvent.Reloading event) {
            Aeronautics.LOGGER.info("Reloading aeronautics config");
        }
    }
}