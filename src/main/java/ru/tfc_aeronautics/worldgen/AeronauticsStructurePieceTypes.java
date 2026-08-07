package ru.tfc_aeronautics.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import ru.tfc_aeronautics.Aeronautics;

/**
 * Registers the mod's {@link StructurePieceType}s.
 *
 * <p>Unlike {@link net.minecraft.world.level.levelgen.structure.Structure} (a datapack
 * registry), piece types live in a static registry and must be registered from code —
 * they are looked up by id when a chunk's saved structure starts are read back from disk.
 */
public final class AeronauticsStructurePieceTypes {
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES =
        DeferredRegister.create(Registries.STRUCTURE_PIECE, Aeronautics.MOD_ID);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> ANCIENT_GRAVEYARD =
        STRUCTURE_PIECES.register("ancient_graveyard", () -> AncientGraveyardPiece::new);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> ATMOSPHERIC_TEMPLATE =
        STRUCTURE_PIECES.register("atmospheric_template", () -> AtmosphericTemplatePiece::new);

    private AeronauticsStructurePieceTypes() {}

    public static void register(IEventBus bus) {
        STRUCTURE_PIECES.register(bus);
    }
}
