package br.com.dlpsystems.cycle.presentation.tracking

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.dlpsystems.cycle.R
import br.com.dlpsystems.cycle.core.accessibility.accessibleTouchTarget
import br.com.dlpsystems.cycle.core.designsystem.CycleIcons
import br.com.dlpsystems.cycle.core.designsystem.PrimaryButton
import br.com.dlpsystems.cycle.domain.model.FlowIntensity
import br.com.dlpsystems.cycle.domain.model.Mood
import br.com.dlpsystems.cycle.domain.model.SkinCondition
import br.com.dlpsystems.cycle.domain.model.Symptom
import br.com.dlpsystems.cycle.presentation.components.SymptomChip
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LogSymptomBottomSheet(
    onDismiss: () -> Unit,
    viewModel: TrackingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    androidx.compose.runtime.LaunchedEffect(state.saved) {
        if (state.saved) onDismiss()
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.log_title), style = MaterialTheme.typography.titleLarge)
            DateStrip(
                selected = state.date,
                onSelect = { date ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.selectDate(date)
                },
            )
            Text(stringResource(R.string.flow), style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FlowIntensity.entries.forEach { flow ->
                    FilterChip(
                        selected = state.flow == flow,
                        onClick = { viewModel.selectFlow(flow) },
                        label = { Text(stringResource(flow.labelRes())) },
                        modifier = Modifier.accessibleTouchTarget(),
                    )
                }
            }
            Text(stringResource(R.string.mood), style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Mood.entries.forEach { mood ->
                    FilterChip(
                        selected = state.mood == mood,
                        onClick = { viewModel.selectMood(mood) },
                        label = { Text(stringResource(mood.labelRes())) },
                        modifier = Modifier.accessibleTouchTarget(),
                    )
                }
            }
            Text(stringResource(R.string.skin), style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SkinCondition.entries.forEach { skin ->
                    FilterChip(
                        selected = state.skin == skin,
                        onClick = { viewModel.selectSkin(skin) },
                        label = { Text(stringResource(skin.labelRes())) },
                        modifier = Modifier.accessibleTouchTarget(),
                    )
                }
            }
            Text(stringResource(R.string.symptoms), style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Symptom.entries.forEach { symptom ->
                    SymptomChip(
                        label = stringResource(symptom.labelRes()),
                        selected = symptom in state.symptoms,
                        icon = CycleIcons.Mind,
                        onClick = { viewModel.toggleSymptom(symptom) },
                    )
                }
            }
            OutlinedTextField(
                value = state.notes,
                onValueChange = viewModel::onNotes,
                label = { Text(stringResource(R.string.notes)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.period_started), style = MaterialTheme.typography.bodyLarge)
                Switch(checked = state.periodStarted, onCheckedChange = viewModel::setPeriodStarted)
            }
            if (state.needsPeriod) {
                Text(stringResource(R.string.need_period), color = MaterialTheme.colorScheme.error)
            }
            state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            PrimaryButton(
                text = stringResource(R.string.log_save),
                enabled = !state.saving,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.save()
                },
            )
        }
    }
}

@Composable
private fun DateStrip(selected: LocalDate, onSelect: (LocalDate) -> Unit) {
    val today = LocalDate.now()
    val days = (0..13).map { today.minusDays(it.toLong()) }.reversed()
    val formatter = DateTimeFormatter.ofPattern("dd/MM")
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        days.forEach { day ->
            FilterChip(
                selected = day == selected,
                onClick = { onSelect(day) },
                label = { Text(day.format(formatter)) },
                modifier = Modifier.accessibleTouchTarget(),
            )
        }
    }
}

private fun FlowIntensity.labelRes(): Int = when (this) {
    FlowIntensity.LIGHT -> R.string.flow_light
    FlowIntensity.MEDIUM -> R.string.flow_medium
    FlowIntensity.HEAVY -> R.string.flow_heavy
    FlowIntensity.NONE -> R.string.flow_none
}

private fun Mood.labelRes(): Int = when (this) {
    Mood.CALM -> R.string.mood_calm
    Mood.ENERGETIC -> R.string.mood_energetic
    Mood.TIRED -> R.string.mood_tired
    Mood.ANXIOUS -> R.string.mood_anxious
    Mood.IRRITABLE -> R.string.mood_irritable
    Mood.SENSITIVE -> R.string.mood_sensitive
}

private fun SkinCondition.labelRes(): Int = when (this) {
    SkinCondition.BALANCED -> R.string.skin_balanced
    SkinCondition.DRY -> R.string.skin_dry
    SkinCondition.OILY -> R.string.skin_oily
    SkinCondition.BREAKOUT -> R.string.skin_breakout
    SkinCondition.SENSITIVE -> R.string.skin_sensitive
}

private fun Symptom.labelRes(): Int = when (this) {
    Symptom.CRAMPS -> R.string.symptom_cramps
    Symptom.HEADACHE -> R.string.symptom_headache
    Symptom.BLOATING -> R.string.symptom_bloating
    Symptom.FATIGUE -> R.string.symptom_fatigue
    Symptom.BREAST_TENDERNESS -> R.string.symptom_breast
    Symptom.BACK_PAIN -> R.string.symptom_back
    Symptom.NAUSEA -> R.string.symptom_nausea
    Symptom.CRAVINGS -> R.string.symptom_cravings
}
