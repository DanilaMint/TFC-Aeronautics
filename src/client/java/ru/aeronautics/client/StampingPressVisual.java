package ru.aeronautics.client;

import java.util.function.Consumer;

import org.joml.Quaternionf;

import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.ShaftVisual;
import com.simibubi.create.content.kinetics.press.PressingBehaviour;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.OrientedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.createmod.catnip.math.AngleHelper;

import ru.tfc_aeronautics.press.StampingPressBlock;
import ru.tfc_aeronautics.press.StampingPressBlockEntity;

/**
 * Flywheel visual for the {@link StampingPressBlockEntity}. Without this, the
 * shaft and head are invisible on any client running Flywheel (which is most
 * setups including the dev modpack) — {@code KineticBlockEntityRenderer} short-
 * circuits when {@code VisualizationManager.supportsVisualization} returns
 * true, and expects Flywheel instances to take over.
 *
 * <p>Mirrors {@code com.simibubi.create.content.kinetics.press.PressVisual}:
 * the {@code ShaftVisual} parent handles the rotating shaft model, this class
 * adds the {@code MECHANICAL_PRESS_HEAD} oriented instance on top and animates
 * it via {@link PressingBehaviour#getRenderedHeadOffset(float)} so the head
 * strikes down when the press runs.
 */
public class StampingPressVisual extends ShaftVisual<StampingPressBlockEntity> implements SimpleDynamicVisual {

    private final OrientedInstance pressHead;

    public StampingPressVisual(VisualizationContext context, StampingPressBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        pressHead = instancerProvider().instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.MECHANICAL_PRESS_HEAD))
            .createInstance();

        Quaternionf q = Axis.YP
            .rotationDegrees(AngleHelper.horizontalAngle(blockState.getValue(StampingPressBlock.HORIZONTAL_FACING)));
        pressHead.rotation(q);

        transformModels(partialTick);
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        transformModels(ctx.partialTick());
    }

    private void transformModels(float pt) {
        float renderedHeadOffset = getRenderedHeadOffset(pt);

        pressHead.position(getVisualPosition())
            .translatePosition(0, -renderedHeadOffset, 0)
            .setChanged();
    }

    private float getRenderedHeadOffset(float pt) {
        PressingBehaviour pressingBehaviour = blockEntity.getPressingBehaviour();
        return pressingBehaviour.getRenderedHeadOffset(pt)
            * pressingBehaviour.mode.headOffset;
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);
        relight(pressHead);
    }

    @Override
    protected void _delete() {
        super._delete();
        pressHead.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        consumer.accept(pressHead);
    }
}
