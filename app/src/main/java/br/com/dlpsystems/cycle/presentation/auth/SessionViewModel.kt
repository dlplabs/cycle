package br.com.dlpsystems.cycle.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.dlpsystems.cycle.data.local.UserPreferencesDataSource
import br.com.dlpsystems.cycle.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionUiState(
    val loading: Boolean = true,
    val userId: String? = null,
    val disclaimerAccepted: Boolean = false,
)

@HiltViewModel
class SessionViewModel @Inject constructor(
    userRepository: UserRepository,
    private val preferences: UserPreferencesDataSource,
) : ViewModel() {
    val state = combine(
        userRepository.observeAuth(),
        preferences.disclaimerAccepted,
    ) { user, accepted ->
        SessionUiState(
            loading = false,
            userId = user?.id,
            disclaimerAccepted = accepted,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SessionUiState(),
    )

    fun acceptDisclaimer() {
        viewModelScope.launch { preferences.setDisclaimerAccepted(true) }
    }
}
