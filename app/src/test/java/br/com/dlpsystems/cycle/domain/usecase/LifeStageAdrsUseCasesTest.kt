package br.com.dlpsystems.cycle.domain.usecase

import br.com.dlpsystems.cycle.domain.model.CervicalMucusType
import br.com.dlpsystems.cycle.domain.model.ContractionRecord
import br.com.dlpsystems.cycle.domain.model.EpdsRiskCategory
import br.com.dlpsystems.cycle.domain.model.FertilityLevel
import br.com.dlpsystems.cycle.domain.model.LhTestResult
import br.com.dlpsystems.cycle.domain.model.StorageCondition
import br.com.dlpsystems.cycle.domain.model.StrawStage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class LifeStageAdrsUseCasesTest {

    private val calculateFertilityWindow = CalculateFertilityWindowUseCase()
    private val evaluateContractions = EvaluateContractionsUseCase()
    private val calculateEpdsScore = CalculateEpdsScoreUseCase()
    private val evaluateLamCriteria = EvaluateLamCriteriaUseCase()
    private val calculateMilkExpiration = CalculateMilkExpirationUseCase()
    private val classifyStrawStage = ClassifyStrawStageUseCase()
    private val calculateMrsScore = CalculateMrsScoreUseCase()
    private val confirmMenopause = ConfirmMenopauseUseCase()

    // ==========================================
    // ADR-0002: TENTANTE E MÉTODO SINTO-TÉRMICO
    // ==========================================
    @Test
    fun fertilityWindowCalculatesProbabilitiesAccordingToDunsonCohort() {
        val cycleStart = LocalDate.of(2026, 1, 1)
        // Ciclo de 28 dias: ovulação esperada no dia 15/01 (14 dias após início)
        // 2 dias antes da ovulação = 13/01 (pico de fertilidade de 33%)
        val targetDayMinus2 = LocalDate.of(2026, 1, 13)

        val prediction = calculateFertilityWindow(
            cycleStartDate = cycleStart,
            targetDate = targetDayMinus2,
            averageCycleLengthDays = 28,
        )

        assertEquals(LocalDate.of(2026, 1, 15), prediction.estimatedOvulationDate)
        assertEquals(LocalDate.of(2026, 1, 10), prediction.fertileWindowStartDate)
        assertEquals(LocalDate.of(2026, 1, 16), prediction.fertileWindowEndDate)
        assertEquals(0.33, prediction.dailyProbability, 0.001)
        assertEquals(FertilityLevel.PEAK, prediction.level)
        assertFalse(prediction.citation.authors.isBlank())
        assertTrue(prediction.citation.url.contains("doi.org"))
    }

    @Test
    fun fertilityWindowElevatesWithLhPeakOrEggWhiteMucus() {
        val cycleStart = LocalDate.of(2026, 1, 1)
        val farDay = LocalDate.of(2026, 1, 5)

        val predictionLh = calculateFertilityWindow(
            cycleStartDate = cycleStart,
            targetDate = farDay,
            recentLhResult = LhTestResult.PEAK,
        )
        assertEquals(0.33, predictionLh.dailyProbability, 0.001)
        assertEquals(FertilityLevel.PEAK, predictionLh.level)

        val predictionMucus = calculateFertilityWindow(
            cycleStartDate = cycleStart,
            targetDate = farDay,
            recentMucus = CervicalMucusType.EGG_WHITE,
        )
        assertEquals(0.30, predictionMucus.dailyProbability, 0.001)
        assertEquals(FertilityLevel.PEAK, predictionMucus.level)
    }

    @Test
    fun bbtShiftConfirmsOvulationWithThreeOverSixRule() {
        val cycleStart = LocalDate.of(2026, 1, 1)
        // 6 leituras na linha de base ~36.3°C seguidas de 3 leituras >= 36.5°C
        val bbtReadings = listOf(36.25, 36.30, 36.28, 36.32, 36.30, 36.31, 36.55, 36.60, 36.58)

        val prediction = calculateFertilityWindow(
            cycleStartDate = cycleStart,
            recentBbtReadings = bbtReadings,
        )
        assertTrue(prediction.isBbtShiftConfirmed)
    }

    // ==========================================
    // ADR-0003: GESTAÇÃO E REGRA 5-1-1
    // ==========================================
    @Test
    fun contractionEvaluationDetectsRule511LaborPattern() {
        // 10 contrações de 60 segundos com intervalo de 240s (~4 minutos)
        val records511 = (1..10).map { i ->
            ContractionRecord(
                id = i.toString(),
                startTimeEpochMs = i * 300_000L,
                endTimeEpochMs = (i * 300_000L) + 60_000L,
                durationSeconds = 60,
                intervalSecondsFromPrevious = if (i == 1) null else 240,
            )
        }

        val result = evaluateContractions(records511)
        assertTrue(result.isRule511Met)
        assertTrue(result.clinicalGuidanceNotice.contains("Padrão 5-1-1"))
        assertTrue(result.citation.authors.contains("Zhang"))
    }

    // ==========================================
    // ADR-0004: PÓS-PARTO E ESCALA EPDS
    // ==========================================
    @Test
    fun epdsEvaluationCategorizesRiskAndTriggersItem10Safeguard() {
        // Cenário 1: Pontuação baixa normal
        val normalAnswers = mapOf(1 to 0, 2 to 1, 3 to 1, 4 to 0, 5 to 0, 6 to 1, 7 to 0, 8 to 0, 9 to 0, 10 to 0)
        val normalEval = calculateEpdsScore(normalAnswers)
        assertEquals(3, normalEval.totalScore)
        assertEquals(EpdsRiskCategory.NORMAL, normalEval.riskCategory)
        assertFalse(normalEval.hasSelfHarmIdeation)

        // Cenário 2: Pontuação moderada (10 pontos)
        val moderateAnswers = mapOf(1 to 1, 2 to 1, 3 to 1, 4 to 1, 5 to 1, 6 to 1, 7 to 1, 8 to 1, 9 to 2, 10 to 0)
        val modEval = calculateEpdsScore(moderateAnswers)
        assertEquals(10, modEval.totalScore)
        assertEquals(EpdsRiskCategory.MODERATE_DISTRESS, modEval.riskCategory)

        // Cenário 3: Pontuação alta com item 10 positivo (salvaguarda CVV)
        val criticalAnswers = mapOf(1 to 2, 2 to 2, 3 to 2, 4 to 2, 5 to 1, 6 to 2, 7 to 2, 8 to 1, 9 to 2, 10 to 1)
        val critEval = calculateEpdsScore(criticalAnswers)
        assertEquals(17, critEval.totalScore)
        assertEquals(EpdsRiskCategory.PROBABLE_MAJOR_DEPRESSION, critEval.riskCategory)
        assertTrue(critEval.hasSelfHarmIdeation)
        assertTrue(critEval.clinicalRecommendation.contains("188"))
        assertTrue(critEval.citation.authors.contains("Santos"))
    }

    // ==========================================
    // ADR-0005: LACTAÇÃO, MELA E BANCO DE LEITE
    // ==========================================
    @Test
    fun lamEvaluationValidatesAllThreeCriteria() {
        // Elegível: Bebê com 90 dias, sem sangramento pós-dia 56, amamentação exclusiva dia/noite
        val eligible = evaluateLamCriteria(
            babyAgeDays = 90,
            hasVaginalBleedingAfterDay56 = false,
            isExclusiveBreastfeedingDayAndNight = true,
        )
        assertTrue(eligible.isEligibleForLam)
        assertEquals(98.5, eligible.estimatedEfficacyPercentage)

        // Inelegível por idade (200 dias > 6 meses)
        val ineligibleAge = evaluateLamCriteria(
            babyAgeDays = 200,
            hasVaginalBleedingAfterDay56 = false,
            isExclusiveBreastfeedingDayAndNight = true,
        )
        assertFalse(ineligibleAge.isEligibleForLam)
        assertTrue(ineligibleAge.clinicalGuidanceNotice.contains("6 meses"))
    }

    @Test
    fun milkExpirationCalculatesCorrectLimits() {
        val now = 1_000_000_000L

        // Geladeira: 12 horas
        val fridgeStatus = calculateMilkExpiration(
            bottleId = "b1",
            expressedAtEpochMs = now,
            storageCondition = StorageCondition.REFRIGERATOR,
            currentEpochMs = now + (6L * 3600 * 1000), // 6 horas depois
        )
        assertFalse(fridgeStatus.isExpired)
        assertEquals(6.0, fridgeStatus.hoursRemaining, 0.1)

        // Geladeira expirada (13 horas depois)
        val expiredFridge = calculateMilkExpiration(
            bottleId = "b1",
            expressedAtEpochMs = now,
            storageCondition = StorageCondition.REFRIGERATOR,
            currentEpochMs = now + (13L * 3600 * 1000),
        )
        assertTrue(expiredFridge.isExpired)
        assertEquals(0.0, expiredFridge.hoursRemaining)
    }

    // ==========================================
    // ADR-0006: CLIMATÉRIO, STRAW+10 E ESCALA MRS
    // ==========================================
    @Test
    fun classifyStrawStageDetectsEarlyAndLatePerimenopause() {
        // Diferença consecutiva >= 7 dias (ex.: 28 e 36) -> PERIMENOPAUSE_EARLY (Estágio -2)
        val stageMinus2 = classifyStrawStage(
            recentCycleLengths = listOf(28, 36, 27),
            currentAmenorrheaDays = 15,
        )
        assertEquals(StrawStage.PERIMENOPAUSE_EARLY, stageMinus2.stage)

        // Amenorreia >= 60 dias -> PERIMENOPAUSE_LATE (Estágio -1)
        val stageMinus1 = classifyStrawStage(
            recentCycleLengths = listOf(28, 30),
            currentAmenorrheaDays = 65,
        )
        assertEquals(StrawStage.PERIMENOPAUSE_LATE, stageMinus1.stage)
    }

    @Test
    fun mrsScoreCalculatesSubscalesAccurately() {
        val scores = mapOf(
            1 to 3, 2 to 2, 3 to 2, 11 to 2, // Somato = 9
            4 to 2, 5 to 2, 6 to 1, 7 to 2,  // Psych = 7
            8 to 1, 9 to 1, 10 to 2,          // Urogenital = 4
        )
        val result = calculateMrsScore(scores)
        assertEquals(9, result.somatoVegetativeScore)
        assertEquals(7, result.psychologicalScore)
        assertEquals(4, result.urogenitalScore)
        assertEquals(20, result.totalScore)
        assertEquals("Severa a Muito Severa", result.severity)
        assertTrue(result.citation.authors.contains("Heinemann"))
    }

    // ==========================================
    // ADR-0007: MENOPAUSA E ALERTA DE SANGRAMENTO
    // ==========================================
    @Test
    fun confirmMenopauseEvaluatesTwelveMonthsAndAlertsPostmenopausalBleeding() {
        val lastPeriod = LocalDate.of(2025, 1, 1)
        val targetDate10Months = LocalDate.of(2025, 11, 1)

        val unconfirmed = confirmMenopause(
            lastMenstruationDate = lastPeriod,
            targetDate = targetDate10Months,
        )
        assertFalse(unconfirmed.isConfirmed)

        val targetDate14Months = LocalDate.of(2026, 3, 1)
        val confirmedSafe = confirmMenopause(
            lastMenstruationDate = lastPeriod,
            targetDate = targetDate14Months,
            hasBleedingAfterTwelveMonths = false,
        )
        assertTrue(confirmedSafe.isConfirmed)
        assertFalse(confirmedSafe.hasPostmenopausalBleedingAlert)

        val confirmedWithBleeding = confirmMenopause(
            lastMenstruationDate = lastPeriod,
            targetDate = targetDate14Months,
            hasBleedingAfterTwelveMonths = true,
        )
        assertTrue(confirmedWithBleeding.isConfirmed)
        assertTrue(confirmedWithBleeding.hasPostmenopausalBleedingAlert)
        assertTrue(confirmedWithBleeding.clinicalGuidanceNotice.contains("ultrassom transvaginal"))
        assertTrue(confirmedWithBleeding.citation.authors.contains("Beral"))
    }
}
