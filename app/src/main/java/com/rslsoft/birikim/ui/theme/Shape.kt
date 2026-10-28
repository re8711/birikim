package com.rslsoft.birikim.ui.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

// ============================================================================
// Birikim Shape System (HarmonyOS 4/5 Inspired Smooth Squircle/Rounded Corners)
// ============================================================================
@Immutable
data class BirikimShapes(
    val card: CornerBasedShape = RoundedCornerShape(24.dp),
    val button: CornerBasedShape = RoundedCornerShape(percent = 50),
    val bottomSheet: CornerBasedShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
    val input: CornerBasedShape = RoundedCornerShape(16.dp),
    val bottomBar: CornerBasedShape = RoundedCornerShape(28.dp)
)

val LocalBirikimShapes = staticCompositionLocalOf { BirikimShapes() }

/**
 * Maps Birikim custom shapes to Material3 Shapes for standard component integration.
 */
fun BirikimShapes.toMaterialShapes(): Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = input,
    large = card,
    extraLarge = bottomSheet
)
