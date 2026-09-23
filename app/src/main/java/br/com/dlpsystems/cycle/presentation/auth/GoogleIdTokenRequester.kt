package br.com.dlpsystems.cycle.presentation.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import br.com.dlpsystems.cycle.domain.model.AuthFailure
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

class GoogleSignInCancelled : Exception()

suspend fun requestGoogleIdToken(context: Context): String {
    val clientId = webClientId(context) ?: throw AuthFailure.GoogleNotConfigured
    val option = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(false)
        .build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(option)
        .build()
    return try {
        val result = CredentialManager.create(context).getCredential(context, request)
        GoogleIdTokenCredential.createFrom(result.credential.data).idToken
    } catch (_: GetCredentialCancellationException) {
        throw GoogleSignInCancelled()
    }
}

private fun webClientId(context: Context): String? {
    val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
    if (resId == 0) return null
    return context.getString(resId).takeIf { it.isNotBlank() }
}
