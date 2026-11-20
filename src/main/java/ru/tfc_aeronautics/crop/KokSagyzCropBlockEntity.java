package ru.tfc_aeronautics.crop;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import net.dries007.tfc.common.blockentities.CropBlockEntity;

/**
 * The block entity of the Kok-Saghyz plant.
 *
 * <p>TFC's {@code CropBlockEntity} has two constructors, and the one taking only
 * a position and a state hardcodes TFC's own {@code tfc:crop} entity type:
 * {@code this(TFCBlockEntities.CROP.get(), pos, state)}. That is the one the
 * vanilla {@code BlockEntityType.BlockEntitySupplier} resolves to, because in
 * 1.21.1 the supplier is {@code create(BlockPos, BlockState)} - it never
 * receives the type it is being built for.
 *
 * <p>{@code BlockEntity}'s type field is final, so a block entity that carries
 * our own type has to be handed that type itself. Passing it in the
 * constructor is the only way, and it also keeps {@code tfc:crop}'s
 * {@code validBlocks} free of foreign blocks.
 *
 * <p>Everything else - the calendar driven growth tick, the nutrient
 * bookkeeping, the yield and the expiry - is inherited from TFC unchanged.
 */
public class KokSagyzCropBlockEntity extends CropBlockEntity
{
    public KokSagyzCropBlockEntity(BlockPos pos, BlockState state)
    {
        super(KokSagyzRegistration.CROP_BE.get(), pos, state);
    }
}
