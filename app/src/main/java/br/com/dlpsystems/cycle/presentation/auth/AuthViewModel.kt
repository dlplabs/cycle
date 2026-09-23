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
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
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
    fun onBirthDate(value: String) = _state.update {
        it.copy(birthDate = BirthDateInput.digits(value), birthDateInvalid = false)
    }

    fun signIn() = launchAuth {
        userRepository.signIn(state.value.email.trim(), state.value.password)
    }

    fun register() {
        val digits = state.value.birthDate
        val parsed = if (digits.isEmpty()) {
            null
        } else {
            BirthDateInput.parseDigits(digits) ?: run {
                _state.update { it.copy(birthDateInvalid = true) }
                return
            }
        }
        launchAuth {
            userRepository.register(
                name = state.value.name.trim(),
                email = state.value.email.trim(),
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
                _state.update { it.copy(loading = false, error = AuthFailure.GoogleFailed) }
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

object BirthDateInput {
    private val formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu")
        .withResolverStyle(ResolverStyle.STRICT)

    fun digits(raw: String): String = sanitize(raw.filter(Char::isDigit).take(8))

    fun format(digits: String): String = buildString {
        digits.forEachIndexed { index, digit ->
            if (index == 2 || index == 4) append('/')
            append(digit)
        }
    }

    fun mask(raw: String): String = format(digits(raw))

    fun parse(value: String): LocalDate? = try {
        LocalDate.parse(value, formatter)
    } catch (_: DateTimeParseException) {
        null
    }

    fun parseDigits(digits: String): LocalDate? {
        if (digits.length != 8) return null
        return parse(format(digits))
    }

    private fun sanitize(digits: String): String {
        val out = StringBuilder()
        digits.forEach { char ->
            val digit = char.digitToInt()
            val accepted = when (out.length) {
                0 -> digit in 0..3
                1 -> digit <= if (out[0] == '3') 1 else 9
                2 -> digit in 0..1
                3 -> digit <= if (out[2] == '1') 2 else 9
                else -> true
            }
            if (accepted) out.append(char)
        }
        return out.toString()
    }
}
