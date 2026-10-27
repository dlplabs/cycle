# ADR-0002: Módulo Tentante e Saúde Pré-Concepcional (TTC & Fertility)

## Status
Aprovado

## Data
2026-10-27

## Contexto
Mulheres que desejam engravidar necessitam de um nível de precisão significativamente superior ao cálculo estatístico de calendário simples (método Ogino-Knaus). O período de maior fertilidade biológica ocorre nos 5 dias que antecedem a ovulação e no próprio dia da ovulação [WILCOX et al., 1995; DUNSON et al., 2002]. A identificação tempestiva dessa janela fértil requer a triangulação de biomarcadores fisiológicos:
1. **Pico do Hormônio Luteinizante (LH Surge):** Detectado por tiras reagentes de urina, antecede a liberação do ovócito em 24 a 36 horas.
2. **Temperatura Basal Corporal (BBT):** Medida ao acordar com termômetro de 2 casas decimais, eleva-se entre 0,2°C e 0,5°C após a ovulação devido ao aumento de progesterona produzido pelo corpo lúteo (desvio térmico bifásico confirmado pela regra "3 sobre 6") [FRANK-HERRMANN et al., 2007].
3. **Muco Cervical (Método de Ovulação Billings):** Transição de seco/cremoso para elástico, translúcido e semelhante à clara de ovo nos dias férteis, proporcionando nutrição e sobrevivência aos espermatozoides [BILLINGS et al., 1972; FRANK-HERRMANN et al., 2007].
4. **Posição e Textura do Colo Uterino:** Alto, macio e aberto no período pré-ovulatório (palpação orientada).

Além disso, a saúde pré-concepcional exige suplementação diária de ácido fólico para prevenção de defeitos do tubo neural (DTN) [MRC, 1991; CZEIZEL & DUDÁS, 1992] e acompanhamento de tempo de tentativa conforme as diretrizes da American Society for Reproductive Medicine [PRACTICE COMMITTEE OF ASRM, 2021].

## Evidências Científicas e Coortes Populacionais (N > 1.000)

1. **Estudo de Coorte Europeu de Fecundabilidade [DUNSON, COLOMBO & BAIRD, 2002]:**
   - **População Estudada:** **N = 1.077 casais férteis acompanhados prospectivamente em 3.160 ciclos menstruais** em centros reprodutivos de Milão, Paris, Verona, Lugano e Düsseldorf.
   - **Achado Clínico Central:** Mapeou as probabilidades diárias matemáticas exatas de concepção em relação ao dia da ovulação (identificado pelo muco e BBT):
     - Dia -5: probabilidade de 0,10;
     - Dia -4: probabilidade de 0,16;
     - Dia -3: probabilidade de 0,14;
     - Dia -2: ápice de fertilidade de **0,33**;
     - Dia -1 (véspera da ovulação): probabilidade de **0,29**;
     - Dia 0 (dia da ovulação): probabilidade de 0,27;
     - Dia +1 pós-ovulação: queda abrupta para 0,08.
   - Este estudo fornece a base dos pesos estatísticos do algoritmo preditivo da janela fértil do Cycle.
2. **Coorte Prospectiva de Eficácia do Método Sinto-Térmico — Sensiplan [FRANK-HERRMANN et al., 2007]:**
   - **População Estudada:** **N = 900 mulheres em 17.638 ciclos menstruais** seguidos prospectivamente na Universidade de Heidelberg (expandido para N > 1.500 participantes).
   - **Achado Clínico Central:** Comprovou que a combinação da Temperatura Basal Corporal (com a regra estrita de "3 temperaturas consecutivas sobre as 6 anteriores") associada à observação do muco cervical clara de ovo atinge eficácia biológica de 99,6% para identificação do período fértil e determinação da ovulação.
3. **Ensaios Clínicos Multicêntricos de Prevenção de Defeitos de Tubo Neural por Folato:**
   - **MRC Vitamin Study Research Group [LANCET, 1991]:** Ensaio clínico randomizado duplo-cego multicêntrico com **N = 1.817 gestantes** em 33 centros internacionais, demonstrando redução de 72% na recorrência de anencefalia e espinha bífida com 4 mg de ácido fólico.
   - **Coorte Húngara de Prevenção Primária [CZEIZEL & DUDÁS, 1992]:** Ensaio clínico randomizado com **N = 4.753 mulheres planejando gravidez**, provando que 800 mcg diários de folato iniciados pelo menos 1 mês antes da concepção preveniu 100% dos primeiros casos de DTN no grupo intervenção.

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
     - Confirma a ovulação quando três temperaturas consecutivas forem superiores às seis temperaturas antecedentes imediatas (regra "3 sobre 6" [FRANK-HERRMANN et al., 2007]).
     - Se `LhTestResult == PEAK`, define a probabilidade de concepção como **Máxima** nas 24 a 48 horas seguintes.
     - Se `CervicalMucusType == EGG_WHITE`, projeta janela fértil ativa com destaque na tela inicial.

3. **Checklists Clínicos Pré-Concepcionais**:
   - **Ácido Fólico / Metilfolato:** Lembrete diário com meta padrão de 400 mcg (ou 5 mg para mulheres com diabetes ou histórico prévio de DTN [CZEIZEL & DUDÁS, 1992]).
   - **Exames Laboratoriais Pré-Concepcionais:** Sorologias (rubéola, toxoplasmose IgG/IgM, hepatites B e C, HIV, sífilis), tipagem sanguínea com fator Rh e Coombs indireto, hemograma e TSH.
   - **Calculadora de Tempo de Tentativa (Diretriz ASRM [PRACTICE COMMITTEE OF ASRM, 2021]):**
     - Se idade da mulher < 35 anos e tempo de tentativa >= 12 meses: exibir recomendação acolhedora para avaliação médica da fertilidade conjugal.
     - Se idade da mulher >= 35 anos e tempo de tentativa >= 6 meses: antecipar a recomendação de consulta com especialista em reprodução assistida.

4. **UI do Módulo Tentante (`presentation/ttc`)**:
   - `FertilityGauge`: marcador semicircular no dashboard indicando "Baixa", "Média", "Alta" ou "Pico de Fertilidade", vinculado à citação [DUNSON et al., 2002] no `ScientificSourceBadge.kt`.
   - `BbtChart`: gráfico de dispersão com linha de tendência conectando as medições de temperatura e destacando a linha de base (cobertura térmica).

## Consequências

### Positivas
- Fornece ferramentas científicas precisas para mulheres que buscam engravidar sem depender de estimativas cegas de calendário.
- Diminui o estresse de tentantes ao fornecer métricas objetivas de ovulação validadas em grandes amostras populacionais.
- Promove a adesão precoce ao ácido fólico com base em evidências definitivas [MRC, 1991; CZEIZEL & DUDÁS, 1992].

### Negativas / Mitigações
- Risco de ansiedade com o registro diário de temperatura. *Mitigação:* Textos acolhedores explicando que variações pontuais de sono ou febre podem alterar a temperatura basal sem comprometer o ciclo.

## Testes e Critérios de Aceite
- Testes unitários para `CalculateFertilityWindowUseCaseTest` com cenários de pico de LH, muco clara de ovo e confirmação térmica (regra 3 sobre 6).
- Verificação de renderização do gráfico de BBT com diferentes resoluções de tela.

---

## Referências Bibliográficas e Citações Formais

1. **DUNSON, D. B.; COLOMBO, B.; BAIRD, D. D.** Changes with age in the level and duration of fertility in the menstrual cycle. *Human Reproduction*, v. 17, n. 5, p. 1399–1403, maio 2002. DOI: [10.1093/humrep/17.5.1399](https://doi.org/10.1093/humrep/17.5.1399). Amostra da coorte: N = 1.077 casais / 3.160 ciclos.
2. **FRANK-HERRMANN, P. et al.** The effectiveness of a fertility awareness based method to avoid pregnancy in relation to a couple's sexual behaviour during the fertile time: a prospective longitudinal study. *Human Reproduction*, v. 22, n. 5, p. 1310–1319, maio 2007. DOI: [10.1093/humrep/dem003](https://doi.org/10.1093/humrep/dem003). Amostra da coorte Sensiplan: N = 900 mulheres / 17.638 ciclos.
3. **MRC VITAMIN STUDY RESEARCH GROUP.** Prevention of neural tube defects: Results of the Medical Research Council Vitamin Study. *The Lancet*, v. 338, n. 8760, p. 131–137, jul. 1991. DOI: [10.1016/0140-6736(91)90133-A](https://doi.org/10.1016/0140-6736(91)90133-A). Amostra do ensaio: N = 1.817 gestantes.
4. **CZEIZEL, A. E.; DUDÁS, I.** Prevention of the first occurrence of neural-tube defects by periconceptional vitamin supplementation. *New England Journal of Medicine*, v. 327, n. 26, p. 1832–1835, dez. 1992. DOI: [10.1056/NEJM199212243272602](https://doi.org/10.1056/NEJM199212243272602). Amostra do ensaio: N = 4.753 mulheres.
5. **WILCOX, A. J.; WEINBERG, C. R.; BAIRD, D. D.** Timing of sexual intercourse in relation to ovulation: effects on the probability of conception, survival of the pregnancy, and sex of the baby. *New England Journal of Medicine*, v. 333, n. 23, p. 1517–1521, dez. 1995. DOI: [10.1056/NEJM199512073332301](https://doi.org/10.1056/NEJM199512073332301).
6. **PRACTICE COMMITTEE OF THE AMERICAN SOCIETY FOR REPRODUCTIVE MEDICINE (ASRM).** Diagnostic evaluation of the infertile female: a committee opinion. *Fertility and Sterility*, v. 116, n. 5, p. 1255–1265, nov. 2021. DOI: [10.1016/j.fertnstert.2021.08.038](https://doi.org/10.1016/j.fertnstert.2021.08.038).
