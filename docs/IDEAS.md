# Ideas - Create: Solarpunk

A running list of possible future features. Nothing here is committed. Grouped by theme,
with the strongest / most on-theme ideas near the top of each section.

## Next up: 2.3, Oxygen

1. **Oxygen uses** (the headline): the Electrolyzer and the Bioreactor both make it, but only
   the Fuel Cell uses it, so most Bioreactor Oxygen vents. Oxy-fuel burning in the Biomass
   Gasifier and Biofuel Engine, and the aerobic Rich Fertilizer mode in the Fermentation Vat.
   See Oxygen uses under Biofuel and farming.
2. **Small wins**: the wire throughput Display Link, and optionally the Electric Sprinkler.

## After that: 2.4, a new generator

Tidal power or the ion wind turbine (FE, boosted by rain and storms, covering solar's weak
spots). See Biggest gaps.

## Biggest gaps in the renewable lineup

- **Hydrogen extras** (the Electrolyzer and Fuel Cell stack are built): Oxygen, shared with
  the Bioreactor (see Oxygen uses under Biofuel and farming).
- **Thermal Generator** - Molten Salt heat to FE.
- **Seebeck generator** - a solid-state thermoelectric generator that turns hot water into a
  trickle of FE from the temperature difference, with no moving parts. Puts waste heat to use,
  like the Steam Turbine's condensed water. There's no hot water fluid yet, so the turbine (and
  maybe the Crystallizer) would need to give out Hot Water instead of plain Water, which cools
  back to Water as it passes through.
- **FE battery** - skipped: the Hydrogen chain is the FE storage.
- **Tidal / wave power** - SU from ocean water levels, changing with the time of day, so it's
  different from Create's water wheel.
- **Ion wind turbine** (a real EWICON: electrostatic wind energy converter) - a solid-state
  FE generator with no moving parts, the wind counterpart to the Brass Solar Panel, since
  Create's windmill only gives SU. Sprays charged water droplets that the wind carries off,
  so it uses a trickle of piped Water. Small, unsteady output: more with height above sea
  level, needs open air around it (count air blocks in a radius, with a penalty for packing
  them close together), and rain and thunder boost it, which covers solar's night gap.
- **Ion thruster** - skipped, like the Oxygen thrusters: thrust is Aeronautics' job, and an
  FE fan would only duplicate an Electric Motor on an Encased Fan. If ever, only as optional
  Aeronautics compat.
- **Dedicated grid-scale storage** beyond Heat Battery / Kinetic Battery - a gravity battery
  that hoists a weighted block on surplus power. (Flywheel storage is largely what the Kinetic
  Battery already does.)

## More uses for FE

Right now FE mostly just powers lights and Grow Lamps, so Brass Panels, Shingles and Solar
Glass have little to feed. The Electric Motor (2.2) gives FE a big job; these come after.

- **Wire throughput readout** - connectors already count FE/s moved; a Display Link source on
  connectors would turn wires into power meters.
- **Electric Sprinkler** - an FE-powered Kinetic Sprinkler, so a greenhouse can run entirely
  on its own Solar Glass roof.
- **Electric Heater** - FE heats a Create boiler (passive heat at low FE/t, superheat at
  high). Competes with the Heat Battery, so only if it's clearly more expensive per heat.

## Solar chain depth

- **Visible output scaling** - goggle readout of current vs peak output, with rain/snow/night
  penalties shown explicitly.
- **Parabolic trough** - a linear mid-tier between the Solar Heater and the Solar Power Tower.
  Overlaps both, so low priority.
- **Stirling dish concentrator** - direct SU/FE from a focused dish, no molten salt loop.
  Overlaps the Solar Heater and tower, so low priority.

## Biofuel and farming

- **Oxygen uses** - from the Electrolyzer and the Bioreactor.
  - Aerobic treatment in the Fermentation Vat: speeds up Rich Fertilizer only. That's
    composting, which really is aerobic; biofuel (ethanol) fermentation is anaerobic and
    Oxygen would hurt it, so the Vat gets a real choice of which mode to run.
  - Oxy-fuel burning: pipe Oxygen into the Biomass Gasifier or Biofuel Engine for more SU per
    unit of fuel. Oxygen isn't a fuel itself, it boosts one. Not the Firebox Boiler: it stays
    a simple early-game Steam source.
  - Space compat: if Northstar or Cosmonautics has an Oxygen fluid for life support, let
    ours count as it, for algae-powered space stations (cheap, like `NorthstarCompat`).
  - Skipped for now: Liquid Oxygen (needs a cooling machine and a second fluid for the same
    gas) and thrusters (Aeronautics and the space mods' job, not a power mod's).

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

Pure decoration drifts from the mod's power focus, so the best ideas here also do something
small: make power, absorb pollution, give light, or use up a byproduct. Blocks that are only
decorative should at least come from the mod's own materials (Salt, Biochar, Ash, Lithium).

## Seasonal events

Already in: model and texture swaps (`client/SeasonalModels`), holiday colours for geyser steam,
Trellis flowers and the Garden Light's glow (`util/SeasonalColors`), and Christmas lights on
wires. What's left needs new art.

- **Easter** - little decorated eggs hidden on the Trellis. Rare chance of an egg item popping
  out of a harvested crop.
- **Halloween** - Garden Lights become carved pumpkin lanterns (the orange glow is done) that
  flicker, cobwebs on the railings, bats perched on the solar panels.
- **St. Patrick's Day** - clovers on the Trellis.
- **April Fools' (Apr 1)** - a chaos pick. Solar panels show a moon or a smiley face, Garden
  Lights look upside down, or the geyser cap has googly eyes.
- **Earth Day (Apr 22)** - solar panels get a globe or leaf pattern, and moss or flowers grow
  over Rammed Earth.
- **Day of the Dead** - papel picado (cut-paper bunting) strung on the railings.
- **Pride Month** - rainbow ribbons on the railings. Garden Lights that slowly cycle through
  the rainbow (they're a fixed colour per block now; cycling needs a renderer). Other pride
  flags (trans, bi, etc.) as random variants per block.
- **New Year's** - confetti on the solar panels.
