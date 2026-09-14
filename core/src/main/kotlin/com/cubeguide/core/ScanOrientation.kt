package com.cubeguide.core

/** Rotate the captured image of one face, without performing a physical cube move. */
fun CubeState.rotateScanFace(face: Face): CubeState {
    val output = stickers.toMutableList()
    val offset = face.ordinal * 9
    for (row in 0..2) for (column in 0..2) {
        output[offset + column * 3 + 2 - row] = stickers[offset + row * 3 + column]
    }
    return CubeState(output)
}

sealed interface ScanOrientationResult {
    data class Unique(val cube: CubeState, val rotatedFaces: List<Face>) : ScanOrientationResult
    data object Ambiguous : ScanOrientationResult
    data object Impossible : ScanOrientationResult
}

object ScanOrientationResolver {
    fun resolve(scan: CubeState): ScanOrientationResult {
        if (CubeColor.entries.any { color -> scan.stickers.count { it == color } != 9 } ||
            Face.entries.map { scan.stickers[it.ordinal * 9 + 4] }.toSet().size != 6
        ) return ScanOrientationResult.Impossible
        if (Validator.validate(scan) == null) return ScanOrientationResult.Unique(scan, emptyList())

        val variants = Face.entries.map { face ->
            val options = mutableListOf(scan)
            repeat(3) { options += options.last().rotateScanFace(face) }
            options.map { it.stickers.subList(face.ordinal * 9, face.ordinal * 9 + 9) }
        }
        var unique: ScanOrientationResult.Unique? = null
        for (combination in 0 until 4096) {
            val rotations = Face.entries.map { (combination shr (it.ordinal * 2)) and 3 }
            val candidate = CubeState(Face.entries.flatMap { face -> variants[face.ordinal][rotations[face.ordinal]] })
            if (Validator.validate(candidate) != null) continue
            if (unique != null && unique.cube != candidate) return ScanOrientationResult.Ambiguous
            if (unique == null) {
                unique = ScanOrientationResult.Unique(candidate, Face.entries.filter { face ->
                    candidate.stickers.subList(face.ordinal * 9, face.ordinal * 9 + 9) !=
                        scan.stickers.subList(face.ordinal * 9, face.ordinal * 9 + 9)
                })
            }
        }
        return unique ?: ScanOrientationResult.Impossible
    }
}
