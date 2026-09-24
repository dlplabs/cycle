package br.com.dlpsystems.cycle.presentation.planner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import br.com.dlpsystems.cycle.R
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
                _state.value = _state.value.copy(blocked = true, phase = null)
                return@launch
            }
            val projected = projectFuturePhase(start, target, length, period)
            val text = insights(projected.phase).pillars.firstOrNull()?.guidance.orEmpty()
            _state.value = PlannerUiState(
                premium = premium,
                phase = projected.phase,
                cycleDay = projected.cycleDay,
                blocked = false,
                guidance = text,
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DatePicker(state = picker)
            PrimaryButton(
                text = stringResource(R.string.planner_calculate),
                onClick = { viewModel.onDate(picker.selectedDateMillis) },
            )
            when {
                state.blocked && !state.premium -> {
                    Text(stringResource(R.string.planner_locked))
                    PrimaryButton(text = stringResource(R.string.open_paywall), onClick = onPaywall)
                }
                state.blocked -> Text(stringResource(R.string.planner_past))
                state.phase != null -> {
                    Text(stringResource(R.string.planner_result, state.cycleDay, stringResource(state.phase!!.labelRes())))
                    Text(state.guidance)
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
