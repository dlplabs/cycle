package br.com.dlpsystems.cycle.domain.usecase

import br.com.dlpsystems.cycle.domain.model.Citation
import br.com.dlpsystems.cycle.domain.model.CyclePhase
import br.com.dlpsystems.cycle.domain.model.PhaseEvidence
import br.com.dlpsystems.cycle.domain.model.PillarGuidance
import br.com.dlpsystems.cycle.domain.model.WellnessPillar
import javax.inject.Inject

class GetPhaseInsightsUseCase @Inject constructor() {
    operator fun invoke(phase: CyclePhase): PhaseEvidence = when (phase) {
        CyclePhase.MENSTRUAL -> menstrual
        CyclePhase.FOLLICULAR -> follicular
        CyclePhase.OVULATORY -> ovulatory
        CyclePhase.LUTEAL -> luteal
    }

    private val menstrual = PhaseEvidence(
        phase = CyclePhase.MENSTRUAL,
        physiology = "Queda de estradiol e progesterona após a lise do corpo lúteo, " +
            "com descamação do endométrio. Prostaglandinas endometriais (PGF2a) aumentam " +
            "as contrações miometriais, e as reservas de ferro sérico são consumidas.",
        pillars = listOf(
            PillarGuidance(
                pillar = WellnessPillar.NUTRITION,
                guidance = "Prefira alimentos ricos em ferro, como espinafre, lentilhas e feijão preto, " +
                    "junto de fontes de vitamina C (laranja, kiwi, limão). Mantenha a hidratação elevada.",
                citation = Citation(
                    authors = "Bull et al.",
                    source = "Nature npj Digital Medicine (2019)",
                    finding = "Em 124.648 mulheres, a duração do ciclo varia o bastante para a previsão ser probabilística, não uma data fixa.",
                    url = "https://www.nature.com/articles/s41746-019-0152-7",
                ),
                suggestions = listOf(
                    "Chás mornos de gengibre ou camomila para relaxamento uterino",
                    "Alimentos ricos em ferro e vitamina C (espinafre, feijão com gotas de limão)",
                    "Manter garrafinha de água por perto para evitar retenção por desidratação",
                ),
            ),
            PillarGuidance(
                pillar = WellnessPillar.EXERCISE,
                guidance = "Priorize descanso reparador, caminhadas lentas, alongamentos e posturas suaves de yoga.",
                citation = Citation(
                    authors = "Armour et al.",
                    source = "Cochrane / BMC (2019)",
                    finding = "Exercício pode reduzir a intensidade da dismenorreia.",
                    url = "https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6337810/",
                ),
                suggestions = listOf(
                    "Alongamentos suaves de coluna e quadris",
                    "Postura da criança (Balasana) para alívio lombar",
                    "Caminhadas leves de 15 a 20 minutos se tiver disposição",
                ),
            ),
            PillarGuidance(
                pillar = WellnessPillar.SKIN,
                guidance = "A barreira cutânea fica mais sensível e perde água. Foque em hidratação oclusiva, " +
                    "ceramidas e pantenol.",
                citation = null,
                suggestions = listOf(
                    "Limpeza com produtos cremosos e sem sabão agressivo",
                    "Hidratação rica com ceramidas e pantenol",
                    "Evitar esfoliantes fortes ou ácidos irritantes nestes dias",
                ),
            ),
            PillarGuidance(
                pillar = WellnessPillar.MIND,
                guidance = "Reduza a carga do dia e use calor local como cuidado de conforto, sem substituir avaliação médica se a dor for intensa.",
                citation = Citation(
                    authors = "Armour et al.",
                    source = "Revisão de termoterapia",
                    finding = "Calor local superficial contínuo é uma medida de alívio estudada para cólica.",
                    url = "https://www.ncbi.nlm.nih.gov/pmc/articles/PMC12876241/",
                ),
                suggestions = listOf(
                    "Bolsa de água morna na região pélvica e lombar",
                    "Priorizar dormir 30 a 60 minutos mais cedo",
                    "Dizer não a tarefas não urgentes e respeitar seu tempo",
                ),
            ),
        ),
    )

    private val follicular = PhaseEvidence(
        phase = CyclePhase.FOLLICULAR,
        physiology = "O FSH recruta folículos e o estradiol sobe de forma progressiva. " +
            "Essa subida favorece sensibilidade à insulina, recaptação de dopamina e serotonina " +
            "e a regeneração tecidual.",
        pillars = listOf(
            PillarGuidance(
                pillar = WellnessPillar.NUTRITION,
                guidance = "Refeições regulares, com proteína e vegetais, acompanham o aumento de energia desta fase.",
                citation = null,
                suggestions = listOf(
                    "Refeições coloridas com bastante proteína magra e vegetais",
                    "Fermentados naturais (iogurte, kefir) para flora intestinal",
                    "Carboidratos integrais para energia sustentada",
                ),
            ),
            PillarGuidance(
                pillar = WellnessPillar.EXERCISE,
                guidance = "É uma janela favorável a cargas progressivas, musculação intensa e treinos intervalados.",
                citation = Citation(
                    authors = "McNulty et al.",
                    source = "Sports Medicine (2020)",
                    finding = "Metanálise com mais de 1.200 mulheres sobre desempenho e a fase do ciclo.",
                    url = "https://pubmed.ncbi.nlm.nih.gov/32661839/",
                ),
                suggestions = listOf(
                    "Treinos de força e musculação com progressão de carga",
                    "Aulas de ritmo acelerado ou treinos funcionais",
                    "Corridas ou treinos intervalados (HIIT)",
                ),
            ),
            PillarGuidance(
                pillar = WellnessPillar.SKIN,
                guidance = "A pele tende a ficar mais luminosa. Renovação suave com esfoliação química branda (AHA) " +
                    "e antioxidantes tópicos, como vitamina C, costuma ser bem tolerada.",
                citation = Citation(
                    authors = "Raghunath et al.",
                    source = "Clinical and Experimental Dermatology (2015)",
                    finding = "A função de barreira da pele muda ao longo do ciclo.",
                    url = "https://doi.org/10.1111/ced.12588",
                ),
                suggestions = listOf(
                    "Vitamina C tópica para potencializar a luminosidade natural",
                    "Esfoliação química suave (ácido lático ou mandélico)",
                    "Protetor solar diário com toque seco",
                ),
            ),
            PillarGuidance(
                pillar = WellnessPillar.MIND,
                guidance = "Muitas pessoas percebem mais clareza e criatividade. Pode ser um bom momento para iniciar projetos e resolver problemas analíticos.",
                citation = null,
                suggestions = listOf(
                    "Planejar metas, projetos e novos hábitos do mês",
                    "Agendar reuniões estratégicas ou sessões criativas",
                    "Aproveitar o ânimo para aprender algo novo",
                ),
            ),
        ),
    )

    private val ovulatory = PhaseEvidence(
        phase = CyclePhase.OVULATORY,
        physiology = "O pico de estradiol dispara o pico de LH, com ruptura folicular e liberação do óvulo. " +
            "Há também um pico transitório e discreto de testosterona livre.",
        pillars = listOf(
            PillarGuidance(
                pillar = WellnessPillar.NUTRITION,
                guidance = "Mantenha hidratação constante e refeições leves, que acompanham o metabolismo mais ativo destes dias.",
                citation = null,
                suggestions = listOf(
                    "Refeições leves com gorduras boas (abacate, azeite de oliva, nozes)",
                    "Alimentos ricos em zinco e antioxidantes (sementes, frutas vermelhas)",
                    "Água de coco e sucos naturais refrescantes",
                ),
            ),
            PillarGuidance(
                pillar = WellnessPillar.EXERCISE,
                guidance = "Atividades aeróbicas contínuas, treinos funcionais e esportes em grupo combinam com a energia desta fase.",
                citation = null,
                suggestions = listOf(
                    "Treinos em grupo ou esportes ao ar livre",
                    "Dança, natação ou circuitos aeróbicos dinâmicos",
                    "Aproveite o pico de coordenação motora e disposição",
                ),
            ),
            PillarGuidance(
                pillar = WellnessPillar.SKIN,
                guidance = "A hidratação basal da pele costuma estar no auge. Texturas leves em gel e proteção solar de amplo espectro (FPS 50+) ajudam a manter o viço.",
                citation = Citation(
                    authors = "Raghunath et al.",
                    source = "Clinical and Experimental Dermatology (2015)",
                    finding = "A barreira cutânea não é constante entre as fases.",
                    url = "https://doi.org/10.1111/ced.12588",
                ),
                suggestions = listOf(
                    "Protetor solar FPS 50+ reaplicado com frequência",
                    "Hidratantes leves em textura aquosa ou gel",
                    "Limpeza suave para controlar a oleosidade sutil",
                ),
            ),
            PillarGuidance(
                pillar = WellnessPillar.MIND,
                guidance = "O período periovulatório é associado a mais assertividade na comunicação. Pode ser um momento propício para conversas importantes, reuniões e apresentações.",
                citation = Citation(
                    authors = "Bull et al.",
                    source = "Nature npj Digital Medicine (2019)",
                    finding = "A janela ovulatória estimada é uma probabilidade, não um dia exato.",
                    url = "https://www.nature.com/articles/s41746-019-0152-7",
                ),
                suggestions = listOf(
                    "Apresentações em público e conversas decisivas",
                    "Encontros sociais, eventos e conexão com pessoas queridas",
                    "Expressar ideias e liderar projetos em equipe",
                ),
            ),
        ),
    )

    private val luteal = PhaseEvidence(
        phase = CyclePhase.LUTEAL,
        physiology = "O corpo lúteo secreta progesterona e a temperatura basal sobe. " +
            "No fim da fase, a queda hormonal pode instabilizar o tônus serotoninérgico e trazer sintomas pré-menstruais.",
        pillars = listOf(
            PillarGuidance(
                pillar = WellnessPillar.NUTRITION,
                guidance = "Carboidratos complexos de baixo índice glicêmico, fontes de triptofano (aveia, sementes, banana) e alimentos ricos em magnésio ajudam a estabilidade. Uma alimentação com ômega-3 também é estudada para irritabilidade.",
                citation = Citation(
                    authors = "Fathizadeh et al.",
                    source = "Journal of Caring Sciences (2010)",
                    finding = "Magnésio com piridoxina (B6) reduziu sintomas afetivos e somáticos da síndrome pré-menstrual.",
                    url = "https://www.ncbi.nlm.nih.gov/pmc/articles/PMC3208934/",
                ),
                suggestions = listOf(
                    "Alimentos ricos em magnésio (sementes de abóbora, chocolate 70%, banana)",
                    "Fontes de ômega-3 (chia, linhaça, peixes) para modulação inflamatória",
                    "Reduzir cafeína e refrigerantes para amenizar irritabilidade e inchaço",
                ),
            ),
            PillarGuidance(
                pillar = WellnessPillar.EXERCISE,
                guidance = "Reduza o impacto. Pilates, caminhadas ao ar livre e mobilidade ajudam sem elevar tanto o cortisol.",
                citation = Citation(
                    authors = "Baker & Driver",
                    source = "Sleep Medicine (2007)",
                    finding = "A fase lútea se associa a mudança de temperatura basal e do sono.",
                    url = "https://doi.org/10.1016/j.sleep.2006.09.011",
                ),
                suggestions = listOf(
                    "Pilates e ioga focados em respiração e estabilidade",
                    "Caminhadas restaurativas ao ar livre",
                    "Treinos de intensidade moderada, ouvindo o ritmo do corpo",
                ),
            ),
            PillarGuidance(
                pillar = WellnessPillar.SKIN,
                guidance = "No fim da fase lútea, poros e glândulas sebáceas podem ficar mais ativos. Cuidados anti-inflamatórios suaves, como niacinamida, ajudam sem agredir a barreira.",
                citation = Citation(
                    authors = "Raghunath et al.",
                    source = "Clinical and Experimental Dermatology (2015)",
                    finding = "Mudanças de barreira cutânea ajudam a explicar a pele mais reativa no fim do ciclo.",
                    url = "https://doi.org/10.1111/ced.12588",
                ),
                suggestions = listOf(
                    "Niacinamida para equilíbrio da oleosidade e poros",
                    "Cuidado redobrado com a higienização facial noturna",
                    "Evitar mexer em eventuais espinhas pré-menstruais",
                ),
            ),
            PillarGuidance(
                pillar = WellnessPillar.MIND,
                guidance = "Rituais noturnos de descompressão e uma higiene de sono estável ajudam a atravessar a queda hormonal do fim do ciclo.",
                citation = Citation(
                    authors = "Baker & Driver",
                    source = "Sleep Medicine (2007)",
                    finding = "Higiene do sono importa mais quando a temperatura basal está elevada.",
                    url = "https://doi.org/10.1016/j.sleep.2006.09.011",
                ),
                suggestions = listOf(
                    "Higiene do sono rigorosa: quarto fresco e longe de telas",
                    "Chás relaxantes de erva-cidreira ou melissa no fim da tarde",
                    "Exercícios de respiração guiada (como a técnica 4-7-8)",
                ),
            ),
        ),
    )
}
