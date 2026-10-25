package net.succ.solar_punk.compat.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.succ.solar_punk.block.custom.AndesiteSolarPanelBlock;
import net.succ.solar_punk.block.custom.BrassSolarPanelBlock;

public class SolarPanelScenes {

    public static void andesiteUsage(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("andesite_panel_usage", "Andesite Solar Panel");
        scene.configureBasePlate(0, 0, 5);

        BlockPos panel = new BlockPos(2, 2, 2);
        BlockPos shaft = new BlockPos(2, 1, 2);
        var both = util.select().position(panel).add(util.select().position(shaft));

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().position(shaft), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(panel), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("The Andesite Solar Panel turns sunlight into Rotational Force")
                .pointAt(util.vector().topOf(panel))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().setKineticSpeed(both, 8f);
        scene.world().modifyBlock(panel, s -> s.setValue(AndesiteSolarPanelBlock.LIT, true), false);
        scene.overlay().showText(70)
                .text("At dawn and dusk it gives 8 RPM and 1024 SU of stress capacity...")
                .pointAt(util.vector().topOf(panel))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().setKineticSpeed(both, 16f);
        scene.effects().indicateSuccess(panel);
        scene.overlay().showText(70)
                .text("...and at noon in clear weather, 16 RPM and 4096 SU")
                .pointAt(util.vector().topOf(panel))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showOutline(PonderPalette.GREEN, "shaft", util.select().position(shaft), 60);
        scene.overlay().showText(60)
                .text("Rotation comes out of the bottom")
                .pointAt(util.vector().centerOf(shaft))
                .placeNearTarget();
        scene.idle(70);

        scene.world().setKineticSpeed(both, 8f);
        scene.overlay().showText(70)
                .text("Rain drops it back to dawn levels")
                .pointAt(util.vector().topOf(panel))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().setKineticSpeed(both, 0f);
        scene.world().modifyBlock(panel, s -> s.setValue(AndesiteSolarPanelBlock.LIT, false), false);
        scene.overlay().showText(70)
                .colored(PonderPalette.RED)
                .text("At night, or with anything above it blocking the sky, it stops")
                .pointAt(util.vector().topOf(panel))
                .placeNearTarget();
        scene.idle(80);

        scene.markAsFinished();
    }

    public static void brassUsage(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("brass_panel_usage", "Brass Solar Panel");
        scene.configureBasePlate(0, 0, 5);

        BlockPos panel = new BlockPos(2, 1, 2);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().position(panel), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("The Brass Solar Panel turns sunlight straight into Forge Energy (FE)")
                .pointAt(util.vector().topOf(panel))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().modifyBlock(panel, s -> s.setValue(BrassSolarPanelBlock.LIT, true), false);
        scene.effects().indicateSuccess(panel);
        scene.overlay().showText(70)
                .text("It makes 40 FE/t at dawn and dusk, and 80 FE/t at noon in clear weather")
                .pointAt(util.vector().topOf(panel))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showOutline(PonderPalette.GREEN, "sides", util.select().position(panel), 70);
        scene.overlay().showText(70)
                .text("Connect cables to any side to draw the power. It keeps a buffer, so it can be drained at night")
                .pointAt(util.vector().blockSurface(panel, Direction.WEST))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().modifyBlock(panel, s -> s.setValue(BrassSolarPanelBlock.LIT, false), false);
        scene.overlay().showText(70)
                .colored(PonderPalette.RED)
                .text("Rain drops it to dawn levels. At night, or with the sky blocked, it stops")
                .pointAt(util.vector().topOf(panel))
                .placeNearTarget();
        scene.idle(80);

        scene.markAsFinished();
    }
}
