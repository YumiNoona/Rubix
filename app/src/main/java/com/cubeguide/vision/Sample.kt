package com.cubeguide.vision

import kotlin.math.*

data class Sample(val l: Double,val a: Double,val b: Double,val hue: Double,val saturation: Double,val value: Double) {
 fun distance(other: Sample): Double {
  val dh=min(abs(hue-other.hue),180-abs(hue-other.hue))/90
  return sqrt((l-other.l).pow(2)*0.35+(a-other.a).pow(2)+(b-other.b).pow(2))+dh*24*min(saturation,other.saturation)/255
 }
 companion object {
  /** HSV hue wraps at 180; a linear median can turn red into green. */
  fun circularMedian(values: List<Double>): Double {
   require(values.isNotEmpty())
   return values.minBy { candidate -> values.sumOf { value -> val delta=abs(candidate-value); min(delta,180-delta) } }
  }
  fun median(samples: List<Sample>): Sample {
   require(samples.isNotEmpty())
   fun component(select: (Sample)->Double)=samples.map(select).sorted().let { it[it.size/2] }
   return Sample(component { it.l },component { it.a },component { it.b },circularMedian(samples.map { it.hue }),component { it.saturation },component { it.value })
  }
 }
}
data class Detection(val samples: List<Sample>,val corners: List<Pair<Float,Float>>,val message: String,val aspectRatio: Float=0.75f)
