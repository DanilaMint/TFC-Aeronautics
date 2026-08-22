package ru.tfc_aeronautics.chain;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Network registration entry point for chain-conveyor packets.
 *
 * <p>Each payload is registered under the {@code tfc_aeronautics} namespace via the
 * NeoForge {@link RegisterPayloadHandlersEvent}. Codec lookup on the wire uses
 * {@link CustomPacketPayload#type()}, so the handlers here drive {@code type().id()}
 * which the client matches by reading the resource location off the buffer.
 */
public final class ChainConveyorPackets {

	private static final String PROTOCOL_VERSION = "1";

	private ChainConveyorPackets() {}

	@SubscribeEvent
	public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

		registrar.playToServer(
				ChainConveyorConnectionPacket.TYPE,
				ChainConveyorConnectionPacket.STREAM_CODEC,
				(payload, ctx) -> payload.handle(asServerPlayer(ctx))
		);

		registrar.playToServer(
				ChainPackageInteractionPacket.TYPE,
				ChainPackageInteractionPacket.STREAM_CODEC,
				(payload, ctx) -> payload.handle(asServerPlayer(ctx))
		);

		registrar.playToServer(
				ServerboundChainConveyorRidingPacket.TYPE,
				ServerboundChainConveyorRidingPacket.STREAM_CODEC,
				(payload, ctx) -> payload.handle(asServerPlayer(ctx))
		);

		registrar.playToClient(
				ClientboundChainConveyorRidingPacket.TYPE,
				ClientboundChainConveyorRidingPacket.STREAM_CODEC,
				(payload, ctx) -> payload.handle(ctx.player() instanceof net.minecraft.client.player.LocalPlayer lp ? lp : null)
		);
	}

	private static net.minecraft.server.level.ServerPlayer asServerPlayer(IPayloadContext ctx) {
		return ctx.player() instanceof net.minecraft.server.level.ServerPlayer sp ? sp : null;
	}

	public static void register(IEventBus modBus) {
		modBus.register(ChainConveyorPackets.class);
	}
}