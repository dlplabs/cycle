package br.com.dlpsystems.cycle.domain.usecase

import br.com.dlpsystems.cycle.domain.model.Citation
import br.com.dlpsystems.cycle.domain.model.EpdsEvaluation
import br.com.dlpsystems.cycle.domain.model.EpdsRiskCategory
import javax.inject.Inject

/**
 * Calcula a pontuação e estratificação de risco da Escala de Depressão Pós-Parto de Edimburgo (EPDS).
 * Validação na população brasileira: SANTOS, I. S. et al. J Affect Disord, 102(1-3): 261-267, 2007 (N = 4.453 puérperas).
 */
class CalculateEpdsScoreUseCase @Inject constructor() {

    operator fun invoke(answers: Map<Int, Int>): EpdsEvaluation {
        val totalScore = answers.values.sum()
        val item10Score = answers[10] ?: 0
        val hasSelfHarmIdeation = item10Score > 0

        val category = when {
            totalScore >= 12 -> EpdsRiskCategory.PROBABLE_MAJOR_DEPRESSION
            totalScore >= 10 -> EpdsRiskCategory.MODERATE_DISTRESS
            else -> EpdsRiskCategory.NORMAL
        }

        val recommendation = when {
            hasSelfHarmIdeation -> "Atenção e acolhimento prioritário: Você relatou pensamentos difíceis sobre si mesma. " +
                "Por favor, converse com alguém de sua confiança agora ou ligue gratuitamente para o CVV (188). Sua vida e seu bem-estar são preciosos."
            category == EpdsRiskCategory.PROBABLE_MAJOR_DEPRESSION -> "Sua pontuação sugere sofrimento emocional significativo compatível com depressão pós-parto. " +
                "Recomendamos fortemente agendar uma consulta acolhedora com seu obstetra, psicólogo ou equipe de saúde da família."
            category == EpdsRiskCategory.MODERATE_DISTRESS -> "Você está apresentando sinais moderados de sobrecarga e ansiedade puerperal. " +
                "Priorize momentos de descanso, compartilhe tarefas e mencione como está se sentindo na sua próxima consulta de revisão."
            else -> "Sua pontuação está dentro dos padrões esperados de adaptação ao pós-parto. " +
                "Continue cuidando de si mesma com leveza, descanso e carinho."
        }

        val citation = Citation(
            authors = "Santos, I. S. et al. (Coorte de Nascimentos de Pelotas)",
            source = "Journal of Affective Disorders, 102(1-3): 261-267, 2007",
            finding = "Validação da EPDS com pontos de corte >=10 e >=12 e sensibilidade para rastreio puerperal (N = 4.453 puérperas).",
            url = "https://doi.org/10.1016/j.jad.2006.12.007",
        )

        return EpdsEvaluation(
            totalScore = totalScore,
            riskCategory = category,
            hasSelfHarmIdeation = hasSelfHarmIdeation,
            clinicalRecommendation = recommendation,
            emergencyHelpline = "Ligue 188 (Centro de Valorização da Vida - CVV)",
            citation = citation,
        )
    }
}
