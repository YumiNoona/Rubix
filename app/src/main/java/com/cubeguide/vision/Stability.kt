package com.cubeguide.vision

import kotlin.math.*

class Stability {
 private val window=java.util.ArrayDeque<List<Sample>>()
 val stableSamples: List<Sample> get()=if(window.isEmpty()) emptyList() else (0..8).map { i -> Sample.median(window.map { it[i] }) }
 private var frames=0
 private var started=0L
 private var corners: List<Pair<Float,Float>> = emptyList()
 fun reset() { window.clear(); frames=0; started=0; corners=emptyList() }
 fun accept(samples: List<Sample>, now: Long, detectedCorners: List<Pair<Float,Float>> = emptyList()): Float {
  if(samples.size!=9) { reset(); return 0f }
  val old=window.peekFirst()
  val moved=corners.size==4 && detectedCorners.size==4 && corners.indices.any { i -> hypot(corners[i].first-detectedCorners[i].first,corners[i].second-detectedCorners[i].second)>0.025f }
  corners=detectedCorners
  if(old==null || moved || samples.indices.any { samples[it].distance(old[it])>5.5 }) { window.clear(); frames=1; started=now } else frames++
  window.addLast(samples); if(window.size>9) window.removeFirst()
  return min(frames/7f,(now-started)/850f).coerceIn(0f,1f)
 }
}
