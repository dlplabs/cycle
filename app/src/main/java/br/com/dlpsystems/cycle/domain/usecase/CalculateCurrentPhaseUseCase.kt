package br.com.dlpsystems.cycle.domain.usecase

import br.com.dlpsystems.cycle.core.config.CycleConstants
import br.com.dlpsystems.cycle.domain.model.CyclePhase
import br.com.dlpsystems.cycle.domain.model.MenstrualCycle
import br.com.dlpsystems.cycle.domain.model.PhaseStatus
import br.com.dlpsystems.cycle.domain.model.UserProfile
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.roundToInt

data class PhaseCalculationInput(
    val date: LocalDate,
    val profile: UserProfile?,
    val cycles: List<MenstrualCycle>,
)

data class PhaseSegment(
    val phase: CyclePhase,
    val startDay: Int,
    val endDay: Int,
)

data class PhaseCalculationResult(
    val cycleDay: Int,
    val cycleLength: Int,
    val periodLength: Int,
    val phase: CyclePhase?,
    val status: PhaseStatus,
    val daysUntilNextPeriod: Int,
    val message: String?,
    val activeCycle: MenstrualCycle?,
    val segments: List<PhaseSegment>,
)

class CalculateCurrentPhaseUseCase @Inject constructor() {
    operator fun invoke(input: PhaseCalculationInput): PhaseCalculationResult {
        val fallbackCycle = (input.profile?.averageCycleDays ?: CycleConstants.DEFAULT_CYCLE_DAYS)
            .coerceIn(15, 90)
        val predictedLength = movingAverageCycleLength(input.cycles, fallbackCycle)
        val fallbackPeriod = (input.profile?.averagePeriodDays ?: CycleConstants.DEFAULT_PERIOD_DAYS)
            .coerceIn(1, predictedLength - 1)
        val active = coveringCycle(input.cycles, input.date)
        if (active == null) {
            return PhaseCalculationResult(
                cycleDay = 0,
                cycleLength = predictedLength,
                periodLength = fallbackPeriod,
                phase = null,
                status = PhaseStatus.NO_CYCLE,
                daysUntilNextPeriod = 0,
                message = null,
                activeCycle = null,
                segments = phaseSegments(predictedLength, fallbackPeriod),
            )
        }
        val cycleLength = if (active.endDate == null) {
            predictedLength
        } else {
            (active.cycleLength ?: predictedLength).coerceAtLeast(2)
        }
        val periodLength = active.periodLength.coerceIn(1, cycleLength - 1)
        val cycleDay = ChronoUnit.DAYS.between(active.startDate, input.date).toInt() + 1
        val extended = active.endDate == null && cycleDay > cycleLength
        val phase = if (extended) {
            CyclePhase.LUTEAL
        } else {
            classifyDay(cycleDay.coerceAtMost(cycleLength), cycleLength, periodLength)
        }
        val status = if (extended) PhaseStatus.EXTENDED else PhaseStatus.IN_PHASE
        val daysUntil = if (extended) 0 else (cycleLength - cycleDay + 1).coerceAtLeast(0)
        return PhaseCalculationResult(
            cycleDay = cycleDay,
            cycleLength = cycleLength,
            periodLength = periodLength,
            phase = phase,
            status = status,
            daysUntilNextPeriod = daysUntil,
            message = if (extended) CycleConstants.EXTENDED_PHASE_MESSAGE else null,
            activeCycle = active,
            segments = phaseSegments(cycleLength, periodLength),
        )
    }

    private fun movingAverageCycleLength(cycles: List<MenstrualCycle>, fallback: Int): Int {
        val lengths = cycles
            .sortedBy { it.startDate }
            .mapNotNull { it.cycleLength }
            .filter { it > 1 }
            .takeLast(CycleConstants.MOVING_AVERAGE_WINDOW)
        if (lengths.isEmpty()) return fallback.coerceAtLeast(2)
        return lengths.average().roundToInt().coerceAtLeast(2)
    }

    private fun coveringCycle(cycles: List<MenstrualCycle>, date: LocalDate): MenstrualCycle? =
        cycles
            .filter { !it.startDate.isAfter(date) }
            .filter { cycle -> cycle.endDate == null || !date.isAfter(cycle.endDate) }
            .maxByOrNull { it.startDate }
}

fun classifyDay(day: Int, cycleLength: Int, periodLength: Int): CyclePhase {
    val ovulationDay = cycleLength - CycleConstants.LUTEAL_PHASE_DAYS
    val ovulatoryStart = ovulationDay - CycleConstants.OVULATORY_LEAD_DAYS
    val ovulatoryEnd = ovulationDay + CycleConstants.OVULATORY_TRAIL_DAYS
    return when {
        day <= periodLength -> CyclePhase.MENSTRUAL
        day in ovulatoryStart..ovulatoryEnd -> CyclePhase.OVULATORY
        day < ovulatoryStart -> CyclePhase.FOLLICULAR
        else -> CyclePhase.LUTEAL
    }
}

fun phaseSegments(cycleLength: Int, periodLength: Int): List<PhaseSegment> {
    if (cycleLength < 2) return emptyList()
    val safePeriod = periodLength.coerceIn(1, cycleLength - 1)
    val days = (1..cycleLength).map { day -> classifyDay(day, cycleLength, safePeriod) }
    val segments = mutableListOf<PhaseSegment>()
    var start = 1
    var current = days.first()
    days.forEachIndexed { index, phase ->
        val day = index + 1
        if (phase != current) {
            segments += PhaseSegment(current, start, day - 1)
            current = phase
            start = day
        }
    }
    segments += PhaseSegment(current, start, cycleLength)
    return segments
}
