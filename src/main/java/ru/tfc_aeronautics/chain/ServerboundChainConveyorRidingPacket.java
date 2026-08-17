package ru.tfc_aeronautics.chain;

import com.simibubi.create.infrastructure.config.AllConfigs;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import ru.tfc_aeronautics.TFCAeronautics;

public record ServerboundChainConveyorRidingPacket(BlockPos pos, boolean stop) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<ServerboundChainConveyorRidingPacket> TYPE =
			new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(TFCAeronautics.MOD_ID, "chain_conveyor_riding"));

	public static final StreamCodec<ByteBuf, ServerboundChainConveyorRidingPacket> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, ServerboundChainConveyorRidingPacket::pos,
			ByteBufCodecs.BOOL, ServerboundChainConveyorRidingPacket::stop,
			ServerboundChainConveyorRidingPacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public void handle(ServerPlayer sender) {
		int maxRange = AllConfigs.server().kinetics.maxChainConveyorLength.get() * 2;
		if (sender == null || sender.isSpectator())
			return;
		if (!sender.level().isLoaded(pos))
			return;
		if (!sender.canInteractWithBlock(pos, maxRange))
			return;

		sender.fallDistance = 0;
		// Parchment 1.21.1 collapses ServerPlayer.connection to the ServerPlayerConnection
		// interface, which has no aboveGroundTickCount field. The concrete impl
		// (ServerGamePacketListenerImpl) still owns these counters; the project's
		// accesstransformer.cfg opens them up so we can reset them here, matching
		// Create's verbatim behaviour (no fall-damage accrual while riding).
		ServerGamePacketListenerImpl connection = (ServerGamePacketListenerImpl) sender.connection;
		connection.aboveGroundTickCount = 0;
		connection.aboveGroundVehicleTickCount = 0;

		if (stop)
			ServerChainConveyorHandler.handleStopRidingPacket(sender);
		else
			ServerChainConveyorHandler.handleTTLPacket(sender);
	}
}