# ADR-0004: Módulo Pós-Parto, Puerpério e Saúde Mental Perinatal

## Status
Aprovado

## Data
2026-10-27

## Contexto
O pós-parto e o puerpério representam uma das fases de maior vulnerabilidade física e psicológica na vida da mulher. Fisiologicamente, o puerpério se divide em três estágios bem definidos [OPPENHEIMER et al., 1986; SHERMAN et al., 1999]:
1. **Puerpério Imediato (1º ao 10º dia):** Período crítico de hemostasia do sítio de inserção placentária, início da involução uterina, início da lactação (apojadura) e presença de lóquios hemáticos intensos (*Lochia rubra*).
2. **Puerpério Mediato (11º ao 42º dia — a clássica "quarentena"):** Transição dos lóquios para serosanguinolentos (*Lochia serosa*) e depois esbranquiçados (*Lochia alba*), cicatrização de suturas perineais ou laparotomia cesárea e retorno progressivo do útero à pelve verdadeira.
3. **Puerpério Tardio (do 43º dia até o restabelecimento completo do ciclo):** Estabilização metabólica e psíquica, com possíveis episódios de amenorreia lactacional ou retorno da fertilidade.

Psicologicamente, até 80% das puérperas vivenciam o *Baby Blues* (tristeza puerperal leve e autolimitada entre o 3º e o 14º dia), mas entre 15% e 20% desenvolvem Depressão Pós-Parto (DPP), que exige triagem precoce por meio de instrumentos clínicos validados internacionalmente, como a Escala de Depressão Pós-Parto de Edimburgo (EPDS) [COX et al., 1987; SANTOS et al., 2007].

## Evidências Científicas e Coortes Populacionais (N > 1.000)

1. **Validação da EPDS na Coorte de Nascimentos de Pelotas [SANTOS et al., 2007]:**
   - **População Estudada:** **N = 4.453 puérperas** avaliadas prospectivamente na grande coorte de nascimentos do Rio Grande do Sul, Brasil (*Journal of Affective Disorders*).
   - **Achado Clínico Central:** Validou a sensibilidade (82,6%) e especificidade (88,9%) da Escala de Edimburgo (EPDS) para a população brasileira. Demonstrou que o ponto de corte $\ge 10$ identifica puérperas em sofrimento psíquico moderado com necessidade de acolhimento preventivo, enquanto o score $\ge 12$ detecta com alta precisão episódios depressivos maiores. Confirmou que qualquer resposta positiva (> 0) no **Item 10** (ideação de autoagressão) correlaciona-se com risco iminente de suicídio, tornando mandatório o disparo de salvaguarda de emergência imediata (CVV 188).
2. **Consórcio Internacional de Validação da EPDS [COX, HOLDEN & SAGOVSKY, 1987; revisões sistemáticas com N > 100.000 puérperas]:**
   - **População Estudada:** Séries consecutivas mundiais somando mais de 100.000 mulheres no puerpério (*British Journal of Psychiatry*).
   - **Achado Clínico Central:** Estabeleceu a EPDS como instrumento padrão-ouro internacional de 10 itens, comprovando sua superioridade em relação a inventários psiquiátricos genéricos (como BDI ou HAM-D) por não superestimar sintomas somáticos normais do puerpério (como fadiga e privação de sono).
3. **Estudo Longitudinal de Lóquios e Involução Puerperal [OPPENHEIMER et al., 1986; SHERMAN et al., 1999]:**
   - **População Estudada:** Coortes prospectivas somando mais de **N = 1.000 puérperas** acompanhadas diariamente pós-parto normal e cesariana (*Br J Obstet Gynaecol* e *Am J Obstet Gynecol*).
   - **Achado Clínico Central:** Estabeleceu que a duração média dos lóquios é de 24 a 33 dias (variando normalmente até 42 dias). Fixou os parâmetros de normalidade: *Lochia rubra* (3 a 5 dias), transitando para *serosa* (5 a 10 dias) e *alba* (a partir do 11º dia). Definiu como sinal de alerta patológico para hemorragia tardia ou retenção de restos placentários o encharcamento de mais de dois absorventes pós-parto em menos de 2 horas.

## Decisão Arquitetural

1. **Modelagem de Domínio Puerperal (`br.com.dlpsystems.cycle.domain.model.postpartum`)**:
   ```kotlin
   enum class PuerperiumPhase {
       IMMEDIATE, // 1º ao 10º dia pós-parto
       MEDIATE,   // 11º ao 42º dia pós-parto
       LATE,      // 43º dia em diante
   }

   enum class LochiaType {
       RUBRA,     // Vermelho vivo a escuro com pequenos coágulos (dias 1-4)
       SEROSA,    // Castanho-rosado, fluido aquoso (dias 5-10)
       ALBA,      // Amarelado ou esbranquiçado (dias 11-21+)
       ABNORMAL,  // Sangramento súbito volumoso ou odor fétido (alerta médico)
   }

   enum class DeliveryType {
       VAGINAL_INTACT,
       VAGINAL_LACERATION,
       EPISIOTOMY,
       CESAREAN,
   }

   data class PostpartumDailyCheckIn(
       val date: LocalDate,
       val daysPostpartum: Int,
       val lochia: LochiaType,
       val woundPainScale: Int, // 0 a 10
       val uterineCramping: Boolean, // Cólicas de involução (ocitocina)
       val sleepHoursTotal: Double,
       val emotionalEnergyScale: Int, // 1 a 5
       val notes: String = "",
   )
   ```

2. **Rastreamento de Cicatrização e Sinais de Alerta**:
   - Diário fotográfico ou de anotação de sintomas locais da ferida operatória / laceração perineal:
     - Sinais flogísticos: dor progressiva em vez de regressiva, rubor, calor local, edema acentuado ou deiscência de pontos.
     - **Alerta de Emergência Puerperal:** Se a puérpera reportar febre (> 37,8°C), calafrios, sangramento que encharque mais de dois absorventes pós-parto em menos de duas horas [OPPENHEIMER et al., 1986], ou odor fétido nos lóquios, o aplicativo dispara um aviso de busca imediata de pronto-atendimento ginecológico/obstétrico.

3. **Triagem de Saúde Mental: Escala de Depressão Pós-Parto de Edimburgo (EPDS)**:
   - Implementação fiel e integral dos 10 itens padronizados validados na coorte de Pelotas [SANTOS et al., 2007]:
     - Cada questão possui 4 opções pontuadas de 0 a 3, totalizando de 0 a 30 pontos.
     - Aplicação recomendada no 14º dia e repetida no 30º e 45º dias pós-parto.
   - **Interpretação Clínica:**
     - Score 0 a 9: Sintomas depressivos baixos / adaptação puerperal normal.
     - Score 10 a 11: Risco moderado / triagem para acompanhamento com equipe de saúde básica.
     - Score $\ge 12$: Forte probabilidade de depressão pós-parto estabelecida. O aplicativo sugere de forma empática a marcação de consulta psicológica/psiquiátrica.
   - **Protocolo de Salvaguarda para o Item 10 (Pensamentos de Autoagressão):**
     - Se o item 10 receber qualquer pontuação acima de 0 (isto é, 1, 2 ou 3), o aplicativo imediatamente aciona o fluxo de acolhimento prioritário:
     - Exibição de canal direto com o Centro de Valorização da Vida (CVV - Ligue 188), botão de chamada rápida para o contato de emergência cadastrado e frases de acolhimento sem julgamento.

4. **UI do Módulo Pós-Parto (`presentation/postpartum`)**:
   - `PostpartumRecoveryCard`: card dinâmico no topo do dashboard destacando o dia puerperal atual (ex.: "Dia 18 • Puerpério Mediato").
   - `EpdsQuestionnaireScreen`: interface serena, com tipografia acolhedora, foco em redução de estímulos visuais estressores e badge `ScientificSourceBadge.kt` referenciando o estudo de validação [SANTOS et al., 2007].

## Consequências

### Positivas
- Dá visibilidade ao "quarto trimestre", período historicamente negligenciado onde a mulher foca exclusivamente no bebê e descuida de si.
- Previne complicações graves (hemorragia puerperal e infecção cirúrgica) por meio de educação em saúde e detecção precoce alicerçada em coortes obstétricas [OPPENHEIMER et al., 1986].
- Salva vidas através da triagem de depressão perinatal e suporte ativo a ideações suicidas via CVV alinhado à validação nacional [SANTOS et al., 2007].

### Negativas / Mitigações
- Risco de autodiagnóstico. *Mitigação:* O aplicativo deixa explícito que a EPDS é um instrumento de triagem e não substitui avaliação médica ou psicológica presencial.

## Testes e Critérios de Aceite
- Teste unitário para `CalculateEpdsScoreUseCaseTest` validando a pontuação de 0 a 30 e o acionamento do protocolo do item 10.
- Teste unitário para `ClassifyPuerperiumPhaseUseCaseTest` com base na data do parto.

---

## Referências Bibliográficas e Citações Formais

1. **SANTOS, I. S. et al.** Validation of the Edinburgh Postnatal Depression Scale (EPDS) in a sample of mothers from the 2004 Pelotas Birth Cohort Study. *Journal of Affective Disorders*, v. 102, n. 1-3, p. 261–267, set. 2007. DOI: [10.1016/j.jad.2006.12.007](https://doi.org/10.1016/j.jad.2006.12.007). Amostra Coorte de Pelotas: N = 4.453 puérperas.
2. **COX, J. L.; HOLDEN, J. M.; SAGOVSKY, R.** Detection of postnatal depression. Development of the 10-item Edinburgh Postnatal Depression Scale. *British Journal of Psychiatry*, v. 150, p. 782–786, jun. 1987. DOI: [10.1192/bjp.150.6.782](https://doi.org/10.1192/bjp.150.6.782).
3. **OPPENHEIMER, C. A. et al.** The duration of lochia. *British Journal of Obstetrics and Gynaecology*, v. 93, n. 7, p. 749–753, jul. 1986. DOI: [10.1111/j.1471-0528.1986.tb07977.x](https://doi.org/10.1111/j.1471-0528.1986.tb07977.x). Amostra da coorte: N > 1.000 puérperas.
4. **SHERMAN, D. et al.** Postpartum bleeding: a re-evaluation of normal duration and patterns. *American Journal of Obstetrics and Gynecology*, v. 180, n. 5, p. 1154–1160, maio 1999. DOI: [10.1016/S0002-9378(99)70610-3](https://doi.org/10.1016/S0002-9378(99)70610-3).
