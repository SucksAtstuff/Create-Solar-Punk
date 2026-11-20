package net.succ.solar_punk.compat.ponder;

import com.simibubi.create.AllItems;
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
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.phys.Vec3;
import net.succ.solar_punk.block.custom.BrassSolarPanelBlock;
import net.succ.solar_punk.block.custom.ElectricMotorBlock;
import net.succ.solar_punk.block.custom.RedstoneWireRelayBlock;
import net.succ.solar_punk.block.custom.WireConnectorBlock;
import net.succ.solar_punk.block.custom.WireSwitchBlock;
import net.succ.solar_punk.block.entity.custom.WireNodeBlockEntity;
import net.succ.solar_punk.item.ModItems;
import net.succ.solar_punk.wire.WireTier;

// Schematics (ModPonderProvider): "wire_connector/usage" is a 2x3 Brass Solar Panel field at the
// back (x 5-6) with a connector on its west side (4,1,2) and two Electric Motors at the front (x=1, z=1 and 3), each
// with a connector on top. "wire_switch/usage" and "wire_switch/relay" have a row of panels at
// x=6 with a connector on the west side of the middle one (5,1,2), the switch or relay on the floor at (3,1,2) and a motor at (1,1,2).
public class WireScenes {

    /** Strings a wire between two nodes, listed at both ends like SpoolItem does. */
    static void wire(CreateSceneBuilder scene, BlockPos a, int terminalA, BlockPos b, int terminalB, WireTier tier) {
        scene.world().modifyBlockEntity(a, WireNodeBlockEntity.class, be -> be.addConnection(terminalA, b, terminalB, tier, 1));
        scene.world().modifyBlockEntity(b, WireNodeBlockEntity.class, be -> be.addConnection(terminalB, a, terminalA, tier, 1));
    }

    /** Where a Wire Connector facing up takes its wires. */
    static Vec3 tip(SceneBuildingUtil util, BlockPos connector) {
        return tip(connector, Direction.UP);
    }

    /** Where a Wire Connector facing the given way takes its wires. */
    static Vec3 tip(BlockPos connector, Direction facing) {
        return WireConnectorBlock.attachPoint(connector, facing);
    }

    private static void setMotor(CreateSceneBuilder scene, SceneBuildingUtil util, BlockPos motor, float rpm) {
        scene.world().modifyBlock(motor, s -> s.setValue(ElectricMotorBlock.LIT, rpm != 0), false);
        scene.world().setKineticSpeed(util.select().position(motor).add(util.select().position(motor.west())), rpm);
    }

    private static void setPanels(CreateSceneBuilder scene, Selection panels, boolean lit) {
        scene.world().modifyBlocks(panels, s -> s.hasProperty(BrassSolarPanelBlock.LIT)
                ? s.setValue(BrassSolarPanelBlock.LIT, lit) : s, false);
    }

    public static void connector(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("wire_connector_usage", "Stringing Wires");
        scene.configureBasePlate(0, 0, 7);
        scene.scaleSceneView(0.9f);

        BlockPos source = new BlockPos(4, 1, 2);
        Vec3 sourceTip = tip(source, Direction.WEST);
        BlockPos motorA = new BlockPos(1, 1, 1), motorB = new BlockPos(1, 1, 3);
        BlockPos plugA = motorA.above(), plugB = motorB.above();
        Selection panels = util.select().fromTo(5, 1, 1, 6, 1, 3);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(panels, Direction.DOWN);
        scene.idle(5);
        setPanels(scene, panels, true);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(0, 1, 1, 1, 1, 1).add(util.select().fromTo(0, 1, 3, 1, 1, 3)), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Wire Connectors carry FE between blocks that are far apart")
                .pointAt(util.vector().topOf(5, 1, 2))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().showSection(util.select().position(source), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(plugA).add(util.select().position(plugB)), Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(70)
                .text("Place one on each block that makes or uses FE")
                .pointAt(sourceTip)
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showControls(sourceTip, Pointing.DOWN, 30).rightClick()
                .withItem(new ItemStack(ModItems.COPPER_SPOOL.get()));
        scene.idle(35);
        scene.overlay().showControls(tip(util, plugA), Pointing.DOWN, 30).rightClick()
                .withItem(new ItemStack(ModItems.COPPER_SPOOL.get()));
        scene.idle(10);
        wire(scene, source, 0, plugA, 0, WireTier.COPPER);
        scene.overlay().showText(70)
                .text("Right-click two connectors with a Spool to string a wire between them")
                .pointAt(tip(util, plugA))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(30);
        setMotor(scene, util, motorA, 32);
        scene.effects().indicateSuccess(motorA);
        scene.idle(50);

        scene.overlay().showText(70)
                .text("Connectors store nothing: energy goes straight through to the machines at the other end")
                .pointAt(util.vector().centerOf(motorA))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showControls(tip(util, plugB), Pointing.DOWN, 30).rightClick()
                .withItem(new ItemStack(ModItems.COPPER_SPOOL.get()));
        scene.idle(10);
        wire(scene, source, 0, plugB, 0, WireTier.COPPER);
        scene.idle(10);
        setMotor(scene, util, motorB, 32);
        scene.effects().indicateSuccess(motorB);
        scene.overlay().showText(70)
                .text("A connector holds up to 8 wires, so one network can reach many machines")
                .pointAt(tip(util, plugB))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        Vec3 mid = sourceTip.add(tip(util, plugA)).scale(0.5).add(0, -0.2, 0);
        scene.overlay().showControls(mid, Pointing.DOWN, 25).withItem(new ItemStack(ModItems.IRON_SPOOL.get()));
        scene.idle(25);
        scene.overlay().showControls(mid, Pointing.DOWN, 25).withItem(new ItemStack(ModItems.COPPER_SPOOL.get()));
        scene.idle(25);
        scene.overlay().showControls(mid, Pointing.DOWN, 25).withItem(new ItemStack(ModItems.GOLD_SPOOL.get()));
        scene.overlay().showText(75)
                .text("Iron, Copper and Gold wires carry 256, 1024 and 4096 FE/t, and reach 16, 24 and 32 blocks")
                .pointAt(mid)
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(85);

        scene.overlay().showText(70)
                .text("A Spool holds 4 wires, and each wire covers 8 blocks of length")
                .pointAt(mid)
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showControls(tip(util, plugB), Pointing.DOWN, 60).rightClick()
                .withItem(new ItemStack(Items.SHEARS));
        scene.overlay().showText(60)
                .text("Shears take down every wire on a connector. Breaking one drops its wires too")
                .pointAt(tip(util, plugB))
                .placeNearTarget();
        scene.idle(70);

        scene.markAsFinished();
    }

    public static void wireSwitch(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("wire_switch_usage", "Switching Wires");
        scene.configureBasePlate(0, 0, 7);
        scene.scaleSceneView(0.9f);

        BlockPos source = new BlockPos(5, 1, 2);
        BlockPos sw = new BlockPos(3, 1, 2);
        BlockPos motor = new BlockPos(1, 1, 2);
        Selection panels = util.select().fromTo(6, 1, 1, 6, 1, 3);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(panels.copy().add(util.select().position(source)), Direction.DOWN);
        setPanels(scene, panels, true);
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(0, 1, 2, 1, 2, 2), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().position(sw), Direction.DOWN);
        scene.idle(15);

        // Facing east, terminal 0 is the post on the panel side and terminal 1 the one on the motor side
        wire(scene, source, 0, sw, 0, WireTier.COPPER);
        scene.idle(5);
        wire(scene, sw, 1, motor.above(), 0, WireTier.COPPER);
        scene.idle(10);

        scene.overlay().showText(80)
                .text("A Wire Switch goes in the middle of a line: one wire on each post")
                .pointAt(util.vector().topOf(sw))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showText(60)
                .text("While it's open, nothing gets past it")
                .pointAt(util.vector().centerOf(motor))
                .colored(PonderPalette.RED)
                .placeNearTarget();
        scene.idle(70);

        scene.overlay().showControls(util.vector().topOf(sw), Pointing.DOWN, 30).rightClick();
        scene.idle(10);
        scene.world().modifyBlock(sw, s -> s.setValue(WireSwitchBlock.CLOSED, true), false);
        scene.idle(5);
        setMotor(scene, util, motor, 32);
        scene.effects().indicateSuccess(motor);
        scene.overlay().showText(70)
                .text("Right-click it to close it and let energy through")
                .pointAt(util.vector().topOf(sw))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showControls(util.vector().topOf(sw), Pointing.DOWN, 30).rightClick();
        scene.idle(10);
        scene.world().modifyBlock(sw, s -> s.setValue(WireSwitchBlock.CLOSED, false), false);
        setMotor(scene, util, motor, 0);
        scene.overlay().showText(70)
                .text("Right-click again to cut the power to everything past it")
                .pointAt(util.vector().topOf(sw))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showText(70)
                .text("It can go on a floor, wall or ceiling. A Comparator reads 15 from it while it's closed")
                .pointAt(util.vector().topOf(sw))
                .placeNearTarget();
        scene.idle(80);

        scene.markAsFinished();
    }

    public static void relay(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("wire_switch_relay", "Switching Wires with Redstone");
        scene.configureBasePlate(0, 0, 7);
        scene.scaleSceneView(0.9f);

        BlockPos source = new BlockPos(5, 1, 2);
        BlockPos relay = new BlockPos(3, 1, 2);
        BlockPos lever = new BlockPos(3, 1, 1);
        BlockPos motor = new BlockPos(1, 1, 2);
        Selection panels = util.select().fromTo(6, 1, 1, 6, 1, 3);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(panels.copy().add(util.select().position(source)), Direction.DOWN);
        setPanels(scene, panels, true);
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(0, 1, 2, 1, 2, 2), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().position(relay), Direction.DOWN);
        scene.idle(10);
        wire(scene, source, 0, relay, 0, WireTier.COPPER);
        scene.idle(5);
        wire(scene, relay, 1, motor.above(), 0, WireTier.COPPER);
        scene.idle(15);

        scene.overlay().showText(70)
                .text("The Redstone Wire Relay is a Wire Switch worked by redstone")
                .pointAt(util.vector().topOf(relay))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().showSection(util.select().position(lever), Direction.DOWN);
        scene.idle(15);
        scene.world().modifyBlock(lever, s -> s.setValue(LeverBlock.POWERED, true), false);
        scene.effects().indicateRedstone(lever);
        scene.idle(5);
        scene.world().modifyBlock(relay, s -> s.setValue(WireSwitchBlock.CLOSED, true), false);
        setMotor(scene, util, motor, 32);
        scene.overlay().showText(70)
                .text("A redstone signal closes it and lets energy through")
                .pointAt(util.vector().topOf(relay))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().modifyBlock(lever, s -> s.setValue(LeverBlock.POWERED, false), false);
        scene.idle(5);
        scene.world().modifyBlock(relay, s -> s.setValue(WireSwitchBlock.CLOSED, false), false);
        setMotor(scene, util, motor, 0);
        scene.overlay().showText(60)
                .text("Without one it opens again")
                .pointAt(util.vector().topOf(relay))
                .colored(PonderPalette.RED)
                .placeNearTarget();
        scene.idle(70);

        scene.overlay().showControls(util.vector().topOf(relay), Pointing.DOWN, 40).rightClick()
                .withItem(AllItems.WRENCH.asStack());
        scene.idle(10);
        scene.world().modifyBlock(relay, s -> s.setValue(RedstoneWireRelayBlock.INVERTED, true)
                .setValue(WireSwitchBlock.CLOSED, true), false);
        setMotor(scene, util, motor, 32);
        scene.overlay().showText(80)
                .text("Use a Wrench to invert it: then it stays closed until it gets a signal")
                .pointAt(util.vector().topOf(relay))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.world().modifyBlock(lever, s -> s.setValue(LeverBlock.POWERED, true), false);
        scene.effects().indicateRedstone(lever);
        scene.idle(5);
        scene.world().modifyBlock(relay, s -> s.setValue(WireSwitchBlock.CLOSED, false), false);
        setMotor(scene, util, motor, 0);
        scene.overlay().showText(70)
                .text("Use it to shut machines down from a control room, or when a tank fills up")
                .pointAt(util.vector().topOf(lever))
                .placeNearTarget();
        scene.idle(80);

        scene.markAsFinished();
    }

    private WireScenes() {}
}
