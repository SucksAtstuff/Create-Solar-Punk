package net.succ.solar_punk.compat.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
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

        BlockPos panelPos = new BlockPos(2, 2, 2);
        BlockPos shaftPos = new BlockPos(2, 1, 2);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().position(shaftPos), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().position(panelPos), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("The Andesite Solar Panel generates Rotational Force from sunlight")
                .pointAt(util.vector().topOf(panelPos))
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(70)
                .text("It outputs rotation downwards — connect a shaft or machine directly below")
                .pointAt(util.vector().centerOf(shaftPos))
                .attachKeyFrame();
        scene.idle(80);

        scene.world().setKineticSpeed(util.select().position(shaftPos), 8f);
        scene.world().modifyBlock(panelPos,
                state -> state.setValue(AndesiteSolarPanelBlock.LIT, true), false);
        scene.overlay().showText(70)
                .text("At dawn and dusk: 8 RPM with 1024 SU of stress capacity")
                .pointAt(util.vector().topOf(panelPos))
                .attachKeyFrame();
        scene.idle(80);

        scene.world().setKineticSpeed(util.select().position(shaftPos), 16f);
        scene.overlay().showText(70)
                .text("At noon in clear weather: 16 RPM with 4096 SU of stress capacity")
                .pointAt(util.vector().topOf(panelPos))
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(60)
                .text("Rain reduces output to dawn levels — night stops generation entirely")
                .pointAt(util.vector().topOf(panelPos))
                .attachKeyFrame();
        scene.idle(70);

        scene.overlay().showText(60)
                .text("The panel must have a clear view of the sky directly above to function")
                .pointAt(util.vector().topOf(panelPos))
                .attachKeyFrame();
        scene.idle(70);

        scene.markAsFinished();
    }

    public static void brassUsage(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("brass_panel_usage", "Brass Solar Panel");
        scene.configureBasePlate(0, 0, 5);

        BlockPos panelPos = new BlockPos(2, 1, 2);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().position(panelPos), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("The Brass Solar Panel generates Forge Energy (FE) directly from sunlight")
                .pointAt(util.vector().topOf(panelPos))
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(70)
                .text("Connect cables or conduits to any face to transfer the generated power")
                .pointAt(util.vector().centerOf(panelPos))
                .attachKeyFrame();
        scene.world().modifyBlock(panelPos,
                state -> state.setValue(BrassSolarPanelBlock.LIT, true), false);
        scene.idle(80);

        scene.overlay().showText(70)
                .text("At dawn and dusk: 40 FE/t — at noon in clear weather: 80 FE/t")
                .pointAt(util.vector().topOf(panelPos))
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(60)
                .text("Rain reduces output to dawn levels — the panel must have clear sky access above it")
                .pointAt(util.vector().topOf(panelPos))
                .attachKeyFrame();
        scene.idle(70);

        scene.markAsFinished();
    }
}