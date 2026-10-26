package net.succ.solar_punk.compat.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
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
        scene.world().showSection(util.select().position(SHAFT), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(MACHINE), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("The Biofuel Engine burns Biofuel to make Rotational Force")
                .pointAt(util.vector().topOf(MACHINE))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showControls(util.vector().blockSurface(MACHINE, Direction.WEST), Pointing.LEFT, 70)
                .withItem(new ItemStack(ModFluids.BIOFUEL_BUCKET.get()));
        scene.overlay().showText(70)
                .text("Pipe Biofuel into any side to fill its tank")
                .pointAt(util.vector().blockSurface(MACHINE, Direction.WEST))
                .placeNearTarget()
                .attachKeyFrame();
        scene.world().modifyBlockEntity(MACHINE, BiofuelEngineBlockEntity.class, be ->
                be.biofuelTank.fill(new FluidStack(ModFluids.BIOFUEL_SOURCE.get(), 4000),
                        IFluidHandler.FluidAction.EXECUTE));
        scene.idle(80);

        scene.world().modifyBlock(MACHINE, s -> s.setValue(BiofuelEngineBlock.LIT, true), false);
        scene.world().setKineticSpeed(util.select().position(MACHINE).add(util.select().position(SHAFT)), 16f);
        scene.effects().indicateSuccess(MACHINE);
        scene.overlay().showText(70)
                .text("While it has fuel it gives 16 RPM and 8192 SU of stress capacity")
                .pointAt(util.vector().centerOf(MACHINE))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showOutline(PonderPalette.GREEN, "shaft", util.select().position(SHAFT), 60);
        scene.overlay().showText(60)
                .text("Rotation comes out of the bottom")
                .pointAt(util.vector().centerOf(SHAFT))
                .placeNearTarget();
        scene.idle(70);

        scene.overlay().showText(70)
                .text("A full tank of 8 buckets lasts about 5 minutes")
                .pointAt(util.vector().topOf(MACHINE))
                .placeNearTarget();
        scene.idle(80);

        scene.world().modifyBlockEntity(MACHINE, BiofuelEngineBlockEntity.class, be ->
                be.biofuelTank.setFluid(FluidStack.EMPTY));
        scene.world().modifyBlock(MACHINE, s -> s.setValue(BiofuelEngineBlock.LIT, false), false);
        scene.world().setKineticSpeed(util.select().position(MACHINE).add(util.select().position(SHAFT)), 0f);
        scene.overlay().showText(70)
                .colored(PonderPalette.RED)
                .text("Once the tank runs dry it stops. A Fermentation Vat can keep it supplied")
                .pointAt(util.vector().centerOf(MACHINE))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.markAsFinished();
    }
}
