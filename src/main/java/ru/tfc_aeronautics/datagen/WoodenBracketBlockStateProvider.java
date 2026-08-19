package ru.tfc_aeronautics.datagen;

import com.simibubi.create.content.decoration.bracket.BracketBlock;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

import ru.tfc_aeronautics.TFCAeronautics;
import ru.tfc_aeronautics.bracket.WoodenBracket;
import ru.tfc_aeronautics.bracket.WoodenBracketRegistration;

/**
 * For each TFC wood species, registers:
 * <ul>
 *   <li>Six per-wood block models — each parented on the matching
 *       {@code create:block/bracket/&lt;type&gt;/&lt;ground|wall&gt;} geometry
 *       with {@code bracket} and {@code plate} textures rebound to the
 *       per-wood PNGs;</li>
 *   <li>A blockstate with the same 36 (along × facing × type) variants as
 *       {@code create:wooden_bracket}, so the ПКМ-on-shaft/cog/pipe behaviour
 *       matches the original.</li>
 * </ul>
 *
 * <p>Rotation table per (facing, axis_along_first) is taken verbatim from
 * Create's generated {@code wooden_bracket.json}; per-type (cog/pipe/shaft)
 * does not change rotation.
 */
public class WoodenBracketBlockStateProvider extends BlockStateProvider {

    public WoodenBracketBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, TFCAeronautics.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        WoodenBracketRegistration.WOODS.forEach(this::registerWood);
    }

    private void registerWood(String wood) {
        DeferredHolder<Block, WoodenBracket> holder = WoodenBracketRegistration.BRACKETS.get(wood);
        Block block = holder.get();

        ResourceLocation bracketTex = ResourceLocation.fromNamespaceAndPath(
            TFCAeronautics.MOD_ID, "block/wood/bracket/bracket_" + wood);
        ResourceLocation plateTex = ResourceLocation.fromNamespaceAndPath(
            TFCAeronautics.MOD_ID, "block/wood/bracket/bracket_plate_" + wood);

        ModelFile[][] groundWall = new ModelFile[3][2];
        for (int t = 0; t < BracketBlock.BracketType.values().length; t++) {
            String type = BracketBlock.BracketType.values()[t].getSerializedName();
            for (int p = 0; p < 2; p++) {
                String position = p == 0 ? "ground" : "wall";
                String childPath = "wood/bracket/" + type + "/" + position + "_" + wood;
                ResourceLocation parent = ResourceLocation.fromNamespaceAndPath(
                    "create", "block/bracket/" + type + "/" + position);
                groundWall[t][p] = models()
                    .withExistingParent(childPath, parent)
                    .texture("bracket", bracketTex)
                    .texture("plate", plateTex);
            }
        }

        for (boolean alongFirst : new boolean[]{false, true}) {
            for (Direction facing : Direction.values()) {
                boolean vertical = facing.getAxis().isVertical();
                int posIdx = vertical ? 0 : 1;
                int[] rot = rotation(facing, alongFirst);
                for (BracketBlock.BracketType type : BracketBlock.BracketType.values()) {
                    int tIdx = type.ordinal();
                    getVariantBuilder(block)
                        .partialState()
                        .with(BracketBlock.AXIS_ALONG_FIRST_COORDINATE, alongFirst)
                        .with(BracketBlock.FACING, facing)
                        .with(BracketBlock.TYPE, type)
                        .modelForState()
                        .modelFile(groundWall[tIdx][posIdx])
                        .rotationX(rot[0])
                        .rotationY(rot[1])
                        .addModel();
                }
            }
        }
    }

    /**
     * Rotation table from Create's {@code wooden_bracket.json}, simplified to
     * two ints (x, y). The pattern is identical for cog/pipe/shaft, so we
     * don't need per-type entries.
     */
    private static int[] rotation(Direction facing, boolean alongFirst) {
        if (alongFirst) {
            switch (facing) {
                case UP:    return new int[]{0, 90};
                case DOWN:  return new int[]{180, 90};
                case NORTH: return new int[]{0, 270};
                case SOUTH: return new int[]{0, 90};
                case EAST:  return new int[]{90, 0};
                case WEST:  return new int[]{90, 180};
                default:    throw new IllegalArgumentException();
            }
        }
        switch (facing) {
            case UP:    return new int[]{0, 0};
            case DOWN:  return new int[]{180, 0};
            case NORTH: return new int[]{90, 270};
            case SOUTH: return new int[]{90, 90};
            case EAST:  return new int[]{0, 0};
            case WEST:  return new int[]{0, 180};
            default:    throw new IllegalArgumentException();
        }
    }
}
