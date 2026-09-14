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

enum class Screen { HOME, SCAN, REVIEW, CORRECT, SETUP, GUIDE, DONE }
class CubeViewModel(private val saved: SavedStateHandle): ViewModel() {
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
 var lowConfidence by mutableStateOf(emptySet<Int>()); private set
 private val captures=mutableMapOf<Face,List<Sample>>()
 private val stability=Stability()
 private var rescanFace: Face?=null
 private var beforeRescan: CubeState?=null
 private var solvingGeneration=0
 init { if(screen==Screen.CORRECT) { correctionOriginal=decode(saved["correctionOriginal"]) ?: cube }; if(screen==Screen.SCAN) screen=Screen.HOME; if(step !in 0..moves.size) { screen=Screen.HOME; step=0 } }
 val pose get()=scanSequence[scanIndex.coerceAtMost(5)]
 private fun decode(text: String?): CubeState? = runCatching { text?.let { CubeState(it.map { c -> CubeColor.entries[c.digitToInt()] }) } }.getOrNull()
 private fun save() { saved["screen"]=screen.name; saved["cube"]=cube.stickers.joinToString("") { it.ordinal.toString() }; saved["initial"]=initial.stickers.joinToString("") { it.ordinal.toString() }; saved["moves"]=moves.joinToString(" ") { it.notation }; saved["step"]=step; saved["replay"]=isReplay; saved["startingFace"]=startingFace.ordinal }
 fun dismissSolveError() { solveError=null }
 fun home() { solveError=null; solvingGeneration++; busy=false; screen=Screen.HOME; save() }
 fun scan(face: Face?=null) {
  solveError=null; solvingGeneration++; busy=false
  rescanFace=if(face!=null && captures.size==6) face else null
  beforeRescan=if(rescanFace!=null) cube else null
  if(face==null) { captures.clear(); scanIndex=0 } else scanIndex=scanSequence.indexOfFirst { it.face==face }
  if(face!=null && captures.size!=6) message="Calibration is needed; scan all six faces in the guided order."
  stability.reset(); progress=0f; message=""; screen=Screen.SCAN; save()
 }
 fun manual() { solveError=null; captures.clear(); cube=CubeState.solved(); lowConfidence=emptySet(); message="Tap a sticker to change its color. Centers define the six faces."; screen=Screen.REVIEW; save() }
 fun detection(d: Detection) {
  if(screen!=Screen.SCAN) return
  corners=d.corners; cameraAspect=d.aspectRatio; message=d.message
  val expected=CubeColor.entries[pose.face.ordinal]
  if(d.samples.size==9 && ColorClassifier.nominal(d.samples[4])!=expected) { stability.reset(); progress=0f; message="Please show the ${expected.label.lowercase()} center face."; return }
  progress=stability.accept(d.samples,System.currentTimeMillis(),d.corners)
  if(progress<1f) return
  captures[pose.face]=d.samples; stability.reset(); progress=0f
  if(captures.size==6) {
   val anchors=Face.entries.associate { f -> CubeColor.entries[f.ordinal] to captures.getValue(f)[4] }
   val confidence=mutableSetOf<Int>()
   val classified=Face.entries.flatMap { f -> captures.getValue(f).mapIndexed { i,s ->
    if(i==4) CubeColor.entries[f.ordinal] else ColorClassifier.classify(s,anchors).let { (c,p) -> if(p<0.18) confidence+=f.ordinal*9+i; c }
   } }
   val previousCube=beforeRescan; val changedFace=rescanFace
   cube=if(previousCube!=null && changedFace!=null) CubeState(previousCube.stickers.mapIndexed { i,c -> if(i/9==changedFace.ordinal) classified[i] else c }) else CubeState(classified)
   lowConfidence=if(changedFace!=null) lowConfidence.filter { it/9!=changedFace.ordinal }.toSet()+confidence.filter { it/9==changedFace.ordinal } else confidence
   rescanFace=null; beforeRescan=null; screen=Screen.REVIEW
   message=Validator.validate(cube)?.message ?: if(lowConfidence.isEmpty()) "All six faces captured. Your cube is valid." else "Check the outlined stickers: their colors were uncertain."
   save()
  } else { scanIndex=(0..5).first { scanSequence[it].face !in captures }; save() }
 }
 fun edit(index: Int,color: CubeColor) { if(busy) return; cube=CubeState(cube.stickers.toMutableList().also { it[index]=color }); lowConfidence=lowConfidence-index; message=Validator.validate(cube)?.message ?: "Your cube is valid and ready to solve."; save() }
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
  message=if(issue==null) "Calculating and verifying your solution…" else "Checking the orientation of your six scans…"
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
    message=if(aligned.rotatedFaces.isEmpty()) "" else "Aligned the ${aligned.rotatedFaces.joinToString { it.label }} scan orientation. Match your cube to the model before beginning."
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
  correctionOriginal=cube; solveError=null; message=""
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
 fun demo() { cube=CubeState.solved().apply(Move.parse("R U R' U' F2 L D2 B R2 U")); lowConfidence=emptySet(); screen=Screen.REVIEW; message="Practice cube. Try the guide before scanning your own."; save() }
}
