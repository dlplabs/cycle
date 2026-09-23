package br.com.dlpsystems.cycle.data.remote

import br.com.dlpsystems.cycle.domain.model.ServiceUnavailableException
import br.com.dlpsystems.cycle.domain.model.SignedInUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthService @Inject constructor(
    private val services: FirebaseServices,
) {
    fun observeUser(): Flow<SignedInUser?> {
        if (!services.available) return flowOf(null)
        return callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { auth ->
                trySend(auth.currentUser?.toSignedInUser())
            }
            services.auth.addAuthStateListener(listener)
            awaitClose { services.auth.removeAuthStateListener(listener) }
        }
    }

    suspend fun signIn(email: String, password: String): SignedInUser {
        val auth = authOrThrow()
        val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
        val user = result.user ?: throw ServiceUnavailableException()
        return user.toSignedInUser()
    }

    suspend fun register(email: String, password: String): SignedInUser {
        val auth = authOrThrow()
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val user = result.user ?: throw ServiceUnavailableException()
        return user.toSignedInUser()
    }

    suspend fun signInWithGoogle(idToken: String): SignedInUser {
        val auth = authOrThrow()
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val user = result.user ?: throw ServiceUnavailableException()
        return user.toSignedInUser()
    }

    fun signOut() {
        if (services.available) services.auth.signOut()
    }

    fun currentUser(): SignedInUser? {
        if (!services.available) return null
        val user = services.auth.currentUser ?: return null
        return user.toSignedInUser()
    }

    private fun FirebaseUser.toSignedInUser() = SignedInUser(
        id = uid,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl?.toString(),
    )

    private fun authOrThrow(): FirebaseAuth {
        if (!services.available) throw ServiceUnavailableException()
        return services.auth
    }
}

fun Throwable.firebaseAuthCode(): String? {
    var current: Throwable? = this
    while (current != null) {
        val code = (current as? FirebaseAuthException)?.errorCode
        if (!code.isNullOrBlank()) return code
        current = current.cause
    }
    return null
}
