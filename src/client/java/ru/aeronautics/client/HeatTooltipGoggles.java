package ru.aeronautics.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import com.simibubi.create.content.equipment.goggles.GogglesItem;

import net.dries007.tfc.common.component.heat.Heat;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.dries007.tfc.common.recipes.HeatingRecipe;
import net.dries007.tfc.util.data.Fuel;

import ru.tfc_aeronautics.TFCAeronautics;

/**
 * When the player wears Create's Engineer's Goggles, TFC heat-related tooltip
 * lines are replaced with exact temperatures in °C, coloured by the heat
 * category. Without goggles, TFC's default behaviour is left untouched.
 *
 * <p>Three flavours of replacement, picked by context:
 * <ul>
 *   <li><b>Stand-alone heat line</b> (IHeatView) — current temperature of the stack.</li>
 *   <li><b>{@code tfc.tooltip.melts_into}</b> — melting temperature from the matching
 *       {@link HeatingRecipe}.</li>
 *   <li><b>{@code tfc.tooltip.fuel_burns_at}</b> — burning temperature from the
 *       matching {@link Fuel}.</li>
 * </ul>
 */
@EventBusSubscriber(modid = TFCAeronautics.MOD_ID, value = Dist.CLIENT)
public class HeatTooltipGoggles
{
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemTooltip(ItemTooltipEvent event)
    {
        try
        {
            final Player player = event.getEntity();
            final ItemStack stack = event.getItemStack();
            if (player == null || stack.isEmpty()) return;
            if (!GogglesItem.isWearingGoggles(player)) return;

            // Three independent sources: current stack temp, recipe melt point,
            // fuel burn point. Some items (e.g. coal) only have fuel — no HEAT
            // component — so we must check all three before bailing.
            final float stackTemp = HeatCapability.getTemperature(stack);
            final HeatingRecipe recipe = HeatingRecipe.getRecipe(stack);
            final Fuel fuel = Fuel.get(stack);

            if (stackTemp <= 0f && recipe == null && fuel == null) return;

            final MutableComponent stackReplacement = stackTemp > 0f ? exactTemp(stackTemp) : null;
            final MutableComponent meltReplacement = recipe != null ? exactTemp(recipe.getTemperature()) : null;
            final MutableComponent burnReplacement = fuel != null ? exactTemp(fuel.temperature()) : null;

            if (stackReplacement == null && meltReplacement == null && burnReplacement == null) return;

            final java.util.List<Component> tooltip = event.getToolTip();

            // Pass 1: stand-alone heat line from IHeatView.addTooltipInfo
            // ("Orange****" or "1000 °C" depending on heatTooltipStyle).
            for (int i = 0; i < tooltip.size(); i++)
            {
                final Component line = tooltip.get(i);
                if (line.getContents() instanceof TranslatableContents tc)
                {
                    final String key = tc.getKey();
                    if (key != null && (key.startsWith("tfc.enum.heat.") || key.startsWith("tfc.tooltip.temperature_")))
                    {
                        // Preserve TFC's " - can work" / " - can weld" / " - DANGER" suffixes
                        // which are attached as siblings of the same MutableComponent.
                        // Drop the heat-category stars (Unicode "٭") that TFC appends
                        // for the COLOR style — see TemperatureDisplayStyle.COLOR.
                        for (final Component sibling : line.getSiblings())
                        {
                            if (sibling.getContents() instanceof PlainTextContents pt
                                && "٭".equals(pt.text()))
                            {
                                continue;
                            }
                            stackReplacement.append(sibling);
                        }
                        tooltip.set(i, stackReplacement);
                        // Don't return: pass 2 still needs to run for fuel/melts_into/etc.
                        break;
                    }
                }
            }

            // Pass 2: heat category hidden inside TranslatableContents args of any tooltip
            // line. The replacement carries the temperature relevant to the parent line
            // (melting point for melts_into, burning point for fuel_burns_at), and falls
            // back to the stack's current temperature for any other parent context.
            for (int i = 0; i < tooltip.size(); i++)
            {
                final Component line = tooltip.get(i);
                final Component rebuilt = remapNestedHeat(line, stackReplacement, meltReplacement, burnReplacement);
                if (rebuilt != line)
                {
                    tooltip.set(i, rebuilt);
                }
            }
        }
        catch (Throwable ignored)
        {
            // TFC or Create unavailable at runtime — fail silently and leave the tooltip as-is.
        }
    }

    /**
     * Builds a "1000 °C" component coloured by the heat category, or {@code null} when the
     * temperature is not positive.
     */
    public static MutableComponent exactTemp(float temperature)
    {
        if (temperature <= 0f) return null;
        final Heat heat = Heat.getHeat(temperature);
        final ChatFormatting color = heat != null ? heat.getColor() : ChatFormatting.WHITE;
        return Component.translatable(
            "tfc.tooltip.temperature_celsius",
            String.format("%.0f", temperature)
        ).withStyle(color);
    }

    /**
     * Returns an exact-temperature component if the local player is wearing Create's
     * Engineer's Goggles; otherwise {@code null}. Used by mixins that intercept TFC's
     * block-GUI heat indicator tooltips (fire pit, charcoal forge, blast furnace,
     * crucible, firebox, grill, pot).
     */
    public static @org.jetbrains.annotations.Nullable MutableComponent withGoggles(float temperature)
    {
        if (temperature <= 0f) return null;
        final Player player = Minecraft.getInstance().player;
        if (player == null || !GogglesItem.isWearingGoggles(player)) return null;
        return exactTemp(temperature);
    }

    /**
     * Recursively rebuilds a component, replacing any nested {@link TranslatableContents}
     * whose key starts with {@code tfc.enum.heat.} (the heat-category head TFC embeds inside
     * melts_into / fuel_burns_at / etc. translatable args) with the temperature appropriate
     * to the parent translation key. Returns the original component when nothing changed.
     */
    private static Component remapNestedHeat(Component input, Component stackReplacement, MutableComponent meltReplacement, MutableComponent burnReplacement)
    {
        if (!(input instanceof MutableComponent mc)) return input;
        if (!(mc.getContents() instanceof TranslatableContents tc)) return input;

        final String key = tc.getKey();
        final Component contextReplacement;
        if ("tfc.tooltip.melts_into".equals(key))
        {
            contextReplacement = meltReplacement != null ? meltReplacement : stackReplacement;
        }
        else if ("tfc.tooltip.fuel_burns_at".equals(key))
        {
            contextReplacement = burnReplacement != null ? burnReplacement : stackReplacement;
        }
        else
        {
            contextReplacement = stackReplacement;
        }

        final Object[] args = tc.getArgs();
        final int length = args.length;
        Object[] newArgs = null;

        for (int j = 0; j < length; j++)
        {
            final Object arg = args[j];
            final Component remapped;
            if (arg instanceof Component argComp)
            {
                if (argComp.getContents() instanceof TranslatableContents argTc
                    && argTc.getKey() != null
                    && argTc.getKey().startsWith("tfc.enum.heat."))
                {
                    remapped = contextReplacement;
                }
                else
                {
                    remapped = remapNestedHeat(argComp, stackReplacement, meltReplacement, burnReplacement);
                }
            }
            else
            {
                remapped = null;
            }

            if (remapped != null && remapped != arg && newArgs == null)
            {
                newArgs = args.clone();
            }
            if (newArgs != null)
            {
                newArgs[j] = remapped != null ? remapped : arg;
            }
        }

        if (newArgs == null) return input;

        final MutableComponent rebuilt = Component.translatable(tc.getKey(), newArgs);
        rebuilt.setStyle(mc.getStyle());
        for (final Component sibling : mc.getSiblings())
        {
            rebuilt.append(remapNestedHeat(sibling, stackReplacement, meltReplacement, burnReplacement));
        }
        return rebuilt;
    }
}
