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

@Keep
data class TrustedContact(
    val id: String,
    val name: String,
    val phone: String,
    val relationship: String = "Rede de Apoio",
) {
    fun encode(): String {
        val safeName = name.replace("|", " ").trim().ifBlank { "Contato de Confiança" }
        val safePhone = phone.replace("|", "").trim()
        val safeRel = relationship.replace("|", " ").trim().ifBlank { "Rede de Apoio" }
        return "$id|$safeName|$safePhone|$safeRel"
    }

    companion object {
        fun decode(raw: String): TrustedContact? {
            val parts = raw.split("|")
            return when {
                parts.size >= 4 -> TrustedContact(
                    id = parts[0],
                    name = parts[1].ifBlank { "Contato de Confiança" },
                    phone = parts[2],
                    relationship = parts[3].ifBlank { "Rede de Apoio" },
                )
                parts.size == 3 -> TrustedContact(
                    id = parts[0],
                    name = parts[1].ifBlank { "Contato de Confiança" },
                    phone = parts[2],
                )
                parts.size == 1 && parts[0].isNotBlank() -> {
                    val clean = parts[0].trim()
                    TrustedContact(id = clean, name = "Contato de Confiança", phone = clean)
                }
                else -> null
            }?.takeIf { it.phone.isNotBlank() }
        }
    }
}

@Keep
data class TransitProtectionSession(
    val destinationLabel: String,
    val durationMinutes: Int,
    val startedAtMillis: Long,
    val expiresAtMillis: Long,
) {
    fun remainingSeconds(nowMillis: Long): Long =
        ((expiresAtMillis - nowMillis) / 1000L).coerceAtLeast(0L)

    fun isExpired(nowMillis: Long): Boolean = nowMillis >= expiresAtMillis

    fun encode(): String {
        val safeDest = destinationLabel.replace("|", " ").trim().ifBlank { "Casa" }
        return "$safeDest|$durationMinutes|$startedAtMillis|$expiresAtMillis"
    }

    companion object {
        fun decode(raw: String?): TransitProtectionSession? {
            if (raw.isNullOrBlank()) return null
            val parts = raw.split("|")
            if (parts.size < 4) return null
            val duration = parts[1].toIntOrNull() ?: return null
            val started = parts[2].toLongOrNull() ?: return null
            val expires = parts[3].toLongOrNull() ?: return null
            return TransitProtectionSession(
                destinationLabel = parts[0].ifBlank { "Casa" },
                durationMinutes = duration,
                startedAtMillis = started,
                expiresAtMillis = expires,
            )
        }
    }
}

@Keep
data class ForensicAudioRecord(
    val fileName: String,
    val filePath: String,
    val createdAtMillis: Long,
    val sizeBytes: Long,
)

