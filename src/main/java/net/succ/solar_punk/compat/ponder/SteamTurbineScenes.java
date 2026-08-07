package net.succ.solar_punk.compat.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class SteamTurbineScenes {

    // Structure scene layout: floor(y=1, no rotor), blades(y=2-4), cap(y=5, rotor at center)
    private static final BlockPos ROTOR_3     = new BlockPos(3, 3, 3);
    private static final BlockPos CAP_ROTOR_5 = new BlockPos(3, 5, 3);

    // Show the 24 border positions of the 7x7 ring at a given y level
    private static void showRing(CreateSceneBuilder scene, SceneBuildingUtil util, int y) {
        for (int x = 0; x <= 6; x++)
            for (int z = 0; z <= 6; z++)
                if (x == 0 || x == 6 || z == 0 || z == 6)
                    scene.world().showSection(util.select().position(new BlockPos(x, y, z)), Direction.DOWN);
    }

    // Show the 4 blade positions at a given y level (+ pattern, 1 blade per arm)
    private static void showBlades(CreateSceneBuilder scene, SceneBuildingUtil util, int y) {
        scene.world().showSection(util.select().position(new BlockPos(4, y, 3)), Direction.DOWN);
        scene.world().showSection(util.select().position(new BlockPos(2, y, 3)), Direction.DOWN);
        scene.world().showSection(util.select().position(new BlockPos(3, y, 4)), Direction.DOWN);
        scene.world().showSection(util.select().position(new BlockPos(3, y, 2)), Direction.DOWN);
    }

    // Show the sealed interior of the cap layer (5x5 minus center rotor)
    private static void showCapInterior(CreateSceneBuilder scene, SceneBuildingUtil util, int y) {
        for (int x = 1; x <= 5; x++)
            for (int z = 1; z <= 5; z++)
                if (x != 3 || z != 3)
                    scene.world().showSection(util.select().position(new BlockPos(x, y, z)), Direction.DOWN);
    }

    // Show the full sealed interior of the floor layer (5x5 including center - all casing, no rotor)
    private static void showFloorInterior(CreateSceneBuilder scene, SceneBuildingUtil util, int y) {
        for (int x = 1; x <= 5; x++)
            for (int z = 1; z <= 5; z++)
                scene.world().showSection(util.select().position(new BlockPos(x, y, z)), Direction.DOWN);
    }

    // X-axis analogs of the 4 helpers above, for a turbine grown East/West instead of
    // up - the 7x7 ring now lies in the Y-Z plane at a given x.
    private static void showRingX(CreateSceneBuilder scene, SceneBuildingUtil util, int x) {
        for (int y = 0; y <= 6; y++)
            for (int z = 0; z <= 6; z++)
                if (y == 0 || y == 6 || z == 0 || z == 6)
                    scene.world().showSection(util.select().position(new BlockPos(x, y, z)), Direction.EAST);
    }

    private static void showBladesX(CreateSceneBuilder scene, SceneBuildingUtil util, int x) {
        scene.world().showSection(util.select().position(new BlockPos(x, 4, 3)), Direction.EAST);
        scene.world().showSection(util.select().position(new BlockPos(x, 2, 3)), Direction.EAST);
        scene.world().showSection(util.select().position(new BlockPos(x, 3, 4)), Direction.EAST);
        scene.world().showSection(util.select().position(new BlockPos(x, 3, 2)), Direction.EAST);
    }

    private static void showCapInteriorX(CreateSceneBuilder scene, SceneBuildingUtil util, int x) {
        for (int y = 1; y <= 5; y++)
            for (int z = 1; z <= 5; z++)
                if (y != 3 || z != 3)
                    scene.world().showSection(util.select().position(new BlockPos(x, y, z)), Direction.EAST);
    }

    private static void showFloorInteriorX(CreateSceneBuilder scene, SceneBuildingUtil util, int x) {
        for (int y = 1; y <= 5; y++)
            for (int z = 1; z <= 5; z++)
                scene.world().showSection(util.select().position(new BlockPos(x, y, z)), Direction.EAST);
    }

    /**
     * Scene 1 - walks through building a minimum turbine component by component.
     * Layout: floor(y=1) + blades(y=2,3,4) + cap(y=5).
     */
    public static void structure(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("turbine_structure", "Building the Steam Turbine");
        scene.configureBasePlate(0, 0, 7);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);

        // --- Floor layer (all casing, no rotor) ---
        showRing(scene, util, 1);
        showFloorInterior(scene, util, 1);
        scene.idle(10);

        scene.overlay().showText(80)
                .text("Start with a sealed floor - fill the entire 7x7 footprint with Turbine Casing, including the center")
                .pointAt(util.vector().centerOf(new BlockPos(3, 1, 3)))
                .attachKeyFrame();
        scene.idle(90);

        // --- First blade layer ---
        showRing(scene, util, 2);
        scene.idle(5);
        scene.world().showSection(util.select().position(new BlockPos(3, 2, 3)), Direction.DOWN);
        showBlades(scene, util, 2);
        scene.idle(10);

        scene.overlay().showText(80)
                .text("Above the floor, build ring-only layers with a Rotor at center and Turbine Blades in the plus pattern")
                .pointAt(util.vector().centerOf(new BlockPos(4, 2, 3)))
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showText(70)
                .text("One blade per arm, four arms - four blades total per layer. Andesite is cheaper; Brass is more efficient")
                .pointAt(util.vector().centerOf(new BlockPos(4, 2, 3)))
                .attachKeyFrame();
        scene.idle(80);

        // --- Two more blade layers ---
        showRing(scene, util, 3);
        scene.world().showSection(util.select().position(ROTOR_3), Direction.DOWN);
        showBlades(scene, util, 3);
        scene.idle(4);
        showRing(scene, util, 4);
        scene.world().showSection(util.select().position(new BlockPos(3, 4, 3)), Direction.DOWN);
        showBlades(scene, util, 4);
        scene.idle(15);

        scene.overlay().showText(60)
                .text("Stack as many blade layers as you want - each adds more throughput")
                .pointAt(util.vector().topOf(ROTOR_3))
                .attachKeyFrame();
        scene.idle(70);

        // --- Top cap ---
        showRing(scene, util, 5);
        showCapInterior(scene, util, 5);
        scene.idle(5);
        scene.world().showSection(util.select().position(CAP_ROTOR_5), Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(80)
                .text("Seal the top the same way as the floor - full 7x7 of Turbine Casing with the Rotor at center")
                .pointAt(util.vector().topOf(CAP_ROTOR_5))
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showText(80)
                .text("Pipe Steam into any face of the outer casing wall - condensate water drains from any casing face too")
                .pointAt(util.vector().centerOf(ROTOR_3))
                .attachKeyFrame();
        scene.idle(90);

        scene.world().setKineticSpeed(util.select().fromTo(ROTOR_3, CAP_ROTOR_5), 16f);
        scene.overlay().showText(60)
                .text("Rotational power exits from the top face of the cap rotor - connect a shaft directly above")
                .pointAt(util.vector().topOf(CAP_ROTOR_5))
                .attachKeyFrame();
        scene.idle(70);

        scene.markAsFinished();
    }

    /**
     * Scene 2 - explains steam condensation and the condensate drain mechanic.
     * Uses same structure layout as scene 1.
     */
    public static void condensate(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("turbine_condensate", "Steam Condensation");
        scene.configureBasePlate(0, 0, 7);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);

        // Reveal the turbine quickly
        showRing(scene, util, 1);
        showFloorInterior(scene, util, 1);
        scene.idle(3);
        for (int y = 2; y <= 4; y++) {
            showRing(scene, util, y);
            scene.world().showSection(util.select().position(new BlockPos(3, y, 3)), Direction.DOWN);
            showBlades(scene, util, y);
            scene.idle(2);
        }
        showRing(scene, util, 5);
        showCapInterior(scene, util, 5);
        scene.idle(2);
        scene.world().showSection(util.select().position(CAP_ROTOR_5), Direction.DOWN);
        scene.idle(10);

        scene.world().setKineticSpeed(util.select().fromTo(ROTOR_3, CAP_ROTOR_5), 16f);

        scene.overlay().showText(80)
                .text("As steam drives the rotor it condenses back into water - this condensate collects inside the turbine")
                .pointAt(util.vector().centerOf(ROTOR_3))
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showText(70)
                .text("Check the condensate water level at any time using Engineer's Goggles on any Rotor block")
                .pointAt(util.vector().centerOf(ROTOR_3))
                .attachKeyFrame();
        scene.idle(80);

        scene.world().setKineticSpeed(util.select().fromTo(ROTOR_3, CAP_ROTOR_5), 0f);

        scene.overlay().showText(80)
                .text("When the condensate tank is full the turbine shuts down automatically - no more steam is consumed until the water is drained")
                .pointAt(util.vector().centerOf(ROTOR_3))
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showText(70)
                .text("Pipe the condensate water out of any casing face to drain it - the turbine restarts as soon as there is room")
                .pointAt(util.vector().centerOf(new BlockPos(0, 2, 3)))
                .attachKeyFrame();
        scene.idle(80);

        scene.markAsFinished();
    }

    /**
     * Scene 3 - shows a full 7-blade-layer, all-brass turbine.
     * Layout: floor(y=1) + blades(y=2-8) + cap(y=9).
     */
    public static void maxTurbine(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("turbine_max", "Maximum Efficiency Turbine");
        scene.configureBasePlate(0, 0, 11);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);

        // Floor
        scene.world().showSection(util.select().layer(1), Direction.DOWN);
        scene.idle(8);

        scene.overlay().showText(60)
                .text("The sealed floor is a full 7x7 of Turbine Casing with no rotor - it anchors the structure from below")
                .pointAt(util.vector().centerOf(new BlockPos(3, 1, 3)))
                .attachKeyFrame();
        scene.idle(70);

        // Blade layers 2-8
        for (int y = 2; y <= 8; y++) {
            scene.world().showSection(util.select().layer(y), Direction.DOWN);
            scene.idle(4);
        }
        scene.idle(10);

        scene.overlay().showText(80)
                .text("A taller turbine consumes more Steam per tick but produces proportionally more SU - height is the main throughput lever")
                .pointAt(util.vector().topOf(new BlockPos(3, 8, 3)))
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showText(70)
                .text("Fill every blade slot with Brass Blades for the best Steam-to-SU efficiency")
                .pointAt(util.vector().centerOf(new BlockPos(5, 5, 3)))
                .attachKeyFrame();
        scene.idle(80);

        // Top cap
        scene.world().showSection(util.select().layer(9), Direction.DOWN);
        scene.idle(10);

        scene.world().setKineticSpeed(util.select().fromTo(new BlockPos(3, 2, 3), new BlockPos(3, 9, 3)), 16f);
        scene.overlay().showText(80)
                .text("Cap the top identically to the floor - full 7x7 Casing with the Rotor at center. Power exits from the top of this rotor")
                .pointAt(util.vector().topOf(new BlockPos(3, 9, 3)))
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showText(70)
                .text("Replace any casing with Turbine Casing Glass anywhere in the structure to see inside - still valid")
                .pointAt(util.vector().centerOf(new BlockPos(0, 5, 3)))
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(70)
                .text("The maximum is 20 blade layers - at full brass and max height it can power an entire base")
                .pointAt(util.vector().topOf(new BlockPos(3, 9, 3)))
                .attachKeyFrame();
        scene.idle(80);

        scene.markAsFinished();
    }

    /**
     * Scene 4 - shows the exact same minimum structure as scene 1, but grown along the
     * X axis instead of standing up. Layout: floor(x=1) + 3 blade layers(x=2-4) +
     * cap(x=5), matching turbine_rotor/structure's size (x=0 left empty like that
     * scene's unused y=0 ground row).
     */
    public static void horizontal(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("turbine_horizontal", "Building On Its Side");
        scene.configureBasePlate(0, 0, 7);

        BlockPos masterRotor = new BlockPos(2, 3, 3);
        BlockPos capRotor    = new BlockPos(5, 3, 3);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);

        // --- Floor layer (x=1) ---
        showRingX(scene, util, 1);
        showFloorInteriorX(scene, util, 1);
        scene.idle(10);

        scene.overlay().showText(90)
                .text("The Steam Turbine doesn't have to stand up - the same shell also works lying on the East/West or North/South axis")
                .pointAt(util.vector().centerOf(new BlockPos(1, 3, 3)))
                .attachKeyFrame();
        scene.idle(100);

        // --- 3 blade layers (x=2,3,4) ---
        for (int x = 2; x <= 4; x++) {
            showRingX(scene, util, x);
            scene.world().showSection(util.select().position(new BlockPos(x, 3, 3)), Direction.EAST);
            showBladesX(scene, util, x);
            scene.idle(5);
        }
        scene.idle(10);

        scene.overlay().showText(80)
                .text("Place the first Rotor against the face pointing the way you want it to grow - the whole structure follows that axis")
                .pointAt(util.vector().centerOf(masterRotor))
                .attachKeyFrame();
        scene.idle(90);

        // --- Top cap (x=5) ---
        showRingX(scene, util, 5);
        showCapInteriorX(scene, util, 5);
        scene.world().showSection(util.select().position(capRotor), Direction.EAST);
        scene.idle(10);

        scene.world().setKineticSpeed(util.select().fromTo(masterRotor, capRotor), 16f);
        scene.overlay().showText(80)
                .text("It works exactly the same way from there - pipe in Steam, and power exits from the far Rotor")
                .pointAt(util.vector().centerOf(capRotor))
                .attachKeyFrame();
        scene.idle(90);

        scene.markAsFinished();
    }
}