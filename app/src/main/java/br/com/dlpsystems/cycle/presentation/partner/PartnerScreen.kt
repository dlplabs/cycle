package br.com.dlpsystems.cycle.presentation.partner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.dlpsystems.cycle.R
import br.com.dlpsystems.cycle.core.designsystem.DeepPlum
import br.com.dlpsystems.cycle.core.designsystem.HeaderSage
import br.com.dlpsystems.cycle.core.designsystem.OffWhiteBackground
import br.com.dlpsystems.cycle.domain.model.PartnerShareCategory

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PartnerScreen(
    onBack: () -> Unit,
    viewModel: PartnerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showInviteDialog by remember { mutableStateOf(false) }

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
                    text = stringResource(R.string.partner_invite_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = DeepPlum),
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showInviteDialog = true },
                containerColor = DeepPlum,
                contentColor = Color.White,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Convidar Parceiro")
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
            Card(
                colors = CardDefaults.cardColors(containerColor = HeaderSage.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = DeepPlum)
                    Text(
                        text = stringResource(R.string.partner_invite_desc),
                        style = MaterialTheme.typography.bodySmall.copy(color = DeepPlum),
                    )
                }
            }

            Text(
                text = "Convites Pendentes",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DeepPlum),
            )
            if (state.invites.isEmpty()) {
                Text(
                    text = "Nenhum convite enviado no momento.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = DeepPlum.copy(alpha = 0.7f)),
                )
            } else {
                state.invites.forEach { invite ->
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
                                    text = "${invite.partnerName} (${invite.partnerEmail})",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DeepPlum),
                                )
                                Text(
                                    text = "Código: ${invite.inviteCode} • Status: ${invite.status.name}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = DeepPlum.copy(alpha = 0.7f)),
                                )
                            }
                            IconButton(onClick = { viewModel.revokeAccess(invite.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Revogar", tint = DeepPlum.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }

            Text(
                text = stringResource(R.string.partner_active_connections),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DeepPlum),
            )
            if (state.activePartners.isEmpty()) {
                Text(
                    text = "Nenhuma conexão ativa.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = DeepPlum.copy(alpha = 0.7f)),
                )
            } else {
                state.activePartners.forEach { conn ->
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
                                    text = conn.partnerName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DeepPlum),
                                )
                                Text(
                                    text = conn.partnerEmail,
                                    style = MaterialTheme.typography.bodySmall.copy(color = DeepPlum.copy(alpha = 0.7f)),
                                )
                            }
                            IconButton(onClick = { viewModel.revokeAccess(conn.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Revogar Acesso", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showInviteDialog) {
        CreateInviteDialog(
            onDismiss = { showInviteDialog = false },
            onConfirm = { email, name, cats ->
                viewModel.createInvite(email, name, cats)
                showInviteDialog = false
            },
        )
    }
}

@Composable
private fun CreateInviteDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Set<PartnerShareCategory>) -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var selectedCategories by remember {
        mutableStateOf(setOf(PartnerShareCategory.CYCLE_PHASES, PartnerShareCategory.MOOD_ENERGY))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Convidar Parceiro(a)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do Parceiro(a)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Categorias autorizadas a compartilhar:", style = MaterialTheme.typography.labelMedium)
                PartnerShareCategory.entries.forEach { category ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = category in selectedCategories,
                            onCheckedChange = { checked ->
                                selectedCategories = if (checked) selectedCategories + category else selectedCategories - category
                            },
                        )
                        Text(text = partnerCategoryLabel(category), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (email.isNotBlank() && name.isNotBlank()) onConfirm(email, name, selectedCategories)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DeepPlum),
            ) {
                Text("Gerar Convite", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

private fun partnerCategoryLabel(category: PartnerShareCategory): String = when (category) {
    PartnerShareCategory.CYCLE_PHASES -> "Fases do Ciclo e Previsões"
    PartnerShareCategory.FERTILE_WINDOW -> "Janela Fértil e Ovulação"
    PartnerShareCategory.MOOD_ENERGY -> "Humor e Disposição Geral"
    PartnerShareCategory.PREGNANCY_WEEK -> "Semana de Gravidez e Bebê"
    PartnerShareCategory.SYMPTOMS_GENERAL -> "Sintomas Gerais (Opcional)"
}
