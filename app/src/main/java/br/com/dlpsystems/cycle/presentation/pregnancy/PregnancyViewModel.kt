package br.com.dlpsystems.cycle.presentation.pregnancy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.dlpsystems.cycle.domain.model.ContractionRecord
import br.com.dlpsystems.cycle.domain.model.ContractionSession
import br.com.dlpsystems.cycle.domain.model.GestationalAge
import br.com.dlpsystems.cycle.domain.model.HealthEducationalArticle
import br.com.dlpsystems.cycle.domain.model.PregnancyCalculationMethod
import br.com.dlpsystems.cycle.domain.model.PregnancyProfile
import br.com.dlpsystems.cycle.domain.repository.HealthRecordsRepository
import br.com.dlpsystems.cycle.domain.usecase.CalculateGestationalAgeUseCase
import br.com.dlpsystems.cycle.domain.usecase.ContractionEvaluation
import br.com.dlpsystems.cycle.domain.usecase.EvaluateContractionsUseCase
import br.com.dlpsystems.cycle.domain.usecase.GetPregnancyContentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

data class PregnancyUiState(
    val profile: PregnancyProfile = PregnancyProfile(),
    val gestationalAge: GestationalAge? = null,
    val currentWeekArticle: HealthEducationalArticle? = null,
    val birthArticles: List<HealthEducationalArticle> = emptyList(),
    val breastfeedingArticles: List<HealthEducationalArticle> = emptyList(),
    val exerciseArticle: HealthEducationalArticle? = null,
    val contractionSessions: List<ContractionSession> = emptyList(),
    val activeContractionStartEpochMs: Long? = null,
    val activeElapsedSeconds: Int = 0,
    val contractionEvaluation: ContractionEvaluation? = null,
)

@HiltViewModel
class PregnancyViewModel @Inject constructor(
    private val healthRepository: HealthRecordsRepository,
    private val calculateGestationalAge: CalculateGestationalAgeUseCase,
    private val getPregnancyContent: GetPregnancyContentUseCase,
    private val evaluateContractions: EvaluateContractionsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(PregnancyUiState())
    val state = _state.asStateFlow()

    private var timerJob: Job? = null
    private var currentSessionId: String = UUID.randomUUID().toString()

    init {
        viewModelScope.launch {
            healthRepository.observePregnancyProfile().collect { profile ->
                val age = calculateGestationalAge(profile)
                val article = age?.let { getPregnancyContent.getWeekInfo(it.weeks) }
                _state.update {
                    it.copy(
                        profile = profile,
                        gestationalAge = age,
                        currentWeekArticle = article,
                        birthArticles = getPregnancyContent.getChildbirthEducation(),
                        breastfeedingArticles = getPregnancyContent.getBreastfeedingArticles(),
                        exerciseArticle = getPregnancyContent.getExerciseGuidance(),
                    )
                }
            }
        }

        viewModelScope.launch {
            healthRepository.observeContractionSessions().collect { sessions ->
                val allRecords = sessions.flatMap { it.records }
                val evaluation = evaluateContractions(allRecords)
                _state.update {
                    it.copy(
                        contractionSessions = sessions,
                        contractionEvaluation = evaluation,
                    )
                }
            }
        }
    }

    fun activatePregnancyWithLmp(lmp: LocalDate) {
        viewModelScope.launch {
            healthRepository.savePregnancyProfile(
                PregnancyProfile(
                    isActive = true,
                    calculationMethod = PregnancyCalculationMethod.DUM,
                    lastMenstrualPeriod = lmp,
                ),
            )
        }
    }

    fun activatePregnancyWithEdd(edd: LocalDate) {
        viewModelScope.launch {
            healthRepository.savePregnancyProfile(
                PregnancyProfile(
                    isActive = true,
                    calculationMethod = PregnancyCalculationMethod.DPP,
                    estimatedDueDate = edd,
                ),
            )
        }
    }

    fun activatePregnancyWithUltrasound(date: LocalDate, weeks: Int, days: Int) {
        viewModelScope.launch {
            healthRepository.savePregnancyProfile(
                PregnancyProfile(
                    isActive = true,
                    calculationMethod = PregnancyCalculationMethod.ULTRASOUND,
                    ultrasoundDate = date,
                    ultrasoundWeeks = weeks,
                    ultrasoundDays = days,
                ),
            )
        }
    }

    fun deactivatePregnancy() {
        viewModelScope.launch {
            val current = _state.value.profile
            healthRepository.savePregnancyProfile(current.copy(isActive = false))
        }
    }

    fun startContraction() {
        val now = System.currentTimeMillis()
        _state.update { it.copy(activeContractionStartEpochMs = now, activeElapsedSeconds = 0) }
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _state.update { it.copy(activeElapsedSeconds = it.activeElapsedSeconds + 1) }
            }
        }
    }

    fun stopContraction(intensity: Int = 2) {
        timerJob?.cancel()
        timerJob = null
        val startMs = _state.value.activeContractionStartEpochMs ?: return
        val endMs = System.currentTimeMillis()
        val duration = ((endMs - startMs) / 1000).toInt().coerceAtLeast(1)

        val previousStart = _state.value.contractionSessions
            .find { it.id == currentSessionId }
            ?.records
            ?.lastOrNull()
            ?.startTimeEpochMs

        val interval = previousStart?.let { ((startMs - it) / 1000).toInt() }

        val record = ContractionRecord(
            id = UUID.randomUUID().toString(),
            startTimeEpochMs = startMs,
            endTimeEpochMs = endMs,
            durationSeconds = duration,
            intervalSecondsFromPrevious = interval,
            intensity = intensity,
        )

        viewModelScope.launch {
            healthRepository.saveContractionRecord(currentSessionId, record)
            _state.update { it.copy(activeContractionStartEpochMs = null, activeElapsedSeconds = 0) }
        }
    }

    fun deleteContraction(recordId: String) {
        viewModelScope.launch {
            healthRepository.deleteContractionRecord(currentSessionId, recordId)
        }
    }
}
