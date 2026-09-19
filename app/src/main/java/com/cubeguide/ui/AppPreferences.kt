package com.cubeguide.ui

import android.content.Context
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalView

enum class AppearanceMode { DARK, LIGHT }

class AppPreferences(context: Context) {
 private val storage=context.applicationContext.getSharedPreferences("display_preferences",Context.MODE_PRIVATE)
 var initials by mutableStateOf(storage.getBoolean("initials",false)); private set
 var haptics by mutableStateOf(storage.getBoolean("haptics",true)); private set
 var sound by mutableStateOf(storage.getBoolean("sound",false)); private set
 var keepAwake by mutableStateOf(storage.getBoolean("keepAwake",true)); private set
 var appearance by mutableStateOf(runCatching { AppearanceMode.valueOf(storage.getString("appearance",AppearanceMode.DARK.name)!!) }.getOrDefault(AppearanceMode.DARK)); private set
 var cameraSensitivity by mutableFloatStateOf(storage.getFloat("cameraSensitivity",.0048f).coerceIn(.0028f,.007f)); private set
 var puzzleSize by mutableIntStateOf(storage.getInt("puzzleSize",3).coerceIn(2,7)); private set
 var puzzleId by mutableStateOf(com.cubeguide.core.PuzzleId.fromStorage(storage.getString("puzzleId",null))); private set
 var completedLessons by mutableStateOf(storage.getStringSet("completedLessons",emptySet())!!.mapNotNull { it.toIntOrNull() }.toSet()); private set
 private var savedTimerRecords by mutableStateOf(
  storage.getString("timerRecords_3",storage.getString("timerRecords",""))!!
   .split(",").mapNotNull { it.toLongOrNull() }.take(20)
 )
 var animationMillis by mutableIntStateOf(storage.getInt("animationMillis",1300)); private set
 var guideDelayMillis by mutableIntStateOf(storage.getInt("guideDelayMillis",1600).coerceIn(1000,2000)); private set
 private val colors=mutableStateMapOf<com.cubeguide.core.CubeColor,Int>().apply {
  com.cubeguide.core.CubeColor.entries.forEach { if(storage.contains("color_${it.name}")) put(it,storage.getInt("color_${it.name}",it.argb.toInt())) }
 }
 fun color(c: com.cubeguide.core.CubeColor): Int = colors[c] ?: c.argb.toInt()
 fun ink(c: com.cubeguide.core.CubeColor): Int = if(androidx.core.graphics.ColorUtils.calculateLuminance(color(c))>0.179) 0xFF101820.toInt() else 0xFFFFFFFF.toInt()
 fun updateColor(c: com.cubeguide.core.CubeColor,value: Int) { colors[c]=value; storage.edit().putInt("color_${c.name}",value).apply() }
 fun resetColors() { colors.clear(); val editor=storage.edit(); com.cubeguide.core.CubeColor.entries.forEach { editor.remove("color_${it.name}") }; editor.apply() }
 fun updatePuzzleSize(value: Int) { puzzleSize=value.coerceIn(2,7);storage.edit().putInt("puzzleSize",puzzleSize).apply() }
 fun updatePuzzle(value: com.cubeguide.core.PuzzleId) {
  puzzleId=value
  com.cubeguide.core.PuzzleRegistry.get(value).squareSize?.let { puzzleSize=it }
  storage.edit().putString("puzzleId",value.storageId).putInt("puzzleSize",puzzleSize).apply()
 }
 fun completeLesson(index: Int) { completedLessons=completedLessons+index;storage.edit().putStringSet("completedLessons",completedLessons.map { it.toString() }.toSet()).apply() }
 fun timerRecords(): List<Long> = savedTimerRecords
 fun addTimerRecord(milliseconds: Long) { if(milliseconds<100) return;savedTimerRecords=(listOf(milliseconds)+savedTimerRecords).take(20);storage.edit().putString("timerRecords_3",savedTimerRecords.joinToString(",")).apply() }
 fun clearTimerRecords() { savedTimerRecords=emptyList();storage.edit().remove("timerRecords_3").remove("timerRecords").apply() }
 fun updateSound(value: Boolean) { sound=value; storage.edit().putBoolean("sound",value).apply() }
 fun updateKeepAwake(value: Boolean) { keepAwake=value; storage.edit().putBoolean("keepAwake",value).apply() }
 fun updateAppearance(value: AppearanceMode) { appearance=value;storage.edit().putString("appearance",value.name).apply() }
 fun updateCameraSensitivity(value: Float) { cameraSensitivity=value.coerceIn(.0028f,.007f);storage.edit().putFloat("cameraSensitivity",cameraSensitivity).apply() }
 fun updateAnimation(value: Int) { animationMillis=value; storage.edit().putInt("animationMillis",value).apply() }
 fun updateGuideDelay(value: Int) { guideDelayMillis=value.coerceIn(1000,2000);storage.edit().putInt("guideDelayMillis",guideDelayMillis).apply() }
 fun updateInitials(value: Boolean) { initials=value; storage.edit().putBoolean("initials",value).apply() }
 fun updateHaptics(value: Boolean) { haptics=value; storage.edit().putBoolean("haptics",value).apply() }
}
val LocalAppPreferences=staticCompositionLocalOf<AppPreferences> { error("App preferences missing") }

/** Uses Android touch feedback and respects the system's haptic preference. */
@Composable fun rememberTouchFeedback(): () -> Unit {
 val view=LocalView.current
 val preferences=LocalAppPreferences.current
 return remember(view,preferences) { {
  if(preferences.haptics) view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
  if(preferences.sound) view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
  Unit
 } }
}

@Composable fun CompletionFeedback(key: Any) {
 val view=LocalView.current
 val preferences=LocalAppPreferences.current
 LaunchedEffect(key) {
  if(preferences.haptics) view.performHapticFeedback(if(Build.VERSION.SDK_INT>=30) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.LONG_PRESS)
 }
}
