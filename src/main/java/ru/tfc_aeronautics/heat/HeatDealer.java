package ru.tfc_aeronautics.heat;

import com.simibubi.create.api.registry.SimpleRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A block that can supply heat to other devices. HeatDealers are the TFC counterpart of
 * Create's {@link com.simibubi.create.api.boiler.BoilerHeater}: any block registered in
 * {@link #REGISTRY} can be queried for its current temperature in degrees Celsius using
 * {@link #findTemperature(Level, BlockPos, BlockState)}. The returned temperature follows
 * TFC's {@code Heat} scale; {@link #NO_HEAT} is returned when the block is not currently
 * providing heat (e.g. an extinguished firepit or a heater with no fuel).
 */
@FunctionalInterface
public interface HeatDealer {

    float NO_HEAT = -1f;

    SimpleRegistry<Block, HeatDealer> REGISTRY = SimpleRegistry.create();

    float getTemperature(Level level, BlockPos pos, BlockState state);

    static float findTemperature(Level level, BlockPos pos, BlockState state) {
        HeatDealer dealer = REGISTRY.get(state);
        return dealer != null ? dealer.getTemperature(level, pos, state) : NO_HEAT;
    }

    static float findTemperature(Level level, BlockPos pos) {
        return findTemperature(level, pos, level.getBlockState(pos));
    }

    static boolean isHeatDealer(BlockState state) {
        return REGISTRY.get(state) != null;
    }
}
