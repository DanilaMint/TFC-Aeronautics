package ru.aeronautics.client.ponder;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.registration.SharedTextRegistrationHelper;

import ru.tfc_aeronautics.Aeronautics;

/**
 * Entry-point for TFC Aeronautics scenes in Create's Ponder Index.
 *
 * Wired into PonderIndex on FMLClientSetupEvent by
 * {@link AeronauticsPonderRegistration}. See tmp_docs/create_ponder_research.md
 * for the full Ponder API reference.
 */
public final class AeronauticsPonderPlugin implements PonderPlugin {

    @Override
    public String getModId() {
        return Aeronautics.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<DeferredHolder<?, ?>> blockHelper =
            helper.withKeyFunction(h -> h.getKey().location());
        AeronauticsPonderScenes.register(blockHelper);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        AeronauticsPonderTags.register(helper);
    }

    @Override
    public void registerSharedText(SharedTextRegistrationHelper helper) {
        helper.registerSharedText("hot_air_burn",
            "Hot air balloons burn fuel to stay aloft");
    }
}