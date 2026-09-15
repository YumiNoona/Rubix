package com.cubeguide.vision

import com.cubeguide.core.*

data class ClassifiedPyraminxScan(
 val facelets:PyraminxFacelets,
 val state:PyraminxState,
 val palette:Map<PyraminxColor,CubeColor>,
 val confidence:List<Double>,
)

sealed interface PyraminxClassificationResult {
 data class Ready(val scan:ClassifiedPyraminxScan):PyraminxClassificationResult
 data class Rejected(val reason:String):PyraminxClassificationResult
}

object PyraminxScanClassifier {
 private fun <T> permutations(values:List<T>):Sequence<List<T>> = sequence {
  if(values.isEmpty()) yield(emptyList()) else values.indices.forEach { index ->
   val rest=values.toMutableList();val selected=rest.removeAt(index)
   permutations(rest).forEach { yield(listOf(selected)+it) }
  }
 }
 fun classify(faces:List<List<Sample>>):PyraminxClassificationResult {
  if(faces.size!=4 || faces.any { it.size!=9 }) return PyraminxClassificationResult.Rejected("Capture all four complete faces.")
  val samples=faces.flatten();val nominal=samples.groupBy(ColorClassifier::nominal)
  val physical=nominal.entries.sortedByDescending { it.value.size }.take(4).map { it.key }.sortedBy { it.ordinal }
  if(physical.size!=4) return PyraminxClassificationResult.Rejected("Four distinct sticker colors were not visible.")
  val anchors=physical.associateWith { color -> Sample.median(nominal.getValue(color)) }
  val capacities=CubeColor.entries.associateWith { if(it in physical) 9 else 0 }
  val decisions=ColorClassifier.balanced(samples,anchors,emptyMap(),capacities)
  if(decisions.any { it.color !in physical }) return PyraminxClassificationResult.Rejected("The four sticker colors could not be separated reliably.")
  val standard=mapOf(PyraminxColor.GREEN to CubeColor.GREEN,PyraminxColor.RED to CubeColor.RED,PyraminxColor.BLUE to CubeColor.BLUE,PyraminxColor.YELLOW to CubeColor.YELLOW)
  val palettes=if(standard.values.toSet()==physical.toSet()) sequenceOf(standard)+permutations(physical).map { PyraminxColor.entries.zip(it).toMap() }.filter { it!=standard } else permutations(physical).map { PyraminxColor.entries.zip(it).toMap() }
  palettes.forEach { palette ->
   val reverse=palette.entries.associate { it.value to it.key };val facelets=PyraminxFacelets(decisions.map { reverse.getValue(it.color) })
   val state=facelets.toState()
   if(state!=null) return PyraminxClassificationResult.Ready(ClassifiedPyraminxScan(facelets,state,palette,decisions.map { it.confidence }))
  }
  return PyraminxClassificationResult.Rejected("The colors do not form a legal Pyraminx. Check face order and orientation.")
 }
}
