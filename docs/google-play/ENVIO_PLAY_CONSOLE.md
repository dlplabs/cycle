# Envio manual — Google Play Console

Pacote `br.com.dlpsystems.cycle`. Nome na loja: **Cycle**. Idioma padrão da ficha: **português (Brasil)**.

Os arquivos de imagem estão em [`graficos/`](graficos/). A política para publicar na web está em [`POLITICA_DE_PRIVACIDADE.md`](POLITICA_DE_PRIVACIDADE.md). Para regerar as imagens: `python3 docs/google-play/gerar_graficos.py`.

Substitua cada campo entre colchetes antes de enviar.

## 1. Criar o app

| Campo | Valor |
| --- | --- |
| Nome do app | Cycle |
| Idioma padrão | Português (Brasil) |
| App ou jogo | App |
| Gratuito ou pago | Gratuito (com anúncios e assinatura) |
| Declarações | Confirmar as políticas do programa para desenvolvedores e, se for o caso, exportação dos EUA |

Categoria: **Saúde e fitness**. Tags sugeridas: ciclo menstrual, bem-estar feminino.

## 2. Ficha da loja — texto para colar

**Título** (5/30)

```
Cycle
```

**Descrição breve** (66/80)

```
Acompanhe ciclo, fases e bem-estar. Não substitui consulta médica.
```

**Texto promocional** (opcional, 58/80)

```
Fases, check-in, SOS e planner. Premium tira os anúncios.
```

**Descrição completa**

```
O Cycle acompanha o ciclo menstrual e organiza o bem-estar por fase: nutrição, movimento, pele e mente, com fontes científicas que abrem no navegador.

O que você faz no app
• Vê o dia e a fase numa roda.
• Faz um check-in rápido de fluxo, sintomas e dor.
• Registra o dia com mais detalhe.
• Usa o SOS com timer de calor local (20 a 30 minutos) e respiração 4-7-8. O app só marca o tempo e vibra. Não controla bolsa térmica nem outro aparelho.
• Consulta a fase prevista no planner.
• Coloca um widget com o dia, a fase e o atalho “Menstruação desceu hoje”.

Gratuito e Premium
A versão gratuita mostra um banner só em Configurações e no planner. A tela inicial, o registro do dia e o SOS não têm anúncio.
A assinatura Cycle Premium (mensal ou anual) remove anúncios, amplia o horizonte do planner e libera o PDF dos últimos ciclos para levar à consulta.

Aviso
O Cycle é uma ferramenta de autoconhecimento e acompanhamento de bem-estar baseada em literatura científica. Ele não substitui consultas médicas, diagnósticos clínicos ou métodos contraceptivos.

Conta
É preciso entrar com e-mail ou Google. Os registros ficam na sua conta. A política de privacidade explica o que é guardado e como pedir exclusão.
```

**Notas da versão 1.0** (menos de 500 caracteres)

```
Primeira versão: roda do ciclo, check-in com dor, SOS, planner, widget, fontes científicas, banner na versão gratuita e assinatura Premium com PDF para a consulta.
```

## 3. Gráficos — o que enviar em cada slot

| Slot na Play Console | Arquivo | Tamanho |
| --- | --- | --- |
| Ícone de alta resolução | `graficos/icone-512.png` | 512×512 PNG |
| Gráfico de recursos | `graficos/grafico-destaque-1024x500.png` | 1024×500 PNG |
| Capturas de telefone (obrigatório, 2 a 8) | `01-home` … `06-configuracoes` | 1080×1920 (9:16) |
| Tablet 7 pol. | `graficos/tablet-7-home-1920x1080.png` | 1920×1080 |
| Tablet 10 pol. | `graficos/tablet-10-planner-1920x1080.png` | 1920×1080 |

Ordem sugerida no telefone:

1. `01-home-1080x1920.png` — dia, fase e check-in
2. `02-evidencias-1080x1920.png` — fontes
3. `03-sos-1080x1920.png` — alívio
4. `04-planner-1080x1920.png` — data futura
5. `05-premium-1080x1920.png` — assinatura
6. `06-configuracoes-1080x1920.png` — ajustes, PDF e anúncio

São composições da interface para a ficha, no padrão visual do app (fundo `#FAF9F6` e as quatro cores de fase). Não são capturas de um aparelho. Se a revisão pedir tela real, substitua por screenshots do build de release.

Não envie vídeo promocional nesta leva.

## 4. Contato da ficha

| Campo | Valor |
| --- | --- |
| E-mail | [E-MAIL PÚBLICO] |
| Telefone | opcional |
| Site | [URL da política ou do site] |
| Política de privacidade | [URL HTTPS da política publicada] |

## 5. Categorização da loja e público

| Pergunta | Resposta |
| --- | --- |
| Categoria | Saúde e fitness |
| Contém anúncios | Sim |
| Público-alvo | 18 anos ou mais. Não é app para crianças. |
| Apelo a crianças | Não |
| Ads SDK / famílias | Não entrar no programa Para a família |

## 6. Acesso ao app (revisores)

O uso depois do login exige conta. Em **Acesso ao app**, escolha “todas ou algumas funcionalidades são restritas” e informe:

| Campo | Valor |
| --- | --- |
| Nome | Conta de revisão |
| Usuário | [E-MAIL DE TESTE CRIADO NO FIREBASE] |
| Senha | [SENHA] |
| Instruções | Entrar com e-mail e senha. Aceitar o aviso de saúde. A Home abre a roda. Configurações leva à assinatura e ao PDF. SOS e Planner estão na Home. |

Crie essa conta no Firebase Authentication antes de enviar para análise. Não use a conta pessoal.

## 7. Classificação de conteúdo (questionário IARC)

Respostas alinhadas ao que o app faz hoje:

| Tema | Resposta |
| --- | --- |
| Violência | Não |
| Medo, horror | Não |
| Sexualidade / nudez | Não. Há registro de ciclo menstrual e bem-estar, sem conteúdo sexual. |
| Linguagem | Não |
| Substâncias controladas | Não |
| Apostas | Não |
| Compras digitais | Sim — assinatura no app |
| Partilha de localização com outros usuários | Não |
| Interação entre usuários | Não |
| Partilha de informação pessoal | A conta fica no Firebase do desenvolvedor; não há rede social |
| Internet irrestrita (navegador aberto) | Os badges abrem artigos no navegador |

Classificação esperada: livre ou baixa maturidade, conforme o IARC devolver. Não marque o app como infantil.

## 8. Segurança dos dados

Marque **coleta**. Não marque venda de dados.

| Tipo | Coletado | Compartilhado | Obrigatório | Finalidade |
| --- | --- | --- | --- | --- |
| Nome | Sim | Não (processador Firebase) | Sim, para a conta | Funcionalidade da conta |
| E-mail | Sim | Não | Sim | Conta e autenticação |
| IDs de usuário | Sim | Não | Sim | Conta |
| Informações de saúde | Sim (ciclo, fluxo, sintomas, dor, humor, pele, notas) | Não | Sim para o recurso | Funcionalidade do app |
| Interações no app | Sim, se o Analytics estiver ligado | Não | Não | Análise, só nome da tela |
| Identificadores do dispositivo | Sim, via AdMob na versão gratuita | Com o Google para anúncio | Não | Publicidade |
| Info financeira / cartão | Não | — | — | A cobrança é do Google Play |
| Localização | Não | — | — | — |
| Fotos, contatos, SMS | Não | — | — | — |

Tratamento:

- Criptografado em trânsito: **sim**
- O usuário pode pedir exclusão: **sim** (ver bloqueio da exclusão no app, abaixo)
- Dados de saúde: marque como dado sensível coletado

Declaração curta sugerida no formulário: “Registros de ciclo ficam na conta Firebase da própria pessoa. Anúncios do AdMob aparecem só na versão gratuita, em Configurações e no planner. A telemetria não envia sintomas, fluxo, dor nem anotações.”

## 9. Anúncios, ID de publicidade e finanças

| Declaração | Resposta |
| --- | --- |
| O app contém anúncios | Sim |
| Usa ID de publicidade | Sim (AdMob). Não use o ID para outra finalidade. |
| Recursos financeiros | Sim — assinaturas no app. Não é banco, empréstimo nem cripto. |
| App de saúde | Sim, bem-estar / acompanhamento de ciclo. Não é dispositivo médico e não diagnostica. O aviso de saúde aparece antes do uso. |

## 10. Produtos de assinatura (criar no Console antes do review de compra)

| ID do produto | Tipo | Nome na loja | Benefício |
| --- | --- | --- | --- |
| `cycle_premium_monthly` | Assinatura, base mensal | Cycle Premium mensal | Sem anúncios, planner estendido, PDF |
| `cycle_premium_yearly` | Assinatura, base anual | Cycle Premium anual | Os mesmos benefícios |

Preço: definir em BRL no Console. O app não traz preço fixo. Plano base único em cada assinatura, com período de teste só se você criar a oferta. Ative os produtos e publique-os no mesmo envio do app, ou a compra falha em produção.

## 11. App bundle e assinatura

1. Crie um upload key **fora do git** (`*.jks` já está no `.gitignore`).
2. Ative **Play App Signing** no Console.
3. Gere o AAB de release com esse keystore.
4. Envie o AAB em Produção, teste interno ou teste fechado.

`targetSdk` 35 e `minSdk` 26 já estão no módulo. Troque os IDs de teste do AdMob e inclua o `google-services.json` de produção no build que for para a loja.

Países: comece pelo Brasil se a ficha e o suporte estão em português.

## 12. Bloqueio antes de mandar para análise

A Play exige, para app com criação de conta, **exclusão da conta dentro do app** e um **link web** para o mesmo pedido. O Cycle hoje só tem **Sair** em Configurações. Sem “Excluir conta” e sem a URL da política no ar, a ficha fica incompleta e a análise pode recusar.

Quando a exclusão existir, o texto da política já descreve o efeito: some a conta de autenticação e os documentos de perfil, ciclos e registros.

## 13. Ordem no Console

1. Publicar a política e colar a URL.
2. Preencher ficha, ícone, gráfico de recursos e capturas.
3. Questionários: anúncios, acesso, classificação, público, segurança dos dados, saúde, ID de publicidade, finanças.
4. Criar as duas assinaturas.
5. Enviar o AAB assinado num teste interno, com a conta de revisão.
6. Só então promover para produção.
