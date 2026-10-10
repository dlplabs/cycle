package br.com.dlpsystems.cycle.domain.usecase

import br.com.dlpsystems.cycle.domain.model.HealthEducationalArticle
import javax.inject.Inject

class GetPregnancyContentUseCase @Inject constructor() {

    fun getWeekInfo(week: Int): HealthEducationalArticle {
        val clampedWeek = week.coerceIn(1, 42)
        return when {
            clampedWeek <= 4 -> HealthEducationalArticle(
                id = "preg_w4",
                title = "Semanas Iniciais: Implantação e Blastocele",
                summary = "Formação do saco gestacional e disco embrionário. Ocorrem divisões celulares rápidas.",
                content = "O blastocisto se fixa no endométrio decidualizado. A secreção de beta-hCG pelo sinciciotrofoblasto sustenta a produção de progesterona pelo corpo lúteo, prevenindo a descamação endometrial.",
                category = "Desenvolvimento Fetal",
                citationAuthors = "Ministério da Saúde do Brasil",
                sourceName = "Cadernos de Atenção Básica nº 32 - Atenção ao Pré-Natal de Baixo Risco",
                publicationDate = "2013, atualizado conforme Protocolos da Atenção Básica",
                referenceUrl = "https://bvsms.saude.gov.br/bvs/publicacoes/cadernos_atencao_basica_32_prenatal.pdf",
                keyTakeaways = listOf(
                    "Início do suplemento de ácido fólico (400 mcg/dia) para fechamento do tubo neural",
                    "Evitar medicamentos sem orientação médica, álcool e tabagismo",
                ),
            )
            clampedWeek <= 8 -> HealthEducationalArticle(
                id = "preg_w8",
                title = "Semana 5 a 8: Organogênese e Batimentos Cardíacos",
                summary = "Desenvolvimento do tubo neural, membros primitivos e primeiros batimentos detectáveis.",
                content = "Fase crítica de diferenciação embrionária. O coração tubular primitivo começa a contrair ritmicamente entre a 5ª e 6ª semana. O botão dos membros superiores e inferiores se projeta.",
                category = "Desenvolvimento Fetal",
                citationAuthors = "American College of Obstetricians and Gynecologists (ACOG)",
                sourceName = "Clinical Consensus: Early Pregnancy Care",
                publicationDate = "2023",
                referenceUrl = "https://www.acog.org/clinical/clinical-guidance",
                keyTakeaways = listOf(
                    "Agendamento da primeira consulta de pré-natal",
                    "Exames de sangue iniciais: tipagem sanguínea, sorologias e hemograma",
                ),
            )
            clampedWeek <= 13 -> HealthEducationalArticle(
                id = "preg_w12",
                title = "Semana 9 a 13: Transição Fetal e Rastreio",
                summary = "Final do período embrionário. Rastreio morfogenético de primeiro trimestre.",
                content = "O embrião agora é denominado feto. Estruturas anatômicas faciais e genitais externos começam a se definir. Janela ideal para o ultrassom morfológico com translucência nucal (11 a 13 semanas e 6 dias).",
                category = "Desenvolvimento Fetal",
                citationAuthors = "Federação Brasileira das Associações de Ginecologia e Obstetrícia (FEBRASGO)",
                sourceName = "Manual de Assistência Pré-Natal",
                publicationDate = "2020",
                referenceUrl = "https://www.febrasgo.org.br/pt/manuais",
                keyTakeaways = listOf(
                    "Ultrassom morfológico de 1º trimestre",
                    "Exames sorológicos e glicemia de jejum",
                ),
            )
            clampedWeek <= 27 -> HealthEducationalArticle(
                id = "preg_w20",
                title = "Segundo Trimestre: Movimentos Fetais e Morfológico",
                summary = "Percepção dos primeiros movimentos (quickening) e maturação auditiva.",
                content = "Entre a 18ª e 22ª semana ocorre a ultrassonografia morfológica detalhada para avaliar integridade de órgãos e placenta. O feto desenvolve vernix caseosa e lanugem protetora.",
                category = "Desenvolvimento Fetal",
                citationAuthors = "Organização Mundial da Saúde (OMS)",
                sourceName = "WHO recommendations on antenatal care for a positive pregnancy experience",
                publicationDate = "2016 (Revisões 2021)",
                referenceUrl = "https://www.who.int/publications/i/item/9789241549912",
                keyTakeaways = listOf(
                    "Ultrassom morfológico de 2º trimestre (18-24 semanas)",
                    "Teste oral de tolerância à glicose (TOTG) entre 24 e 28 semanas",
                ),
            )
            else -> HealthEducationalArticle(
                id = "preg_w36",
                title = "Terceiro Trimestre: Crescimento e Preparação",
                summary = "Ganho ponderal fetal acentuado, produção de surfactante e posição cefálica.",
                content = "O feto acumula gordura subcutânea e os pulmões completam a síntese de surfactante alveolar. Consultas pré-natais tornam-se mais frequentes para aferição de pressão arterial, altura uterina e vitalidade fetal.",
                category = "Desenvolvimento Fetal",
                citationAuthors = "Ministério da Saúde do Brasil",
                sourceName = "Diretrizes Nacionais de Assistência ao Parto Normal",
                publicationDate = "2022",
                referenceUrl = "https://www.gov.br/saude/pt-br/assuntos/saude-de-a-a-z/p/parto-humanizado",
                keyTakeaways = listOf(
                    "Pesquisa de estreptococo do grupo B (GBS) entre 35 e 37 semanas",
                    "Atenção aos sinais de alerta: perda líquida, sangramento, cefaleia intensa ou redução de movimentos",
                ),
            )
        }
    }

    fun getChildbirthEducation(): List<HealthEducationalArticle> = listOf(
        HealthEducationalArticle(
            id = "birth_vaginal",
            title = "Parto Vaginal (Normal): Benefícios e Evidências",
            summary = "Evolução fisiológica com recuperação mais rápida e contato pele a pele imediato.",
            content = "O parto vaginal respeita a maturação fetal espontânea. A passagem pelo canal vaginal auxilia na expulsão de fluidos das vias aéreas do recém-nascido e colonização por microbiota benéfica. Para a parturiente, apresenta menor tempo de internação e menor risco de complicações infecciosas ou hemorrágicas em gestações de risco habitual.",
            category = "Parto",
            citationAuthors = "Organização Mundial da Saúde (OMS)",
            sourceName = "Intrapartum Care for a Positive Childbirth Experience",
            publicationDate = "2018",
            referenceUrl = "https://www.who.int/publications/i/item/9789241550215",
            keyTakeaways = listOf(
                "Direito a acompanhante de livre escolha garantido por lei (Lei Federal nº 11.108)",
                "Métodos não farmacológicos de alívio da dor (banho morno, massagem, bola de pilates)",
                "Contato pele a pele imediato na primeira hora de vida",
            ),
        ),
        HealthEducationalArticle(
            id = "birth_cesarean",
            title = "Cesariana: Indicações Médicas e Considerações Cirúrgicas",
            summary = "Procedimento cirúrgico de vital importância quando há indicações clínicas precisas.",
            content = "A cesariana é uma cirurgia essencial para salvar vidas maternas e fetais em situações de descolamento prematuro de placenta, prolapso de cordão, sofrimento fetal agudo, placenta prévia oclusiva total ou apresentações anômalas não corrigidas. Por ser cirurgia de médio porte, envolve tempo maior de recuperação puerperal, analgesia pós-operatória e riscos inerentes a procedimentos cirúrgicos abdominais.",
            category = "Parto",
            citationAuthors = "FEBRASGO / Ministério da Saúde",
            sourceName = "Diretrizes de Atenção à Gestante: A Operação Cesariana",
            publicationDate = "2016 (Cadernos de Saúde Pública)",
            referenceUrl = "https://pesquisa.bvsalud.org/portal/resource/pt/mis-37887",
            keyTakeaways = listOf(
                "Indicação fundamentada em critérios clínicos de segurança da mãe e do bebê",
                "Planejamento anestésico (raquianestesia/peridural) e suporte cirúrgico estéril",
                "Cuidados pós-operatórios com incisão, movimentação precoce e hidratação",
            ),
        ),
    )

    fun getBreastfeedingArticles(): List<HealthEducationalArticle> = listOf(
        HealthEducationalArticle(
            id = "breastfeeding_basics",
            title = "Amamentação: Pega Correta e Apoio Clínico",
            summary = "Diretrizes da OMS e Ministério da Saúde para aleitamento materno exclusivo.",
            content = "O aleitamento materno exclusivo é recomendado até os 6 meses e continuado até 2 anos ou mais. A pega adequada envolve boca bem aberta abrangendo grande parte da aréola, lábio inferior evertido (em peixinho) e queixo encostado na mama, prevenindo fissuras mamilares e garantindo esvaziamento mamário eficaz.",
            category = "Amamentação",
            citationAuthors = "Ministério da Saúde do Brasil / Sociedade Brasileira de Pediatria (SBP)",
            sourceName = "Guia Alimentar para Crianças Brasileiras Menores de 2 Anos",
            publicationDate = "2019",
            referenceUrl = "https://bvsms.saude.gov.br/bvs/publicacoes/guia_alimentar_criancas_menores_2anos.pdf",
            keyTakeaways = listOf(
                "Livre demanda: amamentar quando o bebê demonstrar sinais de fome",
                "Colostro é a primeira vacina do bebê, rico em imunoglobulinas (IgA)",
                "Procurar bancos de leite humano ou consultoria em amamentação se houver dor",
            ),
        ),
    )

    fun getExerciseGuidance(): HealthEducationalArticle = HealthEducationalArticle(
        id = "pregnancy_exercises",
        title = "Exercícios e Movimento na Gestação",
        summary = "Atividades físicas de intensidade leve a moderada auxiliam no alívio de queixas osteomusculares.",
        content = "Caminhadas, hidroginástica, pilates clínico e yoga pré-natal fortalecem o assoalho pélvico e amenizam a sobrecarga lombar decorrente do deslocamento do centro de gravidade. A prática deve respeitar a frequência cardíaca materna confortável (teste da fala) e ser sempre validada pela equipe médica responsável pelo pré-natal.",
        category = "Exercícios",
        citationAuthors = "American College of Obstetricians and Gynecologists (ACOG)",
        sourceName = "Physical Activity and Exercise During Pregnancy and the Postpartum Period (ACOG Committee Opinion 804)",
        publicationDate = "2020 (Reafirmado em 2023)",
        referenceUrl = "https://www.acog.org/clinical/clinical-guidance/committee-opinion/articles/2020/04/physical-activity-and-exercise-during-pregnancy-and-the-postpartum-period",
        keyTakeaways = listOf(
            "Fortalecimento suave do assoalho pélvico (exercícios de Kegel)",
            "Alongamento lombar e mobilidade de quadril para alívio de dor ciática",
            "Manter hidratação adequada e evitar ambientes com calor excessivo",
        ),
        contraindicationsOrAlerts = listOf(
            "Contraindicações absolutas: sangramento genital não esclarecido, placenta prévia após 26 semanas, pré-eclâmpsia, incompetência istmo-cervical ou rotura prematura de membranas.",
            "Suspender imediatamente e buscar emergência obstétrica em caso de tontura súbita, contrações regulares dolorosas antes do termo, dor no peito ou perda de líquido amniótico.",
        ),
    )
}
