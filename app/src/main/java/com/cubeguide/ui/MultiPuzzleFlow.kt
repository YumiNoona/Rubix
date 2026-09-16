package com.cubeguide.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.cubeguide.camera.CameraPreview
import com.cubeguide.core.*
import com.cubeguide.play.VirtualCube
import com.cubeguide.rendering.FourByFourView
import com.cubeguide.vision.*
import kotlinx.coroutines.*

@Composable internal fun MultiPuzzleFlow(session:MultiPuzzleSession,onExit:()->Unit) {
 val feedback=rememberTouchFeedback();var leave by remember { mutableStateOf(false) }
 val back={
  when(session.stage) {
   MultiPuzzleStage.EDIT -> session.cancelEdit()
   MultiPuzzleStage.SCAN -> session.stage=MultiPuzzleStage.PREPARE
   MultiPuzzleStage.REVIEW -> session.beginScan()
   MultiPuzzleStage.GUIDE -> leave=true
   MultiPuzzleStage.SETUP -> session.stage=MultiPuzzleStage.REVIEW
   MultiPuzzleStage.ANALYZING -> Unit
   else -> onExit()
  }
 }
 Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal=18.dp)) {
  Row(Modifier.fillMaxWidth().height(58.dp),verticalAlignment=Alignment.CenterVertically) {
   IconButton(onClick={feedback();back()},enabled=session.stage!=MultiPuzzleStage.ANALYZING,modifier=Modifier.size(48.dp)) { Icon(Icons.AutoMirrored.Rounded.ArrowBack,"Back",Modifier.size(26.dp)) }
   Text(stageTitle(session),style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f),maxLines=1)
   if(session.stage==MultiPuzzleStage.GUIDE) Text("${session.step+1}/${session.moves.size}",color=MaterialTheme.colorScheme.primary,fontWeight=FontWeight.SemiBold)
  }
  when(session.stage) {
   MultiPuzzleStage.PREPARE -> PuzzlePrepare(session)
   MultiPuzzleStage.SCAN -> PuzzleScanner(session)
   MultiPuzzleStage.REVIEW -> PuzzleReview(session)
   MultiPuzzleStage.EDIT -> PuzzleEditor(session)
   MultiPuzzleStage.ANALYZING -> PuzzleAnalyzing(session)
   MultiPuzzleStage.SETUP -> PuzzleSetup(session)
   MultiPuzzleStage.GUIDE -> PuzzleGuide(session)
   MultiPuzzleStage.DONE -> PuzzleDone(session,onExit)
  }
 }
 if(leave) AlertDialog(onDismissRequest={leave=false},title={Text("Leave this solve?")},text={Text("Your current guide will close.")},confirmButton={TextButton(onClick={leave=false;onExit()}){Text("Leave")}},dismissButton={TextButton(onClick={leave=false}){Text("Keep solving")}})
}

private fun stageTitle(session:MultiPuzzleSession)=when(session.stage) {
 MultiPuzzleStage.PREPARE -> "Prepare ${session.spec.shortName}"
 MultiPuzzleStage.SCAN -> "Scan ${session.spec.shortName}"
 MultiPuzzleStage.REVIEW -> "Review colors"
 MultiPuzzleStage.EDIT -> "Edit colors"
 MultiPuzzleStage.ANALYZING -> "Building solution"
 MultiPuzzleStage.SETUP -> "Starting position"
 MultiPuzzleStage.GUIDE -> "Solve ${session.spec.shortName}"
 MultiPuzzleStage.DONE -> "Complete"
}

@Composable private fun PuzzlePrepare(session:MultiPuzzleSession) {
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally) {
  Spacer(Modifier.height(14.dp));PuzzleGlyphLarge(session.spec)
  Spacer(Modifier.height(22.dp));Text("Set a clear reference",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
  Text(when(session.puzzle) {
   PuzzleId.TWO_BY_TWO -> "Find the white–red–green corner. Hold white on top, green facing you, and red on the right. Keep that orientation while capturing Top, Right, Front, Bottom, Left, and Back."
   PuzzleId.FOUR_BY_FOUR -> "Use a white–red–green corner as your reference: white on top, green facing you, red on the right. Turn the whole puzzle between photos without twisting any layer."
   PuzzleId.PYRAMINX -> "Keep one tip pointing up. Capture the green, red, blue, then yellow face in that order, with the same tip at the top of every photo."
   else -> "Follow the guided capture order."
  },textAlign=TextAlign.Center,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(horizontal=8.dp))
  Spacer(Modifier.height(20.dp))
  Surface(shape=RoundedCornerShape(20.dp),color=MaterialTheme.colorScheme.surfaceContainer) { Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
   PrepRow(Icons.Rounded.LightMode,"Use bright, even light")
   PrepRow(Icons.Rounded.CenterFocusStrong,"Keep the full face inside the frame")
   PrepRow(Icons.Rounded.ScreenRotation,"Rotate the whole puzzle only")
  } }
  Spacer(Modifier.height(22.dp));Primary("Start scanning") { session.beginScan() };TextButton(onClick=session::beginManual,modifier=Modifier.fillMaxWidth()){Text("Enter colors manually")};Spacer(Modifier.height(12.dp))
 }
}

@Composable private fun PrepRow(icon:androidx.compose.ui.graphics.vector.ImageVector,text:String) { Row(verticalAlignment=Alignment.CenterVertically) { Icon(icon,null,tint=MaterialTheme.colorScheme.primary);Spacer(Modifier.width(12.dp));Text(text,modifier=Modifier.weight(1f)) } }

@Composable private fun PuzzleGlyphLarge(spec:PuzzleSpec) {
 Surface(shape=RoundedCornerShape(28.dp),color=MaterialTheme.colorScheme.primaryContainer,modifier=Modifier.size(150.dp)) { Box(contentAlignment=Alignment.Center) { FeatureGlyph(FeatureIcon.CUBE,Modifier.size(74.dp));Text(spec.shortName,modifier=Modifier.align(Alignment.BottomCenter).padding(bottom=16.dp),fontWeight=FontWeight.Bold) } }
}

@Composable private fun PuzzleScanner(session:MultiPuzzleSession) {
 val context=LocalContext.current;val scope=rememberCoroutineScope();val feedback=rememberTouchFeedback()
 var permission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED) }
 var torch by remember { mutableStateOf(false) };var flashAvailable by remember { mutableStateOf(false) };var progress by remember { mutableFloatStateOf(0f) };var detected by remember { mutableStateOf<Detection?>(null) };var importing by remember { mutableStateOf(false) };var image by remember { mutableStateOf<Bitmap?>(null) };var loading by remember { mutableStateOf(false) }
 val stability=remember(session.scanIndex) { Stability() }
 val request=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permission=it }
 fun accept(d:Detection) { if(d.samples.size==session.faceSize && session.addCapture(d.samples)) { feedback();progress=0f;detected=null } else if(d.samples.size!=session.faceSize) session.message=d.message }
 val picker=rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
  if(uri==null) importing=false else scope.launch {
   loading=true
   runCatching { withContext(Dispatchers.Default) { decodePuzzlePhoto(context,uri,session.spec) } }.onSuccess { (bitmap,detection) -> image?.recycle();image=bitmap;detected=detection;session.message=if(detection.samples.size==session.faceSize) "Check the detected face, then add it." else detection.message }.onFailure { session.message=it.message ?: "Could not read that image." }
   loading=false
  }
 }
 DisposableEffect(image) { val current=image;onDispose { current?.recycle() } }
 Column(Modifier.fillMaxSize()) {
  val step=session.plan.steps[session.scanIndex]
  Text(step.title,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
  Text(step.instruction,color=MaterialTheme.colorScheme.onSurfaceVariant)
  Spacer(Modifier.height(10.dp));CaptureProgress(session.faceCount,session.captures.size)
  Spacer(Modifier.height(12.dp))
  Box(Modifier.fillMaxWidth().weight(1f).heightIn(min=260.dp).clip(RoundedCornerShape(24.dp)).background(Color.Black),contentAlignment=Alignment.Center) {
   when {
    importing && loading -> CircularProgressIndicator()
    importing && image!=null -> Image(image!!.asImageBitmap(),"Selected puzzle face",Modifier.fillMaxSize(),contentScale=androidx.compose.ui.layout.ContentScale.Fit)
    permission -> {
     CameraPreview(Modifier.fillMaxSize(),onDetection={ d -> detected=d;session.message=d.message;progress=stability.accept(d.samples,System.currentTimeMillis(),d.corners);if(progress>=1f) accept(d.copy(samples=stability.stableSamples)) },torch=torch,onFlashAvailable={flashAvailable=it},gridSize=session.spec.squareSize ?: 3,scanShape=session.spec.scanShape)
     Canvas(Modifier.fillMaxSize()) {
      val points=detected?.corners.orEmpty().map { androidx.compose.ui.geometry.Offset(it.first*size.width,it.second*size.height) }
      if(points.size>=3) { val path=Path().apply { moveTo(points.first().x,points.first().y);points.drop(1).forEach { lineTo(it.x,it.y) };close() };drawPath(path,AccentBlue,style=Stroke(3.dp.toPx())) }
      else { val edge=size.minDimension*.72f;drawRoundRect(Color.White.copy(alpha=.5f),androidx.compose.ui.geometry.Offset((size.width-edge)/2,(size.height-edge)/2),androidx.compose.ui.geometry.Size(edge,edge),androidx.compose.ui.geometry.CornerRadius(18.dp.toPx()),style=Stroke(2.dp.toPx())) }
     }
    }
    else -> Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.padding(24.dp)) { Icon(Icons.Rounded.PhotoCamera,null,Modifier.size(42.dp));Spacer(Modifier.height(12.dp));Text("Camera permission is needed for live scanning.",textAlign=TextAlign.Center);Spacer(Modifier.height(16.dp));Button(onClick={request.launch(Manifest.permission.CAMERA)}){Text("Allow camera")} }
   }
  }
  Spacer(Modifier.height(10.dp));LinearProgressIndicator(progress={progress},modifier=Modifier.fillMaxWidth())
  Text(session.message.ifBlank { if(session.puzzle==PuzzleId.PYRAMINX) "Point one tip up and hold the triangle steady." else "Fit the full face in the frame and hold steady." },style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(vertical=10.dp))
  if(importing) {
   Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) { OutlinedButton(onClick={importing=false;image=null;detected=null},modifier=Modifier.weight(1f)){Text("Camera")};Button(onClick={detected?.let(::accept)},enabled=detected?.samples?.size==session.faceSize,modifier=Modifier.weight(1f)){Text("Add face")} }
  } else Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
   OutlinedButton(onClick={feedback();torch=!torch},enabled=permission&&flashAvailable,modifier=Modifier.weight(1f)){Icon(if(torch) Icons.Rounded.FlashOn else Icons.Rounded.FlashOff,null);Spacer(Modifier.width(6.dp));Text("Flash")}
   OutlinedButton(onClick={feedback();torch=false;importing=true;picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))},modifier=Modifier.weight(1f)){Icon(Icons.Rounded.PhotoLibrary,null);Spacer(Modifier.width(6.dp));Text("Gallery")}
  }
  Spacer(Modifier.height(12.dp))
 }
}

private suspend fun decodePuzzlePhoto(context:android.content.Context,uri:android.net.Uri,spec:PuzzleSpec):Pair<Bitmap,Detection> {
 val bitmap=if(android.os.Build.VERSION.SDK_INT>=28) {
  val source=android.graphics.ImageDecoder.createSource(context.contentResolver,uri)
  android.graphics.ImageDecoder.decodeBitmap(source) { decoder,info,_ -> decoder.allocator=android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE;val scale=minOf(1f,1280f/maxOf(info.size.width,info.size.height));decoder.setTargetSize((info.size.width*scale).toInt().coerceAtLeast(1),(info.size.height*scale).toInt().coerceAtLeast(1)) }
 } else {
  val bounds=android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds=true }
  context.contentResolver.openInputStream(uri).use { android.graphics.BitmapFactory.decodeStream(it,null,bounds) }
  val options=android.graphics.BitmapFactory.Options().apply { while(maxOf(bounds.outWidth,bounds.outHeight)/inSampleSize>1280) inSampleSize*=2 }
  val original=context.contentResolver.openInputStream(uri).use { android.graphics.BitmapFactory.decodeStream(it,null,options) } ?: error("Could not open that image.")
  val exif=context.contentResolver.openInputStream(uri).use { stream -> stream?.let { androidx.exifinterface.media.ExifInterface(it) } }
  val matrix=android.graphics.Matrix().apply { if(exif?.isFlipped==true) postScale(-1f,1f);postRotate(exif?.rotationDegrees?.toFloat() ?: 0f) }
  android.graphics.Bitmap.createBitmap(original,0,0,original.width,original.height,matrix,true).also { if(it!==original) original.recycle() }
 }
 check(org.opencv.android.OpenCVLoader.initLocal()) { "Image processing is unavailable." }
 val detection=if(spec.scanShape==ScanShape.TRIANGLE_GRID) TriangleFaceDetector().detect(bitmap) else FaceDetector(spec.squareSize!!).detect(bitmap)
 return bitmap to detection
}

@Composable private fun CaptureProgress(total:Int,done:Int) { Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)) { repeat(total) { index -> Surface(Modifier.weight(1f).height(7.dp),shape=CircleShape,color=when { index<done->MaterialTheme.colorScheme.primary;index==done->MaterialTheme.colorScheme.secondary;else->MaterialTheme.colorScheme.surfaceContainerHighest }){} } } }

@Composable private fun PuzzleReview(session:MultiPuzzleSession) {
 val scope=rememberCoroutineScope();var show3D by rememberSaveable { mutableStateOf(false) }
 Column(Modifier.fillMaxSize()) {
  Text("Does it match?",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)
  Text("Compare every sticker with the puzzle in your hands.",color=MaterialTheme.colorScheme.onSurfaceVariant)
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center) { FilterChip(!show3D,{show3D=false},{Text("2D net")});Spacer(Modifier.width(8.dp));FilterChip(show3D,{show3D=true},{Text("3D model")}) }
  Box(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),contentAlignment=Alignment.Center) { if(show3D) PuzzleModel(session,Modifier.fillMaxWidth().height(310.dp)) else PuzzleNet(session,false) { face,_ -> session.beginEdit(face) } }
  Surface(shape=RoundedCornerShape(17.dp),color=MaterialTheme.colorScheme.surfaceContainer,modifier=Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp)) { Text(if(session.validation()==null) "Ready to solve" else "Check the colors",fontWeight=FontWeight.SemiBold,color=if(session.validation()==null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error);Text(session.validation() ?: if(session.lowConfidence.isEmpty()) "All pieces form a legal ${session.spec.shortName}." else "Outlined stickers had low scan confidence. Check them closely.",style=MaterialTheme.typography.bodySmall) } }
  Spacer(Modifier.height(10.dp));Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
   OutlinedButton(onClick={session.beginEdit()},modifier=Modifier.weight(1f).height(54.dp),shape=RoundedCornerShape(17.dp)){Icon(Icons.Rounded.Edit,null);Spacer(Modifier.width(6.dp));Text("Edit colors")}
   Button(onClick={session.busy=true;session.stage=MultiPuzzleStage.ANALYZING;scope.launch { val result=withContext(Dispatchers.Default){session.calculateSolution()};session.acceptSolution(result) }},enabled=session.validation()==null&&!session.busy,modifier=Modifier.weight(1f).height(54.dp),shape=RoundedCornerShape(17.dp)){Text("Solve")}
  };Spacer(Modifier.height(12.dp))
 }
}

@Composable private fun PuzzleEditor(session:MultiPuzzleSession) {
 val preferences=LocalAppPreferences.current;val feedback=rememberTouchFeedback();var brush by remember { mutableStateOf(session.availableColors.first()) }
 Column(Modifier.fillMaxSize()) {
  Row(verticalAlignment=Alignment.CenterVertically) { IconButton(onClick={session.editFace=(session.editFace+session.faceCount-1)%session.faceCount}){Icon(Icons.Rounded.ChevronLeft,"Previous face")};Text("Face ${session.editFace+1} of ${session.faceCount}",fontWeight=FontWeight.SemiBold,textAlign=TextAlign.Center,modifier=Modifier.weight(1f));IconButton(onClick={session.editFace=(session.editFace+1)%session.faceCount}){Icon(Icons.Rounded.ChevronRight,"Next face")} }
  Box(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),contentAlignment=Alignment.Center) { PuzzleFocusedFace(session,preferences.initials) { index -> feedback();session.setColor(index,brush) } }
  Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(8.dp)) { session.availableColors.chunked(if(session.availableColors.size>4) 3 else 4).forEach { row -> Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly) { row.forEach { color -> Column(horizontalAlignment=Alignment.CenterHorizontally) { Surface(onClick={feedback();brush=color},shape=CircleShape,color=Color(preferences.color(color)),border=BorderStroke(if(brush==color) 3.dp else 1.dp,if(brush==color) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),modifier=Modifier.size(48.dp).semantics { contentDescription=color.label }) { Box(contentAlignment=Alignment.Center) { if(preferences.initials) Text(color.initial,color=Color(preferences.ink(color)),fontWeight=FontWeight.Bold) } };Text(session.colors.count { it==color }.toString(),style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant) } } } } }
  Row(verticalAlignment=Alignment.CenterVertically) { Text("Show color initials",modifier=Modifier.weight(1f));Switch(preferences.initials,{preferences.updateInitials(it)}) }
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center) { FilledTonalIconButton(onClick=session::undoEdit,enabled=session.canUndo){Icon(Icons.AutoMirrored.Rounded.Undo,"Undo color change")};Spacer(Modifier.width(14.dp));FilledTonalIconButton(onClick=session::redoEdit,enabled=session.canRedo){Icon(Icons.AutoMirrored.Rounded.Redo,"Redo color change")} }
  Text(session.validation() ?: "Colors are valid.",style=MaterialTheme.typography.bodySmall,color=if(session.validation()==null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
  Spacer(Modifier.height(10.dp));Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) { OutlinedButton(onClick=session::cancelEdit,modifier=Modifier.weight(1f).height(52.dp)){Text("Cancel")};Button(onClick=session::finishEdit,modifier=Modifier.weight(1f).height(52.dp)){Text("Done")} };Spacer(Modifier.height(12.dp))
 }
}

@Composable private fun PuzzleNet(session:MultiPuzzleSession,showInitials:Boolean,onSticker:(Int,Int)->Unit) {
 if(session.puzzle==PuzzleId.PYRAMINX) {
  val facelets=session.pyraminxDisplayFacelets() ?: return
  Column(verticalArrangement=Arrangement.spacedBy(10.dp)) { repeat(2) { row -> Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)) { repeat(2) { column -> val face=row*2+column;val color=session.palette.getValue(PyraminxColor.entries[face]);Surface(Modifier.weight(1f),shape=RoundedCornerShape(18.dp),color=MaterialTheme.colorScheme.surfaceContainer){Column(Modifier.padding(8.dp),horizontalAlignment=Alignment.CenterHorizontally){Text("Face ${face+1} · ${color.label}",style=MaterialTheme.typography.labelMedium);PyraminxFaceView(facelets,face,Modifier.fillMaxWidth().aspectRatio(1.05f),palette=session.palette,showInitials=showInitials,onStickerClick={onSticker(face,it)})}} } } } }
 } else SquarePuzzleNet(session.colors,session.spec.squareSize!!,session.lowConfidence,showInitials,onSticker)
}

@Composable private fun SquarePuzzleNet(colors:List<CubeColor>,n:Int,uncertain:Set<Int>,showInitials:Boolean,onSticker:(Int,Int)->Unit) {
 val preferences=LocalAppPreferences.current;val faceArea=n*n;val rows=listOf(listOf(null,Face.U,null,null),listOf(Face.L,Face.F,Face.R,Face.B),listOf(null,Face.D,null,null))
 Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(5.dp)) { rows.forEach { faces -> Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)) { faces.forEach { face -> Box(Modifier.weight(1f).aspectRatio(1f)) { if(face!=null) Column(verticalArrangement=Arrangement.spacedBy(2.dp)) { repeat(n) { r -> Row(Modifier.weight(1f),horizontalArrangement=Arrangement.spacedBy(2.dp)) { repeat(n) { c -> val index=face.ordinal*faceArea+r*n+c;Box(Modifier.weight(1f).fillMaxHeight().background(Color(preferences.color(colors[index])),RoundedCornerShape(3.dp)).border(if(index in uncertain) 2.dp else .5.dp,if(index in uncertain) MaterialTheme.colorScheme.secondary else Color.Black.copy(alpha=.25f),RoundedCornerShape(3.dp)).clickable { onSticker(face.ordinal,index) },contentAlignment=Alignment.Center) { if(showInitials) Text(colors[index].initial,color=Color(preferences.ink(colors[index])),style=MaterialTheme.typography.labelSmall,fontWeight=FontWeight.Bold) } } } } } } } } } }
}

@Composable private fun PuzzleFocusedFace(session:MultiPuzzleSession,showInitials:Boolean,onSticker:(Int)->Unit) {
 if(session.puzzle==PuzzleId.PYRAMINX) { val facelets=session.pyraminxDisplayFacelets() ?: return;PyraminxFaceView(facelets,session.editFace,Modifier.fillMaxWidth().aspectRatio(1.05f).padding(18.dp),palette=session.palette,showInitials=showInitials,onStickerClick=onSticker) }
 else { val n=session.spec.squareSize!!;val preferences=LocalAppPreferences.current;Column(Modifier.fillMaxWidth().padding(horizontal=32.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) { repeat(n) { r -> Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) { repeat(n) { c -> val index=session.editFace*n*n+r*n+c;Box(Modifier.weight(1f).aspectRatio(1f).background(Color(preferences.color(session.colors[index])),RoundedCornerShape(9.dp)).border(1.dp,Color.Black.copy(alpha=.25f),RoundedCornerShape(9.dp)).clickable { onSticker(index) },contentAlignment=Alignment.Center) { if(showInitials) Text(session.colors[index].initial,color=Color(preferences.ink(session.colors[index])),fontWeight=FontWeight.Bold) } } } } } }
}

@Composable private fun PuzzleAnalyzing(session:MultiPuzzleSession) { Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) { CircularProgressIndicator(Modifier.size(54.dp));Spacer(Modifier.height(22.dp));Text("Finding a replay-verified solution",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.SemiBold,textAlign=TextAlign.Center);Text(if(session.puzzle==PuzzleId.FOUR_BY_FOUR) "4×4 reduction can take a little longer." else "This usually takes a moment.",color=MaterialTheme.colorScheme.onSurfaceVariant,textAlign=TextAlign.Center) } }

@Composable private fun PuzzleSetup(session:MultiPuzzleSession) {
 Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally) {
  Text("Solution ready",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)
  Text("${session.moves.size} verified moves",color=MaterialTheme.colorScheme.primary)
  Box(Modifier.fillMaxWidth().weight(1f),contentAlignment=Alignment.Center) { PuzzleModel(session,Modifier.fillMaxSize()) }
  Surface(shape=RoundedCornerShape(18.dp),color=MaterialTheme.colorScheme.surfaceContainer,modifier=Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("Hold this position",fontWeight=FontWeight.SemiBold);Text(if(session.puzzle==PuzzleId.PYRAMINX) "Keep one tip pointing up and the first scanned face toward you." else "Keep white on top, green facing you, and red on the right throughout the guide.",color=MaterialTheme.colorScheme.onSurfaceVariant) } }
  Spacer(Modifier.height(12.dp));Primary("Start solving",onClick=session::startGuide);Spacer(Modifier.height(12.dp))
 }
}

@Composable private fun PuzzleGuide(session:MultiPuzzleSession) {
 val preferences=LocalAppPreferences.current;val feedback=rememberTouchFeedback();var auto by rememberSaveable { mutableStateOf(true) };var replay by remember { mutableIntStateOf(0) };var progress by remember(session.step) { mutableFloatStateOf(0f) };var menu by remember { mutableStateOf(false) };val notation=session.moves.getOrNull(session.step) ?: return
 val pyrProgress=remember(session.step,replay) { Animatable(0f) }
 if(session.puzzle==PuzzleId.PYRAMINX) LaunchedEffect(pyrProgress,replay) { pyrProgress.animateTo(1f,tween(preferences.animationMillis));progress=1f }
 LaunchedEffect(session.step,progress,auto) { if(auto&&progress>=.99f) { delay(preferences.guideDelayMillis.toLong());if(auto&&session.stage==MultiPuzzleStage.GUIDE) { feedback();session.next() } } }
 Column(Modifier.fillMaxSize()) {
  LinearProgressIndicator(progress={((session.step+progress)/session.moves.size).coerceIn(0f,1f)},modifier=Modifier.fillMaxWidth())
  Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) { Text(session.instruction(notation),style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.SemiBold,modifier=Modifier.weight(1f));IconButton(onClick={feedback();auto=!auto}){Icon(if(auto) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,if(auto) "Pause autoplay" else "Start autoplay")};Box { IconButton(onClick={auto=false;menu=true}){Icon(Icons.Rounded.MoreVert,"More options")};DropdownMenu(menu,{menu=false}) { DropdownMenuItem({Text("Replay move")},{menu=false;replay++;progress=0f},leadingIcon={Icon(Icons.Rounded.Replay,null)});DropdownMenuItem({Text("Correct current colors")},{menu=false;session.beginEdit()},leadingIcon={Icon(Icons.Rounded.Edit,null)});DropdownMenuItem({Text("Restart guide")},{menu=false;session.restart()},leadingIcon={Icon(Icons.Rounded.RestartAlt,null)}) } } }
  Box(Modifier.fillMaxWidth().weight(1f),contentAlignment=Alignment.Center) {
   when(session.puzzle) {
    PuzzleId.TWO_BY_TWO -> VirtualCubeView(VirtualCube(2,session.colors.toList()),Modifier.fillMaxSize(),Move.parse(notation).single(),replay){progress=it}
    PuzzleId.FOUR_BY_FOUR -> FourByFourView(FourByFourState(session.colors.toList()),Modifier.fillMaxSize(),FourByFourMove.parse(notation).single(),replay){progress=it}
    PuzzleId.PYRAMINX -> PyraminxGuideModel(session,Modifier.fillMaxSize(),pyrProgress.value)
    else -> Unit
   }
  }
  Surface(shape=RoundedCornerShape(18.dp),color=MaterialTheme.colorScheme.surfaceContainer,modifier=Modifier.fillMaxWidth()) { Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically) { TurnArrow(notation.endsWith("'"),Modifier.size(34.dp));Spacer(Modifier.width(12.dp));Column { Text(notation,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);Text(if(notation.contains("w")) "Wide turn: move two layers together." else if(notation.first().isLowerCase()) "Tip only: turn the small point." else "Keep the same holding position.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant) } } }
  Spacer(Modifier.height(10.dp));Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) { OutlinedButton(onClick={feedback();auto=false;session.previous()},enabled=session.step>0,modifier=Modifier.weight(1f).height(54.dp)){Text("Previous")};Button(onClick={feedback();auto=false;session.next()},enabled=progress>=.99f,modifier=Modifier.weight(1f).height(54.dp)){Text("Next")} };Spacer(Modifier.height(12.dp))
 }
}

@Composable private fun PuzzleModel(session:MultiPuzzleSession,modifier:Modifier) {
 when(session.puzzle) {
  PuzzleId.TWO_BY_TWO -> VirtualCubeView(VirtualCube(2,session.colors.toList()),modifier)
  PuzzleId.FOUR_BY_FOUR -> FourByFourView(FourByFourState(session.colors.toList()),modifier)
  PuzzleId.PYRAMINX -> PyraminxGuideModel(session,modifier,1f)
  else -> Unit
 }
}

@Composable private fun PyraminxGuideModel(session:MultiPuzzleSession,modifier:Modifier,progress:Float) {
 val facelets=session.pyraminxDisplayFacelets() ?: return;val active=session.moves.getOrNull(session.step)?.let { PyraminxMove.parse(it).single().axis.ordinal } ?: 0
 Box(modifier,contentAlignment=Alignment.Center) {
  PyraminxFaceView(facelets,(active+2)%4,Modifier.fillMaxWidth(.58f).aspectRatio(1.02f).offset(x=(-72).dp,y=18.dp).graphicsLayer { rotationY=58f;alpha=.72f },palette=session.palette)
  PyraminxFaceView(facelets,(active+1)%4,Modifier.fillMaxWidth(.58f).aspectRatio(1.02f).offset(x=72.dp,y=18.dp).graphicsLayer { rotationY=-58f;alpha=.72f },palette=session.palette)
  PyraminxFaceView(facelets,active,Modifier.fillMaxWidth(.66f).aspectRatio(1.02f).graphicsLayer { scaleX=1f+.035f*kotlin.math.sin(progress*3.14f);scaleY=scaleX },palette=session.palette)
 }
}

@Composable private fun PuzzleDone(session:MultiPuzzleSession,onExit:()->Unit) {
 CompletionFeedback(session.moves)
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally) {
  Spacer(Modifier.height(28.dp));Icon(Icons.Rounded.CheckCircle,"Solved",Modifier.size(72.dp),tint=MaterialTheme.colorScheme.primary);Spacer(Modifier.height(12.dp));Text("${session.spec.name} solved",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text("${session.moves.size} verified moves",color=MaterialTheme.colorScheme.onSurfaceVariant)
  Box(Modifier.fillMaxWidth().height(300.dp),contentAlignment=Alignment.Center) { when(session.puzzle) { PuzzleId.TWO_BY_TWO->VirtualCubeView(VirtualCube(2,session.colors.toList()),Modifier.fillMaxSize());PuzzleId.FOUR_BY_FOUR->FourByFourView(FourByFourState(session.colors.toList()),Modifier.fillMaxSize());PuzzleId.PYRAMINX->PyraminxGuideModel(session,Modifier.fillMaxSize(),1f);else->Unit } }
  Primary("Solve another") { onExit() };TextButton(onClick=session::replay,enabled=session.moves.isNotEmpty()){Text("Replay solution")};Spacer(Modifier.height(12.dp))
 }
}
