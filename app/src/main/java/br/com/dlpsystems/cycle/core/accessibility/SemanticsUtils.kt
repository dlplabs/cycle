package br.com.dlpsystems.cycle.core.accessibility

object SemanticsUtils {
    fun cycleWheelDescription(
        day: Int,
        totalDays: Int,
        phaseName: String,
        daysRemaining: Int,
    ): String =
        "Dia $day do ciclo de $totalDays dias. Fase atual: $phaseName. " +
            "Previsão da próxima menstruação em $daysRemaining dias."
}
