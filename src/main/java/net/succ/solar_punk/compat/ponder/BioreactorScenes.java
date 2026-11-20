package net.succ.solar_punk.compat.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.succ.solar_punk.block.custom.BioreactorBlock;
import net.succ.solar_punk.block.entity.custom.BioreactorBlockEntity;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.item.ModItems;

public class BioreactorScenes {

    // A WIDTH x WIDTH Bioreactor, HEIGHT tall, on its Base at y=1; the glass starts at y=2 with
    // the controller at (1,2,1). Matches the schematic in ModPonderProvider, which loads the
    // tank already formed.
    public static final int WIDTH = 2, HEIGHT = 3;
    private static final BlockPos CONTROLLER = new BlockPos(1, 2, 1);
    private static final int CAPACITY = 8000 * WIDTH * WIDTH * HEIGHT;

    private static void setContents(CreateSceneBuilder scene, int water, int culture) {
        scene.world().modifyBlockEntity(CONTROLLER, BioreactorBlockEntity.class, be -> {
            be.waterTank.setFluid(water > 0 ? new FluidStack(Fluids.WATER, water) : FluidStack.EMPTY);
            be.cultureTank.setFluid(culture > 0 ? new FluidStack(ModFluids.ALGAE_CULTURE_SOURCE.get(), culture) : FluidStack.EMPTY);
        });
    }

    public static void usage(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("bioreactor_usage", "Growing Algae in the Bioreactor");
        scene.configureBasePlate(0, 0, 5);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);

        BlockPos base = new BlockPos(1, 1, 1);
        BlockPos baseSide = new BlockPos(2, 1, 1);
        BlockPos top = new BlockPos(1, HEIGHT + 1, 1);
        BlockPos glass = new BlockPos(2, 2, 1);

        scene.world().showSection(util.select().fromTo(1, 1, 1, WIDTH, 1, WIDTH), Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(70)
                .text("A Bioreactor stands on a layer of Bioreactor Base blocks, the same size as the tank")
                .pointAt(util.vector().blockSurface(base, Direction.WEST))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        for (int y = 2; y <= HEIGHT + 1; y++) {
            scene.world().showSection(util.select().fromTo(1, y, 1, WIDTH, y, WIDTH), Direction.DOWN);
            scene.idle(6);
        }
        scene.idle(10);
        scene.overlay().showOutline(PonderPalette.GREEN, "tank",
                util.select().fromTo(1, 2, 1, WIDTH, HEIGHT + 1, WIDTH), 80);
        scene.overlay().showText(80)
                .text("Bioreactor glass merges into one tank, up to 3x3 wide and 8 tall. Its top needs open sky")
                .pointAt(util.vector().topOf(top))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showControls(util.vector().blockSurface(baseSide, Direction.NORTH), Pointing.RIGHT, 60)
                .withItem(new ItemStack(Items.WATER_BUCKET));
        for (int i = 1; i <= 8; i++) {
            setContents(scene, CAPACITY * 3 / 4 * i / 8, 0);
            scene.idle(3);
        }
        scene.overlay().showText(60)
                .text("Fill it with Water through any side")
                .pointAt(util.vector().blockSurface(baseSide, Direction.NORTH))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(60);

        scene.overlay().showControls(util.vector().blockSurface(baseSide, Direction.NORTH), Pointing.RIGHT, 60)
                .withItem(new ItemStack(Items.KELP));
        int water = CAPACITY * 3 / 4;
        setContents(scene, water - 1000, 1000);
        scene.overlay().showText(70)
                .text("Then add Kelp or Seagrass through the Base to start a culture of algae")
                .pointAt(util.vector().blockSurface(baseSide, Direction.NORTH))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().modifyBlock(CONTROLLER, s -> s.setValue(BioreactorBlock.LIT, true), false);
        for (int i = 1; i <= 10; i++) {
            int culture = 1000 + (water * 2 / 3 - 1000) * i / 10;
            setContents(scene, water - culture, culture);
            scene.idle(5);
        }
        scene.overlay().showText(70)
                .text("In sunlight the culture slowly spreads through the Water")
                .pointAt(util.vector().centerOf(glass))
                .placeNearTarget();
        scene.idle(70);

        scene.world().modifyBlockEntity(CONTROLLER, BioreactorBlockEntity.class, be -> {
            be.inventory.setStackInSlot(BioreactorBlockEntity.OUTPUT_SLOT, new ItemStack(ModItems.BIOMASS.get(), 8));
            be.oxygenTank.setFluid(new FluidStack(ModFluids.OXYGEN_SOURCE.get(), 2000));
        });
        scene.effects().indicateSuccess(base);
        scene.overlay().showText(80)
                .text("The algae turn Water and sunlight into Biomass and Oxygen. A wider tank has more surface, so it makes more")
                .pointAt(util.vector().topOf(top))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showControls(util.vector().blockSurface(baseSide, Direction.NORTH), Pointing.RIGHT, 70)
                .withItem(new ItemStack(ModItems.BIOMASS.get()));
        scene.overlay().showText(70)
                .text("Pull Biomass out of the Base with a Funnel or Hopper, and pump Oxygen out of it")
                .pointAt(util.vector().blockSurface(baseSide, Direction.NORTH))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showControls(util.vector().blockSurface(base, Direction.WEST), Pointing.LEFT, 70)
                .withItem(new ItemStack(ModFluids.FERTILIZER_BUCKET.get()));
        scene.world().modifyBlockEntity(CONTROLLER, BioreactorBlockEntity.class, be ->
                be.nutrientTank.setFluid(new FluidStack(ModFluids.FERTILIZER_SOURCE.get(), 4000)));
        scene.overlay().showText(70)
                .text("Fertilizer or Rich Fertilizer piped into the Base speeds it up")
                .pointAt(util.vector().blockSurface(base, Direction.WEST))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(70)
                .text("With global warming on, a running Bioreactor also cleans pollution out of its chunk")
                .pointAt(util.vector().topOf(top))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showControls(util.vector().blockSurface(glass, Direction.NORTH), Pointing.RIGHT, 70)
                .withItem(new ItemStack(ModFluids.ALGAE_CULTURE_BUCKET.get()));
        scene.overlay().showText(70)
                .text("Pipe Algae Culture out of the glass to start another Bioreactor right away")
                .pointAt(util.vector().blockSurface(glass, Direction.NORTH))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.markAsFinished();
    }
}
