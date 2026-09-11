package ru.aeronautics.client.ponder.scenes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import net.createmod.catnip.math.VecHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.fluids.FluidFX;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import ru.tfc_aeronautics.fluid.Fluids;

/**
 * Ponder scenes for tfc_aeronautics:condenser_coil.
 *
 * Scene id: "condenser_coil/distillating"
 *   - NBT path:  assets/tfc_aeronautics/ponder/condenser_coil/distillating.nbt
 *   - Lang keys: tfc_aeronautics.ponder.condenser_coil/distillating.header
 *                tfc_aeronautics.ponder.condenser_coil/distillating.text_N
 *
 * Layout (size [7, 4, 7]) — y=0 is a checkerboard platform; content is at y=1..3:
 *
 *   y=0: white / light_gray concrete checkerboard (7x7)
 *   (1, 1, 3): copper_scaffolding (decorative)
 *   (5, 1, 3): TFC firepit (lit=true baked in NBT) — heat source under input tank
 *   (1, 2, 3): Create fluid tank (controller) — OUTPUT (empty in NBT; filled by Java to 10 × 704 = 7040 mB ethanol)
 *   (5, 2, 3): Create fluid tank (controller) — INPUT (16000 mB whiskey seeded by NBT; 10 × 604 mB drained by loop, final 9960 mB swapped to stillage)
 *   (1, 3, 3): Create fluid tank (top) — output multi-block extension
 *   (2, 3, 3): Create fluid pipe — steam axis, output side (BE ejects west through here)
 *   (3, 3, 2): Create fluid pipe — coolant loop, north face (drops water)
 *   (3, 3, 3): condenser_coil — center, inputFace=5 (EAST), resultFace=4 (WEST)
 *   (3, 3, 4): Create fluid pipe — coolant loop, south face (drops water)
 *   (4, 3, 3): Create fluid pipe — steam axis, input side
 *   (5, 3, 3): Create fluid tank (top) — input multi-block extension
 *
 * Initial reveal uses a single combined {@code showSection(layersFrom(1), DOWN)}
 * (cf. {@code com.simibubi.create.infrastructure.ponder.scenes.TemplateScenes.templateMethod})
 * after {@code showSection(layer(0), UP)} for the base plate — multiple
 * progressive {@code showSection} calls caused visual overlap of fade
 * animations on disjoint selections (one section already settled while the
 * next was still mid-fade), so we now reveal the entire y=1..3 content as one
 * block.
 *
 * Visual-only simulation: CondenserCoilBlockEntity.tick() returns early on the
 * client (level.isClientSide), so the BE never advances IDLE -> WARMUP -> RUNNING
 * during the scene. We drive the visible side effects directly: drain the input
 * tank, fill the output tank, swap to stillage at the end, and emit drip particles
 * from the output steam pipe.
 */
public final class CondenserCoilScenes {

    /**
     * Input tank starts at 16000 mB of whiskey (seeded in distillating.nbt).
     * The loop drains 10 × 604 = 6040 mB, leaving 9960 mB in the tank, which
     * is then atomically swapped to stillage in Phase D. The loop never
     * visually empties the input.
     */
    private static final int STEP_WHISKEY = 604;
    /** 10 × 704 = 7040 mB of ethanol — the full recipe output, decoupled from whiskey drain. */
    private static final int STEP_ETHANOL = 704;
    private static final int ITERATIONS = 10;
    /** Replaces the 9960 mB of whiskey left in the input tank at end of loop. */
    private static final int FINAL_STILLAGE = 9960;
    private static final int IDLE_PER_ITER = 10;

    private CondenserCoilScenes() {}

    public static void distillating(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("condenser_coil/distillating", "Condensing distillate with the Condenser Coil");
        scene.configureBasePlate(0, 0, 7);

        BlockPos inputPos = util.grid().at(5, 2, 3);
        BlockPos outputPos = util.grid().at(1, 2, 3);
        BlockPos coilPos = util.grid().at(3, 3, 3);
        BlockPos firepitPos = util.grid().at(5, 1, 3);
        BlockPos outputPipePos = util.grid().at(2, 3, 3);
        BlockPos coolantNorthPos = util.grid().at(3, 3, 2);

        Selection inputTankSel = util.select().fromTo(5, 2, 3, 5, 3, 3);
        Selection outputTankSel = util.select().fromTo(1, 2, 3, 1, 3, 3);
        Selection firepitSel = util.select().position(firepitPos);
        Selection coilSel = util.select().position(coilPos);
        Selection coolantPipes = util.select().fromTo(3, 3, 2, 3, 3, 4);
        Selection steamPipes = util.select().fromTo(2, 3, 3, 4, 3, 3);

        Fluid whiskeyFluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse("tfc:whiskey"));
        FluidStack ethanolStack = new FluidStack(Fluids.ETHANOL.getSource(), STEP_ETHANOL);
        FluidStack stillageStack = new FluidStack(Fluids.STILLAGE.getSource(), FINAL_STILLAGE);

        // === Phase A — reveal base plate + all content as one fade =======

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);
        scene.world().showSection(util.select().layersFrom(1), Direction.DOWN);
        scene.idle(20);
        // Firepit is baked with lit='true' in distillating.nbt so its
        // animateTick/clientTick smoke particles run from the moment the section
        // is revealed — no runtime modifyBlock needed (and the previous version
        // didn't actually drive TFC's fire animation visibly).

        // === Phase B — educational texts ==================================

        scene.overlay().showText(120)
            .text("The coil is needed to build a distillator. Inside, the substance's vapors cool down and collect on the other side.")
            .pointAt(util.vector().topOf(coilPos))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(140);

        scene.overlay().showText(120)
            .text("Distillable liquid is poured into the tank; there must be a heat source below.")
            .pointAt(util.vector().topOf(inputPos))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(140);

        scene.overlay().showText(80)
            .text("Coolant flows through the coil.")
            .pointAt(util.vector().topOf(coolantNorthPos))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(100);

        // Coolant source: water block at (3, 0, 5), mechanical pump on top facing UP,
        // a vertical pipe rising out of it (3, 2, 5), then a corner pipe at (3, 3, 5)
        // that turns NORTH to merge into the existing coolant loop at (3, 3, 4).
        BlockPos waterPos = util.grid().at(3, 0, 5);
        BlockPos pumpPos = util.grid().at(3, 1, 5);
        BlockPos pumpPipeVerticalPos = util.grid().at(3, 2, 5);
        BlockPos pumpPipeCornerPos = util.grid().at(3, 3, 5);
        BlockPos existingCoolantSouth = util.grid().at(3, 3, 4);

        scene.world().setBlock(waterPos, Blocks.WATER.defaultBlockState(), false);
        scene.idle(5);

        BlockState pumpState = AllBlocks.MECHANICAL_PUMP.get()
            .defaultBlockState()
            .setValue(BlockStateProperties.FACING, Direction.UP);
        scene.world().setBlock(pumpPos, pumpState, true);
        scene.world().setKineticSpeed(util.select().position(pumpPos), 16.0f);
        scene.idle(5);

        // FluidPipeBlock.getDefaultState() renders stubs on all 6 faces — connection
        // booleans must be set explicitly. PonderLevel.onPlace early-returns (client
        // side), propagatePipeChange in CreateSceneBuilder is hardcoded to
        // PumpBlockEntity only, so the BE never updates connectivity on its own.
        // Pattern is the same as Create's own PumpScenes.java:54-60 / PipeScenes.java:54-60.
        BlockState pipeNoConnect = AllBlocks.FLUID_PIPE.getDefaultState()
            .setValue(FluidPipeBlock.NORTH, false)
            .setValue(FluidPipeBlock.SOUTH, false)
            .setValue(FluidPipeBlock.EAST, false)
            .setValue(FluidPipeBlock.WEST, false)
            .setValue(FluidPipeBlock.UP, false)
            .setValue(FluidPipeBlock.DOWN, false);
        BlockState pipeLower = pipeNoConnect
            .setValue(FluidPipeBlock.DOWN, true)
            .setValue(FluidPipeBlock.UP, true);
        BlockState pipeCorner = pipeNoConnect
            .setValue(FluidPipeBlock.DOWN, true)
            .setValue(FluidPipeBlock.NORTH, true);

        scene.world().setBlock(pumpPipeVerticalPos, pipeLower, false);
        scene.world().setBlock(pumpPipeCornerPos, pipeCorner, false);
        // Existing pipe at (3, 3, 4) was baked with NORTH=true only (toward the coil).
        // Add SOUTH=true so the visual joint to the corner pipe at (3, 3, 5) renders.
        scene.world().modifyBlock(existingCoolantSouth,
            s -> s.setValue(FluidPipeBlock.SOUTH, true), false);
        scene.idle(5);

        scene.overlay().showText(100)
            .text("When all conditions are met, liquid forms on the opposite side of the coil.")
            .pointAt(util.vector().topOf(outputPos))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(120);

        // === Phase C — accelerated distillation loop =====================

        RandomSource random = RandomSource.create();
        Vec3 dripOrigin = util.vector().centerOf(outputPipePos).add(-0.25, 0.5, 0);

        for (int i = 0; i < ITERATIONS; i++) {
            scene.world().modifyBlockEntity(inputPos, FluidTankBlockEntity.class,
                be -> be.getTankInventory().drain(new FluidStack(whiskeyFluid, STEP_WHISKEY), FluidAction.EXECUTE));
            scene.world().modifyBlockEntity(outputPos, FluidTankBlockEntity.class,
                be -> be.getTankInventory().fill(ethanolStack.copy(), FluidAction.EXECUTE));

            ParticleOptions ethanolParticle = FluidFX.getFluidParticle(ethanolStack);
            for (int p = 0; p < 3; p++) {
                scene.effects().emitParticles(dripOrigin,
                    scene.effects().simpleParticleEmitter(ethanolParticle,
                        VecHelper.offsetRandomly(Vec3.ZERO, random, 0.1f)),
                    1, 1);
            }
            scene.idle(IDLE_PER_ITER);
        }

        // === Phase D — residue swap + closing text =======================

        // Atomic swap: drain the remaining 9960 mB of whiskey, fill with
        // 9960 mB of stillage in the same tick. The tank never visually
        // empties — it goes straight from "9960 mB of whiskey" to "9960 mB of
        // stillage" once the loop's last drain has settled.
        scene.world().modifyBlockEntity(inputPos, FluidTankBlockEntity.class,
            be -> {
                FluidTank tank = be.getTankInventory();
                tank.drain(FINAL_STILLAGE, FluidAction.EXECUTE);
                tank.fill(stillageStack, FluidAction.EXECUTE);
            });

        scene.overlay().showText(100)
            .text("When the process finishes, residue remains in the original tank.")
            .pointAt(util.vector().topOf(inputPos))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(100);

        scene.markAsFinished();
    }
}
