package com.cubeguide.vision

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.*

class FaceDetector(private val gridSize:Int=3) {
 init { require(gridSize in 2..7) }
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
   val points=best ?: return Detection(emptyList(),emptyList(),"Show one complete face. Move closer and keep all stickers visible.")
   val ordered=arrayOf(points.minBy { it.x+it.y },points.maxBy { it.x-it.y },points.maxBy { it.x+it.y },points.minBy { it.x-it.y })
   if(ordered.toSet().size!=4) return Detection(emptyList(),emptyList(),"Tilt the face toward the camera.")
   val source=MatOfPoint2f(*ordered); val target=MatOfPoint2f(Point(0.0,0.0),Point(299.0,0.0),Point(299.0,299.0),Point(0.0,299.0))
   transform=try { Imgproc.getPerspectiveTransform(source,target) } finally { source.release(); target.release() }
   Imgproc.warpPerspective(rgb,warp,transform,Size(300.0,300.0))
   Imgproc.cvtColor(warp,lab,Imgproc.COLOR_RGB2Lab); Imgproc.cvtColor(warp,hsv,Imgproc.COLOR_RGB2HSV)
   val cell=300/gridSize
   val samples=(0 until gridSize*gridSize).map { i ->
    val pixels=mutableListOf<DoubleArray>()
    val inset=(cell*0.28).roundToInt();val far=(cell*0.72).roundToInt()
    for(y in (i/gridSize*cell+inset)..(i/gridSize*cell+far) step max(2,cell/25)) for(x in (i%gridSize*cell+inset)..(i%gridSize*cell+far) step max(2,cell/25)) {
     val centerIndex=(gridSize*gridSize)/2
     if(gridSize%2==1 && i==centerIndex && x%cell in (cell*0.4).toInt()..(cell*0.6).toInt() && y%cell in (cell*0.4).toInt()..(cell*0.6).toInt()) continue
     val hp=hsv.get(y,x);val lp=lab.get(y,x)
     if(hp[2]>=35) pixels+=doubleArrayOf(lp[0]*100/255,lp[1]-128,lp[2]-128,hp[0],hp[1],hp[2])
    }
    val saturation90=pixels.map { it[4] }.sorted().let { values -> if(values.isEmpty()) 0.0 else values[(values.lastIndex*0.9).toInt()] }
    // Colored stickers keep enough saturated pixels to discard white specular highlights.
    val useful=if(saturation90>=70) pixels.filter { it[4]>=max(35.0,saturation90*0.35) } else pixels
    if(useful.size<25) return Detection(emptyList(),ordered.map { (it.x/bitmap.width).toFloat() to (it.y/bitmap.height).toFloat() },"Glare or shadow hides a sticker. Tilt the cube slightly.")
    fun median(component: Int)=useful.map { it[component] }.sorted().let { it[it.size/2] }
    Sample(median(0),median(1),median(2),Sample.circularMedian(useful.map { it[3] }),median(4),median(5))
   }
   // A uniform square surface is not evidence of a sticker grid. Require internal seams.
   var gap=0.0; var inside=0.0; var count=0
   val boundaries=(1 until gridSize).map { it*cell }
   for(pos in (cell/3)..(300-cell/3) step max(4,cell/12)) for(boundary in boundaries) {
    gap+=grayValue(warp,boundary,pos)+grayValue(warp,pos,boundary)
    inside+=grayValue(warp,boundary-cell/4,pos)+grayValue(warp,pos,boundary-cell/4); count+=2
   }
   if(count>0 && inside/count-gap/count<4 && samples.all { it.distance(samples.first())<6 }) return Detection(emptyList(),emptyList(),"Couldn't locate the sticker grid. Show the entire cube face.")
   return Detection(samples,ordered.map { (it.x/bitmap.width).toFloat() to (it.y/bitmap.height).toFloat() },"Hold steady to capture automatically")
  } finally { listOf(rgba,rgb,gray,edges,hierarchy,warp,lab,hsv).forEach { it.release() }; contours.forEach { it.release() }; transform?.release() }
 }
 private fun grayValue(mat: Mat,x: Int,y: Int): Double = mat.get(y,x).average()
}
