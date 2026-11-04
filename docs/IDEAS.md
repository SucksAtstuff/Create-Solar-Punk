# Ideas - Create: Solarpunk

A running list of possible future features. Nothing here is committed. Grouped by theme,
with the strongest / most on-theme ideas near the top of each section.

## Next Up (2.1 Shortlist)

In rough priority order.

1. **Grid-powered streetlights** that switch on at dusk. A visible everyday use for FE.

## Biggest gaps in the renewable lineup

- **Hydrogen chain** - an Electrolyzer splits Water into Hydrogen using FE from Brass Panels,
  and a Fuel Cell turns it back into SU or FE. Gives solar FE a storage use, and ties into
  Northstar's Liquid Hydrogen, which the Steam Turbine already burns.
  - The problem it solves: solar FE has nowhere to go beyond each block's small buffer, and
    solar stops at night. Hydrogen made at noon can run the base after dark.
  - Better than a plain battery: storage is only limited by tank space, and Hydrogen can be
    piped across the base or carried on trains and contraptions, so a desert solar field can
    ship fuel home.
  - Lossy on purpose: getting back maybe 60-70% of the FE put in makes it a trade-off, so
    using solar directly stays worth it.
  - The Fuel Cell's only waste is Water, which could be piped back into the Electrolyzer.
  - Open: Fuel Cell output FE, SU, or switchable like the Solar Power Tower's modes?
  - Open: its own Hydrogen fluid, or reuse Northstar's when installed (like `NorthstarCompat`)?
  - Open: is Oxygen a byproduct, and does anything use it, or is it just vented?
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

## Solar chain depth

- **Visible output scaling** - goggle readout of current vs peak output, with rain/snow/night
  penalties shown explicitly.
- **Parabolic trough** - a linear mid-tier between the Solar Heater and the Solar Power Tower.
  Overlaps both, so low priority.
- **Stirling dish concentrator** - direct SU/FE from a focused dish, no molten salt loop.
  Overlaps the Solar Heater and tower, so low priority.

## Biofuel expansion

- **Algae bioreactor** - light + water + CO2 to biomass, an alternative to farming. Could use the
  Biofilter's Carbon Dust.
- **Greenhouse extras** - the Greenhouse Controller works; possible additions:
  - Plants inside keep their normal biome (the chunk still turns into the dead biome, only
    decay is blocked), and grow whatever the outside season.
  - Powered add-ons such as grow lights, taking their own power.
  - A Display Link readout of the boost and plant count.

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

### Solar you can build with

- **Grid-powered streetlights** - see Next Up.

### Living buildings

- **Green Roof** - a grass or moss slab that absorbs pollution like leaves do. Gives the
  global warming system a roof-shaped answer besides trees, trellises and the Biofilter.
  - Could slowly spread flowers on top of itself, and dry out to dead grass in heavy
    pollution, so it doubles as a visible pollution meter.
- **Planter Box** - a wooden or brick box that holds dirt so crops and flowers grow on roofs
  and balconies. A Kinetic Sprinkler in range waters it like farmland.
- **Solar Plant Bed** - a raised bed with a small panel built in, a self-contained garden for
  roofs and balconies. The panel charges by day and powers a grow light at night, so crops in
  the bed keep growing after dark. It also keeps its own soil hydrated, so it needs no water
  nearby.
  - Small on purpose: one bed, one crop, slower than a proper farm. A way to grow food and
    Biomass in a city build without a field.
  - Bigger version of the idea (agrivoltaics, which is real): crops planted under raised
    solar panels grow at normal speed in the shade, and the panel gets a small output bonus from
    the plants cooling it. Rewards stacking farms and solar instead of building them side
    by side.
  - Open: does the grow light need the panel's own charge only, or can it take FE from the
    grid so it also works in caves and space stations?

### Natural and reclaimed materials

- **Charred Timber** - dark scorched planks (like Japanese shou sugi ban), made by applying
  Biochar to planks or by item application with a Blaze Burner. A second use for Biochar
  besides Fertilizer.
- **Tuff Rammed Earth** - skipped because Tuff tints almost the same as Ashen; it would need a
  greener tint than real Tuff.

### Machine-themed decor

- **More filigree railing materials** (iron) - only a texture and a few lines each.

## Compat

- **Ad Astra** - strong solar in orbit, weak on Mars, no wind on airless bodies. Space support
  already works through the `solarpunk:always_sunny` / `solarpunk:no_weather` dimension type
  tags, so this may only need tagging (check Ad Astra exists for 1.21.1 NeoForge first).
