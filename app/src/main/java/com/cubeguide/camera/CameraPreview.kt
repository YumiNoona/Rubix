package com.cubeguide.camera

import android.graphics.Matrix
import android.os.Handler
import android.os.Looper
import androidx.camera.core.*
import androidx.camera.core.resolutionselector.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.cubeguide.vision.*
import org.opencv.android.OpenCVLoader
import java.util.concurrent.Executors

@Composable fun CameraPreview(modifier: Modifier,onDetection: (Detection)->Unit) {
 val context=LocalContext.current; val lifecycle=LocalLifecycleOwner.current
 val callback by rememberUpdatedState(onDetection)
 val previewView=remember { PreviewView(context).apply { scaleType=PreviewView.ScaleType.FIT_CENTER; implementationMode=PreviewView.ImplementationMode.COMPATIBLE } }
 AndroidView(factory={ previewView },modifier=modifier)
 DisposableEffect(lifecycle) {
  val executor=Executors.newSingleThreadExecutor(); val handler=Handler(Looper.getMainLooper())
  val future=ProcessCameraProvider.getInstance(context); var provider: ProcessCameraProvider?=null
  var disposed=false; var preview: Preview?=null; var analysis: ImageAnalysis?=null
  future.addListener({
   if(!disposed) try {
    check(OpenCVLoader.initLocal()) { "Camera processing couldn't start. Use manual color entry." }
    provider=future.get()
    preview=Preview.Builder().build().also { it.surfaceProvider=previewView.surfaceProvider }
    analysis=ImageAnalysis.Builder().setResolutionSelector(ResolutionSelector.Builder().setResolutionStrategy(ResolutionStrategy(android.util.Size(640,480),ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER)).build()).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888).build()
    val detector=FaceDetector(); var lastFrame=0L
    analysis!!.setAnalyzer(executor) { image ->
     try {
      val now=System.currentTimeMillis()
      if(now-lastFrame>=120) {
       lastFrame=now
       val original=image.toBitmap()
       val matrix=Matrix().apply { postRotate(image.imageInfo.rotationDegrees.toFloat()) }
       val rotated=android.graphics.Bitmap.createBitmap(original,0,0,original.width,original.height,matrix,true)
       val detection=try { detector.detect(rotated).copy(aspectRatio=rotated.width.toFloat()/rotated.height) } finally { if(rotated!==original) rotated.recycle(); original.recycle() }
       handler.post { if(!disposed) callback(detection) }
      }
     } catch(e: Exception) { handler.post { if(!disposed) callback(Detection(emptyList(),emptyList(),"Couldn't read the camera frame. Try again or edit colors manually.")) } }
     finally { image.close() }
    }
    provider!!.bindToLifecycle(lifecycle,CameraSelector.DEFAULT_BACK_CAMERA,preview,analysis)
   } catch(e: Exception) { callback(Detection(emptyList(),emptyList(),e.message ?: "Camera unavailable. Use manual entry.")) }
  },ContextCompat.getMainExecutor(context))
  onDispose { disposed=true; analysis?.clearAnalyzer(); val useCases=listOfNotNull(preview,analysis).toTypedArray(); if(useCases.isNotEmpty()) provider?.unbind(*useCases); executor.shutdown(); handler.removeCallbacksAndMessages(null) }
 }
}
