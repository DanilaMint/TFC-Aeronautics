package ru.tfc_aeronautics.kinetics;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.AbstractShaftBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import ru.tfc_aeronautics.TFCAeronautics;
import ru.tfc_aeronautics.DamageTypes;
import ru.tfc_aeronautics.Config;

/**
 * Makes bare, spinning Create shafts and cogwheels hurt whatever touches them,
 * with damage scaling linearly with rotation speed.
 *
 * <p>Only {@link AbstractShaftBlock} is considered dangerous. That class covers
 * exactly the uncovered relays — {@code ShaftBlock}, {@code CogWheelBlock} (small
 * and large) and {@code PoweredShaftBlock} — while every encased variant sits on
 * the {@code AbstractEncasedShaftBlock}/{@code EncasedCogwheelBlock} branch of the
 * hierarchy instead. Slapping an andesite or brass casing on a shaft therefore
 * makes it safe without any extra bookkeeping here.
 *
 * <p>Create's classes cannot be extended from an addon, so the hook is an entity
 * tick listener that scans the block cells overlapping the entity's hitbox rather
 * than an {@code entityInside} override.
 */
@EventBusSubscriber(modid = TFCAeronautics.MOD_ID)
public final class ShaftDamageHandler
{
    /**
     * Shafts are solid, so an entity can only ever end up flush against them,
     * never inside. Inflating the hitbox by a sliver turns "pressed against"
     * into a shape intersection.
     */
    private static final double CONTACT_EPSILON = 0.05;

    private ShaftDamageHandler() {}

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event)
    {
        if (!Config.SHAFT_DAMAGE_ENABLED.get())
            return;

        final Entity entity = event.getEntity();
        final Level level = entity.level();
        if (level.isClientSide())
            return;
        if (!(entity instanceof LivingEntity living) || !living.isAlive() || living.isInvulnerable() || living.isSpectator())
            return;
        if (living instanceof Player player && player.getAbilities().invulnerable)
            return;

        final double startRpm = Config.SHAFT_DAMAGE_START_RPM.get();
        final AABB probe = living.getBoundingBox().inflate(CONTACT_EPSILON);
        final VoxelShape probeShape = Shapes.create(probe);

        for (BlockPos pos : BlockPos.betweenClosed(
            BlockPos.containing(probe.minX, probe.minY, probe.minZ),
            BlockPos.containing(probe.maxX, probe.maxY, probe.maxZ)))
        {
            final BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof AbstractShaftBlock shaft))
                continue;

            final BlockEntity be = level.getBlockEntity(pos);
            // getSpeed() is signed, and already reports 0 while overstressed or frozen.
            if (!(be instanceof KineticBlockEntity kinetic))
                continue;
            final float rpm = Math.abs(kinetic.getSpeed());
            if (rpm < startRpm)
                continue;

            // Test against the actual pole/gear shape, not the whole block cell.
            final VoxelShape shape = state.getShape(level, pos).move(pos.getX(), pos.getY(), pos.getZ());
            if (!Shapes.joinIsNotEmpty(shape, probeShape, BooleanOp.AND))
                continue;

            final float damage = damageFor(rpm);
            // A failed hurt() means the entity is still in i-frames; skip the feedback too.
            if (damage <= 0.0f || !living.hurt(DamageTypes.shaft(level), damage))
                continue;

            applyKnockback(living, shaft.getRotationAxis(state), pos, rpm, startRpm);

            final float volume = Config.SHAFT_SOUND_VOLUME.get().floatValue();
            if (volume > 0.0f)
                AllSoundEvents.CRUSHING_1.playOnServer(level, pos, volume, 0.7f + Math.min(rpm / 256.0f, 1.0f) * 0.6f);

            return; // at most one shaft may hit per tick
        }
    }

    /**
     * Linear ramp from zero damage at {@code shaftDamageStartRpm} to
     * {@code shaftDamageLethal} at {@code shaftDamageLethalRpm}, continuing to
     * grow past it so that Create's top speeds punch through armour.
     */
    private static float damageFor(float rpm)
    {
        final double start = Config.SHAFT_DAMAGE_START_RPM.get();
        final double lethalRpm = Config.SHAFT_DAMAGE_LETHAL_RPM.get();
        if (lethalRpm <= start)
            return 0.0f;
        final double ramp = (rpm - start) / (lethalRpm - start);
        return (float) (ramp * Config.SHAFT_DAMAGE_LETHAL.get() * Config.SHAFT_DAMAGE_MULTIPLIER.get());
    }

    /**
     * Flings the entity away from the shaft, perpendicular to its rotation axis,
     * so a horizontal shaft tosses you up or sideways rather than along itself.
     */
    private static void applyKnockback(LivingEntity living, Direction.Axis axis, BlockPos pos, float rpm, double startRpm)
    {
        Vec3 away = living.position()
            .add(0.0, living.getBbHeight() * 0.5, 0.0)
            .subtract(Vec3.atCenterOf(pos));
        away = switch (axis)
        {
            case X -> new Vec3(0.0, away.y, away.z);
            case Y -> new Vec3(away.x, 0.0, away.z);
            case Z -> new Vec3(away.x, away.y, 0.0);
        };
        if (away.lengthSqr() < 1.0E-4) // dead centre on the axis — shove upwards
            away = new Vec3(0.0, 1.0, 0.0);

        final double strength = Config.SHAFT_KNOCKBACK_BASE.get()
            + (rpm - startRpm) * Config.SHAFT_KNOCKBACK_PER_RPM.get();
        living.setDeltaMovement(living.getDeltaMovement().add(away.normalize().scale(strength)));
        living.hurtMarked = true; // without this the impulse never reaches the client
    }
}
