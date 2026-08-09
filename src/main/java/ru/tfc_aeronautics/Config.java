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

    public static final ModConfigSpec.DoubleValue RESIN_DROP_CHANCE = BUILDER
            .comment("Chance that stripping (right-clicking with an axe) a resin-bearing log drops a resin clump. 0.15 = 15%.")
            .defineInRange("resinDropChance", 0.15, 0.0, 1.0);

    public static final ModConfigSpec.BooleanValue SHAFT_DAMAGE_ENABLED = BUILDER
            .comment("Whether touching a bare spinning shaft or cogwheel hurts. Encased ones are always safe.")
            .define("shaftDamageEnabled", true);

    public static final ModConfigSpec.DoubleValue SHAFT_DAMAGE_START_RPM = BUILDER
            .comment("Minimum rotation speed (RPM) at which a bare shaft starts dealing damage.")
            .defineInRange("shaftDamageStartRpm", 128.0, 0.0, 1024.0);

    public static final ModConfigSpec.DoubleValue SHAFT_DAMAGE_LETHAL_RPM = BUILDER
            .comment("Rotation speed (RPM) at which a bare shaft deals shaftDamageLethal. Damage keeps growing above this.")
            .defineInRange("shaftDamageLethalRpm", 256.0, 1.0, 1024.0);

    public static final ModConfigSpec.DoubleValue SHAFT_DAMAGE_LETHAL = BUILDER
            .comment("Damage in HP dealt at shaftDamageLethalRpm. ~6.67 is roughly a third of an unarmoured player's HP.")
            .defineInRange("shaftDamageLethal", 6.67, 0.0, 1000.0);

    public static final ModConfigSpec.DoubleValue SHAFT_DAMAGE_MULTIPLIER = BUILDER
            .comment("Multiplier applied to the whole shaft damage curve. 1.0 = as configured above.")
            .defineInRange("shaftDamageMultiplier", 1.0, 0.0, 100.0);

    public static final ModConfigSpec.DoubleValue SHAFT_KNOCKBACK_BASE = BUILDER
            .comment("Base sideways impulse applied when a shaft hits you.")
            .defineInRange("shaftKnockbackBase", 0.15, 0.0, 2.0);

    public static final ModConfigSpec.DoubleValue SHAFT_KNOCKBACK_PER_RPM = BUILDER
            .comment("Extra knockback impulse per RPM above shaftDamageStartRpm.")
            .defineInRange("shaftKnockbackPerRpm", 0.0025, 0.0, 0.1);

    public static final ModConfigSpec.DoubleValue SHAFT_SOUND_VOLUME = BUILDER
            .comment("Volume of the sound played when a shaft hits an entity. 0.0 = silent.")
            .defineInRange("shaftSoundVolume", 0.5, 0.0, 1.0);

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