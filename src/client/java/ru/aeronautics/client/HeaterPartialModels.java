package ru.aeronautics.client;

import com.simibubi.create.client.model.PartialModel;

import net.minecraft.resources.ResourceLocation;

import ru.tfc_aeronautics.Aeronautics;

/**
 * Client-only {@link PartialModel}s for the heater. Mirrors {@code AllPartialModels}
 * from Create: each constant references a model JSON under
 * {@code assets/tfc_aeronautics/models/block/<path>.json}, which the heater's
 * block-entity renderer can look up at runtime and animate.
 *
 * <p>{@link #HEATER_FLAME} is the small glowing quad rendered on top of the heater
 * body whenever the block is in the {@code lit=true} state. Its flicker comes
 * from {@link HeaterBlockEntityRenderer}, not from the model itself.
 */
public final class HeaterPartialModels {

    public static final PartialModel HEATER_FLAME =
        new PartialModel(ResourceLocation.fromNamespaceAndPath(Aeronautics.MOD_ID, "heater/flame"));

    private HeaterPartialModels() {}
}