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
import net.succ.solar_punk.block.custom.BrassSolarPanelBlock;
import net.succ.solar_punk.block.custom.ElectrolyzerBlock;
import net.succ.solar_punk.block.entity.custom.ElectrolyzerBlockEntity;
import net.succ.solar_punk.fluid.ModFluids;

public class ElectrolyzerScenes {

    private static final BlockPos MACHINE = new BlockPos(2, 1, 2);
    private static final BlockPos PANEL = new BlockPos(2, 1, 3);

    public static void usage(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("electrolyzer_usage", "Using the Electrolyzer");
        scene.configureBasePlate(0, 0, 5);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().position(MACHINE), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("The Electrolyzer splits Water into Hydrogen and Oxygen using FE")
                .pointAt(util.vector().centerOf(MACHINE))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().showSection(util.select().position(PANEL), Direction.DOWN);
        scene.idle(10);
        scene.world().modifyBlock(PANEL, s -> s.setValue(BrassSolarPanelBlock.LIT, true), false);
        scene.overlay().showText(70)
                .text("Power it from any side, with a Brass Solar Panel or a Wire Connector")
                .pointAt(util.vector().topOf(PANEL))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().showSection(util.select().fromTo(2, 2, 2, 2, 3, 2), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showControls(util.vector().topOf(2, 3, 2), Pointing.DOWN, 30)
                .withItem(new ItemStack(Items.WATER_BUCKET));
        scene.idle(10);
        scene.world().modifyBlockEntity(MACHINE, ElectrolyzerBlockEntity.class, be ->
                be.waterTank.fill(new FluidStack(Fluids.WATER, 4000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(25);
        scene.overlay().showText(70)
                .text("Pipe Water into the top, or any face without a flange")
                .pointAt(util.vector().blockSurface(MACHINE, Direction.NORTH))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().modifyBlock(MACHINE, s -> s.setValue(ElectrolyzerBlock.LIT, true), false);
        scene.world().modifyBlockEntity(MACHINE, ElectrolyzerBlockEntity.class, be -> {
            be.waterTank.drain(1500, IFluidHandler.FluidAction.EXECUTE);
            be.hydrogenTank.fill(new FluidStack(ModFluids.HYDROGEN_SOURCE.get(), 3000), IFluidHandler.FluidAction.EXECUTE);
            be.oxygenTank.fill(new FluidStack(ModFluids.OXYGEN_SOURCE.get(), 1500), IFluidHandler.FluidAction.EXECUTE);
        });
        scene.overlay().showText(70)
                .text("Gas collects at the top of each tube. It makes twice as much Hydrogen as Oxygen")
                .pointAt(util.vector().centerOf(MACHINE))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        // Facing north, Hydrogen comes out of the east flange and Oxygen out of the west one
        // (the left and right as seen from the front).
        scene.world().showSection(util.select().fromTo(3, 1, 2, 4, 1, 2), Direction.WEST);
        scene.idle(10);
        scene.overlay().showControls(util.vector().topOf(3, 1, 2), Pointing.DOWN, 30)
                .withItem(new ItemStack(ModFluids.HYDROGEN_BUCKET.get()));
        scene.idle(35);
        scene.overlay().showText(60)
                .text("Hydrogen leaves through the copper flange beside its tube...")
                .pointAt(util.vector().centerOf(3, 1, 2))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(70);

        scene.world().showSection(util.select().fromTo(0, 1, 2, 1, 1, 2), Direction.EAST);
        scene.idle(10);
        scene.overlay().showControls(util.vector().topOf(1, 1, 2), Pointing.DOWN, 30)
                .withItem(new ItemStack(ModFluids.OXYGEN_BUCKET.get()));
        scene.idle(35);
        scene.overlay().showText(60)
                .text("...and Oxygen through the one on the other side")
                .pointAt(util.vector().centerOf(1, 1, 2))
                .placeNearTarget();
        scene.idle(70);

        scene.overlay().showText(70)
                .text("If either gas tank fills up it stops, so pipe both gases away: to a Fuel Cell stack, or a Steam Turbine for the Hydrogen")
                .pointAt(util.vector().centerOf(MACHINE))
                .placeNearTarget();
        scene.idle(80);

        scene.markAsFinished();
    }
}
