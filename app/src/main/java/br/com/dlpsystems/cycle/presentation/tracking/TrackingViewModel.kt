package br.com.dlpsystems.cycle.presentation.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.dlpsystems.cycle.domain.model.DailyLog
import br.com.dlpsystems.cycle.domain.model.FlowIntensity
import br.com.dlpsystems.cycle.domain.model.Mood
import br.com.dlpsystems.cycle.domain.model.PhaseStatus
import br.com.dlpsystems.cycle.domain.model.SkinCondition
import br.com.dlpsystems.cycle.domain.model.Symptom
import br.com.dlpsystems.cycle.domain.repository.CycleRepository
import br.com.dlpsystems.cycle.domain.repository.UserRepository
import br.com.dlpsystems.cycle.domain.usecase.CalculateCurrentPhaseUseCase
import br.com.dlpsystems.cycle.domain.usecase.PhaseCalculationInput
import br.com.dlpsystems.cycle.domain.usecase.SaveDailyLogUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class TrackingUiState(
    val date: LocalDate = LocalDate.now(),
    val mood: Mood? = null,
    val skin: SkinCondition? = null,
    val symptoms: Set<Symptom> = emptySet(),
    val flow: FlowIntensity? = null,
    val notes: String = "",
    val periodStarted: Boolean = false,
    val saving: Boolean = false,
    val saved: Boolean = false,
    val needsPeriod: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val cycleRepository: CycleRepository,
    private val calculateCurrentPhase: CalculateCurrentPhaseUseCase,
    private val saveDailyLog: SaveDailyLogUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(TrackingUiState())
    val state = _state.asStateFlow()

    fun selectDate(date: LocalDate) = _state.update { it.copy(date = date, saved = false) }
    fun selectMood(mood: Mood) = _state.update { it.copy(mood = mood) }
    fun selectSkin(skin: SkinCondition) = _state.update { it.copy(skin = skin) }
    fun selectFlow(flow: FlowIntensity) = _state.update { it.copy(flow = flow) }
    fun onNotes(value: String) = _state.update { it.copy(notes = value.take(500)) }
    fun setPeriodStarted(started: Boolean) = _state.update { it.copy(periodStarted = started) }

    fun toggleSymptom(symptom: Symptom) = _state.update { current ->
        val next = current.symptoms.toMutableSet()
        if (!next.add(symptom)) next.remove(symptom)
        current.copy(symptoms = next)
    }

    fun save() {
        val snapshot = state.value
        viewModelScope.launch {
            _state.update { it.copy(saving = true, needsPeriod = false, errorMessage = null) }
            try {
                if (snapshot.periodStarted) {
                    cycleRepository.startPeriod(snapshot.date)
                }
                val profile = userRepository.getProfile()
                val cycles = cycleRepository.getCycles()
                val phase = calculateCurrentPhase(
                    PhaseCalculationInput(snapshot.date, profile, cycles),
                )
                val cycle = phase.activeCycle
                if (cycle == null || phase.status == PhaseStatus.NO_CYCLE || phase.phase == null) {
                    _state.update { it.copy(saving = false, needsPeriod = true) }
                    return@launch
                }
                val flow = snapshot.flow ?: if (snapshot.periodStarted) FlowIntensity.MEDIUM else null
                saveDailyLog(
                    DailyLog(
                        date = snapshot.date,
                        cycleId = cycle.id,
                        cycleDay = phase.cycleDay,
                        phase = phase.phase,
                        mood = snapshot.mood,
                        skin = snapshot.skin,
                        symptoms = snapshot.symptoms.toList(),
                        flowIntensity = flow,
                        notes = snapshot.notes.trim(),
                    ),
                )
                _state.update { it.copy(saving = false, saved = true) }
            } catch (error: Throwable) {
                _state.update { it.copy(saving = false, errorMessage = error.message) }
            }
        }
    }
}
