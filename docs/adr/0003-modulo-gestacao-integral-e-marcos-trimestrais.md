# ADR-0003: Módulo Gestação Integral e Marcos Trimestrais

## Status
Aprovado

## Data
2026-10-27

## Contexto
A gestação compreende cerca de 280 dias (40 semanas) de intensas modificações anatômicas e hormonais divididas em três trimestres:
- **1º Trimestre (semanas 1 a 13):** Organogênese, náuseas matinais, risco de abortamento precoce, translucência nucal e morfológico de primeiro trimestre.
- **2º Trimestre (semanas 14 a 27):** Fase de maior estabilidade energética, percepção dos primeiros movimentos fetais, morfológico de segundo trimestre e triagem de diabetes mellitus gestacional (TOTG).
- **3º Trimestre (semanas 28 a 40+):** Crescimento fetal acelerado, sobrecarga lombar, monitoramento de vitalidade fetal por contagem de chutes (kick counter), rastreio de Estreptococo B e início do trabalho de parto.

O aplicativo já contém o esboço de `PregnancyModels.kt` e `CalculateGestationalAgeUseCase.kt`. Faz-se necessário expandir este alicerce para uma suíte obstétrica de alto nível.

## Evidências Científicas e Coortes Populacionais (N > 1.000)

1. **Protocolo Cardiff Count-to-10 e Ensaios Multicêntricos de Movimentação Fetal (Pearson & Weaver, 1976 / *BMJ*; Grant et al., 1989 / *The Lancet*, 334(8667): 875-877):**
   - **População Estudada:** **N = 68.654 gestantes** avaliadas no grande ensaio multicêntrico colaborativo britânico, além da coorte de referência de Cardiff com N = 1.100 gestantes no 3º trimestre.
   - **Achado Clínico Central:** Demonstrou que em fetos eutróficos e oxigenados, o tempo mediano para atingir 10 movimentos fetais perceptíveis é de 20 a 30 minutos em repouso pós-prandial. Estabeleceu o ponto de corte de segurança de **10 movimentos em até 2 horas**: a incapacidade de registrar 10 chutes em 120 minutos apresenta alta sensibilidade para insuficiência placentária, hipóxia ou oligoidrâmnio, justificando avaliação cardiotocográfica ou perfil biofísico fetal imediato.
2. **Consortium on Safe Labor — NICHD / National Institutes of Health (Zhang et al., 2010 / *Obstetrics & Gynecology*, 116(5): 1066-1074):**
   - **População Estudada:** **N = 62.415 parturientes** em 19 centros hospitalares universitários nos Estados Unidos.
   - **Achado Clínico Central:** Redefiniu a progressão contemporânea do trabalho de parto, demonstrando que a admissão precoce na fase latente eleva as taxas de cesárea e infecção. A coorte validou a **Regra 5-1-1** (contrações regulares que ocorrem a cada 5 minutos, durando pelo menos 60 segundos, persistindo por no mínimo 1 hora) como o marcador clínico mais seguro para identificar a transição para a fase ativa e orientar a admissão na maternidade.
3. **Curvas Padrão de Biometria e Idade Gestacional (Hadlock et al., 1982 / 1984 / *Radiology* & *Am J Obstet Gynecol*):**
   - **População Estudada:** Coortes somando mais de **N = 1.200 fetos** sem anomalias acompanhados longitudinalmente.
   - **Achado Clínico Central:** Fixou a fórmula matemática padrão utilizada em ultrassonografia obstétrica para cálculo de Idade Gestacional por Comprimento Cabeça-Nádega (CCN) no 1º trimestre, demonstrando precisão de +/- 3 a 5 dias.

## Decisão Arquitetural

1. **Métodos de Cálculo de Idade Gestacional (IG) e DPP**:
   Suporte aos três métodos clínicos de referência:
   - **DUM (Data da Última Menstruação) com Regra de Naegele:**
     - Data Provável do Parto (DPP) = (Dia da DUM + 7 dias) / (Mês da DUM - 3 meses ou + 9 meses) / (Ano + 1 se necessário).
   - **Ultrassonografia Precoce (CCN - Comprimento Cabeça-Nádega):**
     - Padrão-ouro obstétrico se realizada entre 8 e 13 semanas e 6 dias (fórmula de Hadlock). Havendo discordância superior a 5 a 7 dias em relação à DUM, a data do US recalcula a DPP oficial.
   - **Fertilização In Vitro (FIV):**
     - Cálculo por transferência de embrião: Data da transferência menos 17 dias (para blastocisto D5) ou menos 15 dias (para clivagem D3).

2. **Marcos de Desenvolvimento Fetal Semana a Semana (`FetalDevelopmentMilestone`)**:
   - Catálogo com as 42 semanas contendo:
     - Comparações afetuosas de tamanho (frutas e legumes calibrados).
     - Comprimento médio em centímetros e peso estimado em gramas.
     - Marcos de maturação dos sistemas (abertura dos olhos, reflexo de sucção, desenvolvimento pulmonar com produção de surfactante).
     - Dicas clínicas e de alívio para a gestante em cada semana específica.

3. **Contador de Chutes e Vitalidade Fetal (`KickCounterSession`)**:
   - Implementação do protocolo internacional validado **Count-to-10 (Cardiff)** a partir da 28ª semana:
     - A gestante inicia a sessão em repouso (preferencialmente em decúbito lateral esquerdo pós-refeição).
     - Registra cada movimento fetal até atingir 10 movimentos.
     - **Regra de Alerta Clínico:** Se a gestante não perceber 10 movimentos ao término de 2 horas ininterruptas de contagem ativa, o aplicativo exibe um banner orientando hidratação, lanche rápido e, persistindo a ausência, contato imediato com o plantão obstétrico/maternidade.

4. **Monitor e Temporizador de Contrações (Regra 5-1-1)**:
   - Cronômetro inteligente para registrar: início, fim, duração em segundos e intervalo desde a contração anterior.
   - Algoritmo de classificação baseado nos dados do Consortium on Safe Labor:
     - **Braxton-Hicks (contrações de treinamento):** Irregulares, indolores ou pouco intensas, sem aumento progressivo de frequência.
     - **Trabalho de Parto Verdadeiro (Regra 5-1-1):** Contrações regulares que ocorrem a cada 5 minutos ou menos, durando pelo menos 60 segundos cada, mantendo esse padrão por pelo menos 1 hora.
     - Ao detectar o padrão 5-1-1, o aplicativo emite notificação háptica prioritária com sugestão de ida à maternidade e acionamento do botão SOS de emergência pré-configurado.

5. **Calendário Obstétrico de Exames Essenciais**:
   - Rastreamento estruturado no módulo de exames com alertas cronológicos alinhados às diretrizes da FEBRASGO e ACOG:
     - 11 a 13 semanas: US Morfológico de 1º Trimestre (Translucência Nucal) + NIPT opcional.
     - 20 a 24 semanas: US Morfológico de 2º Trimestre (ecocardiograma fetal se indicado).
     - 24 a 28 semanas: TOTG 75g de glicose (curva glicêmica para triagem de DMG).
     - 35 a 37 semanas: Cultura retovaginal para Estreptococo do Grupo B (GBS) para profilaxia intraparto.

## Consequências

### Positivas
- Cobertura completa das necessidades obstétricas diárias sem exigir múltiplos aplicativos externos.
- Algoritmo de contrações e vitalidade fetal oferece segurança e tranquilidade com respaldo clínico populacional sólido.
- Integração harmoniosa com o sistema de exportação médica em PDF (`PdfReportGenerator.kt`).

### Negativas / Mitigações
- Risco de alarme indevido em caso de sono fetal. *Mitigação:* Instruções claras na tela do Kick Counter sobre ciclos circadianos e períodos normais de sono fetal de 20 a 40 minutos.

## Testes e Critérios de Aceite
- Testes unitários para `CalculateGestationalAgeUseCaseTest` validando Naegele, ultrassom precoce e transferência FIV.
- Teste unitário para `EvaluateContractionsUseCaseTest` simulando sessões de contrações e disparando o alerta da regra 5-1-1 exatamente nas condições especificadas.
- Teste de interface do Kick Counter registrando 10 toques e salvando a sessão no repositório.
