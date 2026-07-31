package ru.tfc_aeronautics.forge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.simibubi.create.content.kinetics.fan.EncasedFanBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import ru.tfc_aeronautics.Aeronautics;
import ru.tfc_aeronautics.Config;

import net.dries007.tfc.common.blockentities.CharcoalForgeBlockEntity;
import net.dries007.tfc.common.blocks.devices.CharcoalForgeBlock;

/**
 * Routes airflow from Create's {@link EncasedFanBlockEntity} into TFC's
 * {@link CharcoalForgeBlockEntity} via its public {@code intakeAir(int)} hook.
 *
 * <p>Each server tick, every tracked charcoal forge is checked for an adjacent
 * Encased Fan blowing into one of its four horizontal sides. Air is delivered
 * proportional to the fan's RPM scaled by
 * {@link Config#FAN_FORGE_AIR_PER_TICK}. Forge positions are tracked via
 * {@link BlockEvent.EntityPlaceEvent} / {@link BlockEvent.BreakEvent} and via
 * {@link ChunkEvent.Load} / {@link ChunkEvent.Unload} so reloads and dynamic
 * loads are covered without scanning every chunk every tick.
 */
@EventBusSubscriber(modid = Aeronautics.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class FanForgeIntake {

    private static final Map<Level, Set<BlockPos>> FORGES = new HashMap<>();

    private FanForgeIntake() {}

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        Level level = (Level) event.getLevel();
        if (level.isClientSide()) return;
        if (!(event.getPlacedBlock().getBlock() instanceof CharcoalForgeBlock)) return;
        register(level, event.getPos().immutable());
    }

    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        Level level = (Level) event.getLevel();
        if (level.isClientSide()) return;
        if (!(event.getState().getBlock() instanceof CharcoalForgeBlock)) return;
        unregister(level, event.getPos());
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        Level level = (Level) event.getLevel();
        if (level.isClientSide()) return;
        ChunkAccess chunk = event.getChunk();
        if (!(chunk instanceof LevelChunk levelChunk)) return;
        for (BlockPos pos : levelChunk.getBlockEntitiesPos()) {
            if (levelChunk.getBlockEntity(pos) instanceof CharcoalForgeBlockEntity) {
                register(level, pos.immutable());
            }
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        Level level = (Level) event.getLevel();
        if (level.isClientSide()) return;
        ChunkAccess chunk = event.getChunk();
        if (!(chunk instanceof LevelChunk levelChunk)) return;
        Set<BlockPos> set = FORGES.get(level);
        if (set == null) return;
        for (BlockPos pos : levelChunk.getBlockEntitiesPos()) {
            set.remove(pos);
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(net.neoforged.neoforge.event.level.LevelEvent.Unload event) {
        Level level = (Level) event.getLevel();
        FORGES.remove(level);
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        Level level = (Level) event.getLevel();
        if (level.isClientSide()) return;
        Set<BlockPos> set = FORGES.get(level);
        if (set == null || set.isEmpty()) return;

        double scale = Config.FAN_FORGE_AIR_PER_TICK.get().doubleValue();
        if (scale <= 0.0) return;

        for (BlockPos forgePos : set) {
            if (!level.hasChunkAt(forgePos)) continue;
            BlockEntity be = level.getBlockEntity(forgePos);
            if (!(be instanceof CharcoalForgeBlockEntity forge)) {
                // Forge gone but tracker still holds it — drop it.
                set.remove(forgePos);
                continue;
            }

            int totalAir = 0;
            for (Direction side : Direction.Plane.HORIZONTAL) {
                BlockPos neighbor = forgePos.relative(side);
                if (!(level.getBlockEntity(neighbor) instanceof EncasedFanBlockEntity fan)) continue;
                Direction flow = fan.getAirFlowDirection();
                if (flow != side.getOpposite()) continue;
                float rpm = Math.abs(fan.getSpeed());
                if (rpm <= 0f) continue;
                totalAir += Mth.floor(rpm * scale);
            }
            if (totalAir > 0) {
                forge.intakeAir(totalAir);
            }
        }
    }

    private static void register(Level level, BlockPos pos) {
        FORGES.computeIfAbsent(level, k -> new HashSet<>()).add(pos);
    }

    private static void unregister(Level level, BlockPos pos) {
        Set<BlockPos> set = FORGES.get(level);
        if (set != null) set.remove(pos);
    }

    // Visible for tests / debug
    static int trackedForges(Level level) {
        Set<BlockPos> set = FORGES.get(level);
        return set == null ? 0 : set.size();
    }
}