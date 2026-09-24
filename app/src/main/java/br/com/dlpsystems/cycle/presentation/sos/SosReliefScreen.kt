package br.com.dlpsystems.cycle.presentation.sos

import android.content.Context
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.dlpsystems.cycle.R
import br.com.dlpsystems.cycle.core.designsystem.AppCard
import br.com.dlpsystems.cycle.presentation.components.ScreenHeader
import br.com.dlpsystems.cycle.presentation.components.CoachMarkOverlay
import br.com.dlpsystems.cycle.presentation.components.coachRoot
import br.com.dlpsystems.cycle.presentation.components.coachTarget
import br.com.dlpsystems.cycle.presentation.components.rememberCoachMark
import br.com.dlpsystems.cycle.core.designsystem.DeepPlum
import br.com.dlpsystems.cycle.core.designsystem.PrimaryButton
import br.com.dlpsystems.cycle.data.remote.AnalyticsEvents
import br.com.dlpsystems.cycle.data.remote.AnalyticsService
import br.com.dlpsystems.cycle.domain.repository.BillingRepository
import br.com.dlpsystems.cycle.presentation.components.AdBannerContainer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import kotlinx.coroutines.delay

@Composable
fun SosReliefScreen(onAccount: () -> Unit) {
    val context = LocalContext.current
    var minutes by remember { mutableIntStateOf(25) }
    var remaining by remember { mutableLongStateOf(0L) }
    var breathing by remember { mutableIntStateOf(0) }

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
        remaining -= 1000
    }

    LaunchedEffect(breathing) {
        if (breathing == 0) return@LaunchedEffect
        val pattern = listOf(4 to 40, 7 to 80, 8 to 160)
        while (breathing > 0) {
            pattern.forEach { (seconds, amplitude) ->
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
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

        // Card 1: Heat Therapy
        AppCard {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_toalha_flor),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color.Unspecified,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_cha),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color.Unspecified,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Bolsa de Calor & Chá Morno",
                        style = MaterialTheme.typography.titleMedium,
                        color = DeepPlum,
                    )
                }
                Text(
                    text = stringResource(R.string.sos_heat, minutes),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Slider(
                    value = minutes.toFloat(),
                    onValueChange = { minutes = it.toInt() },
                    valueRange = 20f..30f,
                    steps = 9,
                )
                PrimaryButton(
                    text = if (remaining > 0) stringResource(R.string.sos_remaining, remaining / 1000) else stringResource(R.string.sos_start_timer),
                    onClick = { remaining = minutes * 60_000L },
                    modifier = Modifier.coachTarget(coach),
                )
            }
        }

        // Card 2: Guided Breathing 4-7-8
        AppCard {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_lua_lavanda),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color.Unspecified,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Respiração Guiada 4-7-8",
                        style = MaterialTheme.typography.titleMedium,
                        color = DeepPlum,
                    )
                }
                Text(
                    text = stringResource(R.string.sos_breath),
                    style = MaterialTheme.typography.bodyMedium,
                )
                PrimaryButton(
                    text = stringResource(if (breathing == 0) R.string.sos_start_breath else R.string.sos_stop_breath),
                    onClick = { breathing = if (breathing == 0) 1 else 0 },
                )
            }
        }
        if (remaining == 0L && breathing == 0) {
            val premium by hiltViewModel<SosPremiumViewModel>().isPremium.collectAsStateWithLifecycle()
            AdBannerContainer(isPremium = premium)
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
        vibrator.vibrate(VibrationEffect.createOneShot(180, amplitude.coerceIn(1, 255)))
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SosEntryPoint {
    fun analytics(): AnalyticsService
}

@HiltViewModel
class SosPremiumViewModel @Inject constructor(
    billingRepository: BillingRepository,
) : ViewModel() {
    val isPremium = billingRepository.isPremiumUser
}
