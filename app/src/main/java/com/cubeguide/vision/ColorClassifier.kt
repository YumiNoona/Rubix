package com.cubeguide.vision

import com.cubeguide.core.CubeColor
import kotlin.math.*

object ColorClassifier {
 fun nominal(s: Sample): CubeColor = when {
  s.saturation<65 && s.l>45 -> CubeColor.WHITE
  s.hue<8 || s.hue>170 -> CubeColor.RED
  s.hue<22 -> CubeColor.ORANGE
  s.hue<39 -> CubeColor.YELLOW
  s.hue<92 -> CubeColor.GREEN
  else -> CubeColor.BLUE
 }
 fun classify(sample: Sample, anchors: Map<CubeColor,Sample>): Pair<CubeColor,Double> {
  val distances=anchors.map { (c,s) -> c to sample.distance(s) }.sortedBy { it.second }
  val margin=(distances[1].second-distances[0].second)/max(distances[1].second,1.0)
  return distances.first().first to (margin*exp(-distances.first().second/35.0)).coerceIn(0.0,1.0)
 }
}
