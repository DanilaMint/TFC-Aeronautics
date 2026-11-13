package ru.tfc_aeronautics.heater;

import java.util.List;

import com.simibubi.create.content.kinetics.fan.EncasedFanBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import net.dries007.tfc.common.blockentities.BellowsBlockEntity;
import net.dries007.tfc.common.blocks.devices.IBellowsConsumer;
import net.dries007.tfc.common.component.heat.Heat;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.dries007.tfc.common.component.heat.IHeat;
import net.dries007.tfc.common.component.heat.IHeatView;
import net.dries007.tfc.common.recipes.HeatingRecipe;
import net.dries007.tfc.util.data.Fuel;

import org.jetbrains.annotations.Nullable;

import ru.tfc_aeronautics.Config;

/**
 * The heater's brain: holds the inventory (item + fuel), the internal molten-metal
 * fluid tank, the current device temperature, and the {@link HeaterValueBehaviour}
 * for the side-mounted max-temperature knob.
 *
 * <p>Tick logic mirrors TFC's {@code CharcoalForge}: a fuel item defines a target
 * burn temperature, {@code airTicks} from bellows / Create fans boost it on top,
 * and {@code HeatCapability.adjustDeviceTemp} drives the temperature towards the
 * (capped) target. The item in the input slot is heated each tick, and when a
 * {@code HeatingRecipe} matches its temperature the recipe's fluid output is
 * pushed into the internal {@link FluidTank} (exposed via {@code IFluidHandler}
 * on the down face).
 *
 * <p>The tank holds up to {@link #TANK_CAPACITY} mB of a single molten metal. If the
 * tank already holds a different metal, the incoming fluid is rejected — metals do not
 * mix. If the recipe would overflow the tank, the excess is silently discarded; the
 * input item has still melted, so it is consumed either way.
 */
public class HeaterBlockEntity extends SmartBlockEntity implements IBellowsConsumer {

    public static final int SLOT_ITEM = 0;
    public static final int SLOT_FUEL = 1;

    public static final int TANK_CAPACITY = 2000; // internal molten-metal volume, in mB

    public static final int MAX_TEMP = (int) Heat.maxVisibleTemperature();

    private final ItemStackHandler inventory;
    private final FluidTank tank;
    private final IItemHandler hotAwareItemHandler;

    private float temperature;
    private float burnTemperature;
    private int burnTicks;
    private int airTicks;
    private int maxTemperature = 1000;
    private HeatingRecipe cachedRecipe;

    private boolean wasLit = false;

    private HeaterValueBehaviour maxTempBehaviour;

    public HeaterBlockEntity(BlockPos pos, BlockState state) {
        super(HeaterRegistration.HEATER_BE.get(), pos, state);
        this.inventory = new ItemStackHandler(2) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                setChanged();
                if (slot == SLOT_ITEM) invalidateRecipe();
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                if (slot == SLOT_ITEM) {
                    return HeatCapability.has(stack);
                }
                if (slot == SLOT_FUEL) {
                    return Fuel.get(stack) != null;
                }
                return false;
            }

            @Override
            public int getSlotLimit(int slot) {
                return slot == SLOT_FUEL ? 64 : 1;
            }
        };
        this.tank = new FluidTank(TANK_CAPACITY) {
            @Override
            protected void onContentsChanged() {
                super.onContentsChanged();
                setChanged();
                sendData();
            }

            /**
             * Two rules govern insertion:
             * <ul>
             *   <li>If the tank already holds a different molten metal, the incoming fluid is rejected —
             *       metals do not mix inside the heater.</li>
             *   <li>If the request would push the tank past {@link #TANK_CAPACITY}, only what fits is
             *       accepted; the overflow is silently discarded (the molten metal "disappears").</li>
             * </ul>
             * The caller is responsible for handling the remainder — in practice this means the input
             * item is always consumed by {@link #tryApplyHeatingRecipe}, since the metal has already melted.
             */
            @Override
            public int fill(FluidStack resource, FluidAction action) {
                if (resource.isEmpty()) return 0;
                FluidStack current = getFluid();
                if (!current.isEmpty() && !FluidStack.isSameFluidSameComponents(current, resource)) return 0;
                int space = getCapacity() - current.getAmount();
                if (space <= 0) return 0;
                int accepted = Math.min(resource.getAmount(), space);
                if (action.execute()) {
                    if (current.isEmpty()) {
                        setFluid(resource.copyWithAmount(accepted));
                    } else {
                        current.grow(accepted);
                    }
                    onContentsChanged();
                }
                return accepted;
            }
        };
        this.hotAwareItemHandler = new HotAwareItemHandler(this, inventory);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        maxTempBehaviour = new HeaterValueBehaviour(
            Component.translatable("tfc_aeronautics.heater.max_temperature"),
            this,
            new HeaterValueBoxTransform()
        ).withRange(0, MAX_TEMP);
        maxTempBehaviour.withFormatter(i -> i + " °C");
        maxTempBehaviour.withCallback(this::setMaxTemperature);
        maxTempBehaviour.value = maxTemperature;
        behaviours.add(maxTempBehaviour);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) return;

        boolean isRaining = level.isRainingAt(worldPosition.above());

        // 1. Pull in fresh air from bellows and Create fans
        scanForAirSources();

        // 2. Decrement fuel and air timers
        if (burnTicks > 0) {
            burnTicks -= (airTicks > 0 || isRaining) ? 2 : 1;
            if (burnTicks <= 0) {
                burnTicks = 0;
                burnTemperature = 0;
            }
        }
        if (airTicks > 0) {
            airTicks--;
        }

        // 3. Try to light (consume a new fuel item) if needed
        if (burnTicks <= 0 && !consumeFuel()) {
            burnTemperature = 0;
        }

        // 4. Drift device temperature
        float baseTarget = Math.min(burnTemperature, maxTemperature);
        temperature = HeatCapability.adjustDeviceTemp(temperature, baseTarget, airTicks, isRaining);
        temperature = Mth.clamp(temperature, 0f, MAX_TEMP + 200f); // hard physical cap (visible scale + small headroom)

        // Resolve heating-speed multiplier. 0.0 means "instant heating" — we bypass TFC's
        // per-tick addTemp physics and set the item's temperature directly to the knob value
        // (`maxTemperature`). On the same tick, the normal tryApplyHeatingRecipe below checks
        // whether a recipe fires at that temperature and transforms the item. If the knob is
        // set below the recipe's required temperature, the item stays hot but never transforms.
        // Otherwise the multiplier scales TFC's addTemp `modifier` (default 3) so that the
        // per-tick heating rate (`modifier / heatCapacity`) is multiplied by the config value.
        float multiplier = Config.HEATER_SPEED_MULTIPLIER.get().floatValue();
        float effectiveTemp = multiplier <= 0f ? maxTemperature : temperature * multiplier;

        // 5. Hand our device temperature up to whatever consumes heat above us
        HeatCapability.provideHeatTo(level, worldPosition.above(), Direction.DOWN, effectiveTemp);

        // 6. Heat the item in slot 0
        ItemStack stack = inventory.getStackInSlot(SLOT_ITEM);
        @Nullable IHeat heat = HeatCapability.get(stack);
        if (heat != null && temperature > 0f) {
            if (multiplier <= 0f) {
                heat.setTemperature(maxTemperature);
            } else {
                HeatCapability.addTemp(heat, effectiveTemp, 3f * multiplier);
            }
        }

        // 7. Apply any HeatingRecipe transformation
        if (heat != null) {
            tryApplyHeatingRecipe(stack, heat);
        }

        // 8. Sync the LIT block-state property when burning toggles on/off
        boolean isLit = burnTicks > 0;
        if (isLit != wasLit) {
            wasLit = isLit;
            level.setBlock(worldPosition, getBlockState().setValue(BlockStateProperties.LIT, isLit), Block.UPDATE_ALL);
        }

        // 9. Emit flame/smoke particles while burning
        if (isLit && level instanceof ServerLevel serverLevel && level.getGameTime() % 3 == 0) {
            double x = worldPosition.getX() + 0.5;
            double y = worldPosition.getY() + 0.375; // 6 px above the block bottom — inside the coal cavity
            double z = worldPosition.getZ() + 0.5;
            serverLevel.sendParticles(ParticleTypes.FLAME, x, y, z, 3, 0.3, 0.1, 0.3, 0.02);
            serverLevel.sendParticles(ParticleTypes.SMOKE, x, y + 0.3, z, 1, 0.4, 0.2, 0.4, 0.01);
        }

        setChanged();
        sendData();
    }

    private void scanForAirSources() {
        // TFC bellows call intakeAir() themselves through IBellowsConsumer dispatch;
        // nothing to do here unless a fan also adds air.
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = worldPosition.relative(dir);
            if (!(level.getBlockEntity(neighbor) instanceof EncasedFanBlockEntity fan)) continue;
            Direction flow = fan.getAirFlowDirection();
            if (flow != dir.getOpposite()) continue;
            float rpm = Math.abs(fan.getSpeed());
            if (rpm <= 0) continue;
            intakeAir(level, worldPosition, getBlockState(), Math.min(200, Mth.floor(rpm)));
        }
    }

    private boolean consumeFuel() {
        ItemStack fuelStack = inventory.getStackInSlot(SLOT_FUEL);
        if (fuelStack.isEmpty()) return false;
        Fuel fuel = Fuel.get(fuelStack);
        if (fuel == null) return false;
        fuelStack.shrink(1);
        burnTicks = fuel.duration();
        burnTemperature = fuel.temperature();
        return true;
    }

    private void tryApplyHeatingRecipe(ItemStack stack, IHeat heat) {
        if (stack.isEmpty()) return;
        if (cachedRecipe == null || !cachedRecipe.matches(stack)) {
            cachedRecipe = HeatingRecipe.getRecipe(stack);
        }
        if (cachedRecipe == null) return;
        if (!cachedRecipe.isValidTemperature(heat.getTemperature())) return;

        FluidStack fluidOut = cachedRecipe.assembleFluid(stack);
        ItemStack itemOut = cachedRecipe.assembleItem(stack);

        // The molten metal either fits in the tank or is discarded (overflow / type mismatch / full).
        // In every case the input item has melted, so it is always consumed.
        if (!fluidOut.isEmpty()) {
            tank.fill(fluidOut, FluidAction.EXECUTE);
        }

        inventory.setStackInSlot(SLOT_ITEM, itemOut);
        cachedRecipe = null;
    }

    private void invalidateRecipe() {
        cachedRecipe = null;
    }

    public void setMaxTemperature(int value) {
        this.maxTemperature = Mth.clamp(value, 0, MAX_TEMP);
        setChanged();
        sendData();
    }

    public int getMaxTemperature() {
        return maxTemperature;
    }

    public float getTemperature() {
        return temperature;
    }

    public float getBurnTemperature() {
        return burnTemperature;
    }

    public int getBurnTicks() {
        return burnTicks;
    }

    public int getAirTicks() {
        return airTicks;
    }

    public FluidTank getTank() {
        return tank;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public IItemHandler getHotAwareItemHandler() {
        return hotAwareItemHandler;
    }

    // --- IBellowsConsumer ------------------------------------------------

    @Override
    public boolean canAcceptAir(Level level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void intakeAir(Level level, BlockPos pos, BlockState state, int amount) {
        airTicks = Math.min(airTicks + amount, BellowsBlockEntity.MAX_DEVICE_AIR_TICKS);
        setChanged();
    }

    // --- persistence -----------------------------------------------------

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putFloat("temperature", temperature);
        tag.putFloat("burnTemperature", burnTemperature);
        tag.putInt("burnTicks", burnTicks);
        tag.putInt("airTicks", airTicks);
        tag.putInt("maxTemperature", maxTemperature);
        tag.put("inventory", inventory.serializeNBT(registries));
        CompoundTag tankTag = new CompoundTag();
        tank.writeToNBT(registries, tankTag);
        tag.put("tank", tankTag);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        temperature = tag.getFloat("temperature");
        burnTemperature = tag.getFloat("burnTemperature");
        burnTicks = tag.getInt("burnTicks");
        airTicks = tag.getInt("airTicks");
        maxTemperature = Mth.clamp(tag.getInt("maxTemperature"), 0, MAX_TEMP);
        if (tag.contains("inventory")) inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        if (tag.contains("tank")) tank.readFromNBT(registries, tag.getCompound("tank"));
    }

    // --- wrappers --------------------------------------------------------

    /**
     * {@link IItemHandler} wrapper that gates extraction on the slot-0 item
     * having reached the heater's set {@code maxTemperature}. Fuel slot is
     * not extractable at all.
     *
     * <p>This intentionally does NOT use {@code IHeatView#canWork()} — that
     * fires as soon as the item is hot enough to hammer, well below the
     * target temperature, which made chutes pull items out halfway through
     * heating. Tying extraction to {@code maxTemperature} means the user
     * controls when the item is "done" via the knob, and a chute connected
     * on any face only fires when the heater's target is reached.
     */
    private static final class HotAwareItemHandler implements IItemHandler {

        private final HeaterBlockEntity owner;
        private final ItemStackHandler inventory;

        HotAwareItemHandler(HeaterBlockEntity owner, ItemStackHandler inventory) {
            this.owner = owner;
            this.inventory = inventory;
        }

        @Override
        public int getSlots() {
            return inventory.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return inventory.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot == SLOT_FUEL) return ItemStack.EMPTY;
            if (slot == SLOT_ITEM && !isItemHot(slot)) return ItemStack.EMPTY;
            return inventory.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return inventory.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return inventory.isItemValid(slot, stack);
        }

        private boolean isItemHot(int slot) {
            ItemStack stack = inventory.getStackInSlot(slot);
            IHeatView heat = HeatCapability.view(stack);
            if (heat == null) return false;
            int maxTemp = owner.getMaxTemperature();
            if (maxTemp <= 0) return false;
            return heat.getTemperature() >= maxTemp;
        }
    }
}