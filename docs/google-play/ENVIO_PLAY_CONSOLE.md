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
• Roda do ciclo com cálculo das 4 fases hormonais
• Modo gestação e cronômetro de contrações
• Mandala lunar e identificação de padrões pessoais
• Mapeamento de sintomas por região do corpo
• Lembrete de anticoncepcional e pasta de exames
• Central SOS para alívio térmico de cólicas
• Backup seguro e gratuito em nuvem
• Compatível com Android 16 (API 36) e Play Billing 8
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
