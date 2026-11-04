package com.rslsoft.birikim.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.rslsoft.birikim.R
import com.rslsoft.birikim.ui.navigation.Screen
import com.rslsoft.birikim.ui.theme.BirikimTheme

/**
 * HarmonyOS tarzı alt navigasyon barı.
 * - Ekranın altında yüzen
 * - Yuvarlak köşeli
 * - Yarı saydam (blur background simülasyonu)
 * - Seçili sekme mavi pill zeminle vurgulanır
 * - Ripple efekti yok, scale/opacity geri bildirimi
 */
@Composable
fun HBottomBar(
    selectedScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomBarItem(Screen.Home, stringResource(R.string.nav_home), Icons.Outlined.Home),
        BottomBarItem(Screen.Calculator, stringResource(R.string.nav_calculator), Icons.Outlined.Calculate),
        BottomBarItem(Screen.History, stringResource(R.string.nav_history), Icons.Outlined.History),
        BottomBarItem(Screen.Goals, stringResource(R.string.nav_goals), Icons.Outlined.Flag)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    BirikimTheme.colors.surface.copy(alpha = 0.92f)
                )
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = item.screen == selectedScreen
                BottomBarTab(
                    item = item,
                    isSelected = isSelected,
                    onClick = { onScreenSelected(item.screen) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private data class BottomBarItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

@Composable
private fun BottomBarTab(
    item: BottomBarItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) BirikimTheme.colors.primarySubtle else Color.Transparent,
        animationSpec = spring(dampingRatio = 0.8f),
        label = "tabBg"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) BirikimTheme.colors.primary else BirikimTheme.colors.textTertiary,
        animationSpec = spring(dampingRatio = 0.8f),
        label = "tabIcon"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) BirikimTheme.colors.primary else BirikimTheme.colors.textTertiary,
        animationSpec = spring(dampingRatio = 0.8f),
        label = "tabText"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.9f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
        label = "tabScale"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 8.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .semantics {
                contentDescription = item.label
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = item.label,
                style = BirikimTheme.typography.label,
                color = textColor
            )
        }
    }
}
