package br.com.dlpsystems.cycle.presentation.settings

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import br.com.dlpsystems.cycle.R
import br.com.dlpsystems.cycle.core.designsystem.PrimaryButton
import br.com.dlpsystems.cycle.domain.repository.BillingRepository
import br.com.dlpsystems.cycle.domain.repository.UserRepository
import br.com.dlpsystems.cycle.domain.usecase.ExportDoctorReportUseCase
import br.com.dlpsystems.cycle.presentation.components.AdBannerContainer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    billingRepository: BillingRepository,
    private val exportDoctorReport: ExportDoctorReportUseCase,
    private val userRepository: UserRepository,
) : ViewModel() {
    val isPremium = billingRepository.isPremiumUser
    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    init {
        billingRepository.connect()
    }

    fun signOut() {
        viewModelScope.launch { userRepository.signOut() }
    }

    fun export(onFile: (java.io.File) -> Unit) {
        viewModelScope.launch {
            val file = exportDoctorReport()
            if (file == null) {
                _message.value = "premium"
            } else {
                _message.value = null
                onFile(file)
            }
        }
    }
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onPaywall: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val premium by viewModel.isPremium.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.weight(1f).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium)
            PrimaryButton(text = stringResource(R.string.open_paywall), onClick = onPaywall)
            PrimaryButton(
                text = stringResource(R.string.export_pdf),
                onClick = {
                    viewModel.export { file ->
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file,
                        )
                        val share = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(share, null))
                    }
                },
            )
            if (message == "premium") {
                Text(stringResource(R.string.export_locked))
            }
            PrimaryButton(text = stringResource(R.string.sign_out), onClick = viewModel::signOut)
            PrimaryButton(text = stringResource(R.string.back), onClick = onBack)
        }
        AdBannerContainer(isPremium = premium)
    }
}
