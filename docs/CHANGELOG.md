# 2.2.0-1.21.1

## New

### Wire Switches

- **Wire Switch** - a knife switch with two posts. Wire one side of a network to each post, then flip it to cut power to a street of lights, a greenhouse or a whole wing of your base.
- **Redstone Wire Relay** - a Wire Switch thrown by redstone: closed while powered, or open while powered once you hit it with a Wrench. Pair it with a Daylight Sensor or a solar panel's Comparator signal.

### Electric Motor

- **Electric Motor** - turns FE back into rotation, so a roof of solar panels can drive Create machines anywhere your wires reach. Set its speed up to 64 RPM; faster uses more FE, and the speed picker shows the cost of each speed. If your supply can't keep up, Goggles show the fastest speed it can hold.

### Hydrogen

- **Electrolyzer** - splits Water into **Hydrogen** and **Oxygen** with FE, making twice as much Hydrogen as Oxygen. Pipe Water into the top, and each gas out of the copper flange beside its glass tube, where you can watch it collect.
- **Fuel Cell** and **End Plate** - burn Hydrogen and Oxygen back into FE, so noon sunshine can run your base at night. Line up Fuel Cells with an End Plate at each end, and pipe Hydrogen into one plate and Oxygen into the other: every cell adds output, up to 16. Its only waste is Water, ready to pipe back into the Electrolyzer.

### Comparators and Display Links

- A Comparator reads Andesite and Brass Solar Panels: 0 while they aren't generating, otherwise 1-15 by how directly they face the sun. The new **Sun Exposure** Display Link readout shows the same as a percentage.
- A Comparator reads the Biofilter: 0 in clean air, rising with its chunk's pollution up to 15 at the biome decay threshold. New Display Link readouts: **Chunk Pollution** and **Pollution Level** (as a percentage of the decay threshold).
- A Comparator reads the Greenhouse Controller: 0 while it isn't sealed, otherwise stronger the faster plants grow (10 by default, 15 with bees inside).
- A Comparator reads 15 from a closed Wire Switch or Redstone Wire Relay, and 0 while it's open.
- New Display Link readout: **Electric Motor FE Use**.

### Compatibility

- **Create: CROWNS** - the Steam Turbine can burn steam from CROWNS reactors.

### Holidays

- **Christmas** (December): Streetlamps and Solar Shingles dress up, and wires turn into strings of glowing Christmas lights.
- **New Year's** (Dec 31 - Jan 1): Solar Shingles get a festive look, Garden Lights glow gold and silver, and geysers throw sparks.
- **Valentine's Day**: heart-shaped Trellis flowers, pink and red Garden Lights, and pink geyser steam.
- **St. Patrick's Day**: green solar panels, Garden Lights and geyser steam.
- **Easter** (Good Friday to Easter Monday): pastel solar panels, Garden Lights, Trellis flowers and geyser steam.
- **Halloween** (last week of October): orange Garden Lights, orange and purple Trellis flowers, and green and purple geyser steam.
- **Day of the Dead** (Nov 1-2): marigold-orange Trellis flowers.
- **Pride Month** (June): rainbow solar panels, Garden Lights, Trellis flowers and geyser steam.

## Changes

- Steam is now a gas: in a Create Fluid Tank it fills from the top down instead of pooling at the bottom.

## Fixes

- Fixed Kinetic Batteries making free power. Charging now puts stress on the network, the same SU it gives back when discharging, so batteries can no longer charge each other in a loop forever. Faster networks charge them faster.
- Fixed the Biofuel Engine running forever on a small amount of leftover fuel. It now stops once it doesn't have enough Biofuel for the next burn.

# 2.1.0-1.21.1

## New

### Erupting Geysers

- Uncapped Geyser Vents now erupt every few minutes. They rumble for a few seconds first, then blast a column of steam that scalds and launches anything standing over them.
- Goggles on a vent show a countdown to the next eruption, and a Comparator signal rises as it gets close.
- A Geyser Cap stops the eruptions. Everything is configurable under `[geyser_vent]`, and eruptions can be turned off.

### Carbon Capture

- With global warming on, the Biofilter now collects **Carbon Dust** from the pollution it cleans up. Take it out by hand or with a funnel.
- Compact 4 Carbon Dust into Biochar with a Mechanical Press and Basin, for Fertilizer or the Particle Accelerator.

### Rich Fertilizer

- Put mob and farm waste (Rotten Flesh, Spider Eyes, Poisonous Potatoes, Bones, Pufferfish) in a Fermentation Vat with water to brew **Rich Fertilizer**. Plant matter still makes Biofuel.
- Rich Fertilizer in a Kinetic Sprinkler reaches a block further and lasts twice as long as normal Fertilizer.

### Greenhouses

- New **Greenhouse Controller**: place it in the wall of a closed room with a glass roof, facing inside, and plants under the glass grow faster. Bees inside speed them up even more, and with global warming on, a sealed greenhouse is safe from pollution decay. Needs no power, can be turned or picked up with a Wrench, and has a Ponder scene.
- New **Grow Lamp**: an FE-powered purple grow light. Hang it under a ceiling or from a wall, and crops below it grow faster, at night and underground too. A redstone signal switches it off.
- **Agrivoltaics**: an Andesite or Brass Solar Panel raised over crops makes 10% more power, cooled by the plants below. The crops still grow at normal speed in its shade.
- New **Solar Plant Bed**: a raised bed for one plant, for roofs and balconies. Its soil never dries out, so it needs no water, and its little panel charges by day to run a grow light at night that makes the plant grow faster.
- New **Greenhouse Glass** and **Clear Greenhouse Glass**: copper-framed glass for greenhouses and conservatories, each with a pane. Also comes as frosted **Greenhouse Panels** in two patterns, which still let light through.
- When pollution turns a chunk into the dead biome, the inside of a sealed greenhouse keeps its original biome, so grass and leaves in there stay green.
- With Serene Seasons, crops under any greenhouse glass (and Solar Glass) grow in every season.
- New Display Link readouts on the Greenhouse Controller: **Greenhouse Status**, **Greenhouse Plant Count** and **Greenhouse Growth Speed**.

### Wires

- New **Wire Connectors**: small brass insulators that go on any face of any block.
- Cut **Iron**, **Copper** and **Gold Wire** from metal sheets with a Mechanical Saw, and wind 4 onto an Empty Spool. Click two Wire Connectors with a spool to string a wire between them. Longer wires use more of the spool.
- Power going into one connector comes out into the blocks under the other connectors it's wired to, so one solar roof can light a whole street. Generators that don't push power out on their own get drained.
- Gold wire carries the most power and reaches furthest, then Copper, then Iron. Shears cut a connector's wires and give them back.

### New Recipes

- New Particle Accelerator recipes: **Slime Balls** from Biomass, **Beryllium Dust** from Lithium Dust, and **Glowstone Dust** from Redstone and Lithium Dust.
- Mix Carbon Dust or Biochar with Salt in a Mixer to make **Gunpowder**.

### Advancements

- New advancements for the Particle Accelerator: crafting the coils, closing your first ring, your first collision, getting a Tritium Capsule and running a huge ring.
- New Fusion Reactor advancement for breeding your own Tritium.
- New advancement for sealing your first greenhouse.

### Building Blocks

- New **Salt Lamp**: a warm glowing chunk of salt. Right-click to switch it on or off.
- New **Salt Stairs** and **Salt Slab**.
- New **Polished Salt** with stairs, slab and wall.
- New **Salt Bricks** with stairs, slab and wall, plus **Chiseled Salt Bricks**.
- New **Polished Ash** and **Ash Bricks**, each with stairs, slab and wall, plus **Chiseled Ash Bricks**.
- New **Solar Glass** and **Solar Glass Pane**: framed glass that makes a little FE under open sky, for skylights and windows.
- New **Solar Shingles**: stepped solar roofing that makes a little FE, so your roof can be the solar array. With Create Aeronautics installed, roofs sloping toward the sun make more (can be turned off in the config). Also comes in **Solar Glass Shingles**, a glass version.
- New **Rammed Earth** in 25 colours, with stairs, slabs and walls: pack soil, Gravel, Clay and Ash or Biochar together with a Mechanical Press. The soil you use (Dirt, Sand, Red Sand, Mud, Clay, Ash, Salt, Ruined Dirt, or any Terracotta) picks the colour. Every colour also comes as **Rammed Earth Bricks**, with stairs, slab and wall, and **Chiseled Rammed Earth Bricks**.
- New **Garden Light**: a small solar light that charges in the sun during the day and glows through the night.
- New **Streetlamp**: a two-block-tall street light that runs on FE and switches itself on at dusk. Power it with cables or Wire Connectors, or set it on a Brass Solar Panel or Solar Glass.
- New **Charred Timber**: dark scorched planks, made by applying Biochar to any planks, with stairs and slabs. Unlike planks, it doesn't burn.
- New **Moss Stairs** and **Moss Slab**, crafted from Moss Blocks, for green roofs. With global warming on, they soak up pollution like leaves do.
- New **Trellis**: a climbable wooden lattice for walls. Grow Vines on it, then add a flower or Glow Berries. Vines slowly spread to bare trellises next to them. With global warming on, a Trellis with vines soaks up pollution like leaves do.
- New **Brass**, **Andesite**, **Copper** and **Iron Filigree Railings**: art nouveau railings that connect to their neighbours. Hold Alt to switch the item between flat, slope and steep slope, for ramps, arches and staircases. Gentle slopes blend into flat railings at both ends, steep slopes placed on stairs follow them automatically, and Create-style placement arrows help extend runs and slopes.
- New stairs, slabs and walls for the Turbine Casing and Fusion Reactor Casing, so engine rooms can match.
- Decorative blocks now have their own **Create: Solarpunk Building Blocks** creative tab, and the main tab is now grouped by power type.
- New **Ash** item: blast Biomass in a Blast Furnace or with Bulk Blasting, and craft 4 into an Ash Block. Ash Blocks now need a shovel and drop Ash, like Snow.

### Christmas

- During December, Garden Lights dress up for Christmas.

### Sun Readouts

- New **Sun Angle** Display Link readout on Solar Panels: shows where the sun is in the sky, in degrees from straight up toward the East or West. https://github.com/SucksAtstuff/Create-Solar-Punk/issues/53
- New **Sun Offset** Display Link readout on Solar Panels: shows how far the panel is turned away from the sun, so 0° means it's facing the sun head on. Handy for lining up sun-tracking arrays. https://github.com/SucksAtstuff/Create-Solar-Punk/issues/53

## Fixes

- Lithium Ore now generates as **Deepslate Lithium Ore** in deepslate, instead of stone-looking ore.
- Geysers no longer generate on top of or in water, so you won't find stone domes floating on oceans, lakes and rivers.
- The mod now only loads on Minecraft 1.21.1. It was wrongly marked as working on later 1.21 versions, which it doesn't support.
- Solar blocks on Create: Aeronautics contraptions now get shaded by the world around them, so flying one indoors or underground stops it working. https://github.com/SucksAtstuff/Create-Solar-Punk/issues/52
- Solar blocks on contraptions now follow the real sun angle: tilt the contraption toward the sun for full output, or away from it for less. Sun-tracking solar arrays are now possible. https://github.com/SucksAtstuff/Create-Solar-Punk/issues/52
- Contraptions now cast shade: parking one over solar blocks, on the ground or on another contraption, blocks their sunlight.
- Solar blocks on Create: Cosmonautics stations now properly react to which way the station is facing.
- The Brass Solar Panel has a new hum sound.

# 2.0.0-1.21.1

> **Back up your world before updating.** This is a big update, so make a copy of your world (and your server's config folder) first, just in case.

> Now built for **Create 6.0.10**. Update Create alongside this version.

## New

### Particle Accelerator

- New endgame machine: a ring of glass pipes, powered by rotation, that smashes items together into new materials. Watch the two beams race around and collide as each recipe finishes.
- Bigger rings unlock rarer results: Leather from Rotten Flesh, Tritium Capsules for the Fusion Reactor, Diamonds, Echo Shards and Netherite Scrap.
- New blocks: **Particle Accelerator Pipe** (one item for every shape, press Alt to pick) and **Particle Accelerator Input**.
- New items: **Electromagnet Coil**, **Superconducting Coil**, **Empty Fuel Capsule** and **Tritium Capsule**.
- JEI, Ponder, goggle and Display Link support. Datapacks can add their own recipes.
- The ring hums higher as the beams speed up, and you can hear them collide.

### Solar Power in Space

Solar blocks now work in space with Create: Cosmonautics, and follow the real sun just like they do in the Overworld. In deep space, Solar Panels, the Solar Heater, and the Solar Power Tower track where the sun actually is: turn your station away from it and they go dark, fly into a planet's shadow and it's night, and your station's own hull can shade them. Stations are built as ships with Create Aeronautics, and the panels follow the ship as it turns, so "up" is always the ship's up. Goggles show the panel's real output wherever it is. There's no weather in space. On the Moon they follow the Moon's own day and night instead of the Overworld's, and never get rained on. Cosmonautics is optional, and this was built against Cosmonautics 26.08.307.

Other space mods and modpacks can add their dimensions to the `solarpunk:always_sunny` (constant sunshine, no weather) or `solarpunk:no_weather` (normal day and night, no weather) dimension type tags with a datapack. Two new config options under `[space]`: `output_multiplier` boosts solar output in deep space, and `real_sun` can be turned off to make deep space always sunny instead of following the real sun.

## Changes

### Fusion Reactor

- The Fusion Reactor's Casing shell now matches a standard hollow sphere, so you can build it straight from WorldEdit's `//hsphere` or an online sphere generator. Reactors built the old way still work.
- Now burns **Tritium Capsules** from the Particle Accelerator, and its Breeder Modules slowly make more.
- Now drops its contents when broken.

### Recipe Overhaul

Higher-tier machines now need real Create processing instead of just a crafting table, so the mod's progression follows Create's own: cheap to get started, then sheets, assembly lines and Precision Mechanisms as you move up.

**New: Photovoltaic Cell**, made by sequenced assembly: Glass Pane, then deploy Polished Rose Quartz, deploy a Copper Sheet, and press. It's used in the Brass Solar Panel.

- **Andesite Solar Panel:** now uses Iron Sheets.
- **Solar Heater:** Copper Ingots replaced with Copper Sheets.
- **Crystallizer** and **Heat Battery:** Brass Ingots replaced with Brass Sheets.
- **Brass Solar Panel:** now made in a Mechanical Crafter, with Photovoltaic Cells, a Brass Casing, Electron Tubes and a Brass Sheet.
- **Biofuel Engine:** now made in a Mechanical Crafter, with Brass Sheets instead of Ingots.
- **Solar Mirror:** now made by sequenced assembly: Iron Sheet, then deploy a Glass Pane, deploy a Cogwheel, and press.
- **Solar Power Tower:** now made in a Mechanical Crafter with a Precision Mechanism, and gives 2 blocks per craft.
- **Fusion Reactor Core:** now made in a Mechanical Crafter, with 4 Precision Mechanisms added.
- **Lithium Breeder Module:** 8 Lithium Ingots around a Fluid Tank.
- **Beryllium Reflector Module:** 8 Beryllium Ingots around a Copper Sheet.

**For modpack makers:** the Brass Solar Panel, Biofuel Engine, Solar Mirror, Solar Power Tower and Fusion Reactor Core recipes have new IDs, under `mechanical_crafting/` or `sequenced_assembly/`. Update any recipe overrides that point at the old ones.

### Turbine Blades

- New model and textures for the Andesite and Brass Turbine Blades.
- Blades now show their 3D model when held, dropped, and in your inventory.
- Placing a blade now puts its second half to your right instead of straight ahead.

## Compatibility

- Salt from Northstar and Create: Food works anywhere Solarpunk Salt does, and Solarpunk Salt works in their recipes too.
- The Fusion Reactor accepts Lithium Dust from other mods.
- The Solar Heater now turns any stone (Andesite, Diorite, Granite, Deepslate, Tuff) and any cobblestone into Lava, not just plain Stone and Cobblestone.
- **Create: New Age:** the Heat Battery now trades heat with New Age's heat pipes and machines. A charged battery heats them, and an emptier one soaks up their heat. https://github.com/SucksAtstuff/Create-Solar-Punk/issues/49

## Polish

### Tooltips and Ponder

- Fixed outdated item tooltips for the Solar Panels, Solar Power Tower, Solar Mirror, Steam Turbine, Kinetic Sprinkler, Heat Battery, Solar Heater and Biofilter.
- The Crystallizer's tooltip now actually shows up, and the Fusion Reactor Core and both blanket modules have tooltips.
- Fixed wrong Ponder text: the Fusion Reactor scene no longer mentions the removed Extractor blocks, the turbine scene now says one blade per arm, and the Biomass Gasifier scene gives the correct burn time.
- Every Ponder scene has been redone to be clearer and walk through each machine step by step.
- Ponder scenes no longer play machine sounds.

### Sounds

- Machine sounds now fade with distance and come from the machine's direction, instead of being heard at full volume from far away.

## Bug Fixes

- Recipes now accept the same material from other mods, so salt, lithium, beryllium, metal ingots, Create's sheets and glass from any mod all work. https://github.com/SucksAtstuff/Create-Solar-Punk/issues/48
- Hoppers and funnels now only take products out of the Solar Heater, Biomass Gasifier and Firebox Boiler, instead of also pulling out their fuel or the item being melted.
- The Solar Heater and Crystallizer no longer delete items when their output tank holds a different fluid.
- Evaporated Salt can now be taken out of the Solar Heater by hand.
- Taking items out of a machine with a full inventory drops them instead of deleting them.
- The Firebox Boiler now gives back the empty bucket from Lava Buckets, and stops looking lit while it's out of water.
- Goggles no longer show a tank as full after it empties, and the Biomass Gasifier's goggle readings now update.
- Solar Power Towers no longer lose their mirrors when their chunk unloads.

# 1.1.1-1.21.1

Thanks to Studio V for reviewing the mod in [This Create Addon Adds FUSION](https://www.youtube.com/watch?v=OxV7FP923lA)! Watching the video is how I found out the Turbine Rotor was missing its recipe.

### Bug Fixes

- Steam Turbines no longer run at full power from a tiny trickle of Steam. Their SU output now scales with how much of the Steam they need is actually being supplied, and the goggles show a new Steam Supply percentage. https://github.com/SucksAtstuff/Create-Solar-Punk/issues/45
- The Turbine Rotor now has a crafting recipe (two Shafts, two Turbine Casings, and an Andesite Alloy). Before this you could only get it from the creative menu, so you couldn't build a Steam Turbine in survival.
- The Block of Lithium and Block of Beryllium now have crafting recipes: 9 Ingots make a Block, and a Block breaks back down into 9 Ingots.

# 1.1.0-1.21.1

## Display Link support, plus bug fixes from the 1.0.0 release

### Display Link Support

Added Create Display Link sources for the Heat Battery (charge % and Heated/Superheated status), Kinetic Battery (charge %), Solar Power Tower (mirror efficiency %, connected mirror count, and Molten Salt/Steam output rate in mB/s), and the Fusion Reactor (status, including why it stopped, Steam output in mB/s, redstone throttle %, Deuterium level, Steam tank fill %, and Lithium supply, all readable from any Casing block on the outside of the shell). Hook a Display Link up to any of these blocks to show their live stats on a Display Board, Nixie Tube, or any other Create display.

### Bug Fixes

- Fixed the Fusion Reactor Core crashing dedicated servers when placed. https://github.com/SucksAtstuff/Create-Solar-Punk/issues/36
- The Fusion Reactor's ambient startup/running sounds no longer play during its Ponder scenes.
- A maxed-out, fully efficient Solar Power Tower still couldn't keep a full bank of 9 Heat Batteries topped up overnight - it only made enough Molten Salt to break even during the day, with nothing left over to cover the night. Doubled its Molten Salt output so it can actually bank a surplus. **Existing worlds won't get this automatically** - either delete `solarpunk-common.toml` in your world/server config folder so it regenerates, or open it and change `salt_output_multiplier` under `[solar_power_tower]` from `1.0` to `2.0` by hand.
- Fixed the Solar Power Tower sometimes stopping Molten Salt or Steam production for the rest of the day if its output tank filled up or it briefly ran short on water, even after you fixed the problem.
- Fixed the Fermentation Vat occasionally losing its stored Water/Biofuel and getting stuck with no tank space after being reshaped.
- The "Maximum Size Turbine" Ponder scene now actually builds a 20-blade-layer turbine instead of a 7-layer one, so it matches the SU numbers it's advertising.
- Fixed a horizontal (on-its-side) Steam Turbine hand-built past the minimum size sometimes reporting itself invalid, or losing Steam/water piping on casing blocks further from the master Rotor - both were caused by the turbine's axis-agnostic scan not being applied consistently to every Rotor block and to piping capability lookups. Horizontal turbines can now be built up to the same 20-blade-layer maximum as a vertical one.

# 1.0.0-1.21.1

## This is the big one, the big 1.0.0 release, which brings a bunch of stuff and bug fixes, but most importantly Nuclear Fusion

### Fusion Reactor

Added the Fusion Reactor, the mod's endgame capstone. Build a Netherite-and-Copper Casing shell around a Core, ring it with Lithium Breeder and Beryllium Reflector Blanket modules in whatever mix you like (Lithium for efficiency, Beryllium for raw output), then feed it Deuterium and Lithium Dust to produce a huge amount of Steam - far more than any single Steam Turbine can use on its own, so it's meant to feed a whole bank of them. A redstone signal anywhere on the shell throttles it up or down. Comes with its own dedicated advancement, "A Star in a Bottle."

Deuterium is made by Mixing Water with a Copper Ingot overheat, and Lithium Dust by Mixing Water with Salt over superheat (with a small chance of bonus Beryllium Dust alongside it) - both show up in JEI automatically.


### Crystallizer

Added the Crystallizer, a brass-tier machine that quenches Molten Salt with Water to crystallize it back into Salt, releasing a bit of Steam as a byproduct - a much faster bulk alternative to the Solar Heater's slow evaporation trickle. Pipe both fluids in from any side, and collect Salt from the output slot (or a hopper underneath) and Steam from any face. It's recipe-driven, so modpacks and datapacks can add their own custom conversions to the same machine.

### Lithium & Beryllium

Added Lithium, a new ore found underground worldwide rather than in any specific biome. Mine it for Raw Lithium and smelt that into Lithium Ingots, or crush it for Lithium Dust - which also smelts into ingots - with a small chance of bonus Beryllium Dust. Beryllium Dust can be smelted into Beryllium Ingots too, and both metals' Ingots and Nuggets craft into each other 1:9 the usual way. Both metals can be stored compactly as a Block of Lithium or Block of Beryllium.

### Fusion Reactor

Added the Fusion Reactor, the mod's endgame capstone. Build a Netherite-and-Copper Casing shell around a Core, ring it with Lithium Breeder and Beryllium Reflector Blanket modules in whatever mix you like (Lithium for efficiency, Beryllium for raw output), then feed it Deuterium and Lithium Dust to produce a huge amount of Steam - far more than any single Steam Turbine can use on its own, so it's meant to feed a whole bank of them. A redstone signal anywhere on the shell throttles it up or down. Comes with its own dedicated advancement, "A Star in a Bottle."

Deuterium is made by Mixing Water with a Copper Ingot overheat, and Lithium Dust by Mixing Water with Salt over superheat (with a small chance of bonus Beryllium Dust alongside it) - both show up in JEI automatically.

### Compatibility

The Steam Turbine can now burn Liquid Hydrogen from Northstar Redux as an alternative to Steam, at the same rate. Pipe it in the same way you would Steam - just not both at once, the tank has to fully drain before switching fuels.

With Serene Seasons installed, solar output now shifts with the seasons: higher in summer, lower in winter, near-normal in spring and autumn. Hot biomes like deserts and savannas barely change. Affects the Solar Panels and the Solar Power Tower; turn it off in the config if you'd rather keep flat year-round output.

### QoL
- The Biofuel Engine, Biofilter, Biomass Gasifier, Fermentation Vat, Solar Heater, and Steam Turbine now have ambient sounds while they're running, and the Crystallizer hisses each time it finishes a batch.
- The Firebox Boiler now crackles like a furnace while it's lit, instead of burning silently.
- The Biofilter's ambient sound now pitches up the faster it's spinning.
- All of the mod's sounds now show subtitles when subtitles are enabled.
- The Solar Power Tower's Molten Salt and Steam output rates can each be tuned in the config file now.

### Balance

- Salt is significantly rarer in desert, badlands, and savanna terrain now. It used to generate in almost every chunk; you'll have to actually go looking for it.

### Bug Fixes

- Fixed the Brass Solar Panel producing more FE per tick than intended when multiple cables or machines were drawing from it at once.
- Fixed pollution detection not working correctly for machines placed inside a Sable sublevel.
- Gave the Biomass Gasifier a proper spinning shaft and fixed it briefly rendering all the way through the block instead of as a short nub at the bottom.
- The Biofuel Engine's shaft now actually spins while it's running, instead of sitting there as a static texture.
- Moved the Biomass Gasifier's rotation-direction scroll box off the input shaft face, matching the Biofuel Engine.
- Fixed some stretched and mismatched textures on the Biomass Gasifier and Biofuel Engine.
- The Biofilter's pollution absorption now scales with how fast it's spinning, instead of removing the same amount no matter how much Rotational Force you feed it.
- The Geyser Vent now actually plays its puff sound when it blows out steam, and reliably keeps doing so even with no Geyser Cap placed on top.
- Molten Salt now actually glows, instead of just looking hot without lighting up the surrounding area.
- Fixed a maxed-out Solar Power Tower making far too little Molten Salt in salt mode. It had dropped to where it couldn't keep even 3 Heat Batteries superheated; a full tower at noon now makes enough to hold 9 Heat Batteries (a max-size boiler's worth) superheated around the clock.
- The Solar Power Tower's Steam mode now actually requires and consumes Water to produce Steam, instead of generating it for free. https://github.com/SucksAtstuff/Create-Solar-Punk/issues/34
- Fix crash when a kinetic conflict destroys a generator mid-tick https://github.com/SucksAtstuff/Create-Solar-Punk/pull/29

# 0.4.0-1.21.1

### Steam Turbine

The Steam Turbine can now be built lying on its side as well as standing up. Build the same 7x7 shell along the East/West or North/South axis instead of straight up, and it works exactly the same way - pipe in steam, collect rotational force from the end of the rotor column.

### Firebox Boiler

Added the Firebox Boiler, a cheap early-game way to make Steam. Craft it with iron, a furnace, and a bucket, feed it any furnace fuel, and pipe water in to get Steam out - no Salt or Solar Power Tower required. It's much weaker per block than a fully built Solar Power Tower, so it's meant to get a Steam Turbine running early rather than replace the tower later on.

### Solar Mirrors

Solar Mirrors have been reworked into a proper heliostat field. Instead of bolting onto the tower's walls, place them on the ground nearby and they'll link to a Solar Power Tower automatically, as long as they have open sky above them and a clear view of it. Linked mirrors turn and tilt throughout the day to track the sun and reflect it toward the tower - taller towers can reach farther and track more mirrors at once. A tower can now track up to 128 mirrors at once (up from 64), and Solar Mirrors cost a bit more to craft to match - 2 Redstone Dust alongside their existing materials.

### QoL

- The Solar Heater's JEI page now also shows its Water-to-Salt evaporation, alongside the existing item-melting recipes.
- Updated the Solar Power Tower's Ponder scenes to show the new mirror field and sun-tracking behavior.

### Bug Fixes

- Fixed list-based config options (pollution blacklist, per-block pollution, geyser biomes) silently reverting to their defaults when a single entry was malformed. A bad entry is now logged and skipped on its own, leaving the rest of your list intact.
- Fixed Geyser Vents spawning in mountain biomes instead of the documented desert, badlands, and savanna, and fixed the geyser biomes and geyser spawn chance config options doing nothing at all. Both now actually control where and how often Geyser Vents generate.
- Fixed Solar Mirrors sometimes failing to link to any tower at all.

# 0.3.2-1.21.1

### Achievements

Added a dedicated **Create: Solarpunk** advancement tab with achievements that guide you through the mod from your first piece of Salt all the way to a running Steam Turbine.

### Compatibility

- Diesel engines from Create: Diesel Generators now count as pollution sources when Global Warming is enabled.

### Bug Fixes

- Fixed Create: New Age's Generator Coil incorrectly producing pollution.
- Fixed the Biomass Gasifier shaft not spinning when the machine was active.
- Fixed the Steam Turbine efficiency calculation requiring 2 blades per arm for full output. One brass blade per arm (4 per layer) is now enough to reach 100% efficiency.


### QoL

- Added a pollution blacklist config option, letting you exclude specific blocks from ever counting as pollution sources.
- The **Biomass Gasifier**, **Biofuel Engine**, and **Geyser Cap** now support rotation direction control. Scroll on the side of the block (away from the shaft faces) to toggle between clockwise and counter-clockwise output, the same way the Andesite Solar Panel works.
- Updated the Steam Turbine Ponder scenes: walls now use Turbine Casing Glass between the corner frame columns, blades are shown facing the correct direction, and the max-size scene zooms out far enough to see the full structure.


# 0.3.1-1.21.1

### Bug Fixes

- Fixed Turbine Blades always facing the same direction regardless of placement. They now orient to the direction you're looking when placed, like stairs. Also fixed the hitbox being a full block - it now matches the thin flat shape of the model.

# 0.3.0-1.21.1

### Steam Turbine

Added the **Steam Turbine** multiblock. Build a 7x7 shell of **Turbine Casing** (or **Turbine Casing Glass**) around a column of **Turbine Rotors**, then fill each interior layer with **Turbine Blades** in a plus pattern - two blades per arm, four arms. Pipe steam in from any side face and rotational power exits from the top of the rotor column.

Blade type determines efficiency: **Andesite Blades** are cheaper but waste more steam, **Brass Blades** squeeze the most power out of every bucket. Mixing types is fine - efficiency scales smoothly. Taller turbines consume more steam and produce more SU, with condensate water draining from the bottom.

### Steam

Added **Steam** as a new fluid. It is the fuel source for the Steam Turbine and can be bucketed and piped like any other fluid.

### Solar Power Tower Steam Mode

The **Solar Power Tower** can now produce **Steam** in addition to Molten Salt. Right-click the tower with a **Wrench** to toggle between the two modes. Switching clears the stored fluid, so you cannot accumulate both at once. Steam can be piped directly to a Steam Turbine, making the tower a self-contained solar-to-power chain without needing a Heat Battery and boiler.

### Turbine Casing Crafting

**Turbine Casing** is crafted by right-clicking a Block of Industrial Iron with a Zinc Ingot, or by running it through a Deployer holding zinc. Right-click a Turbine Casing with Glass to get the glass variant. Both work in-world and in a Deployer contraption.

### Solar Mirror

The **Solar Mirror** has a new model and texture, replacing the old glass-and-stone design with a single dedicated texture.

### Bug Fixes

- Fixed milling crops, leaves, and saplings in the Millstone producing nothing. The recipes were missing a processing time, so the mill would spin forever without completing.

### Balance

- **Solar Power Tower (Salt Mode)**: Molten Salt output has been substantially reduced. The previous rate was tuned for steam production and left the salt chain with far more capacity than a Heat Battery setup could ever use. A max-size tower now produces enough to run 9 Heat Batteries continuously.
- **Fermentation Vat**: Now requires a minimum height of 4 blocks to operate in addition to the existing 2x2 footprint requirement. This is configurable.

# 0.2.1-1.21.1

### Create: Aeronautics Compat

Pollution sources inside Aeronautics sublevels (engines on airships) now correctly accumulate pollution in the real-world chunk where the airship is located. Previously they had no effect on the pollution system at all. Requires Sable/Aeronautics to be installed; has no effect otherwise.

### Bug Fixes

- Fixed the Solar Heater not producing salt from water evaporation. Water buckets were filling the wrong internal tank, so evaporation never started.
- Fermentation Vat biofuel output now scales super-linearly with height, using the same curve as the Solar Power Tower. A minimum-height vat produces very little; building taller is now the main way to scale up biofuel production. Goggle overlay now shows the actual mB output per batch.

---

# 0.2.0-1.21.1

### Biomass Pellet

**Biomass Pellets** now have a distinct role in the Biomass Gasifier. Raw Biomass burns quickly (200 ticks) with 1 Biochar per piece. Pellets burn slower (400 ticks) and produce 2 Biochar each, making them the dedicated path for farming Biochar and Fertilizer. All values are configurable.

### Global Warming

Added an optional **global warming** system (disabled by default, toggle in the config).

Active pollution sources - campfires, furnaces, blast furnaces, smokers, blaze burners, steam engines, biofuel engines, and biomass gasifiers - emit black smoke and accumulate pollution in their chunk. When a chunk's pollution level crosses the threshold, it starts to decay: grass withers into **Dead Grass**, dirt crumbles into **Ruined Dirt**, and ruined dirt turns to **Ash**. Plants, leaves, and other fragile blocks are cleared along the way. Push a chunk far enough and the biome itself converts to a dead wasteland. Custom textures for the decay blocks are coming in the next release.

All rates, thresholds, and the target dead biome are configurable. The system is designed to make renewable energy the only sustainable long-term choice. For full details on pollution sources, mod compatibility, and config options, see the [wiki](https://github.com/SucksAtstuff/Create-Solar-Punk/wiki).

Cleanup reverses the damage: as pollution clears, dead blocks gradually heal back toward grass, and once a chunk drops below the threshold its biome restores to what it was before.

### Biofilter

Added the **Biofilter**, a brass-tier machine. Connect rotational power to its bottom face and it absorbs pollution from chunks within a configurable radius, gradually healing blocks and eventually restoring the biome. It draws stress from the kinetic network while running. Wearing goggles shows the current chunk pollution level and how fast the filter is removing it.

### Kinetic Sprinkler

Added the **Kinetic Sprinkler**. Pipe water into it from any face and it will hydrate farmland and accelerate crop growth in a 5x5 area below it. Pipe in Fertilizer instead for forced instant growth every cycle.

### Fertilizer

Added **Fertilizer**, a liquid made by mixing Biochar with water in a Create basin. Pipe it into the Kinetic Sprinkler for forced crop growth on every cycle.

### Biochar

Added **Biochar**, a byproduct of the Biomass Gasifier. Each fuel item burned produces one Biochar in the Gasifier's output slot. The Gasifier pauses if the output is full, so keep it drained with a funnel or chute.

Biochar acts as a direct bonemeal substitute - right-click crops, grass, and other growable blocks to fertilize them. It is obtained only from the Gasifier, closing the biofuel loop with a useful agricultural byproduct.

### Comparator Support

Comparators now read stored levels from three blocks:

- **Heat Battery** - signal proportional to stored heat
- **Kinetic Battery** - signal proportional to stored charge
- **Biofuel Engine** - signal proportional to fuel in the tank

### Config

Every machine and generator is now configurable in the mod config file. You can adjust tank sizes, generator RPM and stress output, production speeds, output amounts, and geyser spawn rates and biomes.

### Tooltips

All machines now show a description tooltip when hovering over them in the inventory, matching Create's style. Generators also display their stress capacity tier and, when wearing goggles, the exact SU output.

### Bug Fixes

- Fixed a crash where Andesite and Brass Solar Panels could tick after their block was removed, causing a crash during day/night transitions in large connected networks.
- Fixed Solar Mirror blocks becoming excessively dark when another mirror is placed directly above them on the tower.
- Fixed the Brass Solar Panel showing the brass gearbox texture on its underside instead of the brass casing.
- Geyser Vents can no longer be moved by pistons or Create contraptions.

---

# 0.1.1-1.21.1

### Solar Power Tower

Added the **Solar Power Tower** multiblock and **Solar Mirror** heliostat block.

The tower concentrates reflected sunlight to produce large quantities of Molten Salt from water. Build it in a 1x1, 2x2, or 3x3 footprint (maximum heights 5, 10, and 20 blocks respectively) and surround the sides with Solar Mirrors to increase output. Mirror efficiency follows a triangle curve - filling roughly half the available wall faces is optimal; over-mirroring past twice the optimal count reduces output to zero.

The tower requires a minimum **3x3 footprint** to produce anything. Water consumption scales with footprint area (9:1 water-to-salt for a full 3x3 tower), making larger towers a serious infrastructure investment.

The Solar Mirror is a wall-mountable heliostat that can be placed on floors, walls, and ceilings. It must touch the tower's side face directly to count. Use a Create wrench to cycle it through all six facing orientations.

Both blocks are wrenchable: sneak + right-click breaks them and returns them as items.

### Fermentation Vat

The **Fermentation Vat** now requires a minimum **2x2 footprint** to operate. In exchange, production now scales with footprint area: a 2x2 vat processes 4 batches per cycle and a 3x3 processes 9, consuming water and biomass proportionally. Height still only adds tank capacity. This makes larger vats meaningfully more productive than stacking many small ones. Goggles now display the current batch scale and warn when the vat is too small to function.

### Ponder Scenes

Added in-game Ponder tutorials (press W on a block in the creative menu or use a Ponder Wand) for all machines:

- **Solar Power Tower & Solar Mirror** - two scenes: multiblock assembly and mirror placement. Both blocks share the same entry.
- **Fermentation Vat** - two scenes: basic usage (2x2 minimum, water and biomass input, biofuel output) and scaling (how footprint area increases batch size and tank capacity).
- **Biomass Gasifier** - shows biomass insertion, ignition, and shaft output below.
- **Biofuel Engine** - shows biofuel piping, ignition, and shaft output below.

Ponder entries are organised into three tag groups in the index: **Solar Machines** (Solar Heater, Solar Panels, Heat Battery, Kinetic Battery, Geyser Cap), **Bio Machines** (Gasifier, Engine, Fermentation Vat), and **Solar Power Tower**.

### JEI Integration

Added JEI recipe categories for the **Fermentation Vat** (Biomass + water -> Biofuel). Click either block in the JEI item list to open its category.

### Bug Fixes

- Added missing cross-mod fluid tags: `c:molten_salt` and `c:molten` for Molten Salt, `c:biofuel` for Biofuel.

---

# 0.0.1-1.21.1

### Biofuel Chain

Added a full biofuel energy chain: **Biomass**, **Biomass Gasifier**, **Fermentation Vat**, and **Biofuel Engine**. Biomass is a new item crafted from organic matter. The **Biomass Gasifier** processes raw biomass using rotational force. The **Fermentation Vat** then converts it into liquid biofuel using water: it's a multiblock structure that can be stacked vertically to increase capacity. Finally, the **Biofuel Engine** burns that fuel to generate rotational force (SU).

### Geyser Cap

**Geyser Vents** now spawn naturally in desert, badlands, and savanna biomes. Placing a **Geyser Cap** on top of one harnesses the geothermal energy and converts it into rotational force (SU): a completely passive power source that works day and night, regardless of weather. The cap has an animated model, can be picked up and rotated with the Create wrench, and has a Ponder entry.

### Improvements

- The **Brass Solar Panel** now plays an ambient electrical hum while generating power.

### Bug Fixes

- Fixed a bug where SU generators (Solar Panels, Geyser Cap) would double their stored capacity on every world reload.