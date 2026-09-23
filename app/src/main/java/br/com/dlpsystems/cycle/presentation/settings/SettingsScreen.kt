package br.com.dlpsystems.cycle.presentation.settings

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
import br.com.dlpsystems.cycle.core.config.CycleConstants
import br.com.dlpsystems.cycle.core.designsystem.AppCard
import br.com.dlpsystems.cycle.core.designsystem.PrimaryButton
import br.com.dlpsystems.cycle.core.notification.ReminderScheduler
import br.com.dlpsystems.cycle.data.local.UserPreferencesDataSource
import br.com.dlpsystems.cycle.domain.repository.BillingRepository
import br.com.dlpsystems.cycle.domain.repository.UserRepository
import br.com.dlpsystems.cycle.domain.usecase.ExportDoctorReportUseCase
import br.com.dlpsystems.cycle.presentation.components.AdBannerContainer
import br.com.dlpsystems.cycle.presentation.components.CoachMarkOverlay
import br.com.dlpsystems.cycle.presentation.components.coachRoot
import br.com.dlpsystems.cycle.presentation.components.coachTarget
import br.com.dlpsystems.cycle.presentation.components.rememberCoachMark
import kotlinx.coroutines.flow.combine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountPrefs(
    val cycleDays: Int = CycleConstants.DEFAULT_CYCLE_DAYS,
    val periodDays: Int = CycleConstants.DEFAULT_PERIOD_DAYS,
    val remindersEnabled: Boolean = true,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    billingRepository: BillingRepository,
    private val exportDoctorReport: ExportDoctorReportUseCase,
    private val userRepository: UserRepository,
    private val preferences: UserPreferencesDataSource,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {
    val isPremium = billingRepository.isPremiumUser
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
            ) { profile, reminders ->
                AccountPrefs(
                    cycleDays = profile?.averageCycleDays ?: CycleConstants.DEFAULT_CYCLE_DAYS,
                    periodDays = profile?.averagePeriodDays ?: CycleConstants.DEFAULT_PERIOD_DAYS,
                    remindersEnabled = reminders,
                )
            }.collect { snapshot ->
                val changed = _prefs.value.remindersEnabled != snapshot.remindersEnabled
                _prefs.value = snapshot
                if (changed) reminderScheduler.setEnabled(snapshot.remindersEnabled)
            }
        }
    }

    fun setReminders(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setRemindersEnabled(enabled)
            reminderScheduler.setEnabled(enabled)
        }
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
    onPaywall: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val premium by viewModel.isPremium.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }
    LaunchedEffect(prefs.remindersEnabled) {
        if (prefs.remindersEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    val coach = rememberCoachMark("account")
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .coachRoot(coach),
    ) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium)
            AveragesCard(
                cycleDays = prefs.cycleDays,
                periodDays = prefs.periodDays,
                onCycle = { viewModel.updateAverages(it, prefs.periodDays) },
                onPeriod = { viewModel.updateAverages(prefs.cycleDays, it) },
                modifier = Modifier.coachTarget(coach),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(stringResource(R.string.reminders), style = MaterialTheme.typography.titleMedium)
                Switch(checked = prefs.remindersEnabled, onCheckedChange = viewModel::setReminders)
            }
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
        }
        AdBannerContainer(isPremium = premium)
    }
    CoachMarkOverlay(
        state = coach,
        message = stringResource(R.string.coach_account),
    )
    }
}

@Composable
private fun AveragesCard(
    cycleDays: Int,
    periodDays: Int,
    onCycle: (Int) -> Unit,
    onPeriod: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.your_cycle), style = MaterialTheme.typography.titleMedium)
            Stepper(
                label = stringResource(R.string.average_cycle),
                value = cycleDays,
                onDecrease = { onCycle((cycleDays - 1).coerceAtLeast(15)) },
                onIncrease = { onCycle((cycleDays + 1).coerceAtMost(90)) },
            )
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
        Column {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(stringResource(R.string.days_value, value), style = MaterialTheme.typography.bodyMedium)
        }
        Row {
            IconButton(onClick = onDecrease) { Text(stringResource(R.string.decrease)) }
            IconButton(onClick = onIncrease) { Text(stringResource(R.string.increase)) }
        }
    }
}
