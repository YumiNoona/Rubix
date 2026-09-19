package com.cubeguide

import androidx.activity.compose.setContent
import androidx.lifecycle.SavedStateHandle
import com.cubeguide.ui.CubeApp
import com.cubeguide.ui.CubeViewModel
import com.cubeguide.core.CubeState
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import org.junit.runner.RunWith
import android.graphics.*
import com.cubeguide.core.CubeColor
import com.cubeguide.vision.*
import org.opencv.android.OpenCVLoader

@RunWith(AndroidJUnit4::class)
class AppFlowTest {
 @get:Rule val compose=createAndroidComposeRule<MainActivity>()
 private fun screenshot(name: String) {
  compose.waitForIdle()
  val instrumentation=InstrumentationRegistry.getInstrumentation()
  val bitmap=instrumentation.uiAutomation.takeScreenshot()
  val file=java.io.File(instrumentation.targetContext.getExternalFilesDir(null),"$name.png")
  file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }; bitmap.recycle()
 }
 @Test fun mainDockOrdersPracticeSolveAndLearn() {
  val practice=compose.onNodeWithText("Practice",useUnmergedTree=true).fetchSemanticsNode().boundsInRoot.center.x
  val solve=compose.onNodeWithContentDescription("Solve",useUnmergedTree=true).fetchSemanticsNode().boundsInRoot.center.x
  val learn=compose.onNodeWithText("Learn",useUnmergedTree=true).fetchSemanticsNode().boundsInRoot.center.x
  Assert.assertTrue(practice<solve)
  Assert.assertTrue(solve<learn)
 }
 @Test fun editingAnUncertainFrontStickerKeepsFrontSelected() {
  val vm=CubeViewModel(SavedStateHandle())
  val anchors=listOf(
   Sample(90.0,0.0,2.0,0.0,20.0,230.0),Sample(45.0,60.0,35.0,2.0,220.0,200.0),
   Sample(60.0,-50.0,35.0,65.0,200.0,190.0),Sample(90.0,-10.0,80.0,30.0,220.0,240.0),
   Sample(65.0,40.0,65.0,15.0,225.0,240.0),Sample(40.0,20.0,-65.0,115.0,220.0,200.0)
  )
  val ambiguous=Sample(55.0,50.0,50.0,8.5,222.0,220.0)
  compose.activity.runOnUiThread {
   vm.scan()
   repeat(6) {
    val face=vm.pose.face
    val samples=CubeState.solved().stickers.drop(face.ordinal*9).take(9).map { anchors[it.ordinal] }.toMutableList()
    if(face.ordinal==1 || face.ordinal==2) samples[0]=ambiguous
    Assert.assertTrue(vm.importFace(Detection(samples,emptyList(),"")))
   }
   compose.activity.setContent { CubeApp(vm) }
  }
  compose.onAllNodesWithContentDescription("front row 1 column 1",substring=true,useUnmergedTree=true).onFirst().performScrollTo().performClick()
  compose.onNodeWithText("Front · Green").assertIsDisplayed()
  compose.onNodeWithContentDescription("Green").performClick()
  compose.onAllNodesWithContentDescription("front row 1 column 1",substring=true,useUnmergedTree=true).onFirst().performScrollTo().performClick()
  compose.onNodeWithText("Front · Green").assertIsDisplayed()
 }
 @Test fun practiceFlowReachesSolved() {
  val vm=CubeViewModel(SavedStateHandle())
  compose.activity.runOnUiThread { vm.demo();compose.activity.setContent { CubeApp(vm) } }
  screenshot("home")
  screenshot("review")
  compose.onNodeWithText("Continue").assertIsDisplayed().performClick()
  compose.waitUntil(30000) { compose.onAllNodesWithText("Start guide").fetchSemanticsNodes().isNotEmpty() }
  compose.onNodeWithText("FACING YOU").assertIsDisplayed()
  compose.onNodeWithText("White").assertIsDisplayed()
  compose.onNodeWithText("Start guide").performClick()
  compose.onNodeWithContentDescription("Pause autoplay").performClick()
  compose.waitForIdle(); screenshot("guide")
  repeat(30) {
   if(compose.onAllNodesWithText("Order restored.").fetchSemanticsNodes().isNotEmpty()) return@repeat
   compose.waitUntil(5000) { compose.onAllNodesWithText("Next").fetchSemanticsNodes().any { !it.config.contains(androidx.compose.ui.semantics.SemanticsProperties.Disabled) } }
  compose.onNodeWithText("Next").assertIsDisplayed().performClick()
  }
  compose.onNodeWithText("Cube solved").assertExists()
  screenshot("solved")
 }
 @Test fun manualCorrectionRejectsBadCounts() {
  compose.onNodeWithText("Enter colors manually").performScrollTo().performClick()
  compose.onNodeWithContentDescription("Preview cube").assertIsDisplayed().performClick()
  compose.onNodeWithText("Cube preview").assertIsDisplayed()
  compose.activity.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
  compose.onAllNodesWithContentDescription("front row 1 column 1, Green",useUnmergedTree=true).onFirst().performClick()
  compose.onNodeWithText("Solve cube").assertIsDisplayed().performClick()
  compose.onNodeWithText("This cube needs a check").assertIsDisplayed()
  compose.onAllNodesWithText("exactly 9",substring=true).onLast().assertIsDisplayed()
 }
 @Test fun scanKeepsGalleryAndBackWithoutDuplicatingManualEntry() {
  compose.onNodeWithText("Scan puzzle").performScrollTo().performClick()
  compose.onNodeWithText("Choose puzzle").assertExists()
  compose.onNodeWithText("3×3").performClick()
  compose.onNodeWithText("Start scan").performScrollTo().performClick()
  compose.onNodeWithText("Gallery").performScrollTo().assertExists()
  compose.onNodeWithText("Enter colors instead").assertDoesNotExist()
  compose.onNodeWithContentDescription("Back").performClick()
  compose.onNodeWithText("Choose puzzle").assertExists()
 }

 @Test fun homePreviewIsLabelFreeAndScanUsesPuzzlePicker() {
  compose.onNodeWithContentDescription("Cube scrambling and solving itself").assertExists()
  compose.onNodeWithText("Demo").assertDoesNotExist()
  compose.onNodeWithText("Scan puzzle").performScrollTo().performClick()
  compose.onNodeWithText("Choose puzzle").assertExists()
  compose.onNodeWithText("2×2").assertExists()
  compose.onNodeWithText("6×6").assertExists()
  compose.onNodeWithText("7×7").assertExists()
 }

 @Test fun guideAutoplayAdvancesAndManualControlsRemainAvailable() {
  val vm=CubeViewModel(SavedStateHandle())
  compose.activity.runOnUiThread {
   vm.demo()
   vm.solve()
   compose.activity.setContent { CubeApp(vm) }
  }
  compose.waitUntil(30000) { vm.screen==com.cubeguide.ui.Screen.SETUP }
  compose.onNodeWithText("Start guide").performClick()
  compose.onNodeWithText("Previous").assertExists()
  compose.onNodeWithText("Next").assertExists()
  compose.waitUntil(8000) { vm.step>0 || vm.screen==com.cubeguide.ui.Screen.DONE }
 }
}

@RunWith(AndroidJUnit4::class)
class VisionInstrumentedTest {
 @Test fun perspectiveAndLightingSamplesRecognizeAllStickers() {
  Assert.assertTrue(OpenCVLoader.initLocal())
  val detector=FaceDetector()
  for(light in listOf(0.65f,0.85f,1f)) for(tilt in listOf(false,true)) {
   val bitmap=fixture(light,tilt)
   val result=try { detector.detect(bitmap) } finally { bitmap.recycle() }
   Assert.assertEquals("$light $tilt: ${result.message}",9,result.samples.size)
   val expected=listOf(CubeColor.WHITE,CubeColor.RED,CubeColor.BLUE,CubeColor.YELLOW,CubeColor.GREEN,CubeColor.ORANGE,CubeColor.RED,CubeColor.BLUE,CubeColor.WHITE)
   expected.forEachIndexed { i,c -> Assert.assertEquals("Sticker $i at $light / $tilt",c,ColorClassifier.nominal(result.samples[i])) }
  }
 }
 @Test fun uniformSurfaceAndDarkFrameAreRejected() {
  Assert.assertTrue(OpenCVLoader.initLocal()); val detector=FaceDetector()
  val black=Bitmap.createBitmap(480,640,Bitmap.Config.ARGB_8888); black.eraseColor(Color.BLACK)
  Assert.assertTrue(detector.detect(black).samples.isEmpty()); black.recycle()
  val blank=Bitmap.createBitmap(480,640,Bitmap.Config.ARGB_8888); val canvas=Canvas(blank); canvas.drawColor(Color.GRAY)
  canvas.drawRect(60f,140f,420f,500f,Paint().apply { color=Color.rgb(180,170,150) })
  Assert.assertTrue(detector.detect(blank).samples.isEmpty()); blank.recycle()
 }
 private fun fixture(light: Float,tilt: Boolean): Bitmap {
  val face=Bitmap.createBitmap(360,360,Bitmap.Config.ARGB_8888); val c=Canvas(face); c.drawColor(Color.rgb(15,18,16))
  val colors=listOf(CubeColor.WHITE,CubeColor.RED,CubeColor.BLUE,CubeColor.YELLOW,CubeColor.GREEN,CubeColor.ORANGE,CubeColor.RED,CubeColor.BLUE,CubeColor.WHITE)
  colors.forEachIndexed { i,color ->
   val value=color.argb.toInt(); val paint=Paint().apply { this.color=Color.rgb((Color.red(value)*light).toInt(),(Color.green(value)*light).toInt(),(Color.blue(value)*light).toInt()) }
   val x=i%3*120f; val y=i/3*120f; c.drawRect(x+8,y+8,x+112,y+112,paint)
  }
  val out=Bitmap.createBitmap(480,640,Bitmap.Config.ARGB_8888); val canvas=Canvas(out); canvas.drawColor(Color.rgb(95,95,95))
  val matrix=Matrix()
  val corners=if(tilt) floatArrayOf(70f,130f,410f,165f,390f,510f,80f,485f) else floatArrayOf(60f,140f,420f,140f,420f,500f,60f,500f)
  matrix.setPolyToPoly(floatArrayOf(0f,0f,360f,0f,360f,360f,0f,360f),0,corners,0,4)
  canvas.drawBitmap(face,matrix,Paint(Paint.ANTI_ALIAS_FLAG)); face.recycle(); return out
 }
}
