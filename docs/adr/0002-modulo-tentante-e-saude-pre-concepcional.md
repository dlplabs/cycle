# ADR-0002: Módulo Tentante e Saúde Pré-Concepcional (TTC & Fertility)

## Status
Aprovado

## Data
2026-10-27

## Contexto
Mulheres que desejam engravidar necessitam de um nível de precisão significativamente superior ao cálculo estatístico de calendário simples (método Ogino-Knaus). O período de maior fertilidade ocorre nos 5 dias que antecedem a ovulação e no próprio dia da ovulação. A identificação tempestiva dessa janela fértil requer a triangulação de biomarcadores fisiológicos:
1. **Pico do Hormônio Luteinizante (LH Surge):** Detectado por tiras reagentes de urina, antecede a liberação do ovócito em 24 a 36 horas.
2. **Temperatura Basal Corporal (BBT):** Medida ao acordar com termômetro de 2 casas decimais, eleva-se entre 0,2°C e 0,5°C após a ovulação devido ao aumento de progesterona produzido pelo corpo lúteo (desvio térmico bifásico).
3. **Muco Cervical (Método de Ovulação Billings):** Transição de seco/cremoso para elástico, translúcido e semelhante à clara de ovo nos dias férteis, proporcionando sobrevivência aos espermatozoides.
4. **Posição e Textura do Colo Uterino:** Alto, macio e aberto no período pré-ovulatório (palpação orientada).

Além disso, a saúde pré-concepcional exige suplementação de ácido fólico para prevenção de defeitos do tubo neural (DTN) e acompanhamento de tempo de tentativa conforme as diretrizes da American Society for Reproductive Medicine (ASRM).

## Decisão Arquitetural

1. **Modelagem de Domínio (`br.com.dlpsystems.cycle.domain.model.ttc`)**:
   ```kotlin
   enum class CervicalMucusType {
       DRY,         // Não fértil (seco / ausente)
       STICKY,      // Pouco fértil (espesso / pegajoso)
       CREAMY,      // Transição (cremoso / esbranquiçado)
       EGG_WHITE,   // Altamente fértil (clara de ovo / elástico > 2cm)
       WATERY,      // Fértil (aquoso / fluido transparente)
   }

   enum class LhTestResult {
       NOT_TESTED,
       NEGATIVE,    // Linha de teste ausente ou mais clara que controle
       LOW,         // Traço leve
       HIGH,        // Próximo à intensidade do controle
       PEAK,        // Linha de teste mais escura ou igual ao controle (pico)
   }

   enum class CervixPosition {
       UNKNOWN,
       LOW_HARD_CLOSED, // Pós-menstrual ou lútea precoce
       MID_MEDIUM_CLOSED,
       HIGH_SOFT_OPEN,  // Período ovulatório
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
   ```

2. **Algoritmo de Predição da Janela Fértil (`CalculateFertilityWindowUseCase`)**:
   - Integra o histórico de comprimento dos últimos 6 ciclos.
   - Aplica o método sinto-térmico:
     - Confirma a ovulação quando três temperaturas consecutivas forem superiores às seis temperaturas antecedentes imediatas (regra "3 sobre 6").
     - Se `LhTestResult == PEAK`, define a probabilidade de concepção como **Máxima** nas 24 a 48 horas seguintes.
     - Se `CervicalMucusType == EGG_WHITE`, projeta janela fértil ativa com destaque na tela inicial.

3. **Checklists Clínicos Pré-Concepcionais**:
   - **Ácido Fólico / Metilfolato:** Lembrete diário com meta padrão de 400 mcg (ou 5 mg para mulheres de alto risco de DTN).
   - **Exames Laboratoriais Pré-Concepcionais:** Sorologias (rubéola, toxoplasmose IgG/IgM, hepatites B e C, HIV, sífilis), tipagem sanguínea com fator Rh e Coombs indireto, hemograma e TSH.
   - **Calculadora de Tempo de Tentativa (ASRM):**
     - Se idade da mulher < 35 anos e tempo de tentativa >= 12 meses: exibir recomendação acolhedora para avaliação médica da fertilidade conjugal.
     - Se idade da mulher >= 35 anos e tempo de tentativa >= 6 meses: antecipar a recomendação de consulta com especialista em reprodução assistida.

4. **UI do Módulo Tentante (`presentation/ttc`)**:
   - `FertilityGauge`: marcador semicircular no dashboard indicando "Baixa", "Média", "Alta" ou "Pico de Fertilidade".
   - `BbtChart`: gráfico de dispersão com linha de tendência conectando as medições de temperatura e destacando a linha de base (cobertura térmica).

## Consequências

### Positivas
- Fornece ferramentas científicas precisas para mulheres que buscam engravidar sem depender de suposições genéricas.
- Diminui o estresse de tentantes ao fornecer métricas objetivas de ovulação.
- Promove a adesão precoce ao ácido fólico, prevenindo desfechos neonatais adversos.

### Negativas / Mitigações
- Risco de frustração ou ansiedade com o registro diário de temperatura. *Mitigação:* Textos acolhedores explicando que variações pontuais de sono ou febre podem alterar a temperatura basal sem comprometer o ciclo.

## Testes e Critérios de Aceite
- Testes unitários para `CalculateFertilityWindowUseCaseTest` com cenários de pico de LH, muco clara de ovo e confirmação térmica (regra 3 sobre 6).
- Verificação de renderização do gráfico de BBT com diferentes resoluções de tela.
