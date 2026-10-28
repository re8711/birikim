package com.rslsoft.birikim.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rslsoft.birikim.domain.CurrencyUtils
import com.rslsoft.birikim.ui.theme.BirikimTheme

data class DonutSegment(
    val label: String,
    val value: Long,
    val color: Color
)

/**
 * Özel çizilmiş donut (halka) grafik bileşeni.
 * Canvas ile çizilir, açılış animasyonlu.
 */
@Composable
fun HDonutChart(
    segments: List<DonutSegment>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 32.dp,
    centerText: String = "",
    centerSubText: String = ""
) {
    val total = segments.sumOf { it.value }.coerceAtLeast(1)
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(segments) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val strokePx = strokeWidth.toPx()
            val diameter = minOf(size.width, size.height) - strokePx
            val topLeft = Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)

            var startAngle = -90f
            val totalSweep = 360f * animationProgress.value

            segments.forEach { segment ->
                val sweepAngle = (segment.value.toFloat() / total.toFloat()) * totalSweep
                drawArc(
                    color = segment.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
                startAngle += sweepAngle
            }
        }

        // Ortadaki metin
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (centerSubText.isNotEmpty()) {
                Text(
                    text = centerSubText,
                    style = BirikimTheme.typography.label,
                    color = BirikimTheme.colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
            if (centerText.isNotEmpty()) {
                Text(
                    text = centerText,
                    style = BirikimTheme.typography.titleLarge,
                    color = BirikimTheme.colors.textPrimary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Dairesel ilerleme göstergesi (Hedefler için).
 */
@Composable
fun HCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 8.dp,
    trackColor: Color = BirikimTheme.colors.primarySubtle,
    progressColor: Color = BirikimTheme.colors.primary,
    content: @Composable () -> Unit = {}
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = spring(
                dampingRatio = 0.8f,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val diameter = minOf(size.width, size.height) - strokePx
            val topLeft = Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)

            // Track
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Progress
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress.value,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        content()
    }
}

/**
 * Yatay ilerleme çubuğu (gelirin yüzde kaçı harcanıyor).
 */
@Composable
fun HProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
    trackColor: Color = BirikimTheme.colors.primarySubtle,
    progressColor: Color = BirikimTheme.colors.primary
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow)
        )
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val cornerRadiusPx = size.height / 2f

        // Track
        drawRoundRect(
            color = trackColor,
            size = size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadiusPx)
        )

        // Progress
        val progressWidth = size.width * animatedProgress.value
        if (progressWidth > 0) {
            drawRoundRect(
                color = progressColor,
                size = Size(progressWidth, size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadiusPx)
            )
        }
    }
}
