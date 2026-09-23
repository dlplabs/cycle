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
            ),
            PillarGuidance(
                pillar = WellnessPillar.SKIN,
                guidance = "A barreira cutânea fica mais sensível e perde água. Foque em hidratação oclusiva, " +
                    "ceramidas e pantenol.",
                citation = null,
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
            ),
            PillarGuidance(
                pillar = WellnessPillar.MIND,
                guidance = "Muitas pessoas percebem mais clareza e criatividade. Pode ser um bom momento para iniciar projetos e resolver problemas analíticos.",
                citation = null,
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
            ),
            PillarGuidance(
                pillar = WellnessPillar.EXERCISE,
                guidance = "Atividades aeróbicas contínuas, treinos funcionais e esportes em grupo combinam com a energia desta fase.",
                citation = null,
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
            ),
        ),
    )
}
