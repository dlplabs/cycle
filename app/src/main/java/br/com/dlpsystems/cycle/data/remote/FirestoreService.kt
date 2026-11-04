package br.com.dlpsystems.cycle.data.remote

import br.com.dlpsystems.cycle.data.model.CycleEntity
import br.com.dlpsystems.cycle.data.model.DailyLogEntity
import br.com.dlpsystems.cycle.data.model.toDomain
import br.com.dlpsystems.cycle.data.model.toEntity
import br.com.dlpsystems.cycle.data.model.toFirestoreMap
import br.com.dlpsystems.cycle.data.model.toLogId
import br.com.dlpsystems.cycle.data.model.toPregnancyProfile
import br.com.dlpsystems.cycle.data.model.toContraceptiveAlarmConfig
import br.com.dlpsystems.cycle.data.model.toBodySymptomEntry
import br.com.dlpsystems.cycle.domain.model.BodySymptomEntry
import br.com.dlpsystems.cycle.domain.model.ContraceptiveAlarmConfig
import br.com.dlpsystems.cycle.domain.model.DailyLog
import br.com.dlpsystems.cycle.domain.model.MenstrualCycle
import br.com.dlpsystems.cycle.domain.model.PregnancyProfile
import br.com.dlpsystems.cycle.domain.model.UserProfile
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreService @Inject constructor(
    private val services: FirebaseServices,
) {
    fun observeProfile(uid: String): Flow<UserProfile?> = callbackFlow {
        val registration = userDocument(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val entity = snapshot?.get("profile", br.com.dlpsystems.cycle.data.model.UserProfileEntity::class.java)
            trySend(entity?.toDomain())
        }
        awaitClose { registration.remove() }
    }

    suspend fun getProfile(uid: String): UserProfile? = runCatching {
        val snapshot = userDocument(uid).get().await()
        snapshot.get("profile", br.com.dlpsystems.cycle.data.model.UserProfileEntity::class.java)?.toDomain()
    }.getOrNull()

    suspend fun saveProfile(uid: String, profile: UserProfile) {
        userDocument(uid).set(mapOf("profile" to profile.toFirestoreMap()), SetOptions.merge()).await()
    }

    suspend fun updatePhotoDrive(uid: String, fileId: String) {
        userDocument(uid).update("profile.photoDriveId", fileId).await()
        runCatching {
            userDocument(uid).update("profile.photoUrl", com.google.firebase.firestore.FieldValue.delete()).await()
        }
    }

    suspend fun updateAverages(uid: String, averageCycleDays: Int, averagePeriodDays: Int) {
        userDocument(uid).update(
            mapOf(
                "profile.averageCycleDays" to averageCycleDays,
                "profile.averagePeriodDays" to averagePeriodDays,
            ),
        ).await()
    }

    fun observeCycles(uid: String): Flow<List<MenstrualCycle>> = callbackFlow {
        val registration = cycles(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val items = snapshot?.documents?.mapNotNull { document ->
                document.toObject(CycleEntity::class.java)?.toDomain(document.id)
            }.orEmpty().sortedBy { it.startDate }
            trySend(items)
        }
        awaitClose { registration.remove() }
    }

    suspend fun getCycles(uid: String): List<MenstrualCycle> = runCatching {
        cycles(uid).get().await().documents.mapNotNull { document ->
            document.toObject(CycleEntity::class.java)?.toDomain(document.id)
        }.sortedBy { it.startDate }
    }.getOrDefault(emptyList())

    suspend fun startPeriod(uid: String, date: LocalDate, periodLength: Int): MenstrualCycle =
        togglePeriodDay(uid, date, periodLength, forceStart = true)

    suspend fun togglePeriodDay(
        uid: String,
        date: LocalDate,
        periodLength: Int,
        forceStart: Boolean = false,
    ): MenstrualCycle {
        if (date.isAfter(LocalDate.now())) {
            throw IllegalArgumentException("Não é permitido registrar menstruação real em datas futuras.")
        }
        val existing = getCycles(uid).sortedBy { it.startDate }
        val open = existing.lastOrNull { it.endDate == null }
        val batch = services.firestore.batch()

        if (existing.isEmpty()) {
            val document = cycles(uid).document()
            val created = MenstrualCycle(
                id = document.id,
                startDate = date,
                flowEndDate = date,
                endDate = null,
                cycleLength = null,
                periodLength = periodLength,
                actualFlowDays = 1,
            )
            batch.set(document, created.toEntity())
            batch.commit().await()
            return created
        }

        if (open != null) {
            val currentFlowEnd = open.flowEndDate ?: open.startDate.plusDays((open.actualFlowDays ?: open.periodLength).toLong() - 1L)

            // 1. Toque no último dia ativo de sangramento -> desmarcar último dia (recuo dinâmico)
            if (!forceStart && date == currentFlowEnd && date.isAfter(open.startDate)) {
                val newFlowEnd = date.minusDays(1)
                val newActualDays = ChronoUnit.DAYS.between(open.startDate, newFlowEnd).toInt() + 1
                val updated = open.copy(
                    flowEndDate = newFlowEnd,
                    actualFlowDays = newActualDays,
                )
                batch.set(cycles(uid).document(open.id), updated.toEntity())
                batch.commit().await()
                return updated
            }

            // 2. Toque após o último dia ativo de sangramento
            if (date.isAfter(currentFlowEnd)) {
                val daysDiff = ChronoUnit.DAYS.between(currentFlowEnd, date).toInt()
                // Janela de tolerância de até 5 dias para extensão/fusão automática
                if (daysDiff in 1..5 && !forceStart) {
                    val newActualDays = ChronoUnit.DAYS.between(open.startDate, date).toInt() + 1
                    val updated = open.copy(
                        flowEndDate = date,
                        actualFlowDays = newActualDays,
                    )
                    batch.set(cycles(uid).document(open.id), updated.toEntity())
                    batch.commit().await()
                    return updated
                } else {
                    // Mais de 5 dias após o fluxo ativo -> fecha o ciclo anterior e inicia novo ciclo
                    val daysSinceOpenStart = ChronoUnit.DAYS.between(open.startDate, date).toInt()
                    val closed = open.copy(
                        endDate = date.minusDays(1),
                        cycleLength = daysSinceOpenStart,
                        flowEndDate = open.flowEndDate ?: currentFlowEnd,
                    )
                    batch.set(cycles(uid).document(open.id), closed.toEntity())

                    val document = cycles(uid).document()
                    val created = MenstrualCycle(
                        id = document.id,
                        startDate = date,
                        flowEndDate = date,
                        endDate = null,
                        cycleLength = null,
                        periodLength = periodLength,
                        actualFlowDays = 1,
                    )
                    batch.set(document, created.toEntity())
                    batch.commit().await()
                    return created
                }
            }

            // 3. Toque antes do início do ciclo aberto
            if (!date.isAfter(open.startDate)) {
                val daysBefore = ChronoUnit.DAYS.between(date, open.startDate).toInt()
                if (daysBefore in 1..5) {
                    // Estende o início para antes sem duplicar ciclo
                    val currentEnd = open.flowEndDate ?: currentFlowEnd
                    val newActualDays = ChronoUnit.DAYS.between(date, currentEnd).toInt() + 1
                    val updated = open.copy(
                        startDate = date,
                        actualFlowDays = newActualDays,
                    )
                    batch.set(cycles(uid).document(open.id), updated.toEntity())
                    batch.commit().await()
                    return updated
                }

                // Ajusta ciclo anterior se houver
                val previous = existing.filter { it.id != open.id && it.startDate.isBefore(open.startDate) }
                    .maxByOrNull { it.startDate }
                if (previous != null && date.isAfter(previous.startDate)) {
                    val adjustedPrev = previous.copy(
                        endDate = date.minusDays(1),
                        cycleLength = ChronoUnit.DAYS.between(previous.startDate, date).toInt(),
                    )
                    batch.set(cycles(uid).document(previous.id), adjustedPrev.toEntity())
                }

                val adjustedOpen = open.copy(
                    startDate = date,
                    flowEndDate = open.flowEndDate ?: date,
                    actualFlowDays = open.actualFlowDays ?: 1,
                )
                batch.set(cycles(uid).document(open.id), adjustedOpen.toEntity())
                batch.commit().await()
                return adjustedOpen
            }

            // 4. Toque entre startDate e currentFlowEnd (definir novo fim de fluxo recuado)
            if (date.isAfter(open.startDate) && date.isBefore(currentFlowEnd)) {
                val newActualDays = ChronoUnit.DAYS.between(open.startDate, date).toInt() + 1
                val updated = open.copy(
                    flowEndDate = date,
                    actualFlowDays = newActualDays,
                )
                batch.set(cycles(uid).document(open.id), updated.toEntity())
                batch.commit().await()
                return updated
            }
        }

        val lastClosed = existing.maxByOrNull { it.startDate }!!
        if (date.isAfter(lastClosed.startDate)) {
            val document = cycles(uid).document()
            val created = MenstrualCycle(
                id = document.id,
                startDate = date,
                flowEndDate = date,
                endDate = null,
                cycleLength = null,
                periodLength = periodLength,
                actualFlowDays = 1,
            )
            batch.set(document, created.toEntity())
            batch.commit().await()
            return created
        } else {
            val targetCycle = existing.find { c ->
                !date.isBefore(c.startDate) && (c.endDate == null || !date.isAfter(c.endDate))
            } ?: existing.minByOrNull { it.startDate }!!

            val prevCycle = existing.filter { it.id != targetCycle.id && it.startDate.isBefore(targetCycle.startDate) }
                .maxByOrNull { it.startDate }

            if (prevCycle != null && date.isAfter(prevCycle.startDate)) {
                val adjustedPrev = prevCycle.copy(
                    endDate = date.minusDays(1),
                    cycleLength = ChronoUnit.DAYS.between(prevCycle.startDate, date).toInt(),
                )
                batch.set(cycles(uid).document(prevCycle.id), adjustedPrev.toEntity())
            }

            val adjustedTarget = targetCycle.copy(startDate = date)
            batch.set(cycles(uid).document(targetCycle.id), adjustedTarget.toEntity())
            batch.commit().await()
            return adjustedTarget
        }
    }

    suspend fun setFlowEndDate(uid: String, cycleId: String, flowEndDate: LocalDate?): MenstrualCycle {
        if (flowEndDate != null && flowEndDate.isAfter(LocalDate.now())) {
            throw IllegalArgumentException("Data de fim do fluxo não pode ser futura.")
        }
        val doc = cycles(uid).document(cycleId)
        val snapshot = doc.get().await()
        val cycle = snapshot.toObject(CycleEntity::class.java)?.toDomain(cycleId)
            ?: throw IllegalStateException("Ciclo não encontrado.")

        if (flowEndDate != null && flowEndDate.isBefore(cycle.startDate)) {
            throw IllegalArgumentException("Data de término do fluxo não pode ser anterior ao início.")
        }

        val actualDays = flowEndDate?.let { ChronoUnit.DAYS.between(cycle.startDate, it).toInt() + 1 }
        val updated = cycle.copy(
            flowEndDate = flowEndDate,
            actualFlowDays = actualDays,
        )
        doc.set(updated.toEntity()).await()
        return updated
    }

    fun observeDailyLogs(uid: String): Flow<List<DailyLog>> = callbackFlow {
        val registration = logs(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val items = snapshot?.documents?.mapNotNull { document ->
                val date = runCatching { LocalDate.parse(document.id) }.getOrNull() ?: return@mapNotNull null
                document.toObject(DailyLogEntity::class.java)?.toDomain(date)
            }.orEmpty().sortedBy { it.date }
            trySend(items)
        }
        awaitClose { registration.remove() }
    }

    suspend fun saveDailyLog(uid: String, log: DailyLog) {
        logs(uid).document(log.date.toLogId()).set(log.toEntity()).await()
    }

    suspend fun getDailyLogs(uid: String): List<DailyLog> = runCatching {
        logs(uid).get().await().documents.mapNotNull { document ->
            val date = runCatching { LocalDate.parse(document.id) }.getOrNull() ?: return@mapNotNull null
            document.toObject(DailyLogEntity::class.java)?.toDomain(date)
        }.sortedBy { it.date }
    }.getOrDefault(emptyList())

    suspend fun savePregnancyProfile(uid: String, profile: br.com.dlpsystems.cycle.domain.model.PregnancyProfile) {
        pregnancy(uid).set(profile.toFirestoreMap(), SetOptions.merge()).await()
    }

    suspend fun getPregnancyProfile(uid: String): br.com.dlpsystems.cycle.domain.model.PregnancyProfile? = runCatching {
        val snapshot = pregnancy(uid).get().await()
        if (snapshot.exists()) snapshot.data?.toPregnancyProfile() else null
    }.getOrNull()

    suspend fun saveContraceptiveConfig(uid: String, config: br.com.dlpsystems.cycle.domain.model.ContraceptiveAlarmConfig) {
        contraceptive(uid).set(config.toFirestoreMap(), SetOptions.merge()).await()
    }

    suspend fun getContraceptiveConfig(uid: String): br.com.dlpsystems.cycle.domain.model.ContraceptiveAlarmConfig? = runCatching {
        val snapshot = contraceptive(uid).get().await()
        if (snapshot.exists()) snapshot.data?.toContraceptiveAlarmConfig() else null
    }.getOrNull()

    suspend fun saveBodySymptom(uid: String, entry: br.com.dlpsystems.cycle.domain.model.BodySymptomEntry) {
        bodySymptoms(uid).document(entry.id).set(entry.toFirestoreMap(), SetOptions.merge()).await()
    }

    suspend fun deleteBodySymptom(uid: String, id: String) {
        bodySymptoms(uid).document(id).delete().await()
    }

    suspend fun getBodySymptoms(uid: String): List<BodySymptomEntry> = runCatching {
        val docs = bodySymptoms(uid).get().await().documents
        val result = mutableListOf<BodySymptomEntry>()
        for (doc in docs) {
            val data = doc.data
            if (data != null) {
                val item = data.toBodySymptomEntry(doc.id)
                if (item != null) {
                    result.add(item)
                }
            }
        }
        result
    }.getOrDefault(emptyList())

    suspend fun deleteUserData(uid: String) {
        val cyclesSnapshot = cycles(uid).get().await()
        val logsSnapshot = logs(uid).get().await()
        val symptomsSnapshot = bodySymptoms(uid).get().await()
        val batch = services.firestore.batch()
        cyclesSnapshot.documents.forEach { batch.delete(it.reference) }
        logsSnapshot.documents.forEach { batch.delete(it.reference) }
        symptomsSnapshot.documents.forEach { batch.delete(it.reference) }
        batch.delete(pregnancy(uid))
        batch.delete(contraceptive(uid))
        batch.delete(userDocument(uid))
        batch.commit().await()
    }

    private fun userDocument(uid: String) =
        services.firestore.collection("users").document(uid)

    private fun cycles(uid: String) = userDocument(uid).collection("cycles")

    private fun logs(uid: String) = userDocument(uid).collection("daily_logs")

    private fun bodySymptoms(uid: String) = userDocument(uid).collection("body_symptoms")

    private fun pregnancy(uid: String) = userDocument(uid).collection("health").document("pregnancy")

    private fun contraceptive(uid: String) = userDocument(uid).collection("health").document("contraceptive")

}
