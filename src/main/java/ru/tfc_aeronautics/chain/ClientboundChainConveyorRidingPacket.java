package ru.tfc_aeronautics.chain;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import com.simibubi.create.foundation.render.PlayerSkyhookRenderer;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import ru.tfc_aeronautics.TFCAeronautics;

public record ClientboundChainConveyorRidingPacket(Collection<UUID> uuids) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<ClientboundChainConveyorRidingPacket> TYPE =
			new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(TFCAeronautics.MOD_ID, "clientbound_chain_conveyor"));

	public static final StreamCodec<ByteBuf, ClientboundChainConveyorRidingPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.collection(HashSet::new, UUIDUtil.STREAM_CODEC), ClientboundChainConveyorRidingPacket::uuids,
			ClientboundChainConveyorRidingPacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public void handle(LocalPlayer player) {
		PlayerSkyhookRenderer.updatePlayerList(this.uuids);
	}
}