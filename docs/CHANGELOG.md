# 3.2.0

- Rebuilt manual color entry as a focused face painter with no competing 3D preview, a roomy 3-by-2 color palette, fixed solve action and compact face navigation.
- Removed the home demo action and retained only Scan puzzle and Enter colors manually.
- Made the center dock action an icon-only Rubix mark and removed decorative frames from the app mark and animated home cube.
- Added ten randomized, continuously animated scramble/solve sequences with a five-second solved pause between cycles.
- Replaced history-only virtual hints with a current-state solver plan for 3x3, an exact safe return plan for other sizes, next-move context, preview and apply actions.
- Added persisted System, Dark and Light appearance modes plus a reorganized settings sheet without redundant Done or helper copy.
- Replaced unreliable system click effects with an explicit short media-stream tone when Touch sounds is enabled.

# 3.1.0

- Rebuilt the root shell around a floating three-action dock with Practice on the left, Solve emphasized in the center, and Learn on the right.
- Replaced directional full-screen slides with calm bounded fades, preventing toolbar and dock changes from producing a jumping transition.
- Added a shared Rubix design system for semantic colors, typography, spacing, shapes, buttons, page introductions and action cards.
- Simplified Home, Practice, Learn, puzzle selection, scan preparation, review, setup, completion, timer and virtual-mode copy and hierarchy.
- Separated supported scanners from planned puzzles and made supported puzzle cards launch directly.
- Replaced every guide and cube-size dropdown with accessible bottom action sheets that do not move the underlying screen.
- Added a persisted Reduce motion preference and edge-to-edge system-bar handling.
- Updated Compose flow tests for the new labels and added a regression for Practice–Solve–Learn dock ordering.

# 3.0.0

- Reduced the app shell to Home, Practice and Learn; removed the Progress screen and standalone Puzzle Library route with safe restoration for older saved navigation.
- Added a dedicated white-up, green-front 3x3 scan preparation screen before camera permission and capture.
- Rebuilt Virtual Cube around a size-aware mode hub for Free play, Challenge and Guided solve, including mode-aware Back behavior and challenge time, move and hint tracking.
- Rebuilt Cube Timer as a focused hold-to-ready 3x3 timer with full-width responsive scramble/reset controls and preserved 3x3 records.
- Introduced a logo-derived navy, blue and amber UI palette, directional screen transitions, and the supplied Rubix artwork as legacy, round, adaptive and splash icon assets.

# 2.3.0

- Enabled complete scan, gallery, review, focused editing, validation, setup, solve, autoplay guide and completion flows for 2x2, Pyraminx and 4x4.
- Added a centerless-cube reference-corner capture flow, a dedicated triangular Pyraminx capture/editor, adaptive per-puzzle color capacity, and low-confidence review outlines.
- Added solid animated 2x2 layer turns, wide-turn 4x4 guidance and a face-accurate layered Pyraminx model with puzzle-specific notation help.
- Added current-state color correction, undo/redo, replay, restart, Previous/Next and configurable autoplay to all three guides.
- Restricted color initials to focused color editors. Home, review previews, virtual cubes, setup, guides and completion models are label-free.
- Added end-to-end regressions that classify synthetic scans, solve through the replay gate and apply every move to a solved 2x2, Pyraminx and 4x4 state.

# 2.2.1

- Polished the complete 3x3 flow for release: fresh scans now return to puzzle selection, while cancelling a one-face rescan returns to the existing review without losing edits.
- Removed the reverse rewind from move replay to prevent a misleading flicker, and made the solution progress bar track the current animated turn.
- Added touch feedback to autoplay controls and refreshed completion with a quieter verified-solve summary.

# 2.2.0

- Replaced the broken 3x3 virtual rendering path with the same solid, draggable cube used by Home and the solve guide; single-step undo now reverses exactly one move.
- Rebuilt Learn into Rookie, Experienced and Veteran tracks with a visual cube on every lesson, problem/solved comparison, recognition cues, plans, move examples and lesson-specific virtual practice.
- Added a dedicated scan color editor with persistent face navigation, a compact labeled palette, initials, undo, redo, Cancel and Done.
- Simplified review to Net/3D inspection plus fixed Edit colors and Looks good actions.
- Added six visible scan states with current-face emphasis, color-coded completion checks, and Material flash/gallery actions; removed the duplicate manual-entry action.
- Moved guide Back, step count, play/pause and overflow into one top row while retaining timed 1–2 second advancement and manual Previous/Next.
- Replaced remaining hand-drawn navigation glyphs with Material icons, enlarged detail Back targets, fixed parent-aware back navigation, and removed redundant puzzle selection text.

# 2.1.0

- Replaced duplicated root/detail navigation with Home, Practice, Learn and Progress roots; virtual cube, timer and solver flows now use focused detail navigation.
- Simplified Home to one cube, one primary scan action, manual entry and a compact demo action.
- Rebuilt virtual-cube face visibility around transformed outward normals and solid backing faces, eliminating the exploded sticker rendering.
- Reworked virtual controls into compact single-line move, direction and action rows.
- Made timer text responsive and kept every statistic on one line.
- Simplified scan review to a Net/3D switch and a focused one-face color editor.
- Added a dedicated analyzing screen and a clearer starting-position solution overview.
- Added solver autoplay with Pause, Previous and Next controls and a configurable 1–2 second interval between animated steps.
- Added per-size progress and lesson completion to the new Progress screen.

# 2.0.0

- Rebuilt the app shell around distinct Home, Play, Timer and Learn destinations with compact persistent navigation.
- Added a touch-rotatable virtual cube for 2x2 through 7x7 puzzles, including face controls, scrambles, state restoration and animated undo.
- Added a cube timer with generated scrambles, saved last/best/average-of-five results and size selection.
- Added seven beginner lessons with saved progress and virtual-cube practice.
- Added a puzzle library and a focused manual color editor with a larger 3D preview, face navigation, direct painting, counts and undo.
- Moved display and interaction settings into a full-width bottom sheet and refreshed the dark visual system, cards, buttons, selection states and transitions.

# 1.3.0

- Globally assign scanned stickers to exactly nine of each center-calibrated color.
- Keep forced color decisions visibly uncertain instead of producing extra whites.
- Reject low-saturation glare from otherwise colorful sticker samples and recognize tinted neutral whites.
- Apply balanced capacities when rescanning one face while preserving all other reviewed stickers.
- Search for verified solutions within 20 face turns and continue through 1,000 probes for shorter candidates.
- Merge compatible same-axis turns across commuting opposite faces.
- Add false-white, capacity, confidence and 20-move regression coverage.

# 1.2.1

- Replace the sticker color list with a labeled six-color bottom sheet and current-color selection.
- Keep face focus while uncertain stickers change; preserve face, paint mode and brush.
- Animate double turns as two quarter-turns with numbered progress; explain inverse turns clearly.
- Avoid old animation progress on a changed cube, rewind explicit replays smoothly and cull hidden faces.
- Enable Next after the animation preview finishes.
- Add circular hue medians, stable-frame median capture and lower confidence for distant calibration outliers.
- Split vision into Sample, FaceDetector, ColorClassifier and Stability; remove unused screen imports.
- Add README badges and recognition edge-case regressions.

# 1.2.0

- Rename the app Rubix and use distinct screen titles.
- Animate a scrambled cube back to solved on the redesigned home screen.
- Brighten yellow to separate it from orange.
- Add hardware-aware flash and per-face photo-picker import with resized, orientation-corrected decoding.
- Add manual paint mode, fixed centers, color counts and undo.
- Replace the home privacy/credits controls with one Settings entry.
- Configure display colors, initials, haptics, native touch sounds, screen awake and animation speed.
- Remove the long completed-move string from the completion screen.
- Deliver one current Rubix.apk.

# 1.1.0

- Deepen sticker colors and add contrasting optional color initials across review and 3D guidance.
- Persist display and haptic settings; add touch, capture and completion feedback.
- Correct the actual current cube from the guide and recalculate a verified solution; cancel safely restores progress.
- Keep the screen awake during scanning and guidance; add a cube-view reset.
- Split Compose UI into focused screen files and simplify secondary controls.
- Deliver one APK supporting ARM 32-bit and 64-bit phones.
- Refresh the README, repository ignores and folder structure.

# 1.0.2

- Fix corrupted symbols in the title, arrows and recovery labels.
- Show a starting-position screen, defaulting to white facing you.
- Let users choose a different front center; remap the 3D cube and solution together.
- Reduce the guide to the cube, instruction and Previous/Next; move extras to More.
- Replace dense recovery dialogs with short staged choices and bounded scrolling.
- Collapse scan editing options and clip/adapt the cube display to avoid overlaps.
- Add UTF-8 and all-24-orientation regression coverage.

# 1.0.1

- Review all six scans through a 3D preview, editable face net and color counts.
- Keep the solve action and status visible at the bottom of review.
- Show an explicit correction dialog instead of silently remaining on review.
- Recover face-image rotations only when one unique valid cube matches.
- Keep Previous and Next visible beneath animated 3D move guidance.
- Show the affected face in 2D with its center color and turn direction.
- Add regression coverage for rotated, ambiguous and invalid scans.

Install 1.0.1 over the earlier development APK. Device-level verification of the new screens remains pending.
