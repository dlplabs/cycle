package br.com.dlpsystems.cycle.domain.usecase

import br.com.dlpsystems.cycle.domain.model.CyclePhase
import br.com.dlpsystems.cycle.domain.model.DailyLog
import br.com.dlpsystems.cycle.domain.model.Symptom
import javax.inject.Inject

class CorrelateSymptomsUseCase @Inject constructor() {
    operator fun invoke(logs: List<DailyLog>): Map<CyclePhase, Map<Symptom, Int>> =
        logs.groupBy { it.phase }.mapValues { (_, phaseLogs) ->
            phaseLogs.flatMap { it.symptoms }.groupingBy { it }.eachCount()
        }
}
