package br.com.dlpsystems.cycle.presentation.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import br.com.dlpsystems.cycle.MainActivity
import br.com.dlpsystems.cycle.R
import br.com.dlpsystems.cycle.data.local.UserPreferencesDataSource
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first

class CycleGlanceWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val preferences = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java,
        ).preferences()
        val day = preferences.widgetDay.first()
        val phaseText = preferences.widgetPhase.first()

        val phaseColor = when (phaseText.uppercase()) {
            "MENSTRUAL", "MENSTRUAÇÃO" -> Color(0xFFD07C70)
            "FOLLICULAR", "FOLICULAR" -> Color(0xFF8DB094)
            "OVULATORY", "OVULATÓRIA" -> Color(0xFFF4B886)
            "LUTEAL", "LÚTEA" -> Color(0xFFBCA6CE)
            else -> Color(0xFFBCA6CE)
        }

        val deepPlum = Color(0xFF4A2B4D)

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(Color.White))
                    .cornerRadius(16.dp)
                    .padding(14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Header / Fase Badge
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = GlanceModifier
                                .size(10.dp)
                                .background(ColorProvider(phaseColor))
                                .cornerRadius(5.dp),
                        ) {}
                        Spacer(modifier = GlanceModifier.size(6.dp))
                        Text(
                            text = phaseText.ifBlank { context.getString(R.string.phase_unknown) },
                            style = TextStyle(
                                color = ColorProvider(deepPlum),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(8.dp))

                    // Conteúdo Central: Dia do Ciclo
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (day == 0) "-" else "$day",
                            style = TextStyle(
                                color = ColorProvider(deepPlum),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                        Text(
                            text = if (day == 0) context.getString(R.string.widget_empty) else "Dia do Ciclo",
                            style = TextStyle(
                                color = ColorProvider(Color(0xFF707070)),
                                fontSize = 11.sp,
                            ),
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(10.dp))

                    // Ação Rápida
                    Button(
                        text = context.getString(R.string.widget_period_started),
                        onClick = actionRunCallback<StartPeriodAction>(),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = ColorProvider(deepPlum),
                            contentColor = ColorProvider(Color.White),
                        ),
                        modifier = GlanceModifier.fillMaxWidth().height(36.dp),
                    )
                }
            }
        }
    }
}

class StartPeriodAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        context.startActivity(
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(MainActivity.EXTRA_START_PERIOD, true)
            },
        )
    }
}

class CycleGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CycleGlanceWidget()
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun preferences(): UserPreferencesDataSource
}
