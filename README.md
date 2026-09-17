# Rubix

![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-7F52FF?logo=kotlin&logoColor=white)
![Version](https://img.shields.io/badge/version-3.0.0-238BFF)
![JVM checks](https://img.shields.io/badge/JVM_tests-71_passed-15824F)


An offline Android puzzle workspace for scanning and solving 2x2, 3x3, 4x4 and Pyraminx puzzles, practising on virtual cubes, timing solves and learning the beginner method.

## What is inside

- **Solve** — dedicated camera geometry, gallery import, color review/editing, physical validation and replay-verified animated guides for 2x2, 3x3, 4x4 and Pyraminx.
- **Practice** — a focused home for the virtual cube and 3x3 timer.
- **Virtual cube** — a mode hub for Free play, Challenge and Guided solve across 2x2 through 7x7, with animated outer, inner and wide turns.
- **Cube timer** — a hold-to-ready 3x3 timer with valid scrambles, large stop target, personal records and responsive controls.
- **Learn** — Rookie, Experienced and Veteran tracks with visual problem/solved examples, recognition cues, plans and playable practice states.

Rubix uses a quiet dark theme, compact navigation, clear pressed and selected states, system-aware haptics, and display colors that can be tuned for a particular physical puzzle. Color initials are available only inside color editors and stay off every cube preview and solving guide.

## Install

**[Download Rubix.apk](dist/Rubix.apk)** - Android 8.0 or newer, ARM 32-bit and 64-bit phones. There is one installable download. Android uses APK files rather than Windows EXE files.

This APK is development-signed. Install it by opening the downloaded file on your phone, or run:

```sh
adb install -r dist/Rubix.apk
```

The accompanying `dist/Rubix.apk.sha256` lets you check download integrity.

## Solve your cube

1. Tap **Scan my cube** and choose 2x2, 3x3, 4x4 or Pyraminx.
2. Match the preparation screen. The 3x3 tutorial begins white-up and green-front; centerless 2x2 and 4x4 scans use the white-red-green reference corner; Pyraminx uses the guided green, red, blue and yellow face order.
3. Capture each face with the live camera or gallery. Rubix uses a 2x2, 3x3, 4x4 or triangular detector for the selected puzzle.
4. Review the full net. **Edit colors** opens a focused face editor with counts, optional initials, undo, redo, Cancel and Done.
5. Solve only after validation succeeds, match the starting position, then follow the animated model. Autoplay, Pause, Previous, Next, replay, restart and current-state correction remain available.

Keep the same holding orientation while following the guide. Clockwise is viewed directly at the face being turned.

## Capture and personalize

The scanner supports a live camera, hardware flash where available, and the Android photo picker. Import one cube face at a time in the guided face/top order. Photos are resized, their orientation is normalized, and the expected center is checked before acceptance. Gallery access does not require broad storage permission. Check every imported sticker in review.

Manual entry uses a large 3D preview, one focused face at a time, persistent brush selection, fixed centers, live per-color counts and undo. Scan review uses a separate editor with a labeled six-color palette, initials, undo and redo. Face selection and virtual lesson context survive UI restoration.

Settings is available from the three root areas in a spacious bottom sheet. Customize hue, saturation and brightness for each display color, reset the palette, toggle haptics, touch sounds and screen-awake handling, or adjust animation and autoplay timing. The initials switch lives only in color editors. Display colors do not alter camera classification.

## A clearer guide

- Deeper, distinct sticker colors with optional initials restricted to color editing.
- Persistent display-color and haptic settings under **Settings**.
- Touch feedback for selections and moves, plus capture/completion feedback; Android's system settings are respected.
- Double turns animate as two quarter-turns, with numbered progress and a brief pause. An inverse turn is one counter-clockwise quarter-turn. Next becomes available when the preview finishes.
- Automatic step progression with a visible pause/play control and a configurable 1–2 second interval after each animation.
- Fixed Previous/Next controls for missed steps, scrollable instructions, and extra actions tucked into **More**.
- **Correct cube colors** in the guide lets you enter the actual current stickers and calculate a fresh verified solution. Cancel restores the original guide. Match the holding orientation before editing.
- Known-turn mistake recovery, restart, replay, holding help, and cube-view reset.
- Screen stays awake during scanning and active guidance. Confirmed progress survives saved-state restoration.

## Build

Use Android Studio with **JDK 17**, **Android SDK 36**, and the SDK build tools. Open this repository and let Android Studio create `local.properties` with your SDK path.

```powershell
.\gradlew.bat :core:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

On macOS/Linux use `./gradlew` with the same tasks. The APK is produced at `app/build/outputs/apk/debug/app-debug.apk`. The default APK includes both ARM architectures. For an x86_64 emulator development build use `-PtestAbi=x86_64`; do not replace the phone download with that build.

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat :app:assembleRelease
```

Instrumented tests need a connected device or accelerated emulator. Release builds enable R8 and require your own signing configuration; the unsigned release output is not an installable download.

## Project structure

```text
app/       Android application: camera, vision, rendering and Compose screens
core/      Android-independent cube geometry, validation and solving
vendor/    Pinned min2phase source and its upstream license documentation
docs/      Architecture, verification notes and changelog
gradle/    Reproducible Gradle wrapper
dist/      One phone APK and its SHA-256 checksum
```

The UI is split by screen under `app/src/main/java/com/cubeguide/ui`. `CubeViewModel` owns navigation, scan review, background solving, corrections and confirmed guide state. `AppPreferences` stores display and feedback choices.

## Solution quality

For 3x3, Rubix searches within the 20 face-turn bound and spends extra probes improving its first result. It also merges same-face turns across commuting opposite faces. Under 10 cannot be guaranteed for arbitrary scrambles. The 2x2, Pyraminx and 4x4 engines use their own state models and validators. Every returned sequence is replayed by the matching engine and must reach a solved state before the guide is shown.

## Verification and limits

JVM tests cover puzzle permutations, physical validity, solver replay, 2x2 scan orientation, Pyraminx facelets, 4x4 wide turns, all 24 3x3 holding orientations, guide progression, editing and current-state correction. End-to-end session tests feed synthetic captures through each new classifier and replay every solution. Android instrumentation covers the existing 3x3 practice flow and synthetic vision fixtures. Real-camera measurement is still required for all puzzle detectors. See [testing notes](docs/TESTING.md) for executed checks and the physical acceptance matrix.

The latest layout, photo import, flash, sounds, haptics and real-camera behavior still need physical-device verification. Synthetic fixtures do not establish real-world scan accuracy. Lighting, glare, cube shades and capture orientation can require manual sticker correction.

## Dependencies and licenses

Kotlin, Jetpack Compose, CameraX, OpenCV, pinned min2phase and WCA TNoodle threephase sources, plus Rubix's independent 2x2 and Pyraminx state engines. Upstream notices and licenses ship with the application. See [third-party notices](THIRD_PARTY_NOTICES.md), [architecture](docs/ARCHITECTURE.md) and [changelog](docs/CHANGELOG.md).
