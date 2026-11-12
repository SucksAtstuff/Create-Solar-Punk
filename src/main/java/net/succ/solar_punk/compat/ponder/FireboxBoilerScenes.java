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
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.succ.solar_punk.block.custom.FireboxBoilerBlock;
import net.succ.solar_punk.block.entity.custom.FireboxBoilerBlockEntity;
import net.succ.solar_punk.fluid.ModFluids;

// Schematic (ModPonderProvider "firebox_boiler/usage"): three boilers facing north at x 1-3, z=2,
// a water line behind them (y=1, z=3) and a steam line on top (y=2, z=2), both running off east.
public class FireboxBoilerScenes {

    private static final BlockPos BOILER = new BlockPos(2, 1, 2);

    public static void usage(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("firebox_boiler_usage", "Using the Firebox Boiler");
        scene.configureBasePlate(0, 0, 5);

        Selection boilers = util.select().fromTo(1, 1, 2, 3, 1, 2);
        Selection otherBoilers = boilers.copy().substract(util.select().position(BOILER));
        Selection waterLine = util.select().fromTo(1, 1, 3, 4, 1, 3);
        Selection steamLine = util.select().fromTo(1, 2, 2, 4, 2, 2);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().position(BOILER), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(80)
                .text("The Firebox Boiler burns fuel to boil Water into Steam: an early way to run a Steam Turbine")
                .pointAt(util.vector().topOf(BOILER))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showControls(util.vector().blockSurface(BOILER, Direction.NORTH), Pointing.DOWN, 70)
                .withItem(new ItemStack(Items.COAL));
        scene.overlay().showText(70)
                .text("It burns anything a Furnace does. Put fuel in by hand, or with a Hopper or Funnel")
                .pointAt(util.vector().blockSurface(BOILER, Direction.NORTH))
                .placeNearTarget()
                .attachKeyFrame();
        scene.world().modifyBlockEntity(BOILER, FireboxBoilerBlockEntity.class, be ->
                be.itemHandler.setStackInSlot(0, new ItemStack(Items.COAL, 16)));
        scene.idle(80);

        scene.overlay().showControls(util.vector().topOf(BOILER), Pointing.DOWN, 70)
                .withItem(new ItemStack(Items.WATER_BUCKET));
        scene.overlay().showText(70)
                .text("Pipe Water into any side")
                .pointAt(util.vector().topOf(BOILER))
                .placeNearTarget()
                .attachKeyFrame();
        scene.world().modifyBlockEntity(BOILER, FireboxBoilerBlockEntity.class, be ->
                be.waterTank.fill(new FluidStack(Fluids.WATER, 4000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(80);

        scene.world().modifyBlock(BOILER, s -> s.setValue(FireboxBoilerBlock.LIT, true), false);
        scene.world().modifyBlockEntity(BOILER, FireboxBoilerBlockEntity.class, be ->
                be.steamTank.fill(new FluidStack(ModFluids.STEAM_SOURCE.get(), 2000), IFluidHandler.FluidAction.EXECUTE));
        scene.effects().indicateSuccess(BOILER);
        scene.overlay().showText(70)
                .text("While it burns, it turns 2 mB of Water into 2 mB of Steam every tick")
                .pointAt(util.vector().centerOf(BOILER))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showControls(util.vector().topOf(BOILER), Pointing.DOWN, 70)
                .withItem(new ItemStack(ModFluids.STEAM_BUCKET.get()));
        scene.overlay().showText(70)
                .text("Pipe the Steam out of any side to a Steam Turbine")
                .pointAt(util.vector().topOf(BOILER))
                .placeNearTarget();
        scene.idle(80);

        scene.world().showSection(otherBoilers, Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(waterLine, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(steamLine, Direction.DOWN);
        scene.idle(10);
        scene.world().modifyBlocks(boilers, s -> s.setValue(FireboxBoilerBlock.LIT, true), false);
        scene.overlay().showText(80)
                .text("One boiler won't keep a turbine busy. Build a row of them on shared Water and Steam lines")
                .pointAt(util.vector().topOf(3, 2, 2))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showText(80)
                .text("For more Steam than that, move on to a Solar Power Tower in steam mode")
                .pointAt(util.vector().topOf(1, 2, 2))
                .placeNearTarget();
        scene.idle(90);

        scene.world().modifyBlock(BOILER, s -> s.setValue(FireboxBoilerBlock.LIT, false), false);
        scene.overlay().showText(80)
                .text("Out of Water, or with its Steam tank full, the fire waits instead of wasting fuel")
                .pointAt(util.vector().centerOf(BOILER))
                .colored(PonderPalette.RED)
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.markAsFinished();
    }

    private FireboxBoilerScenes() {}
}
