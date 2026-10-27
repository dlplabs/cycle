package br.com.dlpsystems.cycle.domain.model

/**
 * Representa os modos de vida e fases hormonais da mulher.
 * Baseado no consenso internacional STRAW+10 (Stages of Reproductive Aging Workshop)
 * e diretrizes da FIGO e ACOG.
 *
 * Referência científica:
 * HARLOW, S. D. et al. Executive summary of the Stages of Reproductive Aging Workshop + 10.
 * Climacteric, 15(2): 105-114, 2012. DOI: 10.3109/13697137.2011.650656 (N > 10.000).
 */
enum class LifeStageMode {
    REGULAR_CYCLE,       // Ciclo menstrual e bem-estar regular
    TRYING_TO_CONCEIVE,  // Tentante / Saúde pré-concepcional e fertilidade
    PREGNANCY,           // Gestação integral em três trimestres
    POSTPARTUM,          // Pós-parto imediato e mediato (quarentena e puerpério)
    LACTATION,           // Amamentação, conservação de leite e método MELA
    PERIMENOPAUSE,       // Transição menopáusica e climatério (estágios -2 e -1 STRAW+10)
    MENOPAUSE,           // Menopausa definitiva e longevidade ativa (estágios +1 e +2 STRAW+10)
}
