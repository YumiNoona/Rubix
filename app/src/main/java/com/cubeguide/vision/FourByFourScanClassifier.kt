package com.cubeguide.vision

import com.cubeguide.core.*

data class ClassifiedFourByFourScan(val state:FourByFourState,val confidence:List<Double>)
sealed interface FourByFourClassificationResult {
 data class Ready(val scan:ClassifiedFourByFourScan):FourByFourClassificationResult
 data class Rejected(val reason:String):FourByFourClassificationResult
}

object FourByFourScanClassifier {
 fun classify(faces:List<List<Sample>>):FourByFourClassificationResult {
  if(faces.size!=6 || faces.any { it.size!=16 }) return FourByFourClassificationResult.Rejected("Capture all six complete 4×4 faces.")
  val samples=faces.flatten();val groups=samples.groupBy(ColorClassifier::nominal)
  if(CubeColor.entries.any { groups[it].isNullOrEmpty() }) return FourByFourClassificationResult.Rejected("All six sticker colors were not detected.")
  val anchors=CubeColor.entries.associateWith { color -> Sample.median(groups.getValue(color)) }
  val decisions=ColorClassifier.balanced(samples,anchors,emptyMap(),CubeColor.entries.associateWith { 16 })
  val state=FourByFourState(decisions.map { it.color })
  FourByFourValidator.validate(state)?.let { return FourByFourClassificationResult.Rejected("$it Keep each face in the guided orientation, then check the outlined stickers.") }
  return FourByFourClassificationResult.Ready(ClassifiedFourByFourScan(state,decisions.map { it.confidence }))
 }
}
