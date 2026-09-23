package br.com.dlpsystems.cycle.data.model

import com.google.firebase.Timestamp

data class UserProfileEntity(
    val name: String = "",
    val birthDate: Timestamp? = null,
    val averageCycleDays: Int = 28,
    val averagePeriodDays: Int = 5,
    val createdAt: Timestamp? = null,
)

data class CycleEntity(
    val startDate: Timestamp? = null,
    val endDate: Timestamp? = null,
    val cycleLength: Int? = null,
    val periodLength: Int = 5,
)

data class DailyLogEntity(
    val cycleId: String = "",
    val cycleDay: Int = 0,
    val phase: String = "",
    val mood: String? = null,
    val skin: String? = null,
    val symptoms: List<String> = emptyList(),
    val flowIntensity: String? = null,
    val notes: String = "",
    val painLevel: Int? = null,
)
