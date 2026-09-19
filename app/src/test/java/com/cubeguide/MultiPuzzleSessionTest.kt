package com.cubeguide

import com.cubeguide.core.*
import com.cubeguide.ui.*
import com.cubeguide.vision.Sample
import kotlin.test.*
import org.junit.Test

class MultiPuzzleSessionTest {
 private val samples=mapOf(
  CubeColor.WHITE to Sample(90.0,0.0,2.0,0.0,20.0,230.0),
  CubeColor.RED to Sample(45.0,60.0,35.0,2.0,220.0,200.0),
  CubeColor.GREEN to Sample(60.0,-50.0,35.0,65.0,200.0,190.0),
  CubeColor.YELLOW to Sample(90.0,-10.0,80.0,30.0,220.0,240.0),
  CubeColor.ORANGE to Sample(65.0,40.0,65.0,15.0,225.0,240.0),
  CubeColor.BLUE to Sample(40.0,20.0,-65.0,115.0,220.0,200.0),
 )

 @Test fun pocketCubeScansReviewsSolvesAndReplaysEveryMove() {
  val state=PocketCube.solved().apply(Move.parse("R U F2 L D'"))
  val session=MultiPuzzleSession(PuzzleId.TWO_BY_TWO);session.beginScan()
  state.stickers.chunked(4).forEach { assertTrue(session.addCapture(it.map(samples::getValue))) }
  assertEquals(MultiPuzzleStage.REVIEW,session.stage);assertNull(session.validation())
  val original=session.colors[0];val replacement=CubeColor.entries.first { it!=original }
  session.beginEdit();session.setColor(0,replacement);assertTrue(session.canUndo);session.undoEdit();assertEquals(original,session.colors[0]);session.redoEdit();assertEquals(replacement,session.colors[0]);session.cancelEdit();assertEquals(original,session.colors[0])
  val solution=session.calculateSolution();assertIs<VerifiedSolution.Ready>(solution);session.acceptSolution(solution);session.startGuide()
  while(session.stage==MultiPuzzleStage.GUIDE) session.next()
  assertEquals(MultiPuzzleStage.DONE,session.stage);assertTrue(PocketCube(session.colors).solved)
 }

 @Test fun fourByFourScansReviewsSolvesAndReplaysEveryMove() {
  val state=FourByFourState.solved().apply(FourByFourMove.parse("Rw U F2 Lw' D B"))
  val session=MultiPuzzleSession(PuzzleId.FOUR_BY_FOUR);session.beginScan()
  state.stickers.chunked(16).forEach { assertTrue(session.addCapture(it.map(samples::getValue))) }
  assertEquals(MultiPuzzleStage.REVIEW,session.stage);assertNull(session.validation())
  val solution=session.calculateSolution();assertIs<VerifiedSolution.Ready>(solution);session.acceptSolution(solution);session.startGuide()
  while(session.stage==MultiPuzzleStage.GUIDE) session.next()
  assertEquals(MultiPuzzleStage.DONE,session.stage);assertTrue(FourByFourState(session.colors).solved)
 }

 @Test fun fiveSixAndSevenCaptureReviewAndEditAllStickers() {
  listOf(PuzzleId.FIVE_BY_FIVE,PuzzleId.SIX_BY_SIX,PuzzleId.SEVEN_BY_SEVEN).forEach { puzzle ->
   val session=MultiPuzzleSession(puzzle);session.beginScan()
   val size=session.spec.squareSize!!
   CubeColor.entries.forEach { color -> assertTrue(session.addCapture(List(size*size) { samples.getValue(color) })) }
   assertEquals(MultiPuzzleStage.REVIEW,session.stage);assertNull(session.validation());assertEquals(size*size*6,session.colors.size)
   assertFalse(session.supportsAutomaticSolution)
   session.beginEdit();val original=session.colors[0];val replacement=CubeColor.entries.first { it!=original };session.setColor(0,replacement);session.undoEdit();assertEquals(original,session.colors[0]);session.cancelEdit()
  }
 }
}
