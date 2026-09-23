# Cycle

Aplicativo Android nativo de acompanhamento do ciclo menstrual, com arquitetura limpa, Firestore e uma camada freemium (anúncios na versão gratuita, assinatura sem anúncios).

![Min SDK](https://img.shields.io/badge/minSdk-26-3D7858)
![Target SDK](https://img.shields.io/badge/targetSdk-35-65538A)
![CI](https://github.com/dlplabs/cycle/actions/workflows/android.yml/badge.svg)

O nome visível fica só em `app/src/main/res/values/strings.xml` (`app_name`) e em `AppConfig.displayName`.

## Ambiente

- Android Studio Ladybug ou superior
- JDK 17
- Min SDK 26, Target SDK 35

## Setup

1. Abra esta pasta no Android Studio e sincronize o Gradle.
2. Coloque o `google-services.json` do Firebase em `app/`. Sem esse arquivo o app compila, e login e Firestore ficam desligados.
3. Publique `firestore.rules` no console do Firebase.
4. O App ID de anúncio em `strings.xml` (`admob_app_id`) usa o ID de teste do Google. Troque pelo ID real antes de publicar. Os SKUs de assinatura são `cycle_premium_monthly` e `cycle_premium_yearly`.

## Arquitetura

`br.com.dlpsystems.cycle` separa `core`, `data`, `domain` e `presentation`. A interface usa Jetpack Compose e MVVM. A fase atual é calculada em `CalculateCurrentPhaseUseCase`.

## Commits

Conventional Commits: `feat:`, `fix:`, `refactor:`, `test:`.
