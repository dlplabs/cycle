package br.com.dlpsystems.cycle.domain.usecase

import br.com.dlpsystems.cycle.domain.model.ContractionRecord
import javax.inject.Inject

data class ContractionEvaluation(
    val averageDurationSeconds: Int,
    val averageIntervalSeconds: Int,
    val totalCount: Int,
    val clinicalGuidanceNotice: String,
    val emergencyWarning: String,
)

class EvaluateContractionsUseCase @Inject constructor() {

    operator fun invoke(records: List<ContractionRecord>): ContractionEvaluation {
        val count = records.size
        val avgDuration = if (count > 0) records.map { it.durationSeconds }.average().toInt() else 0
        val intervals = records.mapNotNull { it.intervalSecondsFromPrevious }
        val avgInterval = if (intervals.isNotEmpty()) intervals.average().toInt() else 0

        val notice = "Este cronômetro tem finalidade de registro pessoal. O aplicativo NÃO diagnostica trabalho de parto " +
            "nem substitui a avaliação presencial da sua equipe médica ou maternidade de referência."

        // Referência: Ministério da Saúde / Diretrizes de Assistência ao Parto Normal (2022) / ACOG
        // Regra geral de monitoramento em gestação a termo: contrações regulares de cerca de 50-60 segundos a cada 3 a 5 minutos por pelo menos 1 a 2 horas.
        val warning = "Procure atendimento médico imediato ou dirija-se à maternidade se houver: perda de líquido amniótico, " +
            "sangramento vaginal vivo, diminuição perceptível dos movimentos do bebê, febre, cefaleia intensa ou dor contínua " +
            "que não cede entre as contrações."

        return ContractionEvaluation(
            averageDurationSeconds = avgDuration,
            averageIntervalSeconds = avgInterval,
            totalCount = count,
            clinicalGuidanceNotice = notice,
            emergencyWarning = warning,
        )
    }
}
