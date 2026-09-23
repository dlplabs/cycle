package br.com.dlpsystems.cycle.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.dlpsystems.cycle.R
import br.com.dlpsystems.cycle.core.accessibility.accessibleTouchTarget
import br.com.dlpsystems.cycle.core.designsystem.CycleIcons
import br.com.dlpsystems.cycle.core.designsystem.PrimaryButton
import br.com.dlpsystems.cycle.domain.model.FlowIntensity
import br.com.dlpsystems.cycle.domain.model.Symptom

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickCheckInBar(
    onSave: (FlowIntensity?, Set<Symptom>, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    var flow by remember { mutableStateOf<FlowIntensity?>(null) }
    var symptoms by remember { mutableStateOf(setOf<Symptom>()) }
    var pain by remember { mutableFloatStateOf(0f) }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.quick_checkin), style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FlowIntensity.entries.forEach { item ->
                FilterChip(
                    selected = flow == item,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        flow = item
                    },
                    label = { Text(stringResource(item.labelRes())) },
                    modifier = Modifier.accessibleTouchTarget(),
                )
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(Symptom.CRAMPS, Symptom.HEADACHE, Symptom.BLOATING, Symptom.FATIGUE).forEach { symptom ->
                SymptomChip(
                    label = stringResource(symptom.labelRes()),
                    selected = symptom in symptoms,
                    icon = CycleIcons.Mind,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        symptoms = if (symptom in symptoms) symptoms - symptom else symptoms + symptom
                    },
                )
            }
        }
        Text(stringResource(R.string.pain_level, pain.toInt()))
        Slider(
            value = pain,
            onValueChange = {
                if (it.toInt() != pain.toInt()) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                pain = it
            },
            valueRange = 0f..10f,
            steps = 9,
        )
        PrimaryButton(
            text = stringResource(R.string.quick_save),
            onClick = { onSave(flow, symptoms, pain.toInt()) },
        )
    }
}

private fun FlowIntensity.labelRes(): Int = when (this) {
    FlowIntensity.LIGHT -> R.string.flow_light
    FlowIntensity.MEDIUM -> R.string.flow_medium
    FlowIntensity.HEAVY -> R.string.flow_heavy
    FlowIntensity.NONE -> R.string.flow_none
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
