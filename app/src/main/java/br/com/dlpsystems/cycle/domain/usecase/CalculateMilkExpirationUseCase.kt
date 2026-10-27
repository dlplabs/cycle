package br.com.dlpsystems.cycle.domain.usecase

import br.com.dlpsystems.cycle.domain.model.Citation
import br.com.dlpsystems.cycle.domain.model.MilkExpirationStatus
import br.com.dlpsystems.cycle.domain.model.StorageCondition
import javax.inject.Inject

/**
 * Calcula o tempo limite seguro de conservação de leite materno ordenhado.
 * Baseado nas normas microbiológicas da Rede Brasileira de Bancos de Leite Humano:
 * VIEIRA, A. A. et al. Rev Bras Saude Mater Infant, 18(2): 345-354, 2018 (rBLH-Fiocruz N > 160.000 doadoras/ano).
 */
class CalculateMilkExpirationUseCase @Inject constructor() {

    operator fun invoke(
        bottleId: String,
        expressedAtEpochMs: Long,
        storageCondition: StorageCondition,
        currentEpochMs: Long = System.currentTimeMillis(),
    ): MilkExpirationStatus {
        val durationMs = when (storageCondition) {
            StorageCondition.ROOM_TEMPERATURE -> 2L * 3600 * 1000 // 2 horas
            StorageCondition.REFRIGERATOR -> 12L * 3600 * 1000     // 12 horas
            StorageCondition.FREEZER -> 15L * 24 * 3600 * 1000     // 15 dias
        }

        val expiresAt = expressedAtEpochMs + durationMs
        val remainingMs = expiresAt - currentEpochMs
        val isExpired = remainingMs <= 0
        val hoursRemaining = if (isExpired) 0.0 else remainingMs.toDouble() / (3600 * 1000)

        val citation = Citation(
            authors = "Vieira, A. A. et al. (Rede Brasileira de Bancos de Leite Humano)",
            source = "Revista Brasileira de Saúde Materno Infantil, 18(2): 345-354, 2018",
            finding = "Conservação segura de leite materno doméstico por até 12h em geladeira e 15 dias em freezer (N > 160.000 doadoras/ano).",
            url = "https://doi.org/10.1590/1806-93042018000200007",
        )

        return MilkExpirationStatus(
            bottleId = bottleId,
            expressedAtEpochMs = expressedAtEpochMs,
            expiresAtEpochMs = expiresAt,
            isExpired = isExpired,
            hoursRemaining = hoursRemaining,
            storageCondition = storageCondition,
            citation = citation,
        )
    }
}
