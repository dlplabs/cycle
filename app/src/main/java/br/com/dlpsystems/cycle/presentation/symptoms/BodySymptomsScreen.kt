package br.com.dlpsystems.cycle.presentation.symptoms

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.dlpsystems.cycle.R
import br.com.dlpsystems.cycle.core.designsystem.DeepPlum
import br.com.dlpsystems.cycle.core.designsystem.HeaderSage
import br.com.dlpsystems.cycle.core.designsystem.OffWhiteBackground
import br.com.dlpsystems.cycle.domain.model.BodyRegion
import br.com.dlpsystems.cycle.domain.model.BodySymptomEntry

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BodySymptomsScreen(
    onBack: () -> Unit,
    viewModel: BodySymptomsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    val filtered = if (state.filterRegion == null) {
        state.symptomsForDate
    } else {
        state.symptomsForDate.filter { it.region == state.filterRegion }
    }

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
                    text = stringResource(R.string.body_symptoms_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = DeepPlum),
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = DeepPlum,
                contentColor = Color.White,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Novo Sintoma")
            }
        },
        containerColor = OffWhiteBackground,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Seletor de Regiões Anatômicas (Acessível via lista e visual)
            Text(
                text = "Regiões do Corpo",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DeepPlum),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RegionChip(
                    label = "Todas",
                    selected = state.filterRegion == null,
                    onClick = { viewModel.setFilterRegion(null) },
                )
                BodyRegion.entries.forEach { region ->
                    RegionChip(
                        label = regionLabel(region),
                        selected = state.filterRegion == region,
                        onClick = { viewModel.setFilterRegion(region) },
                    )
                }
            }

            // Lista de Sintomas Registrados no Dia
            Text(
                text = "Sintomas Registrados (${state.selectedDate})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DeepPlum),
            )

            if (filtered.isEmpty()) {
                Text(
                    text = "Nenhum sintoma registrado para esta área hoje. Toque no '+' para adicionar.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = DeepPlum.copy(alpha = 0.7f)),
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            } else {
                filtered.forEach { entry ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${entry.symptomName} (${regionLabel(entry.region)})",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DeepPlum),
                                )
                                Text(
                                    text = "Intensidade: ${intensityLabel(entry.intensity)}${entry.durationMinutes?.let { " • Duração: ${it}min" } ?: ""}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = DeepPlum.copy(alpha = 0.7f)),
                                )
                                if (entry.notes.isNotBlank()) {
                                    Text(
                                        text = entry.notes,
                                        style = MaterialTheme.typography.bodySmall.copy(color = DeepPlum.copy(alpha = 0.9f)),
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.deleteSymptom(entry.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = DeepPlum.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddSymptomDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { region, name, intensity, duration, notes ->
                viewModel.addSymptom(region, name, intensity, duration, notes)
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun RegionChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) DeepPlum else Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else DeepPlum,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun AddSymptomDialog(
    onDismiss: () -> Unit,
    onConfirm: (BodyRegion, String, Int, Int?, String) -> Unit,
) {
    var selectedRegion by remember { mutableStateOf(BodyRegion.HEAD) }
    var symptomName by remember { mutableStateOf("") }
    var intensity by remember { mutableIntStateOf(2) }
    var durationStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Sintoma por Região") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Selecione a Região:", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BodyRegion.entries.forEach { region ->
                        val sel = selectedRegion == region
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (sel) DeepPlum else HeaderSage.copy(alpha = 0.3f))
                                .clickable { selectedRegion = region }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = regionLabel(region),
                                color = if (sel) Color.White else DeepPlum,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = symptomName,
                    onValueChange = { symptomName = it },
                    label = { Text("Nome do Sintoma (ex: Enxaqueca, Dor nas costas)") },
                    modifier = Modifier.fillMaxWidth(),
                )

                Text("Intensidade: ${intensityLabel(intensity)}", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1 to "Leve", 2 to "Moderada", 3 to "Intensa").forEach { (level, lbl) ->
                        val sel = intensity == level
                        Button(
                            onClick = { intensity = level },
                            colors = ButtonDefaults.buttonColors(containerColor = if (sel) DeepPlum else HeaderSage.copy(alpha = 0.4f)),
                        ) {
                            Text(lbl, color = if (sel) Color.White else DeepPlum)
                        }
                    }
                }

                OutlinedTextField(
                    value = durationStr,
                    onValueChange = { durationStr = it },
                    label = { Text("Duração aproximada em minutos (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (symptomName.isNotBlank()) {
                        val duration = durationStr.toIntOrNull()
                        onConfirm(selectedRegion, symptomName, intensity, duration, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DeepPlum),
            ) {
                Text("Adicionar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

private fun regionLabel(region: BodyRegion): String = when (region) {
    BodyRegion.HEAD -> "Cabeça"
    BodyRegion.BREASTS -> "Mamas"
    BodyRegion.ABDOMEN -> "Abdômen"
    BodyRegion.PELVIS -> "Pelve"
    BodyRegion.BACK -> "Costas"
    BodyRegion.LIMBS -> "Membros"
    BodyRegion.GENERAL -> "Gerais"
    BodyRegion.EMOTIONAL -> "Emocionais"
}

private fun intensityLabel(intensity: Int): String = when (intensity) {
    1 -> "Leve"
    2 -> "Moderada"
    3 -> "Intensa"
    else -> "Moderada"
}
