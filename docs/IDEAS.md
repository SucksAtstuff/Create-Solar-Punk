# Ideas - Create: Solarpunk

A running list of possible future features. Nothing here is committed. Grouped by theme,
with the strongest / most on-theme ideas near the top of each section.

## Next up: 2.2

### 1. Christmas art, if 2.2 ships in December

The code side is done (Streetlamp, Shingles, Garden Light, wire lights). Still needs art:

- String lights wound around the Trellis.
- Snow caps on the solar panels.
- A little Santa hat on the Geyser Cap.

Saved for after 2.2: a simple FE battery, the last big gap now that the Hydrogen chain
gives long-term FE storage.

## Biggest gaps in the renewable lineup

- **Hydrogen extras** (the Electrolyzer and Fuel Cell stack are built): Oxygen, shared with
  the Bioreactor (see Biofuel and farming).
- **FE battery** - the Electrolyzer and Fuel Cell stack store FE as gas, but lose about a
  third and need plumbing. A plain battery to fill and drain directly with no loss would be the
  cheap early-game store, with hydrogen as the big long-term one. Maybe a large multiblock
  later. A Thermal Generator (Molten Salt heat to FE) is another way in.
- **Tidal / wave power** - SU from ocean water levels, changing with the time of day, so it's
  different from Create's water wheel.
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

- **Bioreactor** - a glass tank of green Algae Culture that turns Water and sunlight into
  Biomass and Oxygen. Biomass from sunlight instead of crushing plants, and the Oxygen gives
  the mod's first gas byproduct somewhere to go.
  - Built like the Vat and Tower (`MultiBlockFluidBE`, `TankModel` connected textures), with
    glass casing so Create's fluid rendering shows the culture inside, and bubble particles
    rising while it's `LIT`. Sits on a **Bioreactor Base** block (aerator, item output for
    hoppers and funnels), so the bottom layer doesn't need special rules.
  - Needs open sky at the top layer (through `SolarHelper`, so space, seasons and Sable keep
    working). Output scales with top surface area, so wide beats tall. A Grow Lamp beside it
    could stand in for the sun at night.
  - Fertilizer or Rich Fertilizer as optional nutrients for a speed boost, closing the loop:
    biochar -> fertilizer -> algae -> biomass -> biochar.
  - Absorbs pollution in its chunk like the Biofilter (real photobioreactors are built
    around eating CO2).
  - Multiblock only, max 3 wide and 8 tall, seeded with Kelp (see docs/BIOREACTOR.md).
- **Oxygen uses** - from the Electrolyzer, and later the Bioreactor.
  - Aerobic treatment in the Fermentation Vat: speeds up Rich Fertilizer only. That's
    composting, which really is aerobic; biofuel (ethanol) fermentation is anaerobic and
    Oxygen would hurt it, so the Vat gets a real choice of which mode to run.
  - Oxy-fuel burning: pipe Oxygen into the Firebox Boiler, Biomass Gasifier or Biofuel
    Engine for more SU or Steam per unit of fuel. Oxygen isn't a fuel itself, it boosts one.
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
