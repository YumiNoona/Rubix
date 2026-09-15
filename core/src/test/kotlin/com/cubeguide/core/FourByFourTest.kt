package com.cubeguide.core

import kotlin.random.Random
import kotlin.test.*
import org.junit.Test

class FourByFourTest {
 @Test fun moveNotationAndGeometryRoundTrip() {
  val moves=FourByFourMove.parse("R U2 F' Rw Uw2 Bw'")
  assertEquals("R U2 F' Rw Uw2 Bw'",moves.joinToString(" "){it.notation})
  moves.forEach { move -> assertEquals(FourByFourState.solved(),FourByFourState.solved().apply(List(4){move})) }
  val random=Random(44);repeat(40) {
   val sequence=List(30) { FourByFourMove(Face.entries.random(random),random.nextInt(1,4),random.nextBoolean()) }
   assertEquals(FourByFourState.solved(),FourByFourState.solved().apply(sequence).apply(sequence.asReversed().map(FourByFourMove::inverse)))
  }
 }
 @Test fun validatorRejectsWrongCounts() {
  val bad=FourByFourState.solved().stickers.toMutableList().also { it[0]=CubeColor.RED }
  assertNotNull(FourByFourValidator.validate(FourByFourState(bad)))
 }
 @Test fun threePhaseSolutionReplaysOnIndependentFaceletModel() {
  val state=FourByFourState.solved().apply(FourByFourMove.parse("Rw U R2 Fw' D Lw2 B"))
  val checked=PuzzleSolverGate.solve(FourByFourEngine,state)
  val result=assertIs<VerifiedSolution.Ready>(checked,checked.toString())
  assertTrue(state.apply(result.moves.flatMap(FourByFourMove::parse)).solved)
 }
 @Test fun randomizedThreePhaseSolutionsReplayToUniformFaces() {
  val random=Random(404);repeat(8) {
   val scramble=List(18) { FourByFourMove(Face.entries.random(random),random.nextInt(1,4),random.nextBoolean()) }
   val state=FourByFourState.solved().apply(scramble)
   val checked=PuzzleSolverGate.solve(FourByFourEngine,state)
   assertIs<VerifiedSolution.Ready>(checked,checked.toString())
  }
 }
}
