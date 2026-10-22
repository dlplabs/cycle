package br.com.dlpsystems.cycle.presentation.auth

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import br.com.dlpsystems.cycle.domain.model.AuthFailure
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.tasks.await

class GoogleSignInCancelled : Exception()

suspend fun requestGoogleIdTokenWithCredentialManager(activity: Activity): String {
    val clientId = webClientId(activity)
    val credentialManager = CredentialManager.create(activity)
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(false)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    return try {
        val result = credentialManager.getCredential(activity, request)
        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            googleIdTokenCredential.idToken
        } else {
            throw AuthFailure.GoogleNotConfigured
        }
    } catch (_: GetCredentialCancellationException) {
        throw GoogleSignInCancelled()
    }
}

suspend fun googleSignInIntent(activity: Activity): PendingIntent {
    val clientId = webClientId(activity)
    val request = GetSignInIntentRequest.builder()
        .setServerClientId(clientId)
        .build()
    return Identity.getSignInClient(activity).getSignInIntent(request).await()
}

fun googleIdTokenFromIntent(context: Context, data: Intent?): String {
    if (data == null) throw GoogleSignInCancelled()
    return try {
        val credential = Identity.getSignInClient(context).getSignInCredentialFromIntent(data)
        credential.googleIdToken ?: throw AuthFailure.GoogleNotConfigured
    } catch (error: ApiException) {
        when (error.statusCode) {
            CommonStatusCodes.CANCELED,
            SIGN_IN_CANCELLED,
            -> throw GoogleSignInCancelled()
            CommonStatusCodes.DEVELOPER_ERROR,
            SIGN_IN_FAILED,
            -> throw AuthFailure.GoogleNotConfigured
            CommonStatusCodes.NETWORK_ERROR -> throw AuthFailure.Network
            else -> throw AuthFailure.GoogleFailed
        }
    }
}

fun webClientId(context: Context): String {
    val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
    val fromResource = if (resId != 0) context.getString(resId).takeIf { it.isNotBlank() } else null
    return fromResource ?: DEFAULT_FALLBACK_WEB_CLIENT_ID
}

private const val DEFAULT_FALLBACK_WEB_CLIENT_ID =
    "604557808835-02cdd7s9righc14lhrbuq0juqtbd2047.apps.googleusercontent.com"

private const val SIGN_IN_CANCELLED = 12501
private const val SIGN_IN_FAILED = 12500

