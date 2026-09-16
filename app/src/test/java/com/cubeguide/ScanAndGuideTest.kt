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
 @Test fun balancedClassificationPreventsExtraWhiteStickers() {
  val samples=CubeState.solved().stickers.map { anchors.getValue(it) }.toMutableList()
  val glare=listOf(10,19,28,37)
  glare.forEach { samples[it]=anchors.getValue(CubeColor.WHITE) }
  val independent=samples.mapIndexed { index,sample -> if(index%9==4) CubeColor.entries[index/9] else ColorClassifier.classify(sample,anchors).first }
  assertEquals(13,independent.count { it==CubeColor.WHITE })
  val centers=Face.entries.associate { it.ordinal*9+4 to CubeColor.entries[it.ordinal] }
  val result=ColorClassifier.balanced(samples,anchors,centers,CubeColor.entries.associateWith { 9 })
  CubeColor.entries.forEach { color -> assertEquals(9,result.count { it.color==color },color.label) }
  centers.forEach { (index,color) -> assertEquals(color,result[index].color) }
  assertTrue(glare.all { result[it].confidence<0.18 })
 }
 @Test fun redHueWrapAndTemporalNoiseStayRed() {
  val red=anchors.getValue(CubeColor.RED)
  val reds=listOf(red.copy(hue=179.0),red.copy(hue=1.0),red.copy(hue=178.0),red.copy(hue=2.0))
  val result=Sample.median(reds)
  assertEquals(CubeColor.RED,ColorClassifier.nominal(result))
  assertEquals(CubeColor.RED,ColorClassifier.classify(result,anchors).first)
  val stability=Stability()
  repeat(8) { i -> stability.accept(List(9) { reds[i%reds.size].copy(l=red.l+(i%3-1)*1.5) },i*130L) }
  assertEquals(9,stability.stableSamples.size)
  assertTrue(stability.stableSamples.all { ColorClassifier.nominal(it)==CubeColor.RED })
  stability.reset();assertTrue(stability.stableSamples.isEmpty())
 }
 @Test fun uncalibratedOutliersDoNotReceiveHighConfidence() {
  val outlier=Sample(10.0,-120.0,120.0,60.0,240.0,50.0)
  assertTrue(ColorClassifier.classify(outlier,anchors).second<0.18)
  val saved=SavedStateHandle();val vm=CubeViewModel(saved);vm.scan()
  repeat(6) {
   val face=vm.pose.face;val samples=List(9) { anchors.getValue(CubeColor.entries[face.ordinal]) }.toMutableList()
   if(face==Face.R || face==Face.F) samples[0]=outlier
   assertTrue(vm.importFace(Detection(samples,emptyList(),"")))
  }
  assertTrue(9 in vm.lowConfidence);assertTrue(18 in vm.lowConfidence)
  val unchanged=vm.cube;vm.edit(18,vm.cube.stickers[18])
  assertEquals(unchanged,vm.cube);assertFalse(18 in vm.lowConfidence);assertTrue(9 in vm.lowConfidence)
  assertEquals(vm.lowConfidence,CubeViewModel(saved).lowConfidence)
 }
 @Test fun galleryCaptureChecksCenterAndBuildsAllSixFaces() {
  val vm=CubeViewModel(SavedStateHandle()); vm.scan()
  val target=CubeState.solved().apply(Move.parse("R U F2 L D"))
  val wrong=CubeColor.entries.first { it!=CubeColor.entries[vm.pose.face.ordinal] }
  assertFalse(vm.importFace(Detection(List(9) { anchors.getValue(wrong) },emptyList(),"")))
  assertEquals(0,vm.scanIndex)
  assertFalse(vm.importFace(Detection(emptyList(),emptyList(),"No face")))
  repeat(6) {
   val face=vm.pose.face
   val samples=target.stickers.drop(face.ordinal*9).take(9).map { anchors.getValue(it) }
   assertTrue(vm.importFace(Detection(samples,emptyList(),"")))
  }
  assertEquals(Screen.REVIEW,vm.screen)
  assertEquals(target,vm.cube)
  assertFalse(vm.importFace(Detection(List(9) { anchors.getValue(CubeColor.WHITE) },emptyList(),"")))
 }
 @Test fun manualEntryCanUndoPaintAndRestoreItsMode() {
  val saved=SavedStateHandle(); val vm=CubeViewModel(saved); vm.manual()
  val original=vm.cube
  vm.edit(0,CubeColor.RED); vm.edit(1,CubeColor.BLUE)
  assertTrue(vm.canUndoEdit)
  vm.undoEdit(); assertEquals(CubeColor.WHITE,vm.cube.stickers[1])
  assertEquals(CubeColor.RED,vm.cube.stickers[0])
  vm.undoEdit(); assertEquals(original,vm.cube); assertFalse(vm.canUndoEdit)
  assertTrue(CubeViewModel(saved).manualEntry)
  vm.demo(); assertFalse(vm.manualEntry)
 }
 @Test fun focusedEditorSupportsFaceNavigationUndoRedoAndCancel() {
  val vm=CubeViewModel(SavedStateHandle());vm.demo();val original=vm.cube
  vm.beginEdit(Face.F);assertEquals(Screen.EDIT,vm.screen);assertEquals(Face.F.ordinal,vm.editorFace)
  val index=Face.F.ordinal*9
  val replacement=CubeColor.entries.first { it!=vm.cube.stickers[index] }
  vm.edit(index,replacement);assertEquals(replacement,vm.cube.stickers[index])
  vm.undoEdit();assertEquals(original.stickers[index],vm.cube.stickers[index]);assertTrue(vm.canRedoEdit)
  vm.redoEdit();assertEquals(replacement,vm.cube.stickers[index])
  vm.selectEditorFace(Face.R);assertEquals(Face.R.ordinal,vm.editorFace)
  vm.cancelEdit();assertEquals(Screen.REVIEW,vm.screen);assertEquals(original,vm.cube)
 }
 @Test fun lessonPracticeContextSurvivesStateRestoration() {
  val saved=SavedStateHandle();val vm=CubeViewModel(saved)
  vm.startLessonPractice("White cross","F R U")
  val restored=CubeViewModel(saved)
  assertEquals(Screen.VIRTUAL,restored.screen)
  assertEquals("White cross",restored.virtualLessonTitle)
  assertEquals("F R U",restored.virtualLessonScramble)
  restored.open(Screen.PRACTICE);restored.open(Screen.VIRTUAL)
  assertNull(restored.virtualLessonTitle)
 }
 @Test fun scanPickerPersistsAndDispatchesVerifiedThreeByThreeScanner() {
  val saved=SavedStateHandle();val vm=CubeViewModel(saved)
  vm.openScanPicker()
  assertEquals(Screen.SCAN_PICKER,vm.screen)
  assertEquals(Screen.SCAN_PICKER,CubeViewModel(saved).screen)
  vm.scanPuzzle(PuzzleId.THREE_BY_THREE)
  assertEquals(Screen.SCAN,vm.screen)
  vm.openScanPicker();vm.scanPuzzle(PuzzleId.TWO_BY_TWO)
  assertEquals(Screen.PUZZLE_SOLVE,vm.screen);assertEquals(PuzzleId.TWO_BY_TWO,vm.activePuzzle)
  vm.closePuzzleSolve();vm.scanPuzzle(PuzzleId.PYRAMINX)
  assertEquals(Screen.PUZZLE_SOLVE,vm.screen);assertEquals(PuzzleId.PYRAMINX,vm.activePuzzle)
  vm.closePuzzleSolve();vm.scanPuzzle(PuzzleId.FOUR_BY_FOUR)
  assertEquals(Screen.PUZZLE_SOLVE,vm.screen);assertEquals(PuzzleId.FOUR_BY_FOUR,vm.activePuzzle)
 }
 @Test fun scanBackReturnsToItsActualParentWithoutLosingReviewedCube() {
  val vm=CubeViewModel(SavedStateHandle())
  vm.openScanPicker();vm.scanPuzzle(PuzzleId.THREE_BY_THREE);vm.leaveScan()
  assertEquals(Screen.SCAN_PICKER,vm.screen)

  vm.scan()
  val target=CubeState.solved().apply(Move.parse("R U F2 L D"))
  repeat(6) {
   val face=vm.pose.face
   assertTrue(vm.importFace(Detection(target.stickers.drop(face.ordinal*9).take(9).map(anchors::getValue),emptyList(),"")))
  }
  assertEquals(Screen.REVIEW,vm.screen)
  vm.scan(Face.F);vm.leaveScan()
  assertEquals(Screen.REVIEW,vm.screen)
  assertEquals(target,vm.cube)
 }
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
