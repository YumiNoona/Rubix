package com.cubeguide.vision

import com.cubeguide.core.CubeColor

data class ClassifiedLargeCubeScan(val stickers:List<CubeColor>,val confidence:List<Double>)
sealed interface LargeCubeClassificationResult {
 data class Ready(val scan:ClassifiedLargeCubeScan):LargeCubeClassificationResult
 data class Rejected(val reason:String):LargeCubeClassificationResult
}

/** Balanced color classification for regular 5×5, 6×6 and 7×7 cubes. */
object LargeCubeScanClassifier {
 fun classify(faces:List<List<Sample>>,size:Int):LargeCubeClassificationResult {
  require(size in 5..7)
  val faceArea=size*size
  if(faces.size!=6 || faces.any { it.size!=faceArea }) return LargeCubeClassificationResult.Rejected("Capture all six complete ${size}×$size faces.")
  val samples=faces.flatten()
  val groups=samples.groupBy(ColorClassifier::nominal)
  if(CubeColor.entries.any { groups[it].isNullOrEmpty() }) return LargeCubeClassificationResult.Rejected("All six sticker colors were not detected.")
  val anchors=CubeColor.entries.associateWith { color -> Sample.median(groups.getValue(color)) }
  val fixed=if(size%2==1) CubeColor.entries.associate { color -> color.ordinal*faceArea+faceArea/2 to color } else emptyMap()
  val decisions=ColorClassifier.balanced(samples,anchors,fixed,CubeColor.entries.associateWith { faceArea })
  validate(decisions.map { it.color },size)?.let { return LargeCubeClassificationResult.Rejected(it) }
  return LargeCubeClassificationResult.Ready(ClassifiedLargeCubeScan(decisions.map { it.color },decisions.map { it.confidence }))
 }

 fun validate(stickers:List<CubeColor>,size:Int):String? {
  val faceArea=size*size
  if(stickers.size!=faceArea*6) return "Capture all ${faceArea*6} stickers."
  CubeColor.entries.forEach { color ->
   val count=stickers.count { it==color }
   if(count!=faceArea) return "${color.label} has $count stickers; a ${size}×$size needs exactly $faceArea."
  }
  if(size%2==1) {
   val centers=CubeColor.entries.map { stickers[it.ordinal*faceArea+faceArea/2] }
   if(centers.toSet().size!=6) return "Each center must have a different color."
  }
  return null
 }
}
