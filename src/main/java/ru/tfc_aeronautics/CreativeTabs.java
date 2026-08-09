package ru.tfc_aeronautics;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import ru.tfc_aeronautics.burlap.BurlapRegistration;
import ru.tfc_aeronautics.composite.CompositeRegistration;
import ru.tfc_aeronautics.fluid.AeronauticsFluidItems;
import ru.tfc_aeronautics.heater.HeaterRegistration;
import ru.tfc_aeronautics.metal.TightSheetRegistration;
import ru.tfc_aeronautics.powder.MetalPowder;
import ru.tfc_aeronautics.powder.PowderRegistration;
import ru.tfc_aeronautics.resin.ResinRegistration;
import ru.tfc_aeronautics.stamping_press.StampingPressRegistration;

/**
 * Single creative tab for the mod, exposing every item/block this mod adds.
 * Translation key: {@code itemGroup.tfc_aeronautics}.
 */
public final class CreativeTabs
{
    public static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Aeronautics.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.tfc_aeronautics"))
            .icon(() -> new ItemStack(PowderRegistration.POWDERS.get(MetalPowder.NICKEL).get()))
            .displayItems((params, output) -> {
                PowderRegistration.POWDERS.values().forEach(p -> output.accept(p.get()));
                output.accept(CompositeRegistration.DRY_COMPOSITE.get());
                output.accept(CompositeRegistration.COMPOSITE.get());
                TightSheetRegistration.TIGHT_SHEETS.values().forEach(s -> output.accept(s.get()));
                output.accept(HeaterRegistration.HEATER_ITEM.get());
                output.accept(StampingPressRegistration.STAMPING_PRESS_ITEM.get());
                output.accept(ResinRegistration.RESIN_CLUMP.get());
                output.accept(BurlapRegistration.IMPREGNATED_BURLAP_CLOTH.get());
                output.accept(AeronauticsFluidItems.MOLTEN_MAGMATITE_BUCKET.get());
                output.accept(AeronauticsFluidItems.ROSIN_BUCKET.get());
            })
            .build());

    private CreativeTabs() {}

    public static void register(IEventBus bus)
    {
        TABS.register(bus);
    }
}
