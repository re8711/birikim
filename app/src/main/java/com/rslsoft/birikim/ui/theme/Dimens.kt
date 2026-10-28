package com.rslsoft.birikim.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ============================================================================
// Birikim Dimension & Spacing System
// ============================================================================
@Immutable
data class BirikimDimens(
    val screenPadding: Dp = 16.dp,
    val cardSpacing: Dp = 12.dp,
    val cardInnerPadding: Dp = 20.dp,
    val dividerThickness: Dp = 0.5.dp,
    val minTouchTarget: Dp = 48.dp,
    val iconSize: Dp = 24.dp,
    val bottomBarHeight: Dp = 64.dp
)

val LocalBirikimDimens = staticCompositionLocalOf { BirikimDimens() }

// Top-level constants for direct access
val ScreenPadding: Dp = 16.dp
val CardSpacing: Dp = 12.dp
val CardInnerPadding: Dp = 20.dp
val DividerThickness: Dp = 0.5.dp
val MinTouchTarget: Dp = 48.dp
val IconSize: Dp = 24.dp
val BottomBarHeight: Dp = 64.dp
