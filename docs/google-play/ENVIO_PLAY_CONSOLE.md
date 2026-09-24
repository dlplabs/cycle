# Plano e Manual de Implantação — Google Play Console

**Aplicativo:** Cycle — Acompanhamento Menstrual & Bem-Estar  
**ID do Pacote (Package ID):** `br.com.dlpsystems.cycle`  
**Idioma padrão:** Português (Brasil) — `pt-BR`  
**Target SDK:** 35 (Android 15) | **Min SDK:** 26 (Android 8.0)  
**Status de Conformidade:** 100% aderente às diretrizes de 2026 da Google Play Store (Segurança, Exclusão de Conta, Billing v7 e Data Safety).

---

## 1. Informações Básicas do App

| Campo na Play Console | Valor / Instrução |
| :--- | :--- |
| **Nome do App** | Cycle |
| **Idioma Padrão** | Português (Brasil) |
| **Tipo de Aplicativo** | App |
| **Gratuito ou Pago** | Gratuito (com compras no app e anúncios) |
| **Categoria** | Saúde e fitness |
| **Tags Sugeridas** | Ciclo menstrual, Saúde feminina, Bem-estar, Diário de hábitos |

---

## 2. Ficha da Google Play Store (Copywriting Oficial)

### Título (5/30 caracteres)
```
Cycle
```

### Descrição Breve (66/80 caracteres)
```
Acompanhe ciclo, fases e bem-estar. Não substitui consulta médica.
```

### Texto Promocional (Opcional, 58/80 caracteres)
```
Fases, check-in, SOS e planner. Premium tira os anúncios.
```

### Descrição Completa (Formatada para a Play Store)
```
O Cycle é o seu companheiro diário para viver em harmonia com cada fase do ciclo menstrual. Baseado em literatura científica internacional, ele traduz variações hormonais em orientações práticas de autocuidado, acolhimento e nutrição.

O QUE VOCÊ ENCONTRA NO CYCLE:
• CycleWheel Orgânica: Visualize graficamente o dia atual e a sua fase em um círculo contínuo e elegante.
• Cuidado por Pilares: Recomendações personalizadas para Nutrição, Exercício, Pele e Mente adaptadas à fase em que você está.
• Fontes Científicas Transparentes: Artigos e estudos médicos indexados que você pode consultar diretamente a um toque.
• Check-in Rápido & Diário Completo: Registre fluxo, sintomas (cólicas, dor de cabeça, inchaço), humor e notas pessoais.
• SOS Alívio de Cólica: Timer térmico para bolsa de calor morna (20 a 30 min) e Respiração Guiada 4-7-8 com vibrações táteis para relaxamento imediato.
• Previsor de Datas Futuras: Planeje viagens, eventos e compromissos sabendo com antecedência sua fase hormonal prevista.
• Widget de Tela Inicial (Glance): Veja o dia e a fase direto na Home do aparelho com atalho rápido "Menstruação desceu hoje".

GRATUITO & CYCLE PREMIUM:
• A versão gratuita oferece todas as ferramentas essenciais com anúncios discretos apenas no Planner e em Configurações (a tela inicial e o SOS são 100% livres de anúncios).
• O Cycle Premium (mensal ou anual) remove completamente os anúncios, expande o horizonte do previsor e libera a exportação do Relatório Médico em PDF dos últimos ciclos para você levar ao seu ginecologista.

PRIVACIDADE & CONTROLE TOTAL:
Seus registros de saúde pertencem exclusivamente a você. Seus dados nunca são vendidos nem compartilhados com terceiros para marketing. Você pode excluir sua conta e todos os dados associados a qualquer momento dentro do app ou pela web.

AVISO DE SAÚDE:
O Cycle é uma ferramenta de autoconhecimento e bem-estar. Ele não substitui consultas médicas, diagnósticos clínicos, tratamentos ou métodos contraceptivos.
```

### Notas da Versão 1.0 (What's New)
```
Primeira versão oficial do Cycle: roda do ciclo interativa, check-in diário de sintomas e dor, alívio SOS com calor e respiração tátil 4-7-8, previsor de datas futuras, widget para tela inicial, artigos científicos integrados e opção Premium sem anúncios com relatório médico em PDF.
```

---

## 3. Ativos Visuais e Gráficos de Loja

Todos os gráficos estão gerados no padrão **Wellness Premium** (paleta botânica, fontes Playfair Display e Inter). Para regerar todos os arquivos em alta resolução:
```bash
python3 docs/google-play/gerar_graficos.py
```

| Slot na Play Console | Arquivo Local | Especificação Exigida |
| :--- | :--- | :--- |
| **Ícone de Alta Resolução** | `docs/google-play/graficos/icone-512.png` | 512×512 PNG 32-bit (sem transparência no fundo) |
| **Gráfico de Recursos** | `docs/google-play/graficos/grafico-destaque-1024x500.png` | 1024×500 PNG/JPEG |
| **Captura Telefone 1** | `docs/google-play/graficos/01-home-1080x1920.png` | 1080×1920 (9:16) — Roda, Dia do Ciclo e Check-in |
| **Captura Telefone 2** | `docs/google-play/graficos/02-evidencias-1080x1920.png` | 1080×1920 (9:16) — Pilares e Evidências Científicas |
| **Captura Telefone 3** | `docs/google-play/graficos/03-sos-1080x1920.png` | 1080×1920 (9:16) — Alívio SOS e Respiração 4-7-8 |
| **Captura Telefone 4** | `docs/google-play/graficos/04-planner-1080x1920.png` | 1080×1920 (9:16) — Previsor de Datas Futuras |
| **Captura Telefone 5** | `docs/google-play/graficos/05-premium-1080x1920.png` | 1080×1920 (9:16) — Planos Cycle Premium |
| **Captura Telefone 6** | `docs/google-play/graficos/06-configuracoes-1080x1920.png` | 1080×1920 (9:16) — Configurações, PDF e Exclusão |
| **Tablet de 7 polegadas** | `docs/google-play/graficos/tablet-7-home-1920x1080.png` | 1920×1080 (16:9) |
| **Tablet de 10 polegadas** | `docs/google-play/graficos/tablet-10-planner-1920x1080.png` | 1920×1080 (16:9) |

---

## 4. Contato da Ficha & Política de Privacidade

| Campo | Valor Recomendado |
| :--- | :--- |
| **E-mail de Contato** | `suporte@dlpsystems.com.br` |
| **Site Oficial** | `https://dlpsystems.com.br` (ou repositório oficial do projeto) |
| **URL da Política de Privacidade** | URL pública HTTPS onde `PRIVACY_POLICY.md` está hospedado (ex: GitHub Pages ou site próprio). |

---

## 5. Acesso ao App (Credenciais para os Revisores do Google)

O Google Play exige que apps com autenticação forneçam uma conta de testes ativa para a equipe de revisão:

1. Selecione: **Todas ou algumas funcionalidades são restritas**.
2. Clique em **Adicionar instruções**:
   - **Nome:** Conta de Revisão Google Play
   - **Nome de usuário/E-mail:** `revisor@dlpsystems.com.br` *(crie este usuário no Firebase Auth)*
   - **Senha:** `CycleReview2026!`
   - **Instruções:**
     > "Entrar utilizando E-mail e Senha. Ao abrir o app, aceite o aviso de saúde inicial. O app exibirá a Home com a CycleWheel. A aba Alívio contém o SOS térmico e respiração. A aba Planejar contém o previsor. A aba Conta (Configurações) dá acesso aos ajustes, PDF de consulta médica, saída e botão de exclusão de conta."

---

## 6. Questionário de Classificação de Conteúdo (IARC)

| Categoria do Questionário | Resposta |
| :--- | :--- |
| **Violência, Sangue ou Medo** | Não |
| **Sexualidade ou Nudez** | Não (app aborda saúde biológica e ciclo menstrual sem teor sexual) |
| **Linguagem Ofensiva** | Não |
| **Substâncias Controladas / Drogas / Álcool** | Não |
| **Jogos de Azar / Apostas** | Não |
| **Compras Digitais** | Sim (Assinatura Premium no app) |
| **Compartilhamento de Localização Física** | Não |
| **Interação Social entre Usuários** | Não |
| **Acesso irrestrito à Web** | Não (apenas abertura de artigos científicos via navegador padrão) |

*Classificação esperada:* **Livre** ou **Classificação Indicativa 10/12 anos**.

---

## 7. Segurança dos Dados (Data Safety Form)

Marque que o aplicativo **coleta dados**, mas **NÃO compartilha dados de saúde** e **NÃO vende dados**.

| Dado Coletado | Finalidade | Compartilhado? | Efêmero? | Obrigatório? |
| :--- | :--- | :--- | :--- | :--- |
| **Nome** | Funcionalidade do app / Conta | Não | Não | Sim |
| **E-mail** | Autenticação / Conta | Não | Não | Sim |
| **IDs de Usuário (UID)** | Identificação da conta no Firebase | Não | Não | Sim |
| **Dados de Saúde** *(ciclo, fluxo, sintomas, dor, humor, pele, notas)* | Funcionalidade do app (cálculos e histórico) | **Não** | Não | Sim |
| **Atividade no App** *(telas visitadas)* | Analytics (Firebase Analytics) | Não | Não | Opcional |
| **IDs de Dispositivo** *(Advertising ID)* | Publicidade (Google AdMob na versão free) | Sim (com Google) | Não | Opcional |
| **Informações Financeiras** | Processadas diretamente pelo Google Play | Não coletado pelo app | — | — |

**Práticas de Segurança Declaradas:**
- Dados criptografados em trânsito (HTTPS / TLS 1.3): **Sim**
- O app permite que os usuários solicitem a exclusão de seus dados: **Sim** (disponível diretamente no app e via solicitação web)
- Compromisso com os Padrões de Segurança da Família: **Não aplicável** (o app é direcionado para 18+ anos).

---

## 8. Produtos de Assinatura (Google Play Billing v7)

Cadastre os dois produtos de assinatura no menu **Monetizar > Produtos de assinatura**:

| ID do Produto (Product ID) | Tipo | Nome no Console | Benefício |
| :--- | :--- | :--- | :--- |
| `cycle_premium_monthly` | Assinatura (Base Mensal) | Cycle Premium Mensal | Sem anúncios, planner estendido, relatório médico em PDF |
| `cycle_premium_yearly` | Assinatura (Base Anual) | Cycle Premium Anual | Sem anúncios, planner estendido, relatório médico em PDF (com desconto anual) |

*Importante:* O app utiliza a Google Play Billing Library v7.1.1 com confirmação ativa de compras (`acknowledgePurchase`). Certifique-se de ativar os produtos e publicá-los junto com o envio do app para evitar rejeições na validação de compra.

---

## 9. Conformidade de Exclusão de Conta (RESOLVIDO & CONFORME)

A Google Play Store exige obrigatoriamente que aplicativos que oferecem criação de contas permitam ao usuário:
1. **Excluir a conta de dentro do próprio aplicativo:**
   - **Implementado:** Na tela de Configurações (`SettingsScreen`), existe o botão destacado **Excluir conta**, que exibe um diálogo de confirmação claro. Ao confirmar, o app executa a limpeza completa dos registros no Cloud Firestore (`users/{uid}`, `cycles`, `daily_logs`) e remove a conta de autenticação no Firebase Auth.
2. **Link web para solicitação de exclusão:**
   - No formulário de Segurança dos Dados, preencha o campo de URL com o link público da política de privacidade ou página de exclusão (ex: `https://seusite.com/exclusao-de-dados` ou seção correspondente no `PRIVACY_POLICY.md`).

---

## 10. Checklist de Geração do Android App Bundle (AAB) & Envio

1. **Configuração de Produção:**
   - Certifique-se de colocar o arquivo `google-services.json` de produção em `app/google-services.json`.
   - Substitua os IDs de teste do AdMob em `app/src/main/res/values/strings.xml` pelos IDs reais criados na sua conta do Google AdMob.
2. **Geração do Keystore de Upload (fora do git):**
   ```bash
   keytool -genkey -v -keystore cycle-upload-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias cycle-key
   ```
3. **Compilação do Pacote AAB de Release:**
   ```bash
   ./gradlew bundleRelease
   ```
   O arquivo final será gerado em: `app/build/outputs/bundle/release/app-release.aab`.
4. **Execução da Trilha de Testes:**
   - Envie primeiro o pacote `.aab` na trilha de **Teste Interno (Internal Testing)** ou **Teste Fechado (Closed Testing)**.
   - Teste o login, a exclusão de conta, a exibição de banners AdMob e o fluxo de assinatura com contas licenciadas de teste.
5. **Promoção para Produção:**
   - Após validação dos testes, envie para revisão da Google Play e promova para a faixa de Produção.
