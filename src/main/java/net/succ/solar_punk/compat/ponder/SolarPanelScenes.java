package net.succ.solar_punk.compat.ponder;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.millstone.MillstoneBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.succ.solar_punk.block.custom.AndesiteSolarPanelBlock;
import net.succ.solar_punk.block.custom.BrassSolarPanelBlock;
import net.succ.solar_punk.block.custom.ElectricMotorBlock;
import net.succ.solar_punk.block.custom.ElectrolyzerBlock;
import net.succ.solar_punk.wire.WireTier;

public class SolarPanelScenes {

    // Schematic (ModPonderProvider "solar_panel/andesite"): panels at x 0-2 (y=2, z=2) on a row of
    // meshing cogwheels, and a Millstone at (3,1,2) meshing with the last cog.
    public static void andesiteUsage(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("andesite_panel_usage", "Andesite Solar Panel");
        scene.configureBasePlate(0, 0, 5);

        BlockPos firstPanel = new BlockPos(0, 2, 2);
        BlockPos firstCog = new BlockPos(0, 1, 2);
        BlockPos mill = new BlockPos(3, 1, 2);
        Selection panels = util.select().fromTo(0, 2, 2, 2, 2, 2);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().position(firstCog), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(firstPanel), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("The Andesite Solar Panel turns sunlight into Rotational Force")
                .pointAt(util.vector().topOf(firstPanel))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showOutline(PonderPalette.GREEN, "shaft", util.select().position(firstCog), 60);
        scene.overlay().showText(60)
                .text("Rotation comes out of the bottom")
                .pointAt(util.vector().centerOf(firstCog))
                .placeNearTarget();
        scene.idle(70);

        for (int x = 1; x <= 2; x++) {
            scene.world().showSection(util.select().position(x, 1, 2), Direction.DOWN);
            scene.idle(5);
            scene.world().showSection(util.select().position(x, 2, 2), Direction.DOWN);
            scene.idle(8);
        }
        scene.world().showSection(util.select().position(mill), Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(70)
                .text("Put several on one network and their stress capacity adds up")
                .pointAt(util.vector().topOf(1, 2, 2))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        spin(scene, util, 8);
        scene.world().modifyBlocks(panels, s -> s.setValue(AndesiteSolarPanelBlock.LIT, true), false);
        scene.overlay().showText(70)
                .text("At dawn and dusk each panel gives 8 RPM and 1024 SU of stress capacity...")
                .pointAt(util.vector().topOf(firstPanel))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        spin(scene, util, 16);
        scene.effects().indicateSuccess(new BlockPos(1, 2, 2));
        scene.overlay().showText(70)
                .text("...and at noon in clear weather, 16 RPM and 4096 SU")
                .pointAt(util.vector().topOf(1, 2, 2))
                .placeNearTarget();
        scene.idle(80);

        // A Millstone grinding wheat while the sun's up
        Vec3 drop = util.vector().topOf(mill).add(0, 0.6, 0);
        var wheat = scene.world().createItemEntity(drop, Vec3.ZERO, new ItemStack(Items.WHEAT));
        scene.idle(15);
        scene.world().modifyEntity(wheat, e -> e.discard());
        scene.world().modifyBlockEntity(mill, MillstoneBlockEntity.class,
                be -> be.inputInv.setStackInSlot(0, new ItemStack(Items.WHEAT)));
        scene.overlay().showText(60)
                .text("Here, three panels run a Millstone grinding Wheat")
                .pointAt(util.vector().topOf(mill))
                .placeNearTarget();
        scene.idle(40);
        scene.world().modifyBlockEntity(mill, MillstoneBlockEntity.class, be -> {
            be.inputInv.setStackInSlot(0, ItemStack.EMPTY);
            be.outputInv.setStackInSlot(0, AllItems.WHEAT_FLOUR.asStack());
        });
        scene.overlay().showControls(util.vector().blockSurface(mill, Direction.EAST), Pointing.RIGHT, 40)
                .withItem(AllItems.WHEAT_FLOUR.asStack());
        scene.idle(50);

        spin(scene, util, 8);
        scene.overlay().showText(70)
                .text("Rain drops them back to dawn levels")
                .pointAt(util.vector().topOf(2, 2, 2))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        spin(scene, util, 0);
        scene.world().modifyBlocks(panels, s -> s.setValue(AndesiteSolarPanelBlock.LIT, false), false);
        scene.overlay().showText(70)
                .colored(PonderPalette.RED)
                .text("At night, or with anything above them blocking the sky, they stop. A Kinetic Battery can cover the night")
                .pointAt(util.vector().topOf(firstPanel))
                .placeNearTarget();
        scene.idle(80);

        scene.markAsFinished();
    }

    // Neighbouring cogwheels turn opposite ways
    private static void spin(CreateSceneBuilder scene, SceneBuildingUtil util, float rpm) {
        for (int x = 0; x <= 3; x++) {
            float speed = x % 2 == 0 ? rpm : -rpm;
            Selection column = util.select().fromTo(x, 1, 2, x, 2, 2);
            scene.world().setKineticSpeed(column, speed);
        }
    }

    // Schematic (ModPonderProvider "solar_panel/brass"): panels at x 0-2 (y=1, z=1), an Electric
    // Motor at (3,1,1) facing south into shafts and an Encased Fan, and a Wire Connector on the
    // middle panel's south side (1,1,2) wired to one on the back of an Electrolyzer at (0,1,3),
    // the one face without a pipe port.
    public static void brassUsage(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("brass_panel_usage", "Brass Solar Panel");
        scene.configureBasePlate(0, 0, 5);

        BlockPos panel = new BlockPos(2, 1, 1);
        BlockPos motor = new BlockPos(3, 1, 1);
        BlockPos plug = new BlockPos(1, 1, 2);
        BlockPos electrolyzer = new BlockPos(0, 1, 3);
        BlockPos electrolyzerPlug = new BlockPos(0, 1, 4);
        Selection panels = util.select().fromTo(0, 1, 1, 2, 1, 1);
        Selection fanLine = util.select().fromTo(3, 1, 1, 3, 1, 4);

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

        scene.world().showSection(util.select().fromTo(0, 1, 1, 1, 1, 1), Direction.DOWN);
        scene.idle(10);
        scene.world().modifyBlocks(panels, s -> s.setValue(BrassSolarPanelBlock.LIT, true), false);
        scene.effects().indicateSuccess(panel);
        scene.overlay().showText(70)
                .text("Each makes 40 FE/t at dawn and dusk, and 80 FE/t at noon in clear weather")
                .pointAt(util.vector().topOf(1, 1, 1))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().showSection(fanLine, Direction.DOWN);
        scene.idle(15);
        scene.world().modifyBlock(motor, s -> s.setValue(ElectricMotorBlock.LIT, true), false);
        scene.world().setKineticSpeed(fanLine, 16);
        scene.overlay().showText(70)
                .text("It pushes its power into any machine touching it, like this Electric Motor")
                .pointAt(util.vector().topOf(motor))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().showSection(util.select().position(plug), Direction.NORTH);
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(electrolyzer, electrolyzerPlug), Direction.DOWN);
        scene.idle(15);
        WireScenes.wire(scene, plug, 0, electrolyzerPlug, 0, WireTier.COPPER);
        scene.idle(10);
        scene.world().modifyBlock(electrolyzer, s -> s.setValue(ElectrolyzerBlock.LIT, true), false);
        scene.overlay().showText(70)
                .text("For machines further away, run wires from a Wire Connector on its side")
                .pointAt(WireScenes.tip(plug, Direction.SOUTH))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(70)
                .text("Keep the top clear: it needs open sky. It keeps a buffer, so it can still be drained at night")
                .pointAt(util.vector().topOf(1, 1, 1))
                .placeNearTarget();
        scene.idle(80);

        scene.world().modifyBlocks(panels, s -> s.setValue(BrassSolarPanelBlock.LIT, false), false);
        scene.world().modifyBlock(motor, s -> s.setValue(ElectricMotorBlock.LIT, false), false);
        scene.world().modifyBlock(electrolyzer, s -> s.setValue(ElectrolyzerBlock.LIT, false), false);
        scene.world().setKineticSpeed(fanLine, 0);
        scene.overlay().showText(70)
                .colored(PonderPalette.RED)
                .text("Rain drops it to dawn levels. At night, or with the sky blocked, it stops")
                .pointAt(util.vector().topOf(panel))
                .placeNearTarget();
        scene.idle(80);

        scene.markAsFinished();
    }
}
