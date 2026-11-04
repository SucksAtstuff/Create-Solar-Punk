# Ideas - Create: Solarpunk

A running list of possible future features. Nothing here is committed. Grouped by theme,
with the strongest / most on-theme ideas near the top of each section.

## Next Up (2.1 Shortlist)

In rough priority order.

1. **Anaerobic digester** - turns waste (rotten flesh, spider eyes, poisonous potatoes, spare
   crops) and water into a **stronger Fertilizer**, for the Kinetic Sprinkler and the
   Greenhouse. Fits the "nothing is wasted" theme of the Particle Accelerator's Leather
   recipe.
   - Not a fuel: the Fermentation Vat already turns organic matter into Biofuel, so a second
     burnable fluid would only duplicate it.
   - Biogas could come back later as a byproduct if something needs it, such as a route to
     Hydrogen for the hydrogen chain.
2. **Grid-powered streetlights** that switch on at dusk. A visible everyday use for FE.

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

- **Anaerobic digester** - see Next Up.
- **Algae bioreactor** - light + water + CO2 to biomass, an alternative to farming. Could use the
  Biofilter's Carbon Dust.
- **Greenhouse** - a new **Greenhouse Glass** block and a **Greenhouse Controller**. Any shape
  works (a big dome, or just a box around the farm) as long as the glass fully seals the crops
  in; the ground can be any solid block. Crops inside grow faster, more with a Kinetic
  Sprinkler and Fertilizer. A proper home for Biomass farms.
  - The Controller is required: one placed inside makes the space a greenhouse, and without
    one the glass is just glass. It runs the detection, applies the growth boost, and shows
    goggle info (sealed or not, volume, crop count, current boost).
  - Detection: the Controller flood fills the air inside every few seconds, capped at a max
    volume, so crops never have to check anything themselves.
  - No power: the Controller runs on its own, like a real greenhouse that only needs its
    glass. Powered add-ons (grow lights and the like) take their own power if they're added.
  - One Controller per greenhouse: a second one in the same space does nothing, so boosts
    never stack. Its goggle info should say another Controller already runs this greenhouse.
  - Possible extras: crops inside are safe from pollution decay and wasteland biomes, and
    grow whatever the outside biome or season.
  - Pollination: vanilla Beehives or Bee Nests inside the greenhouse (with bees) give the
    crops a further growth boost. No new block needed, and the flood fill can count them
    while it scans.
  - Solar Glass could count as greenhouse glass too, so the roof also makes FE.
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
- **Rammed Earth extras** - follow-ups for the existing sets.
  - Polished Rammed Earth: one more light grey texture, then raw -> polished -> bricks like
    Salt and Ash (`bricksRecipes()` already takes a polished step).
  - A plain top texture: every face currently shows the stripes, but the layers should only
    show on the sides. A tinted `cube_column` parent would do it.
  - A Tuff colour was skipped because it tints almost the same as Ashen; it would need a
    greener tint than real Tuff.

### Machine-themed decor

- **More filigree railing materials** (copper, iron) - only a texture and a few lines each.

## Compat

- **Ad Astra** - strong solar in orbit, weak on Mars, no wind on airless bodies. Space support
  already works through the `solarpunk:always_sunny` / `solarpunk:no_weather` dimension type
  tags, so this may only need tagging (check Ad Astra exists for 1.21.1 NeoForge first).
