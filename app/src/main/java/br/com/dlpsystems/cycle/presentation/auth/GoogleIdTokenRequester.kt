package br.com.dlpsystems.cycle.presentation.auth

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import br.com.dlpsystems.cycle.domain.model.AuthFailure
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import kotlinx.coroutines.tasks.await

class GoogleSignInCancelled : Exception()

suspend fun googleSignInIntent(activity: Activity): PendingIntent {
    val clientId = webClientId(activity) ?: throw AuthFailure.GoogleNotConfigured
    val request = GetSignInIntentRequest.builder()
        .setServerClientId(clientId)
        .build()
    return Identity.getSignInClient(activity).getSignInIntent(request).await()
}

fun googleIdTokenFromIntent(context: Context, data: Intent?): String {
    if (data == null) throw GoogleSignInCancelled()
    return try {
        val credential = Identity.getSignInClient(context).getSignInCredentialFromIntent(data)
        credential.googleIdToken ?: throw AuthFailure.GoogleFailed
    } catch (error: ApiException) {
        when (error.statusCode) {
            CommonStatusCodes.CANCELED,
            SIGN_IN_CANCELLED,
            -> throw GoogleSignInCancelled()
            CommonStatusCodes.DEVELOPER_ERROR -> throw AuthFailure.GoogleNotConfigured
            CommonStatusCodes.NETWORK_ERROR -> throw AuthFailure.Network
            else -> throw AuthFailure.GoogleFailed
        }
    }
}

private fun webClientId(context: Context): String? {
    val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
    if (resId == 0) return null
    return context.getString(resId).takeIf { it.isNotBlank() }
}

private const val SIGN_IN_CANCELLED = 12501
