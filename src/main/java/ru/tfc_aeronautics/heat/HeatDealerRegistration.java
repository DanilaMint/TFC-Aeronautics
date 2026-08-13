package ru.tfc_aeronautics.heat;

import com.simibubi.create.api.boiler.BoilerHeater;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import net.dries007.tfc.common.blocks.TFCBlocks;

import ru.tfc_aeronautics.heater.HeaterRegistration;

/**
 * Wires the {@link HeatDealer} and {@link BoilerHeater} registries to concrete blocks.
 *
 * <p>Runs during {@link FMLCommonSetupEvent} via {@code enqueueWork} because the
 * {@code DeferredHolder}s from TFC ({@link TFCBlocks}) are not yet resolved at
 * construction time. Any block registered as a {@code HeatDealer} is automatically
 * exposed to Create's boilers through a {@link BoilerHeater} provider.
 */
public final class HeatDealerRegistration {

    private HeatDealerRegistration() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(FMLCommonSetupEvent.class,
            event -> event.enqueueWork(HeatDealerRegistration::registerHeatDealers));
    }

    private static void registerHeatDealers() {
        HeatDealer.REGISTRY.register(TFCBlocks.FIREPIT.get(), HeatDealers.FIREPIT);
        HeatDealer.REGISTRY.register(TFCBlocks.STOVE.get(), HeatDealers.FIREPIT);
        HeatDealer.REGISTRY.register(TFCBlocks.STOVE_POT.get(), HeatDealers.FIREPIT);
        HeatDealer.REGISTRY.register(TFCBlocks.GRILL.get(), HeatDealers.FIREPIT);
        HeatDealer.REGISTRY.register(TFCBlocks.POT.get(), HeatDealers.FIREPIT);
        HeatDealer.REGISTRY.register(HeaterRegistration.HEATER.get(), HeatDealers.HEATER);

        BoilerHeater.REGISTRY.registerProvider(block ->
            HeatDealer.REGISTRY.get(block) != null ? HeatDealers::boilerAdapter : null);
    }
}
