package br.com.dlpsystems.cycle.domain.model

import java.time.Instant

enum class PartnerShareCategory {
    CYCLE_PHASES,     // Fase atual do ciclo e previsões
    FERTILE_WINDOW,   // Janela fértil e ovulação
    MOOD_ENERGY,      // Humor e disposição geral
    PREGNANCY_WEEK,   // Semana gestacional e desenvolvimento do bebê
    SYMPTOMS_GENERAL, // Sintomas gerais (apenas se consentido explicitamente)
}

enum class InviteStatus {
    PENDING,
    ACCEPTED,
    REVOKED,
    EXPIRED,
}

data class PartnerInvite(
    val id: String,
    val inviteCode: String,
    val partnerEmail: String,
    val partnerName: String,
    val allowedCategories: Set<PartnerShareCategory>,
    val createdAt: Instant,
    val expiresAt: Instant,
    val status: InviteStatus,
)

data class ActivePartnerConnection(
    val id: String,
    val partnerName: String,
    val partnerEmail: String,
    val sharedCategories: Set<PartnerShareCategory>,
    val connectedAt: Instant,
)
