package ru.tfc_aeronautics.mixin.client.screens;

import net.minecraft.network.chat.MutableComponent;

import net.dries007.tfc.client.screen.CrucibleScreen;
import net.dries007.tfc.config.TemperatureDisplayStyle;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import ru.aeronautics.client.HeatTooltipGoggles;

@Mixin(CrucibleScreen.class)
public abstract class CrucibleScreenMixin
{
    @Redirect(
        method = "renderTooltip",
        at = @At(
            value = "INVOKE",
            target = "Lnet/dries007/tfc/config/TemperatureDisplayStyle;formatColored(F)Lnet/minecraft/network/chat/MutableComponent;"
        )
    )
    private MutableComponent tfcAeronautics$gogglesFormatColored(TemperatureDisplayStyle style, float temperature)
    {
        final MutableComponent goggled = HeatTooltipGoggles.withGoggles(temperature);
        return goggled != null ? goggled : style.formatColored(temperature);
    }
}
