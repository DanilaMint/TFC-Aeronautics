package ru.tfc_aeronautics;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.tfc_aeronautics.fluid.AeronauticsFluidBlocks;
import ru.tfc_aeronautics.fluid.AeronauticsFluidItems;
import ru.tfc_aeronautics.fluid.AeronauticsFluids;
import ru.tfc_aeronautics.powder.PowderRegistration;
import ru.tfc_aeronautics.recipe.RecipeOverrides;

/**
 * Main entry point for the TFC Aeronautics mod.
 * Provides airships, gliders and other aerial contraptions
 * to integrate with TerraFirmaCraft's realistic survival experience.
 */
@Mod(Aeronautics.MOD_ID)
public class Aeronautics {
    public static final String MOD_ID = "tfc_aeronautics";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public Aeronautics(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing TFC Aeronautics");

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        PowderRegistration.register(modEventBus);
        AeronauticsFluidBlocks.register(modEventBus);
        AeronauticsFluidItems.register(modEventBus);
        AeronauticsFluids.register(modEventBus);
        RecipeOverrides.register();
    }
}