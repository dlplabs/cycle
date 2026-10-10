package br.com.dlpsystems.cycle.domain.model

import java.time.LocalDate
import java.time.LocalTime

enum class BodyRegion {
    HEAD,       // Cabeça (Enxaqueca, cefaleia tensional, tontura)
    BREASTS,    // Mamas (Mastalgia, inchaço, sensibilidade ao toque)
    ABDOMEN,    // Abdômen (Inchaço epigástrico, gases, refluxo)
    PELVIS,     // Pelve (Cólicas miometriais, peso no baixo ventre, pontadas)
    BACK,       // Costas / Lombar (Dor lombar baixa, tensão escapular)
    LIMBS,      // Membros (Pernas pesadas, retenção em tornozelos, dores articulares)
    GENERAL,    // Gerais (Fadiga, calafrios, ondas de calor, alteração de apetite)
    EMOTIONAL,  // Emocionais (Sensibilidade, irritabilidade, choro fácil, ansiedade)
}

data class BodySymptomEntry(
    val id: String,
    val date: LocalDate,
    val time: LocalTime? = null,
    val region: BodyRegion,
    val symptomName: String,
    val intensity: Int, // 1 (Leve), 2 (Moderada), 3 (Intensa)
    val durationMinutes: Int? = null,
    val notes: String = "",
)
