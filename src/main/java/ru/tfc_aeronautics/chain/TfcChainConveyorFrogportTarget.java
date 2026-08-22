package ru.tfc_aeronautics.chain;

import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.simibubi.create.content.logistics.packagePort.PackagePortTarget;
import com.simibubi.create.content.logistics.packagePort.PackagePortTargetType;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import ru.tfc_aeronautics.chain.ChainConveyorBlockEntity.ConnectedPort;
import ru.tfc_aeronautics.chain.ChainConveyorBlockEntity.ConnectionStats;

public class TfcChainConveyorFrogportTarget extends PackagePortTarget {
	public static final MapCodec<TfcChainConveyorFrogportTarget> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		BlockPos.CODEC.fieldOf("relative_pos").forGetter(i -> i.relativePos),
		Codec.FLOAT.fieldOf("chain_pos").forGetter(i -> i.chainPos),
		BlockPos.CODEC.optionalFieldOf("connection").forGetter(i -> Optional.ofNullable(i.connection)),
		Codec.BOOL.fieldOf("flipped").forGetter(i -> i.flipped)
	).apply(instance, TfcChainConveyorFrogportTarget::new));

	public static final StreamCodec<ByteBuf, TfcChainConveyorFrogportTarget> STREAM_CODEC = StreamCodec.composite(
	    BlockPos.STREAM_CODEC, i -> i.relativePos,
		ByteBufCodecs.FLOAT, i -> i.chainPos,
		CatnipStreamCodecBuilders.nullable(BlockPos.STREAM_CODEC), i -> i.connection,
		ByteBufCodecs.BOOL, i -> i.flipped,
	    TfcChainConveyorFrogportTarget::new
	);

	public float chainPos;
	@Nullable
	public BlockPos connection;
	public boolean flipped;

	public TfcChainConveyorFrogportTarget(BlockPos relativePos, float chainPos, Optional<BlockPos> connection, boolean flipped) {
		super(relativePos);
		this.chainPos = chainPos;
		this.connection = connection.orElse(null);
		this.flipped = flipped;
	}

	public TfcChainConveyorFrogportTarget(BlockPos relativePos, float chainPos, @Nullable BlockPos connection, boolean flipped) {
		this(relativePos, chainPos, Optional.ofNullable(connection), flipped);
	}

	@Override
	public void setup(PackagePortBlockEntity ppbe, LevelAccessor level, BlockPos portPos) {
		if (be(level, portPos) instanceof ChainConveyorBlockEntity clbe)
			flipped = clbe.getSpeed() < 0;
	}

	@Override
	public boolean export(LevelAccessor level, BlockPos portPos, ItemStack box, boolean simulate) {
		if (!(be(level, portPos) instanceof ChainConveyorBlockEntity clbe))
			return false;
		if (connection != null && !clbe.connections.contains(connection))
			return false;
		if (simulate)
			return clbe.getSpeed() != 0 && clbe.canAcceptPackagesFor(connection);
		ChainConveyorPackage box2 = new ChainConveyorPackage(chainPos, box.copy());
		if (connection == null)
			return clbe.addLoopingPackage(box2);
		return clbe.addTravellingPackage(box2, connection);
	}

	@Override
	public void register(PackagePortBlockEntity ppbe, LevelAccessor level, BlockPos portPos) {
		if (!(be(level, portPos) instanceof ChainConveyorBlockEntity clbe))
			return;
		ChainConveyorBlockEntity actualBe = clbe;

		if (connection != null && clbe.getSpeed() < 0 != flipped) {
			deregister(ppbe, level, portPos);
			actualBe = (ChainConveyorBlockEntity) level.getBlockEntity(clbe.getBlockPos()
				.offset(connection));
			if (actualBe == null)
				return;
			clbe.prepareStats();
			ConnectionStats stats = clbe.connectionStats.get(connection);
			if (stats != null)
				chainPos = stats.chainLength() - chainPos;
			connection = connection.multiply(-1);
			flipped = !flipped;
			relativePos = actualBe.getBlockPos()
				.subtract(portPos);
			ppbe.notifyUpdate();
		}

		if (connection != null && !actualBe.connections.contains(connection))
			return;
		String portFilter = ppbe.getFilterString();
		if (portFilter == null)
			return;
		actualBe.routingTable.receivePortInfo(portFilter, connection == null ? BlockPos.ZERO : connection);
		Map<BlockPos, ConnectedPort> portMap = connection == null ? actualBe.loopPorts : actualBe.travelPorts;
		portMap.put(relativePos.multiply(-1), new ConnectedPort(chainPos, connection, portFilter));
	}

	@Override
	public void deregister(PackagePortBlockEntity ppbe, LevelAccessor level, BlockPos portPos) {
		if (!(be(level, portPos) instanceof ChainConveyorBlockEntity clbe))
			return;
		clbe.loopPorts.remove(relativePos.multiply(-1));
		clbe.travelPorts.remove(relativePos.multiply(-1));
		String portFilter = ppbe.getFilterString();
		if (portFilter == null)
			return;
		clbe.routingTable.entriesByDistance.removeIf(e -> e.endOfRoute() && e.port()
			.equals(portFilter));
		clbe.routingTable.changed = true;
	}

	@Override
	public Vec3 getExactTargetLocation(PackagePortBlockEntity ppbe, LevelAccessor level, BlockPos portPos) {
		if (!(be(level, portPos) instanceof ChainConveyorBlockEntity clbe))
			return Vec3.ZERO;
		return clbe.getPackagePosition(chainPos, connection);
	}

	@Override
	public boolean canSupport(BlockEntity be) {
		return AllBlockEntityTypes.PACKAGE_FROGPORT.is(be);
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(ChainConveyorRegistration.CHAIN_CONVEYOR.get());
	}

	@Override
	protected PackagePortTargetType getType() {
		return ChainConveyorPackagePortTargets.CHAIN_CONVEYOR_TARGET.value();
	}

	public static class Type implements PackagePortTargetType {
		@Override
		public MapCodec<TfcChainConveyorFrogportTarget> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, TfcChainConveyorFrogportTarget> streamCodec() {
			return STREAM_CODEC;
		}
	}
}