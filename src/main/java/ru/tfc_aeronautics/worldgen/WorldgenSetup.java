package ru.tfc_aeronautics.worldgen;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import ru.tfc_aeronautics.TFCAeronautics;

/**
 * Registers runtime-only worldgen side-effects that have no datapack equivalent.
 *
 * <p>Static registries ({@link StructureTypes},
 * {@link StructurePieceTypes}, {@link ProcessorTypes}) are wired
 * to the mod event bus in {@link TFCAeronautics}'s constructor. This subscriber handles the
 * rest: code-defined hooks that structures and pieces call into via
 * {@link AtmosphereSpec.Effect}.
 */
@EventBusSubscriber(modid = TFCAeronautics.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class WorldgenSetup {
    private WorldgenSetup() {}

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(GraveyardLootEffect::register);
        event.enqueueWork(AncientShelterEffects::register);
        event.enqueueWork(FarmerHouseEffects::register);
        event.enqueueWork(TannerHouseEffects::register);
        event.enqueueWork(RichGraveyardEffects::register);
    }
}
