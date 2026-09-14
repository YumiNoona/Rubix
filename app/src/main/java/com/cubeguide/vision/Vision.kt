package com.cubeguide.vision

import android.graphics.Bitmap
import com.cubeguide.core.CubeColor
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.*

data class Sample(val l: Double,val a: Double,val b: Double,val hue: Double,val saturation: Double,val value: Double) {
 fun distance(other: Sample): Double {
  val dh=min(abs(hue-other.hue),180-abs(hue-other.hue))/90
  return sqrt((l-other.l).pow(2)*0.35+(a-other.a).pow(2)+(b-other.b).pow(2))+dh*12*min(saturation,other.saturation)/255
 }
}
data class Detection(val samples: List<Sample>,val corners: List<Pair<Float,Float>>,val message: String,val aspectRatio: Float=0.75f)
class FaceDetector {
 fun detect(bitmap: Bitmap): Detection {
  val rgba=Mat(); val rgb=Mat(); val gray=Mat(); val edges=Mat(); val hierarchy=Mat()
  val contours=mutableListOf<MatOfPoint>(); val warp=Mat(); val lab=Mat(); val hsv=Mat()
  var transform: Mat?=null
  try {
   Utils.bitmapToMat(bitmap,rgba); Imgproc.cvtColor(rgba,rgb,Imgproc.COLOR_RGBA2RGB)
   Imgproc.cvtColor(rgb,gray,Imgproc.COLOR_RGB2GRAY)
   if(Core.mean(gray).`val`[0]<35) return Detection(emptyList(),emptyList(),"Too dark. Move to a brighter area.")
   Imgproc.GaussianBlur(gray,gray,Size(5.0,5.0),0.0)
   Imgproc.Canny(gray,edges,35.0,100.0)
   val kernel=Imgproc.getStructuringElement(Imgproc.MORPH_RECT,Size(5.0,5.0))
   try { Imgproc.morphologyEx(edges,edges,Imgproc.MORPH_CLOSE,kernel) } finally { kernel.release() }
   Imgproc.findContours(edges,contours,hierarchy,Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE)
   var best: Array<Point>?=null; var bestArea=0.0
   for(c in contours) {
    val area=abs(Imgproc.contourArea(c)); if(area<bitmap.width*bitmap.height*0.045 || area>bitmap.width*bitmap.height*0.88) continue
    val curve=MatOfPoint2f(*c.toArray()); val approx=MatOfPoint2f()
    try {
     Imgproc.approxPolyDP(curve,approx,Imgproc.arcLength(curve,true)*0.025,true)
     val pts=approx.toArray(); if(pts.size!=4) continue
     val poly=MatOfPoint(*pts); val convex=try { Imgproc.isContourConvex(poly) } finally { poly.release() }
     if(!convex) continue
     val lengths=(0..3).map { i -> hypot(pts[i].x-pts[(i+1)%4].x,pts[i].y-pts[(i+1)%4].y) }
     if(lengths.max()/lengths.min()>2.1) continue
     if(area>bestArea) { bestArea=area; best=pts }
    } finally { curve.release(); approx.release() }
   }
   val points=best ?: return Detection(emptyList(),emptyList(),"Show one complete face. Move closer and keep all nine stickers visible.")
   val ordered=arrayOf(points.minBy { it.x+it.y },points.maxBy { it.x-it.y },points.maxBy { it.x+it.y },points.minBy { it.x-it.y })
   if(ordered.toSet().size!=4) return Detection(emptyList(),emptyList(),"Tilt the face toward the camera.")
   val source=MatOfPoint2f(*ordered); val target=MatOfPoint2f(Point(0.0,0.0),Point(299.0,0.0),Point(299.0,299.0),Point(0.0,299.0))
   transform=try { Imgproc.getPerspectiveTransform(source,target) } finally { source.release(); target.release() }
   Imgproc.warpPerspective(rgb,warp,transform,Size(300.0,300.0))
   Imgproc.cvtColor(warp,lab,Imgproc.COLOR_RGB2Lab); Imgproc.cvtColor(warp,hsv,Imgproc.COLOR_RGB2HSV)
   val samples=(0..8).map { i ->
    val l=mutableListOf<Double>(); val a=mutableListOf<Double>(); val b=mutableListOf<Double>(); val h=mutableListOf<Double>(); val s=mutableListOf<Double>(); val v=mutableListOf<Double>()
    for(y in (i/3*100+28)..(i/3*100+72) step 4) for(x in (i%3*100+28)..(i%3*100+72) step 4) {
     val hp=hsv.get(y,x); val lp=lab.get(y,x)
     if(hp[2]<35 || (hp[1]<15 && hp[2]>250)) continue
     l+=lp[0]*100/255; a+=lp[1]-128; b+=lp[2]-128; h+=hp[0]; s+=hp[1]; v+=hp[2]
    }
    if(l.size<25) return Detection(emptyList(),ordered.map { (it.x/bitmap.width).toFloat() to (it.y/bitmap.height).toFloat() },"Glare or shadow hides a sticker. Tilt the cube slightly.")
    fun median(values: List<Double>)=values.sorted()[values.size/2]
    Sample(median(l),median(a),median(b),median(h),median(s),median(v))
   }
   // A uniform square surface is not evidence of a sticker grid. Require internal seams.
   var gap=0.0; var inside=0.0; var count=0
   for(pos in 30..270 step 8) for(boundary in listOf(100,200)) {
    gap+=grayValue(warp,boundary,pos)+grayValue(warp,pos,boundary)
    inside+=grayValue(warp,boundary-25,pos)+grayValue(warp,pos,boundary-25); count+=2
   }
   if(inside/count-gap/count<4 && samples.all { it.distance(samples[4])<6 }) return Detection(emptyList(),emptyList(),"Couldn't locate the sticker grid. Show the entire cube face.")
   return Detection(samples,ordered.map { (it.x/bitmap.width).toFloat() to (it.y/bitmap.height).toFloat() },"Hold steady to capture automatically")
  } finally { listOf(rgba,rgb,gray,edges,hierarchy,warp,lab,hsv).forEach { it.release() }; contours.forEach { it.release() }; transform?.release() }
 }
 private fun grayValue(mat: Mat,x: Int,y: Int): Double = mat.get(y,x).average()
}
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
  return distances.first().first to margin
 }
}
class Stability {
 private var previous: List<Sample>?=null
 private var frames=0
 private var started=0L
 private var corners: List<Pair<Float,Float>> = emptyList()
 fun reset() { previous=null; frames=0; started=0; corners=emptyList() }
 fun accept(samples: List<Sample>, now: Long, detectedCorners: List<Pair<Float,Float>> = emptyList()): Float {
  if(samples.size!=9) { reset(); return 0f }
  val old=previous
  val moved=corners.size==4 && detectedCorners.size==4 && corners.indices.any { i -> hypot(corners[i].first-detectedCorners[i].first,corners[i].second-detectedCorners[i].second)>0.025f }
  corners=detectedCorners
  if(old==null || moved || samples.indices.any { samples[it].distance(old[it])>5.5 }) { frames=1; started=now } else frames++
  previous=samples
  return min(frames/7f,(now-started)/850f).coerceIn(0f,1f)
 }
}
