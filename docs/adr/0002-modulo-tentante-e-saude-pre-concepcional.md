# ADR-0002: Módulo Tentante e Saúde Pré-Concepcional (TTC & Fertility)

## Status
Aprovado

## Data
2026-10-27

## Contexto
Mulheres que desejam engravidar necessitam de um nível de precisão significativamente superior ao cálculo estatístico de calendário simples (método Ogino-Knaus). O período de maior fertilidade biológica ocorre nos 5 dias que antecedem a ovulação e no próprio dia da ovulação. A identificação tempestiva dessa janela fértil requer a triangulação de biomarcadores fisiológicos:
1. **Pico do Hormônio Luteinizante (LH Surge):** Detectado por tiras reagentes de urina, antecede a liberação do ovócito em 24 a 36 horas.
2. **Temperatura Basal Corporal (BBT):** Medida ao acordar com termômetro de 2 casas decimais, eleva-se entre 0,2°C e 0,5°C após a ovulação devido ao aumento de progesterona produzido pelo corpo lúteo (desvio térmico bifásico).
3. **Muco Cervical (Método de Ovulação Billings):** Transição de seco/cremoso para elástico, translúcido e semelhante à clara de ovo nos dias férteis, proporcionando nutrição e sobrevivência aos espermatozoides.
4. **Posição e Textura do Colo Uterino:** Alto, macio e aberto no período pré-ovulatório (palpação orientada).

Além disso, a saúde pré-concepcional exige suplementação de ácido fólico para prevenção de defeitos do tubo neural (DTN) e acompanhamento de tempo de tentativa conforme as diretrizes da American Society for Reproductive Medicine (ASRM).

## Evidências Científicas e Coortes Populacionais (N > 1.000)

1. **Estudo de Coorte Europeu de Fecundabilidade (Dunson, Colombo & Baird, 2002 / *Human Reproduction*, 17(5): 1399-1403):**
   - **População Estudada:** **N = 1.077 casais férteis acompanhados em 3.160 ciclos menstruais** em centros de Milão, Paris, Verona, Lugano e Düsseldorf.
   - **Achado Clínico Central:** Mapeou as probabilidades diárias matemáticas exatas de concepção em relação ao dia da ovulação (identificado pelo muco e BBT): a probabilidade é praticamente zero até 6 dias antes da ovulação, sobe para 0,10 a cinco dias antes, atinge o ápice de 0,33 a dois dias antes e 0,29 na véspera, caindo vertiginosamente para 0,08 no dia pós-ovulação. Este estudo fundamenta matematicamente o algoritmo da janela fértil do Cycle.
2. **Coorte Prospectiva de Métodos Baseados em Percepção da Fertilidade — Sensiplan (Frank-Herrmann et al., 2007 / *Human Reproduction*, 22(5): 1310-1319):**
   - **População Estudada:** **N = 900 mulheres em 17.638 ciclos menstruais** acompanhados prospectivamente no banco de dados da Universidade de Heidelberg (expandido para N > 1.500 no consórcio de planejamento natural).
   - **Achado Clínico Central:** Comprovou que a combinação de Temperatura Basal Corporal (com a regra estrita de '3 sobre 6') associada à observação do muco cervical fértil atinge eficácia biológica de 99,6% para identificação do período fértil e determinação da ovulação.
3. **Ensaios Clínicos Multicêntricos de Prevenção de Defeitos de Tubo Neural por Folato:**
   - **MRC Vitamin Study Research Group (Lancet, 1991; 338: 131-137):** Ensaio clínico randomizado multicêntrico internacional com **N = 1.817 gestantes**, comprovando redução de 72% de recorrência de espinha bífida e anencefalia com suplementação periconcepcional de ácido fólico.
   - **Coorte de Czeizel & Dudás (New England Journal of Medicine, 1992; 327: 1832-1835):** Ensaio clínico randomizado com **N = 4.753 mulheres planejando gravidez**, demonstrando eficácia de 100% na prevenção de ocorrência primária de DTN no grupo sob suplementação diária de folato iniciada pelo menos 1 mês antes da concepção.

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
   - Aplica o método sinto-térmico calibrado pelos dados da coorte de Dunson e Heidelberg:
     - Confirma a ovulação quando três temperaturas consecutivas forem superiores às seis temperaturas antecedentes imediatas (regra "3 sobre 6").
     - Se `LhTestResult == PEAK`, define a probabilidade de concepção como **Máxima** nas 24 a 48 horas seguintes.
     - Se `CervicalMucusType == EGG_WHITE`, projeta janela fértil ativa com destaque na tela inicial.

3. **Checklists Clínicos Pré-Concepcionais**:
   - **Ácido Fólico / Metilfolato:** Lembrete diário com meta padrão de 400 mcg (ou 5 mg para mulheres com histórico prévio ou diabetes).
   - **Exames Laboratoriais Pré-Concepcionais:** Sorologias (rubéola, toxoplasmose IgG/IgM, hepatites B e C, HIV, sífilis), tipagem sanguínea com fator Rh e Coombs indireto, hemograma e TSH.
   - **Calculadora de Tempo de Tentativa (Diretriz ASRM):**
     - Se idade da mulher < 35 anos e tempo de tentativa >= 12 meses: exibir recomendação acolhedora para avaliação médica da fertilidade conjugal.
     - Se idade da mulher >= 35 anos e tempo de tentativa >= 6 meses: antecipar a recomendação de consulta com especialista em reprodução assistida.

4. **UI do Módulo Tentante (`presentation/ttc`)**:
   - `FertilityGauge`: marcador semicircular no dashboard indicando "Baixa", "Média", "Alta" ou "Pico de Fertilidade".
   - `BbtChart`: gráfico de dispersão com linha de tendência conectando as medições de temperatura e destacando a linha de base (cobertura térmica).

## Consequências

### Positivas
- Fornece ferramentas científicas precisas para mulheres que buscam engravidar sem depender de estimativas cegas de calendário.
- Diminui o estresse de tentantes ao fornecer métricas objetivas de ovulação validadas em grandes amostras populacionais.
- Promove a adesão precoce ao ácido fólico, prevenindo desfechos neonatais adversos com eficácia comprovada em ensaios multicêntricos.

### Negativas / Mitigações
- Risco de ansiedade com o registro diário de temperatura. *Mitigação:* Textos acolhedores explicando que variações pontuais de sono ou febre podem alterar a temperatura basal sem comprometer o ciclo.

## Testes e Critérios de Aceite
- Testes unitários para `CalculateFertilityWindowUseCaseTest` com cenários de pico de LH, muco clara de ovo e confirmação térmica (regra 3 sobre 6).
- Verificação de renderização do gráfico de BBT com diferentes resoluções de tela.
