package br.com.dlpsystems.cycle.presentation.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.dlpsystems.cycle.domain.model.AuthFailure
import br.com.dlpsystems.cycle.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.inject.Inject

data class AuthUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val birthDate: String = "",
    val loading: Boolean = false,
    val error: AuthFailure? = null,
    val birthDateInvalid: Boolean = false,
    val backendAvailable: Boolean = true,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState(backendAvailable = userRepository.isBackendAvailable))
    val state = _state.asStateFlow()

    fun onName(value: String) = _state.update { it.copy(name = value, error = null) }
    fun onEmail(value: String) = _state.update { it.copy(email = value, error = null) }
    fun onPassword(value: String) = _state.update { it.copy(password = value, error = null) }
    fun onBirthDate(value: String) = _state.update { it.copy(birthDate = value, birthDateInvalid = false) }

    fun signIn() = launchAuth { userRepository.signIn(state.value.email, state.value.password) }

    fun register() {
        val birthDate = state.value.birthDate.trim()
        val parsed = if (birthDate.isEmpty()) {
            null
        } else {
            try {
                LocalDate.parse(birthDate)
            } catch (_: DateTimeParseException) {
                _state.update { it.copy(birthDateInvalid = true) }
                return
            }
        }
        launchAuth {
            userRepository.register(
                name = state.value.name.trim(),
                email = state.value.email,
                password = state.value.password,
                birthDate = parsed,
            )
        }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val token = requestGoogleIdToken(context)
                userRepository.signInWithGoogle(token)
                _state.update { it.copy(loading = false) }
            } catch (_: GoogleSignInCancelled) {
                _state.update { it.copy(loading = false) }
            } catch (failure: AuthFailure) {
                _state.update { it.copy(loading = false, error = failure) }
            } catch (_: Throwable) {
                _state.update { it.copy(loading = false, error = AuthFailure.Unknown) }
            }
        }
    }

    private fun launchAuth(block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                block()
                _state.update { it.copy(loading = false) }
            } catch (failure: AuthFailure) {
                _state.update { it.copy(loading = false, error = failure) }
            } catch (_: Throwable) {
                _state.update { it.copy(loading = false, error = AuthFailure.Unknown) }
            }
        }
    }
}
