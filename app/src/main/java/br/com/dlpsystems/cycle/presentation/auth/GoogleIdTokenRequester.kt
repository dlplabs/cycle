package br.com.dlpsystems.cycle.presentation.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import br.com.dlpsystems.cycle.domain.model.AuthFailure
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

class GoogleSignInCancelled : Exception()

suspend fun requestGoogleIdToken(context: Context): String {
    val activity = context.findActivity() ?: throw AuthFailure.GoogleFailed
    val clientId = webClientId(activity) ?: throw AuthFailure.GoogleNotConfigured
    val manager = CredentialManager.create(activity)
    return try {
        manager.token(activity, signInWithGoogleRequest(clientId))
    } catch (_: GoogleSignInCancelled) {
        throw GoogleSignInCancelled()
    } catch (_: Throwable) {
        try {
            manager.token(activity, googleIdRequest(clientId))
        } catch (_: GoogleSignInCancelled) {
            throw GoogleSignInCancelled()
        } catch (_: Throwable) {
            throw AuthFailure.GoogleFailed
        }
    }
}

private suspend fun CredentialManager.token(activity: Activity, request: GetCredentialRequest): String {
    return try {
        val result = getCredential(activity, request)
        GoogleIdTokenCredential.createFrom(result.credential.data).idToken
    } catch (_: GetCredentialCancellationException) {
        throw GoogleSignInCancelled()
    }
}

private fun signInWithGoogleRequest(clientId: String) = GetCredentialRequest.Builder()
    .addCredentialOption(GetSignInWithGoogleOption.Builder(clientId).build())
    .build()

private fun googleIdRequest(clientId: String) = GetCredentialRequest.Builder()
    .addCredentialOption(
        GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(false)
            .build(),
    )
    .build()

private fun webClientId(context: Context): String? {
    val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
    if (resId == 0) return null
    return context.getString(resId).takeIf { it.isNotBlank() }
}

private fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return this as? Activity
}
