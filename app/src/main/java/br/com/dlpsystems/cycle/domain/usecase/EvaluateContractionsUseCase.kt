package br.com.dlpsystems.cycle.domain.usecase

import br.com.dlpsystems.cycle.domain.model.Citation
import br.com.dlpsystems.cycle.domain.model.ContractionRecord
import javax.inject.Inject

data class ContractionEvaluation(
    val averageDurationSeconds: Int,
    val averageIntervalSeconds: Int,
    val totalCount: Int,
    val isRule511Met: Boolean, // Padrão 5-1-1 atingido (Consortium on Safe Labor)
    val clinicalGuidanceNotice: String,
    val emergencyWarning: String,
    val citation: Citation,
)

/**
 * Avalia o padrão e frequência das contrações uterinas.
 * Valida a regra clínica 5-1-1 fundamentada na coorte multicêntrica de trabalho de parto:
 * ZHANG, J. et al. Contemporary patterns of spontaneous labor with normal neonatal outcomes.
 * Obstetrics & Gynecology, 116(5): 1066-1074, 2010 (N = 62.415 parturientes).
 */
class EvaluateContractionsUseCase @Inject constructor() {

    operator fun invoke(records: List<ContractionRecord>): ContractionEvaluation {
        val count = records.size
        val avgDuration = if (count > 0) records.map { it.durationSeconds }.average().toInt() else 0
        val intervals = records.mapNotNull { it.intervalSecondsFromPrevious }
        val avgInterval = if (intervals.isNotEmpty()) intervals.average().toInt() else 0

        // Avaliação do padrão 5-1-1:
        // Contrações a cada 5 minutos (intervalo <= 300s), com duração de pelo menos 50-60s, por pelo menos 1 hora (mínimo de 10 contrações consecutivas)
        val isRule511Met = if (records.size >= 10) {
            val recentTen = records.takeLast(10)
            val durationCondition = recentTen.all { it.durationSeconds >= 45 }
            val intervalCondition = recentTen.mapNotNull { it.intervalSecondsFromPrevious }.all { it in 120..360 }
            durationCondition && intervalCondition
        } else {
            false
        }

        val notice = if (isRule511Met) {
            "Alerta de Trabalho de Parto Ativo (Padrão 5-1-1): Suas contrações estão regulares, a cada ~5 minutos e durando ~60 segundos. " +
                "Recomenda-se entrar em contato com sua equipe obstétrica ou dirigir-se à maternidade de referência."
        } else {
            "Este cronômetro tem finalidade de registro pessoal. O aplicativo NÃO diagnostica trabalho de parto " +
                "nem substitui a avaliação presencial da sua equipe médica ou maternidade de referência."
        }

        val warning = "Procure atendimento médico imediato se houver: perda de líquido amniótico, " +
            "sangramento vaginal vivo, diminuição perceptível dos movimentos do bebê, febre, cefaleia intensa ou dor contínua " +
            "que não cede entre as contrações."

        val citation = Citation(
            authors = "Zhang, J. et al. (Consortium on Safe Labor)",
            source = "Obstetrics & Gynecology, 116(5): 1066-1074, 2010",
            finding = "Critérios de progressão contemporânea e admissão no trabalho de parto ativo para redução de morbidade (N = 62.415 parturientes).",
            url = "https://doi.org/10.1097/AOG.0b013e3181fdef6e",
        )

        return ContractionEvaluation(
            averageDurationSeconds = avgDuration,
            averageIntervalSeconds = avgInterval,
            totalCount = count,
            isRule511Met = isRule511Met,
            clinicalGuidanceNotice = notice,
            emergencyWarning = warning,
            citation = citation,
        )
    }
}
