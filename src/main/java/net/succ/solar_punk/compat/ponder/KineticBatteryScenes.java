package net.succ.solar_punk.compat.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.succ.solar_punk.block.custom.AndesiteSolarPanelBlock;
import net.succ.solar_punk.block.custom.KineticBatteryBlock;

// Schematic (ModPonderProvider "kinetic_battery/usage"): the battery along Z at (2,1,2). South of
// it a Gearbox carrying an Andesite Solar Panel charges it; north of it a shaft runs an Encased Fan.
public class KineticBatteryScenes {

    private static final BlockPos BATTERY = new BlockPos(2, 1, 2);
    private static final BlockPos GEARBOX = new BlockPos(2, 1, 3);
    private static final BlockPos PANEL = new BlockPos(2, 2, 3);
    private static final BlockPos SHAFT = new BlockPos(2, 1, 1);
    private static final BlockPos FAN = new BlockPos(2, 1, 0);

    public static void usage(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("kinetic_battery_usage", "Using the Kinetic Battery");
        scene.configureBasePlate(0, 0, 5);

        Selection charger = util.select().position(GEARBOX).add(util.select().position(PANEL));
        Selection fanLine = util.select().fromTo(SHAFT, FAN);
        Selection everything = util.select().fromTo(2, 1, 0, 2, 2, 3);

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

        // --- charging by day ---
        scene.world().showSection(util.select().position(GEARBOX), Direction.NORTH);
        scene.idle(5);
        scene.world().showSection(util.select().position(PANEL), Direction.DOWN);
        scene.idle(15);
        scene.world().modifyBlock(PANEL, s -> s.setValue(AndesiteSolarPanelBlock.LIT, true), false);
        scene.world().setKineticSpeed(charger.copy().add(util.select().position(BATTERY)), 16f);
        scene.overlay().showText(80)
                .text("On a spinning network it charges up, drawing 256 SU at 16 RPM. Here a solar panel fills it during the day")
                .pointAt(util.vector().topOf(PANEL))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showText(60)
                .text("Goggles show how full it is")
                .pointAt(util.vector().topOf(BATTERY))
                .placeNearTarget();
        scene.idle(70);

        scene.world().showSection(fanLine, Direction.SOUTH);
        scene.idle(15);

        // --- night: the panel stops, the battery takes over ---
        scene.world().modifyBlock(PANEL, s -> s.setValue(AndesiteSolarPanelBlock.LIT, false), false);
        scene.world().setKineticSpeed(everything, 0f);
        scene.overlay().showText(60)
                .text("Night falls and the panel stops...")
                .pointAt(util.vector().topOf(PANEL))
                .colored(PonderPalette.RED)
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(70);

        scene.overlay().showControls(util.vector().topOf(BATTERY), Pointing.DOWN, 30)
                .withItem(new ItemStack(Items.LEVER));
        scene.idle(10);
        scene.world().modifyBlock(BATTERY, s -> s.setValue(KineticBatteryBlock.LIT, true), false);
        scene.world().setKineticSpeed(everything, -16f);
        scene.effects().indicateRedstone(BATTERY);
        scene.idle(25);
        scene.overlay().showText(80)
                .text("...so give the battery a Redstone signal, and it lets the power back out along its axis")
                .pointAt(util.vector().centerOf(BATTERY))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showOutline(PonderPalette.GREEN, "fan", fanLine, 70);
        scene.overlay().showText(70)
                .text("It runs at a steady 16 RPM with 256 SU, giving back the power it took in")
                .pointAt(util.vector().topOf(FAN))
                .placeNearTarget();
        scene.idle(80);

        scene.world().modifyBlock(BATTERY, s -> s.setValue(KineticBatteryBlock.LIT, false), false);
        scene.world().setKineticSpeed(everything, 0f);
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
