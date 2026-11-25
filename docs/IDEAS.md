# Ideas - Create: Solarpunk

A running list of possible future features. Nothing here is committed. Grouped by theme,
with the strongest / most on-theme ideas near the top of each section.

## Next up: 2.3, Oxygen, Geothermal and Modpack Support

1. **Geothermal** (the headline): the Geyser Well is in. A vent should be worth
   more once you find it, not let geothermal work anywhere. In this order (details under
   Geothermal):
   - **Geothermal Brine**: Lithium from the Well, tying geothermal to fusion.

## After that: 2.4, a new generator

Tidal power or the ion wind turbine (FE, boosted by rain and storms, covering solar's weak
spots). See Biggest gaps.

## Biggest gaps in the renewable lineup

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
- **Dedicated grid-scale storage** beyond Heat Battery / Kinetic Battery - a gravity battery
  that hoists a weighted block on surplus power. (Flywheel storage is largely what the Kinetic
  Battery already does.)

## More uses for FE

Right now FE mostly just powers lights and Grow Lamps, so Brass Panels, Shingles and Solar
Glass have little to feed. The Electric Motor (2.2) gives FE a big job; these come after.

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

## Geothermal

Vents are meant to be scarce, so ideas here should make a natural vent worth more rather than
let geothermal work anywhere.

- **Geothermal Brine** (real: the Salton Sea geothermal plants are now a big lithium source) -
  the Well also gives out a trickle of **Geothermal Brine** with the Steam. The Crystallizer
  turns Brine into Salt plus a little Lithium Dust (a `crystallizing` recipe, no new
  machine). A renewable Lithium source for the Fusion Reactor, and geothermal's first link to
  another chain. Brine has to be piped away or the Well backs up, like the turbine's water.
- **Geyser Collector** - for players who like the eruptions: sits beside an uncapped vent and
  catches each eruption as a big burst of Steam into its tank, keeping the eruptions. Lower
  output than the Well on average, but bursty, so it pairs with storage. Lower priority.
- **Geothermal greenhouse heating** (real: Iceland grows bananas this way) - a Greenhouse
  with a capped vent or Well inside, or Steam piped to its controller, grows faster or lets
  crops grow in cold biomes. Ties into the Greenhouse.
- **Hot Water** - the Well's spent water could come out as Hot Water for the Seebeck generator
  (see Biggest gaps), if that gets built.
- Skipped: ground-source heat pumps and geothermal that works anywhere, since vents are meant
  to be scarce. A vent heating a Create boiler directly competes with the Heat Battery.

## Modpack support

KubeJS recipes and data-driven fuels are in (2.3). What's left:

- **Shorter script syntax** - a `kubejs/recipe_mappings.json` so scripts can write
  `event.solarpunkFermenting(...)` instead of `event.recipes.solarpunk.fermenting(...)` (keep the
  mod prefix: mapping names are shared with every mod), and `.aerobic()` as a no-argument switch
  like Create's `.heated()` instead of `.aerobic(true)`. Mapping support would go in the
  library's `KubeRecipeSchemaProvider` so other addons get it too.
- **More recipe types** - Solar Power Tower output is still hardcoded, so modpacks can't change
  it.
- **Pollution sources as a data map** - they're a tag plus a config list now; a data map on
  block entity types would let datapacks and KubeJS set how much each source pollutes, like
  the fuels.

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

- **Abandoned solarpunk village** - a run-down, overgrown settlement that tried and failed,
  showing what global warming does without anyone having it turned on. Built from pieces in
  `structure/abandoned_village/`; so far the Lost Greenhouse (placed sunk 1 block into the
  ground). Wire it up as a jigsaw structure once there are enough pieces.

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
