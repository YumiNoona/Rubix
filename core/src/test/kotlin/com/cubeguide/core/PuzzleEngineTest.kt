package com.cubeguide.core

import kotlin.test.*
import org.junit.Test

class PuzzleEngineTest {
 @Test fun threeByThreeSolutionIsValidatedAndReplayVerified() {
  val state=CubeState.solved().apply(Move.parse("R U2 F' L D B2"))
  val result=PuzzleSolverGate.solve(ThreeByThreeEngine,state)
  assertIs<VerifiedSolution.Ready>(result)
  assertEquals(PuzzleId.THREE_BY_THREE,result.puzzle)
  assertTrue(result.moves.size<=20)
 }
 @Test fun invalidInputNeverReachesTheSolver() {
  val invalid=CubeState(CubeState.solved().stickers.toMutableList().also { it[0]=CubeColor.RED })
  val result=PuzzleSolverGate.solve(ThreeByThreeEngine,invalid)
  assertIs<VerifiedSolution.Rejected>(result)
  assertTrue(result.reason.isNotBlank())
 }
 @Test fun gateRejectsAnEngineWhoseMovesDoNotActuallySolve() {
  val broken=object:PuzzleEngine<Int> {
   override val puzzle=PuzzleId.CLOCK
   override fun validate(state:Int)=null
   override fun solve(state:Int)=listOf("fake")
   override fun apply(state:Int,move:String)=state
   override fun isSolved(state:Int)=state==0
  }
  val result=PuzzleSolverGate.solve(broken,7)
  assertIs<VerifiedSolution.Rejected>(result)
  assertTrue(result.reason.contains("did not replay"))
 }
}
