package net.succ.solar_punk.compat.ponder;

import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.succ.solar_punk.block.custom.HeatBatteryBlock;
import net.succ.solar_punk.block.entity.custom.HeatBatteryBlockEntity;
import net.succ.solar_punk.fluid.ModFluids;

public class HeatBatteryScenes {

    public static void filling(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("heat_battery_filling", "Filling the Heat Battery");
        scene.configureBasePlate(0, 0, 5);

        BlockPos batteryPos = new BlockPos(2, 1, 2);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().position(batteryPos), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("The Heat Battery stores thermal energy in the form of Molten Salt")
                .pointAt(util.vector().topOf(batteryPos))
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(60)
                .text("Fill it by right-clicking with a Molten Salt bucket, or piping fluid into any face")
                .pointAt(util.vector().centerOf(batteryPos))
                .attachKeyFrame();
        scene.world().modifyBlockEntity(batteryPos, HeatBatteryBlockEntity.class, be ->
                be.fluidTank.fill(
                        new FluidStack(ModFluids.MOLTEN_SALT_SOURCE.get(), 4000),
                        IFluidHandler.FluidAction.EXECUTE));
        scene.world().modifyBlock(batteryPos,
                state -> state.setValue(HeatBatteryBlock.HEAT, 1), false);
        scene.idle(70);

        scene.overlay().showText(70)
                .text("Fill it to capacity for maximum heat output")
                .pointAt(util.vector().topOf(batteryPos))
                .attachKeyFrame();
        scene.world().modifyBlockEntity(batteryPos, HeatBatteryBlockEntity.class, be ->
                be.fluidTank.fill(
                        new FluidStack(ModFluids.MOLTEN_SALT_SOURCE.get(), 12000),
                        IFluidHandler.FluidAction.EXECUTE));
        scene.world().modifyBlock(batteryPos,
                state -> state.setValue(HeatBatteryBlock.HEAT, 2), false);
        scene.idle(80);

        scene.overlay().showText(60)
                .text("The block's glow indicates heat level — brighter means hotter")
                .pointAt(util.vector().centerOf(batteryPos))
                .attachKeyFrame();
        scene.idle(70);

        scene.markAsFinished();
    }

    public static void usage(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("heat_battery_usage", "Using the Heat Battery");
        scene.configureBasePlate(0, 0, 5);

        BlockPos batteryPos = new BlockPos(2, 1, 2);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().position(batteryPos), Direction.DOWN);
        scene.idle(20);

        scene.world().modifyBlockEntity(batteryPos, HeatBatteryBlockEntity.class, be ->
                be.fluidTank.fill(
                        new FluidStack(ModFluids.MOLTEN_SALT_SOURCE.get(), 16000),
                        IFluidHandler.FluidAction.EXECUTE));

        scene.overlay().showText(70)
                .text("A filled Heat Battery acts as a heat source for Create's steam engines")
                .pointAt(util.vector().topOf(batteryPos))
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(70)
                .text("Place it directly below a Boiler to supply heat — no fuel required")
                .pointAt(util.vector().centerOf(batteryPos))
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(60)
                .text("The heat level depends on how much Molten Salt is stored")
                .pointAt(util.vector().topOf(batteryPos))
                .attachKeyFrame();
        scene.idle(70);

        scene.overlay().showText(60)
                .text("Stored heat dissipates slowly over time — keep it topped up with the Solar Heater")
                .pointAt(util.vector().centerOf(batteryPos))
                .attachKeyFrame();
        scene.world().modifyBlock(batteryPos,
                state -> state.setValue(HeatBatteryBlock.HEAT, 1), false);
        scene.idle(70);

        scene.markAsFinished();
    }
}