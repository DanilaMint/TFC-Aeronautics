package ru.tfc_aeronautics.crop;

import net.minecraft.resources.ResourceLocation;

import net.dries007.tfc.util.climate.ClimateRange;
import net.dries007.tfc.util.data.DataManager;

import ru.tfc_aeronautics.TFCAeronautics;

/**
 * Parameters of the Kok-Saghyz crop ({@code Taraxacum kok-saghyz}), a perennial
 * rubber dandelion from the stony foothills of the Tien Shan.
 *
 * <p>The nutrient values follow TFC's convention: they are the fraction of a
 * full farmland nutrient bar the crop takes out of the soil over one growth
 * cycle. They are not raw kilograms per hectare - TFC's own crop numbers do
 * not track real crop removal either (a linear fit scores R^2 ~ 0.02 for
 * nitrogen and ~ 0.15 for phosphorus), they are balance tiers. The derivation
 * from real agronomic data is written up in {@code docs/kok_sagyz_crop.md}.
 *
 * <p>The climate window lives in data rather than in code, exactly like TFC's
 * own crops: {@code data/tfc_aeronautics/tfc/climate_range/crop/kok_sagyz.json}.
 */
public final class KokSagyzCrop
{
    /** Registry id fragment, also the climate range file name. */
    public static final String ID = "kok_sagyz";

    /** Number of growth stages. The block age runs from 0 to {@code STAGES - 1}. */
    public static final int STAGES = 6;

    /** Nitrogen consumed per growth cycle. */
    public static final float NITROGEN = 0.5f;
    /** Phosphorus consumed per growth cycle. */
    public static final float PHOSPHOROUS = 0.3f;
    /** Potassium consumed per growth cycle. */
    public static final float POTASSIUM = 0.4f;

    /**
     * The window in which the crop grows: -15 to 26 degrees Celsius, and 20 to
     * 75 percent soil hydration.
     *
     * <p>A sharply continental steppe plant - it survives hard frosts but stops
     * growing in summer heat and drought, and standing water kills it.
     */
    public static final DataManager.Reference<ClimateRange> CLIMATE =
        ClimateRange.MANAGER.getReference(ResourceLocation.fromNamespaceAndPath(TFCAeronautics.MOD_ID, "crop/" + ID));

    private KokSagyzCrop() {}
}
