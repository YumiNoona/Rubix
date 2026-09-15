package com.cubeguide.core

sealed interface PyraminxScanResult {
 data class Unique(val facelets:PyraminxFacelets,val state:PyraminxState):PyraminxScanResult
 data class Ambiguous(val candidates:Int):PyraminxScanResult
 data object Impossible:PyraminxScanResult
}

object PyraminxScanResolver {
 private fun rotate(face:List<PyraminxColor>,turns:Int):List<PyraminxColor> {
  var value=face
  repeat(turns) {
   val next=MutableList(9) { PyraminxColor.GREEN }
   repeat(9) { index -> next[(index+3)%9]=value[index] }
   value=next
  }
  return value
 }
 private fun <T> permutations(values:List<T>):Sequence<List<T>> = sequence {
  if(values.isEmpty()) yield(emptyList()) else values.indices.forEach { index ->
   val selected=values[index];val rest=values.toMutableList().also { it.removeAt(index) }
   permutations(rest).forEach { yield(listOf(selected)+it) }
  }
 }
 fun resolve(photos:List<List<PyraminxColor>>):PyraminxScanResult {
  if(photos.size!=4 || photos.any { it.size!=9 } || PyraminxColor.entries.any { color -> photos.flatten().count { it==color }!=9 }) return PyraminxScanResult.Impossible
  val states=linkedMapOf<PyraminxState,PyraminxFacelets>()
  for(order in permutations(photos)) for(code in 0 until 81) {
   var value=code
   val arranged=order.flatMap { face -> val turns=value%3;value/=3;rotate(face,turns) }
   val facelets=PyraminxFacelets(arranged);val state=facelets.toState() ?: continue
   states.putIfAbsent(state,facelets)
   if(states.size>24) return PyraminxScanResult.Ambiguous(states.size)
  }
  return when(states.size) { 0->PyraminxScanResult.Impossible;1->states.entries.first().let { PyraminxScanResult.Unique(it.value,it.key) };else->PyraminxScanResult.Ambiguous(states.size) }
 }
 fun resolveGuided(faces:List<List<PyraminxColor>>):PyraminxScanResult {
  if(faces.size!=4 || faces.any { it.size!=9 }) return PyraminxScanResult.Impossible
  val facelets=PyraminxFacelets(faces.flatten());val state=facelets.toState() ?: return PyraminxScanResult.Impossible
  return PyraminxScanResult.Unique(facelets,state)
 }
}
