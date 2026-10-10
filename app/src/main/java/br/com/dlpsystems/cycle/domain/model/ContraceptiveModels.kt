package br.com.dlpsystems.cycle.domain.model

import java.time.LocalDate
import java.time.LocalTime

enum class ContraceptiveType {
    DAILY_PILL_21, // 21 dias ativos + 7 pausa
    DAILY_PILL_24, // 24 dias ativos + 4 placebo/pausa
    DAILY_PILL_28, // 28 dias contínuos
    MINI_PILL,     // Progestagênio isolado (janela estrita de tomada)
    INJECTION_MONTHLY, // Injetável mensal
    INJECTION_QUARTERLY, // Injetável trimestral
    VAGINAL_RING,  // Anel vaginal (3 semanas uso + 1 semana pausa)
    CONTRACEPTIVE_PATCH, // Adesivo semanal
}

data class ContraceptiveAlarmConfig(
    val isEnabled: Boolean = false,
    val type: ContraceptiveType = ContraceptiveType.DAILY_PILL_28,
    val reminderTime: LocalTime = LocalTime.of(21, 0),
    val packStartDate: LocalDate = LocalDate.now(),
    val hideSensitiveInfoOnLockScreen: Boolean = true,
    val snoozeMinutes: Int = 15,
)

data class PillIntakeRecord(
    val id: String,
    val scheduledDate: LocalDate,
    val scheduledTime: LocalTime,
    val takenTime: LocalTime?,
    val isTaken: Boolean,
    val isSnoozed: Boolean = false,
    val notes: String = "",
)
