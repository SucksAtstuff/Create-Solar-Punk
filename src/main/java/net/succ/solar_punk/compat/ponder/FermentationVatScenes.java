package net.succ.solar_punk.compat.ponder;

import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.succ.solar_punk.block.custom.FermentationVatBlock;
import net.succ.solar_punk.block.entity.custom.FermentationVatBlockEntity;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.item.ModItems;

public class FermentationVatScenes {

    // 2×2×3 vat: x=1-2, z=1-2, y=1-3. Bottom-left corner is the controller in ponder.
    private static final BlockPos CONTROLLER = new BlockPos(1, 1, 1);
    private static final BlockPos TOP_2      = new BlockPos(1, 3, 1);

    // 3×3×3 vat: x=1-3, z=1-3, y=1-3.
    private static final BlockPos CENTER_3   = new BlockPos(2, 2, 2);
    private static final BlockPos TOP_3      = new BlockPos(2, 3, 2);

    private static void showVat(SceneBuilder scene, SceneBuildingUtil util, int width, int height) {
        for (int y = 1; y <= height; y++)
            for (int x = 1; x <= width; x++)
                for (int z = 1; z <= width; z++)
                    scene.world().showSection(util.select().position(new BlockPos(x, y, z)), Direction.DOWN);
    }

    public static void usage(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("fermentation_vat_usage", "Using the Fermentation Vat");
        scene.configureBasePlate(0, 0, 5);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);

        showVat(scene, util, 2, 3);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Stack Fermentation Vat blocks — they merge into a single multiblock")
                .pointAt(util.vector().topOf(TOP_2))
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(60)
                .text("The vat needs at least a 2×2 footprint to ferment anything")
                .pointAt(util.vector().centerOf(CONTROLLER))
                .attachKeyFrame();
        scene.idle(70);

        scene.overlay().showText(60)
                .text("Pipe water in through any face to fill the water tank")
                .pointAt(util.vector().centerOf(CONTROLLER))
                .attachKeyFrame();
        scene.world().modifyBlockEntity(CONTROLLER, FermentationVatBlockEntity.class, be ->
                be.waterTank.fill(new FluidStack(Fluids.WATER, 8000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(70);

        scene.overlay().showText(60)
                .text("Insert Biomass into the input from the top")
                .pointAt(util.vector().topOf(TOP_2))
                .attachKeyFrame();
        scene.world().modifyBlockEntity(CONTROLLER, FermentationVatBlockEntity.class, be ->
                be.itemHandler.setStackInSlot(0, new ItemStack(ModItems.BIOMASS.get(), 16)));
        scene.idle(70);

        scene.overlay().showText(80)
                .text("The vat ferments Biomass with water into Biofuel — a 2×2 vat consumes 4 Biomass per batch")
                .pointAt(util.vector().centerOf(CONTROLLER))
                .attachKeyFrame();
        for (int y = 1; y <= 3; y++)
            for (int x = 1; x <= 2; x++)
                for (int z = 1; z <= 2; z++)
                    scene.world().modifyBlock(new BlockPos(x, y, z),
                            s -> s.setValue(FermentationVatBlock.LIT, true), false);
        scene.world().modifyBlockEntity(CONTROLLER, FermentationVatBlockEntity.class, be ->
                be.biofuelTank.fill(new FluidStack(ModFluids.BIOFUEL_SOURCE.get(), 4000),
                        IFluidHandler.FluidAction.EXECUTE));
        scene.idle(90);

        scene.overlay().showText(60)
                .text("Drain Biofuel from any face and pipe it to a Biofuel Engine")
                .pointAt(util.vector().centerOf(CONTROLLER))
                .attachKeyFrame();
        scene.idle(70);

        scene.markAsFinished();
    }

    public static void scaling(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("fermentation_vat_scaling", "Scaling the Fermentation Vat");
        scene.configureBasePlate(0, 0, 5);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);

        showVat(scene, util, 3, 3);
        scene.idle(20);

        scene.overlay().showText(80)
                .text("A larger footprint increases the batch size — a 2×2 vat processes 4 Biomass at once, a 3×3 processes 9")
                .pointAt(util.vector().topOf(TOP_3))
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showText(70)
                .text("Water consumed and Biofuel produced per batch scale with the footprint area")
                .pointAt(util.vector().centerOf(CENTER_3))
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(60)
                .text("Tank capacity grows with each block added — taller vats hold more fluid")
                .pointAt(util.vector().centerOf(CENTER_3))
                .attachKeyFrame();
        scene.idle(70);

        scene.markAsFinished();
    }
}
