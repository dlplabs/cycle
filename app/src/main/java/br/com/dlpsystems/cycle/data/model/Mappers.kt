package br.com.dlpsystems.cycle.data.model

import br.com.dlpsystems.cycle.domain.model.CyclePhase
import br.com.dlpsystems.cycle.domain.model.DailyLog
import br.com.dlpsystems.cycle.domain.model.FlowIntensity
import br.com.dlpsystems.cycle.domain.model.MenstrualCycle
import br.com.dlpsystems.cycle.domain.model.Mood
import br.com.dlpsystems.cycle.domain.model.SkinCondition
import br.com.dlpsystems.cycle.domain.model.Symptom
import br.com.dlpsystems.cycle.domain.model.UserProfile
import com.google.firebase.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val zone: ZoneId = ZoneId.systemDefault()

fun LocalDate.toFirestoreTimestamp(): Timestamp {
    val instant = atStartOfDay(zone).toInstant()
    return Timestamp(instant.epochSecond, instant.nano)
}

fun Timestamp.toLocalDate(): LocalDate =
    toDate().toInstant().atZone(zone).toLocalDate()

fun LocalDate.toLogId(): String = format(DateTimeFormatter.ISO_LOCAL_DATE)

fun UserProfile.toFirestoreMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "birthDate" to birthDate?.toFirestoreTimestamp(),
    "averageCycleDays" to averageCycleDays,
    "averagePeriodDays" to averagePeriodDays,
    "createdAt" to Timestamp(createdAt.epochSecond, createdAt.nano),
)

fun UserProfile.toEntity(): UserProfileEntity = UserProfileEntity(
    name = name,
    birthDate = birthDate?.toFirestoreTimestamp(),
    averageCycleDays = averageCycleDays,
    averagePeriodDays = averagePeriodDays,
    createdAt = Timestamp(createdAt.epochSecond, createdAt.nano),
)

fun UserProfileEntity.toDomain(): UserProfile = UserProfile(
    name = name,
    birthDate = birthDate?.toLocalDate(),
    averageCycleDays = averageCycleDays,
    averagePeriodDays = averagePeriodDays,
    createdAt = createdAt?.toDate()?.toInstant() ?: Instant.EPOCH,
)

fun CycleEntity.toDomain(id: String): MenstrualCycle? {
    val start = startDate?.toLocalDate() ?: return null
    return MenstrualCycle(
        id = id,
        startDate = start,
        endDate = endDate?.toLocalDate(),
        cycleLength = cycleLength,
        periodLength = periodLength,
    )
}

fun MenstrualCycle.toEntity(): CycleEntity = CycleEntity(
    startDate = startDate.toFirestoreTimestamp(),
    endDate = endDate?.toFirestoreTimestamp(),
    cycleLength = cycleLength,
    periodLength = periodLength,
)

fun DailyLog.toEntity(): DailyLogEntity = DailyLogEntity(
    cycleId = cycleId,
    cycleDay = cycleDay,
    phase = phase.name,
    mood = mood?.name,
    skin = skin?.name,
    symptoms = symptoms.map { it.name },
    flowIntensity = flowIntensity?.name,
    notes = notes,
    painLevel = painLevel,
)

fun DailyLogEntity.toDomain(date: LocalDate): DailyLog? {
    val phase = runCatching { CyclePhase.valueOf(phase) }.getOrNull() ?: return null
    return DailyLog(
        date = date,
        cycleId = cycleId,
        cycleDay = cycleDay,
        phase = phase,
        mood = mood?.let { runCatching { Mood.valueOf(it) }.getOrNull() },
        skin = skin?.let { runCatching { SkinCondition.valueOf(it) }.getOrNull() },
        symptoms = symptoms.mapNotNull { runCatching { Symptom.valueOf(it) }.getOrNull() },
        flowIntensity = flowIntensity?.let { runCatching { FlowIntensity.valueOf(it) }.getOrNull() },
        notes = notes,
        painLevel = painLevel,
    )
}
