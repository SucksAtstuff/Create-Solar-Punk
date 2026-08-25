package net.succ.solar_punk.compat.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.succ.solar_punk.block.custom.FusionReactorCoreBlock;
import net.succ.solar_punk.block.entity.custom.FusionReactorCoreBlockEntity;

public class FusionReactorScenes {

    // --- Fusion Reactor structure ----------------------------------------------------

    // Fixed voxel-sphere shell, mirroring FusionReactorCoreBlockEntity.scanStructure():
    // the Core sits at the exact center, everything at rounded 3D distance 4 is the
    // (full) Casing shell, everything at rounded distance 3 on one of the three
    // great-circle rings (isRingPosition()) is the Blanket band, everything else at
    // that distance is left open. Matches the schematic laid out in ModPonderProvider.
    private static final BlockPos CORE = new BlockPos(4, 5, 4);
    private static final int SHELL_RADIUS = 4;
    private static final int BLANKET_RADIUS = 3;

    private static void revealShell(SceneBuilder scene, SceneBuildingUtil util, int dist, boolean ringOnly) {
        for (int dx = -SHELL_RADIUS; dx <= SHELL_RADIUS; dx++)
            for (int dy = -SHELL_RADIUS; dy <= SHELL_RADIUS; dy++)
                for (int dz = -SHELL_RADIUS; dz <= SHELL_RADIUS; dz++)
                    if ((!ringOnly || FusionReactorCoreBlockEntity.isRingPosition(dx, dy, dz))
                            && Math.round((float) Math.sqrt(dx * dx + dy * dy + dz * dz)) == dist)
                        scene.world().showSection(util.select().position(CORE.offset(dx, dy, dz)), Direction.DOWN);
    }

    // A "peek inside" cutaway wedge, used once the reactor has formed to open a clear
    // window straight to the Core - the same front-facing diagonal the Casing Glass
    // corner (dx+dz <= -5) sits in, just widened so there's an actual gap to see
    // through rather than a single glass pane's worth of sightline.
    private static final int WEDGE_CUTOFF = -2;

    private static void setWedgeShown(SceneBuilder scene, SceneBuildingUtil util, boolean shown) {
        for (int dx = -SHELL_RADIUS; dx <= SHELL_RADIUS; dx++)
            for (int dy = -SHELL_RADIUS; dy <= SHELL_RADIUS; dy++)
                for (int dz = -SHELL_RADIUS; dz <= SHELL_RADIUS; dz++) {
                    if (dx + dz > WEDGE_CUTOFF) continue;
                    int dist = Math.round((float) Math.sqrt(dx * dx + dy * dy + dz * dz));
                    boolean isCasing = dist == SHELL_RADIUS;
                    boolean isBlanket = dist == BLANKET_RADIUS && FusionReactorCoreBlockEntity.isRingPosition(dx, dy, dz);
                    if (!isCasing && !isBlanket) continue;

                    var selection = util.select().position(CORE.offset(dx, dy, dz));
                    if (shown) scene.world().showSection(selection, Direction.DOWN);
                    else scene.world().hideSection(selection, Direction.UP);
                }
    }

    public static void structure(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("fusion_reactor_structure", "Building the Fusion Reactor");
        scene.configureBasePlate(0, 0, 9);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);

        scene.world().showSection(util.select().position(CORE), Direction.DOWN);
        scene.idle(15);

        scene.overlay().showText(90)
                .text("The Fusion Reactor Core sits at the exact center of a fixed spherical shell - it doesn't grow taller like the Steam Turbine, it's always this one size")
                .pointAt(util.vector().centerOf(CORE))
                .attachKeyFrame();
        scene.idle(100);

        revealShell(scene, util, BLANKET_RADIUS, true);
        scene.idle(20);

        scene.overlay().showText(90)
                .text("Fill the inner ring band with Blanket Modules - it's an open gyroscope skeleton, not a sealed ball, so the Core stays visible through the gaps. Lithium Breeder for efficient fuel use, Beryllium Reflector for raw output, mixed in whatever ratio you want")
                .pointAt(util.vector().centerOf(CORE.offset(-BLANKET_RADIUS, 0, 0)))
                .attachKeyFrame();
        scene.idle(100);

        revealShell(scene, util, SHELL_RADIUS, false);
        scene.idle(20);

        scene.overlay().showText(80)
                .text("Enclose the whole shell in Fusion Reactor Casing - swap in Casing Glass anywhere for a clear view straight through to the open Blanket band underneath")
                .pointAt(util.vector().centerOf(CORE.offset(0, 0, SHELL_RADIUS)))
                .attachKeyFrame();
        scene.idle(90);

        scene.world().modifyBlockEntity(CORE, FusionReactorCoreBlockEntity.class, be -> {
            be.formed = true;
            be.lithiumModuleCount = 41;
            be.berylliumModuleCount = 57;
            be.blanketEfficiency = 57f / 98f;
        });
        scene.world().modifyBlock(CORE, s -> s.setValue(FusionReactorCoreBlock.FORMED, true), false);
        scene.overlay().showText(80)
                .text("Once the shell and blanket band are complete, the reactor lights up - check it with Goggles to see its status and blanket mix")
                .pointAt(util.vector().topOf(CORE))
                .attachKeyFrame();
        scene.idle(90);

        setWedgeShown(scene, util, false);
        scene.idle(20);

        scene.overlay().showText(100)
                .text("The Core itself disappears once the reactor is fully formed - all that's left floating here is the glowing rings and crackling energy")
                .pointAt(util.vector().centerOf(CORE))
                .attachKeyFrame();
        scene.idle(110);

        setWedgeShown(scene, util, true);
        scene.idle(20);

        scene.overlay().showText(90)
                .text("More Beryllium in the blanket burns fuel faster for more steam output; more Lithium breeds Tritium more efficiently for a lower, steadier output")
                .pointAt(util.vector().centerOf(CORE))
                .attachKeyFrame();
        scene.idle(100);

        scene.overlay().showText(80)
                .text("Feed it with Deuterium and Lithium - both come from Basin recipes, Heated Water + Copper for Deuterium, Superheated Water + Salt for Lithium")
                .pointAt(util.vector().centerOf(CORE))
                .attachKeyFrame();
        scene.idle(90);

        scene.markAsFinished();
    }
}
