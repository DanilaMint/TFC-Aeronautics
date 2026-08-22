package ru.tfc_aeronautics.heat;

import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.dries007.tfc.common.blockentities.AbstractFirepitBlockEntity;

import ru.tfc_aeronautics.heater.HeaterBlockEntity;

import java.util.function.ToDoubleFunction;

/**
 * Built-in {@link HeatDealer} implementations and conversions between
 * TFC's celsius heat scale and Create's blaze-burner / boiler heat scales.
 */
public final class HeatDealers {

    // TFC's Heat scale bands: WARMING starts at 80 C, HOT at 210, VERY_HOT at 480, etc.
    // The basin thresholds below repeat the BoilerHeaters#blazeBurner gradation so a
    // HeatDealer of equivalent brightness shows the same flame tier.
    private static final float THRESHOLD_WARMING = 80f;
    private static final float THRESHOLD_HOT = 400f;
    private static final float THRESHOLD_FAINT_RED = 800f;
    private static final float THRESHOLD_WHITE = 1400f;

    // Steam engine SU contribution per heater. The boiler sums these into BoilerData.activeHeat
    // (truncating the float to int), so each band is 200 C wide starting at WARMING and tops out
    // at 7 SU. Multiple heaters stack up to BoilerData#maxHeatForSize = min(18, boilerSize / 4).
    private static final float BOILER_BAND_WIDTH = 200f;
    private static final float BOILER_MAX_SU = 7f;

    public static final HeatDealer FIREPIT = fromBlockEntity(
        AbstractFirepitBlockEntity.class, AbstractFirepitBlockEntity::getTemperature);

    public static final HeatDealer HEATER = fromBlockEntity(
        HeaterBlockEntity.class, HeaterBlockEntity::getTemperature);

    private HeatDealers() {}

    private static <T extends BlockEntity> HeatDealer fromBlockEntity(Class<T> type, ToDoubleFunction<T> temperature) {
        return (level, pos, state) -> {
            BlockEntity be = level.getBlockEntity(pos);
            if (!type.isInstance(be)) return HeatDealer.NO_HEAT;
            float temp = (float) temperature.applyAsDouble(type.cast(be));
            // Treat anything <= 0 as cold; cooling devices still above ambient continue to report their actual
            // temperature so downstream consumers see residual heat until they truly reach zero.
            return temp > 0f ? temp : HeatDealer.NO_HEAT;
        };
    }

    public static BlazeBurnerBlock.HeatLevel toHeatLevel(float celsius) {
        if (celsius < THRESHOLD_WARMING) return BlazeBurnerBlock.HeatLevel.NONE;
        if (celsius < THRESHOLD_HOT) return BlazeBurnerBlock.HeatLevel.SMOULDERING;
        if (celsius < THRESHOLD_FAINT_RED) return BlazeBurnerBlock.HeatLevel.FADING;
        if (celsius < THRESHOLD_WHITE) return BlazeBurnerBlock.HeatLevel.KINDLED;
        return BlazeBurnerBlock.HeatLevel.SEETHING;
    }

    public static float toBoilerHeat(float celsius) {
        if (celsius < THRESHOLD_WARMING) return BoilerHeater.NO_HEAT;
        return Math.min(BOILER_MAX_SU, (float) Math.floor((celsius - THRESHOLD_WARMING) / BOILER_BAND_WIDTH));
    }

    public static float boilerAdapter(Level level, BlockPos pos, BlockState state) {
        return toBoilerHeat(HeatDealer.findTemperature(level, pos, state));
    }
}
