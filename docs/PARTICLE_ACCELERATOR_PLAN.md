# Particle Accelerator Plan

Status: idea / design stage. Nothing implemented yet.

## Overall Idea

The Particle Accelerator is a very large ring multiblock that uses kinetic energy (SU) to collide two items into each other and transmute them into a different material. It is the mod's endgame "post-scarcity" machine: resources that are normally finite (diamonds, netherite, fusion fuel) become renewable, as long as you feed it enough clean power.

### Theme Fit

This is the post-scarcity end of solarpunk rather than the "live with nature" end, but both belong to the genre. The framing should be:

- **Clean energy replaces mining.** Flavor text, Ponder scenes and the changelog should present it as "you never need to strip mine again", not "free diamonds".
- **It feeds the Fusion Reactor.** Breeding Tritium gives it a clear place in the mod's clean energy chain.
- **It runs on the mod's own power.** Its huge SU demand is a reason to build solar fields, geyser caps and turbines.
- **Danger is realism, not a weapon.** The explosion only happens when the machine is built or broken wrong, the same way a mis-steered beam damages a real accelerator.

## Components

| Block | Role | Status |
|---|---|---|
| Accelerator Pipe | Main straight segment. Cannot bend. Counts toward ring size. | Needed |
| Accelerator Corner | 90 degree turn. Counts toward ring size. | Needed |
| Accelerator Input | Takes kinetic energy (shaft) into the ring. Recipes check its RPM. | Needed |
| Injector | Where the two input items go in, via funnels or belts. | Missing from original idea |
| Collector / Target | Where the output item comes out, for funnels, chutes or hoppers. | Missing from original idea |
| Beam Dump | Safety cap for an open end. Absorbs the beam instead of firing it. | Missing from original idea |

The original idea had items inserted into pipes via funnels. Dedicated Injector and Collector blocks make building clearer (players know exactly where logistics go) and make the code simpler (only one or two BEs hold inventories, not every pipe).

## Core Mechanics

### Ring size

- Size = number of Pipes + Corners in the loop.
- Bigger rings cost more SU but need fewer input items per craft. This tradeoff is the heart of the design.
- Some recipes require a minimum ring size.
- A long thin loop counts the same as a square one (keeps things simple).

### Power

- Kinetic energy enters through Accelerator Inputs.
- Each recipe needs a minimum RPM.
- SU demand scales with ring size.
- **Spin-up phase:** the ring has to charge before the first craft. Flickering power resets or drains the charge, which rewards stable power setups.

### Crafting flow

1. Two items are loaded into the Injector.
2. The ring spins up to the recipe's RPM and charge.
3. A visible particle travels around the ring, speeding up.
4. On collision, the inputs are consumed and the output appears in the Collector.

### Failure state (open ring)

If the loop is not closed **while the ring is running**, the open end fires a projectile that explodes on impact. This is a consequence of building it wrong, not a feature to aim.

- **Only fires while running.** Breaking a pipe on an idle ring does nothing. The danger is the loose beam, not the pipes.
- **Warns first.** Goggle tooltip ("Loop incomplete"), sparking particles and sound at the open end, so careful players can shut it down in time.
- **Beam Dump prevents it.** Capping an open end with a Beam Dump absorbs the beam safely.
- **Scales with size.** A small test ring gives a small pop and a huge ring gives a serious explosion, so players learn on small builds.
- **Config option.** On by default, but servers can disable block damage. Also respect the `mobGriefing` gamerule.

## Recipes

Recipes should focus on making **finite** resources renewable and on feeding the mod's own systems. Numbers below are placeholders, to be tuned by ring size and SU cost.

| Inputs | Output | Notes |
|---|---|---|
| 64 Lithium | 1 Tritium | Key recipe. Tritium replaces Lithium in the Fusion Reactor and lasts much longer. |
| 64 Logs | 1 Diamond | Concern: tree farms are trivial, so this needs a high SU cost or a large minimum ring size. |
| 32 Gold + 32 Iron | 1 Netherite Ingot | Concern: skips the ancient debris gate entirely. Consider requiring netherite scrap or debris as one input. |
| Waste / byproducts | Useful materials | Idea: recycle rotten flesh, gravel, compost or the mod's own byproducts. "Nothing is wasted" fits the theme well. |

### Balance rule of thumb

Any recipe that turns a cheap, easily automated item into a rare one must be expensive, gated by ring size, or both. Otherwise the ring becomes the only thing anyone builds.

## What's Missing / Open Questions

### Needs to exist first

- **Tritium item.** The Fusion Reactor currently has no Tritium item or fluid. It consumes Deuterium (fluid) + Lithium Dust and does the Lithium to Tritium breeding as internal math only. The accelerator needs a real Tritium item (or fluid).
- **Fusion Reactor accepts Tritium.** The reactor has to take Tritium as an alternative to Lithium, with a much higher fuel value, and probably ignore or bypass the breeder blanket math when running on it.
- **Custom recipe type.** An accelerator recipe with two inputs, one output, minimum RPM, minimum ring size, and input count scaling. Plus JEI category and datagen support.

### Design decisions still open

- **Scaling curves.** Exact formulas for SU vs ring size, input count vs ring size, and minimum size per recipe.
- **Size cap.** Without one, someone will build a 2000 block ring and get absurdly cheap crafts. Either cap the size or make input savings plateau.
- **Ring orientation.** Flat (horizontal) only is much easier to validate and render. Decide whether vertical or 3D rings are allowed.
- **Multiple Inputs.** Can a ring have several Accelerator Inputs, and do their SU add up? Do they all need the same RPM?
- **Multiple Injectors / Collectors.** One of each per ring, or more?
- **Projectile direction.** Straight out of the open end along the pipe axis, so it is predictable.

### Technical risks

- **Chunk borders.** Large rings will span several chunks. If part of the ring is unloaded, it should pause, not count as broken (and definitely not fire).
- **Validation cost.** Only validate the loop when a ring block is placed or broken, never every tick. Walking a 200 block ring every tick adds up.
- **Controller.** Needs a single controller BE (probably the Injector or an Input) holding ring state, similar to how the other multiblocks proxy to a controller.
- **Rendering.** The travelling particle should be one renderer on the controller, not per-pipe logic.

## Nice to Have

- **Display Link support** for charge, speed, current recipe and ring status, matching the Fusion Reactor display sources.
- **Ponder scenes** for building a ring, the spin-up, and the Beam Dump / failure state.
- **Goggle info** showing ring size, SU demand, current RPM, charge and loop status.
- **Sound design** for spin-up hum, collision, and the open-end warning.