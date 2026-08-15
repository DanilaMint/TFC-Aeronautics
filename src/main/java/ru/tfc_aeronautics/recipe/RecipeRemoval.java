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
     * reachable via crafting grid / JEI.
     *
     * <p><b>Note:</b> recipe IDs in vanilla are derived from the JSON file
     * path under {@code recipe/}, not from the item id. So a recipe at
     * {@code data/create/recipe/crafting/kinetics/fluid_pipe.json} has key
     * {@code create:crafting/kinetics/fluid_pipe} — {@code create:fluid_pipe}
     * is the output item id and would silently miss every reload.
     */
    public static final Set<ResourceLocation> BANNED_RECIPES = ImmutableSet.of(
        ResourceLocation.fromNamespaceAndPath("create", "crafting/kinetics/fluid_pipe"),
        ResourceLocation.fromNamespaceAndPath("create", "crafting/kinetics/fluid_pipe_vertical")
    );

    private RecipeRemoval() {}
}