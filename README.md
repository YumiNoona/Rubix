# Cube Guide

An offline Android app that scans a 3x3 Rubik's Cube and guides you through solving it, one turn at a time.

## Install

**[Download CubeGuide.apk](dist/CubeGuide.apk)** - Android 8.0 or newer, ARM 32-bit and 64-bit phones. There is one installable download. Android uses APK files rather than Windows EXE files.

This APK is development-signed. Install it by opening the downloaded file on your phone, or run:

```sh
adb install -r dist/CubeGuide.apk
```

The accompanying `dist/CubeGuide.apk.sha256` lets you check download integrity.

## Solve your cube

1. Select **Scan my cube** and follow the face and top-center instructions for all six sides. Manual entry and a practice cube are also available.
2. **Check your scan** using the 3D preview and editable face grid. Turn on **Color initials** if you want W, R, G, Y, O and B labels. Correct stickers, rotate a scanned face, or rescan before solving.
3. Tap **Solve this cube**. Invalid scans show a correction message; solving shows progress. A scan with exactly one valid face-rotation reconstruction can be aligned automatically.
4. Match the **Starting position**: white center facing you and blue center on top by default. You can select another front center; the cube and solution change reference frame together.
5. Follow the animated cube and turn instruction. Confirm each physical move with **Next**. **Previous** explains the inverse physical move before stepping back.

Keep the same holding orientation while following the guide. Clockwise is viewed directly at the face being turned.

## A clearer guide

- Deeper, distinct sticker colors with contrasting optional initials on the net and 3D cube.
- Persistent color-label and haptic settings under **Display & feedback**.
- Touch feedback for selections and moves, plus capture/completion feedback; Android's system settings are respected.
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

## Verification and limits

JVM tests cover cube permutations, physical validity, solver replay, scan rotations, all 24 holding orientations, guide progression, undo, restoration and current-state correction. Android instrumentation covers the practice flow and synthetic vision fixtures. See [testing notes](docs/TESTING.md) for executed checks and the physical acceptance matrix.

The latest layout, haptics and real-camera behavior still need physical-device verification. Synthetic fixtures do not establish real-world scan accuracy. Lighting, glare, cube shades and capture orientation can require manual sticker correction.

## Dependencies and licenses

Kotlin, Jetpack Compose, CameraX, OpenCV and a pinned min2phase solver. Upstream solver Java is retained unmodified. Its README provides an MIT license grant; notices ship in the application. See [third-party notices](THIRD_PARTY_NOTICES.md), [architecture](docs/ARCHITECTURE.md) and [changelog](docs/CHANGELOG.md).
