# Create: Solar Powered — Wiki

## Table of Contents

- [Salt](#salt)
- [Solar Heater](#solar-heater)
- [Heat Battery](#heat-battery)
- [Andesite Solar Panel](#andesite-solar-panel)
- [Brass Solar Panel](#brass-solar-panel)
- [Kinetic Battery](#kinetic-battery)
- [Progression Guide](#progression-guide)

---

## Salt

Salt is the primary fuel source for the solar heat chain.

### Obtaining

**World generation** — Salt deposits spawn underground in desert, badlands, and savanna biomes between Y=48 and Y=90. They generate in veins of up to 20 blocks, replacing sand, sandstone, red sandstone, and terracotta. Any pickaxe can mine them.

**Crafting**
- 9 Salt → 1 Salt Block (shaped, 3×3)
- 1 Salt Block → 9 Salt (shapeless)

**Solar Heater evaporation** — The Solar Heater can evaporate water into salt crystals (see below).

### Uses

| Input | Output | Where |
|-------|--------|-------|
| 1 Salt | 100 mB Molten Salt | Solar Heater |
| 1 Salt Block | 1000 mB Molten Salt | Solar Heater |

---

## Solar Heater

The Solar Heater concentrates sunlight onto a mirrored surface to perform two operations simultaneously: **item melting** and **water evaporation**.

### Requirements

- Clear sky directly above the block (no blocks in the way — this is also why pipes must connect to the sides or bottom, not the top)
- Daytime (time < 12000)
- Not thundering
- Rain halves the effective processing speed (skips every other tick)

### Placement

The Solar Heater is directional. It faces the player when placed and can be rotated with a Create wrench (click the top face). Shift-wrench picks it up instantly.

### Item Melting

Right-click a salt item (or any supported ingredient) into the input slot. The heater melts it into molten salt over **200 ticks** (10 seconds) of sunlight. Output fluid collects in the internal tank (8000 mB capacity) and can be piped out from the sides or bottom.

| Input | Output | Time |
|-------|--------|------|
| 1 Salt | 100 mB Molten Salt | 200 ticks |
| 1 Salt Block | 1000 mB Molten Salt | 200 ticks |

### Water Evaporation

Pipe water into any non-top face. The heater evaporates **250 mB of water every 200 ticks**, producing 1 salt item into the output slot (up to a stack of 64). Extract salt items by shift-right-clicking the block, or use a hopper/funnel on the sides.

### Fluid I/O

| Face | Accepts | Outputs |
|------|---------|---------|
| Top | Nothing (keeps sky clear) | Nothing |
| Sides / Bottom | Water (fill) | Molten Salt (drain) |

---

## Heat Battery

The Heat Battery converts molten salt into stored thermal energy, then releases that heat to adjacent Create boilers.

### Charging

Pipe **Molten Salt** into the Heat Battery (8000 mB tank). Each tick it consumes 1 mB and converts it to **10 heat**, while passively losing **1 heat per tick**. Net charge rate: **+9 heat/tick**.

Maximum heat storage: **40,000**.

### Heat States

| Block Appearance | Heat Stored | Boiler Effect |
|-----------------|-------------|---------------|
| Off (dark top) | 0 | None |
| Heated (lit top) | 1 – 9,999 | Heated |
| Superheated (glowing top) | 10,000+ | Superheated |

The Heat Battery emits light proportional to its heat state (0 / 4 / 8 light level).

### Reaching Superheated

A single Solar Heater melting **salt blocks** (5 mB/tick) comfortably outpaces the 1 mB/tick draw and will push the battery to superheated. A **salt item** heater (0.5 mB/tick) can reach superheated slowly — the battery decays at only 1 heat/tick, so heat accumulates as long as the tank is not empty. Multiple heaters in parallel speed this up significantly.

### Wrench

Shift-wrench to pick up the Heat Battery without losing its contents.

---

## Andesite Solar Panel

Generates **rotational force (SU)** using sunlight. Built on Create's kinetic system.

### Requirements

- Clear sky above
- Daytime, no thunder

### Output

Outputs stress units downward through a shaft. Can be oriented on any axis using a Create wrench. Connect to your Create kinetic network like any other source.

---

## Brass Solar Panel

Generates **Forge Energy (FE)** using sunlight and exports it automatically to adjacent FE-compatible machines.

### Output

| Time of Day | FE/tick |
|-------------|---------|
| Morning / Evening | 40 FE/t |
| Noon | 80 FE/t |
| Raining (noon) | 40 FE/t |
| Night / Thunder | 0 FE/t |

Internal buffer: **100,000 FE**. Exports up to 80 FE/t per face automatically.

---

## Kinetic Battery

Stores **rotational energy (SU)** from a Create kinetic network and releases it on demand.

Connect it in-line with a shaft. When the network is producing more than it consumes, the battery charges; when demand exceeds supply, it discharges.

---

## Progression Guide

### Early Game

1. Explore deserts or badlands to find **Salt Deposits** underground (Y 48–90)
2. Mine salt blocks — each drops itself (use a pickaxe)
3. Craft a **Solar Heater** and place it somewhere with full sky access
4. Put salt into the input slot; during the day it melts into molten salt
5. Collect molten salt in a barrel or pipe it directly

### Mid Game

6. Craft a **Heat Battery** and pipe molten salt into it
7. Place the Heat Battery adjacent to a **Create boiler** — it acts as the heat source
8. For electricity, place a **Brass Solar Panel** and connect it to your FE network
9. Set up a water feed into the Solar Heater's side to auto-produce salt via evaporation

### Late Game

10. Run multiple Solar Heaters in parallel feeding one Heat Battery for reliable superheated output
11. Use **salt blocks** as heater input (10× the molten salt per operation) for maximum throughput
12. Use **Andesite Solar Panels** to supplement your Create kinetic network with free rotational power
13. Chain **Kinetic Batteries** to buffer rotational energy overnight or during rain