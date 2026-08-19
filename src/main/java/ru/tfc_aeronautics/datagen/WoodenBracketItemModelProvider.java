package ru.tfc_aeronautics.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import ru.tfc_aeronautics.TFCAeronautics;
import ru.tfc_aeronautics.bracket.WoodenBracketRegistration;

/**
 * One inventory-render model per TFC wood species. Each item model parents on
 * Create's Blockbench-defined {@code create:block/bracket/item} geometry and
 * rebinds {@code bracket} / {@code plate} textures to the matching per-wood
 * PNGs, mirroring {@code create/models/item/wooden_bracket.json}.
 */
public class WoodenBracketItemModelProvider extends ItemModelProvider {

    public WoodenBracketItemModelProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, TFCAeronautics.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerModels() {
        WoodenBracketRegistration.WOODS.forEach(this::registerWood);
    }

    private void registerWood(String wood) {
        ResourceLocation bracketTex = ResourceLocation.fromNamespaceAndPath(
            TFCAeronautics.MOD_ID, "block/wood/bracket/bracket_" + wood);
        ResourceLocation plateTex = ResourceLocation.fromNamespaceAndPath(
            TFCAeronautics.MOD_ID, "block/wood/bracket/bracket_plate_" + wood);
        withExistingParent("wood/bracket/" + wood, "create:block/bracket/item")
            .texture("bracket", bracketTex)
            .texture("plate", plateTex);
    }
}
