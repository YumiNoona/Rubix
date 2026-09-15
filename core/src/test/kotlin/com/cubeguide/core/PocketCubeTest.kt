package com.cubeguide.core

import kotlin.random.Random
import kotlin.test.*
import org.junit.Test

class PocketCubeTest {
 @Test fun allMovesHaveWorkingInverseAndFourTurnIdentity() {
  Face.entries.forEach { face ->
   val start=PocketCube.solved();val turn=Move(face)
   assertEquals(start,start.apply(turn).apply(turn.inverse()),face.name)
   assertEquals(start,start.apply(List(4){turn}),face.name)
  }
 }
 @Test fun scannedCornerStatesEmbedAndReplayVerifiedSolutions() {
  val random=Random(222)
  repeat(24) {
   val moves=buildList<Move> {
    repeat(12) {
     var move:Move
     do move=Move(Face.entries[random.nextInt(6)],random.nextInt(1,4)) while(lastOrNull()?.face==move.face)
     add(move)
    }
   }
   val state=PocketCube.solved().apply(moves)
   assertNotNull(state.embeddedThreeByThree())
   val result=PuzzleSolverGate.solve(TwoByTwoEngine,state)
   assertIs<VerifiedSolution.Ready>(result)
   assertTrue(result.moves.size<=20)
  }
 }
 @Test fun impossibleCountsAndTwistedCornerAreRejected() {
  val badCount=PocketCube(PocketCube.solved().stickers.toMutableList().also { it[0]=CubeColor.RED })
  assertIs<VerifiedSolution.Rejected>(PuzzleSolverGate.solve(TwoByTwoEngine,badCount))
  val twisted=PocketCube(PocketCube.solved().stickers.toMutableList().also {
   val corner=PocketCube.geometry.withIndex().filter { entry -> entry.value.position==Vec(1,1,1) }.map { entry -> entry.index }
   val first=it[corner[0]];it[corner[0]]=it[corner[1]];it[corner[1]]=it[corner[2]];it[corner[2]]=first
  })
  assertIs<VerifiedSolution.Rejected>(PuzzleSolverGate.solve(TwoByTwoEngine,twisted))
 }
}
