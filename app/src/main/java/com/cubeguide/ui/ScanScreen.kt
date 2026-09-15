package com.cubeguide.ui

import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.clip
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.cubeguide.camera.CameraPreview
import com.cubeguide.core.*

@Composable internal fun Scan(vm: CubeViewModel) {
 if(vm.scanIndex>0) CompletionFeedback(vm.scanIndex)
 val context=LocalContext.current
 val scope=rememberCoroutineScope()
 val feedback=rememberTouchFeedback()
 var permission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED) }
 var torch by remember { mutableStateOf(false) }
 var flashAvailable by remember { mutableStateOf(false) }
 var importing by remember { mutableStateOf(false) }
 var photo by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
 var detected by remember { mutableStateOf<com.cubeguide.vision.Detection?>(null) }
 var photoMessage by remember { mutableStateOf("") }
 var loading by remember { mutableStateOf(false) }
 val request=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permission=it }
 val picker=rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
  if(uri==null) importing=false else {
   loading=true
   scope.launch {
    val result=kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) { runCatching {
     val bitmap=if(android.os.Build.VERSION.SDK_INT>=28) {
      android.graphics.ImageDecoder.decodeBitmap(android.graphics.ImageDecoder.createSource(context.contentResolver,uri)) { decoder,info,_ ->
       decoder.allocator=android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
       val scale=minOf(1f,1280f/maxOf(info.size.width,info.size.height))
       decoder.setTargetSize((info.size.width*scale).toInt().coerceAtLeast(1),(info.size.height*scale).toInt().coerceAtLeast(1))
      }
     } else {
      val bounds=android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds=true }
      context.contentResolver.openInputStream(uri).use { android.graphics.BitmapFactory.decodeStream(it,null,bounds) }
      val options=android.graphics.BitmapFactory.Options().apply { while(maxOf(bounds.outWidth,bounds.outHeight)/inSampleSize>1280) inSampleSize*=2 }
      val original=context.contentResolver.openInputStream(uri).use { android.graphics.BitmapFactory.decodeStream(it,null,options) } ?: error("Couldn't open this image.")
      val exif=context.contentResolver.openInputStream(uri).use { stream -> stream?.let { androidx.exifinterface.media.ExifInterface(it) } }
      val matrix=android.graphics.Matrix().apply { if(exif?.isFlipped==true) postScale(-1f,1f); postRotate(exif?.rotationDegrees?.toFloat() ?: 0f) }
      android.graphics.Bitmap.createBitmap(original,0,0,original.width,original.height,matrix,true).also { if(it!==original) original.recycle() }
     }
     val detection=try { check(org.opencv.android.OpenCVLoader.initLocal()) { "Image processing unavailable. Use color entry instead." }; com.cubeguide.vision.FaceDetector().detect(bitmap) } catch(e: Exception) { bitmap.recycle(); throw e }
     bitmap to detection
    } }
    result.onSuccess { (bitmap,detection) -> photo=bitmap; detected=detection; photoMessage=if(detection.samples.size==9) "Check the face, then add it." else detection.message }.onFailure { photoMessage=it.message ?: "Couldn't read this photo. Try another image." }
    loading=false
   }
  }
 }
 DisposableEffect(photo) { val current=photo; onDispose { current?.recycle() } }
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
  val color=CubeColor.entries[vm.pose.face.ordinal]; val top=CubeColor.entries[vm.pose.top.ordinal]
  Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
   Box(Modifier.size(34.dp).background(Color(LocalAppPreferences.current.color(color)),RoundedCornerShape(10.dp)))
   Spacer(Modifier.width(12.dp))
   Column(Modifier.weight(1f)) { Text("${color.label} face",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold); Text("${top.label} center on top",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant) }
   Text("${vm.scanIndex+1} / 6",style=MaterialTheme.typography.labelLarge)
  }
  Spacer(Modifier.height(14.dp))
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)) {
   scanSequence.forEachIndexed { index,item ->
    val done=item.face in vm.capturedFaces
    val current=index==vm.scanIndex
    Surface(
     modifier=Modifier.weight(1f).aspectRatio(1f),
     shape=RoundedCornerShape(11.dp),
     color=if(done) Color(LocalAppPreferences.current.color(CubeColor.entries[item.face.ordinal])) else MaterialTheme.colorScheme.surfaceContainer,
     border=BorderStroke(if(current) 2.dp else 1.dp,if(current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
    ) { Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center) {
     if(done) Icon(Icons.Rounded.Check,"Face captured",tint=Color(LocalAppPreferences.current.ink(CubeColor.entries[item.face.ordinal])),modifier=Modifier.size(18.dp))
     else Text(CubeColor.entries[item.face.ordinal].initial,style=MaterialTheme.typography.labelMedium,color=if(current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
    } }
   }
  }
  Spacer(Modifier.height(14.dp))
  if(importing) {
   if(loading) Box(Modifier.fillMaxWidth().height(280.dp),contentAlignment=Alignment.Center) { CircularProgressIndicator() }
   photo?.let { bitmap -> androidx.compose.foundation.Image(bitmap=bitmap.asImageBitmap(),contentDescription="Selected cube face photo",modifier=Modifier.fillMaxWidth().height(280.dp),contentScale=androidx.compose.ui.layout.ContentScale.Fit) }
   Text(photoMessage,modifier=Modifier.padding(vertical=12.dp))
   Primary("Add this face",!loading && detected?.samples?.size==9) { detected?.let { if(vm.importFace(it)) { importing=false;photo=null;detected=null } else photoMessage=vm.message } }
   TextButton(onClick={importing=false;photo=null;detected=null},enabled=!loading,modifier=Modifier.fillMaxWidth()) { Text("Back to camera") }
  } else if(permission) {
   Box(Modifier.fillMaxWidth().height(330.dp).clip(RoundedCornerShape(24.dp)).background(Color.Black)) {
    CameraPreview(Modifier.fillMaxSize(),vm::detection,torch= torch,onFlashAvailable={flashAvailable=it})
    Canvas(Modifier.fillMaxSize()) {
     val ratio=vm.cameraAspect; val w=minOf(size.width,size.height*ratio); val h=w/ratio; val left=(size.width-w)/2; val topOffset=(size.height-h)/2
     if(vm.corners.size==4) {
      val points=vm.corners.map { Offset(left+it.first*w,topOffset+it.second*h) }
      val path=Path().apply { moveTo(points[0].x,points[0].y); points.drop(1).forEach { lineTo(it.x,it.y) }; close() }
      drawPath(path,AccentBlue,style=Stroke(3.dp.toPx()))
     } else {
      val edge=minOf(size.width,size.height)*0.7f
      drawRoundRect(Color.White.copy(alpha=0.55f),Offset((size.width-edge)/2,(size.height-edge)/2),androidx.compose.ui.geometry.Size(edge,edge),androidx.compose.ui.geometry.CornerRadius(18.dp.toPx()),style=Stroke(2.dp.toPx()))
     }
    }
   }
   Spacer(Modifier.height(14.dp)); LinearProgressIndicator(progress={vm.progress},modifier=Modifier.fillMaxWidth())
   Text(vm.message.ifBlank { "Fit one face inside the frame and hold steady." },modifier=Modifier.padding(vertical=12.dp),style=MaterialTheme.typography.bodyMedium)
  } else {
   Surface(Modifier.fillMaxWidth().padding(vertical=24.dp),shape=RoundedCornerShape(24.dp),color=MaterialTheme.colorScheme.surfaceContainer) {
    Column(Modifier.padding(24.dp)) { Text("Ready when you are",style=MaterialTheme.typography.titleLarge); Spacer(Modifier.height(12.dp)); Text("Allow camera access or choose a clear face photo from your gallery."); Spacer(Modifier.height(20.dp)); Primary("Allow camera") { request.launch(Manifest.permission.CAMERA) } }
   }
  }
  if(!importing) Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
   OutlinedButton(onClick={feedback();torch=!torch},enabled=permission && flashAvailable,modifier=Modifier.weight(1f)) { Icon(if(torch) Icons.Rounded.FlashOn else Icons.Rounded.FlashOff,null);Spacer(Modifier.width(7.dp));Text(if(torch) "Flash on" else "Flash") }
   OutlinedButton(onClick={feedback();torch=false;importing=true;photoMessage="";picker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))},modifier=Modifier.weight(1f)) { Icon(Icons.Rounded.PhotoLibrary,null);Spacer(Modifier.width(7.dp));Text("Gallery") }
  }
  Spacer(Modifier.height(12.dp))
 }
}
