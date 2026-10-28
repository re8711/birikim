package com.rslsoft.birikim.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ============================================================================
// Light Theme Palette (HarmonyOS 4/5 Inspired)
// ============================================================================
val BackgroundLight = Color(0xFFF1F3F5)
val CardLight = Color(0xFFFFFFFF)
val ElevatedCardLight = Color(0xFFFFFFFF)

val PrimaryBlueLight = Color(0xFF0A59F7)
val PrimaryPressedLight = Color(0xFF0846C4)
val PrimarySubtleLight = Color(0x1A0A59F7) // 10% opacity

val IncomeGreenLight = Color(0xFF64BB5C)
val ExpenseRedLight = Color(0xFFE84026)
val WarningOrangeLight = Color(0xFFED6F21)

val TextPrimaryLight = Color(0xFF182431)
val TextSecondaryLight = Color(0x99182431) // 60% opacity
val TextTertiaryLight = Color(0x66182431)  // 40% opacity

val DividerLight = Color(0x0D000000)      // Black at 5% opacity

// ============================================================================
// Dark Theme Palette (HarmonyOS 4/5 Inspired)
// ============================================================================
val BackgroundDark = Color(0xFF000000)
val CardDark = Color(0xFF1C1C1E)
val ElevatedCardDark = Color(0xFF2C2C2E)

val PrimaryBlueDark = Color(0xFF317AF7)
val PrimaryPressedDark = Color(0xFF2060CE)
val PrimarySubtleDark = Color(0x26317AF7) // ~15% opacity

val IncomeGreenDark = Color(0xFF5BA854)
val ExpenseRedDark = Color(0xFFFF6E5C)
val WarningOrangeDark = Color(0xFFED6F21)

val TextPrimaryDark = Color(0xFFFFFFFF)
val TextSecondaryDark = Color(0x99FFFFFF) // 60% opacity
val TextTertiaryDark = Color(0x66FFFFFF)  // 40% opacity

val DividerDark = Color(0x1AFFFFFF)       // White at 10% opacity

// ============================================================================
// Birikim Design System Colors
// ============================================================================
@Immutable
data class BirikimColors(
    val primary: Color,
    val primaryPressed: Color,
    val primarySubtle: Color,
    val income: Color,
    val expense: Color,
    val warning: Color,
    val background: Color,
    val card: Color,
    val cardElevated: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val divider: Color,
    val onPrimary: Color = Color.White,
    val isDark: Boolean
) {
    val surface: Color get() = card
    val elevatedCard: Color get() = cardElevated
    val onBackground: Color get() = textPrimary
    val onSurface: Color get() = textPrimary
}

val LightBirikimColors = BirikimColors(
    primary = PrimaryBlueLight,
    primaryPressed = PrimaryPressedLight,
    primarySubtle = PrimarySubtleLight,
    income = IncomeGreenLight,
    expense = ExpenseRedLight,
    warning = WarningOrangeLight,
    background = BackgroundLight,
    card = CardLight,
    cardElevated = ElevatedCardLight,
    textPrimary = TextPrimaryLight,
    textSecondary = TextSecondaryLight,
    textTertiary = TextTertiaryLight,
    divider = DividerLight,
    onPrimary = Color.White,
    isDark = false
)

val DarkBirikimColors = BirikimColors(
    primary = PrimaryBlueDark,
    primaryPressed = PrimaryPressedDark,
    primarySubtle = PrimarySubtleDark,
    income = IncomeGreenDark,
    expense = ExpenseRedDark,
    warning = WarningOrangeDark,
    background = BackgroundDark,
    card = CardDark,
    cardElevated = ElevatedCardDark,
    textPrimary = TextPrimaryDark,
    textSecondary = TextSecondaryDark,
    textTertiary = TextTertiaryDark,
    divider = DividerDark,
    onPrimary = Color.White,
    isDark = true
)

val LocalBirikimColors = staticCompositionLocalOf { LightBirikimColors }