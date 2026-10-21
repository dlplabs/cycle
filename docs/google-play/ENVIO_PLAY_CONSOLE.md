# Manual de Envio — Google Play Console — Cycle

**Aplicativo**: Cycle — Acompanhamento Menstrual e Bem-Estar  
**ID do Pacote**: `br.com.dlpsystems.cycle`  
**Idioma padrão**: Português do Brasil — `pt-BR`  
**Target SDK**: 36 — Android 16  
**Min SDK**: 26 — Android 8.0  
**Status de Conformidade**: Totalmente adequado às diretrizes do Google Play para apps de saúde, segurança de dados e Play Billing 8.0.0.

---

## 1. Informações Básicas do Aplicativo

- **Nome do App**: Cycle
- **Idioma Padrão**: Português do Brasil
- **Tipo de Aplicativo**: Aplicativo
- **Modelo de Preço**: Gratuito com compras internas e anúncios
- **Categoria**: Saúde e fitness
- **Tags**: Ciclo menstrual, Saúde feminina, Bem-estar, Diário de hábitos

---

## 2. Textos Oficiais da Loja

### Nome do Aplicativo — Até 30 caracteres
```text
Cycle
```

### Descrição Breve — Até 80 caracteres
```text
Ciclo menstrual, bem-estar, gravidez e backup gratuito na nuvem.
```

### Texto Promocional — Até 140 caracteres
```text
Fases do ciclo menstrual, alívio SOS de cólicas, ciência do bem-estar e backup gratuito em nuvem. Da DLP Systems.
```

### Notas da Versão — O Que Há de Novo (Máximo 500 caracteres)
```text
<pt-BR>
• Liberdade para alterar sua foto de perfil escolhendo da galeria ou tirando foto, sem depender de conta Google.
• Nova ilustração botânica do útero integrada à coroa no topo da tela inicial.
• Exibição de ponta a ponta (Edge-to-Edge) aprimorada para o Android 15 e versões mais recentes.
• Carregamento de imagens mais leve e rápido com consumo reduzido de memória.
• Otimizações de desempenho e estabilidade geral.
</pt-BR>
```

---

## 3. Contato e Política de Privacidade

- **E-mail de Suporte**: suporte@dlpsystems.com.br
- **Site Oficial**: https://www.appcycle.com.br
- **URL da Política de Privacidade**: `https://www.appcycle.com.br/privacidade`
- **URL de Exclusão de Conta e Dados**: `https://www.appcycle.com.br/privacidade`

Atenção: Cadastre exatamente a URL acima para aprovação imediata pelo rastreador do Google Play.

---

## 4. Questionários e Declarações na Play Console

### Acesso ao App
- Selecione a opção informando que funcionalidades requerem credenciais de acesso.
- **Usuário ou E-mail**: `revisor@dlpsystems.com.br`
- **Senha**: `CycleReview2026!`
- **Instruções ao Revisor**:
  > Entrar utilizando o e-mail e senha informados acima. Ao abrir o app, aceite o aviso de saúde inicial. O app exibirá a tela inicial com a CycleWheel. A aba Alívio contém o temporizador térmico e respiração guiada. A aba Planejar contém o previsor de fases futuras. A aba Conta dá acesso às configurações, relatório médico, saída e botão de exclusão definitiva de conta.

### Declaração de Apps de Saúde
- Marcar que o aplicativo é voltado ao bem-estar e controle pessoal de hábitos de ciclo menstrual.
- Confirmar que não se trata de dispositivo médico clínico.
- Confirmar conformidade com a política de apps de saúde do Google Play.

### Questionário de Classificação de Conteúdo — IARC
- Categoria: Saúde e Fitness
- Responda Não para todas as perguntas de violência, teor sexual explícito, drogas e apostas.
- Confirme que o app possui anúncios e compras internas.
- Resultado esperado: Classificação Livre ou 10 a 12 anos.

### Público-alvo e Conteúdo
- Faixas etárias: 13 a 17 anos e 18 anos ou mais.
- Menores de 13 anos: Não marcar.
- Apelo para crianças: Não.

### Apps de Notícias
- O app é de notícias: Não.

### COVID-19
- Rastreamento de contatos ou status de COVID-19: Não.

### Recursos Financeiros
- Serviços de banking, empréstimos ou criptoativos: Não.

### Apps Governamentais
- Representa governo: Não.

### ID de Publicidade do Android
- O app usa o ID de publicidade: Sim — para anúncios Google AdMob e telemetria Firebase Analytics.

### Serviços em Primeiro Plano
- Utiliza permissões de primeiro plano: Não.

### Segurança dos Dados — Data Safety Form
- O aplicativo coleta dados: Sim
- Criptografia em trânsito: Sim — protocolo TLS HTTPS 1.3
- Exclusão de conta: Sim — disponível dentro do aplicativo e via canal web
- Link web para exclusão: `https://www.appcycle.com.br/privacidade`
- Dados declarados:
  - Informações pessoais: Nome, e-mail e IDs de usuário — funcionalidade e gestão de conta
  - Saúde e bem-estar: Dados do ciclo, sintomas e humor — funcionalidade do app
  - Fotos e vídeos: Fotos de perfil ou pele opcionais — funcionalidade do app
  - Atividade e telemetria: Métricas agregadas anônimas — análise
  - Dispositivo: ID de publicidade compartilhado com Google AdMob para anúncios gratuitos
- Dados de saúde nunca são comercializados com terceiros.

---

## 5. Produtos de Assinatura no Play Billing

Cadastrar no menu Monetizar com o Play → Produtos → Assinaturas:

- `cycle_premium_monthly` — Cycle Premium Mensal por R$ 14,90 ao mês
- `cycle_premium_yearly` — Cycle Premium Anual por R$ 119,90 ao ano

Requer perfil de pagamentos Google Payments ativo na conta.

---

## 6. Upload de Recursos Gráficos

- **Ícone do App**: `docs/google-play/graficos/icone-512.png` — 512x512 pixels
- **Gráfico de Recursos**: `docs/google-play/graficos/grafico-destaque-1024x500.png` — 1024x500 pixels
- **Capturas para Smartphone**: Seis arquivos 1080x1920 numerados de `01-home` a `06-configuracoes`
- **Capturas para Tablet 7 Pol**: `docs/google-play/graficos/tablet-7-home-1920x1080.png`
- **Capturas para Tablet 10 Pol**: `docs/google-play/graficos/tablet-10-planner-1920x1080.png`

---

## 7. Geração do Pacote AAB e Envio

```bash
./gradlew bundleRelease
```
Arquivo gerado: `app/build/outputs/bundle/release/app-release.aab`

Observações finais:
1. Certificar que a conta de desenvolvedor de organização DLP Systems permite publicação direta em produção, ou cumprir a etapa de teste fechado se for conta pessoal.
2. Atualizar o `admob_app_id` em `strings.xml` para o ID definitivo antes do build final.
3. Fazer o upload do AAB e submeter as alterações para revisão da equipe do Google Play.

---

## 8. Certificados de Assinatura, Firebase e Google Sign-In

Para garantir que o Google Sign-In funcione corretamente em todos os ambientes (desenvolvimento local, testes e produção distribuída pela Play Store), as seguintes impressões digitais devem estar sempre registradas no Firebase (`cycle-5d812`) no app `br.com.dlpsystems.cycle`:

### Impressões Digitais (Fingerprints)

1. **Google Play App Signing (Chave de Produção/Distribuição da Google Play)**:
   - **SHA-1**: `E4:E0:23:0C:3C:24:51:2D:6C:BC:5B:21:8C:8F:A1:2C:BB:0D:66:9C`
   - **SHA-256**: `32:15:15:74:F7:0A:A0:6F:C7:F2:70:80:BB:F1:65:12:A9:12:A1:53:DE:17:31:90:0D:3C:CC:C9:88:AB:F7:63`
   - **OAuth Client ID Android**: `604557808835-imkt93k0ldg44b2l0df85fpuri4ifih4.apps.googleusercontent.com`

2. **Chave de Upload / Release Keystore Local (`keystore/release.keystore`)**:
   - **SHA-1**: `5F:E1:7E:7C:05:15:0B:2B:3B:53:C7:66:4B:6F:84:4D:3D:30:F9:FD`
   - **SHA-256**: `31:00:B3:C4:3E:70:2B:1B:4C:A8:EA:04:C7:58:74:BE:EF:E8:43:3E:97:2A:8C:9A:EB:A8:C4:F9:BA:80:13:DA`
   - **OAuth Client ID Android**: `604557808835-1vu2r7n3s8rr5v49gcphdg9h5f1t492s.apps.googleusercontent.com`

3. **Chave de Depuração / Debug Keystore Local (`~/.android/debug.keystore`)**:
   - **SHA-1**: `83:A1:F4:7B:12:EC:F0:86:CD:0E:28:87:52:F5:74:7C:23:76:2B:B5`
   - **SHA-256**: `7F:2A:FA:72:38:52:DE:0A:1F:EF:F0:EF:5D:2F:01:0B:0C:2F:65:AE:4B:D9:F6:33:1D:F3:83:14:3A:9A:FA:EF`
   - **OAuth Client ID Android**: `604557808835-5gcm2ioa3ftm9pffg6et3ug8khr7dbe3.apps.googleusercontent.com`

4. **Web Client ID (Servidor OAuth 2.0)**:
   - `604557808835-02cdd7s9righc14lhrbuq0juqtbd2047.apps.googleusercontent.com`

