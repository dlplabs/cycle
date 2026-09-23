package br.com.dlpsystems.cycle.data.repository

import br.com.dlpsystems.cycle.core.config.CycleConstants
import br.com.dlpsystems.cycle.data.remote.FirebaseAuthService
import br.com.dlpsystems.cycle.data.remote.FirebaseServices
import br.com.dlpsystems.cycle.data.remote.FirestoreService
import br.com.dlpsystems.cycle.domain.model.DailyLog
import br.com.dlpsystems.cycle.domain.model.MenstrualCycle
import br.com.dlpsystems.cycle.domain.model.ServiceUnavailableException
import br.com.dlpsystems.cycle.domain.repository.CycleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CycleRepositoryImpl @Inject constructor(
    private val services: FirebaseServices,
    private val authService: FirebaseAuthService,
    private val firestore: FirestoreService,
) : CycleRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeCycles(): Flow<List<MenstrualCycle>> =
        authService.observeUser().flatMapLatest { user ->
            if (user == null || !services.available) flowOf(emptyList())
            else firestore.observeCycles(user.id)
        }

    override suspend fun getCycles(): List<MenstrualCycle> = firestore.getCycles(uid())

    override suspend fun startPeriod(date: LocalDate): MenstrualCycle {
        val profile = firestore.getProfile(uid())
        val periodLength = profile?.averagePeriodDays ?: CycleConstants.DEFAULT_PERIOD_DAYS
        return firestore.startPeriod(uid(), date, periodLength)
    }

    override suspend fun saveDailyLog(log: DailyLog) {
        firestore.saveDailyLog(uid(), log)
    }

    override suspend fun getDailyLogs(): List<DailyLog> = firestore.getDailyLogs(uid())

    private fun uid(): String = authService.currentUser()?.id ?: throw ServiceUnavailableException()
}
