# Create: Solarpunk

A NeoForge mod for Minecraft 1.21.1 that adds renewable power to the Create ecosystem: solar panels, solar heat and thermal storage, steam turbines, biofuel, geothermal power, nuclear fusion, a Particle Accelerator, and an optional global warming system.

**New to the mod?** Watch this video for a walkthrough of sorts covering what it adds and how it all fits together:

[![Create: Solarpunk walkthrough](https://img.youtube.com/vi/OxV7FP923lA/maxresdefault.jpg)](https://youtu.be/OxV7FP923lA)

> **Note:** some of the recipes shown in the video are outdated as of the 2.0 release. Check JEI for the current recipes.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.228+
- Create 6.0.10+
- GeckoLib 4.7.2+

**Optional:**
- JEI (adds recipe categories for the Solar Heater, Fermentation Vat, Solar Power Tower, Crystallizer, and Particle Accelerator)
- Serene Seasons (solar output scales with the season - up in summer, down in winter; can be turned off in the config)
- Create: Northstar Redux (the Steam Turbine can burn its Liquid Hydrogen as an alternative fuel to Steam)
- Create: Cosmonautics (solar blocks work in space and follow the real sun; stations are built with Create Aeronautics)
- Create: New Age (the Heat Battery trades heat with New Age's heat pipes and machines)

## Features

### Solar Panels and Storage
- **Andesite Solar Panel** - generates Create rotational force (SU) from sunlight
- **Kinetic Battery** - stores rotational force (SU) for later use, such as running your factory through the night
- **Brass Solar Panel** - the mod's only electric block: generates Forge Energy (FE) from sunlight for FE-powered mods; built in a Mechanical Crafter from Photovoltaic Cells
- **Photovoltaic Cell** - made by sequenced assembly: a Glass Pane, then deploy Polished Rose Quartz, deploy a Copper Sheet, and press

### Solar Heat Chain
- **Salt** - a new material found as underground deposits in desert, badlands, and savanna biomes; the core fuel of the solar heat chain. Salt from Northstar and Create: Food works too
- **Solar Heater** - concentrates sunlight to melt salt into molten salt or evaporate water into salt crystals; it can also melt any stone or cobblestone into Lava. Requires a clear view of the sky
- **Solar Power Tower** - large multiblock structure (up to 3×3 footprint and 20 blocks tall) that concentrates reflected sunlight to produce molten salt or steam at scale; right-click with a Wrench to toggle between Molten Salt mode and Steam mode; pair with Solar Mirrors for maximum efficiency
- **Solar Mirror** - a freestanding heliostat; place it on the ground anywhere near a Solar Power Tower and it links up automatically as long as it has open sky above it and a clear line of sight to the tower. Linked mirrors continuously turn and tilt to track the sun and reflect it toward the tower
- **Heat Battery** - stores thermal energy from molten salt and heats Create boilers
- **Crystallizer** - a brass-tier machine that quenches Molten Salt with Water back into Salt, releasing a bit of Steam as a byproduct; a much faster bulk alternative to the Solar Heater's slow evaporation trickle. Pipe both fluids in from any side and collect Salt from the output slot (or a hopper underneath) and Steam from any face. It's recipe-driven, so modpacks and datapacks can add their own custom conversions to the same machine

### Steam Turbine
- **Steam** - a new fluid produced by a Solar Power Tower in Steam mode, a Firebox Boiler, or a Fusion Reactor; piped in to power the turbine
- **Steam Turbine** - a multiblock built from a 7×7 shell of Turbine Casing around a column of Turbine Rotors, with a Turbine Blade on each of the four arms of every layer; converts piped-in steam into rotational force, and the condensed water has to be piped back out. Can be built standing up or lying on its side (East/West or North/South axis) - it works exactly the same way either orientation
- **Turbine Casing / Turbine Casing Glass** - the structural shell blocks of the turbine; craft Turbine Casing by right-clicking a Block of Industrial Iron with a Zinc Ingot (or run it through a Deployer), then right-click a Turbine Casing with Glass for the see-through variant
- **Turbine Rotor** - the central column the blades attach to; rotational output exits from the top (or the end of the rotor column, for horizontal builds)
- **Andesite Turbine Blade / Brass Turbine Blade** - Andesite Blades are cheap but waste more steam; Brass Blades squeeze out the most power per bucket and reach 100% efficiency; mixing types is fine, efficiency scales smoothly
- **Firebox Boiler** - a cheap early-game Steam source; craft it with iron, a furnace, and a bucket, feed it any furnace fuel, and pipe water in to get Steam out - no Salt or Solar Power Tower required. Much weaker per block than a fully built Solar Power Tower, but enough to get a Steam Turbine running early

### Biofuel Chain
- **Biomass** - crushed or milled from plants and other organic matter
- **Biomass Pellet** - a compacted form of biomass; burns slower in the Gasifier but produces twice the Biochar per piece
- **Biomass Gasifier** - burns raw biomass or pellets to generate rotational force (SU); produces Biochar as a byproduct
- **Fermentation Vat** - multiblock structure (minimum 2×2 footprint and 4 blocks tall, up to 3×3 and 16 tall) that converts biomass into liquid biofuel using water; larger and taller vats process more per cycle
- **Biofuel Engine** - burns biofuel to generate rotational force (SU)
- **Biochar** - byproduct of the Gasifier; acts as a bonemeal substitute or mixes with water to make Fertilizer
- **Fertilizer** - liquid made from Biochar and water; used in the Kinetic Sprinkler for forced instant crop growth
- **Kinetic Sprinkler** - hydrates farmland and accelerates crop growth in a 5×5 area; pipe in water to boost growth or Fertilizer for instant growth every cycle

The **Biomass Gasifier** and **Biofuel Engine** support rotation direction control, just like the Andesite Solar Panel - scroll on the side of the block, away from the shaft faces, to toggle clockwise/counter-clockwise output.

### Geothermal
- **Geyser Vent** - spawns naturally in desert, badlands, and savanna biomes (configurable)
- **Geyser Cap** - harnesses geothermal energy from a Geyser Vent and converts it into rotational force; passive, works day and night in any weather; also supports rotation direction control via scroll

### Fusion Reactor
- **Fusion Reactor Core** - the mod's endgame capstone; produces a huge amount of Steam from Deuterium plus Lithium Dust or Tritium Capsules, far more than any single Steam Turbine can use on its own - meant to feed a whole bank of them. A redstone signal anywhere on the shell throttles it up or down
- **Fusion Reactor Casing / Casing Glass** - the Netherite-and-Copper shell built around the Core. The shell is a standard hollow sphere, so you can build it straight from WorldEdit's `//hsphere` or an online sphere generator
- **Lithium Breeder Module / Beryllium Reflector Module** - Blanket modules that ring the Core in whatever mix you like; more Lithium breeds Tritium more efficiently for a lower, steadier output, while more Beryllium burns fuel faster for more raw Steam output. Breeder Modules also slowly refill Empty Fuel Capsules into Tritium Capsules
- **Lithium & Beryllium** - Lithium is a new ore found underground worldwide rather than in any specific biome; mine it for Raw Lithium and smelt that into Lithium Ingots, or crush it for Lithium Dust (which also smelts into ingots) with a small chance of bonus Beryllium Dust. Beryllium Dust smelts into Beryllium Ingots too, and both metals store compactly as a Block of Lithium or Block of Beryllium
- **Deuterium** - made by Mixing Water with an overheated Copper Ingot; Lithium Dust can also be made by Mixing Water with Salt over superheat (with a small chance of bonus Beryllium Dust). Both show up in JEI automatically

Building the reactor and getting it running is rewarded with its own dedicated advancement, "A Star in a Bottle."

### Particle Accelerator
- **Particle Accelerator** - an endgame machine: a ring of glass pipes, powered by rotation, that smashes items together into new materials. Watch the two beams race around the ring and collide as each recipe finishes
- **Particle Accelerator Pipe** - one item for every pipe shape (straights, bends and diagonals); hold Alt to pick the shape
- **Particle Accelerator Input** - the controller; there's one per ring. Put items in and take results out by hand, with hoppers, or with funnels, and set a filter on its cap to choose which recipe it makes
- **Electromagnet Coil / Superconducting Coil** - crafting components for the ring
- **Empty Fuel Capsule / Tritium Capsule** - fill capsules with Tritium in the accelerator to fuel the Fusion Reactor

Bigger rings and faster rotation unlock rarer results:

| Result | Ingredients | Min. ring diameter | Min. speed |
|--------|-------------|--------------------|------------|
| Leather | 16 Rotten Flesh | 5 | 64 RPM |
| Tritium Capsule | 32 Lithium Dust, 4 Beryllium Ingots, 1 Empty Fuel Capsule | 9 | 128 RPM |
| Diamond | 64 Biochar | 15 | 128 RPM |
| Echo Shard | 8 Sculk, 4 Amethyst Shards | 15 | 128 RPM |
| Netherite Scrap | 16 Gold Ingots, 16 Netherrack, 4 Nether Quartz | 21 | 192 RPM |

Running faster than the minimum speed crafts faster but draws more stress. Goggles on the Input show why it isn't crafting (ring not closed, too small, too slow or overstressed). Datapacks can add their own recipes.

### Global Warming (optional)
An opt-in pollution system, disabled by default and toggled in the config.

- **Pollution sources** - campfires, furnaces, blast furnaces, smokers, blaze burners, steam engines, biofuel engines, and biomass gasifiers emit smoke and accumulate pollution in nearby chunks
- **Block decay** - polluted chunks decay progressively: grass withers to Dead Grass, dirt crumbles to Ruined Dirt, then Ash; plants and leaves are cleared; biomes convert to a dead wasteland at high pollution levels. Leaves absorb some pollution on their own
- **Biofilter** - a brass-tier machine that draws rotational power and scrubs pollution from surrounding chunks, reversing the decay over time; goggles show the current pollution level and filter rate
- A pollution blacklist config option lets you exclude specific blocks from ever counting as pollution sources
- All pollution rates, thresholds, decay speeds, blacklisted blocks, and the target dead biome are configurable; see the [wiki](https://github.com/SucksAtstuff/Create-Solar-Punk/wiki) for full details

### Create Integration
- **Ponder** scenes for every machine and multiblock, walking through each one step by step
- **Display Link** sources for the Heat Battery, Kinetic Battery, Solar Power Tower, Fusion Reactor, and Particle Accelerator, so their live stats can go on a Display Board or Nixie Tube
- **Goggle** readouts on every machine
- Progression follows Create's own: cheap to get started, then sheets, sequenced assembly, Mechanical Crafters, and Precision Mechanisms for the higher-tier machines

### Achievements
A dedicated **Create: Solarpunk** advancement tab guides you through the mod, from collecting your first Salt all the way to running a Steam Turbine and, eventually, a Fusion Reactor, with branches for the geothermal and biofuel chains along the way.

### Compatibility
- **Create: Cosmonautics** - Solar Panels, the Solar Heater, and the Solar Power Tower work in space. In deep space they track where the sun actually is: turn your station away from it and they go dark, fly into a planet's shadow and it's night, and your station's own hull can shade them. On the Moon they follow the Moon's own day and night. There's no weather in space. Config options under `generators.space` boost deep space output or make it always sunny instead
- **Other space mods** - modpacks can add dimensions to the `solarpunk:always_sunny` (constant sunshine, no weather) or `solarpunk:no_weather` (normal day and night, no weather) dimension type tags with a datapack
- **Create: New Age** - the Heat Battery trades heat with New Age's heat pipes and machines: a charged battery heats them, and an emptier one soaks up their heat. The Generator Coil is excluded from pollution
- **Create: Northstar Redux** - the Steam Turbine can burn its Liquid Hydrogen as an alternative fuel to Steam, at the same rate; pipe it in the same way you would Steam, just not both at once - the tank has to fully drain before switching fuels. Northstar's Salt works in Solarpunk recipes and the other way around
- **Create: Food** - its Salt works in Solarpunk recipes and the other way around
- **Create: Diesel Generators** - diesel engines count as pollution sources when Global Warming is enabled
- **Create: Aeronautics** - engines on airships correctly accumulate pollution in the real-world chunk beneath the airship, even though they run in a simulated sublevel
- **Serene Seasons** - Solar Panel and Solar Power Tower output scales with the season: highest at midsummer, lowest at midwinter, near-normal in spring and autumn. Hot biomes (desert, savanna, badlands, jungle) barely change, matching how Serene Seasons treats them as seasonless. Applies to the Overworld only; toggle it and tune the summer/winter multipliers under `generators.serene_seasons` in the config
- Recipes accept the same materials from other mods (salt, lithium, beryllium, metal ingots, sheets, and glass), and the Fusion Reactor accepts Lithium Dust from other mods

## Quick Start

1. Find **salt** underground in arid biomes (desert, badlands, savanna), or evaporate water in a Solar Heater
2. Place a **Solar Heater** with a clear view of the sky
3. Right-click salt into the heater's input slot - it converts to molten salt during daytime
4. Pipe the molten salt into a **Heat Battery**
5. Place the Heat Battery under a Create boiler to heat it

For high-volume molten salt production, build a **Solar Power Tower** and surround it with a field of **Solar Mirrors**.

Every machine has a Ponder scene - hold W over it in JEI or your inventory to see how it's built.

## Solar Power Tower Guide

The tower is a multiblock built by stacking tower blocks and using the Solar Power Tower item to place multiple in a row (like fluid tanks). It operates only in direct sunlight and stops during rain or thunderstorms.

| Footprint | Max height | Mirror search radius | Optimal mirrors (100% eff.) | Water demand (per mB salt) |
|-----------|-----------|-----------------------|-------------------------------|---------------------------|
| 1×1       | 5         | 14 blocks             | 75                             | 1× (1:1)                  |
| 2×2       | 10        | 24 blocks (capped)    | 128                            | 4× (4:1)                  |
| 3×3       | 20        | 24 blocks (capped)    | 128                            | 9× (9:1)                  |

**Minimum size:** by default the tower needs a 3×3 footprint and at least 3 blocks of height to produce anything. The 1×1 and 2×2 rows only apply if a modpack lowers the minimum width in the config.

Solar Mirrors are freestanding - place them on the ground anywhere within the tower's search radius (a circle scaling with tower height, capped at 24 blocks by default). A mirror links automatically if it has open sky above it and a clear line of sight to the tower; once linked, it continuously turns and tilts to track the sun and reflect it toward the tower, like a real heliostat.

**Mirror efficiency** follows a triangle curve: efficiency rises linearly from 0 mirrors to the optimal count (100%), then falls back to 0% at twice the optimal. Over-mirroring shuts the tower down. A tower will never track more than 128 mirrors total by default (configurable) - for any tower with a search radius at the 24-block cap (height 10+), that limit lands right at the optimal count, so over-mirroring isn't reachable at max size.

## Steam Turbine Guide

Build a 7×7 shell of Turbine Casing (or Turbine Casing Glass) around a column of Turbine Rotors, with a solid floor and cap, then place a Turbine Blade on each of the four arms of every interior layer. Pipe steam in from any side face; rotational power exits from the top of the rotor column, and condensed water has to be piped back out. Turbines can be up to 20 blade layers tall.

The turbine can also be built lying on its side: build the same 7×7 shell along the East/West or North/South axis instead of straight up, and it works exactly the same way.

Blade type determines efficiency: Andesite Blades are cheaper but waste more steam, while Brass Blades reach 100% efficiency. Mixing blade types is fine - efficiency scales smoothly. Taller turbines consume more steam and produce more SU, and output scales with how much of the steam they need is actually being supplied.

No Solar Power Tower yet? A **Firebox Boiler** (iron, a furnace, and a bucket, fed with any furnace fuel) makes Steam early on to get a Turbine running before you've scaled up solar production.

## Thanks

Thanks to the following people for their ideas:

- Slopton
- [Multyfora](https://modrinth.com/user/Multyfora)
- Jimmithy5
- pumpkin_p._patch
- yui_riku
- arccam

## Links

- [Issues](https://github.com/SucksAtstuff/Create-Solar-Punk/issues)
- [Wiki](https://github.com/SucksAtstuff/Create-Solar-Punk/wiki)
- [Progress board](https://app.milanote.com/1WkoDV1EO0yY5F?p=lX3oRKKq2ax) - follow along with what's being worked on between releases
