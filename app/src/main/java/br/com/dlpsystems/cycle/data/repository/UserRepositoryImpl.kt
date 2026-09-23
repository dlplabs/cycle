package br.com.dlpsystems.cycle.data.repository

import br.com.dlpsystems.cycle.core.config.CycleConstants
import br.com.dlpsystems.cycle.data.remote.FirebaseAuthService
import br.com.dlpsystems.cycle.data.remote.FirebaseServices
import br.com.dlpsystems.cycle.data.remote.FirestoreService
import br.com.dlpsystems.cycle.data.remote.firebaseAuthCode
import br.com.dlpsystems.cycle.domain.model.AuthFailure
import br.com.dlpsystems.cycle.domain.model.ServiceUnavailableException
import br.com.dlpsystems.cycle.domain.model.SignedInUser
import br.com.dlpsystems.cycle.domain.model.UserProfile
import br.com.dlpsystems.cycle.domain.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val services: FirebaseServices,
    private val authService: FirebaseAuthService,
    private val firestore: FirestoreService,
) : UserRepository {
    override val isBackendAvailable: Boolean
        get() = services.available

    override fun observeAuth(): Flow<SignedInUser?> = authService.observeUser()

    override suspend fun signIn(email: String, password: String) = guard {
        val user = authService.signIn(email, password)
        ensureProfile(user, user.displayName.orEmpty())
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        birthDate: LocalDate?,
    ) = guard {
        val user = authService.register(email, password)
        saveNewProfile(user.id, name, birthDate)
    }

    override suspend fun signInWithGoogle(idToken: String) = guard {
        val user = authService.signInWithGoogle(idToken)
        ensureProfile(user, user.displayName.orEmpty())
    }

    override suspend fun signOut() {
        authService.signOut()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeProfile(): Flow<UserProfile?> =
        authService.observeUser().flatMapLatest { user ->
            if (user == null || !services.available) flowOf(null)
            else firestore.observeProfile(user.id)
        }

    override suspend fun getProfile(): UserProfile? {
        val uid = currentUid()
        return firestore.getProfile(uid)
    }

    override suspend fun updateAverages(averageCycleDays: Int, averagePeriodDays: Int) {
        firestore.updateAverages(
            currentUid(),
            averageCycleDays.coerceIn(15, 90),
            averagePeriodDays.coerceIn(1, 12),
        )
    }

    private suspend fun ensureProfile(user: SignedInUser, fallbackName: String) {
        if (firestore.getProfile(user.id) == null) {
            saveNewProfile(user.id, fallbackName.ifBlank { user.email.orEmpty() }, null)
        }
    }

    private suspend fun saveNewProfile(uid: String, name: String, birthDate: LocalDate?) {
        firestore.saveProfile(
            uid,
            UserProfile(
                name = name,
                birthDate = birthDate,
                averageCycleDays = CycleConstants.DEFAULT_CYCLE_DAYS,
                averagePeriodDays = CycleConstants.DEFAULT_PERIOD_DAYS,
                createdAt = Instant.now(),
            ),
        )
    }

    private fun currentUid(): String =
        authService.currentUser()?.id ?: throw ServiceUnavailableException()

    private suspend fun guard(block: suspend () -> Unit) {
        if (!services.available) throw AuthFailure.NotConfigured
        try {
            block()
        } catch (failure: AuthFailure) {
            throw failure
        } catch (error: ServiceUnavailableException) {
            throw AuthFailure.NotConfigured
        } catch (error: Throwable) {
            throw when (error.firebaseAuthCode()) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> AuthFailure.EmailInUse
                "ERROR_WEAK_PASSWORD" -> AuthFailure.WeakPassword
                "ERROR_OPERATION_NOT_ALLOWED" -> AuthFailure.ProviderDisabled
                "ERROR_NETWORK_REQUEST_FAILED" -> AuthFailure.Network
                "ERROR_INVALID_EMAIL",
                "ERROR_WRONG_PASSWORD",
                "ERROR_USER_NOT_FOUND",
                "ERROR_INVALID_CREDENTIAL",
                -> AuthFailure.InvalidCredentials
                else -> AuthFailure.Unknown
            }
        }
    }
}
