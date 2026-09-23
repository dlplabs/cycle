package br.com.dlpsystems.cycle.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import br.com.dlpsystems.cycle.R
import br.com.dlpsystems.cycle.core.designsystem.DeepPlum
import br.com.dlpsystems.cycle.core.designsystem.SurfaceCard
import br.com.dlpsystems.cycle.data.local.UserPreferencesDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class CoachMarkViewModel @Inject constructor(
    private val preferences: UserPreferencesDataSource,
) : ViewModel() {
    val seen = preferences.seenCoachMarks
        .map<Set<String>, Set<String>?> { it }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            null,
        )

    fun markSeen(key: String) {
        viewModelScope.launch { preferences.markCoachSeen(key) }
    }
}

class CoachMarkState internal constructor(
    val visible: Boolean,
    val bounds: Rect?,
    internal val onRoot: (LayoutCoordinates) -> Unit,
    internal val onTarget: (LayoutCoordinates) -> Unit,
    val dismiss: () -> Unit,
)

@Composable
fun rememberCoachMark(key: String): CoachMarkState {
    val viewModel: CoachMarkViewModel = hiltViewModel()
    val seen by viewModel.seen.collectAsStateWithLifecycle()
    var root by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var bounds by remember { mutableStateOf<Rect?>(null) }
    val loaded = seen
    return CoachMarkState(
        visible = loaded != null && key !in loaded,
        bounds = bounds,
        onRoot = { root = it },
        onTarget = { target ->
            val base = root
            if (base != null && base.isAttached && target.isAttached) {
                bounds = base.localBoundingBoxOf(target)
            }
        },
        dismiss = { viewModel.markSeen(key) },
    )
}

fun Modifier.coachRoot(state: CoachMarkState): Modifier =
    onGloballyPositioned(state.onRoot)

fun Modifier.coachTarget(state: CoachMarkState): Modifier =
    onGloballyPositioned(state.onTarget)

@Composable
fun CoachMarkOverlay(
    state: CoachMarkState,
    message: String,
) {
    val bounds = state.bounds
    if (!state.visible || bounds == null) return
    val density = LocalDensity.current
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(state.visible) {
                detectTapGestures { state.dismiss() }
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val pad = 8.dp.toPx()
            val radius = 22.dp.toPx()
            val hole = Rect(
                left = bounds.left - pad,
                top = bounds.top - pad,
                right = bounds.right + pad,
                bottom = bounds.bottom + pad,
            )
            val path = Path().apply {
                fillType = PathFillType.EvenOdd
                addRect(Rect(0f, 0f, size.width, size.height))
                addRoundRect(RoundRect(hole, CornerRadius(radius, radius)))
            }
            drawPath(path, Color.Black.copy(alpha = 0.62f))
            drawRoundRect(
                color = Color.White.copy(alpha = 0.95f),
                topLeft = hole.topLeft,
                size = hole.size,
                cornerRadius = CornerRadius(radius, radius),
                style = Stroke(width = 2.dp.toPx()),
            )
        }

        var bubbleSize by remember { mutableStateOf(IntSize.Zero) }
        val margin = 16.dp
        val gap = 8.dp
        val bubbleWidth = with(density) { bubbleSize.width.toDp() }
        val bubbleHeight = with(density) { bubbleSize.height.toDp() }
        val maxLeft = (maxWidth - bubbleWidth - margin).coerceAtLeast(margin)
        val left = with(density) { bounds.center.x.toDp() - bubbleWidth / 2 }
            .coerceIn(margin, maxLeft)
        val placeAbove = with(density) { bounds.top.toDp() } > bubbleHeight + gap + margin
        val maxTop = (maxHeight - bubbleHeight - margin).coerceAtLeast(margin)
        val top = (
            if (placeAbove) {
                with(density) { bounds.top.toDp() } - gap - bubbleHeight
            } else {
                with(density) { bounds.bottom.toDp() } + gap
            }
            ).coerceIn(margin, maxTop)
        val tailWidth = 22.dp
        val tailShift = (
            with(density) { bounds.center.x.toDp() } - left - tailWidth / 2
            ).coerceIn(18.dp, (bubbleWidth - tailWidth - 18.dp).coerceAtLeast(18.dp))

        Column(
            modifier = Modifier
                .offset {
                    IntOffset(
                        with(density) { left.toPx() }.roundToInt(),
                        with(density) { top.toPx() }.roundToInt(),
                    )
                }
                .widthIn(max = 300.dp)
                .onSizeChanged { bubbleSize = it },
        ) {
            if (!placeAbove) {
                Tail(pointingUp = true, shift = tailShift)
            }
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = SurfaceCard,
                modifier = Modifier.shadow(8.dp, RoundedCornerShape(22.dp)),
            ) {
                Column(modifier = Modifier.padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 4.dp)) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = DeepPlum,
                    )
                    TextButton(onClick = state.dismiss) {
                        Text(
                            text = stringResource(R.string.coach_dismiss),
                            color = DeepPlum,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            if (placeAbove) {
                Tail(pointingUp = false, shift = tailShift)
            }
        }
    }
}

@Composable
private fun Tail(pointingUp: Boolean, shift: androidx.compose.ui.unit.Dp) {
    Canvas(
        modifier = Modifier
            .padding(start = shift)
            .size(width = 22.dp, height = 12.dp),
    ) {
        val path = Path().apply {
            if (pointingUp) {
                moveTo(size.width / 2f, 0f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
            } else {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2f, size.height)
            }
            close()
        }
        drawPath(path, SurfaceCard)
    }
}
