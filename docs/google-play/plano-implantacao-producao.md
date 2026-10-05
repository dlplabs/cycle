# Plano de Implantação em Produção — Cycle Brasil

Guia operacional e parâmetros oficiais para publicação do aplicativo Cycle no Google Play Console.

---

## 1. Identificação Técnica do Aplicativo

- **Nome do Pacote — Application ID**: `br.com.dlpsystems.cycle`
- **Nome do Aplicativo**: Cycle
- **Versão Atual**: `1.0`
- **Código da Versão — Version Code**: `1`
- **SDK Mínimo**: Android 8.0 — API 26
- **SDK Alvo**: Android 14 — API 35
- **SDK Compilação**: Android 15 — API 36
- **Biblioteca Play Faturamento**: Google Play Billing Library com suporte a assinaturas recorrentes
- **Publicidade e Monetização**: Google AdMob na versão gratuita e assinaturas Cycle Premium
- **Estrutura de Distribuição**: Android App Bundle — AAB otimizado de 64 bits
- **Identificador de Anúncios AdMob em Produção**: Substituir o ID de teste de `strings.xml` pelo App ID oficial da conta DLP Systems antes do fechamento de release

---

## 2. Metadados Oficiais para a Ficha na Google Play Store

Todos os campos foram revisados em conformidade com as diretrizes do Google Play e com a política de zero parênteses da DLP Systems.

### Nome do Aplicativo — Até 30 caracteres
```text
Cycle
```

### Descrição Breve — Até 80 caracteres
```text
Ciclo menstrual, bem-estar, alívio de cólicas e backup gratuito na nuvem.
```

### Descrição Completa — Até 4000 caracteres
```text
Viva em profunda harmonia com o seu corpo. O Cycle é o seu companheiro diário para compreender cada fase do ciclo menstrual com acolhimento, ciência e leveza.

Baseado em literatura médica internacional, o Cycle traduz as oscilações hormonais do mês em orientações práticas de autocuidado, energia, alimentação e bem-estar emocional.

Tudo o que você encontra no Cycle:

CycleWheel Orgânica e Elegante
Visualize graficamente o seu momento hormonal em uma roda contínua e intuitiva: descubra se você está na fase Menstrual, Folicular, Ovulatória ou Lútea e o que esperar de cada uma delas.

Cuidado por 4 Pilares Baseados em Ciência
Receba recomendações personalizadas para Nutrição, Exercício, Pele e Mente em sintonia com a fase em que você está. Acesse os artigos científicos indexados que fundamentam cada sugestão com total transparência.

SOS Alívio de Cólica Imediato
Para os momentos de desconforto: temporizador térmico seguro para compressa morna entre 20 e 30 minutos e Respiração Guiada 4-7-8 com vibrações táteis relaxantes no smartphone para acalmar o corpo e aliviar a tensão.

Seus Dados Sempre Protegidos com Backup Gratuito em Nuvem
Trocou de aparelho, perdeu o celular ou passou por um imprevisto? Suas anotações e histórico do ciclo nunca se perdem. Basta entrar com sua conta Google em outro celular e ter seus registros restaurados desde o primeiro dia de uso de forma gratuita.

Check-in Diário de Sintomas e Humor
Registre fluxo, dores, disposição, sono e reflexões em poucos toques. Acompanhe padrões do seu corpo ao longo dos meses.

Previsor de Datas Futuras e Widget na Tela Inicial
Planeje viagens e compromissos sabendo com antecedência sua fase prevista. Tenha o resumo do seu ciclo na tela inicial do celular com o widget oficial.

Cycle Gratuito e Cycle Premium Opcional
A versão gratuita oferece todas as ferramentas essenciais. O Cycle Premium remove anúncios e libera a exportação do Relatório Médico consolidado para você apresentar à sua médica ginecologista nas consultas de rotina.

Privacidade e Sigilo Total
Sua saúde íntima é sagrada. Seus dados pertencem exclusivamente a você e nunca são vendidos ou repassados a terceiros para marketing. Exclua sua conta e registros quando desejar.

Aviso de Saúde
O Cycle é uma ferramenta de autoconhecimento e apoio ao bem-estar. Não substitui diagnósticos médicos, consultas clínicas, tratamentos ou métodos contraceptivos.

Desenvolvido por DLP Systems — https://www.dlpsystems.com.br
```

### Texto Promocional — Até 140 caracteres
```text
Fases do ciclo menstrual, alívio SOS de cólicas, ciência do bem-estar e backup gratuito em nuvem. Da DLP Systems.
```

### O Que Há de Novo — Notas da Versão 1.0 — Até 500 caracteres
```text
• Lançamento oficial do Cycle no Google Play.
• Roda do ciclo menstrual intuitiva com cálculo das quatro fases.
• Alívio SOS de cólicas com calor e respiração tátil 4-7-8.
• Check-in diário de sintomas, fluxo, humor, pele e notas pessoais.
• Previsor de fases futuras e widget exclusivo para tela inicial.
• Backup seguro e gratuito em nuvem com sincronização por conta Google.
• Plano Premium com relatório consolidado para consulta médica.
```

### Categoria e Tags ASO
- **Categoria Principal**: Saúde e Fitness
- **Tags de Descoberta**: Ciclo menstrual, Saúde feminina, Bem-estar, Menstruação, Ovulação, TPM, Cólica, Autocuidado

---

## 3. Dados de Contato e URLs Oficiais

- **Nome da Empresa**: DLP Systems Ltda
- **E-mail de Suporte**: suporte@dlpsystems.com.br
- **Site Institucional**: https://www.dlpsystems.com.br
- **URL Oficial da Política de Privacidade**: https://www.dlpsystems.com.br/privacidade
- **URL Oficial para Solicitação de Exclusão de Conta**: https://www.dlpsystems.com.br/privacidade
- **E-mail do Encarregado de Proteção de Dados**: privacidade@dlpsystems.com.br

---

## 4. Desbloqueio e Configuração da Política de Privacidade

Para assegurar aprovação do rastreador do Google Play:
1. No menu lateral esquerdo da Google Play Console, acesse **Política e programas do app → Conteúdo do app**.
2. Localize a seção **Política de privacidade** e clique em **Gerenciar** ou **Editar**.
3. No campo **URL da política de privacidade**, insira a rota oficial testada com código HTTP 200:
   ```text
   https://www.dlpsystems.com.br/privacidade
   ```
4. Salve a alteração.
5. Em **Visão geral da publicação**, o alerta desaparecerá e o botão de submissão estará habilitado.

---

## 5. Questionários Oficiais da Google Play Console

### 5.1 Acesso ao App — Credenciais para a Equipe de Revisão do Google
- **Status**: Todas ou algumas funcionalidades são restritas — requer login.
- **Nome do Conjunto**: Revisão Google Play Cycle
- **Usuário ou E-mail**: `revisor@dlpsystems.com.br`
- **Senha**: `CycleReview2026!`
- **Instruções ao Revisor**:
  > Entrar utilizando o e-mail e senha informados acima. Ao abrir o app, aceite o aviso de saúde inicial. O app exibirá a tela inicial com a CycleWheel. A aba Alívio contém o temporizador térmico e respiração guiada. A aba Planejar contém o previsor de fases futuras. A aba Conta dá acesso às configurações, relatório médico, saída e botão de exclusão definitiva de conta.

### 5.2 Declaração de Apps de Saúde
- **Classificação**: Aplicativo de apoio ao bem-estar e rastreamento de estilo de vida.
- **Categorias no Console**: Rastreamento de ciclo menstrual e controle de bem-estar.
- **Dispositivo Médico**: Não — o aplicativo não é um software médico regulado e não realiza diagnósticos ou recomendações terapêuticas.
- **Aconselhamento Clínico**: Não — atua estritamente como diário pessoal e organizador de hábitos.
- **Conformidade de Saúde do Google Play**: Declarar conformidade total com a política de apps de saúde do Google.

### 5.3 Classificação de Conteúdo — IARC
- **E-mail de Contato**: `suporte@dlpsystems.com.br`
- **Categoria do Aplicativo**: Saúde e Fitness
- **Violência, Sangue ou Medo**: Não
- **Sexualidade ou Nudez**: Não — conteúdo focado em biologia, ciclo hormonal e saúde reprodutiva
- **Linguagem Ofensiva**: Não
- **Substâncias Controladas, Álcool ou Drogas**: Não
- **Jogos de Azar ou Apostas**: Não
- **Compras Digitais**: Sim — assinaturas Cycle Premium
- **Compartilhamento de Localização Física**: Não
- **Interação Social entre Usuários**: Não
- **Acesso Irrestrito à Web**: Não
- **Exibição de Anúncios**: Sim — Google AdMob na versão gratuita
- **Resultado Esperado**: Classificação Livre ou recomendada a partir de 10 a 12 anos

### 5.4 Público-alvo e Conteúdo
- **Faixas Etárias Marcadas**: 13 a 17 anos e 18 anos ou mais.
- **Menores de 13 Anos**: Não marcar — evita enquadramento automático na rigorosa Política para Famílias que impõe restrições adicionais de SDKs.
- **Apelo para Crianças**: Marcar Não — o design visual, ficha e temática do ciclo menstrual não são voltados intencionalmente para crianças.

### 5.5 Declaração de Aplicativos de Notícias
- **Pergunta**: O aplicativo é um app de notícias?
- **Resposta**: Não — é um aplicativo de saúde e controle de hábitos.

### 5.6 Declaração de Rastreamento da COVID-19
- **Pergunta**: O app é voltado para rastreamento de contatos ou status de vacinação da COVID-19?
- **Resposta**: Não.

### 5.7 Declaração de Recursos e Serviços Financeiros
- **Pergunta**: O aplicativo oferece serviços bancários, empréstimos, custódia de criptoativos ou negociação financeira?
- **Resposta**: Não — as compras limitam-se a assinaturas digitais gerenciadas pelo Google Play Billing.

### 5.8 Declaração de Aplicativos Governamentais
- **Pergunta**: O aplicativo representa ou foi criado para um governo ou órgão público oficial?
- **Resposta**: Não — produto privado desenvolvido pela DLP Systems Ltda.

### 5.9 ID de Publicidade do Android — AAID
- **Pergunta**: O aplicativo usa o identificador de publicidade do Android?
- **Resposta**: Sim.
- **Finalidade Declarada**: Publicidade e marketing através do Google AdMob e Análise com Firebase Analytics.

### 5.10 Serviços em Primeiro Plano — FGS
- **Pergunta**: O aplicativo utiliza permissões de serviço em primeiro plano?
- **Resposta**: Não — nenhuma permissão de primeiro plano é declarada no manifesto.

### 5.11 Segurança dos Dados — Data Safety Form
- **O aplicativo coleta ou compartilha dados de usuários**: Sim
- **Criptografia em Trânsito**: Sim — todas as conexões usam protocolo seguro HTTPS TLS 1.3
- **Exclusão de Conta e Dados**: Sim — o aplicativo oferece botão nativo de exclusão imediata na tela de configurações e solicitação web
- **URL da Web para Solicitação de Exclusão de Conta**: `https://www.dlpsystems.com.br/privacidade`
- **Mapeamento Detalhado dos Tipos de Dados**:

| Categoria do Dado | Tipo Específico | Coleta | Compartilha | Obrigatório ou Opcional | Finalidade |
|---|---|---|---|---|---|
| Informações Pessoais | Nome | Sim | Não | Obrigatório no cadastro | Funcionalidade do app e Gerenciamento de conta |
| Informações Pessoais | Endereço de e-mail | Sim | Não | Obrigatório no cadastro | Funcionalidade do app e Gerenciamento de conta |
| Informações Pessoais | IDs de Usuário | Sim | Não | Obrigatório | Funcionalidade do app e Gerenciamento de conta |
| Saúde e Bem-estar | Informações de Saúde | Sim | Não | Opcional | Funcionalidade do app — registros do ciclo e sintomas |
| Fotos e Vídeos | Fotos | Sim | Não | Opcional | Funcionalidade do app — avatar do perfil e foto de pele |
| Atividade no App | Interações no aplicativo | Sim | Não | Opcional | Análise e melhoria contínua via Firebase Analytics |
| Desempenho do App | Diagnósticos e registros técnicos | Sim | Não | Opcional | Análise técnica e prevenção de falhas |
| Dispositivo e Outros | IDs de Dispositivo ou Publicidade | Sim | Sim com rede de anúncios | Opcional | Publicidade e marketing via Google AdMob |

- **Garantia de Sigilo**: A DLP Systems declara que dados de saúde nunca são vendidos nem compartilhados com terceiros para fins comerciais.

### 5.12 Declaração de Anúncios
- Marcar que o aplicativo **contém anúncios** veiculados na versão gratuita através do Google AdMob.

---

## 6. Produtos e Assinaturas no Google Play Billing

Cadastrar no menu **Monetizar com o Play → Produtos → Assinaturas**:

| ID do Produto SKU | Nome no Console | Tipo de Público | Preço BRL | Benefícios |
|---|---|---|---|---|
| `cycle_premium_monthly` | Cycle Premium Mensal | Assinatura Mensal | R$ 14,90 ao mês | Sem anúncios, previsor estendido e exportação de relatório médico |
| `cycle_premium_yearly` | Cycle Premium Anual | Assinatura Anual | R$ 119,90 ao ano | Todos os benefícios do plano mensal com economia anual |

**Pré-requisito**: A conta de desenvolvedor precisa ter um Perfil para Pagamentos configurado e vinculado no Google Payments Merchant Center para liberar a criação de assinaturas pagas.

---

## 7. Inventário Completo de Recursos Gráficos

Todos os ativos visuais já estão gerados no repositório nas dimensões exatas:

- **Ícone de Alta Resolução**: `docs/google-play/graficos/icone-512.png` com 512x512 pixels — formato PNG 32-bit totalmente opaco sem canal alfa transparente
- **Gráfico de Recursos — Banner de Destaque**: `docs/google-play/graficos/grafico-destaque-1024x500.png` com 1024x500 pixels — formato PNG
- **Capturas de Tela para Smartphone — 1080x1920 pixels**:
  - `01-home-1080x1920.png` — Roda do ciclo, fase atual e check-in
  - `02-evidencias-1080x1920.png` — Pilares de nutrição, treino, pele e mente
  - `03-sos-1080x1920.png` — SOS térmico e respiração guiada 4-7-8
  - `04-planner-1080x1920.png` — Previsor de datas e fases futuras
  - `05-premium-1080x1920.png` — Vantagens do plano Cycle Premium
  - `06-configuracoes-1080x1920.png` — Relatório médico e exclusão de conta
- **Capturas de Tela para Tablet de 7 Polegadas — 1920x1080 pixels**:
  - `docs/google-play/graficos/tablet-7-home-1920x1080.png` — Visão expandida da tela inicial em tablets de 7 polegadas
- **Capturas de Tela para Tablet de 10 Polegadas — 1920x1080 pixels**:
  - `docs/google-play/graficos/tablet-10-planner-1920x1080.png` — Visão ampla do planejador de fases em tablets de 10 polegadas

---

## 8. Diretrizes de Publicação por Tipo de Conta Google Play

As exigências para o primeiro lançamento variam conforme a natureza da conta:

- **Conta de Organização — Pessoa Jurídica DLP Systems Ltda**:
  - Verificada com número D-U-N-S oficial da empresa.
  - Permite submissão direta para a trilha de **Produção** ou trilha de **Teste Aberto** sem obrigatoriedade de teste fechado prévio de 14 dias.
- **Conta Pessoal — Desenvolvedor Individual**:
  - Contas pessoais criadas a partir de 13 de novembro de 2023 exigem obrigatoriamente a execução de **Teste Fechado**.
  - O teste fechado deve ter pelo menos 20 testadores inscritos ativamente por no mínimo 14 dias contínuos antes de o console liberar o botão de solicitação de acesso à Produção.

---

## 9. Compilação e Envio do Pacote AAB

Para gerar o binário de produção assinado:
```bash
./gradlew bundleRelease
```
O pacote será gerado em:
```text
app/build/outputs/bundle/release/app-release.aab
```

Antes de subir o pacote:
1. Certifique-se de que o arquivo `keystore.properties` na raiz do projeto está preenchido com a chave de produção oficial da DLP Systems.
2. Certifique-se de que o `admob_app_id` em `app/src/main/res/values/strings.xml` contém o código definitivo de produção da conta AdMob.

---

## 10. Checklist Mestre de Execução Passo a Passo

- [x] Package ID validado como `br.com.dlpsystems.cycle`
- [x] Versão 1.0 com version code 1 configurada no Gradle
- [x] Target SDK 35 em conformidade com as regras do Google Play
- [x] URL da Política de Privacidade preenchida como `https://www.dlpsystems.com.br/privacidade`
- [x] URL web para solicitação de exclusão de dados cadastrada
- [x] Acesso ao app com credenciais de teste do revisor configuradas
- [x] Declaração de anúncios marcada como Sim
- [x] Questionário de classificação de conteúdo IARC preenchido
- [x] Público-alvo selecionado como 13 a 17 anos e 18 anos ou mais sem apelo infantil
- [x] Declaração de apps de notícias marcada como Não
- [x] Declaração de rastreamento COVID-19 marcada como Não
- [x] Declaração de recursos financeiros marcada como Não
- [x] Declaração de apps governamentais marcada como Não
- [x] Declaração de ID de Publicidade do Android marcada como Sim
- [x] Declaração de serviços em primeiro plano marcada como Não
- [x] Declaração de apps de saúde preenchida no escopo de bem-estar e ciclo
- [x] Segurança dos dados preenchida cobrindo informações pessoais, saúde, fotos, telemetria e anúncios
- [x] SKUs de assinatura `cycle_premium_monthly` e `cycle_premium_yearly` cadastrados
- [x] Ícone 512x512 opaco e banner de recursos 1024x500 enviados
- [x] Capturas de tela para smartphone, tablet de 7 polegadas e tablet de 10 polegadas enviadas
- [x] Upload do binário app-release.aab no Google Play Console
- [ ] Envio das alterações para revisão da equipe do Google
