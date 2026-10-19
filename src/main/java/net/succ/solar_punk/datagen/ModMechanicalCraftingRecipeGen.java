package net.succ.solar_punk.datagen;

import com.simibubi.create.api.data.recipe.MechanicalCraftingRecipeGen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.item.ModItems;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;

public class ModMechanicalCraftingRecipeGen extends MechanicalCraftingRecipeGen {

    // Andesite blade: 4x2 andesite casing in an L-shape, outputs 2
    //   A A A A
    //   A A
    GeneratedRecipe ANDESITE_TURBINE_BLADE = create(() -> ModBlocks.ANDESITE_TURBINE_BLADE.get())
            .returns(2)
            .recipe(b -> b
                    .patternLine("AAAA")
                    .patternLine("AA  ")
                    .key('A', createItem("andesite_casing")));

    // Brass blade: same shape with brass casing, outputs 2
    //   B B B B
    //   B B
    GeneratedRecipe BRASS_TURBINE_BLADE = create(() -> ModBlocks.BRASS_TURBINE_BLADE.get())
            .returns(2)
            .recipe(b -> b
                    .patternLine("BBBB")
                    .patternLine("BB  ")
                    .key('B', createItem("brass_casing")));

    // Fusion Reactor blanket modules - a ring of ingots around a core that hints at
    // the module's job. See plan_for_fusion.md's Tunable knob section.
    // Breeder breeds fuel, so its core is a Fluid Tank.
    //   L L L
    //   L T L
    //   L L L
    GeneratedRecipe LITHIUM_BREEDER_MODULE = create(() -> ModBlocks.LITHIUM_BREEDER_MODULE.get())
            .returns(2)
            .recipe(b -> b
                    .patternLine("LLL")
                    .patternLine("LTL")
                    .patternLine("LLL")
                    .key('L', ModItems.LITHIUM_INGOT.get())
                    .key('T', createItem("fluid_tank")));

    // Reflector bounces neutrons back, so its core is a Copper Sheet plate.
    //   B B B
    //   B S B
    //   B B B
    GeneratedRecipe BERYLLIUM_REFLECTOR_MODULE = create(() -> ModBlocks.BERYLLIUM_REFLECTOR_MODULE.get())
            .returns(2)
            .recipe(b -> b
                    .patternLine("BBB")
                    .patternLine("BSB")
                    .patternLine("BBB")
                    .key('B', ModItems.BERYLLIUM_INGOT.get())
                    .key('S', createItem("copper_sheet")));

    // Brass Solar Panel - brass-tier, so it needs Photovoltaic Cells (sequenced
    // assembly) and a mechanical crafter instead of a crafting table.
    //   G G G
    //   P H P
    //   E B E
    GeneratedRecipe BRASS_SOLAR_PANEL = create(() -> ModBlocks.BRASS_SOLAR_PANEL.get())
            .recipe(b -> b
                    .patternLine("GGG")
                    .patternLine("PHP")
                    .patternLine("EBE")
                    .key('G', Items.GLASS_PANE)
                    .key('P', ModItems.PHOTOVOLTAIC_CELL.get())
                    .key('H', createItem("brass_casing"))
                    .key('E', createItem("electron_tube"))
                    .key('B', createItem("brass_sheet")));

    // Biofuel Engine - same layout as its old crafting recipe, with brass sheets
    // instead of ingots and moved to mechanical crafting.
    //   B C B
    //   S F S
    //   B C B
    GeneratedRecipe BIOFUEL_ENGINE = create(() -> ModBlocks.BIOFUEL_ENGINE.get())
            .recipe(b -> b
                    .patternLine("BCB")
                    .patternLine("SFS")
                    .patternLine("BCB")
                    .key('B', createItem("brass_sheet"))
                    .key('C', createItem("brass_casing"))
                    .key('S', createItem("shaft"))
                    .key('F', createItem("flywheel")));

    // Solar Power Tower - the tower block itself is the "intelligent" part that aims
    // the mirror field, so it takes a Precision Mechanism. Outputs 2 because a full
    // 3x3x20 tower is 180 blocks.
    //   Q I Q
    //   I P I
    //   Q I Q
    GeneratedRecipe SOLAR_POWER_TOWER = create(() -> ModBlocks.SOLAR_POWER_TOWER.get())
            .returns(2)
            .recipe(b -> b
                    .patternLine("QIQ")
                    .patternLine("IPI")
                    .patternLine("QIQ")
                    .key('Q', Items.QUARTZ_BLOCK)
                    .key('I', createItem("iron_sheet"))
                    .key('P', createItem("precision_mechanism")));

    // Fusion Reactor Core - both fuel-chain ingots ring a Netherite Ingot at the
    // literal center (the block is the reactor's core, so the recipe's core is too),
    // with a Precision Mechanism at each corner for the control systems.
    //   P L B L P
    //   L B N B L
    //   P L B L P
    GeneratedRecipe FUSION_REACTOR_CORE = create(() -> ModBlocks.FUSION_REACTOR_CORE.get())
            .recipe(b -> b
                    .patternLine("PLBLP")
                    .patternLine("LBNBL")
                    .patternLine("PLBLP")
                    .key('P', createItem("precision_mechanism"))
                    .key('L', ModItems.LITHIUM_INGOT.get())
                    .key('B', ModItems.BERYLLIUM_INGOT.get())
                    .key('N', Items.NETHERITE_INGOT));

    public ModMechanicalCraftingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, SolarPunk.MODID);
    }

    private static Item createItem(String name) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("create", name));
    }
}