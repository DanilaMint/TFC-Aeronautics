package ru.tfc_aeronautics.basin;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.api.event.BlockEntityBehaviourEvent;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import ru.tfc_aeronautics.TFCAeronautics;

/**
 * Attaches {@link BasinBarrelInstantBehaviour} to every Create basin, so that the basin can run
 * TFC's {@code tfc:barrel_instant} recipes.
 *
 * <p>Uses Create's public {@code BlockEntityBehaviourEvent} API instead of a mixin: the event is
 * posted once per block entity instance (on the first NBT read, or on the first tick for a freshly
 * placed block), and the behaviour it attaches is ticked and initialized like any built-in one.
 */
@EventBusSubscriber(modid = TFCAeronautics.MOD_ID)
public final class BasinBarrelInstantCompat
{
    private BasinBarrelInstantCompat() {}

    @SubscribeEvent
    public static void onBlockEntityBehaviour(BlockEntityBehaviourEvent event)
    {
        event.forType(AllBlockEntityTypes.BASIN.get(), be -> event.attach(new BasinBarrelInstantBehaviour(be)));
    }
}
