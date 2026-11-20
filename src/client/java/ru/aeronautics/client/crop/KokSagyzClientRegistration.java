package ru.aeronautics.client.crop;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import net.dries007.tfc.client.TFCColors;

import ru.tfc_aeronautics.TFCAeronautics;
import ru.tfc_aeronautics.crop.KokSagyzRegistration;

/**
 * Client side wiring for the Kok-Saghyz crop.
 *
 * <p>The plant is drawn with the crossed plane models TFC uses for its own
 * crops, which need the cutout render layer - without it the leaves render
 * as solid squares. The wild plant additionally takes the seasonal grass tint,
 * the same way TFC tints its own wild crops.
 */
@EventBusSubscriber(modid = TFCAeronautics.MOD_ID, value = Dist.CLIENT)
public final class KokSagyzClientRegistration
{
    private KokSagyzClientRegistration() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event)
    {
        final RenderType cutout = RenderType.cutout();
        ItemBlockRenderTypes.setRenderLayer(KokSagyzRegistration.CROP.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(KokSagyzRegistration.DEAD_CROP.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(KokSagyzRegistration.WILD_CROP.get(), cutout);
    }

    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.Block event)
    {
        event.register((state, level, pos, tintIndex) -> TFCColors.getGrassColor(pos, tintIndex),
            KokSagyzRegistration.WILD_CROP.get());
    }

    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event)
    {
        final ItemColor grassColor = (stack, tintIndex) -> TFCColors.getGrassColor(null, tintIndex);
        event.register(grassColor, KokSagyzRegistration.WILD_CROP_ITEM.get());
    }
}
