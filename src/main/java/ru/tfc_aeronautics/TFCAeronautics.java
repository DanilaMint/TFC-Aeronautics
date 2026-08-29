package ru.tfc_aeronautics;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.tfc_aeronautics.anvil.AnvilRegistration;
import ru.tfc_aeronautics.bracket.WoodenBracketRegistration;
import ru.tfc_aeronautics.burlap.BurlapRegistration;
import ru.tfc_aeronautics.chain.ChainConveyorRegistration;
import ru.tfc_aeronautics.composite.CompositeRegistration;
import ru.tfc_aeronautics.drill_head.DrillHeadRegistration;
import ru.tfc_aeronautics.fluid.FluidBlocks;
import ru.tfc_aeronautics.fluid.FluidItems;
import ru.tfc_aeronautics.fluid.Fluids;
import ru.tfc_aeronautics.heat.HeatDealerRegistration;
import ru.tfc_aeronautics.heater.HeaterRegistration;
import ru.tfc_aeronautics.metal.TightSheetRegistration;
import ru.tfc_aeronautics.powder.PowderRegistration;
import ru.tfc_aeronautics.recipe.RecipeRegistration;
import ru.tfc_aeronautics.resin.ResinRegistration;
import ru.tfc_aeronautics.sequenced.SequencedRegistration;
import ru.tfc_aeronautics.saw.SawBladeRegistration;
import ru.tfc_aeronautics.stamping_press.StampingPressRegistration;
import ru.tfc_aeronautics.welding_depot.WeldingDepotRegistration;
import ru.tfc_aeronautics.wrench.WrenchHeadRegistration;
import ru.tfc_aeronautics.worldgen.ProcessorTypes;
import ru.tfc_aeronautics.worldgen.StructurePieceTypes;
import ru.tfc_aeronautics.worldgen.StructureTypes;

/**
 * Main entry point for the TFC Aeronautics mod.
 * Provides airships, gliders and other aerial contraptions
 * to integrate with TerraFirmaCraft's realistic survival experience.
 */
@Mod(TFCAeronautics.MOD_ID)
public class TFCAeronautics {
    public static final String MOD_ID = "tfc_aeronautics";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public TFCAeronautics(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing TFC Aeronautics");

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        PowderRegistration.register(modEventBus);
        CompositeRegistration.register(modEventBus);
        SequencedRegistration.register(modEventBus);
        TightSheetRegistration.register(modEventBus);
        SawBladeRegistration.register(modEventBus);
        DrillHeadRegistration.register(modEventBus);
        WrenchHeadRegistration.register(modEventBus);
        FluidBlocks.register(modEventBus);
        FluidItems.register(modEventBus);
        Fluids.register(modEventBus);
        HeaterRegistration.register(modEventBus);
        HeatDealerRegistration.register(modEventBus);
        StampingPressRegistration.register(modEventBus);
        ResinRegistration.register(modEventBus);
        BurlapRegistration.register(modEventBus);
        AnvilRegistration.register(modEventBus);
        ChainConveyorRegistration.register(modEventBus);
        WoodenBracketRegistration.register(modEventBus);
        WeldingDepotRegistration.register(modEventBus);
        CreativeTabs.register(modEventBus);
        RecipeRegistration.register(modEventBus);

        StructureTypes.register(modEventBus);
        StructurePieceTypes.register(modEventBus);
        ProcessorTypes.register(modEventBus);
    }
}