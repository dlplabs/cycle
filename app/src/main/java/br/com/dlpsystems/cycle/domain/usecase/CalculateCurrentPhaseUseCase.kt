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
    val needsFlowEndConfirmation: Boolean = false,
    val isSevereDelay: Boolean = false,
)

class CalculateCurrentPhaseUseCase @Inject constructor() {
    operator fun invoke(input: PhaseCalculationInput): PhaseCalculationResult {
        val fallbackCycle = (input.profile?.averageCycleDays ?: CycleConstants.DEFAULT_CYCLE_DAYS)
            .coerceIn(15, 90)
        val predictedLength = weightedMovingAverageCycleLength(input.cycles, fallbackCycle)
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
                needsFlowEndConfirmation = false,
                isSevereDelay = false,
            )
        }
        val cycleLength = if (active.endDate == null) {
            predictedLength
        } else {
            (active.cycleLength ?: predictedLength).coerceAtLeast(2)
        }
        val effectiveFlowPeriod = (active.actualFlowDays ?: active.periodLength).coerceIn(1, cycleLength - 1)
        val cycleDay = ChronoUnit.DAYS.between(active.startDate, input.date).toInt() + 1
        val extended = active.endDate == null && cycleDay > cycleLength
        val isSevereDelay = active.endDate == null && cycleDay > 60
        val needsConfirmation = active.endDate == null &&
            active.flowEndDate == null &&
            cycleDay > (fallbackPeriod + 3).coerceAtMost(10)

        val phase = if (extended) {
            CyclePhase.LUTEAL
        } else {
            classifyDay(cycleDay.coerceAtMost(cycleLength), cycleLength, effectiveFlowPeriod)
        }
        val status = if (extended) PhaseStatus.EXTENDED else PhaseStatus.IN_PHASE
        val daysUntil = if (extended) 0 else (cycleLength - cycleDay + 1).coerceAtLeast(0)

        // Se atraso > 60 dias, suprime o alerta repetitivo para evitar ansiedade e oferece transição
        val displayMessage = when {
            isSevereDelay -> null
            extended -> CycleConstants.EXTENDED_PHASE_MESSAGE
            else -> null
        }

        return PhaseCalculationResult(
            cycleDay = cycleDay,
            cycleLength = cycleLength,
            periodLength = effectiveFlowPeriod,
            phase = phase,
            status = status,
            daysUntilNextPeriod = daysUntil,
            message = displayMessage,
            activeCycle = active,
            segments = phaseSegments(cycleLength, effectiveFlowPeriod),
            needsFlowEndConfirmation = needsConfirmation,
            isSevereDelay = isSevereDelay,
        )
    }

    private fun weightedMovingAverageCycleLength(cycles: List<MenstrualCycle>, fallback: Int): Int {
        val completedCycles = cycles
            .sortedBy { it.startDate }
            .filter { it.endDate != null && (it.cycleLength ?: 0) > 1 }

        if (completedCycles.isEmpty()) return fallback.coerceAtLeast(2)

        // 1. Filtro de Outliers Clínicos (ciclos menores que 21 ou maiores que 45 dias)
        val regularCycles = completedCycles.filter {
            val len = it.cycleLength ?: 0
            len in 21..45 && !it.isOutlier
        }

        // Se todos foram descartados pelo filtro rígido, tenta média simples dos originais ou fallback
        val candidateCycles = if (regularCycles.isNotEmpty()) regularCycles else completedCycles

        // 2. Filtro Estatístico de 2 Desvios Padrão (se houver pelo menos 4 ciclos)
        val finalSample = if (candidateCycles.size >= 4) {
            val lengths = candidateCycles.mapNotNull { it.cycleLength }
            val mean = lengths.average()
            val variance = lengths.map { (it - mean) * (it - mean) }.average()
            val stdDev = kotlin.math.sqrt(variance)
            val lowerBound = mean - (2 * stdDev)
            val upperBound = mean + (2 * stdDev)
            candidateCycles.filter { (it.cycleLength?.toDouble() ?: mean) in lowerBound..upperBound }
                .ifEmpty { candidateCycles }
        } else {
            candidateCycles
        }

        // 3. Ponderação nos últimos 3 a 6 ciclos (maior peso nos mais recentes)
        val recentWindow = finalSample.takeLast(6)
        if (recentWindow.isEmpty()) return fallback.coerceAtLeast(2)

        var weightedSum = 0.0
        var totalWeight = 0.0
        recentWindow.forEachIndexed { index, cycle ->
            val weight = (index + 1).toDouble() // Pesos lineares crescentes [1, 2, 3...]
            val length = (cycle.cycleLength ?: fallback).toDouble()
            weightedSum += length * weight
            totalWeight += weight
        }

        return if (totalWeight > 0) {
            (weightedSum / totalWeight).roundToInt().coerceIn(15, 90)
        } else {
            fallback.coerceAtLeast(2)
        }
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
