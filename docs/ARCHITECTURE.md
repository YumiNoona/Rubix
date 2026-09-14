# Architecture and research decisions

## Boundaries

`core` is a JVM Kotlin module: `CubeState`, `Face`, `CubeColor`, `Move`, integer sticker geometry, physical validator, scan poses, optimizer and min2phase adapter. Integer geometry maps a sticker's position and outward normal into a permutation for each clockwise layer turn. Each inverse and double turn uses the same engine. Independent reference scrambles in tests verify the mapping instead of comparing the engine only with itself.

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
