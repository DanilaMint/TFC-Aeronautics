package ru.tfc_aeronautics.fluid;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import net.dries007.tfc.common.fluids.FluidHolder;
import net.dries007.tfc.common.fluids.MixingFluid;
import net.dries007.tfc.common.fluids.MoltenFluid;
import net.dries007.tfc.util.registry.RegistrationHelpers;

import ru.tfc_aeronautics.Aeronautics;

/**
 * Registers the molten magmatite fluid, mirroring TFC's
 * {@code TFCFluids.METALS} pattern using TFC's own {@link MoltenFluid} Source/Flowing
 * classes, {@link net.dries007.tfc.common.blocks.MoltenFluidBlock MoltenFluidBlock},
 * and {@link RegistrationHelpers#registerFluid}.
 *
 * <p>FluidType properties are copied verbatim from TFC's {@code lavaLike()} so the
 * magmatite behaves identically to TFC's molten metals.</p>
 */
public final class AeronauticsFluids
{
    public static final DeferredRegister<FluidType> FLUID_TYPES =
        DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, Aeronautics.MOD_ID);
    public static final DeferredRegister<net.minecraft.world.level.material.Fluid> FLUIDS =
        DeferredRegister.create(Registries.FLUID, Aeronautics.MOD_ID);

    public static final FluidHolder<BaseFlowingFluid> MOLTEN_MAGMATITE =
        RegistrationHelpers.registerFluid(
            FLUID_TYPES, FLUIDS,
            "molten_magmatite",
            "molten_magmatite",
            "flowing_molten_magmatite",
            properties -> properties
                .block(AeronauticsFluidBlocks.MOLTEN_MAGMATITE)
                .bucket(AeronauticsFluidItems.MOLTEN_MAGMATITE_BUCKET)
                .explosionResistance(100),
            () -> new FluidType(lavaLike().descriptionId("fluid.tfc_aeronautics.molten_magmatite")),
            MoltenFluid.Source::new,
            MoltenFluid.Flowing::new);

    public static final FluidHolder<BaseFlowingFluid> ROSIN =
        RegistrationHelpers.registerFluid(
            FLUID_TYPES, FLUIDS,
            "rosin",
            "rosin",
            "flowing_rosin",
            properties -> properties
                .block(AeronauticsFluidBlocks.ROSIN)
                .bucket(AeronauticsFluidItems.ROSIN_BUCKET),
            () -> new FluidType(waterLikeRosin().descriptionId("fluid.tfc_aeronautics.rosin")),
            MixingFluid.Source::new,
            MixingFluid.Flowing::new);

    private AeronauticsFluids() {}

    /** Lava-like FluidType properties copied from {@code TFCFluids.lavaLike()}. */
    private static FluidType.Properties lavaLike()
    {
        return FluidType.Properties.create()
            .adjacentPathType(PathType.LAVA)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
            .lightLevel(15)
            .density(3000)
            .viscosity(6000)
            .temperature(1300)
            .canConvertToSource(false)
            .canDrown(false)
            .canExtinguish(false)
            .canHydrate(false)
            .canPushEntity(false)
            .canSwim(false)
            .supportsBoating(false)
            .fallDistanceModifier(0);
    }

    /** Water-like FluidType properties copied from {@code TFCFluids.waterLike()} for rosin. */
    private static FluidType.Properties waterLikeRosin()
    {
        return FluidType.Properties.create()
            .adjacentPathType(PathType.WATER)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
            .canConvertToSource(true)
            .canDrown(true)
            .canExtinguish(true)
            .canHydrate(true)
            .canPushEntity(true)
            .canSwim(true)
            .supportsBoating(true)
            .fallDistanceModifier(0);
    }

    public static void register(IEventBus bus)
    {
        FLUID_TYPES.register(bus);
        FLUIDS.register(bus);
    }
}