---
name: verify
description: Verify VimiCalc changes by driving the running JavaFX app headlessly and capturing screenshots as evidence. Use after changing model/controller/view code that affects runtime behavior.
---

# Verifying VimiCalc changes end-to-end

VimiCalc is a JavaFX canvas app; its surface is pixels + key input. Verify by
launching the real app under Xvfb, injecting key events with TestFX, and
capturing `scene.snapshot` PNGs.

## Requirements / gotchas

- **JDK 17 required** (the Gradle toolchain pins it) but it may not be
  installed system-wide. If `xvfb-run ./gradlew test` fails with "Cannot find
  a Java installation", download a Temurin 17 tarball, extract it anywhere,
  and pass `-Dorg.gradle.java.installations.paths=<jdk17-dir>` (plus
  `--no-daemon` so a daemon started without DISPLAY isn't reused).
- **TestFX `robot.write()` does not work** — the Controller reads
  `event.getText()` from KEY_PRESSED. Type real key codes
  (`robot.type(KeyCode.X)`); uppercase characters need an explicit
  SHIFT press/release around the key.
- Colon commands are entered with `;` (SEMICOLON) and committed with ENTER.
  Unrecognized commands are silently ignored — double-check spelling/case.
- The cursor starts at **B2**, not A1.
- The cell selector renders as an opaque blue rectangle covering the selected
  cell's background color — move the cursor away before asserting on colors.
- The app logs verbosely per keypress (cursor coords, camera offsets, full
  cell dumps) — captured in `build/test-results/test/TEST-*.xml`, useful as
  model-state evidence alongside the screenshots.

## Recipe

1. Write a throwaway "probe director" test class in
   `src/test/java/vimicalc/controller/` (same package gives access to
   `Controller` fields). Copy the `@Start` launcher from
   `MergeInteractionUiTest` (loads GUI.fxml at 900x600, wires
   `scene.setOnKeyPressed`). Drive the scenario with `robot.type(...)`
   (~40–80ms sleeps between keys) and save checkpoint PNGs:
   `scene.snapshot(null)` + `SwingFXUtils.fromFXImage` + `ImageIO.write`,
   run on the FX thread.
2. Run it headlessly:
   ```bash
   xvfb-run -a ./gradlew --no-daemon \
     -Dorg.gradle.java.installations.paths=<jdk17-dir> \
     test --tests 'vimicalc.controller.<ProbeClass>'
   ```
3. Inspect the PNGs; for precise checks, pixel-scan them (e.g. Python/PIL)
   for expected fill colors and rectangle geometry.
4. Delete the probe class afterwards; don't commit it.

For a full screen recording instead of stills: start `Xvfb :99` and an
ffmpeg `x11grab` capture of `:99` manually, run `xset -r off` (key
auto-repeat floods input during long FX stalls), then run the director test
with `DISPLAY=:99` set.
