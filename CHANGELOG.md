# 0.1.0-1.21.1

### Solar Power Tower

Added the **Solar Power Tower** multiblock and **Solar Mirror** heliostat block.

The tower concentrates reflected sunlight to produce large quantities of Molten Salt from water. Build it in a 1×1, 2×2, or 3×3 footprint (maximum heights 5, 10, and 20 blocks respectively) and surround the sides with Solar Mirrors to increase output. Mirror efficiency follows a triangle curve — filling roughly half the available wall faces is optimal; over-mirroring past twice the optimal count reduces output to zero.

The tower requires a minimum **3×3 footprint** to produce anything. Water consumption scales with footprint area (9:1 water-to-salt for a full 3×3 tower), making larger towers a serious infrastructure investment.

The Solar Mirror is a wall-mountable heliostat that can be placed on floors, walls, and ceilings. It must touch the tower's side face directly to count. Use a Create wrench to cycle it through all six facing orientations.

Both blocks are wrenchable: sneak + right-click breaks them and returns them as items.

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
