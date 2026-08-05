package ru.aeronautics.client.ponder.scenes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

/**
 * Ponder scenes for tfc_aeronautics:heater.
 */
public final class HeaterScenes {

    private HeaterScenes() {}

    public static void intro(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("heater/intro", "Heating molten metal with the Heater");
        scene.configureBasePlate(0, 0, 5);

        BlockPos heaterPos = util.grid().at(2, 1, 2);
        Selection heater = util.select().position(heaterPos);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);
        scene.world().showSection(heater, Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(60)
            .text("The Heater accepts molten metal from above and keeps it hot for downstream use")
            .pointAt(util.vector().topOf(heaterPos));
        scene.idle(80);
    }
}