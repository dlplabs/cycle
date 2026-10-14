package br.com.dlpsystems.cycle.presentation.safety

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.dlpsystems.cycle.core.designsystem.AppCard
import br.com.dlpsystems.cycle.core.designsystem.DeepPlum
import br.com.dlpsystems.cycle.core.designsystem.HeaderSage
import br.com.dlpsystems.cycle.core.designsystem.LutealLavender
import br.com.dlpsystems.cycle.core.designsystem.OffWhiteBackground
import br.com.dlpsystems.cycle.core.designsystem.PrimaryButton
import br.com.dlpsystems.cycle.presentation.components.ScreenHeader

@Composable
fun PersonalSafetyScreen(
    onBack: () -> Unit,
    onStartIavp: () -> Unit,
    viewModel: IavpViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var newPhoneInput by remember { mutableStateOf("") }
    var isRecordingAudio by remember { mutableStateOf(false) }

    if (state.isDisguised) {
        DisguiseHydrationScreen(onExitDisguise = { viewModel.setDisguised(false) })
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhiteBackground),
    ) {
        ScreenHeader(
            title = "Check-in de Segurança Pessoal",
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Card do IAVP (Avaliação Preventiva de Risco)
            AppCard {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(LutealLavender),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = DeepPlum,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Column {
                            Text(
                                text = "Avaliação de Risco Preventiva (IAVP)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DeepPlum,
                            )
                            Text(
                                text = "Questionário sigiloso em 13 etapas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Text(
                        text = "Identifique sinais de vulnerabilidade e receba diretrizes personalizadas para plano de apoio e segurança.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    PrimaryButton(
                        text = "Iniciar Avaliação Preventiva",
                        onClick = onStartIavp,
                    )
                }
            }

            // Card de Contatos de Confiança e Acionamento Silencioso SOS
            Text(
                text = "Contatos de Confiança & SOS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepPlum,
            )

            AppCard {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Cadastre telefones para envio silencioso de localização em caso de urgência.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = newPhoneInput,
                            onValueChange = { newPhoneInput = it },
                            placeholder = { Text("DDD + Telefone") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                        )
                        IconButton(
                            onClick = {
                                viewModel.addTrustedContact(newPhoneInput)
                                newPhoneInput = ""
                            },
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Adicionar", tint = DeepPlum)
                        }
                    }

                    if (state.trustedContacts.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            state.trustedContacts.forEach { phone ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = phone,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    IconButton(onClick = { viewModel.removeTrustedContact(phone) }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Remover",
                                            tint = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    PrimaryButton(
                        text = "Disparar Alerta SOS Silencioso",
                        onClick = {
                            viewModel.dispatchSos { intent ->
                                context.startActivity(intent)
                            }
                        },
                    )
                }
            }

            // Gravador Forense de Áudio & Camuflagem
            Text(
                text = "Ferramentas de Proteção de Contingência",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepPlum,
            )

            AppCard {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = DeepPlum)
                            Column {
                                Text(
                                    text = "Gravação de Áudio Forense",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = if (isRecordingAudio) "Gravando em diretório protegido..." else "Armazenamento em área privada do app",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    if (isRecordingAudio) {
                        OutlinedButton(
                            onClick = {
                                viewModel.stopAudioEvidence()
                                isRecordingAudio = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                            ),
                        ) {
                            Text("Parar Gravação de Áudio")
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                viewModel.startAudioEvidence()
                                isRecordingAudio = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Iniciar Gravação Discreta")
                        }
                    }

                    HorizontalDivider()

                    OutlinedButton(
                        onClick = { viewModel.setDisguised(true) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.VisibilityOff, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Ativar Tela de Camuflagem Rápida")
                    }
                }
            }

            // Canais de Atendimento de Emergência
            AppCard {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Canais Oficiais de Atendimento",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = DeepPlum,
                    )
                    EmergencyCallRow(title = "Polícia Militar", phone = "190")
                    EmergencyCallRow(title = "Central de Atendimento à Mulher", phone = "180")
                }
            }
        }
    }
}

@Composable
private fun EmergencyCallRow(title: String, phone: String) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(text = "Discagem rápida: $phone", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        OutlinedButton(
            onClick = {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                context.startActivity(intent)
            },
        ) {
            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.size(6.dp))
            Text(phone)
        }
    }
}
