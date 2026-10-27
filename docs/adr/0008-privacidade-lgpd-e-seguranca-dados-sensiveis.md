# ADR-0008: Governança de Dados Sensíveis, Criptografia e Privacidade LGPD

## Status
Aprovado

## Data
2026-10-27

## Contexto
O aplicativo Cycle armazena informações de alta intimidade sobre a usuária: datas de menstruação, atividade sexual, muco cervical, frequência de relações íntimas, resultados de testes de ovulação, idade gestacional, contrações de parto, notas de saúde mental pós-parto (incluindo triagem de depressão pela escala EPDS), episódios de fogachos e registros de climatério.

Nos termos da **Lei Geral de Proteção de Dados (LGPD — Lei nº 13.709/2018)**, esses dados enquadram-se categoricamente como **Dados Pessoais Sensíveis** (Art. 5º, II — "dado pessoal sobre a saúde ou à vida sexual"). No cenário internacional, alinham-se aos padrões de proteção de informações protegidas de saúde (**HIPAA Security & Privacy Rule — 45 CFR Part 160 e 164**).

O tratamento desses dados exige garantias invioláveis de confidencialidade, integridade e autodeterminação informativa da titular.

## Base Legal e Diretrizes de Governança

1. **Classificação Legal Estrita (LGPD Art. 5º, II e Art. 11):**
   - O tratamento de dados de saúde íntima exige base legal expressa (tutela da saúde exclusivamente em procedimento realizado por profissionais de saúde, ou consentimento específico e destacado da titular).
   - O Cycle adota o princípio de **Privacy by Design and by Default** (Privacidade desde a Concepção e por Padrão), garantindo que nenhum dado saia do aparelho sem autenticação voluntária da usuária.
2. **Resoluções da Autoridade Nacional de Proteção de Dados (ANPD):**
   - Implementação de medidas de segurança da informação compatíveis com agentes de pequeno e grande porte, contemplando criptografia ponta a ponta e gestão de acessos mínimos necessários.
3. **Padrões Internacionais de Health Privacy (HIPAA / GDPR Health Data):**
   - Desacoplamento arquitetural entre a identidade civil da usuária (email/nome) e a base de registros biométricos e diários hormonais.

## Decisão Arquitetural

1. **Princípio Local-First com Criptografia em Repouso**:
   - Dados íntimos e de sessões clínicas são gravados primariamente no dispositivo do usuário antes de qualquer sincronização com a nuvem.
   - Utilização do `EncryptedSharedPreferences` (da biblioteca `androidx.security:security-crypto`) com chaves gerenciadas pelo Android Keystore em hardware seguro (TEE / StrongBox).
   - Para bancos de dados relacionais locais (Room), adoção mandatória de `SQLCipher` com chave derivada de hardware seguro.

2. **Segurança no Firebase Firestore (Nuvem)**:
   - Sincronização em nuvem condicionada à autenticação formal (`Firebase Auth`).
   - As regras de segurança (`firestore.rules`) garantem isolamento matemático estrito de inquilino (multi-tenant):
     ```javascript
     rules_version = '2';
     service cloud.firestore {
       match /databases/{database}/documents {
         match /users/{userId} {
           allow read, write: if request.auth != null && request.auth.uid == userId;
           
           match /{document=**} {
             allow read, write: if request.auth != null && request.auth.uid == userId;
           }
         }
       }
     }
     ```
   - **Zero Acesso Compartilhado Não Autorizado:** É vedada qualquer permissão de leitura global ou cruzada entre usuárias. A única exceção é o módulo de Compartilhamento com Parceiro (`ADR de Partner Sharing`), que exige token criptográfico assinado e consentimento bilateral com escopo restrito de leitura revogável a qualquer momento.

3. **Modo Discreto em Notificações Push**:
   - Para proteger a usuária contra olhares indiscretos de terceiros na tela de bloqueio do smartphone (em ambientes de trabalho, transporte ou convívio familiar):
     - As notificações do aplicativo utilizam títulos e mensagens neutros e afetuosos por padrão.
     - *Exemplo Inadequado:* "Registrar corrimento vaginal ou fogacho da menopausa".
     - *Exemplo Cycle (Discreto):* "Momento Cycle: que tal fazer seu check-in de autocuidado de hoje?".
     - As categorias de notificações são segregadas em Notification Channels do Android, permitindo que a usuária desative canais específicos nas configurações do sistema operacional.

4. **Bloqueio por Biometria / PIN no Aplicativo (App Lock)**:
   - Integração com a API nativa `androidx.biometric.BiometricPrompt`.
   - Opção para exigir autenticação biométrica (impressão digital ou reconhecimento facial) ou PIN seguro sempre que o aplicativo for aberto ou retornar do segundo plano após 1 minuto de inatividade.

5. **Geração Local de Relatórios Médicos em PDF**:
   - A compilação do relatório ginecológico/obstétrico (`PdfReportGenerator.kt`) é executada **100% no hardware do dispositivo da usuária** utilizando as APIs nativas do Android `android.graphics.pdf.PdfDocument`.
   - O documento gerado nunca transita por servidores remotos de renderização de PDF ou serviços de terceiros.
   - O compartilhamento do arquivo PDF ocorre via `Intent.ACTION_SEND` e `FileProvider` com permissões temporárias concedidas apenas ao aplicativo de destino escolhido pela usuária (ex.: WhatsApp com o médico ou e-mail pessoal).

6. **Direito de Eliminação e Portabilidade (Art. 18 da LGPD)**:
   - Disponibilização na tela de Configurações do botão **"Excluir Minha Conta e Todos os Dados"**:
     - Ao ser acionado, remove em cascata todas as coleções do Firestore vinculadas ao UID da usuária (`users/{userId}/*`).
     - Apaga o cache e banco de dados local no dispositivo.
     - Exclui o cadastro da usuária no Firebase Authentication.
   - Disponibilização de exportação integral de dados em formato JSON aberto para garantir a portabilidade.

## Consequências

### Positivas
- Total conformidade jurídica com a LGPD e regulamentações internacionais de privacidade em saúde reprodutiva.
- Confiança plena da usuária, essencial para adoção contínua de um aplicativo de saúde íntima.
- Arquitetura blindada contra vazamentos em massa de dados em nuvem.

### Negativas / Mitigações
- Perda irreversível de dados em caso de exclusão de conta. *Mitigação:* Diálogo de confirmação dupla com aviso claro sobre a permanência da ação.

## Testes e Critérios de Aceite
- Teste de auditoria de regras de segurança do Firestore (Firestore Rules Unit Tests) garantindo que um usuário A não consiga ler documentos do usuário B.
- Teste de integridade da exclusão completa de conta no `UserRepositoryTest`.
- Teste de geração de PDF validando que o arquivo gerado pode ser lido sem requisições de rede.
