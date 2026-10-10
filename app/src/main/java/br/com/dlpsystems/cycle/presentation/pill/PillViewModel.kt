package br.com.dlpsystems.cycle.presentation.pill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.dlpsystems.cycle.core.notification.ContraceptiveAlarmScheduler
import br.com.dlpsystems.cycle.domain.model.ContraceptiveAlarmConfig
import br.com.dlpsystems.cycle.domain.model.ContraceptiveType
import br.com.dlpsystems.cycle.domain.model.PillIntakeRecord
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

data class PillUiState(
    val config: ContraceptiveAlarmConfig = ContraceptiveAlarmConfig(),
    val records: List<PillIntakeRecord> = emptyList(),
    val canScheduleExactAlarms: Boolean = true,
)

@HiltViewModel
class PillViewModel @Inject constructor(
    private val healthRepository: HealthRecordsRepository,
    private val alarmScheduler: ContraceptiveAlarmScheduler,
) : ViewModel() {

    private val _state = MutableStateFlow(PillUiState())
    val state = _state.asStateFlow()

    init {
        _state.update { it.copy(canScheduleExactAlarms = alarmScheduler.canScheduleExactAlarms()) }

        viewModelScope.launch {
            healthRepository.observeContraceptiveConfig().collect { config ->
                _state.update { it.copy(config = config) }
            }
        }

        viewModelScope.launch {
            healthRepository.observePillRecords(LocalDate.now()).collect { records ->
                _state.update { it.copy(records = records) }
            }
        }
    }

    fun toggleEnabled(enabled: Boolean) {
        val updated = _state.value.config.copy(isEnabled = enabled)
        viewModelScope.launch {
            healthRepository.saveContraceptiveConfig(updated)
            alarmScheduler.scheduleAlarm(updated)
        }
    }

    fun updateConfig(type: ContraceptiveType, hour: Int, minute: Int, hideSensitive: Boolean) {
        val updated = _state.value.config.copy(
            type = type,
            reminderTime = LocalTime.of(hour, minute),
            hideSensitiveInfoOnLockScreen = hideSensitive,
        )
        viewModelScope.launch {
            healthRepository.saveContraceptiveConfig(updated)
            if (updated.isEnabled) alarmScheduler.scheduleAlarm(updated)
        }
    }

    fun confirmIntakeToday() {
        val now = LocalTime.now()
        val today = LocalDate.now()
        val record = PillIntakeRecord(
            id = UUID.randomUUID().toString(),
            scheduledDate = today,
            scheduledTime = _state.value.config.reminderTime,
            takenTime = now,
            isTaken = true,
        )
        viewModelScope.launch {
            healthRepository.recordPillIntake(record)
        }
    }

    fun snoozeIntake(minutes: Int = 15) {
        alarmScheduler.scheduleSnooze(minutes, _state.value.config.hideSensitiveInfoOnLockScreen)
    }
}
