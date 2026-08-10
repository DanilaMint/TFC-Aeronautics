package ru.tfc_aeronautics.powder;

import java.util.function.Supplier;
import net.minecraft.world.level.material.Fluid;

import net.dries007.tfc.common.fluids.TFCFluids;
import net.dries007.tfc.util.Metal;

/**
 * Registry of metal powders the aeronautics mod adds.
 * Each entry's {@link #outputFluid()} is a lazy supplier — TFC's {@code TFCFluids.METALS}
 * map is only resolved when the supplier is invoked, so enum class loading does not
 * force initialization of TFC's static state.
 */
public enum MetalPowder
{
    COPPER("copper", 1080, () -> TFCFluids.METALS.get(Metal.COPPER).getSource()),
    TIN("tin", 230, () -> TFCFluids.METALS.get(Metal.TIN).getSource()),
    ZINC("zinc", 420, () -> TFCFluids.METALS.get(Metal.ZINC).getSource()),
    BISMUTH("bismuth", 270, () -> TFCFluids.METALS.get(Metal.BISMUTH).getSource()),
    CAST_IRON("cast_iron", 1535, () -> TFCFluids.METALS.get(Metal.CAST_IRON).getSource()),
    GOLD("gold", 1060, () -> TFCFluids.METALS.get(Metal.GOLD).getSource()),
    SILVER("silver", 970, () -> TFCFluids.METALS.get(Metal.SILVER).getSource()),
    NICKEL("nickel", 1450, () -> TFCFluids.METALS.get(Metal.NICKEL).getSource());

    public static final String ITEM_ID_PREFIX = "powder/";

    private final String id;
    private final float meltTemperature;
    private final Supplier<? extends Fluid> outputFluid;

    MetalPowder(String id, float meltTemperature, Supplier<? extends Fluid> outputFluid)
    {
        this.id = id;
        this.meltTemperature = meltTemperature;
        this.outputFluid = outputFluid;
    }

    public String id() { return id; }

    public float meltTemperature() { return meltTemperature; }

    public Supplier<? extends Fluid> outputFluid() { return outputFluid; }

    /** Registry id segment for the powder item, e.g. {@code powder/copper}. */
    public String itemId() { return ITEM_ID_PREFIX + id; }

    /** Filename for the corresponding heating recipe JSON, e.g. {@code copper_powder}. */
    public String recipeFileName() { return id + "_powder"; }
}