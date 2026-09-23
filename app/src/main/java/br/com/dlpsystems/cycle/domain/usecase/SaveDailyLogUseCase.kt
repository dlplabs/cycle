package br.com.dlpsystems.cycle.domain.usecase

import br.com.dlpsystems.cycle.domain.model.DailyLog
import br.com.dlpsystems.cycle.domain.repository.CycleRepository
import javax.inject.Inject

class SaveDailyLogUseCase @Inject constructor(
    private val cycleRepository: CycleRepository,
) {
    suspend operator fun invoke(log: DailyLog) {
        cycleRepository.saveDailyLog(log)
    }
}
