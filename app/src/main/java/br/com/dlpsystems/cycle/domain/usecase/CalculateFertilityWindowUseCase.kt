package br.com.dlpsystems.cycle.domain.usecase

import br.com.dlpsystems.cycle.domain.model.CervicalMucusType
import br.com.dlpsystems.cycle.domain.model.Citation
import br.com.dlpsystems.cycle.domain.model.FertilityLevel
import br.com.dlpsystems.cycle.domain.model.FertilityWindowPrediction
import br.com.dlpsystems.cycle.domain.model.LhTestResult
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Calcula a probabilidade diária e os limites temporais da janela fértil.
 * Baseado na coorte europeia de fecundabilidade (DUNSON et al., 2002) e no método sinto-térmico (FRANK-HERRMANN et al., 2007).
 */
class CalculateFertilityWindowUseCase @Inject constructor() {

    operator fun invoke(
        cycleStartDate: LocalDate,
        targetDate: LocalDate = LocalDate.now(),
        averageCycleLengthDays: Int = 28,
        recentBbtReadings: List<Double> = emptyList(),
        recentLhResult: LhTestResult = LhTestResult.NOT_TESTED,
        recentMucus: CervicalMucusType? = null,
    ): FertilityWindowPrediction {
        val lutealLength = 14L
        val estimatedOvulation = cycleStartDate.plusDays((averageCycleLengthDays - lutealLength).coerceAtLeast(10))
        val fertileStart = estimatedOvulation.minusDays(5)
        val fertileEnd = estimatedOvulation.plusDays(1)
        val peakDate = estimatedOvulation.minusDays(1)

        val daysToOvulation = ChronoUnit.DAYS.between(targetDate, estimatedOvulation)

        // Verificação da regra sinto-térmica "3 sobre 6" (Frank-Herrmann et al., 2007)
        val isBbtShiftConfirmed = if (recentBbtReadings.size >= 9) {
            val precedingSix = recentBbtReadings.takeLast(9).take(6)
            val lastThree = recentBbtReadings.takeLast(3)
            val maxPreceding = precedingSix.maxOrNull() ?: 0.0
            lastThree.all { it >= maxPreceding + 0.2 }
        } else {
            false
        }

        // Calibração matemática de probabilidade (Dunson et al., 2002 / Human Reproduction, N = 1.077 casais)
        val (dailyProb, level) = when {
            recentLhResult == LhTestResult.PEAK -> 0.33 to FertilityLevel.PEAK
            recentMucus == CervicalMucusType.EGG_WHITE -> 0.30 to FertilityLevel.PEAK
            daysToOvulation == 2L -> 0.33 to FertilityLevel.PEAK   // 2 dias antes da ovulação
            daysToOvulation == 1L -> 0.29 to FertilityLevel.PEAK   // Véspera da ovulação
            daysToOvulation == 0L -> 0.27 to FertilityLevel.HIGH   // Dia da ovulação
            daysToOvulation == 3L -> 0.16 to FertilityLevel.HIGH
            daysToOvulation == 4L -> 0.14 to FertilityLevel.MEDIUM
            daysToOvulation == 5L -> 0.10 to FertilityLevel.MEDIUM
            daysToOvulation == -1L -> 0.08 to FertilityLevel.LOW   // 1 dia pós-ovulação
            else -> 0.01 to FertilityLevel.LOW
        }

        val citation = Citation(
            authors = "Dunson, D. B.; Colombo, B.; Baird, D. D.",
            source = "Human Reproduction, 17(5): 1399-1403, 2002",
            finding = "Probabilidade diária de concepção atinge o ápice de 33% a dois dias da ovulação (N = 1.077 casais / 3.160 ciclos).",
            url = "https://doi.org/10.1093/humrep/17.5.1399",
        )

        return FertilityWindowPrediction(
            cycleStartDate = cycleStartDate,
            estimatedOvulationDate = estimatedOvulation,
            fertileWindowStartDate = fertileStart,
            fertileWindowEndDate = fertileEnd,
            peakFertilityDate = peakDate,
            dailyProbability = dailyProb,
            level = level,
            isBbtShiftConfirmed = isBbtShiftConfirmed,
            citation = citation,
        )
    }
}
