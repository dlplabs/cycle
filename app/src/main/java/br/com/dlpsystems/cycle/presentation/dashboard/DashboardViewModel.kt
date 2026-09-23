package br.com.dlpsystems.cycle.presentation.dashboard

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.dlpsystems.cycle.core.config.CycleConstants
import br.com.dlpsystems.cycle.data.local.UserPreferencesDataSource
import br.com.dlpsystems.cycle.core.notification.ReminderScheduler
import br.com.dlpsystems.cycle.core.notification.labelRes
import br.com.dlpsystems.cycle.data.remote.AnalyticsEvents
import br.com.dlpsystems.cycle.data.remote.AnalyticsService
import br.com.dlpsystems.cycle.domain.model.DailyLog
import br.com.dlpsystems.cycle.domain.model.FlowIntensity
import br.com.dlpsystems.cycle.domain.model.PhaseEvidence
import br.com.dlpsystems.cycle.domain.model.Symptom
import br.com.dlpsystems.cycle.domain.repository.CycleRepository
import br.com.dlpsystems.cycle.domain.repository.UserRepository
import br.com.dlpsystems.cycle.domain.usecase.CalculateCurrentPhaseUseCase
import br.com.dlpsystems.cycle.domain.usecase.GetPhaseInsightsUseCase
import br.com.dlpsystems.cycle.domain.usecase.PhaseCalculationInput
import br.com.dlpsystems.cycle.domain.usecase.PhaseCalculationResult
import br.com.dlpsystems.cycle.domain.usecase.SaveDailyLogUseCase
import br.com.dlpsystems.cycle.presentation.widget.CycleGlanceWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DashboardUiState(
    val loading: Boolean = true,
    val userName: String = "",
    val photoUrl: String? = null,
    val result: PhaseCalculationResult? = null,
    val insight: PhaseEvidence? = null,
    val remindersEnabled: Boolean = true,
    val averageCycleDays: Int = CycleConstants.DEFAULT_CYCLE_DAYS,
    val averagePeriodDays: Int = CycleConstants.DEFAULT_PERIOD_DAYS,
    val errorMessage: String? = null,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val cycleRepository: CycleRepository,
    private val calculateCurrentPhase: CalculateCurrentPhaseUseCase,
    private val getPhaseInsights: GetPhaseInsightsUseCase,
    private val preferences: UserPreferencesDataSource,
    private val reminderScheduler: ReminderScheduler,
    private val saveDailyLog: SaveDailyLogUseCase,
    private val analyticsService: AnalyticsService,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _state = MutableStateFlow(DashboardUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                userRepository.observeProfile(),
                cycleRepository.observeCycles(),
                preferences.remindersEnabled,
                userRepository.observeAuth(),
            ) { profile, cycles, reminders, user ->
                val result = calculateCurrentPhase(
                    PhaseCalculationInput(LocalDate.now(), profile, cycles),
                )
                DashboardUiState(
                    loading = false,
                    userName = profile?.name.orEmpty(),
                    photoUrl = user?.photoUrl,
                    result = result,
                    insight = result.phase?.let(getPhaseInsights::invoke),
                    remindersEnabled = reminders,
                    averageCycleDays = profile?.averageCycleDays ?: CycleConstants.DEFAULT_CYCLE_DAYS,
                    averagePeriodDays = profile?.averagePeriodDays ?: CycleConstants.DEFAULT_PERIOD_DAYS,
                )
            }.collect { snapshot ->
                val remindersChanged = _state.value.remindersEnabled != snapshot.remindersEnabled ||
                    _state.value.loading
                _state.value = snapshot
                if (remindersChanged) reminderScheduler.setEnabled(snapshot.remindersEnabled)
                val phaseName = snapshot.result?.phase?.let { context.getString(it.labelRes()) }.orEmpty()
                preferences.setWidgetSnapshot(snapshot.result?.cycleDay ?: 0, phaseName)
                val manager = GlanceAppWidgetManager(context)
                manager.getGlanceIds(CycleGlanceWidget::class.java).forEach { id ->
                    CycleGlanceWidget().update(context, id)
                }
            }
        }
    }

    fun onOpenSource() {
        analyticsService.log(AnalyticsEvents.EVENT_EVIDENCE_CLICKED, screen = "dashboard")
    }

    fun startPeriodToday() {
        viewModelScope.launch {
            runCatching { cycleRepository.startPeriod(java.time.LocalDate.now()) }
                .onFailure { error -> _state.update { it.copy(errorMessage = error.message) } }
        }
    }

    fun saveQuickCheckIn(flow: FlowIntensity?, symptoms: Set<Symptom>, pain: Int) {
        viewModelScope.launch {
            val profile = userRepository.getProfile()
            val cycles = cycleRepository.getCycles()
            val result = calculateCurrentPhase(PhaseCalculationInput(java.time.LocalDate.now(), profile, cycles))
            val cycle = result.activeCycle ?: return@launch
            val phase = result.phase ?: return@launch
            saveDailyLog(
                DailyLog(
                    date = java.time.LocalDate.now(),
                    cycleId = cycle.id,
                    cycleDay = result.cycleDay,
                    phase = phase,
                    mood = null,
                    skin = null,
                    symptoms = symptoms.toList(),
                    flowIntensity = flow,
                    notes = "",
                    painLevel = pain,
                ),
            )
            analyticsService.log(AnalyticsEvents.EVENT_CYCLE_LOGGED, screen = "dashboard")
        }
    }
}
