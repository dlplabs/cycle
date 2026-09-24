package br.com.dlpsystems.cycle.data.repository

import br.com.dlpsystems.cycle.data.remote.FirebaseAuthService
import br.com.dlpsystems.cycle.data.remote.FirebaseServices
import br.com.dlpsystems.cycle.data.remote.FirestoreService
import br.com.dlpsystems.cycle.domain.model.AuthFailure
import br.com.dlpsystems.cycle.domain.model.SignedInUser
import com.google.firebase.auth.FirebaseAuthException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class UserRepositoryImplTest {
    private val services = mockk<FirebaseServices>()
    private val authService = mockk<FirebaseAuthService>(relaxed = true)
    private val firestore = mockk<FirestoreService>(relaxed = true)
    private val repository = UserRepositoryImpl(services, authService, firestore)

    @Test
    fun deleteAccountDeletesFirestoreDataAndAuthUser() = runTest {
        every { services.available } returns true
        every { authService.currentUser() } returns SignedInUser("uid_123", "test@cycle.com", "Maria")
        coEvery { firestore.deleteUserData("uid_123") } returns Unit
        coEvery { authService.deleteAccount() } returns Unit

        repository.deleteAccount()

        coVerify(exactly = 1) { firestore.deleteUserData("uid_123") }
        coVerify(exactly = 1) { authService.deleteAccount() }
    }

    @Test
    fun deleteAccountThrowsRequiresRecentLoginWhenFirebaseDemandsReauth() = runTest {
        every { services.available } returns true
        every { authService.currentUser() } returns SignedInUser("uid_123", "test@cycle.com", "Maria")
        coEvery { firestore.deleteUserData("uid_123") } returns Unit
        val firebaseException = mockk<FirebaseAuthException>()
        every { firebaseException.errorCode } returns "ERROR_REQUIRES_RECENT_LOGIN"
        coEvery { authService.deleteAccount() } throws firebaseException

        val exception = assertThrows(AuthFailure.RequiresRecentLogin::class.java) {
            kotlinx.coroutines.runBlocking { repository.deleteAccount() }
        }
        assertEquals(AuthFailure.RequiresRecentLogin, exception)
    }
}
