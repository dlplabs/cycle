package br.com.dlpsystems.cycle.presentation.dashboard

import android.content.Context
import android.net.Uri
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.dlpsystems.cycle.core.config.CycleConstants
import br.com.dlpsystems.cycle.data.local.AvatarCompressor
import br.com.dlpsystems.cycle.data.local.UserPreferencesDataSource
import br.com.dlpsystems.cycle.core.notification.ReminderScheduler
import br.com.dlpsystems.cycle.core.notification.labelRes
import br.com.dlpsystems.cycle.data.remote.AnalyticsEvents
import br.com.dlpsystems.cycle.data.remote.AnalyticsService
import br.com.dlpsystems.cycle.domain.model.DailyLog
import br.com.dlpsystems.cycle.domain.model.FlowIntensity
import br.com.dlpsystems.cycle.domain.model.PhaseEvidence
import br.com.dlpsystems.cycle.domain.model.Symptom
import br.com.dlpsystems.cycle.domain.repository.BillingRepository
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.time.LocalDate
import javax.inject.Inject

data class DashboardUiState(
    val loading: Boolean = true,
    val userName: String = "",
    val photoUrl: String? = null,
    val photoDriveId: String? = null,
    val avatarBytes: ByteArray? = null,
    val googlePhotoUrl: String? = null,
    val result: PhaseCalculationResult? = null,
    val insight: PhaseEvidence? = null,
    val remindersEnabled: Boolean = true,
    val averageCycleDays: Int = CycleConstants.DEFAULT_CYCLE_DAYS,
    val averagePeriodDays: Int = CycleConstants.DEFAULT_PERIOD_DAYS,
    val errorMessage: String? = null,
    val premium: Boolean = false,
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
    private val billingRepository: BillingRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _state = MutableStateFlow(DashboardUiState())
    val state = _state.asStateFlow()
    private var shownAvatar: ByteArray? = null

    init {
        viewModelScope.launch {
            combine(
                userRepository.observeProfile(),
                cycleRepository.observeCycles(),
                preferences.remindersEnabled,
                userRepository.observeAuth(),
                billingRepository.isPremiumUser,
            ) { profile, cycles, reminders, user, premium ->
                val result = calculateCurrentPhase(
                    PhaseCalculationInput(LocalDate.now(), profile, cycles),
                )
                DashboardUiState(
                    loading = false,
                    userName = profile?.name.orEmpty(),
                    photoUrl = if (profile?.photoDriveId.isNullOrBlank()) {
                        profile?.photoUrl?.takeIf { it.isNotBlank() } ?: user?.photoUrl
                    } else {
                        null
                    },
                    photoDriveId = profile?.photoDriveId,
                    googlePhotoUrl = user?.photoUrl,
                    result = result,
                    insight = result.phase?.let(getPhaseInsights::invoke),
                    remindersEnabled = reminders,
                    averageCycleDays = profile?.averageCycleDays ?: CycleConstants.DEFAULT_CYCLE_DAYS,
                    averagePeriodDays = profile?.averagePeriodDays ?: CycleConstants.DEFAULT_PERIOD_DAYS,
                    premium = premium,
                )
            }.collect { snapshot ->
                val previous = _state.value
                val remindersChanged = previous.remindersEnabled != snapshot.remindersEnabled || previous.loading
                if (snapshot.photoDriveId.isNullOrBlank()) shownAvatar = null
                _state.value = snapshot.copy(avatarBytes = shownAvatar)
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

    fun useGooglePhoto(accessToken: String) {
        val url = _state.value.googlePhotoUrl ?: return
        viewModelScope.launch {
            runCatching {
                val jpeg = AvatarCompressor.jpeg(
                    withContext(Dispatchers.IO) { URL(url).openStream().use { it.readBytes() } },
                )
                shownAvatar = jpeg
                _state.update { it.copy(avatarBytes = jpeg, errorMessage = null) }
                userRepository.saveAvatar(accessToken, jpeg)
            }.onFailure { reportPhotoFailure() }
        }
    }

    fun uploadAvatar(uri: Uri, accessToken: String) {
        viewModelScope.launch {
            runCatching {
                val jpeg = AvatarCompressor.jpeg(context, uri)
                shownAvatar = jpeg
                _state.update { it.copy(avatarBytes = jpeg, errorMessage = null) }
                userRepository.saveAvatar(accessToken, jpeg)
            }.onFailure { reportPhotoFailure() }
        }
    }

    fun loadStoredAvatar(accessToken: String) {
        val fileId = _state.value.photoDriveId ?: return
        viewModelScope.launch {
            runCatching { userRepository.readAvatar(accessToken, fileId) }
                .onSuccess { bytes ->
                    shownAvatar = bytes
                    _state.update { it.copy(avatarBytes = bytes, errorMessage = null) }
                }
                .onFailure { reportPhotoFailure() }
        }
    }

    private fun reportPhotoFailure() {
        _state.update { it.copy(errorMessage = context.getString(br.com.dlpsystems.cycle.R.string.photo_save_failed)) }
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
