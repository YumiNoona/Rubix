package com.cubeguide.play

import com.cubeguide.core.*

/** A face turn that can address outer, inner, or several adjacent layers. */
data class VirtualMove(
    val face: Face,
    val depth: Int = 0,
    val width: Int = 1,
    val turns: Int = 1,
) {
    init {
        require(depth >= 0)
        require(width >= 1)
        require(turns in 1..3)
    }

    fun inverse() = copy(turns = if (turns == 2) 2 else 4 - turns)

    val notation: String
        get() {
            val layer = when {
                depth > 0 -> "${depth + 1}${face.name}"
                width == 1 -> face.name
                width == 2 -> "${face.name}w"
                else -> "$width${face.name}w"
            }
            return layer + when (turns) { 2 -> "2"; 3 -> "'"; else -> "" }
        }

    internal fun encode() = "${face.name}:$depth:$width:$turns"

    companion object {
        fun from(move: Move) = VirtualMove(move.face, turns = move.turns)
        internal fun decode(value: String): VirtualMove? = runCatching {
            val fields = value.split(':')
            VirtualMove(Face.valueOf(fields[0]), fields[1].toInt(), fields[2].toInt(), fields[3].toInt())
        }.getOrNull()
    }
}

data class VirtualCube(
    val size: Int,
    val stickers: List<CubeColor>,
    val history: List<VirtualMove> = emptyList(),
) {
    init {
        require(size in 2..7)
        require(stickers.size == 6 * size * size)
    }

    fun apply(move: Move, record: Boolean = true) = apply(VirtualMove.from(move), record)

    fun apply(move: VirtualMove, record: Boolean = true): VirtualCube {
        require(move.depth + move.width <= size) { "Move layers must fit a ${size}x$size cube" }
        var current = stickers
        val axis = Geometry.normals[move.face.ordinal]
        val edge = size - 1
        val layerCoordinates = (move.depth until move.depth + move.width).map { edge - 2 * it }.toSet()
        repeat(move.turns) {
            val output = current.toMutableList()
            val geometry = geometry(size)
            geometry.forEachIndexed { index, sticker ->
                if (sticker.position.dot(axis) in layerCoordinates) {
                    val position = Geometry.rotate(sticker.position, axis)
                    val normal = Geometry.rotate(sticker.normal, axis)
                    val target = geometry.indexOfFirst { it.position == position && it.normal == normal }
                    check(target >= 0)
                    output[target] = current[index]
                }
            }
            current = output
        }
        return VirtualCube(size, current, if (record) history + move else history)
    }

    fun withoutLastHistory() = copy(history = history.dropLast(1))
    fun reset() = solved(size)

    fun encode(): String = buildString {
        append(size); append('|')
        append(stickers.joinToString("") { it.ordinal.toString() }); append('|')
        append(history.joinToString(";") { it.encode() })
    }

    val solved get() = Face.entries.all { face ->
        (0 until size * size).all { stickers[face.ordinal * size * size + it] == stickers[face.ordinal * size * size] }
    }

    companion object {
        fun solved(size: Int) = VirtualCube(
            size,
            Face.entries.flatMap { face -> List(size * size) { CubeColor.entries[face.ordinal] } },
        )

        fun decode(value: String): VirtualCube? = runCatching {
            val fields = value.split('|', limit = 3)
            val size = fields[0].toInt()
            val stickers = fields[1].map { CubeColor.entries[it.digitToInt()] }
            val encodedHistory = fields.getOrElse(2) { "" }
            val history = when {
                encodedHistory.isBlank() -> emptyList()
                ':' in encodedHistory -> encodedHistory.split(';').mapNotNull(VirtualMove::decode)
                else -> Move.parse(encodedHistory).map(VirtualMove::from)
            }
            VirtualCube(size, stickers, history)
        }.getOrNull()

        fun geometry(size: Int): List<StickerGeometry> {
            val edge = size - 1
            return Face.entries.flatMap { face ->
                (0 until size * size).map { index ->
                    val normal = Geometry.normals[face.ordinal]
                    val right = Geometry.rights[face.ordinal]
                    val down = Geometry.downs[face.ordinal]
                    StickerGeometry(
                        normal * edge + right * (2 * (index % size) - edge) + down * (2 * (index / size) - edge),
                        normal, right, down,
                    )
                }
            }
        }
    }
}

fun virtualScramble(size: Int, count: Int = if (size == 2) 9 else 18): List<VirtualMove> {
    val out = mutableListOf<VirtualMove>()
    while (out.size < count) {
        val maxWidth = if (size >= 4) minOf(2, size / 2) else 1
        val move = VirtualMove(Face.entries.random(), width = (1..maxWidth).random(), turns = (1..3).random())
        if (out.lastOrNull()?.face != move.face) out += move
    }
    return out
}
