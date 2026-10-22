package br.com.dlpsystems.cycle.presentation.auth

import android.content.Context
import br.com.dlpsystems.cycle.domain.model.AuthFailure
import br.com.dlpsystems.cycle.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { userRepository.isBackendAvailable } returns true
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun sendPasswordResetWithInvalidEmailSetsErrorImmediately() = runTest {
        val viewModel = AuthViewModel(userRepository, context)

        viewModel.sendPasswordReset("invalid")

        val state = viewModel.state.value
        assertEquals(AuthFailure.InvalidCredentials, state.passwordResetError)
        assertFalse(state.passwordResetSuccess)
        assertFalse(state.passwordResetLoading)
        coVerify(exactly = 0) { userRepository.sendPasswordResetEmail(any()) }
    }

    @Test
    fun sendPasswordResetWithValidEmailCallsRepositoryAndSetsSuccess() = runTest {
        coEvery { userRepository.sendPasswordResetEmail("user@example.com") } returns Unit
        val viewModel = AuthViewModel(userRepository, context)

        viewModel.sendPasswordReset("user@example.com")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.passwordResetSuccess)
        assertNull(state.passwordResetError)
        assertFalse(state.passwordResetLoading)
        coVerify(exactly = 1) { userRepository.sendPasswordResetEmail("user@example.com") }
    }

    @Test
    fun clearPasswordResetStateResetsFields() = runTest {
        coEvery { userRepository.sendPasswordResetEmail("user@example.com") } returns Unit
        val viewModel = AuthViewModel(userRepository, context)

        viewModel.sendPasswordReset("user@example.com")
        advanceUntilIdle()
        assertTrue(viewModel.state.value.passwordResetSuccess)

        viewModel.clearPasswordResetState()
        assertFalse(viewModel.state.value.passwordResetSuccess)
        assertNull(viewModel.state.value.passwordResetError)
    }

    @Test
    fun cancelGoogleSignInStopsLoading() = runTest {
        val viewModel = AuthViewModel(userRepository, context)
        viewModel.cancelGoogleSignIn()
        assertFalse(viewModel.state.value.loading)
    }
}
