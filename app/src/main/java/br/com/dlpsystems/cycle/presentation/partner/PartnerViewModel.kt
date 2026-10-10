package br.com.dlpsystems.cycle.presentation.partner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.dlpsystems.cycle.domain.model.ActivePartnerConnection
import br.com.dlpsystems.cycle.domain.model.PartnerInvite
import br.com.dlpsystems.cycle.domain.model.PartnerShareCategory
import br.com.dlpsystems.cycle.domain.repository.HealthRecordsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PartnerUiState(
    val invites: List<PartnerInvite> = emptyList(),
    val activePartners: List<ActivePartnerConnection> = emptyList(),
    val isCreating: Boolean = false,
)

@HiltViewModel
class PartnerViewModel @Inject constructor(
    private val healthRepository: HealthRecordsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PartnerUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            healthRepository.observePartnerInvites().collect { list ->
                _state.update { it.copy(invites = list) }
            }
        }
        viewModelScope.launch {
            healthRepository.observeActivePartners().collect { list ->
                _state.update { it.copy(activePartners = list) }
            }
        }
    }

    fun createInvite(partnerEmail: String, partnerName: String, categories: Set<PartnerShareCategory>) {
        viewModelScope.launch {
            _state.update { it.copy(isCreating = true) }
            healthRepository.createPartnerInvite(partnerEmail, partnerName, categories)
            _state.update { it.copy(isCreating = false) }
        }
    }

    fun revokeAccess(inviteOrConnectionId: String) {
        viewModelScope.launch {
            healthRepository.revokePartnerAccess(inviteOrConnectionId)
        }
    }
}
