package net.succ.solar_punk.compat.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.succ.solar_punk.block.custom.BiomassGasifierBlock;
import net.succ.solar_punk.block.entity.custom.BiomassGasifierBlockEntity;
import net.succ.solar_punk.item.ModItems;

public class BiomassGasifierScenes {

    private static final BlockPos MACHINE = new BlockPos(2, 2, 2);
    private static final BlockPos SHAFT   = new BlockPos(2, 1, 2);

    public static void usage(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("biomass_gasifier_usage", "Using the Biomass Gasifier");
        scene.configureBasePlate(0, 0, 5);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);

        scene.world().showSection(util.select().position(SHAFT), Direction.UP);
        scene.world().showSection(util.select().position(MACHINE), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("The Biomass Gasifier burns Biomass to generate Rotational Force")
                .pointAt(util.vector().centerOf(MACHINE))
                .attachKeyFrame();
        scene.idle(80);

        scene.world().modifyBlockEntity(MACHINE, BiomassGasifierBlockEntity.class, be ->
                be.itemHandler.setStackInSlot(0, new ItemStack(ModItems.BIOMASS.get(), 8)));
        scene.overlay().showText(60)
                .text("Add Biomass into the top slot - each piece burns for 15 seconds")
                .pointAt(util.vector().topOf(MACHINE))
                .attachKeyFrame();
        scene.idle(70);

        scene.world().modifyBlock(MACHINE, s -> s.setValue(BiomassGasifierBlock.LIT, true), false);
        scene.world().setKineticSpeed(util.select().position(MACHINE), 8f);
        scene.world().setKineticSpeed(util.select().position(SHAFT), 8f);
        scene.overlay().showText(70)
                .text("While burning, it outputs 8 RPM with 2048 SU of stress capacity")
                .pointAt(util.vector().centerOf(MACHINE))
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(60)
                .text("Rotation exits downward - connect a shaft or machine directly below")
                .pointAt(util.vector().centerOf(SHAFT))
                .attachKeyFrame();
        scene.idle(70);

        scene.overlay().showText(70)
                .text("Output is intermittent - use a Kinetic Battery to buffer power between refills")
                .pointAt(util.vector().centerOf(MACHINE))
                .attachKeyFrame();
        scene.idle(80);

        scene.markAsFinished();
    }
}