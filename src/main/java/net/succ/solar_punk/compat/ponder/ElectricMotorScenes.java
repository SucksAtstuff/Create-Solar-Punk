package net.succ.solar_punk.compat.ponder;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.simibubi.create.content.kinetics.press.PressingBehaviour;
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
import net.minecraft.world.phys.Vec3;
import net.succ.solar_punk.block.custom.BrassSolarPanelBlock;
import net.succ.solar_punk.block.custom.ElectricMotorBlock;

// Schematic (ModPonderProvider "electric_motor/usage"): two-high Andesite Casing pillars at x=0
// and x=1 (z=2) carrying a Brass Solar Panel and the motor (facing east) at y=3, then a shaft and
// a Mechanical Press at y=3, with the one-block gap a press needs above the Depot at (3,1,2).
public class ElectricMotorScenes {

    private static final BlockPos PANEL = new BlockPos(0, 3, 2);
    private static final BlockPos MOTOR = new BlockPos(1, 3, 2);
    private static final BlockPos PRESS = new BlockPos(3, 3, 2);
    private static final BlockPos DEPOT = new BlockPos(3, 1, 2);

    public static void usage(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("electric_motor_usage", "Using the Electric Motor");
        scene.configureBasePlate(0, 0, 5);

        Selection kinetics = util.select().fromTo(1, 3, 2, 3, 3, 2);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(0, 1, 2, 1, 2, 2), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(MOTOR), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("The Electric Motor turns FE into Rotational Force")
                .pointAt(util.vector().topOf(MOTOR))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().showSection(util.select().position(PANEL), Direction.DOWN);
        scene.idle(10);
        scene.world().modifyBlock(PANEL, s -> s.setValue(BrassSolarPanelBlock.LIT, true), false);
        scene.overlay().showText(70)
                .text("Feed it FE from any side: a Brass Solar Panel, a Wire Connector or a Fuel Cell stack")
                .pointAt(util.vector().topOf(PANEL))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().showSection(util.select().position(DEPOT), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(2, 3, 2, 3, 3, 2), Direction.DOWN);
        scene.idle(15);
        scene.world().modifyBlock(MOTOR, s -> s.setValue(ElectricMotorBlock.LIT, true), false);
        scene.world().setKineticSpeed(kinetics, 16);
        scene.effects().indicateSuccess(MOTOR);
        scene.overlay().showText(70)
                .text("Rotation comes out of the front")
                .pointAt(util.vector().blockSurface(MOTOR, Direction.EAST))
                .placeNearTarget();
        scene.idle(80);

        Vec3 valueBox = util.vector().blockSurface(MOTOR, Direction.NORTH);
        scene.overlay().showFilterSlotInput(valueBox, Direction.NORTH, 70);
        scene.overlay().showControls(valueBox, Pointing.DOWN, 70).rightClick();
        scene.overlay().showText(70)
                .text("Set its speed on any side, like a Creative Motor, up to 64 RPM")
                .pointAt(valueBox)
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(40);
        scene.world().setKineticSpeed(kinetics, 32);
        scene.idle(40);

        ItemStack ingot = new ItemStack(Items.IRON_INGOT);
        scene.world().createItemOnBeltLike(DEPOT, Direction.UP, ingot);
        scene.idle(15);
        scene.world().modifyBlockEntity(PRESS, MechanicalPressBlockEntity.class,
                be -> be.getPressingBehaviour().start(PressingBehaviour.Mode.BELT));
        scene.idle(15);
        scene.world().removeItemsFromBelt(DEPOT);
        scene.world().createItemOnBeltLike(DEPOT, Direction.UP, AllItems.IRON_SHEET.asStack());
        scene.idle(20);

        scene.overlay().showText(80)
                .text("It uses 4 FE/t for every RPM it's set to, however much work it's doing. 32 RPM costs 128 FE/t")
                .pointAt(util.vector().topOf(MOTOR))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showText(80)
                .text("Stress capacity grows with speed too, so gearing it up or down never saves energy")
                .pointAt(util.vector().topOf(MOTOR))
                .placeNearTarget();
        scene.idle(90);

        scene.world().modifyBlock(PANEL, s -> s.setValue(BrassSolarPanelBlock.LIT, false), false);
        scene.world().modifyBlock(MOTOR, s -> s.setValue(ElectricMotorBlock.LIT, false), false);
        scene.world().setKineticSpeed(kinetics, 0);
        scene.overlay().showText(80)
                .text("When its FE runs out it stops, and starts again once it has enough for 10 ticks")
                .pointAt(util.vector().centerOf(MOTOR))
                .colored(PonderPalette.RED)
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showControls(util.vector().topOf(MOTOR), Pointing.DOWN, 60)
                .withItem(AllItems.GOGGLES.asStack());
        scene.overlay().showText(60)
                .text("Engineer's Goggles show the FE coming in, and what speed it can hold on that")
                .pointAt(util.vector().topOf(MOTOR))
                .placeNearTarget();
        scene.idle(70);

        scene.markAsFinished();
    }

    private ElectricMotorScenes() {}
}
