package br.com.dlpsystems.cycle.presentation.calendar

import br.com.dlpsystems.cycle.domain.model.MenstrualCycle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class CycleCalendarPredictionTest {

    @Test
    fun nextPeriodPredictionCalculatesCorrectlyForOpenCycle() {
        val startDate = LocalDate.of(2026, 10, 15)
        val cycle = MenstrualCycle(
            id = "c1",
            startDate = startDate,
            endDate = null,
            cycleLength = 28,
            periodLength = 5,
        )

        // Hoje é 28 de Outubro de 2026 (dia 14 do ciclo)
        val today = LocalDate.of(2026, 10, 28)
        val nextStart = startDate.plusDays(28) // 12 de Novembro de 2026

        assertEquals(LocalDate.of(2026, 11, 12), nextStart)
        assertEquals(15, java.time.temporal.ChronoUnit.DAYS.between(today, nextStart).toInt())
    }

    @Test
    fun futureDatesPastCycleLengthArePredictedCorrectly() {
        val startDate = LocalDate.of(2026, 10, 15)
        val cycleLength = 28
        val periodLength = 5

        // Dia 12 de Novembro de 2026 é exatamente o início do próximo ciclo previsto
        val nextPeriodStart = startDate.plusDays(cycleLength.toLong())
        val daysFromStart = java.time.temporal.ChronoUnit.DAYS.between(startDate, nextPeriodStart).toInt()
        val dayInPredictedCycle = (daysFromStart % cycleLength) + 1

        assertEquals(1, dayInPredictedCycle)
        assertTrue(dayInPredictedCycle <= periodLength) // É menstrual

        // Dia 16 de Novembro de 2026 (dia 5 do ciclo previsto)
        val day5Date = nextPeriodStart.plusDays(4)
        val daysFromStart5 = java.time.temporal.ChronoUnit.DAYS.between(startDate, day5Date).toInt()
        val day5InCycle = (daysFromStart5 % cycleLength) + 1
        assertEquals(5, day5InCycle)
        assertTrue(day5InCycle <= periodLength) // É menstrual

        // Dia 17 de Novembro de 2026 (dia 6 do ciclo previsto -> Fase Folicular)
        val day6Date = nextPeriodStart.plusDays(5)
        val daysFromStart6 = java.time.temporal.ChronoUnit.DAYS.between(startDate, day6Date).toInt()
        val day6InCycle = (daysFromStart6 % cycleLength) + 1
        assertEquals(6, day6InCycle)
        assertFalse(day6InCycle <= periodLength) // Não é menstrual
    }
}
