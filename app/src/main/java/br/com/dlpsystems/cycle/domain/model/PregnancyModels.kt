package br.com.dlpsystems.cycle.domain.model

import java.time.LocalDate

enum class PregnancyCalculationMethod {
    DUM,        // Data da Última Menstruação (Regra de Naegele)
    DPP,        // Data Provável do Parto informada diretamente
    ULTRASOUND, // Data de ultrassom com idade gestacional aferida por CCN (Hadlock et al., 1984)
    IVF,        // Fertilização in vitro por data de transferência de embrião (D3 ou D5)
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
    val ivfTransferDate: LocalDate? = null,
    val isBlastocystD5: Boolean = true,
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

data class FetalDevelopmentMilestone(
    val week: Int,
    val fruitOrObjectAnalogy: String,
    val estimatedLengthCm: Double,
    val estimatedWeightGrams: Double,
    val anatomicalHighlights: String,
    val maternalSelfCareTip: String,
    val citation: Citation,
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

/**
 * Sessão de monitoramento de chutes e movimentos fetais.
 * Baseado no protocolo internacional Cardiff Count-to-10:
 * GRANT, A. et al. The Lancet, 334(8667): 875-877, 1989 (N = 68.654 gestantes).
 */
data class KickCounterSession(
    val id: String,
    val startedAtEpochMs: Long,
    val finishedAtEpochMs: Long? = null,
    val kicksTimestampsEpochMs: List<Long> = emptyList(),
    val targetCount: Int = 10,
    val isCompleted: Boolean = false,
    val isWarningExceeded2Hours: Boolean = false,
)
