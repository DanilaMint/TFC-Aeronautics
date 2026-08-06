package ru.tfc_aeronautics.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import ru.tfc_aeronautics.Aeronautics;

/**
 * Registers the mod's {@link StructureProcessorType}s.
 *
 * <p>{@link GraveyardMaterialProcessor} is resolved against the surrounding world at
 * placement time rather than configured from a processor list, so its codec is only ever
 * used to satisfy {@link net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor#getType()}.
 */
public final class AeronauticsProcessorTypes {
    public static final DeferredRegister<StructureProcessorType<?>> PROCESSOR_TYPES =
        DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, Aeronautics.MOD_ID);

    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<GraveyardMaterialProcessor>> GRAVEYARD_MATERIAL =
        PROCESSOR_TYPES.register("graveyard_material", () -> () -> GraveyardMaterialProcessor.CODEC);

    private AeronauticsProcessorTypes() {}

    public static void register(IEventBus bus) {
        PROCESSOR_TYPES.register(bus);
    }
}
