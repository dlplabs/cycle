# Índice de Architecture Decision Records (ADR) — Plataforma Cycle

Este repositório documenta as decisões arquiteturais estruturantes do aplicativo **Cycle** (`br.com.dlpsystems.cycle`), direcionadas à expansão da plataforma para abranger a saúde integral da mulher em todas as suas fases hormonais e reprodutivas.

---

## 1. Matriz Geral de Decisões Arquiteturais

| ADR | Título | Status | Data | Área Principal |
| :--- | :--- | :--- | :--- | :--- |
| [ADR-0001](0001-motor-de-fases-de-vida-e-dashboard-polimorfico.md) | Motor de Fases de Vida e Dashboard Polimórfico (`LifeStageMode`) | Aprovado | 2026-10-27 | Arquitetura / UI Core |
| [ADR-0002](0002-modulo-tentante-e-saude-pre-concepcional.md) | Módulo Tentante e Saúde Pré-Concepcional (TTC & Fertility) | Aprovado | 2026-10-27 | Domínio / Fertilidade |
| [ADR-0003](0003-modulo-gestacao-integral-e-marcos-trimestrais.md) | Módulo Gestação Integral e Marcos Trimestrais | Aprovado | 2026-10-27 | Domínio / Obstetrícia |
| [ADR-0004](0004-modulo-pos-parto-puerperio-e-saude-mental.md) | Módulo Pós-Parto, Puerpério e Saúde Mental Perinatal | Aprovado | 2026-10-27 | Domínio / Puerpério |
| [ADR-0005](0005-modulo-amamentacao-lactacao-e-mela.md) | Módulo Amamentação, Lactação, Banco de Leite e MELA | Aprovado | 2026-10-27 | Domínio / Lactação |
| [ADR-0006](0006-modulo-perimenopausa-e-climaterio.md) | Módulo Perimenopausa e Climatério (Critérios STRAW+10) | Aprovado | 2026-10-27 | Domínio / Climatério |
| [ADR-0007](0007-modulo-menopausa-e-longevidade-ativa.md) | Módulo Menopausa, Pós-Menopausa e Longevidade Ativa | Aprovado | 2026-10-27 | Domínio / Maturidade |
| [ADR-0008](0008-privacidade-lgpd-e-seguranca-dados-sensiveis.md) | Governança de Dados Sensíveis, Criptografia e LGPD | Aprovado | 2026-10-27 | Segurança / Compliance |

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

1. **STRAW+10 (Stages of Reproductive Aging Workshop + 10):** Padrão-ouro global para estadiamento do envelhecimento reprodutivo feminino (estágios -2 a +2).
2. **ACOG (American College of Obstetricians and Gynecologists):** Diretrizes para monitoramento pré-natal, regras de contagem fetal de chutes e manejo do puerpério.
3. **FEBRASGO (Federação Brasileira das Associações de Ginecologia e Obstetrícia):** Parâmetros de pré-natal no Brasil, rastreamento de diabetes gestacional (TOTG) e rastreamento de estreptococo do grupo B.
4. **ASRM (American Society for Reproductive Medicine):** Parâmetros de janela fértil e tempo de espera para investigação de infertilidade conjugal.
5. **OMS (Organização Mundial da Saúde) & Ministério da Saúde do Brasil:** Critérios de eficácia do Método da Amenorreia Lactacional (MELA/LAM) e conservação de leite materno.
6. **EPDS (Edinburgh Postnatal Depression Scale):** Escala padronizada e validada para triagem de depressão perinatal e puerperal.
7. **NAMS / The Menopause Society:** Diretrizes para avaliação de sintomas vasomotores, síndrome geniturinária da menopausa (GSM) e proteção óssea/cardiovascular.
