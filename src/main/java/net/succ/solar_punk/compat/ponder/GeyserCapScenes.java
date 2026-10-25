package net.succ.solar_punk.compat.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.succ.solar_punk.block.custom.GeyserCapBlock;

public class GeyserCapScenes {

    private static final BlockPos VENT = new BlockPos(2, 1, 2);
    private static final BlockPos CAP = new BlockPos(2, 2, 2);
    private static final BlockPos SHAFT_WEST = new BlockPos(1, 2, 2);
    private static final BlockPos SHAFT_EAST = new BlockPos(3, 2, 2);

    public static void usage(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("geyser_cap_usage", "Geyser Cap");
        scene.configureBasePlate(0, 0, 5);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().position(VENT), Direction.UP);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Geyser Vents are found naturally in deserts, badlands and savannas")
                .pointAt(util.vector().topOf(VENT))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().showSection(util.select().position(CAP), Direction.DOWN);
        scene.idle(20);
        scene.overlay().showText(70)
                .text("Put a Geyser Cap right on top of one to harness it")
                .pointAt(util.vector().topOf(CAP))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().modifyBlock(CAP, s -> s.setValue(GeyserCapBlock.LIT, true), false);
        scene.world().setKineticSpeed(util.select().position(CAP), 32f);
        scene.effects().indicateSuccess(CAP);
        scene.overlay().showText(70)
                .text("It gives a constant 32 RPM and 16384 SU of stress capacity - no fuel, no sunlight, day and night")
                .pointAt(util.vector().centerOf(CAP))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().showSection(util.select().position(SHAFT_WEST), Direction.EAST);
        scene.world().showSection(util.select().position(SHAFT_EAST), Direction.WEST);
        scene.world().setKineticSpeed(util.select().position(SHAFT_WEST).add(util.select().position(SHAFT_EAST)), 32f);
        scene.idle(10);
        scene.overlay().showOutline(PonderPalette.GREEN, "shafts",
                util.select().position(SHAFT_WEST).add(util.select().position(SHAFT_EAST)), 70);
        scene.overlay().showText(70)
                .text("Rotation comes out of both of its sides")
                .pointAt(util.vector().centerOf(SHAFT_EAST))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.markAsFinished();
    }
}
