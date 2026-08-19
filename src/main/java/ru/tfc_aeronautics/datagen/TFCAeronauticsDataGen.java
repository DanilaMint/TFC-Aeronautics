package ru.tfc_aeronautics.datagen;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import ru.tfc_aeronautics.TFCAeronautics;

/**
 * Entry point for the mod's client/data resource generation.
 *
 * <p>Hooked via {@link EventBusSubscriber} on the mod event bus so that
 * {@code ./gradlew runData} (or the integrated NeoForged datagen) picks
 * up the providers registered here without the mod entry class having to
 * wire them up.
 */
@EventBusSubscriber(modid = TFCAeronautics.MOD_ID)
public final class TFCAeronauticsDataGen {

    private TFCAeronauticsDataGen() {}

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var packOutput = generator.getPackOutput();
        var lookupProvider = event.getLookupProvider();
        var existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeServer(), new WoodenBracketBlockStateProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeServer(), new WoodenBracketItemModelProvider(packOutput, existingFileHelper));
    }
}
