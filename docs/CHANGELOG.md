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
