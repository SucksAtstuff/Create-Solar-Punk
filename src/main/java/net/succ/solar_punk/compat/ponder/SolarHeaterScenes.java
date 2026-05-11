package net.succ.solar_punk.compat.ponder;

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
import net.succ.solar_punk.item.ModItems;

public class SolarHeaterScenes {

    public static void usage(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("solar_heater_usage", "Using the Solar Heater");
        scene.configureBasePlate(0, 0, 5);

        BlockPos heaterPos = new BlockPos(2, 1, 2);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().position(heaterPos), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("The Solar Heater melts items into fluids using sunlight")
                .pointAt(util.vector().topOf(heaterPos))
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(60)
                .text("Right-click to place an item into the input slot")
                .pointAt(util.vector().topOf(heaterPos))
                .attachKeyFrame();
        scene.world().modifyBlockEntity(heaterPos, SolarHeaterBlockEntity.class, be ->
                be.itemHandler.setStackInSlot(0, new ItemStack(Items.STONE)));
        scene.idle(70);

        scene.overlay().showText(70)
                .text("During the day with clear skies, the heater slowly processes the item")
                .pointAt(util.vector().centerOf(heaterPos))
                .attachKeyFrame();
        scene.world().modifyBlock(heaterPos,
                state -> state.setValue(SolarHeaterBlock.LIT, true), false);
        scene.idle(80);

        scene.overlay().showText(60)
                .text("Rain halves the processing speed")
                .pointAt(util.vector().centerOf(heaterPos))
                .attachKeyFrame();
        scene.idle(70);

        scene.overlay().showText(60)
                .text("The resulting fluid fills the output tank — drain it with a pipe or bucket")
                .pointAt(util.vector().topOf(heaterPos))
                .attachKeyFrame();
        scene.idle(70);

        scene.markAsFinished();
    }

    public static void evaporation(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("solar_heater_evaporation", "Evaporating Salt");
        scene.configureBasePlate(0, 0, 5);

        BlockPos heaterPos = new BlockPos(2, 1, 2);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().position(heaterPos), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("The Solar Heater can also evaporate water into Salt")
                .pointAt(util.vector().topOf(heaterPos))
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(60)
                .text("Fill the water tank by right-clicking with a water bucket, or pipe water in from the side")
                .pointAt(util.vector().topOf(heaterPos))
                .attachKeyFrame();
        scene.world().modifyBlockEntity(heaterPos, SolarHeaterBlockEntity.class, be ->
                be.waterTank.fill(new FluidStack(Fluids.WATER, 4000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(70);

        scene.overlay().showText(70)
                .text("With sunlight present, water slowly evaporates and Salt accumulates in the second slot")
                .pointAt(util.vector().centerOf(heaterPos))
                .attachKeyFrame();
        scene.world().modifyBlock(heaterPos,
                state -> state.setValue(SolarHeaterBlock.LIT, true), false);
        scene.idle(80);

        scene.overlay().showText(60)
                .text("Salt can be smelted into Molten Salt directly in the Solar Heater")
                .pointAt(util.vector().topOf(heaterPos))
                .attachKeyFrame();
        scene.world().modifyBlockEntity(heaterPos, SolarHeaterBlockEntity.class, be ->
                be.itemHandler.setStackInSlot(1, new ItemStack(ModItems.SALT.get(), 4)));
        scene.idle(70);

        scene.markAsFinished();
    }
}