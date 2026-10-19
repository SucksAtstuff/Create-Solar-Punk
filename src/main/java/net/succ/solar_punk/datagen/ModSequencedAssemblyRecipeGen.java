package net.succ.solar_punk.datagen;

import com.simibubi.create.api.data.recipe.SequencedAssemblyRecipeGen;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.item.ModItems;

import java.util.concurrent.CompletableFuture;

public class ModSequencedAssemblyRecipeGen extends SequencedAssemblyRecipeGen {

    // Photovoltaic Cell: glass pane -> deploy polished rose quartz (the semiconductor)
    // -> deploy copper sheet (the contacts) -> press it together.
    GeneratedRecipe PHOTOVOLTAIC_CELL = create("photovoltaic_cell", b -> b
            .require(Items.GLASS_PANE)
            .transitionTo(ModItems.INCOMPLETE_PHOTOVOLTAIC_CELL.get())
            .addOutput(ModItems.PHOTOVOLTAIC_CELL.get(), 1)
            .loops(1)
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(createItem("polished_rose_quartz")))
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(createItem("copper_sheet")))
            .addStep(PressingRecipe::new, rb -> rb));

    // Solar Mirror (heliostat): iron sheet base -> deploy glass pane (the mirror)
    // -> deploy cogwheel (the sun-tracking drive) -> press it together. Kept short
    // since a tower wants a whole field of these.
    GeneratedRecipe SOLAR_MIRROR = create("solar_mirror", b -> b
            .require(createItem("iron_sheet"))
            .transitionTo(ModItems.INCOMPLETE_SOLAR_MIRROR.get())
            .addOutput(ModBlocks.SOLAR_MIRROR.get(), 1)
            .loops(1)
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(Items.GLASS_PANE))
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(createItem("cogwheel")))
            .addStep(PressingRecipe::new, rb -> rb));

    public ModSequencedAssemblyRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, SolarPunk.MODID);
    }

    private static Item createItem(String name) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("create", name));
    }
}
