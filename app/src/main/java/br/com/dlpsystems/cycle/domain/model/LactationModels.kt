package br.com.dlpsystems.cycle.domain.model

import java.time.LocalDate

enum class BreastSide {
    LEFT,
    RIGHT,
    BOTH,
}

/**
 * Condições de armazenamento do leite materno conforme Ministério da Saúde e rBLH/Fiocruz.
 * Referência: VIEIRA, A. A. et al. Rev Bras Saude Mater Infant, 18(2): 345-354, 2018 (N > 160.000 doadoras/ano).
 */
enum class StorageCondition {
    ROOM_TEMPERATURE, // Até 2 horas (ambiente tropical até 25°C)
    REFRIGERATOR,     // Até 12 horas (em geladeira doméstica <= 4°C)
    FREEZER,          // Até 15 dias (congelador/freezer <= -10°C)
}

data class NursingSession(
    val id: String,
    val startTimeEpochMs: Long,
    val endTimeEpochMs: Long? = null,
    val durationLeftSeconds: Int,
    val durationRightSeconds: Int,
    val lastSide: BreastSide,
    val supplementedFormulaMl: Int = 0,
    val supplementedExpressedMilkMl: Int = 0,
    val notes: String = "",
)

data class MilkExpressedBottle(
    val id: String,
    val expressedAtEpochMs: Long,
    val volumeMl: Int,
    val breastSide: BreastSide,
    val storage: StorageCondition,
    val isConsumed: Boolean = false,
    val consumedAtEpochMs: Long? = null,
)

data class MilkExpirationStatus(
    val bottleId: String,
    val expressedAtEpochMs: Long,
    val expiresAtEpochMs: Long,
    val isExpired: Boolean,
    val hoursRemaining: Double,
    val storageCondition: StorageCondition,
    val citation: Citation,
)

data class BreastConditionReport(
    val date: LocalDate,
    val side: BreastSide,
    val engorgement: Boolean,
    val soreNipples: Boolean,
    val blockedDuct: Boolean,
    val feverCelsius: Double? = null, // Alerta para mastite se >= 38.0°C
    val painLevel: Int, // 1 a 5
)

/**
 * Avaliação dos critérios do Método da Amenorreia Lactacional (MELA / LAM).
 * Referência: LABBOK, M. H. et al. Pediatrics, 99(1): e2, 1997 (N = 4.118 lactantes / OMS).
 */
data class LamCriteriaEvaluation(
    val isBabyUnder6Months: Boolean,
    val isMotherInAmenorrhea: Boolean,      // Nenhum sangramento vaginal após 56 dias de puerpério
    val isExclusiveBreastfeeding: Boolean,  // Livre demanda dia e noite sem suplementação
    val isEligibleForLam: Boolean,          // Verdadeiro se os 3 critérios forem atendidos
    val estimatedEfficacyPercentage: Double,// 98.5% a 99.5% se elegível
    val clinicalGuidanceNotice: String,
    val citation: Citation,
)
