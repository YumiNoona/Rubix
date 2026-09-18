# Rubix UI overhaul plan

## Implementation status — 3.1.0

The application-side overhaul is implemented across the shared theme, root shell, Solve, Practice, Learn, capture/review, guide, virtual cube, timer, settings and completion surfaces. The old directional route slide and popup menus have been removed. Unit, lint, Android-test compilation and debug assembly pass.

The only release gate that cannot be completed on this host is physical-device acceptance: there is no connected device or configured accelerated emulator. The generated debug APK must still be visually checked at the font scales, window sizes, animation scales and camera conditions listed in the testing matrix before replacing the distributed APK.

## 1. Product and codebase summary

Rubix is an offline Android puzzle workspace built with Kotlin and Jetpack Compose. Its core journeys are:

1. Scan a physical puzzle, review its colors, solve it, and follow a guided solution.
2. Practise with a virtual cube in free-play, challenge, or guided modes.
3. Time physical solves and retain local statistics.
4. Learn the beginner method through short lessons and playable examples.

The app is a single activity. `CubeViewModel` owns both domain state and a hand-written `Screen` enum. `CubeApp` switches composables with `AnimatedContent`. Puzzle rendering is custom: 3D cube views and puzzle-specific canvases are valuable brand assets and should remain central to the experience.

The current product is functionally broad, but the interface grew screen by screen. Visual tokens, layout rules, navigation, and motion are not yet expressed as a coherent system. The overhaul should improve the shell and reusable foundations first, then migrate journeys without changing solver behavior.

## 2. Current-state findings

### Information architecture

- The three root destinations—Home, Practice, and Learn—are appropriate for a small app.
- Scan is the main action, but the transition from Home into a nine-item puzzle picker adds friction before a user sees which scanners actually work.
- Practice contains only two large cards while virtual play itself contains several modes. The hierarchy is deeper than the amount of content requires.
- Settings is only visible on root screens, while guide-specific settings are hidden in a separate overflow menu.
- Multi-puzzle solve stages live inside one `PUZZLE_SOLVE` route, so navigation, restoration, and transitions cannot distinguish capture, review, edit, setup, guide, and completion.
- The 3x3 and multi-puzzle solve journeys use separate UI implementations, which creates visible and behavioral drift.

### Visual system

- Theme colors live inside `CubeApp`; component dimensions and shapes are repeated across screen files.
- Cards use several radii (14, 16, 18, 20, 22, 24, and 28 dp) without a semantic reason.
- Primary buttons are mostly 52–54 dp high but are recreated locally, leading to inconsistent content padding and typography.
- Screen headings alternate among the app bar, `Heading`, and local `Text` calls.
- Blue, yellow, green, and red sometimes mean interaction or status and sometimes decorate puzzle art. Semantic UI colors should be separate from sticker colors.
- The app is dark-only and does not currently expose a stable type scale, spacing scale, elevation policy, or state-layer policy.
- Long single-line composables such as `MultiPuzzleFlow` make visual behavior difficult to inspect and safely change.

### Motion and the reported strange transition

The current transition is produced in `CubeApp.kt` by an `AnimatedContent` that only owns the middle content. The top bar and bottom navigation sit outside it and appear or disappear immediately when `vm.screen` changes. That changes the content bounds while the old and new screens are sliding, which can look like a jump, crop, or diagonal movement.

There are additional contributors:

- Route direction is inferred from a numeric `depth()`, not from an actual back stack. Lateral destinations with the same depth may animate as if the user moved forward.
- Root destinations fade, while nested destinations slide and fade; switching rapidly can overlap different motion models.
- `VIRTUAL` contains its own mode picker and `PUZZLE_SOLVE` contains many internal stages, but their route key never changes. Some large in-place changes therefore have no coordinated transition, while other smaller changes animate the whole screen.
- Each routed screen is wrapped in a permanently named saveable-state bucket. This is useful, but it can restore stale scroll or local overlay state when returning to a route.
- Dropdown menus use platform popup animation while nearby content may also be moving. On the guide and virtual-cube screens, these popups feel visually unrelated to the rest of the app.

## 3. Experience direction

Use a **focused puzzle workbench** direction: dark, precise, tactile, and calm. The cube supplies the playful color; navigation and controls remain restrained.

Principles:

- One obvious action per screen.
- Keep the puzzle or current result as the visual hero.
- Reveal advanced controls only when they become relevant.
- Use color for meaning: blue for interaction, green for success, amber for attention, red for destructive/error, and sticker colors only for puzzle content.
- Prefer stable layouts. Motion should explain where content came from, never decorate a state change.
- Make every critical action usable with one hand and with large text enabled.

## 4. Target information architecture

### Root navigation

Keep three destinations:

- **Solve**: launch the most recent puzzle immediately, change puzzle type, use camera/gallery/manual entry, and resume an unfinished solve.
- **Practice**: virtual puzzle modes and timer, with recent/best performance surfaced on the landing screen.
- **Learn**: progress summary, lesson tracks, and contextual practice.

Rename Home to Solve in the navigation label. The brand remains in the top app bar or launch treatment; the destination name should describe the job.

### Solve journey

`Solve landing -> Puzzle selector -> Capture method -> Capture -> Review -> Ready/holding position -> Guide -> Completion`

- Default to the last successfully used puzzle.
- Show verified scanners first. Put unavailable puzzle types in a clearly labelled “Planned” section rather than mixing disabled tiles with working options.
- Let users choose Camera, Photo, or Manual before entering capture.
- Use one shared stage indicator for capture and review, but avoid a stepper during the move guide.
- Preserve the exact current stage when the process is interrupted.

### Practice journey

`Practice -> Virtual mode or Timer`

- Surface Free play, Challenge, and Guided solve directly as distinct cards or segmented options.
- Keep puzzle size selection close to the cube, not in a popup detached from it.
- Keep the timer screen distraction-free; secondary statistics should sit below the main tap target.

### Learn journey

- Show one “Continue learning” card first.
- Group the rest by Rookie, Experienced, and Veteran.
- Replace the tall lesson bottom sheet with a dedicated lesson detail route on compact screens. It will be easier to read, restore, and navigate with accessibility services.

## 5. Design-system implementation

Create a small, explicit UI foundation under `ui/design/`:

- `RubixTheme.kt`: color schemes, typography, system-bar treatment, and optional dynamic-color policy.
- `RubixColors.kt`: semantic colors plus a separate `PuzzlePalette`.
- `RubixDimens.kt`: 4 dp base spacing scale, compact/regular content widths, touch targets, and screen gutters.
- `RubixShapes.kt`: three semantic radii only—control, card, and modal.
- `RubixMotion.kt`: duration/easing tokens and reduced-motion handling.
- `RubixIcons.kt`: consistent icon size and stroke treatment.

Create shared components under `ui/components/`:

- `RubixScaffold`, `RubixTopBar`, and `RubixBottomBar`.
- `ScreenHeader`, `SectionHeader`, `HeroCard`, and `ActionCard`.
- `PrimaryButton`, `SecondaryButton`, `TertiaryButton`, and `DestructiveButton`.
- `StatusBanner`, `EmptyState`, `LoadingState`, and `InlineError`.
- `PuzzleThumbnail`, `PuzzleSelector`, `ModeSelector`, and `StageIndicator`.
- `RubixActionSheet` for multi-action menus.

Do not make a universal component for every arrangement. Share repeated visual grammar while keeping screen-specific composition readable.

## 6. Navigation and motion rebuild

### Navigation state

Introduce a small typed route model and back stack. Navigation Compose is optional; the important requirement is that navigation records intent instead of guessing direction from enum depth.

Each route must carry the state needed to distinguish its UI, for example:

- `Root(Solve|Practice|Learn)`
- `PuzzlePicker`
- `CaptureMethod(puzzleId)`
- `Capture(puzzleId, face)`
- `Review(puzzleId)`
- `Edit(puzzleId, face)`
- `Setup(puzzleId)`
- `Guide(puzzleId)`
- `Virtual(mode, size)`
- `Timer(size)`
- `LessonDetail(lessonId)`

Domain state remains in view models/session objects; routes should not contain sticker arrays or solver results.

### Transition rules

- Root tab change: 120–160 ms crossfade with no horizontal movement.
- Push to a detail screen: 200–240 ms shared-axis horizontal motion, 6–8% of the content width.
- Pop/back: exact reverse of the push.
- Modal action sheet: fade scrim plus vertical sheet motion, handled only by the sheet.
- In-screen state change: animate the smallest affected region, not the entire route.
- Solver/camera transitions: crossfade the status region and preserve the puzzle/camera frame position.
- Respect Android animator duration scale. If animations are disabled, change state immediately.

### Immediate transition fix

Before the rest of the redesign, make the app shell geometrically stable:

1. Replace the outer `Column` with `Scaffold`.
2. Give every destination a deliberate top-bar and bottom-bar policy.
3. Animate content inside the fixed scaffold content bounds.
4. Keep root bottom-navigation space stable during root-to-root transitions.
5. Key nested content by its real substate (`virtualMode`, multi-puzzle stage), or promote that substate to a route.
6. Remove `depth()` once back-stack direction is available.
7. Replace guide and cube overflow popups with the shared action sheet on phones; retain an anchored dropdown only on sufficiently wide layouts.

Acceptance criteria:

- No top/bottom jump when moving from Solve to puzzle selection or back.
- Back motion always reverses the preceding forward motion.
- Rapidly tapping tabs never shows two bottom bars, blank frames, or clipped screens.
- Opening a menu does not move the underlying layout.
- Menu dismissal returns focus to its trigger.
- All behavior also works with animation scale set to 0× and 10×.

## 7. Screen-by-screen overhaul

### Solve landing

- Replace the looping solve animation as the only large content with a compact hero that combines the chosen puzzle, “Scan” primary action, and change-puzzle control.
- Add Camera, Photo, and Manual as explicit entry methods after the main action or as a compact action row.
- Surface an unfinished session as “Continue solve”; do not silently discard it.
- Keep the demo as low-emphasis onboarding, not a peer of primary actions.

### Puzzle selector

- Split “Available now” from “Coming later.”
- Use a two-column grid on phones and three/four columns on wider screens.
- Give each tile a consistent puzzle thumbnail, full name, support badge, and selection state.
- Make selection and confirmation one clear model: either tap a tile to open it, or select then confirm—not both without explanation.

### Capture preparation and camera

- Use one preparation card with a visual orientation example and three short checks.
- Move technical camera status into a small status banner.
- Keep face name, progress, and capture action anchored so they do not shift as instructions change.
- Ensure flash, gallery, and help have labels, not icon-only ambiguity.
- Use a consistent confidence treatment that does not rely on magenta alone.

### Review and color editing

- Use the 3D/net toggle as a standard segmented control.
- Show color counts and validation in a collapsible status card.
- Use the same editor for 2x2, 3x3, and 4x4 where their interaction model matches.
- Keep Cancel and Done anchored; never make users scroll to commit edits.
- Show undo/redo as a compact toolbar with disabled states and accessible descriptions.

### Holding setup and guide

- Treat orientation as a visual instruction: large front/top chips beside the cube, then one primary “I’m ready” action.
- Keep guide chrome stable: move number, instruction, cube, turn explanation, and Previous/Next should not reflow between moves.
- Move recovery actions to a bottom action sheet with clear grouping: Replay/Inspect, Correct, Restart/Leave.
- Use a single autoplay control with explicit current state and a progress treatment that does not compete with move progress.
- Keep important recovery paths available without making the main guide feel like a control panel.

### Completion

- Lead with the solved puzzle and concise result.
- Offer “Solve another” as primary, “Replay guide” as secondary, and return-to-Solve as tertiary.
- Show time/move stats only when they are reliable for that puzzle type.

### Virtual puzzle

- Separate mode selection from the play surface.
- Use a fixed play layout: status at top, puzzle canvas in the flexible middle, core controls at bottom.
- Replace size dropdown with compact chips or a selector sheet.
- Group moves by face and keep Scramble/Undo/Reset visually distinct from turns.
- Provide a short first-run gesture hint and then stop showing it.

### Timer

- Make the timer/touch target the dominant visual area.
- Keep scramble readable with a copy/regenerate action.
- Use unambiguous phases: Idle, Hold, Ready, Running, Result.
- Place Last, Best, and Average in one consistent metric row.
- Confirm destructive history reset; do not confirm a simple current-time reset.

### Learn

- Add completion percentage and “Continue” at the top.
- Use consistent lesson cards with status, goal, and expected duration/difficulty.
- Move lesson detail into its own route and keep practice launch at the bottom.
- Retain before/after cube examples but allow a labelled toggle for screen readers.

### Settings

- Divide into Appearance, Interaction, Guide, and Accessibility.
- Use a preview for display-color changes.
- Add reduced motion, larger cube labels, high-contrast outlines, and reset-to-default per section.
- Persist settings exactly as today; the overhaul should not reset user preferences.

## 8. Code migration plan

### Phase 0 — baseline and safeguards

- Capture screenshots/video for every route at a phone size, small phone, tablet, font scale 1.0, and font scale 1.3.
- Record the current transition bug at normal, 0×, and 10× animator scale.
- Add Compose UI tests for root navigation, scan entry/back, guide menu, virtual mode/back, and state restoration.
- Add semantic tags only where roles/text cannot locate controls.

Deliverable: a baseline gallery and green behavioral tests before visual changes.

### Phase 1 — foundations

- Extract theme and token files.
- Implement shared buttons, cards, headers, banners, selectors, and action sheet.
- Add previews for normal, disabled, pressed/selected, error, long-text, and large-font states.
- Replace hard-coded semantic colors and repeated dimensions incrementally.

Deliverable: a component catalog usable by all screens.

### Phase 2 — shell, navigation, and motion

- Introduce typed route/back-stack state.
- Build `RubixScaffold` with stable bars and insets.
- Apply the transition rules above.
- Migrate Settings and overflow menus to the shared modal model.
- Add restoration tests for routes and nested modes.

Deliverable: the reported transition defect is fixed before screen redesign begins.

### Phase 3 — Solve journey

- Redesign Solve landing, picker, capture method, preparation, scan, review, editor, setup, guide, and completion in that order.
- Extract shared 2x2/3x3/4x4 review/editor/guide structures where behavior genuinely matches.
- Keep camera, classification, cube validation, and solver APIs unchanged.

Deliverable: one polished end-to-end 3x3 flow, then parity for verified 2x2, Pyraminx, and 4x4 flows.

### Phase 4 — Practice and Learn

- Redesign the Practice landing and virtual mode selection.
- Stabilize the virtual-cube play layout across 2x2–7x7.
- Redesign the timer and metric states.
- Redesign Learn landing and lesson detail.

Deliverable: all root journeys use the same visual and interaction language.

### Phase 5 — adaptive layout and accessibility

- Add compact/medium/expanded width behavior.
- Verify 48 dp minimum targets, focus order, content descriptions, and TalkBack announcements.
- Test 1.0–2.0 font scale, landscape, contrast, color-blind-safe status cues, reduced motion, and RTL.
- Ensure camera/editor critical actions remain reachable with large fonts.

Deliverable: accessibility and adaptive-layout checklist passes on supported Android versions.

### Phase 6 — polish, performance, and release

- Profile recomposition and rendering around camera preview and 3D puzzle canvases.
- Remove obsolete components and screen-state branches after migration.
- Run unit, lint, instrumentation, screenshot, and physical-device tests.
- Update README screenshots, architecture notes, changelog, version, APK, and checksum only after acceptance.

Deliverable: release candidate with comparison gallery and no known navigation regressions.

## 9. Testing matrix

Test these combinations before release:

- Android 8/API 26 and current target API.
- Small phone, common phone, foldable/tablet, portrait, and landscape.
- Gesture navigation and three-button navigation.
- Font scales 1.0, 1.3, 1.5, and 2.0.
- Light-on-dark contrast, color initials off/on, TalkBack, reduced motion, and system animations off.
- Cold start, process recreation, rotation, back gesture, repeated fast taps, camera permission denied, and camera unavailable.
- Each verified solver flow plus cancel/re-enter at every stage.
- Virtual sizes 2x2–7x7, all modes, timer start/stop/reset, and lesson-to-practice handoff.

Release gates:

- No route loses puzzle progress unexpectedly.
- No content is hidden behind system bars or the keyboard.
- No primary action is clipped at supported font scales.
- No menu or route transition causes a layout jump.
- All tappable controls have a 48 dp target and meaningful semantics.
- Existing core/unit tests, Compose UI tests, lint, and assembly pass.
- Physical-device camera and animation checks pass before distributing the APK.

## 10. Suggested delivery sequence

Use small reviewable changes rather than one full-app rewrite:

1. Baseline tests and visual inventory.
2. Theme/tokens/components.
3. Stable scaffold and transition fix.
4. Solve landing and puzzle selection.
5. Capture/review/editor.
6. Setup/guide/completion.
7. Practice/virtual/timer.
8. Learn/settings.
9. Accessibility/adaptive layouts.
10. Cleanup, documentation, and release build.

The first implementation milestone should be phases 0–2. It resolves the visible motion defect and creates the foundation needed to avoid recreating inconsistencies during the screen overhaul.
