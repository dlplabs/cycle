package br.com.dlpsystems.cycle

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import br.com.dlpsystems.cycle.core.designsystem.CycleTheme
import br.com.dlpsystems.cycle.presentation.CycleApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var startPeriod by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        consumeStartPeriod(intent)
        enableEdgeToEdge()
        setContent {
            CycleTheme {
                CycleApp(
                    startPeriodOnOpen = startPeriod,
                    onStartPeriodConsumed = { startPeriod = false },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        consumeStartPeriod(intent)
    }

    private fun consumeStartPeriod(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_START_PERIOD, false) == true) {
            startPeriod = true
            intent.removeExtra(EXTRA_START_PERIOD)
        }
    }

    companion object {
        const val EXTRA_START_PERIOD = "start_period"
    }
}
