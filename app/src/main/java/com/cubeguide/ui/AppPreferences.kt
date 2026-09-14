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
