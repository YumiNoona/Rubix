# Rubix

![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-7F52FF?logo=kotlin&logoColor=white)
![Version](https://img.shields.io/badge/version-1.3.0-2463BB)
![JVM checks](https://img.shields.io/badge/JVM_tests-23_passed-15824F)


An offline Android app that scans a 3x3 Rubik's Cube and guides you through solving it, one turn at a time.

## Install

**[Download Rubix.apk](dist/Rubix.apk)** - Android 8.0 or newer, ARM 32-bit and 64-bit phones. There is one installable download. Android uses APK files rather than Windows EXE files.

This APK is development-signed. Install it by opening the downloaded file on your phone, or run:

```sh
adb install -r dist/Rubix.apk
```

The accompanying `dist/Rubix.apk.sha256` lets you check download integrity.

## Solve your cube

1. Select **Scan your cube** and follow the face and top-center instructions for all six sides. Manual entry and a practice cube are also available.
2. Review your scan using the 3D preview and editable face grid. Turn on **Color initials** if you want W, R, G, Y, O and B labels. Correct stickers, rotate a scanned face, or rescan before solving.
3. Tap **Solve this cube**. Invalid scans show a correction message; solving shows progress. A scan with exactly one valid face-rotation reconstruction can be aligned automatically.
4. Match the **Starting position**: white center facing you and blue center on top by default. You can select another front center; the cube and solution change reference frame together.
5. Follow the animated cube and turn instruction. Confirm each physical move with **Next**. **Previous** explains the inverse physical move before stepping back.

Keep the same holding orientation while following the guide. Clockwise is viewed directly at the face being turned.

## Capture and personalize

The scanner supports a live camera, hardware flash where available, and the Android photo picker. Import one cube face at a time in the guided face/top order. Photos are resized, their orientation is normalized, and the expected center is checked before acceptance. Gallery access does not require broad storage permission. Check every imported sticker in review.

Manual entry supports a selected-color paint brush, fixed centers, a large face editor, per-color counts and undo. Sticker selection opens a labeled six-color bottom sheet with the current color outlined. The selected face and brush survive edits and saved UI restoration.

Settings is available from home and active screens. Customize hue, saturation and brightness for each display color, reset the palette, toggle initials, haptics, touch sounds and screen-awake handling, or adjust animation speed. Display colors do not alter camera classification. Touch sounds use Android's native click and respect system sound settings.

## A clearer guide

- Deeper, distinct sticker colors with contrasting optional initials on the net and 3D cube.
- Persistent color-label and haptic settings under **Settings**.
- Touch feedback for selections and moves, plus capture/completion feedback; Android's system settings are respected.
- Double turns animate as two quarter-turns, with numbered progress and a brief pause. An inverse turn is one counter-clockwise quarter-turn. Next becomes available when the preview finishes.
- Fixed Previous/Next controls, scrollable instructions, and extra actions tucked into **More**.
- **Correct cube colors** in the guide lets you enter the actual current stickers and calculate a fresh verified solution. Cancel restores the original guide. Match the holding orientation before editing.
- Known-turn mistake recovery, restart, replay, holding help, and cube-view reset.
- Screen stays awake during scanning and active guidance. Confirmed progress survives saved-state restoration.

## Privacy

Scanning, image processing, validation and solving run on your device. Camera frames are not saved or uploaded. The app has no Internet permission, account, analytics, or remote solver. Camera permission is optional.

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

Rubix searches within the 20 face-turn bound and spends extra probes improving its first result. It also merges same-face turns across commuting opposite faces. Under 10 cannot be guaranteed for arbitrary scrambles: some states require 20 face turns, and optimal random states are usually much closer to 18 than 10. A half turn such as `R2` counts as one solver step but two physical quarter-turns in the guide.

## Verification and limits

JVM tests cover cube permutations, physical validity, solver replay, scan rotations, all 24 holding orientations, guide progression, undo, restoration and current-state correction. Android instrumentation covers the practice flow and synthetic vision fixtures. Final classification globally assigns exactly nine stickers to each center color, so glare cannot create three or four extra whites. Forced assignments stay highlighted when the image evidence is weak. Circular hue sampling keeps reds around the HSV wrap point together; live capture uses a median across stable frames. Out-of-calibration colors receive lower confidence rather than a confident forced label. These changes need real-camera measurement. See [testing notes](docs/TESTING.md) for executed checks and the physical acceptance matrix.

The latest layout, photo import, flash, sounds, haptics and real-camera behavior still need physical-device verification. Synthetic fixtures do not establish real-world scan accuracy. Lighting, glare, cube shades and capture orientation can require manual sticker correction.

## Dependencies and licenses

Kotlin, Jetpack Compose, CameraX, OpenCV and a pinned min2phase solver. Upstream solver Java is retained unmodified. Its README provides an MIT license grant; notices ship in the application. See [third-party notices](THIRD_PARTY_NOTICES.md), [architecture](docs/ARCHITECTURE.md) and [changelog](docs/CHANGELOG.md).
