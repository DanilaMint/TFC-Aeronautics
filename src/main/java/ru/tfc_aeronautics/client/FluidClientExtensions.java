package ru.tfc_aeronautics.client;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

import net.dries007.tfc.client.extensions.FluidRendererExtension;
import net.dries007.tfc.common.fluids.TFCFluids;

import ru.tfc_aeronautics.TFCAeronautics;
import ru.tfc_aeronautics.fluid.Fluids;

/**
 * Registers {@link net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions}
 * for rosin so TFC's {@code ContainedFluidModel} can resolve a still/flowing
 * texture sprite when rendering it inside containers (buckets, molds).
 *
 * Without this registration, {@code ContainedFluidModel.bake()} dereferences a null
 * {@link ResourceLocation} from {@code IClientFluidTypeExtensions.of(fluid).getStillTexture()}
 * and crashes the client with a {@code NullPointerException} in {@link TextureAtlas}.
 */
@EventBusSubscriber(modid = TFCAeronautics.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FluidClientExtensions
{
    private FluidClientExtensions() {}

    @SubscribeEvent
    public static void registerFluidExtensions(RegisterClientExtensionsEvent event)
    {
        // Rosin — honey-amber tint over vanilla water textures (same approach TFC uses for alcohol)
        event.registerFluidType(
            new FluidRendererExtension(
                TFCFluids.ALPHA_MASK | 0xC68A3A,
                ResourceLocation.withDefaultNamespace("block/water_still"),
                ResourceLocation.withDefaultNamespace("block/water_flow"),
                null, null),
            Fluids.ROSIN.getType());
    }
}
