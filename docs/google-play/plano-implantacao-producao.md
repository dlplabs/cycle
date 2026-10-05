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
- **Biblioteca Play Faturamento**: Google Play Billing Library com suporte a assinaturas
- **Publicidade e Monetização**: Google AdMob na versão gratuita e assinaturas Cycle Premium
- **Estrutura de Distribuição**: Android App Bundle — AAB otimizado de 64 bits

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
- **URL Oficial da Política de Privacidade — Principal**: https://www.dlpsystems.com.br/privacidade
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

### Acesso ao App — Credenciais para a Equipe de Revisão do Google
- **Status**: Todas ou algumas funcionalidades são restritas — requer login.
- **Nome do Conjunto**: Revisão Google Play Cycle
- **Usuário ou E-mail**: `revisor@dlpsystems.com.br`
- **Senha**: `CycleReview2026!`
- **Instruções ao Revisor**:
  > Entrar utilizando o e-mail e senha informados acima. Ao abrir o app, aceite o aviso de saúde inicial. O app exibirá a tela inicial com a CycleWheel. A aba Alívio contém o temporizador térmico e respiração guiada. A aba Planejar contém o previsor de fases futuras. A aba Conta dá acesso às configurações, relatório médico, saída e botão de exclusão definitiva de conta.

### Declaração de Apps de Saúde
- **Classificação**: Aplicativo de apoio ao bem-estar e rastreamento de estilo de vida.
- **Dispositivo Médico**: Não — o aplicativo não é um software médico regulado e não realiza diagnósticos ou recomendações terapêuticas.
- **Aconselhamento Clínico**: Não — atua estritamente como diário pessoal e organizador de hábitos.

### Classificação de Conteúdo — IARC
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

### Segurança dos Dados — Data Safety Form
- **O aplicativo coleta ou compartilha dados de usuários**: Sim
- **Criptografia em Trânsito**: Sim — todas as conexões usam protocolo seguro HTTPS TLS 1.3
- **Exclusão de Conta e Dados**: Sim — o aplicativo oferece botão nativo de exclusão imediata na tela de configurações e solicitação web
- **Tipos de Dados Declarados**:
  - Informações Pessoais: Nome, e-mail e identificador de usuário para autenticação e gestão de perfil
  - Dados de Saúde e Bem-estar: Registros de ciclo menstrual, sintomas, dores, humor e notas diárias — coletados unicamente para a funcionalidade do app
  - Identificadores de Dispositivo: Utilizados pelo Google AdMob para exibição de anúncios na versão gratuita
  - Diagnóstico e Desempenho: Registros técnicos anônimos de falhas e estabilidade
- **Garantia de Sigilo**: A DLP Systems declara que dados de saúde nunca são vendidos nem compartilhados com terceiros para fins comerciais

### Declaração de Anúncios
- Marcar que o aplicativo **contém anúncios** veiculados na versão gratuita através do Google AdMob.

---

## 6. Produtos e Assinaturas no Google Play Billing

Cadastrar no menu **Monetizar → Produtos → Assinaturas**:

| ID do Produto SKU | Nome no Console | Tipo de Público | Preço BRL | Benefícios |
|---|---|---|---|---|
| `cycle_premium_monthly` | Cycle Premium Mensal | Assinatura Mensal | R$ 14,90 ao mês | Sem anúncios, previsor estendido e exportação de relatório médico |
| `cycle_premium_yearly` | Cycle Premium Anual | Assinatura Anual | R$ 119,90 ao ano | Todos os benefícios do plano mensal com economia anual |

---

## 7. Inventário de Recursos Gráficos

- **Ícone de Alta Resolução**: `docs/google-play/graficos/icone-512.png` com 512x512 pixels
- **Gráfico de Recursos**: `docs/google-play/graficos/grafico-destaque-1024x500.png` com 1024x500 pixels
- **Capturas de Tela para Smartphone — 1080x1920 pixels**:
  - `01-home-1080x1920.png` — Roda do ciclo, fase atual e check-in
  - `02-evidencias-1080x1920.png` — Pilares de nutrição, treino, pele e mente
  - `03-sos-1080x1920.png` — SOS térmico e respiração guiada 4-7-8
  - `04-planner-1080x1920.png` — Previsor de datas e fases futuras
  - `05-premium-1080x1920.png` — Vantagens do plano Cycle Premium
  - `06-configuracoes-1080x1920.png` — Relatório médico e exclusão de conta

---

## 8. Compilação e Envio do Pacote AAB

Para gerar o binário de produção assinado:
```bash
./gradlew bundleRelease
```
O pacote será gerado em:
```text
app/build/outputs/bundle/release/app-release.aab
```

---

## 9. Checklist Executivo de Publicação

- [x] Package ID configurado como `br.com.dlpsystems.cycle`
- [x] Versão 1.0 com código de versão 1
- [x] Compilação do pacote Android App Bundle com target SDK 35
- [x] Declaração de apps de saúde preenchida com escopo de bem-estar
- [x] Questionário de classificação de conteúdo IARC respondido
- [x] Formulário de segurança dos dados preenchido com proteção total a dados de saúde
- [x] Mecanismo de exclusão de conta funcional no app e informado no console
- [x] Credenciais de teste para revisores configuradas
- [x] URL da Política de Privacidade preenchida como `https://www.dlpsystems.com.br/privacidade`
- [ ] Upload do binário app-release.aab no Google Play Console
- [ ] Envio das alterações para revisão da equipe do Google
