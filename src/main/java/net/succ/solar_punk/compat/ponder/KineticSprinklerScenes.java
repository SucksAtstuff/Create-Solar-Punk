package net.succ.solar_punk.compat.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.phys.Vec3;
import net.succ.solar_punk.fluid.ModFluids;

// Schematic (ModPonderProvider "kinetic_sprinkler/usage"): a 7x7 wheat field on dry farmland,
// with the sprinkler at (3,3,3) on a pipe rising out of the floor in the middle.
public class KineticSprinklerScenes {

    private static final BlockPos SPRINKLER = new BlockPos(3, 3, 3);

    public static void usage(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("kinetic_sprinkler_usage", "Using the Kinetic Sprinkler");
        scene.configureBasePlate(0, 0, 7);
        scene.scaleSceneView(0.9f);

        Selection pipe = util.select().fromTo(3, 0, 3, 3, 2, 3);
        Selection field = util.select().fromTo(0, 1, 0, 6, 2, 6).substract(pipe);
        // Range 2 reaches the 5x5 around the sprinkler; Rich Fertilizer adds one more ring
        Selection inner = util.select().fromTo(1, 1, 1, 5, 2, 5).substract(pipe);
        Selection outer = field.copy().substract(inner);
        Selection innerCrops = util.select().fromTo(1, 2, 1, 5, 2, 5);
        Selection allCrops = util.select().fromTo(0, 2, 0, 6, 2, 6);

        GrowLampScenes.plant(scene, allCrops.copy().substract(pipe), Blocks.WHEAT.defaultBlockState());
        scene.world().showSection(util.select().layer(0).substract(pipe), Direction.UP);
        scene.idle(10);
        scene.world().showSection(field, Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("The Kinetic Sprinkler waters and feeds the crops below it")
                .pointAt(util.vector().topOf(3, 2, 2))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().showSection(pipe, Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().position(SPRINKLER), Direction.DOWN);
        scene.idle(15);
        scene.overlay().showControls(util.vector().topOf(SPRINKLER), Pointing.DOWN, 70)
                .withItem(new ItemStack(Items.WATER_BUCKET));
        scene.overlay().showText(70)
                .text("Place it above the field and pipe Water into any side")
                .pointAt(util.vector().centerOf(SPRINKLER))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        spray(scene, 2, ParticleTypes.FALLING_WATER);
        scene.world().modifyBlocks(inner, s -> s.hasProperty(FarmBlock.MOISTURE) ? s.setValue(FarmBlock.MOISTURE, 7) : s, false);
        scene.overlay().showOutline(PonderPalette.BLUE, "range", util.select().fromTo(1, 1, 1, 5, 1, 5), 70);
        scene.overlay().showText(70)
                .text("Every second it uses 100 mB and wets the farmland in a 5x5 area, up to 5 blocks below")
                .pointAt(util.vector().topOf(1, 1, 3))
                .placeNearTarget()
                .attachKeyFrame();
        grow(scene, innerCrops, 1, 2, 30);
        scene.idle(20);

        scene.overlay().showText(60)
                .text("Each crop it reaches also gets an extra growth tick")
                .pointAt(util.vector().topOf(2, 2, 2))
                .placeNearTarget();
        spray(scene, 2, ParticleTypes.FALLING_WATER);
        scene.idle(70);

        scene.overlay().showControls(util.vector().topOf(SPRINKLER), Pointing.DOWN, 70)
                .withItem(new ItemStack(ModFluids.FERTILIZER_BUCKET.get()));
        scene.overlay().showText(70)
                .text("Pipe in Fertilizer instead, and it works like Bone Meal on every crop in range")
                .pointAt(util.vector().centerOf(SPRINKLER))
                .placeNearTarget()
                .attachKeyFrame();
        spray(scene, 2, ParticleTypes.HAPPY_VILLAGER);
        grow(scene, innerCrops, 3, 7, 10);
        scene.idle(40);

        scene.overlay().showText(60)
                .text("Mix Fertilizer from Biochar and Water")
                .pointAt(util.vector().centerOf(SPRINKLER))
                .placeNearTarget();
        scene.idle(70);

        scene.overlay().showControls(util.vector().topOf(SPRINKLER), Pointing.DOWN, 80)
                .withItem(new ItemStack(ModFluids.RICH_FERTILIZER_BUCKET.get()));
        scene.overlay().showOutline(PonderPalette.GREEN, "range", util.select().fromTo(0, 1, 0, 6, 1, 6), 80);
        scene.overlay().showText(80)
                .text("Rich Fertilizer from a Fermentation Vat reaches one block further and lasts twice as long")
                .pointAt(util.vector().topOf(0, 2, 3))
                .placeNearTarget()
                .attachKeyFrame();
        spray(scene, 3, ParticleTypes.HAPPY_VILLAGER);
        scene.world().modifyBlocks(outer, s -> s.hasProperty(FarmBlock.MOISTURE) ? s.setValue(FarmBlock.MOISTURE, 7) : s, false);
        grow(scene, allCrops, 3, 7, 10);
        scene.idle(40);

        scene.markAsFinished();
    }

    // A second's worth of spray over every column within range of the sprinkler, like the real one
    private static void spray(CreateSceneBuilder scene, int range, ParticleOptions particle) {
        for (int dx = -range; dx <= range; dx++)
            for (int dz = -range; dz <= range; dz++) {
                Vec3 at = new Vec3(SPRINKLER.getX() + dx + 0.5, SPRINKLER.getY() + 0.1, SPRINKLER.getZ() + dz + 0.5);
                scene.effects().emitParticles(at,
                        scene.effects().particleEmitterWithinBlockSpace(particle, Vec3.ZERO), 0.15f, 20);
            }
    }

    // Raises every crop in the selection one age at a time, from `from` to `to`
    private static void grow(CreateSceneBuilder scene, Selection crops, int from, int to, int ticksPerStage) {
        for (int age = from; age <= to; age++) {
            int a = age;
            scene.world().modifyBlocks(crops, s -> s.hasProperty(CropBlock.AGE) && s.getValue(CropBlock.AGE) < a
                    ? s.setValue(CropBlock.AGE, a) : s, false);
            scene.idle(ticksPerStage);
        }
    }

    private KineticSprinklerScenes() {}
}
