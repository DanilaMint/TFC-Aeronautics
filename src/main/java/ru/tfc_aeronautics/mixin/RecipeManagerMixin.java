package ru.tfc_aeronautics.mixin;

import java.util.Map;

import com.google.gson.JsonElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;

import ru.tfc_aeronautics.recipe.RecipeRemoval;

/**
 * Drops recipes listed in {@link RecipeRemoval#BANNED_RECIPES} from the
 * {@link RecipeManager} on every reload, on both server and client.
 *
 * <p>{@code RecipeManager} is a {@code SimpleJsonResourceReloadListener}, so
 * {@code apply()} fires symmetrically on both logical sides — vanilla populates
 * {@code byName} and {@code byType} identically from the datapacks, and the same
 * code path is hit on dedicated servers, integrated servers, and the client
 * (which also reloads recipes for the recipe book / JEI display). Mutating the
 * input map here is therefore enough to keep server lookup and client display
 * in agreement: the recipe is gone everywhere.
 *
 * <p><b>Why HEAD on the input map, not TAIL on the outputs.</b> In 1.21.1,
 * {@code RecipeManager.apply()} finishes by handing the caller an
 * {@code ImmutableMap} for {@code byName} and an {@code ImmutableMultimap} for
 * {@code byType} — they are built by the time TAIL fires, so injecting there
 * and calling {@code .remove()} throws {@code UnsupportedOperationException}
 * and aborts the whole datapack reload. We therefore inject at HEAD and
 * mutate the first parameter directly. That parameter is the
 * {@code Map<ResourceLocation, JsonElement>} that
 * {@code SimpleJsonResourceReloadListener.prepare()} just built — a fresh
 * mutable {@code HashMap} — and any entry we delete never gets parsed into a
 * {@code RecipeHolder} in the first place, so {@code byName}/{@code byType}
 * naturally come out without it.
 *
 * <p>This is the same pattern vanilla / Forge / Fabric recipe filters use:
 * filter the JSON before it reaches the codec, not the parsed map after.
 */
@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin
{
    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"))
    private void aeronautics$stripBannedRecipes(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci)
    {
        int removed = 0;
        for (ResourceLocation id : RecipeRemoval.BANNED_RECIPES)
        {
            if (map.remove(id) != null)
            {
                removed++;
            }
        }
        if (removed > 0)
        {
            RecipeRemoval.LOGGER.info("Stripped {} recipe(s) from RecipeManager", removed);
        }
    }
}