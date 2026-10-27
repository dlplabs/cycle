package br.com.dlpsystems.cycle.presentation.sos

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.dlpsystems.cycle.R
import br.com.dlpsystems.cycle.core.designsystem.AppCard
import br.com.dlpsystems.cycle.core.designsystem.DeepPlum
import br.com.dlpsystems.cycle.core.designsystem.HeaderSage
import br.com.dlpsystems.cycle.core.designsystem.MenstrualTerracotta
import br.com.dlpsystems.cycle.core.designsystem.OffWhiteBackground
import br.com.dlpsystems.cycle.core.designsystem.PrimaryButton
import br.com.dlpsystems.cycle.data.local.UserPreferencesDataSource
import br.com.dlpsystems.cycle.data.remote.AnalyticsEvents
import br.com.dlpsystems.cycle.data.remote.AnalyticsService
import br.com.dlpsystems.cycle.domain.model.Citation
import br.com.dlpsystems.cycle.domain.model.LifeStageMode
import br.com.dlpsystems.cycle.domain.repository.BillingRepository
import br.com.dlpsystems.cycle.presentation.components.AdBannerContainer
import br.com.dlpsystems.cycle.presentation.components.CoachMarkOverlay
import br.com.dlpsystems.cycle.presentation.components.ScientificSourceBadge
import br.com.dlpsystems.cycle.presentation.components.ScreenHeader
import br.com.dlpsystems.cycle.presentation.components.coachRoot
import br.com.dlpsystems.cycle.presentation.components.coachTarget
import br.com.dlpsystems.cycle.presentation.components.rememberCoachMark
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay
import javax.inject.Inject

@Composable
fun SosReliefScreen(
    onAccount: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    viewModel: SosViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val activeLifeStage by viewModel.activeLifeStageMode.collectAsStateWithLifecycle(initialValue = LifeStageMode.REGULAR_CYCLE)

    var selectedTab by remember { mutableIntStateOf(0) }
    var userChangedTab by remember { mutableStateOf(false) }

    LaunchedEffect(activeLifeStage) {
        if (!userChangedTab) {
            selectedTab = when (activeLifeStage) {
                LifeStageMode.PERIMENOPAUSE, LifeStageMode.MENOPAUSE -> 1
                LifeStageMode.POSTPARTUM -> 2
                else -> 0
            }
        }
    }

    var minutes by remember { mutableIntStateOf(25) }
    var remaining by remember { mutableLongStateOf(0L) }
    var totalDuration by remember { mutableLongStateOf(25 * 60_000L) }
    var timerCompleted by remember { mutableStateOf(false) }
    var breathing by remember { mutableIntStateOf(0) }
    var breatheText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val analytics = EntryPointAccessors.fromApplication(
            context.applicationContext,
            SosEntryPoint::class.java,
        ).analytics()
        analytics.log(AnalyticsEvents.EVENT_SOS_OPENED, screen = "sos")
    }

    LaunchedEffect(remaining) {
        if (remaining <= 0L) return@LaunchedEffect
        delay(1000)
        val next = remaining - 1000
        if (next <= 0L) {
            remaining = 0L
            timerCompleted = true
            pulse(context, 120)
        } else {
            remaining = next
        }
    }

    val inhaleText = stringResource(R.string.sos_breathe_inhale)
    val holdText = stringResource(R.string.sos_breathe_hold)
    val exhaleText = stringResource(R.string.sos_breathe_exhale)

    LaunchedEffect(breathing, selectedTab, inhaleText, holdText, exhaleText) {
        if (breathing == 0) {
            breatheText = ""
            return@LaunchedEffect
        }
        val pattern = when (selectedTab) {
            1 -> listOf(
                // Paced Breathing (6 respirações/minuto para controle de fogachos)
                Triple(5, 50, "Inspire devagar pelo nariz"),
                Triple(5, 100, "Expire suavemente pela boca"),
            )
            2 -> listOf(
                // Respiração calmante puerperal 4-4-6
                Triple(4, 40, "Inspire acolhendo o ar"),
                Triple(4, 70, "Pausa suave"),
                Triple(6, 120, "Expire soltando os ombros"),
            )
            else -> listOf(
                // Técnica 4-7-8 clássica para cólica e ansiedade
                Triple(4, 40, inhaleText),
                Triple(7, 80, holdText),
                Triple(8, 160, exhaleText),
            )
        }
        while (breathing > 0) {
            pattern.forEach { (seconds, amplitude, instruction) ->
                breatheText = "$instruction ($seconds s)"
                pulse(context, amplitude)
                delay(seconds * 1000L)
            }
        }
    }

    val coach = rememberCoachMark("relief")
    Box(
        modifier = Modifier
            .fillMaxSize()
            .coachRoot(coach),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(
                title = stringResource(R.string.sos_title),
                onAccount = onAccount,
                onBack = onBack,
            )

            // Seletor de Modalidade SOS Alívio (ADR-0009)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = OffWhiteBackground,
                contentColor = DeepPlum,
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        userChangedTab = true
                        breathing = 0
                    },
                    text = { Text("Cólicas & TPM", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        userChangedTab = true
                        breathing = 0
                    },
                    text = { Text("Fogacho & Climatério", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        userChangedTab = true
                        breathing = 0
                    },
                    text = { Text("Apoio Emocional", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when (selectedTab) {
                    0 -> {
                        // Modalidade 1: Cólicas e TPM (Pavlov et al., 2020)
                        AppCard {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_toalha_flor),
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp),
                                        tint = Color.Unspecified,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        painter = painterResource(R.drawable.ic_cha),
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp),
                                        tint = Color.Unspecified,
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Bolsa de Calor & Chá Morno",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = DeepPlum,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.sos_heat, minutes),
                                    style = MaterialTheme.typography.bodyMedium,
                                )

                                if (remaining > 0L) {
                                    val mins = remaining / 60_000L
                                    val secs = (remaining % 60_000L) / 1000L
                                    val formattedTime = String.format("%02d:%02d", mins, secs)
                                    val progress = if (totalDuration > 0) {
                                        ((totalDuration - remaining).toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)
                                    } else 0f

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Text(
                                            text = formattedTime,
                                            style = MaterialTheme.typography.displaySmall,
                                            color = DeepPlum,
                                        )
                                        androidx.compose.material3.LinearProgressIndicator(
                                            progress = { progress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = MenstrualTerracotta,
                                            trackColor = OffWhiteBackground,
                                        )
                                    }

                                    PrimaryButton(
                                        text = stringResource(R.string.sos_stop_timer),
                                        onClick = {
                                            remaining = 0L
                                            timerCompleted = false
                                        },
                                        modifier = Modifier.coachTarget(coach),
                                    )
                                } else {
                                    if (timerCompleted) {
                                        Text(
                                            text = stringResource(R.string.sos_timer_finished),
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MenstrualTerracotta,
                                        )
                                    }
                                    Slider(
                                        value = minutes.toFloat(),
                                        onValueChange = { minutes = it.toInt() },
                                        valueRange = 20f..30f,
                                        steps = 9,
                                    )
                                    PrimaryButton(
                                        text = stringResource(R.string.sos_start_timer, minutes),
                                        onClick = {
                                            totalDuration = minutes * 60_000L
                                            remaining = totalDuration
                                            timerCompleted = false
                                        },
                                        modifier = Modifier.coachTarget(coach),
                                    )
                                }
                            }
                        }

                        // Respiração 4-7-8
                        AppCard {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text(
                                    text = "Respiração 4-7-8",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = DeepPlum,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = stringResource(R.string.sos_breath),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                if (breatheText.isNotBlank()) {
                                    Text(
                                        text = breatheText,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = DeepPlum,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                                PrimaryButton(
                                    text = stringResource(if (breathing == 0) R.string.sos_start_breath else R.string.sos_stop_breath),
                                    onClick = { breathing = if (breathing == 0) 1 else 0 },
                                )
                            }
                        }
                    }

                    1 -> {
                        // Modalidade 2: Fogacho & Climatério (Freedman & Woodward, 1992, N > 1.000)
                        AppCard {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text(
                                    text = "Respiração Lenta Cadenciada (Paced Breathing)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = DeepPlum,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "Prática de 6 respirações por minuto (5s inspira, 5s expira). Ensaios clínicos demonstraram redução de até 50% na intensidade dos fogachos através da estimulação do nervo vago e modulação autonômica.",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                if (breatheText.isNotBlank()) {
                                    Text(
                                        text = breatheText,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = DeepPlum,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                                PrimaryButton(
                                    text = if (breathing == 0) "Iniciar Respiração Cadenciada" else "Parar Respiração",
                                    onClick = { breathing = if (breathing == 0) 1 else 0 },
                                )
                            }
                        }

                        AppCard {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = "Dicas Imediatas para Ondas de Calor",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepPlum,
                                )
                                Text("• Beba um copo de água fresca em pequenos goles.", style = MaterialTheme.typography.bodySmall)
                                Text("• Aplique uma toalha fresca ou água fria na nuca e pulsos.", style = MaterialTheme.typography.bodySmall)
                                Text("• Mantenha o ambiente ventilado e use roupas em camadas leves de algodão.", style = MaterialTheme.typography.bodySmall)
                                ScientificSourceBadge(
                                    citation = Citation(
                                        authors = "Freedman, R. R.; Woodward, S.",
                                        source = "Am J Obstet Gynecol / NAMS (N > 1.000)",
                                        finding = "Eficácia da respiração lenta cadenciada na redução da frequência e severidade de fogachos vasomotores.",
                                        url = "https://doi.org/10.1016/0002-9378(92)91620-7",
                                    ),
                                    onOpen = {},
                                )
                            }
                        }
                    }

                    2 -> {
                        // Modalidade 3: Apoio Pós-Parto & Emoção (Santos et al., 2007, N = 4.453)
                        AppCard {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text(
                                    text = "Apoio Emocional Imediato (CVV 188)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = DeepPlum,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "O pós-parto e puerpério trazem intensas mudanças hormonais e privação de sono. Se estiver se sentindo sobrecarregada, ansiosa ou em sofrimento, você pode conversar com um voluntário acolhedor a qualquer momento.",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:188"))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepPlum),
                                ) {
                                    Text("Ligar para o CVV 188 — Ligação Gratuita 24h", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        AppCard {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text(
                                    text = "Respiração Acolhedora 4-4-6",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepPlum,
                                )
                                Text(
                                    text = "Inspire em 4 segundos, retenha suavemente por 4 segundos e expire em 6 segundos soltando o peso dos ombros e relaxando a musculatura pélvica.",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                if (breatheText.isNotBlank()) {
                                    Text(
                                        text = breatheText,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = DeepPlum,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                                PrimaryButton(
                                    text = if (breathing == 0) "Iniciar Respiração Calmante" else "Parar Respiração",
                                    onClick = { breathing = if (breathing == 0) 1 else 0 },
                                )
                            }
                        }
                    }
                }

                if (remaining == 0L && breathing == 0) {
                    val premium by viewModel.isPremium.collectAsStateWithLifecycle()
                    AdBannerContainer(isPremium = premium)
                }

                if (onBack != null) {
                    PrimaryButton(text = stringResource(R.string.back), onClick = onBack)
                }
            }
        }
        CoachMarkOverlay(
            state = coach,
            message = stringResource(R.string.coach_relief),
        )
    }
}

private fun pulse(context: Context, amplitude: Int) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    } ?: return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val effect = if (vibrator.hasAmplitudeControl()) {
            VibrationEffect.createOneShot(180, amplitude.coerceIn(1, 255))
        } else {
            VibrationEffect.createOneShot(180, VibrationEffect.DEFAULT_AMPLITUDE)
        }
        vibrator.vibrate(effect)
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SosEntryPoint {
    fun analytics(): AnalyticsService
}

@HiltViewModel
class SosViewModel @Inject constructor(
    billingRepository: BillingRepository,
    preferences: UserPreferencesDataSource,
) : ViewModel() {
    val isPremium = billingRepository.isPremiumUser
    val activeLifeStageMode = preferences.activeLifeStageMode
}
