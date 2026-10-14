Você atuará como um time multidisciplinar de Engenharia de Software e Design composto por:
1. Arquiteto de Software Especialista em Segurança e Criptografia (Infosec & Privacy-by-Design);
2. Engenheiro Mobile Android Sênior (Kotlin, Jetpack Compose, Coroutines, Room/SQLCipher, Play Services);
3. Engenheiro Frontend Web Sênior (Next.js 16, React 19, TypeScript, Tailwind CSS);
4. Product Designer Sênior (UX/UI voltado a Segurança Crítica e Acessibilidade).

OBJETIVO DO PROJETO:
Projetar e implementar de ponta a ponta a feature de "Proteção e Segurança da Mulher" para o ecossistema Cycle (App Android nativo e Portal Web cycle.com.br), contemplando:
- Botão de Emergência Silencioso e Acionamento Rápido (SOS);
- Avaliação de Risco Doméstico com Questionário IAVP (Instrumento de Avaliação de Risco em Violência Doméstica);
- Camuflagem Visual e Modos de Discrição (Disguise Mode / Duress PIN);
- Coleta Segura de Provas e Histórico Autodestrutível.

---

### PILAR 1: DIRETRIZES DE UX/UI E DESIGN DE SEGURANÇA CRÍTICA

1. CAMUFLAGEM E DISCREÇÃO DE INTERFACE
   - No mobile, a funcionalidade NUNCA deve ser rotulada como "Violência Doméstica" ou "Botão de Pânico" em menus visíveis. Utilize rótulos contextuais e neutros como "Rotina de Segurança Pessoal", "Contatos de Confiança" ou "Check-in Seguro".
   - Implemente um gatilho de camuflagem rápida ("Panic Disguise"): um toque gestual (ex.: toque triplo no título ou swipe específico) substitui instantaneamente a interface ativa por uma tela inofensiva (ex.: tela de registro de hidratação ou anotação comum do ciclo menstrual).
   - Na versão Web, mantenha o "Botão de Saída Rápida" fixo e persistente: um clique ou o pressionamento de `Esc` redireciona imediatamente para um site neutro (ex.: `google.com.br`), sobrescrevendo a entrada atual no histórico do navegador (`window.location.replace`).

2. DESIGN DO FORMULÁRIO IAVP
   - Fluxo de perguntas em etapas (step-by-step) sem rolagem longa para reduzir a sobrecarga cognitiva e evitar exposição de respostas caso a tela seja vista de relance.
   - Linguagem humanizada, clara e sem termos jurídicos densos.
   - Tela final com diagnóstico de risco (Moderado / Grave / Extremo) apresentada sem alarmismo estético agressivo, priorizando passos acionáveis, orientações de apoio e discagem direta para a Central de Atendimento (180) e Polícia (190).
   - Botão obrigatório "Concluir e Não Salvar Histórico" na tela de resultados.

---

### PILAR 2: ARQUITETURA MOBILE (ANDROID / KOTLIN / JETPACK COMPOSE)

1. GESTÃO DE ESTADO E SEGURANÇA LOCAL
   - Criptografia em repouso: todas as respostas do IAVP, contatos de confiança e registros de ocorrências devem ser salvos em banco local criptografado utilizando `Room` integrado com `SQLCipher`, com chaves gerenciadas pelo `Android Keystore` (AES-256-GCM).
   - Suporte a Duress PIN (PIN de Coação): se o app possuir bloqueio biométrico/PIN, crie suporte para dois PINs. O PIN comum abre os dados normais. O PIN de coação abre um banco de dados falso e neutro, mascarando contatos de socorro.

2. FLUXO DO BOTÃO DE EMERGÊNCIA
   - Localização: captura de coordenadas via `FusedLocationProviderClient` com `PRIORITY_HIGH_ACCURACY` e timeout defensivo (fallback para última localização conhecida caso o GPS demore).
   - Envio Silencioso de SMS: envio por `SmsManager` para os números cadastrados na lista de confiança, estruturando a mensagem:
     `SOS Cycle: Preciso de ajuda imediata. Minha localização: https://maps.google.com/?q={lat},{lng}`
   - Ativação por Hardware/Físico (opcional): implementar listener de cliques sucessivos nas teclas físicas de volume em background para disparo sem necessidade de desbloquear a tela.
   - Gravação de Áudio Forense: gravação de áudio de contingência em background por meio de `MediaRecorder`, armazenado no diretório privado da aplicação via `EncryptedFile` (sem indexação no `MediaStore` do sistema).

---

### PILAR 3: ARQUITETURA WEB (NEXT.JS / REACT / TAILWIND CSS)

1. SANITIZAÇÃO DE DADOS E HISTÓRICO
   - Nenhuma resposta do IAVP deve ser persistida em `localStorage` ou `cookies` por padrão sem consentimento explícito. O estado do formulário deve ser estritamente gerenciado em memória (`React Context` ou `Zustand`).
   - Implementar o componente `QuickExit`:
     * Limpa o estado da sessão em memória.
     * Invoca `window.location.replace('https://www.google.com.br')` para invalidar o botão "Voltar" do navegador.

2. MOTOR DE CÁLCULO E RECOMENDAÇÕES DO IAVP
   - Módulo desacoplado em TypeScript (`evaluateIavpRisk.ts`) que processe os pesos das respostas:
     * Indicadores de Perigo Extremo (tentativa de sufocamento/estrangulamento, armas de fogo, ameaça de morte/suicídio): elevação automática para nível EXTREMO independente da soma ponderada.
     * Indicadores Graves e Moderados gerando plano de proteção customizado e links diretos para delegacias virtuais e DEAMs.

---

### ENTREGÁVEIS TÉCNICOS ESPERADOS:

1. **Arquitetura & Modelagem:** Diagrama de fluxo e entidades (Contatos de Confiança, Pergunta IAVP, Resposta IAVP, Avaliação de Risco).
2. **Implementação Kotlin (Android):**
   - `EmergencySosManager.kt`: orquestração de obtenção de GPS, despacho de SMS silencioso e ativação do serviço de áudio.
   - `IavpViewModel.kt` e tela declarativa em `Jetpack Compose` com suporte a saída rápida e camuflagem de UI.
3. **Implementação Web (Next.js/React):**
   - Componente `QuickExitButton.tsx` com tratamento de evento de teclado (`Esc`) e manipulação segura de histórico.
   - Componente do questionário IAVP com validação, cálculo de score e exibição das diretrizes de segurança.