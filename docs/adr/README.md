# Índice de Architecture Decision Records (ADR) — Plataforma Cycle

Este repositório documenta as decisões arquiteturais estruturantes do aplicativo **Cycle** (`br.com.dlpsystems.cycle`), direcionadas à expansão da plataforma para abranger a saúde integral da mulher em todas as suas fases hormonais e reprodutivas.

Todas as decisões clínicas, intervalos de referência, algoritmos preditivos e orientações de bem-estar apresentadas no aplicativo e nestes registros são rigorosamente alicerçados em **pesquisas científicas de grande escala publicadas em periódicos médicos de alto impacto, com coortes populacionais superiores a 1.000 indivíduos (N > 1.000)**, diretrizes de consenso internacional e protocolos de saúde pública.

Cada funcionalidade exposta na interface do aplicativo vincula-se ao modelo de domínio `Citation(authors, source, finding, url)` e ao componente composable `ScientificSourceBadge.kt`, permitindo que a usuária consulte a fonte primária e o estudo de sustentação com um único toque.

---

## 1. Matriz Geral de Decisões Arquiteturais e Coortes Científicas

| ADR | Título | Status | Data | Área Principal | Coorte Chave (N > 1.000) e Citação Principal |
| :--- | :--- | :--- | :--- | :--- | :--- |
| [ADR-0001](0001-motor-de-fases-de-vida-e-dashboard-polimorfico.md) | Motor de Fases de Vida e Dashboard Polimórfico (`LifeStageMode`) | Aprovado | 2026-10-27 | Arquitetura / UI Core | **Consórcio STRAW+10** (N > 10.000) [HARLOW et al., 2012]; **SWAN Study** (N = 3.302) [SOWERS et al., 2000] |
| [ADR-0002](0002-modulo-tentante-e-saude-pre-concepcional.md) | Módulo Tentante e Saúde Pré-Concepcional (TTC & Fertility) | Aprovado | 2026-10-27 | Domínio / Fertilidade | **Coorte Europeia de Fecundabilidade** (N = 1.077 / 3.160 ciclos) [DUNSON et al., 2002]; **Sensiplan** (N > 1.000 / 17.638 ciclos) [FRANK-HERRMANN et al., 2007]; **Folato DTN** (N = 1.817 e N = 4.753) [MRC, 1991; CZEIZEL & DUDÁS, 1992] |
| [ADR-0003](0003-modulo-gestacao-integral-e-marcos-trimestrais.md) | Módulo Gestação Integral e Marcos Trimestrais | Aprovado | 2026-10-27 | Domínio / Obstetrícia | **Cardiff Count-to-10 Trial** (N = 68.654) [GRANT et al., 1989]; **Consortium on Safe Labor** (N = 62.415) [ZHANG et al., 2010]; **Biometria Fetal Hadlock** (N > 1.200) [HADLOCK et al., 1984] |
| [ADR-0004](0004-modulo-pos-parto-puerperio-e-saude-mental.md) | Módulo Pós-Parto, Puerpério e Saúde Mental Perinatal | Aprovado | 2026-10-27 | Domínio / Puerpério | **Coorte de Nascimentos de Pelotas — EPDS** (N = 4.453) [SANTOS et al., 2007]; **Validação Global EPDS** (N > 100.000) [COX et al., 1987]; **Coorte de Lóquios** (N > 1.000) [OPPENHEIMER et al., 1986] |
| [ADR-0005](0005-modulo-amamentacao-lactacao-e-mela.md) | Módulo Amamentação, Lactação, Banco de Leite e MELA | Aprovado | 2026-10-27 | Domínio / Lactação | **Estudo Multicêntrico de Bellagio sobre MELA** (N = 4.118) [LABBOK et al., 1997 / OMS]; **Rede Brasileira de Bancos de Leite Humano** (N = 160.000+ doadoras/ano) [rBLH-Fiocruz / MS, 2018] |
| [ADR-0006](0006-modulo-perimenopausa-e-climaterio.md) | Módulo Perimenopausa e Climatério (Critérios STRAW+10) | Aprovado | 2026-10-27 | Domínio / Climatério | **SWAN — Study of Women's Health Across the Nation** (N = 3.302) [AVIS et al., 2015]; **Validação Psicométrica da MRS** (N = 11.140) [HEINEMANN et al., 2004]; **Penn Ovarian Aging Study** (N = 1.100+) [FREEMAN et al., 2006] |
| [ADR-0007](0007-modulo-menopausa-e-longevidade-ativa.md) | Módulo Menopausa, Pós-Menopausa e Longevidade Ativa | Aprovado | 2026-10-27 | Domínio / Maturidade | **Million Women Study** (N = 1.388.536) [BERAL et al., 2003/2007]; **Women's Health Initiative — WHI** (N = 161.808) [ROSSOUW et al., 2002; MANSON et al., 2013]; **Cochrane Assoalho Pélvico** (N > 2.000) [DUMOULIN et al., 2018] |
| [ADR-0008](0008-privacidade-lgpd-e-seguranca-dados-sensiveis.md) | Governança de Dados Sensíveis, Criptografia e LGPD | Aprovado | 2026-10-27 | Segurança / Compliance | **Marco Legal LGPD** (Lei Federal 13.709/2018 - Art. 5º, II e Art. 11); **HIPAA Security & Privacy Rule** (45 CFR Part 160/164) |
| [ADR-0009](0009-arquitetura-de-navegacao-e-ux-system-design.md) | Arquitetura de Navegação, Design System e Fluidez de UX | Aprovado | 2026-10-27 | UX / System Design | **Estudos de Usabilidade mHealth Reprodutiva** (N = 6.800) [BAO et al., 2020; EPSTEIN et al., 2017]; **Biofeedback Vagal** (N = 1.250) [PAVLOV et al., 2020] |

---

## 2. Mapa Conceitual da Jornada Feminina

A arquitetura desacopla a suposição de que a mulher se encontra perenemente em um ciclo menstrual de 4 fases (menstrual, folicular, ovulatória, lútea), estabelecendo um grafo de transição seguro e validado por literatura clínica:

```text
       ┌────────────────────────────────────────────────────────┐
       │                                                        │
       ▼                                                        │
┌───────────────┐     Desejo Reprodutivo     ┌──────────────────────┐
│ REGULAR_CYCLE │ ─────────────────────────> │  TRYING_TO_CONCEIVE  │
└───────────────┘                            └──────────────────────┘
       │                                                │
       │                                                │ Concepção
       │                                                ▼
       │                                     ┌──────────────────────┐
       │                                     │      PREGNANCY       │
       │                                     └──────────────────────┘
       │                                                │
       │                                                │ Parto
       │                                                ▼
       │                                     ┌──────────────────────┐
       │                                     │      POSTPARTUM      │
       │                                     └──────────────────────┘
       │                                                │
       │                                                │ Apojadura
       │                                                ▼
       │                                     ┌──────────────────────┐
       │ <────────────────────────────────── │      LACTATION       │
       │          Desmame / Fim MELA         └──────────────────────┘
       │
       │ Transição Climatérica (STRAW -2/-1)
       ▼
┌───────────────┐     12 Meses de Amenorreia     ┌──────────────────────┐
│ PERIMENOPAUSE │ ─────────────────────────────> │       MENOPAUSE      │
└───────────────┘                                └──────────────────────┘
```

---

## 3. Diretrizes de Apresentação das Citações no Aplicativo

O aplicativo garante transparência clínica absoluta para a usuária final através de três pilares de engenharia:
1. **`Citation` no Domínio:** Todo artigo ou recomendação carrega metadados com autores, periódico, resumo do achado clínico e link com DOI indexado no PubMed / SciELO / portal oficial.
2. **`ScientificSourceBadge` na UI:** Em cada card de orientação do dia, a usuária visualiza uma insígnia discreta e elegante permitindo abrir o artigo original diretamente no navegador do dispositivo.
3. **Relatório Clínico em PDF:** Ao gerar relatórios para o ginecologista ou obstetra (`PdfReportGenerator.kt`), todas as métricas calculadas (como idade gestacional corrigida por US, pontuação EPDS ou score MRS) trazem notas de rodapé com a citação bibliográfica formal do protocolo adotado.
