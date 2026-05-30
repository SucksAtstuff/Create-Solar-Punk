Add Steam Turbine multiblock, Steam fluid, and ponder scenes

### Steam Turbine
New multiblock structure: sealed 7x7 floor of Turbine Casing, any
number of blade layers (ring + Turbine Rotor + Turbine Blades in a
plus pattern), and a sealed 7x7 top cap with a Rotor at center.
Rotational force exits from the cap rotor's top face. Structure is
validated on every scan tick.

- Turbine Rotor: kinetic generator; master (lowest rotor) drives the
  column. Speed and SU scale with height; efficiency scales with blade
  count and type (Andesite 0.7x, Brass 1.0x, base 10% with no blades)
- Turbine Casing / Turbine Casing Glass: 7x7 shell blocks with
  Create-style OMNIDIRECTIONAL connected textures (industrial iron
  casing texture). Glass variant is interchangeable everywhere.
- Andesite Turbine Blade / Brass Turbine Blade: placed in plus pattern
  inside each blade layer, 2 per arm, 4 arms = 8 per layer

### Steam fluid
New Steam fluid (source + flowing + bucket + fluid type). Consumed by
the master rotor; condensate water is produced as a byproduct.

### Fluid piping
Steam pipes into any face of the outer Turbine Casing wall; condensate
water drains from any casing face. Implemented via registerBlock
capability that scans for the nearest master rotor.

### Crafting
- Turbine Casing: right-click a Block of Industrial Iron with a Zinc
  Ingot (or use a Deployer). Works via create:item_application.
- Turbine Casing Glass: right-click a Turbine Casing with Glass.

### Ponder
Two scenes registered for all five turbine blocks:
- "Building the Steam Turbine" — walks through floor, blade layers, and
  cap step by step with explanations for each component
- "Maximum Efficiency Turbine" — shows a full 7-blade-layer all-brass
  turbine and explains height, blade efficiency, and the 20-layer max

### Fixes
- Resolved all unchecked/unsafe compiler warnings without suppression
  where avoidable (MultiBlockFluidBE, BrassSolarPanelBlock)
- Migrated KineticSprinklerItem off deprecated initializeClient to
  RegisterClientExtensionsEvent
- Removed deprecated EventBusSubscriber.Bus.MOD (bus now auto-inferred)
- Dead Grass: no longer drops when broken; renders with cutout (no
  black background)

### Version
Bumped to 0.3.0-1.21.1