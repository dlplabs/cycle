package br.com.dlpsystems.cycle.presentation.safety

import android.content.Intent
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.dlpsystems.cycle.core.safety.EmergencySosManager
import br.com.dlpsystems.cycle.core.safety.SosDispatchResult
import br.com.dlpsystems.cycle.data.local.UserPreferencesDataSource
import br.com.dlpsystems.cycle.domain.model.ForensicAudioRecord
import br.com.dlpsystems.cycle.domain.model.IavpEvaluationResult
import br.com.dlpsystems.cycle.domain.model.IavpQuestion
import br.com.dlpsystems.cycle.domain.model.TransitProtectionSession
import br.com.dlpsystems.cycle.domain.model.TrustedContact
import br.com.dlpsystems.cycle.domain.usecase.IavpCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class SafetyUiState(
    val currentQuestionIndex: Int = 0,
    val answersMap: Map<String, Boolean> = emptyMap(),
    val evaluationResult: IavpEvaluationResult? = null,
    val trustedContactList: List<TrustedContact> = emptyList(),
    val customSosMessage: String = "SOS Cycle: Preciso de apoio imediato. Acompanhe minha localização:",
    val activeTransitSession: TransitProtectionSession? = null,
    val transitRemainingSeconds: Long = 0L,
    val isTransitExpiredAlert: Boolean = false,
    val isDisguised: Boolean = false,
    val isDispatchingSos: Boolean = false,
    val sosResult: SosDispatchResult? = null,
    val statusBannerMessage: String? = null,
    val isRecordingAudio: Boolean = false,
    val audioRecordedPath: String? = null,
    val forensicAudioRecords: List<ForensicAudioRecord> = emptyList(),
    val playingAudioPath: String? = null,
    val hasLocationPermission: Boolean = false,
    val hasAudioPermission: Boolean = false,
) {
    val trustedContacts: List<String>
        get() = trustedContactList.map { it.phone }
}

@HiltViewModel
class IavpViewModel @Inject constructor(
    val calculator: IavpCalculator,
    private val sosManager: EmergencySosManager,
    private val preferences: UserPreferencesDataSource,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SafetyUiState())
    val uiState: StateFlow<SafetyUiState> = _uiState.asStateFlow()

    val questions: List<IavpQuestion> get() = calculator.questions

    private var transitTickerJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null

    init {
        refreshPermissionsAndAudioRecords()

        viewModelScope.launch {
            combine(
                preferences.trustedContacts,
                preferences.customSosMessage,
                preferences.activeTransitSession,
            ) { contacts, sosMsg, transitSession ->
                Triple(contacts, sosMsg, transitSession)
            }.collect { (contacts, sosMsg, transitSession) ->
                _uiState.update { current ->
                    current.copy(
                        trustedContactList = contacts,
                        customSosMessage = sosMsg,
                        activeTransitSession = transitSession,
                    )
                }
                syncTransitTicker(transitSession)
            }
        }
    }

    fun refreshPermissionsAndAudioRecords() {
        _uiState.update {
            it.copy(
                hasLocationPermission = sosManager.hasLocationPermission(),
                hasAudioPermission = sosManager.hasAudioPermission(),
                forensicAudioRecords = sosManager.listForensicAudioRecords(),
            )
        }
    }

    private fun syncTransitTicker(session: TransitProtectionSession?) {
        transitTickerJob?.cancel()
        if (session == null) {
            _uiState.update {
                it.copy(
                    transitRemainingSeconds = 0L,
                    isTransitExpiredAlert = false,
                )
            }
            return
        }

        transitTickerJob = viewModelScope.launch {
            var alertTriggered = false
            while (true) {
                val now = System.currentTimeMillis()
                val remaining = session.remainingSeconds(now)
                val expired = session.isExpired(now)

                _uiState.update {
                    it.copy(
                        transitRemainingSeconds = remaining,
                        isTransitExpiredAlert = expired,
                    )
                }

                if (expired && !alertTriggered) {
                    alertTriggered = true
                    sosManager.vibrateAlert()
                    _uiState.update {
                        it.copy(
                            statusBannerMessage = "O tempo estimado do seu trajeto para \"${session.destinationLabel}\" terminou. Confirme que chegou bem ou acione seu Círculo de Confiança.",
                        )
                    }
                    break
                }
                delay(1_000L)
            }
        }
    }

    fun clearStatusBanner() {
        _uiState.update { it.copy(statusBannerMessage = null) }
    }

    // -------------------------------------------------------------------------
    // 1. Círculo de Confiança Sempre Ativo
    // -------------------------------------------------------------------------

    fun addTrustedContact(name: String, phone: String, relationship: String = "Rede de Apoio") {
        val cleanPhone = phone.filter { it.isDigit() || it == '+' }
        if (cleanPhone.length < 8) {
            _uiState.update {
                it.copy(statusBannerMessage = "Informe um telefone válido com DDD (ex.: 11999999999).")
            }
            return
        }
        val cleanName = name.trim().ifBlank { "Contato de Confiança" }
        val cleanRel = relationship.trim().ifBlank { "Rede de Apoio" }

        val existing = _uiState.value.trustedContactList
        if (existing.any { it.phone == cleanPhone }) {
            _uiState.update {
                it.copy(statusBannerMessage = "Este telefone já faz parte do seu Círculo de Confiança.")
            }
            return
        }

        val newContact = TrustedContact(
            id = UUID.randomUUID().toString(),
            name = cleanName,
            phone = cleanPhone,
            relationship = cleanRel,
        )
        val updated = existing + newContact
        _uiState.update {
            it.copy(
                trustedContactList = updated,
                statusBannerMessage = "$cleanName adicionado(a) ao seu Círculo de Confiança.",
            )
        }
        viewModelScope.launch {
            preferences.setTrustedContacts(updated)
        }
    }

    fun addTrustedContact(phone: String) {
        addTrustedContact(name = "Contato de Confiança", phone = phone)
    }

    fun removeTrustedContact(contactIdOrPhone: String) {
        val updated = _uiState.value.trustedContactList.filterNot {
            it.id == contactIdOrPhone || it.phone == contactIdOrPhone
        }
        _uiState.update { it.copy(trustedContactList = updated) }
        viewModelScope.launch {
            preferences.setTrustedContacts(updated)
        }
    }

    fun updateCustomSosMessage(message: String) {
        _uiState.update { it.copy(customSosMessage = message) }
        viewModelScope.launch {
            preferences.setCustomSosMessage(message)
        }
    }

    fun dispatchSos(onFallbackIntent: ((Intent) -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDispatchingSos = true, statusBannerMessage = null) }
            val phones = _uiState.value.trustedContacts
            if (phones.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isDispatchingSos = false,
                        statusBannerMessage = "Cadastre pelo menos um contato no Círculo de Confiança acima antes de disparar o SOS.",
                    )
                }
                return@launch
            }

            val result = sosManager.dispatchEmergencySos(
                trustedPhones = phones,
                customMessage = _uiState.value.customSosMessage,
            )
            val feedbackMsg = when (result) {
                is SosDispatchResult.SmsSent ->
                    "Alerta SOS enviado para ${result.contactCount} contato(s) do seu Círculo de Confiança."
                is SosDispatchResult.FallbackIntent ->
                    "Abrindo envio imediato de SMS com sua localização para seu Círculo de Confiança."
                is SosDispatchResult.Error -> result.message
            }
            _uiState.update {
                it.copy(
                    isDispatchingSos = false,
                    sosResult = result,
                    statusBannerMessage = feedbackMsg,
                )
            }

            if (result is SosDispatchResult.FallbackIntent && onFallbackIntent != null) {
                onFallbackIntent(result.intent)
            }
        }
    }

    fun shareCurrentLocation(onShareIntent: (Intent) -> Unit) {
        viewModelScope.launch {
            val intent = sosManager.buildQuickLocationShareIntent(_uiState.value.customSosMessage)
            onShareIntent(Intent.createChooser(intent, "Compartilhar localização com Círculo de Confiança"))
        }
    }

    // -------------------------------------------------------------------------
    // 2. Modo Discreto & Proteção em Trânsito ("Cheguei Bem")
    // -------------------------------------------------------------------------

    fun startTransitProtection(
        destinationLabel: String,
        durationMinutes: Int,
        notifyCircleNow: Boolean = false,
        onShareIntent: ((Intent) -> Unit)? = null,
    ) {
        val safeDest = destinationLabel.trim().ifBlank { "Casa" }
        val safeDuration = durationMinutes.coerceIn(5, 240)
        val now = System.currentTimeMillis()
        val expires = now + (safeDuration * 60_000L)

        val session = TransitProtectionSession(
            destinationLabel = safeDest,
            durationMinutes = safeDuration,
            startedAtMillis = now,
            expiresAtMillis = expires,
        )

        sosManager.vibrateConfirmation()
        _uiState.update {
            it.copy(
                activeTransitSession = session,
                transitRemainingSeconds = safeDuration * 60L,
                isTransitExpiredAlert = false,
                statusBannerMessage = "Proteção em Trânsito ativa para \"$safeDest\" ($safeDuration min).",
            )
        }

        viewModelScope.launch {
            preferences.setActiveTransitSession(session)
            if (notifyCircleNow && onShareIntent != null) {
                val shareIntent = sosManager.buildTransitStartShareIntent(safeDest, safeDuration)
                onShareIntent(Intent.createChooser(shareIntent, "Avisar Círculo de Confiança sobre trajeto"))
            }
        }
    }

    fun shareActiveTransitWithCircle(onShareIntent: (Intent) -> Unit) {
        val session = _uiState.value.activeTransitSession ?: return
        viewModelScope.launch {
            val remainingMin = ((_uiState.value.transitRemainingSeconds + 59) / 60).toInt().coerceAtLeast(1)
            val intent = sosManager.buildTransitStartShareIntent(session.destinationLabel, remainingMin)
            onShareIntent(Intent.createChooser(intent, "Compartilhar trajeto com Círculo de Confiança"))
        }
    }

    fun extendTransitTime(extraMinutes: Int = 10) {
        val current = _uiState.value.activeTransitSession ?: return
        val baseTime = maxOf(System.currentTimeMillis(), current.expiresAtMillis)
        val updated = current.copy(
            durationMinutes = current.durationMinutes + extraMinutes,
            expiresAtMillis = baseTime + (extraMinutes * 60_000L),
        )
        sosManager.vibrateConfirmation()
        _uiState.update {
            it.copy(
                activeTransitSession = updated,
                isTransitExpiredAlert = false,
                statusBannerMessage = "Tempo de trajeto estendido em +$extraMinutes minutos.",
            )
        }
        viewModelScope.launch {
            preferences.setActiveTransitSession(updated)
        }
    }

    fun confirmSafeArrival(
        notifyCircle: Boolean = false,
        onShareIntent: ((Intent) -> Unit)? = null,
    ) {
        val current = _uiState.value.activeTransitSession ?: return
        val dest = current.destinationLabel
        transitTickerJob?.cancel()
        sosManager.vibrateConfirmation()
        _uiState.update {
            it.copy(
                activeTransitSession = null,
                transitRemainingSeconds = 0L,
                isTransitExpiredAlert = false,
                statusBannerMessage = "Que bom que você chegou em segurança em \"$dest\"! Proteção encerrada.",
            )
        }
        viewModelScope.launch {
            preferences.setActiveTransitSession(null)
            if (notifyCircle && onShareIntent != null) {
                val intent = sosManager.buildSafeArrivalShareIntent(dest)
                onShareIntent(Intent.createChooser(intent, "Avisar que cheguei em segurança"))
            }
        }
    }

    // -------------------------------------------------------------------------
    // 3. Cofre de Áudio Forense & Camuflagem Rápida
    // -------------------------------------------------------------------------

    fun startAudioEvidence(): Boolean {
        val file = sosManager.startForensicAudioRecording()
        val started = file != null
        _uiState.update {
            it.copy(
                isRecordingAudio = started,
                audioRecordedPath = file?.absolutePath,
                hasAudioPermission = sosManager.hasAudioPermission(),
                statusBannerMessage = if (started) {
                    "Gravação discreta iniciada em cofre local privado."
                } else {
                    "Não foi possível iniciar a gravação. Verifique a permissão do microfone."
                },
            )
        }
        return started
    }

    fun stopAudioEvidence() {
        sosManager.stopForensicAudioRecording()
        val records = sosManager.listForensicAudioRecords()
        _uiState.update {
            it.copy(
                isRecordingAudio = false,
                forensicAudioRecords = records,
                statusBannerMessage = "Gravação salva com segurança no cofre privado do aplicativo.",
            )
        }
    }

    fun togglePlayAudioRecord(filePath: String) {
        if (_uiState.value.playingAudioPath == filePath) {
            stopAudioPlayback()
            return
        }
        stopAudioPlayback()
        runCatching {
            val player = MediaPlayer().apply {
                setDataSource(filePath)
                setOnCompletionListener {
                    stopAudioPlayback()
                }
                prepare()
                start()
            }
            mediaPlayer = player
            _uiState.update { it.copy(playingAudioPath = filePath) }
        }.onFailure {
            stopAudioPlayback()
            _uiState.update { it.copy(statusBannerMessage = "Não foi possível reproduzir este arquivo de áudio.") }
        }
    }

    fun stopAudioPlayback() {
        runCatching {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
        mediaPlayer = null
        _uiState.update { it.copy(playingAudioPath = null) }
    }

    fun deleteAudioRecord(filePath: String) {
        if (_uiState.value.playingAudioPath == filePath) {
            stopAudioPlayback()
        }
        sosManager.deleteForensicAudioRecord(filePath)
        _uiState.update {
            it.copy(
                forensicAudioRecords = sosManager.listForensicAudioRecords(),
                statusBannerMessage = "Gravação excluída permanentemente.",
            )
        }
    }

    fun shareAudioRecord(filePath: String, onShareIntent: (Intent) -> Unit) {
        val intent = sosManager.buildShareAudioEvidenceIntent(filePath)
        if (intent != null) {
            onShareIntent(Intent.createChooser(intent, "Exportar evidência de áudio"))
        } else {
            _uiState.update { it.copy(statusBannerMessage = "Arquivo de evidência não encontrado.") }
        }
    }

    fun toggleDisguise() {
        _uiState.update { it.copy(isDisguised = !it.isDisguised) }
    }

    fun setDisguised(disguised: Boolean) {
        _uiState.update { it.copy(isDisguised = disguised) }
    }

    // -------------------------------------------------------------------------
    // 4. Questionário Preventivo IAVP (13 Etapas)
    // -------------------------------------------------------------------------

    fun answerQuestion(questionId: String, answer: Boolean) {
        val updated = _uiState.value.answersMap.toMutableMap()
        updated[questionId] = answer
        _uiState.update { it.copy(answersMap = updated) }
    }

    fun nextQuestion() {
        val nextIdx = _uiState.value.currentQuestionIndex + 1
        if (nextIdx < questions.size) {
            _uiState.update { it.copy(currentQuestionIndex = nextIdx) }
        } else {
            calculateResult()
        }
    }

    fun previousQuestion() {
        val prevIdx = (_uiState.value.currentQuestionIndex - 1).coerceAtLeast(0)
        _uiState.update { it.copy(currentQuestionIndex = prevIdx) }
    }

    fun calculateResult() {
        val result = calculator.calculate(_uiState.value.answersMap)
        _uiState.update { it.copy(evaluationResult = result) }
    }

    fun resetEvaluation() {
        _uiState.update {
            it.copy(
                currentQuestionIndex = 0,
                answersMap = emptyMap(),
                evaluationResult = null,
            )
        }
    }

    fun destroySessionAndExit(onExit: () -> Unit) {
        resetEvaluation()
        stopAudioEvidence()
        stopAudioPlayback()
        onExit()
    }

    override fun onCleared() {
        super.onCleared()
        stopAudioPlayback()
    }
}

