package com.cubeguide.ui

import androidx.compose.runtime.*
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cubeguide.core.*
import com.cubeguide.vision.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Screen { HOME, VIRTUAL, TIMER, LEARN, PUZZLES, SCAN, REVIEW, CORRECT, SETUP, GUIDE, DONE }
class CubeViewModel(private val saved: SavedStateHandle): ViewModel() {
 var manualEntry by mutableStateOf(saved.get<Boolean>("manualEntry") ?: false); private set
 private val editHistory=java.util.ArrayDeque<CubeState>()
 var canUndoEdit by mutableStateOf(false); private set
 var screen by mutableStateOf(Screen.valueOf(saved.get<String>("screen") ?: "HOME")); private set
 var cube by mutableStateOf(decode(saved["cube"]) ?: CubeState.solved()); private set
 var initial by mutableStateOf(decode(saved["initial"]) ?: cube); private set
 var moves by mutableStateOf(Move.parse(saved.get<String>("moves") ?: "")); private set
 var step by mutableIntStateOf(saved.get<Int>("step") ?: 0); private set
 var isReplay by mutableStateOf(saved.get<Boolean>("replay") ?: false); private set
 private var correctionOriginal: CubeState? = null
 var startingFace by mutableStateOf(Face.entries[saved.get<Int>("startingFace")?.coerceIn(0,5) ?: Face.U.ordinal]); private set
 var busy by mutableStateOf(false); private set
 var solveError by mutableStateOf<String?>(null); private set
 var message by mutableStateOf(""); private set
 var scanIndex by mutableIntStateOf(0); private set
 var progress by mutableFloatStateOf(0f); private set
 var cameraAspect by mutableFloatStateOf(0.75f); private set
 var corners by mutableStateOf(emptyList<Pair<Float,Float>>()); private set
 var lowConfidence by mutableStateOf(saved.get<IntArray>("lowConfidence")?.toSet() ?: emptySet()); private set
 private val captures=mutableMapOf<Face,List<Sample>>()
 private val stability=Stability()
 private var rescanFace: Face?=null
 private var beforeRescan: CubeState?=null
 private var solvingGeneration=0
 init { if(screen==Screen.CORRECT) { correctionOriginal=decode(saved["correctionOriginal"]) ?: cube }; if(screen==Screen.SCAN) screen=Screen.HOME; if(step !in 0..moves.size) { screen=Screen.HOME; step=0 } }
 val pose get()=scanSequence[scanIndex.coerceAtMost(5)]
 private fun decode(text: String?): CubeState? = runCatching { text?.let { CubeState(it.map { c -> CubeColor.entries[c.digitToInt()] }) } }.getOrNull()
 private fun save() { saved["lowConfidence"]=lowConfidence.toIntArray(); saved["manualEntry"]=manualEntry; saved["screen"]=screen.name; saved["cube"]=cube.stickers.joinToString("") { it.ordinal.toString() }; saved["initial"]=initial.stickers.joinToString("") { it.ordinal.toString() }; saved["moves"]=moves.joinToString(" ") { it.notation }; saved["step"]=step; saved["replay"]=isReplay; saved["startingFace"]=startingFace.ordinal }
 fun open(screen: Screen) { require(screen in listOf(Screen.HOME,Screen.VIRTUAL,Screen.TIMER,Screen.LEARN,Screen.PUZZLES));solvingGeneration++;busy=false;this.screen=screen;save() }
 fun dismissSolveError() { solveError=null }
 fun home() { solveError=null; solvingGeneration++; busy=false; screen=Screen.HOME; save() }
 fun scan(face: Face?=null) {
  manualEntry=false; editHistory.clear();canUndoEdit=false
  solveError=null; solvingGeneration++; busy=false
  rescanFace=if(face!=null && captures.size==6) face else null
  beforeRescan=if(rescanFace!=null) cube else null
  if(face==null) { captures.clear(); scanIndex=0 } else scanIndex=scanSequence.indexOfFirst { it.face==face }
  if(face!=null && captures.size!=6) message="Calibration is needed; scan all six faces in the guided order."
  stability.reset(); progress=0f; message=""; screen=Screen.SCAN; save()
 }
 fun manual() { manualEntry=true;editHistory.clear();canUndoEdit=false; solveError=null; captures.clear(); cube=CubeState.solved(); lowConfidence=emptySet(); message="Tap a sticker to change its color. Centers define the six faces."; screen=Screen.REVIEW; save() }
 fun detection(d: Detection) {
  if(screen!=Screen.SCAN) return
  corners=d.corners; cameraAspect=d.aspectRatio; message=d.message
  val expected=CubeColor.entries[pose.face.ordinal]
  if(d.samples.size==9 && ColorClassifier.nominal(d.samples[4])!=expected) { stability.reset(); progress=0f; message="Please show the ${expected.label.lowercase()} center face."; return }
  progress=stability.accept(d.samples,System.currentTimeMillis(),d.corners)
  if(progress<1f) return
  captureFace(d.copy(samples=stability.stableSamples))
 }
 fun importFace(d: Detection): Boolean {
  if(screen!=Screen.SCAN || d.samples.size!=9) return false
  if(ColorClassifier.nominal(d.samples[4])!=CubeColor.entries[pose.face.ordinal]) { message="Choose the ${CubeColor.entries[pose.face.ordinal].label.lowercase()} center face."; return false }
  captureFace(d); return true
 }
 private fun captureFace(d: Detection) {
  captures[pose.face]=d.samples; stability.reset(); progress=0f
  if(captures.size==6) {
   val anchors=Face.entries.associate { f -> CubeColor.entries[f.ordinal] to captures.getValue(f)[4] }
   val previousCube=beforeRescan;val changedFace=rescanFace
   val confidence=mutableSetOf<Int>()
   if(previousCube!=null && changedFace!=null) {
    val offset=changedFace.ordinal*9
    val outside=previousCube.stickers.filterIndexed { index,_ -> index/9!=changedFace.ordinal }
    val capacity=CubeColor.entries.associateWith { color -> 9-outside.count { it==color } }
    val decisions=ColorClassifier.balanced(captures.getValue(changedFace),anchors,mapOf(4 to CubeColor.entries[changedFace.ordinal]),capacity)
    cube=CubeState(previousCube.stickers.toMutableList().also { stickers -> decisions.forEachIndexed { i,decision -> stickers[offset+i]=decision.color;if(decision.confidence<0.18) confidence+=offset+i } })
    lowConfidence=lowConfidence.filter { it/9!=changedFace.ordinal }.toSet()+confidence
   } else {
    val samples=Face.entries.flatMap { captures.getValue(it) }
    val centers=Face.entries.associate { it.ordinal*9+4 to CubeColor.entries[it.ordinal] }
    val capacity=CubeColor.entries.associateWith { 9 }
    val decisions=ColorClassifier.balanced(samples,anchors,centers,capacity)
    decisions.forEachIndexed { index,decision -> if(decision.confidence<0.18) confidence+=index }
    cube=CubeState(decisions.map { it.color });lowConfidence=confidence
   }
   rescanFace=null; beforeRescan=null; screen=Screen.REVIEW
   message=Validator.validate(cube)?.message ?: if(lowConfidence.isEmpty()) "All six faces captured. Your cube is valid." else "Check the outlined stickers: their colors were uncertain."
   save()
  } else { scanIndex=(0..5).first { scanSequence[it].face !in captures }; save() }
 }
 fun edit(index: Int,color: CubeColor) { if(busy) return; if(cube.stickers[index]==color) { lowConfidence=lowConfidence-index;save();return }; editHistory.addLast(cube); if(editHistory.size>54) editHistory.removeFirst(); canUndoEdit=true; cube=CubeState(cube.stickers.toMutableList().also { it[index]=color }); lowConfidence=lowConfidence-index; message=Validator.validate(cube)?.message ?: "Your cube is valid and ready to solve."; save() }
 fun undoEdit() { if(busy || editHistory.isEmpty()) return; cube=editHistory.removeLast();canUndoEdit=editHistory.isNotEmpty();save() }
 val validationIssue: ValidationIssue? get() = Validator.validate(cube)

 fun rotateFace(face: Face) {
  if(busy) return
  cube=cube.rotateScanFace(face)
  message=Validator.validate(cube)?.message ?: "Your cube is valid and ready to solve."
  save()
 }

 fun solve(state: CubeState=cube) {
  if(busy) return
  solveError=null
  val correcting=screen==Screen.CORRECT
  val issue=Validator.validate(state)
  val completeColors=CubeColor.entries.all { color -> state.stickers.count { it==color }==9 } &&
   Face.entries.map { state.stickers[it.ordinal*9+4] }.toSet().size==6
  if(issue!=null && (!completeColors || correcting)) {
   message=issue.message
   solveError=issue.message
   lowConfidence=lowConfidence+issue.suspectStickers
   return
  }
  val needsSetup=screen==Screen.REVIEW
  val generation=++solvingGeneration
  busy=true
  message=if(issue==null) "Searching for a shorter verified solution..." else "Checking the orientation of your six scans…"
  viewModelScope.launch {
   val result=withContext(Dispatchers.Default) { runCatching {
    val aligned=if(issue==null) ScanOrientationResult.Unique(state,emptyList()) else ScanOrientationResolver.resolve(state)
    when(aligned) {
     ScanOrientationResult.Ambiguous -> error("More than one face orientation fits this scan. Check which side was above each face, then rotate or rescan it.")
     ScanOrientationResult.Impossible -> error((issue?.message ?: "The scan needs correction.")+" Check the sticker colors or rescan the highlighted faces.")
     is ScanOrientationResult.Unique -> aligned to Solver.solve(aligned.cube)
    }
   } }
   if(generation!=solvingGeneration) return@launch
   result.onSuccess { (aligned,solution) ->
    isReplay=false; initial=aligned.cube; cube=aligned.cube; moves=solution; step=0
    lowConfidence=emptySet()
    if(needsSetup) startingFace=Face.entries.first { aligned.cube.stickers[it.ordinal*9+4]==CubeColor.WHITE }
    screen=if(solution.isEmpty()) Screen.DONE else if(needsSetup) Screen.SETUP else Screen.GUIDE
    message=if(aligned.rotatedFaces.isEmpty()) "Found a verified ${solution.size}-step solution." else "Aligned the ${aligned.rotatedFaces.joinToString { it.label }} scan orientation and found a verified ${solution.size}-step solution. Match your cube to the model."
    correctionOriginal=null; saved.remove<String>("correctionOriginal")
    save()
   }.onFailure {
    message=it.message ?: "Solving failed. Try again."
    solveError=message
    lowConfidence=lowConfidence+(issue?.suspectStickers ?: emptyList())
   }
   busy=false
  }
 }

 fun beginCorrection() {
  if(screen!=Screen.GUIDE || busy || isReplay) return
  editHistory.clear();canUndoEdit=false; correctionOriginal=cube; solveError=null; message=""
  saved["correctionOriginal"]=cube.stickers.joinToString("") { it.ordinal.toString() }
  screen=Screen.CORRECT; save()
 }
 fun cancelCorrection() {
  if(screen!=Screen.CORRECT || busy) return
  correctionOriginal?.let { cube=it }
  correctionOriginal=null; saved.remove<String>("correctionOriginal")
  screen=Screen.GUIDE; solveError=null; message=""; save()
 }

 val holdingOrientation get() = HoldingOrientation.forFront(startingFace)
 val holdingPreview get() = holdingOrientation.apply(initial)
 fun chooseStartingFace(face: Face) { if(screen==Screen.SETUP) { startingFace=face; save() } }
 fun startGuide() {
  if(screen!=Screen.SETUP || busy) return
  val holding=holdingOrientation
  initial=holding.apply(initial); cube=initial; moves=moves.map { holding.apply(it) }
  check(initial.apply(moves).solved)
  screen=Screen.GUIDE; message=""; save()
 }

 fun next() { if(busy || step>=moves.size) return; cube=cube.apply(moves[step]); step++; if(step==moves.size) screen=Screen.DONE; save() }
 fun back() { if(step==0) return; step--; cube=cube.apply(moves[step].inverse()); screen=Screen.GUIDE; save() }
 fun restart() { cube=initial; step=0; screen=if(moves.isEmpty()) Screen.DONE else Screen.GUIDE; save() }
 fun recover(actual: Move) { solve(cube.apply(actual)) }
 fun recoverPrevious(actual: Move) {
  if(step==0) return
  solve(cube.apply(moves[step-1].inverse()).apply(actual))
 }
 fun viewSolution() { isReplay=true; cube=initial; step=0; screen=if(moves.isEmpty()) Screen.DONE else Screen.GUIDE; save() }
 fun demo() { manualEntry=false; cube=CubeState.solved().apply(Move.parse("R U R' U' F2 L D2 B R2 U")); lowConfidence=emptySet(); screen=Screen.REVIEW; message="Practice cube. Try the guide before scanning your own."; save() }
}
