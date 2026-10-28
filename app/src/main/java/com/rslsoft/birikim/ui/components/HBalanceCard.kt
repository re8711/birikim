package com.rslsoft.birikim.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rslsoft.birikim.domain.CurrencyUtils
import com.rslsoft.birikim.ui.theme.BirikimTheme

/**
 * Büyük bakiye kartı (mavi gradyan, beyaz yazı).
 * Bakiye gizle/göster: blur + fade animasyonu.
 * Basıldığında da toggle olur.
 */
@Composable
fun HBalanceCard(
    balanceKurus: Long,
    isHidden: Boolean,
    onToggleVisibility: () -> Unit,
    onUpdateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
        label = "balanceScale"
    )
    val blurRadius by animateFloatAsState(
        targetValue = if (isHidden) 20f else 0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow),
        label = "balanceBlur"
    )

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0A59F7),
            Color(0xFF4C8DFF)
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(24.dp))
            .background(gradientBrush)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onToggleVisibility() }
                )
            }
            .padding(BirikimTheme.dimens.cardInnerPadding)
            .semantics {
                contentDescription = if (isHidden) "Bakiye gizli" else "Bakiye: ${CurrencyUtils.formatCurrency(balanceKurus)}"
            }
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mevcut bakiye",
                    style = BirikimTheme.typography.body,
                    color = Color.White.copy(alpha = 0.8f)
                )

                IconButton(
                    onClick = onToggleVisibility,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isHidden) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (isHidden) "Bakiyeyi göster" else "Bakiyeyi gizle",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box {
                Text(
                    text = if (isHidden) "••••••" else CurrencyUtils.formatCurrency(balanceKurus),
                    style = BirikimTheme.typography.balanceDisplay.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    modifier = Modifier.graphicsLayer {
                        alpha = if (isHidden) 0.5f else 1f
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                HPillButton(
                    text = "Güncelle",
                    onClick = onUpdateClick,
                    backgroundColor = Color.White.copy(alpha = 0.2f),
                    textColor = Color.White,
                    contentDescription = "Bakiyeyi güncelle"
                )
            }
        }
    }
}
