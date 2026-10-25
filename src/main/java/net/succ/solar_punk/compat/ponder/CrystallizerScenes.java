package net.succ.solar_punk.compat.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.succ.solar_punk.block.custom.CrystallizerBlock;
import net.succ.solar_punk.block.entity.custom.CrystallizerBlockEntity;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.item.ModItems;

public class CrystallizerScenes {

    private static final BlockPos MACHINE = new BlockPos(2, 1, 2);

    public static void usage(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("crystallizer_usage", "Using the Crystallizer");
        scene.configureBasePlate(0, 0, 5);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().position(MACHINE), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("The Crystallizer quenches Molten Salt with Water, turning it back into Salt")
                .pointAt(util.vector().topOf(MACHINE))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showControls(util.vector().blockSurface(MACHINE, Direction.WEST), Pointing.LEFT, 70)
                .withItem(new ItemStack(ModFluids.MOLTEN_SALT_BUCKET.get()));
        scene.overlay().showControls(util.vector().blockSurface(MACHINE, Direction.NORTH), Pointing.RIGHT, 70)
                .withItem(new ItemStack(Items.WATER_BUCKET));
        scene.overlay().showText(70)
                .text("Pipe both fluids into any side, or right-click with buckets")
                .pointAt(util.vector().topOf(MACHINE))
                .placeNearTarget()
                .attachKeyFrame();
        scene.world().modifyBlockEntity(MACHINE, CrystallizerBlockEntity.class, be -> {
            be.inputTankA.fill(new FluidStack(ModFluids.MOLTEN_SALT_SOURCE.get(), 4000), IFluidHandler.FluidAction.EXECUTE);
            be.inputTankB.fill(new FluidStack(Fluids.WATER, 4000), IFluidHandler.FluidAction.EXECUTE);
        });
        scene.idle(80);

        scene.world().modifyBlock(MACHINE, s -> s.setValue(CrystallizerBlock.LIT, true), false);
        scene.effects().indicateSuccess(MACHINE);
        scene.overlay().showText(70)
                .text("It works day or night, and much faster than evaporating water in a Solar Heater")
                .pointAt(util.vector().centerOf(MACHINE))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showControls(util.vector().topOf(MACHINE), Pointing.DOWN, 70)
                .withItem(new ItemStack(ModItems.SALT.get()));
        scene.overlay().showText(70)
                .text("Salt collects inside. Take it by hand, or with a Hopper or Funnel")
                .pointAt(util.vector().topOf(MACHINE))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showControls(util.vector().blockSurface(MACHINE, Direction.EAST), Pointing.RIGHT, 70)
                .withItem(new ItemStack(ModFluids.STEAM_BUCKET.get()));
        scene.overlay().showText(70)
                .text("The quench also flashes off a little Steam - pipe it out to a Steam Turbine")
                .pointAt(util.vector().blockSurface(MACHINE, Direction.EAST))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showText(60)
                .text("Its recipes come from datapacks, so modpacks can add their own")
                .pointAt(util.vector().topOf(MACHINE))
                .placeNearTarget();
        scene.idle(70);

        scene.markAsFinished();
    }
}
