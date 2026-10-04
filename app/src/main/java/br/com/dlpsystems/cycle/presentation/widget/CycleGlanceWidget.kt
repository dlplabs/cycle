package br.com.dlpsystems.cycle.presentation.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
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
    override val sizeMode: SizeMode = SizeMode.Exact

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
            val size = LocalSize.current
            val currentWidth = size.width
            val currentHeight = size.height

            // Layout adaptativo:
            // 1. Horizontal / Baixo e Largo (ex: 3x1, 4x1 com width >= 230dp e height < 135dp)
            // 2. Vertical / Padrão (height >= 135dp)
            // 3. Compacto (width < 230dp e height < 135dp)
            val isHorizontal = currentWidth >= 230.dp && currentHeight < 135.dp
            val showButton = when {
                isHorizontal -> currentHeight >= 85.dp
                currentHeight < 105.dp -> false
                else -> true
            }

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(Color.White))
                    .cornerRadius(16.dp)
                    .clickable(actionStartActivity<MainActivity>())
                    .padding(if (currentHeight < 100.dp) 8.dp else 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (isHorizontal) {
                    HorizontalWidgetContent(
                        day = day,
                        phaseText = phaseText,
                        phaseColor = phaseColor,
                        deepPlum = deepPlum,
                        height = currentHeight,
                        showButton = showButton,
                        context = context,
                    )
                } else {
                    VerticalWidgetContent(
                        day = day,
                        phaseText = phaseText,
                        phaseColor = phaseColor,
                        deepPlum = deepPlum,
                        width = currentWidth,
                        height = currentHeight,
                        showButton = showButton,
                        context = context,
                    )
                }
            }
        }
    }
}

@Composable
private fun HorizontalWidgetContent(
    day: Int,
    phaseText: String,
    phaseColor: Color,
    deepPlum: Color,
    height: Dp,
    showButton: Boolean,
    context: Context,
) {
    val wheelSize = (height - 16.dp).coerceIn(56.dp, 100.dp)
    Row(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WheelCenterContent(
            day = day,
            phaseText = phaseText,
            phaseColor = phaseColor,
            deepPlum = deepPlum,
            wheelSize = wheelSize,
            context = context,
        )
        Spacer(modifier = GlanceModifier.width(12.dp))
        Column(
            modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = if (day == 0) context.getString(R.string.widget_empty) else "Dia do Ciclo",
                style = TextStyle(
                    color = ColorProvider(Color(0xFF707070)),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                ),
                maxLines = 1,
            )
            if (showButton) {
                Spacer(modifier = GlanceModifier.height(6.dp))
                Button(
                    text = context.getString(R.string.widget_period_started),
                    onClick = actionRunCallback<StartPeriodAction>(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = ColorProvider(deepPlum),
                        contentColor = ColorProvider(Color.White),
                    ),
                    modifier = GlanceModifier.fillMaxWidth().height(32.dp),
                )
            }
        }
    }
}

@Composable
private fun VerticalWidgetContent(
    day: Int,
    phaseText: String,
    phaseColor: Color,
    deepPlum: Color,
    width: Dp,
    height: Dp,
    showButton: Boolean,
    context: Context,
) {
    val buttonReserve = if (showButton) (if (height < 140.dp) 38.dp else 48.dp) else 0.dp
    val availableWheelHeight = height - (if (height < 100.dp) 16.dp else 24.dp) - buttonReserve
    val wheelSize = minOf(width - 24.dp, availableWheelHeight).coerceIn(52.dp, 160.dp)

    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            WheelCenterContent(
                day = day,
                phaseText = phaseText,
                phaseColor = phaseColor,
                deepPlum = deepPlum,
                wheelSize = wheelSize,
                context = context,
            )
        }
        if (showButton) {
            Spacer(modifier = GlanceModifier.height(if (height < 140.dp) 4.dp else 8.dp))
            Button(
                text = context.getString(R.string.widget_period_started),
                onClick = actionRunCallback<StartPeriodAction>(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = ColorProvider(deepPlum),
                    contentColor = ColorProvider(Color.White),
                ),
                modifier = GlanceModifier.fillMaxWidth().height(if (height < 140.dp) 32.dp else 36.dp),
            )
        }
    }
}

@Composable
private fun WheelCenterContent(
    day: Int,
    phaseText: String,
    phaseColor: Color,
    deepPlum: Color,
    wheelSize: Dp,
    context: Context,
) {
    val dayFontSize = when {
        wheelSize < 70.dp -> 16.sp
        wheelSize < 90.dp -> 20.sp
        wheelSize < 120.dp -> 26.sp
        wheelSize < 150.dp -> 32.sp
        else -> 38.sp
    }
    val phaseFontSize = when {
        wheelSize < 70.dp -> 8.sp
        wheelSize < 90.dp -> 9.sp
        wheelSize < 120.dp -> 11.sp
        else -> 12.sp
    }
    val dotSize = when {
        wheelSize < 70.dp -> 4.dp
        wheelSize < 90.dp -> 6.dp
        else -> 7.dp
    }

    Box(
        modifier = GlanceModifier.size(wheelSize),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_widget_wheel),
            contentDescription = null,
            modifier = GlanceModifier.size(wheelSize),
            contentScale = ContentScale.Fit,
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (day == 0) "-" else "$day",
                style = TextStyle(
                    color = ColorProvider(deepPlum),
                    fontSize = dayFontSize,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(dotSize)
                        .background(ColorProvider(phaseColor))
                        .cornerRadius(dotSize / 2),
                ) {}
                Spacer(modifier = GlanceModifier.size(3.dp))
                Text(
                    text = phaseText.ifBlank { context.getString(R.string.phase_unknown) },
                    style = TextStyle(
                        color = ColorProvider(deepPlum),
                        fontSize = phaseFontSize,
                        fontWeight = FontWeight.Medium,
                    ),
                    maxLines = 1,
                )
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
