package br.com.dlpsystems.cycle.domain.model

import java.time.LocalDate

enum class MoonPhase {
    NEW_MOON,        // Lua Nova
    WAXING_CRESCENT, // Lua Crescente
    FIRST_QUARTER,   // Quarto Crescente
    WAXING_GIBBOUS,  // Crescente Gibosa
    FULL_MOON,       // Lua Cheia
    WANING_GIBBOUS,  // Minguante Gibosa
    LAST_QUARTER,    // Quarto Minguante
    WANING_CRESCENT, // Lua Minguante
}

enum class EnergyLevel(val value: Int) {
    VERY_LOW(1),
    LOW(2),
    MODERATE(3),
    HIGH(4),
    VERY_HIGH(5),
}

enum class ProductivityLevel(val value: Int) {
    VERY_LOW(1),
    LOW(2),
    MODERATE(3),
    HIGH(4),
    VERY_HIGH(5),
}

data class LunarLogEntry(
    val date: LocalDate,
    val moonPhase: MoonPhase,
    val cyclePhase: CyclePhase?,
    val energyLevel: EnergyLevel?,
    val productivityLevel: ProductivityLevel?,
    val sleepHours: Float? = null,
    val notes: String = "",
)

data class LunarPatternSummary(
    val totalRecords: Int,
    val isDataSufficient: Boolean,
    val periodDescription: String,
    val highestEnergyPhase: CyclePhase?,
    val highestEnergyMoonPhase: MoonPhase?,
    val lowestEnergyPhase: CyclePhase?,
    val lowestEnergyMoonPhase: MoonPhase?,
    val averageEnergyByCyclePhase: Map<CyclePhase, Double>,
    val averageEnergyByMoonPhase: Map<MoonPhase, Double>,
    val disclaimer: String,
)
