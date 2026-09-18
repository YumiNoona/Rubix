package com.cubeguide.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

internal object RubixTokens {
    val screenPadding = 20.dp
    val sectionGap = 24.dp
    val itemGap = 12.dp
    val controlShape = RoundedCornerShape(16.dp)
    val cardShape = RoundedCornerShape(24.dp)
    val modalShape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
}

internal val RubixBlue = Color(0xFF5AA7FF)
internal val RubixBlueStrong = Color(0xFF278BFF)
internal val RubixMint = Color(0xFF52D6A3)
internal val RubixAmber = Color(0xFFFFBE55)

internal fun rubixDarkScheme() = darkColorScheme(
    primary = RubixBlue,
    onPrimary = Color(0xFF001A2F),
    primaryContainer = Color(0xFF153A5D),
    onPrimaryContainer = Color(0xFFD8EAFF),
    secondary = RubixAmber,
    onSecondary = Color(0xFF2B1900),
    secondaryContainer = Color(0xFF4B3509),
    onSecondaryContainer = Color(0xFFFFE4AD),
    tertiary = RubixMint,
    onTertiary = Color(0xFF002116),
    error = Color(0xFFFF6B73),
    background = Color(0xFF071019),
    surface = Color(0xFF071019),
    surfaceVariant = Color(0xFF15222D),
    surfaceContainer = Color(0xFF0F1B25),
    surfaceContainerHigh = Color(0xFF172631),
    surfaceContainerHighest = Color(0xFF20313D),
    outline = Color(0xFF647988),
    outlineVariant = Color(0xFF293C49),
    onSurface = Color(0xFFF4F7FA),
    onSurfaceVariant = Color(0xFFACBBC5),
)

internal fun rubixLightScheme() = lightColorScheme(
    primary = Color(0xFF0061A4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = Color(0xFF7A5700),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDEA1),
    onSecondaryContainer = Color(0xFF261A00),
    tertiary = Color(0xFF006C4D),
    onTertiary = Color.White,
    error = Color(0xFFBA1A1A),
    background = Color(0xFFF7F9FC),
    surface = Color(0xFFF7F9FC),
    surfaceVariant = Color(0xFFDFE3E8),
    surfaceContainer = Color(0xFFEDF1F5),
    surfaceContainerHigh = Color(0xFFE5E9ED),
    surfaceContainerHighest = Color(0xFFDDE3E8),
    outline = Color(0xFF6F797F),
    outlineVariant = Color(0xFFBFC8CE),
    onSurface = Color(0xFF171C20),
    onSurfaceVariant = Color(0xFF40484D),
)

@Composable
internal fun rubixTypography() = Typography(
    displaySmall = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
    headlineLarge = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
    headlineMedium = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
    headlineSmall = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
)

@Composable
internal fun PageIntro(title: String, modifier: Modifier = Modifier, subtitle: String? = null) {
    Column(modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        if (!subtitle.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
internal fun RubixPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val feedback = rememberTouchFeedback()
    Button(
        onClick = { feedback(); onClick() },
        modifier = modifier.fillMaxWidth().heightIn(min = 56.dp),
        enabled = enabled,
        shape = RubixTokens.controlShape,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
    ) {
        if (icon != null) {
            Icon(icon, null, Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
        }
        Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
internal fun RubixActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    trailing: Boolean = true,
) {
    val feedback = rememberTouchFeedback()
    Card(
        onClick = { feedback(); onClick() },
        modifier = modifier.fillMaxWidth(),
        shape = RubixTokens.cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .72f)),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = RoundedCornerShape(16.dp), color = accent.copy(alpha = .14f)) {
                Box(Modifier.size(54.dp), contentAlignment = Alignment.Center) {
                    Icon(icon, null, Modifier.size(28.dp), tint = accent)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(3.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (trailing) {
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, "Open", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
internal fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
    )
}
