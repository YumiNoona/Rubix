package com.cubeguide

import com.cubeguide.core.Face
import com.cubeguide.core.Move
import com.cubeguide.play.VirtualCube
import com.cubeguide.play.VirtualMove
import com.cubeguide.play.virtualScramble
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VirtualCubeTest {
    @Test fun everySupportedCubeReturnsToSolvedAfterMoveAndInverse() {
        for (size in 2..7) {
            for (face in Face.entries) {
                val moved = VirtualCube.solved(size).apply(Move(face, 1))
                assertFalse("$size x $size $face should move", moved.solved)
                assertTrue("$size x $size $face inverse should restore", moved.apply(Move(face, 3)).solved)
            }
        }
    }

    @Test fun fourQuarterTurnsAreIdentityForEverySupportedSize() {
        for (size in 2..7) {
            for (face in Face.entries) {
                var cube = VirtualCube.solved(size)
                repeat(4) { cube = cube.apply(Move(face, 1)) }
                assertEquals(VirtualCube.solved(size).stickers, cube.stickers)
            }
        }
    }

    @Test fun scrambleAvoidsRepeatedFacesAndCanBeUndone() {
        for (size in 2..7) {
            val moves = virtualScramble(size, 30)
            assertTrue(moves.zipWithNext().all { (a, b) -> a.face != b.face })
            var cube = moves.fold(VirtualCube.solved(size)) { state, move -> state.apply(move) }
            assertFalse(cube.solved)
            moves.asReversed().forEach { cube = cube.apply(it.inverse(), record = false) }
            assertTrue(cube.solved)
        }
    }

    @Test fun encodedCubeRestoresItsStickersAndUndoHistory() {
        val cube = virtualScramble(4, 12).fold(VirtualCube.solved(4)) { state, move -> state.apply(move) }
        assertEquals(cube, VirtualCube.decode(cube.encode()))
    }

    @Test fun innerLayersAndWideTurnsRoundTripOnLargeCubes() {
        for (size in 4..7) {
            for (face in Face.entries) {
                val inner = VirtualMove(face, depth = 1)
                val wide = VirtualMove(face, width = 2, turns = 3)
                val start = VirtualCube.solved(size)
                assertEquals(start.stickers, start.apply(inner).apply(inner.inverse()).stickers)
                assertEquals(start.stickers, start.apply(wide).apply(wide.inverse()).stickers)
            }
        }
    }

    @Test fun encodedCubePreservesLayerAwareMoveHistory() {
        val cube = VirtualCube.solved(7)
            .apply(VirtualMove(Face.R, depth = 2, turns = 3))
            .apply(VirtualMove(Face.U, width = 3, turns = 2))
        assertEquals(cube, VirtualCube.decode(cube.encode()))
    }
}
