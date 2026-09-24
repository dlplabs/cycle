package br.com.dlpsystems.cycle.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.dlpsystems.cycle.R
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
    wheelSize: Dp = 280.dp,
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
    Box(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = description
        },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_moldura_dias),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(wheelSize)
                .graphicsLayer {
                    val shown = 0.92f + 0.08f * progress
                    scaleX = shown
                    scaleY = shown
                    alpha = progress
                }
                .clearAndSetSemantics { },
        )
        Canvas(
            modifier = Modifier
                .size(wheelSize)
                .clearAndSetSemantics { },
        ) {
            val inset = 18.dp.toPx()
            val arcSize = size.width - inset * 2
            if (cycleDay > 0 && cycleLength > 0) {
                val markerDay = cycleDay.coerceAtMost(cycleLength)
                val angle = Math.toRadians((-90.0 + (markerDay - 0.5) / cycleLength * 360.0 * progress))
                val radius = arcSize / 2f
                val centerPoint = Offset(
                    x = center.x + radius * cos(angle).toFloat(),
                    y = center.y + radius * sin(angle).toFloat(),
                )
                val currentPhase = phase ?: CyclePhase.LUTEAL
                val markerColor = PhaseColors.forPhase(currentPhase, false).primary
                drawCircle(color = DeepPlum, radius = 12.dp.toPx() * scale, center = centerPoint)
                drawCircle(
                    color = markerColor,
                    radius = 6.dp.toPx(),
                    center = centerPoint,
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset(y = wheelSize * 0.045f)
                .clearAndSetSemantics { },
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
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
