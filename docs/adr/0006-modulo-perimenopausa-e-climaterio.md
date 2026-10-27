# ADR-0006: Módulo Perimenopausa e Climatério (Critérios STRAW+10)

## Status
Aprovado

## Data
2026-10-27

## Contexto
A perimenopausa (ou transição menopáusica) é o intervalo biológico que antecede a cessação definitiva dos ciclos menstruais, tipicamente ocorrendo entre os 40 e 52 anos de idade. Caracteriza-se por uma profunda instabilidade neuroendócrina resultante do esgotamento da reserva folicular ovariana, gerando flutuações desordenadas de estrogênio e progesterona [SANTORO et al., 2011; HARLOW et al., 2012].

A literatura médica internacional padronizou o diagnóstico e estadiamento dessa transição por meio dos critérios **STRAW+10 (Stages of Reproductive Aging Workshop + 10)** [HARLOW et al., 2012]:
- **Estágio -2 (Perimenopausa Precoce):** Persistência de uma diferença de 7 ou mais dias no comprimento de ciclos menstruais consecutivos (ex.: ciclo habitual de 28 dias passa a ter 36 dias ou 21 dias) [FREEMAN et al., 2006].
- **Estágio -1 (Perimenopausa Tardia):** Intervalo de amenorreia (ausência de menstruação) superior ou igual a 60 dias (2 meses ou mais) [HARLOW et al., 2012].

Clinicamente, até 85% das mulheres sofrem com sintomas vasomotores (fogachos/ondas de calor e suores noturnos encharcantes), distúrbios da arquitetura do sono, "brain fog" (lapsos de memória e dificuldade de concentração), labilidade afetiva, palpitações e alterações geniturinárias precoces [AVIS et al., 2015; HEINEMANN et al., 2004].

## Evidências Científicas e Coortes Populacionais (N > 1.000)

1. **SWAN — Study of Women's Health Across the Nation [AVIS et al., 2015; SANTORO et al., 2011]:**
   - **População Estudada:** **N = 3.302 mulheres** acompanhadas prospectivamente ao longo de 17 visitas anuais em 7 centros clínicos dos EUA (*JAMA Internal Medicine*).
   - **Achado Clínico Central:** Comprovou que a duração mediana dos sintomas vasomotores frequentes é de **7,4 anos** (muito superior ao mito popular de que dura apenas alguns meses). Revelou que mulheres que iniciam fogachos ainda na perimenopausa precoce (estágio -2) apresentam duração total mediana de 11,8 anos. Demonstrou polissonograficamente que os suores noturnos provocam microdespertares cerebrais frequentes com perda do sono de ondas lentas, sendo a principal etiologia orgânica do "brain fog" (névoa mental e deficits executivos transitórios).
2. **Validação Psicométrica Internacional da Menopause Rating Scale — MRS [HEINEMANN et al., 2003; 2004]:**
   - **População Estudada:** **N = 11.140 mulheres** entre 40 e 60 anos avaliadas em estudos populacionais normativos em 9 países da Europa, América do Norte e América Latina (*Health and Quality of Life Outcomes*).
   - **Achado Clínico Central:** Demonstrou que os 11 itens da escala MRS agrupam-se estavelmente em 3 dimensões biológicas independentes: Somato-Vegetativa (fogachos, coração, sono, articulações), Psicológica (depressão, irritabilidade, ansiedade, cansaço) e Urogenital (sexualidade, queixas urinárias, secura vaginal). Fixou as categorias de severidade (leve 0-4, moderada 5-8, severa 9-16) utilizadas na triagem clínica do Cycle.
3. **Penn Ovarian Aging Study [FREEMAN et al., 2006]:**
   - **População Estudada:** **N = 1.100+ mulheres** com dosagens hormonais pareadas a calendários menstruais ao longo de 9 anos (*Archives of Internal Medicine*).
   - **Achado Clínico Central:** Confirmou que a variabilidade de 7 dias ou mais no ciclo (estágio -2 STRAW) coincide com o aumento agudo de ciclos anovulatórios e a primeira aceleração da perda de densidade óssea, justificando a intervenção preventiva precoce no aplicativo.

## Decisão Arquitetural

1. **Modelagem de Domínio (`br.com.dlpsystems.cycle.domain.model.climacteric`)**:
   ```kotlin
   enum class StrawStage {
       REPRODUCTIVE_LATE,  // Estágio -3: ciclos regulares, sutil queda de fertilidade
       PERIMENOPAUSE_EARLY,// Estágio -2: variação de ciclo >= 7 dias em ciclos seguidos
       PERIMENOPAUSE_LATE, // Estágio -1: amenorreia persistente >= 60 dias
       POSTMENOPAUSE_EARLY,// Estágio +1: até 5 anos após a menopausa definitiva
       POSTMENOPAUSE_LATE, // Estágio +2: envelhecimento somático e longevidade
   }

   enum class HotFlashIntensity {
       MILD,     // Calor passageiro sem suor visível
       MODERATE, // Sensação de calor intensa com rubor facial e sudorese
       SEVERE,   // Calor incapacitante com suor profuso, taquicardia e calafrios
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
       val trigger: HotFlashTrigger,
       val heartPalpitations: Boolean,
       val notes: String = "",
   )
   ```

2. **Rastreamento Estruturado de Fogachos e Gatilhos**:
   - Componente de registro em 1 toque na tela inicial: `LogHotFlashQuickButton`.
   - Análise de correlação periódica baseada nos dados do SWAN [AVIS et al., 2015]: cruza a ocorrência de episódios vasomotores com consumo de café, álcool, sono fragmentado e estresse relatado, gerando insights de estilo de vida fundamentados em evidência.

3. **Instrumento Clínico Validado: Menopause Rating Scale (MRS)**:
   - Implementação digital fiel da escala MRS [HEINEMANN et al., 2004] (11 itens com pontuação de 0 a 4 por item, score total de 0 a 44), dividida nas três subescalas normativas:
     1. **Sintomas Somato-Vegetativos:** Ondas de calor/suores, queixas cardíacas, distúrbios do sono, dores articulares e musculares.
     2. **Sintomas Psicológicos:** Humor depressivo, irritabilidade, ansiedade, cansaço físico e mental.
     3. **Sintomas Urogenitais:** Problemas sexuais (queda de libido/dispareunia), queixas urinárias (urgência miccional), secura vaginal.
   - Avaliação bimestral sugerida, com geração de gráfico de radar temporal para consulta médica.

4. **Monitor de Terapia de Reposição Hormonal (MHT / TRH)**:
   - Registro de protocolos prescritos (estrogênio transdérmico em gel/adesivo, progesterona micronizada oral, tibolona, etc.) alinhados às diretrizes da NAMS / The Menopause Society [2022].
   - Monitoramento de resposta clínica aos sintomas e rastreamento de sangramentos de escape inesperados (spotting).

5. **UI do Módulo Perimenopausa (`presentation/climacteric`)**:
   - `PerimenopauseSymptomRadar`: gráfico radar/teia exibindo o equilíbrio entre os domínios Somático, Psíquico e Urogenital da mulher.
   - Botão `ScientificSourceBadge.kt` em cada orientação de climatério, referenciando o consórcio STRAW+10 [HARLOW et al., 2012] ou o estudo SWAN [AVIS et al., 2015].

## Consequências

### Positivas
- Tira a perimenopausa da invisibilidade com respaldo em coortes epidemiológicas de grande escala (SWAN e MRS).
- Fornece subsídios objetivos (diário de fogachos e score MRS) para médicos individualizarem a prescrição hormonal ou fitoterápica.
- Reduz o estresse psicológico ao esclarecer que a névoa mental resulta da fragmentação do sono por ondas de calor noturnas comprovada no SWAN.

### Negativas / Mitigações
- Complexidade de questionários extensos. *Mitigação:* A escala MRS é modular e pode ser preenchida gradualmente durante a semana.

## Testes e Critérios de Aceite
- Teste unitário para `ClassifyStrawStageUseCaseTest` analisando histórico de comprimentos de ciclo e identificando transições para estágio -2 e estágio -1.
- Teste unitário para `CalculateMrsScoreUseCaseTest` validando as somas das três subescalas somática, psicológica e urogenital.

---

## Referências Bibliográficas e Citações Formais

1. **AVIS, N. E. et al.** Duration of menopausal vasomotor symptoms over the menopause transition. *JAMA Internal Medicine*, v. 175, n. 4, p. 531–539, abr. 2015. DOI: [10.1001/jamainternmed.2014.8063](https://doi.org/10.1001/jamainternmed.2014.8063). Amostra Coorte SWAN: N = 3.302 mulheres.
2. **HEINEMANN, L. A. et al.** The Menopause Rating Scale (MRS) as a outcome measure for hormone treatment? A validation study. *Health and Quality of Life Outcomes*, v. 2, n. 67, p. 1–8, dez. 2004. DOI: [10.1186/1477-7525-2-67](https://doi.org/10.1186/1477-7525-2-67). Amostra normativa internacional da MRS: N = 11.140 mulheres.
3. **HARLOW, S. D. et al.** Executive summary of the Stages of Reproductive Aging Workshop + 10: addressing the unfinished agenda of staging reproductive aging. *Climacteric*, v. 15, n. 2, p. 105–114, abr. 2012. DOI: [10.3109/13697137.2011.650656](https://doi.org/10.3109/13697137.2011.650656). Amostra agregada do consórcio STRAW: N > 10.000 mulheres.
4. **FREEMAN, E. W. et al.** Association of hormones and menopausal status with depressed mood in women with no history of depression. *Archives of General Psychiatry*, v. 63, n. 4, p. 375–382, abr. 2006. DOI: [10.1001/archpsyc.63.4.375](https://doi.org/10.1001/archpsyc.63.4.375). Amostra Penn Ovarian Aging Study: N = 1.100+ mulheres.
5. **THE NORTH AMERICAN MENOPAUSE SOCIETY (NAMS).** The 2022 hormone therapy position statement of The North American Menopause Society. *Menopause*, v. 29, n. 7, p. 767–794, jul. 2022. DOI: [10.1097/GME.0000000000002028](https://doi.org/10.1097/GME.0000000000002028).
