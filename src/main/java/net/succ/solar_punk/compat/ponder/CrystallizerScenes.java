package net.succ.solar_punk.compat.ponder;

import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.succ.solar_punk.block.custom.CrystallizerBlock;
import net.succ.solar_punk.block.entity.custom.CrystallizerBlockEntity;
import net.succ.solar_punk.fluid.ModFluids;

public class CrystallizerScenes {

    public static void usage(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("crystallizer_usage", "Using the Crystallizer");
        scene.configureBasePlate(0, 0, 5);

        BlockPos crystallizerPos = new BlockPos(2, 1, 2);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().position(crystallizerPos), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("The Crystallizer runs data-driven recipes: two fluids in, an item out, plus an optional fluid byproduct")
                .pointAt(util.vector().topOf(crystallizerPos))
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(60)
                .text("By default it quenches Molten Salt with Water to produce Salt")
                .pointAt(util.vector().topOf(crystallizerPos))
                .attachKeyFrame();
        scene.world().modifyBlockEntity(crystallizerPos, CrystallizerBlockEntity.class, be -> {
            be.inputTankA.fill(new FluidStack(ModFluids.MOLTEN_SALT_SOURCE.get(), 4000), IFluidHandler.FluidAction.EXECUTE);
            be.inputTankB.fill(new FluidStack(Fluids.WATER, 4000), IFluidHandler.FluidAction.EXECUTE);
        });
        scene.idle(70);

        scene.overlay().showText(70)
                .text("Unlike the Solar Heater, it works day or night - cooling doesn't need sunlight")
                .pointAt(util.vector().centerOf(crystallizerPos))
                .attachKeyFrame();
        scene.world().modifyBlock(crystallizerPos,
                state -> state.setValue(CrystallizerBlock.LIT, true), false);
        scene.idle(80);

        scene.overlay().showText(70)
                .text("The result collects in the block's output slot - grab it by hand or pull it out with a hopper")
                .pointAt(util.vector().topOf(crystallizerPos))
                .attachKeyFrame();
        scene.idle(70);

        scene.overlay().showText(60)
                .text("Quenching Molten Salt also flashes off some Steam as a byproduct, ready for a Steam Turbine")
                .pointAt(util.vector().topOf(crystallizerPos))
                .attachKeyFrame();
        scene.idle(70);

        scene.markAsFinished();
    }
}
