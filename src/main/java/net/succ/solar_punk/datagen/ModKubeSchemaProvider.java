package net.succ.solar_punk.datagen;

import net.minecraft.data.PackOutput;
import net.succ.offbyonelib.datagen.KubeRecipeSchemaProvider;
import net.succ.solar_punk.SolarPunk;

/**
 * KubeJS script syntax for Solarpunk's recipe types: event.recipes.solarpunk.<type>(...).
 * Key names match each recipe's JSON fields (see the recipe classes' codecs).
 */
public class ModKubeSchemaProvider extends KubeRecipeSchemaProvider {

    public ModKubeSchemaProvider(PackOutput output) {
        super(output, SolarPunk.MODID);
    }

    @Override
    protected void addSchemas() {
        // solar_heating(Fluid.of('solarpunk:molten_salt', 1000), '#c:storage_blocks/salt')
        schema("solar_heating")
                .key("result", Role.OUTPUT, FLUID_STACK)
                .key("ingredient", Role.INPUT, INGREDIENT)
                .unique("result");

        // crystallizing('4x solarpunk:salt', Fluid.of('solarpunk:molten_salt', 100))
        //     .secondInput(Fluid.water(50)).byproduct(Fluid.of('solarpunk:steam', 25)).processingTime(100)
        schema("crystallizing")
                .key("result", Role.OUTPUT, ITEM_STACK)
                .key("input_a", Role.INPUT, FLUID_STACK)
                .key("input_b", Role.INPUT, OPTIONAL_FLUID_STACK).optional().functionNames("secondInput")
                .key("byproduct", Role.OUTPUT, OPTIONAL_FLUID_STACK).optional().functionNames("byproduct")
                .key("processing_time", Role.INPUT, TICKS).optional(100).alwaysWrite().functionNames("processingTime")
                .unique("result");

        // particle_accelerator('solarpunk:tritium_capsule', ['32x #c:dusts/lithium', 'solarpunk:empty_fuel_capsule'])
        //     .minDiameter(9).minSpeed(128).stress(16384).processingTime(600)
        schema("particle_accelerator")
                .key("result", Role.OUTPUT, ITEM_STACK)
                .key("ingredients", Role.INPUT, list(FLAT_SIZED_INGREDIENT))
                .key("min_diameter", Role.OTHER, POSITIVE_INT).optional(5).alwaysWrite().functionNames("minDiameter")
                .key("min_speed", Role.OTHER, intRange(1, 256)).optional(64).alwaysWrite().functionNames("minSpeed")
                .key("stress", Role.OTHER, NON_NEGATIVE_FLOAT).optional(4096).alwaysWrite().functionNames("stress")
                .key("processing_time", Role.INPUT, TICKS).optional(200).alwaysWrite().functionNames("processingTime")
                .unique("result");

        // fermenting(Fluid.of('solarpunk:biofuel', 1000), 'minecraft:sugar_cane')
        //     .water(1000).processingTime(400).aerobic(true)
        schema("fermenting")
                .key("result", Role.OUTPUT, FLUID_STACK)
                .key("ingredient", Role.INPUT, INGREDIENT)
                .key("water", Role.INPUT, NON_NEGATIVE_INT).optional(1000).functionNames("water")
                .key("processing_time", Role.INPUT, TICKS).optional(400).functionNames("processingTime")
                .key("aerobic", Role.OTHER, BOOLEAN).optional(false).functionNames("aerobic")
                .unique("result");
    }
}
