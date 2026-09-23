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
        val expected = listOf(30, 32, 24, 28, 30, 22).average().roundToInt()
        assertEquals(expected, result.cycleLength)
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
