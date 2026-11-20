package net.succ.solar_punk.compat.ponder;

import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
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
import net.succ.solar_punk.block.custom.BiomassGasifierBlock;
import net.succ.solar_punk.block.entity.custom.BiomassGasifierBlockEntity;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.item.ModItems;

// Schematic (ModPonderProvider "biomass_gasifier/usage"): a Basin at (2,1,2) with the Mechanical
// Mixer one block above the gap at (2,3,2), turned by a cogwheel at (1,3,2). The gasifier sits on
// that cogwheel at (1,4,2), on an Andesite Casing pillar, and a Hopper with a Chest on it feeds it
// from a pillar to the west.
public class BiomassGasifierScenes {

    private static final BlockPos MACHINE = new BlockPos(1, 4, 2);
    private static final BlockPos COG = new BlockPos(1, 3, 2);
    private static final BlockPos MIXER = new BlockPos(2, 3, 2);
    private static final BlockPos BASIN = new BlockPos(2, 1, 2);
    private static final BlockPos HOPPER = new BlockPos(0, 4, 2);

    public static void usage(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("biomass_gasifier_usage", "Using the Biomass Gasifier");
        scene.configureBasePlate(0, 0, 5);

        Selection gasifierSide = util.select().position(MACHINE).add(util.select().position(COG));
        Selection feeder = util.select().fromTo(0, 1, 2, 0, 5, 2);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(1, 1, 2, 1, 2, 2), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(COG), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(MACHINE), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("The Biomass Gasifier burns Biomass to make Rotational Force")
                .pointAt(util.vector().topOf(MACHINE))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showOutline(PonderPalette.GREEN, "cog", util.select().position(COG), 60);
        scene.overlay().showText(60)
                .text("Rotation comes out of the bottom, here into a cogwheel")
                .pointAt(util.vector().centerOf(COG))
                .placeNearTarget();
        scene.idle(70);

        scene.world().showSection(util.select().position(BASIN), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(MIXER), Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(60)
                .text("The cogwheel turns a Mechanical Mixer, which works a Basin one block below it")
                .pointAt(util.vector().blockSurface(MIXER, Direction.EAST))
                .placeNearTarget();
        scene.idle(70);

        scene.world().showSection(feeder, Direction.DOWN);
        scene.idle(15);
        scene.overlay().showControls(util.vector().topOf(HOPPER.above()), Pointing.DOWN, 30)
                .withItem(new ItemStack(ModItems.BIOMASS.get()));
        scene.idle(35);
        scene.overlay().showText(70)
                .text("Feed it Biomass with a Hopper, Funnel or Chute")
                .pointAt(util.vector().centerOf(HOPPER))
                .placeNearTarget()
                .attachKeyFrame();
        scene.world().modifyBlockEntity(MACHINE, BiomassGasifierBlockEntity.class, be ->
                be.itemHandler.setStackInSlot(0, new ItemStack(ModItems.BIOMASS.get(), 8)));
        scene.idle(80);

        scene.world().modifyBlock(MACHINE, s -> s.setValue(BiomassGasifierBlock.LIT, true), false);
        // Meshing cogwheels turn opposite ways
        scene.world().setKineticSpeed(gasifierSide, 8f);
        scene.world().setKineticSpeed(util.select().position(MIXER), -8f);
        scene.effects().indicateSuccess(MACHINE);
        scene.overlay().showText(70)
                .text("While burning it gives 8 RPM and 2048 SU of stress capacity")
                .pointAt(util.vector().centerOf(MACHINE))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        // The Mixer turning the Gasifier's own Biochar and some Water into Fertilizer
        scene.world().modifyBlockEntity(BASIN, BasinBlockEntity.class, be ->
                be.getInputInventory().insertItem(0, new ItemStack(ModItems.BIOCHAR.get(), 4), false));
        scene.overlay().showControls(util.vector().blockSurface(BASIN, Direction.NORTH), Pointing.UP, 30)
                .withItem(new ItemStack(Items.WATER_BUCKET));
        scene.idle(10);
        scene.world().modifyBlockEntity(MIXER, MechanicalMixerBlockEntity.class,
                MechanicalMixerBlockEntity::startProcessingBasin);
        scene.idle(25);
        scene.overlay().showText(70)
                .text("Burnt fuel leaves Biochar behind, which mixes with Water into Fertilizer")
                .pointAt(util.vector().blockSurface(BASIN, Direction.WEST))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(40);
        scene.world().modifyBlockEntity(BASIN, BasinBlockEntity.class, be ->
                be.getInputInventory().extractItem(0, 64, false));
        scene.overlay().showControls(util.vector().blockSurface(BASIN, Direction.NORTH), Pointing.UP, 40)
                .withItem(new ItemStack(ModFluids.FERTILIZER_BUCKET.get()));
        scene.idle(50);

        scene.overlay().showText(70)
                .text("Pull the Biochar out with a Funnel or Chute, or the Gasifier pauses when it's full")
                .pointAt(util.vector().blockSurface(MACHINE, Direction.NORTH))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showControls(util.vector().topOf(HOPPER.above()), Pointing.DOWN, 30)
                .withItem(new ItemStack(ModItems.BIOMASS_PELLET.get()));
        scene.idle(35);
        scene.overlay().showText(70)
                .text("Biomass Pellets burn twice as long, and leave twice the Biochar")
                .pointAt(util.vector().centerOf(HOPPER))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().modifyBlock(MACHINE, s -> s.setValue(BiomassGasifierBlock.LIT, false), false);
        scene.world().setKineticSpeed(gasifierSide.copy().add(util.select().position(MIXER)), 0f);
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
