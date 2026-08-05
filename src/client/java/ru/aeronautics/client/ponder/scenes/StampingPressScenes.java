package ru.aeronautics.client.ponder.scenes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

/**
 * Ponder scenes for tfc_aeronautics:stamping_press.
 */
public final class StampingPressScenes {

    private StampingPressScenes() {}

    public static void pressing(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("stamping_press/pressing", "Pressing sheets with the Stamping Press");
        scene.configureBasePlate(0, 0, 5);

        BlockPos pressPos = util.grid().at(2, 1, 2);
        Selection press = util.select().position(pressPos);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);
        scene.world().showSection(press, Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(60)
            .text("Power the press from below with Create's kinetic system — the piston presses the tight sheet")
            .pointAt(util.vector().topOf(pressPos));
        scene.idle(80);
    }
}