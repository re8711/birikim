package com.rslsoft.birikim.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Accessor object for Birikim design system tokens.
 */
object BirikimTheme {
    val colors: BirikimColors
        @Composable
        @ReadOnlyComposable
        get() = LocalBirikimColors.current

    val typography: BirikimTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalBirikimTypography.current

    val shapes: BirikimShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalBirikimShapes.current

    val dimens: BirikimDimens
        @Composable
        @ReadOnlyComposable
        get() = LocalBirikimDimens.current
}

/**
 * HarmonyOS 4/5 inspired custom theme for Birikim application.
 * Dynamic color is intentionally omitted to maintain consistent brand aesthetics.
 */
@Composable
fun BirikimTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkBirikimColors else LightBirikimColors
    val typography = BirikimTypography()
    val shapes = BirikimShapes()
    val dimens = BirikimDimens()

    // Transparent status & navigation bar with light/dark appearance icons for edge-to-edge
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window
            window?.let { win ->
                val insetsController = WindowCompat.getInsetsController(win, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    // Map custom Birikim color system to Material3 ColorScheme for built-in components
    val materialColorScheme = if (darkTheme) {
        darkColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            primaryContainer = colors.primarySubtle,
            onPrimaryContainer = colors.primary,
            secondary = colors.primary,
            onSecondary = colors.onPrimary,
            background = colors.background,
            onBackground = colors.textPrimary,
            surface = colors.card,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.cardElevated,
            onSurfaceVariant = colors.textSecondary,
            outline = colors.divider,
            outlineVariant = colors.divider,
            error = colors.expense,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            primaryContainer = colors.primarySubtle,
            onPrimaryContainer = colors.primary,
            secondary = colors.primary,
            onSecondary = colors.onPrimary,
            background = colors.background,
            onBackground = colors.textPrimary,
            surface = colors.card,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.background,
            onSurfaceVariant = colors.textSecondary,
            outline = colors.divider,
            outlineVariant = colors.divider,
            error = colors.expense,
            onError = Color.White
        )
    }

    CompositionLocalProvider(
        LocalBirikimColors provides colors,
        LocalBirikimTypography provides typography,
        LocalBirikimShapes provides shapes,
        LocalBirikimDimens provides dimens
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = typography.toMaterialTypography(),
            shapes = shapes.toMaterialShapes(),
            content = content
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}