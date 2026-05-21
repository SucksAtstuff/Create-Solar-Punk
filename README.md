# Create: Solarpunk

A NeoForge mod for Minecraft 1.21.1 that adds solar energy generation, thermal storage, and biofuel production to the Create ecosystem.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.228+
- Create 6.0.4+

## Features

### Solar Heat Chain
- **Salt** - a new material found as underground deposits in desert, badlands, and savanna biomes; the core fuel of the solar heat chain
- **Solar Heater** - concentrates sunlight to melt salt into molten salt or evaporate water into salt crystals; requires a clear view of the sky
- **Solar Power Tower** - large multiblock structure (1×1 up to 3×3 footprint, up to 20 blocks tall) that concentrates reflected sunlight to produce molten salt at scale; pair with Solar Mirrors for maximum efficiency
- **Solar Mirror** - a heliostat that reflects sunlight toward a Solar Power Tower; place directly against the tower's side faces; can be mounted on floors, walls, and ceilings
- **Heat Battery** - stores thermal energy from molten salt and heats Create boilers

### Electricity
- **Andesite Solar Panel** - generates Create rotational force (SU) from sunlight
- **Brass Solar Panel** - generates Forge Energy (FE) from sunlight
- **Kinetic Battery** - stores rotational energy (SU) for later use

### Biofuel Chain
- **Biomass** - crafted from organic matter
- **Biomass Gasifier** - processes raw biomass using rotational force
- **Fermentation Vat** - multiblock structure (stackable vertically) that converts biomass into liquid biofuel using water
- **Biofuel Engine** - burns biofuel to generate rotational force (SU)

### Geothermal
- **Geyser Vent** - spawns naturally in desert, badlands, and savanna biomes
- **Geyser Cap** - harnesses geothermal energy from a Geyser Vent and converts it into rotational force; passive, works day and night in any weather

## Quick Start

1. Find **salt** underground in arid biomes (desert, badlands, savanna) or craft it
2. Place a **Solar Heater** with a clear view of the sky
3. Right-click salt into the heater's input slot - it converts to molten salt during daytime
4. Pipe the molten salt into a **Heat Battery**
5. Place the Heat Battery adjacent to a Create boiler to heat it

For high-volume molten salt production, build a **Solar Power Tower** and surround its sides with **Solar Mirrors**.

## Solar Power Tower Guide

The tower is a multiblock built by stacking tower blocks and using the Solar Power Tower item to place multiple in a row (like fluid tanks). It operates only in direct sunlight and stops during rain or thunderstorms.

| Footprint | Max height | Optimal mirrors | Water demand (per mB salt) |
|-----------|-----------|-----------------|---------------------------|
| 1×1       | 5         | 10              | 1× (1:1)                  |
| 2×2       | 10        | 40              | 4× (4:1)                  |
| 3×3       | 20        | 180             | 9× (9:1)                  |

**Mirror efficiency** follows a triangle curve: efficiency rises linearly from 0 mirrors to the optimal count (100%), then falls back to 0% at twice the optimal. Over-mirroring shuts the tower down.

**Minimum size:** the tower requires at least a 3×3 footprint to produce anything.

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
