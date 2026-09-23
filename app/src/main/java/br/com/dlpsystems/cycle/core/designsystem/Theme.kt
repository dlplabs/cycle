package br.com.dlpsystems.cycle.core.designsystem

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import br.com.dlpsystems.cycle.domain.model.CyclePhase

private val CycleLightColorScheme = lightColorScheme(
    primary = DeepPlum,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF2EAF3),
    onPrimaryContainer = DeepPlum,
    secondary = LutealLavender,
    onSecondary = DeepPlum,
    secondaryContainer = Color(0xFFF5F1F8),
    onSecondaryContainer = DeepPlum,
    tertiary = FollicularSage,
    onTertiary = Color.White,
    background = OffWhiteBackground,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF0ECE9),
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFFDDD7D3),
    outlineVariant = Color(0xFFEDE8E5),
)

private val CycleDarkColorScheme = darkColorScheme(
    primary = Color(0xFFD6B5DA),
    onPrimary = DeepPlum,
    primaryContainer = DeepPlumLight,
    onPrimaryContainer = Color.White,
    secondary = LutealLavender,
    onSecondary = DeepPlum,
    background = PhaseColors.neutralDarkBackground,
    onBackground = PhaseColors.neutralDarkOnSurface,
    surface = PhaseColors.neutralDarkSurface,
    onSurface = PhaseColors.neutralDarkOnSurface,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleTheme(
    phase: CyclePhase = CyclePhase.LUTEAL,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val palette = PhaseColors.forPhase(phase, darkTheme)
    val dynamic = dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val context = LocalContext.current
    val baseScheme = when {
        dynamic && darkTheme -> dynamicDarkColorScheme(context)
        dynamic -> dynamicLightColorScheme(context)
        darkTheme -> CycleDarkColorScheme
        else -> CycleLightColorScheme
    }

    val colorScheme = if (!dynamic && !darkTheme) {
        baseScheme.copy(
            secondary = palette.primary,
            secondaryContainer = palette.container,
            onSecondaryContainer = palette.onContainer,
        )
    } else {
        baseScheme
    }

    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 48.dp) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = CycleTypography,
            shapes = CycleShapes,
            content = content,
        )
    }
}
