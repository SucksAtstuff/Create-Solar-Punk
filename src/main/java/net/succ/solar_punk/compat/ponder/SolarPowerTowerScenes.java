package net.succ.solar_punk.compat.ponder;

import com.simibubi.create.AllItems;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.succ.solar_punk.block.custom.SolarPowerTowerBlock;
import net.succ.solar_punk.block.entity.custom.SolarMirrorBlockEntity;
import net.succ.solar_punk.block.entity.custom.SolarPowerTowerBlockEntity;
import net.succ.solar_punk.fluid.ModFluids;

public class SolarPowerTowerScenes {

    // Tower occupies x=5-7, z=5-7, y=1-3 - pushed to the back corner of the plate so
    // the mirror field in front of it (toward lower x/z) isn't hidden behind it.
    // Controller is the bottom-nearest corner (5,1,5).
    private static final BlockPos CONTROLLER = new BlockPos(5, 1, 5);
    private static final BlockPos CENTER     = new BlockPos(6, 2, 6);
    private static final BlockPos TOP        = new BlockPos(6, 3, 6);
    private static final BlockPos BOT        = new BlockPos(6, 1, 6);

    /** Shows all 27 tower blocks with no idle between them so they all exist at the same render frame. */
    private static void showTower(SceneBuilder scene, SceneBuildingUtil util) {
        for (int y = 1; y <= 3; y++)
            for (int x = 5; x <= 7; x++)
                for (int z = 5; z <= 7; z++)
                    scene.world().showSection(util.select().position(new BlockPos(x, y, z)), Direction.DOWN);
    }

    public static void usage(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("solar_power_tower_usage", "Using the Solar Power Tower");
        scene.configureBasePlate(0, 0, 9);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);

        showTower(scene, util);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Stack Solar Power Tower blocks vertically - they merge into a single multiblock")
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
                .text("During the day with a clear sky, the tower converts water into Molten Salt - rain and night stop production")
                .pointAt(util.vector().topOf(TOP))
                .attachKeyFrame();
        scene.world().modifyBlock(BOT,    s -> s.setValue(SolarPowerTowerBlock.LIT, true), false);
        scene.world().modifyBlock(CENTER, s -> s.setValue(SolarPowerTowerBlock.LIT, true), false);
        scene.world().modifyBlock(TOP,    s -> s.setValue(SolarPowerTowerBlock.LIT, true), false);
        scene.world().modifyBlockEntity(CONTROLLER, SolarPowerTowerBlockEntity.class, be ->
                be.saltTank.fill(new FluidStack(ModFluids.MOLTEN_SALT_SOURCE.get(), 8000),
                        IFluidHandler.FluidAction.EXECUTE));
        scene.idle(90);

        scene.overlay().showText(70)
                .text("Solar Mirrors placed nearby link to the tower automatically and boost output - see the Solar Mirrors scene for details")
                .pointAt(util.vector().centerOf(CENTER))
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(60)
                .text("Drain the Molten Salt from the output and pipe it to a Heat Battery")
                .pointAt(util.vector().centerOf(BOT))
                .attachKeyFrame();
        scene.idle(70);

        // --- Mode switch ---
        scene.overlay().showControls(util.vector().centerOf(CENTER), Pointing.RIGHT, 40)
                .rightClick()
                .withItem(new ItemStack(AllItems.WRENCH.get()));
        scene.idle(10);
        scene.world().modifyBlockEntity(CONTROLLER, SolarPowerTowerBlockEntity.class, be -> {
            be.steamMode = true;
            be.saltTank.setFluid(FluidStack.EMPTY);
        });
        scene.idle(30);

        scene.overlay().showText(70)
                .text("Right-click the tower with a Wrench to switch it to Steam mode - the stored fluid is cleared on switch")
                .pointAt(util.vector().centerOf(CENTER))
                .attachKeyFrame();
        scene.world().modifyBlockEntity(CONTROLLER, SolarPowerTowerBlockEntity.class, be ->
                be.steamTank.fill(new FluidStack(ModFluids.STEAM_SOURCE.get(), 8000),
                        IFluidHandler.FluidAction.EXECUTE));
        scene.idle(80);

        scene.overlay().showText(60)
                .text("In Steam mode the tower produces Steam directly - pipe it to a Steam Turbine or other consumer")
                .pointAt(util.vector().centerOf(BOT))
                .attachKeyFrame();
        scene.idle(70);

        scene.markAsFinished();
    }

    /** Reveals a ground-mounted mirror's lower and upper halves (see SolarMirrorBlock). */
    private static void showMirror(SceneBuilder scene, SceneBuildingUtil util, int x, int z) {
        scene.world().showSection(util.select().position(new BlockPos(x, 1, z)), Direction.DOWN);
        scene.world().showSection(util.select().position(new BlockPos(x, 2, z)), Direction.DOWN);
    }

    // West side (near + far) and north side (near + far) - kept a couple of blocks
    // clear of the tower's walls rather than right up against them.
    private static final BlockPos[] MIRRORS = {
            new BlockPos(3, 1, 5), new BlockPos(3, 1, 7),
            new BlockPos(0, 1, 5), new BlockPos(0, 1, 7),
            new BlockPos(5, 1, 3), new BlockPos(7, 1, 3),
            new BlockPos(5, 1, 0), new BlockPos(7, 1, 0),
    };

    public static void mirrors(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("solar_power_tower_mirrors", "Solar Mirrors");
        scene.configureBasePlate(0, 0, 9);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);

        showTower(scene, util);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Solar Mirrors are freestanding - place them on the ground, they don't attach to the tower")
                .pointAt(util.vector().centerOf(CENTER))
                .attachKeyFrame();
        scene.idle(80);

        showMirror(scene, util, MIRRORS[0].getX(), MIRRORS[0].getZ());
        scene.idle(15);

        scene.overlay().showText(80)
                .text("A mirror links automatically if it has open sky above it and a clear line of sight to the tower")
                .pointAt(util.vector().centerOf(MIRRORS[0]))
                .attachKeyFrame();
        scene.idle(90);

        for (int i = 1; i < MIRRORS.length; i++)
            showMirror(scene, util, MIRRORS[i].getX(), MIRRORS[i].getZ());
        scene.idle(15);

        scene.overlay().showText(80)
                .text("The search radius scales with the tower's height, and taller towers can track more mirrors at once")
                .pointAt(util.vector().topOf(TOP))
                .attachKeyFrame();
        scene.idle(90);

        // Actually link every mirror to the tower and let the real renderer do the work -
        // it keeps each static base fixed and only turns the post (azimuth) and tilts the
        // plate (elevation), exactly like it does in normal gameplay.
        for (BlockPos mirror : MIRRORS)
            scene.world().modifyBlockEntity(mirror, SolarMirrorBlockEntity.class, be ->
                    be.setLinkedTower(CONTROLLER));
        scene.overlay().showText(90)
                .text("Each linked mirror's post turns to face the tower, and its plate tilts to bisect the sun and the tower - tracking the sun all day")
                .pointAt(util.vector().centerOf(CENTER))
                .attachKeyFrame();
        scene.idle(100);

        scene.overlay().showText(80)
                .text("Efficiency follows a triangle curve peaking at an optimal mirror count for the field - doubling past that drops it back to zero")
                .pointAt(util.vector().centerOf(CENTER))
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showText(60)
                .text("Check a linked tower with Goggles to see its mirror count and current efficiency")
                .pointAt(util.vector().centerOf(CENTER))
                .attachKeyFrame();
        scene.idle(70);

        scene.markAsFinished();
    }
}
