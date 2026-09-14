package com.cubeguide.core

import kotlin.test.*
import org.junit.Test

class ScanOrientationTest {
    private val scramble = "R U2 F' L D B2 R' D2 F L2 B U R2 F D' L' B2 U' F2 R"

    @Test fun uniquelyRotatedScansRecoverTheActualCube() {
        val physical = CubeState.solved().apply(Move.parse(scramble))
        val scan = physical.rotateScanFace(Face.U).rotateScanFace(Face.B)
            .rotateScanFace(Face.D).rotateScanFace(Face.D)
        assertNotNull(Validator.validate(scan))
        val result = assertIs<ScanOrientationResult.Unique>(ScanOrientationResolver.resolve(scan))
        assertEquals(physical, result.cube)
        assertEquals(setOf(Face.U, Face.B, Face.D), result.rotatedFaces.toSet())
        assertTrue(result.cube.apply(Solver.solve(result.cube)).solved)
    }

    @Test fun ambiguousFaceImagesAreNeverAutomaticallyChosen() {
        val physical = CubeState.solved().apply(Move(Face.R))
        val scan = physical.rotateScanFace(Face.F)
        assertNotNull(Validator.validate(scan))
        assertEquals(ScanOrientationResult.Ambiguous, ScanOrientationResolver.resolve(scan))
    }

    @Test fun validScansArePreservedAndWrongColorCountsAreNotGuessed() {
        val physical = CubeState.solved().apply(Move.parse(scramble))
        val valid = assertIs<ScanOrientationResult.Unique>(ScanOrientationResolver.resolve(physical))
        assertEquals(physical, valid.cube)
        assertTrue(valid.rotatedFaces.isEmpty())
        val invalid = CubeState(physical.stickers.toMutableList().also { it[0] = it[0].let { c -> CubeColor.entries.first { it != c } } })
        assertEquals(ScanOrientationResult.Impossible, ScanOrientationResolver.resolve(invalid))
    }
}
