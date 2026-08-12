package ru.aeronautics.client.stamping_press;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;

import net.minecraft.resources.ResourceLocation;

import ru.tfc_aeronautics.TFCAeronautics;

/**
 * Partial models used by the {@link StampingPressRenderer}. The head model is
 * baked from {@code assets/tfc_aeronautics/models/block/stamping_press_head.json}.
 */
public final class StampingPressPartialModels {

    public static final PartialModel STAMPING_PRESS_HEAD =
        PartialModel.of(ResourceLocation.fromNamespaceAndPath(TFCAeronautics.MOD_ID, "block/stamping_press_head"));

    private StampingPressPartialModels() {}
}
