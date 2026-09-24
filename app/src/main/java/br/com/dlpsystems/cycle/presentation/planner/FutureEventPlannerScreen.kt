package br.com.dlpsystems.cycle.presentation.planner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import br.com.dlpsystems.cycle.R
import br.com.dlpsystems.cycle.core.designsystem.AppCard
import br.com.dlpsystems.cycle.core.designsystem.CycleCard
import br.com.dlpsystems.cycle.core.designsystem.DeepPlum
import br.com.dlpsystems.cycle.core.designsystem.FollicularSage
import br.com.dlpsystems.cycle.core.designsystem.HeaderSage
import br.com.dlpsystems.cycle.core.designsystem.LutealLavender
import br.com.dlpsystems.cycle.core.designsystem.MenstrualTerracotta
import br.com.dlpsystems.cycle.core.designsystem.OffWhiteBackground
import br.com.dlpsystems.cycle.core.designsystem.OvulatoryPeach
import br.com.dlpsystems.cycle.core.designsystem.PrimaryButton
import br.com.dlpsystems.cycle.core.notification.labelRes
import br.com.dlpsystems.cycle.domain.model.CyclePhase
import br.com.dlpsystems.cycle.domain.repository.BillingRepository
import br.com.dlpsystems.cycle.domain.repository.CycleRepository
import br.com.dlpsystems.cycle.domain.repository.UserRepository
import br.com.dlpsystems.cycle.domain.usecase.GetPhaseInsightsUseCase
import br.com.dlpsystems.cycle.domain.usecase.ProjectFuturePhaseUseCase
import br.com.dlpsystems.cycle.presentation.components.AdBannerContainer
import br.com.dlpsystems.cycle.presentation.components.ScreenHeader
import br.com.dlpsystems.cycle.presentation.components.CoachMarkOverlay
import br.com.dlpsystems.cycle.presentation.components.coachRoot
import br.com.dlpsystems.cycle.presentation.components.coachTarget
import br.com.dlpsystems.cycle.presentation.components.rememberCoachMark
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class PlannerUiState(
    val premium: Boolean = false,
    val phase: CyclePhase? = null,
    val cycleDay: Int = 0,
    val blocked: Boolean = false,
    val guidance: String = "",
    val physiology: String = "",
    val targetDate: LocalDate? = null,
)

@HiltViewModel
class PlannerViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
    private val cycleRepository: CycleRepository,
    private val userRepository: UserRepository,
    private val projectFuturePhase: ProjectFuturePhaseUseCase,
    private val insights: GetPhaseInsightsUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(PlannerUiState())
    val state = _state.asStateFlow()

    init {
        billingRepository.connect()
        viewModelScope.launch {
            billingRepository.isPremiumUser.collect { premium ->
                _state.value = _state.value.copy(premium = premium)
            }
        }
    }

    fun onDate(epochMillis: Long?) {
        if (epochMillis == null) return
        val target = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        viewModelScope.launch {
            val premium = billingRepository.isPremiumUser.value
            val profile = userRepository.getProfile()
            val cycles = cycleRepository.getCycles()
            val open = cycles.lastOrNull { it.endDate == null }
            val length = open?.cycleLength ?: profile?.averageCycleDays ?: 28
            val period = open?.periodLength ?: profile?.averagePeriodDays ?: 5
            val start = open?.startDate ?: LocalDate.now()
            val horizon = if (premium) length * 6 else length
            val tooFar = target.isAfter(LocalDate.now().plusDays(horizon.toLong()))
            if (tooFar || target.isBefore(LocalDate.now())) {
                _state.value = _state.value.copy(blocked = true, phase = null, targetDate = target)
                return@launch
            }
            val projected = projectFuturePhase(start, target, length, period)
            val evidence = insights(projected.phase)
            val text = evidence.pillars.firstOrNull()?.guidance.orEmpty()
            _state.value = PlannerUiState(
                premium = premium,
                phase = projected.phase,
                cycleDay = projected.cycleDay,
                blocked = false,
                guidance = text,
                physiology = evidence.physiology,
                targetDate = target,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FutureEventPlannerScreen(
    onPaywall: () -> Unit,
    onAccount: () -> Unit,
    viewModel: PlannerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val picker = rememberDatePickerState()
    val coach = rememberCoachMark("planner")
    Box(
        modifier = Modifier
            .fillMaxSize()
            .coachRoot(coach),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(
                title = stringResource(R.string.planner_title),
                onAccount = onAccount,
                titleModifier = Modifier.coachTarget(coach),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.planner_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = br.com.dlpsystems.cycle.core.designsystem.DeepPlum.copy(alpha = 0.8f),
                )

                DatePicker(
                    state = picker,
                    modifier = Modifier.padding(vertical = 4.dp),
                )

                PrimaryButton(
                    text = stringResource(R.string.planner_calculate),
                    onClick = { viewModel.onDate(picker.selectedDateMillis) },
                )

                when {
                    state.blocked && !state.premium -> {
                        br.com.dlpsystems.cycle.core.designsystem.AppCard {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.planner_locked_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = br.com.dlpsystems.cycle.core.designsystem.DeepPlum,
                                )
                                Text(
                                    text = stringResource(R.string.planner_locked),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                PrimaryButton(
                                    text = stringResource(R.string.open_paywall),
                                    onClick = onPaywall,
                                )
                            }
                        }
                    }
                    state.blocked -> {
                        br.com.dlpsystems.cycle.core.designsystem.AppCard {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = stringResource(R.string.planner_past),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = br.com.dlpsystems.cycle.core.designsystem.DeepPlum,
                                )
                            }
                        }
                    }
                    state.phase != null -> {
                        ProjectedPhaseCard(state = state)
                        AdBannerContainer(isPremium = state.premium)
                    }
                }
            }
        }
        CoachMarkOverlay(
            state = coach,
            message = stringResource(R.string.coach_planner),
        )
    }
}

@Composable
private fun ProjectedPhaseCard(
    state: PlannerUiState,
    modifier: Modifier = Modifier,
) {
    val phase = state.phase ?: return
    val formattedDate = state.targetDate?.let { date ->
        val formatter = java.time.format.DateTimeFormatter.ofPattern("d 'de' MMMM", java.util.Locale("pt", "BR"))
        date.format(formatter)
    }.orEmpty()

    val phaseColor = when (phase) {
        CyclePhase.MENSTRUAL -> MenstrualTerracotta
        CyclePhase.FOLLICULAR -> FollicularSage
        CyclePhase.OVULATORY -> OvulatoryPeach
        CyclePhase.LUTEAL -> LutealLavender
    }

    CycleCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header: Badge e Data
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = phaseColor.copy(alpha = 0.2f),
                ) {
                    Text(
                        text = stringResource(R.string.planner_badge),
                        style = MaterialTheme.typography.labelSmall,
                        color = DeepPlum,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }

                if (formattedDate.isNotBlank()) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelMedium,
                        color = DeepPlum.copy(alpha = 0.7f),
                    )
                }
            }

            // Phase and Cycle Day
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(phase.labelRes()),
                    style = MaterialTheme.typography.headlineMedium,
                    color = DeepPlum,
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HeaderSage.copy(alpha = 0.35f),
                ) {
                    Text(
                        text = "Dia ${state.cycleDay} do ciclo",
                        style = MaterialTheme.typography.labelMedium,
                        color = DeepPlum,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }

            // Physiology info
            if (state.physiology.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = OffWhiteBackground,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "O que acontece no seu corpo:",
                            style = MaterialTheme.typography.titleSmall,
                            color = DeepPlum,
                        )
                        Text(
                            text = state.physiology,
                            style = MaterialTheme.typography.bodyMedium,
                            color = DeepPlum.copy(alpha = 0.85f),
                        )
                    }
                }
            }

            // Guidance
            if (state.guidance.isNotBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.planner_what_to_expect),
                        style = MaterialTheme.typography.titleSmall,
                        color = DeepPlum,
                    )
                    Text(
                        text = state.guidance,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DeepPlum.copy(alpha = 0.85f),
                    )
                }
            }
        }
    }
}
