package br.com.dlpsystems.cycle.presentation.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Circle
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
import androidx.compose.ui.draw.clip
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
import br.com.dlpsystems.cycle.domain.model.Citation
import br.com.dlpsystems.cycle.domain.model.LifeStageMode
import br.com.dlpsystems.cycle.domain.repository.BillingRepository
import br.com.dlpsystems.cycle.domain.repository.UserRepository
import br.com.dlpsystems.cycle.domain.usecase.ExportDoctorReportUseCase
import br.com.dlpsystems.cycle.presentation.components.AdBannerContainer
import br.com.dlpsystems.cycle.presentation.components.ScientificSourceBadge
import br.com.dlpsystems.cycle.presentation.components.ScreenHeader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
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

    val activeLifeStageMode: StateFlow<LifeStageMode> = preferences.activeLifeStageMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LifeStageMode.REGULAR_CYCLE,
        )

    fun setLifeStageMode(mode: LifeStageMode) {
        viewModelScope.launch {
            preferences.setActiveLifeStageMode(mode)
        }
    }

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
    val activeLifeStage by viewModel.activeLifeStageMode.collectAsStateWithLifecycle()
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
            // Seção Estágio de Vida & Jornada da Mulher
            Text(
                text = "Fase de Vida Atual",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepPlum,
            )
            LifeStageSelectorCard(
                activeStage = activeLifeStage,
                onSelectStage = viewModel::setLifeStageMode,
            )

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

@Composable
private fun LifeStageSelectorCard(
    activeStage: LifeStageMode,
    onSelectStage: (LifeStageMode) -> Unit,
) {
    val stages = listOf(
        LifeStageOption(
            mode = LifeStageMode.REGULAR_CYCLE,
            title = "Ciclo Menstrual Regular",
            description = "Acompanhamento fisiológico das 4 fases hormonais do ciclo.",
            citation = Citation(
                authors = "Lenton et al.",
                source = "Clin Endocrinol (N > 1.000)",
                finding = "Variação e previsibilidade das fases folicular e lútea em ciclos ovulatórios normais.",
                url = "https://doi.org/10.1111/j.1365-2265.1984.tb03457.x",
            ),
        ),
        LifeStageOption(
            mode = LifeStageMode.TRYING_TO_CONCEIVE,
            title = "Tentante (TTC)",
            description = "Foco em janela fértil, método sintotérmico e chances diárias de concepção.",
            citation = Citation(
                authors = "Dunson et al. / Sensiplan",
                source = "Hum Reprod (N = 1.077)",
                finding = "Probabilidades de concepção dia a dia da janela fértil e eficácia de regras sintotérmicas.",
                url = "https://doi.org/10.1093/humrep/17.5.1399",
            ),
        ),
        LifeStageOption(
            mode = LifeStageMode.PREGNANCY,
            title = "Gestação",
            description = "Evolução semana a semana, trimestres, contagem de chutes e contrações.",
            citation = Citation(
                authors = "Cardiff / Hadlock et al.",
                source = "Lancet / Radiology (N = 68.654)",
                finding = "Contador de chutes Cardiff Count-to-10 e datação biométrica ultrassonográfica.",
                url = "https://doi.org/10.1016/S0140-6736(89)90370-3",
            ),
        ),
        LifeStageOption(
            mode = LifeStageMode.POSTPARTUM,
            title = "Pós-Parto (Puerpério)",
            description = "Recuperação puerperal, involução uterina, lóquios e triagem de depressão EPDS.",
            citation = Citation(
                authors = "Cox et al. / Pelotas",
                source = "Br J Psychiatry / J Affect Disord (N = 4.453)",
                finding = "Escala de Depressão Pós-Parto de Edimburgo (EPDS) com validação psicométrica.",
                url = "https://doi.org/10.1192/bjp.150.6.782",
            ),
        ),
        LifeStageOption(
            mode = LifeStageMode.LACTATION,
            title = "Amamentação & Lactação",
            description = "Cronômetro de mamadas, conservação de leite materno e critérios de MELA.",
            citation = Citation(
                authors = "Bellagio / Fiocruz rBLH",
                source = "Lancet (N = 4.118; rBLH N > 160.000/ano)",
                finding = "Amenorreia da Lactação (MELA) 98% eficaz e protocolos térmicos da Rede Brasileira de Leite Humano.",
                url = "https://doi.org/10.1016/0140-6736(88)90753-6",
            ),
        ),
        LifeStageOption(
            mode = LifeStageMode.PERIMENOPAUSE,
            title = "Perimenopausa (Climatério)",
            description = "Monitoramento de irregularidade menstrual, fogachos e escore de sintomas MRS.",
            citation = Citation(
                authors = "STRAW+10 / SWAN",
                source = "Menopause / Am J Epidemiol (N > 10.000)",
                finding = "Critérios de estadiamento reprodutivo de Harlow et al. e prevalência de sintomas vasomotores.",
                url = "https://doi.org/10.1097/gme.0b013e31824d8f40",
            ),
        ),
        LifeStageOption(
            mode = LifeStageMode.MENOPAUSE,
            title = "Pós-Menopausa & Longevidade",
            description = "Amenorreia consolidada de 12 meses, saúde óssea e alerta de sangramento atípico.",
            citation = Citation(
                authors = "Million Women Study / WHI",
                source = "Lancet / JAMA (N = 1.388.536)",
                finding = "Alerta protetor para sangramento pós-menopausa e prevenção osteoporótica com cálcio/vitamina D.",
                url = "https://doi.org/10.1016/S0140-6736(03)14596-5",
            ),
        ),
    )

    AppCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            stages.forEach { stage ->
                val isSelected = stage.mode == activeStage
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectStage(stage.mode) }
                        .padding(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stage.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isSelected) DeepPlum else MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = stage.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(
                            imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                            contentDescription = if (isSelected) "Selecionado" else null,
                            tint = if (isSelected) DeepPlum else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    if (isSelected) {
                        ScientificSourceBadge(
                            citation = stage.citation,
                            onOpen = {},
                        )
                    }
                }
                if (stage != stages.last()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

private data class LifeStageOption(
    val mode: LifeStageMode,
    val title: String,
    val description: String,
    val citation: Citation,
)

