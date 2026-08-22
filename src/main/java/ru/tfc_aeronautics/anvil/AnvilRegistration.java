package ru.tfc_aeronautics.anvil;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.devices.AnvilBlock;
import net.dries007.tfc.util.Metal;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

import ru.tfc_aeronautics.TFCAeronautics;

/**
 * Registers tier-1 anvil blocks for every TFC metal that doesn't already have one.
 * Reuses TFC's {@link AnvilBlock} and pairs the blocks with
 * {@link TFCBlockEntities#ANVIL} so that TFC's {@code AnvilContainer} menu factory
 * (which hardcodes that BE type) finds the placed BE.
 *
 * <p>TFC's {@code ANVIL} type's {@code validBlocks} set does not include our 19 custom
 * anvil blocks, so we reflectively extend it after registration so
 * {@code BlockEntityType.create(pos, state)} succeeds when our blocks are placed.
 */
public final class AnvilRegistration {

    public static final DeferredRegister.Blocks BLOCKS =
        DeferredRegister.createBlocks(TFCAeronautics.MOD_ID);

    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(TFCAeronautics.MOD_ID);

    public static final Map<Metal, DeferredHolder<Block, AnvilBlock>> ANVILS =
        new EnumMap<>(Metal.class);

    public static final Map<Metal, DeferredHolder<Item, BlockItem>> ANVIL_ITEMS =
        new EnumMap<>(Metal.class);

    static {
        for (Metal metal : Metal.values()) {
            if (Metal.BlockType.ANVIL.has(metal)) {
                continue;
            }
            String id = "metal/anvil/" + metal.getSerializedName();
            DeferredHolder<Block, AnvilBlock> block = BLOCKS.register(id, () -> createAnvil(metal));
            ANVILS.put(metal, block);
            ANVIL_ITEMS.put(metal, ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties())));
        }
    }

    private static AnvilBlock createAnvil(Metal metal) {
        return new AnvilBlock(
            ExtendedProperties.of()
                .mapColor(metal.mapColor())
                .noOcclusion()
                .sound(SoundType.ANVIL)
                .strength(10F, 10F)
                .requiresCorrectToolForDrops()
                .blockEntity(TFCBlockEntities.ANVIL),
            1
        );
    }

    private static boolean extendedTfcAnvilType = false;

    private static synchronized void extendTfcAnvilTypeValidBlocks() {
        if (extendedTfcAnvilType) return;
        extendedTfcAnvilType = true;

        try {
            BlockEntityType<?> type = TFCBlockEntities.ANVIL.get();
            Field validBlocksField = BlockEntityType.class.getDeclaredField("validBlocks");
            validBlocksField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Set<Block> validBlocks = (Set<Block>) validBlocksField.get(type);
            Set<Block> ours = ANVILS.values().stream()
                .map(DeferredHolder::get)
                .collect(Collectors.toSet());
            try {
                validBlocks.addAll(ours);
            } catch (UnsupportedOperationException e) {
                Set<Block> mutable = new HashSet<>(validBlocks);
                mutable.addAll(ours);
                validBlocksField.set(type, mutable);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to extend TFC anvil BlockEntityType.validBlocks", e);
        }
    }

    private AnvilRegistration() {}

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        bus.addListener((RegisterEvent event) -> {
            if (event.getRegistryKey().equals(Registries.BLOCK_ENTITY_TYPE)) {
                extendTfcAnvilTypeValidBlocks();
            }
        });
    }
}
