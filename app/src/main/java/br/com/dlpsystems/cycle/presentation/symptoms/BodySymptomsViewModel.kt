package br.com.dlpsystems.cycle.presentation.symptoms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.dlpsystems.cycle.domain.model.BodyRegion
import br.com.dlpsystems.cycle.domain.model.BodySymptomEntry
import br.com.dlpsystems.cycle.domain.repository.HealthRecordsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

data class BodySymptomsUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val symptomsForDate: List<BodySymptomEntry> = emptyList(),
    val filterRegion: BodyRegion? = null,
)

@HiltViewModel
class BodySymptomsViewModel @Inject constructor(
    private val healthRepository: HealthRecordsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BodySymptomsUiState())
    val state = _state.asStateFlow()

    init {
        loadSymptomsForDate(LocalDate.now())
    }

    fun selectDate(date: LocalDate) {
        _state.update { it.copy(selectedDate = date) }
        loadSymptomsForDate(date)
    }

    fun setFilterRegion(region: BodyRegion?) {
        _state.update { it.copy(filterRegion = region) }
    }

    private fun loadSymptomsForDate(date: LocalDate) {
        viewModelScope.launch {
            healthRepository.observeBodySymptoms(date).collect { list ->
                _state.update { it.copy(symptomsForDate = list) }
            }
        }
    }

    fun addSymptom(
        region: BodyRegion,
        symptomName: String,
        intensity: Int,
        durationMinutes: Int?,
        notes: String,
    ) {
        val entry = BodySymptomEntry(
            id = UUID.randomUUID().toString(),
            date = _state.value.selectedDate,
            time = LocalTime.now(),
            region = region,
            symptomName = symptomName,
            intensity = intensity,
            durationMinutes = durationMinutes,
            notes = notes,
        )
        viewModelScope.launch {
            healthRepository.saveBodySymptom(entry)
        }
    }

    fun deleteSymptom(entryId: String) {
        viewModelScope.launch {
            healthRepository.deleteBodySymptom(entryId)
        }
    }
}
