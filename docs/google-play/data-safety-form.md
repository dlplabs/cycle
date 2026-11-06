# Segurança dos Dados (Data Safety Form) — Google Play Console — Cycle

Guia oficial para preenchimento da seção **Política e programas do app → Segurança dos dados** na Google Play Console para o aplicativo **Cycle** (`br.com.dlpsystems.cycle`).

---

## 1. Resumo Geral de Coleta e Segurança

| Pergunta no Console | Resposta |
|---|---|
| **O app coleta ou compartilha algum dos tipos de dados de usuários necessários?** | **Sim** |
| **Todos os dados de usuários coletados pelo app são criptografados em trânsito?** | **Sim** (Todas as conexões utilizam HTTPS/TLS 1.3) |
| **O app oferece aos usuários uma forma de solicitar a exclusão dos dados?** | **Sim** (Exclusão no perfil do app ou via `privacidade@dlpsystems.com.br`) |

---

## 2. Dados Coletados e Finalidades

| Tipo de Dado | Coletado? | Compartilhado? | Efêmero? | Obrigatório? | Finalidades Declaradas |
|---|---|---|---|---|---|
| **Nome** | Sim | Não | Não | Não | Gerenciamento de conta e personalização do perfil |
| **E-mail** | Sim | Não | Não | Não | Autenticação, login, recuperação de conta e suporte |
| **Identificadores Pessoais (User ID)** | Sim | Não | Não | Não | Funcionalidade do app, autenticação de sessão e vínculo de assinaturas ativas |
| **Saúde e Fitness (Dados do Ciclo e Sintomas)** | Sim | Não | Não | Não | Funcionalidade do app (Roda do Ciclo, histórico de fases e relatórios de bem-estar) |
| **Localização Aproximada** | Sim | Sim [Google AdMob] | Não | Não [Opcional ao usuário] | Publicidade ou marketing [Google AdMob] |
| **Fotos / Galeria** | Sim | Não | Sim | Não | Funcionalidade do app (foto de perfil e anotações visuais opcionais) |
| **Histórico de Compras** | Sim | Sim (Google Play) | Não | Não | Gerenciamento de compras e assinaturas (Google Play Billing) |
| **IDs de Dispositivo / Publicidade** | Sim | Sim (Google AdMob) | Não | Sim (na versão gratuita) | Publicidade no app e análise de desempenho (AdMob) |

---

## 3. Práticas de Segurança e Exclusão de Conta

- **Criptografia em Trânsito:** Todos os dados transferidos entre o aplicativo e os serviços em nuvem utilizam HTTPS com TLS 1.3.
- **Proteção Rigorosa de Dados de Saúde:** Os registros íntimos e de saúde da usuária nunca são comercializados nem compartilhados com terceiros para fins de marketing.
- **Política de Exclusão de Contas (Play Store Compliance):**
  - Usuárias podem excluir seus dados diretamente na tela de Configurações do aplicativo ou enviando uma solicitação para `privacidade@dlpsystems.com.br`.
  - Página oficial de solicitação de exclusão web disponível em: `https://www.appcycle.com.br/privacidade`.
- **Compromisso de Não Venda:** A DLP Systems não comercializa nem transfere dados pessoais de usuários para terceiros para fins de revenda ou marketing.

---

## 4. Política de Privacidade Obrigatória

URL pública HTTPS informada na Play Console:
```text
https://www.appcycle.com.br/privacidade
```
