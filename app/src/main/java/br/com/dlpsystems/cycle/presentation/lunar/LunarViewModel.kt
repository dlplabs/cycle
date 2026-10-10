package br.com.dlpsystems.cycle.presentation.lunar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.dlpsystems.cycle.domain.model.CyclePhase
import br.com.dlpsystems.cycle.domain.model.EnergyLevel
import br.com.dlpsystems.cycle.domain.model.LunarLogEntry
import br.com.dlpsystems.cycle.domain.model.LunarPatternSummary
import br.com.dlpsystems.cycle.domain.model.MoonPhase
import br.com.dlpsystems.cycle.domain.model.ProductivityLevel
import br.com.dlpsystems.cycle.domain.repository.HealthRecordsRepository
import br.com.dlpsystems.cycle.domain.usecase.AnalyzeLunarPatternsUseCase
import br.com.dlpsystems.cycle.domain.usecase.CalculateMoonPhaseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class LunarUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val currentMoonPhase: MoonPhase = MoonPhase.NEW_MOON,
    val logs: List<LunarLogEntry> = emptyList(),
    val summary: LunarPatternSummary? = null,
    val selectedEnergy: EnergyLevel? = null,
    val selectedProductivity: ProductivityLevel? = null,
    val notes: String = "",
)

@HiltViewModel
class LunarViewModel @Inject constructor(
    private val healthRepository: HealthRecordsRepository,
    private val calculateMoonPhase: CalculateMoonPhaseUseCase,
    private val analyzePatterns: AnalyzeLunarPatternsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(LunarUiState())
    val state = _state.asStateFlow()

    init {
        val today = LocalDate.now()
        _state.update { it.copy(currentMoonPhase = calculateMoonPhase(today)) }

        viewModelScope.launch {
            healthRepository.observeLunarLogs().collect { list ->
                val summary = analyzePatterns(list)
                val currentLog = list.find { it.date == _state.value.selectedDate }
                _state.update {
                    it.copy(
                        logs = list,
                        summary = summary,
                        selectedEnergy = currentLog?.energyLevel,
                        selectedProductivity = currentLog?.productivityLevel,
                        notes = currentLog?.notes.orEmpty(),
                    )
                }
            }
        }
    }

    fun selectDate(date: LocalDate) {
        val moon = calculateMoonPhase(date)
        val existing = _state.value.logs.find { it.date == date }
        _state.update {
            it.copy(
                selectedDate = date,
                currentMoonPhase = moon,
                selectedEnergy = existing?.energyLevel,
                selectedProductivity = existing?.productivityLevel,
                notes = existing?.notes.orEmpty(),
            )
        }
    }

    fun setEnergy(energy: EnergyLevel) = _state.update { it.copy(selectedEnergy = energy) }
    fun setProductivity(prod: ProductivityLevel) = _state.update { it.copy(selectedProductivity = prod) }
    fun setNotes(notes: String) = _state.update { it.copy(notes = notes) }

    fun saveEntry(cyclePhase: CyclePhase?) {
        val snapshot = _state.value
        val entry = LunarLogEntry(
            date = snapshot.selectedDate,
            moonPhase = snapshot.currentMoonPhase,
            cyclePhase = cyclePhase,
            energyLevel = snapshot.selectedEnergy,
            productivityLevel = snapshot.selectedProductivity,
            notes = snapshot.notes,
        )
        viewModelScope.launch {
            healthRepository.saveLunarLog(entry)
        }
    }
}
