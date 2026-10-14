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
    "photoUrl" to photoUrl,
    "photoDriveId" to photoDriveId,
)

fun UserProfile.toEntity(): UserProfileEntity = UserProfileEntity(
    name = name,
    birthDate = birthDate?.toFirestoreTimestamp(),
    averageCycleDays = averageCycleDays,
    averagePeriodDays = averagePeriodDays,
    createdAt = Timestamp(createdAt.epochSecond, createdAt.nano),
    photoUrl = photoUrl,
    photoDriveId = photoDriveId,
)

fun UserProfileEntity.toDomain(): UserProfile = UserProfile(
    name = name,
    birthDate = birthDate?.toLocalDate(),
    averageCycleDays = averageCycleDays,
    averagePeriodDays = averagePeriodDays,
    createdAt = createdAt?.toDate()?.toInstant() ?: Instant.EPOCH,
    photoUrl = photoUrl,
    photoDriveId = photoDriveId,
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

fun br.com.dlpsystems.cycle.domain.model.PregnancyProfile.toFirestoreMap(): Map<String, Any?> = mapOf(
    "isActive" to isActive,
    "calculationMethod" to calculationMethod.name,
    "lastMenstrualPeriod" to lastMenstrualPeriod?.toString(),
    "estimatedDueDate" to estimatedDueDate?.toString(),
    "ultrasoundDate" to ultrasoundDate?.toString(),
    "ultrasoundWeeks" to ultrasoundWeeks,
    "ultrasoundDays" to ultrasoundDays,
    "notes" to notes,
)

fun Map<String, Any?>.toPregnancyProfile(): br.com.dlpsystems.cycle.domain.model.PregnancyProfile {
    val methodStr = this["calculationMethod"] as? String ?: br.com.dlpsystems.cycle.domain.model.PregnancyCalculationMethod.DUM.name
    val method = runCatching { br.com.dlpsystems.cycle.domain.model.PregnancyCalculationMethod.valueOf(methodStr) }.getOrDefault(br.com.dlpsystems.cycle.domain.model.PregnancyCalculationMethod.DUM)
    val lmp = (this["lastMenstrualPeriod"] as? String)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    val edd = (this["estimatedDueDate"] as? String)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    val usDate = (this["ultrasoundDate"] as? String)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    val weeks = (this["ultrasoundWeeks"] as? Number)?.toInt() ?: 0
    val days = (this["ultrasoundDays"] as? Number)?.toInt() ?: 0
    val active = this["isActive"] as? Boolean ?: false
    val notes = this["notes"] as? String ?: ""

    return br.com.dlpsystems.cycle.domain.model.PregnancyProfile(
        isActive = active,
        calculationMethod = method,
        lastMenstrualPeriod = lmp,
        estimatedDueDate = edd,
        ultrasoundDate = usDate,
        ultrasoundWeeks = weeks,
        ultrasoundDays = days,
        notes = notes,
    )
}

fun br.com.dlpsystems.cycle.domain.model.BodySymptomEntry.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "date" to date.toString(),
    "time" to time?.toString(),
    "region" to region.name,
    "symptomName" to symptomName,
    "intensity" to intensity,
    "durationMinutes" to durationMinutes,
    "notes" to notes,
)

fun Map<String, Any?>.toBodySymptomEntry(docId: String): br.com.dlpsystems.cycle.domain.model.BodySymptomEntry? {
    val dateStr = this["date"] as? String ?: return null
    val date = runCatching { LocalDate.parse(dateStr) }.getOrNull() ?: return null
    val time = (this["time"] as? String)?.let { runCatching { java.time.LocalTime.parse(it) }.getOrNull() }
    val regionStr = this["region"] as? String ?: br.com.dlpsystems.cycle.domain.model.BodyRegion.GENERAL.name
    val region = runCatching { br.com.dlpsystems.cycle.domain.model.BodyRegion.valueOf(regionStr) }.getOrDefault(br.com.dlpsystems.cycle.domain.model.BodyRegion.GENERAL)
    val name = this["symptomName"] as? String ?: ""
    val intensity = (this["intensity"] as? Number)?.toInt() ?: 1
    val duration = (this["durationMinutes"] as? Number)?.toInt()
    val notes = this["notes"] as? String ?: ""

    return br.com.dlpsystems.cycle.domain.model.BodySymptomEntry(
        id = docId,
        date = date,
        time = time,
        region = region,
        symptomName = name,
        intensity = intensity,
        durationMinutes = duration,
        notes = notes,
    )
}

fun br.com.dlpsystems.cycle.domain.model.ContraceptiveAlarmConfig.toFirestoreMap(): Map<String, Any?> = mapOf(
    "isEnabled" to isEnabled,
    "type" to type.name,
    "reminderTime" to reminderTime.toString(),
    "packStartDate" to packStartDate.toString(),
    "hideSensitiveInfoOnLockScreen" to hideSensitiveInfoOnLockScreen,
    "snoozeMinutes" to snoozeMinutes,
)

fun Map<String, Any?>.toContraceptiveAlarmConfig(): br.com.dlpsystems.cycle.domain.model.ContraceptiveAlarmConfig {
    val enabled = this["isEnabled"] as? Boolean ?: false
    val typeStr = this["type"] as? String ?: br.com.dlpsystems.cycle.domain.model.ContraceptiveType.DAILY_PILL_28.name
    val type = runCatching { br.com.dlpsystems.cycle.domain.model.ContraceptiveType.valueOf(typeStr) }.getOrDefault(br.com.dlpsystems.cycle.domain.model.ContraceptiveType.DAILY_PILL_28)
    val timeStr = this["reminderTime"] as? String ?: "21:00"
    val time = runCatching { java.time.LocalTime.parse(timeStr) }.getOrDefault(java.time.LocalTime.of(21, 0))
    val startStr = this["packStartDate"] as? String ?: LocalDate.now().toString()
    val start = runCatching { LocalDate.parse(startStr) }.getOrDefault(LocalDate.now())
    val hide = this["hideSensitiveInfoOnLockScreen"] as? Boolean ?: true
    val snooze = (this["snoozeMinutes"] as? Number)?.toInt() ?: 15

    return br.com.dlpsystems.cycle.domain.model.ContraceptiveAlarmConfig(
        isEnabled = enabled,
        type = type,
        reminderTime = time,
        packStartDate = start,
        hideSensitiveInfoOnLockScreen = hide,
        snoozeMinutes = snooze,
    )
}
