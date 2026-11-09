# Ideas - Create: Solarpunk

A running list of possible future features. Nothing here is committed. Grouped by theme,
with the strongest / most on-theme ideas near the top of each section.

## Next up: 2.2

In order. Build the Switch first, since the Relay is the same block with redstone. 2.1 added
wires, Shingles, Solar Glass and Grow Lamps, but FE still only runs lights; these give it
jobs. The Relay pairs with the new solar panel and Biofilter comparator signals.

How wires work today, since the first three build on it: wire networks hold no energy. A Wire
Connector passes FE straight from the block it sits on to the blocks under every other
connector it can reach (`WireConnectorBlockEntity.route()`), round-robin, capped per wire by
its tier (Iron 256, Copper 1024, Gold 4096 FE/t). Reachability is a BFS cached per connector
until the global `WireNetwork.topologyVersion()` changes, which happens on any wire change.

Reference numbers (defaults): Brass Panel 40/80 FE/t (dawn/noon), Solar Shingle 4/8 FE/t,
Solar Glass 2/4 FE/t; Streetlamp uses 2 FE/t, Grow Lamp 8 FE/t. Andesite Panel 128/256 SU at
16 RPM.

### 1. Wire Switch

A lever-like block wires attach to. Flip it to cut everything on one side off from the other:
a street of lights, a greenhouse, a whole wing of the base.

- **Shape:** one block with one wire point (like a Wire Connector with no block behind it),
  not two points. Every wire on it joins when it's closed; when it's open, nothing passes
  through it, so each wire becomes its own island. That covers "split this network in two"
  without asking players which side is which, and wires stay hooked up either way.
- **Behaviour:** right-click toggles, like a Lever, with a click sound and a visible lever arm
  or blade. The state shows in goggles ("Closed" / "Open") and the model.
- **What happens when it opens:** there's no stored energy in a network to split, so it's just
  a gap in the path. Sources on the cut-off side stop sending to the far side on the next
  tick; anything with a buffer (Streetlamps, Grow Lamps) runs on what it has.
- **Code:** a `WireSwitchBlockEntity` that holds `Connection`s like a connector but has no
  attached block. `WireNetwork.findEndpoints()` walks through it only while it's closed, and
  never lists it as an endpoint. Toggling calls `WireNetwork.changed()`.
- **Recipe:** Lever + Wire Connector + Andesite Casing (shapeless or Create item
  application).

### 2. Redstone Wire Relay

The same thing switched by redstone, so Daylight Sensors, the new solar panel and Biofilter
comparator signals, or Create's Redstone Links can switch power automatically. Examples:
streetlights that turn on at dusk; Grow Lamps that only run while panels are strong; cutting
a far wing off when a tank is full.

- **Separate block, not a mode on the Switch.** A manual switch that redstone can also flip
  makes "why is this open?" hard to answer. The Relay is closed while powered; a Wrench
  inverts it (open while powered), shown on the model like Create's Redstone Link
  transmit/receive toggle.
- **Redstone clocks:** a toggle bumps the global topology version, so every connector in the
  world searches its network again. A relay on a fast clock would do that every few ticks.
  Either rate-limit the relay (it follows the signal at most every 4-10 ticks), or give
  networks their own version so a toggle only invalidates the connectors it can reach. The
  rate limit is far less work and nobody needs FE switched faster than that.
- **Comparator output:** reads 15 while closed, 0 while open, so Relays can drive each other or
  a Display Link can show state.
- **Recipe:** Wire Switch + Redstone Torch, or Wire Switch + Electron Tube to match Create's
  redstone parts.

### 3. Electric Motor

Turns FE back into SU, so solar FE can drive Create machines, and wires can carry rotational
power across a base without long shaft lines.

- **Balance target:** a Brass Panel feeding a Motor gives half what an Andesite Panel placed
  there would. The Andesite Panel stays the cheap, direct choice; the Brass Panel + wires +
  Motor route pays off in scale (a whole roof of Shingles into one Motor) and reach (power
  generated on a roof, used in a basement).
- **Proposed numbers** (all config):
  - Speed set with a scroll value like Create's Creative Motor, 1 to 64 RPM, either direction.
  - Stress capacity 8 SU per RPM, so 128 SU at 16 RPM and 512 SU at 64 RPM.
  - FE use = SU / 2: 64 FE/t at 16 RPM (128 SU), 256 FE/t at 64 RPM (512 SU).
  - Check: one Brass Panel at noon (80 FE/t) runs a Motor at 16 RPM for 128 SU, half an
    Andesite Panel's 256 SU. About 32 Solar Shingles at noon (8 FE/t each, more on a
    sun-facing roof) run one at full speed. Iron wire (256 FE/t)
    carries exactly one full-speed Motor; Gold carries 16.
- **FE use follows speed, not load.** Create doesn't tell a generator how much of its capacity
  is actually being used, and charging for the full capacity keeps it predictable and
  impossible to exploit. A slower setting is how players save FE. (Could later scale by the
  network's stress / capacity ratio, but that's harder to read in goggles.)
- **Running out of FE:** a buffer of about one second of use (20 x the FE/t). It runs while the
  buffer has a tick's worth, stops when it runs dry, and only restarts once the buffer is back
  to half. Without that gap a Motor on a weak supply would start and stop every tick, which
  re-solves the whole Create network each time. Stopping clears `LIT`, like the other
  generators.
- **Night:** with no FE storage yet, Motors on solar stop at sunset, the same as the Andesite
  Panel. That's fine for 2.2 and is exactly what the Hydrogen chain and FE storage fix later.
  A Kinetic Battery behind the Motor already bridges the night.
- **Code:** extends `GeneratingKineticBlockEntity` like the other generators (set
  `lastCapacityProvided` in `calculateAddedStressCapacity()`), with a receive-only FE
  capability on its back face so a Wire Connector or a Brass Panel can feed it directly.
  `BlockStressValues` entries for tooltips, goggle lines (FE/t in, buffer, SU out), a Display
  Link source (FE/t), a looping hum through `MachineSoundInstance`, and a Ponder scene
  (Brass Panel -> connector -> wire -> connector -> Motor -> machine).
- **Recipe:** Brass Casing + Shaft + Electron Tube + Copper Wire (Create mechanical crafting,
  or sequenced assembly to make it feel mid-game).
- **Overlap with Create: New Age and Create Crafts & Additions:** both have their own electric
  motors. Keep ours anyway: it's tuned to solar FE, matches the mod's look, and players with
  those mods can use whichever they like. Their FE works with our Motor and vice versa, since
  it's all NeoForge FE. Just don't make ours the strongest option.

### 4. Christmas art, if 2.2 ships in December

The code side is done (Streetlamp, Shingles, Garden Light, wire lights). Still needs art:

- String lights wound around the Trellis and railings.
- Snow caps on the solar panels.
- A little Santa hat on the Geyser Cap.

Saved for after 2.2: the Hydrogen chain and FE storage. They're the biggest gaps but a much
bigger job, and work best once the Electric Motor gives stored FE somewhere to go.

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

## More uses for FE

Right now FE mostly just powers lights and Grow Lamps, so Brass Panels, Shingles and Solar
Glass have little to feed. The Wire Switch, Redstone Wire Relay and Electric Motor are planned
for 2.2 (see "Next up"); these come after.

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
