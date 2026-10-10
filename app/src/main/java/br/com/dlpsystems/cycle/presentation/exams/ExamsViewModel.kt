package br.com.dlpsystems.cycle.presentation.exams

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.dlpsystems.cycle.domain.model.ExamCategory
import br.com.dlpsystems.cycle.domain.model.MedicalExam
import br.com.dlpsystems.cycle.domain.repository.HealthRecordsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.InputStream
import java.time.LocalDate
import javax.inject.Inject

data class ExamsUiState(
    val exams: List<MedicalExam> = emptyList(),
    val filterCategory: ExamCategory? = null,
    val isImporting: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class ExamsViewModel @Inject constructor(
    private val healthRepository: HealthRecordsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ExamsUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            healthRepository.observeMedicalExams().collect { list ->
                _state.update { it.copy(exams = list) }
            }
        }
    }

    fun setFilter(category: ExamCategory?) = _state.update { it.copy(filterCategory = category) }

    fun importExam(
        title: String,
        date: LocalDate,
        category: ExamCategory,
        notes: String,
        inputStream: InputStream,
        fileName: String,
        mimeType: String,
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isImporting = true, errorMessage = null) }
            try {
                healthRepository.saveMedicalExam(
                    title = title,
                    date = date,
                    category = category,
                    notes = notes,
                    inputStream = inputStream,
                    originalFileName = fileName,
                    mimeType = mimeType,
                )
                _state.update { it.copy(isImporting = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isImporting = false, errorMessage = e.message ?: "Erro ao importar") }
            }
        }
    }

    fun deleteExam(examId: String) {
        viewModelScope.launch {
            healthRepository.deleteMedicalExam(examId)
        }
    }
}
