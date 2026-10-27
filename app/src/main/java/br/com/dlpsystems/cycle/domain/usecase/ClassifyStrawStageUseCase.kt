package br.com.dlpsystems.cycle.domain.usecase

import br.com.dlpsystems.cycle.domain.model.Citation
import br.com.dlpsystems.cycle.domain.model.StrawStage
import kotlin.math.abs
import javax.inject.Inject

data class StrawClassificationResult(
    val stage: StrawStage,
    val description: String,
    val clinicalInsight: String,
    val citation: Citation,
)

/**
 * Classifica o estágio de transição menopáusica conforme o Consórcio STRAW+10.
 * Referência: HARLOW, S. D. et al. Climacteric, 15(2): 105-114, 2012 (Consórcio STRAW N > 10.000).
 */
class ClassifyStrawStageUseCase @Inject constructor() {

    operator fun invoke(
        recentCycleLengths: List<Int> = emptyList(),
        currentAmenorrheaDays: Int = 0,
        isConfirmedMenopause12Months: Boolean = false,
        yearsSinceMenopause: Double = 0.0,
    ): StrawClassificationResult {
        val (stage, desc, insight) = when {
            isConfirmedMenopause12Months && yearsSinceMenopause > 5.0 -> Triple(
                StrawStage.POSTMENOPAUSE_LATE,
                "Pós-Menopausa Tardia (Estágio +2)",
                "Fase de longevidade ativa: foco na preservação de densidade óssea, massa muscular e saúde cardiovascular.",
            )
            isConfirmedMenopause12Months -> Triple(
                StrawStage.POSTMENOPAUSE_EARLY,
                "Pós-Menopausa Inicial (Estágio +1)",
                "Primeiros 5 anos pós-menopausa: fase de maior velocidade de reabsorção óssea; acompanhe cálcio e vitamina D.",
            )
            currentAmenorrheaDays >= 60 -> Triple(
                StrawStage.PERIMENOPAUSE_LATE,
                "Perimenopausa Tardia (Estágio -1)",
                "Intervalo de amenorreia igual ou superior a 60 dias. Frequência elevada de sintomas vasomotores.",
            )
            hasConsecutiveDifferenceOf7DaysOrMore(recentCycleLengths) -> Triple(
                StrawStage.PERIMENOPAUSE_EARLY,
                "Perimenopausa Precoce (Estágio -2)",
                "Variação persistente de 7 ou mais dias no comprimento de ciclos consecutivos, refletindo ciclos anovulatórios.",
            )
            else -> Triple(
                StrawStage.REPRODUCTIVE_LATE,
                "Fase Reprodutiva Tardia (Estágio -3)",
                "Ciclos ainda predominantemente regulares, com alterações endócrinas sutis.",
            )
        }

        val citation = Citation(
            authors = "Harlow, S. D. et al. (Consórcio STRAW+10)",
            source = "Climacteric, 15(2): 105-114, 2012",
            finding = "Estadiamento clínico do envelhecimento reprodutivo feminino baseado em grandes coortes (N > 10.000 mulheres).",
            url = "https://doi.org/10.3109/13697137.2011.650656",
        )

        return StrawClassificationResult(
            stage = stage,
            description = desc,
            clinicalInsight = insight,
            citation = citation,
        )
    }

    private fun hasConsecutiveDifferenceOf7DaysOrMore(lengths: List<Int>): Boolean {
        if (lengths.size < 2) return false
        for (i in 0 until lengths.size - 1) {
            if (abs(lengths[i] - lengths[i + 1]) >= 7) {
                return true
            }
        }
        return false
    }
}
