package com.cubeguide.ui

import android.content.Context
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalView

class AppPreferences(context: Context) {
 private val storage=context.applicationContext.getSharedPreferences("display_preferences",Context.MODE_PRIVATE)
 var initials by mutableStateOf(storage.getBoolean("initials",false)); private set
 var haptics by mutableStateOf(storage.getBoolean("haptics",true)); private set
 var sound by mutableStateOf(storage.getBoolean("sound",false)); private set
 var keepAwake by mutableStateOf(storage.getBoolean("keepAwake",true)); private set
 var animationMillis by mutableIntStateOf(storage.getInt("animationMillis",1300)); private set
 private val colors=mutableStateMapOf<com.cubeguide.core.CubeColor,Int>().apply {
  com.cubeguide.core.CubeColor.entries.forEach { if(storage.contains("color_${it.name}")) put(it,storage.getInt("color_${it.name}",it.argb.toInt())) }
 }
 fun color(c: com.cubeguide.core.CubeColor): Int = colors[c] ?: c.argb.toInt()
 fun ink(c: com.cubeguide.core.CubeColor): Int = if(androidx.core.graphics.ColorUtils.calculateLuminance(color(c))>0.179) 0xFF101820.toInt() else 0xFFFFFFFF.toInt()
 fun updateColor(c: com.cubeguide.core.CubeColor,value: Int) { colors[c]=value; storage.edit().putInt("color_${c.name}",value).apply() }
 fun resetColors() { colors.clear(); val editor=storage.edit(); com.cubeguide.core.CubeColor.entries.forEach { editor.remove("color_${it.name}") }; editor.apply() }
 fun updateSound(value: Boolean) { sound=value; storage.edit().putBoolean("sound",value).apply() }
 fun updateKeepAwake(value: Boolean) { keepAwake=value; storage.edit().putBoolean("keepAwake",value).apply() }
 fun updateAnimation(value: Int) { animationMillis=value; storage.edit().putInt("animationMillis",value).apply() }
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
