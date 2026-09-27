# 3.2.8

- Restored frictionless 3×3 capture: faces are identified by center color and accepted in any order and rotation, while duplicate faces are rejected before they can overwrite a capture.
- Simplified 3×3 preparation and scanning copy around automatic center-based stitching; focused one-face rescans still require the requested center.
- Kept selected-face outlines while making camera focus optional and off by default, and added persistent highlight-color presets in Settings.

# 3.2.7

- Fixed the remaining balanced-scan failure where several valid face rotations had the same correction score. Equal-cost candidates now use the guided capture order as a deterministic tie-breaker instead of blocking Continue.
- Added a regression fixture reconstructed from the reported review net and verified that it resolves to a physically valid cube with a replay-verified solution.

# 3.2.6

- Kept Timed Challenge hints closed until the user taps Hint and added safe end padding around the cube-size control.
- Made every cube preview draggable during layer animation, including the continuously animated Home cube.
- Linked U/R/F/D/L/B selection to a strong outline on the matching cube face and turns the model toward newly selected hidden faces.
- Restored guided scan orientation handling: valid rotations are ranked against the requested top-center pose and corrected before Review, with uncertainty markers remapped to the corrected stickers.

# 3.2.5

- Moved the larger cube-size control into the Virtual Cube app bar and removed the duplicated mode heading so the model receives the full upper canvas.
- Reduced the virtual mode chooser to three icon-and-label cards and removed repeated quick-start actions from Practice.
- Reduced the hint panel to move notation, one turn instruction and balanced Preview/Apply buttons; supporting detail now lives behind an information icon.
- Removed redundant introduction blocks from Home, Learn and puzzle selection so each page begins directly with its primary content.

# 3.2.4

- Let balanced six-face 3×3 scans continue into automatic orientation repair instead of trapping them behind a disabled action, while still blocking incomplete color counts and duplicate centers.
- Removed the Review-to-Scan back-navigation cycle; leaving a completed scan now returns to puzzle selection and cancelling an explicit face rescan still restores Review.
- Replaced Net/3D text with accessible view icons, moved the virtual mode actions toward the bottom of the screen and clarified Free play, Timed challenge and Hint mode descriptions.
- Condensed hints around one verified next move, added an animation preview, simplified redundant large-cube rewind turns and increased 3×3 solution-improvement search before replay verification.

# 3.2.3

- Reflowed 3×3 and large-cube review pages so the preview, compact color counts, validation card and actions form one evenly spaced scrollable layout without a large empty middle section.
- Replaced automatic selection checkmarks with clear filled states and Net/3D icons, and removed redundant check glyphs from capture, practice and completion surfaces.
- Disabled Continue for invalid scans, kept Edit available for correction, and replaced the misleading approval icon with a directional action.
- Refined completion and learning cards with larger gaps, calmer containers and meaningful celebration, problem and solved icons.

# 3.2.2

- Moved every 2×2 and 4×4–7×7 stage onto the same app bar, safe-area and content frame as the polished 3×3 flow, removing the double inset that made those pages look scaled down.
- Made Back from manual entry return directly to Home for every cube size, including after switching the manual editor size.
- Gave lesson sheets, cube previews and Look for/Plan/Moves cards more padding and clearer vertical rhythm.

# 3.2.1

- Aligned back controls to the screen edge throughout detail flows and solving guides.
- Replaced crowded review actions with concise Material icons and single-line labels.
- Rebuilt Learn as a calm three-path landing screen; each path now opens its own focused lesson list with an in-context back action.

# 3.2.0

- Unified every regular cube's preparation page with the 3×3 tutorial format, including a stable solved model, lighting/framing tips and the six-face capture order.
- Replaced the old 4×4 display with the shared solid regular-cube renderer for review, setup, animated guidance and completion.
- Added a compact 2×2–7×7 type selector beside the manual-entry title and wired it to open the matching editor.
- Added guided camera and gallery capture for 5×5, 6×6 and 7×7, including dense-grid detection, capacity-balanced color assignment, complete review nets, manual correction and solid 3D inspection.
- Kept automatic big-cube guidance behind replay verification so a color-balanced scan cannot produce unverified moves.
- Reworked Learn to match the spacious Practice visual language with larger lesson previews, clearer hierarchy and a small-screen-safe lesson sheet.
- Expanded large-cube session and classifier regression coverage and refreshed the installable APK.

# 3.1.0

- Replaced the virtual cube's frame-by-frame auto-fit with a fixed camera target and scale, eliminating zoom and model drift during drag and layer animations from 2×2 through 7×7.
- Added adjustable cube rotation sensitivity and persistent Dark or Light appearance settings.
- Reduced the puzzle catalog to regular cubes from 2×2 through 7×7 and removed the retired Pyraminx, Clock, Megaminx, Skewb and Square-1 implementations.
- Refined Learn with clearer skill paths and lesson cards, and removed completion check marks from the lesson list.
- Made the bottom navigation edge-to-edge and transparent instead of placing it inside a dark framed dock.

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
