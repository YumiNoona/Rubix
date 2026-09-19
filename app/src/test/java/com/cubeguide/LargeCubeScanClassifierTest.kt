package com.cubeguide

import com.cubeguide.core.CubeColor
import com.cubeguide.vision.*
import kotlin.test.*
import org.junit.Test

class LargeCubeScanClassifierTest {
 private val samples=mapOf(
  CubeColor.WHITE to Sample(90.0,0.0,2.0,0.0,20.0,230.0),CubeColor.RED to Sample(45.0,60.0,35.0,2.0,220.0,200.0),
  CubeColor.GREEN to Sample(60.0,-50.0,35.0,65.0,200.0,190.0),CubeColor.YELLOW to Sample(90.0,-10.0,80.0,30.0,220.0,240.0),
  CubeColor.ORANGE to Sample(65.0,40.0,65.0,15.0,225.0,240.0),CubeColor.BLUE to Sample(40.0,20.0,-65.0,115.0,220.0,200.0),
 )
 @Test fun classifiesBalancedFiveSixAndSevenFaceScans() {
  (5..7).forEach { size ->
   val faces=CubeColor.entries.map { color -> List(size*size) { samples.getValue(color) } }
   val ready=assertIs<LargeCubeClassificationResult.Ready>(LargeCubeScanClassifier.classify(faces,size)).scan
   assertEquals(size*size*6,ready.stickers.size)
   CubeColor.entries.forEach { color -> assertEquals(size*size,ready.stickers.count { it==color }) }
  }
 }
 @Test fun rejectsIncompleteLargeCubeCapture() {
  assertIs<LargeCubeClassificationResult.Rejected>(LargeCubeScanClassifier.classify(emptyList(),5))
 }
}
