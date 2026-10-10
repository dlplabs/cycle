package br.com.dlpsystems.cycle.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import br.com.dlpsystems.cycle.domain.model.ActivePartnerConnection
import br.com.dlpsystems.cycle.domain.model.BodyRegion
import br.com.dlpsystems.cycle.domain.model.BodySymptomEntry
import br.com.dlpsystems.cycle.domain.model.ContraceptiveAlarmConfig
import br.com.dlpsystems.cycle.domain.model.ContraceptiveType
import br.com.dlpsystems.cycle.domain.model.ContractionRecord
import br.com.dlpsystems.cycle.domain.model.ContractionSession
import br.com.dlpsystems.cycle.domain.model.ExamCategory
import br.com.dlpsystems.cycle.domain.model.InviteStatus
import br.com.dlpsystems.cycle.domain.model.LunarLogEntry
import br.com.dlpsystems.cycle.domain.model.MedicalExam
import br.com.dlpsystems.cycle.domain.model.PartnerInvite
import br.com.dlpsystems.cycle.domain.model.PartnerShareCategory
import br.com.dlpsystems.cycle.domain.model.PillIntakeRecord
import br.com.dlpsystems.cycle.domain.model.PregnancyCalculationMethod
import br.com.dlpsystems.cycle.domain.model.PregnancyProfile
import br.com.dlpsystems.cycle.domain.repository.HealthRecordsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.healthPreferencesDataStore by preferencesDataStore(name = "health_records_preferences")

@Singleton
class HealthRecordsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : HealthRecordsRepository {

    private val dataStore = context.healthPreferencesDataStore

    // Cache em memória reativo persistido no DataStore / arquivos locais protegidos
    private val contractionSessionsFlow = MutableStateFlow<List<ContractionSession>>(emptyList())
    private val lunarLogsFlow = MutableStateFlow<List<LunarLogEntry>>(emptyList())
    private val pillRecordsFlow = MutableStateFlow<List<PillIntakeRecord>>(emptyList())
    private val medicalExamsFlow = MutableStateFlow<List<MedicalExam>>(emptyList())
    private val partnerInvitesFlow = MutableStateFlow<List<PartnerInvite>>(emptyList())
    private val activePartnersFlow = MutableStateFlow<List<ActivePartnerConnection>>(emptyList())
    private val bodySymptomsFlow = MutableStateFlow<List<BodySymptomEntry>>(emptyList())

    private val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO + kotlinx.coroutines.SupervisorJob())

    init {
        scope.launch {
            loadContractionsFromDisk()
            loadLunarLogsFromDisk()
            loadPillRecordsFromDisk()
            loadExamsFromDisk()
            loadPartnerInvitesFromDisk()
            loadBodySymptomsFromDisk()
        }
    }

    // ==========================================
    // 1. MODO GRAVIDEZ
    // ==========================================
    override fun observePregnancyProfile(): Flow<PregnancyProfile> = dataStore.data.map { prefs ->
        val active = prefs[PregnancyKeys.IS_ACTIVE] ?: false
        val methodStr = prefs[PregnancyKeys.METHOD] ?: PregnancyCalculationMethod.DUM.name
        val method = runCatching { PregnancyCalculationMethod.valueOf(methodStr) }.getOrDefault(PregnancyCalculationMethod.DUM)
        val lmp = prefs[PregnancyKeys.LMP]?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        val edd = prefs[PregnancyKeys.EDD]?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        val usDate = prefs[PregnancyKeys.US_DATE]?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        val usWeeks = prefs[PregnancyKeys.US_WEEKS] ?: 0
        val usDays = prefs[PregnancyKeys.US_DAYS] ?: 0
        val notes = prefs[PregnancyKeys.NOTES] ?: ""

        PregnancyProfile(
            isActive = active,
            calculationMethod = method,
            lastMenstrualPeriod = lmp,
            estimatedDueDate = edd,
            ultrasoundDate = usDate,
            ultrasoundWeeks = usWeeks,
            ultrasoundDays = usDays,
            notes = notes,
        )
    }

    override suspend fun getPregnancyProfile(): PregnancyProfile = observePregnancyProfile().first()

    override suspend fun savePregnancyProfile(profile: PregnancyProfile) {
        dataStore.edit { prefs ->
            prefs[PregnancyKeys.IS_ACTIVE] = profile.isActive
            prefs[PregnancyKeys.METHOD] = profile.calculationMethod.name
            if (profile.lastMenstrualPeriod != null) prefs[PregnancyKeys.LMP] = profile.lastMenstrualPeriod.toString() else prefs.remove(PregnancyKeys.LMP)
            if (profile.estimatedDueDate != null) prefs[PregnancyKeys.EDD] = profile.estimatedDueDate.toString() else prefs.remove(PregnancyKeys.EDD)
            if (profile.ultrasoundDate != null) prefs[PregnancyKeys.US_DATE] = profile.ultrasoundDate.toString() else prefs.remove(PregnancyKeys.US_DATE)
            prefs[PregnancyKeys.US_WEEKS] = profile.ultrasoundWeeks
            prefs[PregnancyKeys.US_DAYS] = profile.ultrasoundDays
            prefs[PregnancyKeys.NOTES] = profile.notes
        }
    }

    // ==========================================
    // 2. CRONÔMETRO DE CONTRAÇÕES
    // ==========================================
    override fun observeContractionSessions(): Flow<List<ContractionSession>> = contractionSessionsFlow.asStateFlow()

    override suspend fun saveContractionRecord(sessionId: String, record: ContractionRecord) {
        contractionSessionsFlow.update { current ->
            val existing = current.find { it.id == sessionId }
            if (existing != null) {
                val updatedRecords = existing.records.filter { it.id != record.id } + record
                current.map { if (it.id == sessionId) it.copy(records = updatedRecords.sortedBy { r -> r.startTimeEpochMs }) else it }
            } else {
                val newSession = ContractionSession(
                    id = sessionId,
                    startedAtEpochMs = record.startTimeEpochMs,
                    records = listOf(record),
                )
                listOf(newSession) + current
            }
        }
        persistContractionsToDisk()
    }

    override suspend fun deleteContractionRecord(sessionId: String, recordId: String) {
        contractionSessionsFlow.update { current ->
            current.map { session ->
                if (session.id == sessionId) {
                    session.copy(records = session.records.filter { it.id != recordId })
                } else session
            }
        }
        persistContractionsToDisk()
    }

    override suspend fun endContractionSession(sessionId: String) {
        contractionSessionsFlow.update { current ->
            current.map { session ->
                if (session.id == sessionId) {
                    session.copy(finishedAtEpochMs = System.currentTimeMillis())
                } else session
            }
        }
        persistContractionsToDisk()
    }

    // ==========================================
    // 3. MANDALA LUNAR E PADRÕES PESSOAIS
    // ==========================================
    override fun observeLunarLogs(): Flow<List<LunarLogEntry>> = lunarLogsFlow.asStateFlow()

    override suspend fun saveLunarLog(entry: LunarLogEntry) {
        lunarLogsFlow.update { current ->
            (current.filter { it.date != entry.date } + entry).sortedByDescending { it.date }
        }
        persistLunarLogsToDisk()
    }

    // ==========================================
    // 4. DESPERTADOR ANTICONCEPCIONAL
    // ==========================================
    override fun observeContraceptiveConfig(): Flow<ContraceptiveAlarmConfig> = dataStore.data.map { prefs ->
        val enabled = prefs[PillKeys.IS_ENABLED] ?: false
        val typeStr = prefs[PillKeys.TYPE] ?: ContraceptiveType.DAILY_PILL_28.name
        val type = runCatching { ContraceptiveType.valueOf(typeStr) }.getOrDefault(ContraceptiveType.DAILY_PILL_28)
        val timeHour = prefs[PillKeys.TIME_HOUR] ?: 21
        val timeMinute = prefs[PillKeys.TIME_MINUTE] ?: 0
        val startDateStr = prefs[PillKeys.PACK_START] ?: LocalDate.now().toString()
        val startDate = runCatching { LocalDate.parse(startDateStr) }.getOrDefault(LocalDate.now())
        val hideSensitive = prefs[PillKeys.HIDE_SENSITIVE] ?: true
        val snooze = prefs[PillKeys.SNOOZE_MINUTES] ?: 15

        ContraceptiveAlarmConfig(
            isEnabled = enabled,
            type = type,
            reminderTime = LocalTime.of(timeHour, timeMinute),
            packStartDate = startDate,
            hideSensitiveInfoOnLockScreen = hideSensitive,
            snoozeMinutes = snooze,
        )
    }

    override suspend fun saveContraceptiveConfig(config: ContraceptiveAlarmConfig) {
        dataStore.edit { prefs ->
            prefs[PillKeys.IS_ENABLED] = config.isEnabled
            prefs[PillKeys.TYPE] = config.type.name
            prefs[PillKeys.TIME_HOUR] = config.reminderTime.hour
            prefs[PillKeys.TIME_MINUTE] = config.reminderTime.minute
            prefs[PillKeys.PACK_START] = config.packStartDate.toString()
            prefs[PillKeys.HIDE_SENSITIVE] = config.hideSensitiveInfoOnLockScreen
            prefs[PillKeys.SNOOZE_MINUTES] = config.snoozeMinutes
        }
    }

    override fun observePillRecords(month: LocalDate): Flow<List<PillIntakeRecord>> =
        pillRecordsFlow.map { list ->
            list.filter { it.scheduledDate.year == month.year && it.scheduledDate.monthValue == month.monthValue }
                .sortedBy { it.scheduledDate }
        }

    override suspend fun recordPillIntake(record: PillIntakeRecord) {
        pillRecordsFlow.update { current ->
            (current.filter { it.id != record.id } + record).sortedBy { it.scheduledDate }
        }
        persistPillRecordsToDisk()
    }

    // ==========================================
    // 5. PASTA DE EXAMES
    // ==========================================
    override fun observeMedicalExams(): Flow<List<MedicalExam>> = medicalExamsFlow.asStateFlow()

    override suspend fun saveMedicalExam(
        title: String,
        date: LocalDate,
        category: ExamCategory,
        notes: String,
        inputStream: InputStream,
        originalFileName: String,
        mimeType: String,
    ): MedicalExam = withContext(Dispatchers.IO) {
        val examsDir = File(context.filesDir, "medical_exams").apply { if (!exists()) mkdirs() }
        val examId = UUID.randomUUID().toString()
        val extension = originalFileName.substringAfterLast(".", if (mimeType.contains("pdf")) "pdf" else "jpg")
        val targetFile = File(examsDir, "exam_${examId}.$extension")

        var bytesWritten = 0L
        FileOutputStream(targetFile).use { out ->
            val buffer = ByteArray(8192)
            var bytes: Int
            while (inputStream.read(buffer).also { bytes = it } >= 0) {
                out.write(buffer, 0, bytes)
                bytesWritten += bytes
            }
        }

        val exam = MedicalExam(
            id = examId,
            title = title,
            date = date,
            category = category,
            notes = notes,
            localFilePath = targetFile.absolutePath,
            mimeType = mimeType,
            fileSizeBytes = bytesWritten,
            isEncrypted = false,
        )

        medicalExamsFlow.update { current -> (listOf(exam) + current).sortedByDescending { it.date } }
        persistExamsToDisk()
        exam
    }

    override suspend fun deleteMedicalExam(examId: String) {
        withContext(Dispatchers.IO) {
            val existing = medicalExamsFlow.value.find { it.id == examId }
            if (existing != null) {
                val file = File(existing.localFilePath)
                if (file.exists()) file.delete()
            }
            medicalExamsFlow.update { current -> current.filter { it.id != examId } }
            persistExamsToDisk()
        }
    }

    // ==========================================
    // 6. CONVITE AO PARCEIRO
    // ==========================================
    override fun observePartnerInvites(): Flow<List<PartnerInvite>> = partnerInvitesFlow.asStateFlow()
    override fun observeActivePartners(): Flow<List<ActivePartnerConnection>> = activePartnersFlow.asStateFlow()

    override suspend fun createPartnerInvite(
        partnerEmail: String,
        partnerName: String,
        categories: Set<PartnerShareCategory>,
    ): PartnerInvite {
        val inviteId = UUID.randomUUID().toString()
        val code = UUID.randomUUID().toString().take(6).uppercase()
        val now = Instant.now()
        val expiresAt = now.plusSeconds(86400 * 7) // Validade de 7 dias

        val invite = PartnerInvite(
            id = inviteId,
            inviteCode = code,
            partnerEmail = partnerEmail,
            partnerName = partnerName,
            allowedCategories = categories,
            createdAt = now,
            expiresAt = expiresAt,
            status = InviteStatus.PENDING,
        )

        partnerInvitesFlow.update { listOf(invite) + it }
        persistPartnerInvitesToDisk()
        return invite
    }

    override suspend fun revokePartnerAccess(inviteOrConnectionId: String) {
        partnerInvitesFlow.update { current ->
            current.map { if (it.id == inviteOrConnectionId) it.copy(status = InviteStatus.REVOKED) else it }
        }
        activePartnersFlow.update { current ->
            current.filter { it.id != inviteOrConnectionId }
        }
        persistPartnerInvitesToDisk()
    }

    // ==========================================
    // 7. SINTOMAS POR REGIÃO DO CORPO
    // ==========================================
    override fun observeBodySymptoms(date: LocalDate): Flow<List<BodySymptomEntry>> =
        bodySymptomsFlow.map { list -> list.filter { it.date == date } }

    override fun observeAllBodySymptoms(): Flow<List<BodySymptomEntry>> = bodySymptomsFlow.asStateFlow()

    override suspend fun saveBodySymptom(entry: BodySymptomEntry) {
        bodySymptomsFlow.update { current ->
            (current.filter { it.id != entry.id } + entry).sortedByDescending { it.date }
        }
        persistBodySymptomsToDisk()
    }

    override suspend fun deleteBodySymptom(entryId: String) {
        bodySymptomsFlow.update { current -> current.filter { it.id != entryId } }
        persistBodySymptomsToDisk()
    }

    // ==========================================
    // PERSISTÊNCIA LOCAL (JSON seguro em app storage)
    // ==========================================
    private suspend fun persistContractionsToDisk() = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(context.filesDir, "contractions.json")
            val array = JSONArray()
            contractionSessionsFlow.value.forEach { session ->
                val sObj = JSONObject().apply {
                    put("id", session.id)
                    put("startedAt", session.startedAtEpochMs)
                    session.finishedAtEpochMs?.let { put("finishedAt", it) }
                    val rArr = JSONArray()
                    session.records.forEach { r ->
                        rArr.put(
                            JSONObject().apply {
                                put("id", r.id)
                                put("start", r.startTimeEpochMs)
                                r.endTimeEpochMs?.let { put("end", it) }
                                put("duration", r.durationSeconds)
                                r.intervalSecondsFromPrevious?.let { put("interval", it) }
                                put("intensity", r.intensity)
                                put("notes", r.notes)
                            },
                        )
                    }
                    put("records", rArr)
                }
                array.put(sObj)
            }
            file.writeText(array.toString())
        }
    }

    private suspend fun loadContractionsFromDisk() = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(context.filesDir, "contractions.json")
            if (!file.exists()) return@runCatching
            val array = JSONArray(file.readText())
            val sessions = mutableListOf<ContractionSession>()
            for (i in 0 until array.length()) {
                val sObj = array.getJSONObject(i)
                val rArr = sObj.getJSONArray("records")
                val records = mutableListOf<ContractionRecord>()
                for (j in 0 until rArr.length()) {
                    val rObj = rArr.getJSONObject(j)
                    records += ContractionRecord(
                        id = rObj.getString("id"),
                        startTimeEpochMs = rObj.getLong("start"),
                        endTimeEpochMs = if (rObj.has("end")) rObj.getLong("end") else null,
                        durationSeconds = rObj.getInt("duration"),
                        intervalSecondsFromPrevious = if (rObj.has("interval")) rObj.getInt("interval") else null,
                        intensity = rObj.optInt("intensity", 1),
                        notes = rObj.optString("notes", ""),
                    )
                }
                sessions += ContractionSession(
                    id = sObj.getString("id"),
                    startedAtEpochMs = sObj.getLong("startedAt"),
                    finishedAtEpochMs = if (sObj.has("finishedAt")) sObj.getLong("finishedAt") else null,
                    records = records,
                )
            }
            contractionSessionsFlow.value = sessions
        }
    }

    private suspend fun persistLunarLogsToDisk() = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(context.filesDir, "lunar_logs.json")
            val array = JSONArray()
            lunarLogsFlow.value.forEach { entry ->
                array.put(
                    JSONObject().apply {
                        put("date", entry.date.toString())
                        put("moonPhase", entry.moonPhase.name)
                        entry.cyclePhase?.let { put("cyclePhase", it.name) }
                        entry.energyLevel?.let { put("energy", it.name) }
                        entry.productivityLevel?.let { put("productivity", it.name) }
                        entry.sleepHours?.let { put("sleep", it.toDouble()) }
                        put("notes", entry.notes)
                    },
                )
            }
            file.writeText(array.toString())
        }
    }

    private suspend fun loadLunarLogsFromDisk() = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(context.filesDir, "lunar_logs.json")
            if (!file.exists()) return@runCatching
            val array = JSONArray(file.readText())
            val logs = mutableListOf<LunarLogEntry>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                logs += LunarLogEntry(
                    date = LocalDate.parse(obj.getString("date")),
                    moonPhase = br.com.dlpsystems.cycle.domain.model.MoonPhase.valueOf(obj.getString("moonPhase")),
                    cyclePhase = if (obj.has("cyclePhase")) br.com.dlpsystems.cycle.domain.model.CyclePhase.valueOf(obj.getString("cyclePhase")) else null,
                    energyLevel = if (obj.has("energy")) br.com.dlpsystems.cycle.domain.model.EnergyLevel.valueOf(obj.getString("energy")) else null,
                    productivityLevel = if (obj.has("productivity")) br.com.dlpsystems.cycle.domain.model.ProductivityLevel.valueOf(obj.getString("productivity")) else null,
                    sleepHours = if (obj.has("sleep")) obj.getDouble("sleep").toFloat() else null,
                    notes = obj.optString("notes", ""),
                )
            }
            lunarLogsFlow.value = logs
        }
    }

    private suspend fun persistPillRecordsToDisk() = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(context.filesDir, "pill_records.json")
            val array = JSONArray()
            pillRecordsFlow.value.forEach { r ->
                array.put(
                    JSONObject().apply {
                        put("id", r.id)
                        put("scheduledDate", r.scheduledDate.toString())
                        put("scheduledTime", r.scheduledTime.toString())
                        r.takenTime?.let { put("takenTime", it.toString()) }
                        put("isTaken", r.isTaken)
                        put("isSnoozed", r.isSnoozed)
                        put("notes", r.notes)
                    },
                )
            }
            file.writeText(array.toString())
        }
    }

    private suspend fun loadPillRecordsFromDisk() = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(context.filesDir, "pill_records.json")
            if (!file.exists()) return@runCatching
            val array = JSONArray(file.readText())
            val list = mutableListOf<PillIntakeRecord>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list += PillIntakeRecord(
                    id = obj.getString("id"),
                    scheduledDate = LocalDate.parse(obj.getString("scheduledDate")),
                    scheduledTime = LocalTime.parse(obj.getString("scheduledTime")),
                    takenTime = if (obj.has("takenTime")) LocalTime.parse(obj.getString("takenTime")) else null,
                    isTaken = obj.getBoolean("isTaken"),
                    isSnoozed = obj.optBoolean("isSnoozed", false),
                    notes = obj.optString("notes", ""),
                )
            }
            pillRecordsFlow.value = list
        }
    }

    private suspend fun persistExamsToDisk() = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(context.filesDir, "exams.json")
            val array = JSONArray()
            medicalExamsFlow.value.forEach { exam ->
                array.put(
                    JSONObject().apply {
                        put("id", exam.id)
                        put("title", exam.title)
                        put("date", exam.date.toString())
                        put("category", exam.category.name)
                        put("notes", exam.notes)
                        put("path", exam.localFilePath)
                        put("mime", exam.mimeType)
                        put("size", exam.fileSizeBytes)
                    },
                )
            }
            file.writeText(array.toString())
        }
    }

    private suspend fun loadExamsFromDisk() = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(context.filesDir, "exams.json")
            if (!file.exists()) return@runCatching
            val array = JSONArray(file.readText())
            val list = mutableListOf<MedicalExam>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list += MedicalExam(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    date = LocalDate.parse(obj.getString("date")),
                    category = ExamCategory.valueOf(obj.getString("category")),
                    notes = obj.optString("notes", ""),
                    localFilePath = obj.getString("path"),
                    mimeType = obj.getString("mime"),
                    fileSizeBytes = obj.getLong("size"),
                )
            }
            medicalExamsFlow.value = list
        }
    }

    private suspend fun persistPartnerInvitesToDisk() = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(context.filesDir, "partner_invites.json")
            val array = JSONArray()
            partnerInvitesFlow.value.forEach { inv ->
                array.put(
                    JSONObject().apply {
                        put("id", inv.id)
                        put("code", inv.inviteCode)
                        put("email", inv.partnerEmail)
                        put("name", inv.partnerName)
                        val cats = JSONArray(inv.allowedCategories.map { it.name })
                        put("categories", cats)
                        put("created", inv.createdAt.toEpochMilli())
                        put("expires", inv.expiresAt.toEpochMilli())
                        put("status", inv.status.name)
                    },
                )
            }
            file.writeText(array.toString())
        }
    }

    private suspend fun loadPartnerInvitesFromDisk() = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(context.filesDir, "partner_invites.json")
            if (!file.exists()) return@runCatching
            val array = JSONArray(file.readText())
            val list = mutableListOf<PartnerInvite>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val catArr = obj.getJSONArray("categories")
                val cats = mutableSetOf<PartnerShareCategory>()
                for (k in 0 until catArr.length()) {
                    cats += PartnerShareCategory.valueOf(catArr.getString(k))
                }
                list += PartnerInvite(
                    id = obj.getString("id"),
                    inviteCode = obj.getString("code"),
                    partnerEmail = obj.getString("email"),
                    partnerName = obj.getString("name"),
                    allowedCategories = cats,
                    createdAt = Instant.ofEpochMilli(obj.getLong("created")),
                    expiresAt = Instant.ofEpochMilli(obj.getLong("expires")),
                    status = InviteStatus.valueOf(obj.getString("status")),
                )
            }
            partnerInvitesFlow.value = list
        }
    }

    private suspend fun persistBodySymptomsToDisk() = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(context.filesDir, "body_symptoms.json")
            val array = JSONArray()
            bodySymptomsFlow.value.forEach { s ->
                array.put(
                    JSONObject().apply {
                        put("id", s.id)
                        put("date", s.date.toString())
                        s.time?.let { put("time", it.toString()) }
                        put("region", s.region.name)
                        put("name", s.symptomName)
                        put("intensity", s.intensity)
                        s.durationMinutes?.let { put("duration", it) }
                        put("notes", s.notes)
                    },
                )
            }
            file.writeText(array.toString())
        }
    }

    private suspend fun loadBodySymptomsFromDisk() = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(context.filesDir, "body_symptoms.json")
            if (!file.exists()) return@runCatching
            val array = JSONArray(file.readText())
            val list = mutableListOf<BodySymptomEntry>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list += BodySymptomEntry(
                    id = obj.getString("id"),
                    date = LocalDate.parse(obj.getString("date")),
                    time = if (obj.has("time")) LocalTime.parse(obj.getString("time")) else null,
                    region = BodyRegion.valueOf(obj.getString("region")),
                    symptomName = obj.getString("name"),
                    intensity = obj.getInt("intensity"),
                    durationMinutes = if (obj.has("duration")) obj.getInt("duration") else null,
                    notes = obj.optString("notes", ""),
                )
            }
            bodySymptomsFlow.value = list
        }
    }

    private object PregnancyKeys {
        val IS_ACTIVE = booleanPreferencesKey("pregnancy_is_active")
        val METHOD = stringPreferencesKey("pregnancy_method")
        val LMP = stringPreferencesKey("pregnancy_lmp")
        val EDD = stringPreferencesKey("pregnancy_edd")
        val US_DATE = stringPreferencesKey("pregnancy_us_date")
        val US_WEEKS = intPreferencesKey("pregnancy_us_weeks")
        val US_DAYS = intPreferencesKey("pregnancy_us_days")
        val NOTES = stringPreferencesKey("pregnancy_notes")
    }

    private object PillKeys {
        val IS_ENABLED = booleanPreferencesKey("pill_is_enabled")
        val TYPE = stringPreferencesKey("pill_type")
        val TIME_HOUR = intPreferencesKey("pill_time_hour")
        val TIME_MINUTE = intPreferencesKey("pill_time_minute")
        val PACK_START = stringPreferencesKey("pill_pack_start")
        val HIDE_SENSITIVE = booleanPreferencesKey("pill_hide_sensitive")
        val SNOOZE_MINUTES = intPreferencesKey("pill_snooze_minutes")
    }
}
