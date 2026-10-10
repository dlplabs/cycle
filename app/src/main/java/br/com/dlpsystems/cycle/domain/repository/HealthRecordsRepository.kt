package br.com.dlpsystems.cycle.domain.repository

import br.com.dlpsystems.cycle.domain.model.ActivePartnerConnection
import br.com.dlpsystems.cycle.domain.model.BodySymptomEntry
import br.com.dlpsystems.cycle.domain.model.ContraceptiveAlarmConfig
import br.com.dlpsystems.cycle.domain.model.ContractionRecord
import br.com.dlpsystems.cycle.domain.model.ContractionSession
import br.com.dlpsystems.cycle.domain.model.LunarLogEntry
import br.com.dlpsystems.cycle.domain.model.MedicalExam
import br.com.dlpsystems.cycle.domain.model.PartnerInvite
import br.com.dlpsystems.cycle.domain.model.PillIntakeRecord
import br.com.dlpsystems.cycle.domain.model.PregnancyProfile
import kotlinx.coroutines.flow.Flow
import java.io.InputStream
import java.time.LocalDate

interface HealthRecordsRepository {
    // 1. Gravidez
    fun observePregnancyProfile(): Flow<PregnancyProfile>
    suspend fun getPregnancyProfile(): PregnancyProfile
    suspend fun savePregnancyProfile(profile: PregnancyProfile)

    // 2. Cronômetro de Contrações
    fun observeContractionSessions(): Flow<List<ContractionSession>>
    suspend fun saveContractionRecord(sessionId: String, record: ContractionRecord)
    suspend fun deleteContractionRecord(sessionId: String, recordId: String)
    suspend fun endContractionSession(sessionId: String)

    // 3. Mandala Lunar e Padrões
    fun observeLunarLogs(): Flow<List<LunarLogEntry>>
    suspend fun saveLunarLog(entry: LunarLogEntry)

    // 4. Anticoncepcional
    fun observeContraceptiveConfig(): Flow<ContraceptiveAlarmConfig>
    suspend fun saveContraceptiveConfig(config: ContraceptiveAlarmConfig)
    fun observePillRecords(month: LocalDate): Flow<List<PillIntakeRecord>>
    suspend fun recordPillIntake(record: PillIntakeRecord)

    // 5. Pasta de Exames
    fun observeMedicalExams(): Flow<List<MedicalExam>>
    suspend fun saveMedicalExam(
        title: String,
        date: LocalDate,
        category: br.com.dlpsystems.cycle.domain.model.ExamCategory,
        notes: String,
        inputStream: InputStream,
        originalFileName: String,
        mimeType: String,
    ): MedicalExam
    suspend fun deleteMedicalExam(examId: String)

    // 6. Convite ao Parceiro
    fun observePartnerInvites(): Flow<List<PartnerInvite>>
    fun observeActivePartners(): Flow<List<ActivePartnerConnection>>
    suspend fun createPartnerInvite(
        partnerEmail: String,
        partnerName: String,
        categories: Set<br.com.dlpsystems.cycle.domain.model.PartnerShareCategory>,
    ): PartnerInvite
    suspend fun revokePartnerAccess(inviteOrConnectionId: String)

    // 7. Sintomas por Região do Corpo
    fun observeBodySymptoms(date: LocalDate): Flow<List<BodySymptomEntry>>
    fun observeAllBodySymptoms(): Flow<List<BodySymptomEntry>>
    suspend fun saveBodySymptom(entry: BodySymptomEntry)
    suspend fun deleteBodySymptom(entryId: String)
}
