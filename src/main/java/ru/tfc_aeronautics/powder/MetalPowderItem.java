package ru.tfc_aeronautics.powder;

import net.minecraft.world.item.Item;

/**
 * Powdered metal item. Heating behavior is data-driven via TFC's
 * {@code tfc/item_heat} and {@code tfc:heating} recipe JSON — this item itself
 * has no special behavior.
 */
public class MetalPowderItem extends Item
{
    public MetalPowderItem(Properties properties)
    {
        super(properties);
    }
}