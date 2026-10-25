# Particle Accelerator Plan

Status: working. Rings form, the Input crafts recipes and the particle runs. Still to do: real recipe numbers, Tritium in the Fusion Reactor, and extras (Ponder, sounds, Display Link).

## Overall Idea

The Particle Accelerator is a very large ring multiblock that uses kinetic energy (SU) to collide two items into each other and transmute them into a different material. It is the mod's endgame "post-scarcity" machine: resources that are normally finite (diamonds, netherite, fusion fuel) become renewable, as long as you feed it enough clean power.

### Theme Fit

This is the post-scarcity end of solarpunk rather than the "live with nature" end, but both belong to the genre. The framing should be:

- **Clean energy replaces mining.** Flavor text, Ponder scenes and the changelog should present it as "you never need to strip mine again", not "free diamonds".
- **It feeds the Fusion Reactor.** Breeding Tritium gives it a clear place in the mod's clean energy chain.
- **It runs on the mod's own power.** Its huge SU demand is a reason to build solar fields, geyser caps and turbines.

## Components

| Block | Role | Status |
|---|---|---|
| Accelerator Pipe | Straight segment. Counts toward ring size. | Done |
| Pipe Bend / Pipe Bend Mirrored | 45 degree turn from a straight run to a diagonal one (left and right versions, since block models can't mirror). Counts toward ring size. | Done |
| Diagonal Pipe | Runs corner to corner between two bends. Counts toward ring size. | Done |
| Accelerator Input | The ring's controller. Shaft power enters from below (recipes check its RPM), items go in through the side cap via funnels, belts or hoppers, and the result comes back out of the same block. Has a filter slot to pick which recipe it makes. One per ring. | Done |

The original idea had items inserted into pipes via funnels. A dedicated item block makes building clearer (players know exactly where logistics go) and makes the code simpler (only the Input holds an inventory, not every pipe). Separate Injector and Collector blocks were planned too, but the Input now takes power, takes items and gives the result back, so a ring is just pipes plus one Input. A Beam Dump (a safety cap for open ends) was planned as well but dropped along with the failure state: an open ring simply doesn't run.

All four pipe shapes are one item. Holding it and pressing Create's toolbelt key (Alt) opens a radial menu, like the toolbox's, to pick the shape it places.

Pipes and the other ring blocks share one tube size (4x4 px at y 6-10, centered) and the same stand, so any two pieces line up. The Input only sits on straight runs.

## Core Mechanics

### Ring size

- Size = number of ring blocks in the loop (pipes, bends, diagonals and the Input). A diagonal block covers about 1.4 blocks of distance but counts as 1.
- Every turn is 45 degrees, so two bends placed diagonally next to each other make a 90 degree turn, and rings come out as octagons or rectangles with cut corners.
- Rings are flat (horizontal) only.
- **Ring diameter** is the shorter side of the ring's footprint, so a 17x40 ring has diameter 17. Using the shorter side stops long, thin rings from counting as huge.
- **Recipes are gated by diameter.** Each recipe has a minimum diameter, and the expensive ones need a big ring. That's the reason to build big.
- Item costs are fixed per recipe. A bigger ring doesn't make crafts cheaper, it unlocks more recipes.

### Power

- Kinetic energy enters through Accelerator Inputs, from a shaft below.
- Each recipe needs a minimum RPM.
- Each recipe has an SU cost at its minimum diameter, and the cost scales with the actual ring: `SU = recipe SU x (ring diameter / minimum diameter)`. A 34-wide ring running a "min 17" recipe costs double, so running cheap recipes on a huge ring is wasteful (build a small ring for those).
- JEI shows the minimum diameter, the SU at that diameter and the minimum RPM, e.g. "Ring diameter: 17+, Stress: 8,192 SU at 17 (more on bigger rings), Min speed: 128 RPM".
- **Spin-up phase:** the ring has to charge before the first craft. Flickering power resets or drains the charge, which rewards stable power setups.

### Crafting flow

1. Items go into the Accelerator Input through its side cap. Its inventory works like a Create Basin: a few slots, any order, mixed on one belt or funnel is fine. A recipe starts once the contents match one.
   - **Filter slot:** like a Basin's, a Create filter slot on the Input picks which output to make. When the contents match more than one recipe, the filter decides; with no filter, the first match runs.
2. The ring spins up to the recipe's RPM and charge.
3. Two beams run around the ring in opposite directions, speeding up, timed to meet back in the Input as the craft finishes.
4. On collision, the inputs are consumed and the output appears in the Input's output slots (separate from its input slots, like a Basin), for a funnel to pull out. For Tritium, an Empty Fuel Capsule is simply a third ingredient.

### Open ring

A ring that isn't a closed loop just doesn't run (goggles on the Input say "Not closed"). There's no failure state or explosion: an earlier version of this plan had the open end fire a beam, but it was dropped.

## Recipes

Recipes make **finite** resources renewable and feed the mod's own systems. The smallest closed ring is 5 wide and has no recipe, so it's for learning how the ring works (or for cheap modpack recipes). Recipes then step up by how rare the result is. Times are at minimum speed and halve at double it. SU grows with the ring's diameter past the minimum.

| Inputs | Output | Diameter | SU | RPM | Time |
|---|---|---|---|---|---|
| 32 Lithium Dust + 4 Beryllium Ingots + 1 Empty Fuel Capsule | 1 Tritium Capsule | 9 | 16384 | 128 | 30 s |
| 64 Biochar | 1 Diamond | 15 | 24576 | 128 | 40 s |
| 8 Sculk + 4 Amethyst Shards | 1 Echo Shard | 15 | 24576 | 128 | 40 s |
| 16 Gold Ingots + 16 Netherrack + 4 Nether Quartz | 1 Netherite Scrap | 21 | 32768 | 192 | 60 s |

- **Tritium** is worth 32000 mB in the Fusion Reactor (`reactor_tritium_capsule_mb`), 4x the Lithium that goes in. A pure-Lithium blanket burns a capsule in about 24 s, so one ring at 256 RPM keeps it running. A Beryllium-heavy reactor burns one in about 5 s and needs several rings or Lithium on the side. The Breeder Modules' 2% trickle stays a bonus.
- **Netherite** gives Scrap, not an ingot, so the vanilla Scrap + Gold step still applies.
- **Diamond** uses Biochar, which is easy to farm, so it sits behind a bigger ring and more SU.

### Balance rule of thumb

Any recipe that turns a cheap, easily automated item into a rare one must be expensive, gated by ring size, or both. Otherwise the ring becomes the only thing anyone builds.

## Items

The Input takes and gives items through its cap, the side opposite the cap and its top, so one face can feed it while another empties it. The pipe ends and the shaft underneath have no item access.

## Technical Notes

- **Chunk borders.** If part of the ring is unloaded, it pauses ("Partly unloaded") instead of counting as broken.
- **Validation cost.** The Input rescans its ring every 2 seconds (like the turbine and fusion reactor), not every tick.
- **Controller.** The Input's block entity holds the ring state, inventory, recipe and stress.
- **Rendering.** One renderer on the Input draws two beams (cyan and amber) running around the ring in opposite directions along the tube's centre line (through bend curves and diagonals), emissive so they glow through the see-through pipes. Their positions come from the craft's progress (progress squared times a whole number of laps), so they speed up and meet back in the Input exactly as the craft finishes, where the collision flashes and throws sparks.
- **Sound.** A hum whose pitch follows the beams, fading in and out, and a collision sound at impact.
- **Integration.** JEI category, Ponder scenes, goggle info, item tooltips and Display Link sources (status, recipe, progress). Datapacks can add recipes; there's no size cap.
