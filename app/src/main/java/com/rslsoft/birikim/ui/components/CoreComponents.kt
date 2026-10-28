package com.rslsoft.birikim.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rslsoft.birikim.ui.theme.BirikimTheme

/**
 * HarmonyOS tarzı kart bileşeni. Gölge yok veya çok yumuşak.
 * Basıldığında %97'ye küçülme animasyonu.
 */
@Composable
fun HCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color = BirikimTheme.colors.surface,
    cornerRadius: Dp = 24.dp,
    contentPadding: PaddingValues = PaddingValues(BirikimTheme.dimens.cardInnerPadding),
    contentDescription: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "cardScale"
    )

    val cardModifier = modifier
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .shadow(
            elevation = 2.dp,
            shape = RoundedCornerShape(cornerRadius),
            ambientColor = Color.Black.copy(alpha = 0.06f),
            spotColor = Color.Black.copy(alpha = 0.06f)
        )
        .clip(RoundedCornerShape(cornerRadius))
        .background(backgroundColor)
        .then(
            if (onClick != null) {
                Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        },
                        onTap = { onClick() }
                    )
                }
            } else Modifier
        )
        .then(
            if (contentDescription != null) {
                Modifier.semantics { this.contentDescription = contentDescription }
            } else Modifier
        )

    Column(
        modifier = cardModifier.padding(contentPadding),
        content = content
    )
}

/**
 * Pill şeklinde buton (tam yuvarlak köşeler).
 * Ripple yerine scale/opacity geri bildirimi.
 */
@Composable
fun HPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = BirikimTheme.colors.primary,
    textColor: Color = Color.White,
    enabled: Boolean = true,
    contentDescription: String? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
        label = "pillScale"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isPressed) BirikimTheme.colors.primaryPressed else backgroundColor,
        animationSpec = spring(dampingRatio = 0.8f),
        label = "pillColor"
    )
    val alpha = if (enabled) 1f else 0.5f

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .clip(RoundedCornerShape(50))
            .background(if (enabled) bgColor else backgroundColor.copy(alpha = 0.5f))
            .then(
                if (enabled) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                tryAwaitRelease()
                                isPressed = false
                            },
                            onTap = { onClick() }
                        )
                    }
                } else Modifier
            )
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else Modifier
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = BirikimTheme.typography.cardTitle,
            color = textColor
        )
    }
}

/**
 * HarmonyOS tarzı segment kontrolü (Aylık / Yıllık seçici).
 * Seçili olan beyaz pill ile vurgulanır.
 */
@Composable
fun HSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onSelectionChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val containerColor = BirikimTheme.colors.primarySubtle

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .padding(4.dp)
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else Modifier
            )
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            items.forEachIndexed { index, label ->
                val isSelected = index == selectedIndex
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) BirikimTheme.colors.surface else Color.Transparent,
                    animationSpec = spring(dampingRatio = 0.8f),
                    label = "segmentBg"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) BirikimTheme.colors.textPrimary else BirikimTheme.colors.textSecondary,
                    animationSpec = spring(dampingRatio = 0.8f),
                    label = "segmentText"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(bgColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelectionChanged(index) }
                        )
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = BirikimTheme.typography.cardTitle,
                        color = textColor
                    )
                }
            }
        }
    }
}
