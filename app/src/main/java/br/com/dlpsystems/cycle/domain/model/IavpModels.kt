package br.com.dlpsystems.cycle.domain.model

import androidx.annotation.Keep

@Keep
enum class IavpRiskLevel {
    EXTREMO,
    GRAVE,
    MODERADO,
    BAIXO,
}

@Keep
data class IavpQuestion(
    val id: String,
    val category: Int,
    val weight: Int,
    val questionText: String,
    val detailText: String,
)

@Keep
data class IavpAnswers(
    val answersMap: Map<String, Boolean> = emptyMap(),
)

enum class IavpActionType {
    CALL_POLICE,          // 190
    CALL_WOMEN_CENTER,    // 180
    CALL_EMERGENCY_CONTACT,
    DEAM_LOCATION,
    SAFETY_PLAN,
}

@Keep
data class IavpQuickAction(
    val title: String,
    val description: String,
    val phoneNumber: String? = null,
    val actionType: IavpActionType,
)

@Keep
data class IavpEvaluationResult(
    val score: Int,
    val riskLevel: IavpRiskLevel,
    val fatalTriggerActivated: Boolean,
    val summaryTitle: String,
    val summaryDescription: String,
    val recommendations: List<String>,
    val quickActions: List<IavpQuickAction>,
)
