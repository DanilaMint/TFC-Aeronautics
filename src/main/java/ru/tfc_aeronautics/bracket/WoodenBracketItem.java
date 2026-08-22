package ru.tfc_aeronautics.bracket;

import com.simibubi.create.content.decoration.bracket.BracketBlockItem;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Item form of {@link WoodenBracket} — inherits the
 * применение-на-shaft/cog/pipe logic from {@link BracketBlockItem}.
 */
public class WoodenBracketItem extends BracketBlockItem {
    public WoodenBracketItem(Block block, Item.Properties properties) {
        super(block, properties);
    }
}
