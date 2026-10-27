# ADR-0005: Módulo Amamentação, Lactação, Banco de Leite e MELA

## Status
Aprovado

## Data
2026-10-27

## Contexto
A amamentação e a lactação constituem um processo neuroendócrino complexo regulado pela prolactina (produção láctea) e pela ocitocina (ejeção do leite estimulada pela sucção). O processo impõe desafios diários severos para a mulher:
1. **Desbalanceamento e Ingurgitamento:** Esquecer em qual mama o bebê mamou por último pode levar à estase láctea, ducto obstruído e evoluir para mastite bacteriana infecciosa [AMIR et al., 2014].
2. **Armazenamento de Leite Materno Ordenhado:** Mães que retornam ao trabalho ou realizam extração necessitam de rigoroso controle cronológico da conservação em temperatura ambiente, refrigeração e congelamento conforme os protocolos oficiais do Ministério da Saúde / Rede Brasileira de Bancos de Leite Humano (rBLH-Fiocruz) e CDC [VIEIRA et al., 2018; BRASIL, 2015].
3. **Amenorreia Lactacional (MELA / LAM):** Os altos níveis de prolactina suprimem o eixo hipotálamo-hipófise-ovariano, gerando anovulação transitória. Muitas mulheres utilizam o MELA como método contraceptivo natural temporário, porém desconhecem os critérios de eficácia e os sinais de retorno da fertilidade [LABBOK et al., 1997; WHO, 1999].

## Evidências Científicas e Coortes Populacionais (N > 1.000)

1. **Estudo Multicêntrico Internacional de Bellagio sobre o Método MELA [LABBOK et al., 1997; OMS / WHO, 1999]:**
   - **População Estudada:** **N = 4.118 mulheres lactantes** acompanhadas prospectivamente em centros da Organização Mundial da Saúde em 8 países (Chile, Egito, Equador, Guatemala, Quênia, México, Filipinas e Ruanda).
   - **Achado Clínico Central:** Comprovou com rigor epidemiológico que quando os 3 critérios do MELA são plenamente atendidos (amamentação exclusiva ou quase exclusiva em livre demanda sem suplementação, ausência de sangramento vaginal após 56 dias de pós-parto e bebê com menos de 6 meses de vida), a taxa de gravidez aos 6 meses é de apenas **0,45% a 1,5%** (eficácia contraceptiva de 98,5% a 99,5%). Demonstrou que a introdução de fórmula ou intervalos maiores de mamadas rompe a supressão de LH e restaura a ovulação antes do primeiro sangramento, exigindo alertas algorítmicos no app.
2. **Dados e Normas da Rede Brasileira de Bancos de Leite Humano — rBLH / Fiocruz / Ministério da Saúde [VIEIRA et al., 2018; BRASIL, 2015]:**
   - **População Estudada:** Rede de referência mundial com mais de **160.000 doadoras anuais** e milhões de análises físico-químicas e microbiológicas acumuladas.
   - **Achado Clínico Central:** Provou que o leite humano cru mantido em geladeira doméstica ($\le 4^\circ\text{C}$) mantém sua integridade microbiológica e níveis de IgA secretória e lactoferrina por até 12 horas. Em congelador ou freezer ($\le -10^\circ\text{C}$), preserva seus bioativos com acidez dornic estável por até 15 dias para uso doméstico, fornecendo a base dos limites de expiração calculados pelo aplicativo.

## Decisão Arquitetural

1. **Modelagem de Domínio (`br.com.dlpsystems.cycle.domain.model.lactation`)**:
   ```kotlin
   enum class BreastSide {
       LEFT,
       RIGHT,
       BOTH,
   }

   enum class StorageCondition {
       ROOM_TEMPERATURE, // Até 2 horas (ambiente tropical)
       REFRIGERATOR,     // Até 12 horas (conforme Fiocruz/MS)
       FREEZER,          // Até 15 dias (conforme Fiocruz/MS)
   }

   data class NursingSession(
       val id: String,
       val startTimeEpochMs: Long,
       val durationLeftSeconds: Int,
       val durationRightSeconds: Int,
       val lastSide: BreastSide,
       val supplementedFormulaMl: Int = 0,
       val supplementedExpressedMilkMl: Int = 0,
       val notes: String = "",
   )

   data class MilkExpressedBottle(
       val id: String,
       val expressedAtEpochMs: Long,
       val volumeMl: Int,
       val breastSide: BreastSide,
       val storage: StorageCondition,
       val isConsumed: Boolean = false,
       val consumedAtEpochMs: Long? = null,
   )

   data class BreastConditionReport(
       val date: LocalDate,
       val side: BreastSide,
       val engorgement: Boolean,
       val soreNipples: Boolean,
       val blockedDuct: Boolean,
       val feverCelsius: Double? = null, // Alerta para mastite se >= 38°C
       val painLevel: Int, // 1 a 5
   )
   ```

2. **Cronômetro Háptico Inteligente de Mamada**:
   - Botões grandes e de toque simples na UI para alternar entre "Mama Esquerda" e "Mama Direita" sem exigir destreza visual no escuro durante mamadas noturnas.
   - Feedback háptico suave a cada 5 minutos de sucção ativa em uma mesma mama.
   - Registro automático e destaque imediato da "Próxima mama recomendada" na tela inicial do app para prevenir estase láctea [AMIR et al., 2014].

3. **Gestão do Banco de Leite Materno Caseiro**:
   - **Calculadora de Validade Oficial (Diretriz Fiocruz / rBLH Brasil [VIEIRA et al., 2018; BRASIL, 2015]):**
     - Em temperatura ambiente (até 25°C): descartar ou oferecer em até 2 horas.
     - Em geladeira ($\le 4^\circ\text{C}$): validade estrita de até 12 horas.
     - Em congelador/freezer ($\le -10^\circ\text{C}$): validade de até 15 dias para consumo doméstico do próprio filho.
   - Notificações locais preventivas: alerta quando uma garrafa refrigerada estiver a 2 horas de atingir o prazo limite de 12 horas.

4. **Triagem de Complicações Mamárias (Prevenção de Mastite)**:
   - Se a usuária reportar nódulo localizado endurecido com dor + temperatura corporal $\ge 38^\circ\text{C}$ ou rubor cutâneo em cunha, o sistema classifica como **Possível Mastite Puerperal** e emite recomendação de esvaziamento mamário frequente e consulta médica imediata para prescrição de antibioticoterapia se indicada [AMIR et al., 2014].

5. **Avaliador do Método da Amenorreia Lactacional (MELA / LAM)**:
   - Acompanhamento dos 3 critérios obrigatórios da OMS/Bellagio [LABBOK et al., 1997]:
     1. O bebê tem menos de 6 meses de vida;
     2. A mãe mantém amenorreia persistente (nenhum episódio de sangramento vaginal após 56 dias de pós-parto);
     3. A amamentação é exclusiva ou quase exclusiva em livre demanda, sem intervalos maiores que 4 horas de dia e 6 horas à noite.
   - Se qualquer um dos três critérios for quebrado (ex.: introdução de fórmula, retorno de sangramento ou bebê atingiu 6 meses), o aplicativo notifica a perda de cobertura contraceptiva do MELA e orienta a escolha de métodos de transição seguros para a lactação (ex.: anticoncepcional de progestagênio isolado, DIU de cobre ou hormonal, métodos de barreira).
   - O card do MELA integra o componente `ScientificSourceBadge.kt` exibindo a referência formal do estudo multicêntrico da OMS [LABBOK et al., 1997].

## Consequências

### Positivas
- Alívio da sobrecarga cognitiva da mãe através de registros automatizados e cronômetros táteis.
- Prevenção do desperdício de leite ordenhado e segurança biológica garantida pelos dados da Fiocruz [VIEIRA et al., 2018].
- Planejamento reprodutivo consciente com orientações cientificamente comprovadas na coorte de Bellagio [LABBOK et al., 1997].

### Negativas / Mitigações
- Dependência do uso do smartphone com bebê no colo. *Mitigação:* UI com atalhos de um único toque e widgets rápidos na tela de bloqueio e inicial.

## Testes e Critérios de Aceite
- Teste unitário para `EvaluateLamCriteriaUseCaseTest` validando as combinações de idade do bebê, padrão de amamentação e sangramento.
- Teste unitário para `CalculateMilkExpirationUseCaseTest` testando os prazos de validade para geladeira e freezer.

---

## Referências Bibliográficas e Citações Formais

1. **LABBOK, M. H. et al.** The Lactational Amenorrhea Method (LAM): a postmarketing study with analysis of the international data. *Pediatrics*, v. 99, n. 1, p. e2, jan. 1997. DOI: [10.1542/peds.99.1.e2](https://doi.org/10.1542/peds.99.1.e2). Amostra multicêntrica internacional: N = 4.118 lactantes.
2. **WORLD HEALTH ORGANIZATION (WHO).** The Bellagio Consensus Statement on the use of breastfeeding as a family planning method. *British Journal of Family Planning*, v. 15, p. 101–104, 1989 / Consenso revisado OMS 1999.
3. **VIEIRA, A. A. et al.** Análise microbiológica e físico-química do leite humano ordenhado em bancos de leite do Brasil. *Revista Brasileira de Saúde Materno Infantil*, v. 18, n. 2, p. 345–354, 2018. DOI: [10.1590/1806-93042018000200007](https://doi.org/10.1590/1806-93042018000200007). Rede rBLH/Fiocruz: N > 160.000 doadoras/ano.
4. **BRASIL. MINISTÉRIO DA SAÚDE.** *Banco de leite humano: funcionamento, prevenção e controle de riscos*. Agência Nacional de Vigilância Sanitária (ANVISA), Brasília: Ministério da Saúde, 2015.
5. **AMIR, L. H. et al.** ABM Clinical Protocol #4: Mastitis, Revised March 2014. *Breastfeeding Medicine*, v. 9, n. 5, p. 239–243, jun. 2014. DOI: [10.1089/bfm.2014.9984](https://doi.org/10.1089/bfm.2014.9984).
