package net.succ.solar_punk.compat.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.succ.solar_punk.block.custom.KineticBatteryBlock;

public class KineticBatteryScenes {

    public static void usage(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("kinetic_battery_usage", "Using the Kinetic Battery");
        scene.configureBasePlate(0, 0, 5);

        BlockPos batteryPos      = new BlockPos(2, 1, 2);
        BlockPos chargeShaftPos  = new BlockPos(2, 1, 3);
        BlockPos crankPos        = new BlockPos(2, 1, 4);
        BlockPos dischargeShaftPos = new BlockPos(2, 1, 1);
        BlockPos pressPos        = new BlockPos(2, 1, 0);

        // ── reveal base plate and battery ──────────────────────────────
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().position(batteryPos), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("The Kinetic Battery stores Rotational Force for later use")
                .pointAt(util.vector().topOf(batteryPos))
                .attachKeyFrame();
        scene.idle(80);

        // ── show charging source (hand crank) and start spinning ────────
        ElementLink<WorldSectionElement> crankSection = scene.world().showIndependentSection(
                util.select().fromTo(chargeShaftPos, crankPos), Direction.SOUTH);
        scene.idle(10);
        scene.world().setKineticSpeed(util.select().fromTo(chargeShaftPos, crankPos), 16f);

        scene.overlay().showText(70)
                .text("When connected to a spinning network without a Redstone signal, it charges up")
                .pointAt(util.vector().centerOf(batteryPos))
                .attachKeyFrame();
        scene.idle(80);

        // ── crank disappears; press revealed on the discharge side ──────
        scene.world().hideIndependentSection(crankSection, Direction.SOUTH);
        scene.idle(15);

        scene.world().showSection(
                util.select().fromTo(dischargeShaftPos, pressPos), Direction.NORTH);
        scene.idle(10);

        // set battery LIT to show it is now discharging
        scene.world().modifyBlock(batteryPos,
                state -> state.setValue(KineticBatteryBlock.LIT, true), false);
        // drive the press at discharge speed
        scene.world().setKineticSpeed(util.select().position(pressPos), -16f);

        scene.overlay().showText(70)
                .text("Apply a Redstone signal to discharge — it outputs rotation along its axis")
                .pointAt(util.vector().centerOf(batteryPos))
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(70)
                .text("Discharge output: 16 RPM with 256 SU of stress capacity")
                .pointAt(util.vector().topOf(batteryPos))
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(60)
                .text("The battery discharges at a constant speed until empty, then stops")
                .pointAt(util.vector().topOf(batteryPos))
                .attachKeyFrame();
        scene.idle(70);

        scene.overlay().showText(60)
                .text("Use it to buffer power from intermittent sources like solar panels")
                .pointAt(util.vector().centerOf(batteryPos))
                .attachKeyFrame();
        scene.idle(70);

        scene.markAsFinished();
    }
}