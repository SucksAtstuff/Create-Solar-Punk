package net.succ.solar_punk.datagen;

import com.google.common.collect.Sets;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.advancement.ModTriggers;
import studio.offbyone.lib.advancement.AddonAdvancement;
import studio.offbyone.lib.advancement.AddonAdvancement.TaskType;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.item.ModItems;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class AllSolarpunkAdvancements implements DataProvider {

    public static final List<AddonAdvancement> ENTRIES = new ArrayList<>();

    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "textures/block/salt_block.png");

    private static AddonAdvancement create(String id, UnaryOperator<AddonAdvancement.Builder> configurator) {
        AddonAdvancement advancement = new AddonAdvancement(SolarPunk.MODID, id, BACKGROUND, configurator);
        ENTRIES.add(advancement);
        return advancement;
    }

    // -------------------------------------------------------------------------
    // Solar chain
    // -------------------------------------------------------------------------

    public static final AddonAdvancement ROOT = create("root", b -> b
            .icon(ModItems.SALT.get())
            .title("Powered by Nature")
            .description("Collect Salt - the foundation of solar thermal energy.")
            .whenItemCollected(ModItems.SALT.get())
    );

    public static final AddonAdvancement SOLAR_HEATER = create("solar_heater", b -> b
            .icon(ModBlocks.SOLAR_HEATER.get())
            .title("Heat Collector")
            .description("Craft a Solar Heater to begin concentrating sunlight into Molten Salt.")
            .whenIconCollected()
            .after(ROOT)
    );

    public static final AddonAdvancement HEAT_BATTERY = create("heat_battery", b -> b
            .icon(ModBlocks.HEAT_BATTERY.get())
            .title("Thermal Reserve")
            .description("Fill a Heat Battery with Molten Salt to store thermal energy.")
            .withCustomTrigger(ModTriggers.HEAT_STORED)
            .after(SOLAR_HEATER)
    );

    public static final AddonAdvancement SOLAR_TOWER = create("solar_tower", b -> b
            .icon(ModBlocks.SOLAR_POWER_TOWER.get())
            .title("Tower of Light")
            .description("Build a Solar Power Tower multiblock and bring it online.")
            .withCustomTrigger(ModTriggers.TOWER_BUILT)
            .after(HEAT_BATTERY)
            .special(TaskType.EXPERT)
    );

    public static final AddonAdvancement TURBINE_CASING = create("turbine_casing", b -> b
            .icon(ModBlocks.TURBINE_CASING.get())
            .title("Heavy Metal")
            .description("Craft Turbine Casing - the structural blocks that form the Steam Turbine shell.")
            .whenIconCollected()
            .after(SOLAR_TOWER)
    );

    public static final AddonAdvancement TURBINE_CASING_GLASS = create("turbine_casing_glass", b -> b
            .icon(ModBlocks.TURBINE_CASING_GLASS.get())
            .title("Inspection Window")
            .description("Craft Turbine Casing Glass to watch the blades spinning inside.")
            .whenIconCollected()
            .after(TURBINE_CASING)
    );

    public static final AddonAdvancement STEAM_TURBINE = create("steam_turbine", b -> b
            .icon(ModBlocks.TURBINE_ROTOR.get())
            .title("Steam Powered")
            .description("Assemble a working Steam Turbine and generate rotational force from steam.")
            .withCustomTrigger(ModTriggers.TURBINE_BUILT)
            .after(TURBINE_CASING)
            .special(TaskType.EXPERT)
    );

    public static final AddonAdvancement TURBINE_FLOODED = create("turbine_flooded", b -> b
            .icon(ModBlocks.TURBINE_CASING.get())
            .title("Waterlogged")
            .description("Let the condensate tank fill up and shut the turbine down. Pipe out the water next time.")
            .withCustomTrigger(ModTriggers.TURBINE_FLOODED)
            .after(STEAM_TURBINE)
            .special(TaskType.SECRET)
    );

    public static final AddonAdvancement FUSION_REACTOR = create("fusion_reactor", b -> b
            .icon(ModBlocks.FUSION_REACTOR_CORE.get())
            .title("A Star in a Bottle")
            .description("Bring a Fusion Reactor online - the absolute capstone of clean power.")
            .withCustomTrigger(ModTriggers.REACTOR_BUILT)
            .after(STEAM_TURBINE)
            .special(TaskType.CHALLENGE)
    );

    public static final AddonAdvancement TRITIUM_BRED = create("tritium_bred", b -> b
            .icon(ModBlocks.LITHIUM_BREEDER_MODULE.get())
            .title("Self-Sustaining")
            .description("Let a Fusion Reactor's Breeder Modules fill an Empty Fuel Capsule with Tritium.")
            .withCustomTrigger(ModTriggers.TRITIUM_BRED)
            .after(FUSION_REACTOR)
    );

    // -------------------------------------------------------------------------
    // Particle Accelerator branch
    // -------------------------------------------------------------------------

    public static final AddonAdvancement ELECTROMAGNET_COIL = create("electromagnet_coil", b -> b
            .icon(ModItems.ELECTROMAGNET_COIL.get())
            .title("Magnetic Personality")
            .description("Assemble an Electromagnet Coil, the heart of every Particle Accelerator Pipe.")
            .whenIconCollected()
            .after(STEAM_TURBINE)
    );

    public static final AddonAdvancement SUPERCONDUCTING_COIL = create("superconducting_coil", b -> b
            .icon(ModItems.SUPERCONDUCTING_COIL.get())
            .title("Zero Resistance")
            .description("Assemble a Superconducting Coil without it falling apart.")
            .whenIconCollected()
            .after(ELECTROMAGNET_COIL)
    );

    public static final AddonAdvancement PARTICLE_ACCELERATOR = create("particle_accelerator", b -> b
            .icon(ModBlocks.PARTICLE_ACCELERATOR_INPUT.get())
            .title("Going in Circles")
            .description("Close a Particle Accelerator ring around its Input.")
            .withCustomTrigger(ModTriggers.ACCELERATOR_BUILT)
            .after(SUPERCONDUCTING_COIL)
            .special(TaskType.EXPERT)
    );

    public static final AddonAdvancement PARTICLE_COLLISION = create("particle_collision", b -> b
            .icon(ModBlocks.PARTICLE_ACCELERATOR_PIPE.get())
            .title("Smashing Success")
            .description("Watch the beams collide as a Particle Accelerator finishes its first recipe.")
            .withCustomTrigger(ModTriggers.ACCELERATOR_COLLISION)
            .after(PARTICLE_ACCELERATOR)
    );

    public static final AddonAdvancement TRITIUM_CAPSULE = create("tritium_capsule", b -> b
            .icon(ModItems.TRITIUM_CAPSULE.get())
            .title("Bottled Starfuel")
            .description("Get a Tritium Capsule, a shortcut fuel for the Fusion Reactor.")
            .whenIconCollected()
            .after(PARTICLE_COLLISION)
    );

    public static final AddonAdvancement BIG_RING = create("accelerator_big_ring", b -> b
            .icon(Items.NETHERITE_SCRAP)
            .title("Large Hadron Collider")
            .description("Finish a recipe in a Particle Accelerator ring at least 21 blocks across.")
            .withCustomTrigger(ModTriggers.ACCELERATOR_BIG_RING)
            .after(PARTICLE_COLLISION)
            .special(TaskType.EXPERT)
    );

    // Hidden: a Dispenser puts a Wire Connector or Grow Lamp on your head (HeadwearDispenseBehavior).
    public static final AddonAdvancement DISPENSED_WIRE_CONNECTOR = create("dispensed_wire_connector", b -> b
            .icon(ModBlocks.WIRE_CONNECTOR.get())
            .title("Fashionably Wired")
            .description("Have a Dispenser put a Wire Connector on your head.")
            .withCustomTrigger(ModTriggers.DISPENSED_WIRE_CONNECTOR)
            .after(ROOT)
            .special(TaskType.SECRET)
    );

    public static final AddonAdvancement DISPENSED_GROW_LAMP = create("dispensed_grow_lamp", b -> b
            .icon(ModBlocks.GROW_LAMP.get())
            .title("TO ME, MY X-MEN!")
            .description("Have a Dispenser put a Grow Lamp on your head.")
            .withCustomTrigger(ModTriggers.DISPENSED_GROW_LAMP)
            .after(ROOT)
            .special(TaskType.SECRET)
    );

    // -------------------------------------------------------------------------
    // Geothermal branch
    // -------------------------------------------------------------------------

    public static final AddonAdvancement GEYSER = create("geyser", b -> b
            .icon(ModBlocks.GEYSER_CAP.get())
            .title("From the Deep")
            .description("Cap a Geyser Vent for passive, round-the-clock geothermal power.")
            .withCustomTrigger(ModTriggers.GEYSER_ACTIVE)
            .after(ROOT)
    );

    public static final AddonAdvancement GEYSER_SCALDED = create("geyser_scalded", b -> b
            .icon(ModBlocks.GEYSER_VENT.get())
            .title("Blowing Off Steam")
            .description("Get launched by an erupting Geyser Vent. Maybe cap it next time.")
            .withCustomTrigger(ModTriggers.GEYSER_SCALDED)
            .after(ROOT)
            .special(TaskType.SECRET)
    );

    // -------------------------------------------------------------------------
    // Biofuel chain
    // -------------------------------------------------------------------------

    public static final AddonAdvancement BIOMASS = create("biomass", b -> b
            .icon(ModItems.BIOMASS.get())
            .title("Going Green")
            .description("Gather Biomass to start the biofuel production chain.")
            .whenIconCollected()
            .after(ROOT)
    );

    public static final AddonAdvancement GREENHOUSE = create("greenhouse", b -> b
            .icon(ModBlocks.GREENHOUSE_CONTROLLER.get())
            .title("Glasshouse Gardener")
            .description("Seal a greenhouse with a Greenhouse Controller.")
            .withCustomTrigger(ModTriggers.GREENHOUSE_SEALED)
            .after(BIOMASS)
    );

    public static final AddonAdvancement BIOREACTOR = create("bioreactor", b -> b
            .icon(ModBlocks.BIOREACTOR.get())
            .title("Pond Scum")
            .description("Grow Biomass from algae in a Bioreactor.")
            .withCustomTrigger(ModTriggers.BIOREACTOR_BIOMASS)
            .after(BIOMASS)
    );

    public static final AddonAdvancement GASIFIER = create("gasifier", b -> b
            .icon(ModBlocks.BIOMASS_GASIFIER.get())
            .title("Burning Clean")
            .description("Ignite a Biomass Gasifier and put it to work.")
            .withCustomTrigger(ModTriggers.GASIFIER_RUNNING)
            .after(BIOMASS)
    );

    public static final AddonAdvancement FERMENTATION = create("fermentation", b -> b
            .icon(ModBlocks.FERMENTATION_VAT.get())
            .title("The Brewer")
            .description("Produce Biofuel in a Fermentation Vat.")
            .withCustomTrigger(ModTriggers.FERMENTATION_DONE)
            .after(GASIFIER)
            .special(TaskType.EXPERT)
    );

    public static final AddonAdvancement BIOFUEL_ENGINE = create("biofuel_engine", b -> b
            .icon(ModBlocks.BIOFUEL_ENGINE.get())
            .title("Engines of Tomorrow")
            .description("Power a Biofuel Engine with the Biofuel you produced.")
            .withCustomTrigger(ModTriggers.BIOFUEL_ENGINE_ON)
            .after(FERMENTATION)
    );

    // -------------------------------------------------------------------------
    // DataProvider
    // -------------------------------------------------------------------------

    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public AllSolarpunkAdvancements(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.output = output;
        this.registries = registries;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return registries.thenCompose(provider -> {
            PackOutput.PathProvider pathProvider =
                    output.createPathProvider(PackOutput.Target.DATA_PACK, "advancement");

            List<CompletableFuture<?>> futures = new ArrayList<>();
            Set<ResourceLocation> seen = Sets.newHashSet();

            Consumer<AdvancementHolder> saver = holder -> {
                if (!seen.add(holder.id()))
                    throw new IllegalStateException("Duplicate advancement: " + holder.id());
                Path path = pathProvider.json(holder.id());
                futures.add(DataProvider.saveStable(cache, provider, Advancement.CODEC, holder.value(), path));
            };

            for (AddonAdvancement adv : ENTRIES) {
                adv.save(saver, provider);
            }

            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    @Override
    public String getName() { return "Solarpunk Advancements"; }

    public static void provideLang(BiConsumer<String, String> langOut) {
        for (AddonAdvancement adv : ENTRIES) {
            adv.provideLang(langOut);
        }
    }
}
