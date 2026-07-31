package ru.tfc_aeronautics.metal;

import java.util.function.Supplier;

import net.minecraft.world.level.material.Fluid;

import net.dries007.tfc.common.fluids.TFCFluids;
import net.dries007.tfc.util.Metal;

/**
 * Registry of "tight sheet" metal items the aeronautics mod adds.
 *
 * <p>A tight sheet is a thin, densely-pressed sheet used in aeronautics
 * applications (e.g. airship envelopes) where airtightness matters. It uses
 * less material than a regular {@code tfc:metal/sheet/<metal>} (100 mB on melt
 * vs 200 mB for a standard sheet).
 *
 * <p>Each entry exposes:
 * <ul>
 *   <li>{@link #id()} — the registry id segment, e.g. {@code copper}
 *   <li>{@link #meltTemperature()} — the metal's melting point (mirrors the
 *       values in TFC's {@code data/tfc/tfc/fluid_heat/<metal>.json})
 *   <li>{@link #outputFluid()} — lazy supplier for the molten fluid used as
 *       the heating recipe result. TFC's static {@code TFCFluids.METALS} map
 *       is resolved on first access, mirroring the {@link
 *       ru.tfc_aeronautics.powder.MetalPowder} pattern.
 * </ul>
 */
public enum TightSheet
{
    COPPER("copper", 1080, () -> TFCFluids.METALS.get(Metal.COPPER).getSource()),
    WROUGHT_IRON("wrought_iron", 1535, () -> TFCFluids.METALS.get(Metal.WROUGHT_IRON).getSource()),
    STEEL("steel", 1540, () -> TFCFluids.METALS.get(Metal.STEEL).getSource());

    public static final String ITEM_ID_PREFIX = "metal/tight_sheet/";

    private final String id;
    private final float meltTemperature;
    private final Supplier<? extends Fluid> outputFluid;

    TightSheet(String id, float meltTemperature, Supplier<? extends Fluid> outputFluid)
    {
        this.id = id;
        this.meltTemperature = meltTemperature;
        this.outputFluid = outputFluid;
    }

    public String id() { return id; }

    public float meltTemperature() { return meltTemperature; }

    public Supplier<? extends Fluid> outputFluid() { return outputFluid; }

    /** Registry id segment for the tight-sheet item, e.g. {@code metal/tight_sheet/copper}. */
    public String itemId() { return ITEM_ID_PREFIX + id; }

    /** Filename for the corresponding TFC item_heat JSON, e.g. {@code copper_tight_sheet}. */
    public String itemHeatFileName() { return id + "_tight_sheet"; }
}