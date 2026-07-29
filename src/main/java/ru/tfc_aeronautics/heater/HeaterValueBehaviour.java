package ru.tfc_aeronautics.heater;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour.ValueSettings;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Side-mounted "max temperature" knob for the heater. Opened via the Create
 * {@code ValueSettingsScreen} on the south face, similar to the Rotation Speed
 * Controller's RPM slider.
 *
 * <p>The UI board exposes 32 slots ({@link #INTERVAL} = 50 °C) so the user can
 * step from 0 to 1600 in 50 °C increments. The actual integer value (0..1600)
 * is stored on the behaviour and mirrored to {@link HeaterBlockEntity#setMaxTemperature(int)}
 * via the callback.
 */
public class HeaterValueBehaviour extends ScrollValueBehaviour {

    public static final int INTERVAL = 50;

    private static final Component TITLE = Component.literal("Max Temperature");

    public HeaterValueBehaviour(Component label, SmartBlockEntity be, ValueBoxTransform slot) {
        super(label, be, slot);
    }

    @Override
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        return new ValueSettingsBoard(
            label,
            max / INTERVAL,
            10,
            ImmutableList.of(TITLE),
            new ValueSettingsFormatter(vs -> Component.literal(format(vs.value() * INTERVAL)))
        );
    }

    @Override
    public ValueSettings getValueSettings() {
        return new ValueSettings(0, value / INTERVAL);
    }

    @Override
    public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlDown) {
        if (valueSetting.equals(getValueSettings()))
            return;
        setValue(valueSetting.value() * INTERVAL);
        playFeedbackSound(this);
    }

    public HeaterValueBehaviour withRange(int min, int max) {
        super.between(min, max);
        return this;
    }

    public String format(int value) {
        return value + " °C";
    }
}