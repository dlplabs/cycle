package br.com.dlpsystems.cycle.domain.model

import java.time.LocalDate

enum class PregnancyCalculationMethod {
    DUM, // Data da Última Menstruação (Regra de Naegele)
    DPP, // Data Provável do Parto informada diretamente
    ULTRASOUND, // Data de ultrassom com idade gestacional aferida
}

enum class Trimester {
    FIRST,
    SECOND,
    THIRD,
}

data class PregnancyProfile(
    val isActive: Boolean = false,
    val calculationMethod: PregnancyCalculationMethod = PregnancyCalculationMethod.DUM,
    val lastMenstrualPeriod: LocalDate? = null,
    val estimatedDueDate: LocalDate? = null,
    val ultrasoundDate: LocalDate? = null,
    val ultrasoundWeeks: Int = 0,
    val ultrasoundDays: Int = 0,
    val notes: String = "",
)

data class GestationalAge(
    val weeks: Int,
    val days: Int,
    val totalDays: Int,
    val trimester: Trimester,
    val estimatedDueDate: LocalDate,
    val daysRemaining: Int,
    val isPostTerm: Boolean,
)

data class ContractionRecord(
    val id: String,
    val startTimeEpochMs: Long,
    val endTimeEpochMs: Long?,
    val durationSeconds: Int,
    val intervalSecondsFromPrevious: Int? = null,
    val intensity: Int = 1, // 1 a 3 (Leve, Moderada, Forte)
    val notes: String = "",
)

data class ContractionSession(
    val id: String,
    val startedAtEpochMs: Long,
    val finishedAtEpochMs: Long? = null,
    val records: List<ContractionRecord> = emptyList(),
)
