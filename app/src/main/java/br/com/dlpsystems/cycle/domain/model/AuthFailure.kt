package br.com.dlpsystems.cycle.domain.model

sealed class AuthFailure : Exception() {
    data object InvalidCredentials : AuthFailure()
    data object EmailInUse : AuthFailure()
    data object WeakPassword : AuthFailure()
    data object NotConfigured : AuthFailure()
    data object GoogleNotConfigured : AuthFailure()
    data object ProviderDisabled : AuthFailure()
    data object Network : AuthFailure()
    data object Unknown : AuthFailure()
}
