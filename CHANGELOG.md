# 0.2.0-1.21.1

### Kinetic Sprinkler

Added the **Kinetic Sprinkler**. Pipe water into it from any face and it will hydrate farmland and accelerate crop growth in a 5x5 area below it.

### Bug Fixes

- Fixed Solar Mirror blocks becoming excessively dark when another mirror is placed directly above them on the tower.                                                                                                                                                                              


### Biochar

Added **Biochar**, a byproduct of the Biomass Gasifier. Each fuel item burned produces one Biochar in the Gasifier's output slot. The Gasifier pauses if the output is full, so keep it drained with a funnel or chute.

Biochar acts as a direct bonemeal substitute - right-click crops, grass, and other growable blocks to fertilize them. It is obtained only from the Gasifier, closing the biofuel loop with a useful agricultural byproduct.

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