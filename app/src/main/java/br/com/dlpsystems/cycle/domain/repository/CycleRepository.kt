package br.com.dlpsystems.cycle.domain.repository

import br.com.dlpsystems.cycle.domain.model.MenstrualCycle
import br.com.dlpsystems.cycle.domain.model.DailyLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface CycleRepository {
    fun observeCycles(): Flow<List<MenstrualCycle>>
    suspend fun getCycles(): List<MenstrualCycle>
    suspend fun startPeriod(date: LocalDate): MenstrualCycle
    suspend fun saveDailyLog(log: DailyLog)
    suspend fun getDailyLogs(): List<DailyLog>
}
