package com.cubeguide.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class FeatureIcon { HOME, SCAN, CUBE, TIMER, LEARN, SETTINGS }

@Composable
internal fun FeatureGlyph(icon: FeatureIcon, modifier: Modifier = Modifier) {
    val vector = when (icon) {
        FeatureIcon.HOME -> Icons.Rounded.Home
        FeatureIcon.SCAN -> Icons.Rounded.PhotoCamera
        FeatureIcon.CUBE -> Icons.Rounded.ViewInAr
        FeatureIcon.TIMER -> Icons.Rounded.Timer
        FeatureIcon.LEARN -> Icons.Rounded.School
        FeatureIcon.SETTINGS -> Icons.Rounded.Settings
    }
    Icon(vector, contentDescription = null, modifier = modifier, tint = MaterialTheme.colorScheme.primary)
}
