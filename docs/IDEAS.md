# Ideas - Create: Solarpunk

A running list of possible future features. Nothing here is committed. Grouped by theme,
with the strongest / most on-theme ideas near the top of each section.

## Next Up (2.1 Shortlist)

In rough priority order.

1. **Anaerobic digester / biogas** - methane from rotten flesh, compost, manure. Fits the
   "nothing is wasted" theme of the Particle Accelerator's Leather recipe. Biogas could feed
   the Biofuel Engine or the Firebox Boiler.
2. **Grid-powered streetlights** that switch on at dusk. A visible everyday use for FE.

## Biggest gaps in the renewable lineup

- **Hydrogen chain** - an Electrolyzer splits Water into Hydrogen using FE from Brass Panels,
  and a Fuel Cell turns it back into SU or FE. Gives solar FE a storage use, and ties into
  Northstar's Liquid Hydrogen, which the Steam Turbine already burns.
- **FE storage** - FE only exists in a Brass Panel's small buffer right now. A Thermal
  Generator (Molten Salt heat to FE) or a large battery multiblock.
- **Tidal / wave power** - SU from ocean water levels, changing with the time of day, so it's
  different from Create's water wheel.
- **Dedicated grid-scale storage** beyond Heat Battery / Kinetic Battery - a gravity battery
  that hoists a weighted block on surplus power. (Flywheel storage is largely what the Kinetic
  Battery already does.)

## Solar chain depth

- **Visible output scaling** - goggle readout of current vs peak output, with rain/snow/night
  penalties shown explicitly.
- **Parabolic trough** - a linear mid-tier between the Solar Heater and the Solar Power Tower.
  Overlaps both, so low priority.
- **Stirling dish concentrator** - direct SU/FE from a focused dish, no molten salt loop.
  Overlaps the Solar Heater and tower, so low priority.

## Biofuel expansion

- **Anaerobic digester / biogas** - see Next Up.
- **Algae bioreactor** - light + water + CO2 to biomass, an alternative to farming. Could use the
  Biofilter's Carbon Dust.
- **Greenhouse** - a new **Greenhouse Glass** block. Any shape works (a big dome, or just a
  box around the farm) as long as the glass fully seals the crops in; the ground can be any
  solid block. Crops inside grow faster, more with a Kinetic Sprinkler and Fertilizer. A
  proper home for Biomass farms.
  - Detection: a flood fill of the air inside, capped at a max volume, run every few seconds
    by one block inside (the Kinetic Sprinkler, or a small controller), not by every crop.
  - Possible extras: crops inside are safe from pollution decay and wasteland biomes, and
    grow whatever the outside biome or season.
  - Open: do doors and trapdoors count as sealed? Does a gap of one block break it?

## Geothermal

Vents are meant to be scarce, so ideas here should make a natural vent worth more rather than
let geothermal work anywhere.

- **Enhanced geothermal** - pump water down, steam comes up, run the Steam Turbine.

## Particle Accelerator

- **More recipes over time**, especially turning waste or byproducts into useful materials.

## Grid management

Lower priority: a lot of UI work, Create's own overstress already handles overload, and
Display Links cover most monitoring.

- **Grid controller** - priority routing, load shedding, comparator output on charge level.
- **Energy monitor block** with an output-over-time graph GUI.
- **Brownout behavior** when demand exceeds supply.

## Solarpunk building / aesthetic

- **Grid-powered streetlights** - see Next Up.
- **Decorative solar** - solar glass, shingles, canopy/pergola, fence.
- **Green roofs, living walls, trellises** with climbing plants.
- **Natural building blocks** - rammed earth, bamboo, cork. Drifts from the mod's power focus.

## Compat

- **Ad Astra** - strong solar in orbit, weak on Mars, no wind on airless bodies. Space support
  already works through the `solarpunk:always_sunny` / `solarpunk:no_weather` dimension type
  tags, so this may only need tagging (check Ad Astra exists for 1.21.1 NeoForge first).
