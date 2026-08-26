package ru.tfc_aeronautics.recipe;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.resources.ResourceLocation;

import org.slf4j.Logger;

import ru.tfc_aeronautics.TFCAeronautics;

/**
 * Holds the set of {@link ResourceLocation}s that must never appear in the
 * {@link net.minecraft.world.item.crafting.RecipeManager} — stripped after
 * every reload by {@code mixin/RecipeManagerMixin}.
 *
 * <p>Use this when a recipe must be reachable through a non-vanilla path only
 * (e.g. an anvil recipe in TFC). The vanilla JSON entry still ships with the
 * source mod; we drop it at runtime instead of editing datapacks.
 *
 * <p>Add new IDs here when the next removal shows up. The list lives in code,
 * not a config, because the mod's removal policy is design-level (which
 * recipes exist at all) rather than a player-facing toggle.
 */
public final class RecipeRemoval
{
    public static final Logger LOGGER = TFCAeronautics.LOGGER;

    /**
     * Recipe IDs to strip from the {@code RecipeManager} on every reload.
     * Today: both vanilla crafting recipes for {@code create:fluid_pipe}
     * (horizontal and vertical patterns — same output item, different
     * layouts). The replacement path is
     * {@code tfc_aeronautics:anvil/fluid_pipe}; the vanilla must not be
     * reachable via crafting grid / JEI. The wooden bracket is replaced by
     * twenty per-wood crafting recipes under
     * {@code tfc_aeronautics:crafting/wood/bracket/&lt;wood&gt;}.
     *
     * <p><b>Note:</b> recipe IDs in vanilla are derived from the JSON file
     * path under {@code recipe/}, not from the item id. So a recipe at
     * {@code data/create/recipe/crafting/kinetics/fluid_pipe.json} has key
     * {@code create:crafting/kinetics/fluid_pipe} — {@code create:fluid_pipe}
     * is the output item id and would silently miss every reload.
     *
     * <p>{@code simulated:rope_coupling} is stripped because its only crafting
     * path is now the free {@code tfc:rope} ↔ {@code simulated:rope_coupling}
     * conversion in {@code tfc_aeronautics:crafting/rope_to_rope_coupling}
     * (and the reverse in {@code .../rope_coupling_to_rope}).
     *
     * <p>{@code create:industrial_iron_block_from_ingots_iron_stonecutting}
     * is stripped because its only path is now TFC-anvil recipes under
     * {@code tfc_aeronautics:anvil/industrial_iron_block_cast_iron} and
     * {@code tfc_aeronautics:anvil/industrial_iron_block_steel}.
     */
    public static final Set<ResourceLocation> BANNED_RECIPES = ImmutableSet.of(
        ResourceLocation.fromNamespaceAndPath("create", "crafting/kinetics/fluid_pipe"),
        ResourceLocation.fromNamespaceAndPath("create", "crafting/kinetics/fluid_pipe_vertical"),
        ResourceLocation.fromNamespaceAndPath("create", "crafting/kinetics/encased_chain_drive_from_zinc"),
        ResourceLocation.fromNamespaceAndPath("create", "crafting/kinetics/chain_conveyor"),
        ResourceLocation.fromNamespaceAndPath("create", "crafting/kinetics/clutch"),
        ResourceLocation.fromNamespaceAndPath("create", "crafting/kinetics/metal_bracket"),
        ResourceLocation.fromNamespaceAndPath("create", "crafting/kinetics/wooden_bracket"),
        ResourceLocation.fromNamespaceAndPath("create", "crafting/kinetics/depot"),
        ResourceLocation.fromNamespaceAndPath("create", "crafting/kinetics/copper_valve_handle"),
        ResourceLocation.fromNamespaceAndPath("create", "crafting/kinetics/mechanical_plough"),
        ResourceLocation.fromNamespaceAndPath("simulated", "rope_coupling"),
        ResourceLocation.fromNamespaceAndPath("create", "industrial_iron_block_from_ingots_iron_stonecutting")
    );

    private RecipeRemoval() {}
}