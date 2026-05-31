package net.succ.solar_punk.compat.ponder;

import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class SteamTurbineScenes {

    // Structure scene layout: floor(y=1, no rotor), blades(y=2-4), cap(y=5, rotor at center)
    private static final BlockPos ROTOR_3     = new BlockPos(3, 3, 3);
    private static final BlockPos CAP_ROTOR_5 = new BlockPos(3, 5, 3);

    // Show the 24 border positions of the 7x7 ring at a given y level
    private static void showRing(SceneBuilder scene, SceneBuildingUtil util, int y) {
        for (int x = 0; x <= 6; x++)
            for (int z = 0; z <= 6; z++)
                if (x == 0 || x == 6 || z == 0 || z == 6)
                    scene.world().showSection(util.select().position(new BlockPos(x, y, z)), Direction.DOWN);
    }

    // Show the 8 blade positions at a given y level (+ pattern, 2 blades per arm)
    private static void showBlades(SceneBuilder scene, SceneBuildingUtil util, int y) {
        scene.world().showSection(util.select().position(new BlockPos(4, y, 3)), Direction.DOWN);
        scene.world().showSection(util.select().position(new BlockPos(5, y, 3)), Direction.DOWN);
        scene.world().showSection(util.select().position(new BlockPos(2, y, 3)), Direction.DOWN);
        scene.world().showSection(util.select().position(new BlockPos(1, y, 3)), Direction.DOWN);
        scene.world().showSection(util.select().position(new BlockPos(3, y, 4)), Direction.DOWN);
        scene.world().showSection(util.select().position(new BlockPos(3, y, 5)), Direction.DOWN);
        scene.world().showSection(util.select().position(new BlockPos(3, y, 2)), Direction.DOWN);
        scene.world().showSection(util.select().position(new BlockPos(3, y, 1)), Direction.DOWN);
    }

    // Show the sealed interior of the cap layer (5x5 minus center rotor)
    private static void showCapInterior(SceneBuilder scene, SceneBuildingUtil util, int y) {
        for (int x = 1; x <= 5; x++)
            for (int z = 1; z <= 5; z++)
                if (x != 3 || z != 3)
                    scene.world().showSection(util.select().position(new BlockPos(x, y, z)), Direction.DOWN);
    }

    // Show the full sealed interior of the floor layer (5x5 including center - all casing, no rotor)
    private static void showFloorInterior(SceneBuilder scene, SceneBuildingUtil util, int y) {
        for (int x = 1; x <= 5; x++)
            for (int z = 1; z <= 5; z++)
                scene.world().showSection(util.select().position(new BlockPos(x, y, z)), Direction.DOWN);
    }

    /**
     * Scene 1 - walks through building a minimum turbine component by component.
     * Layout: floor(y=1) + blades(y=2,3,4) + cap(y=5).
     */
    public static void structure(SceneBuilder scene, SceneBuildingUtil util) {
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
                .text("Two blades per arm, four arms - eight blades total per layer. Andesite is cheaper; Brass is more efficient")
                .pointAt(util.vector().centerOf(new BlockPos(5, 2, 3)))
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

        scene.overlay().showText(60)
                .text("Rotational power exits from the top face of the cap rotor - connect a shaft directly above")
                .pointAt(util.vector().topOf(CAP_ROTOR_5))
                .attachKeyFrame();
        scene.idle(70);

        scene.markAsFinished();
    }

    /**
     * Scene 2 - shows a full 7-blade-layer, all-brass turbine.
     * Layout: floor(y=1) + blades(y=2-8) + cap(y=9).
     */
    public static void maxTurbine(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("turbine_max", "Maximum Efficiency Turbine");
        scene.configureBasePlate(0, 0, 7);

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
}