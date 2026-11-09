package net.succ.solar_punk.compat.ponder;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CropBlock;

// Schematic (ModPonderProvider "greenhouse/usage"): a 5x5 smooth stone plate, a 3x3 wheat field
// on farmland in the middle, Greenhouse Glass walls (y 1-3) with the controller in the north
// wall facing in, and a Greenhouse Glass roof at y 4.
public class GreenhouseScenes {

    private static final BlockPos CONTROLLER = new BlockPos(2, 2, 0);
    private static final BlockPos GAP = new BlockPos(4, 2, 2);

    public static void usage(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("greenhouse_usage", "Building a Greenhouse");
        scene.configureBasePlate(0, 0, 5);

        Selection field = util.select().fromTo(1, 1, 1, 3, 2, 3);
        Selection walls = util.select().fromTo(0, 1, 0, 4, 3, 4).substract(field);
        Selection roof = util.select().fromTo(0, 4, 0, 4, 4, 4);
        Selection crops = util.select().fromTo(1, 2, 1, 3, 2, 3);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(field, Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("A Greenhouse Controller turns a closed room into a greenhouse where plants grow faster")
                .pointAt(util.vector().topOf(2, 2, 2))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().showSection(walls, Direction.DOWN);
        scene.idle(20);
        scene.overlay().showText(70)
                .text("Place it in a wall with its front facing inside. Walls can be glass or any solid block")
                .pointAt(util.vector().blockSurface(CONTROLLER, Direction.NORTH))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().showSection(roof, Direction.DOWN);
        scene.idle(20);
        scene.overlay().showText(70)
                .text("Plants only grow faster with Greenhouse Glass or Solar Glass straight above them")
                .pointAt(util.vector().topOf(2, 4, 2))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.effects().indicateSuccess(CONTROLLER);
        scene.overlay().showText(70)
                .text("Once the room is sealed the plants inside start growing faster. No power needed")
                .pointAt(util.vector().centerOf(2, 2, 2))
                .placeNearTarget()
                .attachKeyFrame();
        for (int age = 2; age <= 7; age++) {
            int a = age;
            scene.world().modifyBlocks(crops, s -> s.hasProperty(CropBlock.AGE) ? s.setValue(CropBlock.AGE, a) : s, false);
            scene.idle(12);
        }
        scene.idle(10);

        scene.world().hideSection(util.select().position(GAP), Direction.EAST);
        scene.idle(15);
        scene.overlay().showText(70)
                .text("Any gap, or an open door, breaks the seal and stops the boost")
                .pointAt(util.vector().centerOf(GAP))
                .placeNearTarget()
                .colored(PonderPalette.RED)
                .attachKeyFrame();
        scene.idle(80);
        scene.world().showSection(util.select().position(GAP), Direction.WEST);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Bees inside speed growth up even more, and with global warming on a sealed greenhouse is safe from pollution decay")
                .pointAt(util.vector().topOf(2, 2, 2))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showControls(util.vector().blockSurface(CONTROLLER, Direction.NORTH), Pointing.DOWN, 60)
                .withItem(new ItemStack(AllItems.GOGGLES.get()));
        scene.overlay().showText(60)
                .text("Look at the controller with Engineer's Goggles to see whether it's sealed and how many plants it's boosting")
                .pointAt(util.vector().blockSurface(CONTROLLER, Direction.NORTH))
                .placeNearTarget();
        scene.idle(70);

        scene.markAsFinished();
    }

    private GreenhouseScenes() {}
}
