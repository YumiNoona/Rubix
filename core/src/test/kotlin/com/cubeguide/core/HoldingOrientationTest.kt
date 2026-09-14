package com.cubeguide.core

import org.junit.Test
import kotlin.test.*

class HoldingOrientationTest {
 @Test fun everyHoldingPositionMapsMovesWithoutChangingThePhysicalSolution() {
  val physical=CubeState.solved().apply(Move.parse("R U2 F' L D B2 R' D2 F L2 B U R2 F D' L' B2 U' F2 R"))
  val solution=Solver.solve(physical)
  for(front in Face.entries) for(top in Face.entries) {
   if(Geometry.normals[front.ordinal].dot(Geometry.normals[top.ordinal])!=0) continue
   val holding=HoldingOrientation(front,top)
   val oriented=holding.apply(physical)
   assertEquals(physical.stickers[front.ordinal*9+4],oriented.stickers[22])
   assertEquals(physical.stickers[top.ordinal*9+4],oriented.stickers[4])
   assertNull(Validator.validate(oriented))
   assertTrue(oriented.apply(solution.map { holding.apply(it) }).solved)
   for(face in Face.entries) for(turns in 1..3) {
    val move=Move(face,turns)
    assertEquals(holding.apply(physical.apply(move)),oriented.apply(holding.apply(move)))
   }
  }
 }
 @Test fun whiteFrontDefaultHasBlueOnTopAndImpossiblePositionsAreRejected() {
  val holding=HoldingOrientation.forFront(Face.U).apply(CubeState.solved())
  assertEquals(CubeColor.WHITE,holding.stickers[22]); assertEquals(CubeColor.BLUE,holding.stickers[4])
  assertFailsWith<IllegalArgumentException> { HoldingOrientation(Face.U,Face.D) }
 }
}
