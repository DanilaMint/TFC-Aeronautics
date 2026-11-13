package ru.aeronautics.client.ponder;

import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.neoforged.neoforge.registries.DeferredHolder;
import ru.aeronautics.client.ponder.scenes.CondenserCoilScenes;
import ru.aeronautics.client.ponder.scenes.StampingPressScenes;
import ru.tfc_aeronautics.condenser_coil.CondenserCoilRegistration;
import ru.tfc_aeronautics.stamping_press.StampingPressRegistration;

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
public final class PonderScenes {

    private PonderScenes() {}

    public static void register(PonderSceneRegistrationHelper<DeferredHolder<?, ?>> helper) {
        helper.forComponents(CondenserCoilRegistration.CONDENSER_COIL)
            .addStoryBoard("condenser_coil/distillating", CondenserCoilScenes::distillating,
                PonderTags.FLUIDS);

        helper.forComponents(StampingPressRegistration.STAMPING_PRESS)
            .addStoryBoard("stamping_press/pressing", StampingPressScenes::pressing,
                PonderTags.KINETIC_APPLIANCES);
    }
}
