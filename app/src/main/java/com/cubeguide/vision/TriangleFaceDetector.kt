package com.cubeguide.vision

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.*

class TriangleFaceDetector {
 fun detect(bitmap:Bitmap):Detection {
  val rgba=Mat();val rgb=Mat();val gray=Mat();val edges=Mat();val hierarchy=Mat();val warp=Mat();val lab=Mat();val hsv=Mat()
  val contours=mutableListOf<MatOfPoint>()
  try {
   Utils.bitmapToMat(bitmap,rgba);Imgproc.cvtColor(rgba,rgb,Imgproc.COLOR_RGBA2RGB);Imgproc.cvtColor(rgb,gray,Imgproc.COLOR_RGB2GRAY)
   if(Core.mean(gray).`val`[0]<35) return Detection(emptyList(),emptyList(),"Too dark. Move to a brighter area.")
   Imgproc.GaussianBlur(gray,gray,Size(5.0,5.0),0.0);Imgproc.Canny(gray,edges,35.0,105.0)
   Imgproc.findContours(edges,contours,hierarchy,Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE)
   var best:Array<Point>?=null;var bestArea=0.0
   contours.forEach { contour ->
    val area=abs(Imgproc.contourArea(contour));if(area<bitmap.width*bitmap.height*.06 || area>bitmap.width*bitmap.height*.9) return@forEach
    val curve=MatOfPoint2f(*contour.toArray());val approx=MatOfPoint2f()
    try {
     Imgproc.approxPolyDP(curve,approx,Imgproc.arcLength(curve,true)*.035,true);val points=approx.toArray()
     if(points.size==3 && area>bestArea) { best=points;bestArea=area }
    } finally { curve.release();approx.release() }
   }
   val points=best ?: return Detection(emptyList(),emptyList(),"Show one complete triangular face with one vertex pointing up.")
   val top=points.minBy { it.y };val bottom=points.filter { it!==top }.sortedBy { it.x }
   if(bottom.size!=2 || top.y>min(bottom[0].y,bottom[1].y)-bitmap.height*.12) return Detection(emptyList(),emptyList(),"Point one vertex upward and keep the full triangle visible.")
   val source=MatOfPoint2f(top,bottom[0],bottom[1]);val target=MatOfPoint2f(Point(150.0,8.0),Point(8.0,292.0),Point(292.0,292.0))
   val transform=Imgproc.getAffineTransform(source,target);source.release();target.release()
   try { Imgproc.warpAffine(rgb,warp,transform,Size(300.0,300.0)) } finally { transform.release() }
   Imgproc.cvtColor(warp,lab,Imgproc.COLOR_RGB2Lab);Imgproc.cvtColor(warp,hsv,Imgproc.COLOR_RGB2HSV)
   val samples=sampleCenters.map { center ->
    val pixels=mutableListOf<DoubleArray>();val cx=(center.first*300).roundToInt();val cy=(center.second*300).roundToInt()
    for(y in cy-10..cy+10 step 2) for(x in cx-10..cx+10 step 2) if(x in 0..299 && y in 0..299) {
     val hp=hsv.get(y,x);val lp=lab.get(y,x);if(hp[2]>=35) pixels+=doubleArrayOf(lp[0]*100/255,lp[1]-128,lp[2]-128,hp[0],hp[1],hp[2])
    }
    if(pixels.size<40) return Detection(emptyList(),normalized(points,bitmap),"Glare or shadow hides a triangular sticker.")
    fun median(i:Int)=pixels.map { it[i] }.sorted()[pixels.size/2]
    Sample(median(0),median(1),median(2),Sample.circularMedian(pixels.map { it[3] }),median(4),median(5))
   }
   return Detection(samples,normalized(listOf(top,bottom[1],bottom[0]).toTypedArray(),bitmap),"Hold steady to capture automatically")
  } finally { listOf(rgba,rgb,gray,edges,hierarchy,warp,lab,hsv).forEach(Mat::release);contours.forEach(Mat::release) }
 }
 private fun normalized(points:Array<Point>,bitmap:Bitmap)=points.map { (it.x/bitmap.width).toFloat() to (it.y/bitmap.height).toFloat() }
 companion object {
  // Facelet order matches PyraminxFacelets: 0 / 8,1,2 / 6,7,5,4,3.
  val sampleCenters=listOf(
   .50f to .13f,
   .50f to .39f,
   .63f to .48f,
   .83f to .82f,
   .70f to .70f,
   .57f to .82f,
   .17f to .82f,
   .30f to .70f,
   .37f to .48f,
  )
 }
}
