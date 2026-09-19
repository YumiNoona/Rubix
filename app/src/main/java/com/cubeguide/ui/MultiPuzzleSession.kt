package com.cubeguide.ui

import androidx.compose.runtime.*
import com.cubeguide.core.*
import com.cubeguide.vision.*

enum class MultiPuzzleStage { PREPARE, SCAN, REVIEW, EDIT, ANALYZING, SETUP, GUIDE, DONE }

class MultiPuzzleSession(val puzzle:PuzzleId) {
 val spec=PuzzleRegistry.get(puzzle)
 val plan=PuzzleCapturePlans.get(puzzle)
 val captures=mutableStateListOf<List<Sample>>()
 val colors=mutableStateListOf<CubeColor>()
 var stage by mutableStateOf(MultiPuzzleStage.PREPARE)
 var confidence by mutableStateOf<List<Double>>(emptyList())
 var message by mutableStateOf("")
 var editFace by mutableIntStateOf(0)
 var moves by mutableStateOf<List<String>>(emptyList())
 var step by mutableIntStateOf(0)
 var busy by mutableStateOf(false)
 var manualEntry by mutableStateOf(false);private set
 private var originalColors:List<CubeColor>?=null
 private var editReturnStage=MultiPuzzleStage.REVIEW
 private val editHistory=ArrayDeque<List<CubeColor>>()
 private val redoHistory=ArrayDeque<List<CubeColor>>()
 var canUndo by mutableStateOf(false);private set
 var canRedo by mutableStateOf(false);private set
 private var initialColors:List<CubeColor> = emptyList()

 val faceCount get()=spec.scanViews
 val faceSize get()=spec.stickersPerView
 val scanIndex get()=captures.size.coerceAtMost(faceCount-1)
 val lowConfidence get()=confidence.mapIndexedNotNull { index,value -> index.takeIf { value<.18 } }.toSet()
 val supportsAutomaticSolution get()=puzzle in setOf(PuzzleId.TWO_BY_TWO,PuzzleId.FOUR_BY_FOUR)

 fun beginScan() { manualEntry=false;captures.clear();colors.clear();confidence=emptyList();message="";stage=MultiPuzzleStage.SCAN }
 fun beginManual() {
  manualEntry=true
  captures.clear()
  val stickers=when(puzzle) {
   PuzzleId.TWO_BY_TWO -> PocketCube.solved().stickers
   PuzzleId.FOUR_BY_FOUR -> FourByFourState.solved().stickers
   PuzzleId.FIVE_BY_FIVE,PuzzleId.SIX_BY_SIX,PuzzleId.SEVEN_BY_SEVEN -> Face.entries.flatMap { face -> List(faceSize) { CubeColor.entries[face.ordinal] } }
   else -> emptyList()
  }
  colors.clear();colors.addAll(stickers);confidence=List(stickers.size){1.0};message="Enter every sticker exactly as it appears.";stage=MultiPuzzleStage.REVIEW;beginEdit()
 }
 fun addCapture(samples:List<Sample>):Boolean {
  if(stage!=MultiPuzzleStage.SCAN || samples.size!=faceSize) return false
  captures+=samples
  if(captures.size<faceCount) return true
  when(puzzle) {
   PuzzleId.TWO_BY_TWO -> when(val result=PocketScanClassifier.classify(captures)) {
    is PocketClassificationResult.Ready -> setReviewed(result.scan.state.stickers,result.scan.confidence)
    is PocketClassificationResult.Rejected -> reject(result.reason)
   }
   PuzzleId.FOUR_BY_FOUR -> when(val result=FourByFourScanClassifier.classify(captures)) {
    is FourByFourClassificationResult.Ready -> setReviewed(result.scan.state.stickers,result.scan.confidence)
    is FourByFourClassificationResult.Rejected -> reject(result.reason)
   }
   PuzzleId.FIVE_BY_FIVE,PuzzleId.SIX_BY_SIX,PuzzleId.SEVEN_BY_SEVEN -> when(val result=LargeCubeScanClassifier.classify(captures,spec.squareSize!!)) {
    is LargeCubeClassificationResult.Ready -> setReviewed(result.scan.stickers,result.scan.confidence)
    is LargeCubeClassificationResult.Rejected -> reject(result.reason)
   }
   else -> reject("This puzzle flow is unavailable.")
  }
  return stage==MultiPuzzleStage.REVIEW
 }
 private fun setReviewed(stickers:List<CubeColor>,scores:List<Double>) { colors.clear();colors.addAll(stickers);confidence=scores;message=validation() ?: if(supportsAutomaticSolution) "Scan verified. Check every face before solving." else "Capture complete. Check every sticker in review.";stage=MultiPuzzleStage.REVIEW }
 private fun reject(reason:String) { captures.clear();message=reason;stage=MultiPuzzleStage.SCAN }

 fun beginEdit(face:Int=0) { editReturnStage=stage;editFace=face.coerceIn(0,faceCount-1);originalColors=colors.toList();editHistory.clear();redoHistory.clear();canUndo=false;canRedo=false;stage=MultiPuzzleStage.EDIT }
 fun cancelEdit() { originalColors?.let { colors.clear();colors.addAll(it) };originalColors=null;editHistory.clear();redoHistory.clear();canUndo=false;canRedo=false;stage=editReturnStage }
 fun finishEdit() { originalColors=null;editHistory.clear();redoHistory.clear();canUndo=false;canRedo=false;confidence=List(colors.size){1.0};message=validMessage();stage=MultiPuzzleStage.REVIEW }
 fun setColor(index:Int,color:CubeColor) { if(index in colors.indices && color in availableColors && colors[index]!=color) { editHistory.addLast(colors.toList());if(editHistory.size>96) editHistory.removeFirst();redoHistory.clear();canUndo=true;canRedo=false;colors[index]=color;confidence=confidence.toMutableList().also { if(index in it.indices) it[index]=1.0 };message=validMessage() } }
 fun undoEdit() { if(editHistory.isEmpty()) return;redoHistory.addLast(colors.toList());val previous=editHistory.removeLast();colors.clear();colors.addAll(previous);canUndo=editHistory.isNotEmpty();canRedo=true;message=validMessage() }
 fun redoEdit() { if(redoHistory.isEmpty()) return;editHistory.addLast(colors.toList());val next=redoHistory.removeLast();colors.clear();colors.addAll(next);canUndo=true;canRedo=redoHistory.isNotEmpty();message=validMessage() }
 val availableColors get()=CubeColor.entries

 fun validation():String? = when(puzzle) {
  PuzzleId.TWO_BY_TWO -> runCatching { TwoByTwoEngine.validate(PocketCube(colors.toList())) }.getOrElse { "Capture all 24 stickers." }
  PuzzleId.FOUR_BY_FOUR -> runCatching { FourByFourEngine.validate(FourByFourState(colors.toList())) }.getOrElse { "Capture all 96 stickers." }
  PuzzleId.FIVE_BY_FIVE,PuzzleId.SIX_BY_SIX,PuzzleId.SEVEN_BY_SEVEN -> LargeCubeScanClassifier.validate(colors,spec.squareSize!!)
  else -> "This puzzle is unavailable."
 }
 private fun validMessage()=validation() ?: if(supportsAutomaticSolution) "Colors verified. Ready to solve." else "Colors balanced. Review complete."

 fun calculateSolution():VerifiedSolution = when(puzzle) {
   PuzzleId.TWO_BY_TWO -> runCatching { PuzzleSolverGate.solve(TwoByTwoEngine,PocketCube(colors.toList())) }.getOrElse { VerifiedSolution.Rejected(puzzle,it.message ?: "Could not solve this 2×2.") }
   PuzzleId.FOUR_BY_FOUR -> runCatching { PuzzleSolverGate.solve(FourByFourEngine,FourByFourState(colors.toList())) }.getOrElse { VerifiedSolution.Rejected(puzzle,it.message ?: "Could not solve this 4×4.") }
   else -> VerifiedSolution.Rejected(puzzle,"This puzzle is unavailable.")
 }
 fun acceptSolution(result:VerifiedSolution) {
  when(result) {
   is VerifiedSolution.Ready -> { moves=result.moves;initialColors=colors.toList();step=0;message="Verified ${moves.size}-move solution.";stage=if(moves.isEmpty()) MultiPuzzleStage.DONE else MultiPuzzleStage.SETUP }
   is VerifiedSolution.Rejected -> { message=result.reason;stage=MultiPuzzleStage.REVIEW }
  }
  busy=false
 }
 fun startGuide() { if(stage==MultiPuzzleStage.SETUP) stage=MultiPuzzleStage.GUIDE }

 fun next() { if(step>=moves.size) return;apply(moves[step]);step++;if(step==moves.size) stage=MultiPuzzleStage.DONE }
 fun previous() { if(step<=0) return;step--;apply(inverse(moves[step]));stage=MultiPuzzleStage.GUIDE }
 fun restart() { colors.clear();colors.addAll(initialColors);step=0;stage=if(moves.isEmpty()) MultiPuzzleStage.DONE else MultiPuzzleStage.GUIDE }
 fun replay()=restart()
 private fun apply(notation:String) {
  val next=when(puzzle) {
   PuzzleId.TWO_BY_TWO -> PocketCube(colors.toList()).apply(Move.parse(notation)).stickers
   PuzzleId.FOUR_BY_FOUR -> FourByFourState(colors.toList()).apply(FourByFourMove.parse(notation)).stickers
   else -> colors.toList()
  }
  colors.clear();colors.addAll(next)
 }
 fun inverse(notation:String)=when(puzzle) {
  PuzzleId.TWO_BY_TWO -> Move.parse(notation).single().inverse().notation
  PuzzleId.FOUR_BY_FOUR -> FourByFourMove.parse(notation).single().inverse().notation
  else -> notation
 }
 fun instruction(notation:String):String=when(puzzle) {
  PuzzleId.TWO_BY_TWO -> Move.parse(notation).single().instruction
  PuzzleId.FOUR_BY_FOUR -> FourByFourMove.parse(notation).single().let { "Turn the ${if(it.wide) "two-layer " else ""}${it.face.label.lowercase()} face ${turnWords(it.turns)}" }
  else -> notation
 }
 companion object {
  private fun turnWords(turns:Int)=when(turns) { 1->"clockwise";2->"twice";else->"counter-clockwise" }
 }
}
