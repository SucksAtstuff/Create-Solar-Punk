package net.succ.solar_punk.compat.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.succ.solar_punk.block.custom.KineticBatteryBlock;

public class KineticBatteryScenes {

    private static final BlockPos BATTERY = new BlockPos(2, 1, 2);
    private static final BlockPos CHARGE_SHAFT = new BlockPos(2, 1, 3);
    private static final BlockPos CRANK = new BlockPos(2, 1, 4);
    private static final BlockPos DISCHARGE_SHAFT = new BlockPos(2, 1, 1);
    private static final BlockPos PRESS = new BlockPos(2, 1, 0);

    public static void usage(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("kinetic_battery_usage", "Using the Kinetic Battery");
        scene.configureBasePlate(0, 0, 5);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().position(BATTERY), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("The Kinetic Battery stores Rotational Force to use later")
                .pointAt(util.vector().topOf(BATTERY))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        // --- charging from a spinning network ---
        ElementLink<WorldSectionElement> crank = scene.world().showIndependentSection(
                util.select().fromTo(CHARGE_SHAFT, CRANK), Direction.NORTH);
        scene.idle(10);
        scene.world().setKineticSpeed(util.select().fromTo(CHARGE_SHAFT, CRANK), 16f);
        scene.overlay().showText(70)
                .text("Connected to a spinning network, it charges up")
                .pointAt(util.vector().centerOf(CHARGE_SHAFT))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(60)
                .text("Goggles show how full it is")
                .pointAt(util.vector().topOf(BATTERY))
                .placeNearTarget();
        scene.idle(70);

        scene.world().hideIndependentSection(crank, Direction.SOUTH);
        scene.idle(15);

        // --- discharging into a machine ---
        scene.world().showSection(util.select().fromTo(DISCHARGE_SHAFT, PRESS), Direction.SOUTH);
        scene.idle(15);

        scene.overlay().showControls(util.vector().topOf(BATTERY), Pointing.DOWN, 70)
                .withItem(new ItemStack(Items.LEVER));
        scene.world().modifyBlock(BATTERY, s -> s.setValue(KineticBatteryBlock.LIT, true), false);
        scene.world().setKineticSpeed(util.select().fromTo(BATTERY, PRESS), -16f);
        scene.effects().indicateRedstone(BATTERY);
        scene.overlay().showText(70)
                .text("Give it a Redstone signal and it lets the power back out along its axis")
                .pointAt(util.vector().centerOf(BATTERY))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showOutline(PonderPalette.GREEN, "press", util.select().fromTo(DISCHARGE_SHAFT, PRESS), 70);
        scene.overlay().showText(70)
                .text("It runs at a steady 16 RPM with 256 SU, for up to twice as long as it charged")
                .pointAt(util.vector().topOf(PRESS))
                .placeNearTarget();
        scene.idle(80);

        scene.world().modifyBlock(BATTERY, s -> s.setValue(KineticBatteryBlock.LIT, false), false);
        scene.world().setKineticSpeed(util.select().fromTo(BATTERY, PRESS), 0f);
        scene.overlay().showText(70)
                .colored(PonderPalette.RED)
                .text("When it's empty it stops")
                .pointAt(util.vector().centerOf(BATTERY))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(70)
                .text("Use it to bridge the gaps in Solar Panels, Gasifiers and other on-and-off power")
                .pointAt(util.vector().topOf(BATTERY))
                .placeNearTarget();
        scene.idle(80);

        scene.markAsFinished();
    }
}
