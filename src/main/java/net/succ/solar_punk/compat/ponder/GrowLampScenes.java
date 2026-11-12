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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.succ.solar_punk.block.custom.BrassSolarPanelBlock;
import net.succ.solar_punk.block.custom.GrowLampBlock;
import net.succ.solar_punk.wire.WireTier;

// Schematic (ModPonderProvider "grow_lamp/usage"): a 3x3 carrot bed (x/z 1-3), a stripped spruce
// post at x=0 and x=4 (z=2, y 1-4) with a Brass Solar Panel on top and a Wire Connector on the
// panel's north side, and three
// Grow Lamps hung post to post at y=4, each with a Wire Connector on top.
public class GrowLampScenes {

    private static final BlockPos MIDDLE_LAMP = new BlockPos(2, 4, 2);

    public static void usage(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("grow_lamp_usage", "Using Grow Lamps");
        scene.configureBasePlate(0, 0, 5);

        Selection bed = util.select().fromTo(1, 1, 1, 3, 2, 3);
        Selection crops = util.select().fromTo(1, 2, 1, 3, 2, 3);
        Selection posts = util.select().fromTo(0, 1, 2, 0, 4, 2).add(util.select().fromTo(4, 1, 2, 4, 4, 2));
        Selection lamps = util.select().fromTo(1, 4, 2, 3, 4, 2);
        Selection panels = util.select().position(0, 5, 2).add(util.select().position(4, 5, 2));
        Selection plugs = util.select().fromTo(1, 5, 2, 3, 5, 2)
                .add(util.select().position(0, 5, 1)).add(util.select().position(4, 5, 1));

        plant(scene, crops, Blocks.CARROTS.defaultBlockState());
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(bed, Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Grow Lamps help crops grow where sunlight doesn't reach: indoors, underground or at night")
                .pointAt(util.vector().topOf(2, 2, 2))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().showSection(posts, Direction.DOWN);
        scene.idle(15);
        for (int x = 1; x <= 3; x++) {
            scene.world().showSection(util.select().position(x, 4, 2), Direction.DOWN);
            scene.idle(6);
        }
        scene.idle(10);
        scene.overlay().showText(80)
                .text("Hang them from a ceiling, or between two walls. Lamps in a row hold each other up")
                .pointAt(util.vector().blockSurface(MIDDLE_LAMP, Direction.DOWN))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.world().showSection(panels, Direction.DOWN);
        scene.idle(5);
        scene.world().modifyBlocks(panels, s -> s.setValue(BrassSolarPanelBlock.LIT, true), false);
        scene.world().showSection(plugs, Direction.DOWN);
        scene.idle(15);
        BlockPos left = new BlockPos(0, 5, 1), right = new BlockPos(4, 5, 1);
        WireScenes.wire(scene, left, 0, new BlockPos(1, 5, 2), 0, WireTier.COPPER);
        scene.idle(4);
        WireScenes.wire(scene, new BlockPos(1, 5, 2), 0, new BlockPos(2, 5, 2), 0, WireTier.COPPER);
        scene.idle(4);
        WireScenes.wire(scene, new BlockPos(2, 5, 2), 0, new BlockPos(3, 5, 2), 0, WireTier.COPPER);
        scene.idle(4);
        WireScenes.wire(scene, new BlockPos(3, 5, 2), 0, right, 0, WireTier.COPPER);
        scene.idle(10);
        scene.overlay().showText(70)
                .text("They run on FE from any side, 8 FE/t each. Here Wire Connectors bring it from the solar panels")
                .pointAt(WireScenes.tip(util, new BlockPos(2, 5, 2)))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().modifyBlocks(lamps, s -> s.setValue(GrowLampBlock.LIT, true), false);
        scene.idle(10);
        scene.overlay().showOutline(PonderPalette.GREEN, "area", crops, 70);
        scene.overlay().showText(70)
                .text("While powered, a lamp speeds up the first plant in each column of the 3x3 below it, up to 6 blocks down")
                .pointAt(util.vector().topOf(2, 2, 2))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        for (int age = 1; age <= 7; age++) {
            int a = age;
            scene.world().modifyBlocks(crops, s -> s.hasProperty(CropBlock.AGE) ? s.setValue(CropBlock.AGE, a) : s, false);
            scene.idle(10);
        }
        scene.overlay().showText(70)
                .text("Lamps reaching the same plant add up, and they stack with a Greenhouse too")
                .pointAt(util.vector().topOf(2, 2, 2))
                .placeNearTarget();
        scene.idle(80);

        scene.effects().indicateRedstone(MIDDLE_LAMP);
        scene.world().modifyBlock(MIDDLE_LAMP, s -> s.setValue(GrowLampBlock.LIT, false), false);
        scene.overlay().showText(60)
                .text("A redstone signal turns a lamp off")
                .pointAt(util.vector().centerOf(MIDDLE_LAMP))
                .colored(PonderPalette.RED)
                .placeNearTarget();
        scene.idle(70);
        scene.world().modifyBlock(MIDDLE_LAMP, s -> s.setValue(GrowLampBlock.LIT, true), false);
        scene.idle(10);

        scene.overlay().showControls(util.vector().centerOf(MIDDLE_LAMP), Pointing.DOWN, 60)
                .withItem(new ItemStack(AllItems.GOGGLES.get()));
        scene.overlay().showText(60)
                .text("Engineer's Goggles show its stored FE and how many plants it's reaching")
                .pointAt(util.vector().centerOf(MIDDLE_LAMP))
                .placeNearTarget();
        scene.idle(70);

        scene.markAsFinished();
    }

    /**
     * Plants crops again at the start of a scene. They're in the schematic too, but can drop out
     * while Ponder loads it (they need light), so Create's harvester scene does the same.
     */
    static void plant(CreateSceneBuilder scene, Selection where, BlockState crop) {
        scene.world().setBlocks(where, crop, false);
    }

    private GrowLampScenes() {}
}
