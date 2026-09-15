package com.cubeguide.core

import kotlin.random.Random
import kotlin.test.*
import org.junit.Test

class PyraminxTest {
 @Test fun notationRoundTripsAndMovesHaveOrderThree() {
  val moves=PyraminxMove.parse("U L' R B' u l' r b'")
  assertEquals("U L' R B' u l' r b'",moves.joinToString(" ") { it.notation })
  moves.forEach { move -> assertEquals(PyraminxState.solved(),PyraminxState.solved().apply(List(3){move})) }
 }
 @Test fun inverseSequenceRestoresRandomStates() {
  val random=Random(77);repeat(80) {
   val moves=List(30) { PyraminxMove(PyraminxAxis.entries.random(random),random.nextInt(1,3),random.nextInt(5)==0) }
   val scrambled=PyraminxState.solved().apply(moves)
   assertNull(PyraminxValidator.validate(scrambled))
   assertEquals(PyraminxState.solved(),scrambled.apply(moves.asReversed().map(PyraminxMove::inverse)))
  }
 }
 @Test fun validatorRejectsOrientationAndParityImpossibilities() {
  assertNotNull(PyraminxValidator.validate(PyraminxState.solved().copy(edgeOrientation=listOf(1,0,0,0,0,0))))
  assertNotNull(PyraminxValidator.validate(PyraminxState.solved().copy(edgePermutation=listOf(1,0,2,3,4,5))))
 }
 @Test fun solverGateReplaysRandomScramblesToSolved() {
  val random=Random(91);repeat(35) {
   val scramble=List(24) { PyraminxMove(PyraminxAxis.entries.random(random),random.nextInt(1,3),random.nextInt(6)==0) }
   val state=PyraminxState.solved().apply(scramble)
   val result=assertIs<VerifiedSolution.Ready>(PuzzleSolverGate.solve(PyraminxEngine,state))
   assertTrue(state.apply(result.moves.flatMap(PyraminxMove::parse)).solved)
  }
 }
}
