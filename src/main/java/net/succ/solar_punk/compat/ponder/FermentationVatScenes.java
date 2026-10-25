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
import net.succ.solar_punk.block.custom.FermentationVatBlock;
import net.succ.solar_punk.block.entity.custom.FermentationVatBlockEntity;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.item.ModItems;

public class FermentationVatScenes {

    // Both vats start at (1,1,1), which is the controller. Usage: 2x2x4, the smallest that
    // ferments. Scaling: 3x3x6. Matches the schematics in ModPonderProvider.
    private static final BlockPos CONTROLLER = new BlockPos(1, 1, 1);
    public static final int USAGE_WIDTH = 2, USAGE_HEIGHT = 4;
    public static final int SCALING_WIDTH = 3, SCALING_HEIGHT = 6;

    private static void showLayer(CreateSceneBuilder scene, SceneBuildingUtil util, int width, int y) {
        scene.world().showSection(util.select().fromTo(1, y, 1, width, y, width), Direction.DOWN);
    }

    private static void setLit(CreateSceneBuilder scene, int width, int height, boolean lit) {
        for (int y = 1; y <= height; y++)
            for (int x = 1; x <= width; x++)
                for (int z = 1; z <= width; z++)
                    scene.world().modifyBlock(new BlockPos(x, y, z), s -> s.setValue(FermentationVatBlock.LIT, lit), false);
    }

    public static void usage(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("fermentation_vat_usage", "Using the Fermentation Vat");
        scene.configureBasePlate(0, 0, 5);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        BlockPos top = new BlockPos(1, USAGE_HEIGHT, 1);
        BlockPos side = new BlockPos(1, 2, 1);

        for (int y = 1; y <= USAGE_HEIGHT; y++) {
            showLayer(scene, util, USAGE_WIDTH, y);
            scene.idle(6);
        }
        scene.idle(15);

        scene.overlay().showOutline(PonderPalette.GREEN, "vat",
                util.select().fromTo(1, 1, 1, USAGE_WIDTH, USAGE_HEIGHT, USAGE_WIDTH), 80);
        scene.overlay().showText(80)
                .text("Fermentation Vat blocks merge into one big vat. It needs at least a 2x2 footprint and 4 blocks of height")
                .pointAt(util.vector().topOf(top))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showControls(util.vector().blockSurface(side, Direction.WEST), Pointing.LEFT, 70)
                .withItem(new ItemStack(Items.WATER_BUCKET));
        scene.world().modifyBlockEntity(CONTROLLER, FermentationVatBlockEntity.class, be ->
                be.waterTank.fill(new FluidStack(Fluids.WATER, 8000), IFluidHandler.FluidAction.EXECUTE));
        scene.overlay().showText(70)
                .text("Pipe Water into any side")
                .pointAt(util.vector().blockSurface(side, Direction.WEST))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showControls(util.vector().topOf(top), Pointing.DOWN, 70)
                .withItem(new ItemStack(ModItems.BIOMASS.get()));
        scene.world().modifyBlockEntity(CONTROLLER, FermentationVatBlockEntity.class, be ->
                be.itemHandler.setStackInSlot(0, new ItemStack(ModItems.BIOMASS.get(), 16)));
        scene.overlay().showText(70)
                .text("...and feed it Biomass with a Funnel or Hopper")
                .pointAt(util.vector().topOf(top))
                .placeNearTarget();
        scene.idle(80);

        setLit(scene, USAGE_WIDTH, USAGE_HEIGHT, true);
        scene.world().modifyBlockEntity(CONTROLLER, FermentationVatBlockEntity.class, be ->
                be.biofuelTank.fill(new FluidStack(ModFluids.BIOFUEL_SOURCE.get(), 4000),
                        IFluidHandler.FluidAction.EXECUTE));
        scene.effects().indicateSuccess(top);
        scene.overlay().showText(70)
                .text("It ferments them together into Biofuel, a batch at a time")
                .pointAt(util.vector().centerOf(side))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showControls(util.vector().blockSurface(side, Direction.NORTH), Pointing.RIGHT, 70)
                .withItem(new ItemStack(ModFluids.BIOFUEL_BUCKET.get()));
        scene.overlay().showText(70)
                .text("Pipe the Biofuel out of any side, to a Biofuel Engine")
                .pointAt(util.vector().blockSurface(side, Direction.NORTH))
                .placeNearTarget();
        scene.idle(80);

        scene.markAsFinished();
    }

    public static void scaling(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("fermentation_vat_scaling", "Scaling the Fermentation Vat");
        scene.configureBasePlate(0, 0, 5);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        BlockPos middle = new BlockPos(2, 3, 2);

        for (int y = 1; y <= 4; y++) {
            showLayer(scene, util, SCALING_WIDTH, y);
            scene.idle(6);
        }
        scene.idle(15);

        scene.overlay().showText(80)
                .text("A wider vat takes more Biomass per batch, and makes more Biofuel: 4 at a time for 2x2, 9 for 3x3")
                .pointAt(util.vector().topOf(new BlockPos(2, 4, 2)))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        for (int y = 5; y <= SCALING_HEIGHT; y++) {
            showLayer(scene, util, SCALING_WIDTH, y);
            scene.idle(8);
        }
        scene.idle(10);

        scene.overlay().showOutline(PonderPalette.GREEN, "vat",
                util.select().fromTo(1, 1, 1, SCALING_WIDTH, SCALING_HEIGHT, SCALING_WIDTH), 80);
        scene.overlay().showText(80)
                .text("Building it taller makes each batch give much more Biofuel, and holds more fluid")
                .pointAt(util.vector().topOf(new BlockPos(2, SCALING_HEIGHT, 2)))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        setLit(scene, SCALING_WIDTH, SCALING_HEIGHT, true);
        scene.effects().indicateSuccess(middle);
        scene.overlay().showText(70)
                .text("Vats can go up to 3x3 wide and 16 tall. Goggles show each batch's size and output")
                .pointAt(util.vector().centerOf(middle))
                .placeNearTarget();
        scene.idle(80);

        scene.markAsFinished();
    }
}
