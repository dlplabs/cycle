package br.com.dlpsystems.cycle.domain.model

import java.time.Instant
import java.time.LocalDate

data class UserProfile(
    val name: String,
    val birthDate: LocalDate?,
    val averageCycleDays: Int,
    val averagePeriodDays: Int,
    val createdAt: Instant,
    val photoUrl: String? = null,
    val photoDriveId: String? = null,
)

data class MenstrualCycle(
    val id: String,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val cycleLength: Int?,
    val periodLength: Int,
)

data class DailyLog(
    val date: LocalDate,
    val cycleId: String,
    val cycleDay: Int,
    val phase: CyclePhase,
    val mood: Mood?,
    val skin: SkinCondition?,
    val symptoms: List<Symptom>,
    val flowIntensity: FlowIntensity?,
    val notes: String,
    val painLevel: Int? = null,
)

data class SignedInUser(
    val id: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String? = null,
)
