package ru.tfc_aeronautics.welding_depot;

import net.dries007.tfc.util.Metal;
import net.minecraft.world.level.material.MapColor;

public enum DepotTier {
    WROUGHT_IRON(Metal.WROUGHT_IRON, "wrought_iron", MapColor.METAL, 3),
    STEEL       (Metal.STEEL,        "steel",        MapColor.COLOR_LIGHT_GRAY, 4),
    BLACK_STEEL (Metal.BLACK_STEEL,  "black_steel",  MapColor.COLOR_BLACK, 5),
    BLUE_STEEL  (Metal.BLUE_STEEL,   "blue_steel",   MapColor.COLOR_BLUE,  6),
    RED_STEEL   (Metal.RED_STEEL,    "red_steel",    MapColor.COLOR_RED,   6);

    final Metal metal;
    final String materialSerializedName;
    final MapColor mapColor;
    final int level;

    DepotTier(Metal metal, String materialSerializedName, MapColor mapColor, int level) {
        this.metal = metal;
        this.materialSerializedName = materialSerializedName;
        this.mapColor = mapColor;
        this.level = level;
    }
}
