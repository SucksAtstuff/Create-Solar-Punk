# Ideas - Create: Solarpunk

A running list of possible future features. Nothing here is committed. Grouped by theme,
with the strongest / most on-theme ideas near the top of each section.

## Next up: 2.2

### 1. Christmas art, if 2.2 ships in December

The code side is done (Streetlamp, Shingles, Garden Light, wire lights). Still needs art:

- String lights wound around the Trellis.
- Snow caps on the solar panels.
- A little Santa hat on the Geyser Cap.

Saved for after 2.2: the Hydrogen chain and FE storage. They're the biggest gaps but a much
bigger job, and now that the Electric Motor gives stored FE somewhere to go they're next.

## Biggest gaps in the renewable lineup

- **Fuel Cell Stack** - turns the Electrolyzer's Hydrogen back into SU or FE. Gives solar FE
  a storage use. Northstar's Liquid Hydrogen (which the Steam Turbine already burns) is a
  separate fluid; a cooler could turn our Hydrogen into it.
  - Built like a real one: a stack of **Fuel Cell** blocks in a row or column, joined like the
    Fermentation Vat or Create's tanks (`MultiBlockFluidBE` / `ConnectivityHandler`). Each
    cell adds output and fuel use, so a stack grows with the base instead of needing tiers.
  - **End Plates** clamp the stack at both ends and are where it connects: Hydrogen and
    Oxygen pipe in, Water and FE come out through them. A stack only runs with an End Plate
    on each end. Cells have no connections of their own, so the plates are what you pipe to.
  - Open: a max stack length, or let it run as long as you like?
  - The problem it solves: solar FE has nowhere to go beyond each block's small buffer, and
    solar stops at night. Hydrogen made at noon can run the base after dark.
  - Better than a plain battery: storage is only limited by tank space, and Hydrogen can be
    piped across the base or carried on trains and contraptions, so a desert solar field can
    ship fuel home.
  - Lossy on purpose: getting back maybe 60-70% of the FE put in makes it a trade-off, so
    using solar directly stays worth it.
  - The Fuel Cell's only waste is Water, which could be piped back into the Electrolyzer.
  - Open: Fuel Cell output FE, SU, or switchable like the Solar Power Tower's modes?
  - Oxygen, shared with the Bioreactor (see Biofuel and farming): the Fuel
    Cell could run on Hydrogen alone but give a bonus with Oxygen, the real clean-fuel pair.
  - Open: could the Electrolyzer give a little Deuterium as a rare byproduct for the Fusion
    Reactor?
- **FE storage** - FE only exists in the small buffers of the Brass Panel, Solar Shingles and
  Solar Glass right now. A Thermal Generator (Molten Salt heat to FE) or a large battery
  multiblock.
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
  - Open: a fixed 1x1x2 small version as well, or only the multiblock?
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
