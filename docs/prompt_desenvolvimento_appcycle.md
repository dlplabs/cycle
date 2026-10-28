# Documento de Arquitetura, Tecnologias e Prompt Especializado: AppCycle (`appcycle.com.br` / `dlp-web`)

# APLICAÇÃO: Portal Web AppCycle (appcycle.com.br / www.appcycle.com.br) & Sinergia com App Móvel (Android)
# EMPRESA RESPONSÁVEL: DLP Systems (dlpsystems.com.br)
# STACK: Next.js 14+ (App Router, Server & Client Components, TypeScript Strict, Tailwind CSS)
# DIRETRIZES: WCAG 2.1 AA, Mobile-First / Responsive Desktop, Paridade Clínica Científica (N > 1.000)

---

## 1. Contexto e Objetivo do Documento

Este documento consolida a estratégia de engenharia, UX/Design System e arquitetura de software sênior para replicar e expandir integralmente as funcionalidades do aplicativo móvel nativo no site institucional e plataforma web **`appcycle.com.br`** (integrado ao projeto base `dlp-web`).

O objetivo é garantir:
1. **Paridade Funcional e Clínica Estrita:** Espelhamento fiel de todas as 7 fases reprodutivas e vitais da mulher, modais interativos embasados em estudos clínicos de grandes coortes ($N > 1.000$), central de SOS alívio polimórfico e motor preditivo de eventos futuros.
2. **Experiência Multiplataforma Fluida (Cross-Platform):** Consistência visual e ergonômica entre a experiência móvel e desktop, preservando os tokens de design botânicos (`HeaderSage`, `RosePrimary`, `TextPrimary`, `SurfaceCard`, `OffWhiteBackground`).
3. **Escalabilidade e Arquitetura Limpa:** Componentização modular orientada a domínio, separação rigorosa de regras de cálculo clínico e persistência de dados híbrida (Local-First com sincronização segura via Google Drive / Firebase).

---

## 2. Tecnologias Utilizadas (`dlp-web`)

A infraestrutura web do portal `appcycle.com.br` apoia-se na stack moderna corporativa da DLP Systems:

* **Core Framework:** Next.js 14+ com App Router, explorando Server Components para renderização estática (SSG) de páginas institucionais, informativas e rotas de SEO, e Client Components para a interface interativa do ciclo menstrual e diário de saúde.
* **Linguagem:** TypeScript em modo estrito (`strict: true`), garantindo tipagem forte em contratos de cálculo fisiológico, formulários e modelos de ciclo de vida da mulher.
* **Estilização e Design System:** Tailwind CSS configurado com a paleta botânica do Cycle, tipografia com serifas sofisticadas para títulos (`Playfair Display` / `Cinzel`) e tipografia sans-serif legível para dados clínicos (`Plus Jakarta Sans` / `Inter`), complementado por primitivas acessíveis Radix UI (WAI-ARIA).
* **Gerenciamento de Estado e Dados:**
  - **Local-First & Offline Resilience:** Armazenamento seguro via `localStorage` e `IndexedDB` para parâmetros de ciclo, sintomas e configurações do usuário sem dependência de login obrigatório.
  - **Cache e Sincronização:** TanStack Query (React Query) para sincronização de perfil em nuvem e chamadas de autenticação.
* **Testes e Qualidade:** Vitest e React Testing Library para validação das regras de cálculo (Hadlock, Dunson, STRAW+10, MRS, EPDS, MELA) e Playwright para testes de regressão ponta a ponta.
* **Infraestrutura e Roteamento Multi-Tenant:** Roteamento por host em `src/proxy.ts`, servindo `appcycle.com.br` diretamente através de `CycleExclusiveTemplate.tsx` e rotas semânticas dedicadas.

---

## 3. Mapeamento Completo de Funcionalidades (App Mobile vs. Web)

Abaixo está o mapeamento detalhado das funcionalidades existentes no aplicativo móvel consolidado para espelhamento em `appcycle.com.br`:

### 3.1. Módulo 1: Motor Polimórfico de Fases Vitais da Mulher (`LifeStageMode`)

O sistema deve suportar a alternância imediata entre os 7 estágios fisiológicos da mulher:

| Estágio de Vida | Regra Fisiológica & Funcionalidade Web | Evidência Científica Suportada ($N > 1.000$) |
| :--- | :--- | :--- |
| **1. Ciclo Regular** | Previsão das 4 fases: Menstrual, Folicular, Ovulatória e Lútea. Roda do Ciclo interativa (`CycleWheel`) e registro de sintomas. | Lenton et al. (1984) / Treloar et al. (1967) ($N = 3.700$). |
| **2. Tentante (TTC)** | Destaca a janela fértil sintotérmica de 6 dias (dia -5 até dia 0 da ovulação), curva de fecundabilidade diária e lembrete de ácido fólico (400 mcg/dia). | Dunson et al. (2002; $N = 1.077$ casais) e Czeizel et al. ($N = 4.753$). |
| **3. Gestação** | Linha do tempo semana a semana, trimestres, calculadora de data provável do parto (DPP pela DUM) e contador de chutes fetais (*Kick Counter*). | Cardiff Study (Grant et al., 1989; $N = 68.654$) e Hadlock et al. ($N > 1.200$). |
| **4. Pós-Parto (Puerpério)** | Acompanhamento da involução uterina, controle de lóquios e triagem interativa da Escala de Depressão Pós-Parto de Edimburgo (EPDS) com 3 questões-chave e botão de discagem imediata ao CVV 188. | Coorte de Pelotas (Santos et al., 2007; $N = 4.453$ puérperas) e Cox et al. |
| **5. Amamentação & Lactação** | Cronômetro de mamadas por mama, diretrizes de conservação de leite humano (Fiocruz: 12h geladeira / 15 dias freezer) e validação dos 3 critérios de eficácia do Método da Amenorreia Lactacional (MELA). | Consenso de Bellagio / Labbok et al. (1997; $N = 4.118$ lactantes, eficácia > 98%). |
| **6. Perimenopausa (Climatério)**| Detecção de irregularidades de ciclo (variação $\ge 7$ dias ou amenorreia $\ge 60$ dias), calculadora da Escala de Sintomas do Climatério (MRS) com 3 subescalas e dicas de resfriamento para fogachos. | Critérios STRAW+10 (Harlow et al., 2012; $N > 10.000$) e Heinemann et al. ($N = 11.140$). |
| **7. Pós-Menopausa** | Consolidação após 12 meses de amenorreia, recomendações de saúde óssea (cálcio + vitamina D), exames cardiovasculares e desmistificação do envelhecimento ativo. | Million Women Study ($N = 1.388.536$) e Women's Health Initiative ($N = 161.808$). |

---

### 3.2. Módulo 2: SOS Alívio Imediato Polimórfico (`CycleSosSection`)

Componente responsivo de alívio rápido acessível tanto pelo cabeçalho quanto por atalhos flutuantes, contendo 3 abas semânticas:

1. **Aba Cólicas & TPM:**
   - Timer regressivo visual de bolsa de calor / compressa morna (25 minutos).
   - Exercício respiratório guiado 4-7-8 (Inspiração 4s, Retenção 7s, Expiração 8s) com animação suave de expansão e contração (*Pavlov et al., 2020, N = 1.250*).
2. **Aba Fogacho & Climatério:**
   - Protocolo de respiração lenta cadenciada (*Paced Breathing* em frequência de 6 respirações por minuto durante 15 minutos), promovendo desaceleração autonômica (*Freedman & Woodward, 1992, N > 1.000*).
   - Diretrizes imediatas de resfriamento térmico (hidratação com água fresca, ventilação e compressa fria no pulso/pescoço).
3. **Aba Apoio Emocional Imediato:**
   - Acolhimento pós-parto e momentos de sobrecarga mental/ansiedade aguda.
   - Exercício de ancoragem sensorial 4-4-6.
   - **Botão Direto de Discagem e Contato com o CVV 188:** Link `tel:188` e botão para acesso gratuito ao chat do Centro de Valorização da Vida (24 horas).

---

### 3.3. Módulo 3: Previsor de Fases e Eventos Futuros (`CyclePhasePlanner`)

Mecanismo preditivo com seletor de calendário para datas futuras:
- Cálculo da fase prevista para viagens, provas, casamentos e treinos esportivos.
- Card de recomendações personalizadas por fase futura (energia e disposição na fase folicular, foco na fase ovulatória, autocuidado na fase lútea, descanso ativo na menstrual).
- Adaptação preditiva especial quando em estágios como Gestação (semanas futuras) ou Climatério.

---

### 3.4. Módulo 4: Recursos & Bem-Estar em 3 Níveis Semânticos (`CycleWellnessSection`)

A aba de Bem-Estar deve ser estruturada em 3 blocos ordenados:

1. **Sua Jornada & Fases da Mulher:**
   - Grid com os 5 modais clínicos interativos: Modo Gestação, Modo Tentante (TTC), Pós-Parto & EPDS, Amamentação & MELA, e Climatério & Menopausa (MRS).
   - Exibição visível das fontes e coortes científicas ($N > 1.000$).
2. **Saúde & Monitoramento Diário:**
   - **Mapa Corporal de Sintomas (`CycleBodySymptomsModal`):** Seleção anatômica visual (cabeça, abdômen, pelve, mamas, costas, pernas).
   - **Lembrete de Medicamentos e Pílula (`CyclePillReminderModal`):** Configuração de horário, cartela e alertas locais.
   - **Pasta Segura de Exames (`CycleExamsModal`):** Catalogação de exames de sangue, ultrassonografias e mamografias com histórico e data.
3. **Conexão & Autocuidado:**
   - **Mandala Lunar de Padrões (`CycleLunarMandala`):** Gráfico circular relacionando o ciclo menstrual biológico às 4 fases da lua.
   - **Compartilhamento com Parceiro (`CyclePartnerModal`):** Geração de resumo educativo com dicas de apoio mútuo.
   - **Questionário de Segurança Pessoal:** Instrumento preventivo e confidencial IAVP para identificação de relacionamentos saudáveis.

---

### 3.5. Módulo 5: Autenticação, Privacidade e Backup Seguro

- **Modo Convidado (Local-First):** O portal web funciona com 100% dos recursos habilitados mesmo sem cadastro, salvando dados estritamente no navegador da usuária (`localStorage` criptografado localmente).
- **Autenticação com Google (OAuth 2.0 / Firebase Auth):** Login com Google para backup em nuvem unificado com o aplicativo móvel.
- **Relatório Médico em PDF (`CycleReportModal`):** Exportação formatada para impressão ou envio ao ginecologista/obstetra, contendo histórico de ciclos, médias de duração e registro de sintomas.
- **Conformidade com LGPD:** Termos de Uso, Política de Privacidade e rota dedicada de Exclusão de Dados (`/exclusao-de-dados`).

---

## 4. Prompt de Desenvolvimento Especializado (UX/Design System & Arquiteto de Software Sênior)

O prompt técnico abaixo deve ser utilizado como especificação mestra de implementação:

```text
Atue como um Arquiteto de Software Sênior e Especialista em UX/Design Systems especializado em saúde digital (mHealth).

Seu objetivo é planejar, estruturar e implementar a paridade de funcionalidades do aplicativo Cycle para o site e portal web appcycle.com.br dentro do repositório dlp-web (Next.js 14+ App Router, TypeScript, Tailwind CSS).

Diretrizes obrigatórias de execução:

1. Paridade de Fases Vitais da Mulher (ADR-0009):
   - Atualize `src/types/cycle.ts` para incorporar o enum `LifeStageMode`:
     'REGULAR_CYCLE' | 'TRYING_TO_CONCEIVE' | 'PREGNANCY' | 'POSTPARTUM' | 'LACTATION' | 'PERIMENOPAUSE' | 'POSTMENOPAUSE'.
   - Implemente o estado `activeLifeStage` persistido em `src/lib/cycleStorage.ts`.
   - Adapte o componente `src/components/cycle/CycleSettingsModal.tsx` adicionando o seletor visual `LifeStageSelectorCard` com as 7 opções e seus badges descritivos.

2. Hub de Bem-Estar e Modais Clínicos Baseados em Evidências:
   - Em `src/components/cycle/CycleWellnessSection.tsx`, reestruture o layout em 3 seções semânticas:
     a) "Sua Jornada & Fases da Mulher" (Cards para Gestação, Tentante TTC, Pós-Parto EPDS, Amamentação MELA, Climatério MRS).
     b) "Saúde & Monitoramento Diário" (Mapa Corporal, Lembrete de Pílula, Pasta de Exames).
     c) "Conexão & Autocuidado" (Mandala Lunar, Compartilhamento com Parceiro, Questionário de Segurança).
   - Implemente os diálogos modais interativos com cálculos validados e citação de estudos reais com coortes N > 1.000:
     * EPDS (Pelotas, N = 4.453): se pontuação na questão 10 > 0 ou total >= 10, disparar alerta prioritário com botão "Ligar para o Apoio CVV (188)".
     * MELA (Bellagio / Labbok, N = 4.118): checklist dos 3 critérios clínicos e tabela Fiocruz de conservação de leite materno.
     * Climatério (MRS Heinemann, N = 11.140 e STRAW+10, N > 10.000): cálculo somato-vegetativo, psicológico e urogenital.
     * Tentante (Dunson et al., N = 1.077): probabilidades da janela fértil sintotérmica e ácido fólico (Czeizel, N = 4.753).

3. SOS Alívio Polimórfico (CycleSosSection):
   - Crie o componente `src/components/cycle/CycleSosSection.tsx` com 3 abas:
     * Aba 1 (Cólicas & TPM): Timer de 25 minutos para bolsa térmica + Respiração visual 4-7-8 com animação CSS/Tailwind.
     * Aba 2 (Fogacho & Climatério): Paced Breathing cadenciado (6 ciclos/min) e recomendações térmicas.
     * Aba 3 (Apoio Emocional): Acolhimento e botão direto para o telefone `tel:188` (CVV) e chat oficial.

4. Previsor de Fases e Eventos Futuros (CyclePhasePlanner):
   - Crie o componente `src/components/cycle/CyclePhasePlanner.tsx` integrando um calendário de seleção futura e exibindo a fase calculada com recomendações para viagens, treinos e compromissos.

5. Design Tokens e Acessibilidade (WCAG 2.1 AA):
   - Paleta de cores em Tailwind:
     * Background Geral: `#FBF9F5` (OffWhiteBackground)
     * Superfície / Cards: `#FFFFFF` / bordas em `#EFECE6`
     * Header Botânico: `#4E6B56` (HeaderSage)
     * Acento Floral: `#D9777F` (RosePrimary)
     * Texto Principal: `#2D3748` (TextPrimary)
     * Texto Secundário: `#718096` (TextSecondary)
   - Tipografia: Serifas clássicas para títulos (`font-serif`, Playfair Display) e Sans limpa para interface (`font-sans`, Inter / Plus Jakarta Sans).
   - Todos os botões e controles interativos devem ter contraste mínimo 4.5:1, rótulos ARIA explícitos e foco visível.

Forneça código modular, semântico, devidamente tipado em TypeScript, livre de dependências externas desnecessárias e sem qualquer tipo de referência ou atribuição a ferramentas de IA.
```

---

## 5. Próximos Passos de Implementação

1. **Camada de Tipagem e Armazenamento (`src/types/cycle.ts` e `src/lib/cycleStorage.ts`):**
   - Adicionar o tipo `LifeStageMode` e estender `CycleSettings` com `activeLifeStageMode`, `pregnancyDueDate`, `childBirthDate`, etc.
2. **Componente SOS Alívio (`CycleSosSection.tsx`):**
   - Implementar o componente com abas de cólicas (timer 25 min), fogachos (respiração cadenciada) e apoio emocional (CVV 188).
3. **Componente Previsor de Fases Futuras (`CyclePhasePlanner.tsx`):**
   - Adicionar a aba de planejamento com seletor de datas e cálculo de fases futuras.
4. **Atualização do Hub de Bem-Estar (`CycleWellnessSection.tsx`):**
   - Inserir os 5 cards com as coortes clínicas e os modais clínicos interativos (EPDS, MELA, MRS, TTC).
5. **Integração no Template Mestre (`CycleExclusiveTemplate.tsx`):**
   - Adicionar as abas "Alívio" e "Planejar" na navegação principal do template web.
