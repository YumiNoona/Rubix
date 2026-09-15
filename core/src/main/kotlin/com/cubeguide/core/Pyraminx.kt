package com.cubeguide.core

enum class PyraminxAxis { U,L,R,B }

data class PyraminxMove(val axis:PyraminxAxis,val turns:Int=1,val tipOnly:Boolean=false) {
 init { require(turns in 1..2) }
 val notation get()=(if(tipOnly) axis.name.lowercase() else axis.name)+(if(turns==2) "'" else "")
 fun inverse()=copy(turns=3-turns)
 companion object {
  fun parse(text:String)=text.trim().split(Regex("\\s+")).filter(String::isNotBlank).map { token ->
   val letter=token.first();val axis=PyraminxAxis.valueOf(letter.uppercaseChar().toString())
   require(token.length<=2 && (token.length==1 || token[1]=='\'')) { "Invalid Pyraminx move: $token" }
   PyraminxMove(axis,if(token.endsWith("'")) 2 else 1,letter.isLowerCase())
  }
 }
}

data class PyraminxState(
 val edgePermutation:List<Int>,
 val edgeOrientation:List<Int>,
 val axialOrientation:List<Int>,
 val tipOrientation:List<Int>,
) {
 init { require(edgePermutation.size==6 && edgeOrientation.size==6 && axialOrientation.size==4 && tipOrientation.size==4) }
 fun apply(move:PyraminxMove):PyraminxState {
  var state=this
  repeat(move.turns) { state=state.quarter(move.axis,move.tipOnly) }
  return state
 }
 fun apply(moves:List<PyraminxMove>)=moves.fold(this) { state,move -> state.apply(move) }
 private fun quarter(axis:PyraminxAxis,tipOnly:Boolean):PyraminxState {
  val index=axis.ordinal
  val tips=tipOrientation.toMutableList().also { it[index]=(it[index]+1)%3 }
  if(tipOnly) return copy(tipOrientation=tips)
  val axial=axialOrientation.toMutableList().also { it[index]=(it[index]+1)%3 }
  val permutation=edgePermutation.toMutableList();val orientation=edgeOrientation.toMutableList()
  val cycle=when(axis) { PyraminxAxis.U->intArrayOf(5,3,1);PyraminxAxis.L->intArrayOf(2,1,0);PyraminxAxis.R->intArrayOf(0,3,4);PyraminxAxis.B->intArrayOf(2,4,5) }
  val oldPermutation=permutation.toList();val oldOrientation=orientation.toList()
  permutation[cycle[0]]=oldPermutation[cycle[2]]
  permutation[cycle[1]]=oldPermutation[cycle[0]]
  permutation[cycle[2]]=oldPermutation[cycle[1]]
  orientation[cycle[0]]=oldOrientation[cycle[2]]
  orientation[cycle[1]]=oldOrientation[cycle[0]] xor 1
  orientation[cycle[2]]=oldOrientation[cycle[1]] xor 1
  return PyraminxState(permutation,orientation,axial,tips)
 }
 val solved get()=edgePermutation==List(6){it} && edgeOrientation.all { it==0 } && axialOrientation.all { it==0 } && tipOrientation.all { it==0 }
 companion object { fun solved()=PyraminxState(List(6){it},List(6){0},List(4){0},List(4){0}) }
}

object PyraminxValidator {
 fun validate(state:PyraminxState):String? {
  if(state.edgePermutation.sorted()!=List(6){it}) return "Pyraminx edges contain a missing or duplicate piece."
  if(state.edgeOrientation.any { it !in 0..1 } || state.edgeOrientation.fold(0,Int::xor)!=0) return "Pyraminx edge orientation is impossible."
  if(state.axialOrientation.any { it !in 0..2 } || state.tipOrientation.any { it !in 0..2 }) return "Pyraminx tip orientation is invalid."
  var inversions=0
  for(i in 0 until 6) for(j in i+1 until 6) if(state.edgePermutation[i]>state.edgePermutation[j]) inversions++
  if(inversions%2!=0) return "Pyraminx edge permutation has impossible parity."
  return null
 }
}

object PyraminxEngine:PuzzleEngine<PyraminxState> {
 override val puzzle=PuzzleId.PYRAMINX
 private val mainMoves=PyraminxAxis.entries.flatMap { listOf(PyraminxMove(it,1),PyraminxMove(it,2)) }
 private fun permutationKey(state:PyraminxState)=state.edgePermutation.joinToString("")
 private fun orientationKey(state:PyraminxState)=state.edgeOrientation.joinToString("")+state.axialOrientation.joinToString("")
 private fun distances(key:(PyraminxState)->String):Map<String,Int> {
  val root=PyraminxState.solved();val result=mutableMapOf(key(root) to 0);val queue=ArrayDeque<PyraminxState>();queue+=root
  while(queue.isNotEmpty()) {
   val state=queue.removeFirst();val depth=result.getValue(key(state))
   mainMoves.forEach { move -> val next=state.apply(move);val nextKey=key(next);if(nextKey !in result) { result[nextKey]=depth+1;queue+=next } }
  }
  return result
 }
 private val permutationDistance by lazy { distances(::permutationKey) }
 private val orientationDistance by lazy { distances(::orientationKey) }
 override fun validate(state:PyraminxState)=PyraminxValidator.validate(state)
 override fun solve(state:PyraminxState):List<String> {
  require(validate(state)==null)
  val path=mutableListOf<PyraminxMove>()
  fun mainSolved(value:PyraminxState)=value.edgePermutation==List(6){it} && value.edgeOrientation.all { it==0 } && value.axialOrientation.all { it==0 }
  fun search(value:PyraminxState,remaining:Int,lastAxis:PyraminxAxis?):Boolean {
   if(remaining==0) return mainSolved(value)
   val heuristic=maxOf(permutationDistance.getValue(permutationKey(value)),orientationDistance.getValue(orientationKey(value)))
   if(heuristic>remaining) return false
   for(move in mainMoves) if(move.axis!=lastAxis) {
    path+=move
    if(search(value.apply(move),remaining-1,move.axis)) return true
    path.removeLast()
   }
   return false
  }
  for(depth in 0..12) if(search(state,depth,null)) break
  check(mainSolved(state.apply(path))) { "Pyraminx search exceeded its verified depth." }
  val afterMain=state.apply(path)
  PyraminxAxis.entries.forEachIndexed { index,axis ->
   val turns=(3-afterMain.tipOrientation[index])%3
   if(turns>0) path+=PyraminxMove(axis,turns,tipOnly=true)
  }
  return path.map { it.notation }
 }
 override fun apply(state:PyraminxState,move:String)=state.apply(PyraminxMove.parse(move).single())
 override fun isSolved(state:PyraminxState)=state.solved
}
