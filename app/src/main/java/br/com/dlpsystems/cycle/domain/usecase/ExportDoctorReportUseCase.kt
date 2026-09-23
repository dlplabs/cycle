package br.com.dlpsystems.cycle.domain.usecase

import br.com.dlpsystems.cycle.core.pdf.PdfReportGenerator
import br.com.dlpsystems.cycle.data.remote.AnalyticsEvents
import br.com.dlpsystems.cycle.data.remote.AnalyticsService
import br.com.dlpsystems.cycle.domain.repository.BillingRepository
import br.com.dlpsystems.cycle.domain.repository.CycleRepository
import java.io.File
import javax.inject.Inject

class ExportDoctorReportUseCase @Inject constructor(
    private val cycleRepository: CycleRepository,
    private val correlateSymptoms: CorrelateSymptomsUseCase,
    private val billingRepository: BillingRepository,
    private val pdfReportGenerator: PdfReportGenerator,
    private val analyticsService: AnalyticsService,
) {
    suspend operator fun invoke(): File? {
        if (!billingRepository.isPremiumUser.value) return null
        val file = pdfReportGenerator.write(
            cycles = cycleRepository.getCycles().takeLast(6),
            symptoms = correlateSymptoms(cycleRepository.getDailyLogs()),
        )
        analyticsService.log(AnalyticsEvents.EVENT_PDF_EXPORTED, screen = "settings")
        return file
    }
}
