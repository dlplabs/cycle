package br.com.dlpsystems.cycle.presentation.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import br.com.dlpsystems.cycle.BuildConfig
import br.com.dlpsystems.cycle.R
import br.com.dlpsystems.cycle.core.config.CycleConstants
import br.com.dlpsystems.cycle.core.designsystem.AppCard
import br.com.dlpsystems.cycle.core.designsystem.DeepPlum
import br.com.dlpsystems.cycle.core.designsystem.PrimaryButton
import br.com.dlpsystems.cycle.core.notification.ReminderScheduler
import br.com.dlpsystems.cycle.data.local.UserPreferencesDataSource
import br.com.dlpsystems.cycle.domain.model.AuthFailure
import br.com.dlpsystems.cycle.domain.repository.BillingRepository
import br.com.dlpsystems.cycle.domain.repository.UserRepository
import br.com.dlpsystems.cycle.domain.usecase.ExportDoctorReportUseCase
import br.com.dlpsystems.cycle.presentation.components.AdBannerContainer
import br.com.dlpsystems.cycle.presentation.components.ScreenHeader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class AccountPrefs(
    val cycleDays: Int = CycleConstants.DEFAULT_CYCLE_DAYS,
    val periodDays: Int = CycleConstants.DEFAULT_PERIOD_DAYS,
    val remindersEnabled: Boolean = true,
    val cycleRemindersEnabled: Boolean = true,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    billingRepository: BillingRepository,
    private val exportDoctorReport: ExportDoctorReportUseCase,
    private val userRepository: UserRepository,
    private val preferences: UserPreferencesDataSource,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {
    val isPremium: StateFlow<Boolean> = billingRepository.isPremiumUser
    val authUser = userRepository.observeAuth()
    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()
    private val _prefs = MutableStateFlow(AccountPrefs())
    val prefs = _prefs.asStateFlow()

    init {
        billingRepository.connect()
        viewModelScope.launch {
            combine(
                userRepository.observeProfile(),
                preferences.remindersEnabled,
                preferences.cycleRemindersEnabled,
            ) { profile, dailyReminders, cycleReminders ->
                AccountPrefs(
                    cycleDays = profile?.averageCycleDays ?: CycleConstants.DEFAULT_CYCLE_DAYS,
                    periodDays = profile?.averagePeriodDays ?: CycleConstants.DEFAULT_PERIOD_DAYS,
                    remindersEnabled = dailyReminders,
                    cycleRemindersEnabled = cycleReminders,
                )
            }.collect { snapshot ->
                val changed = _prefs.value.remindersEnabled != snapshot.remindersEnabled ||
                    _prefs.value.cycleRemindersEnabled != snapshot.cycleRemindersEnabled
                _prefs.value = snapshot
                if (changed) reminderScheduler.setEnabled(snapshot.remindersEnabled || snapshot.cycleRemindersEnabled)
            }
        }
    }

    fun setDailyReminders(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setRemindersEnabled(enabled)
            reminderScheduler.setEnabled(enabled || _prefs.value.cycleRemindersEnabled)
        }
    }

    fun setCycleReminders(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setCycleRemindersEnabled(enabled)
            reminderScheduler.setEnabled(_prefs.value.remindersEnabled || enabled)
        }
    }

    fun setReminders(enabled: Boolean) {
        setDailyReminders(enabled)
    }

    fun updateAverages(cycleDays: Int, periodDays: Int) {
        viewModelScope.launch {
            runCatching { userRepository.updateAverages(cycleDays, periodDays) }
                .onFailure { error -> _message.value = error.message }
        }
    }

    fun signOut() {
        viewModelScope.launch { userRepository.signOut() }
    }

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting = _isDeleting.asStateFlow()

    fun deleteAccount(onDeleted: () -> Unit) {
        viewModelScope.launch {
            _isDeleting.value = true
            try {
                userRepository.deleteAccount()
                preferences.clear()
                reminderScheduler.setEnabled(false)
                _isDeleting.value = false
                onDeleted()
            } catch (_: AuthFailure.RequiresRecentLogin) {
                _isDeleting.value = false
                _message.value = "requires_recent_login"
            } catch (error: Throwable) {
                _isDeleting.value = false
                _message.value = error.message ?: "unknown"
            }
        }
    }

    fun export(onFile: (File) -> Unit) {
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
    onBack: () -> Unit = {},
    onLogin: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val premium by viewModel.isPremium.collectAsStateWithLifecycle()
    val authUser by viewModel.authUser.collectAsStateWithLifecycle(initialValue = null)
    val isGuest = authUser == null
    val message by viewModel.message.collectAsStateWithLifecycle()
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    val isDeleting by viewModel.isDeleting.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    LaunchedEffect(prefs.remindersEnabled) {
        if (prefs.remindersEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_account_confirm_title)) },
            text = { Text(stringResource(R.string.delete_account_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteAccount {}
                    },
                ) {
                    Text(
                        stringResource(R.string.delete_account_confirm_button),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = stringResource(R.string.settings_title),
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Seção Meu Ciclo
            Text(
                text = stringResource(R.string.your_cycle),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepPlum,
            )
            AveragesCard(
                cycleDays = prefs.cycleDays,
                periodDays = prefs.periodDays,
                onCycle = { viewModel.updateAverages(it, prefs.periodDays) },
                onPeriod = { viewModel.updateAverages(prefs.cycleDays, it) },
            )

            // Seção Notificações
            Text(
                text = "Notificações",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepPlum,
            )
            AppCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.reminders),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Lembrete diário para registrar sintomas e fase",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = prefs.remindersEnabled,
                        onCheckedChange = viewModel::setReminders,
                    )
                }
            }

            // Seção Geral & Utilidades
            Text(
                text = "Geral e Relatórios",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepPlum,
            )
            AppCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
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
                        Text(
                            text = stringResource(R.string.export_locked),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else if (message == "requires_recent_login") {
                        Text(
                            text = stringResource(R.string.error_requires_recent_login),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            // Seção Conta / Sessão
            Text(
                text = "Sua Conta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepPlum,
            )
            if (isGuest) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.guest_account_notice),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        PrimaryButton(
                            text = stringResource(R.string.welcome_login),
                            onClick = onLogin,
                        )
                    }
                }
            } else {
                OutlinedButton(
                    onClick = viewModel::signOut,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.sign_out))
                }

                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    enabled = !isDeleting,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(
                        if (isDeleting) stringResource(R.string.deleting_account)
                        else stringResource(R.string.delete_account),
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Rodapé
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Cycle v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        AdBannerContainer(isPremium = premium)
    }
}

@Composable
private fun AveragesCard(
    cycleDays: Int,
    periodDays: Int,
    onCycle: (Int) -> Unit,
    onPeriod: (Int) -> Unit,
) {
    AppCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Stepper(
                label = stringResource(R.string.average_cycle),
                value = cycleDays,
                onDecrease = { onCycle((cycleDays - 1).coerceAtLeast(15)) },
                onIncrease = { onCycle((cycleDays + 1).coerceAtMost(90)) },
            )
            HorizontalDivider()
            Stepper(
                label = stringResource(R.string.average_period),
                value = periodDays,
                onDecrease = { onPeriod((periodDays - 1).coerceAtLeast(1)) },
                onIncrease = { onPeriod((periodDays + 1).coerceAtMost(12)) },
            )
        }
    }
}

@Composable
private fun Stepper(
    label: String,
    value: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.days_value, value),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedIconButton(onClick = onDecrease) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Diminuir",
                    tint = DeepPlum,
                )
            }
            Text(
                text = "$value",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepPlum,
            )
            OutlinedIconButton(onClick = onIncrease) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Aumentar",
                    tint = DeepPlum,
                )
            }
        }
    }
}
