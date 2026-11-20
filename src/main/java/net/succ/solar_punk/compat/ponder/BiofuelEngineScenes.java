package net.succ.solar_punk.compat.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.succ.solar_punk.block.custom.BiofuelEngineBlock;
import net.succ.solar_punk.block.entity.custom.BiofuelEngineBlockEntity;
import net.succ.solar_punk.fluid.ModFluids;

// Schematic (ModPonderProvider "biofuel_engine/usage"): the engine at (1,2,2) on a Gearbox, a shaft
// east into a Mechanical Drill facing a block of Andesite at (4,1,2), and a fuel pipe into the
// engine's west side.
public class BiofuelEngineScenes {

    private static final BlockPos MACHINE = new BlockPos(1, 2, 2);
    private static final BlockPos GEARBOX = new BlockPos(1, 1, 2);
    private static final BlockPos PIPE = new BlockPos(0, 2, 2);
    private static final BlockPos ROCK = new BlockPos(4, 1, 2);

    public static void usage(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("biofuel_engine_usage", "Using the Biofuel Engine");
        scene.configureBasePlate(0, 0, 5);

        Selection drivetrain = util.select().position(MACHINE).add(util.select().fromTo(1, 1, 2, 3, 1, 2));

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().position(GEARBOX), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(MACHINE), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("The Biofuel Engine burns Biofuel to make Rotational Force")
                .pointAt(util.vector().topOf(MACHINE))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().showSection(util.select().position(PIPE), Direction.EAST);
        scene.idle(10);
        scene.overlay().showControls(util.vector().topOf(PIPE), Pointing.DOWN, 30)
                .withItem(new ItemStack(ModFluids.BIOFUEL_BUCKET.get()));
        scene.idle(35);
        scene.overlay().showText(70)
                .text("Pipe Biofuel into any side to fill its tank")
                .pointAt(util.vector().centerOf(PIPE))
                .placeNearTarget()
                .attachKeyFrame();
        scene.world().modifyBlockEntity(MACHINE, BiofuelEngineBlockEntity.class, be ->
                be.biofuelTank.fill(new FluidStack(ModFluids.BIOFUEL_SOURCE.get(), 4000),
                        IFluidHandler.FluidAction.EXECUTE));
        scene.idle(80);

        scene.world().showSection(util.select().fromTo(2, 1, 2, 4, 1, 2), Direction.DOWN);
        scene.idle(15);
        scene.world().modifyBlock(MACHINE, s -> s.setValue(BiofuelEngineBlock.LIT, true), false);
        scene.world().setKineticSpeed(drivetrain, 16f);
        scene.effects().indicateSuccess(MACHINE);
        scene.overlay().showText(70)
                .text("While it has fuel it gives 16 RPM and 8192 SU of stress capacity, out of the bottom")
                .pointAt(util.vector().centerOf(MACHINE))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(30);
        for (int i = 0; i < 10; i++) {
            scene.idle(6);
            scene.world().incrementBlockBreakingProgress(ROCK);
        }
        scene.world().restoreBlocks(util.select().position(ROCK));
        scene.idle(30);

        scene.overlay().showText(70)
                .text("That's plenty for heavy machinery, like this Mechanical Drill")
                .pointAt(util.vector().topOf(3, 1, 2))
                .placeNearTarget();
        for (int i = 0; i < 10; i++) {
            scene.idle(6);
            scene.world().incrementBlockBreakingProgress(ROCK);
        }
        scene.world().setBlock(ROCK, Blocks.ANDESITE.defaultBlockState(), false);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("A full tank of 8 buckets lasts about 5 minutes")
                .pointAt(util.vector().topOf(MACHINE))
                .placeNearTarget();
        scene.idle(80);

        scene.world().modifyBlockEntity(MACHINE, BiofuelEngineBlockEntity.class, be ->
                be.biofuelTank.setFluid(FluidStack.EMPTY));
        scene.world().modifyBlock(MACHINE, s -> s.setValue(BiofuelEngineBlock.LIT, false), false);
        scene.world().setKineticSpeed(drivetrain, 0f);
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
