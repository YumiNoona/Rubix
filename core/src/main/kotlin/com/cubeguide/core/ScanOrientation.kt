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
        var best: ScanOrientationResult.Unique? = null
        var bestScore = Int.MAX_VALUE
        var tied = false
        for (combination in 0 until 4096) {
            val rotations = Face.entries.map { (combination shr (it.ordinal * 2)) and 3 }
            val candidate = CubeState(Face.entries.flatMap { face -> variants[face.ordinal][rotations[face.ordinal]] })
            if (Validator.validate(candidate) != null) continue
            // The scanner explicitly tells the user which neighboring center stays
            // above each face. Prefer the valid orientation nearest that guided pose.
            val score=rotations.sumOf { minOf(it,4-it) }
            val resolved=ScanOrientationResult.Unique(candidate,Face.entries.filter { rotations[it.ordinal]!=0 })
            when {
                score<bestScore -> { best=resolved;bestScore=score;tied=false }
                score==bestScore && best?.cube!=candidate -> tied=true
            }
        }
        return when { best==null -> ScanOrientationResult.Impossible;tied -> ScanOrientationResult.Ambiguous;else -> best }
    }
}
