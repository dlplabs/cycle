package br.com.dlpsystems.cycle.presentation.lunar

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.dlpsystems.cycle.R
import br.com.dlpsystems.cycle.core.designsystem.DeepPlum
import br.com.dlpsystems.cycle.core.designsystem.HeaderSage
import br.com.dlpsystems.cycle.core.designsystem.OffWhiteBackground
import br.com.dlpsystems.cycle.domain.model.EnergyLevel
import br.com.dlpsystems.cycle.domain.model.MoonPhase
import br.com.dlpsystems.cycle.domain.model.ProductivityLevel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LunarScreen(
    onBack: () -> Unit,
    viewModel: LunarViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var viewModeTab by remember { mutableIntStateOf(0) } // 0 = Mandala, 1 = Lista
    val scroll = rememberScrollState()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HeaderSage)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back), tint = DeepPlum)
                }
                Text(
                    text = stringResource(R.string.lunar_mandala_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = DeepPlum),
                )
            }
        },
        containerColor = OffWhiteBackground,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TabRow(
                selectedTabIndex = viewModeTab,
                containerColor = HeaderSage.copy(alpha = 0.4f),
                contentColor = DeepPlum,
            ) {
                Tab(
                    selected = viewModeTab == 0,
                    onClick = { viewModeTab = 0 },
                    text = { Text(stringResource(R.string.lunar_view_mandala)) },
                )
                Tab(
                    selected = viewModeTab == 1,
                    onClick = { viewModeTab = 1 },
                    text = { Text(stringResource(R.string.lunar_view_list)) },
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (viewModeTab == 0) {
                    // Mandala Visual
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "Mandala de Padrões Pessoais",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DeepPlum),
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            MandalaWheel(
                                energyLevel = state.selectedEnergy,
                                moonPhase = state.currentMoonPhase,
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Fase da Lua hoje: ${moonPhaseName(state.currentMoonPhase)} ${moonPhaseEmoji(state.currentMoonPhase)}",
                                style = MaterialTheme.typography.bodyMedium.copy(color = DeepPlum),
                            )
                        }
                    }
                }

                // Registro do Dia
                Card(
                    colors = CardDefaults.cardColors(containerColor = HeaderSage.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Check-in de Hoje (${state.selectedDate})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DeepPlum),
                        )

                        Text(
                            text = stringResource(R.string.lunar_energy_label),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = DeepPlum),
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            EnergyLevel.entries.forEach { level ->
                                val selected = state.selectedEnergy == level
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (selected) DeepPlum else Color.White)
                                        .clickable { viewModel.setEnergy(level) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                ) {
                                    Text(
                                        text = energyLevelLabel(level),
                                        color = if (selected) Color.White else DeepPlum,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    )
                                }
                            }
                        }

                        Text(
                            text = stringResource(R.string.lunar_productivity_label),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = DeepPlum),
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ProductivityLevel.entries.forEach { prod ->
                                val selected = state.selectedProductivity == prod
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (selected) DeepPlum else Color.White)
                                        .clickable { viewModel.setProductivity(prod) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                ) {
                                    Text(
                                        text = productivityLevelLabel(prod),
                                        color = if (selected) Color.White else DeepPlum,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = state.notes,
                            onValueChange = viewModel::setNotes,
                            label = { Text("Anotações de sono e sintomas") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                        )

                        Button(
                            onClick = { viewModel.saveEntry(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepPlum),
                            modifier = Modifier.align(Alignment.End),
                        ) {
                            Text("Salvar Registro", color = Color.White)
                        }
                    }
                }

                // Resumo de Padrões Pessoais
                state.summary?.let { summary ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Padrões Observados",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DeepPlum),
                            )
                            Text(
                                text = "Base de análise: ${summary.periodDescription}",
                                style = MaterialTheme.typography.bodySmall.copy(color = DeepPlum.copy(alpha = 0.7f)),
                            )
                            if (summary.isDataSufficient) {
                                summary.highestEnergyPhase?.let {
                                    Text(text = "• Maior disposição relatada na fase: ${it.name}", style = MaterialTheme.typography.bodyMedium, color = DeepPlum)
                                }
                                summary.lowestEnergyPhase?.let {
                                    Text(text = "• Menor disposição relatada na fase: ${it.name}", style = MaterialTheme.typography.bodyMedium, color = DeepPlum)
                                }
                            } else {
                                Text(
                                    text = stringResource(R.string.lunar_insufficient_data),
                                    style = MaterialTheme.typography.bodySmall.copy(color = DeepPlum.copy(alpha = 0.8f)),
                                )
                            }
                            Text(
                                text = summary.disclaimer,
                                style = MaterialTheme.typography.labelSmall.copy(color = DeepPlum.copy(alpha = 0.6f)),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MandalaWheel(energyLevel: EnergyLevel?, moonPhase: MoonPhase) {
    Box(
        modifier = Modifier
            .size(200.dp)
            .clip(CircleShape)
            .background(HeaderSage.copy(alpha = 0.15f))
            .border(2.dp, HeaderSage, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 10.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val energyVal = energyLevel?.value ?: 0
            val sweep = (energyVal / 5f) * 360f

            drawCircle(
                color = HeaderSage.copy(alpha = 0.3f),
                radius = radius,
                style = Stroke(strokeWidth),
            )
            if (sweep > 0) {
                drawArc(
                    color = DeepPlum,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round),
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = moonPhaseEmoji(moonPhase), fontSize = 32.sp)
            Text(
                text = if (energyLevel != null) "${energyLevel.value}/5" else "Sem registro",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = DeepPlum),
            )
        }
    }
}

private fun moonPhaseName(phase: MoonPhase): String = when (phase) {
    MoonPhase.NEW_MOON -> "Nova"
    MoonPhase.WAXING_CRESCENT -> "Crescente"
    MoonPhase.FIRST_QUARTER -> "Quarto Crescente"
    MoonPhase.WAXING_GIBBOUS -> "Crescente Gibosa"
    MoonPhase.FULL_MOON -> "Cheia"
    MoonPhase.WANING_GIBBOUS -> "Minguante Gibosa"
    MoonPhase.LAST_QUARTER -> "Quarto Minguante"
    MoonPhase.WANING_CRESCENT -> "Minguante"
}

private fun moonPhaseEmoji(phase: MoonPhase): String = when (phase) {
    MoonPhase.NEW_MOON -> "🌑"
    MoonPhase.WAXING_CRESCENT -> "🌒"
    MoonPhase.FIRST_QUARTER -> "🌓"
    MoonPhase.WAXING_GIBBOUS -> "🌔"
    MoonPhase.FULL_MOON -> "🌕"
    MoonPhase.WANING_GIBBOUS -> "🌖"
    MoonPhase.LAST_QUARTER -> "🌗"
    MoonPhase.WANING_CRESCENT -> "🌘"
}

private fun energyLevelLabel(level: EnergyLevel): String = when (level) {
    EnergyLevel.VERY_LOW -> "Muito Baixa"
    EnergyLevel.LOW -> "Baixa"
    EnergyLevel.MODERATE -> "Moderada"
    EnergyLevel.HIGH -> "Alta"
    EnergyLevel.VERY_HIGH -> "Excelente"
}

private fun productivityLevelLabel(level: ProductivityLevel): String = when (level) {
    ProductivityLevel.VERY_LOW -> "Muito Baixa"
    ProductivityLevel.LOW -> "Baixa"
    ProductivityLevel.MODERATE -> "Equilibrada"
    ProductivityLevel.HIGH -> "Focada"
    ProductivityLevel.VERY_HIGH -> "Muito Produtiva"
}
