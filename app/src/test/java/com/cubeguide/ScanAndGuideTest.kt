package com.cubeguide

import androidx.lifecycle.SavedStateHandle
import com.cubeguide.core.*
import com.cubeguide.ui.*
import com.cubeguide.vision.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlin.test.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScanAndGuideTest {
 private val anchors=mapOf(
  CubeColor.WHITE to Sample(90.0,0.0,2.0,0.0,20.0,230.0),
  CubeColor.RED to Sample(45.0,60.0,35.0,2.0,220.0,200.0),
  CubeColor.GREEN to Sample(60.0,-50.0,35.0,65.0,200.0,190.0),
  CubeColor.YELLOW to Sample(90.0,-10.0,80.0,30.0,220.0,240.0),
  CubeColor.ORANGE to Sample(65.0,40.0,65.0,15.0,225.0,240.0),
  CubeColor.BLUE to Sample(40.0,20.0,-65.0,115.0,220.0,200.0)
 )
 @Test fun colorsUseCentersAndFlagAmbiguity() {
  anchors.forEach { (color,s) ->
   assertEquals(color,ColorClassifier.nominal(s))
   val shifted=s.copy(l=s.l-4,a=s.a+2,b=s.b-2)
   val result=ColorClassifier.classify(shifted,anchors)
   assertEquals(color,result.first); assertTrue(result.second>0.5)
  }
  val mid=Sample(55.0,50.0,50.0,8.5,222.0,220.0)
  assertTrue(ColorClassifier.classify(mid,anchors).second<0.3)
 }
 @Test fun captureRequiresTimeStabilityAndStillCorners() {
  val stability=Stability(); val face=List(9) { anchors.getValue(CubeColor.GREEN) }
  assertEquals(0f,stability.accept(face,0))
  for(i in 1..5) assertTrue(stability.accept(face,i*100L)<1f)
  assertEquals(1f,stability.accept(face,900))
  assertEquals(0f,stability.accept(emptyList(),1000))
  val corners=listOf(0.1f to 0.1f,0.8f to 0.1f,0.8f to 0.8f,0.1f to 0.8f)
  for(i in 0..8) stability.accept(face,i*130L,corners)
  assertEquals(0f,stability.accept(face,1400,corners.map { it.first+0.1f to it.second }))
  assertEquals(0f,stability.accept(List(9) { anchors.getValue(CubeColor.BLUE) },1600))
 }
 @Test fun guideUndoRecoveryAndRestoration() = runBlocking {
  Dispatchers.setMain(UnconfinedTestDispatcher())
  try {
   val saved=SavedStateHandle(); val vm=CubeViewModel(saved)
   vm.demo(); vm.solve()
   withTimeout(10000) { while(vm.busy) delay(10) }
   assertEquals(Screen.SETUP,vm.screen)
   assertEquals(CubeColor.WHITE,vm.holdingPreview.stickers[22])
   vm.startGuide(); val initial=vm.initial
   assertEquals(Screen.GUIDE,vm.screen)
   assertTrue(initial.apply(vm.moves).solved)
   val first=vm.moves.first(); vm.next()
   assertEquals(initial.apply(first),vm.cube)
   vm.back(); assertEquals(initial,vm.cube)
   vm.next()
   val restored=CubeViewModel(saved)
   assertEquals(vm.cube,restored.cube); assertEquals(vm.step,restored.step); assertEquals(vm.moves,restored.moves)
   val last=vm.moves[vm.step-1]
   val corrected=vm.cube.apply(last.inverse()).apply(last.inverse())
   vm.recoverPrevious(last.inverse())
   withTimeout(10000) { while(vm.busy) delay(10) }
   assertEquals(corrected,vm.initial)
   val actual=Move(Face.F,3); val physical=vm.cube.apply(actual)
   vm.recover(actual)
   withTimeout(10000) { while(vm.busy) delay(10) }
   assertEquals(physical,vm.initial)
   repeat(vm.moves.size) { vm.next() }
   assertEquals(Screen.DONE,vm.screen); assertTrue(vm.cube.solved)
   vm.viewSolution(); assertEquals(physical,vm.cube); assertEquals(0,vm.step); assertTrue(vm.isReplay)
  } finally { Dispatchers.resetMain() }
 }
 @Test fun scannedFacesWithImageRotationAdvanceFromReviewToGuide() = runBlocking {
  Dispatchers.setMain(UnconfinedTestDispatcher())
  try {
   val physical=CubeState.solved().apply(Move.parse("R U2 F' L D B2 R' D2 F L2 B U R2 F D' L' B2 U' F2 R"))
   val scan=physical.rotateScanFace(Face.U).rotateScanFace(Face.B)
   val saved=SavedStateHandle(mapOf("screen" to "REVIEW", "cube" to scan.stickers.joinToString("") { it.ordinal.toString() }))
   val vm=CubeViewModel(saved)
   assertNotNull(vm.validationIssue)
   vm.solve()
   withTimeout(15000) { while(vm.busy) delay(10) }
   assertEquals(Screen.SETUP,vm.screen,vm.message)
   assertEquals(physical,vm.initial)
   assertTrue(vm.initial.apply(vm.moves).solved)
   assertTrue(vm.message.contains("Aligned"))
  } finally { Dispatchers.resetMain() }
 }
 @Test fun chosenStartingSideIsSavedAndDirectionsAreRebasedOnce() = runBlocking {
  Dispatchers.setMain(UnconfinedTestDispatcher())
  try {
   val saved=SavedStateHandle(); val vm=CubeViewModel(saved); vm.demo(); vm.solve()
   withTimeout(15000) { while(vm.busy) delay(10) }
   val physical=vm.initial
   vm.chooseStartingFace(Face.B)
   assertEquals(physical,vm.cube)
   val restored=CubeViewModel(saved)
   assertEquals(Screen.SETUP,restored.screen); assertEquals(Face.B,restored.startingFace)
   restored.startGuide()
   assertEquals(CubeColor.BLUE,restored.initial.stickers[22])
   assertEquals(CubeColor.WHITE,restored.initial.stickers[4])
   assertTrue(restored.initial.apply(restored.moves).solved)
   val before=restored.initial; restored.startGuide(); assertEquals(before,restored.initial)
  } finally { Dispatchers.resetMain() }
 }
 @Test fun guideCorrectionsCancelSafelyAndRecalculateFromTheActualState() = runBlocking {
  Dispatchers.setMain(UnconfinedTestDispatcher())
  try {
   val saved=SavedStateHandle(); val vm=CubeViewModel(saved); vm.demo(); vm.solve()
   withTimeout(15000) { while(vm.busy) delay(10) }
   vm.startGuide(); vm.next()
   val original=vm.cube; val oldStep=vm.step
   vm.beginCorrection(); vm.edit(0,CubeColor.entries.first { it!=original.stickers[0] })
   vm.solve(); assertNotNull(vm.solveError); assertEquals(Screen.CORRECT,vm.screen)
   val restored=CubeViewModel(saved); restored.cancelCorrection()
   assertEquals(original,restored.cube); assertEquals(oldStep,restored.step)
   vm.cancelCorrection(); assertEquals(original,vm.cube)
   val physical=original.apply(Move(Face.R))
   vm.beginCorrection()
   physical.stickers.forEachIndexed { index,color -> vm.edit(index,color) }
   vm.solve()
   withTimeout(15000) { while(vm.busy) delay(10) }
   assertEquals(Screen.GUIDE,vm.screen,vm.message)
   assertEquals(physical,vm.initial); assertEquals(0,vm.step)
   assertTrue(vm.initial.apply(vm.moves).solved)
  } finally { Dispatchers.resetMain() }
 }
 @Test fun manualEditsKeepImpossibleStateOutOfGuide() {
  val vm=CubeViewModel(SavedStateHandle())
  vm.manual(); vm.edit(0,CubeColor.RED); vm.solve()
  assertFalse(vm.busy); assertEquals(Screen.REVIEW,vm.screen); assertTrue(vm.message.contains("exactly 9")); assertTrue(vm.solveError!!.contains("exactly 9"))
  vm.dismissSolveError(); assertNull(vm.solveError)
 }
}
