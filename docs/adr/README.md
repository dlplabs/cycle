# Índice de Architecture Decision Records (ADR) — Plataforma Cycle

Este repositório documenta as decisões arquiteturais estruturantes do aplicativo **Cycle** (`br.com.dlpsystems.cycle`), direcionadas à expansão da plataforma para abranger a saúde integral da mulher em todas as suas fases hormonais e reprodutivas.

Todas as decisões clínicas e algoritmos implementados são rigorosamente alicerçados em **pesquisas científicas de grande porte publicadas em periódicos médicos de alto impacto, com coortes populacionais superiores a 1.000 participantes (N > 1.000)**, diretrizes de consenso internacional e protocolos oficiais de saúde pública.

---

## 1. Matriz Geral de Decisões Arquiteturais

| ADR | Título | Status | Data | Área Principal | Coortes Científicas Chave (N > 1.000) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| [ADR-0001](0001-motor-de-fases-de-vida-e-dashboard-polimorfico.md) | Motor de Fases de Vida e Dashboard Polimórfico (`LifeStageMode`) | Aprovado | 2026-10-27 | Arquitetura / UI Core | STRAW+10 Consortium (N > 10.000), SWAN Study (N = 3.302) |
| [ADR-0002](0002-modulo-tentante-e-saude-pre-concepcional.md) | Módulo Tentante e Saúde Pré-Concepcional (TTC & Fertility) | Aprovado | 2026-10-27 | Domínio / Fertilidade | Dunson et al. (N = 1.077 / 3.160 ciclos), MRC Vitamin Study (N = 1.817), Czeizel et al. (N = 4.753) |
| [ADR-0003](0003-modulo-gestacao-integral-e-marcos-trimestrais.md) | Módulo Gestação Integral e Marcos Trimestrais | Aprovado | 2026-10-27 | Domínio / Obstetrícia | Cardiff Count-to-10 (N = 68.654), Consortium on Safe Labor (N = 62.415), Hadlock Biometry (N = 1.200+) |
| [ADR-0004](0004-modulo-pos-parto-puerperio-e-saude-mental.md) | Módulo Pós-Parto, Puerpério e Saúde Mental Perinatal | Aprovado | 2026-10-27 | Domínio / Puerpério | EPDS Pelotas Birth Cohort (N = 4.453), Cox EPDS Validation (N > 100.000 global), Oppenheimer Lochia (N = 1.000+) |
| [ADR-0005](0005-modulo-amamentacao-lactacao-e-mela.md) | Módulo Amamentação, Lactação, Banco de Leite e MELA | Aprovado | 2026-10-27 | Domínio / Lactação | Bellagio LAM Multicenter Study (N = 4.118), Rede Brasileira de Bancos de Leite Humano rBLH (N = 160.000+/ano) |
| [ADR-0006](0006-modulo-perimenopausa-e-climaterio.md) | Módulo Perimenopausa e Climatério (Critérios STRAW+10) | Aprovado | 2026-10-27 | Domínio / Climatério | SWAN Study (N = 3.302), Menopause Rating Scale Validation (N = 11.140), Penn Ovarian Aging Study (N = 1.100+) |
| [ADR-0007](0007-modulo-menopausa-e-longevidade-ativa.md) | Módulo Menopausa, Pós-Menopausa e Longevidade Ativa | Aprovado | 2026-10-27 | Domínio / Maturidade | Million Women Study (N = 1.388.536), Women's Health Initiative WHI (N = 161.808) |
| [ADR-0008](0008-privacidade-lgpd-e-seguranca-dados-sensiveis.md) | Governança de Dados Sensíveis, Criptografia e LGPD | Aprovado | 2026-10-27 | Segurança / Compliance | Marco Legal LGPD (Lei 13.709/18 - Art. 5º, II e Art. 11), HIPAA Security Rule |

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

## 3. Diretrizes Clínicas e Referências Científicas Adotadas

1. **STRAW+10 (Stages of Reproductive Aging Workshop + 10):** Consórcio internacional integrando coortes como o SWAN (N = 3.302), Melbourne Women's Midlife Health Project e Penn Ovarian Aging Study (total N > 10.000). Padrão-ouro global para estadiamento reprodutivo e transição menopáusica (estágios -2 a +2).
2. **ACOG (American College of Obstetricians and Gynecologists):** Diretrizes obstétricas para avaliação de idade gestacional, protocolos de vitalidade fetal por contagem de chutes e conduta de pré-natal e puerpério.
3. **FEBRASGO (Federação Brasileira das Associações de Ginecologia e Obstetrícia):** Parâmetros de pré-natal no Brasil, triagem de diabetes gestacional (TOTG 75g) e profilaxia de sepse neonatal por Estreptococo do Grupo B.
4. **ASRM (American Society for Reproductive Medicine):** Parâmetros biológicos de janela fértil e definição de tempo de espera para investigação de infertilidade conjugal fundamentados em coortes populacionais de fecundabilidade.
5. **OMS (Organização Mundial da Saúde) & Ministério da Saúde do Brasil:** Critérios de eficácia do Método da Amenorreia Lactacional (MELA/LAM) com base no Estudo Multicêntrico de Bellagio (N = 4.118) e normatização bacteriológica da Rede Brasileira de Bancos de Leite Humano (rBLH / Fiocruz).
6. **EPDS (Edinburgh Postnatal Depression Scale):** Escala padronizada de rastreamento de depressão perinatal e puerperal validada no Brasil na Coorte de Nascimentos de Pelotas com N = 4.453 puérperas (Santos et al., 2007) e em coortes globais somando N > 100.000 mulheres.
7. **NAMS / The Menopause Society & Endocrine Society:** Diretrizes clínicas para manejo de sintomas vasomotores, síndrome geniturinária da menopausa (GSM) e proteção óssea/cardiovascular fundamentadas nos ensaios do WHI (N = 161.808) e Million Women Study (N = 1.388.536).
