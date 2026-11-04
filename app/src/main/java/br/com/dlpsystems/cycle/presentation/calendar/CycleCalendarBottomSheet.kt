package br.com.dlpsystems.cycle.presentation.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import br.com.dlpsystems.cycle.R
import br.com.dlpsystems.cycle.core.accessibility.accessibleTouchTarget
import br.com.dlpsystems.cycle.core.config.CycleConstants
import br.com.dlpsystems.cycle.core.designsystem.DeepPlum
import br.com.dlpsystems.cycle.core.designsystem.FollicularSage
import br.com.dlpsystems.cycle.core.designsystem.HeaderSage
import br.com.dlpsystems.cycle.core.designsystem.LutealLavender
import br.com.dlpsystems.cycle.core.designsystem.MenstrualTerracotta
import br.com.dlpsystems.cycle.core.designsystem.OffWhiteBackground
import br.com.dlpsystems.cycle.core.designsystem.OvulatoryPeach
import br.com.dlpsystems.cycle.core.designsystem.PlayfairDisplay
import br.com.dlpsystems.cycle.core.designsystem.PrimaryButton
import br.com.dlpsystems.cycle.core.designsystem.SurfaceCard
import br.com.dlpsystems.cycle.core.designsystem.TextPrimary
import br.com.dlpsystems.cycle.core.designsystem.TextSecondary
import br.com.dlpsystems.cycle.domain.model.CyclePhase
import br.com.dlpsystems.cycle.domain.model.DailyLog
import br.com.dlpsystems.cycle.domain.model.MenstrualCycle
import br.com.dlpsystems.cycle.domain.usecase.classifyDay
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

data class CalendarDayState(
    val date: LocalDate,
    val phase: CyclePhase?,
    val cycleDay: Int?,
    val isMenstrual: Boolean,
    val isOvulationPeak: Boolean,
    val isToday: Boolean,
    val isPastOrToday: Boolean,
    val hasLog: Boolean,
    val dailyLog: DailyLog?,
    val isPredicted: Boolean,
)

data class NextPeriodInfo(
    val expectedDate: LocalDate,
    val daysRemaining: Int,
    val isDelayed: Boolean,
    val isToday: Boolean,
    val cycleLength: Int,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CycleCalendarBottomSheet(
    onDismiss: () -> Unit,
    cycles: List<MenstrualCycle>,
    dailyLogs: List<DailyLog>,
    averageCycleDays: Int,
    averagePeriodDays: Int,
    onLogSymptomOnDate: (LocalDate) -> Unit,
    onStartPeriodOnDate: (LocalDate) -> Unit,
    onTogglePeriodOnDate: (LocalDate) -> Unit = onStartPeriodOnDate,
    onSetFlowEndDate: (String, LocalDate?) -> Unit = { _, _ -> },
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val today = remember { LocalDate.now() }
    val baseMonth = remember { YearMonth.now() }
    val pageCount = 2400
    val initialPage = 1200
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { pageCount })
    val coroutineScope = rememberCoroutineScope()

    // Inversão estrita solicitada pela regra de UX:
    // Swipe esquerda -> avança página -> offset negativo -> meses anteriores (passado)
    // Swipe direita -> volta página -> offset positivo -> meses posteriores (futuro)
    val currentMonth = remember(pagerState.currentPage) {
        baseMonth.plusMonths((initialPage - pagerState.currentPage).toLong())
    }

    var selectedDate by remember { mutableStateOf(today) }
    var showConfirmStartPeriod by remember { mutableStateOf<LocalDate?>(null) }
    val haptic = LocalHapticFeedback.current
    val localePt = remember { Locale.forLanguageTag("pt-BR") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = OffWhiteBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.94f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header: Title & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.calendar_title),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = PlayfairDisplay,
                            fontWeight = FontWeight.Bold,
                            color = DeepPlum,
                        ),
                    )
                    Text(
                        text = "Previsões e histórico completo do ciclo",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = DeepPlum,
                    )
                }
            }

            // Month Navigation Bar
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceCard,
                border = BorderStroke(1.dp, HeaderSage.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Mês anterior",
                            tint = DeepPlum,
                        )
                    }

                    val monthTitle = currentMonth.format(DateTimeFormatter.ofPattern("MMMM 'de' yyyy", localePt))
                        .replaceFirstChar { it.uppercase() }

                    Text(
                        text = monthTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepPlum,
                        ),
                    )

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Próximo mês",
                            tint = DeepPlum,
                        )
                    }
                }
            }

            // Next Period Prediction Card
            val nextPeriodInfo = remember(today, cycles, averageCycleDays) {
                calculateNextPeriodPrediction(today, cycles, averageCycleDays)
            }

            if (nextPeriodInfo != null) {
                NextPeriodPredictionCard(
                    info = nextPeriodInfo,
                    localePt = localePt,
                    onNavigateToExpectedDate = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        val diffMonths = ChronoUnit.MONTHS.between(baseMonth, YearMonth.from(nextPeriodInfo.expectedDate)).toInt()
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(initialPage - diffMonths)
                        }
                        selectedDate = nextPeriodInfo.expectedDate
                    },
                )
            }

            // Phase Legend
            PhaseLegendRow()

            // Days of Week Header
            DaysOfWeekHeader(localePt)

            // Monthly Calendar Grid with HorizontalPager
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                modifier = Modifier.fillMaxWidth(),
            ) { page ->
                val monthForPage = remember(page) {
                    baseMonth.plusMonths((initialPage - page).toLong())
                }
                CalendarMonthGrid(
                    yearMonth = monthForPage,
                    selectedDate = selectedDate,
                    today = today,
                    cycles = cycles,
                    dailyLogs = dailyLogs,
                    averageCycleDays = averageCycleDays,
                    averagePeriodDays = averagePeriodDays,
                    onDateSelected = { date ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedDate = date
                    },
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = HeaderSage.copy(alpha = 0.3f),
            )

            // Selected Day Detail Card
            val selectedDayState = remember(selectedDate, cycles, dailyLogs, currentMonth) {
                computeDayState(
                    date = selectedDate,
                    today = today,
                    cycles = cycles,
                    dailyLogs = dailyLogs,
                    averageCycleDays = averageCycleDays,
                    averagePeriodDays = averagePeriodDays,
                )
            }

            SelectedDayCard(
                state = selectedDayState,
                nextPeriodInfo = nextPeriodInfo,
                cycles = cycles,
                today = today,
                averagePeriodDays = averagePeriodDays,
                localePt = localePt,
                onLogSymptom = {
                    onLogSymptomOnDate(selectedDate)
                    onDismiss()
                },
                onStartPeriod = { showConfirmStartPeriod = selectedDate },
                onTogglePeriod = { onTogglePeriodOnDate(selectedDate) },
                onSetFlowEndDate = { cycleId, end -> onSetFlowEndDate(cycleId, end) },
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    showConfirmStartPeriod?.let { date ->
        val formattedDate = date.format(DateTimeFormatter.ofPattern("d 'de' MMMM", localePt))
        AlertDialog(
            onDismissRequest = { showConfirmStartPeriod = null },
            title = {
                Text(
                    text = stringResource(R.string.calendar_confirm_mark_period),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepPlum,
                    ),
                )
            },
            text = {
                Text(
                    text = "Isso marcará $formattedDate como o início do ciclo menstrual. Deseja continuar?",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                PrimaryButton(
                    text = "Confirmar",
                    onClick = {
                        onStartPeriodOnDate(date)
                        showConfirmStartPeriod = null
                    },
                    modifier = Modifier.width(130.dp),
                )
            },
            dismissButton = {
                TextButton(onClick = { showConfirmStartPeriod = null }) {
                    Text(stringResource(R.string.cancel), color = DeepPlum)
                }
            },
            containerColor = SurfaceCard,
        )
    }
}

@Composable
private fun NextPeriodPredictionCard(
    info: NextPeriodInfo,
    localePt: Locale,
    onNavigateToExpectedDate: () -> Unit,
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", localePt) }
    val formattedDate = info.expectedDate.format(dateFormatter).replaceFirstChar { it.uppercase() }

    val countdownText = when {
        info.isToday -> stringResource(R.string.calendar_next_period_today)
        info.daysRemaining == 1 -> stringResource(R.string.calendar_next_period_tomorrow)
        info.daysRemaining > 1 -> "Em ${info.daysRemaining} dias"
        info.isDelayed -> "Atrasada em ${-info.daysRemaining} dias"
        else -> "Hoje"
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MenstrualTerracotta.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, MenstrualTerracotta.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNavigateToExpectedDate),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MenstrualTerracotta.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WaterDrop,
                        contentDescription = null,
                        tint = MenstrualTerracotta,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.calendar_next_period_title),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MenstrualTerracotta,
                        ),
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = DeepPlum,
                        ),
                    )
                    Text(
                        text = "$countdownText • Ciclo estimado de ${info.cycleLength} dias",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MenstrualTerracotta.copy(alpha = 0.18f),
            ) {
                Text(
                    text = "Ver",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MenstrualTerracotta,
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun PhaseLegendRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendItem(color = MenstrualTerracotta, label = stringResource(R.string.calendar_legend_menstrual))
        LegendItem(
            color = MenstrualTerracotta.copy(alpha = 0.45f),
            label = stringResource(R.string.calendar_legend_predicted),
            isBordered = true,
        )
        LegendItem(color = FollicularSage, label = stringResource(R.string.calendar_legend_follicular))
        LegendItem(color = OvulatoryPeach, label = stringResource(R.string.calendar_legend_ovulatory))
        LegendItem(color = LutealLavender, label = stringResource(R.string.calendar_legend_luteal))
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    isBordered: Boolean = false,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (isBordered) Modifier.border(1.dp, MenstrualTerracotta, CircleShape)
                    else Modifier
                ),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
            ),
        )
    }
}

@Composable
private fun DaysOfWeekHeader(localePt: Locale) {
    val weekDays = listOf("DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SÁB")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        weekDays.forEach { dayName ->
            Text(
                text = dayName,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = DeepPlum.copy(alpha = 0.6f),
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CalendarMonthGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate,
    cycles: List<MenstrualCycle>,
    dailyLogs: List<DailyLog>,
    averageCycleDays: Int,
    averagePeriodDays: Int,
    onDateSelected: (LocalDate) -> Unit,
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val leadingEmptyDays = firstDayOfMonth.dayOfWeek.value % 7 // 0 for Sunday
    val daysInMonth = yearMonth.lengthOfMonth()

    val totalSlots = leadingEmptyDays + daysInMonth
    val rows = (totalSlots + 6) / 7

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                for (col in 0 until 7) {
                    val slotIndex = row * 7 + col
                    val dayNumber = slotIndex - leadingEmptyDays + 1

                    if (dayNumber in 1..daysInMonth) {
                        val date = yearMonth.atDay(dayNumber)
                        val isSelected = date == selectedDate
                        val dayState = computeDayState(
                            date = date,
                            today = today,
                            cycles = cycles,
                            dailyLogs = dailyLogs,
                            averageCycleDays = averageCycleDays,
                            averagePeriodDays = averagePeriodDays,
                        )

                        DayCell(
                            state = dayState,
                            isSelected = isSelected,
                            onClick = { onDateSelected(date) },
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    state: CalendarDayState,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val phaseBgColor = when {
        state.isMenstrual && !state.isPredicted -> MenstrualTerracotta.copy(alpha = 0.88f)
        state.isMenstrual && state.isPredicted -> MenstrualTerracotta.copy(alpha = 0.45f)
        state.phase == CyclePhase.FOLLICULAR -> FollicularSage.copy(alpha = 0.55f)
        state.phase == CyclePhase.OVULATORY -> OvulatoryPeach.copy(alpha = 0.75f)
        state.phase == CyclePhase.LUTEAL -> LutealLavender.copy(alpha = 0.55f)
        else -> Color.Transparent
    }

    val textColor = when {
        state.isMenstrual && !state.isPredicted -> Color.White
        state.isMenstrual && state.isPredicted -> DeepPlum
        state.phase != null -> DeepPlum
        else -> TextPrimary
    }

    val borderColor = when {
        isSelected -> DeepPlum
        state.isToday -> DeepPlum.copy(alpha = 0.6f)
        state.isMenstrual && state.isPredicted -> MenstrualTerracotta.copy(alpha = 0.7f)
        state.isOvulationPeak -> DeepPlum.copy(alpha = 0.4f)
        else -> Color.Transparent
    }

    Surface(
        color = phaseBgColor,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if (isSelected) 2.dp else if (state.isToday || (state.isMenstrual && state.isPredicted)) 1.5.dp else 0.dp, borderColor),
        shadowElevation = if (isSelected) 3.dp else 0.dp,
        modifier = modifier
            .padding(2.dp)
            .aspectRatio(1f)
            .accessibleTouchTarget()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = state.date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (state.isToday || isSelected || (state.isMenstrual && state.isPredicted)) FontWeight.Bold else FontWeight.Medium,
                        color = textColor,
                        fontSize = 13.sp,
                    ),
                )
                if (state.hasLog || state.isOvulationPeak || (state.isMenstrual && state.isPredicted)) {
                    Spacer(modifier = Modifier.height(1.dp))
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(
                                if (state.isOvulationPeak) DeepPlum
                                else if (state.isMenstrual && state.isPredicted) MenstrualTerracotta
                                else if (state.isMenstrual) Color.White
                                else DeepPlum.copy(alpha = 0.7f),
                            ),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SelectedDayCard(
    state: CalendarDayState,
    nextPeriodInfo: NextPeriodInfo?,
    cycles: List<MenstrualCycle>,
    today: LocalDate,
    averagePeriodDays: Int,
    localePt: Locale,
    onLogSymptom: () -> Unit,
    onStartPeriod: () -> Unit,
    onTogglePeriod: () -> Unit,
    onSetFlowEndDate: (String, LocalDate?) -> Unit,
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", localePt) }
    val formattedDate = state.date.format(dateFormatter).replaceFirstChar { it.uppercase() }

    val activeOpenCycle = remember(cycles) { cycles.lastOrNull { it.endDate == null } }
    val activeFlowEnd = remember(activeOpenCycle, averagePeriodDays) {
        activeOpenCycle?.let {
            it.flowEndDate ?: it.startDate.plusDays((it.actualFlowDays ?: averagePeriodDays).toLong() - 1L)
        }
    }

    val isOpenPeriodExtended = remember(activeOpenCycle, today, averagePeriodDays) {
        activeOpenCycle != null &&
            activeOpenCycle.flowEndDate == null &&
            ChronoUnit.DAYS.between(activeOpenCycle.startDate, today) > (averagePeriodDays + 3)
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = SurfaceCard,
        border = BorderStroke(1.dp, HeaderSage.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Date Title & Phase Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (state.isToday) "Hoje • $formattedDate" else formattedDate,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepPlum,
                        ),
                    )
                    val phaseDescription = when {
                        state.isMenstrual -> if (state.isPredicted) "Previsão da Menstruação (Próximo Ciclo)" else "Menstruação"
                        state.isOvulationPeak -> if (state.isPredicted) "Previsão do Pico de Ovulação" else "Pico da Ovulação (Fase Ovulatória)"
                        state.phase == CyclePhase.OVULATORY -> if (state.isPredicted) "Previsão de Janela Fértil" else "Janela Fértil (Fase Ovulatória)"
                        state.phase == CyclePhase.FOLLICULAR -> if (state.isPredicted) "Previsão de Fase Folicular" else "Fase Folicular"
                        state.phase == CyclePhase.LUTEAL -> if (state.isPredicted) "Previsão de Fase Lútea" else "Fase Lútea"
                        else -> "Sem ciclo registrado"
                    }
                    val cycleDayText = state.cycleDay?.let { " • Dia $it" }.orEmpty()
                    Text(
                        text = "$phaseDescription$cycleDayText",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = when {
                                state.isMenstrual -> MenstrualTerracotta
                                state.phase == CyclePhase.OVULATORY -> DeepPlum
                                state.phase == CyclePhase.FOLLICULAR -> FollicularSage
                                state.phase == CyclePhase.LUTEAL -> DeepPlum.copy(alpha = 0.8f)
                                else -> TextSecondary
                            },
                        ),
                    )

                    if (!state.isMenstrual && nextPeriodInfo != null) {
                        val nextDateFormatted = nextPeriodInfo.expectedDate.format(DateTimeFormatter.ofPattern("d 'de' MMMM", localePt))
                        val countdown = when {
                            nextPeriodInfo.isToday -> "começa hoje"
                            nextPeriodInfo.daysRemaining == 1 -> "começa amanhã"
                            nextPeriodInfo.daysRemaining > 1 -> "em ${nextPeriodInfo.daysRemaining} dias"
                            nextPeriodInfo.isDelayed -> "atrasada em ${-nextPeriodInfo.daysRemaining} dias"
                            else -> "hoje"
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "🩸 Próxima menstruação: $nextDateFormatted ($countdown)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary,
                                fontSize = 11.sp,
                            ),
                        )
                    } else if (state.isMenstrual && state.isPredicted) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "🩸 Previsão estimada com base na duração do seu ciclo.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = MenstrualTerracotta,
                                fontSize = 11.sp,
                            ),
                        )
                    }
                }
            }

            // Alerta sutil de fluxo em aberto além da média esperada
            if (isOpenPeriodExtended && state.isMenstrual && !state.isPredicted) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MenstrualTerracotta.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, MenstrualTerracotta.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "💡 Seu fluxo continua ativo? Defina o dia de término tocando em 'Encerrar fluxo nesta data' quando parar.",
                        style = MaterialTheme.typography.bodySmall.copy(color = DeepPlum, fontSize = 11.sp),
                        modifier = Modifier.padding(10.dp),
                    )
                }
            }

            // Existing Logs or Empty state
            if (state.dailyLog != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HeaderSage.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "Sintomas registrados:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepPlum,
                            ),
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            state.dailyLog.mood?.let {
                                Text("Humor: ${it.name}", style = MaterialTheme.typography.bodySmall)
                            }
                            state.dailyLog.flowIntensity?.let {
                                Text("Fluxo: ${it.name}", style = MaterialTheme.typography.bodySmall)
                            }
                            state.dailyLog.symptoms.forEach {
                                Text("• ${it.name}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        if (state.dailyLog.notes.isNotBlank()) {
                            Text(
                                text = "\"${state.dailyLog.notes}\"",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                ),
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.calendar_no_log),
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                )
            }

            // Ações: Para hoje ou datas passadas
            if (state.isPastOrToday) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        PrimaryButton(
                            text = stringResource(R.string.calendar_log_day),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.EditNote,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp),
                                )
                            },
                            onClick = onLogSymptom,
                            modifier = Modifier.weight(1f),
                        )

                        // Ações de fluxo menstrual direto (sem modais complexos)
                        if (state.isMenstrual && !state.isPredicted && activeOpenCycle != null) {
                            val isLastFlowDay = state.date == activeFlowEnd && state.date.isAfter(activeOpenCycle.startDate)
                            if (isLastFlowDay) {
                                OutlinedButton(
                                    onClick = onTogglePeriod,
                                    shape = RoundedCornerShape(24.dp),
                                    border = BorderStroke(1.dp, MenstrualTerracotta),
                                    modifier = Modifier.accessibleTouchTarget(),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.RemoveCircleOutline,
                                        contentDescription = null,
                                        tint = MenstrualTerracotta,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Desmarcar dia",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MenstrualTerracotta,
                                        ),
                                    )
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { onSetFlowEndDate(activeOpenCycle.id, state.date) },
                                    shape = RoundedCornerShape(24.dp),
                                    border = BorderStroke(1.dp, MenstrualTerracotta),
                                    modifier = Modifier.accessibleTouchTarget(),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = MenstrualTerracotta,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Encerrar fluxo aqui",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MenstrualTerracotta,
                                        ),
                                    )
                                }
                            }
                        } else if (!state.isMenstrual && activeOpenCycle != null && activeFlowEnd != null &&
                            state.date.isAfter(activeFlowEnd) && ChronoUnit.DAYS.between(activeFlowEnd, state.date) in 1..5
                        ) {
                            // Extensão inteligente dentro da janela de tolerância de 5 dias
                            OutlinedButton(
                                onClick = onTogglePeriod,
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(1.dp, MenstrualTerracotta),
                                modifier = Modifier.accessibleTouchTarget(),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.WaterDrop,
                                    contentDescription = null,
                                    tint = MenstrualTerracotta,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Estender fluxo até cá",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MenstrualTerracotta,
                                    ),
                                )
                            }
                        } else {
                            OutlinedButton(
                                onClick = onStartPeriod,
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(1.dp, MenstrualTerracotta),
                                modifier = Modifier.accessibleTouchTarget(),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.WaterDrop,
                                    contentDescription = null,
                                    tint = MenstrualTerracotta,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (state.cycleDay == 1) "Início registrado" else "Início do ciclo",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MenstrualTerracotta,
                                    ),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HeaderSage.copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "📅 Projeção estimada para datas futuras. O registro de menstruação real é restrito até o dia de hoje.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp,
                        ),
                        modifier = Modifier.padding(10.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

private fun computeDayState(
    date: LocalDate,
    today: LocalDate,
    cycles: List<MenstrualCycle>,
    dailyLogs: List<DailyLog>,
    averageCycleDays: Int,
    averagePeriodDays: Int,
): CalendarDayState {
    val isToday = date == today
    val isPastOrToday = !date.isAfter(today)
    val dailyLog = dailyLogs.find { it.date == date }
    val hasLog = dailyLog != null

    // 1. Ciclo passado finalizado com endDate explícito
    val closedCycle = cycles.filter { it.endDate != null && !date.isBefore(it.startDate) && !date.isAfter(it.endDate) }
        .maxByOrNull { it.startDate }

    if (closedCycle != null) {
        val cycleLength = (closedCycle.cycleLength ?: averageCycleDays).coerceIn(15, 60)
        val periodLength = closedCycle.periodLength.coerceIn(1, cycleLength - 1)
        val day = ChronoUnit.DAYS.between(closedCycle.startDate, date).toInt() + 1
        val ovulationDay = cycleLength - CycleConstants.LUTEAL_PHASE_DAYS
        val isOvulationPeak = day == ovulationDay
        val isMenstrual = if (closedCycle.flowEndDate != null) {
            !date.isBefore(closedCycle.startDate) && !date.isAfter(closedCycle.flowEndDate)
        } else {
            day <= (closedCycle.actualFlowDays ?: periodLength)
        }
        val phase = if (day > cycleLength) CyclePhase.LUTEAL else classifyDay(day, cycleLength, periodLength)

        return CalendarDayState(
            date = date,
            phase = phase,
            cycleDay = day,
            isMenstrual = isMenstrual,
            isOvulationPeak = isOvulationPeak,
            isToday = isToday,
            isPastOrToday = isPastOrToday,
            hasLog = hasLog,
            dailyLog = dailyLog,
            isPredicted = false,
        )
    }

    // 2. Ciclo mais recente (em andamento ou projeção futura)
    val latestCycle = cycles.maxByOrNull { it.startDate }
    if (latestCycle != null && !date.isBefore(latestCycle.startDate)) {
        val cycleLength = (latestCycle.cycleLength ?: averageCycleDays).coerceIn(15, 60)
        val periodLength = latestCycle.periodLength.coerceIn(1, cycleLength - 1)
        val ovulationDay = cycleLength - CycleConstants.LUTEAL_PHASE_DAYS
        val daysFromStart = ChronoUnit.DAYS.between(latestCycle.startDate, date).toInt()

        if (daysFromStart < cycleLength) {
            val cycleDay = daysFromStart + 1
            val isMenstrual = if (latestCycle.flowEndDate != null) {
                !date.isBefore(latestCycle.startDate) && !date.isAfter(latestCycle.flowEndDate)
            } else {
                cycleDay <= (latestCycle.actualFlowDays ?: periodLength)
            }
            val isOvulationPeak = cycleDay == ovulationDay
            val phase = classifyDay(cycleDay, cycleLength, periodLength)

            return CalendarDayState(
                date = date,
                phase = phase,
                cycleDay = cycleDay,
                isMenstrual = isMenstrual,
                isOvulationPeak = isOvulationPeak,
                isToday = isToday,
                isPastOrToday = isPastOrToday,
                hasLog = hasLog,
                dailyLog = dailyLog,
                isPredicted = false,
            )
        } else {
            // Ciclos futuros previstos após o término do ciclo corrente
            val dayInPredictedCycle = (daysFromStart % cycleLength) + 1
            val isMenstrual = dayInPredictedCycle <= periodLength
            val isOvulationPeak = dayInPredictedCycle == ovulationDay
            val phase = classifyDay(dayInPredictedCycle, cycleLength, periodLength)

            return CalendarDayState(
                date = date,
                phase = phase,
                cycleDay = dayInPredictedCycle,
                isMenstrual = isMenstrual,
                isOvulationPeak = isOvulationPeak,
                isToday = isToday,
                isPastOrToday = isPastOrToday,
                hasLog = hasLog,
                dailyLog = dailyLog,
                isPredicted = true,
            )
        }
    }

    return CalendarDayState(
        date = date,
        phase = null,
        cycleDay = null,
        isMenstrual = false,
        isOvulationPeak = false,
        isToday = isToday,
        isPastOrToday = isPastOrToday,
        hasLog = hasLog,
        dailyLog = dailyLog,
        isPredicted = false,
    )
}

private fun calculateNextPeriodPrediction(
    today: LocalDate,
    cycles: List<MenstrualCycle>,
    averageCycleDays: Int,
): NextPeriodInfo? {
    val latestCycle = cycles.maxByOrNull { it.startDate } ?: return null
    val cycleLength = (latestCycle.cycleLength ?: averageCycleDays).coerceIn(15, 60)

    var expectedStart = latestCycle.startDate.plusDays(cycleLength.toLong())

    // Se a data já passou há mais de 14 dias sem novo registro, projeta os próximos ciclos
    if (today.isAfter(expectedStart) && ChronoUnit.DAYS.between(expectedStart, today) > 14) {
        while (expectedStart.isBefore(today)) {
            expectedStart = expectedStart.plusDays(cycleLength.toLong())
        }
    }

    val daysDiff = ChronoUnit.DAYS.between(today, expectedStart).toInt()
    val isDelayed = daysDiff < 0
    val isToday = daysDiff == 0

    return NextPeriodInfo(
        expectedDate = expectedStart,
        daysRemaining = daysDiff,
        isDelayed = isDelayed,
        isToday = isToday,
        cycleLength = cycleLength,
    )
}

