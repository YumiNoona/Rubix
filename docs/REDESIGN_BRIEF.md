# Rubix redesign brief

Build a sleek personal Android 3x3 cube solver with a clear capture-to-completion flow. Brand the app Rubix. Use a playful scramble-to-solved animation on home, minimal action choices, distinct screen titles and layouts, and bright yellow that is easy to distinguish from deeper orange.

## Main flow

Home -> camera or gallery capture -> editable review -> holding orientation -> animated move guide -> completion.

Manual entry joins the same flow at review. Guide correction returns to an editor of the actual current state, then produces a new verified guide. Cancel preserves the previous solve.

## Requirements implemented

- One primary home action, with manual entry, demo and Settings as secondary actions.
- Flash support with hardware availability checks; image import through the Android photo picker.
- Bounded image decoding, orientation normalization, nine-sticker detection and expected-center validation.
- Color-brush manual entry, fixed manual centers, a large face editor, color counts and undo.
- Persistent display palette, initials, touch sounds, haptics, animation speed and screen-awake settings.
- Distinct screen titles, fixed guide confirmation controls and secondary recovery tools in More.
- Required third-party notices retained in source/assets without a credits link in the main user flow.
- One current Android phone APK.

## Suggestions for future iterations

- Named display palettes for multiple cube brands.
- Optional local solve history and saved cube sessions.
- A short interactive tutorial that teaches clockwise turns before the first solve.
- Physical-device testing across glare, lighting, small displays, large fonts and stickerless cube shades before claiming scan accuracy.
