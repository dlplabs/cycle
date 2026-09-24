package br.com.dlpsystems.cycle.presentation.tracking

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.dlpsystems.cycle.R
import br.com.dlpsystems.cycle.core.accessibility.accessibleTouchTarget
import br.com.dlpsystems.cycle.core.designsystem.DeepPlum
import br.com.dlpsystems.cycle.core.designsystem.HeaderSage
import br.com.dlpsystems.cycle.core.designsystem.OffWhiteBackground
import br.com.dlpsystems.cycle.core.designsystem.PlayfairDisplay
import br.com.dlpsystems.cycle.core.designsystem.PrimaryButton
import br.com.dlpsystems.cycle.core.designsystem.SurfaceCard
import br.com.dlpsystems.cycle.core.designsystem.TextPrimary
import br.com.dlpsystems.cycle.core.designsystem.TextSecondary
import br.com.dlpsystems.cycle.domain.model.FlowIntensity
import br.com.dlpsystems.cycle.domain.model.Mood
import br.com.dlpsystems.cycle.domain.model.SkinCondition
import br.com.dlpsystems.cycle.domain.model.Symptom
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LogSymptomBottomSheet(
    onDismiss: () -> Unit,
    initialDate: LocalDate = LocalDate.now(),
    viewModel: TrackingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(initialDate) {
        viewModel.selectDate(initialDate)
    }

    LaunchedEffect(state.saved) {
        if (state.saved) onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = OffWhiteBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f),
        ) {
            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Top Header: Title & Current Date in Focus
                val today = LocalDate.now()
                val isToday = state.date == today
                Column(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.log_title),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = PlayfairDisplay,
                                fontWeight = FontWeight.Normal,
                                color = DeepPlum,
                            ),
                        )
                        if (!isToday) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = DeepPlum,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.selectDate(today)
                                    },
                            ) {
                                Text(
                                    text = "Ir para Hoje",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                )
                            }
                        }
                    }

                    // Card em destaque: Data Atual em Foco
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isToday) HeaderSage.copy(alpha = 0.35f) else OffWhiteBackground,
                        border = BorderStroke(1.dp, if (isToday) DeepPlum.copy(alpha = 0.2f) else HeaderSage.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isToday) DeepPlum else HeaderSage.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = if (isToday) "✨" else "📅",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                            Column {
                                Text(
                                    text = if (isToday) "Data Atual em Foco" else "Data do Registro",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DeepPlum.copy(alpha = 0.7f),
                                    ),
                                )
                                Text(
                                    text = formatSelectedDateSubtitle(state.date),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = DeepPlum,
                                    ),
                                )
                            }
                        }
                    }
                }

                // Date Strip Picker at the Top
                DateStrip(
                    selected = state.date,
                    onSelect = { date ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.selectDate(date)
                    },
                )

                // Section 1: Fluxo Menstrual
                TrackingSectionCard(
                    title = stringResource(R.string.flow),
                    iconEmoji = "🩸",
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(HeaderSage.copy(alpha = 0.15f))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.period_started),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = DeepPlum,
                                    ),
                                )
                                Text(
                                    text = "Marca o início de um novo ciclo",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary,
                                    ),
                                )
                            }
                            Switch(
                                checked = state.periodStarted,
                                onCheckedChange = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.setPeriodStarted(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = DeepPlum,
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = HeaderSage.copy(alpha = 0.4f),
                                ),
                            )
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FlowIntensity.entries.forEach { flow ->
                                WellnessChip(
                                    label = stringResource(flow.labelRes()),
                                    emoji = flow.emoji(),
                                    selected = state.flow == flow,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.selectFlow(flow)
                                    },
                                )
                            }
                        }
                    }
                }

                // Section 2: Humor
                TrackingSectionCard(
                    title = stringResource(R.string.mood),
                    iconEmoji = "✨",
                    badge = state.mood?.let { stringResource(it.labelRes()) },
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Mood.entries.forEach { mood ->
                            WellnessChip(
                                label = stringResource(mood.labelRes()),
                                emoji = mood.emoji(),
                                selected = state.mood == mood,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.selectMood(mood)
                                },
                            )
                        }
                    }
                }

                // Section 3: Sintomas do Corpo
                TrackingSectionCard(
                    title = stringResource(R.string.symptoms),
                    iconEmoji = "🩺",
                    badge = if (state.symptoms.isNotEmpty()) "${state.symptoms.size} marcados" else null,
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Symptom.entries.forEach { symptom ->
                            WellnessChip(
                                label = stringResource(symptom.labelRes()),
                                emoji = symptom.emoji(),
                                selected = symptom in state.symptoms,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.toggleSymptom(symptom)
                                },
                            )
                        }
                    }
                }

                // Section 4: Pele
                TrackingSectionCard(
                    title = stringResource(R.string.skin),
                    iconEmoji = "🌿",
                    badge = state.skin?.let { stringResource(it.labelRes()) },
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SkinCondition.entries.forEach { skin ->
                            WellnessChip(
                                label = stringResource(skin.labelRes()),
                                emoji = skin.emoji(),
                                selected = state.skin == skin,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.selectSkin(skin)
                                },
                            )
                        }
                    }
                }

                // Section 5: Notas
                TrackingSectionCard(
                    title = stringResource(R.string.notes),
                    iconEmoji = "📝",
                ) {
                    OutlinedTextField(
                        value = state.notes,
                        onValueChange = viewModel::onNotes,
                        placeholder = {
                            Text(
                                "Como foi o seu dia? Sentiu algo diferente?",
                                color = TextSecondary.copy(alpha = 0.7f),
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DeepPlum,
                            unfocusedBorderColor = HeaderSage.copy(alpha = 0.5f),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                        ),
                    )
                }

                // Feedback Errors
                if (state.needsPeriod) {
                    Surface(
                        color = Color(0xFFFBEAEA),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = stringResource(R.string.need_period),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(14.dp),
                        )
                    }
                }

                state.errorMessage?.let { errorMsg ->
                    Surface(
                        color = Color(0xFFFBEAEA),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(14.dp),
                        )
                    }
                }

                // Extra breathing space at bottom of scroll
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Sticky Bottom Save Button
            Surface(
                color = OffWhiteBackground,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
            ) {
                HorizontalDivider(color = HeaderSage.copy(alpha = 0.3f), thickness = 1.dp)
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                    PrimaryButton(
                        text = if (state.saving) "Salvando..." else stringResource(R.string.log_save),
                        enabled = !state.saving,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.save()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackingSectionCard(
    title: String,
    iconEmoji: String,
    badge: String? = null,
    content: @Composable () -> Unit,
) {
    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, HeaderSage.copy(alpha = 0.35f)),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(text = iconEmoji, fontSize = 18.sp)
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = DeepPlum,
                        ),
                    )
                }
                if (badge != null) {
                    Surface(
                        color = HeaderSage.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = DeepPlum,
                                fontWeight = FontWeight.Medium,
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }
            content()
        }
    }
}

@Composable
private fun WellnessChip(
    label: String,
    emoji: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) DeepPlum.copy(alpha = 0.12f) else HeaderSage.copy(alpha = 0.1f),
        animationSpec = tween(durationMillis = 150),
        label = "chipBg",
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) DeepPlum else HeaderSage.copy(alpha = 0.4f),
        animationSpec = tween(durationMillis = 150),
        label = "chipBorder",
    )

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, borderColor),
        modifier = modifier
            .accessibleTouchTarget()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
        ) {
            Text(text = emoji, fontSize = 16.sp)
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) DeepPlum else TextPrimary,
                ),
            )
        }
    }
}

@Composable
private fun DateStrip(
    selected: LocalDate,
    onSelect: (LocalDate) -> Unit,
) {
    val today = LocalDate.now()
    val days = (0..13).map { today.minusDays(it.toLong()) }.reversed()
    val localePt = Locale.forLanguageTag("pt-BR")
    val scrollState = rememberScrollState()

    LaunchedEffect(scrollState.maxValue) {
        if (scrollState.maxValue > 0 && selected == today) {
            scrollState.scrollTo(scrollState.maxValue)
        }
    }

    LaunchedEffect(selected) {
        if (selected == today && scrollState.maxValue > 0) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        days.forEach { day ->
            val isSelected = day == selected
            val isToday = day == today
            val weekDayName = if (isToday) {
                "HOJE"
            } else {
                day.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, localePt)
                    .uppercase()
                    .take(3)
                    .replace(".", "")
            }

            val bgColor = when {
                isSelected -> DeepPlum
                isToday -> HeaderSage.copy(alpha = 0.25f)
                else -> SurfaceCard
            }
            val textColor = when {
                isSelected -> Color.White
                else -> DeepPlum
            }
            val borderColor = when {
                isSelected -> DeepPlum
                isToday -> DeepPlum.copy(alpha = 0.5f)
                else -> HeaderSage.copy(alpha = 0.5f)
            }

            Surface(
                color = bgColor,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(if (isToday && !isSelected) 1.5.dp else 1.dp, borderColor),
                shadowElevation = if (isSelected) 3.dp else 0.dp,
                modifier = Modifier
                    .accessibleTouchTarget()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSelect(day) },
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = weekDayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = when {
                                isSelected -> Color.White.copy(alpha = 0.9f)
                                isToday -> DeepPlum
                                else -> TextSecondary
                            },
                        ),
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = day.dayOfMonth.toString(),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                        ),
                    )
                    if (isToday) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color.White else DeepPlum),
                        )
                    }
                }
            }
        }
    }
}

private fun formatSelectedDateSubtitle(date: LocalDate): String {
    val today = LocalDate.now()
    val localePt = Locale.forLanguageTag("pt-BR")
    val formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", localePt)
    val formatted = date.format(formatter).replaceFirstChar { it.uppercase() }

    return when (date) {
        today -> "Hoje • $formatted"
        today.minusDays(1) -> "Ontem • $formatted"
        else -> formatted
    }
}

private fun Mood.emoji(): String = when (this) {
    Mood.CALM -> "🧘‍♀️"
    Mood.ENERGETIC -> "⚡"
    Mood.TIRED -> "🥱"
    Mood.ANXIOUS -> "💭"
    Mood.IRRITABLE -> "💢"
    Mood.SENSITIVE -> "🌸"
}

private fun Symptom.emoji(): String = when (this) {
    Symptom.CRAMPS -> "⚡"
    Symptom.HEADACHE -> "🤕"
    Symptom.BLOATING -> "🎈"
    Symptom.FATIGUE -> "🔋"
    Symptom.BREAST_TENDERNESS -> "🌸"
    Symptom.BACK_PAIN -> "🦴"
    Symptom.NAUSEA -> "🤢"
    Symptom.CRAVINGS -> "🍫"
}

private fun SkinCondition.emoji(): String = when (this) {
    SkinCondition.BALANCED -> "✨"
    SkinCondition.DRY -> "🏜️"
    SkinCondition.OILY -> "💧"
    SkinCondition.BREAKOUT -> "🔴"
    SkinCondition.SENSITIVE -> "🧴"
}

private fun FlowIntensity.emoji(): String = when (this) {
    FlowIntensity.NONE -> "⚪"
    FlowIntensity.LIGHT -> "💧"
    FlowIntensity.MEDIUM -> "💧💧"
    FlowIntensity.HEAVY -> "💧💧💧"
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
