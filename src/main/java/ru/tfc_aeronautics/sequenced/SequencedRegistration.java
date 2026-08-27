package ru.tfc_aeronautics.sequenced;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;

import ru.tfc_aeronautics.TFCAeronautics;

/**
 * Registers transitional items consumed by {@code create:sequenced_assembly}
 * recipes defined by this mod. Each entry here is a Create
 * {@link SequencedAssemblyItem} carrying a transient progress bar — it
 * shuttles through deployer/press/fill stations before becoming the final
 * output.
 *
 * <p>Currently used by
 * {@code data/tfc_aeronautics/recipe/sequenced_assembly/electron_tube.json}
 * (input: copper sheet → output: {@code create:electron_tube}). Model and
 * placeholder texture live under
 * {@code assets/tfc_aeronautics/models/item/incomplete_electron_tube.json}
 * and {@code textures/item/incomplete_electron_tube.png}.
 */
public final class SequencedRegistration {
    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(TFCAeronautics.MOD_ID);

    public static final DeferredHolder<Item, SequencedAssemblyItem> INCOMPLETE_ELECTRON_TUBE =
        ITEMS.register("incomplete_electron_tube", () -> new SequencedAssemblyItem(new Item.Properties()));

    private SequencedRegistration() {}

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
