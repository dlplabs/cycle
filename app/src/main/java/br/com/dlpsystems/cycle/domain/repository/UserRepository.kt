package br.com.dlpsystems.cycle.domain.repository

import br.com.dlpsystems.cycle.domain.model.SignedInUser
import br.com.dlpsystems.cycle.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface UserRepository {
    val isBackendAvailable: Boolean
    fun observeAuth(): Flow<SignedInUser?>
    suspend fun signIn(email: String, password: String)
    suspend fun register(name: String, email: String, password: String, birthDate: LocalDate?)
    suspend fun signInWithGoogle(idToken: String)
    suspend fun signOut()
    fun observeProfile(): Flow<UserProfile?>
    suspend fun getProfile(): UserProfile?
    suspend fun updateAverages(averageCycleDays: Int, averagePeriodDays: Int)
    suspend fun saveAvatar(accessToken: String, jpeg: ByteArray)
    suspend fun readAvatar(accessToken: String, fileId: String): ByteArray
}
