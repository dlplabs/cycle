package br.com.dlpsystems.cycle.domain.usecase

import br.com.dlpsystems.cycle.domain.model.Citation
import br.com.dlpsystems.cycle.domain.model.MenopauseConfirmation
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Avalia o critério formal de consolidação da menopausa (12 meses ininterruptos de amenorreia)
 * e dispara alertas de segurança para sangramento pós-menopausa.
 * Referência: BERAL, V. et al. The Lancet, 362: 419-427, 2003 (Million Women Study N = 1.388.536).
 */
class ConfirmMenopauseUseCase @Inject constructor() {

    operator fun invoke(
        lastMenstruationDate: LocalDate?,
        targetDate: LocalDate = LocalDate.now(),
        hasBleedingAfterTwelveMonths: Boolean = false,
    ): MenopauseConfirmation {
        if (lastMenstruationDate == null) {
            return MenopauseConfirmation(
                isConfirmed = false,
                lastMenstruationDate = null,
                consecutiveAmenorrheaMonths = 0,
                confirmationDate = null,
                hasPostmenopausalBleedingAlert = false,
                clinicalGuidanceNotice = "Informe a data do seu último ciclo para acompanhamento.",
                citation = defaultCitation(),
            )
        }

        val daysDifference = ChronoUnit.DAYS.between(lastMenstruationDate, targetDate).toInt()
        val monthsDifference = (daysDifference / 30.44).toInt()
        val isConfirmed = daysDifference >= 365
        val confirmationDate = if (isConfirmed) lastMenstruationDate.plusYears(1) else null

        val (notice, alert) = when {
            isConfirmed && hasBleedingAfterTwelveMonths -> Pair(
                "Atenção clínica prioritária: Você relatou sangramento vaginal após a consolidação de 12 meses de menopausa. " +
                    "Recomendamos agendar uma consulta ginecológica para realização de ultrassom transvaginal e avaliação endometrial preventiva.",
                true,
            )
            isConfirmed -> Pair(
                "Menopausa consolidada: Você completou 12 meses consecutivos de ausência de menstruação. " +
                    "Seu aplicativo agora prioriza o acompanhamento de saúde óssea, bem-estar cardiovascular e longevidade ativa.",
                false,
            )
            else -> Pair(
                "Em transição: Você está há $monthsDifference meses sem menstruar. O marco de confirmação ocorre ao completar 12 meses consecutivos.",
                false,
            )
        }

        return MenopauseConfirmation(
            isConfirmed = isConfirmed,
            lastMenstruationDate = lastMenstruationDate,
            consecutiveAmenorrheaMonths = monthsDifference,
            confirmationDate = confirmationDate,
            hasPostmenopausalBleedingAlert = alert,
            clinicalGuidanceNotice = notice,
            citation = defaultCitation(),
        )
    }

    private fun defaultCitation() = Citation(
        authors = "Beral, V. et al. (Million Women Study)",
        source = "The Lancet, 362(9382): 419-427, 2003",
        finding = "Epidemiologia pós-menopáusica e protocolo de investigação mandatório para sangramento vaginal tardio (N = 1.388.536 mulheres).",
        url = "https://doi.org/10.1016/S0140-6736(03)14065-2",
    )
}
