package com.cubeguide

import com.cubeguide.core.*
import com.cubeguide.vision.*
import kotlin.test.*
import org.junit.Test

class PyraminxScanClassifierTest {
 private val samples=mapOf(
  CubeColor.GREEN to Sample(58.0,-48.0,34.0,64.0,205.0,190.0),CubeColor.RED to Sample(44.0,61.0,36.0,1.0,220.0,195.0),
  CubeColor.BLUE to Sample(39.0,20.0,-64.0,114.0,215.0,195.0),CubeColor.YELLOW to Sample(89.0,-8.0,79.0,29.0,225.0,240.0)
 )
 @Test fun fourFaceSamplesReconstructAndSolve() {
  val moves=PyraminxMove.parse("U R' l B u' L")
  val facelets=PyraminxFacelets.solved().apply(moves)
  val mapping=mapOf(PyraminxColor.GREEN to CubeColor.GREEN,PyraminxColor.RED to CubeColor.RED,PyraminxColor.BLUE to CubeColor.BLUE,PyraminxColor.YELLOW to CubeColor.YELLOW)
  val faces=(0..3).map { face -> facelets.stickers.drop(face*9).take(9).map { samples.getValue(mapping.getValue(it)) } }
  val ready=assertIs<PyraminxClassificationResult.Ready>(PyraminxScanClassifier.classify(faces)).scan
  assertEquals(PyraminxState.solved().apply(moves),ready.state)
  assertIs<VerifiedSolution.Ready>(PuzzleSolverGate.solve(PyraminxEngine,ready.state))
 }
 @Test fun incompleteCaptureIsRejected() {
  assertIs<PyraminxClassificationResult.Rejected>(PyraminxScanClassifier.classify(emptyList()))
 }
}
