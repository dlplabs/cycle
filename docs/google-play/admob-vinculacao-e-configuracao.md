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

## 2. Validação da Infraestrutura Externa

### 2.1. Arquivo `app-ads.txt`
O arquivo `app-ads.txt` do domínio oficial `https://appcycle.com.br/app-ads.txt` está validado e acessível via HTTPS com código de resposta HTTP 200:
```text
google.com, pub-6678306811480199, DIRECT, f08c47fec0942fa0
google.com, pub-7431268393847668, DIRECT, f08c47fec0942fa0
```
O identificador de publisher corresponde diretamente ao ID configurado no aplicativo (`pub-6678306811480199`).

### 2.2. Google Play Console
No Google Play Console, a ficha do aplicativo está ativa em produção (trilha de lançamento completa, versão 1.0.22, versionCode 23). O website de contato informado nas configurações da loja aponta corretamente para `https://appcycle.com.br`, permitindo que os rastreadores do AdMob façam a correlação automática do inventário.

---

## 3. Inicialização no Aplicativo Android

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

## 4. Vinculação no Painel do Google AdMob

O Google não disponibiliza comandos de CLI (`gcloud`) nem operações de escrita na API pública do AdMob (`admob.googleapis.com`) para criação ou vinculação de lojas de aplicativos — a API pública é restrita a relatórios e leitura de inventário. A vinculação de loja é realizada exclusivamente no console do AdMob:

1. Acesse o [Console do Google AdMob](https://apps.admob.com/).
2. No menu lateral, acesse **Apps (Aplicativos)** > **View all apps (Ver todos os apps)**.
3. Clique sobre o aplicativo **Cycle** (App ID `ca-app-pub-6678306811480199~9347331345`).
4. Selecione **App settings (Configurações do app)**.
5. Na seção **App store details (Detalhes da app store)**, clique no ícone de lápis ou no botão **Link with an app store (Vincular à app store)**.
6. Selecione a loja **Google Play** e busque pelo pacote:
   ```text
   br.com.dlpsystems.cycle
   ```
   ou pela URL completa:
   ```text
   https://play.google.com/store/apps/details?id=br.com.dlpsystems.cycle
   ```
7. Selecione o aplicativo **Cycle: Calendário Menstrual** e confirme a vinculação clicando em **Save (Salvar)**.
8. Uma vez salvo, o AdMob iniciará a checagem automática do `app-ads.txt` e a revisão de preparação do aplicativo (App Readiness).
