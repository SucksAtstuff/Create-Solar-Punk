# Trailer Plan - Create: Solarpunk

Cinematic tour, ~90 seconds. Follows one resource chain start to finish inside a single
showcase build, then pulls back to show the whole system running - same approach Create's
own trailers use, rather than cutting between disconnected feature clips.

## Narrative arc

One build "comes alive" in sequence: raw sun/heat -> stored energy -> machines running.

| Time | Beat | What it shows |
|---|---|---|
| 0:00-0:08 | Establish | Wide static/slow-push shot of the complex at golden hour - silhouettes of solar mirrors, a Geyser Vent steaming in the distance. No motion yet. Sets the tone. |
| 0:08-0:20 | Raw resource | Salt ore breaking, salt crystals in hand/chest. Quick, punchy cuts. |
| 0:20-0:35 | Solar heat chain | Solar Power Tower mirrors rotating to track sun -> molten salt glowing through the pipe -> into the Heat Battery. Best glow/VFX shot - hold it a beat longer. |
| 0:35-0:45 | Payoff #1 | Heat Battery feeding a Create boiler -> steam engine/train kicks into motion. Cut on the beat when the engine starts moving. |
| 0:45-0:58 | Electric chain | Pan across a field of Andesite/Brass panels, then Kinetic Battery flywheel spinning up. |
| 0:58-1:15 | Biofuel chain | Biomass Gasifier -> Fermentation Vat (tall multiblock, low-angle tilt up) -> Biofuel Engine igniting. |
| 1:15-1:22 | Geothermal punch | Geyser Cap eruption, timed hard to a music hit. The puff sound now fires reliably on its own schedule - use it as the loudest single cut. |
| 1:22-1:30 | Wide pull-back | Drone-style pull-back/rise showing the whole complex operating together. Logo card fades in. |

On-screen text should stay minimal and player-facing, same voice as the changelog - short
labels, not descriptions: `Solar heat.` `Stored as molten salt.` `Biofuel.` `Geothermal.`
then the title card.

## What to build

One compact "showcase valley," not scattered builds - desert or badlands (matches the
salt/solar worldgen tags), everything within a short flight path so the camera can move
through it continuously:

- Geyser Vent + Cap near the edge (naturally-spawning feel)
- Solar Power Tower as the visual centerpiece, mirrors visible from most angles
- A small salt mine/quarry face nearby
- Heat Battery -> boiler -> a short Create train line or piston contraption as the kinetic payoff
- A field of Andesite/Brass panels on one side
- Fermentation Vat built near a cliff so the tilt-up shot reads well
- Keep builds functional, not just for-show - actually running (SU flowing, molten salt
  visibly filling) reads better on camera than static blocks

## Recording setup

- **Replay Mod** over raw F1 footage - lets you fly smooth camera paths, re-time cuts, and
  re-render at higher settings/framerate after the fact. Worth the setup time for this style.
- `F1` to hide HUD, `F3` off, disable other mods' overlays (JEI button, Create goggle text)
  before filming.
- `/gamerule doWeatherCycle false`, `/gamerule doDaylightCycle false`, and manually
  `/time set` per shot to control lighting instead of fighting it.
- Lower FOV (~50-60) for a less fisheye, more large-scale cinematic look than default 70.
- Optional but high value: a shader pack (Complementary or BSL) for bloom on the molten salt
  pipe and Biofuel Engine fire - molten salt now has a real light level (13), so a shader's
  bloom pass will pick it up as an actual glow, not just a bright texture.

## Audio

The recent sound pass (subtitles, remastered levels, synced geyser puff) is trailer-ready
material - don't bury it under music.

- Sync hard cuts/hits in the music track to real in-game sound moments (geyser burst, fusion
  reactor startup hum kicking in), or
- Do a quiet cold-open with just ambient machine sound before music comes in at 0:08.