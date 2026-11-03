package br.com.dlpsystems.cycle.presentation.safety

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.dlpsystems.cycle.core.designsystem.AppCard
import br.com.dlpsystems.cycle.core.designsystem.DeepPlum
import br.com.dlpsystems.cycle.core.designsystem.HeaderSage
import br.com.dlpsystems.cycle.core.designsystem.LutealLavender
import br.com.dlpsystems.cycle.core.designsystem.OffWhiteBackground
import br.com.dlpsystems.cycle.core.designsystem.PlayfairDisplay
import br.com.dlpsystems.cycle.core.designsystem.PrimaryButton
import br.com.dlpsystems.cycle.domain.model.ForensicAudioRecord
import br.com.dlpsystems.cycle.domain.model.TrustedContact
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val WarmTerracotta = Color(0xFFD96548)
private val SoftPeachBg = Color(0xFFFFF1EC)
private val SoftLavenderBg = Color(0xFFF1ECFF)
private val DeepVioletAccent = Color(0xFF6B48C8)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PersonalSafetyScreen(
    onBack: () -> Unit,
    onStartIavp: () -> Unit,
    viewModel: IavpViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var newContactName by remember { mutableStateOf("") }
    var newPhoneInput by remember { mutableStateOf("") }
    var showEditSosMsg by remember { mutableStateOf(false) }
    var sosMsgDraft by remember(state.customSosMessage) { mutableStateOf(state.customSosMessage) }

    var destinationInput by remember { mutableStateOf("Casa") }
    var selectedDurationMin by remember { mutableIntStateOf(30) }

    var pendingLocationAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { _ ->
        viewModel.refreshPermissionsAndAudioRecords()
        val action = pendingLocationAction
        pendingLocationAction = null
        action?.invoke()
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.refreshPermissionsAndAudioRecords()
        if (granted) {
            viewModel.startAudioEvidence()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshPermissionsAndAudioRecords()
    }

    fun runWithLocationPermission(action: () -> Unit) {
        if (state.hasLocationPermission) {
            action()
        } else {
            pendingLocationAction = action
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    if (state.isDisguised) {
        DisguiseHydrationScreen(onExitDisguise = { viewModel.setDisguised(false) })
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhiteBackground),
    ) {
        // Barra Superior com Acesso Imediato ao Modo Discreto (Camuflagem)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeaderSage)
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = DeepPlum,
                    )
                }
                Text(
                    text = "Segurança Pessoal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DeepPlum,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { viewModel.setDisguised(true) },
                color = DeepPlum,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = "Modo Discreto",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Modo Discreto",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            // Banner de feedback de ações
            AnimatedVisibility(visible = state.statusBannerMessage != null) {
                state.statusBannerMessage?.let { msg ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (state.isTransitExpiredAlert) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            HeaderSage.copy(alpha = 0.45f)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (state.isTransitExpiredAlert) {
                                    MaterialTheme.colorScheme.onErrorContainer
                                } else {
                                    DeepPlum
                                },
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(
                                onClick = viewModel::clearStatusBanner,
                                modifier = Modifier.size(28.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Fechar aviso",
                                    tint = DeepPlum,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // HERO CARD: Identidade Visual "Cuidar de você também é garantir que você chegue bem em casa"
            // =========================================================================
            SafetyHeroShowcaseCard()

            // =========================================================================
            // PILAR 1: CÍRCULO DE CONFIANÇA SEMPRE ATIVO & SOS SILENCIOSO
            // =========================================================================
            SectionTitleWithBadge(
                dotColor = WarmTerracotta,
                title = "Círculo de Confiança Sempre Ativo",
                subtitle = "Compartilhamento seguro e alerta discreto para quem cuida de você",
            )

            AppCard {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        text = "Cadastre pessoas da sua rede de confiança (mãe, irmã, amiga, parceiro). Os contatos ficam salvos com segurança no aparelho para acionamento imediato.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    // Formulário de adição de contato com Nome + Telefone
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedTextField(
                                value = newContactName,
                                onValueChange = { newContactName = it },
                                label = { Text("Nome (ex: Mãe, Amiga)") },
                                modifier = Modifier.weight(0.95f),
                                singleLine = true,
                            )
                            OutlinedTextField(
                                value = newPhoneInput,
                                onValueChange = { newPhoneInput = it },
                                label = { Text("DDD + Telefone") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.weight(1.05f),
                                singleLine = true,
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.addTrustedContact(
                                    name = newContactName,
                                    phone = newPhoneInput,
                                )
                                if (newPhoneInput.filter { it.isDigit() }.length >= 8) {
                                    newContactName = ""
                                    newPhoneInput = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Adicionar ao Círculo de Confiança")
                        }
                    }

                    // Lista de Contatos Salvos
                    if (state.trustedContactList.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SoftPeachBg,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "Nenhum contato cadastrado ainda. Adicione ao menos 1 pessoa de confiança acima para ativar o envio rápido de SOS.",
                                style = MaterialTheme.typography.bodySmall,
                                color = WarmTerracotta,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.trustedContactList.forEach { contact ->
                                TrustedContactItemRow(
                                    contact = contact,
                                    onCall = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}"))
                                        context.startActivity(intent)
                                    },
                                    onRemove = { viewModel.removeTrustedContact(contact.id) },
                                )
                            }
                        }
                    }

                    // Personalização da mensagem discreta de SOS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if ( state.hasLocationPermission ) "GPS Ativo para localização exata" else "Toque em SOS para liberar GPS",
                            style = MaterialTheme.typography.labelSmall,
                            color = DeepPlum.copy(alpha = 0.75f),
                        )
                        TextButton(onClick = { showEditSosMsg = !showEditSosMsg }) {
                            Text(if (showEditSosMsg) "Ocultar mensagem" else "Personalizar texto SOS")
                        }
                    }

                    AnimatedVisibility(visible = showEditSosMsg) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = sosMsgDraft,
                                onValueChange = { sosMsgDraft = it },
                                label = { Text("Mensagem de Alerta SOS") },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            OutlinedButton(
                                onClick = {
                                    viewModel.updateCustomSosMessage(sosMsgDraft)
                                    showEditSosMsg = false
                                },
                                modifier = Modifier.align(Alignment.End),
                            ) {
                                Text("Salvar Mensagem")
                            }
                        }
                    }

                    HorizontalDivider()

                    // Ações imediatas do Círculo de Confiança
                    PrimaryButton(
                        text = if (state.isDispatchingSos) "Obtendo GPS e Enviando..." else "Disparar Alerta SOS Silencioso",
                        onClick = {
                            runWithLocationPermission {
                                viewModel.dispatchSos { fallbackIntent ->
                                    runCatching { context.startActivity(fallbackIntent) }
                                }
                            }
                        },
                    )

                    OutlinedButton(
                        onClick = {
                            runWithLocationPermission {
                                viewModel.shareCurrentLocation { shareIntent ->
                                    runCatching { context.startActivity(shareIntent) }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Compartilhar Localização Atual (WhatsApp / SMS)")
                    }
                }
            }

            // =========================================================================
            // PILAR 2: MODO DISCRETO & PROTEÇÃO EM TRÂNSITO ("CHEGUEI BEM")
            // =========================================================================
            SectionTitleWithBadge(
                dotColor = DeepVioletAccent,
                title = "Modo Discreto & Proteção em Trânsito",
                subtitle = "Acompanhamento de trajeto pensado para a tranquilidade da sua rotina",
            )

            AppCard {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    val activeTransit = state.activeTransitSession
                    if (activeTransit != null) {
                        // Card de Trajeto em Andamento
                        val minutesPart = state.transitRemainingSeconds / 60
                        val secondsPart = state.transitRemainingSeconds % 60
                        val formattedTimer = String.format(Locale.US, "%02d:%02d", minutesPart, secondsPart)

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (state.isTransitExpiredAlert) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                SoftLavenderBg
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        imageVector = if (state.isTransitExpiredAlert) Icons.Default.Warning else Icons.AutoMirrored.Filled.DirectionsWalk,
                                        contentDescription = null,
                                        tint = if (state.isTransitExpiredAlert) MaterialTheme.colorScheme.error else DeepVioletAccent,
                                    )
                                    Text(
                                        text = if (state.isTransitExpiredAlert) {
                                            "Tempo Expirado — Confirme sua Segurança!"
                                        } else {
                                            "Em Trânsito para: ${activeTransit.destinationLabel}"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepPlum,
                                    )
                                }

                                Text(
                                    text = formattedTimer,
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (state.isTransitExpiredAlert) MaterialTheme.colorScheme.error else DeepPlum,
                                    ),
                                )

                                Text(
                                    text = if (state.isTransitExpiredAlert) {
                                        "Você já chegou ao seu destino? Confirme abaixo ou acione o SOS imediatamente."
                                    } else {
                                        "Ao chegar ao destino, toque em \"Cheguei Bem\" para encerrar o monitoramento."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        if (state.isTransitExpiredAlert) {
                            Button(
                                onClick = {
                                    runWithLocationPermission {
                                        viewModel.dispatchSos { intent ->
                                            runCatching { context.startActivity(intent) }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Acionar Alerta SOS Agora")
                            }
                        }

                        PrimaryButton(
                            text = "Cheguei Bem (Encerrar Trajeto)",
                            onClick = { viewModel.confirmSafeArrival(notifyCircle = false) },
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.confirmSafeArrival(notifyCircle = true) { intent ->
                                        runCatching { context.startActivity(intent) }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Avisar que Cheguei")
                            }

                            OutlinedButton(
                                onClick = { viewModel.extendTransitTime(10) },
                                modifier = Modifier.weight(0.7f),
                            ) {
                                Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+10 min")
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                runWithLocationPermission {
                                    viewModel.shareActiveTransitWithCircle { intent ->
                                        runCatching { context.startActivity(intent) }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Enviar Atualização de Rota ao Círculo")
                        }
                    } else {
                        // Configuração de novo trajeto
                        Text(
                            text = "Vai voltar para casa à noite, pegar transporte por aplicativo ou fazer um trajeto sozinha? Ative o timer de proteção e compartilhe sua saída.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        val quickDestinations = listOf("Casa", "Corrida de App", "Trabalho / Estudo", "Caminhada")
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            quickDestinations.forEach { preset ->
                                FilterChip(
                                    selected = destinationInput == preset,
                                    onClick = { destinationInput = preset },
                                    label = { Text(preset) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SoftLavenderBg,
                                        selectedLabelColor = DeepPlum,
                                    ),
                                )
                            }
                        }

                        OutlinedTextField(
                            value = destinationInput,
                            onValueChange = { destinationInput = it },
                            label = { Text("Destino ou Identificação do Trajeto") },
                            placeholder = { Text("Ex: Voltando para casa, Uber placa...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )

                        Text(
                            text = "Tempo estimado de chegada:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = DeepPlum,
                        )

                        val durationOptions = listOf(15, 30, 45, 60)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            durationOptions.forEach { mins ->
                                val selected = selectedDurationMin == mins
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selected) DeepPlum else OffWhiteBackground,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedDurationMin = mins },
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = "${mins}m",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selected) Color.White else DeepPlum,
                                        )
                                    }
                                }
                            }
                        }

                        PrimaryButton(
                            text = "Iniciar Trajeto e Avisar Círculo",
                            onClick = {
                                runWithLocationPermission {
                                    viewModel.startTransitProtection(
                                        destinationLabel = destinationInput,
                                        durationMinutes = selectedDurationMin,
                                        notifyCircleNow = true,
                                    ) { shareIntent ->
                                        runCatching { context.startActivity(shareIntent) }
                                    }
                                }
                            },
                        )

                        OutlinedButton(
                            onClick = {
                                viewModel.startTransitProtection(
                                    destinationLabel = destinationInput,
                                    durationMinutes = selectedDurationMin,
                                    notifyCircleNow = false,
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Iniciar Monitoramento Silencioso (${selectedDurationMin} min)")
                        }
                    }
                }
            }

            // =========================================================================
            // PILAR 3: GRAVAÇÃO DE ÁUDIO FORENSE & CAMUFLAGEM DE CONTINGÊNCIA
            // =========================================================================
            Text(
                text = "Cofre de Áudio Forense & Camuflagem",
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (state.isRecordingAudio) MaterialTheme.colorScheme.errorContainer else SoftPeachBg),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = if (state.isRecordingAudio) MaterialTheme.colorScheme.error else WarmTerracotta,
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Gravação de Áudio Discreta",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = DeepPlum,
                            )
                            Text(
                                text = if (state.isRecordingAudio) {
                                    "Gravando áudio em diretório privado protegido..."
                                } else {
                                    "O arquivo não aparece na galeria pública do celular"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    if (state.isRecordingAudio) {
                        Button(
                            onClick = { viewModel.stopAudioEvidence() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Parar e Salvar no Cofre Privado")
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                if (state.hasAudioPermission) {
                                    viewModel.startAudioEvidence()
                                } else {
                                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Iniciar Gravação Discreta de Ambiente")
                        }
                    }

                    if (state.forensicAudioRecords.isNotEmpty()) {
                        HorizontalDivider()
                        Text(
                            text = "Gravações Salvas no Cofre (${state.forensicAudioRecords.size})",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = DeepPlum,
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.forensicAudioRecords.forEach { record ->
                                ForensicAudioRecordRow(
                                    record = record,
                                    isPlaying = state.playingAudioPath == record.filePath,
                                    onPlayStop = { viewModel.togglePlayAudioRecord(record.filePath) },
                                    onShare = {
                                        viewModel.shareAudioRecord(record.filePath) { shareIntent ->
                                            runCatching { context.startActivity(shareIntent) }
                                        }
                                    },
                                    onDelete = { viewModel.deleteAudioRecord(record.filePath) },
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    OutlinedButton(
                        onClick = { viewModel.setDisguised(true) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.VisibilityOff, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ativar Tela de Camuflagem Rápida (Hidratação)")
                    }
                }
            }

            // =========================================================================
            // PILAR 4: AVALIAÇÃO PREVENTIVA DE RISCO (IAVP - 13 ETAPAS)
            // =========================================================================
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
                                text = "Questionário sigiloso em 13 etapas • Sem rastro local",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Text(
                        text = "Identifique sinais de vulnerabilidade relacional e receba orientações personalizadas para seu plano de proteção.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    PrimaryButton(
                        text = "Iniciar Avaliação Preventiva",
                        onClick = onStartIavp,
                    )
                }
            }

            // =========================================================================
            // PILAR 5: CANAIS OFICIAIS DE ATENDIMENTO E DELEGACIAS DA MULHER (DEAM)
            // =========================================================================
            AppCard {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Rede Oficial de Proteção à Mulher",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = DeepPlum,
                    )
                    EmergencyCallRow(
                        title = "Central de Atendimento à Mulher",
                        subtitle = "Orientação 24h gratuita e confidencial",
                        phone = "180",
                    )
                    EmergencyCallRow(
                        title = "Polícia Militar (Emergência)",
                        subtitle = "Acionamento imediato de viatura",
                        phone = "190",
                    )
                    HorizontalDivider()
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://www.google.com/maps/search/Delegacia+da+Mulher+DEAM"),
                            )
                            runCatching { context.startActivity(intent) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Localizar Delegacia da Mulher (DEAM) Próxima")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SafetyHeroShowcaseCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFBE9E7),
                        Color(0xFFF5EBF7),
                    ),
                ),
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.8f),
                shape = RoundedCornerShape(24.dp),
            )
            .padding(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Pill "Segurança Pessoal"
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.92f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(WarmTerracotta),
                    )
                    Text(
                        text = "Segurança Pessoal",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = WarmTerracotta,
                    )
                }
            }

            // Headline
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White.copy(alpha = 0.95f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Cuidar de você também é garantir que você chegue bem em casa.",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = PlayfairDisplay,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 28.sp,
                        ),
                        color = DeepPlum,
                    )
                    Text(
                        text = "Recursos discretos de segurança feminina e rede de apoio integrados ao app, porque seu bem-estar completo vem em primeiro lugar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Destaques rápidos dos dois pilares
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White.copy(alpha = 0.95f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SoftPeachBg,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(WarmTerracotta),
                                )
                                Text(
                                    text = "Círculo de Confiança Sempre Ativo",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = WarmTerracotta,
                                )
                            }
                            Text(
                                text = "Compartilhamento seguro e alerta discreto para quem cuida de você.",
                                style = MaterialTheme.typography.bodySmall,
                                color = DeepPlum,
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SoftLavenderBg,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(DeepVioletAccent),
                                )
                                Text(
                                    text = "Modo Discreto & Proteção em Trânsito",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepVioletAccent,
                                )
                            }
                            Text(
                                text = "Acompanhamento de trajeto pensado para a tranquilidade da sua rotina.",
                                style = MaterialTheme.typography.bodySmall,
                                color = DeepPlum,
                            )
                        }
                    }

                    Text(
                        text = "Tecnologia empática desenhada por quem prioriza a vida real.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitleWithBadge(
    dotColor: Color,
    title: String,
    subtitle: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(dotColor),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepPlum,
            )
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TrustedContactItemRow(
    contact: TrustedContact,
    onCall: () -> Unit,
    onRemove: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = OffWhiteBackground,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SoftPeachBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = contact.name.firstOrNull()?.uppercaseChar()?.toString() ?: "C",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = WarmTerracotta,
                    )
                }
                Column {
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = DeepPlum,
                    )
                    Text(
                        text = contact.phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCall) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Ligar para ${contact.name}",
                        tint = DeepPlum,
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remover ${contact.name}",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ForensicAudioRecordRow(
    record: ForensicAudioRecord,
    isPlaying: Boolean,
    onPlayStop: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
) {
    val formattedDate = remember(record.createdAtMillis) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(record.createdAtMillis))
    }
    val sizeKb = (record.sizeBytes / 1024L).coerceAtLeast(1L)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = OffWhiteBackground,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = DeepPlum,
                )
                Text(
                    text = "${record.fileName} • ${sizeKb} KB",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPlayStop) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Parar reprodução" else "Ouvir gravação",
                        tint = DeepPlum,
                    )
                }
                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Compartilhar evidência",
                        tint = DeepPlum,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir gravação",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmergencyCallRow(
    title: String,
    subtitle: String,
    phone: String,
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = DeepPlum,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        OutlinedButton(
            onClick = {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                context.startActivity(intent)
            },
        ) {
            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.size(6.dp))
            Text(phone, fontWeight = FontWeight.Bold)
        }
    }
}
