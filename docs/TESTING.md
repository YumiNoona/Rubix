# Verification and physical acceptance

## Executed locally

- `:core:test`: covers the 2×2, 3×3 and 4×4 state models, inverses, doubles, wide turns, physical validators, randomized scrambles and replay-verified solver output.
- `:app:testDebugUnitTest`: covers color classification, capture stability, scan orientation, focused editing, guide progression, complete synthetic scan-to-solved sessions for 2×2 and 4×4, and balanced capture-review-edit sessions for 5×5 through 7×7.
- `:app:lintDebug`: passes with no errors. Dependency-upgrade/target-API notices are expected because the build deliberately pins a compatible stable SDK 36 toolchain.
- `:app:assembleDebug`: installable development APK built.
- `:app:assembleDebugAndroidTest`: instrumentation APK compiles.
- `:app:assembleRelease`: R8 release build passes; output is unsigned.

## Runtime checks not executed

The host had no connected Android device and no configured emulator. An API 35 x86_64 test image and AVD were created. Emulator startup reported that the Android hypervisor driver was absent; a bounded software-acceleration fallback exited with Windows access violation `-1073741819`. No system driver was installed or boot settings changed. Consequently, Compose screenshot QA and Android-native OpenCV fixture execution are pending. Compiling those tests is not a passing runtime result.

Run on a connected Android device or an accelerated emulator:
```
.\gradlew.bat :app:connectedDebugAndroidTest
```

`AppFlowTest` exercises practice -> review -> solve -> orientation -> move confirmation -> completion, manual invalid correction, and camera fallback. It writes home/review/guide/solved PNGs into the app's external-files directory for inspection. `VisionInstrumentedTest` generates six perspective/lighting fixture combinations and negative uniform/dark frames. Synthetic images test geometric/code behavior, not real camera accuracy.

## Required physical acceptance matrix

Use at least three Android phone vendors, entry/mid/high performance devices, Android 8 and a current Android release, and multiple cube manufacturers (stickered and stickerless).

| Condition | Checks |
|---|---|
| Daylight, warm/cool indoor, mixed lighting | 9 correct colors per face; useful uncertainty and count feedback |
| Dim lighting and shadows | Dark-frame rejection; no false stable captures |
| Glossy stickers, white/yellow glare | Tilt guidance; uncertain stickers surfaced |
| Rotation +/-15 degrees and perspective +/-25 degrees | Corner ordering and corrected grid match physical stickers |
| Near/far distances and clipped corners | Actionable capture feedback, no partial face accepted |
| Different red/orange shades | Center-relative classification and confidence margin |
| Six scan poses | Each face's row/column direction agrees with canonical URFDLB layout |
| Incorrect top-side orientation | Review rotation/rescan repairs the state; no wrong solution shown |
| Permission denial, no camera, app backgrounding | Manual fallback; no leaked frames or bound use cases |
| Physical move execution and undo | Right layer/direction for every basic, inverse and double move |
| Error before Next and error after Next | Digital twin matches the modeled actual physical turn after recovery |
| Rotation/process recreation | Confirmed guide index and expected stickers restored |
| Airplane mode | Scanning, solving and 3D guidance function with no network |

Record detection/capture rate, incorrect confident color rate, scan duration, solver latency, preview responsiveness, dropped frames, thermal behavior, and memory. A consumer release requires successful complete solves across this matrix and visual inspection of small-phone/tablet/font-scale layouts. No camera accuracy, frame rate or latency claim is made without these measurements.

## Release

The supplied debug APK is development-signed. Release source enables R8 but needs your own signing configuration for distribution. Do not distribute the unsigned release artifact as an installable app. Preserve upstream source and bundled notices when distributing.

## 1.0.1 scan-review regression

The reported behavior was no visible response when tapping Solve on scan review. The existing invalid-state branch returned to the same page with its existing validation message. The original physical scan is not available, so its exact failure reason is unknown.

The updated review keeps the solve button and status visible, previews the captured 3D cube and six-face net, and shows color counts. Invalid solve attempts open a correction dialog; accepted attempts show an immediate progress indicator. Face-image rotation errors with exactly one valid reconstruction are aligned on a background dispatcher, solved and independently verified. Ambiguous reconstructions and incorrect color counts are rejected instead of guessed.

New tests cover uniquely rotated scans recovering the known original cube, ambiguous scans refusing automatic selection, unchanged valid scans, invalid counts, and review-to-guide advancement from a rotated scan. Instrumentation tests assert that Solve, Previous and Next are displayed without scrolling and that invalid attempts produce a visible correction dialog. Instrumentation execution remains pending on a connected phone; those tests compile.

## 1.0.2 text, orientation and layout update

Screenshots showed mojibake in the title, direction glyphs and recovery labels. App source edits now explicitly read/write UTF-8; text-based decorative symbols were removed and turn/check icons use Canvas geometry. `UiEncodingTest` decodes every app Kotlin source with strict UTF-8 and rejects corrupted-character markers.

A separate starting-position screen precedes the first physical move. White faces the user by default (blue on top for the standard scanned scheme), and another front center can be selected. A right-handed change of reference frame remaps all 54 stickers and every move. Core tests cover all 24 front/top combinations, all 18 move types commuting with that frame change, physical validity and completed solutions. App tests cover saved front choice, setup-to-guide confirmation, one-time remapping, undo/recovery after remapping and solved replay.

Guide controls now use a More menu; recovery uses staged, bounded scrollable choices. The cube Canvas clips its content and uses a smaller projection; its height adapts to the available screen. Text content remains scrollable while Previous/Next stay fixed. Instrumentation tests compile for setup labels and practice flow, but device screenshot QA remains pending. Existing user screenshots confirm earlier scanning and guide execution, not validation of this new build.

## 1.1.0 usability and current-state correction

Guide correction tests cover invalid edited stickers, cancellation, saved correction restoration and recalculation from an actual changed physical state. Initials and haptics persist as display preferences; screen-awake handling and the darker palette need device verification. The deliverable is one ARM 32/64-bit APK.

## 1.2.0 Rubix redesign

Two further JVM regressions cover photo-face acceptance (wrong/missing centers rejected, all six faces reconstructed in canonical order) and manual paint undo/restoration. Android flow selectors match the new home and paint flow. Native photo decoding, torch, system touch sound and the home animation require connected-device runtime QA. Total JVM test methods: 20.

## 1.2.1 recognition and interaction regressions

JVM regressions cover red hue wrap-around, stable-frame median sampling, reset behavior and reduced confidence for distant calibration outliers. Total JVM test methods: 23. The face-focus/color-sheet and animation changes require Android runtime checks, especially double turns, fast taps, replay, hidden-face rendering and process restoration. Real camera accuracy remains unmeasured.

## 1.3.0 white-glare and solution bounds

A regression reproduces 13 independently classified white stickers and verifies that global center-anchored assignment restores nine of every color while leaving forced choices uncertain. Sixty seeded scrambles now assert verified solutions of at most 20 face turns. Synthetic fixtures and compilation pass; physical camera tests remain required to tune glare thresholds against real cubes and phones.

## 2.3.0 multi-puzzle solve gates

Synthetic end-to-end sessions capture all six 2×2 faces and all six 4×4 faces. Each session asserts successful review validation, runs the puzzle-specific solver through `PuzzleSolverGate`, applies every guide move and checks the final state is solved. Editor undo, redo and cancel restoration are covered in the 2×2 session. Android UI tests compile against the regular-cube catalog routes and shared flow; physical-device acceptance remains required for camera geometry, face-order instructions, flash, gallery orientation and responsive rendering.
# 2.2.0 focused editing, learning and solid-cube checks

- JVM tests cover dedicated editor restoration, face navigation, undo/redo/cancel and saved lesson practice context. Total JVM test methods: 30.
- The default 3x3 virtual playground directly renders the solver's `CubeView` from the virtual sticker state, eliminating the separate exploded 3x3 geometry path.
- Compose UI tests compile against the new Material icon controls, scan progress states, review actions, editor screen, consolidated guide header and autoplay semantics.
- Physical camera, touch and responsive-layout acceptance remains pending because this Windows host has no connected device and cannot boot the installed emulator without a hypervisor driver.

# 2.1.0 navigation, rendering and autoplay checks

- Android Compose tests cover the updated root labels, focused manual editor, solving transition and automatic advancement while Previous and Next remain present.
- Virtual rendering visibility was checked at the default camera transform: exactly the top, right and front sticker planes pass the outward-normal camera test.
- Layout constraints now force action labels, timer values, statistics and navigation labels to one line.
- The Android UI suite compiles. Runtime screenshot and camera acceptance still require a physical device because the installed x86_64 image cannot start without a Windows hypervisor.

# 2.0.0 UI shell and virtual puzzle checks

- `VirtualCubeTest` covers every outer face and inverse on 2x2 through 7x7 cubes, inner and wide move round trips, four quarter-turn identity, non-repeating scramble generation, full scramble reversal, and encoded state/history restoration.
- `TimerFormattingTest` covers zero, sub-minute and minute-plus stopwatch formatting.
- Android Compose flow tests were updated for the new Home and focused manual-entry labels and compile against the redesigned navigation.
- `lintDebug` and the dual-ABI debug APK build pass. A connected physical device or accelerated emulator is still required for camera, touch-layout and screenshot acceptance tests.
