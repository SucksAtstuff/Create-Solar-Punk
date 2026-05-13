package net.succ.solar_punk.compat.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.succ.solar_punk.block.custom.GeyserCapBlock;

public class GeyserCapScenes {

    public static void usage(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("geyser_cap_usage", "Geyser Cap");
        scene.configureBasePlate(0, 0, 5);

        BlockPos ventPos  = new BlockPos(2, 1, 2);
        BlockPos capPos   = new BlockPos(2, 2, 2);
        BlockPos shaftWest = new BlockPos(1, 2, 2);
        BlockPos shaftEast = new BlockPos(3, 2, 2);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().position(ventPos), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Geyser Vents naturally generate in hot, arid biomes")
                .pointAt(util.vector().topOf(ventPos))
                .attachKeyFrame();
        scene.idle(80);

        scene.world().showSection(util.select().position(capPos), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Place a Geyser Cap directly on top of a Geyser Vent to harness its energy")
                .pointAt(util.vector().topOf(capPos))
                .attachKeyFrame();
        scene.idle(80);

        scene.world().modifyBlock(capPos,
                state -> state.setValue(GeyserCapBlock.LIT, true), false);
        scene.world().setKineticSpeed(util.select().position(capPos), 32f);

        scene.overlay().showText(70)
                .text("While active, the cap generates 32 RPM with 16384 SU of stress capacity")
                .pointAt(util.vector().centerOf(capPos))
                .attachKeyFrame();
        scene.idle(80);

        scene.world().showSection(util.select().position(shaftWest), Direction.EAST);
        scene.world().showSection(util.select().position(shaftEast), Direction.WEST);
        scene.world().setKineticSpeed(util.select().position(shaftWest), 32f);
        scene.world().setKineticSpeed(util.select().position(shaftEast), 32f);
        scene.idle(10);

        scene.overlay().showText(70)
                .text("Rotation is output along the cap's sides — connect shafts or machines to collect the power")
                .pointAt(util.vector().centerOf(capPos))
                .attachKeyFrame();
        scene.idle(80);

        scene.markAsFinished();
    }
}