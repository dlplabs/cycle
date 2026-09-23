package br.com.dlpsystems.cycle.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.dlpsystems.cycle.core.accessibility.SemanticsUtils
import br.com.dlpsystems.cycle.core.designsystem.DeepPlum
import br.com.dlpsystems.cycle.core.designsystem.PhaseColors
import br.com.dlpsystems.cycle.domain.model.CyclePhase
import br.com.dlpsystems.cycle.domain.usecase.PhaseSegment
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CycleWheel(
    cycleDay: Int,
    cycleLength: Int,
    phase: CyclePhase?,
    phaseName: String,
    daysRemaining: Int,
    segments: List<PhaseSegment>,
    modifier: Modifier = Modifier,
) {
    val description = SemanticsUtils.cycleWheelDescription(
        day = cycleDay.coerceAtLeast(0),
        totalDays = cycleLength.coerceAtLeast(1),
        phaseName = phaseName,
        daysRemaining = daysRemaining,
    )
    var opened by remember { mutableStateOf(false) }
    LaunchedEffect(cycleLength) { opened = true }
    val progress by animateFloatAsState(
        targetValue = if (opened) 1f else 0f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "wheelOpen",
    )
    val pulse = rememberInfiniteTransition(label = "markerPulse")
    val scale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "pulseScale",
    )
    val dark = isSystemInDarkTheme()
    val trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    Box(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = description
        },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .size(280.dp)
                .clearAndSetSemantics { },
        ) {
            val strokeWidth = 24.dp.toPx()
            val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            val inset = 18.dp.toPx()
            val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
            val topLeft = Offset(inset, inset)

            // Fundo orgânico suave (Trilha)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )

            // Desenhar os arcos orgânicos de cada fase com pontas arredondadas (StrokeCap.Round)
            if (cycleLength > 0) {
                segments.forEach { segment ->
                    val rawStart = -90f + (segment.startDay - 1f) / cycleLength * 360f * progress
                    val rawSweep = (segment.endDay - segment.startDay + 1f) / cycleLength * 360f * progress
                    // Pequeno ajuste para sobrepor e suavizar cantos arredondados
                    val gapAdjustment = 2f
                    val sweep = (rawSweep - gapAdjustment).coerceAtLeast(1f)

                    drawArc(
                        color = PhaseColors.forPhase(segment.phase, dark).primary,
                        startAngle = rawStart + (gapAdjustment / 2f),
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = stroke,
                    )
                }
            }

            // Indicador / Marcador do Dia Atual
            if (cycleDay > 0 && cycleLength > 0) {
                val markerDay = cycleDay.coerceAtMost(cycleLength)
                val angle = Math.toRadians((-90.0 + (markerDay - 0.5) / cycleLength * 360.0 * progress))
                val radius = arcSize.minDimension / 2f
                val centerPoint = Offset(
                    x = center.x + radius * cos(angle).toFloat(),
                    y = center.y + radius * sin(angle).toFloat(),
                )
                val currentPhase = phase ?: CyclePhase.LUTEAL
                val markerColor = PhaseColors.forPhase(currentPhase, dark).primary
                drawCircle(color = DeepPlum, radius = 12.dp.toPx() * scale, center = centerPoint)
                drawCircle(
                    color = markerColor,
                    radius = 6.dp.toPx(),
                    center = centerPoint,
                )
            }
        }

        // Centro do Círculo com Tipografia Serifada (Playfair Display) e Cor DeepPlum
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clearAndSetSemantics { },
        ) {
            Text(
                text = cycleDay.coerceAtLeast(0).toString(),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = DeepPlum,
            )
            Text(
                text = phaseName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                ),
                color = DeepPlum,
            )
        }
    }
}
