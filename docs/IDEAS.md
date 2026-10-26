# Ideas - Create: Solarpunk

A running list of possible future features. Nothing here is committed. Grouped by theme,
with the strongest / most on-theme ideas near the top of each section.

## Next Up (2.1 Shortlist)

In rough priority order.

1. **Pumped hydro storage** (multiblock) - pump water up on surplus, run it back down through
   a turbine after dark. Pairs directly with solar's day/night cycle, and reuses the Steam
   Turbine's look.
2. **Anaerobic digester / biogas** - methane from rotten flesh, compost, manure. Fits the
   "nothing is wasted" theme of the Particle Accelerator's Leather recipe. Biogas could feed
   the Biofuel Engine or the Firebox Boiler.
3. **CO2 loop / carbon capture** - the Biofilter (or Biofuel Engine exhaust) captures pollution
   as carbon, an algae bioreactor turns it into Biomass, or it's compressed into Biochar or fed
   to the Particle Accelerator. Gives global warming a reward loop, not just punishment.
4. **Mirror soiling / cleaning** - mirrors lose efficiency over time in desert dust; wash with
   a water spray or a brush to restore output. Optional in the config.
5. **Grid-powered streetlights** that switch on at dusk. A visible everyday use for FE.
6. **Geothermal borehole multiblock** - drill for geothermal anywhere, not just at natural
   vents.

## Biggest gaps in the renewable lineup

- **Pumped hydro storage** - see Next Up.
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

- **Mirror soiling / cleaning** - see Next Up.
- **Visible output scaling** - goggle readout of current vs peak output, with rain/snow/night
  penalties shown explicitly.
- **Heat exchanger block** - move heat between molten salt and steam or other fluids.
- **Parabolic trough** - a linear mid-tier between the Solar Heater and the Solar Power Tower.
  Overlaps both, so low priority.
- **Stirling dish concentrator** - direct SU/FE from a focused dish, no molten salt loop.
  Overlaps the Solar Heater and tower, so low priority.
- **Molten salt thermocline tank** - a much larger storage multiblock than the Heat Battery.
  Close to the Heat Battery; pumped hydro covers "survive the night" better.

## Cross-chain synergy (tie salt into the electric chain)

- **Brine evaporation ponds** - low-tech early salt from sun + saltwater, no machine needed.
- **Panel fabrication chain** - quartz to silicon wafer to doped cell to panel (the cell step
  exists, see Done in 2.0).

## Biofuel expansion

- **Anaerobic digester / biogas** - see Next Up.
- **Algae bioreactor** - light + water + CO2 to biomass, an alternative to farming. Part of the
  CO2 loop in Next Up.
- **Greenhouse multiblock** - glass walls plus a Kinetic Sprinkler and Fertilizer for faster
  crop growth. A proper home for Biomass farms.

## Geothermal

- **Geothermal borehole multiblock** - see Next Up.
- **Enhanced geothermal** - pump water down, steam comes up, run the Steam Turbine.
- **Uncapped geyser hazard** - scalding damage, eruption timing, a prediction readout.

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
