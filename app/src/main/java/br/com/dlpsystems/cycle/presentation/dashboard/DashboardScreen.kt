package br.com.dlpsystems.cycle.presentation.dashboard

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.dlpsystems.cycle.R
import br.com.dlpsystems.cycle.core.designsystem.CycleTheme
import br.com.dlpsystems.cycle.core.designsystem.HeaderSage
import br.com.dlpsystems.cycle.core.designsystem.OffWhiteBackground
import br.com.dlpsystems.cycle.core.designsystem.PlayfairDisplay
import br.com.dlpsystems.cycle.core.designsystem.PrimaryButton
import br.com.dlpsystems.cycle.core.designsystem.SurfaceCard
import br.com.dlpsystems.cycle.core.designsystem.TextPrimary
import br.com.dlpsystems.cycle.core.notification.labelRes
import br.com.dlpsystems.cycle.domain.model.CyclePhase
import br.com.dlpsystems.cycle.domain.model.PhaseStatus
import br.com.dlpsystems.cycle.domain.model.WellnessPillar
import br.com.dlpsystems.cycle.presentation.components.CoachMarkOverlay
import br.com.dlpsystems.cycle.presentation.components.CycleWheel
import br.com.dlpsystems.cycle.presentation.components.PhaseRecommendationCard
import br.com.dlpsystems.cycle.presentation.components.coachRoot
import br.com.dlpsystems.cycle.presentation.components.coachTarget
import br.com.dlpsystems.cycle.presentation.components.rememberCoachMark
import br.com.dlpsystems.cycle.presentation.tracking.LogSymptomBottomSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun DashboardScreen(
    startPeriodOnOpen: Boolean,
    onStartPeriodConsumed: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showLog by remember { mutableStateOf(false) }
    var showCare by remember { mutableStateOf(false) }
    LaunchedEffect(startPeriodOnOpen) {
        if (startPeriodOnOpen) {
            viewModel.startPeriodToday()
            onStartPeriodConsumed()
        }
    }
    val phase = state.result?.phase ?: CyclePhase.LUTEAL
    val result = state.result
    val needsFirstPeriod = result == null || result.status == PhaseStatus.NO_CYCLE
    val coach = rememberCoachMark("today")
    CycleTheme(phase = phase) {
        if (showCare && result?.phase != null) {
            PhaseCareScreen(
                state = state,
                onBack = { showCare = false },
                onOpenSource = viewModel::onOpenSource,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(HeaderSage)
                    .coachRoot(coach),
            ) {
            Column(modifier = Modifier.fillMaxSize()) {
                HomeHeader(name = state.userName, photoUrl = state.photoUrl)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    color = OffWhiteBackground,
                ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    val wheel = minOf(maxWidth, maxHeight, 280.dp)
                    AnimatedContent(targetState = result?.phase, label = "phaseCrossfade") { current ->
                        val phaseName = current?.let { stringResource(it.labelRes()) }
                            ?: stringResource(R.string.phase_unknown)
                        CycleWheel(
                            cycleDay = result?.cycleDay ?: 0,
                            cycleLength = result?.cycleLength ?: 28,
                            phase = current,
                            phaseName = phaseName,
                            daysRemaining = result?.daysUntilNextPeriod ?: 0,
                            segments = result?.segments.orEmpty(),
                            wheelSize = wheel,
                        )
                    }
                }
                Text(
                    text = when (result?.status) {
                        PhaseStatus.EXTENDED -> result.message.orEmpty()
                        PhaseStatus.NO_CYCLE, null -> stringResource(R.string.no_cycle_hint)
                        PhaseStatus.IN_PHASE -> stringResource(
                            R.string.days_until_period,
                            result.daysUntilNextPeriod,
                        )
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
                state.insight?.physiology?.takeIf { it.isNotBlank() && !needsFirstPeriod }?.let { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                PrimaryButton(
                    text = stringResource(
                        if (needsFirstPeriod) R.string.start_first_period else R.string.register_today,
                    ),
                    onClick = {
                        if (needsFirstPeriod) viewModel.startPeriodToday() else showLog = true
                    },
                    modifier = Modifier.coachTarget(coach),
                )
                if (!needsFirstPeriod && result?.phase != null) {
                    TextButton(onClick = { showCare = true }) {
                        Text(stringResource(R.string.see_phase_care))
                    }
                }
                state.errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                }
            }
                }
            }
            CoachMarkOverlay(
                state = coach,
                message = stringResource(
                    if (needsFirstPeriod) R.string.coach_today_start else R.string.coach_today_log,
                ),
            )
            }
        }
        if (showLog) {
            LogSymptomBottomSheet(onDismiss = { showLog = false })
        }
    }
}

@Composable
private fun HomeHeader(name: String, photoUrl: String?) {
    val firstName = name.trim().substringBefore(' ').ifBlank { name.trim() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 24.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = if (firstName.isBlank()) {
                stringResource(R.string.greeting_anonymous)
            } else {
                stringResource(R.string.greeting, firstName)
            },
            style = MaterialTheme.typography.headlineLarge.copy(
                fontFamily = PlayfairDisplay,
                fontWeight = FontWeight.Normal,
                fontSize = 32.sp,
                color = TextPrimary,
            ),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        ProfileAvatar(name = firstName.ifBlank { name }, photoUrl = photoUrl)
    }
}

@Composable
private fun ProfileAvatar(name: String, photoUrl: String?) {
    var photo by remember(photoUrl) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(photoUrl) {
        photo = photoUrl?.let { loadProfilePhoto(it) }
    }
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(SurfaceCard)
            .border(2.dp, SurfaceCard, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        val bitmap = photo
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = stringResource(R.string.cd_profile_photo, name),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(
                text = name.firstOrNull()?.uppercaseChar()?.toString().orEmpty(),
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
        }
    }
}

private suspend fun loadProfilePhoto(url: String): ImageBitmap? = withContext(Dispatchers.IO) {
    runCatching {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 4_000
        connection.readTimeout = 4_000
        connection.inputStream.use { stream ->
            BitmapFactory.decodeStream(stream)?.asImageBitmap()
        }
    }.getOrNull()
}

@Composable
private fun PhaseCareScreen(
    state: DashboardUiState,
    onBack: () -> Unit,
    onOpenSource: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBack) {
            Text(stringResource(R.string.back))
        }
        Text(
            text = stringResource(R.string.pillars_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        state.insight?.pillars?.forEach { pillar ->
            PhaseRecommendationCard(
                title = stringResource(pillar.pillar.labelRes()),
                iconRes = pillar.pillar.iconRes(),
                guidance = pillar,
                onOpenSource = { onOpenSource() },
            )
        }
    }
}

private fun WellnessPillar.iconRes(): Int = when (this) {
    WellnessPillar.NUTRITION -> R.drawable.ic_alimentacao
    WellnessPillar.EXERCISE -> R.drawable.ic_yoga
    WellnessPillar.SKIN -> R.drawable.ic_skincare
    WellnessPillar.MIND -> R.drawable.ic_lua_lavanda
}

private fun WellnessPillar.labelRes(): Int = when (this) {
    WellnessPillar.NUTRITION -> R.string.pillar_nutrition
    WellnessPillar.EXERCISE -> R.string.pillar_exercise
    WellnessPillar.SKIN -> R.string.pillar_skin
    WellnessPillar.MIND -> R.string.pillar_mind
}
