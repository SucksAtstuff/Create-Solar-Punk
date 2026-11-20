package net.succ.solar_punk.compat.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.block.custom.FireboxBoilerBlock;
import net.succ.solar_punk.block.entity.custom.BiofilterBlockEntity;
import net.succ.solar_punk.item.ModItems;

// Schematic (ModPonderProvider "biofilter/usage"): a floor of Dead Grass Blocks and Ruined Dirt with
// Dead Grass on it, a lit Firebox Boiler in the back corner (4,1,4), the Biofilter at (2,1,2) on a
// shaft set into the floor, and a Comparator reading it into a Redstone Lamp to the east.
public class BiofilterScenes {

    private static final BlockPos FILTER = new BlockPos(2, 1, 2);
    private static final BlockPos SHAFT = new BlockPos(2, 0, 2);
    private static final BlockPos BOILER = new BlockPos(4, 1, 4);
    private static final BlockPos COMPARATOR = new BlockPos(3, 1, 2);
    private static final BlockPos LAMP = new BlockPos(4, 1, 2);

    public static void usage(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("biofilter_usage", "Cleaning Pollution with the Biofilter");
        scene.configureBasePlate(0, 0, 5);

        Selection ground = util.select().fromTo(0, 0, 0, 4, 1, 4)
                .substract(util.select().position(SHAFT))
                .substract(util.select().position(FILTER))
                .substract(util.select().position(BOILER))
                .substract(util.select().fromTo(3, 1, 2, 4, 1, 2));
        Selection spinning = util.select().position(FILTER).add(util.select().position(SHAFT));

        scene.world().showSection(ground, Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().position(BOILER), Direction.DOWN);
        scene.idle(10);
        smoke(scene, 200);

        scene.overlay().showText(80)
                .text("With global warming turned on, burning fuel pollutes the chunks around it, and the land slowly dies")
                .pointAt(util.vector().topOf(BOILER))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.world().showSection(util.select().position(SHAFT), Direction.UP);
        scene.idle(5);
        scene.world().showSection(util.select().position(FILTER), Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(70)
                .text("The Biofilter cleans the air within 32 blocks of it")
                .pointAt(util.vector().topOf(FILTER))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().setKineticSpeed(spinning, 16);
        scene.overlay().showOutline(PonderPalette.GREEN, "shaft", util.select().position(SHAFT), 70);
        scene.overlay().showText(80)
                .text("Drive it from below. The faster it spins the faster it cleans: 10 pollution a second at 16 RPM")
                .pointAt(util.vector().centerOf(SHAFT))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showControls(util.vector().topOf(FILTER), Pointing.DOWN, 70)
                .withItem(new ItemStack(ModItems.CARBON_DUST.get()));
        scene.world().modifyBlockEntity(FILTER, BiofilterBlockEntity.class, be ->
                be.inventory.setStackInSlot(0, new ItemStack(ModItems.CARBON_DUST.get(), 3)));
        scene.overlay().showText(70)
                .text("What it takes out of the air turns into Carbon Dust. Four of them compact into Biochar")
                .pointAt(util.vector().topOf(FILTER))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(60)
                .text("Take the dust out by hand or with a Funnel, or it stops collecting when full")
                .pointAt(util.vector().topOf(FILTER))
                .placeNearTarget();
        scene.idle(70);

        scene.world().showSection(util.select().fromTo(3, 1, 2, 4, 1, 2), Direction.DOWN);
        scene.idle(10);
        scene.world().modifyBlock(COMPARATOR, s -> s.setValue(ComparatorBlock.POWERED, true), false);
        scene.world().modifyBlock(LAMP, s -> s.setValue(RedstoneLampBlock.LIT, true), false);
        scene.overlay().showText(70)
                .text("A Comparator reads how polluted its chunk is")
                .pointAt(util.vector().topOf(COMPARATOR))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        // The fire goes out, the filter catches up and the land recovers
        scene.world().modifyBlock(BOILER, s -> s.setValue(FireboxBoilerBlock.LIT, false), false);
        scene.idle(20);
        scene.world().modifyBlocks(ground, BiofilterScenes::heal, false);
        scene.world().modifyBlock(COMPARATOR, s -> s.setValue(ComparatorBlock.POWERED, false), false);
        scene.world().modifyBlock(LAMP, s -> s.setValue(RedstoneLampBlock.LIT, false), false);
        scene.effects().indicateSuccess(FILTER);
        scene.overlay().showText(80)
                .text("Once a chunk is clean enough, its land and biome heal again")
                .pointAt(util.vector().topOf(1, 0, 3))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showText(70)
                .text("Leaves and vine-covered Trellises soak up pollution too, for free")
                .pointAt(util.vector().topOf(1, 0, 3))
                .placeNearTarget();
        scene.idle(80);

        scene.markAsFinished();
    }

    private static BlockState heal(BlockState state) {
        if (state.is(ModBlocks.DEAD_GRASS_BLOCK.get())) return Blocks.GRASS_BLOCK.defaultBlockState();
        if (state.is(ModBlocks.RUINED_DIRT.get())) return Blocks.DIRT.defaultBlockState();
        if (state.is(ModBlocks.DEAD_GRASS.get())) return Blocks.SHORT_GRASS.defaultBlockState();
        return state;
    }

    private static void smoke(CreateSceneBuilder scene, int ticks) {
        Vec3 top = new Vec3(BOILER.getX() + 0.5, BOILER.getY() + 1.1, BOILER.getZ() + 0.5);
        scene.effects().emitParticles(top,
                scene.effects().simpleParticleEmitter(ParticleTypes.CAMPFIRE_COSY_SMOKE, new Vec3(0, 0.04, 0)), 0.1f, ticks);
    }

    private BiofilterScenes() {}
}
