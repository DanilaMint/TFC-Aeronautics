package ru.tfc_aeronautics.client;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

import net.dries007.tfc.client.extensions.FluidRendererExtension;
import net.dries007.tfc.common.fluids.TFCFluids;

import ru.tfc_aeronautics.Aeronautics;
import ru.tfc_aeronautics.fluid.AeronauticsFluids;

/**
 * Registers {@link net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions}
 * for our molten fluids so TFC's {@code ContainedFluidModel} can resolve a still/flowing
 * texture sprite when rendering them inside containers (buckets, molds).
 *
 * Without this registration, {@code ContainedFluidModel.bake()} dereferences a null
 * {@link ResourceLocation} from {@code IClientFluidTypeExtensions.of(fluid).getStillTexture()}
 * and crashes the client with a {@code NullPointerException} in {@link TextureAtlas}.
 *
 * Reuses TFC's {@code block/molten_still} and {@code block/molten_flow} textures from
 * its own JAR — no new PNGs needed.
 */
@EventBusSubscriber(modid = Aeronautics.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FluidClientExtensions
{
    private static final ResourceLocation TFC_MOLTEN_STILL =
        ResourceLocation.fromNamespaceAndPath("tfc", "block/molten_still");
    private static final ResourceLocation TFC_MOLTEN_FLOW =
        ResourceLocation.fromNamespaceAndPath("tfc", "block/molten_flow");

    private FluidClientExtensions() {}

    @SubscribeEvent
    public static void registerFluidExtensions(RegisterClientExtensionsEvent event)
    {
        // Andesite alloy — dark orange molten metal look
        event.registerFluidType(
            new FluidRendererExtension(
                TFCFluids.ALPHA_MASK | 0xB06820,
                TFC_MOLTEN_STILL, TFC_MOLTEN_FLOW, null, null),
            AeronauticsFluids.MOLTEN_ANDESITE_ALLOY.getType());

        // Magmatite — dark grey molten metal look
        event.registerFluidType(
            new FluidRendererExtension(
                TFCFluids.ALPHA_MASK | 0x3F3F3F,
                TFC_MOLTEN_STILL, TFC_MOLTEN_FLOW, null, null),
            AeronauticsFluids.MOLTEN_MAGMATITE.getType());
    }
}
