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
  - The problem it solves: Brass Panel FE has nowhere to go beyond each panel's small buffer,
    and solar stops at night. Hydrogen made at noon can run the base after dark.
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
- **Solar Garden Light** - a small post light that charges by day and glows at night, no
  wiring. The cheap early-game cousin of the streetlights, and a nice first use of a
  Photovoltaic Cell.
  - Built (the Garden Light): charges if it sees the sky during the day, glows through the
    night after. No block entity, just a CHARGED block state and a scheduled tick.
- **Solar Shingles** - stair and slab shaped roof panels that make a small amount of FE.
  Lets a roof be the solar array instead of a flat field of panels next to the house.
  - Built: one stair-like block (low and high pieces alternate up a roof, corners bend like
    stairs). Output depends on which way the slope faces (`SolarHelper.tiltFactor`). Vanilla's
    sun moves east to west straight overhead, so east roofs win in the morning and west roofs
    in the evening; north and south roofs are equal. A south bias would need a fake latitude.
  - Each shingle has its own buffer and FE capability, and energy flows to touching shingles
    that hold less, so pulling from any one shingle drains the whole roof through it. No
    Roof Inverter needed.
- **Solar Glass** - a see-through panel that makes much less than a Brass Panel but works as
  a window or skylight. Pairs with the Greenhouse idea: a greenhouse roof that also powers
  its own sprinklers.
  - The block exists as decorative glass (crafted from Glass and a Photovoltaic Cell). Making
    it generate means a block entity and FE capability; its ID can stay the same, so placed
    glass would start working.
  - A Solar Glass Pane exists too; if the glass generates, the pane could as well (less).
- **Solar Canopy / Pergola** - a panel raised on posts, so the space underneath stays usable
  (paths, markets, parking for trains and contraptions). Mostly a model variant; could just be
  a Brass Panel on a post block.
- **Solar Railing / Fence** - a fence with a thin panel on top, for lining bridges and
  balconies. Low output, high charm. Could reuse the filigree railing's connecting and
  placement code.

### Living buildings

- **Green Roof** - a grass or moss slab that absorbs pollution like leaves do. Gives the
  global warming system a building-shaped answer besides trees and the Biofilter.
  - Could slowly spread flowers on top of itself, and dry out to dead grass in heavy
    pollution, so it doubles as a visible pollution meter.
- **Living Wall** - a thin vertical panel of moss and ferns placed on the side of a block.
  Same pollution absorption as the Green Roof, smaller amount.
- **Trellis** - a climbable lattice that vines, glow berries and flowers grow up on their own.
  - Built: Vines first, then a small flower (tinted from the flower's own texture) or Glow
    Berries (light 14) on top; Shears peel off a layer. Vines spread to bare trellises above
    or beside. Flowers and berries don't spread yet, and modded flowers aren't supported.
  - Possible new climbing crop (hops or grapes) that only grows on a trellis and gives a
    better Biomass yield, or ferments faster in the Fermentation Vat.
- **Planter Box** - a wooden or brick box that holds dirt so crops and flowers grow on roofs
  and balconies. A Kinetic Sprinkler in range waters it like farmland.
- **Solar Plant Bed** - a raised bed with a small panel built in, a self-contained garden for
  roofs and balconies. The panel charges by day and powers a grow light at night, so crops in
  the bed keep growing after dark. It also keeps its own soil hydrated, so it needs no water
  nearby.
  - Small on purpose: one bed, one crop, slower than a proper farm. A way to grow food and
    Biomass in a city build without a field.
  - Bigger version of the idea (agrivoltaics, which is real): crops planted under a Solar
    Canopy grow at normal speed in the shade, and the panel gets a small output bonus from
    the plants cooling it. Rewards stacking farms and solar instead of building them side
    by side.
  - Open: does the grow light need the panel's own charge only, or can it take FE from the
    grid so it also works in caves and space stations?

### Natural and reclaimed materials

- **Ashcrete** - a concrete-like block mixed from Ash, Water and Gravel in a Mixer, maybe in
  colours like vanilla concrete. Polished Ash and Ash Bricks already exist; this would be the
  Create-native, mass-produced option.
- **Charred Timber** - dark scorched planks (like Japanese shou sugi ban), made by applying
  Biochar to planks or by item application with a Blaze Burner. A second use for Biochar
  besides Fertilizer.
- **Rammed Earth** - layered earth blocks pressed from Dirt, Clay and Gravel in a Mechanical
  Press, with a few color bands by input (red sand, terracotta). A good Create-native recipe.
- **Bamboo panels and lattice** - bamboo screens and lattice walls; the lattice could share
  the Trellis model.
- **Cork** has no vanilla source, so skip it unless a cork oak tree is added.

### Machine-themed decor

- **Filigree railings and arches** - done: Brass and Andesite Filigree Railings and Railing
  Slopes (gentle half-block slopes) and Steep Slopes (45 degrees, follow stairs when placed on them),
  with Create-style placement arrows.
  - Possible extras: more materials (copper, iron) need only a texture and a few lines each.
- **Lithium / Beryllium blocks with variants** - polished, tiled and pillar versions of the
  existing storage blocks. Polished and tiled (as `cut`) fit the family system; a pillar
  doesn't (no family variant, and it's a rotatable `RotatedPillarBlock`), so it would be
  registered and modelled on its own.

## Compat

- **Ad Astra** - strong solar in orbit, weak on Mars, no wind on airless bodies. Space support
  already works through the `solarpunk:always_sunny` / `solarpunk:no_weather` dimension type
  tags, so this may only need tagging (check Ad Astra exists for 1.21.1 NeoForge first).
