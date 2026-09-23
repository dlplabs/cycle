package br.com.dlpsystems.cycle.presentation.dashboard

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.dlpsystems.cycle.R
import br.com.dlpsystems.cycle.core.designsystem.AppCard
import br.com.dlpsystems.cycle.core.designsystem.CycleIcons
import br.com.dlpsystems.cycle.core.designsystem.CycleTheme
import br.com.dlpsystems.cycle.core.designsystem.PrimaryButton
import br.com.dlpsystems.cycle.core.notification.labelRes
import br.com.dlpsystems.cycle.domain.model.CyclePhase
import br.com.dlpsystems.cycle.domain.model.PhaseStatus
import br.com.dlpsystems.cycle.domain.model.WellnessPillar
import br.com.dlpsystems.cycle.presentation.components.CycleWheel
import br.com.dlpsystems.cycle.presentation.components.PhaseRecommendationCard
import br.com.dlpsystems.cycle.presentation.components.QuickCheckInBar
import br.com.dlpsystems.cycle.presentation.tracking.LogSymptomBottomSheet

@Composable
fun DashboardScreen(
    onOpenSettings: () -> Unit,
    onOpenSos: () -> Unit,
    onOpenPlanner: () -> Unit,
    startPeriodOnOpen: Boolean,
    onStartPeriodConsumed: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showLog by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }
    LaunchedEffect(startPeriodOnOpen) {
        if (startPeriodOnOpen) {
            viewModel.startPeriodToday()
            onStartPeriodConsumed()
        }
    }
    LaunchedEffect(state.remindersEnabled) {
        if (state.remindersEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    val phase = state.result?.phase ?: CyclePhase.LUTEAL
    CycleTheme(phase = phase) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (state.userName.isBlank()) {
                        stringResource(R.string.greeting_anonymous)
                    } else {
                        stringResource(R.string.greeting, state.userName)
                    },
                    style = MaterialTheme.typography.headlineMedium,
                )
                TextButton(onClick = onOpenSettings) {
                    Text(stringResource(R.string.open_settings))
                }
            }
            QuickCheckInBar(onSave = viewModel::saveQuickCheckIn)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onOpenSos) { Text(stringResource(R.string.open_sos)) }
                TextButton(onClick = onOpenPlanner) { Text(stringResource(R.string.open_planner)) }
            }
            val result = state.result
            AnimatedContent(targetState = result?.phase, label = "phaseCrossfade") { current ->
                val phaseName = current?.let { stringResource(it.labelRes()) }
                    ?: stringResource(R.string.phase_unknown)
                CycleWheel(
                    cycleDay = result?.cycleDay ?: 0,
                    cycleLength = result?.cycleLength ?: 28,
                    phase = current,
                    phaseName = phaseName,
                    daysRemaining = result?.daysUntilNextPeriod ?: 0,
                    segments = result?.segments.orEmpty(),
                )
            }
            when (result?.status) {
                PhaseStatus.EXTENDED -> Text(
                    text = result.message.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                )
                PhaseStatus.NO_CYCLE -> Text(
                    text = stringResource(R.string.no_cycle_hint),
                    style = MaterialTheme.typography.bodyLarge,
                )
                PhaseStatus.IN_PHASE -> Text(
                    text = stringResource(R.string.days_until_period, result.daysUntilNextPeriod),
                    style = MaterialTheme.typography.bodyLarge,
                )
                null -> Unit
            }
            result?.phase?.let { current ->
                Text(
                    text = state.insight?.physiology.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(R.string.pillars_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth(),
                )
                state.insight?.pillars?.forEach { pillar ->
                    PhaseRecommendationCard(
                        title = stringResource(pillar.pillar.labelRes()),
                        iconRes = pillar.pillar.iconRes(),
                        guidance = pillar,
                        onOpenSource = { viewModel.onOpenSource() },
                    )
                }
                Icon(
                    imageVector = current.icon(),
                    contentDescription = stringResource(R.string.cd_phase_icon, stringResource(current.labelRes())),
                )
            }
            AveragesCard(
                cycleDays = state.averageCycleDays,
                periodDays = state.averagePeriodDays,
                onCycle = { viewModel.updateAverages(it, state.averagePeriodDays) },
                onPeriod = { viewModel.updateAverages(state.averageCycleDays, it) },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(stringResource(R.string.reminders), style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = state.remindersEnabled,
                    onCheckedChange = viewModel::setReminders,
                )
            }
            PrimaryButton(
                text = stringResource(R.string.register_today),
                onClick = { showLog = true },
            )
            state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Spacer(modifier = Modifier.height(12.dp))
        }
        if (showLog) {
            LogSymptomBottomSheet(onDismiss = { showLog = false })
        }
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
            IconButton(onClick = onDecrease) {
                Text(stringResource(R.string.decrease))
            }
            IconButton(onClick = onIncrease) {
                Text(stringResource(R.string.increase))
            }
        }
    }
}

private fun CyclePhase.icon() = when (this) {
    CyclePhase.MENSTRUAL -> CycleIcons.Menstrual
    CyclePhase.FOLLICULAR -> CycleIcons.Follicular
    CyclePhase.OVULATORY -> CycleIcons.Ovulatory
    CyclePhase.LUTEAL -> CycleIcons.Luteal
}

private fun WellnessPillar.iconRes(): Int = when (this) {
    WellnessPillar.NUTRITION -> R.drawable.ic_alimentacao
    WellnessPillar.EXERCISE -> R.drawable.ic_yoga
    WellnessPillar.SKIN -> R.drawable.ic_skincare
    WellnessPillar.MIND -> R.drawable.ic_lua_lavanda
}

private fun WellnessPillar.labelRes(): Int = when (this) {
    WellnessPillar.NUTRITION -> R.string.pillar_nutrition
    WellnessPillar.EXERCISE -> R.string.pillar_exercise
    WellnessPillar.SKIN -> R.string.pillar_skin
    WellnessPillar.MIND -> R.string.pillar_mind
}
