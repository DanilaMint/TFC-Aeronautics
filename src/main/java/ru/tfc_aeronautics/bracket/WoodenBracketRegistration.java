package ru.tfc_aeronautics.bracket;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import ru.tfc_aeronautics.TFCAeronautics;

/**
 * Registers twenty wooden bracket blocks and matching items, one per TFC wood
 * species. Item/block ids follow the TFC-style dotted path
 * {@code wood/bracket/<wood>} so that the per-wood tint stays close to the
 * rest of the mod's wood inventory (planks, lumber, etc.).
 *
 * <p>The vanilla Create {@code craft/kinetics/wooden_bracket} recipe is banned
 * separately in {@link ru.tfc_aeronautics.recipe.RecipeRemoval}; this class
 * only provides the new blocks plus per-wood crafting recipes.
 */
public final class WoodenBracketRegistration {

    public static final List<String> WOODS = List.of(
        "acacia", "ash", "aspen", "birch", "blackwood", "chestnut",
        "douglas_fir", "hickory", "kapok", "mangrove", "maple", "oak",
        "palm", "pine", "rosewood", "sequoia", "spruce", "sycamore",
        "white_cedar", "willow"
    );

    public static final DeferredRegister.Blocks BLOCKS =
        DeferredRegister.createBlocks(TFCAeronautics.MOD_ID);

    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(TFCAeronautics.MOD_ID);

    private static final Map<String, DeferredHolder<Block, WoodenBracket>> BRACKETS_INTERNAL =
        new LinkedHashMap<>();
    private static final Map<String, DeferredHolder<Item, WoodenBracketItem>> BRACKET_ITEMS_INTERNAL =
        new LinkedHashMap<>();

    static {
        for (String wood : WOODS) {
            DeferredHolder<Block, WoodenBracket> block = BLOCKS.register("wood/bracket/" + wood,
                () -> new WoodenBracket(
                    BlockBehaviour.Properties.of()
                        .mapColor(MapColor.WOOD)
                        .strength(0.5F, 0.5F)
                        .noOcclusion()
                        .sound(net.minecraft.world.level.block.SoundType.WOOD)));
            DeferredHolder<Item, WoodenBracketItem> item = ITEMS.register("wood/bracket/" + wood,
                () -> new WoodenBracketItem(block.get(), new Item.Properties()));
            BRACKETS_INTERNAL.put(wood, block);
            BRACKET_ITEMS_INTERNAL.put(wood, item);
        }
    }

    public static final Map<String, DeferredHolder<Block, WoodenBracket>> BRACKETS = Map.copyOf(BRACKETS_INTERNAL);
    public static final Map<String, DeferredHolder<Item, WoodenBracketItem>> BRACKET_ITEMS = Map.copyOf(BRACKET_ITEMS_INTERNAL);

    private WoodenBracketRegistration() {}

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }
}
