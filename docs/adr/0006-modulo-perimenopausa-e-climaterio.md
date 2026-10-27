# ADR-0006: Módulo Perimenopausa e Climatério (Critérios STRAW+10)

## Status
Aprovado

## Data
2026-10-27

## Contexto
A perimenopausa (ou transição menopáusica) é o intervalo biológico que antecede a cessação definitiva dos ciclos menstruais, tipicamente ocorrendo entre os 40 e 52 anos de idade. Caracteriza-se por uma profunda instabilidade neuroendócrina resultante do esgotamento da reserva folicular ovariana, gerando flutuações desordenadas de estrogênio e progesterona.

A literatura médica internacional padronizou o diagnóstico e estadiamento dessa transição por meio dos critérios **STRAW+10 (Stages of Reproductive Aging Workshop + 10)**:
- **Estágio -2 (Perimenopausa Precoce):** Persistência de uma diferença de 7 ou mais dias no comprimento de ciclos menstruais consecutivos (ex.: ciclo habitual de 28 dias passa a ter 36 dias ou 21 dias).
- **Estágio -1 (Perimenopausa Tardia):** Intervalo de amenorreia (ausência de menstruação) superior ou igual a 60 dias (2 meses ou mais).

Clinicamente, até 85% das mulheres sofrem com sintomas vasomotores (fogachos/ondas de calor e suores noturnos encharcantes), distúrbios da arquitetura do sono, "brain fog" (lapsos de memória e dificuldade de concentração), labilidade afetiva, palpitações e alterações geniturinárias precoces.

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
   - Análise de correlação periódica: cruza a ocorrência de episódios vasomotores com consumo de café, álcool, noites de sono mal dormidas e estresse relatado, gerando insights de estilo de vida baseados em evidência científica.

3. **Instrumento Clínico Validado: Menopause Rating Scale (MRS)**:
   - Implementação digital da escala MRS (11 itens com pontuação de 0 a 4 por item, score total de 0 a 44), dividida em três subescalas:
     1. **Sintomas Somato-Vegetativos:** Ondas de calor/suores, queixas cardíacas, distúrbios do sono, dores articulares e musculares.
     2. **Sintomas Psicológicos:** Humor depressivo, irritabilidade, ansiedade, cansaço físico e mental.
     3. **Sintomas Urogenitais:** Problemas sexuais (queda de libido/dispareunia), queixas urinárias (urgência miccional), secura vaginal.
   - Avaliação bimestral sugerida, com geração de gráfico de evolução temporal para apresentação ao médico ginecologista/endocrinologista.

4. **Monitor de Terapia de Reposição Hormonal (MHT / TRH)**:
   - Registro de protocolos prescritos (estrogênio transdérmico em gel/adesivo, progesterona micronizada oral, tibolona, etc.).
   - Monitoramento de resposta clínica aos sintomas e rastreamento de sangramentos de escape inesperados (spotting).

5. **UI do Módulo Perimenopausa (`presentation/climacteric`)**:
   - `PerimenopauseSymptomRadar`: gráfico radar/teia exibindo o equilíbrio entre os domínios Somático, Psíquico e Urogenital da mulher.
   - Recursos educativos com curadoria científica sobre fitoterápicos estudados (isoflavonas, cimicifuga racemosa, etc.) e práticas de autocuidado.

## Consequências

### Positivas
- Tira a perimenopausa da invisibilidade, acolhendo mulheres em uma fase de alta angústia e diagnósticos tardios.
- Fornece subsídios objetivos (diário de fogachos e score MRS) para médicos individualizarem a prescrição hormonal ou fitoterápica.
- Reduz o estresse psicológico ao desmistificar a névoa mental ("brain fog") como reflexo da flutuação estrogênica e não demência precoce.

### Negativas / Mitigações
- Complexidade de questionários extensos. *Mitigação:* A escala MRS é modular e pode ser preenchida em partes ao longo da semana.

## Testes e Critérios de Aceite
- Teste unitário para `ClassifyStrawStageUseCaseTest` analisando histórico de comprimentos de ciclo e identificando transições para estágio -2 e estágio -1.
- Teste unitário para `CalculateMrsScoreUseCaseTest` validando as somas das três subescalas somática, psicológica e urogenital.
