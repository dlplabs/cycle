package br.com.dlpsystems.cycle.domain.usecase

import br.com.dlpsystems.cycle.domain.model.Citation
import br.com.dlpsystems.cycle.domain.model.MenopauseRatingScaleResult
import javax.inject.Inject

/**
 * Calcula a pontuação e subescalas da Menopause Rating Scale (MRS).
 * Validação psicométrica internacional: HEINEMANN, L. A. et al. Health Qual Life Outcomes, 2: 67, 2004 (N = 11.140 mulheres).
 */
class CalculateMrsScoreUseCase @Inject constructor() {

    operator fun invoke(itemScores: Map<Int, Int>): MenopauseRatingScaleResult {
        // Subescala Somato-Vegetativa: itens 1 (fogachos/suores), 2 (coração), 3 (sono), 11 (dores articulares)
        val somato = listOf(1, 2, 3, 11).sumOf { itemScores[it] ?: 0 }

        // Subescala Psicológica: itens 4 (humor), 5 (irritabilidade), 6 (ansiedade), 7 (esgotamento)
        val psych = listOf(4, 5, 6, 7).sumOf { itemScores[it] ?: 0 }

        // Subescala Urogenital: itens 8 (sexualidade), 9 (queixas urinárias), 10 (secura vaginal)
        val urogenital = listOf(8, 9, 10).sumOf { itemScores[it] ?: 0 }

        val total = somato + psych + urogenital

        val severity = when {
            total >= 17 -> "Severa a Muito Severa"
            total >= 9 -> "Moderada a Severa"
            total >= 5 -> "Leve a Moderada"
            else -> "Pouco Sintomática ou Assintomática"
        }

        val citation = Citation(
            authors = "Heinemann, L. A. et al.",
            source = "Health and Quality of Life Outcomes, 2: 67, 2004",
            finding = "Validação internacional da escala MRS em três domínios funcionais invariantes (N = 11.140 mulheres).",
            url = "https://doi.org/10.1186/1477-7525-2-67",
        )

        return MenopauseRatingScaleResult(
            somatoVegetativeScore = somato,
            psychologicalScore = psych,
            urogenitalScore = urogenital,
            totalScore = total,
            severity = severity,
            citation = citation,
        )
    }
}
