package com.cubeguide.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cubeguide.core.CubeColor
import com.cubeguide.core.CubeState
import com.cubeguide.core.PuzzleId
import com.cubeguide.core.PuzzleRegistry
import com.cubeguide.core.PuzzleSpec
import com.cubeguide.play.VirtualCube
import com.cubeguide.rendering.CubeView

@Composable
internal fun ThreeByThreeScanPreparation(onStart: () -> Unit) {
    RegularCubeScanPreparation(PuzzleRegistry.get(PuzzleId.THREE_BY_THREE),onStart)
}

@Composable
internal fun RegularCubeScanPreparation(
    spec: PuzzleSpec,
    onStart: () -> Unit,
    onManual: (() -> Unit)? = null,
) {
    val feedback = rememberTouchFeedback()
    val preferences = LocalAppPreferences.current
    val size = spec.squareSize ?: 3
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PageIntro("Set up your ${spec.shortName}",subtitle="White on top. Green facing you. Red on the right.")
        if(size==3) CubeView(CubeState.solved(),Modifier.fillMaxWidth().height(215.dp),showInitials=false)
        else VirtualCubeView(VirtualCube.solved(size),Modifier.fillMaxWidth().height(215.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ScanTip(Icons.Rounded.LightMode, "Even light", "Avoid glare", Modifier.weight(1f))
            ScanTip(Icons.Rounded.CenterFocusStrong, "Fill frame", "Show ${size*size} tiles", Modifier.weight(1f))
            ScanTip(Icons.Rounded.ScreenRotation, "Keep top", "Rotate cube", Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RubixTokens.cardShape,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Capture order", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf(
                        CubeColor.GREEN,
                        CubeColor.RED,
                        CubeColor.BLUE,
                        CubeColor.ORANGE,
                        CubeColor.WHITE,
                        CubeColor.YELLOW,
                    ).forEachIndexed { index, color ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                Modifier.size(34.dp)
                                    .background(Color(preferences.color(color)), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    (index + 1).toString(),
                                    color = Color(preferences.ink(color)),
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(color.label.take(1), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        RubixPrimaryButton("Start scan",onStart,icon=Icons.Rounded.PhotoCamera)
        if(onManual!=null) TextButton(onClick={feedback();onManual()},modifier=Modifier.fillMaxWidth()) { Text("Enter colors manually") }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ScanTip(icon: ImageVector, title: String, subtitle: String, modifier: Modifier) {
    Surface(modifier, color = MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(16.dp)) {
        Column(
            Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(7.dp))
            Text(title, style = MaterialTheme.typography.labelLarge, maxLines = 1)
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}
