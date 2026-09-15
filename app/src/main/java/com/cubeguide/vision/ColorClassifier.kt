package com.cubeguide.vision

import com.cubeguide.core.CubeColor
import kotlin.math.*

data class ColorDecision(val color: CubeColor,val confidence: Double)

object ColorClassifier {
 fun nominal(s: Sample): CubeColor = when {
  s.l>48 && s.saturation<100 && hypot(s.a,s.b)<25 -> CubeColor.WHITE
  s.hue<8 || s.hue>170 -> CubeColor.RED
  s.hue<22 -> CubeColor.ORANGE
  s.hue<39 -> CubeColor.YELLOW
  s.hue<92 -> CubeColor.GREEN
  else -> CubeColor.BLUE
 }
 fun classify(sample: Sample,anchors: Map<CubeColor,Sample>): Pair<CubeColor,Double> {
  val distances=anchors.map { (color,anchor) -> color to sample.distance(anchor) }.sortedBy { it.second }
  return distances.first().first to confidence(distances[0].second,distances[1].second)
 }

 /** Finds the lowest-cost color assignment while honoring a real cube's color capacities. */
 fun balanced(
  samples: List<Sample>,anchors: Map<CubeColor,Sample>,fixed: Map<Int,CubeColor>,capacities: Map<CubeColor,Int>
 ): List<ColorDecision> {
  require(fixed.keys.all { it in samples.indices })
  val remaining=CubeColor.entries.associateWith { color ->
   (capacities[color] ?: 0)-fixed.values.count { it==color }
  }
  val variable=samples.indices.filter { it !in fixed }
  if(remaining.values.any { it<0 } || remaining.values.sum()!=variable.size) return samples.mapIndexed { index,sample ->
   fixed[index]?.let { ColorDecision(it,1.0) } ?: classify(sample,anchors).let { ColorDecision(it.first,it.second) }
  }
  val slots=CubeColor.entries.flatMap { color -> List(remaining.getValue(color)) { color } }
  val costs=variable.map { index -> slots.map { samples[index].distance(anchors.getValue(it)) } }
  val slotForRow=hungarian(costs)
  val result=MutableList<ColorDecision?>(samples.size) { null }
  fixed.forEach { (index,color) -> result[index]=ColorDecision(color,1.0) }
  variable.forEachIndexed { row,index ->
   val assigned=slots[slotForRow[row]]
   val assignedDistance=samples[index].distance(anchors.getValue(assigned))
   val alternative=CubeColor.entries.filter { it!=assigned }.minOf { samples[index].distance(anchors.getValue(it)) }
   result[index]=ColorDecision(assigned,confidence(assignedDistance,alternative))
  }
  return result.map { checkNotNull(it) }
 }

 private fun confidence(best: Double,alternative: Double): Double =
  (((alternative-best)/max(alternative,1.0))*exp(-best/35.0)).coerceIn(0.0,1.0)

 /** Square minimum-cost matching. Rows are samples; columns are repeated color slots. */
 private fun hungarian(cost: List<List<Double>>): IntArray {
  val n=cost.size
  if(n==0) return IntArray(0)
  require(cost.all { it.size==n })
  val u=DoubleArray(n+1);val v=DoubleArray(n+1);val p=IntArray(n+1);val way=IntArray(n+1)
  for(i in 1..n) {
   p[0]=i;var j0=0;val min=DoubleArray(n+1) { Double.POSITIVE_INFINITY };val used=BooleanArray(n+1)
   do {
    used[j0]=true;val i0=p[j0];var delta=Double.POSITIVE_INFINITY;var j1=0
    for(j in 1..n) if(!used[j]) {
     val current=cost[i0-1][j-1]-u[i0]-v[j]
     if(current<min[j]) { min[j]=current;way[j]=j0 }
     if(min[j]<delta) { delta=min[j];j1=j }
    }
    for(j in 0..n) if(used[j]) { u[p[j]]+=delta;v[j]-=delta } else min[j]-=delta
    j0=j1
   } while(p[j0]!=0)
   do { val j1=way[j0];p[j0]=p[j1];j0=j1 } while(j0!=0)
  }
  return IntArray(n).also { answer -> for(j in 1..n) answer[p[j]-1]=j-1 }
 }
}
