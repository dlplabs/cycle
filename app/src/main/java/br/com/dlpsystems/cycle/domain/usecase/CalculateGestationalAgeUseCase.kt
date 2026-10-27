package br.com.dlpsystems.cycle.domain.usecase

import br.com.dlpsystems.cycle.domain.model.GestationalAge
import br.com.dlpsystems.cycle.domain.model.PregnancyCalculationMethod
import br.com.dlpsystems.cycle.domain.model.PregnancyProfile
import br.com.dlpsystems.cycle.domain.model.Trimester
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class CalculateGestationalAgeUseCase @Inject constructor() {

    operator fun invoke(profile: PregnancyProfile, targetDate: LocalDate = LocalDate.now()): GestationalAge? {
        if (!profile.isActive) return null

        val estimatedDueDate = when (profile.calculationMethod) {
            PregnancyCalculationMethod.DUM -> {
                val lmp = profile.lastMenstrualPeriod ?: return null
                // Regra de Naegele: DUM + 280 dias (40 semanas / 9 meses + 7 dias)
                lmp.plusDays(280)
            }
            PregnancyCalculationMethod.DPP -> {
                profile.estimatedDueDate ?: return null
            }
            PregnancyCalculationMethod.ULTRASOUND -> {
                val usDate = profile.ultrasoundDate ?: return null
                val measuredDays = profile.ultrasoundWeeks * 7 + profile.ultrasoundDays
                val daysToDue = 280 - measuredDays
                usDate.plusDays(daysToDue.toLong())
            }
            PregnancyCalculationMethod.IVF -> {
                val transferDate = profile.ivfTransferDate ?: return null
                // Fertilização In Vitro:
                // Blastocisto D5: gestação correspondente a 2 semanas + 5 dias na transferência -> 280 - 14 - 5 = 261 dias restantes para a DPP
                // Embrião D3: gestação correspondente a 2 semanas + 3 dias na transferência -> 280 - 14 - 3 = 263 dias restantes para a DPP
                val daysToDue = if (profile.isBlastocystD5) 261L else 263L
                transferDate.plusDays(daysToDue)
            }
        }

        // Data de concepção biológica aproximada (DUM presumida = DPP - 280 dias)
        val presumedLmp = estimatedDueDate.minusDays(280)
        val elapsedDays = ChronoUnit.DAYS.between(presumedLmp, targetDate).toInt()

        if (elapsedDays < 0) {
            return GestationalAge(
                weeks = 0,
                days = 0,
                totalDays = 0,
                trimester = Trimester.FIRST,
                estimatedDueDate = estimatedDueDate,
                daysRemaining = 280,
                isPostTerm = false,
            )
        }

        val weeks = elapsedDays / 7
        val days = elapsedDays % 7
        val daysRemaining = ChronoUnit.DAYS.between(targetDate, estimatedDueDate).toInt().coerceAtLeast(0)
        val isPostTerm = weeks >= 42

        val trimester = when {
            weeks < 14 -> Trimester.FIRST
            weeks < 28 -> Trimester.SECOND
            else -> Trimester.THIRD
        }

        return GestationalAge(
            weeks = weeks,
            days = days,
            totalDays = elapsedDays,
            trimester = trimester,
            estimatedDueDate = estimatedDueDate,
            daysRemaining = daysRemaining,
            isPostTerm = isPostTerm,
        )
    }
}
