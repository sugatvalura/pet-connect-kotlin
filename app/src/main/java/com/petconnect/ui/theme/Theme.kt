package com.petconnect.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Design tokens — the single place colours live (ported from theme.ts). */
object PcColors {
    // Teal primary is deliberately distinct from the success green.
    val Primary = Color(0xFF0E9594)
    val PrimaryDark = Color(0xFF0A7574)
    val PrimarySoft = Color(0xFFE4F5F5)
    val Accent = Color(0xFFF4A259)
    val AccentSoft = Color(0xFFFDF0E3)
    val Ink = Color(0xFF14262B)
    val InkMuted = Color(0xFF5A737A)
    val InkFaint = Color(0xFF93A7AD)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceAlt = Color(0xFFF4F8F8)
    val Border = Color(0xFFE2EDED)
    val Success = Color(0xFF2E9E5B)
    val SuccessSoft = Color(0xFFE8F6EE)
    val Warning = Color(0xFFC8890B)
    val WarningSoft = Color(0xFFFBF2DF)
    val Danger = Color(0xFFE5484D)
    val DangerSoft = Color(0xFFFDECEC)
    val Male = Color(0xFF3E8BFF)
    val MaleSoft = Color(0xFFEAF2FF)
    val Female = Color(0xFFF76CA6)
    val FemaleSoft = Color(0xFFFDEDF4)
    val Scrim = Color(0x8C14262B)
}

object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

private val colorScheme = lightColorScheme(
    primary = PcColors.Primary,
    onPrimary = Color.White,
    primaryContainer = PcColors.PrimarySoft,
    onPrimaryContainer = PcColors.PrimaryDark,
    secondary = PcColors.Accent,
    secondaryContainer = PcColors.AccentSoft,
    background = PcColors.SurfaceAlt,
    onBackground = PcColors.Ink,
    surface = PcColors.Surface,
    onSurface = PcColors.Ink,
    surfaceVariant = PcColors.SurfaceAlt,
    onSurfaceVariant = PcColors.InkMuted,
    outline = PcColors.Border,
    outlineVariant = PcColors.Border,
    error = PcColors.Danger,
    errorContainer = PcColors.DangerSoft,
)

private val typography = Typography(
    headlineMedium = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold),
    headlineSmall = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
    bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
    labelSmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
)

private val shapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(26.dp),
)

@Composable
fun PetConnectTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colorScheme, typography = typography, shapes = shapes, content = content)
}
