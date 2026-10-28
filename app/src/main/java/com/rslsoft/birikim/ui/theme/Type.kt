package com.rslsoft.birikim.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ============================================================================
// Birikim Typography System (HarmonyOS 4/5 Inspired)
// ============================================================================
@Immutable
data class BirikimTypography(
    val balanceDisplay: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        fontFeatureSettings = "tnum"
    ),
    val titleLarge: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    val cardTitle: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    val body: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    val label: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    val numberTabular: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontFeatureSettings = "tnum"
    )
)

val LocalBirikimTypography = staticCompositionLocalOf { BirikimTypography() }

/**
 * Maps Birikim custom typography to Material3 Typography for standard component fallback.
 */
fun BirikimTypography.toMaterialTypography(): Typography = Typography(
    displayLarge = balanceDisplay,
    headlineMedium = titleLarge,
    titleMedium = cardTitle,
    bodyLarge = body.copy(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = body,
    labelLarge = body.copy(fontWeight = FontWeight.Medium),
    labelMedium = label,
    labelSmall = label.copy(fontSize = 11.sp, lineHeight = 14.sp)
)

val DefaultBirikimTypography = BirikimTypography()
val Typography = DefaultBirikimTypography.toMaterialTypography()