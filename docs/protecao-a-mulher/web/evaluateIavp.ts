/**
 * Motor de Avaliação de Risco IAVP (Instrumento de Avaliação de Risco em Violência Doméstica)
 * Executado 100% Client-Side no ecossistema Cycle (cycle.com.br)
 * Ephemeral By Design: Não grava respostas em localStorage, cookies ou IndexedDB.
 */

export interface IavpQuestion {
  id: string;
  category: 1 | 2 | 3;
  weight: number;
  questionText: string;
  detailText: string;
}

export enum IavpRiskLevel {
  EXTREMO = 'EXTREMO',
  GRAVE = 'GRAVE',
  MODERADO = 'MODERADO',
  BAIXO = 'BAIXO',
}

export interface IavpEvaluationResult {
  score: number;
  riskLevel: IavpRiskLevel;
  fatalTriggerActivated: boolean;
  summaryTitle: string;
  summaryDescription: string;
  recommendations: string[];
}

export const IAVP_QUESTIONS: IavpQuestion[] = [
  // Categoria 1: Gatilhos Críticos de Letalidade (Peso 4 + Override)
  {
    id: 'Q01',
    category: 1,
    weight: 4,
    questionText: 'Houve alguma tentativa ou histórico de sufocamento, estrangulamento ou asfixia?',
    detailText: 'Apertar o pescoço, cobrir boca/nariz ou impedir a respiração em qualquer momento.',
  },
  {
    id: 'Q02',
    category: 1,
    weight: 4,
    questionText: 'O agressor usou ou ameaçou usar arma de fogo, faca ou objeto cortante?',
    detailText: 'Exibição de armas ou objetos cortantes com intenção de intimidar ou ferir.',
  },
  {
    id: 'Q03',
    category: 1,
    weight: 4,
    questionText: 'Existe ameaça explícita de morte ou frases como "se não for minha, não será de mais ninguém"?',
    detailText: 'Declarações diretas de que tirará sua vida ou cometerá ato extremo.',
  },
  {
    id: 'Q04',
    category: 1,
    weight: 4,
    questionText: 'Ocorreu agressão física durante a gestação ou no período pós-parto?',
    detailText: 'Empurrões, chutes ou socos ocorridos durante a gravidez ou puerpério.',
  },

  // Categoria 2: Indicadores Graves de Escalada (Peso 2)
  {
    id: 'Q05',
    category: 2,
    weight: 2,
    questionText: 'Existe perseguição persistente (stalking), monitoramento do celular ou de sua rotina?',
    detailText: 'Rastreamento sem consentimento, controle de redes sociais ou presença constante nos locais que você frequenta.',
  },
  {
    id: 'Q06',
    category: 2,
    weight: 2,
    questionText: 'Ocorreram agressões físicas anteriores com hematomas ou necessidade de atendimento médico?',
    detailText: 'Lesões visíveis, cortes ou idas ao hospital/unidade de saúde.',
  },
  {
    id: 'Q07',
    category: 2,
    weight: 2,
    questionText: 'Há ameaças diretas contra filhos, familiares próximos ou animais de estimação?',
    detailText: 'Uso de entes queridos ou pets como forma de coação psicológica.',
  },
  {
    id: 'Q08',
    category: 2,
    weight: 2,
    questionText: 'Houve término recente do relacionamento (últimos 3 meses) ou recusa do agressor em aceitar a separação?',
    detailText: 'Aumento de episódios agressivos após pedido de término ou afastamento.',
  },
  {
    id: 'Q09',
    category: 2,
    weight: 2,
    questionText: 'Houve descumprimento de Medida Protetiva de Urgência anteriormente decretada?',
    detailText: 'Aproximação não autorizada violando ordem judicial prévia.',
  },

  // Categoria 3: Fatores de Risco e Vulnerabilidade Situacional (Peso 1)
  {
    id: 'Q10',
    category: 3,
    weight: 1,
    questionText: 'O agressor faz uso abusivo ou diário de álcool, medicamentos ou substâncias ilícitas?',
    detailText: 'Alterações graves de comportamento induzidas por substâncias.',
  },
  {
    id: 'Q11',
    category: 3,
    weight: 1,
    questionText: 'O agressor enfrenta desemprego recente ou grave instabilidade financeira associada a explosões de raiva?',
    detailText: 'Estresse econômico utilizado como estopim para descontrole verbal ou físico.',
  },
  {
    id: 'Q12',
    category: 3,
    weight: 1,
    questionText: 'Há ciúme obsessivo ou isolamento forçado de seus amigos e familiares?',
    detailText: 'Proibição de trabalhar, estudar ou manter contato com sua rede de apoio.',
  },
  {
    id: 'Q13',
    category: 3,
    weight: 1,
    questionText: 'O agressor possui histórico de violência com outras parceiras ou antecedentes criminais?',
    detailText: 'Registros anteriores de agressão contra ex-relacionamentos ou terceiros.',
  },
];

export function evaluateIavpRisk(answers: Record<string, boolean>): IavpEvaluationResult {
  const cat1Positives = IAVP_QUESTIONS.filter((q) => q.category === 1 && answers[q.id] === true);
  const cat2Positives = IAVP_QUESTIONS.filter((q) => q.category === 2 && answers[q.id] === true);
  const cat3Positives = IAVP_QUESTIONS.filter((q) => q.category === 3 && answers[q.id] === true);

  const fatalTriggerActivated = cat1Positives.length > 0;
  const score = cat1Positives.length * 4 + cat2Positives.length * 2 + cat3Positives.length * 1;

  let riskLevel: IavpRiskLevel;
  if (fatalTriggerActivated || score >= 10) {
    riskLevel = IavpRiskLevel.EXTREMO;
  } else if (score >= 6) {
    riskLevel = IavpRiskLevel.GRAVE;
  } else if (score >= 3) {
    riskLevel = IavpRiskLevel.MODERADO;
  } else {
    riskLevel = IavpRiskLevel.BAIXO;
  }

  const recommendationsMap: Record<IavpRiskLevel, { title: string; desc: string; recs: string[] }> = {
    [IavpRiskLevel.EXTREMO]: {
      title: 'Situação de Risco Extremo Detectada',
      desc: 'Os indicadores apontam perigo iminente. Priorize sua integridade física e evite confrontar o agressor.',
      recs: [
        'Não anuncie nem comunique a intenção de saída ao agressor.',
        'Monte uma bolsa discreta de emergência com documentos, chaves reserva, remédios e dinheiro.',
        'Combine um sinal silencioso de socorro com um contato de extrema confiança.',
        'Procure a Central 180 ou a Polícia Militar (190) para acolhimento imediato.',
      ],
    },
    [IavpRiskLevel.GRAVE]: {
      title: 'Risco Elevado Identificado',
      desc: 'Existem múltiplos sinais de escalada da violência. É fundamental acionar redes formais de apoio.',
      recs: [
        'Fortaleça sua rede de apoio com amigas, familiares ou centro de referência (CREAS/CRAS).',
        'Guarde registros de mensagens e fotos com segurança em local externo.',
        'Consulte a Defensoria Pública ou DEAM para avaliar pedido de Medida Protetiva.',
      ],
    },
    [IavpRiskLevel.MODERADO]: {
      title: 'Pontos de Atenção Identificados',
      desc: 'Existem fatores de vulnerabilidade e instabilidade que exigem acompanhamento cauteloso.',
      recs: [
        'Mantenha contatos de confiança salvos e informados sobre sua rotina.',
        'Evite conversas sobre separação em locais sem saída ou sem terceiros por perto.',
        'Busque orientação preventiva na Central 180.',
      ],
    },
    [IavpRiskLevel.BAIXO]: {
      title: 'Monitorização Preventiva',
      desc: 'Poucos fatores de risco direto identificados nesta avaliação, mas permaneça atenta aos seus limites.',
      recs: [
        'Mantenha sua autonomia financeira e relacionamento com familiares e amigas.',
        'Refaça a avaliação caso perceba mudanças bruscas de comportamento.',
      ],
    },
  };

  const config = recommendationsMap[riskLevel];

  return {
    score,
    riskLevel,
    fatalTriggerActivated,
    summaryTitle: config.title,
    summaryDescription: config.desc,
    recommendations: config.recs,
  };
}
