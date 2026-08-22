package ru.tfc_aeronautics.chain;

import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.logistics.packagePort.PackagePortTargetType;

import net.minecraft.core.Holder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.tfc_aeronautics.TFCAeronautics;

public class ChainConveyorPackagePortTargets {
	private static final DeferredRegister<PackagePortTargetType> REGISTER =
		DeferredRegister.create(CreateRegistries.PACKAGE_PORT_TARGET_TYPE, TFCAeronautics.MOD_ID);

	public static final Holder<PackagePortTargetType> CHAIN_CONVEYOR_TARGET =
		REGISTER.register("tfc_chain_conveyor", TfcChainConveyorFrogportTarget.Type::new);

	public static void register(IEventBus eventBus) {
		REGISTER.register(eventBus);
	}
}