# Ideas - Create: Solarpunk

A running list of possible future features. Nothing here is committed. Grouped by theme,
with the strongest / most on-theme ideas near the top of each section.

## Next up: 2.3, Oxygen and Geothermal

1. **Geothermal** (the headline now that Oxygen is built): nothing new since the Geyser Cap. A vent should be
   worth more once you find it, not let geothermal work anywhere. In this order (details
   under Geothermal):
   - **Geothermal Well**: the Steam option for a vent, for the Steam Turbine. Only on a
     natural Geyser Vent, like the Cap.
   - **Geothermal Brine**: Lithium from the Well, tying geothermal to fusion.
   - **Nether vents**: stronger vents in Basalt Deltas, worth the danger.
2. **Small wins**: the wire throughput Display Link.
3. **KubeJS integration**, mirroring what KubeJS Create does for Create. KubeJS Create
   (2101.3.x) is a `KubeJSPlugin` (listed in `kubejs.plugins.txt`) that adds four things;
   Solarpunk could do the same in a `compat/kubejs/` package, compiled against KubeJS
   compile-only and only loaded when it's installed:
   - **Recipe schemas**: nicer script syntax for every recipe type (`.heated()`,
     `.processingTime()`, chance outputs). KubeJS 2101 loads these from plain JSON at
     `data/<namespace>/kubejs/recipe_schema/<type>.json`, so this part needs no Java. For us:
     `event.recipes.solarpunk.solar_heating/crystallizing/particle_accelerator(...)` with
     `.processingTime()`, `.byproduct()`, `.minDiameter()`, `.minSpeed()`, `.stress()`.
     Hardcoded processes (fermentation, water evaporation, tower output) could become recipe
     types first so they get schemas too.
   - **Custom item/block types** for `StartupEvents.registry`: KubeJS Create adds
     `create:sequenced_assembly` and `create:sandpaper` item types. For us, pack makers making
     their own machines from ours: `solarpunk:solar_panel` (SU or FE, custom output),
     `solarpunk:turbine_blade`, `solarpunk:reactor_blanket` (output/fuel weights),
     `solarpunk:wire_tier` (custom spools with their own FE/t cap). Blocks with block entities
     need our BE types to accept any block of the right class.
   - **Startup events**, like `CreateEvents.boilerHeatHandler`/`pipeFluidEffect`/
     `spoutHandler`. For us, a `SolarpunkEvents` group: turbine fuels (fluid -> steam value),
     Biomass Gasifier and Biofuel Engine fuels, Heat Battery heat values, pollution sources
     and absorbers, Greenhouse/Grow Lamp plant rules, geyser biomes.
   - **Bindings** for scripts, like `CreateItem`: read the sun (`SolarHelper` phase/sky at a
     position), chunk pollution get/add, and a fusion reactor/turbine state for custom logic.
   Also register our data component types for script autocomplete
   (`registerDataComponentTypeDescriptions`), as KubeJS Create does.

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

## Geothermal

Vents are meant to be scarce, so ideas here should make a natural vent worth more rather than
let geothermal work anywhere.

- **Geothermal Well** (like a real flash steam plant on natural vents) - a second block for a
  natural Geyser Vent, instead of the Geyser Cap. It can't go anywhere else, so it doesn't
  add geothermal spots: it's another way to use a vent you've found. Pipe Water in, Steam
  comes out, for the Steam Turbine. So a vent gives a
  choice: SU straight from the Cap, or Steam from the Well (more total power, but you need a
  turbine and a water supply). Steam rate set so one vent runs a small turbine. Like the Cap,
  it stops eruptions. Ponder scene, and the turbine's condensed water can loop back in.
- **Geothermal Brine** (real: the Salton Sea geothermal plants are now a big lithium source) -
  the Well also gives out a trickle of **Geothermal Brine** with the Steam. The Crystallizer
  turns Brine into Salt plus a little Lithium Dust (a `crystallizing` recipe, no new
  machine). A renewable Lithium source for the Fusion Reactor, and geothermal's first link to
  another chain. Brine has to be piped away or the Well backs up, like the turbine's water.
- **Vent strength** - each vent rolls weak, normal or strong at world gen (saved in the vent's
  block entity), scaling Cap and Well output (e.g. 0.5x / 1x / 2x). Goggles on a vent show
  it, and strong vents puff more steam, so a good vent is worth travelling for. Old vents
  read as normal.
- **Nether vents** - vents in Basalt Deltas, with more output than Overworld vents (a
  multiplier on the Cap and Well, or always strong if vent strength gets built), surrounded
  by lava and magma. High risk, high reward, and a reason to build in the Nether. The Cap works there as
  is; the Well needs Water piped in, which is fine, since Water doesn't evaporate in pipes.
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
