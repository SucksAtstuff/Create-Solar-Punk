package net.succ.solar_punk.compat.ponder;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.block.custom.PipeShape;
import net.succ.solar_punk.block.entity.custom.ParticleAcceleratorInputBlockEntity;
import net.succ.solar_punk.item.ModItems;
import net.succ.solar_punk.item.ParticleAcceleratorPipeItem;

import java.util.List;

public class ParticleAcceleratorScenes {

    /** One ring block in the scenes' octagon: where it is, which block, and its facing. */
    public record Piece(int x, int z, String block, String facing) {
        public BlockPos pos() { return new BlockPos(x, 1, z); }
    }

    // A 20-block octagon (diameter 8) on a 10x10 plate, in loop order from the Input.
    // The Input faces east, so its item cap points into the ring; its shaft is in the
    // floor below it. Shared with ModPonderProvider, which builds the schematics from it.
    public static final List<Piece> RING = List.of(
            new Piece(5, 8, "particle_accelerator_input", "east"),
            new Piece(6, 8, "particle_accelerator_pipe_bend_mirrored", "west"),
            new Piece(7, 7, "particle_accelerator_pipe_diagonal", "north"),
            new Piece(8, 6, "particle_accelerator_pipe_bend", "north"),
            new Piece(8, 5, "particle_accelerator_pipe", "north"),
            new Piece(8, 4, "particle_accelerator_pipe", "north"),
            new Piece(8, 3, "particle_accelerator_pipe_bend_mirrored", "south"),
            new Piece(7, 2, "particle_accelerator_pipe_diagonal", "east"),
            new Piece(6, 1, "particle_accelerator_pipe_bend", "west"),
            new Piece(5, 1, "particle_accelerator_pipe", "east"),
            new Piece(4, 1, "particle_accelerator_pipe", "east"),
            new Piece(3, 1, "particle_accelerator_pipe_bend_mirrored", "east"),
            new Piece(2, 2, "particle_accelerator_pipe_diagonal", "north"),
            new Piece(1, 3, "particle_accelerator_pipe_bend", "south"),
            new Piece(1, 4, "particle_accelerator_pipe", "north"),
            new Piece(1, 5, "particle_accelerator_pipe", "north"),
            new Piece(1, 6, "particle_accelerator_pipe_bend_mirrored", "north"),
            new Piece(2, 7, "particle_accelerator_pipe_diagonal", "east"),
            new Piece(3, 8, "particle_accelerator_pipe_bend", "east"),
            new Piece(4, 8, "particle_accelerator_pipe", "east"));

    public static final BlockPos INPUT = RING.get(0).pos();
    public static final BlockPos SHAFT = INPUT.below();

    // The corner from the east straight run round to the north one: Bend, Diagonal, Bend
    private static final BlockPos CORNER_BEND_A = RING.get(6).pos();
    private static final BlockPos CORNER_DIAGONAL = RING.get(7).pos();
    private static final BlockPos CORNER_BEND_B = RING.get(8).pos();
    // A straight pipe on the far side, taken out to show an open ring
    private static final BlockPos GAP = RING.get(9).pos();

    private static final int RPM = 128;
    private static final int CRAFT_TICKS = 160;

    public static void ring(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("particle_accelerator_ring", "Building a Particle Accelerator");
        scene.configureBasePlate(0, 0, 10);
        scene.scaleSceneView(0.65f);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().position(INPUT), Direction.DOWN);
        scene.idle(15);

        scene.overlay().showText(80)
                .text("A Particle Accelerator is a closed ring of Pipes with one Accelerator Input in it")
                .pointAt(util.vector().topOf(INPUT))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(20);

        // build the ring up in loop order
        for (int i = 1; i < RING.size(); i++) {
            scene.world().showSection(util.select().position(RING.get(i).pos()), Direction.DOWN);
            scene.idle(3);
        }
        scene.idle(30);

        ItemStack bendItem = ParticleAcceleratorPipeItem.stackOf(
                ModBlocks.PARTICLE_ACCELERATOR_PIPE.get().asItem(), PipeShape.BEND);
        scene.overlay().showControls(util.vector().topOf(CORNER_BEND_B), Pointing.DOWN, 80).withItem(bendItem);
        scene.overlay().showText(80)
                .text("Every shape comes from the same Pipe item. Hold the Toolbelt key (Alt) while holding Pipes to pick Straight, Bend or Diagonal")
                .pointAt(util.vector().topOf(CORNER_BEND_B))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showControls(util.vector().topOf(CORNER_DIAGONAL), Pointing.DOWN, 70)
                .rightClick().withItem(AllItems.WRENCH.asStack());
        scene.overlay().showText(70)
                .text("Placed next to the ring, a Pipe turns itself to connect. A Wrench turns it by hand")
                .pointAt(util.vector().topOf(CORNER_DIAGONAL))
                .placeNearTarget();
        scene.idle(80);

        Selection corner = util.select().position(CORNER_BEND_A)
                .add(util.select().position(CORNER_DIAGONAL))
                .add(util.select().position(CORNER_BEND_B));
        scene.overlay().showOutline(PonderPalette.GREEN, "corner", corner, 80);
        scene.overlay().showText(80)
                .text("Every turn is 45 degrees: a Bend, Diagonal Pipes, then another Bend make a corner")
                .pointAt(util.vector().topOf(CORNER_DIAGONAL))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().chaseBoundingBoxOutline(PonderPalette.WHITE, "ring", new AABB(1, 1, 1, 9, 2, 9), 80);
        scene.overlay().showText(80)
                .text("The ring's diameter is the shorter side of its footprint. Bigger recipes need a wider ring")
                .pointAt(util.vector().topOf(new BlockPos(5, 1, 5)))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.world().hideSection(util.select().position(GAP), Direction.UP);
        scene.idle(15);
        scene.overlay().showText(70)
                .colored(PonderPalette.RED)
                .text("If the ring isn't a closed loop, the Accelerator won't run")
                .pointAt(util.vector().centerOf(GAP))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);
        scene.world().showSection(util.select().position(GAP), Direction.DOWN);
        scene.idle(20);

        scene.markAsFinished();
    }

    public static void crafting(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("particle_accelerator_crafting", "Running the Particle Accelerator");
        scene.configureBasePlate(0, 0, 10);
        scene.scaleSceneView(0.65f);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().layer(1), Direction.DOWN);
        scene.idle(20);

        scene.world().setKineticSpeed(util.select().position(SHAFT).add(util.select().position(INPUT)), RPM);
        scene.overlay().showText(70)
                .text("Power the Accelerator Input with a shaft from below")
                .pointAt(util.vector().blockSurface(INPUT, Direction.DOWN))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        // the item cap sticks out of the Input's left side, into the ring
        var cap = util.vector().blockSurface(INPUT, Direction.NORTH);
        scene.overlay().showControls(cap, Pointing.DOWN, 80).withItem(new ItemStack(ModItems.LITHIUM_INGOT.get()));
        scene.overlay().showText(80)
                .text("Items go in through the cap, the opposite side or the top, with Funnels, Belts or Hoppers. Like a Basin, any order works")
                .pointAt(cap)
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showText(70)
                .text("The filter slot on the cap picks which recipe it makes")
                .pointAt(cap)
                .placeNearTarget();
        scene.idle(80);

        scene.world().modifyBlockEntity(INPUT, ParticleAcceleratorInputBlockEntity.class,
                be -> be.ponderStartCraft(new ItemStack(ModItems.TRITIUM_CAPSULE.get()), CRAFT_TICKS, RPM));
        scene.overlay().showText(80)
                .text("Once the contents match a recipe the ring is wide and fast enough for, two beams race around it in opposite directions")
                .pointAt(util.vector().topOf(new BlockPos(8, 1, 5)))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(CRAFT_TICKS / 2);
        scene.overlay().showText(70)
                .text("They speed up as the recipe goes on...")
                .pointAt(util.vector().topOf(new BlockPos(1, 1, 5)))
                .placeNearTarget();
        scene.idle(CRAFT_TICKS / 2);

        scene.world().modifyBlockEntity(INPUT, ParticleAcceleratorInputBlockEntity.class,
                ParticleAcceleratorInputBlockEntity::ponderFinishCraft);
        scene.overlay().showText(80)
                .text("...and collide in the Input exactly as it finishes. Pull the result out of any of those faces")
                .pointAt(util.vector().topOf(INPUT))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showText(90)
                .text("Bigger rings draw more stress. Goggles on the Input say what's missing: a closed ring, a wider ring, more speed or more stress capacity")
                .pointAt(util.vector().topOf(INPUT))
                .placeNearTarget();
        scene.idle(100);

        scene.markAsFinished();
    }
}