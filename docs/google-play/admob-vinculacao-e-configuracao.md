# AdMob: Vinculação, Configuração e Diagnóstico — Cycle

Este documento detalha a arquitetura de monetização e registro do aplicativo **Cycle** no Google AdMob, descrevendo as etapas de vinculação, credenciais e os aspectos de integração com a Google Play Store e Google Cloud.

---

## 1. Identificadores Oficiais do Aplicativo

| Parâmetro | Valor de Produção |
| :--- | :--- |
| **Package Name** | `br.com.dlpsystems.cycle` |
| **AdMob Publisher ID** | `pub-6678306811480199` |
| **AdMob App ID (Produção)** | `ca-app-pub-6678306811480199~9347331345` |
| **URL da Loja Google Play** | `https://play.google.com/store/apps/details?id=br.com.dlpsystems.cycle` |
| **Website do Desenvolvedor** | `https://appcycle.com.br` |
| **Arquivo app-ads.txt** | `https://appcycle.com.br/app-ads.txt` |

---

## 2. Blocos de Anúncios Oficiais (Ad Units)

Cadastrados no console do AdMob para o aplicativo Cycle (`9347331345`):

| Nome do Bloco | Formato | Ad Unit ID Oficial |
| :--- | :--- | :--- |
| **Banner (Padrão de Produção)** | Banner | `ca-app-pub-6678306811480199/3623607621` |
| **Banner 1** | Banner | `ca-app-pub-6678306811480199/5447402755` |
| **Banner 2** | Banner | `ca-app-pub-6678306811480199/4134321081` |
| **Inter1** | Intersticial | `ca-app-pub-6678306811480199/3571636739` |
| **Intersticial 1** | Intersticial | `ca-app-pub-6678306811480199/7794364498` |

O bloco padrão de Banner está configurado em `app/build.gradle.kts` como fallback oficial para compilações de release.

---

## 3. Validação da Infraestrutura Externa

### 3.1. Arquivo `app-ads.txt`
O arquivo `app-ads.txt` do domínio oficial `https://appcycle.com.br/app-ads.txt` está validado e acessível via HTTPS com código de resposta HTTP 200:
```text
google.com, pub-6678306811480199, DIRECT, f08c47fec0942fa0
google.com, pub-7431268393847668, DIRECT, f08c47fec0942fa0
```
O identificador de publisher corresponde diretamente ao ID configurado no aplicativo (`pub-6678306811480199`).

### 3.2. Google Play Console
No Google Play Console, a ficha do aplicativo está ativa em produção (trilha de lançamento completa, versão 1.0.22, versionCode 23). O website de contato informado nas configurações da loja aponta para `https://appcycle.com.br`, permitindo que os rastreadores do AdMob façam a correlação automática do inventário.

---

## 4. Inicialização no Aplicativo Android

Para que o AdMob reconheça o aplicativo, registre a atividade da aplicação e valide os sinais de integridade de telemetria, o SDK do Google Mobile Ads é inicializado na abertura do app dentro de `CycleApplication.kt`:

```kotlin
override fun onCreate() {
    super.onCreate()
    CoroutineScope(Dispatchers.IO).launch {
        MobileAds.initialize(this@CycleApplication)
    }
}
```

A chamada inicializa o SDK em segundo plano, enviando o ping com o `com.google.android.gms.ads.APPLICATION_ID` (`ca-app-pub-6678306811480199~9347331345`) declarado no `AndroidManifest.xml`.

---

## 5. Vinculação e Confirmação no Painel do Google AdMob

O Google não disponibiliza comandos de CLI (`gcloud`) nem operações de escrita na API pública do AdMob (`admob.googleapis.com`) para criação ou vinculação de lojas de aplicativos — a API pública é restrita a relatórios e leitura de inventário.

Para concluir o registro no AdMob:

### Opção A — Pela seção "Apps para confirmar" (no menu lateral esquerdo do AdMob):
1. No menu lateral do AdMob, clique em **Apps para confirmar** (visível abaixo de "Configurações do app").
2. Caso o Cycle ou o pacote `br.com.dlpsystems.cycle` conste na lista, clique em **Confirmar** ou **Aprovar**.
3. Associe com o aplicativo Cycle existente para unificar as métricas e liberar a veiculação de anúncios.

### Opção B — Pela seção "Configurações do app":
1. No menu lateral, clique em **Configurações do app (App settings)**.
2. Na seção **Detalhes da app store (App store details)**, clique no ícone de edição (lápis) ou no botão **Vincular à app store (Link with an app store)**.
3. Busque por:
   ```text
   br.com.dlpsystems.cycle
   ```
   ou pela URL completa:
   ```text
   https://play.google.com/store/apps/details?id=br.com.dlpsystems.cycle
   ```
4. Selecione o aplicativo **Cycle: Calendário Menstrual** e clique em **Salvar**.
5. O AdMob iniciará a checagem automática do `app-ads.txt` e a revisão de preparação do aplicativo (App Readiness).
