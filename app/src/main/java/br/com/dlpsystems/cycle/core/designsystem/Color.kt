package br.com.dlpsystems.cycle.core.designsystem

import androidx.compose.ui.graphics.Color
import br.com.dlpsystems.cycle.domain.model.CyclePhase

// Cores Principais da Marca (Wellness Premium)
val DeepPlum = Color(0xFF4A2B4D)
val DeepPlumLight = Color(0xFF6E4572)
val OffWhiteBackground = Color(0xFFF9F7F6)
val SurfaceCard = Color(0xFFFFFFFF)

// Cores das Fases do Ciclo (Orgânicas e Suaves)
val MenstrualTerracotta = Color(0xFFD07C70)
val FollicularSage = Color(0xFF8DB094)
val OvulatoryPeach = Color(0xFFF4B886)
val LutealLavender = Color(0xFFBCA6CE)

// Cores de Texto e Apoio
val TextPrimary = Color(0xFF2C2C2C)
val TextSecondary = Color(0xFF707070)

data class PhasePalette(
    val primary: Color,
    val onPrimary: Color,
    val container: Color,
    val onContainer: Color,
)

object PhaseColors {
    val Menstrual = MenstrualTerracotta
    val Follicular = FollicularSage
    val Ovulatory = OvulatoryPeach
    val Luteal = LutealLavender

    private val menstrualLight = PhasePalette(
        primary = MenstrualTerracotta,
        onPrimary = Color.White,
        container = Color(0xFFFBF0EE),
        onContainer = Color(0xFF4A1515),
    )
    private val menstrualDark = PhasePalette(
        primary = Color(0xFFE28B8B),
        onPrimary = Color(0xFF4A1515),
        container = Color(0xFF5C2424),
        onContainer = Color(0xFFFCEAEA),
    )

    private val follicularLight = PhasePalette(
        primary = FollicularSage,
        onPrimary = Color.White,
        container = Color(0xFFF0F5F1),
        onContainer = Color(0xFF143823),
    )
    private val follicularDark = PhasePalette(
        primary = Color(0xFF7EC69E),
        onPrimary = Color(0xFF143823),
        container = Color(0xFF1C4A30),
        onContainer = Color(0xFFEAF6EE),
    )

    private val ovulatoryLight = PhasePalette(
        primary = OvulatoryPeach,
        onPrimary = DeepPlum,
        container = Color(0xFFFDF5ED),
        onContainer = Color(0xFF4D2C05),
    )
    private val ovulatoryDark = PhasePalette(
        primary = Color(0xFFE8A962),
        onPrimary = Color(0xFF4D2C05),
        container = Color(0xFF5A360E),
        onContainer = Color(0xFFFDF4E7),
    )

    private val lutealLight = PhasePalette(
        primary = LutealLavender,
        onPrimary = DeepPlum,
        container = Color(0xFFF5F1F8),
        onContainer = Color(0xFF281D42),
    )
    private val lutealDark = PhasePalette(
        primary = Color(0xFFB3A2D9),
        onPrimary = Color(0xFF281D42),
        container = Color(0xFF3B2C5C),
        onContainer = Color(0xFFF3EFFB),
    )

    val neutralLightBackground = OffWhiteBackground
    val neutralLightSurface = SurfaceCard
    val neutralLightOnSurface = TextPrimary
    val neutralDarkBackground = Color(0xFF19141A)
    val neutralDarkSurface = Color(0xFF261F27)
    val neutralDarkOnSurface = Color(0xFFE6E1E5)

    fun forPhase(phase: CyclePhase, darkTheme: Boolean = false): PhasePalette = when (phase) {
        CyclePhase.MENSTRUAL -> if (darkTheme) menstrualDark else menstrualLight
        CyclePhase.FOLLICULAR -> if (darkTheme) follicularDark else follicularLight
        CyclePhase.OVULATORY -> if (darkTheme) ovulatoryDark else ovulatoryLight
        CyclePhase.LUTEAL -> if (darkTheme) lutealDark else lutealLight
    }
}
