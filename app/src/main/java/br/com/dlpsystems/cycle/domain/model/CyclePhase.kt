package br.com.dlpsystems.cycle.domain.model

enum class CyclePhase {
    MENSTRUAL,
    FOLLICULAR,
    OVULATORY,
    LUTEAL,
}

enum class Mood {
    CALM,
    ENERGETIC,
    TIRED,
    ANXIOUS,
    IRRITABLE,
    SENSITIVE,
}

enum class SkinCondition {
    BALANCED,
    DRY,
    OILY,
    BREAKOUT,
    SENSITIVE,
}

enum class FlowIntensity {
    LIGHT,
    MEDIUM,
    HEAVY,
    NONE,
}

enum class Symptom {
    CRAMPS,
    HEADACHE,
    BLOATING,
    FATIGUE,
    BREAST_TENDERNESS,
    BACK_PAIN,
    NAUSEA,
    CRAVINGS,
}

enum class WellnessPillar {
    NUTRITION,
    EXERCISE,
    SKIN,
    MIND,
}

enum class PhaseStatus {
    IN_PHASE,
    EXTENDED,
    NO_CYCLE,
}
