package br.com.dlpsystems.cycle.domain.usecase

import br.com.dlpsystems.cycle.domain.model.ContractionRecord
import br.com.dlpsystems.cycle.domain.model.EnergyLevel
import br.com.dlpsystems.cycle.domain.model.LunarLogEntry
import br.com.dlpsystems.cycle.domain.model.MoonPhase
import br.com.dlpsystems.cycle.domain.model.PregnancyCalculationMethod
import br.com.dlpsystems.cycle.domain.model.PregnancyProfile
import br.com.dlpsystems.cycle.domain.model.Trimester
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class HealthFeaturesUseCasesTest {

    private val calculateGestationalAge = CalculateGestationalAgeUseCase()
    private val getPregnancyContent = GetPregnancyContentUseCase()
    private val evaluateContractions = EvaluateContractionsUseCase()
    private val calculateMoonPhase = CalculateMoonPhaseUseCase()
    private val analyzeLunarPatterns = AnalyzeLunarPatternsUseCase()

    // ==========================================
    // 1. TESTES DE IDADE GESTACIONAL (Regra de Naegele e Ultrassom)
    // ==========================================
    @Test
    fun gestationalAgeCalculatesProperlyWithLmp() {
        val lmp = LocalDate.of(2026, 1, 1)
        val profile = PregnancyProfile(
            isActive = true,
            calculationMethod = PregnancyCalculationMethod.DUM,
            lastMenstrualPeriod = lmp,
        )
        // 70 dias após a DUM = exatamente 10 semanas (1º Trimestre)
        val target = lmp.plusDays(70)
        val age = calculateGestationalAge(profile, target)

        assertNotNull(age)
        assertEquals(10, age!!.weeks)
        assertEquals(0, age.days)
        assertEquals(Trimester.FIRST, age.trimester)
        assertEquals(lmp.plusDays(280), age.estimatedDueDate)
        assertEquals(210, age.daysRemaining)
        assertFalse(age.isPostTerm)
    }

    @Test
    fun gestationalAgeTrimestersTransitionCorrectly() {
        val lmp = LocalDate.of(2026, 1, 1)
        val profile = PregnancyProfile(
            isActive = true,
            calculationMethod = PregnancyCalculationMethod.DUM,
            lastMenstrualPeriod = lmp,
        )

        // 14 semanas = 2º Trimestre
        val age2nd = calculateGestationalAge(profile, lmp.plusWeeks(14))
        assertEquals(Trimester.SECOND, age2nd?.trimester)

        // 28 semanas = 3º Trimestre
        val age3rd = calculateGestationalAge(profile, lmp.plusWeeks(28))
        assertEquals(Trimester.THIRD, age3rd?.trimester)

        // 42 semanas = Pós-termo
        val agePostTerm = calculateGestationalAge(profile, lmp.plusWeeks(42))
        assertTrue(agePostTerm!!.isPostTerm)
    }

    @Test
    fun gestationalAgeWithDirectEdd() {
        val edd = LocalDate.of(2026, 10, 8)
        val profile = PregnancyProfile(
            isActive = true,
            calculationMethod = PregnancyCalculationMethod.DPP,
            estimatedDueDate = edd,
        )
        val age = calculateGestationalAge(profile, edd)
        assertNotNull(age)
        assertEquals(40, age!!.weeks)
        assertEquals(0, age.daysRemaining)
    }

    @Test
    fun inactivePregnancyReturnsNull() {
        val profile = PregnancyProfile(isActive = false)
        val age = calculateGestationalAge(profile)
        assertNull(age)
    }

    // ==========================================
    // 2. CONTEÚDOS CLÍNICOS E FONTES
    // ==========================================
    @Test
    fun pregnancyArticlesIncludeVerifiableSources() {
        val week4 = getPregnancyContent.getWeekInfo(4)
        assertFalse(week4.sourceName.isBlank())
        assertTrue(week4.referenceUrl.startsWith("http"))

        val birthArticles = getPregnancyContent.getChildbirthEducation()
        assertEquals(2, birthArticles.size)
        assertTrue(birthArticles.any { it.title.contains("Normal") || it.title.contains("Vaginal") })
        assertTrue(birthArticles.any { it.title.contains("Cesariana") })

        val exercise = getPregnancyContent.getExerciseGuidance()
        assertTrue(exercise.contraindicationsOrAlerts.isNotEmpty())
    }

    // ==========================================
    // 3. CRONÔMETRO DE CONTRAÇÕES
    // ==========================================
    @Test
    fun contractionEvaluationCalculatesAveragesAndProvidesDisclaimer() {
        val records = listOf(
            ContractionRecord("1", 1000L, 1050L, 50, null),
            ContractionRecord("2", 1350L, 1410L, 60, 300),
            ContractionRecord("3", 1700L, 1750L, 50, 290),
        )
        val evaluation = evaluateContractions(records)
        assertEquals(53, evaluation.averageDurationSeconds)
        assertEquals(295, evaluation.averageIntervalSeconds)
        assertEquals(3, evaluation.totalCount)
        assertTrue(evaluation.clinicalGuidanceNotice.contains("NÃO diagnostica"))
        assertTrue(evaluation.emergencyWarning.contains("atendimento médico"))
    }

    // ==========================================
    // 4. MANDALA LUNAR E PADRÕES DESCRITIVOS
    // ==========================================
    @Test
    fun moonPhaseCalculatesConsistentPhase() {
        val date = LocalDate.of(2024, 1, 11) // Dia de Lua Nova conhecida
        val phase = calculateMoonPhase(date)
        assertEquals(MoonPhase.NEW_MOON, phase)
    }

    @Test
    fun lunarPatternAnalysisDemandsSufficientData() {
        val fewLogs = listOf(
            LunarLogEntry(LocalDate.of(2026, 1, 1), MoonPhase.NEW_MOON, null, EnergyLevel.HIGH, null),
            LunarLogEntry(LocalDate.of(2026, 1, 2), MoonPhase.WAXING_CRESCENT, null, EnergyLevel.MODERATE, null),
        )
        val summaryFew = analyzeLunarPatterns(fewLogs)
        assertFalse(summaryFew.isDataSufficient)

        val enoughLogs = (1..10).map { i ->
            LunarLogEntry(
                LocalDate.of(2026, 1, i),
                MoonPhase.FULL_MOON,
                null,
                if (i % 2 == 0) EnergyLevel.HIGH else EnergyLevel.MODERATE,
                null,
            )
        }
        val summaryEnough = analyzeLunarPatterns(enoughLogs)
        assertTrue(summaryEnough.isDataSufficient)
        assertEquals(10, summaryEnough.totalRecords)
        assertTrue(summaryEnough.disclaimer.contains("não estabelece relação de causa e efeito"))
    }
}
