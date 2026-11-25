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
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.block.custom.GeyserWellBlock;
import net.succ.solar_punk.block.entity.custom.GeyserWellBlockEntity;
import net.succ.solar_punk.fluid.ModFluids;

// Schematic (ModPonderProvider "geyser_well/usage"): a Geyser Vent at (2,1,2) with a Geyser Well
// facing north on it, so its ports are north and south. A Water line runs in from the north edge
// (z 0-1) and a Steam line out to the south edge (z 3-4), both at y=2.
public class GeyserWellScenes {

    private static final BlockPos VENT = new BlockPos(2, 1, 2);
    private static final BlockPos WELL = new BlockPos(2, 2, 2);

    public static void usage(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("geyser_well_usage", "Using the Geyser Well");
        scene.configureBasePlate(0, 0, 5);

        var waterLine = util.select().fromTo(2, 2, 0, 2, 2, 1);
        var steamLine = util.select().fromTo(2, 2, 3, 2, 2, 4);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().position(VENT), Direction.UP);
        scene.idle(20);

        scene.overlay().showText(80)
                .text("A Geyser Vent can run a Steam Turbine instead of turning a shaft")
                .pointAt(util.vector().topOf(VENT))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.world().showSection(util.select().position(WELL), Direction.DOWN);
        scene.idle(20);
        scene.overlay().showText(70)
                .text("Put a Geyser Well on it in place of a Geyser Cap")
                .pointAt(util.vector().topOf(WELL))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().showSection(waterLine, Direction.SOUTH);
        scene.idle(10);
        scene.overlay().showControls(util.vector().blockSurface(WELL, Direction.NORTH), Pointing.DOWN, 70)
                .withItem(new ItemStack(Items.WATER_BUCKET));
        scene.overlay().showText(70)
                .text("Pipe Water into either of its two ports")
                .pointAt(util.vector().blockSurface(WELL, Direction.NORTH))
                .placeNearTarget()
                .attachKeyFrame();
        scene.world().modifyBlockEntity(WELL, GeyserWellBlockEntity.class, be ->
                be.waterTank.fill(new FluidStack(Fluids.WATER, 4000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(80);

        scene.world().modifyBlock(WELL, s -> s.setValue(GeyserWellBlock.LIT, true), false);
        scene.world().modifyBlockEntity(WELL, GeyserWellBlockEntity.class, be ->
                be.steamTank.fill(new FluidStack(ModFluids.STEAM_SOURCE.get(), 2000), IFluidHandler.FluidAction.EXECUTE));
        scene.effects().indicateSuccess(WELL);
        scene.overlay().showText(80)
                .text("On a normal vent, its heat boils 4 mB of Water into 4 mB of Steam every tick, with no fuel")
                .pointAt(util.vector().centerOf(WELL))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.world().showSection(steamLine, Direction.NORTH);
        scene.idle(10);
        scene.overlay().showText(80)
                .text("Pump the Steam out of either port to a Steam Turbine. The turbine's condensed Water can go straight back in")
                .pointAt(util.vector().blockSurface(WELL, Direction.SOUTH))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showText(70)
                .text("That's about twice the power of a Geyser Cap, if you build the turbine")
                .pointAt(util.vector().topOf(WELL))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showText(70)
                .text("Like a Geyser Cap, it stops the vent erupting")
                .pointAt(util.vector().topOf(VENT))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showText(80)
                .text("The gauges on its sides show how strong the vent below is. Weak vents make half the Steam, strong ones twice as much")
                .pointAt(util.vector().blockSurface(WELL, Direction.EAST))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.world().setBlock(VENT, ModBlocks.NETHER_GEYSER_VENT.get().defaultBlockState(), true);
        scene.idle(20);
        scene.overlay().showText(90)
                .text("A Nether Geyser Vent, from the Basalt Deltas, doubles it again")
                .pointAt(util.vector().blockSurface(WELL, Direction.EAST))
                .colored(PonderPalette.GREEN)
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(100);

        scene.world().modifyBlock(WELL, s -> s.setValue(GeyserWellBlock.LIT, false), false);
        scene.overlay().showText(80)
                .text("Out of Water, or with its Steam tank full, it stops until there's room")
                .pointAt(util.vector().centerOf(WELL))
                .colored(PonderPalette.RED)
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.markAsFinished();
    }

    private GeyserWellScenes() {}
}
