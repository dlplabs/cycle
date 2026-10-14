# Prompt de Desenvolvimento e Arquitetura: Proteção à Mulher & Motor IAVP (Cycle)

Você atuará como Engenheiro Especialista em Android (Kotlin), Frontend (Next.js/TypeScript) e Especialista em Segurança e Análise de Risco.

OBJETIVO:
Implementar o Motor de Avaliação de Risco IAVP (Instrumento de Avaliação de Risco em Violência Doméstica) e a suíte completa de Proteção à Mulher para o ecossistema "Cycle" (Android nativo e Web cycle.com.br).

---

### PARTE 1: MODELAGEM DO MOTOR DE CÁLCULO IAVP (Algoritmo Multiplataforma)

O IAVP avalia a probabilidade de reincidência, escalada de violência e letalidade. O cálculo NÃO é uma soma linear simples; obedece a regras de gatilhos críticos (Fatal Triggers) e ponderação por blocos de severidade.

1. ESTRUTURA DE DADOS E PESOS (TypeScript e Kotlin):
   - Categoria 1: Gatilhos Críticos de Letalidade (Peso 4 + Flag de Risco Imediato)
     * Q01: Tentativa ou histórico de sufocamento, estrangulamento ou asfixia.
     * Q02: Ameaça com uso ou exibição de arma de fogo ou faca/objeto cortante.
     * Q03: Ameaça explícita de morte, esquartejamento ou homicídio-suicídio ("se não for minha, não será de mais ninguém").
     * Q04: Agressão física durante a gestação ou logo após o parto.
   - Categoria 2: Indicadores Graves de Escalada (Peso 2)
     * Q05: Perseguição persistente (stalking), monitorização digital de telemóvel, redes sociais ou rotina física.
     * Q06: Agressões físicas graves anteriores que demandaram atendimento médico ou deixaram hematomas.
     * Q07: Ameaça direta contra filhos, parentes próximos ou animais de estimação.
     * Q08: Rompimento recente do relacionamento (últimos 3 meses) ou pedido de separação rejeitado pelo agressor.
     * Q09: Violação anterior de medida protetiva de urgência (se existente).
   - Categoria 3: Fatores de Risco e Vulnerabilidade Situacional (Peso 1)
     * Q10: Uso compulsivo ou abusivo de álcool, medicamentos controlados ou drogas ilícitas pelo agressor.
     * Q11: Desemprego recente ou grave instabilidade financeira associada a explosões de raiva.
     * Q12: Ciúme obsessivo, isolamento forçado de amigos e rede familiar da mulher.
     * Q13: Histórico de violência contra outras parceiras ou antecedentes criminais.

2. REGRAS DO MOTOR DE DECISÃO (Decision Engine):
   - REGRA ZERO (Override Crítico): Se QUALQUER pergunta da Categoria 1 (Q01 a Q04) for respondida como POSITIVA (true), o nível é compulsoriamente classificado como EXTREMO, independentemente da pontuação total.
   - PONTUAÇÃO PONDERADA:
     * Score = (Cat1_Count * 4) + (Cat2_Count * 2) + (Cat3_Count * 1)
   - CLASSIFICAÇÃO:
     * Score >= 10 OU Override Crítico = RISCO EXTREMO
     * Score entre 6 e 9 = RISCO ELEVADO / GRAVE
     * Score entre 3 e 5 = RISCO MODERADO
     * Score <= 2 = RISCO BAIXO / MONITORIZAÇÃO

3. MATRIZ DE RECOMENDAÇÕES E DIRETRIZES DO PLANO DE FUGA:
   - RISCO EXTREMO:
     * Orientações: Não confronte nem comunique saída ao agressor; prepare bolsa de emergência invisível (documentos pessoais, medicação, cópias de chaves, quantia de dinheiro guardada fora de casa); estabeleça sinal de socorro com pessoa de confiança externa.
     * Ações Rápidas: Ligações diretas para acionar 190 (Polícia Militar), 180 (Central da Mulher) e localização da DEAM mais próxima.
   - RISCO GRAVE:
     * Recomendações de suporte jurídico, registo de provas digitais no cofre seguro e fortalecimento da rede de proteção (CREAS / CRAS / Defensoria Pública).
   - RISCO MODERADO / BAIXO:
     * Alertas de limites relacionais, registo de linha do tempo de incidentes para monitorização preventiva.

---

### PARTE 2: PERMISSÕES E ARQUITETURA NO ANDROID (KOTLIN)

1. PERMISSÕES NO AndroidManifest.xml:
```xml
<!-- Localização precisa e em segundo plano para socorro -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- Envio silencioso de SMS -->
<uses-permission android:name="android.permission.SEND_SMS" />

<!-- Gravação de áudio forense -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />

<!-- Vibração discreta de confirmação tátil -->
<uses-permission android:name="android.permission.VIBRATE" />

<!-- Manter execução durante o despacho SOS -->
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
```

2. TRATAMENTO SEGURO DE PERMISSÕES CRÍTICAS:
   - Implemente um fluxo de permissões sob demanda (runtime permissions) justificado dentro da interface como "Recursos de Localização para Atendimento Emergencial".
   - Caso a permissão SEND_SMS não seja concedida pelo sistema/utilizador, crie fallback transparente disparando uma Intent.ACTION_SENDTO para smsto: ou link direto via Intent de WhatsApp/Mensageiro.

3. PERSISTÊNCIA CRIPTOGRAFADA:
   - Configuração de Room com SQLCipher via SupportFactory(SQLiteDatabase.getBytes("CYCLE_SECURE_PASS".toCharArray())).
   - As respostas do IAVP nunca devem ser salvas no banco persistente sem que a utilizadora marque explicitamente a opção "Salvar no meu histórico privado". Por padrão, os resultados vivem unicamente no StateFlow da ViewModel durante a sessão e são limpos da memória ao sair da tela.

---

### PARTE 3: IMPLEMENTAÇÃO WEB (cycle.com.br / NEXT.JS)

1. ARQUITETURA DE MEMÓRIA (EPHEMERAL BY DESIGN):
   - Motor em TypeScript (evaluateIavp.ts) executado 100% no cliente (sem envio de payload para o servidor para proteger a utilizadora contra logs de tráfego/provedores).
   - Não gravar respostas em localStorage, sessionStorage ou IndexedDB.
   - Implementação de Atalho Global: Pressionar Esc três vezes ou clicar no botão "Sair Imediatamente" limpa todo o estado do React e executa:
```typescript
window.location.replace("https://www.google.com.br");
```

---

### ENTREGÁVEIS DE CÓDIGO ESPERADOS:

1. IavpCalculator.kt e evaluateIavp.ts: Código completo das funções de cálculo com todos os 13 itens, pontuação ponderada, verificação de gatilhos fatais e saída estruturada.
2. EmergencySosManager.kt: Classe orquestradora do Android para obter GPS via FusedLocationProviderClient, formatar mensagem SOS padronizada e despachar silenciosamente com SmsManager.
3. Telas em Jetpack Compose:
   - IavpQuestionnaireScreen.kt: Formulário sequencial com progresso, transições suaves e botão de saída emergencial.
   - IavpResultScreen.kt: Apresentação visual limpa do resultado de risco e do plano de ação, contendo o botão "Destruir Histórico e Fechar".
