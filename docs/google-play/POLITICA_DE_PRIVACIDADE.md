# Política de privacidade — Cycle

**Controlador:** [NOME DO DESENVOLVEDOR — preencher, por exemplo DLP Systems]  
**Contato:** [E-MAIL PÚBLICO — preencher]  
**Aplicativo:** Cycle (`br.com.dlpsystems.cycle`)  
**Vigência:** 22 de setembro de 2026

Esta política descreve o que o Cycle coleta, para quê, e como pedir exclusão. O Cycle é uma ferramenta de autoconhecimento e bem-estar. Não substitui consulta médica, diagnóstico ou método contraceptivo.

A Play Console exige o **link público** desta página (GitHub Pages, site ou Firebase Hosting). O arquivo neste repositório não substitui a URL.

## Dados da conta

Com e-mail e senha, ou com Google Sign-In, o app guarda:

- identificador da conta
- nome
- e-mail

A autenticação fica no Firebase Authentication, quando o `google-services.json` está configurado.

## Dados de ciclo e bem-estar

Você registra, e o app guarda no Cloud Firestore, na sua conta:

- datas de ciclo e duração
- fase calculada e dia do ciclo
- fluxo, sintomas, humor, pele e anotações
- nível de dor de 0 a 10

Esses dados são informação de saúde. Servem para mostrar a roda, o planner, o widget e, no plano Premium, o PDF de consulta. Não são vendidos.

## O que a telemetria não envia

O Firebase Analytics, se estiver ativo, recebe só eventos de tela: registro diário, SOS, exportação de PDF, abertura de fonte científica, visualização do paywall e ativação da assinatura. Esses eventos não incluem fluxo, sintomas, dor, notas nem fase.

## Anúncios

Na versão gratuita há um banner do Google AdMob em Configurações e no planner. A Home, o registro do dia e o SOS não exibem anúncio. Quem assina o Premium não carrega anúncio.

O AdMob pode usar o identificador de publicidade do aparelho para medir e exibir anúncios. Dá para limitar a personalização nas configurações de anúncios do Android.

Os IDs atuais do projeto são os de **teste** do Google. Antes de publicar, troque `admob_app_id` e `admob_banner_id` pelos IDs da conta AdMob real.

## Assinatura

As assinaturas `cycle_premium_monthly` e `cycle_premium_yearly` são cobradas pelo Google Play. O Cycle não vê nem guarda número de cartão. Guarda só se a conta está Premium, inclusive numa cópia local no aparelho.

## Com quem os dados ficam

| Serviço | Papel |
| --- | --- |
| Google Firebase (Auth, Firestore, Analytics) | Conta, registros e eventos de tela |
| Google AdMob | Banner na versão gratuita |
| Google Play Billing | Cobrança da assinatura |

Não há venda de dados. Não há repasse para corretores de dados.

## Retenção e exclusão

Os registros permanecem enquanto a conta existir.

Para apagar a conta e os dados de ciclo, use **Excluir conta** nas configurações do app ou escreva para o e-mail de contato. A exclusão remove o perfil, os ciclos e os registros diários no Firestore e a conta de autenticação. O pedido pelo e-mail é atendido em até 30 dias. Cópias de backup do Firebase podem levar mais alguns dias para sair dos sistemas do Google.

A assinatura é cancelada na Play Store, em Pagamentos e assinaturas. Apagar a conta no app não cancela sozinha a cobrança do Google Play.

## Crianças

O Cycle não é dirigido a menores de 13 anos e não deve ser usado por crianças.

## Segurança

O tráfego com Firebase e Google usa criptografia em trânsito. O acesso aos documentos no Firestore fica restrito à conta dona dos dados.

## Mudanças

Alterações desta política passam a valer quando o texto publicado na URL da ficha for atualizado.
