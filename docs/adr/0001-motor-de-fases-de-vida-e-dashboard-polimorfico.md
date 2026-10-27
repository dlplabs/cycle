# ADR-0001: Motor de Fases de Vida e Dashboard Polimórfico (`LifeStageMode`)

## Status
Aprovado

## Data
2026-10-27

## Contexto
O aplicativo Cycle foi concebido inicialmente sob a premissa de um ciclo menstrual contínuo e regular subdividido em quatro fases: Menstrual, Folicular, Ovulatória e Lútea. O componente visual central (`CycleWheel.kt`) e os casos de uso associados (`CalculateCurrentPhaseUseCase.kt`) assumem a existência de menstruações cíclicas e durações médias de ciclo.

Entretanto, as mulheres vivenciam transformações fisiológicas profundas ao longo da vida:
- Tentativas ativas de engravidar com foco estrito em ovulação e fertilidade (`TRYING_TO_CONCEIVE`);
- Gestação, em que o ciclo menstrual é interrompido por cerca de 40 semanas (`PREGNANCY`);
- Pós-parto imediato e mediato, com involução uterina e recuperação puerperal (`POSTPARTUM`);
- Lactação e amamentação, em que a prolactina suprime gonadotrofinas gerando amenorreia transitória (`LACTATION`);
- Perimenopausa/Climatério, com ciclos irregulares, anovulatórios e sintomas vasomotores (`PERIMENOPAUSE`);
- Menopausa definitiva e pós-menopausa, caracterizada por 12 meses ininterruptos de amenorreia (`MENOPAUSE`).

Manter o aplicativo estático na roda de 4 fases para usuárias grávidas ou menopáusicas gera frustração e quebra de utilidade clínica.

## Evidências Científicas e Coortes Populacionais (N > 1.000)

A estratificação da vida feminina em estágios biológicos bem definidos não é uma convenção cosmética, mas uma necessidade neuroendócrina comprovada por grandes coortes longitudinais:

1. **Consórcio STRAW+10 (Stages of Reproductive Aging Workshop + 10, Harlow et al., 2012 / Climacteric & Menopause):**
   - **População Estudada:** Coortes combinadas de mais de **10.000 mulheres** seguidas longitudinalmente em estudos pioneiros (SWAN nos EUA com N = 3.302; Penn Ovarian Aging Study com N = 1.100+; Melbourne Women's Midlife Health Project na Austrália com N = 2.001; Seattle Midlife Women's Health Study com N = 508).
   - **Achado Clínico Central:** Comprovou que a vida reprodutiva e não reprodutiva da mulher segue 7 marcos biológicos discretos (-5 a +2), demonstrando que a fisiologia de uma mulher no estágio -2 (perimenopausa inicial com ciclos anovulatórios alternados) difere radicalmente da fisiologia fértil (estágio -3), justificando interfaces e métricas totalmente polimórficas.
2. **SWAN — Study of Women's Health Across the Nation (Sowers et al., 2000; Santoro et al., 2011):**
   - **População Estudada:** **N = 3.302 mulheres** multiétnicas acompanhadas prospectivamente por mais de 15 anos.
   - **Achado Clínico Central:** Demonstrou que os ciclos sofrem expansão e contração de fase lútea e folicular antes da cessação final, inviabilizando modelos determinísticos de 28 dias e exigindo adaptabilidade algorítmica no painel principal do aplicativo.

## Decisão Arquitetural

1. **Criação do Modelo de Estado Central `LifeStageMode`**:
   Definir no pacote de domínio `br.com.dlpsystems.cycle.domain.model` o enum representativo do modo vital da usuária:
   ```kotlin
   enum class LifeStageMode {
       REGULAR_CYCLE,
       TRYING_TO_CONCEIVE,
       PREGNANCY,
       POSTPARTUM,
       LACTATION,
       PERIMENOPAUSE,
       MENOPAUSE,
   }
   ```

2. **Desacoplamento do Dashboard Polimórfico**:
   O `DashboardScreen.kt` deixará de renderizar exclusivamente o `CycleWheel`. Ele integrará um slot polimórfico gerenciado pelo `DashboardViewModel`, selecionando o componente visual representativo:
   - `REGULAR_CYCLE`: `CycleWheel` (fases menstrual, folicular, ovulatória, lútea).
   - `TRYING_TO_CONCEIVE`: `FertilityGauge` (termômetro de fertilidade, contagem regressiva para o pico de LH e status da janela fértil).
   - `PREGNANCY`: `GestationalCircle` (semana gestacional + dias, trimestre, comparação volumétrica do feto e contagem para a DPP).
   - `POSTPARTUM`: `PostpartumRecoveryCard` (dias pós-parto, status de cicatrização, evolução de lóquios e bem-estar emocional).
   - `LACTATION`: `NursingQuickTracker` (última mama ofertada, timer de mamada rápida e volume de leite em banco).
   - `PERIMENOPAUSE`: `PerimenopauseSymptomRadar` (variabilidade de ciclos, índice de fogachos e monitor de bem-estar).
   - `MENOPAUSE`: `LongevityWellnessDial` (tempo em amenorreia estável, adesão a exercícios de força e saúde óssea/cardíaca).

3. **Gerenciamento de Transição de Modos (`LifeStageEngine`)**:
   - **Transição Manual:** A usuária pode alternar diretamente nas Configurações do aplicativo ou através de atalhos no banner de acolhimento.
   - **Detecção Assistida e Não Invasiva:** Quando as anotações diárias indicarem um padrão de mudança (exemplo: gravidez confirmada registrada, ou amenorreia de 60 dias em mulher com mais de 43 anos), o aplicativo apresentará um `CoachMark` ou diálogo acolhedor perguntando se deseja alternar para o modo correspondente.
   - O aplicativo nunca forçará uma mudança sem consentimento explícito.

4. **Persistência e Sincronização Local-First**:
   O modo ativo é armazenado no `DataStore` (chave `active_life_stage_mode`) para resposta offline instantânea (< 16ms no arranque) e sincronizado com o Firestore na coleção `users/{userId}` no campo `activeLifeStage`.

## Consequências

### Positivas
- **Inclusão Abrangente:** O aplicativo acompanha a mulher dos 15 aos 80 anos com respeito e respaldo biomédico.
- **Coesão e Baixo Acoplamento:** Cada modo tem seus próprios Use Cases e componentes de UI desacoplados, sem poluir a lógica de ciclo regular.
- **Retenção Real de Usuárias:** Elimina a taxa de desinstalação que ocorria quando a mulher engravidava ou chegava à maturidade.

### Negativas / Mitigações
- **Complexidade de Navegação:** A barra de abas inferior e os cards diários precisam responder dinamicamente ao modo. *Mitigação:* Usar o padrão Strategy para prover os cards do dia com base no `LifeStageMode`.

## Testes e Critérios de Aceite
- Teste unitário no `DashboardViewModelTest` cobrindo a emissão correta do estado de UI para cada um dos 7 modos.
- Teste de migração no `DataStore` garantindo que usuárias legadas sem modo configurado assumam `REGULAR_CYCLE` por padrão.
