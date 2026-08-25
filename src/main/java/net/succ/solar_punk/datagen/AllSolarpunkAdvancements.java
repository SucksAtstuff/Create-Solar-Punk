package net.succ.solar_punk.datagen;

import com.google.common.collect.Sets;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.advancement.ModTriggers;
import net.succ.solar_punk.advancement.SolarpunkAdvancement;
import net.succ.solar_punk.advancement.SolarpunkAdvancement.TaskType;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.item.ModItems;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AllSolarpunkAdvancements implements DataProvider {

    public static final List<SolarpunkAdvancement> ENTRIES = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Solar chain
    // -------------------------------------------------------------------------

    public static final SolarpunkAdvancement ROOT = SolarpunkAdvancement.create("root", b -> b
            .icon(ModItems.SALT.get())
            .title("Powered by Nature")
            .description("Collect Salt - the foundation of solar thermal energy.")
            .whenItemCollected(ModItems.SALT.get())
    );

    public static final SolarpunkAdvancement SOLAR_HEATER = SolarpunkAdvancement.create("solar_heater", b -> b
            .icon(ModBlocks.SOLAR_HEATER.get())
            .title("Heat Collector")
            .description("Craft a Solar Heater to begin concentrating sunlight into Molten Salt.")
            .whenIconCollected()
            .after(ROOT)
    );

    public static final SolarpunkAdvancement HEAT_BATTERY = SolarpunkAdvancement.create("heat_battery", b -> b
            .icon(ModBlocks.HEAT_BATTERY.get())
            .title("Thermal Reserve")
            .description("Fill a Heat Battery with Molten Salt to store thermal energy.")
            .withCustomTrigger(ModTriggers.HEAT_STORED)
            .after(SOLAR_HEATER)
    );

    public static final SolarpunkAdvancement SOLAR_TOWER = SolarpunkAdvancement.create("solar_tower", b -> b
            .icon(ModBlocks.SOLAR_POWER_TOWER.get())
            .title("Tower of Light")
            .description("Build a Solar Power Tower multiblock and bring it online.")
            .withCustomTrigger(ModTriggers.TOWER_BUILT)
            .after(HEAT_BATTERY)
            .special(TaskType.EXPERT)
    );

    public static final SolarpunkAdvancement TURBINE_CASING = SolarpunkAdvancement.create("turbine_casing", b -> b
            .icon(ModBlocks.TURBINE_CASING.get())
            .title("Heavy Metal")
            .description("Craft Turbine Casing - the structural blocks that form the Steam Turbine shell.")
            .whenIconCollected()
            .after(SOLAR_TOWER)
    );

    public static final SolarpunkAdvancement TURBINE_CASING_GLASS = SolarpunkAdvancement.create("turbine_casing_glass", b -> b
            .icon(ModBlocks.TURBINE_CASING_GLASS.get())
            .title("Inspection Window")
            .description("Craft Turbine Casing Glass to watch the blades spinning inside.")
            .whenIconCollected()
            .after(TURBINE_CASING)
    );

    public static final SolarpunkAdvancement STEAM_TURBINE = SolarpunkAdvancement.create("steam_turbine", b -> b
            .icon(ModBlocks.TURBINE_ROTOR.get())
            .title("Steam Powered")
            .description("Assemble a working Steam Turbine and generate rotational force from steam.")
            .withCustomTrigger(ModTriggers.TURBINE_BUILT)
            .after(TURBINE_CASING)
            .special(TaskType.EXPERT)
    );

    public static final SolarpunkAdvancement TURBINE_FLOODED = SolarpunkAdvancement.create("turbine_flooded", b -> b
            .icon(ModBlocks.TURBINE_CASING.get())
            .title("Waterlogged")
            .description("Let the condensate tank fill up and shut the turbine down. Pipe out the water next time.")
            .withCustomTrigger(ModTriggers.TURBINE_FLOODED)
            .after(STEAM_TURBINE)
            .special(TaskType.SECRET)
    );

    public static final SolarpunkAdvancement FUSION_REACTOR = SolarpunkAdvancement.create("fusion_reactor", b -> b
            .icon(ModBlocks.FUSION_REACTOR_CORE.get())
            .title("A Star in a Bottle")
            .description("Bring a Fusion Reactor online - the absolute capstone of clean power.")
            .withCustomTrigger(ModTriggers.REACTOR_BUILT)
            .after(STEAM_TURBINE)
            .special(TaskType.CHALLENGE)
    );

    // -------------------------------------------------------------------------
    // Geothermal branch
    // -------------------------------------------------------------------------

    public static final SolarpunkAdvancement GEYSER = SolarpunkAdvancement.create("geyser", b -> b
            .icon(ModBlocks.GEYSER_CAP.get())
            .title("From the Deep")
            .description("Cap a Geyser Vent for passive, round-the-clock geothermal power.")
            .withCustomTrigger(ModTriggers.GEYSER_ACTIVE)
            .after(ROOT)
    );

    // -------------------------------------------------------------------------
    // Biofuel chain
    // -------------------------------------------------------------------------

    public static final SolarpunkAdvancement BIOMASS = SolarpunkAdvancement.create("biomass", b -> b
            .icon(ModItems.BIOMASS.get())
            .title("Going Green")
            .description("Gather Biomass to start the biofuel production chain.")
            .whenIconCollected()
            .after(ROOT)
    );

    public static final SolarpunkAdvancement GASIFIER = SolarpunkAdvancement.create("gasifier", b -> b
            .icon(ModBlocks.BIOMASS_GASIFIER.get())
            .title("Burning Clean")
            .description("Ignite a Biomass Gasifier and put it to work.")
            .withCustomTrigger(ModTriggers.GASIFIER_RUNNING)
            .after(BIOMASS)
    );

    public static final SolarpunkAdvancement FERMENTATION = SolarpunkAdvancement.create("fermentation", b -> b
            .icon(ModBlocks.FERMENTATION_VAT.get())
            .title("The Brewer")
            .description("Produce Biofuel in a Fermentation Vat.")
            .withCustomTrigger(ModTriggers.FERMENTATION_DONE)
            .after(GASIFIER)
            .special(TaskType.EXPERT)
    );

    public static final SolarpunkAdvancement BIOFUEL_ENGINE = SolarpunkAdvancement.create("biofuel_engine", b -> b
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

            for (SolarpunkAdvancement adv : ENTRIES) {
                adv.save(saver, provider);
            }

            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    @Override
    public String getName() { return "Solarpunk Advancements"; }

    public static void provideLang(BiConsumer<String, String> langOut) {
        for (SolarpunkAdvancement adv : ENTRIES) {
            adv.provideLang(langOut);
        }
    }
}
