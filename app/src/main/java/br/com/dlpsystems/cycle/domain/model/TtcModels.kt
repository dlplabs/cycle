package br.com.dlpsystems.cycle.domain.model

import java.time.LocalDate

/**
 * Tipos de muco cervical conforme o Método de Ovulação Billings.
 * Referência: BILLINGS, E. L. et al. Symptoms of Ovulation by Mucus. The Lancet, 1972.
 * FRANK-HERRMANN, P. et al. Human Reproduction, 22(5): 1310-1319, 2007 (N = 900 mulheres / 17.638 ciclos).
 */
enum class CervicalMucusType {
    DRY,         // Não fértil (seco / ausente)
    STICKY,      // Baixa fertilidade (espesso / pegajoso)
    CREAMY,      // Transição (cremoso / esbranquiçado)
    EGG_WHITE,   // Máxima fertilidade (clara de ovo / elástico > 2cm)
    WATERY,      // Alta fertilidade (aquoso / fluido transparente)
}

/**
 * Resultados de tiras de teste rápido de LH (Hormônio Luteinizante) urinário.
 */
enum class LhTestResult {
    NOT_TESTED,
    NEGATIVE,    // Linha de teste ausente ou mais clara que o controle
    LOW,         // Traço leve de LH
    HIGH,        // Intensidade próxima ao controle
    PEAK,        // Pico de LH: linha de teste igual ou mais escura que o controle
}

/**
 * Posição e textura do colo uterino (palpação orientada).
 */
enum class CervixPosition {
    UNKNOWN,
    LOW_HARD_CLOSED, // Pós-menstrual ou lútea precoce (baixo, firme e fechado)
    MID_MEDIUM_CLOSED,
    HIGH_SOFT_OPEN,  // Período fértil/ovulatório (alto, macio e aberto)
}

/**
 * Nível de probabilidade diária de concepção biológica.
 * Calibrado pela coorte europeia de fecundabilidade:
 * DUNSON, D. B. et al. Human Reproduction, 17(5): 1399-1403, 2002 (N = 1.077 casais / 3.160 ciclos).
 */
enum class FertilityLevel {
    LOW,        // Fora da janela fértil
    MEDIUM,     // 4 a 5 dias antes da ovulação
    HIGH,       // 3 dias antes ou no dia da ovulação
    PEAK,       // 1 a 2 dias antes da ovulação (ápice de fecundabilidade)
}

data class TtcDailyEntry(
    val date: LocalDate,
    val bbtCelsius: Double? = null,
    val mucus: CervicalMucusType? = null,
    val lhResult: LhTestResult = LhTestResult.NOT_TESTED,
    val cervix: CervixPosition = CervixPosition.UNKNOWN,
    val intercourseLogged: Boolean = false,
    val tookFolicAcid: Boolean = false,
    val notes: String = "",
)

data class FertilityWindowPrediction(
    val cycleStartDate: LocalDate,
    val estimatedOvulationDate: LocalDate,
    val fertileWindowStartDate: LocalDate, // 5 dias antes da ovulação
    val fertileWindowEndDate: LocalDate,   // 1 dia após a ovulação
    val peakFertilityDate: LocalDate,     // Véspera da ovulação estimada
    val dailyProbability: Double,         // 0.0 a 0.33 conforme Dunson et al.
    val level: FertilityLevel,
    val isBbtShiftConfirmed: Boolean,      // Regra 3 sobre 6 confirmada
    val citation: Citation,
)
