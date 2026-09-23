package br.com.dlpsystems.cycle.data.remote

import br.com.dlpsystems.cycle.data.model.CycleEntity
import br.com.dlpsystems.cycle.data.model.DailyLogEntity
import br.com.dlpsystems.cycle.data.model.toDomain
import br.com.dlpsystems.cycle.data.model.toEntity
import br.com.dlpsystems.cycle.data.model.toFirestoreMap
import br.com.dlpsystems.cycle.data.model.toLogId
import br.com.dlpsystems.cycle.domain.model.DailyLog
import br.com.dlpsystems.cycle.domain.model.MenstrualCycle
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

    suspend fun getProfile(uid: String): UserProfile? {
        val snapshot = userDocument(uid).get().await()
        return snapshot.get("profile", br.com.dlpsystems.cycle.data.model.UserProfileEntity::class.java)?.toDomain()
    }

    suspend fun saveProfile(uid: String, profile: UserProfile) {
        userDocument(uid).set(mapOf("profile" to profile.toFirestoreMap()), SetOptions.merge()).await()
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

    suspend fun getCycles(uid: String): List<MenstrualCycle> =
        cycles(uid).get().await().documents.mapNotNull { document ->
            document.toObject(CycleEntity::class.java)?.toDomain(document.id)
        }.sortedBy { it.startDate }

    suspend fun startPeriod(uid: String, date: LocalDate, periodLength: Int): MenstrualCycle {
        val existing = getCycles(uid)
        val open = existing.lastOrNull { it.endDate == null }
        if (open != null && !date.isAfter(open.startDate)) {
            if (date == open.startDate) return open
            throw IllegalArgumentException("period-before-open-cycle")
        }
        val batch = services.firestore.batch()
        if (open != null) {
            val length = ChronoUnit.DAYS.between(open.startDate, date).toInt()
            val closed = open.copy(
                endDate = date.minusDays(1),
                cycleLength = length,
            )
            batch.set(cycles(uid).document(open.id), closed.toEntity())
        }
        val document = cycles(uid).document()
        val created = MenstrualCycle(
            id = document.id,
            startDate = date,
            endDate = null,
            cycleLength = null,
            periodLength = periodLength,
        )
        batch.set(document, created.toEntity())
        batch.commit().await()
        return created
    }

    suspend fun saveDailyLog(uid: String, log: DailyLog) {
        logs(uid).document(log.date.toLogId()).set(log.toEntity()).await()
    }

    suspend fun getDailyLogs(uid: String): List<DailyLog> =
        logs(uid).get().await().documents.mapNotNull { document ->
            val date = runCatching { LocalDate.parse(document.id) }.getOrNull() ?: return@mapNotNull null
            document.toObject(DailyLogEntity::class.java)?.toDomain(date)
        }.sortedBy { it.date }

    private fun userDocument(uid: String) =
        services.firestore.collection("users").document(uid)

    private fun cycles(uid: String) = userDocument(uid).collection("cycles")

    private fun logs(uid: String) = userDocument(uid).collection("daily_logs")

}
