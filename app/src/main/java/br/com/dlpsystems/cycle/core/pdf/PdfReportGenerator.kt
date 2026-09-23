package br.com.dlpsystems.cycle.core.pdf

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import br.com.dlpsystems.cycle.core.config.AppConfig
import br.com.dlpsystems.cycle.domain.model.CyclePhase
import br.com.dlpsystems.cycle.domain.model.MenstrualCycle
import br.com.dlpsystems.cycle.domain.model.Symptom
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfReportGenerator @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun write(
        cycles: List<MenstrualCycle>,
        symptoms: Map<CyclePhase, Map<Symptom, Int>>,
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val title = Paint().apply {
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val body = Paint().apply { textSize = 12f }
        var y = 48f
        canvas.drawText("${AppConfig.displayName} — relatório para consulta", 40f, y, title)
        y += 28f
        val recent = cycles.takeLast(6)
        val lengths = recent.mapNotNull { it.cycleLength }
        val average = if (lengths.isEmpty()) null else lengths.average()
        canvas.drawText(
            "Ciclos no relatório: ${recent.size}. Média de duração: ${average?.let { "%.0f dias".format(it) } ?: "sem ciclos fechados"}.",
            40f,
            y,
            body,
        )
        y += 22f
        recent.forEach { cycle ->
            canvas.drawText(
                "Início ${cycle.startDate} · fluxo ${cycle.periodLength} dias · duração ${cycle.cycleLength ?: "em aberto"}",
                40f,
                y,
                body,
            )
            y += 18f
        }
        y += 10f
        canvas.drawText("Sintomas por fase", 40f, y, title)
        y += 24f
        symptoms.forEach { (phase, counts) ->
            val line = counts.entries.sortedByDescending { it.value }
                .joinToString { "${it.key.name.lowercase()} ${it.value}" }
                .ifBlank { "sem registros" }
            canvas.drawText("$phase: $line", 40f, y, body)
            y += 18f
        }
        document.finishPage(page)
        val file = File(context.cacheDir, "consulta-ciclo.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }
}
