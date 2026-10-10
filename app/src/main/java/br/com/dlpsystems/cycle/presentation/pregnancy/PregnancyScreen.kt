package br.com.dlpsystems.cycle.presentation.pregnancy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import br.com.dlpsystems.cycle.domain.model.HealthEducationalArticle
import java.time.LocalDate

@Composable
fun PregnancyScreen(
    onBack: () -> Unit,
    viewModel: PregnancyViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showSetupDialog by remember { mutableStateOf(false) }
    var showEndDialog by remember { mutableStateOf(false) }

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
                    text = stringResource(R.string.pregnancy_mode_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = DeepPlum),
                    modifier = Modifier.weight(1f),
                )
                if (state.profile.isActive) {
                    TextButton(onClick = { showEndDialog = true }) {
                        Text("Encerrar", color = DeepPlum, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        containerColor = OffWhiteBackground,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (!state.profile.isActive) {
                PregnancyInactiveBanner(onActivate = { showSetupDialog = true })
            } else {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = HeaderSage.copy(alpha = 0.4f),
                    contentColor = DeepPlum,
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Evolução") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Contrações") })
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Cuidados") })
                }

                when (selectedTab) {
                    0 -> PregnancyEvolutionTab(state = state, onEdit = { showSetupDialog = true })
                    1 -> ContractionTimerTab(state = state, viewModel = viewModel)
                    2 -> PregnancyCareTab(state = state)
                }
            }
        }
    }

    if (showSetupDialog) {
        PregnancySetupDialog(
            currentLmp = state.profile.lastMenstrualPeriod,
            onDismiss = { showSetupDialog = false },
            onConfirmLmp = { lmp ->
                viewModel.activatePregnancyWithLmp(lmp)
                showSetupDialog = false
            },
        )
    }

    if (showEndDialog) {
        AlertDialog(
            onDismissRequest = { showEndDialog = false },
            title = { Text(stringResource(R.string.pregnancy_deactivate)) },
            text = { Text(stringResource(R.string.pregnancy_confirm_end)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deactivatePregnancy()
                        showEndDialog = false
                    },
                ) {
                    Text("Encerrar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun PregnancyInactiveBanner(onActivate: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(HeaderSage),
            contentAlignment = Alignment.Center,
        ) {
            Text("🌱", fontSize = 32.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.pregnancy_mode_title),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = DeepPlum),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.pregnancy_mode_desc),
            style = MaterialTheme.typography.bodyMedium.copy(color = DeepPlum.copy(alpha = 0.8f)),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onActivate,
            colors = ButtonDefaults.buttonColors(containerColor = DeepPlum),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(stringResource(R.string.pregnancy_activate), color = Color.White)
        }
    }
}

@Composable
private fun PregnancyEvolutionTab(state: PregnancyUiState, onEdit: () -> Unit) {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val age = state.gestationalAge
        if (age != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = HeaderSage.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.gestational_week_format, age.weeks, age.days),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = DeepPlum),
                        )
                        TextButton(onClick = onEdit) {
                            Text("Ajustar", color = DeepPlum)
                        }
                    }
                    Text(
                        text = stringResource(
                            when (age.trimester) {
                                br.com.dlpsystems.cycle.domain.model.Trimester.FIRST -> R.string.gestational_trimester_1
                                br.com.dlpsystems.cycle.domain.model.Trimester.SECOND -> R.string.gestational_trimester_2
                                br.com.dlpsystems.cycle.domain.model.Trimester.THIRD -> R.string.gestational_trimester_3
                            },
                        ),
                        style = MaterialTheme.typography.bodyMedium.copy(color = DeepPlum.copy(alpha = 0.8f)),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.estimated_due_date_label, age.estimatedDueDate.toString()),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = DeepPlum),
                    )
                }
            }
        }

        // Artigo da semana
        state.currentWeekArticle?.let { article ->
            ArticleCard(article = article)
        }

        // Disclaimer Clínico
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.pregnancy_calculation_disclaimer),
                style = MaterialTheme.typography.bodySmall.copy(color = DeepPlum.copy(alpha = 0.7f)),
                modifier = Modifier.padding(14.dp),
            )
        }
    }
}

@Composable
private fun ContractionTimerTab(state: PregnancyUiState, viewModel: PregnancyViewModel) {
    val scroll = rememberScrollState()
    val isRunning = state.activeContractionStartEpochMs != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Cronômetro Circular
        Card(
            colors = CardDefaults.cardColors(containerColor = HeaderSage.copy(alpha = 0.25f)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "${state.activeElapsedSeconds}s",
                    style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold, color = DeepPlum),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (isRunning) viewModel.stopContraction() else viewModel.startContraction()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) MaterialTheme.colorScheme.error else DeepPlum,
                    ),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = stringResource(if (isRunning) R.string.contractions_stop else R.string.contractions_start),
                        color = Color.White,
                    )
                }
            }
        }

        // Avaliação descritiva clínica
        state.contractionEvaluation?.let { evaluation ->
            if (evaluation.totalCount > 0) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Médias Recentes: ${evaluation.averageDurationSeconds}s de duração | intervalo médio de ${evaluation.averageIntervalSeconds / 60}m ${evaluation.averageIntervalSeconds % 60}s",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = DeepPlum),
                        )
                        Text(
                            text = evaluation.emergencyWarning,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error),
                        )
                    }
                }
            }
        }

        // Histórico de contrações
        val allRecords = state.contractionSessions.flatMap { it.records }.sortedByDescending { it.startTimeEpochMs }
        Text(
            text = "Histórico de Registros (${allRecords.size})",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DeepPlum),
            modifier = Modifier.align(Alignment.Start),
        )

        allRecords.forEach { record ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.contraction_duration_format, record.durationSeconds),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = DeepPlum),
                        )
                        record.intervalSecondsFromPrevious?.let { interval ->
                            Text(
                                text = stringResource(R.string.contraction_interval_format, interval / 60, interval % 60),
                                style = MaterialTheme.typography.bodySmall.copy(color = DeepPlum.copy(alpha = 0.7f)),
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.deleteContraction(record.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir registro", tint = DeepPlum.copy(alpha = 0.6f))
                    }
                }
            }
        }

        // Aviso final
        Text(
            text = stringResource(R.string.contractions_disclaimer),
            style = MaterialTheme.typography.bodySmall.copy(color = DeepPlum.copy(alpha = 0.7f)),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PregnancyCareTab(state: PregnancyUiState) {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Parto e Escolhas Informadas",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DeepPlum),
        )
        state.birthArticles.forEach { ArticleCard(article = it) }

        Text(
            text = "Amamentação e Pós-Parto",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DeepPlum),
        )
        state.breastfeedingArticles.forEach { ArticleCard(article = it) }

        state.exerciseArticle?.let {
            Text(
                text = "Movimento e Alívio de Desconfortos",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DeepPlum),
            )
            ArticleCard(article = it)
        }
    }
}

@Composable
private fun ArticleCard(article: HealthEducationalArticle) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DeepPlum),
            )
            Text(
                text = article.summary,
                style = MaterialTheme.typography.bodyMedium.copy(color = DeepPlum.copy(alpha = 0.85f)),
            )
            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = article.content,
                        style = MaterialTheme.typography.bodySmall.copy(color = DeepPlum.copy(alpha = 0.9f)),
                    )
                    if (article.keyTakeaways.isNotEmpty()) {
                        Text(
                            text = "Pontos Principais:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = DeepPlum),
                        )
                        article.keyTakeaways.forEach { item ->
                            Text(text = "• $item", style = MaterialTheme.typography.bodySmall, color = DeepPlum)
                        }
                    }
                    if (article.contraindicationsOrAlerts.isNotEmpty()) {
                        Text(
                            text = "Alertas e Contraindicações:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error),
                        )
                        article.contraindicationsOrAlerts.forEach { item ->
                            Text(text = "⚠️ $item", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    Text(
                        text = "Fonte: ${article.citationAuthors} — ${article.sourceName} (${article.publicationDate})",
                        style = MaterialTheme.typography.labelSmall.copy(color = DeepPlum.copy(alpha = 0.6f)),
                    )
                }
            }
        }
    }
}

@Composable
private fun PregnancySetupDialog(
    currentLmp: LocalDate?,
    onDismiss: () -> Unit,
    onConfirmLmp: (LocalDate) -> Unit,
) {
    var lmpInput by remember { mutableStateOf(currentLmp?.toString() ?: LocalDate.now().minusWeeks(8).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar Idade Gestacional") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Informe a Data da Última Menstruação (DUM) no formato AAAA-MM-DD:")
                OutlinedTextField(
                    value = lmpInput,
                    onValueChange = { lmpInput = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "A data provável do parto é calculada somando 280 dias à DUM (Regra de Naegele). Você poderá corrigir a datação a qualquer momento conforme sua ultrassonografia.",
                    style = MaterialTheme.typography.bodySmall.copy(color = DeepPlum.copy(alpha = 0.7f)),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = runCatching { LocalDate.parse(lmpInput.trim()) }.getOrNull() ?: LocalDate.now()
                    onConfirmLmp(parsed)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DeepPlum),
            ) {
                Text("Salvar", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
