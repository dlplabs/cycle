package br.com.dlpsystems.cycle.domain.model

import java.time.LocalDate

/**
 * Estádios do envelhecimento reprodutivo feminino conforme o Consórcio STRAW+10.
 * Referência: HARLOW, S. D. et al. Climacteric, 15(2): 105-114, 2012 (N > 10.000 mulheres).
 */
enum class StrawStage {
    REPRODUCTIVE_LATE,  // Estágio -3: ciclos regulares com início de flutuação folicular sutil
    PERIMENOPAUSE_EARLY,// Estágio -2: variação no comprimento de ciclos consecutivos >= 7 dias
    PERIMENOPAUSE_LATE, // Estágio -1: intervalo de amenorreia persistente >= 60 dias
    POSTMENOPAUSE_EARLY,// Estágio +1: primeiros 5 anos após a consolidação da menopausa
    POSTMENOPAUSE_LATE, // Estágio +2: envelhecimento somático e longevidade feminina
}

/**
 * Intensidade de episódios vasomotores (fogachos / ondas de calor).
 * Referência: AVIS, N. E. et al. JAMA Intern Med, 175(4): 531-539, 2015 (SWAN Cohort N = 3.302).
 */
enum class HotFlashIntensity {
    MILD,     // Sensação de calor passageira sem sudorese expressiva
    MODERATE, // Calor intenso acompanhado de rubor e sudorese
    SEVERE,   // Calor incapacitante com suor profuso, taquicardia ou calafrios
}

enum class HotFlashTrigger {
    STRESS,
    CAFFEINE,
    ALCOHOL,
    SPICY_FOOD,
    HOT_ENVIRONMENT,
    UNKNOWN,
}

data class HotFlashLog(
    val id: String,
    val timestampEpochMs: Long,
    val intensity: HotFlashIntensity,
    val durationMinutes: Int,
    val isNightSweat: Boolean,
    val trigger: HotFlashTrigger = HotFlashTrigger.UNKNOWN,
    val heartPalpitations: Boolean = false,
    val notes: String = "",
)

/**
 * Avaliação da Menopause Rating Scale (MRS).
 * Referência: HEINEMANN, L. A. et al. Health Qual Life Outcomes, 2: 67, 2004 (N = 11.140 mulheres).
 */
data class MenopauseRatingScaleResult(
    val somatoVegetativeScore: Int, // Itens 1, 2, 3 e 11 (0 a 16)
    val psychologicalScore: Int,    // Itens 4, 5, 6 e 7 (0 a 16)
    val urogenitalScore: Int,       // Itens 8, 9 e 10 (0 a 12)
    val totalScore: Int,            // 0 a 44
    val severity: String,           // Leve (0-4), Moderada (5-8), Severa (>=9)
    val citation: Citation,
)

/**
 * Confirmação da menopausa definitiva e alerta de segurança.
 * Referência: BERAL, V. et al. The Lancet, 362: 419-427, 2003 (Million Women Study N = 1.388.536).
 */
data class MenopauseConfirmation(
    val isConfirmed: Boolean,
    val lastMenstruationDate: LocalDate?,
    val consecutiveAmenorrheaMonths: Int,
    val confirmationDate: LocalDate?,
    val hasPostmenopausalBleedingAlert: Boolean, // Sangramento após 12 meses exige US transvaginal
    val clinicalGuidanceNotice: String,
    val citation: Citation,
)

enum class BoneHealthClassification {
    NORMAL,       // T-Score > -1.0
    OSTEOPENIA,   // T-Score entre -1.0 e -2.5
    OSTEOPOROSIS, // T-Score < -2.5
}

/**
 * Rastreamento de saúde óssea e prevenção de fraturas.
 * Referência: MANSON, J. E. et al. JAMA, 310(13): 1353-1368, 2013 (WHI Cohort N = 161.808).
 */
data class BoneHealthProfile(
    val latestDexaScanDate: LocalDate? = null,
    val spineTScore: Double? = null,
    val femurTScore: Double? = null,
    val classification: BoneHealthClassification = BoneHealthClassification.NORMAL,
    val dailyCalciumMgTarget: Int = 1200,
    val dailyVitaminDUnitsTarget: Int = 1000,
    val resistanceTrainingSessionsPerWeekTarget: Int = 3,
    val citation: Citation,
)

data class CardiovascularVitalsLog(
    val date: LocalDate,
    val systolicMmHg: Int,
    val diastolicMmHg: Int,
    val restingHeartRateBpm: Int? = null,
    val fastingGlucoseMgDl: Int? = null,
    val totalCholesterolMgDl: Int? = null,
    val ldlMgDl: Int? = null,
    val hdlMgDl: Int? = null,
)

/**
 * Sessão de treinamento muscular do assoalho pélvico (exercícios de Kegel).
 * Referência: DUMOULIN, C. et al. Cochrane Database Syst Rev, 10: CD005654, 2018 (N > 2.000).
 */
data class PelvicFloorSession(
    val id: String,
    val timestampEpochMs: Long,
    val completedCycles: Int,
    val contractionSeconds: Int,
    val relaxationSeconds: Int,
    val totalDurationMinutes: Int,
)
