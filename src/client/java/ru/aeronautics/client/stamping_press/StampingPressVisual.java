package ru.aeronautics.client.stamping_press;

import java.util.function.Consumer;

import org.joml.Quaternionf;

import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.simibubi.create.content.kinetics.press.PressingBehaviour;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.OrientedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import ru.tfc_aeronautics.stamping_press.StampingPressBlockEntity;

/**
 * Flywheel visual for the stamping press. Draws the rotating shaft (inherited
 * from {@link SingleAxisRotatingVisual}) plus a moving press head that bobs up
 * and down as the press cycles.
 *
 * <p>Mirrors {@code PressVisual} from Create: the head sits above the block and
 * translates down by {@code renderedHeadOffset} on each press cycle.
 */
public class StampingPressVisual extends SingleAxisRotatingVisual<StampingPressBlockEntity>
    implements SimpleDynamicVisual {

    private final OrientedInstance pressHead;

    public StampingPressVisual(VisualizationContext context, StampingPressBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick,
            Models.partial(com.simibubi.create.AllPartialModels.SHAFT));

        BlockState state = blockState;
        pressHead = instancerProvider().instancer(InstanceTypes.ORIENTED,
            Models.partial(StampingPressPartialModels.STAMPING_PRESS_HEAD))
            .createInstance();

        Quaternionf q = Axis.YP.rotationDegrees(
            AngleHelper.horizontalAngle(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
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
        PressingBehaviour pressingBehaviour = blockEntity.pressingBehaviour;
        return pressingBehaviour.getRenderedHeadOffset(pt) * pressingBehaviour.mode.headOffset;
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
