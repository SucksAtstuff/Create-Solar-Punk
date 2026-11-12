package net.succ.solar_punk.compat.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.succ.solar_punk.block.custom.HeatBatteryBlock;
import net.succ.solar_punk.block.custom.SolarHeaterBlock;
import net.succ.solar_punk.block.entity.custom.HeatBatteryBlockEntity;
import net.succ.solar_punk.fluid.ModFluids;

public class HeatBatteryScenes {

    private static final BlockPos BATTERY = new BlockPos(2, 1, 2);

    // Schematic "heat_battery/filling" is the Solar Heater's line: a Chest on a Hopper feeding a
    // Solar Heater, piped into the battery at (3,1,2). See SolarHeaterScenes.
    public static void filling(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("heat_battery_filling", "Filling the Heat Battery");
        scene.configureBasePlate(0, 0, 5);

        BlockPos battery = SolarHeaterScenes.LINE_BATTERY;
        BlockPos heater = SolarHeaterScenes.LINE_HEATER;
        BlockPos pipe = SolarHeaterScenes.LINE_PIPE;

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().position(battery), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("The Heat Battery stores heat from Molten Salt")
                .pointAt(util.vector().topOf(battery))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().showSection(util.select().fromTo(0, 1, 2, 1, 2, 2), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().position(pipe), Direction.DOWN);
        scene.idle(15);
        scene.world().modifyBlock(heater, s -> s.setValue(SolarHeaterBlock.LIT, true), false);
        scene.overlay().showText(70)
                .text("Pipe Molten Salt into any side, here straight from a Solar Heater, or right-click with a bucket")
                .pointAt(util.vector().centerOf(pipe))
                .placeNearTarget()
                .attachKeyFrame();
        scene.world().modifyBlockEntity(battery, HeatBatteryBlockEntity.class, be ->
                be.fluidTank.fill(new FluidStack(ModFluids.MOLTEN_SALT_SOURCE.get(), 8000),
                        IFluidHandler.FluidAction.EXECUTE));
        scene.idle(80);

        scene.world().modifyBlock(battery, s -> s.setValue(HeatBatteryBlock.HEAT, 1), false);
        scene.overlay().showText(70)
                .text("It slowly turns the salt into stored heat, and starts glowing")
                .pointAt(util.vector().centerOf(battery))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().modifyBlock(battery, s -> s.setValue(HeatBatteryBlock.HEAT, 2), false);
        scene.effects().indicateSuccess(battery);
        scene.overlay().showText(70)
                .text("Once its heat store is a quarter full, it's Superheated and glows brighter")
                .pointAt(util.vector().centerOf(battery))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showControls(util.vector().topOf(battery), Pointing.DOWN, 30)
                .withItem(new ItemStack(Items.COMPARATOR));
        scene.idle(35);
        scene.overlay().showText(70)
                .text("Goggles show the stored heat, and Comparators read it as a signal")
                .pointAt(util.vector().topOf(battery))
                .placeNearTarget();
        scene.idle(80);

        scene.markAsFinished();
    }

    public static void usage(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("heat_battery_usage", "Using the Heat Battery");
        scene.configureBasePlate(0, 0, 5);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().position(BATTERY), Direction.DOWN);
        scene.idle(20);
        scene.world().modifyBlockEntity(BATTERY, HeatBatteryBlockEntity.class, be ->
                be.fluidTank.fill(new FluidStack(ModFluids.MOLTEN_SALT_SOURCE.get(), 8000),
                        IFluidHandler.FluidAction.EXECUTE));

        scene.overlay().showText(70)
                .text("A hot Heat Battery heats Create's Boilers, just like a Blaze Burner")
                .pointAt(util.vector().topOf(BATTERY))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showControls(util.vector().topOf(BATTERY), Pointing.DOWN, 70)
                .withItem(new ItemStack(AllBlocks.FLUID_TANK.asItem()));
        scene.overlay().showText(70)
                .text("Put the Boiler's Fluid Tank right on top of it - no fuel needed")
                .pointAt(util.vector().topOf(BATTERY))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(70)
                .text("Heated runs a Boiler like a lit Blaze Burner, Superheated like one fed a Blaze Cake")
                .pointAt(util.vector().centerOf(BATTERY))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showText(70)
                .text("Stored heat slowly leaks away, so keep the salt coming from a Solar Heater or Solar Power Tower")
                .pointAt(util.vector().blockSurface(BATTERY, Direction.WEST))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().modifyBlock(BATTERY, s -> s.setValue(HeatBatteryBlock.HEAT, 1), false);
        scene.idle(30);
        scene.world().modifyBlock(BATTERY, s -> s.setValue(HeatBatteryBlock.HEAT, 0), false);
        scene.overlay().showText(70)
                .colored(PonderPalette.RED)
                .text("When the heat runs out, the Boiler goes cold")
                .pointAt(util.vector().centerOf(BATTERY))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.markAsFinished();
    }
}
