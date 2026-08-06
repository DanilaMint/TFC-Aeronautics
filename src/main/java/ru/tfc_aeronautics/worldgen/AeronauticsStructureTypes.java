package ru.tfc_aeronautics.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import ru.tfc_aeronautics.Aeronautics;

/**
 * Registers the mod's custom {@link StructureType}s for the atmospheric structure framework.
 *
 * <p>Vanilla structures (e.g. {@code minecraft:jigsaw}, {@code minecraft:village})
 * are registered by Minecraft itself; this registry exists only for the mod's own
 * structure codec — currently {@link AtmosphericStructure}.
 *
 * <p>Registration follows the same pattern as the rest of the mod
 * (see {@link ru.tfc_aeronautics.heater.HeaterRegistration}, {@code StampingPressRegistration}).
 */
public final class AeronauticsStructureTypes {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
        DeferredRegister.create(Registries.STRUCTURE_TYPE, Aeronautics.MOD_ID);

    public static final DeferredHolder<StructureType<?>, StructureType<AtmosphericStructure>> ATMOSPHERIC =
        STRUCTURE_TYPES.register("atmospheric", () -> () -> AtmosphericStructure.CODEC);

    public static final DeferredHolder<StructureType<?>, StructureType<AncientGraveyardStructure>> ANCIENT_GRAVEYARD =
        STRUCTURE_TYPES.register("ancient_graveyard", () -> () -> AncientGraveyardStructure.CODEC);

    private AeronauticsStructureTypes() {}

    public static void register(IEventBus bus) {
        STRUCTURE_TYPES.register(bus);
    }
}