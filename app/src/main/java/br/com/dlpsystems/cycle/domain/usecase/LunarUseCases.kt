package br.com.dlpsystems.cycle.domain.usecase

import br.com.dlpsystems.cycle.domain.model.EnergyLevel
import br.com.dlpsystems.cycle.domain.model.LunarLogEntry
import br.com.dlpsystems.cycle.domain.model.LunarPatternSummary
import br.com.dlpsystems.cycle.domain.model.MoonPhase
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class CalculateMoonPhaseUseCase @Inject constructor() {

    // Conhecida Lua Nova de referência: 11 de Janeiro de 2024 às 11:57 UTC
    private val referenceNewMoon = LocalDate.of(2024, 1, 11)
    private val synodicMonthDays = 29.53058867 // Duração média do ciclo lunar em dias solares

    operator fun invoke(date: LocalDate): MoonPhase {
        val daysDiff = ChronoUnit.DAYS.between(referenceNewMoon, date).toDouble()
        val phaseProgress = ((daysDiff % synodicMonthDays) + synodicMonthDays) % synodicMonthDays

        return when {
            phaseProgress < 1.8457 -> MoonPhase.NEW_MOON
            phaseProgress < 7.3827 -> MoonPhase.WAXING_CRESCENT
            phaseProgress < 9.2283 -> MoonPhase.FIRST_QUARTER
            phaseProgress < 14.7653 -> MoonPhase.WAXING_GIBBOUS
            phaseProgress < 16.6109 -> MoonPhase.FULL_MOON
            phaseProgress < 22.1479 -> MoonPhase.WANING_GIBBOUS
            phaseProgress < 23.9935 -> MoonPhase.LAST_QUARTER
            else -> MoonPhase.WANING_CRESCENT
        }
    }
}

class AnalyzeLunarPatternsUseCase @Inject constructor() {

    operator fun invoke(entries: List<LunarLogEntry>): LunarPatternSummary {
        val entriesWithEnergy = entries.filter { it.energyLevel != null }
        val count = entriesWithEnergy.size

        val disclaimer = "Padrões identificados representam correlações descritivas dos seus registros pessoais. " +
            "A literatura médica não estabelece relação de causa e efeito entre fases lunares e fisiologia humana."

        if (count < 7) {
            return LunarPatternSummary(
                totalRecords = count,
                isDataSufficient = false,
                periodDescription = if (count == 0) "Sem registros" else "$count registros (mínimo de 7 para análise preliminar)",
                highestEnergyPhase = null,
                highestEnergyMoonPhase = null,
                lowestEnergyPhase = null,
                lowestEnergyMoonPhase = null,
                averageEnergyByCyclePhase = emptyMap(),
                averageEnergyByMoonPhase = emptyMap(),
                disclaimer = disclaimer,
            )
        }

        val byCyclePhase = entriesWithEnergy
            .filter { it.cyclePhase != null }
            .groupBy { it.cyclePhase!! }
            .mapValues { (_, list) -> list.map { it.energyLevel!!.value }.average() }

        val byMoonPhase = entriesWithEnergy
            .groupBy { it.moonPhase }
            .mapValues { (_, list) -> list.map { it.energyLevel!!.value }.average() }

        val highestCycle = byCyclePhase.maxByOrNull { it.value }?.key
        val lowestCycle = byCyclePhase.minByOrNull { it.value }?.key
        val highestMoon = byMoonPhase.maxByOrNull { it.value }?.key
        val lowestMoon = byMoonPhase.minByOrNull { it.value }?.key

        val dates = entriesWithEnergy.map { it.date }.sorted()
        val periodDesc = "${count} registros entre ${dates.first()} e ${dates.last()}"

        return LunarPatternSummary(
            totalRecords = count,
            isDataSufficient = true,
            periodDescription = periodDesc,
            highestEnergyPhase = highestCycle,
            highestEnergyMoonPhase = highestMoon,
            lowestEnergyPhase = lowestCycle,
            lowestEnergyMoonPhase = lowestMoon,
            averageEnergyByCyclePhase = byCyclePhase,
            averageEnergyByMoonPhase = byMoonPhase,
            disclaimer = disclaimer,
        )
    }
}
