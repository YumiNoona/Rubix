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
 var palette by mutableStateOf(defaultPyraminxPalette())
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

 fun beginScan() { captures.clear();colors.clear();confidence=emptyList();message="";stage=MultiPuzzleStage.SCAN }
 fun beginManual() {
  captures.clear();palette=defaultPyraminxPalette()
  val stickers=when(puzzle) {
   PuzzleId.TWO_BY_TWO -> PocketCube.solved().stickers
   PuzzleId.FOUR_BY_FOUR -> FourByFourState.solved().stickers
   PuzzleId.PYRAMINX -> PyraminxFacelets.solved().stickers.map { palette.getValue(it) }
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
   PuzzleId.PYRAMINX -> when(val result=PyraminxScanClassifier.classify(captures)) {
    is PyraminxClassificationResult.Ready -> {
     palette=result.scan.palette
     setReviewed(result.scan.facelets.stickers.map { palette.getValue(it) },result.scan.confidence)
    }
    is PyraminxClassificationResult.Rejected -> reject(result.reason)
   }
   else -> reject("This puzzle flow is unavailable.")
  }
  return stage==MultiPuzzleStage.REVIEW
 }
 private fun setReviewed(stickers:List<CubeColor>,scores:List<Double>) { colors.clear();colors.addAll(stickers);confidence=scores;message=validation() ?: "Scan verified. Check every face before solving.";stage=MultiPuzzleStage.REVIEW }
 private fun reject(reason:String) { captures.clear();message=reason;stage=MultiPuzzleStage.SCAN }

 fun beginEdit(face:Int=0) { editReturnStage=stage;editFace=face.coerceIn(0,faceCount-1);originalColors=colors.toList();editHistory.clear();redoHistory.clear();canUndo=false;canRedo=false;stage=MultiPuzzleStage.EDIT }
 fun cancelEdit() { originalColors?.let { colors.clear();colors.addAll(it) };originalColors=null;editHistory.clear();redoHistory.clear();canUndo=false;canRedo=false;stage=editReturnStage }
 fun finishEdit() { originalColors=null;editHistory.clear();redoHistory.clear();canUndo=false;canRedo=false;confidence=List(colors.size){1.0};message=validation() ?: "Colors verified. Ready to solve.";stage=MultiPuzzleStage.REVIEW }
 fun setColor(index:Int,color:CubeColor) { if(index in colors.indices && color in availableColors && colors[index]!=color) { editHistory.addLast(colors.toList());if(editHistory.size>96) editHistory.removeFirst();redoHistory.clear();canUndo=true;canRedo=false;colors[index]=color;confidence=confidence.toMutableList().also { if(index in it.indices) it[index]=1.0 };message=validation() ?: "Colors verified. Ready to solve." } }
 fun undoEdit() { if(editHistory.isEmpty()) return;redoHistory.addLast(colors.toList());val previous=editHistory.removeLast();colors.clear();colors.addAll(previous);canUndo=editHistory.isNotEmpty();canRedo=true;message=validation() ?: "Colors verified. Ready to solve." }
 fun redoEdit() { if(redoHistory.isEmpty()) return;editHistory.addLast(colors.toList());val next=redoHistory.removeLast();colors.clear();colors.addAll(next);canUndo=true;canRedo=redoHistory.isNotEmpty();message=validation() ?: "Colors verified. Ready to solve." }
 val availableColors get()=if(puzzle==PuzzleId.PYRAMINX) palette.values.distinct() else CubeColor.entries

 fun validation():String? = when(puzzle) {
  PuzzleId.TWO_BY_TWO -> runCatching { TwoByTwoEngine.validate(PocketCube(colors.toList())) }.getOrElse { "Capture all 24 stickers." }
  PuzzleId.FOUR_BY_FOUR -> runCatching { FourByFourEngine.validate(FourByFourState(colors.toList())) }.getOrElse { "Capture all 96 stickers." }
  PuzzleId.PYRAMINX -> run {
   val state=pyraminxFacelets()?.toState() ?: return@run "The four faces do not form a legal Pyraminx."
   PyraminxValidator.validate(state)
  }
  else -> "This puzzle is unavailable."
 }

 fun calculateSolution():VerifiedSolution = when(puzzle) {
   PuzzleId.TWO_BY_TWO -> runCatching { PuzzleSolverGate.solve(TwoByTwoEngine,PocketCube(colors.toList())) }.getOrElse { VerifiedSolution.Rejected(puzzle,it.message ?: "Could not solve this 2×2.") }
   PuzzleId.FOUR_BY_FOUR -> runCatching { PuzzleSolverGate.solve(FourByFourEngine,FourByFourState(colors.toList())) }.getOrElse { VerifiedSolution.Rejected(puzzle,it.message ?: "Could not solve this 4×4.") }
   PuzzleId.PYRAMINX -> pyraminxFacelets()?.toState()?.let { PuzzleSolverGate.solve(PyraminxEngine,it) } ?: VerifiedSolution.Rejected(puzzle,validation()!!)
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
   PuzzleId.PYRAMINX -> pyraminxFacelets()!!.apply(PyraminxMove.parse(notation)).stickers.map { palette.getValue(it) }
   else -> colors.toList()
  }
  colors.clear();colors.addAll(next)
 }
 fun inverse(notation:String)=when(puzzle) {
  PuzzleId.TWO_BY_TWO -> Move.parse(notation).single().inverse().notation
  PuzzleId.FOUR_BY_FOUR -> FourByFourMove.parse(notation).single().inverse().notation
  PuzzleId.PYRAMINX -> PyraminxMove.parse(notation).single().let { it.copy(turns=3-it.turns).notation }
  else -> notation
 }
 fun instruction(notation:String):String=when(puzzle) {
  PuzzleId.TWO_BY_TWO -> Move.parse(notation).single().instruction
  PuzzleId.FOUR_BY_FOUR -> FourByFourMove.parse(notation).single().let { "Turn the ${if(it.wide) "two-layer " else ""}${it.face.label.lowercase()} face ${turnWords(it.turns)}" }
  PuzzleId.PYRAMINX -> PyraminxMove.parse(notation).single().let { "Turn the ${it.axis.name} ${if(it.tipOnly) "tip" else "layer"} ${turnWords(it.turns)}" }
  else -> notation
 }
 private fun pyraminxFacelets():PyraminxFacelets? {
  if(colors.size!=36) return null
  val reverse=palette.entries.associate { it.value to it.key }
  return runCatching { PyraminxFacelets(colors.map { reverse.getValue(it) }) }.getOrNull()
 }
 fun pyraminxDisplayFacelets()=pyraminxFacelets()

 companion object {
  private fun defaultPyraminxPalette()=mapOf(PyraminxColor.GREEN to CubeColor.GREEN,PyraminxColor.RED to CubeColor.RED,PyraminxColor.BLUE to CubeColor.BLUE,PyraminxColor.YELLOW to CubeColor.YELLOW)
  private fun turnWords(turns:Int)=when(turns) { 1->"clockwise";2->"twice";else->"counter-clockwise" }
 }
}
