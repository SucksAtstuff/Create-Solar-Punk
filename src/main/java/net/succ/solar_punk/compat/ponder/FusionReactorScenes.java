package net.succ.solar_punk.compat.ponder;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.succ.solar_punk.block.custom.FusionReactorCoreBlock;
import net.succ.solar_punk.block.entity.custom.FusionReactorCoreBlockEntity;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.item.ModItems;

public class FusionReactorScenes {

    // --- Fusion Reactor structure ----------------------------------------------------

    // Fixed voxel-sphere shell, mirroring FusionReactorCoreBlockEntity.scanStructure():
    // the Core sits at the exact center, isCasingPosition() is the Casing shell and
    // isBlanketPosition() is the Blanket band. Matches the schematic laid out in ModPonderProvider.
    private static final BlockPos CORE = new BlockPos(4, 5, 4);
    private static final int SHELL_RADIUS = FusionReactorCoreBlockEntity.SCAN_RADIUS;

    private static void revealShell(SceneBuilder scene, SceneBuildingUtil util, boolean blanket) {
        for (int dx = -SHELL_RADIUS; dx <= SHELL_RADIUS; dx++)
            for (int dy = -SHELL_RADIUS; dy <= SHELL_RADIUS; dy++)
                for (int dz = -SHELL_RADIUS; dz <= SHELL_RADIUS; dz++)
                    if (blanket ? FusionReactorCoreBlockEntity.isBlanketPosition(dx, dy, dz)
                                : FusionReactorCoreBlockEntity.isCasingPosition(dx, dy, dz))
                        scene.world().showSection(util.select().position(CORE.offset(dx, dy, dz)), Direction.DOWN);
    }

    // A cutaway wedge, hidden once the reactor has formed to open a clear view of the
    // Core. It's the same front-facing diagonal as the Casing Glass corner (dx+dz <= -5),
    // widened so there's an actual gap to see through.
    private static final int WEDGE_CUTOFF = -2;

    private static void setWedgeShown(SceneBuilder scene, SceneBuildingUtil util, boolean shown) {
        for (int dx = -SHELL_RADIUS; dx <= SHELL_RADIUS; dx++)
            for (int dy = -SHELL_RADIUS; dy <= SHELL_RADIUS; dy++)
                for (int dz = -SHELL_RADIUS; dz <= SHELL_RADIUS; dz++) {
                    if (dx + dz > WEDGE_CUTOFF) continue;
                    boolean isCasing = FusionReactorCoreBlockEntity.isCasingPosition(dx, dy, dz);
                    boolean isBlanket = FusionReactorCoreBlockEntity.isBlanketPosition(dx, dy, dz);
                    if (!isCasing && !isBlanket) continue;

                    var selection = util.select().position(CORE.offset(dx, dy, dz));
                    if (shown) scene.world().showSection(selection, Direction.DOWN);
                    else scene.world().hideSection(selection, Direction.UP);
                }
    }

    public static void structure(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("fusion_reactor_structure", "Building the Fusion Reactor");
        scene.configureBasePlate(0, 0, 9);
        // The 9x9x10 sphere overflows the default view on smaller/wider windows
        scene.scaleSceneView(0.7f);
        scene.setSceneOffsetY(-1f);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);

        scene.world().showSection(util.select().position(CORE), Direction.DOWN);
        scene.idle(15);

        scene.overlay().showText(90)
                .text("The Fusion Reactor Core sits at the exact center of a fixed spherical shell - it doesn't grow taller like the Steam Turbine, it's always this one size")
                .pointAt(util.vector().centerOf(CORE))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(100);

        revealShell(scene, util, true);
        scene.idle(20);

        scene.overlay().showText(90)
                .text("Fill the inner ring band with Blanket Modules - it's an open gyroscope skeleton, not a sealed ball, so the Core stays visible through the gaps. Lithium Breeder for efficient fuel use, Beryllium Reflector for raw output, mixed in whatever ratio you want")
                .pointAt(util.vector().centerOf(CORE.offset(-FusionReactorCoreBlockEntity.BLANKET_BAND_DIST, 0, 0)))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(100);

        revealShell(scene, util, false);
        scene.idle(20);

        scene.overlay().showText(80)
                .text("Enclose the whole shell in Fusion Reactor Casing - swap in Casing Glass anywhere for a clear view straight through to the open Blanket band underneath")
                .pointAt(util.vector().centerOf(CORE.offset(0, 0, SHELL_RADIUS)))
                .placeNearTarget()
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
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        setWedgeShown(scene, util, false);
        scene.idle(20);

        scene.overlay().showText(100)
                .text("The Core itself disappears once the reactor is fully formed - all that's left floating here is the glowing rings and crackling energy")
                .pointAt(util.vector().centerOf(CORE))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(110);

        setWedgeShown(scene, util, true);
        scene.idle(20);

        scene.overlay().showText(90)
                .text("More Beryllium in the blanket burns fuel faster for more steam output; more Lithium breeds Tritium more efficiently for a lower, steadier output")
                .pointAt(util.vector().centerOf(CORE))
                .attachKeyFrame();
        scene.idle(100);

        // The reactor is fed and drained through its Casing; point at the shell's front face
        BlockPos shellFront = CORE.offset(0, 0, -SHELL_RADIUS);

        scene.overlay().showControls(util.vector().blockSurface(shellFront, Direction.NORTH), Pointing.LEFT, 80)
                .withItem(new ItemStack(ModFluids.DEUTERIUM_BUCKET.get()));
        scene.overlay().showControls(util.vector().topOf(shellFront), Pointing.DOWN, 80)
                .withItem(new ItemStack(ModItems.LITHIUM_DUST.get()));
        scene.overlay().showText(80)
                .text("Pipe Deuterium and feed Lithium Dust into any Casing block. Both come from Basin mixing: Deuterium from heated Water and Copper, Lithium from superheated Water and Salt")
                .pointAt(util.vector().blockSurface(shellFront, Direction.NORTH))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showControls(util.vector().topOf(shellFront), Pointing.DOWN, 80)
                .withItem(new ItemStack(ModItems.TRITIUM_CAPSULE.get()));
        scene.overlay().showText(80)
                .text("Tritium Capsules from a Particle Accelerator burn before Lithium, with no breeding waste. The empty capsules come back out")
                .pointAt(util.vector().topOf(shellFront))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showControls(util.vector().blockSurface(shellFront, Direction.NORTH), Pointing.LEFT, 70)
                .withItem(new ItemStack(Items.LEVER));
        scene.effects().indicateRedstone(shellFront);
        scene.overlay().showText(70)
                .text("A Redstone signal on any Casing block sets the throttle - a stronger signal burns more fuel. No signal, no power")
                .pointAt(util.vector().blockSurface(shellFront, Direction.NORTH))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showControls(util.vector().blockSurface(shellFront, Direction.NORTH), Pointing.LEFT, 70)
                .withItem(new ItemStack(ModFluids.STEAM_BUCKET.get()));
        scene.overlay().showText(70)
                .text("Steam comes out of any Casing block - enough for a whole bank of Steam Turbines")
                .pointAt(util.vector().blockSurface(shellFront, Direction.NORTH))
                .placeNearTarget();
        scene.idle(80);

        scene.markAsFinished();
    }
}
