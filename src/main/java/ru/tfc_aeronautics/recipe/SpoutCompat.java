package ru.tfc_aeronautics.recipe;

import com.simibubi.create.api.behaviour.spouting.BlockSpoutingBehaviour;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import ru.tfc_aeronautics.Aeronautics;

/**
 * Wires Create's spout API to TFC's mold table: while a spout is placed above a
 * mold table, its {@link BlockSpoutingBehaviour} drains the recipe's fluid amount
 * from the spout's tank and executes the matching {@code tfc:casting} recipe.
 *
 * Registration runs in {@link FMLCommonSetupEvent#enqueueWork} because both
 * {@code BlockSpoutingBehaviour.BY_BLOCK_ENTITY} and TFC's block entity registry
 * must be live — TFC registers {@code mold_table} during its own common setup.
 */
@EventBusSubscriber(modid = Aeronautics.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class SpoutCompat
{
    private SpoutCompat() {}

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event)
    {
        event.enqueueWork(() ->
            BlockSpoutingBehaviour.BY_BLOCK_ENTITY.register(
                TFCBlockEntities.MOLD_TABLE.get(),
                SpoutCastingBehavior.INSTANCE
            )
        );
    }
}