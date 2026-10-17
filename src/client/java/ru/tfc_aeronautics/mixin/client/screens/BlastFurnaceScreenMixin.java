package ru.tfc_aeronautics.mixin.client.screens;

import net.minecraft.network.chat.MutableComponent;

import net.dries007.tfc.client.screen.BlastFurnaceScreen;
import net.dries007.tfc.config.ItemTemperatureDisplayStyle;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import ru.aeronautics.client.HeatTooltipGoggles;

@Mixin(BlastFurnaceScreen.class)
public abstract class BlastFurnaceScreenMixin
{
    @Redirect(
        method = "renderTooltip",
        require = 0,
        at = @At(
            value = "INVOKE",
            target = "Lnet/dries007/tfc/config/ItemTemperatureDisplayStyle;formatColored(F)Lnet/minecraft/network/chat/MutableComponent;"
        )
    )
    private MutableComponent tfcAeronautics$gogglesFormatColored(@Nullable ItemTemperatureDisplayStyle style, float temperature)
    {
        final MutableComponent goggled = HeatTooltipGoggles.withGoggles(temperature);
        return goggled != null ? goggled : HeatTooltipGoggles.formatFallback(style, temperature);
    }
}
