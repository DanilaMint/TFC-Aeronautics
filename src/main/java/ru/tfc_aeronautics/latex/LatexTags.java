package ru.tfc_aeronautics.latex;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import ru.tfc_aeronautics.TFCAeronautics;

public final class LatexTags {
    private LatexTags() {}

    public static final TagKey<Block> CAN_COLLECT_LATEX =
        BlockTags.create(ResourceLocation.fromNamespaceAndPath(TFCAeronautics.MOD_ID, "can_collect_latex"));
}