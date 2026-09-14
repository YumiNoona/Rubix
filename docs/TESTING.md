# Verification and physical acceptance

## Executed locally

- `:core:test`: 10 test methods passed, covering all 18 face moves against independent min2phase scrambles, inverses, doubles, four-turn identity, 60 seeded random scrambles and verified solver solutions, invalid color/center counts, impossible/mirrored pieces, flips, twists, parity, optimizer preservation, strict parsing and solved states.
- `:app:testDebugUnitTest`: 12 test methods passed, covering all six LAB/HSV anchor classifications, uncertain red/orange colors, minimum capture time/frame count, moved-corner and changed-color resets, manual invalid-state rejection, full guide progression, inverse Back, saved-state restoration, known-turn recovery and replay.
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

JVM regressions cover red hue wrap-around, stable-frame median sampling, reset behavior and reduced confidence for distant calibration outliers. Total JVM test methods: 22. The face-focus/color-sheet and animation changes require Android runtime checks, especially double turns, fast taps, replay, hidden-face rendering and process restoration. Real camera accuracy remains unmeasured.
