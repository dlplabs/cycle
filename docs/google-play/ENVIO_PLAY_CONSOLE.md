# Manual de Envio — Google Play Console — Cycle

**Aplicativo**: Cycle — Acompanhamento Menstrual e Bem-Estar  
**ID do Pacote**: `br.com.dlpsystems.cycle`  
**Idioma padrão**: Português do Brasil — `pt-BR`  
**Target SDK**: 35 — Android 15  
**Min SDK**: 26 — Android 8.0  
**Status de Conformidade**: Totalmente adequado às diretrizes do Google Play para apps de saúde, segurança de dados e Play Billing.

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
Ciclo menstrual, bem-estar, alívio de cólicas e backup gratuito na nuvem.
```

### Texto Promocional — Até 140 caracteres
```text
Fases do ciclo menstrual, alívio SOS de cólicas, ciência do bem-estar e backup gratuito em nuvem. Da DLP Systems.
```

### Notas da Versão — O Que Há de Novo
```text
• Lançamento oficial do Cycle no Google Play.
• Roda do ciclo menstrual intuitiva com cálculo das quatro fases.
• Alívio SOS de cólicas com calor e respiração tátil 4-7-8.
• Check-in diário de sintomas, fluxo, humor, pele e notas pessoais.
• Previsor de fases futuras e widget exclusivo para tela inicial.
• Backup seguro e gratuito em nuvem com sincronização por conta Google.
• Plano Premium com relatório consolidado para consulta médica.
```

---

## 3. Contato e Política de Privacidade

- **E-mail de Suporte**: suporte@dlpsystems.com.br
- **Site Oficial**: https://www.dlpsystems.com.br
- **URL da Política de Privacidade**: `https://www.dlpsystems.com.br/privacidade`

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

### Questionário de Classificação de Conteúdo — IARC
- Categoria: Saúde e Fitness
- Responda Não para todas as perguntas de violência, teor sexual explícito, drogas e apostas.
- Confirme que o app possui anúncios e compras internas.
- Resultado esperado: Classificação Livre ou 10 a 12 anos.

### Segurança dos Dados — Data Safety Form
- O aplicativo coleta dados: Sim
- Criptografia em trânsito: Sim — protocolo TLS HTTPS 1.3
- Exclusão de conta: Sim — disponível dentro do aplicativo e via canal web
- Dados de saúde nunca são comercializados com terceiros.

---

## 5. Produtos de Assinatura no Play Billing

- `cycle_premium_monthly` — Cycle Premium Mensal por R$ 14,90 ao mês
- `cycle_premium_yearly` — Cycle Premium Anual por R$ 119,90 ao ano

---

## 6. Geração do Pacote AAB e Envio

```bash
./gradlew bundleRelease
```
Arquivo gerado: `app/build/outputs/bundle/release/app-release.aab`
Faça o upload no console e submeta as alterações para revisão.
