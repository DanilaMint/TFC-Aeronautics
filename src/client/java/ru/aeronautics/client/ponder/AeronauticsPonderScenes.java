package ru.aeronautics.client.ponder;

import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

import ru.tfc_aeronautics.heater.HeaterRegistration;
import ru.tfc_aeronautics.stamping_press.StampingPressRegistration;
import ru.aeronautics.client.ponder.scenes.HeaterScenes;
import ru.aeronautics.client.ponder.scenes.StampingPressScenes;

/**
 * Binds storyboards (scene methods) to the blocks they describe.
 *
 * Scene id format:
 *   - "<scene>" for simple single-scene blocks
 *   - "<block>/<scene>" for blocks with multiple scenes (e.g. "stamping_press/pressing")
 * The id is used to look up the schematic NBT at
 * assets/tfc_aeronautics/ponder/<id>.nbt and to generate lang keys
 * tfc_aeronautics.ponder.<id>.header / .text_N.
 */
public final class AeronauticsPonderScenes {

    private AeronauticsPonderScenes() {}

    public static void register(PonderSceneRegistrationHelper<DeferredHolder<?, ?>> helper) {
        helper.forComponents(HeaterRegistration.HEATER)
            .addStoryBoard("heater/intro", HeaterScenes::intro,
                AeronauticsPonderTags.AERONAUTICS_KINETICS);

        helper.forComponents(StampingPressRegistration.STAMPING_PRESS)
            .addStoryBoard("stamping_press/pressing", StampingPressScenes::pressing,
                AeronauticsPonderTags.AERONAUTICS_KINETICS);
    }
}