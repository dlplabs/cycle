package br.com.dlpsystems.cycle.domain.usecase

import br.com.dlpsystems.cycle.domain.model.Citation
import br.com.dlpsystems.cycle.domain.model.LamCriteriaEvaluation
import javax.inject.Inject

/**
 * Avalia os 3 critérios clínicos do Método da Amenorreia Lactacional (MELA / LAM).
 * Baseado no estudo multicêntrico internacional da OMS:
 * LABBOK, M. H. et al. Pediatrics, 99(1): e2, 1997 (N = 4.118 lactantes).
 */
class EvaluateLamCriteriaUseCase @Inject constructor() {

    operator fun invoke(
        babyAgeDays: Int,
        hasVaginalBleedingAfterDay56: Boolean,
        isExclusiveBreastfeedingDayAndNight: Boolean,
    ): LamCriteriaEvaluation {
        val isBabyUnder6Months = babyAgeDays in 0..182
        val isMotherInAmenorrhea = !hasVaginalBleedingAfterDay56
        val isExclusive = isExclusiveBreastfeedingDayAndNight

        val isEligible = isBabyUnder6Months && isMotherInAmenorrhea && isExclusive
        val efficacy = if (isEligible) 98.5 else 0.0

        val notice = if (isEligible) {
            "Critérios do MELA atendidos: A amamentação exclusiva em livre demanda com amenorreia nos primeiros 6 meses " +
                "proporciona eficácia contraceptiva superior a 98%. Caso introduza fórmulas ou note sangramento, converse com seu médico para escolher outro método."
        } else {
            val reasons = mutableListOf<String>()
            if (!isBabyUnder6Months) reasons.add("o bebê atingiu ou ultrapassou 6 meses de vida")
            if (!isMotherInAmenorrhea) reasons.add("ocorreu retorno de sangramento menstrual após os 56 dias")
            if (!isExclusive) reasons.add("a amamentação não é exclusiva em livre demanda dia e noite")

            "Critérios do MELA não preenchidos porque: ${reasons.joinToString("; ")}. " +
                "A ovulação pode ter retornado. Recomendamos a adoção de um método contraceptivo seguro para a fase de amamentação (ex.: progestagênio isolado, DIU ou métodos de barreira)."
        }

        val citation = Citation(
            authors = "Labbok, M. H. et al. (Consórcio Bellagio / OMS)",
            source = "Pediatrics, 99(1): e2, 1997",
            finding = "Eficácia contraceptiva de 98,5% a 99,5% do MELA quando os 3 critérios biológicos são atendidos (N = 4.118 lactantes).",
            url = "https://doi.org/10.1542/peds.99.1.e2",
        )

        return LamCriteriaEvaluation(
            isBabyUnder6Months = isBabyUnder6Months,
            isMotherInAmenorrhea = isMotherInAmenorrhea,
            isExclusiveBreastfeeding = isExclusive,
            isEligibleForLam = isEligible,
            estimatedEfficacyPercentage = efficacy,
            clinicalGuidanceNotice = notice,
            citation = citation,
        )
    }
}
