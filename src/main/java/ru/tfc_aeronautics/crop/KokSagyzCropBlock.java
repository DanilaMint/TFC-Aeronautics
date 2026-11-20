package ru.tfc_aeronautics.crop;

import java.util.function.Supplier;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.crop.DefaultCropBlock;

/**
 * The living Kok-Saghyz plant.
 *
 * <p>TFC's {@code DefaultCropBlock} already implements dying into the dead
 * variant and turning accumulated growth into the block age, so all that is
 * left here is the age property - it has to be a static field, because
 * {@code CropBlock}'s constructor asks for it while the instance is still being
 * built.
 */
public class KokSagyzCropBlock extends DefaultCropBlock
{
    private static final IntegerProperty AGE = TFCBlockStateProperties.getAgeProperty(KokSagyzCrop.STAGES - 1);

    public KokSagyzCropBlock(ExtendedProperties properties, Supplier<? extends Block> dead, Supplier<? extends Item> seeds)
    {
        super(properties,
            KokSagyzCrop.STAGES - 1,
            dead,
            seeds,
            KokSagyzCrop.NITROGEN,
            KokSagyzCrop.PHOSPHOROUS,
            KokSagyzCrop.POTASSIUM,
            KokSagyzCrop.CLIMATE);
    }

    @Override
    public IntegerProperty getAgeProperty()
    {
        return AGE;
    }
}
