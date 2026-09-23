package com.seuapp.cycle.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CycleLightColorScheme = lightColorScheme(
    primary = DeepPlum,
    onPrimary = Color.White,
    secondary = LutealLavender, // Uma cor secundária de apoio
    background = OffWhiteBackground,
    surface = SurfaceCard,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun CycleTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CycleLightColorScheme, // Focado no Light Mode para manter o aspecto "clean"
        typography = CycleTypography,
        content = content
    )
}