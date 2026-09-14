package com.cubeguide.core

/** A right-handed change of reference frame; this turns the whole cube, not a layer. */
data class HoldingOrientation(val front: Face, val top: Face) {
    private val forward = Geometry.normals[front.ordinal]
    private val up = Geometry.normals[top.ordinal]
    private val right = up.cross(forward)
    init { require(forward.dot(up) == 0) { "Front and top must be adjacent faces." } }
    private fun transform(v: Vec) = Vec(v.dot(right), v.dot(up), v.dot(forward))
    fun mapFace(face: Face): Face = Face.entries[Geometry.normals.indexOf(transform(Geometry.normals[face.ordinal]))]
    fun apply(cube: CubeState): CubeState {
        val output = cube.stickers.toMutableList()
        Geometry.stickers.forEachIndexed { i, g ->
            val position = transform(g.position)
            val normal = transform(g.normal)
            val destination = Geometry.stickers.indexOfFirst { it.position == position && it.normal == normal }
            check(destination >= 0)
            output[destination] = cube.stickers[i]
        }
        return CubeState(output)
    }
    fun apply(move: Move) = Move(mapFace(move.face), move.turns)
    companion object {
        fun forFront(front: Face) = HoldingOrientation(front, when (front) {
            Face.U -> Face.B
            Face.D -> Face.F
            else -> Face.U
        })
    }
}
