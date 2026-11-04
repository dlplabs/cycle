package br.com.dlpsystems.cycle.domain.usecase

import br.com.dlpsystems.cycle.core.config.CycleConstants
import br.com.dlpsystems.cycle.domain.model.CyclePhase
import br.com.dlpsystems.cycle.domain.model.MenstrualCycle
import br.com.dlpsystems.cycle.domain.model.PhaseStatus
import br.com.dlpsystems.cycle.domain.model.UserProfile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import kotlin.math.roundToInt

class CalculateCurrentPhaseUseCaseTest {
    private val useCase = CalculateCurrentPhaseUseCase()
    private val start = LocalDate.of(2026, 1, 1)
    private val profile = UserProfile(
        name = "Ana",
        birthDate = null,
        averageCycleDays = 28,
        averagePeriodDays = 5,
        createdAt = Instant.EPOCH,
    )

    @Test
    fun classifiesStandardCycleBoundaries() {
        assertPhase(1, CyclePhase.MENSTRUAL)
        assertPhase(5, CyclePhase.MENSTRUAL)
        assertPhase(6, CyclePhase.FOLLICULAR)
        assertPhase(11, CyclePhase.FOLLICULAR)
        assertPhase(12, CyclePhase.OVULATORY)
        assertPhase(15, CyclePhase.OVULATORY)
        assertPhase(16, CyclePhase.LUTEAL)
        assertPhase(28, CyclePhase.LUTEAL)
    }

    @Test
    fun dayBeyondAverageIsExtendedLuteal() {
        val result = evaluate(29)
        assertEquals(PhaseStatus.EXTENDED, result.status)
        assertEquals(CyclePhase.LUTEAL, result.phase)
        assertEquals(CycleConstants.EXTENDED_PHASE_MESSAGE, result.message)
        assertEquals(0, result.daysUntilNextPeriod)
        assertEquals(29, result.cycleDay)
    }

    @Test
    fun movingAverageUsesLastCompletedCycles() {
        val cycles = listOf(
            closed("a", LocalDate.of(2025, 6, 1), 26),
            closed("b", LocalDate.of(2025, 7, 1), 30),
            closed("c", LocalDate.of(2025, 8, 1), 32),
            closed("d", LocalDate.of(2025, 9, 1), 24),
            closed("e", LocalDate.of(2025, 10, 1), 28),
            closed("f", LocalDate.of(2025, 11, 1), 30),
            closed("g", LocalDate.of(2025, 12, 1), 22),
            MenstrualCycle("open", start, null, null, 5),
        )
        val result = useCase(
            PhaseCalculationInput(start.plusDays(9), profile.copy(averageCycleDays = 40), cycles),
        )
        // Média Móvel Ponderada dos 6 mais recentes: [30, 32, 24, 28, 30, 22] com pesos [1, 2, 3, 4, 5, 6]
        // (30*1 + 32*2 + 24*3 + 28*4 + 30*5 + 22*6) / 21 = 560 / 21 = 26.666 -> 27
        val recentLengths = listOf(30, 32, 24, 28, 30, 22)
        val weights = listOf(1, 2, 3, 4, 5, 6)
        val expected = (recentLengths.zip(weights).sumOf { (l, w) -> l * w }.toDouble() / weights.sum()).roundToInt()
        assertEquals(expected, result.cycleLength)
    }

    @Test
    fun ignoresClinicalOutliersFromWeightedAverage() {
        val cycles = listOf(
            closed("outlier_short", LocalDate.of(2025, 7, 1), 16), // Ignorado (< 21)
            closed("valid_1", LocalDate.of(2025, 8, 1), 28),
            closed("valid_2", LocalDate.of(2025, 9, 1), 30),
            closed("outlier_long", LocalDate.of(2025, 10, 1), 58), // Ignorado (> 45)
            closed("valid_3", LocalDate.of(2025, 11, 1), 32),
            MenstrualCycle("open", start, null, null, 5),
        )
        val result = useCase(
            PhaseCalculationInput(start.plusDays(5), profile.copy(averageCycleDays = 28), cycles),
        )
        // Válidos: [28, 30, 32] com pesos [1, 2, 3] -> (28*1 + 30*2 + 32*3) / 6 = (28 + 60 + 96) / 6 = 184/6 = 30.66 -> 31
        assertEquals(31, result.cycleLength)
    }

    @Test
    fun severeDelaySuppressesRepetitiveAlertAndFlagsState() {
        val longAgoStart = LocalDate.now().minusDays(65)
        val result = useCase(
            PhaseCalculationInput(
                date = LocalDate.now(),
                profile = profile,
                cycles = listOf(MenstrualCycle("open_long", longAgoStart, null, null, 5)),
            ),
        )
        org.junit.jupiter.api.Assertions.assertTrue(result.isSevereDelay)
        assertNull(result.message) // Alerta repetitivo suprimido para evitar ansiedade
    }

    @Test
    fun fallsBackToProfileAverageWithoutHistory() {
        val result = useCase(
            PhaseCalculationInput(
                date = start.plusDays(9),
                profile = profile.copy(averageCycleDays = 30, averagePeriodDays = 4),
                cycles = listOf(MenstrualCycle("open", start, null, null, 4)),
            ),
        )
        assertEquals(30, result.cycleLength)
        assertEquals(10, result.cycleDay)
        assertEquals(CyclePhase.FOLLICULAR, result.phase)
    }

    @Test
    fun menstrualWinsWhenOvulatoryWindowOverlapsPeriod() {
        val result = useCase(
            PhaseCalculationInput(
                date = LocalDate.of(2026, 3, 6),
                profile = profile.copy(averageCycleDays = 22, averagePeriodDays = 6),
                cycles = listOf(
                    MenstrualCycle("open", LocalDate.of(2026, 3, 1), null, null, 6),
                ),
            ),
        )
        assertEquals(CyclePhase.MENSTRUAL, result.phase)
        val next = useCase(
            PhaseCalculationInput(
                date = LocalDate.of(2026, 3, 7),
                profile = profile.copy(averageCycleDays = 22, averagePeriodDays = 6),
                cycles = listOf(
                    MenstrualCycle("open", LocalDate.of(2026, 3, 1), null, null, 6),
                ),
            ),
        )
        assertEquals(CyclePhase.OVULATORY, next.phase)
    }

    @Test
    fun noCycleWhenNothingCoversTheDate() {
        val result = useCase(
            PhaseCalculationInput(start, profile, emptyList()),
        )
        assertEquals(PhaseStatus.NO_CYCLE, result.status)
        assertNull(result.phase)
    }

    private fun assertPhase(day: Int, expected: CyclePhase) {
        val result = evaluate(day)
        assertEquals(expected, result.phase, "day $day")
        assertEquals(PhaseStatus.IN_PHASE, result.status)
    }

    private fun evaluate(day: Int) = useCase(
        PhaseCalculationInput(
            date = start.plusDays((day - 1).toLong()),
            profile = profile,
            cycles = listOf(MenstrualCycle("open", start, null, null, 5)),
        ),
    )

    private fun closed(id: String, startDate: LocalDate, length: Int) = MenstrualCycle(
        id = id,
        startDate = startDate,
        endDate = startDate.plusDays((length - 1).toLong()),
        cycleLength = length,
        periodLength = 5,
    )
}
