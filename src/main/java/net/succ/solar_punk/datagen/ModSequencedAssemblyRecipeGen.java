package net.succ.solar_punk.datagen;

import com.simibubi.create.api.data.recipe.SequencedAssemblyRecipeGen;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.common.Tags;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.ModTags;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.item.ModItems;

import java.util.concurrent.CompletableFuture;

import static net.succ.solar_punk.datagen.DatagenUtil.createItem;

public class ModSequencedAssemblyRecipeGen extends SequencedAssemblyRecipeGen {

    // Photovoltaic Cell: glass pane -> deploy polished rose quartz (the semiconductor)
    // -> deploy copper sheet (the contacts) -> press it together.
    GeneratedRecipe PHOTOVOLTAIC_CELL = create("photovoltaic_cell", b -> b
            .require(Tags.Items.GLASS_PANES_COLORLESS)
            .transitionTo(ModItems.INCOMPLETE_PHOTOVOLTAIC_CELL.get())
            .addOutput(ModItems.PHOTOVOLTAIC_CELL.get(), 1)
            .loops(1)
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(createItem("polished_rose_quartz")))
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(ModTags.Items.PLATES_COPPER))
            .addStep(PressingRecipe::new, rb -> rb));

    // Solar Mirror (heliostat): iron sheet base -> deploy glass pane (the mirror)
    // -> deploy cogwheel (the sun-tracking drive) -> press it together. Kept short
    // since a tower wants a whole field of these.
    GeneratedRecipe SOLAR_MIRROR = create("solar_mirror", b -> b
            .require(ModTags.Items.PLATES_IRON)
            .transitionTo(ModItems.INCOMPLETE_SOLAR_MIRROR.get())
            .addOutput(ModBlocks.SOLAR_MIRROR.get(), 1)
            .loops(1)
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(Tags.Items.GLASS_PANES_COLORLESS))
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(createItem("cogwheel")))
            .addStep(PressingRecipe::new, rb -> rb));

    // Electromagnet Coil: lightning rod core -> wind two copper sheets around it -> press
    // the windings tight -> deploy an electron tube (the driver). 3 loops.
    GeneratedRecipe ELECTROMAGNET_COIL = create("electromagnet_coil", b -> b
            .require(Items.LIGHTNING_ROD)
            .transitionTo(ModItems.INCOMPLETE_ELECTROMAGNET_COIL.get())
            .addOutput(ModItems.ELECTROMAGNET_COIL.get(), 1)
            .loops(3)
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(ModTags.Items.PLATES_COPPER))
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(ModTags.Items.PLATES_COPPER))
            .addStep(PressingRecipe::new, rb -> rb)
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(createItem("electron_tube"))));

    // Superconducting Coil: Electromagnet Coil -> deploy beryllium ingot -> fill with
    // Deuterium (the cryogenic coolant) -> press -> deploy lithium ingot. 4 loops, and
    // every step needs the fusion chain, so the accelerator comes after the reactor.
    // Like Create's Precision Mechanism it can fail (a "quench"): 10% of the time it
    // comes out as scrap, usually just the Electromagnet Coil it started from.
    GeneratedRecipe SUPERCONDUCTING_COIL = create("superconducting_coil", b -> b
            .require(ModItems.ELECTROMAGNET_COIL.get())
            .transitionTo(ModItems.INCOMPLETE_SUPERCONDUCTING_COIL.get())
            .addOutput(ModItems.SUPERCONDUCTING_COIL.get(), 90)
            .addOutput(ModItems.ELECTROMAGNET_COIL.get(), 5)
            .addOutput(ModItems.BERYLLIUM_NUGGET.get(), 3)
            .addOutput(ModItems.LITHIUM_NUGGET.get(), 2)
            .loops(4)
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(ModTags.Items.INGOTS_BERYLLIUM))
            .addStep(FillingRecipe::new, rb -> rb.require(ModFluids.DEUTERIUM_SOURCE.get(), 250))
            .addStep(PressingRecipe::new, rb -> rb)
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(ModTags.Items.INGOTS_LITHIUM)));

    public ModSequencedAssemblyRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, SolarPunk.MODID);
    }
}
