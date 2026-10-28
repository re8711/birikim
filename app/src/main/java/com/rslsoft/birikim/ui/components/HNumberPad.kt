package com.rslsoft.birikim.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rslsoft.birikim.ui.theme.BirikimTheme

/**
 * Özel tasarımlı sayısal klavye (sistem klavyesi yerine).
 * HarmonyOS tarzı, basıldığında scale geri bildirimi.
 */
@Composable
fun HNumberPad(
    onDigit: (String) -> Unit,
    onDecimal: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    showOperators: Boolean = false,
    onOperator: ((String) -> Unit)? = null
) {
    val keys = if (showOperators) {
        listOf(
            listOf("C", "(", ")", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("%", "0", ",", "⌫")
        )
    } else {
        listOf(
            listOf("7", "8", "9"),
            listOf("4", "5", "6"),
            listOf("1", "2", "3"),
            listOf("C", "0", "⌫")
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    NumberPadKey(
                        key = key,
                        modifier = Modifier.weight(1f),
                        isOperator = key in listOf("+", "-", "×", "÷", "%", "(", ")"),
                        isAction = key in listOf("C", "⌫"),
                        onClick = {
                            when (key) {
                                "⌫" -> onBackspace()
                                "C" -> onClear()
                                "," -> onDecimal()
                                "+", "-", "×", "÷", "%", "(", ")" -> onOperator?.invoke(key)
                                else -> onDigit(key)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NumberPadKey(
    key: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isOperator: Boolean = false,
    isAction: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium),
        label = "keyScale"
    )

    val bgColor = when {
        isOperator -> BirikimTheme.colors.primarySubtle
        isAction -> BirikimTheme.colors.background
        else -> BirikimTheme.colors.surface
    }

    val textColor = when {
        isOperator -> BirikimTheme.colors.primary
        isAction -> BirikimTheme.colors.textSecondary
        else -> BirikimTheme.colors.textPrimary
    }

    Box(
        modifier = modifier
            .aspectRatio(1.6f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            }
            .semantics {
                contentDescription = when (key) {
                    "⌫" -> "Sil"
                    "C" -> "Temizle"
                    "," -> "Virgül"
                    "×" -> "Çarpı"
                    "÷" -> "Bölü"
                    else -> key
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = key,
            style = BirikimTheme.typography.titleLarge.copy(
                fontSize = if (isOperator || isAction) 20.sp else 24.sp
            ),
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}
