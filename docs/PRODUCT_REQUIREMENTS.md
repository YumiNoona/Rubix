# Build a Production-Quality Android Rubik's Cube Solver

You are a senior Android engineer, computer-vision engineer, UX designer, and algorithms engineer.

I want you to **design and build a complete Android app that solves a physical 3×3 Rubik's Cube using the phone's camera**.

The goal is to create a polished app that is significantly better than basic cube-solver apps in terms of:

* Camera scanning
* Color detection
* Error handling
* User guidance
* 3D visualization
* Step-by-step solving instructions
* Overall UX
* Offline functionality
* Reliability

Do not treat this as a prototype or a conceptual exercise. Build the project as if it is intended to become a real production Android application.

---

# 1. CORE USER EXPERIENCE

The entire experience should be:

**Open app → Scan cube → App understands cube → App validates cube → App calculates solution → App visually guides the user through every move → Cube solved.**

The user should NOT need to manually enter 54 colors unless the automatic scanner fails.

The application should work completely offline after installation.

No cloud server should be required for normal operation.

No cube image should need to leave the user's device.

---

# 2. TARGET PLATFORM

Build for:

* Android
* Kotlin
* Jetpack Compose
* Modern Android architecture
* CameraX
* OpenCV where appropriate
* Native processing where it provides meaningful performance benefits

Target modern Android devices while maintaining reasonable compatibility with common Android phones.

Use a clean architecture that is maintainable and extensible.

---

# 3. MVP SCOPE

The first complete version must support:

### Cube

* 3×3×3 Rubik's Cube
* Standard six-color cube
* Standard face turns
* Clockwise turns
* Counter-clockwise turns
* Double turns

### Camera

Automatically scan all six faces.

### Solver

Use a reliable two-phase solver such as:

* Kociemba
* min2phase
* or another proven optimal/near-optimal 3×3 solver

The solver must run locally.

### Guidance

Show the solution one move at a time using:

* 3D cube animation
* Direction arrow
* Human-readable instruction
* Standard notation

Example:

> TURN RIGHT FACE CLOCKWISE
> R

For:

> R'

show:

> TURN RIGHT FACE COUNTER-CLOCKWISE

For:

> R2

show:

> TURN RIGHT FACE TWICE

The user should never have to understand cube notation to use the application.

---

# 4. CAMERA SCANNING SYSTEM

This is one of the most important parts of the application.

Do NOT make the scanner simply take six photographs and run basic RGB thresholding.

Create an intelligent scanning system.

The camera should continuously analyze frames and detect:

* The Rubik's Cube
* Its four visible corners
* Perspective
* Orientation
* The 3×3 sticker grid
* Individual sticker regions
* Sticker colors

The scanner should work under realistic lighting conditions.

Consider:

* Indoor lighting
* Daylight
* Shadows
* Reflections
* Slight glare
* Different cube manufacturers
* Different sticker brightness
* Camera white-balance differences
* Slightly rotated cubes
* Slight perspective distortion

---

# 5. CUBE DETECTION

The user should see a camera overlay similar to:

```text
┌──────────────────────────────┐

          SCAN 1 / 6

       ┌─────────────┐
       │             │
       │    CUBE     │
       │             │
       └─────────────┘

       Show WHITE face

└──────────────────────────────┘
```

Detect the cube automatically.

Do not require the user to perfectly align the cube manually.

Use computer vision to estimate the face corners and perspective-transform the face into a normalized square.

---

# 6. STICKER DETECTION

Once the face is detected:

1. Determine the four corners.
2. Apply perspective correction.
3. Divide the normalized face into a 3×3 grid.
4. Sample the interior/center region of each sticker.
5. Avoid borders between stickers.
6. Avoid black/dark cube gaps.
7. Determine the color of each sticker.

Do not sample only one pixel.

Use a region of pixels and robust statistics.

---

# 7. COLOR RECOGNITION

Do NOT depend solely on fixed RGB values.

Build a robust color-classification system.

Prefer a combination of:

* RGB
* HSV
* CIELAB/LAB
* Adaptive clustering
* Relative color comparison

A useful approach is:

1. Detect the six center stickers.
2. Use the center colors as reference anchors.
3. Cluster/classify the remaining stickers relative to those anchors.
4. Use LAB/HSV distances rather than simple RGB thresholds.
5. Check confidence for every sticker.

Consider K-means or a similar clustering technique where appropriate.

The final classification must always produce one of:

* White
* Yellow
* Red
* Orange
* Blue
* Green

---

# 8. MULTI-FRAME STABILITY

Do not immediately accept a single camera frame.

Continuously analyze multiple frames.

Example:

```text
Frame 1 → detected colors
Frame 2 → detected colors
Frame 3 → detected colors
Frame 4 → detected colors
Frame 5 → detected colors
```

If the results remain stable for several frames:

```text
FACE STABLE
✓
```

Then automatically capture the face.

If detection is unstable, provide useful feedback.

Examples:

> Move the cube slightly closer.

> Keep the cube inside the outline.

> Too much glare.

> Improve lighting.

> Hold the cube steady.

The scanner should feel automatic and intelligent.

---

# 9. GUIDED SIX-FACE SCANNING

Do not let users scan six arbitrary faces without orientation tracking.

The app should control the scanning sequence.

For example:

### Face 1

> Show WHITE face.

After successful capture:

> ✓ White face captured.

Then:

> Turn the cube to the RIGHT.

Then scan the next required face.

The application must maintain knowledge of how the physical cube was oriented between scans.

This is critical.

Incorrect face orientation is one of the most common reasons cube scanners produce invalid cube states.

Create a robust internal orientation/mapping system.

---

# 10. SCAN PROGRESS

Show clear progress:

```text
SCAN CUBE

● ● ○ ○ ○ ○

3 / 6
```

Also identify the requested face:

```text
Show GREEN face
```

After capture:

```text
GREEN FACE
✓ CAPTURED
```

Then automatically advance.

---

# 11. CUBE STATE REPRESENTATION

Create a proper internal cube model.

It should represent:

* Six faces
* 54 stickers
* 8 corners
* 12 edges
* Orientation
* Permutation

The internal model must support:

* Applying moves
* Reversing moves
* Validating states
* Generating the solution
* Updating the 3D visualization

Use a clean abstraction such as:

```text
CubeState
CubeFace
CubeColor
CubeMove
CubeOrientation
```

or a better architecture if appropriate.

---

# 12. CUBE VALIDATION

Before attempting to solve, validate the reconstructed cube.

Check:

### Color count

Exactly nine stickers of each color.

### Centers

Six unique center colors.

### Edge validity

All twelve edge combinations must be physically possible.

### Corner validity

All eight corner combinations must be physically possible.

### Orientation

Validate edge and corner orientation constraints.

### Permutation

Validate permutation parity and other cube-state constraints.

If invalid:

DO NOT simply say:

> No solution found.

Instead explain the problem.

Example:

```text
Cube scan needs correction.

We detected an unusual color on
the FRONT face, top-right sticker.

[ Rescan Face ]

[ Edit Colors ]
```

---

# 13. MANUAL CORRECTION MODE

If automatic scanning fails, provide a manual correction interface.

Display the cube as a 2D net:

```text
          [ U ]

[ L ] [ F ] [ R ] [ B ]

          [ D ]
```

Each sticker can be tapped and assigned a color.

This should be a fallback, NOT the primary workflow.

The user should almost never need it.

---

# 14. SOLVER

Use a proven 3×3 solver.

Preferred:

**Kociemba Two-Phase Algorithm / min2phase**

The solver should run entirely on the device.

Pipeline:

```text
Camera
   ↓
Sticker Detection
   ↓
Color Classification
   ↓
Cube State
   ↓
Validation
   ↓
Solver
   ↓
Move Sequence
   ↓
Move Optimization
   ↓
Step-by-Step Guide
```

Do not call an external solving API.

---

# 15. SOLUTION OPTIMIZATION

After obtaining the solver's solution, process the move sequence.

Handle:

* Duplicate moves
* Consecutive inverse moves
* Double turns
* Redundant rotations
* Cube rotations where possible

Produce a clean human-friendly solution.

For example:

```text
R R
```

should become:

```text
R2
```

And:

```text
R R R
```

should become:

```text
R'
```

Do not optimize in a way that makes the solution difficult to follow.

**Human usability is more important than saving one or two moves.**

---

# 16. 3D CUBE VISUALIZATION

Create a real interactive 3D Rubik's Cube.

The cube should contain:

* Individual cubies
* Six colored faces
* Correct sticker colors
* Correct layer rotations
* Smooth animations

When the instruction is:

```text
R
```

only the right layer should rotate.

When:

```text
U
```

only the top layer should rotate.

The user should be able to clearly see exactly which physical layer they need to turn.

---

# 17. STEP-BY-STEP SOLVING SCREEN

The primary solving screen should look approximately like:

```text
┌───────────────────────────────┐

          STEP 7 / 23

       [ 3D CUBE ]

           ↻

      TURN RIGHT FACE
          CLOCKWISE

             R

     ─────────────────

       [ BACK ] [ NEXT ]

└───────────────────────────────┘
```

Make the animation the main visual element.

Do not make notation the primary instruction.

Notation should be secondary.

---

# 18. MOVE INSTRUCTIONS

For every move, provide:

### Visual

Animated layer rotation.

### Arrow

Show the direction.

### Text

Example:

> TURN RIGHT FACE CLOCKWISE

### Notation

```text
R
```

For inverse:

> TURN RIGHT FACE COUNTER-CLOCKWISE

```text
R'
```

For double:

> TURN RIGHT FACE TWICE

```text
R2
```

The user should understand the move without knowing what `R`, `U`, `F`, etc. mean.

---

# 19. WHOLE-CUBE ROTATIONS

Sometimes the user needs to physically rotate the entire cube.

Make this explicit.

Example:

```text
ROTATE THE WHOLE CUBE

Put BLUE on the FRONT.

Then press NEXT.
```

Use the 3D cube to demonstrate the required orientation.

Do not silently assume the user understands cube orientation.

---

# 20. DIGITAL TWIN

Maintain a synchronized digital representation of the physical cube.

After each user-confirmed move:

```text
Physical cube
      ↕
Digital cube state
```

The 3D model should represent the expected current state.

This enables:

* Step navigation
* Undo
* Previous step
* Recovery
* Move history
* Accurate animations
* Error handling

---

# 21. USER MADE A WRONG MOVE

Provide:

```text
I MADE A MISTAKE
```

If the user selects it:

```text
What happened?

[ I did the previous move again ]

[ I turned the wrong direction ]

[ I made a different move ]

[ I don't know ]
```

Provide a sensible recovery flow.

If the state can no longer be trusted, offer:

> Re-scan cube

or a partial verification scan rather than forcing the user to restart unnecessarily.

---

# 22. MOVE CONFIRMATION

Do not assume the user physically performed a move correctly.

The default interaction should be:

```text
Animation
    ↓
Instruction
    ↓
User performs move
    ↓
NEXT
```

Optionally investigate whether camera-based move verification is practical, but do not make this a dependency for the first production version.

The application must remain reliable without live move recognition.

---

# 23. SOLUTION PROGRESS

Show:

```text
STEP 12 / 24

████████████░░░░░░░░

TURN FRONT FACE
CLOCKWISE

F
```

Also provide:

* Back
* Next
* Restart
* Exit

Do not overwhelm the interface.

---

# 24. COMPLETION SCREEN

When solved:

```text
          ✓ SOLVED

        24 MOVES

      Great job.

   [ SOLVE ANOTHER ]

   [ VIEW SOLUTION ]
```

Optionally show:

* Number of moves
* Time
* Solution efficiency
* Scan time

---

# 25. UI DESIGN

The UI should be modern, clean, minimal and premium.

Avoid making it look like an old utility app.

Prioritize:

* Large visual instructions
* Large touch targets
* Clear typography
* Strong hierarchy
* Minimal clutter
* Smooth animations
* Dark/light theme support

The cube itself should be the visual focus.

Do not add unnecessary gamification.

---

# 26. PERFORMANCE

The camera processing loop must be efficient.

Do not process full-resolution frames unnecessarily.

Use:

* Appropriate CameraX resolution
* Region-of-interest processing
* Frame throttling
* Background processing
* Efficient OpenCV operations
* Native code only where justified

Do not block the Android main/UI thread.

The UI must remain responsive while scanning and solving.

---

# 27. OFFLINE-FIRST

The core application must work without internet.

Offline:

* Camera scanning
* Color recognition
* Cube reconstruction
* Validation
* Solver
* 3D visualization
* Solution instructions

No account should be required.

No cloud processing should be required.

---

# 28. PRIVACY

Camera data should remain on-device.

Do not upload camera frames.

Do not introduce analytics or tracking unless explicitly requested later.

Explain camera permission clearly.

If camera permission is denied, provide the manual cube-entry workflow.

---

# 29. ERROR HANDLING

Every failure should produce an actionable message.

Bad:

> Error.

Good:

> We couldn't detect all 9 stickers. Move the cube into better lighting and try again.

Examples:

### Poor lighting

> Too dark. Move to a brighter area.

### Glare

> Glare detected on the yellow face. Tilt the cube slightly.

### Cube too far

> Move the cube closer.

### Cube too close

> Move the cube slightly farther away.

### Wrong face

> Please show the GREEN center face.

### Invalid cube

> The scanned colors don't represent a physically solvable cube.

---

# 30. PROJECT ARCHITECTURE

Use a clean, modular architecture.

A possible structure:

```text
app/
│
├── camera/
│   ├── CameraController
│   ├── FrameAnalyzer
│   └── CameraPermission
│
├── vision/
│   ├── CubeDetector
│   ├── FaceDetector
│   ├── StickerDetector
│   ├── PerspectiveCorrector
│   ├── ColorClassifier
│   ├── ColorCalibration
│   └── ScanStability
│
├── cube/
│   ├── CubeState
│   ├── CubeColor
│   ├── CubeFace
│   ├── CubeMove
│   ├── CubeOrientation
│   └── CubeValidator
│
├── solver/
│   ├── KociembaSolver
│   ├── MoveParser
│   └── SolutionOptimizer
│
├── rendering/
│   ├── Cube3D
│   ├── Cubie
│   ├── Facelet
│   └── MoveAnimator
│
├── guide/
│   ├── SolutionPlayer
│   ├── MoveInstruction
│   └── RecoveryManager
│
└── ui/
    ├── HomeScreen
    ├── ScanScreen
    ├── ScanReviewScreen
    ├── ManualCorrectionScreen
    ├── SolutionScreen
    ├── CompletionScreen
    └── SettingsScreen
```

You may change this structure if you have a better architecture.

---

# 31. TESTING

Do not stop after implementing the happy path.

Create tests for:

### Cube logic

* Every basic move
* Inverse moves
* Double moves
* Move combinations
* Four identical moves returning to original state
* Solver output
* State validation

### Camera

Test different:

* Lighting conditions
* Cube colors
* Angles
* Distances
* Reflections

### Color recognition

Test all six colors.

### Invalid states

Test:

* Duplicate/missing colors
* Impossible corners
* Impossible edges
* Invalid parity
* Invalid orientations

### UI

Test the complete flow:

```text
Home
→ Scan
→ 6 faces
→ Validation
→ Solve
→ Instructions
→ Completion
```

---

# 32. IMPORTANT DEVELOPMENT RULE

Do not simply give me a huge amount of code without validating the architecture.

Work systematically.

First establish:

1. Project architecture
2. Dependencies
3. Cube representation
4. Move engine
5. Cube validator
6. Solver integration
7. 3D cube
8. Camera pipeline
9. Color recognition
10. Guided scanner
11. Solution UI
12. Error/recovery system
13. Testing
14. Polish

Build and verify each major subsystem before moving to the next.

---

# 33. USE EXISTING OPEN-SOURCE WORK WHERE APPROPRIATE

Research existing implementations before reinventing components.

Useful references include projects implementing combinations of:

* Android
* CameraX
* OpenCV
* CIELAB/K-means color detection
* Kociemba/min2phase
* 3D cube visualization

Examples worth examining include:

* RubiksScanAndSolve
* Android Rubik's cube solver implementations
* Other established open-source Kociemba implementations

Do not blindly copy code.

Understand the implementation, evaluate its license, and use compatible/open components appropriately.

If a dependency has licensing restrictions, identify them before incorporating it.

---

# 34. DO NOT OVERENGINEER THE FIRST VERSION

The first production milestone is:

**A highly reliable 3×3 camera scanner + solver + excellent step-by-step 3D guide.**

Do NOT initially add:

* 2×2
* 4×4
* 5×5
* Online accounts
* Social features
* Leaderboards
* Cloud solving
* Unnecessary AI features

Those can be considered later.

---

# 35. FUTURE EXTENSIBILITY

However, design the core architecture so future versions could support:

* 2×2
* 4×4
* 5×5
* Other twisty puzzles
* Better move verification
* AR-style guidance
* Solver statistics
* Tutorials

Do not implement these now unless necessary for the architecture.

---

# 36. QUALITY BAR

The finished application should feel like a real consumer application, not a university project.

Prioritize:

**Reliability > cleverness**

**User understanding > notation**

**Offline operation > cloud dependencies**

**Smooth UX > feature count**

**Recoverability > perfect first attempt**

The most important part is that a normal person can take their scrambled Rubik's Cube, point their Android phone at it, scan six faces, and successfully follow the instructions to solve it.

---

# 37. HOW I WANT YOU TO WORK

Do not just explain how I could build this.

Actually build it.

When something is ambiguous, make a technically sensible decision rather than stopping unnecessarily.

When you encounter a technical limitation, explain it and provide the best practical alternative.

Use current Android APIs and current stable libraries rather than outdated tutorials.

Before implementing third-party libraries, verify their current APIs and licenses.

Keep the project buildable at every major stage.

When providing code:

* Give complete files when practical.
* Clearly identify file paths.
* Do not provide disconnected code fragments that cannot be integrated.
* Ensure imports and dependencies are correct.
* Avoid pseudocode where real implementation is possible.
* Do not leave core functionality as TODOs.

If you need to make a tradeoff, choose the option that produces the most reliable real-world app.

---

# 38. START HERE

Begin by researching the current best technical approaches and existing open-source implementations for:

1. Android Rubik's Cube camera scanning
2. CameraX image analysis
3. Cube face detection
4. Perspective correction
5. Sticker/grid detection
6. LAB/HSV color classification
7. K-means color clustering
8. Rubik's Cube state representation
9. Kociemba/min2phase
10. Android 3D cube rendering
11. Current Jetpack Compose architecture
12. Current Android performance recommendations

Then propose the final technical architecture.

After that, start implementing the project systematically.

Do not stop at the architecture.

Continue through implementation, testing, debugging, and refinement until there is a complete working application.

## Final Objective

The finished app should let a user do this:

**Open → Scan six sides → Automatic recognition → Validation → Solve → Follow beautiful 3D move instructions → Cube solved.**

That is the product I want you to build.
