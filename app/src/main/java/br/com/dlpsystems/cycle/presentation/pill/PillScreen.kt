package br.com.dlpsystems.cycle.presentation.pill

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import br.com.dlpsystems.cycle.domain.model.ContraceptiveType
import java.time.LocalDate

@Composable
fun PillScreen(
    onBack: () -> Unit,
    viewModel: PillViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scroll = rememberScrollState()

    val todayTaken = state.records.any { it.scheduledDate == LocalDate.now() && it.isTaken }

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
                    text = stringResource(R.string.pill_alarm_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = DeepPlum),
                )
            }
        },
        containerColor = OffWhiteBackground,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scroll)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Aviso de permissão exata caso necessário no Android 12+
            if (!state.canScheduleExactAlarms) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFF856404))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Permissão de Alarme Exato Necessária",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF856404)),
                            )
                            Text(
                                text = "Para garantir que o alarme toque no minuto correto mesmo com economia de bateria, ative os alarmes exatos.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF856404)),
                            )
                        }
                        TextButton(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                                }
                            },
                        ) {
                            Text("Ativar")
                        }
                    }
                }
            }

            // Status da dose de hoje
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (todayTaken) HeaderSage.copy(alpha = 0.4f) else Color.White,
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "Dose de Hoje",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DeepPlum),
                            )
                            Text(
                                text = "Horário previsto: ${state.config.reminderTime}",
                                style = MaterialTheme.typography.bodyMedium.copy(color = DeepPlum.copy(alpha = 0.8f)),
                            )
                        }
                        if (todayTaken) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Tomada confirmada", tint = DeepPlum)
                        }
                    }

                    if (!todayTaken) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = viewModel::confirmIntakeToday,
                                colors = ButtonDefaults.buttonColors(containerColor = DeepPlum),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text(stringResource(R.string.pill_taken_button), color = Color.White)
                            }
                            OutlinedButton(
                                onClick = { viewModel.snoozeIntake(15) },
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text(stringResource(R.string.pill_snooze_button))
                            }
                        }
                    } else {
                        Text(
                            text = "Tomada registrada com sucesso! Seu histórico foi atualizado.",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = DeepPlum),
                        )
                    }
                }
            }

            // Configuração do Alarme
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Configuração do Lembrete",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DeepPlum),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(stringResource(R.string.pill_alarm_enable), style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = state.config.isEnabled,
                            onCheckedChange = viewModel::toggleEnabled,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(stringResource(R.string.pill_hide_sensitive), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = state.config.hideSensitiveInfoOnLockScreen,
                            onCheckedChange = { hide ->
                                viewModel.updateConfig(
                                    state.config.type,
                                    state.config.reminderTime.hour,
                                    state.config.reminderTime.minute,
                                    hide,
                                )
                            },
                        )
                    }
                }
            }

            // Disclaimer Clínico
            Card(
                colors = CardDefaults.cardColors(containerColor = HeaderSage.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.pill_disclaimer),
                    style = MaterialTheme.typography.bodySmall.copy(color = DeepPlum.copy(alpha = 0.8f)),
                    modifier = Modifier.padding(14.dp),
                )
            }
        }
    }
}
