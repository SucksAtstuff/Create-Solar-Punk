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
import net.succ.solar_punk.block.custom.SolarHeaterBlock;
import net.succ.solar_punk.block.entity.custom.SolarHeaterBlockEntity;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.item.ModItems;

public class SolarHeaterScenes {

    private static final BlockPos HEATER = new BlockPos(2, 1, 2);

    public static void usage(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("solar_heater_usage", "Using the Solar Heater");
        scene.configureBasePlate(0, 0, 5);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().position(HEATER), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("The Solar Heater uses sunlight to melt items into fluids")
                .pointAt(util.vector().topOf(HEATER))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showControls(util.vector().topOf(HEATER), Pointing.DOWN, 70)
                .rightClick().withItem(new ItemStack(ModItems.SALT.get()));
        scene.world().modifyBlockEntity(HEATER, SolarHeaterBlockEntity.class, be ->
                be.itemHandler.setStackInSlot(0, new ItemStack(ModItems.SALT.get())));
        scene.overlay().showText(70)
                .text("Right-click to put an item in, one at a time - Salt melts into Molten Salt")
                .pointAt(util.vector().topOf(HEATER))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().modifyBlock(HEATER, s -> s.setValue(SolarHeaterBlock.LIT, true), false);
        scene.world().modifyBlockEntity(HEATER, SolarHeaterBlockEntity.class, be ->
                be.fluidTank.fill(new FluidStack(ModFluids.MOLTEN_SALT_SOURCE.get(), 100),
                        IFluidHandler.FluidAction.EXECUTE));
        scene.effects().indicateSuccess(HEATER);
        scene.overlay().showText(70)
                .text("In daylight it melts one item every 10 seconds. Rain halves that, and at night it stops")
                .pointAt(util.vector().centerOf(HEATER))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showOutline(PonderPalette.RED, "top", util.select().position(HEATER.above()), 70);
        scene.overlay().showText(70)
                .colored(PonderPalette.RED)
                .text("It needs open sky right above it, so nothing can go on top")
                .pointAt(util.vector().topOf(HEATER))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showControls(util.vector().blockSurface(HEATER, Direction.WEST), Pointing.LEFT, 70)
                .withItem(new ItemStack(ModFluids.MOLTEN_SALT_BUCKET.get()));
        scene.overlay().showText(70)
                .text("Pipe the fluid out of any side, or take it with a bucket")
                .pointAt(util.vector().blockSurface(HEATER, Direction.WEST))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showControls(util.vector().topOf(HEATER), Pointing.DOWN, 70)
                .withItem(new ItemStack(Items.COBBLESTONE));
        scene.overlay().showText(70)
                .text("Stone and Cobblestone melt into Lava. JEI shows everything it can melt")
                .pointAt(util.vector().topOf(HEATER))
                .placeNearTarget();
        scene.idle(80);

        scene.markAsFinished();
    }

    public static void evaporation(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("solar_heater_evaporation", "Evaporating Salt");
        scene.configureBasePlate(0, 0, 5);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().position(HEATER), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("The Solar Heater can also boil Water down into Salt")
                .pointAt(util.vector().topOf(HEATER))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showControls(util.vector().blockSurface(HEATER, Direction.WEST), Pointing.LEFT, 70)
                .withItem(new ItemStack(Items.WATER_BUCKET));
        scene.world().modifyBlockEntity(HEATER, SolarHeaterBlockEntity.class, be ->
                be.waterTank.fill(new FluidStack(Fluids.WATER, 4000), IFluidHandler.FluidAction.EXECUTE));
        scene.overlay().showText(70)
                .text("Pipe Water into any side, or right-click with a bucket")
                .pointAt(util.vector().blockSurface(HEATER, Direction.WEST))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().modifyBlock(HEATER, s -> s.setValue(SolarHeaterBlock.LIT, true), false);
        scene.world().modifyBlockEntity(HEATER, SolarHeaterBlockEntity.class, be ->
                be.itemHandler.setStackInSlot(1, new ItemStack(ModItems.SALT.get(), 4)));
        scene.effects().indicateSuccess(HEATER);
        scene.overlay().showText(70)
                .text("In daylight, every 250 mB of Water leaves behind one Salt")
                .pointAt(util.vector().centerOf(HEATER))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showControls(util.vector().topOf(HEATER), Pointing.DOWN, 70)
                .withItem(new ItemStack(ModItems.SALT.get()));
        scene.overlay().showText(70)
                .text("Take the Salt by hand or with a Hopper or Funnel, then melt it into Molten Salt")
                .pointAt(util.vector().topOf(HEATER))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showText(70)
                .text("It's slow, so once you have Molten Salt, a Crystallizer is a faster way to make more")
                .pointAt(util.vector().centerOf(HEATER))
                .placeNearTarget();
        scene.idle(80);

        scene.markAsFinished();
    }
}
