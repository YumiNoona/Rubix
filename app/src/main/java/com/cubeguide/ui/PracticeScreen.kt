package com.cubeguide.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun PracticeScreen(vm: CubeViewModel) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        PageIntro("Build your speed", subtitle = "Play, practise, repeat.")
        Spacer(Modifier.height(RubixTokens.sectionGap))
        RubixActionCard(
            title = "Virtual cube",
            subtitle = "Free play and guided challenges",
            icon = Icons.Rounded.ViewInAr,
            accent = MaterialTheme.colorScheme.primary,
            onClick = { vm.open(Screen.VIRTUAL) },
        )
        Spacer(Modifier.height(RubixTokens.itemGap))
        RubixActionCard(
            title = "Solve timer",
            subtitle = "Scrambles, records and averages",
            icon = Icons.Rounded.Timer,
            accent = MaterialTheme.colorScheme.tertiary,
            onClick = { vm.open(Screen.TIMER) },
        )
        Spacer(Modifier.height(RubixTokens.sectionGap))
        SectionLabel("Quick start")
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickPractice("Free play", Icons.Rounded.TouchApp, Modifier.weight(1f)) {
                vm.open(Screen.VIRTUAL); vm.selectVirtualMode(VirtualMode.FREE)
            }
            QuickPractice("Challenge", Icons.Rounded.Bolt, Modifier.weight(1f)) {
                vm.open(Screen.VIRTUAL); vm.selectVirtualMode(VirtualMode.CHALLENGE)
            }
        }
    }
}

@Composable
private fun QuickPractice(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val feedback = rememberTouchFeedback()
    androidx.compose.material3.OutlinedButton(
        onClick = { feedback(); onClick() },
        modifier = modifier.height(54.dp),
        shape = RubixTokens.controlShape,
    ) {
        androidx.compose.material3.Icon(icon, null, Modifier.size(20.dp))
        Spacer(Modifier.width(7.dp))
        androidx.compose.material3.Text(label, maxLines = 1)
    }
}
