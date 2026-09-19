# Architecture and research decisions

## Boundaries

`core` is a JVM Kotlin module for regular cubes: 2×2, 3×3 and 4×4 state models, moves, physical validators, scan-orientation rules and solver adapters. Integer geometry maps a sticker's position and outward normal into a permutation for each clockwise layer turn. Each inverse and double turn uses the same engine. Independent reference scrambles in tests verify the mapping instead of comparing the engine only with itself.

`app/camera` owns lifecycle binding, frame backpressure, RGBA bitmap conversion and rotation, frame throttling, executor cleanup and permission fallback. No frames are stored or uploaded.

`app/vision` finds convex quadrilateral contours, rejects extreme area/aspect ratios, normalizes a face to 300 pixels, samples sticker interiors away from borders, filters dark/glare pixels, and takes medians in LAB/HSV. Classification uses captured center anchors, weighted LAB distance with a circular hue contribution, and a nearest/second-nearest confidence margin. Initial nominal HSV classification checks that the user is presenting the requested center; it is not the final 54-sticker classifier. A face requires seven stable samples spanning at least 850 ms; moved corners or changed samples reset that window.

`app/rendering` builds cubie and sticker quads from the same core geometry. Rodrigues rotation animates only geometry in the selected layer. Camera yaw/pitch are interactive. Perspective projection and depth ordering render the cube on Compose Canvas without a second rendering framework. This provides actual 3D layer geometry while keeping the runtime small. The integer move commits only after user confirmation.

`app/ui/CubeViewModel` owns screen transitions, captures, review, solver dispatch, progress, confirmed digital twin, and saved state. A generation token discards stale solver results after navigation. Solver work runs on Default; camera analysis runs on its executor; UI changes run on the main thread. Guided and read-only replay modes are separate.

## Validation pipeline

Color counts -> unique centers -> explicit 12 edge identities -> explicit 8 cyclic corner identities -> min2phase edge/corner orientations and parity -> solve -> adjacent-face simplification -> apply every solution move independently -> verify solved.

Explicit piece validation is necessary because malformed input can fall back to a default cubie in the upstream converter. The application rejects those arrangements before relying on the library's parity/orientation tests. Local impossible-piece issues include suspect sticker indices for correction.

## Dependencies

- AGP 8.13.1 / Gradle 8.14.3 / Kotlin and Compose compiler plugin 2.2.21 / compile and target API 36 / min API 26.
- Compose BOM 2026.03.00, Activity 1.12.4, Lifecycle 2.10.0: compatible stable set tested with the installed SDK/build toolchain. Later Compose releases inspected during development required AGP 9.1+ and API 37; no automatic upgrades are applied.
- CameraX 1.6.2: lifecycle camera management and bounded ImageAnalysis with KEEP_ONLY_LATEST.
- OpenCV 4.13.0: packaged native contour, perspective and color conversion operations; no separate OpenCV Manager install required.
- min2phase: pinned unmodified source, MIT grant in its README. No remote solver API or runtime table downloads.
- WCA TNoodle threephase: pinned 4x4 reduction/search source under its GPLv3 license, with source headers and bundled notices preserved.

A native custom pipeline or Filament engine would add build and rendering complexity without evidence of a benefit at this 300-pixel ROI / 26-cubie scale. K-means is unnecessary once six centers supply labeled anchors; ambiguous nearest-anchor assignments are surfaced for correction rather than forced to nine colors each.

## Primary references

- CameraX image analysis: https://developer.android.com/media/camera/camerax/analyze
- CameraX current releases: https://developer.android.com/jetpack/androidx/releases/camera
- Android architecture: https://developer.android.com/topic/architecture/recommendations
- Compose BOM mapping: https://developer.android.com/develop/ui/compose/bom/bom-mapping
- OpenCV Android packaging: https://opencv.org/opencv4android-usage-models/
- OpenCV contour approximation: https://docs.opencv.org/4.13.0/da/d0c/tutorial_bounding_rects_circles.html
- OpenCV perspective transforms: https://docs.opencv.org/4.13.0/da/d6e/tutorial_py_geometric_transformations.html
- OpenCV color spaces: https://docs.opencv.org/4.13.0/df/d9d/tutorial_py_colorspaces.html
- min2phase implementation, algorithm, APIs and license: https://github.com/cs0x7f/min2phase

Existing scanner projects were considered as reference categories. No scanner code from an unverified-license application was copied. This implementation uses the documented OpenCV geometry/color APIs and the licensed solver.

## Focused interaction shell (2.2.0)

The 3x3 virtual playground converts its `VirtualCube` sticker list directly into the shared `CubeState` renderer used by Home and Guide. Larger practice sizes retain the generalized outer-layer renderer. Learn lesson definitions contain a valid scramble, an automatically derived inverse solution, a recognition cue and a plan; opening practice persists the exact lesson state through recreation.

Scan capture exposes six compact face states while the camera remains automatic. Review and editing are separate navigation states: review owns Net/3D inspection and the final decision, while the editor owns face selection, painting, initials and bounded undo/redo histories. Cancel restores the complete pre-edit cube. Detail Back actions return to their actual parent screen; the guide owns its Back, numbered step, play/pause and overflow controls in one header.

## Scan review and guidance update (1.0.1)

The review content scrolls separately from a fixed solve/status panel. Color errors always produce a correction dialog. The guide uses a fixed Previous/Next footer, a projected animated cube, and a 2D preview of the affected face viewed straight on.

`ScanOrientationResolver` tries the 4^6 face-image rotations after correct counts/centers but invalid piece validation. It deduplicates identical sticker states and accepts only a single physically valid reconstruction. It preserves already-valid input and never forces colors to satisfy counts. Resolution runs alongside solving on the background dispatcher. Reported aligned faces are shown in starting-orientation guidance. No partial solution is displayed without a full replay verification.

## Starting orientation (1.0.2)

`HoldingOrientation` constructs forward/up/right axes from adjacent face normals. It changes reference frame for positions and outward normals, producing a complete sticker permutation. Face moves map by their outward normal, retaining turn count because the basis is right-handed. Setup defaults to the white center facing the user. Confirmation remaps the verified initial state and move list exactly once and verifies the rebased solution before entering the guide. Recovery operates in that already-selected frame and does not require a new orientation setup.

Source read/write operations must specify UTF-8 explicitly. Direction icons are drawn geometry rather than decorative Unicode strings. Source encoding regression tests guard against the observed mojibake.

## Preferences and correction (1.1.0)

Compose screens live in separate UI files. SharedPreferences persists initials and haptics through AppPreferences. Android touch feedback respects system preferences; a DisposableEffect restores the previous keep-screen-on value when leaving active screens. Guide correction saves the original current state for cancellation and process restoration. Accepted edits are physically validated and independently replay-verified, then start a fresh guide in the existing holding frame. Invalid manual edits never invoke scan-rotation guessing.

## Rubix capture and display (1.2.0)

Photo picker input is decoded on Default with a 1280-pixel bound and software allocation. ImageDecoder normalizes image orientation on API 28+; older devices use BitmapFactory and [AndroidX ExifInterface 1.4.2](https://developer.android.com/jetpack/androidx/releases/exifinterface) for rotation/reflection. Detected faces require explicit confirmation and the expected center, then enter the same center-calibrated classification pipeline as the live camera. Gallery selection suspends camera binding, preventing simultaneous live captures. Torch follows CameraX hardware availability and is switched off on disposal.

Display palette overrides are preferences only; solver identity and camera anchors remain independent. The home demonstration applies the inverse of a known scramble and animates real layer geometry. Manual editing uses a bounded undo history and persists the entry mode.

## Focus and rendering polish (1.2.1)

Review focus is saved local UI state. Uncertainty affects outlines, not face selection; only explicit sticker/face selection or a requested error review changes focus. StickerColorPicker owns the bottom sheet. Vision now has separate sampling, detection, classification and stability files. Live capture classifies the stable window median, with circular hue aggregation and absolute-distance confidence attenuation.

Animation progress belongs to the cube/move pair and is initialized during composition rather than reset after a new frame has drawn. Double turns use two animations separated by 180 ms. Explicit replay rewinds the same geometry. Back-face culling reduces hidden-face overdraw and depth-order artifacts. Guide progress observes the current animation and enables confirmation after completion.

## Globally constrained classification (1.3.0)

Independent nearest-center decisions can create impossible color totals under glare. Balanced classification expands each color into its remaining physical capacity and uses square minimum-cost matching across sticker-to-color distances. Six centers are fixed labels. A full scan assigns eight more stickers per color; a face rescan derives capacity from the 45 preserved stickers. The assigned color confidence compares its cost with the best alternative, so a capacity-forced choice remains highlighted. The algorithm is O(n^3) for n=48 and runs off the main thread as part of the existing scan flow.

Color sampling uses the 90th-percentile saturation to detect chromatic stickers and excludes desaturated specular pixels before the LAB/HSV median. Neutral stickers retain their pixels. The solver requests max depth 20 and 1,000 minimum phase-two probes, then combines same-face turns separated only by a commuting opposite-face move. All resulting moves are replay-verified.

## Product shell and virtual puzzles (2.0.0)

The Compose shell separates Home, Play, Timer and Learn into saved destinations with a shared compact navigation bar. Solver screens remain a linear capture-review-setup-guide flow, so utility navigation cannot accidentally mutate an active solve. Settings uses a full-width sheet, and manual entry keeps its solve action visible while the cube and editor scroll on short displays.

`app/play/VirtualCube` reuses the core integer sticker geometry for outer, inner and wide moves on 2x2 through 7x7 cubes. It has no second cube rules engine. A compact encoded sticker/history value restores the playground after navigation or activity recreation. Tests verify outer, inner and wide inverses, four-turn identity, scramble reversal and serialization. The 3x3 uses the solver's proven solid renderer; the generalized Canvas renderer constructs solid cubies for every other size.

Timer results, selected puzzle size and completed lessons live in `AppPreferences`.

## Multi-puzzle solve flow (2.3.0)

The scanner dispatches by `PuzzleSpec` and square detectors sample 2×2, 3×3 or 4×4 grids. The 2×2 flow repairs independent face-image rotations after a guided white-red-green reference-corner setup. The 4×4 classifier assigns sixteen of every color and validates corner and wing inventories before the reduction solver runs.

`MultiPuzzleSession` owns the non-3×3 capture, review, edit, setup and guide state. `PuzzleSolverGate` remains the enabling boundary: it validates the input, parses and applies every returned move with the matching puzzle engine, and exposes the guide only when replay reaches solved. The 2×2 guide reuses generalized cube geometry and 4×4 renders outer and wide layers. The virtual renderer supports legal outer, inner and wide turns for 2×2 through 7×7. Initials are consumed only by focused editors; renderers default to label-free output.

## Focused navigation and autoplay (2.1.0)

Only Home, Practice and Learn are root destinations and receive bottom navigation. Virtual cube, timer, scanning, review, analysis, setup and guidance are detail flows with one toolbar. Stored Progress and Puzzle Library routes migrate safely to Home or Practice.

Virtual-cube visibility uses each face's transformed outward normal against the camera vector. The renderer draws a dark backing plane just behind each sticker grid, then depth-sorts only the three visible faces. This avoids winding-dependent culling and closes the gaps that previously exposed stickers from the opposite side.

Guide autoplay begins after the current layer-turn animation reaches completion. A second linear progress animation provides a configurable 1,000–2,000 ms physical-turn interval before committing the move and starting the next step. Pause, More and manual navigation cancel the pending advance. Previous immediately revisits a missed autoplay step; manual confirmed-step recovery retains the explicit physical undo dialog.
