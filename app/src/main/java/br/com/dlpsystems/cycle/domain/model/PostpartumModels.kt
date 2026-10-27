package br.com.dlpsystems.cycle.domain.model

import java.time.LocalDate

/**
 * Fases fisiológicas do puerpério.
 * Referência: OPPENHEIMER, C. A. et al. The duration of lochia. Br J Obstet Gynaecol, 1986 (N > 1.000).
 */
enum class PuerperiumPhase {
    IMMEDIATE, // 1º ao 10º dia pós-parto (hemostasia e início da involução)
    MEDIATE,   // 11º ao 42º dia pós-parto (a clássica quarentena)
    LATE,      // 43º dia em diante (até o retorno ovulatório)
}

/**
 * Tipos de lóquios e evolução fisiológica do sangramento pós-parto.
 */
enum class LochiaType {
    RUBRA,     // Vermelho vivo a escuro com pequenos coágulos fisiológicos (dias 1-4)
    SEROSA,    // Castanho-rosado, fluido aquoso (dias 5-10)
    ALBA,      // Amarelado ou esbranquiçado (dias 11-21+)
    ABNORMAL,  // Sangramento súbito volumoso ou odor fétido (alerta de hemorragia/infecção)
}

enum class DeliveryType {
    VAGINAL_INTACT,
    VAGINAL_LACERATION,
    EPISIOTOMY,
    CESAREAN,
}

data class PostpartumDailyCheckIn(
    val date: LocalDate,
    val daysPostpartum: Int,
    val lochia: LochiaType,
    val woundPainScale: Int, // 0 a 10
    val uterineCramping: Boolean, // Cólicas de involução induzidas por ocitocina
    val sleepHoursTotal: Double,
    val emotionalEnergyScale: Int, // 1 a 5
    val notes: String = "",
)

/**
 * Categorias de risco na Escala de Depressão Pós-Parto de Edimburgo (EPDS).
 * Validação brasileira: SANTOS, I. S. et al. J Affect Disord, 102(1-3): 261-267, 2007 (N = 4.453 puérperas).
 */
enum class EpdsRiskCategory {
    NORMAL,                     // Score 0 a 9: Adaptação puerperal habitual
    MODERATE_DISTRESS,          // Score 10 a 11: Sofrimento psíquico moderado
    PROBABLE_MAJOR_DEPRESSION,  // Score 12 ou superior: Forte probabilidade de depressão
}

data class EpdsAnswerOption(
    val score: Int, // 0 a 3
    val text: String,
)

data class EpdsQuestion(
    val index: Int, // 1 a 10
    val prompt: String,
    val options: List<EpdsAnswerOption>,
)

data class EpdsEvaluation(
    val totalScore: Int, // 0 a 30
    val riskCategory: EpdsRiskCategory,
    val hasSelfHarmIdeation: Boolean, // Se resposta no item 10 for > 0
    val clinicalRecommendation: String,
    val emergencyHelpline: String = "Ligue 188 (Centro de Valorização da Vida - CVV)",
    val citation: Citation,
)
