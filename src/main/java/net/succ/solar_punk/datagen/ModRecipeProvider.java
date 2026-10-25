package net.succ.solar_punk.datagen;

import java.util.List;
import net.succ.solar_punk.recipe.ParticleAcceleratorRecipe;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.succ.solar_punk.ModTags;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.item.ModItems;
import net.succ.solar_punk.recipe.CrystallizerRecipe;
import net.succ.solar_punk.recipe.SolarHeaterRecipe;

import java.util.concurrent.CompletableFuture;

import static net.succ.solar_punk.datagen.DatagenUtil.anySalt;
import static net.succ.solar_punk.datagen.DatagenUtil.createItem;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        solarHeating(output, "stone_to_lava", Ingredient.of(Tags.Items.STONES), new FluidStack(Fluids.LAVA, 1000));
        solarHeating(output, "cobblestone_to_lava", Ingredient.of(Tags.Items.COBBLESTONES), new FluidStack(Fluids.LAVA, 1000));
        // Salt item gives 100 mB; block gives 1000 mB (vs 900 mB for 9 items, an ~11% bonus for bulk)
        solarHeating(output, "salt_item_to_molten_salt", anySalt(), new FluidStack(ModFluids.MOLTEN_SALT_SOURCE.get(), 100));
        solarHeating(output, "salt_to_molten_salt", Ingredient.of(ModTags.Items.STORAGE_BLOCKS_SALT), new FluidStack(ModFluids.MOLTEN_SALT_SOURCE.get(), 1000));

        // Particle Accelerator: Tritium bred from Lithium, with Beryllium as the neutron
        // source, caught in an Empty Fuel Capsule. PLACEHOLDER numbers until the recipe
        // balance is decided: 17+ wide ring, 8192 SU at 128 RPM, 20 seconds at minimum speed.
        particleAccelerator(output, "tritium_capsule",
                List.of(new SizedIngredient(Ingredient.of(ModTags.Items.INGOTS_LITHIUM), 32),
                        new SizedIngredient(Ingredient.of(ModTags.Items.INGOTS_BERYLLIUM), 4),
                        new SizedIngredient(Ingredient.of(ModItems.EMPTY_FUEL_CAPSULE.get()), 1)),
                new ItemStack(ModItems.TRITIUM_CAPSULE.get()), 17, 8192, 128, 400);

        // Default Crystallizer recipe (datapacks can add more). Much faster than the Solar
        // Heater's evaporation: it's the bulk path once you have a Molten Salt supply.
        crystallizing(output, "molten_salt_to_salt",
                new FluidStack(ModFluids.MOLTEN_SALT_SOURCE.get(), 100),
                new FluidStack(Fluids.WATER, 50),
                new ItemStack(ModItems.SALT.get(), 4),
                new FluidStack(ModFluids.STEAM_SOURCE.get(), 25),
                100);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.SALT_BLOCK.get())
                .pattern("SSS")
                .pattern("SSS")
                .pattern("SSS")
                .define('S', anySalt())
                .unlockedBy("has_salt", has(ModTags.Items.DUSTS_SALT))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.SALT.get(), 9)
                .requires(ModTags.Items.STORAGE_BLOCKS_SALT)
                .unlockedBy("has_salt_block", has(ModTags.Items.STORAGE_BLOCKS_SALT))
                .save(output, id("salt_from_salt_block"));

        // Lithium ore chain: Raw Lithium and Lithium Dust both smelt to the same ingot.
        // Not using vanilla's oreSmelting/oreBlasting helpers, which save under the
        // minecraft namespace instead of solarpunk.
        ingotSmelting(output, ModTags.Items.RAW_MATERIALS_LITHIUM, "raw_lithium", ModItems.LITHIUM_INGOT.get(), "lithium");
        ingotSmelting(output, ModTags.Items.DUSTS_LITHIUM, "lithium_dust", ModItems.LITHIUM_INGOT.get(), "lithium");
        metalStorage(output, "lithium", ModItems.LITHIUM_NUGGET.get(), ModItems.LITHIUM_INGOT.get(), ModBlocks.LITHIUM_BLOCK.get(),
                ModTags.Items.NUGGETS_LITHIUM, ModTags.Items.INGOTS_LITHIUM, ModTags.Items.STORAGE_BLOCKS_LITHIUM);

        ingotSmelting(output, ModTags.Items.DUSTS_BERYLLIUM, "beryllium_dust", ModItems.BERYLLIUM_INGOT.get(), "beryllium");
        metalStorage(output, "beryllium", ModItems.BERYLLIUM_NUGGET.get(), ModItems.BERYLLIUM_INGOT.get(), ModBlocks.BERYLLIUM_BLOCK.get(),
                ModTags.Items.NUGGETS_BERYLLIUM, ModTags.Items.INGOTS_BERYLLIUM, ModTags.Items.STORAGE_BLOCKS_BERYLLIUM);

        // Turbine Rotor - kept cheap since a turbine needs one per blade layer plus the
        // cap. Shafts run through the column axis, and the Turbine Casing matches the
        // rotor's casing texture.
        //   . S .
        //   C A C
        //   . S .
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.TURBINE_ROTOR.get())
                .pattern(" S ")
                .pattern("CAC")
                .pattern(" S ")
                .define('S', createItem("shaft"))
                .define('C', ModBlocks.TURBINE_CASING.get())
                .define('A', createItem("andesite_alloy"))
                .unlockedBy("has_turbine_casing", has(ModBlocks.TURBINE_CASING.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.SOLAR_HEATER.get())
                .pattern("GGG")
                .pattern("C C")
                .pattern("AAA")
                .define('G', Tags.Items.GLASS_PANES_COLORLESS)
                .define('C', ModTags.Items.PLATES_COPPER)
                .define('A', createItem("andesite_alloy"))
                .unlockedBy("has_andesite_alloy", has(createItem("andesite_alloy")))
                .save(output);

        // Empty Fuel Capsule: a glass window between two iron end caps. Cheap, since capsules are
        // a consumable container that the Fusion Reactor will hand back.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.EMPTY_FUEL_CAPSULE.get(), 4)
                .pattern("I")
                .pattern("G")
                .pattern("I")
                .define('I', ModTags.Items.PLATES_IRON)
                .define('G', Tags.Items.GLASS_PANES_COLORLESS)
                .unlockedBy("has_iron_sheet", has(ModTags.Items.PLATES_IRON))
                .save(output);

        // Brass-tier, since the Crystallizer is the Solar Heater's faster, pricier counterpart.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.CRYSTALLIZER.get())
                .pattern("BGB")
                .pattern("CIC")
                .pattern("BGB")
                .define('B', ModTags.Items.PLATES_BRASS)
                .define('G', Tags.Items.GLASS_PANES_COLORLESS)
                .define('C', createItem("brass_casing"))
                .define('I', createItem("fluid_tank"))
                .unlockedBy("has_brass_casing", has(createItem("brass_casing")))
                .save(output);

        // Vanilla-tier on purpose (iron, furnace, bucket) so it's the first machine you can make.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FIREBOX_BOILER.get())
                .pattern("III")
                .pattern("IFI")
                .pattern("IBI")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('F', Tags.Items.PLAYER_WORKSTATIONS_FURNACES)
                .define('B', Tags.Items.BUCKETS_EMPTY)
                .unlockedBy("has_furnace", has(Items.FURNACE))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ANDESITE_SOLAR_PANEL.get())
                .pattern("GGG")
                .pattern("IAI")
                .pattern("ASA")
                .define('G', Tags.Items.GLASS_PANES_COLORLESS)
                .define('I', ModTags.Items.PLATES_IRON)
                .define('A', createItem("andesite_alloy"))
                .define('S', createItem("shaft"))
                .unlockedBy("has_andesite_alloy", has(createItem("andesite_alloy")))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.KINETIC_BATTERY.get())
                .pattern("AAA")
                .pattern("AFA")
                .pattern("AAA")
                .define('A', createItem("andesite_alloy"))
                .define('F', createItem("flywheel"))
                .unlockedBy("has_flywheel", has(createItem("flywheel")))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.HEAT_BATTERY.get())
                .pattern("DDD")
                .pattern("BSB")
                .pattern("DDD")
                .define('D', Items.DEEPSLATE_BRICKS)
                .define('B', ModTags.Items.PLATES_BRASS)
                .define('S', ModTags.Items.STORAGE_BLOCKS_SALT)
                .unlockedBy("has_brass_sheet", has(ModTags.Items.PLATES_BRASS))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.BIOMASS_GASIFIER.get())
                .pattern("ACA")
                .pattern("CSC")
                .pattern("ACA")
                .define('A', createItem("andesite_alloy"))
                .define('C', createItem("andesite_casing"))
                .define('S', createItem("shaft"))
                .unlockedBy("has_andesite_casing", has(createItem("andesite_casing")))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FERMENTATION_VAT.get())
                .pattern("ACA")
                .pattern("C C")
                .pattern("ACA")
                .define('A', createItem("andesite_alloy"))
                .define('C', createItem("industrial_iron_block"))
                .unlockedBy("has_andesite_alloy", has(createItem("andesite_alloy")))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.GEYSER_CAP.get())
                .pattern("RAR")
                .pattern("SCS")
                .pattern("AHA")
                .define('R', createItem("polished_rose_quartz"))
                .define('A', createItem("andesite_alloy"))
                .define('C', createItem("andesite_casing"))
                .define('S', createItem("shaft"))
                .define('H', createItem("chute"))
                .unlockedBy("has_andesite_casing", has(createItem("andesite_casing")))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.KINETIC_SPRINKLER.get())
                .pattern("C C")
                .pattern("CAC")
                .pattern(" S ")
                .define('C', Tags.Items.INGOTS_COPPER)
                .define('A', createItem("andesite_alloy"))
                .define('S', createItem("shaft"))
                .unlockedBy("has_andesite_alloy", has(createItem("andesite_alloy")))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.BIOFILTER.get())
                .pattern("GMG")
                .pattern("MBM")
                .pattern("GSG")
                .define('G', Blocks.GRAVEL)
                .define('M', Blocks.MOSS_BLOCK)
                .define('B', createItem("brass_casing"))
                .define('S', createItem("shaft"))
                .unlockedBy("has_brass_casing", has(createItem("brass_casing")))
                .save(output);
    }

    private static void solarHeating(RecipeOutput output, String name, Ingredient ingredient, FluidStack result) {
        output.accept(
                id("solar_heating/" + name),
                new SolarHeaterRecipe(ingredient, result),
                null
        );
    }

    // Smelting and blasting recipes turning `input` into a metal ingot.
    private static void ingotSmelting(RecipeOutput output, TagKey<Item> input, String inputName, Item ingot, String metal) {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(input), RecipeCategory.MISC, ingot, 0.7f, 200)
                .unlockedBy("has_" + inputName, has(input))
                .save(output, id(metal + "_ingot_from_smelting_" + inputName));

        SimpleCookingRecipeBuilder.blasting(Ingredient.of(input), RecipeCategory.MISC, ingot, 0.7f, 100)
                .unlockedBy("has_" + inputName, has(input))
                .save(output, id(metal + "_ingot_from_blasting_" + inputName));
    }

    // 9 nuggets <-> ingot <-> 9 ingots as a storage block. Inputs use the c: tags so any
    // mod's version packs and unpacks into ours.
    private static void metalStorage(RecipeOutput output, String metal, Item nugget, Item ingot, ItemLike block,
                                     TagKey<Item> nuggetTag, TagKey<Item> ingotTag, TagKey<Item> blockTag) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ingot)
                .pattern("NNN")
                .pattern("NNN")
                .pattern("NNN")
                .define('N', nuggetTag)
                .unlockedBy("has_" + metal + "_nugget", has(nuggetTag))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, nugget, 9)
                .requires(ingotTag)
                .unlockedBy("has_" + metal + "_ingot", has(ingotTag))
                .save(output, id(metal + "_nugget_from_ingot"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, block)
                .pattern("III")
                .pattern("III")
                .pattern("III")
                .define('I', ingotTag)
                .unlockedBy("has_" + metal + "_ingot", has(ingotTag))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ingot, 9)
                .requires(blockTag)
                .unlockedBy("has_" + metal + "_block", has(blockTag))
                .save(output, id(metal + "_ingot_from_block"));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, path);
    }

    private static void particleAccelerator(RecipeOutput output, String name, List<SizedIngredient> ingredients,
                                            ItemStack result, int minDiameter, float stress, int minSpeed, int processingTime) {
        output.accept(
                id("particle_accelerator/" + name),
                new ParticleAcceleratorRecipe(ingredients, result, minDiameter, stress, minSpeed, processingTime),
                null
        );
    }

    private static void crystallizing(RecipeOutput output, String name, FluidStack inputA, FluidStack inputB,
                                       ItemStack result, FluidStack byproduct, int processingTime) {
        output.accept(
                id("crystallizing/" + name),
                new CrystallizerRecipe(inputA, inputB, result, byproduct, processingTime),
                null
        );
    }
}
