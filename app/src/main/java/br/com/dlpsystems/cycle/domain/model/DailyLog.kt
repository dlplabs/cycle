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
    val endDate: LocalDate? = null,
    val cycleLength: Int? = null,
    val periodLength: Int = 5,
    val flowEndDate: LocalDate? = null,
    val actualFlowDays: Int? = null,
    val isOutlier: Boolean = false,
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
