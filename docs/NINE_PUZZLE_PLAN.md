# Nine-puzzle scan and solve program

Rubix will support the nine distinct puzzle types in current WCA speed-solving events: 2x2, 3x3, 4x4, 5x5, Clock, Megaminx, Pyraminx, Skewb and Square-1. One-handed and blindfolded events reuse these physical puzzles and are not separate scan formats.

## Release gate

A puzzle is marked available only when all of these are complete:

1. A puzzle-specific scan sequence captures every independent visible state value.
2. Review offers a complete 2D net plus a rotatable 3D model with direct correction.
3. Validation rejects bad counts, duplicate pieces, orientation errors, parity errors and inconsistent linked parts that apply to that puzzle.
4. The offline engine accepts an arbitrary legal scanned state rather than only reversing an app-generated scramble.
5. Every returned token parses, applies to the same state model and replays to the puzzle's solved predicate.
6. Guide animations use the same move implementation as verification.
7. Random-state, impossible-state, restoration and Android flow tests pass.

PuzzleSolverGate enforces items 3–5. A solver cannot become visible merely by returning a move list.

## Puzzle contracts

| Puzzle | Capture | State and validation | Offline solution | Views |
|---|---|---|---|---|
| 2x2 | Six 2x2 faces in a fixed holding sequence | Eight corners; color counts, identity, twist and permutation | Corner solver, replayed on the 2x2 model | 2D six-face net and solid 2x2 cubies |
| 3x3 | Six 3x3 faces | Centers, 12 edges, 8 corners, orientation and parity | Existing two-phase engine | Existing net and animated cubies |
| 4x4 | Six 4x4 faces | Centers, paired wings, corners and even-cube parity | Centers → edge pairing → reduced 3x3 → parity | Full inner/outer slice model |
| 5x5 | Six 5x5 faces | Fixed centers, center orbits, wings, middle edges and corners | Center/edge reduction → verified 3x3 | Full inner/outer slice model |
| Clock | Front/back dial readings plus four pins | Four shared corner dials must mirror; values modulo 12 | Exact 14-coordinate linear solve | 2D dial editor and two-sided 3D model |
| Megaminx | Twelve pentagonal faces, 11 stickers each | 20 corners, 30 edges and 12 centers | Deterministic layer reduction with full replay | Dodecahedral net and solid model |
| Pyraminx | Four triangular faces | Four tips, four axial pieces and six edges | Pruned optimal search plus tip alignment | Triangular net and tetrahedral model |
| Skewb | Six five-part faces | Eight corners and six centers under corner-axis turns | Pruned optimal search | Skewb net and solid corner-turn model |
| Square-1 | Top, bottom and equator/shape | Piece widths, slice legality, permutation and shape parity | Shape phase then permutation phase | Width-aware circular 2D and solid shape-shifting model |

## Delivery order

The implementation proceeds as vertical, testable slices while main keeps the stable 3x3 release:

1. Shared registry, scan-shape metadata and replay-verification gate.
2. 2x2 capture, review, editor, renderer and guide on the completed TwoByTwoEngine.
3. Pyraminx and Skewb, whose state spaces are bounded enough for offline pruning tables.
4. Clock, using exact arithmetic over fourteen independent dial coordinates.
5. Square-1, including non-cubic shape validation before permutation solving.
6. 4x4 then 5x5 reduction, including inner-slice animation and parity cases.
7. Megaminx last because it has the largest capture surface and mobile solve cost.

Each slice is merged only after the release gate passes. The catalog can describe unfinished puzzles, but its Scan and Solve action stays disabled until then.

## Current feature-branch status

- Nine typed puzzle definitions and responsive catalog: complete.
- Shared validation/solve/replay gate: complete and used by 3x3.
- 2x2 state, arbitrary-state solver adapter and replay tests: complete.
- Square camera sampling generalized for 2x2 through 5x5: complete.
- 2x2 scan/review/guide UI and the remaining seven puzzle engines: in progress.
