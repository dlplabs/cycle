package br.com.dlpsystems.cycle.domain.usecase

import br.com.dlpsystems.cycle.domain.model.CyclePhase
import br.com.dlpsystems.cycle.domain.model.DailyLog
import br.com.dlpsystems.cycle.domain.model.FlowIntensity
import br.com.dlpsystems.cycle.domain.model.MenstrualCycle
import br.com.dlpsystems.cycle.domain.model.Mood
import br.com.dlpsystems.cycle.domain.repository.CycleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SaveDailyLogUseCaseTest {
    @Test
    fun delegatesSaveToRepository() = runTest {
        val store = RecordingCycleRepository()
        val log = sampleLog()
        SaveDailyLogUseCase(store).invoke(log)
        assertEquals(listOf(log), store.saved)
    }
}

private class RecordingCycleRepository : CycleRepository {
    val saved = mutableListOf<DailyLog>()
    override fun observeCycles(): Flow<List<MenstrualCycle>> = flowOf(emptyList())
    override suspend fun getCycles(): List<MenstrualCycle> = emptyList()
    override suspend fun startPeriod(date: LocalDate): MenstrualCycle = error("unused")
    override suspend fun saveDailyLog(log: DailyLog) {
        saved += log
    }
    override suspend fun getDailyLogs(): List<DailyLog> = saved
}

class GetPhaseInsightsUseCaseTest {
    private val useCase = GetPhaseInsightsUseCase()

    @Test
    fun everyPhaseCitesIndexedAuthorsFromTheBrief() {
        val evidence = CyclePhase.entries.map(useCase::invoke)
        val authors = evidence.flatMap { phase -> phase.pillars.mapNotNull { it.citation?.authors } }
        listOf(
            "Bull",
            "McNulty",
            "Armour",
            "Baker",
            "Fathizadeh",
            "Raghunath",
        ).forEach { author ->
            assertTrue(authors.any { it.contains(author) }, author)
        }
        assertTrue(
            evidence.flatMap { it.pillars }.mapNotNull { it.citation?.url }.any { it.startsWith("http") },
        )
        assertTrue(evidence.all { it.physiology.isNotBlank() && it.pillars.size == 4 })
    }
}

private fun sampleLog() = DailyLog(
    date = LocalDate.of(2026, 2, 1),
    cycleId = "c1",
    cycleDay = 1,
    phase = CyclePhase.MENSTRUAL,
    mood = Mood.TIRED,
    skin = null,
    symptoms = emptyList(),
    flowIntensity = FlowIntensity.MEDIUM,
    notes = "",
)
