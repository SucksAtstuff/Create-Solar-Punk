package net.succ.solar_punk.compat.ponder;

import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.succ.solar_punk.block.custom.SolarPowerTowerBlock;
import net.succ.solar_punk.block.entity.custom.SolarPowerTowerBlockEntity;
import net.succ.solar_punk.fluid.ModFluids;

public class SolarPowerTowerScenes {

    // Tower occupies x=1-3, z=1-3, y=1-3. Controller is bottom-left corner (1,1,1).
    private static final BlockPos CONTROLLER = new BlockPos(1, 1, 1);
    private static final BlockPos CENTER     = new BlockPos(2, 2, 2);
    private static final BlockPos TOP        = new BlockPos(2, 3, 2);
    private static final BlockPos BOT        = new BlockPos(2, 1, 2);

    /** Shows all 27 tower blocks with no idle between them so they all exist at the same render frame. */
    private static void showTower(SceneBuilder scene, SceneBuildingUtil util) {
        for (int y = 1; y <= 3; y++)
            for (int x = 1; x <= 3; x++)
                for (int z = 1; z <= 3; z++)
                    scene.world().showSection(util.select().position(new BlockPos(x, y, z)), Direction.DOWN);
    }

    public static void usage(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("solar_power_tower_usage", "Using the Solar Power Tower");
        scene.configureBasePlate(0, 0, 5);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);

        showTower(scene, util);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Stack Solar Power Tower blocks vertically — they merge into a single multiblock")
                .pointAt(util.vector().topOf(TOP))
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(70)
                .text("The tower requires at least a 3x3 footprint and 3 blocks tall to produce anything")
                .pointAt(util.vector().centerOf(CENTER))
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(60)
                .text("Pipe water in through any side face to fill the water tank")
                .pointAt(util.vector().centerOf(BOT))
                .attachKeyFrame();
        scene.world().modifyBlockEntity(CONTROLLER, SolarPowerTowerBlockEntity.class, be ->
                be.waterTank.fill(new FluidStack(Fluids.WATER, 16000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(70);

        scene.overlay().showText(80)
                .text("During the day with a clear sky, the tower converts water into Molten Salt — rain and night stop production")
                .pointAt(util.vector().topOf(TOP))
                .attachKeyFrame();
        scene.world().modifyBlock(BOT,    s -> s.setValue(SolarPowerTowerBlock.LIT, true), false);
        scene.world().modifyBlock(CENTER, s -> s.setValue(SolarPowerTowerBlock.LIT, true), false);
        scene.world().modifyBlock(TOP,    s -> s.setValue(SolarPowerTowerBlock.LIT, true), false);
        scene.world().modifyBlockEntity(CONTROLLER, SolarPowerTowerBlockEntity.class, be ->
                be.saltTank.fill(new FluidStack(ModFluids.MOLTEN_SALT_SOURCE.get(), 8000),
                        IFluidHandler.FluidAction.EXECUTE));
        scene.idle(90);

        for (int y = 1; y <= 3; y++)
            for (int z = 1; z <= 3; z++) {
                scene.world().showSection(util.select().position(new BlockPos(0, y, z)), Direction.EAST);
                scene.world().showSection(util.select().position(new BlockPos(4, y, z)), Direction.WEST);
            }
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Place Solar Mirrors on the tower's side faces to boost output — see the Solar Mirrors scene for details")
                .pointAt(util.vector().centerOf(CENTER))
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(60)
                .text("Drain the Molten Salt from the output and pipe it to a Heat Battery")
                .pointAt(util.vector().centerOf(BOT))
                .attachKeyFrame();
        scene.idle(70);

        scene.markAsFinished();
    }

    public static void mirrors(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("solar_power_tower_mirrors", "Solar Mirrors");
        scene.configureBasePlate(0, 0, 5);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);

        // Show tower and all mirrors with no idle between so connected textures see their neighbours
        showTower(scene, util);
        for (int y = 1; y <= 3; y++)
            for (int z = 1; z <= 3; z++) {
                scene.world().showSection(util.select().position(new BlockPos(0, y, z)), Direction.EAST);
                scene.world().showSection(util.select().position(new BlockPos(4, y, z)), Direction.WEST);
            }
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Place Solar Mirrors directly against the tower's side faces to increase output")
                .pointAt(util.vector().centerOf(CENTER))
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(80)
                .text("Mirror efficiency follows a triangle curve — it peaks when roughly half the available side faces are covered")
                .pointAt(util.vector().centerOf(CENTER))
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showText(70)
                .text("Adding mirrors past twice the optimal count reduces efficiency to zero — do not over-mirror")
                .pointAt(util.vector().centerOf(CENTER))
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(60)
                .text("Taller and wider towers have more wall space for mirrors and a higher maximum output rate")
                .pointAt(util.vector().topOf(TOP))
                .attachKeyFrame();
        scene.idle(70);

        scene.markAsFinished();
    }
}
