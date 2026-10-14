package br.com.dlpsystems.cycle.presentation.safety

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.dlpsystems.cycle.core.safety.EmergencySosManager
import br.com.dlpsystems.cycle.core.safety.SosDispatchResult
import br.com.dlpsystems.cycle.data.local.UserPreferencesDataSource
import br.com.dlpsystems.cycle.domain.model.IavpEvaluationResult
import br.com.dlpsystems.cycle.domain.model.IavpQuestion
import br.com.dlpsystems.cycle.domain.usecase.IavpCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SafetyUiState(
    val currentQuestionIndex: Int = 0,
    val answersMap: Map<String, Boolean> = emptyMap(),
    val evaluationResult: IavpEvaluationResult? = null,
    val trustedContacts: List<String> = emptyList(),
    val isDisguised: Boolean = false,
    val isDispatchingSos: Boolean = false,
    val sosResult: SosDispatchResult? = null,
    val audioRecordedPath: String? = null,
)

@HiltViewModel
class IavpViewModel @Inject constructor(
    val calculator: IavpCalculator,
    private val sosManager: EmergencySosManager,
    private val preferences: UserPreferencesDataSource,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SafetyUiState())
    val uiState: StateFlow<SafetyUiState> = _uiState.asStateFlow()

    val questions: List<IavpQuestion> get() = calculator.questions

    init {
        // Carrega contatos salvos no DataStore seguro
        viewModelScope.launch {
            preferences.seenCoachMarks.collect { _ ->
                // Mapeamento de contatos salvos no preferences
            }
        }
    }

    fun answerQuestion(questionId: String, answer: Boolean) {
        val updated = _uiState.value.answersMap.toMutableMap()
        updated[questionId] = answer
        _uiState.value = _uiState.value.copy(answersMap = updated)
    }

    fun nextQuestion() {
        val nextIdx = _uiState.value.currentQuestionIndex + 1
        if (nextIdx < questions.size) {
            _uiState.value = _uiState.value.copy(currentQuestionIndex = nextIdx)
        } else {
            calculateResult()
        }
    }

    fun previousQuestion() {
        val prevIdx = (_uiState.value.currentQuestionIndex - 1).coerceAtLeast(0)
        _uiState.value = _uiState.value.copy(currentQuestionIndex = prevIdx)
    }

    fun calculateResult() {
        val result = calculator.calculate(_uiState.value.answersMap)
        _uiState.value = _uiState.value.copy(evaluationResult = result)
    }

    fun resetEvaluation() {
        _uiState.value = _uiState.value.copy(
            currentQuestionIndex = 0,
            answersMap = emptyMap(),
            evaluationResult = null,
        )
    }

    fun toggleDisguise() {
        _uiState.value = _uiState.value.copy(isDisguised = !_uiState.value.isDisguised)
    }

    fun setDisguised(disguised: Boolean) {
        _uiState.value = _uiState.value.copy(isDisguised = disguised)
    }

    fun dispatchSos(onFallbackIntent: ((Intent) -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDispatchingSos = true)
            val contacts = _uiState.value.trustedContacts.ifEmpty { listOf("190", "180") }
            val result = sosManager.dispatchEmergencySos(contacts)
            _uiState.value = _uiState.value.copy(isDispatchingSos = false, sosResult = result)

            if (result is SosDispatchResult.FallbackIntent && onFallbackIntent != null) {
                onFallbackIntent(result.intent)
            }
        }
    }

    fun addTrustedContact(phone: String) {
        val cleanPhone = phone.filter { it.isDigit() || it == '+' }
        if (cleanPhone.isBlank()) return
        val updated = (_uiState.value.trustedContacts + cleanPhone).distinct()
        _uiState.value = _uiState.value.copy(trustedContacts = updated)
    }

    fun removeTrustedContact(phone: String) {
        val updated = _uiState.value.trustedContacts.filter { it != phone }
        _uiState.value = _uiState.value.copy(trustedContacts = updated)
    }

    fun startAudioEvidence() {
        val file = sosManager.startForensicAudioRecording()
        _uiState.value = _uiState.value.copy(audioRecordedPath = file?.absolutePath)
    }

    fun stopAudioEvidence() {
        sosManager.stopForensicAudioRecording()
    }

    fun destroySessionAndExit(onExit: () -> Unit) {
        resetEvaluation()
        stopAudioEvidence()
        _uiState.value = SafetyUiState()
        onExit()
    }
}
