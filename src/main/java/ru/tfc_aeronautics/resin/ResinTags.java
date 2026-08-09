package ru.tfc_aeronautics.resin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import ru.tfc_aeronautics.Aeronautics;

public final class ResinTags {
    private ResinTags() {}

    public static final TagKey<Block> CAN_COLLECT_RESIN =
        BlockTags.create(ResourceLocation.fromNamespaceAndPath(Aeronautics.MOD_ID, "can_collect_resin"));
}
