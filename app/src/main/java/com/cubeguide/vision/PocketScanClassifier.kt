package com.cubeguide.vision

import com.cubeguide.core.*

data class ClassifiedPocketScan(val state:PocketCube,val confidence:List<Double>)
sealed interface PocketClassificationResult {
 data class Ready(val scan:ClassifiedPocketScan):PocketClassificationResult
 data class Rejected(val reason:String):PocketClassificationResult
}

object PocketScanClassifier {
 fun classify(faces:List<List<Sample>>):PocketClassificationResult {
  if(faces.size!=6 || faces.any { it.size!=4 }) return PocketClassificationResult.Rejected("Capture all six complete 2×2 faces.")
  val samples=faces.flatten();val groups=samples.groupBy(ColorClassifier::nominal)
  if(CubeColor.entries.any { groups[it].isNullOrEmpty() }) return PocketClassificationResult.Rejected("All six sticker colors were not detected. Try brighter, even lighting.")
  val anchors=CubeColor.entries.associateWith { Sample.median(groups.getValue(it)) }
  val decisions=ColorClassifier.balanced(samples,anchors,emptyMap(),CubeColor.entries.associateWith { 4 })
  val arranged=decisions.map { it.color }.chunked(4)
  val resolution=PocketScanOrientationResolver.resolve(arranged)
  val state=when(resolution) {
   is PocketScanOrientationResult.Unique -> resolution.cube
   PocketScanOrientationResult.Ambiguous -> return PocketClassificationResult.Rejected("More than one face orientation fits. Keep the reference corner fixed and recapture each face in order.")
   PocketScanOrientationResult.Impossible -> return PocketClassificationResult.Rejected("The colors do not form a legal 2×2. Check face order and the outlined stickers.")
  }
  val rotations=(resolution as PocketScanOrientationResult.Unique).rotations
  val rawConfidence=decisions.map { it.confidence }.chunked(4)
  val alignedConfidence=rawConfidence.flatMapIndexed { face,values ->
   var rotated=values
   repeat(rotations[face]) { rotated=listOf(rotated[2],rotated[0],rotated[3],rotated[1]) }
   rotated
  }
  return PocketClassificationResult.Ready(ClassifiedPocketScan(state,alignedConfidence))
 }
}
