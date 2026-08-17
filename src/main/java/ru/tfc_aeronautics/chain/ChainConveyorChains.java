package ru.tfc_aeronautics.chain;

import java.util.List;

import com.google.common.cache.Cache;
import com.simibubi.create.foundation.utility.TickBasedCache;

import net.createmod.catnip.data.WorldAttached;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Holds the client-side world-attached cache of {@link ChainConveyorShape} entries
 * for every loaded chain conveyor. Lives in {@code main} because
 * {@link ChainConveyorBlockEntity} needs to invalidate entries from server-driven
 * {@code updateChainShapes()} calls and invalidate-on-remove; the cache itself
 * is only populated and consumed by client code (rendering + interaction).
 */
public final class ChainConveyorChains {

	public static final WorldAttached<Cache<BlockPos, List<ChainConveyorShape>>> LOADED =
		new WorldAttached<>($ -> new TickBasedCache<>(60, true));

	private ChainConveyorChains() {}
}