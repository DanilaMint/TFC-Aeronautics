package ru.aeronautics.client.ponder.scenes;

import net.minecraft.core.Direction;

import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

/**
 * Empty template for new ponder scenes. Copy as a starting point.
 *
 * The method signature
 *   public static void name(SceneBuilder builder, SceneBuildingUtil util)
 * is what addStoryBoard(...) registers; see PonderScenes for examples.
 */
public final class TemplateScenes {

    private TemplateScenes() {}

    public static void template(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("template", "Template Scene Title");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);
        scene.world().showSection(util.select().layersFrom(1), Direction.DOWN);
    }
}