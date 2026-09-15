package com.cubeguide

import com.cubeguide.vision.TriangleFaceDetector
import kotlin.test.*
import org.junit.Test

class TriangleFaceDetectorGeometryTest {
 @Test fun allNineSamplesStayInsideTheNormalizedTriangle() {
  assertEquals(9,TriangleFaceDetector.sampleCenters.size)
  TriangleFaceDetector.sampleCenters.forEach { (x,y) ->
   assertTrue(y in .05f..95f);assertTrue(x in 0f..1f)
   val halfWidth=y/2f+.03f
   assertTrue(x>=.5f-halfWidth && x<=.5f+halfWidth,"$x,$y")
  }
 }
 @Test fun samplePositionsAreDistinctAndFollowFaceletOrder() {
  assertEquals(9,TriangleFaceDetector.sampleCenters.distinct().size)
  assertTrue(TriangleFaceDetector.sampleCenters[0].second<TriangleFaceDetector.sampleCenters[8].second)
  assertTrue(TriangleFaceDetector.sampleCenters[6].first<TriangleFaceDetector.sampleCenters[3].first)
 }
}
