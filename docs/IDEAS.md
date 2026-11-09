# Ideas - Create: Solarpunk

A running list of possible future features. Nothing here is committed. Grouped by theme,
with the strongest / most on-theme ideas near the top of each section.

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

### Living buildings

- **Planter Box** - a wooden or brick box that holds dirt so crops and flowers grow on roofs
  and balconies. A Kinetic Sprinkler in range waters it like farmland.

### Machine-themed decor

- **More filigree railing materials** (iron) - only a texture and a few lines each.

## Seasonal events

Purely cosmetic texture swaps for part of the year. Easter, Halloween, Christmas and Pride are
the big ones worth doing first.

- **Easter (date moves each year)** - pastel-painted solar panel cells, little decorated eggs
  hidden on the Trellis, and pastel Garden Lights. Rare chance of an egg item popping out of a
  harvested crop.
- **Halloween (Oct 31, or the last week of October)** - Garden Lights become carved pumpkin
  lanterns with a flickering orange glow, cobwebs on the railings, bats perched on the solar
  panels, and green or purple "spooky" steam from the geyser.
- **Christmas (December)** - string lights wound around the Trellis and railings, snow caps on
  solar panels and Shingles, and a little Santa hat on the geyser cap.
- **Valentine's Day (Feb 14)** - Garden Lights glow pink or red, the Trellis gets little
  heart-shaped flowers, and the geyser puffs pink steam.
- **St. Patrick's Day (Mar 17)** - solar panel cells turn green, clovers show up on the
  Trellis, and the Garden Light glows green.
- **April Fools' (Apr 1)** - a chaos pick. Solar panels show a moon or a smiley face, Garden
  Lights look upside down, or the geyser cap has googly eyes.
- **Earth Day (Apr 22)** - solar panels get a globe or leaf pattern, and moss or flowers grow
  over Rammed Earth.
- **Day of the Dead (Nov 1-2)** - marigolds on the Trellis and papel picado (cut-paper bunting)
  strung on the railings. Could be folded into Halloween instead.
- **Pride Month (June)** - rainbow-striped solar panel cells, Garden Lights that slowly cycle
  through rainbow colors, rainbow ribbons on the railings, and rainbow steam from the geyser.
  Could use other pride flags (trans, bi, etc.) as random variants per block.
- **New Year's Eve (Dec 31 - Jan 1)** - gold and silver Garden Lights, confetti on the solar
  panels, and sparkler particles from the geyser.

## Compat

- **Ad Astra** - strong solar in orbit, weak on Mars, no wind on airless bodies. Space support
  already works through the `solarpunk:always_sunny` / `solarpunk:no_weather` dimension type
  tags, so this may only need tagging (check Ad Astra exists for 1.21.1 NeoForge first).
