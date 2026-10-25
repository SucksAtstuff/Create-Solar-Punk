package net.succ.solar_punk.compat.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
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
        scene.world().showSection(util.select().position(SHAFT), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(MACHINE), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("The Biomass Gasifier burns Biomass to make Rotational Force")
                .pointAt(util.vector().topOf(MACHINE))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showControls(util.vector().topOf(MACHINE), Pointing.DOWN, 70)
                .withItem(new ItemStack(ModItems.BIOMASS.get()));
        scene.overlay().showText(70)
                .text("Feed it Biomass with a Funnel, Hopper or Chute")
                .pointAt(util.vector().topOf(MACHINE))
                .placeNearTarget()
                .attachKeyFrame();
        scene.world().modifyBlockEntity(MACHINE, BiomassGasifierBlockEntity.class, be ->
                be.itemHandler.setStackInSlot(0, new ItemStack(ModItems.BIOMASS.get(), 8)));
        scene.idle(80);

        scene.world().modifyBlock(MACHINE, s -> s.setValue(BiomassGasifierBlock.LIT, true), false);
        scene.world().setKineticSpeed(util.select().position(MACHINE).add(util.select().position(SHAFT)), 8f);
        scene.effects().indicateSuccess(MACHINE);
        scene.overlay().showText(70)
                .text("While burning it gives 8 RPM and 2048 SU of stress capacity")
                .pointAt(util.vector().centerOf(MACHINE))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showOutline(PonderPalette.GREEN, "shaft", util.select().position(SHAFT), 60);
        scene.overlay().showText(60)
                .text("Rotation comes out of the bottom")
                .pointAt(util.vector().centerOf(SHAFT))
                .placeNearTarget();
        scene.idle(70);

        scene.overlay().showControls(util.vector().topOf(MACHINE), Pointing.DOWN, 70)
                .withItem(new ItemStack(ModItems.BIOMASS_PELLET.get()));
        scene.overlay().showText(70)
                .text("Biomass Pellets burn twice as long, and leave twice the Biochar")
                .pointAt(util.vector().topOf(MACHINE))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showControls(util.vector().blockSurface(MACHINE, Direction.WEST), Pointing.LEFT, 70)
                .withItem(new ItemStack(ModItems.BIOCHAR.get()));
        scene.overlay().showText(70)
                .text("Burnt fuel leaves Biochar behind. Pull it out with a Funnel or Chute, or the Gasifier pauses when it's full")
                .pointAt(util.vector().blockSurface(MACHINE, Direction.WEST))
                .placeNearTarget();
        scene.idle(80);

        scene.world().modifyBlock(MACHINE, s -> s.setValue(BiomassGasifierBlock.LIT, false), false);
        scene.world().setKineticSpeed(util.select().position(MACHINE).add(util.select().position(SHAFT)), 0f);
        scene.overlay().showText(70)
                .colored(PonderPalette.RED)
                .text("Without fuel it stops. A Kinetic Battery can keep things running between refills")
                .pointAt(util.vector().centerOf(MACHINE))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.markAsFinished();
    }
}
