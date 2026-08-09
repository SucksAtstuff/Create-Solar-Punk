# Create: Solarpunk

A NeoForge mod for Minecraft 1.21.1 that adds solar energy generation, thermal storage, biofuel production, and an optional global warming system to the Create ecosystem.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.228+
- Create 6.0.4+
- GeckoLib 4.7.2+

**Optional:** JEI (adds recipe categories for the Solar Heater and Fermentation Vat)

## Features

### Solar Heat Chain
- **Salt** - a new material found as underground deposits in desert, badlands, and savanna biomes; the core fuel of the solar heat chain
- **Solar Heater** - concentrates sunlight to melt salt into molten salt or evaporate water into salt crystals; requires a clear view of the sky
- **Solar Power Tower** - large multiblock structure (1×1 up to 3×3 footprint, up to 20 blocks tall) that concentrates reflected sunlight to produce molten salt or steam at scale; right-click with a Wrench to toggle between Molten Salt mode and Steam mode; pair with Solar Mirrors for maximum efficiency
- **Solar Mirror** - a freestanding heliostat; place it on the ground anywhere near a Solar Power Tower and it links up automatically as long as it has open sky above it and a clear line of sight to the tower. Linked mirrors continuously turn and tilt to track the sun and reflect it toward the tower
- **Heat Battery** - stores thermal energy from molten salt and heats Create boilers

### Steam Turbine
- **Steam** - a new fluid produced by a Solar Power Tower in Steam mode or a Firebox Boiler; piped in to power the turbine
- **Steam Turbine** - a multiblock built from a 7×7 shell of Turbine Casing around a column of Turbine Rotors, with Turbine Blades filling each interior layer in a plus pattern; converts piped-in steam into rotational force, draining condensate water from the bottom; can be built standing up or lying on its side (East/West or North/South axis) - it works exactly the same way either orientation
- **Turbine Casing / Turbine Casing Glass** - the structural shell blocks of the turbine; craft Turbine Casing by right-clicking a Block of Industrial Iron with a Zinc Ingot (or run it through a Deployer), then right-click a Turbine Casing with Glass for the see-through variant
- **Turbine Rotor** - the central column the blades attach to; rotational output exits from the top (or the end of the rotor column, for horizontal builds)
- **Andesite Turbine Blade / Brass Turbine Blade** - Andesite Blades are cheap but waste more steam; Brass Blades squeeze out the most power per bucket and reach 100% efficiency with just one blade per arm; mixing types is fine, efficiency scales smoothly
- **Firebox Boiler** - a cheap early-game Steam source; craft it with iron, a furnace, and a bucket, feed it any furnace fuel, and pipe water in to get Steam out - no Salt or Solar Power Tower required. Much weaker per block than a fully built Solar Power Tower, but enough to get a Steam Turbine running early

### Electricity
- **Andesite Solar Panel** - generates Create rotational force (SU) from sunlight
- **Brass Solar Panel** - generates Forge Energy (FE) from sunlight
- **Kinetic Battery** - stores rotational energy (SU) for later use

### Biofuel Chain
- **Biomass** - crafted from organic matter
- **Biomass Pellet** - a compacted form of biomass; burns slower in the Gasifier but produces twice the Biochar per piece
- **Biomass Gasifier** - processes raw biomass or pellets using rotational force; produces Biochar as a byproduct
- **Fermentation Vat** - multiblock structure (minimum 2×2 footprint, stackable vertically, minimum 4 blocks tall) that converts biomass into liquid biofuel using water; larger and taller footprints process more per cycle
- **Biofuel Engine** - burns biofuel to generate rotational force (SU)
- **Biochar** - byproduct of the Gasifier; acts as a bonemeal substitute or mixes with water to make Fertilizer
- **Fertilizer** - liquid made from Biochar and water; used in the Kinetic Sprinkler for forced instant crop growth
- **Kinetic Sprinkler** - hydrates farmland and accelerates crop growth in a 5×5 area; pipe in water to boost growth or Fertilizer for instant growth every cycle

The **Biomass Gasifier** and **Biofuel Engine** support rotation direction control, just like the Andesite Solar Panel - scroll on the side of the block, away from the shaft faces, to toggle clockwise/counter-clockwise output.

### Geothermal
- **Geyser Vent** - spawns naturally in desert, badlands, and savanna biomes
- **Geyser Cap** - harnesses geothermal energy from a Geyser Vent and converts it into rotational force; passive, works day and night in any weather; also supports rotation direction control via scroll

### Global Warming (optional)
An opt-in pollution system, disabled by default and toggled in the config.

- **Pollution sources** - campfires, furnaces, blast furnaces, smokers, blaze burners, steam engines, biofuel engines, and biomass gasifiers emit smoke and accumulate pollution in nearby chunks
- **Block decay** - polluted chunks decay progressively: grass withers to Dead Grass, dirt crumbles to Ruined Dirt, then Ash; plants and leaves are cleared; biomes convert to a dead wasteland at high pollution levels
- **Biofilter** - a brass-tier machine that draws rotational power and scrubs pollution from surrounding chunks, reversing the decay over time; goggles show the current pollution level and filter rate
- A pollution blacklist config option lets you exclude specific blocks from ever counting as pollution sources
- All pollution rates, thresholds, decay speeds, blacklisted blocks, and the target dead biome are configurable; see the [wiki](https://github.com/SucksAtstuff/Create-Solar-Punk/wiki) for full details

### Achievements
A dedicated **Create: Solarpunk** advancement tab guides you through the mod, from collecting your first Salt all the way to running a Steam Turbine, with branches for the geothermal and biofuel chains along the way.

### Compatibility
- **Create: Diesel Generators** - diesel engines count as pollution sources when Global Warming is enabled
- **Create: Aeronautics** - engines on airships correctly accumulate pollution in the real-world chunk beneath the airship, even though they run in a simulated sublevel; requires Aeronautics to be installed, otherwise has no effect
- **Create: New Age** - the Generator Coil is excluded from pollution accumulation

## Quick Start

1. Find **salt** underground in arid biomes (desert, badlands, savanna) or craft it
2. Place a **Solar Heater** with a clear view of the sky
3. Right-click salt into the heater's input slot - it converts to molten salt during daytime
4. Pipe the molten salt into a **Heat Battery**
5. Place the Heat Battery adjacent to a Create boiler to heat it

For high-volume molten salt production, build a **Solar Power Tower** and surround it with a field of **Solar Mirrors**.

## Solar Power Tower Guide

The tower is a multiblock built by stacking tower blocks and using the Solar Power Tower item to place multiple in a row (like fluid tanks). It operates only in direct sunlight and stops during rain or thunderstorms.

| Footprint | Max height | Mirror search radius | Optimal mirrors (100% eff.) | Water demand (per mB salt) |
|-----------|-----------|-----------------------|-------------------------------|---------------------------|
| 1×1       | 5         | 14 blocks             | 75                             | 1× (1:1)                  |
| 2×2       | 10        | 24 blocks (capped)    | 128                            | 4× (4:1)                  |
| 3×3       | 20        | 24 blocks (capped)    | 128                            | 9× (9:1)                  |

Solar Mirrors are freestanding - place them on the ground anywhere within the tower's search radius (a circle scaling with tower height, capped at 24 blocks by default). A mirror links automatically if it has open sky above it and a clear line of sight to the tower; once linked, it continuously turns and tilts to track the sun and reflect it toward the tower, like a real heliostat.

**Mirror efficiency** follows a triangle curve: efficiency rises linearly from 0 mirrors to the optimal count (100%), then falls back to 0% at twice the optimal. Over-mirroring shuts the tower down. A tower will never track more than 128 mirrors total by default (configurable) - for any tower with a search radius at the 24-block cap (height 10+), that limit lands right at the optimal count, so over-mirroring isn't reachable at max size.

**Minimum size:** the tower requires at least a 3×3 footprint and 3 blocks of height to produce anything.

## Steam Turbine Guide

Build a 7×7 shell of Turbine Casing (or Turbine Casing Glass) around a column of Turbine Rotors, then fill each interior layer with Turbine Blades in a plus pattern - two blades per arm, four arms. Pipe steam in from any side face; rotational power exits from the top of the rotor column, and condensate water drains from the bottom.

The turbine can also be built lying on its side: build the same 7×7 shell along the East/West or North/South axis instead of straight up, and it works exactly the same way.

Blade type determines efficiency: Andesite Blades are cheaper but waste more steam, while Brass Blades reach 100% efficiency with just one blade per arm. Mixing blade types is fine - efficiency scales smoothly. Taller turbines consume more steam and produce more SU.

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
