package net.succ.solar_punk.compat.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.succ.solar_punk.block.custom.HeatBatteryBlock;
import net.succ.solar_punk.block.custom.SolarHeaterBlock;
import net.succ.solar_punk.block.entity.custom.HeatBatteryBlockEntity;
import net.succ.solar_punk.block.entity.custom.SolarHeaterBlockEntity;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.item.ModItems;

public class SolarHeaterScenes {

    // "solar_heater/usage" (shared with "heat_battery/filling"): a Chest on a Hopper at x=0 feeding
    // the heater at (1,1,2), a pipe at (2,1,2) and a Heat Battery at (3,1,2).
    static final BlockPos LINE_HEATER = new BlockPos(1, 1, 2);
    static final BlockPos LINE_HOPPER = new BlockPos(0, 1, 2);
    static final BlockPos LINE_PIPE = new BlockPos(2, 1, 2);
    static final BlockPos LINE_BATTERY = new BlockPos(3, 1, 2);

    // "solar_heater/evaporation": the heater at (2,2,2) on a Hopper emptying into a Chest to the
    // south, with a water pipe coming in from the west.
    private static final BlockPos EVAP_HEATER = new BlockPos(2, 2, 2);
    private static final BlockPos EVAP_HOPPER = new BlockPos(2, 1, 2);
    private static final BlockPos EVAP_CHEST = new BlockPos(2, 1, 3);

    public static void usage(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("solar_heater_usage", "Using the Solar Heater");
        scene.configureBasePlate(0, 0, 5);

        BlockPos heater = LINE_HEATER;

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().position(heater), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("The Solar Heater uses sunlight to melt items into fluids")
                .pointAt(util.vector().topOf(heater))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().showSection(util.select().fromTo(0, 1, 2, 0, 2, 2), Direction.DOWN);
        scene.idle(15);
        scene.overlay().showControls(util.vector().topOf(LINE_HOPPER.above()), Pointing.DOWN, 30)
                .withItem(new ItemStack(ModItems.SALT.get()));
        scene.idle(10);
        scene.world().modifyBlockEntity(heater, SolarHeaterBlockEntity.class, be ->
                be.itemHandler.setStackInSlot(0, new ItemStack(ModItems.SALT.get())));
        scene.idle(25);
        scene.overlay().showText(70)
                .text("Feed it from a Hopper or Funnel, or right-click items in. Salt melts into Molten Salt")
                .pointAt(util.vector().centerOf(LINE_HOPPER))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().modifyBlock(heater, s -> s.setValue(SolarHeaterBlock.LIT, true), false);
        scene.world().modifyBlockEntity(heater, SolarHeaterBlockEntity.class, be ->
                be.fluidTank.fill(new FluidStack(ModFluids.MOLTEN_SALT_SOURCE.get(), 1000),
                        IFluidHandler.FluidAction.EXECUTE));
        scene.effects().indicateSuccess(heater);
        scene.overlay().showText(70)
                .text("In daylight it melts one item every 10 seconds. Rain halves that, and at night it stops")
                .pointAt(util.vector().centerOf(heater))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showOutline(PonderPalette.RED, "top", util.select().position(heater.above()), 70);
        scene.overlay().showText(70)
                .colored(PonderPalette.RED)
                .text("It needs open sky right above it, so feed it from the side, never from on top")
                .pointAt(util.vector().topOf(heater))
                .placeNearTarget();
        scene.idle(80);

        scene.world().showSection(util.select().position(LINE_PIPE), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(LINE_BATTERY), Direction.DOWN);
        scene.idle(15);
        scene.world().modifyBlockEntity(LINE_BATTERY, HeatBatteryBlockEntity.class, be ->
                be.fluidTank.fill(new FluidStack(ModFluids.MOLTEN_SALT_SOURCE.get(), 4000),
                        IFluidHandler.FluidAction.EXECUTE));
        scene.world().modifyBlock(LINE_BATTERY, s -> s.setValue(HeatBatteryBlock.HEAT, 1), false);
        scene.overlay().showText(70)
                .text("Pipe the fluid out of any side, here into a Heat Battery to run a boiler")
                .pointAt(util.vector().centerOf(LINE_PIPE))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showControls(util.vector().topOf(LINE_HOPPER.above()), Pointing.DOWN, 30)
                .withItem(new ItemStack(Items.COBBLESTONE));
        scene.idle(35);
        scene.overlay().showText(70)
                .text("Stone and Cobblestone melt into Lava. JEI shows everything it can melt")
                .pointAt(util.vector().topOf(heater))
                .placeNearTarget();
        scene.idle(80);

        scene.markAsFinished();
    }

    public static void evaporation(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("solar_heater_evaporation", "Evaporating Salt");
        scene.configureBasePlate(0, 0, 5);

        BlockPos heater = EVAP_HEATER;

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(EVAP_HOPPER, EVAP_CHEST), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(heater), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("The Solar Heater can also boil Water down into Salt")
                .pointAt(util.vector().topOf(heater))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().showSection(util.select().fromTo(0, 2, 2, 1, 2, 2), Direction.EAST);
        scene.idle(10);
        scene.overlay().showControls(util.vector().topOf(1, 2, 2), Pointing.DOWN, 30)
                .withItem(new ItemStack(Items.WATER_BUCKET));
        scene.world().modifyBlockEntity(heater, SolarHeaterBlockEntity.class, be ->
                be.waterTank.fill(new FluidStack(Fluids.WATER, 4000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(35);
        scene.overlay().showText(70)
                .text("Pipe Water into any side, or right-click with a bucket")
                .pointAt(util.vector().centerOf(1, 2, 2))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().modifyBlock(heater, s -> s.setValue(SolarHeaterBlock.LIT, true), false);
        scene.world().modifyBlockEntity(heater, SolarHeaterBlockEntity.class, be ->
                be.itemHandler.setStackInSlot(1, new ItemStack(ModItems.SALT.get(), 4)));
        scene.effects().indicateSuccess(heater);
        scene.overlay().showText(70)
                .text("In daylight, every 250 mB of Water leaves behind one Salt")
                .pointAt(util.vector().centerOf(heater))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showControls(util.vector().topOf(EVAP_CHEST), Pointing.DOWN, 30)
                .withItem(new ItemStack(ModItems.SALT.get()));
        scene.idle(35);
        scene.overlay().showText(70)
                .text("A Hopper underneath pulls the Salt out into a Chest, ready to melt into Molten Salt")
                .pointAt(util.vector().centerOf(EVAP_HOPPER))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(70)
                .text("It's slow, so once you have Molten Salt, a Crystallizer is a faster way to make more")
                .pointAt(util.vector().centerOf(heater))
                .placeNearTarget();
        scene.idle(80);

        scene.markAsFinished();
    }
}
