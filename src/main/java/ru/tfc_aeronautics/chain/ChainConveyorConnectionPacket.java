package ru.tfc_aeronautics.chain;

import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import ru.tfc_aeronautics.TFCAeronautics;

public record ChainConveyorConnectionPacket(BlockPos pos, BlockPos targetPos, ItemStack chain, boolean connect)
		implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<ChainConveyorConnectionPacket> TYPE =
			new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(TFCAeronautics.MOD_ID, "chain_conveyor_connect"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ChainConveyorConnectionPacket> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, ChainConveyorConnectionPacket::pos,
			BlockPos.STREAM_CODEC, ChainConveyorConnectionPacket::targetPos,
			ItemStack.STREAM_CODEC, ChainConveyorConnectionPacket::chain,
			ByteBufCodecs.BOOL, ChainConveyorConnectionPacket::connect,
			ChainConveyorConnectionPacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public void handle(ServerPlayer player) {
		int maxRange = AllConfigs.server().kinetics.maxChainConveyorLength.get() + 16;
		if (player == null || player.isSpectator())
			return;
		if (!player.level().isLoaded(pos))
			return;
		if (!player.canInteractWithBlock(pos, maxRange))
			return;
		if (!(player.level().getBlockEntity(pos) instanceof ChainConveyorBlockEntity be))
			return;
		if (!be.getBlockPos().closerThan(targetPos, maxRange - 16 + 1))
			return;
		if (!(be.getLevel().getBlockEntity(targetPos) instanceof ChainConveyorBlockEntity clbe))
			return;

		if (connect && !player.isCreative()) {
			int chainCost = ChainConveyorBlockEntity.getChainCost(targetPos.subtract(be.getBlockPos()));
			boolean hasEnough = ChainConveyorBlockEntity.getChainsFromInventory(player, chain, chainCost, true);
			if (!hasEnough)
				return;
			ChainConveyorBlockEntity.getChainsFromInventory(player, chain, chainCost, false);
		}

		if (!connect) {
			if (!player.isCreative()) {
				int chainCost = ChainConveyorBlockEntity.getChainCost(targetPos.subtract(pos));
				Item refundItem = clbe.getChainItemForConnection(be.getBlockPos().subtract(targetPos));
				while (chainCost > 0) {
					player.getInventory()
							.placeItemBackInInventory(new ItemStack(refundItem, Math.min(chainCost, 64)));
					chainCost -= 64;
				}
			}
			be.chainDestroyed(targetPos.subtract(be.getBlockPos()), false, true);
			be.getLevel()
					.playSound(null, player.blockPosition(), SoundEvents.CHAIN_BREAK, SoundSource.BLOCKS);
		}

		ResourceLocation chainId = chain.getItem().builtInRegistryHolder().key().location();

		if (connect) {
			if (!clbe.addConnectionTo(be.getBlockPos(), chainId))
				return;
		} else
			clbe.removeConnectionTo(be.getBlockPos());

		if (connect) {
			if (!be.addConnectionTo(targetPos, chainId))
				clbe.removeConnectionTo(be.getBlockPos());
		} else
			be.removeConnectionTo(targetPos);

		be.sendData();
		clbe.sendData();
		be.setChanged();
	}
}