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
import net.succ.solar_punk.block.custom.EndPlateBlock;
import net.succ.solar_punk.block.custom.StreetlampBlock;
import net.succ.solar_punk.block.entity.custom.EndPlateBlockEntity;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.wire.WireTier;

// Schematic (ModPonderProvider "fuel_cell/usage"): the controller End Plate (facing west) at
// (1,1,2), Fuel Cells at x 2-4 and the other plate (facing east) at (5,1,2). Hydrogen comes in on
// the north of the west plate, Oxygen on the south of the east plate, and Water leaves on the
// north of the east plate (each plate's intake is clockwise of its facing). A Wire Connector on
// the middle cell is wired to one on a Streetlamp behind the stack.
public class FuelCellScenes {

    private static final BlockPos WEST_PLATE = new BlockPos(1, 1, 2);
    private static final BlockPos EAST_PLATE = new BlockPos(5, 1, 2);
    private static final BlockPos MIDDLE_CELL = new BlockPos(3, 1, 2);
    private static final BlockPos CELL_PLUG = new BlockPos(3, 2, 2);
    private static final BlockPos LAMP_PLUG = new BlockPos(3, 3, 4);

    public static void usage(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("fuel_cell_usage", "Building a Fuel Cell Stack");
        scene.configureBasePlate(0, 0, 7);
        scene.scaleSceneView(0.9f);

        Selection cells = util.select().fromTo(2, 1, 2, 4, 1, 2);
        Selection plates = util.select().position(WEST_PLATE).add(util.select().position(EAST_PLATE));
        Selection hydrogenLine = util.select().fromTo(1, 1, 0, 1, 1, 1);
        Selection waterLine = util.select().fromTo(5, 1, 0, 5, 1, 1);
        Selection oxygenLine = util.select().fromTo(5, 1, 3, 5, 1, 4);
        Selection lamp = util.select().fromTo(3, 1, 4, 3, 3, 4);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        for (int x = 2; x <= 4; x++) {
            scene.world().showSection(util.select().position(x, 1, 2), Direction.DOWN);
            scene.idle(5);
        }
        scene.idle(10);
        scene.overlay().showText(70)
                .text("A Fuel Cell stack turns Hydrogen and Oxygen back into FE, and Water")
                .pointAt(util.vector().topOf(MIDDLE_CELL))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().showSection(util.select().position(WEST_PLATE), Direction.EAST);
        scene.world().showSection(util.select().position(EAST_PLATE), Direction.WEST);
        scene.idle(15);
        scene.overlay().showOutline(PonderPalette.GREEN, "stack", cells.copy().add(plates), 80);
        scene.overlay().showText(80)
                .text("Put up to 16 Fuel Cells in a straight line, with an End Plate facing out at each end")
                .pointAt(util.vector().blockSurface(WEST_PLATE, Direction.WEST))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.world().showSection(hydrogenLine, Direction.SOUTH);
        scene.idle(5);
        scene.world().showSection(oxygenLine, Direction.NORTH);
        scene.idle(10);
        scene.overlay().showText(80)
                .text("Each plate has a gas intake, marked by the arrow pointing in. Pipe Hydrogen into one plate and Oxygen into the other")
                .pointAt(util.vector().blockSurface(WEST_PLATE, Direction.NORTH))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);
        scene.overlay().showControls(util.vector().topOf(1, 1, 1), Pointing.DOWN, 40)
                .withItem(new ItemStack(ModFluids.HYDROGEN_BUCKET.get()));
        scene.idle(45);
        scene.overlay().showControls(util.vector().topOf(5, 1, 3), Pointing.DOWN, 40)
                .withItem(new ItemStack(ModFluids.OXYGEN_BUCKET.get()));
        scene.world().modifyBlockEntity(WEST_PLATE, EndPlateBlockEntity.class, be -> {
            be.hydrogenTank.fill(new FluidStack(ModFluids.HYDROGEN_SOURCE.get(), 4000), IFluidHandler.FluidAction.EXECUTE);
            be.oxygenTank.fill(new FluidStack(ModFluids.OXYGEN_SOURCE.get(), 2000), IFluidHandler.FluidAction.EXECUTE);
        });
        scene.idle(90);

        scene.world().modifyBlocks(plates, s -> s.setValue(EndPlateBlock.LIT, true), false);
        scene.world().modifyBlockEntity(WEST_PLATE, EndPlateBlockEntity.class, be ->
                be.waterTank.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE));
        scene.effects().indicateSuccess(MIDDLE_CELL);
        scene.overlay().showText(80)
                .text("Every cell makes 13 FE/t, burning 1 mB of Hydrogen and half as much Oxygen each tick")
                .pointAt(util.vector().topOf(MIDDLE_CELL))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.world().showSection(waterLine, Direction.SOUTH);
        scene.idle(10);
        scene.overlay().showControls(util.vector().topOf(5, 1, 1), Pointing.DOWN, 70)
                .withItem(new ItemStack(Items.WATER_BUCKET));
        scene.overlay().showText(70)
                .text("Water comes out of the other port. Pipe it away, or the stack stops when its tank fills")
                .pointAt(util.vector().blockSurface(EAST_PLATE, Direction.NORTH))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().showSection(lamp, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(CELL_PLUG), Direction.DOWN);
        scene.idle(15);
        WireScenes.wire(scene, CELL_PLUG, 0, LAMP_PLUG, 0, WireTier.COPPER);
        scene.idle(10);
        scene.world().modifyBlocks(util.select().fromTo(3, 1, 4, 3, 2, 4), s -> s.setValue(StreetlampBlock.LIT, true), false);
        scene.overlay().showText(80)
                .text("FE comes out of every face of the stack, and a Wire Connector works on any cell")
                .pointAt(WireScenes.tip(util, CELL_PLUG))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showText(80)
                .text("Make Hydrogen with an Electrolyzer while the sun shines, and burn it here to keep the lights on at night")
                .pointAt(util.vector().topOf(3, 2, 4))
                .placeNearTarget();
        scene.idle(90);

        scene.effects().indicateRedstone(EAST_PLATE);
        scene.world().modifyBlocks(plates, s -> s.setValue(EndPlateBlock.LIT, false), false);
        scene.overlay().showText(70)
                .text("A redstone signal on either End Plate pauses the stack")
                .pointAt(util.vector().centerOf(EAST_PLATE))
                .colored(PonderPalette.RED)
                .placeNearTarget();
        scene.idle(80);

        scene.markAsFinished();
    }

    private FuelCellScenes() {}
}
