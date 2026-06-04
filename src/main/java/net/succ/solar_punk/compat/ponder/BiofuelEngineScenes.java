package net.succ.solar_punk.compat.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.succ.solar_punk.block.custom.BiofuelEngineBlock;
import net.succ.solar_punk.block.entity.custom.BiofuelEngineBlockEntity;
import net.succ.solar_punk.fluid.ModFluids;

public class BiofuelEngineScenes {

    private static final BlockPos MACHINE = new BlockPos(2, 2, 2);
    private static final BlockPos SHAFT   = new BlockPos(2, 1, 2);

    public static void usage(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("biofuel_engine_usage", "Using the Biofuel Engine");
        scene.configureBasePlate(0, 0, 5);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);

        scene.world().showSection(util.select().position(SHAFT), Direction.UP);
        scene.world().showSection(util.select().position(MACHINE), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("The Biofuel Engine burns Biofuel to generate Rotational Force")
                .pointAt(util.vector().centerOf(MACHINE))
                .attachKeyFrame();
        scene.idle(80);

        scene.world().modifyBlockEntity(MACHINE, BiofuelEngineBlockEntity.class, be ->
                be.biofuelTank.fill(new FluidStack(ModFluids.BIOFUEL_SOURCE.get(), 4000),
                        IFluidHandler.FluidAction.EXECUTE));
        scene.overlay().showText(60)
                .text("Pipe Biofuel into any face to fill the internal tank")
                .pointAt(util.vector().centerOf(MACHINE))
                .attachKeyFrame();
        scene.idle(70);

        scene.world().modifyBlock(MACHINE, s -> s.setValue(BiofuelEngineBlock.LIT, true), false);
        scene.world().setKineticSpeed(util.select().position(MACHINE), 16f);
        scene.world().setKineticSpeed(util.select().position(SHAFT), 16f);
        scene.overlay().showText(70)
                .text("With Biofuel present, it outputs 16 RPM with 8192 SU of stress capacity")
                .pointAt(util.vector().centerOf(MACHINE))
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(60)
                .text("Rotation exits downward - connect a shaft or machine directly below")
                .pointAt(util.vector().centerOf(SHAFT))
                .attachKeyFrame();
        scene.idle(70);

        scene.markAsFinished();
    }
}