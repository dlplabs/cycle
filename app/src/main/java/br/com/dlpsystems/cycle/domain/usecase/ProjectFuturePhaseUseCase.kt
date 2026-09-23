package br.com.dlpsystems.cycle.domain.usecase

import br.com.dlpsystems.cycle.domain.model.CyclePhase
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class ProjectedPhase(
    val cycleDay: Int,
    val phase: CyclePhase,
)

class ProjectFuturePhaseUseCase @Inject constructor() {
    operator fun invoke(
        cycleStart: LocalDate,
        target: LocalDate,
        cycleLength: Int,
        periodLength: Int,
    ): ProjectedPhase {
        val length = cycleLength.coerceAtLeast(2)
        val elapsed = ChronoUnit.DAYS.between(cycleStart, target).toInt()
        val day = ((elapsed % length) + length) % length + 1
        return ProjectedPhase(
            cycleDay = day,
            phase = classifyDay(day, length, periodLength.coerceIn(1, length - 1)),
        )
    }
}
