package com.cubeguide

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
 @Test fun practiceFlowReachesSolved() {
  screenshot("home")
  compose.onNodeWithText("Try a practice solve").performScrollTo().performClick()
  screenshot("review")
  compose.onNodeWithText("Solve this cube").assertIsDisplayed().performClick()
  compose.waitUntil(30000) { compose.onAllNodesWithText("Cube is in position").fetchSemanticsNodes().isNotEmpty() }
  compose.onNodeWithText("FACING YOU").assertIsDisplayed()
  compose.onNodeWithText("White").assertIsDisplayed()
  compose.onNodeWithText("Cube is in position").performClick()
  compose.waitForIdle(); screenshot("guide")
  repeat(30) {
   if(compose.onAllNodesWithText("Order restored.").fetchSemanticsNodes().isNotEmpty()) return@repeat
   compose.onNodeWithText("Done this turn Ã‚Â· Next").performScrollTo().performClick()
  }
  compose.onNodeWithText("Order restored.").assertExists()
  screenshot("solved")
 }
 @Test fun manualCorrectionRejectsBadCounts() {
  compose.onNodeWithText("Enter colors manually").performScrollTo().performClick()
  compose.onAllNodesWithContentDescription("front row 1 column 1, Green",useUnmergedTree=true).onFirst().performClick()
  compose.onNodeWithText("Red",substring=false).performClick()
  compose.onNodeWithText("Solve this cube").assertIsDisplayed().performClick()
  compose.onNodeWithText("Check the scan before solving").assertIsDisplayed()
  compose.onNodeWithText("Check colors").assertIsDisplayed()
  compose.onNodeWithText("Rescan cube").assertIsDisplayed()
  compose.onNodeWithText("White has",substring=true).assertDoesNotExist()
  compose.onAllNodesWithText("exactly 9",substring=true).onLast().assertIsDisplayed()
 }
 @Test fun deniedCameraStillOffersManualEntry() {
  compose.onNodeWithText("Scan my cube").performScrollTo().performClick()
  compose.onNodeWithText("Use manual entry instead").performScrollTo().assertExists().performClick()
  compose.onNodeWithText("A good look first.").assertExists()
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
