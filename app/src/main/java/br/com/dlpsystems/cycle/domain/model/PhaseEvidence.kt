package br.com.dlpsystems.cycle.domain.model

data class Citation(
    val authors: String,
    val source: String,
    val finding: String,
    val url: String,
)

enum class SubscriptionState {
    FREE,
    PREMIUM,
}

data class ScientificEvidence(
    val authors: String,
    val source: String,
    val summary: String,
    val url: String,
)

data class PillarGuidance(
    val pillar: WellnessPillar,
    val guidance: String,
    val citation: Citation?,
)

data class PhaseEvidence(
    val phase: CyclePhase,
    val physiology: String,
    val pillars: List<PillarGuidance>,
)
