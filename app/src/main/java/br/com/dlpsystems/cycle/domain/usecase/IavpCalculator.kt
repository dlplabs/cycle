package br.com.dlpsystems.cycle.domain.usecase

import br.com.dlpsystems.cycle.domain.model.IavpActionType
import br.com.dlpsystems.cycle.domain.model.IavpEvaluationResult
import br.com.dlpsystems.cycle.domain.model.IavpQuestion
import br.com.dlpsystems.cycle.domain.model.IavpQuickAction
import br.com.dlpsystems.cycle.domain.model.IavpRiskLevel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IavpCalculator @Inject constructor() {

    val questions: List<IavpQuestion> = listOf(
        // Categoria 1: Gatilhos Críticos de Letalidade (Peso 4 + Override)
        IavpQuestion(
            id = "Q01",
            category = 1,
            weight = 4,
            questionText = "Houve alguma tentativa ou histórico de sufocamento, estrangulamento ou asfixia?",
            detailText = "Apertar o pescoço, cobrir boca/nariz ou impedir a respiração em qualquer momento.",
        ),
        IavpQuestion(
            id = "Q02",
            category = 1,
            weight = 4,
            questionText = "O agressor usou ou ameaçou usar arma de fogo, faca ou objeto cortante?",
            detailText = "Exibição de armas ou objetos cortantes com intenção de intimidar ou ferir.",
        ),
        IavpQuestion(
            id = "Q03",
            category = 1,
            weight = 4,
            questionText = "Existe ameaça explícita de morte ou frases como 'se não for minha, não será de mais ninguém'?",
            detailText = "Declarações diretas de que tirará sua vida ou cometerá ato extremo.",
        ),
        IavpQuestion(
            id = "Q04",
            category = 1,
            weight = 4,
            questionText = "Ocorreu agressão física durante a gestação ou no período pós-parto?",
            detailText = "Empurrões, chutes ou socos ocorridos durante a gravidez ou puerpério.",
        ),

        // Categoria 2: Indicadores Graves de Escalada (Peso 2)
        IavpQuestion(
            id = "Q05",
            category = 2,
            weight = 2,
            questionText = "Existe perseguição persistente (stalking), monitoramento do celular ou de sua rotina?",
            detailText = "Rastreamento sem consentimento, controle de redes sociais ou presença constante nos locais que você frequenta.",
        ),
        IavpQuestion(
            id = "Q06",
            category = 2,
            weight = 2,
            questionText = "Ocorreram agressões físicas anteriores com hematomas ou necessidade de atendimento médico?",
            detailText = "Lesões visíveis, cortes ou idas ao hospital/unidade de saúde.",
        ),
        IavpQuestion(
            id = "Q07",
            category = 2,
            weight = 2,
            questionText = "Há ameaças diretas contra filhos, familiares próximos ou animais de estimação?",
            detailText = "Uso de entes queridos ou pets como forma de coação psicológica.",
        ),
        IavpQuestion(
            id = "Q08",
            category = 2,
            weight = 2,
            questionText = "Houve término recente do relacionamento (últimos 3 meses) ou recusa do agressor em aceitar a separação?",
            detailText = "Aumento de episódios agressivos após pedido de término ou afastamento.",
        ),
        IavpQuestion(
            id = "Q09",
            category = 2,
            weight = 2,
            questionText = "Houve descumprimento de Medida Protetiva de Urgência anteriormente decretada?",
            detailText = "Aproximação não autorizada violando ordem judicial prévia.",
        ),

        // Categoria 3: Fatores de Risco e Vulnerabilidade Situacional (Peso 1)
        IavpQuestion(
            id = "Q10",
            category = 3,
            weight = 1,
            questionText = "O agressor faz uso abusivo ou diário de álcool, medicamentos ou substâncias ilícitas?",
            detailText = "Alterações graves de comportamento induzidas por substâncias.",
        ),
        IavpQuestion(
            id = "Q11",
            category = 3,
            weight = 1,
            questionText = "O agressor enfrenta desemprego recente ou grave instabilidade financeira associada a explosões de raiva?",
            detailText = "Estresse econômico utilizado como estopim para descontrole verbal ou físico.",
        ),
        IavpQuestion(
            id = "Q12",
            category = 3,
            weight = 1,
            questionText = "Há ciúme obsessivo ou isolamento forçado de seus amigos e familiares?",
            detailText = "Proibição de trabalhar, estudar ou manter contato com sua rede de apoio.",
        ),
        IavpQuestion(
            id = "Q13",
            category = 3,
            weight = 1,
            questionText = "O agressor possui histórico de violência com outras parceiras ou antecedentes criminais?",
            detailText = "Registros anteriores de agressão contra ex-relacionamentos ou terceiros.",
        ),
    )

    fun calculate(answers: Map<String, Boolean>): IavpEvaluationResult {
        val cat1Positives = questions.filter { it.category == 1 && answers[it.id] == true }
        val cat2Positives = questions.filter { it.category == 2 && answers[it.id] == true }
        val cat3Positives = questions.filter { it.category == 3 && answers[it.id] == true }

        val fatalTrigger = cat1Positives.isNotEmpty()
        val score = (cat1Positives.size * 4) + (cat2Positives.size * 2) + (cat3Positives.size * 1)

        val riskLevel = when {
            fatalTrigger || score >= 10 -> IavpRiskLevel.EXTREMO
            score in 6..9 -> IavpRiskLevel.GRAVE
            score in 3..5 -> IavpRiskLevel.MODERADO
            else -> IavpRiskLevel.BAIXO
        }

        val quickActions = mutableListOf<IavpQuickAction>()
        quickActions.add(
            IavpQuickAction(
                title = "Polícia Militar - 190",
                description = "Acionamento de emergência imediata em situações de perigo urgente.",
                phoneNumber = "190",
                actionType = IavpActionType.CALL_POLICE,
            ),
        )
        quickActions.add(
            IavpQuickAction(
                title = "Central de Atendimento à Mulher - 180",
                description = "Atendimento gratuito, confidencial e orientação sobre direitos e DEAMs.",
                phoneNumber = "180",
                actionType = IavpActionType.CALL_WOMEN_CENTER,
            ),
        )
        quickActions.add(
            IavpQuickAction(
                title = "Delegacias Especializadas (DEAM)",
                description = "Localização e orientação sobre medidas protetivas de urgência.",
                phoneNumber = null,
                actionType = IavpActionType.DEAM_LOCATION,
            ),
        )

        val (summaryTitle, summaryDesc, recs) = when (riskLevel) {
            IavpRiskLevel.EXTREMO -> Triple(
                "Situação de Risco Extremo Detectada",
                "Os indicadores apontam perigo iminente. Priorize sua integridade física e evite confrontar o agressor.",
                listOf(
                    "Não anuncie nem comunique a intenção de saída ao agressor.",
                    "Monte uma bolsa discreta de emergência com documentos, chaves reserva, remédios e dinheiro.",
                    "Combine um sinal silencioso de socorro com um contato de extrema confiança.",
                    "Procure a Central 180 ou a Polícia Militar (190) para acolhimento imediato.",
                ),
            )
            IavpRiskLevel.GRAVE -> Triple(
                "Risco Elevado Identificado",
                "Existem múltiplos sinais de escalada da violência. É fundamental acionar redes formais de apoio.",
                listOf(
                    "Fortaleça sua rede de apoio com amigas, familiares ou centro de referência (CREAS/CRAS).",
                    "Guarde registros de mensagens e fotos com segurança em local externo.",
                    "Consulte a Defensoria Pública ou DEAM para avaliar pedido de Medida Protetiva.",
                ),
            )
            IavpRiskLevel.MODERADO -> Triple(
                "Pontos de Atenção Identificados",
                "Existem fatores de vulnerabilidade e instabilidade que exigem acompanhamento cauteloso.",
                listOf(
                    "Mantenha contatos de confiança salvos e informados sobre sua rotina.",
                    "Evite conversas sobre separação em locais sem saída ou sem terceiros por perto.",
                    "Busque orientação preventiva na Central 180.",
                ),
            )
            IavpRiskLevel.BAIXO -> Triple(
                "Monitorização Preventiva",
                "Poucos fatores de risco direto identificados nesta avaliação, mas permaneça atenta aos seus limites.",
                listOf(
                    "Mantenha sua autonomia financeira e relacionamento com familiares e amigas.",
                    "Refaça a avaliação caso perceba mudanças bruscas de comportamento.",
                ),
            )
        }

        return IavpEvaluationResult(
            score = score,
            riskLevel = riskLevel,
            fatalTriggerActivated = fatalTrigger,
            summaryTitle = summaryTitle,
            summaryDescription = summaryDesc,
            recommendations = recs,
            quickActions = quickActions,
        )
    }
}
