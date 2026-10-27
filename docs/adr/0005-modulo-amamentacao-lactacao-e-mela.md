# ADR-0005: Módulo Amamentação, Lactação, Banco de Leite e MELA

## Status
Aprovado

## Data
2026-10-27

## Contexto
A amamentação e a lactação constituem um processo neuroendócrino complexo regulado pela prolactina (produção láctea) e pela ocitocina (ejeção do leite estimulada pela sucção). O processo impõe desafios diários severos para a mulher:
1. **Desbalanceamento e Ingurgitamento:** Esquecer em qual mama o bebê mamou por último pode levar à estase láctea, ducto obstruído e evoluir para mastite bacteriana infecciosa.
2. **Armazenamento de Leite Materno Ordenhado:** Mães que retornam ao trabalho ou realizam extração exclusiva necessitam de rigoroso controle cronológico da conservação em temperatura ambiente, refrigeração e congelamento conforme os protocolos oficiais do Ministério da Saúde / Rede Brasileira de Bancos de Leite Humano (rBLH-Fiocruz) e CDC.
3. **Amenorreia Lactacional (MELA / LAM):** Os altos níveis de prolactina suprimem o eixo hipotálamo-hipófise-ovariano, gerando anovulação transitória. Muitas mulheres utilizam o MELA como método contraceptivo natural temporário, porém desconhecem os critérios de eficácia e os sinais de retorno da fertilidade.

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
   - Registro automático e destaque imediato da "Próxima mama recomendada" na tela inicial do app.

3. **Gestão do Banco de Leite Materno Caseiro**:
   - **Calculadora de Validade Oficial (Diretriz Fiocruz / rBLH Brasil):**
     - Em temperatura ambiente (até 25°C): descartar ou oferecer em até 2 horas.
     - Em geladeira (<= 4°C): validade estrita de até 12 horas.
     - Em congelador/freezer (<= -10°C): validade de até 15 dias para consumo doméstico do próprio filho.
   - Notificações locais preventivas: alerta quando uma garrafa refrigerada estiver a 2 horas de atingir o prazo limite de 12 horas.

4. **Triagem de Complicações Mamárias (Prevenção de Mastite)**:
   - Se a usuária reportar nódulo localizado endurecido com dor + temperatura corporal >= 38°C ou rubor cutâneo em cunha, o sistema classifica como **Possível Mastite Puerperal** e emite recomendação de esvaziamento mamário frequente e consulta médica imediata para prescrição de antibioticoterapia se indicada.

5. **Avaliador do Método da Amenorreia Lactacional (MELA / LAM)**:
   - Acompanhamento dos 3 critérios obrigatórios da OMS para que o MELA tenha eficácia contraceptiva superior a 98%:
     1. O bebê tem menos de 6 meses de vida;
     2. A mãe mantém amenorreia persistente (nenhum episódio de sangramento vaginal após 56 dias de pós-parto);
     3. A amamentação é exclusiva ou quase exclusiva em livre demanda, sem intervalos maiores que 4 horas de dia e 6 horas à noite.
   - Se qualquer um dos três critérios for quebrado (ex.: introdução de fórmula, retorno de sangramento ou bebê atingiu 6 meses), o aplicativo notifica a perda de cobertura contraceptiva do MELA e orienta a escolha de métodos de transição seguros para a lactação (ex.: anticoncepcional de progestagênio isolado, DIU de cobre ou hormonal, métodos de barreira).

## Consequências

### Positivas
- Alívio da sobrecarga cognitiva da mãe através de registros automatizados e cronômetros táteis.
- Redução de desperdício de leite materno ordenhado com segurança bacteriológica.
- Planejamento reprodutivo consciente com orientações cientificamente comprovadas sobre MELA.

### Negativas / Mitigações
- Dependência do uso do smartphone com bebê no colo. *Mitigação:* UI com atalhos de um único toque e widgets rápidos na tela de bloqueio e inicial.

## Testes e Critérios de Aceite
- Teste unitário para `EvaluateLamCriteriaUseCaseTest` validando as combinações de idade do bebê, padrão de amamentação e sangramento.
- Teste unitário para `CalculateMilkExpirationUseCaseTest` testando os prazos de validade para geladeira e freezer.
