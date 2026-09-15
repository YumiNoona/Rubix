package com.cubeguide

import com.cubeguide.core.*
import com.cubeguide.vision.*
import kotlin.test.*
import org.junit.Test

class FourByFourScanClassifierTest {
 private val anchors=listOf(
  Sample(90.0,0.0,2.0,0.0,20.0,230.0),Sample(45.0,60.0,35.0,2.0,220.0,200.0),Sample(60.0,-50.0,35.0,65.0,200.0,190.0),
  Sample(90.0,-10.0,80.0,30.0,220.0,240.0),Sample(65.0,40.0,65.0,15.0,225.0,240.0),Sample(40.0,20.0,-65.0,115.0,220.0,200.0)
 )
 @Test fun sixFacesReconstructAReplaySolvableState() {
  val state=FourByFourState.solved().apply(FourByFourMove.parse("Rw U F2 Lw' D B R2"))
  val faces=(0..5).map { face -> state.stickers.drop(face*16).take(16).map { anchors[it.ordinal] } }
  val ready=assertIs<FourByFourClassificationResult.Ready>(FourByFourScanClassifier.classify(faces)).scan
  assertEquals(state,ready.state)
  assertIs<VerifiedSolution.Ready>(PuzzleSolverGate.solve(FourByFourEngine,ready.state))
 }
 @Test fun missingFaceAndMissingColorAreRejected() {
  assertIs<FourByFourClassificationResult.Rejected>(FourByFourScanClassifier.classify(emptyList()))
  val faces=List(6) { List(16) { anchors[CubeColor.WHITE.ordinal] } }
  assertIs<FourByFourClassificationResult.Rejected>(FourByFourScanClassifier.classify(faces))
 }
}
